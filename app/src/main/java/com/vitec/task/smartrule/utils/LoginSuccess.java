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
             * {"status":"success",
             * "code":200,
             * "data":
             *   {
             *   "token":"27eea8aee816e66a2823a3912418c0fb",
             *   "user_info":{
             *      "status":200,
             *      "statusInfo":"ok",
             *      "data":{
//             *      用户名密码登录
             *        "wid":null,"userid":"471","UnionID":"oKP_j1Dj8KQQRNVCsEJykp4P8Eog","username":"oppo","language":"","name":null,"file":"","mobile":"13377758605",
             *
             *         微信登录：
             *         "wid":"4","userid":"0","UnionID":null,"username":null,"language":null,"name":null,"mobile":null,
             *
             *         手机验证码登录：
             *         {"wid":null,"userid":"452","UnionID":"oKP_j1Bc43OJwmsw5oYZNcZEPZwA","username":"xjbank","language":"","name":null,"file":"","mobile":"15107620711",
             *
             *         "projectName":"测试",
             *         "classification":2,
             *         "projectImg":"http:\/\/vitec.oss-cn-shenzhen.aliyuncs.com\/vitec\/locales\/20180907logo.png",
             *         "belong":"553",
             *         "admin":"0",
             *         "role":["测试1"],
             *         ]}}},
             *  "msg":"登录成功"}
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
                JSONObject userJson = new JSONObject(new JSONObject(user_info).optString("data"));
//                接下来就可以从userJson中获取对应的用户数据了
                String loginName = userJson.optString("username");
                String mobile = userJson.optString("mobile");
                String realName = userJson.optString("name");
//                因为有时候userID和wid服务器会返回“null”字符串，所以就将ID改成String字符串不定义为int
                String userId = userJson.optString("userid");
                String wid = userJson.optString("wid");
//                如果微信和用户名绑定之后，UnionID就不为空
                String unionID = userJson.optString("UnionID");

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
                map.put(SharePreferenceUtils.user_real_name, userJson.optString("name"));
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
                UserDbHelper userDbHelper = new UserDbHelper(context);
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
                        context.startActivity(intent); }});
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
