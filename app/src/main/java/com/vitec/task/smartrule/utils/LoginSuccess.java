package com.vitec.task.smartrule.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.activity.DeviceManagerActivity;
import com.vitec.task.smartrule.activity.LoginActivity;
import com.vitec.task.smartrule.activity.MainActivity;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.UserDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                String token = dataJson.optString("token");
//                获取user_info的json
                String user_info = dataJson.optString("user_info");
                JSONObject userInfoJson = new JSONObject(user_info);
//                获取user_info中的wUser和userBaseInfo字段
                String wUser = userInfoJson.optString("wUser");
                String userBaseInfo = userInfoJson.optString("userBaseInfo");
                //初始化需要用到的字段
                String loginName = "";
                String mobile = "";
                String realName = "";
                String userId = "0";
                String wid = "0";
                String unionID = "0";
                String job = "测量员";
                String wNickName = "";
                String headImgUrl = "";//微信头像
                String openid = "";
                String imgUrl = "";//用户头像
                /**
                 * 如果该账号没绑定微信，则wUser字段就不是一个json字符串，而是null，或者是空的等
                 *  为了保险起见，还加了一个长度限制的判断
                 *  下面的用户名密码之类的同理
                 */
                UserDbHelper userDbHelper = new UserDbHelper(context);
                if (wUser != null && !wUser.equalsIgnoreCase("null") && wUser.length() > 10) {

                    JSONObject wUserJson = new JSONObject(wUser);
                    wid = wUserJson.optString("id");
                    unionID = wUserJson.optString("UnionID");
                    wNickName = wUserJson.optString("nickName");
                    headImgUrl = wUserJson.optString("headImgUrl");
                    openid = wUserJson.optString("openid");
                    userId = wUserJson.optString("userid");
                    ContentValues values = new ContentValues();
                    values.put(DataBaseParams.user_wid, wid);
                    values.put(DataBaseParams.user_wx_unionid, unionID);
                    values.put(DataBaseParams.user_wx_headImgUrl, headImgUrl);
                    values.put(DataBaseParams.user_wx_nick_name, wNickName);
                    values.put(DataBaseParams.user_wx_openid, openid);
                    values.put(DataBaseParams.user_user_id,userId);
                    userDbHelper.insertUserToSqlite(DataBaseParams.user_wx_table_name, values);
                    LogUtils.show("该账号有绑定微信---添加到数据库的数据内容："+values.toString());
                }

                if (userBaseInfo != null && !userBaseInfo.equalsIgnoreCase("null") && userBaseInfo.length() > 10) {
                    JSONObject userBaseInfoJson = new JSONObject(userBaseInfo);
                    userId = userBaseInfoJson.optString("id");
                    loginName = userBaseInfoJson.optString("userName");
                    realName = userBaseInfoJson.optString("name");
                    mobile = userBaseInfoJson.optString("mobile");
                    openid = userBaseInfoJson.optString("openid");
                    unionID = userBaseInfoJson.optString("UnionID");
                    imgUrl = userBaseInfoJson.optString("file");
                }

                /**
                 * 将刚登录成功的用户数据保存到sharePreference中
                 * 这里只保存正在登录使用的用户数据
                 * 下次就可以直接自动跳转
                 */
                Map<String, String> map = new HashMap<>();
                map.put(SharePreferenceUtils.user_wid, wid);
                map.put(SharePreferenceUtils.user_id, userId);
                map.put(SharePreferenceUtils.user_login_name, loginName);
                map.put(SharePreferenceUtils.user_mobile, mobile);
                map.put(SharePreferenceUtils.user_real_name, realName);
                map.put(SharePreferenceUtils.user_token, token);

                for (OkHttpUtils.Param param:params) {
                    if (param.key.equals(SharePreferenceUtils.user_pwd)) {
                        // 给密码加密
                        map.put(param.key, Base64Utils.encodeBase64(param.value));
                    } else {
                        map.put(param.key, param.value);
                    }
                }
                SharePreferenceUtils.savaData(context,map,SharePreferenceUtils.user_table);


                /**
                 * 根据User_id查询该条数据是否已经保存到数据库
                 *      如果已经保存则直接跳转，
                 *      如果没有保存，则插入到数据库中
                 */

                String where = " where " + DataBaseParams.user_user_id + " = \"" + userId + "\"  OR "+DataBaseParams.user_wid +" = \"" +wid+"\" ;";
                Log.e(TAG, "onSuccess: 查看where语句："+where );
                List<User> userList = userDbHelper.queryUserDataFromSqlite(where);
                ContentValues values = new ContentValues();
                values.put(DataBaseParams.user_login_name, loginName);
                values.put(DataBaseParams.user_user_name, realName);
                values.put(DataBaseParams.user_token, token);
                values.put(DataBaseParams.user_user_id, userId);
                values.put(DataBaseParams.user_wx_unionid,unionID);
                values.put(DataBaseParams.user_mobile,mobile);
                values.put(DataBaseParams.user_wid,wid);
                values.put(DataBaseParams.user_job,job);
                values.put(DataBaseParams.user_img_url,imgUrl);
                for (OkHttpUtils.Param param:params) {
                    if (param.key.equals(SharePreferenceUtils.user_pwd)) {
                        // 给密码加密
                        values.put(param.key, Base64Utils.encodeBase64(param.value));
                    } else {
                        values.put(param.key, param.value);
                    }
                }
                if (userList.size() == 0) {
//                                        请求成功则将用户数据保存到数据库
                    boolean resultFlag = userDbHelper.insertUserToSqlite(DataBaseParams.user_table_name, values);
                    Log.e(TAG, "onSuccess: 查看插入数据库的用户数据：" + values);
                } else {
                    User user = userList.get(0);
                    userDbHelper.updateUserData(values, new String[]{String.valueOf(user.getId())});
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mkLoader != null) {
                            mkLoader.setVisibility(View.GONE);
                        }

                        Intent intent = new Intent(context, DeviceManagerActivity.class);
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
}
