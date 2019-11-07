package com.liuzhao.muzik.model;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicReference;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SumTask extends RecursiveTask<Integer> {

    private ExecutorService service = Executors.newCachedThreadPool();
    @Override
    protected Integer compute() {
        Future future = service.submit(ca1);
        return null;
    }

    // 等凉菜
    Callable ca1 = () -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "凉菜准备完毕";
    };

}
