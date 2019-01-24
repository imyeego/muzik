package com.liuzhao.muzik.ui.activity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.liuzhao.ioc_annotations.BindView;
import com.liuzhao.ioc_annotations.OnClick;
import com.liuzhao.ioc_api.ViewFinder;
import com.liuzhao.muzik.R;
import com.liuzhao.muzik.common.download.DownloadManager;
import com.liuzhao.muzik.model.bean.MovieEntity;
import com.liuzhao.muzik.model.bean.NewsEntity;
import com.liuzhao.muzik.model.event.FirstEvent;
import com.liuzhao.muzik.model.event.SecondEvent;
import com.liuzhao.muzik.presenter.NewsContract;
import com.liuzhao.muzik.presenter.NewsPresenter;
import com.liuzhao.muzik.ui.base.BaseActivity;
import com.liuzhao.muzik.utils.Counter;
import com.liuzhao.okevent.OkEvent;
import com.liuzhao.okevent.Subscribe;
import com.liuzhao.okevent.ThreadMode;


import java.util.List;

public class MainActivity extends BaseActivity<NewsPresenter> implements NewsContract.View, DownloadManager.ObserverProgress {

    private static String TAG = "MainActivity";
    @BindView(R.id.bn_hello)
    Button btnHello;
    @BindView(R.id.bn_stop)
    Button btnStop;

    @BindView(R.id.tv_hello)
    TextView tvHello;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.bn_playlist)
    Button bnPlaylist;
    private DownloadManager manager;
    private Counter counter;

    @Override
    protected void initView() {
        OkEvent.getInstance().register(this);
        counter = Counter.create();
        ViewFinder.inject(this);
        manager = DownloadManager.getInstance();
        manager.setObserverProgress(this);
    }

    @OnClick(R.id.bn_hello)
    void onStart(View view){
        manager.start();
    }

    @OnClick(R.id.bn_stop)
    void onStop(View view){
        if ((counter.getCount() & 1) == 0){
            manager.pause();
            btnStop.setText("restart");
        }
        else{
            manager.reStart();
            btnStop.setText("stop");
        }
        counter.count();
    }

    @OnClick(R.id.bn_playlist)
    void onToPlaylist(View view){
        Intent intent = new Intent();
        intent.setClass(this, PlaylistActivity.class);
        startActivity(intent);
    }



    @Subscribe(threadMode = ThreadMode.POSTING)
    void onText(FirstEvent event){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), event.getMsg(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getMsg() + " " + Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    void onBar(SecondEvent event){
        Log.e(TAG, event.getMsg() + " " + Thread.currentThread().getName());
        Toast.makeText(getApplicationContext(), event.getMsg(), Toast.LENGTH_SHORT).show();

//        tvHello.setText(event.getMsg());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected NewsPresenter getPresenter() {
        presenter = new NewsPresenter(context, this);
        return presenter;
    }

    @Override
    public void onLoadComplete(List<NewsEntity> list) {
        Log.e(TAG, "" + list.toString());
        tvHello.setText(list.get(0).toString());
    }

    @Override
    public void onLoadMovieComplete(List<MovieEntity> list) {
        Log.e(TAG, list.get(3).getTitle());
    }


    @Override
    public void progressChanged(long read, long contentLength, boolean done) {
        final int progress = (int)(100 * read / contentLength);
        progressBar.setProgress(progress);
        tvHello.setText(String.format("%d%%", progress));
        if (done) Toast.makeText(context, "下载完成!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError() {

    }

    @Override
    protected void onDestroy() {
        presenter.unsubscribe();
        counter.clear();
        OkEvent.getInstance().unregister(this);
        super.onDestroy();
    }
}
