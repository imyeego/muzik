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
    private static BlockingQueue<Future<?>> handleQueue;
    private static Queue<Action<?>> thenQueue;
    private static Queue<Object> mapQueue;

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

        if (handleQueue == null) {
            handleQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        }

        if (service == null) {
            service = Executors.newCachedThreadPool();

        }

        if (thenQueue == null) {
            thenQueue = new LinkedList<>();
        }

        return new Promise<>(t);
    }

    private <T> Promise(Callable<T> t){
        Future<T> future = service.submit(t);
        handleQueue.offer(future);
    }

    public Promise<T> then(Action<? extends T> action) {
        thenQueue.offer(action);
        return this;
    }

    public Promise<T> ui(Action<? extends T> action) {
        thenQueue.offer(action);
        return this;
    }

    public Promise<T> io(Action<? extends T> action) {
        thenQueue.offer(action);
        return this;
    }


    public <R> Promise<R> map(Func<? extends T, R> func) {
        if (mapQueue == null) {
            mapQueue = new LinkedList<>();
        }
        T t = null;
        try {
            t = (T) mapQueue.poll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Promise<>(new Call<>(func, t));
    }

    public void make() {
        service.execute(new Loop<>());

    }


    private static class Loop<T> implements Runnable {
        @Override
        public void run() {
            for (;;) {
                try {
                    Future<T> future = (Future<T>) handleQueue.take();
                    T t1 = future.get();
                    if (mapQueue != null) {
                        mapQueue.offer(t1);

                    } else {
                        Result<T> result = new Result<>();
                        result.t = t1;
                        while (!thenQueue.isEmpty()) {
                            Action<T> action = (Action<T>) thenQueue.poll();
                            result.action = action;
                            Message.obtain(mainHandler, THEN, result).sendToTarget();
                        }
                    }

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
