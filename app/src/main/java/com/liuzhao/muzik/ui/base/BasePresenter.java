package com.liuzhao.muzik.ui.base;

import rx.subscriptions.CompositeSubscription;

/**
 *
 * @author liuzhao
 */
public abstract class  BasePresenter<V extends BaseView> {

    protected CompositeSubscription subscriptions;
    protected V view;

    public BasePresenter() {
        subscriptions = new CompositeSubscription();
    }

    public BasePresenter(V view) {
        this();
        this.view = view;
    }

    public abstract void subscribe();
    public void unsubscribe(){
        subscriptions.clear();
        view = null;
    }

    public void setView(V view) {
        this.view = view;
    }
}
