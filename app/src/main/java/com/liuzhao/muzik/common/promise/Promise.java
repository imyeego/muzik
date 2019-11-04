package com.liuzhao.muzik.common.promise;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static com.liuzhao.muzik.common.promise.Signal.THEN;

public class Promise<T> {
    private static final int QUEUE_SIZE = 5;
    private static Handler mainHandler;
    private static ExecutorService service;
    private BlockingQueue<Future<?>> handleQueue;
    private Queue<Action<?>> thenQueue;
    private Queue<Action<?>> mapQueue;

    public static <T> Promise<T> of(Callable<T> t) {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case THEN:
                            Result<T> result = (Result<T>) msg.obj;
                            result.action.then(result.t);
                            break;
                    }
                }
            };
        }

        return new Promise<>(t);
    }

    private <T> Promise(Callable<T> t){
        if (handleQueue == null) {
            handleQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        }

        if (service == null) {
            service = Executors.newCachedThreadPool();

        }
        Future<T> future = service.submit(t);
        handleQueue.offer(future);
    }

    public Promise<T> then(Action<? extends T> action) {
        if (thenQueue == null) {
            thenQueue = new LinkedList<>();
            service.execute(loop);
        }
        thenQueue.offer(action);
        return this;
    }

    public <R> Promise<R> map(Func<? extends T, R> func) {
        if (mapQueue == null) {
            mapQueue = new LinkedList<>();
        }
        T t = null;
        return new Promise<>(new Call<T, R>(func, t));
    }

    private Runnable loop = () -> {
        for (;;) {
            try {
                Future<T> future = (Future<T>) handleQueue.take();
                T t1 = future.get();
                Result<T> result = new Result<>();
                result.t = t1;
                while (!thenQueue.isEmpty()) {
                    Action<T> action = (Action<T>) thenQueue.poll();
                    result.action = action;
                    Message.obtain(mainHandler, THEN, result);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    };


}
