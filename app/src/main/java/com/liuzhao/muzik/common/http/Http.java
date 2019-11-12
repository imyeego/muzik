package com.liuzhao.muzik.common.http;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liuzhao.muzik.app.Constants;
import com.liuzhao.muzik.common.LoggingInterceptor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ikidou.reflect.TypeBuilder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Http {

    private static final String TAG = "http";
    private static final String NULL_DATA = "返回数据为空";
    private static Retrofit retrofit;
    private static APIService service;
    private static Http http;
    private Handler handler;
    private Gson gson;
    private ExecutorService executorService = Executors.newCachedThreadPool();


    public static Http instance() {
        if (http == null) {
            synchronized (Http.class) {
                if (http == null) {
                    http = new Http();
                }
            }
        }
        return http;
    }
    private Http() {
        GsonBuilder builder = new GsonBuilder().serializeNulls();
        gson = builder.create();
        handler = new Handler(Looper.getMainLooper());
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(3000, TimeUnit.SECONDS)
                .connectTimeout(2000, TimeUnit.SECONDS)
                .addNetworkInterceptor(new LoggingInterceptor())
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.MY_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        service = retrofit.create(APIService.class);
    }

    public <T> void postMap(String url, Map<String, Object> map, PostCallback<T> callback) {
        Log.i(TAG, gson.toJson(map));
        Call<ResponseBody> call = service.postMap(url, map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        T t = fromJson(response.body().string(), callback);
                        if (t != null) {
                            handler.post(() -> callback.success(t));
                        } else {
                            handler.post(() -> callback.fail(NULL_DATA));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.post(() -> callback.fail(e.getMessage()));
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handler.post(() -> callback.fail(t.getMessage()));

            }
        });
    }

    public void download(String url, Map<String, Object> map, DownloadListener listener) {
        Call<ResponseBody> call;
        if (map != null) {
            Log.i(TAG, gson.toJson(map));
            call = service.download("bytes=0-", map, url);
        } else {
            call = service.download("bytes=0-", url);

        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {
                    if (response.isSuccessful()) {
                        executorService.execute(() -> {
                            writeResponseToDisk(Constants.DATA_PATH, response, listener);
                        });
                    } else {
//                        Message.obtain(handler, DOWNLOAD_FAIL, response.errorBody()).sendToTarget();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handler.post(() -> listener.onFail(t));
            }
        });
    }

    public <T> void upload(String url, File file, UploadListener<T> listener) {
        MultipartBody.Part part;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        FileRequestBody fileRequestBody = new FileRequestBody(requestBody, listener);
        part = MultipartBody.Part.createFormData("file", file.getName(), fileRequestBody);

        Call<ResponseBody> call = service.upload(url, part);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
//                        ResponseModel<T> responseModel = parseResponse(response.body().string(), listener);
//                        handler.post(() -> listener.onFinish(responseModel.getData()));
                        T t = fromJson(response.body().string(), listener);
                        if (t != null) {
                            listener.onFinish(t);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFail(e);
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.onFail(t);
            }
        });
    }

    private <T> ResponseModel<T> parseResponse(String string, ICallback<T> callback) {
        Type[] types = callback.getClass().getGenericInterfaces();
        Type t = ((ParameterizedType)types[0]).getActualTypeArguments()[0];
        Type type = TypeBuilder
                .newInstance(ResponseModel.class)
                .addTypeParam(t)
                .build();
        return gson.fromJson(string, type);
    }

    private <T> T fromJson(String string, ICallback<T> callback) {
        Type[] types = callback.getClass().getGenericInterfaces();
        Type t = ((ParameterizedType)types[0]).getActualTypeArguments()[0];
        return gson.fromJson(string, t);
    }

    private void writeResponseToDisk(String path, Response<ResponseBody> response, DownloadListener downloadListener) {
        //从response获取输入流以及总大小
        writeFileFromResponse(new File(path), response.body().byteStream(), response.body().contentLength(), downloadListener);
    }

    private static int sBufferSize = 2048;

    private void writeFileFromResponse(File file, InputStream is, final long totalLength, DownloadListener downloadListener) {
        handler.post(() -> downloadListener.onStart());
        if (!file.exists()) {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> downloadListener.onFail(e));

            }
        }

        long currentLength = 0;
        try (OutputStream os =new BufferedOutputStream(new FileOutputStream(file)) ){
            byte data[] = new byte[sBufferSize];
            int len;
            while ((len = is.read(data, 0, sBufferSize)) != -1) {
                os.write(data, 0, len);
                currentLength += len;
                long finalCurrentLength = currentLength;
                handler.post(() -> downloadListener.onProgress((int) (100 * finalCurrentLength / totalLength)));
            }
            handler.post(() -> downloadListener.onFinish());
        } catch (IOException e) {
            e.printStackTrace();
            handler.post(() -> downloadListener.onFail(e));
        }
    }

    private class FileRequestBody<T> extends RequestBody {
        private RequestBody requestBody;
        private BufferedSink bufferedSink;
        private UploadListener<T> listener;
        FileRequestBody(RequestBody requestBody, UploadListener<T> listener) {
            this.requestBody = requestBody;
            this.listener = listener;
        }

        @Nullable
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            bufferedSink = Okio.buffer(sink(sink));
            requestBody.writeTo(bufferedSink);
            handler.post(() -> listener.onStart());
            bufferedSink.flush();
        }

        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                long byteWritten = 0;
                long contentLength = 0;

                @Override
                public void write(@NonNull Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        contentLength = contentLength();
                    }
                    final long finalContentLength = contentLength;
                    byteWritten += byteCount;
                    final long finalBytesWritten = byteWritten;
                    handler.post(() -> listener.onProgress((int) (finalBytesWritten * 1.0 / finalContentLength * 100)));
                }
            };
        }
    }
}
