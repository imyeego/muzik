package com.liuzhao.muzik.presenter;

import com.liuzhao.muzik.model.bean.Movie;
import com.liuzhao.muzik.model.bean.MovieEntity;
import com.liuzhao.muzik.model.bean.NewsEntity;
import com.liuzhao.muzik.model.bean.User;
import com.liuzhao.muzik.ui.base.BasePresenter;
import com.liuzhao.muzik.ui.base.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by zhongyu on 2018/11/5.
 *
 * @author Ann
 */
public interface NewsContract {

    interface View extends BaseView{
        void onLoadComplete(List<NewsEntity> list);

        void onLoadMovieComplete(List<MovieEntity> list);

        void onLogin(User user);

    }

    abstract class  Presenter extends BasePresenter<View>{

        public Presenter() {
            super();
        }

        public Presenter(View view) {
            super(view);
        }

        protected abstract void onLoadNews();
        protected abstract void onLoadMovies();
        public abstract void onLogin(Map<String, Object> map);


    }
}
