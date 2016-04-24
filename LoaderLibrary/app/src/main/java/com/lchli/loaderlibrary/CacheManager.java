package com.lchli.loaderlibrary;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CacheManager {

    private static int getCacheSize() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        return maxMemory / 16;
    }

    private static final LruCache<Object, Bitmap> appBitmapMemCache = new LruCache<Object, Bitmap>(
            getCacheSize()) {
        @Override
        protected int sizeOf(Object key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };


    public static LruCache<Object, Bitmap> getBitmapMemoryCacher() {
        return appBitmapMemCache;
    }

}
