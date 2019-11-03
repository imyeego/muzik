package com.liuzhao.muzik.common.promise;

public class Result<T> {
    T t;
    Action<T> action;
}
