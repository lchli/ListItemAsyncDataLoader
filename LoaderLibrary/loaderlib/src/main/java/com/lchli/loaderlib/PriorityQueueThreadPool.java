package com.lchli.loaderlib;


import com.lchli.loaderlib.android.LogHelper;

import java.lang.reflect.Method;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PriorityQueueThreadPool {

    private static final String tag = PriorityQueueThreadPool.class.getSimpleName();

    private final PriorityBlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<>();

    private final ThreadPoolExecutor mPool;

    private Method removeAtMethod;

    private int mQueueCapacity;


    public PriorityQueueThreadPool(int threadMounts, int queueCapacity) {
        mQueueCapacity = queueCapacity;
        mPool = new ThreadPoolExecutor(threadMounts, threadMounts * 2, 5000,
                TimeUnit.MILLISECONDS, workQueue);
        try {
            removeAtMethod = workQueue.getClass().getDeclaredMethod("removeAt", int.class);
            removeAtMethod.setAccessible(true);
            LogHelper.e(tag, "find removeAt method success.>>>>>>>>>>>>>>>>>>>>>>>>>");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void cancelAllWaitingTask() {
        workQueue.clear();
        LogHelper.e(tag, "cancelAllWaitingTask");

    }

    public void shutdownNow() {
        mPool.shutdownNow();
        workQueue.clear();
        LogHelper.e(tag, "shutdownNow");

    }

    @Override
    protected void finalize() throws Throwable {
        shutdownNow();
        super.finalize();
    }

    public void execute(Runnable r) {
        LogHelper.w(tag, "workQueue size:" + workQueue.size());
        if (workQueue.contains(r)) {
            return;
        }
        if (workQueue.size() >= mQueueCapacity) {
            try {
                removeAt(workQueue.size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPool.execute(r);
    }

    private void removeAt(int index) throws Exception {
        if (removeAtMethod != null) {
            removeAtMethod.invoke(workQueue, index);
        }

    }
}
