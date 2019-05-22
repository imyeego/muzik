package com.liuzhao.muzik.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Calendar;

@Aspect
public class SingleClickAspect {

    private static final long MIN_CLICK_DELAY_TIME = 1000L;
    private long lastClickTime = 0L;

    @Pointcut("execution(@com.liuzhao.muzik.annotation.SingleClick * *(..))")
    public void pointCut() {}

    @Around(value = "pointCut()")
    public void around(ProceedingJoinPoint joinPoint) {

        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
