package com.liuzhao.muzik.common.http;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface APIService {

    @FormUrlEncoded
    @POST
    Call<ResponseBody> postMap(@Url String url, @FieldMap Map<String, Object> map);

    @Streaming
    @GET
    Call<ResponseBody> download(@Header("RANGE") String start, @Url String url);

    @Streaming
    @POST
    @FormUrlEncoded
    Call<ResponseBody> download(@Header("RANGE") String start, @FieldMap Map<String, Object> map, @Url String url);

    @Multipart
    @POST
    Call<ResponseBody> upload(@Url String url, @Part("file") MultipartBody.Part part);
}
