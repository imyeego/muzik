package com.liuzhao.ioc_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhongyu on 2019/1/2.
 *
 * @author Ann
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface OnClick {
    int value() default -1;
}
