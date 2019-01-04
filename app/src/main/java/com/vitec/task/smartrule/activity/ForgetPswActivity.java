package com.vitec.task.smartrule.activity;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForgetPswActivity extends BaseActivity implements View.OnClickListener{

    private TextView tvTip;
    private LinearLayout llPhone;
    private LinearLayout llCode;
    private LinearLayout llPsw;
    private LinearLayout llRepeatPsw;
    private EditText etPhone;
    private EditText etCode;
    private EditText etPsw;
    private EditText etRepeatPsw;
    private Button btnGetCode;
    private Button btnSubmit;
    private MKLoader mkLoader;
    private int countDown;
    private boolean canGetCode = true;//当前状态是否可以获取验证码
    private final String next_string = "下一步";
    private final String submit_string = "确定修改";
    private String mobleCodeString;
    private String mobleString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        initView();
    }

    private void initView() {
        initWidget();
        setTvTitle("修改密码");

        tvTip = findViewById(R.id.tv_tip);
        llPhone = findViewById(R.id.ll_phone);
        llCode = findViewById(R.id.ll_code);
        llPsw = findViewById(R.id.ll_psw);
        llRepeatPsw = findViewById(R.id.ll_repeat_psw);
        etPhone = findViewById(R.id.et_phone);
        etCode = findViewById(R.id.et_mobile_code);
        etPsw = findViewById(R.id.et_psw);
        etRepeatPsw = findViewById(R.id.et_repeat_pwd);
        btnGetCode = findViewById(R.id.btn_get_mobile_code);
        btnSubmit = findViewById(R.id.btn_submit);
        mkLoader = findViewById(R.id.mkloader);
        btnSubmit.setText(next_string);

        llRepeatPsw.setVisibility(View.GONE);
        llPsw.setVisibility(View.GONE);

        btnSubmit.setOnClickListener(this);
        btnGetCode.setOnClickListener(this);
        imgMenu.setOnClickListener(this);

        etPhone.addTextChangedListener(getCodeTextWatcher);
        etCode.addTextChangedListener(submitTextWatcher);
        btnSubmit.setClickable(false);

        etPsw.addTextChangedListener(pswTextWatcher);
        etRepeatPsw.addTextChangedListener(pswTextWatcher);

    }

    /**
     * 监听密码输入状态
     */
    private TextWatcher pswTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (etPsw.getText().toString().length() > 2 && etPsw.getText().toString().equals(etRepeatPsw.getText().toString())) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
            }
        }
    };

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
            if (canGetCode && etPhone.getText().length() == 11) {
                btnGetCode.setClickable(true);
                btnGetCode.setBackgroundResource(R.drawable.btn_nomal);
            } else {
                btnGetCode.setClickable(false);
                btnGetCode.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
            }

            if (etPhone.getText().length() == 11 && etCode.getText().length() >= 4) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
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
            if (etPhone.getText().length() == 11 && etCode.getText().length() >= 4) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 返回按钮
             */
            case R.id.img_menu_toolbar:
                onBackPressed();
                break;

            /**
             * 获取验证码
             */
            case R.id.btn_get_mobile_code:
                final  String phone = etPhone.getText().toString().trim();
                mkLoader.setVisibility(View.VISIBLE);
                if (phone.length() != 11) {
                    mkLoader.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                    return;
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
                                                    btnGetCode.setClickable(false);
                                                    btnGetCode.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
                                                    canGetCode = false;
                                                    final Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            countDown--;
                                                            if (countDown > 0) {
                                                                btnGetCode.setText("重新获取(" + countDown + ")");
                                                                handler.postDelayed(this, 1000);
                                                            } else {
                                                                btnGetCode.setText("重新获取");
                                                                btnGetCode.setClickable(true);
                                                                btnGetCode.setBackgroundResource(R.drawable.btn_nomal);
                                                                canGetCode = true;
                                                            }

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
             * 确定按钮
             */
            case R.id.btn_submit:
                /**
                 * 验证手机验证码，【下一步】按钮
                 */
                mkLoader.setVisibility(View.VISIBLE);
                if (btnSubmit.getText().toString().trim().equals(next_string)) {
                    mobleString= etPhone.getText().toString();
                    mobleCodeString = etCode.getText().toString().trim();
                    if (mobleString.length() != 11) {
                        mkLoader.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final List<OkHttpUtils.Param> paramList = new ArrayList<>();
                    OkHttpUtils.Param mobileParam = new OkHttpUtils.Param(NetConstant.mobile_param, mobleString);
                    OkHttpUtils.Param codeParam = new OkHttpUtils.Param(NetConstant.register_code, mobleCodeString);
                    paramList.add(mobileParam);
                    paramList.add(codeParam);
                    final String url = NetConstant.baseUrl + NetConstant.validateMobileCodeUrl;
                    LogUtils.show("请求前的参数和连接："+url+",参数："+paramList.toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
                                @Override
                                public void onSuccess(String response) {
                                    try {
                                        LogUtils.show("onSuccess-----打印查看返回信息："+response);
                                        JSONObject jsonObject = new JSONObject(response);
                                        final int code = jsonObject.optInt("code");
                                        final String msg = jsonObject.optString("msg");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (code == 200) {
                                                    btnSubmit.setText(submit_string);
                                                    btnSubmit.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
                                                    btnSubmit.setClickable(false);
                                                    llCode.setVisibility(View.GONE);
                                                    llPhone.setVisibility(View.GONE);
                                                    llPsw.setVisibility(View.VISIBLE);
                                                    llRepeatPsw.setVisibility(View.VISIBLE);
                                                } else {
//                                                    mkLoader.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                                }
                                                mkLoader.setVisibility(View.GONE);

                                            }
                                        });

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mkLoader.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(),"服务器请求失败",Toast.LENGTH_SHORT).show();
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
                            }, paramList);
                        }
                    }).start();

                    return;
                }

                /**
                 * 修改密码，
                 */
                if (btnSubmit.getText().toString().trim().equals(submit_string)) {
                    final List<OkHttpUtils.Param> paramList = new ArrayList<>();
                    OkHttpUtils.Param mobileParam = new OkHttpUtils.Param(NetConstant.mobile_param, mobleString);
                    OkHttpUtils.Param codeParam = new OkHttpUtils.Param(NetConstant.register_code, mobleCodeString);
                    OkHttpUtils.Param pswParam = new OkHttpUtils.Param(NetConstant.change_pwd_password, etPsw.getText().toString());
                    paramList.add(mobileParam);
                    paramList.add(codeParam);
                    paramList.add(pswParam);
                }



                break;
        }
    }
}
