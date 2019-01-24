package com.liuzhao.muzik.utils;

import android.util.Log;

import com.liuzhao.muzik.common.download.DownloadInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.ResponseBody;

/**
 * Created by zhongyu on 2018/11/9.
 *
 * @author liuzhao
 */
public class FileUtil {

    private static String TAG = "File Log";

    public static void writeCache(ResponseBody responseBody, File file, DownloadInfo info) throws IOException {
        InputStream is = responseBody.byteStream();
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        long allLength;
        if (info.getContentLength() == 0) {
            allLength = responseBody.contentLength();
        } else {
            allLength = info.getContentLength();
        }
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = null;
        randomAccessFile = new RandomAccessFile(file, "rwd");
        channelOut = randomAccessFile.getChannel();

        MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,
                info.getReadLength(), allLength - info.getReadLength());

        byte[] buffer = new byte[1024 * 2]; //4k
        int len;
        while ((len = is.read(buffer)) != -1) {
            mappedBuffer.put(buffer, 0, len);
        }
        is.close();
        if (channelOut != null) {
            channelOut.close();
        }
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }

    }

}
