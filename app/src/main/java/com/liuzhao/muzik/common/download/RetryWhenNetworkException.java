package com.liuzhao.muzik.common.download;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by zhongyu on 2018/11/12.
 *
 * @author Ann
 */
public class RetryWhenNetworkException implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private int count = 3;
    //    延迟
    private long delay = 3000;
    //    叠加延迟
    private long increaseDelay = 3000;

    public RetryWhenNetworkException() {

    }

    public RetryWhenNetworkException(int count, long delay) {
        this.count = count;
        this.delay = delay;
    }

    public RetryWhenNetworkException(int count, long delay, long increaseDelay) {
        this.count = count;
        this.delay = delay;
        this.increaseDelay = increaseDelay;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .zipWith(Observable.range(1, count + 1), (Func2<Throwable, Integer, Wrapper>) (throwable, integer) -> {
                    //压缩规则 合并后的结果是一个Observable<Wrapper>
                    return new Wrapper(throwable, integer);
                }).flatMap((Func1<Wrapper, Observable<?>>) wrapper -> {
                    //转换规则
                    if ((wrapper.throwable instanceof ConnectException
                            || wrapper.throwable instanceof SocketTimeoutException
                            || wrapper.throwable instanceof TimeoutException)
                            && wrapper.index < count + 1) { //如果超出重试次数也抛出错误，否则默认是会进入onCompleted
                        return Observable.timer(delay + (wrapper.index - 1) * increaseDelay, TimeUnit.MILLISECONDS);

                    }
                    return Observable.error(wrapper.throwable);
                });
    }

    private class Wrapper {
        private int index;
        private Throwable throwable;

        public Wrapper(Throwable throwable, int index) {
            this.index = index;
            this.throwable = throwable;
        }
    }
}
