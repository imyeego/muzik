package com.liuzhao.muzik.common.promise;

public interface Func<T, R> {
    R map(T t);
}
