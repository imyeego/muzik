package com.liuzhao.muzik.common;

import com.google.gson.Gson;
import com.liuzhao.muzik.model.ParameterizedTypeImpl;
import com.liuzhao.muzik.model.bean.Movie;
import com.liuzhao.muzik.model.bean.News;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.ResponseBody;

/**
 *
 * @author liuzhao
 */
public class Convert {

    public static <T> List<T> parseResponseBody(ResponseBody responseBody, ICallback<T> callback) {
        String responseStr;
        Movie<T> movie = null;
        try {
            responseStr = responseBody.string();
            Type[] types = callback.getClass().getGenericInterfaces();
            Type type = ((ParameterizedType)types[0]).getActualTypeArguments()[0];
            Type jsonType = new ParameterizedTypeImpl(Movie.class, new Type[]{type});
            movie = new Gson().fromJson(responseStr, jsonType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movie.getSubjects();
    }

    public static <T> News<T> parseResponse(ResponseBody response, ICallback<T> callback){
        String responseStr = null;
        Type jsonType = null;
        try {
            responseStr = response.string();
            Type[] types = callback.getClass().getGenericInterfaces();
            Type type = ((ParameterizedType)types[0]).getActualTypeArguments()[0];
            jsonType = new ParameterizedTypeImpl(News.class, new Type[]{type});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(responseStr, jsonType);

    }
}
