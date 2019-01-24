package com.liuzhao.muzik.ui.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.List;


/**
 * Created by zhongyu on 2018/11/5.
 *
 * @author liuzhao
 */
public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{

    protected Context context;
    protected LayoutInflater inflater;
    protected List<T> list;

    public BaseAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public BaseAdapter(Context context, List<T> list) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.list = list;
    }



    public void add(final int pos, final T t){
        list.add(pos, t);
        notifyItemInserted(pos);

    }

    public void delete(int pos){
        list.remove(pos);
        notifyItemRemoved(pos);
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
