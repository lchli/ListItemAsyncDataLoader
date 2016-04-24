package com.lchli.loaderlibrary.okhttpWraper;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;


import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class OkNonUiThreadCallback<T> implements Callback {

    private static final String tag=OkNonUiThreadCallback.class.getSimpleName();

    @Override
    public void onFailure(Call call, IOException e) {
        onFailure(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            Gson gson = new Gson();
            String body=response.body().string();
            T t = gson.fromJson(body, getSuperclassTypeParameter(getClass()));
            final int code = response.code();

            OkhttpAppConfig.e(tag,"okResponse body string:  " + body);
            OkhttpAppConfig.e(tag,"okResponse code:   " + code);

            if (response.isSuccessful() && t != null) {
                onSuccess(code, t);
            } else {
                onFailure(new Exception(Integer.toString(code)));
            }
        } catch (final Exception e) {
            onFailure(e);
        }
    }


    public abstract void onSuccess(int code, T t);

    public abstract void onFailure(Throwable e);

    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameter = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameter.getActualTypeArguments()[0]);
    }


}