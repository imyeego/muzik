package com.liuzhao.muzik.common.nio;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NioSocket {

    private static final String TAG = "NioSocket";
    private static final long HEARTBEAT_PERIOD = 3_000;
    private static final long CONNECT_TIMEOUT = 5_000;
    private Queue<Response> responseQueue;
    private Queue<String> tcpRequestQueue;
    private Queue<String> udpRequestQueue;
    private InetSocketAddress udpAddress = new InetSocketAddress("172.16.41.35", 8888);
    private InetSocketAddress tcpAddress;
    private InetSocketAddress localUdpAddress;
    private ExecutorService service;
    private ScheduledExecutorService scheduledService;
    private ScheduledFuture scheduledFuture;
    private Selector selector;
    private SocketChannel socketChannel;
    private DatagramChannel datagramChannel;
    private static NioSocket instance;
    private volatile boolean udpConnected, isRequireHearbeatWithUdp, tcpConnected;
    private boolean timerSetted, timeOuted;
    private Handler handler;


    private NioSocket() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        responseQueue = new LinkedList<>();
        tcpRequestQueue = new LinkedList<>();
        udpRequestQueue = new LinkedList<>();
        service = Executors.newFixedThreadPool(4);
        scheduledService = Executors.newScheduledThreadPool(2);
    }

    public static NioSocket instance() {
        if (instance == null) {
            synchronized (NioSocket.class) {
                if (instance == null) {
                    instance = new NioSocket();
                }
            }
        }

        return instance;
    }


    /**
      * @authur : liuzhao
      * @time : 2020/9/7 10:06 PM
      * @param : 
      * @return : 
      * @des:
      */
    public NioSocket requireHearbeatWithUdp(boolean is) {
        this.isRequireHearbeatWithUdp = is;

        return instance;
    }



    public NioSocket setHostAndPort(String host, int port) {
        
        tcpAddress = new InetSocketAddress(host, port);
        return instance;
    }


    public NioSocket build() {
        try {

            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        service.execute(connectTask);
        service.execute(nioTcp);
        if (isRequireHearbeatWithUdp) {
            service.execute(nioUdp);
            scheduledService.scheduleWithFixedDelay(udpHeartBeat, 1_000, HEARTBEAT_PERIOD, TimeUnit.MILLISECONDS);
        } else {
            scheduledService.scheduleWithFixedDelay(tcpHeartBeat, 1_000, HEARTBEAT_PERIOD, TimeUnit.MILLISECONDS);

        }
        return instance;
    }

    private void connectTcp(){
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(tcpAddress);
            while (!socketChannel.finishConnect()) {
                log("正在连接");
            }
            tcpConnected = true;
            log("Tcp连接成功");
            socketChannel.socket().setKeepAlive(true);
            int ops = socketChannel.validOps();
            SelectionKey key = socketChannel.register(selector, ops);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void connectUdp() {
        if (udpConnected) return;
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            udpConnected = true;
        } catch (IOException e) {
//            connectUdp();
            e.printStackTrace();
        }
    }

    private Runnable delayedTask = () -> {
        log("udp 超时...");
    };

    private Runnable connectTask = () -> {
        while (true) {
            if (!tcpConnected) {
                connectTcp();
            }
        }


    };

    private Runnable nioTcp = () -> {
        int var = 0;
        do {
            try {
                var = selector.selectNow();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (var != 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                        try {
                            if (socketChannel.read(byteBuffer) != -1) {
//                                long currentTime = System.currentTimeMillis();
                                String result = new String(byteBuffer.array()).trim();
//                                if (currentTime - Long.parseLong(result) <= HEARTBEAT_PERIOD) {
//                                    handler.removeCallbacks(delayedConnectTask);
//                                }
                                log("Message Received TCP: " + result);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    if (selectionKey.isWritable()) {
                        if (tcpRequestQueue.peek() != null) {
                            byte[] request = tcpRequestQueue.poll().getBytes();
                            ByteBuffer buffer = ByteBuffer.wrap(request);

                            try {
                                socketChannel.write(buffer);
                                log("sending tcp: " + new String(request));
                            } catch (IOException e) {
                                tcpConnected = false;
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!selectionKey.isValid()) {
                        tcpConnected = false;
                    }


                }
            }

        } while (true);

    };




    private Runnable nioUdp = () -> {
        connectUdp();
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (datagramChannel != null) {
            int ops = datagramChannel.validOps();
            try {
                SelectionKey key = datagramChannel.register(selector, ops);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
            int var = 0;
            while (true) {
                try {
                    var = selector.selectNow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (var != 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isReadable()) {
                            DatagramChannel socketChannel = (DatagramChannel) selectionKey.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                            try {
                                socketChannel.receive(byteBuffer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (byteBuffer.position() != 0) {
                                long currentTime = System.currentTimeMillis();
                                String result = new String(byteBuffer.array()).trim();
                                if (currentTime - Long.parseLong(result) <= HEARTBEAT_PERIOD) {
                                    handler.removeCallbacks(delayedTask);
                                    log("udp 连接正常");
                                }
                                log("Message Received UDP: " + result);
                            }

                        }


                        if (selectionKey.isWritable()) {
                            if (udpRequestQueue.peek() != null) {
                                byte[] request = (udpRequestQueue.poll()).getBytes();
                                ByteBuffer buffer = ByteBuffer.wrap(request);
                                try {
                                    startTimer();
                                    datagramChannel.send(buffer, udpAddress);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                log("sending udp: " + new String(request));
                            }
                        }
                    }
                }
            }

        }
    };

    private Runnable udpHeartBeat = () -> udpRequestQueue.offer(String.valueOf(System.currentTimeMillis()));

    private Runnable tcpHeartBeat = () -> {
        if (tcpConnected)
            tcpRequestQueue.offer(String.valueOf(System.currentTimeMillis()));
    };


    private void startTimer() {
        handler.postDelayed(delayedTask, HEARTBEAT_PERIOD);

    }

    private void log(String message) {
        Log.e(TAG, message);
    }

    class Response {
        SocketAddress address;
        String message;
    }
}
