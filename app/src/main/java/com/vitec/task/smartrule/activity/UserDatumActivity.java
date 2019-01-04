package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDatumActivity extends BaseActivity implements View.OnClickListener{

    private RelativeLayout rlImgHead;
    private RelativeLayout rlUserName;
    private RelativeLayout rlPhone;
    private RelativeLayout rlUserWx;
    private TextView tvUserName;
    private TextView tvPhone;
    private TextView tvUserWx;
    private CircleImageView headCircleImg;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvitiy_user_msg);
        initView();

    }

    private void initView() {
        initWidget();
        setTvTitle("个人资料");

        rlImgHead = findViewById(R.id.rl_img_head);
        rlPhone = findViewById(R.id.rl_phone);
        rlUserName = findViewById(R.id.rl_user_name);
        rlUserWx = findViewById(R.id.rl_wx);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserWx = findViewById(R.id.tv_user_wx);
        tvPhone = findViewById(R.id.tv_phone);
        headCircleImg = findViewById(R.id.cir_img_head);

        rlUserName.setOnClickListener(this);
        rlPhone.setOnClickListener(this);
        rlUserWx.setOnClickListener(this);
        rlImgHead.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }
}
