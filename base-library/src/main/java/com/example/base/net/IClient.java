package com.example.base.net;

import java.net.HttpURLConnection;

public interface IClient {
    String GET = "GET";
    String POST = "POST";
    String get();
    String post();
    HttpURLConnection getConnection();
    void addHeader(String key, String value);
    void setHeader(String key, String value);
    void addParam(String key, String value);
    void release();
}
