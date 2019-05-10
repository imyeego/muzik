package com.liuzhao.muzik.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;

public class NetworkService extends Service {

    private static final String TAG = "NetworkService";
    private Handler workHandler;
    private HandlerThread handlerThread;
    private int i = 0;
    @Override
    public void onCreate() {
        Log.i(TAG, "started ...");
        initHandler();

        super.onCreate();
    }

    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, " " + msg.what);
            }
        };
        Schedulers.newThread().createWorker().schedulePeriodically(() -> {
            workHandler.sendEmptyMessage(++i);
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand ...");

        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        Log.i(TAG, "destroyed ...");
        i = 0;
        workHandler.removeCallbacksAndMessages(null);
        handlerThread.quitSafely();
        super.onDestroy();
    }
}
