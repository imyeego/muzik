package com.liuzhao.muzik.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.liuzhao.muzik.R;
import com.liuzhao.muzik.common.OkHttpUtil;

import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;

public class NetworkService extends Service {

    private static final String TAG = "NetworkService";
    private Handler workHandler;
    private HandlerThread handlerThread;
    private int i = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "started ...");
        initHandler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            BootService.startForeground(this);

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setForegroundService() {
        //设定的通知渠道名称
        String channelName = getString(R.string.app_name);
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        NotificationChannel channel = new NotificationChannel(TAG, channelName, importance);
        channel.setDescription("description");
        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, TAG);
        builder.setContentTitle("程序销毁监控"); //设置通知标题
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(12, builder.build());
    }

    private void initHandler() {
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, " " + msg.what);
            }
        };
        Schedulers.newThread().createWorker().schedulePeriodically(() -> {
            testToken();
            workHandler.sendEmptyMessage(++i);
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void testToken() {

        OkHttpUtil.getInstance().get("http://172.16.41.110:8088/user/liuzhao", new OkHttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.e("TokenInterceptor", response);
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
            }
        });
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
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "程序关闭");
        stopSelf();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.e(TAG, "destroyed ...");
        i = 0;
        workHandler.removeCallbacksAndMessages(null);
        handlerThread.quitSafely();
        stopForeground(true);
    }
}
