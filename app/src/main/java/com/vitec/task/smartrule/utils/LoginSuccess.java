package com.vitec.task.smartrule.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.activity.DeviceManagerActivity;
import com.vitec.task.smartrule.activity.MainActivity;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.net.FileOkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aliyun.alink.linksdk.tools.ThreadTools.getProcessName;
import static com.aliyun.alink.linksdk.tools.ThreadTools.runOnUiThread;

public class LoginSuccess {

    private static final String TAG = "LoginSuccess";
    private Context context;

    public LoginSuccess(Context context) {
        this.context = context;
    }

    /**
     * 登录成功后的操作
     * @param response  服务器返回的数据
     * @param params  需要额外保存的数据
//     * @param flag 登录方式 1-用户名密码登录，2-微信登录，3手机验证码登录
     * @param mkLoader 加载控件，需要取消掉的
     */
    public void doSuccess(String response, List<OkHttpUtils.Param> params,final MKLoader mkLoader) {
        try {
            /**
             */
            Log.e(TAG, "onSuccess: 查看返回的登录信息："+response );
            final JSONObject jsonObject = new JSONObject(response);
            int code = jsonObject.optInt("code");
//                                    判断是否请求成功
            if (code == 200) {
                /**
                 * 1.将对应的数据存储到shareprefrence中
                 * 2.根据userid或者wid来判断该数据是否存在
                 *   2.1 存在-更新数据
                 *   2.2 不存在-新增数据
                 */
                String data = jsonObject.optString("data");
                JSONObject dataJson = new JSONObject(data);
//                获取token,token的时效是两个小时，每次登录后都要进行更新
//                String token = dataJson.optString("token");
                String token =SharePreferenceUtils.getToken(context);
//                获取user_info的json
                String user_info = dataJson.optString("user_info");
//                user_info字段的JSON
                JSONObject userInfoJson = new JSONObject(user_info);
                String realName =userInfoJson.optString("name");
                int userId = userInfoJson.optInt("id");
                int childId = userInfoJson.optInt("cid");
                String position = userInfoJson.optString("position");
                String imgUrl = userInfoJson.optString("avatar");//用户头像
                //初始化需要用到的字段
                String mobile = "";
                String unionID = "0";
                String wNickName = "";
                String headImgUrl = "";//微信头像
                String openid = "";
                int user_type = 0;////账号类型，1-手机号码，2-微信号，3两者都有
                UserDbHelper userDbHelper = new UserDbHelper(context);
                /***判断是否有手机号***/
                String user_main_string = userInfoJson.optString("user_main");
                if (user_main_string != null && !user_main_string.equals("null") && user_main_string.length() > 10) {
                    JSONObject userMainJson = new JSONObject(user_main_string);
                    mobile = userMainJson.optString("mobile");
                    user_type = 1;
                }

                /****判断是否有绑定微信****/
                String user_wechat_string = userInfoJson.optString("user_wechat");
                if (user_wechat_string != null && !user_wechat_string.equals("null") && user_wechat_string.length() > 10) {
                    JSONObject wechatJson = new JSONObject(user_wechat_string);
                    unionID = wechatJson.optString("UnionID");
                    wNickName = wechatJson.optString("nickname");
                    headImgUrl = wechatJson.optString("headImgUrl");
                    ContentValues values = new ContentValues();
//                    values.put(DataBaseParams.user_wid, wid);
                    values.put(DataBaseParams.user_wx_unionid, unionID);
                    values.put(DataBaseParams.user_wx_headImgUrl, headImgUrl);
                    values.put(DataBaseParams.user_wx_nick_name, wNickName);
                    values.put(DataBaseParams.user_wx_openid, openid);
                    values.put(DataBaseParams.user_user_id,userId);
                    userDbHelper.insertUserToSqlite(DataBaseParams.user_wx_table_name, values);
                    if (user_type == 1) {
                        user_type = 3;
                    } else {
                        user_type = 2;
                    }
                }



                /**
                 * 将刚登录成功的用户数据保存到sharePreference中
                 * 这里只保存正在登录使用的用户数据
                 * 下次就可以直接自动跳转
                 */
                Map<String, String> map = new HashMap<>();
                map.put(SharePreferenceUtils.user_id, String.valueOf(userId));
                map.put(SharePreferenceUtils.user_mobile, mobile);
                map.put(SharePreferenceUtils.user_real_name, realName);
                map.put(SharePreferenceUtils.user_token, token);
                map.put(SharePreferenceUtils.user_type, String.valueOf(user_type));
                for (OkHttpUtils.Param param:params) {
                    if (param.key.equals(SharePreferenceUtils.user_pwd)) {
                        // 给密码加密
                        map.put(param.key, Base64Utils.encodeBase64(param.value));
                    } else {
                        map.put(param.key, param.value);
                    }
                }
                LogUtils.show("保存前的数据SharePreferenceUtils："+map);
                SharePreferenceUtils.savaData(context,map,SharePreferenceUtils.user_table);


                /**
                 * 根据User_id查询该条数据是否已经保存到数据库
                 *      如果已经保存则直接跳转，
                 *      如果没有保存，则插入到数据库中
                 */

                String where = " where " + DataBaseParams.user_user_id + " = " + userId +" ;";
                Log.e(TAG, "onSuccess: 查看where语句："+where );
                List<User> userList = userDbHelper.queryUserDataFromSqlite(where);
                final ContentValues values = new ContentValues();
                values.put(DataBaseParams.user_user_name, realName);
                values.put(DataBaseParams.user_child_id,childId);
                values.put(DataBaseParams.user_position,position);
                values.put(DataBaseParams.user_token, token);
                values.put(DataBaseParams.user_user_id, userId);
                values.put(DataBaseParams.user_wx_unionid,unionID);
                values.put(DataBaseParams.user_mobile,mobile);
                values.put(DataBaseParams.user_img_url,imgUrl);
                for (OkHttpUtils.Param param:params) {
                    if (param.key.equals(SharePreferenceUtils.user_pwd)) {
                        // 给密码加密
                        values.put(param.key, Base64Utils.encodeBase64(param.value));
                    } else {
                        values.put(param.key, param.value);
                    }
                }

                LogUtils.show("查看保存到数据库之前的数据："+values);
                /**
                 * 查看数据库是否已经存有一条记录
                 */
                if (userList.size() == 0) {
//                 无记录则新增一条记录
                    boolean resultFlag = userDbHelper.insertUserToSqlite(DataBaseParams.user_table_name, values);
                    Log.e(TAG, "onSuccess: 查看插入数据库的用户数据：" + values);
                } else {
//                    有记录则更新记录信息
                    User user = userList.get(0);
                    userDbHelper.updateUserData(values, new String[]{String.valueOf(user.getId())});
                }

                /***加载网络头像部分****/
                String localImg = headImgUrl;
                if (imgUrl != null && imgUrl.length() > 10) {
                    localImg = imgUrl;
                }
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + DateFormatUtil.transForMilliSecond(new Date()) + ".jpg";
                FileOkHttpUtils.downloadFile(localImg,path,userId,context);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mkLoader != null) {
                            mkLoader.setVisibility(View.GONE);
                        }

                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);

                    }});
                userDbHelper.close();

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mkLoader != null) {
                            mkLoader.setVisibility(View.GONE);
                        }
                        Toast.makeText(context,jsonObject.optString("msg"),Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mkLoader != null) {
                        mkLoader.setVisibility(View.GONE);
                    }
                    Toast.makeText(context,"Json数据解析失败",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//
//     private OkHttpUtils.ResultCallback resultCallback = new OkHttpUtils.ResultCallback() {
//        @Override
//        public void onSuccess(Object response) {
//            byte[] picture_bts = (byte[]) response;
//
//        }
//
//        @Override
//        public void onFailure(Exception e) {
//            LogUtils.show("头像加载失败");
//        }
//    };
}
