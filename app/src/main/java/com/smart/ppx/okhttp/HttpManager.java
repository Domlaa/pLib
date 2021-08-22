
package com.smart.ppx.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.smart.ppx.Config;
import com.smart.ppx.MainActivity;
import com.smart.ppx.MyApplication;
import com.smart.ppx.bean.Result;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@SuppressLint("StaticFieldLeak")
public class HttpManager{


    private static final String TAG = "HttpManager--->>>";

    private Context context;
    private static HttpManager instance;// 单例

    private HttpManager(Context context) {  // 私有构造
        this.context = context;
    }


    public static final String KEY_TOKEN = "token";
    public static final String KEY_COOKIE = "cookie";


    public static HttpManager getInstance() {
        if (instance == null) {
            synchronized (HttpManager.class) {
                if (instance == null) {
                    instance = new HttpManager(MyApplication.getInstance());
                }
            }
        }
        return instance;
    }


    /**
     * json : application/json
     * xml : application/xml
     * png : image/png
     * jpg : image/jpeg
     * gif : image/gif
     */
    public static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");



    //post
    public void submit(final String url, final JSONObject request, final IResponseListener listener) {
        new AsyncTask<Void, Void, Exception>() {
            String result;
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Map<String,String> header = request.getObject("header",Map.class);
                    OkHttpClient client = HttpClientFactory.getInstance();
                    if (client == null) {
                        return new Exception(TAG + ".post  AsyncTask.doInBackground  client == null >> return;");
                    }
                    String body = JSON.toJSONString(request);
                    Log.d(TAG, "post  url = " + url + "\n request = \n" + body);

                    RequestBody requestBody = RequestBody.create(TYPE_JSON, body);
                    Request.Builder builder = new Request.Builder();
                    if (header!=null){
                        for (Map.Entry<String, String> entry : header.entrySet()) {
                            builder.addHeader(entry.getKey(),entry.getValue());
                        }
                    }
                    builder.url(url).post(requestBody);
                    result = getResponseJson(client,builder.build());
                    //获取到的数据 result
                    Log.d(TAG, "\n 返回结果 = \n" + result + "\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
                } catch (Exception e) {
                    Log.e(TAG, "post  AsyncTask.doInBackground  try {  result = getResponseJson(..." +
                            "} catch (Exception e) {\n" + e.getMessage());
                    return e;
                }
                return null;
            }
            @Override
            protected void onPostExecute(Exception exception) {
                super.onPostExecute(exception);
                listener.finished();
                if (exception == null) {
                    try {
                        Result res = JSONObject.parseObject(result, Result.class);
                        listener.onSuccessful(res);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i(TAG, result+"\n->解析json失败：\n" + e.getMessage());
                        listener.onFailure(500, "解析返回数据失败");
                    }
                } else {
                    Log.i(TAG, "请求失败！" + exception.getMessage());
                    listener.onFailure(500, "请求出错");
                }
            }
        }.execute(); //开始执行
    }

    public void gets(final String url, final IResponseListener listener) {
        Log.i(TAG, "get: url >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + url);
        new AsyncTask<Void, Void, Exception>() {
            String result;
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    OkHttpClient client = HttpClientFactory.getInstance();
                    if (client == null) {
                        return new Exception(TAG + ".post  AsyncTask.doInBackground  client == null >> return;");
                    }
                    String value = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
                    Request request = new Request.Builder()
                            .addHeader("User-Agent", value)
                            .url(url)
                            .build();

                    result = getResponseJson(client, request);

                    //获取到的数据 result
                   // Log.d(TAG, "\n 返回结果 = \n" + result + "\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");

                    MainActivity.print(Config.TAG, result);

                    Log.i(TAG, "doInBackground: \n \n");

                } catch (Exception e) {
                    Log.e(TAG, "post  AsyncTask.doInBackground  try {  result = getResponseJson(..." +
                            "} catch (Exception e) {\n" + e.getMessage());
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception exception) {
                super.onPostExecute(exception);
                listener.finished();
                if (exception == null) {
                    try {
                        Result res = new Result();
                        res.setData(result);
                        listener.onSuccessful(res);  //   将结果返回
                    } catch (JSONException e) {
                        Log.i(TAG, "解析json失败！" + e.getMessage());
                        listener.onFailure(400, "解析返回数据失败");
                    }
                } else {
                    Log.i(TAG, "请求失败！" + exception.getMessage());
                    listener.onFailure(500, "请求出错");
                }
            }
        }.execute(); //开始执行
    }



    public void getWithId(final String id, final IResponseListener listener) {
        new AsyncTask<Void, Void, Exception>() {
            String result;
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    OkHttpClient client = HttpClientFactory.getInstance();
                    if (client == null) {
                        return new Exception(TAG + ".post  AsyncTask.doInBackground  client == null >> return;");
                    }

                    /**
                     * headers = {
                     *     'User-Agent': 'ttnet okhttp/3.10.0.2',
                     *     'Host': 'i-lq.snssdk.com',
                     *     'Connection': 'Keep-Alive'
                     * }
                     *
                     * param = {
                     *     'cell_id': '6940625278292990212',
                     *     'aid': '1319',
                     *     'app_name': 'super',
                     * }
                     */

                    HttpUrl.Builder urlBuilder =HttpUrl.parse("https://i-lq.snssdk.com/bds/cell/cell_comment/")
                            .newBuilder();
                    urlBuilder.addQueryParameter("cell_id", id);
                    urlBuilder.addQueryParameter("aid", "1319");
                    urlBuilder.addQueryParameter("app_name", "super");
                    HttpUrl url = urlBuilder.build();

                    //RequestBody requestBody = RequestBody.create(TYPE_JSON, "body");

                    Request request = new Request.Builder()
                            .addHeader("User-Agent", "ttnet okhttp/3.10.0.2")
                            .addHeader("Host", "i-lq.snssdk.com")
                            .addHeader("Connection", "i-lq.Keep-Alive.com")
                            .url(url)
                            .build();

                    Log.i(TAG, "get: url >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + url);


                    result = getResponseJson(client, request);

                    //获取到的数据 result
                    // Log.d(TAG, "\n 返回结果 = \n" + result + "\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");

                    MainActivity.print(Config.TAG, result);

                    Log.i(TAG, "doInBackground: \n \n");

                } catch (Exception e) {
                    Log.e(TAG, "post  AsyncTask.doInBackground  try {  result = getResponseJson(..." +
                            "} catch (Exception e) {\n" + e.getMessage());
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception exception) {
                super.onPostExecute(exception);
                listener.finished();
                if (exception == null) {
                    try {
                        Result res = new Result();
                        res.setData(result);
                        listener.onSuccessful(res);  //   将结果返回
                    } catch (JSONException e) {
                        Log.i(TAG, "解析json失败！" + e.getMessage());
                        listener.onFailure(400, "解析返回数据失败");
                    }
                } else {
                    Log.i(TAG, "请求失败！" + exception.getMessage());
                    listener.onFailure(500, "请求出错");
                }
            }
        }.execute(); //开始执行
    }

    public void get(final String url, final IResponseListener listener) {
        Log.i(TAG, "get: url >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + url);
        new AsyncTask<Void, Void, Exception>() {
            String result;
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    OkHttpClient client = HttpClientFactory.getInstance();
                    if (client == null) {
                        return new Exception(TAG + ".post  AsyncTask.doInBackground  client == null >> return;");
                    }
                    result = getResponseJson(client, new Request.Builder().url(url).build()); //get

                    //获取到的数据 result
                    Log.d(TAG, "\n 返回结果 = \n" + result + "\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");

                } catch (Exception e) {
                    Log.e(TAG, "post  AsyncTask.doInBackground  try {  result = getResponseJson(..." +
                            "} catch (Exception e) {\n" + e.getMessage());
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception exception) {
                super.onPostExecute(exception);
                listener.finished();
                if (exception == null) {
                    try {
                        Result res = JSON.parseObject(result, Result.class);
                        listener.onSuccessful(res);  //   将结果返回
                    } catch (JSONException e) {
                        Log.i(TAG, "解析json失败！" + e.getMessage());
                        listener.onFailure(400, "解析返回数据失败");
                    }
                } else {
                    Log.i(TAG, "请求失败！" + exception.getMessage());
                    listener.onFailure(500, "请求出错");
                }
            }
        }.execute(); //开始执行
    }

    /**
     * @param client  http端
     * @param request 发送的请求
     * @return
     * @throws Exception
     */
    private String getResponseJson(OkHttpClient client, Request request) throws Exception {
        if (client == null || request == null) {
            Log.e(TAG, "getResponseJson  client == null || request == null >> return null;");
            return null;
        }
        Response response = client.newCall(request).execute();
        return response.isSuccessful() ? response.body().string() : null;
    }


}