package com.lchli.loaderlib.android;

import android.os.Handler;
import android.os.Looper;

import com.lchli.loaderlib.ListItem;
import com.lchli.loaderlib.PriorityQueueThreadPool;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public abstract class ListItemAsyncDataLoader<K, V> {

    private static final String tag = ListItemAsyncDataLoader.class
            .getSimpleName();

    private static final int HALF_PROCESSORS = Runtime.getRuntime()
            .availableProcessors() / 2;

    private static final int THREAD_AMOUNT_DEFAULT = HALF_PROCESSORS < 1 ? 1
            : HALF_PROCESSORS;

    private static final int TASK_QUEUE_CAPACITY = 300;

    private static final PriorityQueueThreadPool pool = new PriorityQueueThreadPool(THREAD_AMOUNT_DEFAULT, TASK_QUEUE_CAPACITY);

    private Map<MyWeakReference<ListItem<V>>, String> records = new HashMap<>();

    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    /**
     * if you want use cache,please implements it in subclass.
     */
    protected V getMemoryCache(K cacheKey) {
        return null;
    }


    /**
     * if you want use cache,please implements it in subclass.
     *
     * @param key
     * @param value
     */
    protected void putMemoryCache(K key, V value) {

    }


    /**
     * if you want use cache,please implements it in subclass.
     *
     * @param key
     */
    protected void removeMemoryCache(K key) {

    }

    //

    /**
     * if you want use cache,please implements it in subclass.
     *
     * @param key
     * @return
     */
    protected V getDiskCache(K key) {
        return null;
    }

    /**
     * if you want use cache,please implements it in subclass.
     *
     * @param key
     * @param value
     */
    protected void putDiskCache(K key, V value) {

    }


    public void load(K cacheKey, int position, ListItem<V> listItem, Object... args) {
        final String viewTag = String.format("%s_%d", listItem.toString(), position);
        final MyWeakReference<ListItem<V>> listItemRef = new MyWeakReference<>(listItem);
        LogHelper.e(tag, "isrepeat:" + (records.get(listItemRef) != null));
        records.put(listItemRef, viewTag);

        V cache = getMemoryCache(cacheKey);
        if (isValueValid(cache)) {
            bindData(cache, viewTag, listItemRef);
            return;
        }
        removeMemoryCache(cacheKey);
        Task newTask = new Task(cacheKey, viewTag, listItemRef, args);
        pool.execute(newTask);

    }

    /**
     * get real data in async thread,such as from net.etc.
     *
     * @param args
     * @return
     */
    protected abstract V getDataLogic(Object... args);

    /**
     * check if value is valid,you can override it for your own purpose.
     *
     * @param value
     * @return
     */
    protected boolean isValueValid(V value) {
        return value != null;
    }


    private void bindData(final V value, final Object viewTag, final MyWeakReference<ListItem<V>> listItemRef) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (viewTag.equals(records.get(listItemRef))) {
                    final ListItem<V> listItem = listItemRef.get();
                    if (listItem != null) {
                        listItem.bindData(value);
                    }
                }


            }
        });

    }

    private void loadFail(final Object viewTag, final MyWeakReference<ListItem<V>> listItemRef) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (viewTag.equals(records.get(listItemRef))) {
                    final ListItem<V> listItem = listItemRef.get();
                    if (listItem != null) {
                        listItem.onLoadFail();
                    }
                }


            }
        });
    }

    private class Task implements Runnable, Comparable<Task> {
        private K key;
        private Object[] args;
        private String viewTag;
        private MyWeakReference<ListItem<V>> listItemRef;
        private long commitTime;


        public Task(K key, String viewTag, MyWeakReference<ListItem<V>> listItemRef, Object... args) {
            super();
            this.key = key;
            this.args = args;
            this.viewTag = viewTag;
            this.listItemRef = listItemRef;
            commitTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            /**
             * recheck memory cache.
             */
            V cache = getMemoryCache(key);
            if (isValueValid(cache)) {
                bindData(cache, viewTag, listItemRef);
                return;
            }

            /**
             * check disk cache.
             */
            V diskCache = getDiskCache(key);
            if (isValueValid(diskCache)) {
                putMemoryCache(key, diskCache);
                bindData(diskCache, viewTag, listItemRef);
                return;
            }
            /**
             * get from net.
             */

            V v = getDataLogic(args);
            if (isValueValid(v)) {
                putMemoryCache(key, v);
                putDiskCache(key, v);
                bindData(v, viewTag, listItemRef);
            } else {
                loadFail(viewTag, listItemRef);
            }

        }


        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (o == this)
                return true;
            if (o.getClass() != this.getClass())
                return false;
            Task input = (Task) o;
            return this.viewTag.equals(input.viewTag);
        }

        @Override
        public int hashCode() {
            return this.viewTag.hashCode();
        }


        @Override
        public int compareTo(Task another) {
            if (this.commitTime > another.commitTime) {
                return -1;
            }
            if (this.commitTime < another.commitTime) {
                return 1;
            }
            return 0;
        }
    }


    private void destroy() {
        pool.shutdownNow();
        records.clear();
        cancelAllWaitingTask();
    }


    private void cancelAllWaitingTask() {
        pool.cancelAllWaitingTask();
    }


    private static class MyWeakReference<T> extends WeakReference<T> {

        public MyWeakReference(T r) {
            super(r);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (o instanceof MyWeakReference) {
                MyWeakReference other = (MyWeakReference) o;
                Object otherOrgin = other.get();
                Object thisOrgin = this.get();
                if (otherOrgin == null || thisOrgin == null) {
                    return false;
                } else {
                    return thisOrgin.equals(otherOrgin);
                }

            } else {
                return false;
            }

        }

        @Override
        public int hashCode() {
            Object thisOrgin = this.get();
            if (thisOrgin != null) {
                return thisOrgin.hashCode() + 37;
            }
            return super.hashCode();
        }
    }
}
