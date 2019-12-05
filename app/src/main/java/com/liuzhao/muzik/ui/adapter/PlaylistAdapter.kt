package com.liuzhao.muzik.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.liuzhao.muzik.R
import com.liuzhao.muzik.model.bean.Person
import kotlinx.android.synthetic.main.item_platlist.view.*

class PlaylistAdapter constructor(layoutId: Int, list: List<Person>?) :
        BaseQuickAdapter<Person, BaseViewHolder>(layoutId, list) {

    override fun convert(helper: BaseViewHolder?, item: Person?) {
        helper?.setText(R.id.tv_name, item?.name)
        helper?.setText(R.id.tv_time, item?.time)
        helper?.setText(R.id.tv_follower, "${item?.followerCount}")
        helper?.setText(R.id.tv_following, "${item?.followingCount}")
        item?.avartar?.let { helper?.setImageResource(R.id.iv_avatar, it) }
    }
}