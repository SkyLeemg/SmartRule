package com.vitec.task.smartrule.net;

import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by admin on 2018/4/3.
 */
public class OkHttpUtils {

    private static OkHttpClient client ;
    private static List<Map<String, String>> failListunSyn = new ArrayList<>();
    private static List<Map<String, String>> failList = Collections.synchronizedList(failListunSyn);//存储发送失败的数据
    private static boolean lastOneIsFail = false;
    private static  int performOrder = 0;
    /**
     * 使用OkHttp向服务器发送表单请求，服务器返回的数据使用EventBus返回给actvity
     * @param map 请求需要附带的表单数据
     * @param url 请求的服务器URL链接
     * @param whichEvent 用来判断是哪个页面发来的请求，1表示登陆请求，2表示定位请求 3表示版本更新请求
     */
    public static void postDatatoServer(final Map<String,String> map,final String url, final int whichEvent) {
                    if (client == null) {
                        client = new OkHttpClient.Builder()
                                .connectTimeout(15, TimeUnit.SECONDS)
                                .writeTimeout(20, TimeUnit.SECONDS)
                                .readTimeout(20, TimeUnit.SECONDS)
                                .build();
                    }
                    Log.e("OkHttpUtils", "ceshi,Map:" + map.toString() + ",isWhichEvent:" + whichEvent);
                    FormBody.Builder formBody = new FormBody.Builder();
                    Set<String> keySet = map.keySet();
                    for (String key : keySet) {
                        formBody.add(key, map.get(key));
                    }
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody.build())
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i("OkHttpClient", "ceshi,请求失败");
                            if (whichEvent == 2) {
                                lastOneIsFail = true;
                                saveFailData(map);
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.i("OkHttpClient", "ceshi,请求成功");
                            String resultString = response.body().string();
                            Log.i("ee", "ceshi,访问的链接："+url+"\n ,onResponse: 返回值："+resultString );
                            if (failList.size() > 0) {
                                if (lastOneIsFail) {
                                    saveFailData(map);
                                    lastOneIsFail = false;
                                }

                                dealFailData();
                            }
                        }
                    });


    }

    private static void dealFailData() {
        if ( failList.size() > 0) {
//            postDatatoServer(failList.get(0), Parameter.BEACON_INFO_URL2, 2);
        }
        if ( failList.size() > 0) {
            failList.remove(failList.get(0));
        }

    }

    private static void saveFailData(Map<String,String> map) {
        if (failList.size() > 0) {
            if (!failList.get(failList.size() - 1).equals(map)) {
                failList.add(map);
            }
        } else {
            failList.add(map);
        }

    }

    static String result = "";
    public static String getDataFromServer(String url) {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("getDataFromServer", "onFailure 请求失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                result = response.body().string();
                Log.e("", "onResponse:返回结果 " +result);
                if (response.isSuccessful()) {


                    Log.e("getDataFromServer", "onResponse: 请求成功："+result);
                }
            }
        });

        return result;
    }

    /**
     * 从指定的URL中获取数组
     * @param urlPath
     * @return
     * @throws Exception
     */
    public static String readParse(String urlPath) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream inStream = conn.getInputStream();
        while ((len = inStream.read(data)) != -1) {
            outStream.write(data, 0, len);
        }
        inStream.close();
        return new String(outStream.toByteArray());//通过out.Stream.toByteArray获取到写的数据
    }



}
