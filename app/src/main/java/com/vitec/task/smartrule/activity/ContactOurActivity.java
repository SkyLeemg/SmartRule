package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.vitec.task.smartrule.R;

/**
 * 个人中心--联系我们的界面
 */
public class ContactOurActivity extends BaseActivity implements View.OnClickListener{

    private TextView tvNetAddress;//官网地址
    private TextView tvNetMallAddress;//商城地址
    private TextView tvPhone;//联系电话
    private TextView tvEmail;//联系邮箱
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_our);
        initView();
        initData();
    }

    /**
     * 还差一个后台接口，获取信息显示出来
     */
    private void initData() {

    }

    private void initView() {
        initWidget();
        setTvTitle("联系我们");
        imgIcon.setImageResource(R.mipmap.icon_back);
        imgIcon.setVisibility(View.VISIBLE);
        imgIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_icon_toolbar:
                ContactOurActivity.this.finish();
                break;
        }
    }
}
