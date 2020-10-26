package com.liuzhao.muzik.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.liuzhao.muzik.R;

/**
 * @authur : liuzhao
 * @time : 2020/10/12 5:11 PM
 * @Des :
 */
public class BootService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForeground(this);
//        }
        // stop self to clear the notification
//        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("BootService", "destroy notification");
        stopForeground(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void startForeground(Service context) {
        String channelName = context.getString(R.string.app_name);
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        NotificationChannel channel = new NotificationChannel("BootService", channelName, importance);
        channel.setDescription("description");
        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "BootService");
        builder.setContentTitle("程序销毁监控"); //设置通知标题
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        context.startForeground(12, builder.build());
    }
}
