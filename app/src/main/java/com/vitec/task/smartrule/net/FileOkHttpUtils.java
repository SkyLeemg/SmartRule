package com.vitec.task.smartrule.net;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.vitec.task.smartrule.bean.NetCallBackMessage;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.event.DownFileMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FileOkHttpUtils {

    private static final String TAG = "FileOkHttpUtils";
    private OkHttpClient client;

    public FileOkHttpUtils() {
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

    /**
     * 上传文件
     * @param url
     * @param path
     */
    public static void uploadFile(String url, String path, final OkHttpUtils.ResultCallback resultCallback) {

        OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();

        File file = new File(path);
        if (!file.exists()) {
            LogUtils.show("文件不存在，正在创建");
            try {
                file.createNewFile();
                LogUtils.show("文件创建成功");
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.show("文件创建失败："+e.getMessage());
            }
        }
        MediaType type = MediaType.parse("application/image/jpeg");
        if (path.endsWith(".png")) {
            type = MediaType.parse("image/png");
        }
        RequestBody fileBody = RequestBody.create(type, file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(NetConstant.upload_file_key, file.getName(), fileBody)
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                resultCallback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                resultCallback.onSuccess(response.body().string());
            }
        });
    }

    /**
         * 文件下载
         * @param url 下载路径
         * @param path 保存的本地文件路径和文件名
         * @return 返回是否下载成功
         */
        public static void downloadFile(String url, final String path, final int user_id, final Context context) {

            OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.show("下载失败----"+e.getMessage());
//                resultCallback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    byte[] picture_bts = response.body().bytes();
                    LogUtils.show("下载成功；===="+picture_bts);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(picture_bts, 0, picture_bts.length);
                    File newFile = new File(path);
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdir();
                    }
                    if (!newFile.exists()) {
                        try {
                            newFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(newFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        final ContentValues values = new ContentValues();
                        values.put(DataBaseParams.user_local_img_path, newFile.getPath());
                        UserDbHelper userDbHelper = new UserDbHelper(context);
                        String where = DataBaseParams.user_user_id + "=?";
                        userDbHelper.updateUserData(values,where, new String[]{String.valueOf(user_id)});
                        LogUtils.show("头像加载成功：" + newFile.getPath());
                        DownFileMsgEvent event = new DownFileMsgEvent();
                        event.setSuccess(true);
                        event.setType(1);
                        event.setPath(newFile.getPath());
                        EventBus.getDefault().post(event);
                        fos.flush();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                resultCallback.onSuccess(picture_bts);
                }
            });

        /***************************/


    }

    /**
     * 图纸下载
     * @param url 下载路径
     * @param path 保存的本地文件路径和文件名
     * @return 返回是否下载成功
     */
    public static void downloadPFile(String url, final String path, final RulerCheckOptions options, final Context context) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.show("下载失败----" + e.getMessage());
//                resultCallback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                byte[] picture_bts = response.body().bytes();
                LogUtils.show("下载成功；====" + picture_bts);
                Bitmap bitmap = BitmapFactory.decodeByteArray(picture_bts, 0, picture_bts.length);
                File newFile = new File(path);
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdir();
                }
                if (!newFile.exists()) {
                    try {
                        newFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(newFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    final ContentValues values = new ContentValues();
                    values.put(DataBaseParams.measure_option_img_path, newFile.getPath());
                    String where = DataBaseParams.server_id + "=?";
                    OperateDbUtil.updateOptionsDataToSqlite(context, DataBaseParams.measure_option_table_name, where, values, new String[]{String.valueOf(options.getServerId())});
                    DownFileMsgEvent event = new DownFileMsgEvent();
                    event.setSuccess(true);
                    event.setType(2);
                    event.setPath(newFile.getPath());
                    event.setObject(options);
                    EventBus.getDefault().post(event);
                    LogUtils.show("下载图片-----管控要点图片下载完成：");
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                resultCallback.onSuccess(picture_bts);
            }
        });
    }

}
