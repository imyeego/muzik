package com.liuzhao.muzik.common.promise;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static com.liuzhao.muzik.common.promise.Signal.THEN;

public class Promise<T> {
    private static final int QUEUE_SIZE = 5;
    private static Promise promise;
    private static Handler mainHandler;
    private static ExecutorService service;
    private static BlockingQueue<Future<?>> handleQueue;
    private static Queue<Action<?>> thenQueue;

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

        if (service == null) {
            service = Executors.newCachedThreadPool();
        }
        handleQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        thenQueue = new LinkedList<>();
        promise = new Promise(t);

        return promise;
    }

    private <T> Promise(Callable<T> t){
        Future<T> future = service.submit(t);
        handleQueue.offer(future);
    }

    public Promise<T> then(Action<? extends T> action) {
        thenQueue.offer(action);
        return promise;
    }


}
