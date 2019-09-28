package com.liuzhao.muzik.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.liuzhao.ioc_annotations.BindView
import com.liuzhao.ioc_annotations.OnClick
import com.liuzhao.ioc_api.ViewFinder
import com.liuzhao.muzik.R
import com.liuzhao.muzik.model.event.FirstEvent
import com.liuzhao.muzik.model.event.SecondEvent
import com.liuzhao.okevent.OkEvent
import com.liuzhao.okevent.Subscribe


import rx.functions.Action0
import rx.schedulers.Schedulers

/**
 *
 * @author liuzhao
 */
class PlaylistActivity : AppCompatActivity() {

    @BindView(R.id.tv_title)
    lateinit var tvTitle: TextView
    @BindView(R.id.tv_bar)
    lateinit var tvBar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        ViewFinder.inject(this)
    }


    @OnClick(R.id.tv_title)
    fun onClick(view: View) {
        val event = FirstEvent("first")
        OkEvent.getInstance().post(event)
        Log.e(TAG, event.msg + " " + Thread.currentThread().name)
    }

    @OnClick(R.id.tv_bar)
    fun onBar(view: View) {
        //        Schedulers.io().createWorker().schedule(() -> {
        //
        //        });
        val event = SecondEvent("second")
        OkEvent.getInstance().post(event)
        Log.e(TAG, event.msg + " " + Thread.currentThread().name)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        private val TAG = "PlaylistActivity"
    }
}
