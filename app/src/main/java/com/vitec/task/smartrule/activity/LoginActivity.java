package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
//import com.tencent.mm.opensdk.openapi.WXTextObject;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.db.CopyDbFileFromAsset;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.helper.WeChatHelper;

import java.io.IOException;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private Button btnLogin;
    private EditText etUser;
    private EditText etPwd;
    private CheckBox cbRemenberPwd;
    private TextView tvRegister;
    private ImageView imgWechat;
    private WeChatHelper weChatHelper;
    private CopyDbFileFromAsset copyDbFileFromAsset;

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
        cbRemenberPwd = findViewById(R.id.cb_remenber_pw);
        tvRegister = findViewById(R.id.tv_register);
        imgWechat = findViewById(R.id.img_wechat);

        btnLogin.setOnClickListener(this);
        imgWechat.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login://登陆按钮
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_register:

                break;
            case R.id.img_wechat:
                weChatHelper.regToWx();
                weChatHelper.sendLoginRequest();
                break;
        }
    }
}
