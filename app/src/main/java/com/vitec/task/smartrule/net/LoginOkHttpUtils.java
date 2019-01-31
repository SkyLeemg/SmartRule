package com.vitec.task.smartrule.net;

import android.content.Context;

import com.vitec.task.smartrule.bean.event.LoginMsgEvent;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.utils.SharePreferenceUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginOkHttpUtils {
    private OkHttpClient client;

    public LoginOkHttpUtils() {
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

    public static void loginRequest(final Context context, String url, List<OkHttpUtils.Param> params) {
        OkHttpClient client=new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                        if (cookies != null) {
                            for (Cookie cookie : cookies) {
                                LogUtils.show("---打印查看登录的cookieName:" + cookie.name() + ",cookiePath:" + cookie.path() + ",cookieValue:" + cookie.value());
                                SharePreferenceUtils.saveToken(context,cookie.value());
                            }
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();


        FormBody.Builder builder= new FormBody.Builder();

        for (OkHttpUtils.Param param : params) {
            builder.add(param.getKey(), param.getValue());
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        final LoginMsgEvent event = new LoginMsgEvent();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.show("登录接口-----网络请求失败");
                event.setSuccess(false);
                event.setMsg("网络请求失败");
                EventBus.getDefault().post(event);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                LogUtils.show("登录接口----查看返回的信息：" + str);
                event.setSuccess(true);
                event.setRespone(str);
                event.setMsg("");
                EventBus.getDefault().post(event);
            }
        });


    }
}
