package com.liuzhao.muzik.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.liuzhao.ioc_annotations.BindView;
import com.liuzhao.ioc_annotations.OnClick;
import com.liuzhao.ioc_api.ViewFinder;
import com.liuzhao.muzik.R;
import com.liuzhao.muzik.model.event.FirstEvent;
import com.liuzhao.muzik.model.event.SecondEvent;
import com.liuzhao.okevent.OkEvent;
import com.liuzhao.okevent.Subscribe;


import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 *
 * @author liuzhao
 */
public class PlaylistActivity extends AppCompatActivity {

    private static final String TAG = "PlaylistActivity";

    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_bar) TextView tvBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        ViewFinder.inject(this);
    }


    @OnClick(R.id.tv_title)
    void onClick(View view){
        FirstEvent event = new FirstEvent("first");
        OkEvent.getInstance().post(event);
        Log.e(TAG, event.getMsg() + " " + Thread.currentThread().getName());
    }

    @OnClick(R.id.tv_bar)
    void onBar(View view){
//        Schedulers.io().createWorker().schedule(() -> {
//
//        });
        SecondEvent event = new SecondEvent("second");
        OkEvent.getInstance().post(event);
        Log.e(TAG,  event.getMsg() + " " + Thread.currentThread().getName());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
