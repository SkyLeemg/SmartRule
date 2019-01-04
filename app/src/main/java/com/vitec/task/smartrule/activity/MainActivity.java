package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.fragment.HomePageFragment;
import com.vitec.task.smartrule.fragment.UserCenterFragment;
import com.vitec.task.smartrule.service.intentservice.GetMudelIntentService;
import com.vitec.task.smartrule.service.intentservice.ReplenishDataToServerIntentService;
import com.vitec.task.smartrule.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private LinearLayout llHome;
    private LinearLayout llMe;
    private TextView tvHome;
    private TextView tvMe;
    private ImageView imgHome;
    private ImageView imgMe;
    private FrameLayout rlContent;
    private List<Fragment> fragmentList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        requestLocationPermissions();
        initView();
        initData();
    }

    private void initData() {
        Intent intent = new Intent(this, GetMudelIntentService.class);
        startService(intent);

        fragmentList = new ArrayList<>();
        fragmentList.add(new HomePageFragment());
        fragmentList.add(new UserCenterFragment());
        llMe.setOnClickListener(this);
        llHome.setOnClickListener(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i=0;i<fragmentList.size();i++) {
            transaction.add(R.id.rl_main_content, fragmentList.get(i));
        }
        transaction.hide(fragmentList.get(1));
        transaction.show(fragmentList.get(0));
        transaction.commit();


        /**
         * 补上传服务启动
         */
        Intent replenishIntent = new Intent(getApplicationContext(), ReplenishDataToServerIntentService.class);
        startService(replenishIntent);

    }

    private void initView() {
        llHome = findViewById(R.id.ll_home);
        llMe = findViewById(R.id.ll_me);
        tvHome = findViewById(R.id.tv_home);
        tvMe = findViewById(R.id.tv_me);
        imgHome = findViewById(R.id.img_home);
        imgMe = findViewById(R.id.img_me);
        rlContent = findViewById(R.id.rl_main_content);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 点击首页
             */
            case R.id.ll_home:
                LogUtils.show("点击了首页-----"+fragmentList.get(0).isHidden());
                if (fragmentList.get(0).isHidden()) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    fm.executePendingTransactions();
                    ft.hide(fragmentList.get(1));
                    Fragment fragment = fragmentList.get(0);
                    ft.show(fragment);
                    ft.commit();
                    tvHome.setTextColor(Color.rgb(53,129,251));
                    imgHome.setImageResource(R.mipmap.ico_home_sel_3x);
                    tvMe.setTextColor(Color.rgb(162,162,162));
                    imgMe.setImageResource(R.mipmap.ico_me_n_3x);
                }

                break;
            /**
             * 点击我的个人中心
             */
            case R.id.ll_me:
                LogUtils.show("点击了个人中心-----"+fragmentList.get(1).isHidden());
                if (fragmentList.get(1).isHidden()) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    fm.executePendingTransactions();
                    ft.hide(fragmentList.get(0));
                    Fragment fragment = fragmentList.get(1);
                    ft.show(fragment);
                    ft.commit();
                    tvMe.setTextColor(Color.rgb(53,129,251));
                    imgMe.setImageResource(R.mipmap.ico_me_s_3x);
                    tvHome.setTextColor(Color.rgb(162,162,162));
                    imgHome.setImageResource(R.mipmap.ico_home_not_3x);

                }
                break;
        }
    }
}
