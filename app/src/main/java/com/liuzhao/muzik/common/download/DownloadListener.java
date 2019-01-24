package com.liuzhao.muzik.common.download;

/**
 *
 * @author liuzhao
 */
public interface DownloadListener {

    void setProgress(long downloadLength, long totalLength, boolean done);

    void setFileName(String fileName);
}
