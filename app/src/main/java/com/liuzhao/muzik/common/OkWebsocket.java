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

public class OkWebsocket {
    public static final String TAG = "OkWebsocket";
    private static OkWebsocket INSTANCE;
    private OkHttpClient okHttpClient;
    private String url;
    WebSocket mWebSocket;
    WebSocketCallback callback;

    public static OkWebsocket instance() {
        if (INSTANCE == null) {
            synchronized (OkWebsocket.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OkWebsocket();
                }
            }
        }

        return INSTANCE;
    }

    private OkWebsocket() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10_000, TimeUnit.MILLISECONDS)
                .build();

    }


    public OkWebsocket callback(WebSocketCallback callback) {
        this.callback = callback;
        return this;
    }

    public OkWebsocket url(final String url) {
        this.url = url;
        return this;
    }

    public OkWebsocket connect() {
        return connect(url);
    }

    public OkWebsocket connect(final String url) {
        Request request = new Request.Builder().url(url).build();
        EchoWebsocketListener socketListener = new EchoWebsocketListener();
        okHttpClient.newWebSocket(request, socketListener);
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
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Log.i(TAG, "onClosed: " + reason);

        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            Log.i(TAG, "onClosed: " + t.getMessage());

        }
    }

    public interface WebSocketCallback {
        void onMessage(String message);
    }

}
