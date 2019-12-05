package com.liuzhao.muzik.ui.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.liuzhao.ioc_annotations.BindView
import com.liuzhao.ioc_annotations.OnClick
import com.liuzhao.ioc_api.ViewFinder
import com.liuzhao.muzik.R
import com.liuzhao.muzik.model.bean.Person
import com.liuzhao.muzik.model.event.FirstEvent
import com.liuzhao.muzik.model.event.SecondEvent
import com.liuzhao.muzik.ui.adapter.PlaylistAdapter
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
    @BindView(R.id.rv)
    lateinit var recyclerView: RecyclerView

    var list = ArrayList<Person>()

    var adaper: PlaylistAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        ViewFinder.inject(this)

        var layoutManager: LinearLayoutManager = LinearLayoutManager(baseContext,LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isScrollV = false
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isScrollV = dy > 0
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                layoutManager = recyclerView.layoutManager as LinearLayoutManager
                var lastPostion = layoutManager.findLastVisibleItemPosition()
                var totalCount = layoutManager.itemCount
                if (newState == RecyclerView.SCROLL_STATE_SETTLING && isScrollV) {
                    if (lastPostion == totalCount - 1) {
                        loadMore()
                    }
                }

            }
        })
        adaper = PlaylistAdapter(R.layout.item_platlist, list)
        recyclerView.adapter = adaper
        initData()

    }

    private fun initData() {
        list.clear()
        for (i in 0 .. 10) {
            var person = Person("刘钊", "三天前", 56, 76, R.mipmap.baoer)
            list.add(person)
        }
        adaper?.notifyItemRangeInserted(0, list.size)
    }

    private fun loadMore() {
        for (i in 0 .. 10) {
            var person = Person("刘钊", "三天前", 56, 76, R.mipmap.baoer)
            list.add(person)
        }
        adaper?.notifyItemRangeInserted(adaper?.itemCount!!, 10)

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
