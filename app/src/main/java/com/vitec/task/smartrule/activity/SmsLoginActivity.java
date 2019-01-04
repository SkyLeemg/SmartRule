package com.vitec.task.smartrule.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.User;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scut.carson_ho.diy_view.SuperEditText;

public class SmsLoginActivity extends BaseActivity implements View.OnClickListener{


    private static final String TAG = "SmsLoginActivity";
    private Button btnGetMobileCode;
    private Button btnLogin;
    private EditText etMobileCode;
    private EditText etMobile;
    private TextView tvPwdLogin;
    private TextView tvForgetPsw;
    private MKLoader mkLoader;
    private int countDown = 60;
    private UserDbHelper userDbHelper;
    private TextView tvRegister;
    private ImageView imgWechat;
    private boolean canGetCode = true;//当前状态是否可以获取验证码

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_login);
        intView();

    }

    private void intView() {
        btnGetMobileCode = findViewById(R.id.btn_login_get_mobile_code);
        btnLogin = findViewById(R.id.btn_login);
        etMobile = findViewById(R.id.et_login_phone);
        etMobileCode = findViewById(R.id.et_login_mobile_code);
        tvPwdLogin = findViewById(R.id.tv_pwd_login);
        mkLoader = findViewById(R.id.mkloader);
        tvRegister = findViewById(R.id.tv_register);
        imgWechat = findViewById(R.id.img_wechat);
        tvForgetPsw = findViewById(R.id.cb_remenber_pw);

        btnLogin.setOnClickListener(this);
        btnGetMobileCode.setOnClickListener(this);
        tvPwdLogin.setOnClickListener(this);
        imgWechat.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForgetPsw.setOnClickListener(this);

        btnLogin.setClickable(false);
        btnGetMobileCode.setClickable(false);
        etMobile.addTextChangedListener(getCodeTextWatcher);
        etMobileCode.addTextChangedListener(submitTextWatcher);
    }

    /**
     * 监听手机号码输入状态
     */
    private TextWatcher getCodeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (canGetCode && etMobile.getText().length() == 11) {
                btnGetMobileCode.setClickable(true);
                btnGetMobileCode.setBackgroundResource(R.drawable.btn_nomal);
            } else {
                btnGetMobileCode.setClickable(false);
                btnGetMobileCode.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
            }

            if (etMobileCode.getText().length() >= 4 && etMobile.getText().length() == 11) {
                btnLogin.setClickable(true);
                btnLogin.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnLogin.setClickable(false);
                btnLogin.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
            }
        }
    };

    /**
     * 监听手机验证码输入状态。
     */
    private TextWatcher submitTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (etMobileCode.getText().length() >= 4 && etMobile.getText().length() == 11) {
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
            /**
             * 微信登录
             */
            case R.id.img_wechat:
                mkLoader.setVisibility(View.VISIBLE);
                WeChatHelper  weChatHelper = new WeChatHelper(this);
                weChatHelper.regToWx();
                weChatHelper.sendLoginRequest();
                break;

            /**
             * 注册
             */
            case R.id.tv_register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
//                this.finish();
                break;

            case R.id.cb_remenber_pw:
                Intent forgetIntent = new Intent(this, ForgetPswActivity.class);
                startActivity(forgetIntent);
                break;

            /**
             * 获取验证码
             */
            case R.id.btn_login_get_mobile_code:
                final  String phone = etMobile.getText().toString().trim();
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
                                        int code = jsonObject.optInt("code");
                                        Log.e("aaa", "onSuccess: 获取验证码成功："+response );
                                        final String msg = jsonObject.optString("msg");
                                        countDown = 60;
                                        if (code == 200) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mkLoader.setVisibility(View.GONE);
                                                    canGetCode = false;
                                                    btnGetMobileCode.setClickable(false);
                                                    btnGetMobileCode.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
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
                                                                canGetCode = true;
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
                                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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

            /**
             * 登录
             */
            case R.id.btn_login:
                final String mobile = etMobile.getText().toString().trim();
                final String code = etMobileCode.getText().toString().trim();
                mkLoader.setVisibility(View.VISIBLE);
                if (mobile.length() != 11) {
                    mkLoader.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                }
                if (code.equals("")) {
                    mkLoader.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "验证码不能为空", Toast.LENGTH_SHORT).show();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpUtils.Param mobileParam = new OkHttpUtils.Param(NetConstant.login_mobile, mobile);
                        OkHttpUtils.Param codeParam = new OkHttpUtils.Param(NetConstant.login_code, code);
                        List<OkHttpUtils.Param> paramList = new ArrayList<>();
                        paramList.add(mobileParam);
                        paramList.add(codeParam);
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(NetConstant.baseUrl);
                        stringBuffer.append(NetConstant.loginUrl);
                        OkHttpUtils.post(stringBuffer.toString(), new OkHttpUtils.ResultCallback<String>() {
                            @Override
                            public void onSuccess(String response) {
                                LoginSuccess loginSuccess = new LoginSuccess(SmsLoginActivity.this);
                                List<OkHttpUtils.Param> paramList = new ArrayList<>();
                                loginSuccess.doSuccess(response,paramList,mkLoader);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mkLoader.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        },paramList);

                    }
                }).start();

                break;

            /**
             * 用密码登录
             */
            case R.id.tv_pwd_login:
                startActivity(new Intent(this,LoginActivity.class));
                this.finish();
                break;
        }
    }
}
