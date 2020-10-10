//
// Created by mac on 2020/9/28.
//
#include <stdio.h>
#include <jni.h>
#include <opencl-c-base.h>
#include "libyuv.h"

void nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *dst_i420_data);
void I420ToNv21(jbyte *src_i420_data, jint width, jint height, jbyte *src_nv21_data);

void rotateI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint degree);

JNIEXPORT void JNICALL
Java_com_liuzhao_muzik_jni_YuvUtils_rotate(JNIEnv *env, jclass jcls,
        jbyteArray nv21Src, jint width,
        jint height, jbyteArray nv21Dst,jint degree) {
    jbyte *src_i420_data = (*env)->GetByteArrayElements(env, nv21Src, NULL);

    jbyte *i420_data = (jbyte *) malloc(sizeof(jbyte) * width * height * 3 / 2);

    nv21ToI420(nv21Src, width, height, i420_data);

    // rotate i420
    jbyte *i420_rotate_data = (jbyte *) malloc(sizeof(jbyte) * width * height * 3 / 2);
    rotateI420(src_i420_data, width, height, i420_rotate_data, degree);

    // convert to nv21
    if (degree == 90 || degree == 270) {
        jint tmp = width;
        width = height;
        height = tmp;
    }
    jbyte *dst_nv21_data = (*env)->GetByteArrayElements(env, nv21Dst, NULL);

    I420ToNv21(i420_rotate_data, width, height, dst_nv21_data);
    (*env)->ReleaseByteArrayElements(env, nv21Dst, dst_nv21_data, 0);

    // release
    free(i420_rotate_data);

}

void nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *dst_i420_data) {
    jbyte *src_yplane = src_nv21_data;
    jbyte *src_uvplane = src_nv21_data + width * height;
    jbyte *dst_yplane = dst_i420_data;
    jbyte *dst_uplane = dst_i420_data + width * height;
    jbyte *dst_vplane = dst_uplane + (width * height / 4);
    NV21ToI420(
            (const uint8_t *) src_yplane, width,
            (const uint8_t *) src_uvplane, width,
            (uint8_t *) dst_yplane, width,
            (uint8_t *) dst_uplane, width / 2,
            (uint8_t *) dst_vplane, width / 2,
            width, height);

    free(src_nv21_data);

}

void I420ToNv21(jbyte *src_i420_data, jint width, jint height, jbyte *src_nv21_data) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_nv21_y_data = src_nv21_data;
    jbyte *src_nv21_uv_data = src_nv21_data + src_y_size;

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;


    I420ToNV21(
            (const uint8_t *) src_i420_y_data, width,
            (const uint8_t *) src_i420_u_data, width >> 1,
            (const uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) src_nv21_y_data, width,
            (uint8_t *) src_nv21_uv_data, width,
            width, height);
}

void rotateI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint degree) {
    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    //要注意这里的width和height在旋转之后是相反的
    if (degree == kRotate90 || degree == kRotate270) {
        I420Rotate((const uint8_t *) src_i420_y_data, width,
                   (const uint8_t *) src_i420_u_data, width >> 1,
                   (const uint8_t *) src_i420_v_data, width >> 1,
                   (uint8_t *) dst_i420_y_data, height,
                   (uint8_t *) dst_i420_u_data, height >> 1,
                   (uint8_t *) dst_i420_v_data, height >> 1,
                   width, height,
                   (RotationModeEnum)degree);
    } else {
        I420Rotate((const uint8_t *) src_i420_y_data, width,
                           (const uint8_t *) src_i420_u_data, width >> 1,
                           (const uint8_t *) src_i420_v_data, width >> 1,
                           (uint8_t *) dst_i420_y_data, width,
                           (uint8_t *) dst_i420_u_data, width >> 1,
                           (uint8_t *) dst_i420_v_data, width >> 1,
                           width, height,
                           degree);
    }
}

