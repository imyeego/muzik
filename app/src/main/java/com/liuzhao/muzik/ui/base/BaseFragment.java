package com.liuzhao.muzik.ui.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * @author liuzhao
 */
public abstract class BaseFragment extends Fragment {

    private View rootView;
    private boolean isFirstVisible = true;
    private boolean isFirstInvisible = true;
    private boolean isInited = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutId(), container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isInited){
            isInited = true;
            initView(rootView);
        }

    }

    protected abstract int getLayoutId();

    protected abstract void initView(View view);


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser){
            if (isFirstVisible){
                isFirstVisible = false;
                onFirstVisible();
            }else{
                onVisible();
            }
        }else{
            if (isFirstInvisible){
                isFirstInvisible = false;
                onFirstInvisible();
            }else{
                onInvisible();
            }
        }
    }

    protected abstract void onFirstVisible();
    protected abstract void onVisible();
    protected abstract void onFirstInvisible();
    protected abstract void onInvisible();


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
