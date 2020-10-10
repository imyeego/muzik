package com.liuzhao.libyuv;

/**
 * @authur : liuzhao
 * @time : 2020/10/9 4:35 PM
 * @Des :
 */
public class YuvUtil {

    static {
        System.loadLibrary("yuvutils");
    }


    public static native void rotate(byte[] src, int width, int height, byte[] dst, int degree);
}
