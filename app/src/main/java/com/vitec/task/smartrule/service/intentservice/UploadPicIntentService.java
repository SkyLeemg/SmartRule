package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.UploadFileMessegeEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.net.FileOkHttpUtils;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.ChangeUserMessageIntentService;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UploadPicIntentService extends IntentService {

    public static final String UPLOAD_FLAG = "UploadPicIntentService.UPLOAD_FLAG";//上传标志
//    上传头像标志
    public static final String FLAG_UPLOAD_HEAD_IMG = "UploadPicIntentService.upload_head_img";
    //    上传管控要点对应的图纸标志
    public static final String FLAG_UPLOAD_OPTION_IMG = "UploadPicIntentService.upload_option_img";
    //    图片地址
    public static final String VALUE_IMG_PATH = "UploadPicIntentService.img_path";
    public static final String VALUE_OPTION_LIST = "UploadPicIntentService.option.id.list";
    public static final String VALUE_OPTION_BUNDLE = "UploadPicIntentService.option.bundle";

    public UploadPicIntentService() {
        super("UploadPicIntentService");
    }

    public UploadPicIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String flag = intent.getStringExtra(UPLOAD_FLAG);
        String path = intent.getStringExtra(VALUE_IMG_PATH);
        switch (flag) {
            /*****上传头像*****/
            case FLAG_UPLOAD_HEAD_IMG:
                uploadHeadPic(path);
                break;

            /*****上传管控要点对应的图纸标志********/
            case FLAG_UPLOAD_OPTION_IMG:
                Bundle bundle = intent.getBundleExtra(VALUE_OPTION_BUNDLE);
                String ids = bundle.getString(NetConstant.upload_option_pic_check_options_list);
//                String ids = intent.getStringExtra(UploadPicIntentService.VALUE_OPTION_LIST);
                LogUtils.show("UploadPicIntentService---查看收到的server_id:"+ids);
                if (ids != null && !ids.equals("null")) {
                    uploadOptionPic(path,ids,bundle);
                }

                break;
        }
    }

    /**
     * 请求上传管控要点的图纸
     * @param path
     * @param ids
     */
    private void uploadOptionPic(final String path, final String ids, final Bundle bun) {
        final String url = NetConstant.baseUrl + NetConstant.upload_file_url;
        FileOkHttpUtils.uploadFile(url, path, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("uploadHeadPic上传图纸----上传成功：查看返回值：" + response);
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    String msg = object.optString("msg");
                    if (code == 200) {
                        String newImgUrl = object.getString("data");
//                        将头像地址更新到服务器
                        Bundle bundle = new Bundle();
                        bundle.putString(NetConstant.upload_option_pic_url_key, newImgUrl);
                        bundle.putString(NetConstant.upload_option_pic_check_options_list, ids);
//                        bundle.putInt(DataBaseParams.options_create_time, DateFormatUtil.transForMilliSecond(new Date()));
                        bundle.putString(NetConstant.upload_option_pic_number_list, bun.getString(NetConstant.upload_option_pic_number_list));
                        Intent intent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
                        intent.putExtra(PerformMeasureNetIntentService.VALUE_UPLOAD_PIC_CHECK_OPTIONS_LIST, bundle);
                        intent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPLOAD_OPTION_PIC);
                        startService(intent);

                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    /**
     * 请求上传头像
     * @param path
     */
    private void uploadHeadPic(final String path) {
        final String url = NetConstant.baseUrl + NetConstant.upload_file_url;
        final UploadFileMessegeEvent messegeEvent = new UploadFileMessegeEvent();
        messegeEvent.setUpload_flag(FLAG_UPLOAD_HEAD_IMG);
        messegeEvent.setOldLocalPath(path);
        FileOkHttpUtils.uploadFile(url, path, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("uploadHeadPic----上传成功：查看返回值：" + response);
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    String msg = object.optString("msg");
                    messegeEvent.setMsg(msg);
                    if (code == 200) {
                        String newImgUrl = object.getString("data");
//                        将头像地址更新到服务器
                        List<OkHttpUtils.Param> paramList = new ArrayList<>();
                        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.upload_file_key,newImgUrl);
                        paramList.add(param);
                        Intent intent = new Intent(getApplicationContext(), ChangeUserMessageIntentService.class);
                        intent.putExtra(ChangeUserMessageIntentService.PARAM_LIST_KEY, (Serializable) paramList);
                        startService(intent);
                        messegeEvent.setNewUrl(newImgUrl);
                        messegeEvent.setSuccess(true);
//                        将头像保存到本地
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        String fileName = DateFormatUtil.formatDate(new Date()) + ".jpg";
                        File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), fileName);
                        if (!newFile.getParentFile().exists()) {

                            newFile.getParentFile().mkdir();
                        }
                        if (!newFile.exists()) {
                            newFile.createNewFile();
                        }
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(newFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                            //将图片地址更新到数据库
                            ContentValues values = new ContentValues();
                            values.put(DataBaseParams.user_local_img_path, newFile.getPath());
                            values.put(DataBaseParams.user_img_url,newImgUrl);
                            UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
                            User user = OperateDbUtil.getUser(getApplicationContext());
                            String where = DataBaseParams.user_user_id + "=?";
                            LogUtils.show("正在准备保存图片");
                            userDbHelper.updateUserData(values,where, new String[]{String.valueOf(user.getUserID())});
                            userDbHelper.close();
                            fos.flush();
                            fos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    } else {
                        messegeEvent.setNewUrl("");
                        messegeEvent.setSuccess(false);
                    }
                    EventBus.getDefault().post(messegeEvent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    messegeEvent.setNewUrl("");
                    messegeEvent.setMsg("数据解析失败");
                    messegeEvent.setSuccess(false);
                    EventBus.getDefault().post(messegeEvent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Exception e) {
                messegeEvent.setNewUrl("");
                messegeEvent.setMsg("网络请求失败");
                messegeEvent.setSuccess(false);
                EventBus.getDefault().post(messegeEvent);
            }
        });
    }
}
