package com.lchli.loaderlibrary.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lchli.loaderlib.android.ListItemAsyncDataLoader;
import com.lchli.loaderlibrary.CacheManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * this is sample for load net pictures,and show how to use memory cache with my loader.
 * Created by lchli on 2016/4/24.
 */
public class NetImageLoader extends ListItemAsyncDataLoader<String, Bitmap> {


    @Override
    protected Bitmap getDataLogic(Object... args) {
        //if image is too large,you can resize it here.
        String url = (String) args[0];
        byte[] bmpbytes = downloadFile(url);
        if (bmpbytes != null) {
            return BitmapFactory.decodeByteArray(bmpbytes, 0, bmpbytes.length);
        }
        return null;
    }

    private static byte[] downloadFile(String urlstr) {
        try {
            URL url = new URL(urlstr);
            URLConnection con = url.openConnection();
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
            return ret;
        } catch (Error | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void putMemoryCache(String key, Bitmap value) {
        CacheManager.getBitmapMemoryCacher().put(key, value);
    }

    @Override
    protected void removeMemoryCache(String key) {
        CacheManager.getBitmapMemoryCacher().remove(key);
    }

    @Override
    protected Bitmap getMemoryCache(String cacheKey) {
        return CacheManager.getBitmapMemoryCacher().get(cacheKey);
    }
}
