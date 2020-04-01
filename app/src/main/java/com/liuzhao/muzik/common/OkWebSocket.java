package com.liuzhao.muzik.common;

import android.util.Log;

import com.imyeego.promise.Utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class OkWebSocket {
    public static final String TAG = "OkWebSocket";
    private static OkWebSocket INSTANCE;
    public static final int TIME_OUT = 3_000;
    private OkHttpClient okHttpClient;
    private String url;
    private volatile WebSocket mWebSocket;
    private WebSocketCallback callback;
    private WebSocketListener webSocketListener;
    private Object lock = new Object();
    private boolean flag = true;
    private Request request;

    public static OkWebSocket instance() {
        if (INSTANCE == null) {
            synchronized (OkWebSocket.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OkWebSocket();
                }
            }
        }

        return INSTANCE;
    }

    private OkWebSocket() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .build();

    }


    public OkWebSocket callback(WebSocketCallback callback) {
        this.callback = callback;
        return this;
    }

    public OkWebSocket url(final String url) {
        this.url = url;
        return this;
    }

    public OkWebSocket connect() {
        return connect(url);
    }

    public OkWebSocket disconnect() {
        if (mWebSocket != null) {
            mWebSocket.close(1000, "正常断开...");
            retry(false);
        }

        return this;
    }

    public OkWebSocket connect(final String url) {
        if (mWebSocket != null) {
            Log.i(TAG, "连接已经成功建立，无需重复打开");
        }
        request = new Request.Builder().url(url).build();
        webSocketListener = new EchoWebsocketListener();
        okHttpClient.newWebSocket(request, webSocketListener);
        Utils.executorService().execute(monitor);
        return this;
    }


    private class EchoWebsocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            mWebSocket = webSocket;
            Log.i(TAG, "连接成功");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.i(TAG, "onMessage: " + text);
            Utils.getMainHandler().post(() -> {
                if (callback != null) {
                    callback.onMessage(text);
                }
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            Log.i(TAG, "onClosing: " + reason);
            retry(true);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Log.i(TAG, "onClosed: " + reason);
            mWebSocket = null;
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            Log.i(TAG, "onFailure: " + t.getMessage());
            retry(true);
        }
    }

    private void retry(boolean retry) {
        flag = retry;
        synchronized (lock) {
            mWebSocket = null;
            lock.notify();
        }
    }

    private final Runnable monitor = () -> {
        while (flag) {
            synchronized (lock) {
                try {
                    Thread.sleep(TIME_OUT);
                    if (mWebSocket != null) lock.wait();
                    if (!flag) break;
                    Log.i(TAG, "连接重试中...");
                    okHttpClient.newWebSocket(request, webSocketListener);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    public interface WebSocketCallback {
        void onMessage(String message);
    }

}
