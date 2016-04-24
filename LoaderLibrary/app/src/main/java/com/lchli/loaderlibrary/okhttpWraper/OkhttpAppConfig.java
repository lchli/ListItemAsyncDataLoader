package com.lchli.loaderlibrary.okhttpWraper;


import com.apkfuns.logutils.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by lichenghang on 2016/4/21.
 */
public final class OkhttpAppConfig {


    private static final int CONNECT_TIME_OUT = 10;
    private static final int READ_TIME_OUT = 30;
    private static final int WRITE_TIME_OUT = 30;

    public static OkHttpClient.Builder newOkHttpClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS);
    }

    public static Request.Builder newCommonRequestBuilder() {

        return new Request.Builder();
                //.addHeader("User-Agent", "android")
               // .addHeader("Accept-Encoding", "gzip, deflate");

    }

    public static Map<String, String> filterParams(Map<String, String> params) {
        Map<String, String> commonParams = new HashMap<>();
       // commonParams.put("appName", "lottery");
        if (params != null) {
            commonParams.putAll(params);
        }
        return commonParams;
    }


    public static void e(String tag, String msg) {
        LogUtils.e(msg);
    }
}
