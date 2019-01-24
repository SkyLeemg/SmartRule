package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.MoblieRequestResutEvent;
import com.vitec.task.smartrule.bean.event.UpdateUserMessageResutCallBack;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.ChangeUserMessageIntentService;
import com.vitec.task.smartrule.service.intentservice.RequestMoblieIntentService;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 更换手机号码界面，共有四个步骤：
 * 1.先显示目前绑定的手机号码，是否发送验证码验证身份
 * 2.输入验证码验证身份
 * 3.验证成功显示，点击按钮进入修改手机号码界面
 * 4.更换手机号码界面显示，输入新的手机号码和验证码绑定新的手机号码
 */
public class ChangeMobileActivity extends BaseActivity implements View.OnClickListener{

    private LinearLayout llFirstStep;
    private LinearLayout llSencondStep;
    private LinearLayout llThirdStep;
    private LinearLayout llFourthStep;

    //    第一步相关控件
    private TextView tvFirstMobile;
    private Button btnSendCode;

    //    第二步相关控件
    private TextView tvSencondTip;
    private EditText etMobileCode;
    private Button btnReSendCode;//重发验证码按钮
    private Button btnNext;//下一步按钮

    //    第三步相关控件
    private TextView tvThirdMobile;
    private Button btnUpdate;//更换手机号按钮

    //    第四步相关控件
    private EditText etNewPhone;//新的手机号码
    private EditText etNewCode;//新的手机验证码
    private Button btnGetCode;//获取验证码按钮
    private Button btnSubmit;//最后的确认按钮
    //    加载框
    private MKLoader mkLoader;
//    其他数据对象
    private User user;
    private int countDown;

    private boolean isNewRequest = false;//这个页面有三个请求发送验证码按钮，区分是否为新手机号码的发送按钮
    private StringBuffer oldPhone;
    private boolean canGetCode=true;
    private boolean hasUpdate = false;//用来判断个人信息是否有更新

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_moblie);
        initView();
        initData();
        EventBus.getDefault().register(this);

    }

    private void initData() {
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            User tUser = OperateDbUtil.getUser(getApplicationContext());
            UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
            List<User> userList = new ArrayList<>();
            user = new User();
            String where = " where " + DataBaseParams.user_user_id + " = " + tUser.getUserID() ;
            userList = userDbHelper.queryUserDataFromSqlite(where);
            if (userList.size() > 0) {
                user = userList.get(0);
            } else {
                user = tUser;
            }
        }
        /**
         * 初始化手机号码显示格式：+86138******88
         */
        String phone = user.getMobile();
        oldPhone = new StringBuffer();
        oldPhone.append("+86");
        oldPhone.append(phone.substring(0, 3));
        for (int i=0;i<(phone.length()-5);i++) {
            oldPhone.append("*");
        }
        oldPhone.append(phone.substring((phone.length() - 2), phone.length()));
        tvFirstMobile.setText(oldPhone.toString());

    }

    private void initView() {
        initWidget();
        setTvTitle("修改手机号");
        llFirstStep = findViewById(R.id.ll_first_step);
        llSencondStep = findViewById(R.id.ll_sencond_step);
        llThirdStep = findViewById(R.id.ll_third_step);
        llFourthStep = findViewById(R.id.ll_fourth_step);
        tvFirstMobile = findViewById(R.id.tv_first_mobile);
        btnSendCode = findViewById(R.id.btn_send_code);
        tvSencondTip = findViewById(R.id.tv_sencond_tip);
        etMobileCode = findViewById(R.id.et_mobile_code);
        btnReSendCode = findViewById(R.id.btn_get_mobile_code);
        btnNext = findViewById(R.id.btn_next);
        tvThirdMobile = findViewById(R.id.tv_third_mobile);
        btnUpdate = findViewById(R.id.btn_update);
        etNewCode = findViewById(R.id.et_new_code);
        etNewPhone = findViewById(R.id.et_new_phone);
        btnGetCode = findViewById(R.id.btn_get_code);
        btnSubmit = findViewById(R.id.btn_submit);
        mkLoader = findViewById(R.id.mkloader);
        mkLoader.setVisibility(View.GONE);

        btnSendCode.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnReSendCode.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnGetCode.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        etNewCode.addTextChangedListener(submitTextWatcher);
        etNewPhone.addTextChangedListener(getCodeTextWatcher);

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
            if (canGetCode && etNewPhone.getText().length() == 11) {
                btnGetCode.setClickable(true);
                btnGetCode.setBackgroundResource(R.drawable.btn_nomal);
            } else {
                btnGetCode.setClickable(false);
                btnGetCode.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
            }

            if (etNewPhone.getText().length() == 11 && etNewCode.getText().length() >= 4) {
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
            if (etNewPhone.getText().length() == 11 && etNewCode.getText().length() >= 4) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 退出页面的时候通知上一个页面，数据是否有更新
     */
    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (hasUpdate) {
            intent.putExtra("flag", 1);
        } else {
            intent.putExtra("flag", 0);
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    /**
     * 修改手机号码接口返回的信息
     * @param message
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updatemobileRequestCallBack(UpdateUserMessageResutCallBack message) {
        if (message.isSuccess()) {
            Toast.makeText(getApplicationContext(), "更换成功", Toast.LENGTH_SHORT).show();
            hasUpdate = true;
            onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), message.getMsg(), Toast.LENGTH_SHORT).show();
        }
        mkLoader.setVisibility(View.GONE);
    }


    /**
     * 发送和验证手机验证码的返回信息
     * @param message
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mobileRequestCallBack(MoblieRequestResutEvent message) {
        switch (message.getRequst_flag()) {
            /**
             * 请求发送验证码返回
             */
            case RequestMoblieIntentService.FLAG_REQUEST_MOBLIE_CODE:
//                是否为旧手机号码的验证码返回信息
                if (!isNewRequest) {
                    if (message.isSuccess()) {
//                    隐藏第一步界面，显示第二步界面
                        llFirstStep.setVisibility(View.GONE);
                        llSencondStep.setVisibility(View.VISIBLE);
//                        更新提示信息
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("短信验证码已发送至");
                        stringBuffer.append(oldPhone);
                        stringBuffer.append(" 请注意查收");
                        tvSencondTip.setText(stringBuffer.toString());
//                    同时倒计时60重发验证码
                        countDown = 60;
                        btnReSendCode.setClickable(false);
                        btnReSendCode.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                countDown--;
                                if (countDown > 0) {
                                    btnReSendCode.setText("重新获取(" + countDown + ")");
                                    handler.postDelayed(this, 1000);
                                } else {
                                    btnReSendCode.setText("重新获取");
                                    btnReSendCode.setClickable(true);
                                    btnReSendCode.setBackgroundResource(R.drawable.btn_nomal);
                                }

                            }
                        }, 1000);
                    } else {
                        Toast.makeText(getApplicationContext(), message.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                    mkLoader.setVisibility(View.GONE);
                } else {
//                    新手机号码的验证码返回信息
                    if (message.isSuccess()) {
//                    同时倒计时60重发验证码
                        countDown = 60;
                        btnGetCode.setClickable(false);
                        btnGetCode.setBackgroundResource(R.drawable.shape_btn_gray_unclickable);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                countDown--;
                                canGetCode = false;
                                if (countDown > 0) {
                                    btnGetCode.setText("重新获取(" + countDown + ")");
                                    handler.postDelayed(this, 1000);
                                } else {
                                    btnGetCode.setText("重新获取");
                                    canGetCode = true;
                                    btnGetCode.setClickable(true);
                                    btnGetCode.setBackgroundResource(R.drawable.btn_nomal);
                                }

                            }
                        }, 1000);
                    } else {
                        Toast.makeText(getApplicationContext(), message.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                    mkLoader.setVisibility(View.GONE);
                }

                break;

            /**
             * 验证验证码是否正确的返回信息
             */
            case RequestMoblieIntentService.FLAG_VALIDATE_MOBLIE_CODE:
//                判断是否为第二步返回的信息
                if (!isNewRequest) {
                    if (message.isSuccess()) {
                        llSencondStep.setVisibility(View.GONE);
                        llThirdStep.setVisibility(View.VISIBLE);
                        tvThirdMobile.setText(oldPhone);
                    } else {
                        Toast.makeText(getApplicationContext(), message.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                    mkLoader.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 第一步：发送手机验证码
             */
            case R.id.btn_send_code:
            case R.id.btn_get_mobile_code:
                final  String phone =user.getMobile();
                mkLoader.setVisibility(View.VISIBLE);
                if (phone.length() != 11) {
                    mkLoader.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent(getApplicationContext(), RequestMoblieIntentService.class);
                    intent.putExtra(RequestMoblieIntentService.REQUEST_FLAG, RequestMoblieIntentService.FLAG_REQUEST_MOBLIE_CODE);
                    intent.putExtra(RequestMoblieIntentService.VALUE_MOBLIE, phone);
                    startService(intent);
                }
                break;

            /**
             * 第二步：验证手机验证码输入是否正确
             */
            case R.id.btn_next:
                mkLoader.setVisibility(View.VISIBLE);
                String mobleCodeString = etMobileCode.getText().toString().trim();
                Intent mobileIntent = new Intent(getApplicationContext(), RequestMoblieIntentService.class);
                mobileIntent.putExtra(RequestMoblieIntentService.REQUEST_FLAG, RequestMoblieIntentService.FLAG_VALIDATE_MOBLIE_CODE);
                mobileIntent.putExtra(RequestMoblieIntentService.VALUE_MOBLIE, user.getMobile());
                mobileIntent.putExtra(RequestMoblieIntentService.VALUE_MOBLIE_CODE, mobleCodeString);
                startService(mobileIntent);
                break;

            /**
             * 第三步：更换手机号码按钮
             */
            case R.id.btn_update:
                llThirdStep.setVisibility(View.GONE);
                llFourthStep.setVisibility(View.VISIBLE);
                break;

            /**
             * 第四步：获取手机验证码
             */
            case R.id.btn_get_code:
                final String newPhone = etNewPhone.getText().toString();
                mkLoader.setVisibility(View.VISIBLE);
                if (newPhone.length() != 11) {
                    mkLoader.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent(getApplicationContext(), RequestMoblieIntentService.class);
                    intent.putExtra(RequestMoblieIntentService.REQUEST_FLAG, RequestMoblieIntentService.FLAG_REQUEST_MOBLIE_CODE);
                    intent.putExtra(RequestMoblieIntentService.VALUE_MOBLIE, newPhone);
                    startService(intent);
                }
                break;

            /**
             * 第四步：确定更换手机号码按钮
             */
            case R.id.btn_submit:
                List<OkHttpUtils.Param> paramList = new ArrayList<>();
                OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.mobile_param, etNewPhone.getText().toString().trim());
                OkHttpUtils.Param codeParam = new OkHttpUtils.Param(NetConstant.login_code, etNewCode.getText().toString().trim());
                paramList.add(param);
                paramList.add(codeParam);
                Intent intent = new Intent(ChangeMobileActivity.this, ChangeUserMessageIntentService.class);
                intent.putExtra(ChangeUserMessageIntentService.PARAM_LIST_KEY, (Serializable) paramList);
                startService(intent);
                break;
        }
        
    }
}
