package com.vitec.task.smartrule.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import com.tencent.mm.opensdk.openapi.WXTextObject;
import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.CopyDbFileFromAsset;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.Base64Utils;
import com.vitec.task.smartrule.utils.LoginSuccess;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.utils.SharePreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scut.carson_ho.diy_view.SuperEditText;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG = "LoginActivity";
    private Button btnLogin;
    private SuperEditText etUser;
    private SuperEditText etPwd;
    private TextView tvSmsLogin;
    private TextView tvForgetPwd;

//    private CheckBox cbRemenberPwd;
    private TextView tvRegister;
    private ImageView imgWechat;
    private WeChatHelper weChatHelper;
    private CopyDbFileFromAsset copyDbFileFromAsset;
    private MKLoader mkLoader;
    private UserDbHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registerWeChat();
        initView();
        initDb();
    }

    private void initDb() {
        copyDbFileFromAsset = new CopyDbFileFromAsset(getApplicationContext());
        try {
            copyDbFileFromAsset.CopySqliteFileFromRawToDatabases(DataBaseParams.databaseName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void registerWeChat() {
        weChatHelper = new WeChatHelper(this);
    }

    private void initView() {
        btnLogin = findViewById(R.id.btn_login);
        etUser = findViewById(R.id.et_userid);
        etPwd = findViewById(R.id.et_psw);
//        cbRemenberPwd = findViewById(R.id.cb_remenber_pw);
        tvRegister = findViewById(R.id.tv_register);
        imgWechat = findViewById(R.id.img_wechat);
        mkLoader = findViewById(R.id.mkloader);
        tvForgetPwd = findViewById(R.id.cb_remenber_pw);
        tvSmsLogin = findViewById(R.id.cb_phone_login);

        tvSmsLogin.setOnClickListener(this);
        tvForgetPwd.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        imgWechat.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login://登陆按钮
                final String loginName = etUser.getText().toString().trim();
                final String pwd = etPwd.getText().toString().trim();
                if (loginName.equals("")) {
                    Toast.makeText(getApplicationContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pwd.equals("")) {
                    Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                mkLoader.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpUtils.Param nameParam = new OkHttpUtils.Param(NetConstant.login_username, loginName);
                        OkHttpUtils.Param pwdParam = new OkHttpUtils.Param(NetConstant.login_password, pwd);
                        List<OkHttpUtils.Param> paramList = new ArrayList<>();
                        paramList.add(nameParam);
                        paramList.add(pwdParam);
                        Log.e(TAG, "run: 查看登录请求的信息："+ paramList.toString());
                        StringBuffer url = new StringBuffer();
                        url.append(NetConstant.baseUrl);
                        url.append(NetConstant.loginUrl);
                        OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
                            @Override
                            public void onSuccess(String response) {
                                LoginSuccess loginSuccess = new LoginSuccess(LoginActivity.this);
                                OkHttpUtils.Param pwdParam = new OkHttpUtils.Param(NetConstant.login_password, pwd);
                                List<OkHttpUtils.Param> paramList = new ArrayList<>();
                                paramList.add(pwdParam);
                                loginSuccess.doSuccess(response,paramList,mkLoader);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "onFailure: 网络请求失败："+e.getMessage() );
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

                break;

            /**
             * 注册
             */
            case R.id.tv_register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
//                this.finish();
                break;

            /**
             * 微信登录
             */
            case R.id.img_wechat:
                mkLoader.setVisibility(View.VISIBLE);
                weChatHelper.regToWx();
                weChatHelper.sendLoginRequest();
                break;

            /**
             * 短信验证码登录
             */
            case R.id.cb_phone_login:
                Intent intent1 = new Intent(this, SmsLoginActivity.class);
                startActivity(intent1);
                break;

            /**
             * 忘记密码
             */
            case R.id.cb_remenber_pw:

                break;
        }
    }
}
