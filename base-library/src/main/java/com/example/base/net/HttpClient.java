package com.example.base.net;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class HttpClient implements IClient {

    private static final String CHARSET = "GB2312";

    //链接超时时间
    private static final int conn_timeout= 10000;
    //读取超时
    private static final int read_timeout = 10000;
    //请求地址
    private String address;

    //请求参数
    private List<String> params;

    private HttpURLConnection conn;

    public HttpClient(String address){
        this.address = address;
    }

    private String getParams(){
        if (params == null){
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        boolean first = true ;
        for(String kv : params){
            if(first){
                first = false;
            } else{
                buffer.append("&");
            }
            buffer.append(kv);
        }
        return buffer.toString();
    }

    private  String parsRtn(InputStream is) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, CHARSET));
        StringBuilder buffer = new StringBuilder();
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if(first){
                first = false;
            }else{
                buffer.append("\n");
            }
            buffer.append(line);
        }
        return buffer.toString();
    }

    @SuppressLint("TrustAllX509TrustManager")
    private void useSSL(){
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return null;}
                public void checkClientTrusted(X509Certificate[] certs, String authType){}
                public void checkServerTrusted(X509Certificate[] certs, String authType){}
            }};
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            //ip host verify
            HostnameVerifier hv = (urlHostName, session) -> urlHostName.equals(session.getPeerHost());
            //set ip host verify
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private HttpURLConnection build(String method, String address){
        HttpURLConnection conn;
        try {
            useSSL();
            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(conn_timeout);
            conn.setReadTimeout(read_timeout);
            conn.setRequestMethod(method);
            return conn;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String get(){
        String url = address + "?" + getParams();
        this.conn = build(GET,url);
        System.out.println("-------->>>> get, url --->>> "+url);
        String result = doGet();
        System.out.println("result : "+result);
        return result;
    }

    @Override
    public String post(){
        this.conn = build(POST, address);
        System.out.println("-------->>>> post, url --->>> "+address);
        String result = doPost();
        System.out.println("result : "+result);
        return result;
    }

    private String doPost(){
        String params = getParams();
        Map<String, List<String>> map = getConnection().getRequestProperties();
        for (String key : map.keySet()){
            System.out.println(key+"->"+map.get(key));
        }
        try {
            if (params!=null) {
                System.out.println("params!=null-> "+params);
                conn.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(params.getBytes(Charset.forName("UTF-8")));
                out.flush();
                out.close();
            }
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return parsRtn(conn.getInputStream());
            } else {
                throw new Exception(conn.getResponseCode() + " "+ conn.getResponseMessage());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String doGet(){
        try {
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return parsRtn(conn.getInputStream());
            } else {
                throw new Exception(conn.getResponseCode() + " "+ conn.getResponseMessage());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HttpURLConnection getConnection() {
        return conn;
    }

    public void addHeader(String key, String val){
        conn.addRequestProperty(key, val);
    }
    public void setHeader(String key, String val){
        conn.setRequestProperty(key, val);
    }

    @Override
    public void addParam(String key, String value) {
        if (params==null){
            params = new ArrayList<>();
        }
        if(key!=null&&key.length()> 0){
            params.add(key + "=" + value);
        }
    }

    public void release(){
        if(conn !=null){
            conn.disconnect();
        }
    }

    public interface Callback {
        void complete(String result);
    }
}
