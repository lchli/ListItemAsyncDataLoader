package com.lchli.loaderlibrary.okhttpWraper;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class OkUiThreadCallback<T> implements Callback {

    private static final String tag=OkUiThreadCallback.class.getSimpleName();

    protected static Handler mHandler = new Handler(Looper.getMainLooper());

    protected static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameter = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameter.getActualTypeArguments()[0]);
    }

    @Override
    public void onFailure(Call call, final IOException e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(e);
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            String body = response.body().string();
            final int code = response.code();
            OkhttpAppConfig.e(tag,"okResponse body string:" +body);
            OkhttpAppConfig.e(tag,"okResponse code:" + code);
            final T t = new Gson().fromJson(body, getSuperclassTypeParameter(getClass()));

            if (response.isSuccessful() && t != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSuccess(code, t);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(new Exception(Integer.toString(code)));
                    }
                });
            }
        } catch (final Exception e) {
            e.printStackTrace();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onFailure(e);
                }
            });
        }
    }


    public abstract void onSuccess(int code, T t);

    public abstract void onFailure(Throwable e);


}