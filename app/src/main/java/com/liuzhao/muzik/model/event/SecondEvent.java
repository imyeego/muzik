package com.liuzhao.muzik.model.event;

/**
 * Created by zhongyu on 2018/11/15.
 *
 * @author liuzhao
 */
public class SecondEvent implements Event {

    private String msg;

    public SecondEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
