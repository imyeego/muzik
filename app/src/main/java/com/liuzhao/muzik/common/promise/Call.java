package com.liuzhao.muzik.common.promise;

import java.util.concurrent.Callable;

public class Call<T, R> implements Callable<R> {

    Func<T, R> func;
    T t;

    public Call(Func func, T t) {
        this.func = func;
        this.t = t;
    }

    @Override
    public R call() throws Exception {
        return func.map(t);
    }
}
