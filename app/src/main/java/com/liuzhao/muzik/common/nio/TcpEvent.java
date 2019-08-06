package com.liuzhao.muzik.common.nio;

public interface TcpEvent {

    void connect();

    void read();

    void write();

    void caughtException();
}
