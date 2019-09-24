package com.liuzhao.muzik.common.download;

import android.os.Environment;
import android.util.Log;

import com.liuzhao.muzik.common.ApiService;
import com.liuzhao.muzik.common.LoggingInterceptor;
import com.liuzhao.muzik.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhongyu on 2018/11/9.
 *
 * @author liuzhao
 */
public class DownloadManager implements DownloadListener {
    private static String TAG = "Download Manager";
//    public static final String DOWNLOAD_URL = "http://imtt.dd.qq.com/16891/89E1C87A75EB3E1221F2CDE47A60824A.apk?fsname=com.snda.wifilocating_4.2.62_3192.apk&csr=1bbd";
    public static final String DOWNLOAD_URL = "http://172.16.41.186:8080/newsfyz/dataApi/androidDB?schoolcode=86.63.27.003&taskcode=1";
    public static final File DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private Subscription subscription;
    private ObserverProgress observerProgress;
    private ApiService service;
    private DownloadInfo info;
    private File file;
    private long currentRead;

    private DownloadManager() {
        info = new DownloadInfo();
    }

    public static DownloadManager getInstance() {
        return Holder.manager;
    }

    public static class Holder {
        private static DownloadManager manager = new DownloadManager();
    }


    @Override
    public void setProgress(long downloadLength, long totalLength, boolean done) {
        if (info.getContentLength() > totalLength) {
            downloadLength = downloadLength + (info.getContentLength() - totalLength);
        } else {
            info.setContentLength(totalLength);
        }
        info.setReadLength(downloadLength);
        currentRead = info.getReadLength();
        Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
            if (observerProgress != null) {
                observerProgress.progressChanged(currentRead, info.getContentLength(), done);
            }
        });

    }

    @Override
    public void setFileName(String fileName) {
        file = new File(DOWNLOAD_PATH, fileName);
        info.setSavePath(file.getAbsolutePath());
    }

    public void start() {
        info.setUrl(DOWNLOAD_URL);
        final DownloadInterceptor interceptor = new DownloadInterceptor(this);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .addNetworkInterceptor(new LoggingInterceptor(this))
                .addInterceptor(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(getBasUrl(DOWNLOAD_URL))
                .build();
        service = retrofit.create(ApiService.class);
        download();
    }

    /**
     * 暂停下载
     */
    public void pause() {
        if (subscription != null)
            subscription.unsubscribe();
    }

    /**
     * 继续下载
     */
    public void reStart() {
        download();
    }

    private void download() {
        subscription = service.download("bytes=" + info.getReadLength() + "-", info.getUrl())
                /*指定线程*/
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .retryWhen(new RetryWhenNetworkException())
                /* 读取下载写入文件，并把ResponseBody转成DownInfo */
                .map(responseBody -> {
                    try {
                        //写入文件
                        Log.e(TAG, "" + info.getReadLength() + " " + info.getContentLength());
                        FileUtil.writeCache(responseBody, new File(info.getSavePath()), info);
                    } catch (IOException e) {
                        Log.e("异常:", e.toString());
                    }
                    return info;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DownloadInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.e("下载", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("下载", "onError" + e.toString());
                    }

                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        Log.e("下载", "onNext");
                    }
                });

    }

    public void setObserverProgress(ObserverProgress observerProgress) {
        this.observerProgress = observerProgress;
    }

    public interface ObserverProgress {
        void progressChanged(long read, long contentLength, boolean done);
    }

    public static String getBasUrl(String url) {
        String head = "";
        int index = url.indexOf("://");
        if (index != -1) {
            head = url.substring(0, index + 3);
            url = url.substring(index + 3);
        }
        index = url.indexOf("/");
        if (index != -1) {
            url = url.substring(0, index + 1);
        }
        return head + url;
    }

}
