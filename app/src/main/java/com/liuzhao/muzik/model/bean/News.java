package com.liuzhao.muzik.model.bean;

/**
 * Created by zhongyu on 2018/10/23.
 *
 * @author liuzhao
 */
public class News<T> {
    private String reason;
    private Result<T> result;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Result<T> getResult() {
        return result;
    }

    public void setResult(Result<T> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "News{" +
                "reason='" + reason + '\'' +
                ", result=" + result.toString() +
                '}';
    }
}
