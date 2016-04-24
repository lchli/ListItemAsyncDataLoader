package com.lchli.loaderlibrary.example.phoneInfoList;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.lchli.loaderlib.android.ListItemAsyncDataLoader;
import com.lchli.loaderlibrary.CacheManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * this is sample for load net pictures,and show how to use memory cache with my loader.
 * Created by lchli on 2016/4/24.
 */
public class PhoneInfoLoader extends ListItemAsyncDataLoader<String, PhoneInfoResponse.PhoneInfo> {

    private static final String httpUrlRegx = "http://apis.baidu.com/apistore/mobilephoneservice/mobilephone?tel=%s";

    private static final String API_KEY = "6a9bd03563c60a052c49745809d739ca";

    @Override
    protected PhoneInfoResponse.PhoneInfo getDataLogic(Object... args) {
        //if image is too large,you can resize it here.
        String phoneNumber = (String) args[0];
        return queryFromNet(String.format(httpUrlRegx, phoneNumber));
    }

    private static PhoneInfoResponse.PhoneInfo queryFromNet(String urlstr) {
        try {
            URL url = new URL(urlstr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("apikey", API_KEY);
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();
            InputStream ins = con.getInputStream();
            byte[] buffer = new byte[10240];
            int len = -1;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = ins.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            ins.close();
            bos.flush();
            byte[] ret = bos.toByteArray();
            bos.close();
            String json = new String(ret);
            LogUtils.e("JSON:" + json);
            PhoneInfoResponse response = new Gson().fromJson(json, PhoneInfoResponse.class);
            return response != null ? response.retData : null;
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void putMemoryCache(String key, PhoneInfoResponse.PhoneInfo value) {
        CacheManager.appPhoneInfoMemCache.put(key, value);
    }

    @Override
    protected void removeMemoryCache(String key) {
        CacheManager.appPhoneInfoMemCache.remove(key);
    }

    @Override
    protected PhoneInfoResponse.PhoneInfo getMemoryCache(String cacheKey) {
        return CacheManager.appPhoneInfoMemCache.get(cacheKey);
    }
}
