package com.liuzhao.muzik.jni;

/**
 * @authur : liuzhao
 * @time : 2020/9/28 4:19 PM
 * @Des :
 */
public class YuvUtils {

    static {
        System.loadLibrary("yuvutil");
    }


    public static native void rotate(byte[] src, int width, int height, byte[] dst, int degree);
}
