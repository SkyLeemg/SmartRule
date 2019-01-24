package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import com.tencent.mm.opensdk.openapi.WXTextObject;
import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.WxResultMessage;
import com.vitec.task.smartrule.db.CopyDbFileFromAsset;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.GetMudelIntentService;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.LoginSuccess;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import scut.carson_ho.diy_view.SuperEditText;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private Button btnLogin;
    private EditText etUser;
    private EditText etPwd;
    private TextView tvSmsLogin;
    private TextView tvForgetPwd;

    //    private CheckBox cbRemenberPwd;
    private TextView tvRegister;
    private ImageView imgWechat;
    private WeChatHelper weChatHelper;
    private CopyDbFileFromAsset copyDbFileFromAsset;
    private MKLoader mkLoader;
    private UserDbHelper userDbHelper;
    private boolean isLoginSuccess = false;//如果登录成功了，则置为true，然后关闭页面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EventBus.getDefault().register(this);
        requestLocationPermissions();
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
        /**
         * 获取模板信息
         */
        Intent intent = new Intent(this, GetMudelIntentService.class);
        startService(intent);

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

        btnLogin.setClickable(false);
        etPwd.addTextChangedListener(inputTextWatcher);
        etUser.addTextChangedListener(inputTextWatcher);

    }

    private TextWatcher inputTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (etUser.getText().toString().length() > 1 && etPwd.getText().toString().length() > 2) {
                btnLogin.setClickable(true);
                btnLogin.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnLogin.setClickable(false);
                btnLogin.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_login://登陆按钮
                final String loginName = etUser.getText().toString().trim();
                final String pwd = etPwd.getText().toString();
                if (loginName.equals("")) {
                    if (etUser.getText().toString().length() > 3) {
                        Toast.makeText(getApplicationContext(), "用户名不能是空格", Toast.LENGTH_SHORT).show();
                    }
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
                        OkHttpUtils.Param nameParam = new OkHttpUtils.Param(NetConstant.login_mobile, loginName);
                        OkHttpUtils.Param pwdParam = new OkHttpUtils.Param(NetConstant.login_password, pwd);
                        List<OkHttpUtils.Param> paramList = new ArrayList<>();
                        paramList.add(nameParam);
                        paramList.add(pwdParam);

                        StringBuffer url = new StringBuffer();
                        url.append(NetConstant.baseUrl);
                        url.append(NetConstant.loginUrl);
                        LogUtils.show( "run: 查看登录请求的信息："+ url+"参数："+ paramList.toString());
                        OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
                            @Override
                            public void onSuccess(String response) {
                                LogUtils.show("LoginActivity-----查看登录界面返回的登录信息："+response);
                                LoginSuccess loginSuccess = new LoginSuccess(LoginActivity.this);
                                OkHttpUtils.Param pwdParam = new OkHttpUtils.Param(NetConstant.login_password, pwd);
                                List<OkHttpUtils.Param> paramList = new ArrayList<>();
                                paramList.add(pwdParam);
                                loginSuccess.doSuccess(response, paramList, mkLoader);
                                isLoginSuccess = true;
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "onFailure: 网络请求失败：" + e.getMessage());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mkLoader.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }, paramList);
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
                Intent forgetIntent = new Intent(this, ForgetPswActivity.class);
                startActivity(forgetIntent);
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void wxLoginCallBack(WxResultMessage message) {
//        if (message.getFlag() == 1) {
            String unionId = message.getUionId();
            final String data = message.getData();
            OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.login_data, data);
            List<OkHttpUtils.Param> paramList = new ArrayList<>();
            paramList.add(param);
            StringBuffer url = new StringBuffer();
            url.append(NetConstant.baseUrl);
            url.append(NetConstant.loginUrl);
            LogUtils.show("查看微信登录请求参数："+paramList.toString()+",链接："+url.toString());
            OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
                @Override
                public void onSuccess(String response) {
                    /**
                     * {"status":"success",
                     * "code":200,
                     * "data":
                     *    {"token":"768d33bae04333cb842088405839c6cc",
                     *    "user_info":
                     *       {"status":200,
                     *        "statusInfo":"ok",
                     *        "data":
                     *          {"userid":"452",
                     *          "username":"xjbank",
                     *          "language":"",
                     *          "name":"xjbank",
                     *          "file":"http:\/\/vitec.oss-cn-shenzhen.aliyuncs.com\/vitec\/locales\/20180830\/用户.png",
                     *          "mobile":"15107620711",
                     *          "projectName":"xj_bank",
                     *          "classification":1,
                     *          "projectImg":"http:\/\/vitec.oss-cn-shenzhen.aliyuncs.com\/vitec\/locales\/20180907logo.png","belong":"506","admin":"0","role":["管理员"],"department":[],"authObj":[{"id":177,"name":"人员定位"},{"id":178,"name":"管理员"},{"id":179,"name":"技术员"}],"auth":[177,178,179],"authName":["人员定位","管理员","技术员"],"project":[{"id":506,"name":"xj_bank"}]}}},
                     *          "msg":"登录成功"}
                     */
                    LogUtils.show( "onSuccess: 查看返回的微信登录信息："+response );
                    LoginSuccess loginSuccess = new LoginSuccess(LoginActivity.this);
                    List<OkHttpUtils.Param> paramList = new ArrayList<>();
                    OkHttpUtils.Param param1 = new OkHttpUtils.Param(DataBaseParams.user_data, data);
                    paramList.add(param1);
                    loginSuccess.doSuccess(response,paramList,mkLoader);
                    LogUtils.show("登录界面----请求成功的回调方法");
                    isLoginSuccess = true;
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "onFailure: 网络请求失败："+e.getMessage() );
                }
            },paramList);
//            }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.show("登录界面---stop");
        if (isLoginSuccess) {
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.show("登录界面---onDestroy");
        EventBus.getDefault().unregister(this);
    }
}
