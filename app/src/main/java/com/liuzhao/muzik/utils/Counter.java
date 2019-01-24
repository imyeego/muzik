package com.liuzhao.muzik.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author liuzhao
 */
public class Counter {
    private volatile AtomicInteger integer;
    private ActiveCounter activeCounter;

    private Counter(){
        integer = new AtomicInteger(0);
    }

    public Counter(int count) {
        this.integer = new AtomicInteger(count);
    }

    public static Counter create(){
        return new Counter();
    }

    public final int getCount(){
        return integer.get();
    }

    public ActiveCounter getCounter(){
        if (activeCounter == null)
            activeCounter = new ActiveCounter();
        return activeCounter;
    }

    public void count(){
        getCounter().count();
    }

    public void clear(){
        integer.getAndSet(0);
    }

    class ActiveCounter implements CountExecute{
        @Override
        public void count() {
            integer.getAndIncrement();
        }
    }

    interface CountExecute {
        void count();
    }
}
