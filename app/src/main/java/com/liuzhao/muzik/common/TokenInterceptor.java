package com.liuzhao.muzik.common;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.liuzhao.muzik.common.http.BaseResult;
import com.liuzhao.muzik.utils.SharedPreferencesUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @authur : liuzhao
 * @time : 2020/10/12 9:54 AM
 * @Des :
 */
public class TokenInterceptor implements Interceptor{

    public static final String TAG = "TokenInterceptor";

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String token = SharedPreferencesUtil.getString("token", "");
        if ("".equals(token)) {
            Log.e(TAG, "本地 token为空");
            token = accessToken();
            Log.e(TAG, "获取token:" + token);

            SharedPreferencesUtil.putString("token", token);

        }
        Request re = request.newBuilder().addHeader("Authorization", token).build();
        Response response = chain.proceed(re);
        if (isTokenExpire(response)) {
            Log.e(TAG, "本地token过期");

            token = refreshToken();
            SharedPreferencesUtil.putString("token", token);

            Request request1 = request.newBuilder().header("Authorization", token).build();
            response.close();

            return chain.proceed(request1);
        }

        return response;
    }

    private boolean isTokenExpire(Response response) {
        try {
            ResponseBody body = response.peekBody(Long.MAX_VALUE);

            String resp = body.string();
            BaseResult model = new Gson().fromJson(resp, BaseResult.class);
            //403是与后台商量的token失效后的错误码，具体应根据自己的项目决定
            if(model.getCode() == 403) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized String accessToken() {
        String tokenString = OkHttpUtil.getInstance(5).get("http://172.16.41.110:8088/accessToken/1");
        if (isJson(tokenString)) {
            Log.e(TAG, tokenString);
            Token token = new Gson().fromJson(tokenString, Token.class);
            return token.getToken();
        }
        return "";
    }

    private String refreshToken() {
        String tokenString = OkHttpUtil.getInstance(5).get("http://172.16.41.110:8088/refreshToken");
        Log.e(TAG, "refresh token response:" + tokenString);
        if (isJson(tokenString)) {
            Token token = new Gson().fromJson(tokenString, Token.class);
            return token.getToken();
        }

        return "";
    }

    public static boolean isJson(String json) {
        if (isEmpty(json)) return false;
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        }
    }

    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input)||"null".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }
}
