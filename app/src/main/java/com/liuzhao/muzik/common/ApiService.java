package com.liuzhao.muzik.common;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 *
 * @author liuzhao
 */
public interface ApiService {

    @GET("/toutiao/index")
    Call<ResponseBody> getNews(@Query("type")String type, @Query("key")String apiKey);

    @GET("top250")
    Observable<ResponseBody> getMovie(@Query("start")String start, @Query("count")String count);

    @Streaming
    @GET
    Observable<ResponseBody> download(@Header("RANGE") String start, @Url String url);

    @POST
    @FormUrlEncoded
    Observable<ResponseBody> postMap(@Url String url, @FieldMap Map<String, Object> map);
}
