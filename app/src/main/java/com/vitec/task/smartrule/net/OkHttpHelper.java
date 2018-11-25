package com.vitec.task.smartrule.net;

import android.util.Log;

import com.vitec.task.smartrule.bean.NetCallBackMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpHelper {

    private static final String TAG = "OkHttpHelper";
    private OkHttpClient client;

    public OkHttpHelper() {
        initClient();
    }

    private void initClient() {
        if (client == null) {
            client=new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
        }
    }

    public  void getDataFromServer(final String url,final int flag) {
        String resultJson = "";
        Log.e(TAG, "getDataFromServer: 发起get请求" );
        Request request = new Request.Builder().
                get().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: 网络请求失败" );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = null;

                if (call.isCanceled()) {
                    Log.e(TAG, "onResponse: 请求被取消了" );
                }
//                获取请求结果
                responseBody = response.body();
//                获取json字符串
                String resultJson = responseBody.string();
                NetCallBackMessage message = new NetCallBackMessage(resultJson, flag);
                EventBus.getDefault().post(message);
                Log.e(TAG, "onResponse: 查看请求回来的数据："+resultJson );

            }
        });
//        return resultJson;
    }
}
