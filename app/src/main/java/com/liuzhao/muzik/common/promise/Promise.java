package com.liuzhao.muzik.common.promise;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Promise<T> {
    private static Promise promise;
    private Callable<T> t;
    private static Handler mainHandler;
    private static ExecutorService service;

    public static <T> Promise<T> of(Callable<T> t) {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper()) {

            };
        }

        if (service == null) {
            service = Executors.newCachedThreadPool();
        }
        promise = new Promise(t);

        return promise;
    }

    private Promise(Callable<T> t){
        this.t = t;
        Future<T> future = service.submit(t);
    }

    public <T> Promise then(T t) {

        return promise;
    }


}
