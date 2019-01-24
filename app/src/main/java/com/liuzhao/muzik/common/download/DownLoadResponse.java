package com.liuzhao.muzik.common.download;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 *
 * @author liuzhao
 */
public class DownLoadResponse extends ResponseBody {

    private ResponseBody responseBody;
    private BufferedSource source;
    private DownloadListener listener;

    public DownLoadResponse(ResponseBody responseBody, DownloadListener listener) {
        this.responseBody = responseBody;
        this.listener = listener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (source == null){
            source = Okio.buffer(source(responseBody.source()));
        }
        return source;
    }

    private Source source(Source source){
        return new ForwardingSource(source) {
            long downloadLength = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long readLength =  super.read(sink, byteCount);
                downloadLength += readLength != -1 ? readLength : 0;
                if (listener != null) {
                    listener.setProgress(downloadLength, responseBody.contentLength(), readLength == -1);
                }
                return readLength;
            }
        };

    }
}
