package com.liuzhao.muzik.model.bean;

import java.util.List;

/**
 * Created by zhongyu on 2018/11/6.
 *
 * @author liuzhao
 */
public class Movie<T> {

    private int count;
    private int start;
    private int total;
    private List<T> subjects;
    private String title;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<T> subjects) {
        this.subjects = subjects;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
