package com.liuzhao.muzik.presenter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.liuzhao.muzik.common.EntityCallback;
import com.liuzhao.muzik.common.ICallback;
import com.liuzhao.muzik.common.RetrofitClient;
import com.liuzhao.muzik.model.bean.Movie;
import com.liuzhao.muzik.model.bean.MovieEntity;
import com.liuzhao.muzik.model.bean.News;
import com.liuzhao.muzik.model.bean.NewsEntity;
import com.liuzhao.muzik.model.bean.User;

import java.util.List;
import java.util.Map;

import rx.Subscription;

/**
 * Created by zhongyu on 2018/11/5.
 *
 * @author liuzhao
 */
public class NewsPresenter extends NewsContract.Presenter {

    private static String TAG = "NewsPresenter";
    private Context context;

    public NewsPresenter(Context context) {
        super();
        this.context = context;
    }

    public NewsPresenter(Context context, NewsContract.View view) {
        super(view);
        this.context = context;
    }

    @Override
    protected void onLoadNews() {
        RetrofitClient.getInstance().getNews(new ICallback<NewsEntity>() {
            @Override
            public void onSuccess(List<NewsEntity> result) {
                view.onLoadComplete(result);
            }

            @Override
            public void onFailure() {
                view.onError();
            }
        });

    }

    @Override
    protected void onLoadMovies() {
        Subscription subscription = RetrofitClient.getInstance().getMovies(new ICallback<MovieEntity>() {
            @Override
            public void onSuccess(List<MovieEntity> list) {
                view.onLoadMovieComplete(list);
            }

            @Override
            public void onFailure() {
                view.onError();
            }
        });
        subscriptions.add(subscription);

    }

    @Override
    public void onLogin(Map<String, Object> map) {
        RetrofitClient.getInstance().postMap("user/login", map, new EntityCallback<User>() {
            @Override
            public void onSuccess(User result) {
                view.onLogin(result);
            }

            @Override
            public void onFailure() {
                view.onError();
            }
        });
    }

    @Override
    public void subscribe() {
//        onLoadNews();
//        onLoadMovies();
    }

    @Override
    public void unsubscribe() {

    }
}
