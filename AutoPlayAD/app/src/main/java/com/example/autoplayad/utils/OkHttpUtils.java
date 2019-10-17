package com.example.autoplayad.utils;

import android.text.TextUtils;

import com.example.autoplayad.App;
import com.example.autoplayad.DataCallBack;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtils {

    /**
     * 懒汉 安全 加同步
     * 私有的静态成员变量 只声明不创建
     * 私有的构造方法
     * 提供返回实例的静态方法
     */

    private static OkHttpUtils okHttpUtils = null;

    private OkHttpUtils() {
    }

    public static OkHttpUtils getInstance() {
        if (okHttpUtils == null) {
            //加同步安全
            synchronized (OkHttpUtils.class) {
                if (okHttpUtils == null) {
                    okHttpUtils = new OkHttpUtils();
                }
            }

        }

        return okHttpUtils;
    }

    private static OkHttpClient okHttpClient = null;

    private synchronized OkHttpClient getOkHttpClient(){
        if (okHttpClient==null){
            OkHttpClient.Builder builder=new OkHttpClient.Builder();
            okHttpClient=builder
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .writeTimeout(1,TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient;
    }

    public void doGet(String url,final DataCallBack callBack){
        if (TextUtils.isEmpty(url))
            return;
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient=getOkHttpClient();

        final Request request=new Request.Builder().url(url).build();

        Call call=okHttpClient.newCall(request);
        //发送异步请求，在onResponse中调用接口onSuccess传递数据
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr=response.body().string();
                App.context.runOnUiThread(new Runnable() {                           //runOnUiThread
                    @Override
                    public void run() {
                        callBack.onSuccess(responseStr);
                    }
                });

            }
        });
    }

    public void doGet2(String url,final DataCallBack callBack){
        if (TextUtils.isEmpty(url))
            return;
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient=getOkHttpClient();

        final Request request=new Request.Builder().url(url).build();

        Call call=okHttpClient.newCall(request);
        //发送异步请求，在onResponse中调用接口onSuccess传递数据
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr=response.body().string();
                callBack.onSuccess(responseStr);
            }
        });
    }

    public String get(String url) {
        Request request = new Request.Builder().url(url).build();
        try {
            okHttpClient=getOkHttpClient();
            Response response = okHttpClient.newCall(request).execute();
            String responseStr = response.body().string();
            return responseStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public void doPost(String url, Map<String, String> params, final  DataCallBack callback) {
        if (TextUtils.isEmpty(url))
            return;
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getOkHttpClient();
        //构建FormBody 封装键值对参数
        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));
        }
        //创建Request
        Request request = new Request.Builder().url(url).post(builder.build()).build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr=response.body().string();
                App.context.runOnUiThread(new Runnable() {                           //runOnUiThread
                    @Override
                    public void run() {
                        callback.onSuccess(responseStr);
                    }
                });

            }
        });

    }
}
