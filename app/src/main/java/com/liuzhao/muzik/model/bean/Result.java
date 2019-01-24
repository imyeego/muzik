package com.liuzhao.muzik.model.bean;

import java.util.List;

/**
 * Created by zhongyu on 2018/10/23.
 *
 * @author Ann
 */
public class Result<T> {

    private String stat;
    private List<T> data;

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "stat='" + stat + '\'' +
                ", data=" + data.toString() +
                '}';
    }
}
