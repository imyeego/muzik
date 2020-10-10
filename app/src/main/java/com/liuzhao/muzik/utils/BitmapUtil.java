package com.liuzhao.muzik.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import com.liuzhao.libyuv.YuvUtil;
import com.liuzhao.muzik.app.App;
import com.liuzhao.muzik.app.MyApp;
import com.liuzhao.muzik.app.TinkerApplicationLike;
import com.liuzhao.muzik.jni.YuvUtils;

/**
 * @authur : liuzhao
 * @time : 2020/10/9 3:13 PM
 * @Des :
 */
public class BitmapUtil {

    private static RenderScript rs;
    private static ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private static Type.Builder yuvType, rgbaType;
    private static Allocation in, out;

    static {
        rs = RenderScript.create(TinkerApplicationLike.getContext());
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    public static Bitmap nv21ToBitmap(byte[] data, int width, int height, int orientation) {
        if (rgbaType == null) {

            if (orientation == 90 || orientation == 270) rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(height).setY(width);
            else rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        byte[] dst;
        if (orientation == 0) {
            dst = data;
        }
        else{
            dst = new byte[width * height * 3 / 2];
            YuvUtil.rotate(data, width, height, dst, 360 - orientation);
        }
        if (yuvType == null) {
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(dst.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        }
        in.copyFrom(dst);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout;
        if (orientation == 90 || orientation == 270) {
            bmpout = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

        } else {
            bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        out.copyTo(bmpout);
        return bmpout;
    }
}
