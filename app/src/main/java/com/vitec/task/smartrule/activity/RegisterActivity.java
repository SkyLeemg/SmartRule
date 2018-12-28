package com.vitec.task.smartrule.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.unuse.UnuseMainActivity;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.wxapi.bean.ResultInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import scut.carson_ho.diy_view.SuperEditText;

public class RegisterActivity extends BaseActivity implements OnClickListener{

    private static final String TAG = "RegisterActivity";
    private SuperEditText etUserName;
    private SuperEditText etPhone;
    private SuperEditText etMobileCode;
    private SuperEditText etPwd;
    private SuperEditText etRepeatPwd;
    private SuperEditText etName;
    private Button btnGetMobileCode;
    private Button btnRegister;
    private MKLoader mkLoader;

    private int countDown = 60;//验证码倒计时用
    private String mobileCode;//从服务器获取的验证码
    private ResultInfo resultInfo;//微信登录界面传来的参数
    private UserDbHelper userDbHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        initData();

    }

    private void initData() {
//        获取上个页面传来的数据（WXEntryActivity）
        resultInfo = (ResultInfo) getIntent().getSerializableExtra("wxmsg");
        userDbHelper = new UserDbHelper(getApplicationContext());

    }

    private void initView() {
        etUserName = findViewById(R.id.et_rsg_username);
        etPhone = findViewById(R.id.et_rsg_phone);
        etMobileCode = findViewById(R.id.et_rsg_mibile_code);
        etPwd = findViewById(R.id.et_rsg_pwd);
        etRepeatPwd = findViewById(R.id.et_rsg_repeat_pwd);
        etName = findViewById(R.id.et_rsg_name);
        btnGetMobileCode = findViewById(R.id.btn_rsg_get_mibile_code);
        btnRegister = findViewById(R.id.btn_register);
        mkLoader = findViewById(R.id.mkloader);

        btnRegister.setOnClickListener(this);
        btnGetMobileCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rsg_get_mibile_code://获取手机验证码
                final  String phone = etPhone.getText().toString().trim();
                mkLoader.setVisibility(View.VISIBLE);
                if (phone.length() != 11) {
                    mkLoader.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.mobile_param,phone);
                            List<OkHttpUtils.Param> paramList = new ArrayList<>();
                            paramList.add(param);
                            StringBuffer url = new StringBuffer();
                            url.append(NetConstant.baseUrl);
                            url.append(NetConstant.getMobileCodeUrl);
                            OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
                                @Override
                                public void onSuccess(String response) {
                                    try {
                                        /**
                                         *
                                         {"status":"success","code":200,"msg":"验证码下发成功"}
                                         */
                                        JSONObject jsonObject = new JSONObject(response);
                                        String status = jsonObject.optString("status");
                                        mobileCode = jsonObject.optString("code");
                                        Log.e(TAG, "onSuccess: 获取验证码成功："+response );
                                        final String msg = jsonObject.optString("msg");
                                        countDown = 60;
                                        if (status.equals("success")) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mkLoader.setVisibility(View.GONE);
                                                    btnGetMobileCode.setClickable(false);
                                                    btnGetMobileCode.setBackgroundColor(Color.GRAY);
                                                    final Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (countDown > 0) {
                                                                btnGetMobileCode.setText("重新获取(" + countDown + ")");
                                                                handler.postDelayed(this, 1000);
                                                            } else {
                                                                btnGetMobileCode.setText("重新获取");
                                                                btnGetMobileCode.setClickable(true);
                                                                btnGetMobileCode.setBackgroundResource(R.drawable.btn_nomal);
                                                            }
                                                            countDown--;
                                                        }
                                                    }, 1000);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mkLoader.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mkLoader.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mkLoader.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            },paramList);
                        }
                    }).start();

                }
                break;

            case R.id.btn_register://注册
                final  String loginName = etUserName.getText().toString().trim();
                final  String phoneNum = etPhone.getText().toString().trim();
                final  String pwd = etPwd.getText().toString().trim();
                final  String repeatPwd = etRepeatPwd.getText().toString().trim();
                final  String code = etMobileCode.getText().toString().trim();
                final  String name = etName.getText().toString().trim();

                String userPattern = "^[A-Za-z][A-Za-z1-9_-]+$";//登录名的正则表达式：（字母开头 + 数字/字母/下划线）
                if (!loginName.matches(userPattern)) {
                    Toast.makeText(getApplicationContext(),"登录名格式不正确",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pwd.equals(repeatPwd)) {
                    Toast.makeText(getApplicationContext(),"两次密码不相同",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phoneNum.length() != 11) {
                    Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                    return;
                }

               mkLoader.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        OkHttpUtils.Param uinonId = new OkHttpUtils.Param(NetConstant.unionId, resultInfo.getUnionId());
                        OkHttpUtils.Param loginParam = new OkHttpUtils.Param(NetConstant.register_username, loginName);
                        OkHttpUtils.Param pwdParam = new OkHttpUtils.Param(NetConstant.register_password, pwd);
                        OkHttpUtils.Param pwdConfirmParam = new OkHttpUtils.Param(NetConstant.register_password_confirm, repeatPwd);
                        OkHttpUtils.Param nameParam = new OkHttpUtils.Param(NetConstant.register_name, name);
                        OkHttpUtils.Param mobileParam = new OkHttpUtils.Param(NetConstant.register_mobile, phoneNum);
                        OkHttpUtils.Param codeParam = new OkHttpUtils.Param(NetConstant.register_code, code);
                        List<OkHttpUtils.Param> registerParams = new ArrayList<>();
//                        registerParams.add(uinonId);
                        registerParams.add(loginParam);
                        registerParams.add(pwdParam);
                        registerParams.add(pwdConfirmParam);
                        registerParams.add(nameParam);
                        registerParams.add(mobileParam);
                        registerParams.add(codeParam);
                        StringBuffer url = new StringBuffer();
                        url.append(NetConstant.baseUrl);
                        url.append(NetConstant.registerUrl);
                        Log.e(TAG, "run: 查看注册请求的信息："+registerParams.toString() );
                        OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
                            @Override
                            public void onSuccess(String response) {
                                Log.e(TAG, "onSuccess: 查看注册返回的信息："+response );
                                /**
                                 * {"status":"success",
                                 *  "code":200,
                                 *  "data":{
                                 *     "token":"18e2ecc3203cd8997a1654500c411fee",
                                 *     "user_info":{
                                 *         "status":200,
                                 *         "statusInfo":"ok",
                                 *         "data":{
                                 *             "wid":null,
                                 *             "userid":"491",
                                 *             "UnionID":"",
                                 *             "username":"ceshi",
                                 *             "language":"",
                                 *             "name":null,"file":"","mobile":"18876339015","projectName":null,"classificati
                                 */
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    int code = jsonObject.optInt("code");
                                    final String msg = jsonObject.optString("msg");
//                                    判断是否请求成功
                                    if (code == 200) {
                                        String data = jsonObject.optString("data");
                                        JSONObject dataJson = new JSONObject(data);
                                        String token = dataJson.optString("token");
//                                        请求成功则将用户数据保存到数据库
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.user_login_name, loginName);
                                        values.put(DataBaseParams.user_user_name, name);
//                                        values.put(DataBaseParams.user_wx_unionid, resultInfo.getUnionId());
                                        values.put(DataBaseParams.user_password, pwd);
                                        values.put(DataBaseParams.user_token, token);
                                        values.put(DataBaseParams.user_user_id,new JSONObject(dataJson.optString("user_info")).optString("userid"));
                                        boolean resultFlag = userDbHelper.insertUserToSqlite(DataBaseParams.user_table_name, values);
                                        Log.e(TAG, "onSuccess: 查看插入数据库的用户数据：" + values);
                                        if (resultFlag) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mkLoader.setVisibility(View.GONE);
                                                    Intent intent = new Intent(getApplicationContext(), UnuseMainActivity.class);
                                                    startActivity(intent);
                                                    RegisterActivity.this.finish();
                                                }
                                            });
                                        }
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mkLoader.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mkLoader.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }



                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "onFailure: 查看注册失败的异常信息："+e.getMessage() );
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mkLoader.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        },registerParams);
                    }
                }).start();



                break;

        }
    }

}
