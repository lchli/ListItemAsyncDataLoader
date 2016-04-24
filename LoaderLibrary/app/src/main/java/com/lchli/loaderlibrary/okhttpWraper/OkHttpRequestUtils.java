package com.lchli.loaderlibrary.okhttpWraper;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lchli on 2016/3/20.
 */
public class OkHttpRequestUtils {

    private static final String tag = OkHttpRequestUtils.class.getSimpleName();

    private static OkHttpClient client = OkhttpAppConfig.newOkHttpClientBuilder().build();


    private static Call getImpl(String url, Map<String, String> params, Callback callback) {
        Request request = buildGetRequest(url, params);
        Call c = client.newCall(request);
        c.enqueue(callback);
        return c;
    }

    public static Call get(String url, Map<String, String> params, OkUiThreadCallback callback) {
        return getImpl(url, params, callback);
    }

    public static Call get(String url, Map<String, String> params, OkNonUiThreadCallback callback) {
        return getImpl(url, params, callback);
    }

    public static <T> T get(String url, Map<String, String> params, Class<T> classOfT) {
        try {
            Request request = buildGetRequest(url, params);
            Call c = client.newCall(request);
            Response response = c.execute();
            Gson gson = new Gson();
            T t = gson.fromJson(response.body().string(), classOfT);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Call postImpl(String url, Map<String, String> params, Callback callback) {
        Request request = buildPostRequest(url, params);
        Call c = client.newCall(request);
        c.enqueue(callback);
        return c;
    }

    public static Call post(String url, Map<String, String> params, OkUiThreadCallback callback) {
        return postImpl(url, params, callback);
    }

    public static Call post(String url, Map<String, String> params, OkNonUiThreadCallback callback) {
        return postImpl(url, params, callback);
    }

    public static <T> T post(String url, Map<String, String> params, Class<T> classOfT) {
        try {
            Request request = buildPostRequest(url, params);
            Call c = client.newCall(request);
            Response response = c.execute();
            Gson gson = new Gson();
            T t = gson.fromJson(response.body().string(), classOfT);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static Request buildPostRequest(String url, Map<String, String> params) {
        Map<String, String> totalParams = OkhttpAppConfig.filterParams(params);
        FormBody.Builder builder = new FormBody.Builder();
        Set<Map.Entry<String, String>> keySet = totalParams.entrySet();
        for (Map.Entry<String, String> key : keySet) {
            builder.add(key.getKey(), key.getValue());
        }
        OkhttpAppConfig.e(tag, "ok post wholeParams:" + totalParams);
        return OkhttpAppConfig.newCommonRequestBuilder().url(url).post(builder.build()).build();
    }


    private static Request buildGetRequest(String url, Map<String, String> params) {
        Map<String, String> totalParams = OkhttpAppConfig.filterParams(params);
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> keySet = totalParams.entrySet();
        for (Map.Entry<String, String> key : keySet) {
            sb.append(key.getKey()).append("=").append(key.getValue()).append("&");
        }
        if (!TextUtils.isEmpty(sb.toString())) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String urlWhole = url + "?" + sb.toString();

        OkhttpAppConfig.e(tag, "ok get wholeUrl:" + urlWhole);

        return OkhttpAppConfig.newCommonRequestBuilder().url(urlWhole).build();
    }


}
