package com.smart.ppx.okhttp;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class HttpClientFactory {
    private static final int CONNECT_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 20;

    private HttpClientFactory() { } //私有构造 防止实例化

    private static OkHttpClient instance;

    public static OkHttpClient getInstance() {
        if (instance == null) {
            synchronized (HttpClientFactory.class) {
                if (instance == null) {  //二次判空 防止同时创建多个
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
                    instance = builder.build();
                }
            }
        }
        return instance;
    }
}
