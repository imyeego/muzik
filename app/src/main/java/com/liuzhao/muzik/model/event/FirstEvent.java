package com.liuzhao.muzik.model.event;

/**
 * Created by zhongyu on 2018/11/15.
 *
 * @author liuzhao
 */
public class FirstEvent implements Event{
    private String msg;

    public FirstEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
