package com.liuzhao.muzik.common;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.liuzhao.muzik.app.Constants;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Deque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class OkioSocket {

    private String host;
    private int port;
    private int connectTimeOut = 3000;
    private int readTimeOut = 10000;
    private volatile BufferedSink mSink;
    private volatile BufferedSource mSource;
    private volatile BlockingQueue<Request> requestDeque = new LinkedBlockingDeque<>(5);
    private ExecutorService singleService;
    private ExecutorService executorService;
    private Callback callback;
    private boolean isReading = true;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Call call = (Call) msg.obj;
            switch (msg.what) {
                case 0x1e:
                    if (call != null) {
                        call.getCallback().onSuccess(call.getResponse());
                    }
                    break;
                case 0x2e:
                    if (call != null) {
                        call.getCallback().onFailure(call.getResponse());
                    }
                    break;
            }
        }
    };

    public OkioSocket(String host, int port) {
        this.host = host;
        this.port = port;

        singleService = Executors.newSingleThreadExecutor();
        singleService.execute(readRunnable);
        executorService();
    }

    private synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));
        }
        return executorService;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    @Deprecated
    public void send(String request) {
        executorService.execute(new WriteRunnable(request));

    }

    public void send(String request, Callback callback) {
        executorService.execute(new WriteRunnable(request, callback));

    }

    @Deprecated
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private Runnable readRunnable = () -> {
        Request request;
        Socket socket = null;
        Callback callback = null;

        while (isReading) {
            try {
                request = requestDeque.take();
                socket = request.getSocket();
                callback = request.getCallback();
                socket.setSoTimeout(readTimeOut);
                mSource = Okio.buffer(Okio.source(socket));
                if (request.request.contains("download")) {
                    BufferedSink sink = Okio.buffer(Okio.sink(new File(Constants.DATA_PATH)));
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    while ((len = mSource.read(bytes)) != -1) {
                        String result = new String(bytes, 0, len);
                        sink.writeUtf8(result).flush();
                    }
                    String response = "success";
                    Call call = new Call(response, callback);
                    Message message = Message.obtain(handler, 0x1e, call);
                    message.sendToTarget();

                } else {
                    String response = mSource.readUtf8();
                    if (!response.isEmpty()) {
                        Log.e("Socket", "接收数据 " + response);
                        Call call = new Call(response, callback);
                        Message message = Message.obtain(handler, 0x1e, call);
                        message.sendToTarget();
                    }
                }


            } catch (IOException | InterruptedException e) {
                if (e instanceof IOException) {
                    Message message = Message.obtain(handler, 0x2e, new Call("读取服务器数据超时，请重试...", callback));
                    message.sendToTarget();

                }
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    };

    public void close() {
        executorService.shutdown();
        isReading = false;
        singleService.shutdown();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        requestDeque.clear();
    }


    public interface Callback {
        void onSuccess(String response);
        void onFailure(String msg);
    }

    public class Call{
        String response;
        Callback callback;

        public Call(String response, Callback callback) {
            this.response = response;
            this.callback = callback;
        }

        public String getResponse() {
            return response;
        }

        public Callback getCallback() {
            return callback;
        }
    }


    class Request{
        Socket socket;
        Callback callback;
        String request;

        public Request(Socket socket, Callback callback, String request) {
            this.socket = socket;
            this.callback = callback;
            this.request = request;
        }

        public Socket getSocket() {
            return socket;
        }

        public Callback getCallback() {
            return callback;
        }
    }

    class WriteRunnable implements Runnable {

        String msg;
        Callback callback;

        public WriteRunnable(String msg) {
            this.msg = msg + "\r\n";
        }

        public WriteRunnable(String msg, Callback callback) {
            this.msg = msg + "\r\n";
            this.callback = callback;
        }

        @Override
        public void run() {
            synchronized (OkioSocket.class) {
                try {
                    Socket socket = new Socket();
                    SocketAddress address = new InetSocketAddress(host, port);
                    socket.connect(address, connectTimeOut);
                    mSink = Okio.buffer(Okio.sink(socket));
                    if (callback != null) {
                        Request request = new Request(socket, callback, msg);
                        requestDeque.offer(request);
                    }
                    if (mSink != null) {
                        Log.e("Socket", "发送数据" + msg);
                        mSink.writeUtf8(msg).flush();
                    }
                } catch (IOException e) {
                    Message message = Message.obtain(handler, 0x2e, new Call("连接服务器超时，请重试...", callback));
                    message.sendToTarget();
                    e.printStackTrace();
                }
            }

        }
    }
}
