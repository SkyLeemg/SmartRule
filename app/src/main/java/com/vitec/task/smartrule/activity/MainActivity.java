package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.fragment.FragmentControllerImpl;
import com.vitec.task.smartrule.interfaces.IFragmentController;
import com.vitec.task.smartrule.interfaces.ISettable;

public class MainActivity extends FragmentActivity implements ISettable {

    private BottomNavigationBar bottomNavigationBar;
    private IFragmentController controller;
    public TextView tvToolBarTitle;
    public ImageView imgMenu;
    public ImageView imgOtherIcon;

    private LinearLayout llToolBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initView();
    }

    private void initView() {
        initToolBarView();
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar_container);
        controller = new FragmentControllerImpl(this, bottomNavigationBar,this);
        controller.initBottomNav();
        controller.addBottomNav();

    }

    public void initToolBarView() {
        View layout = getLayoutInflater().inflate(R.layout.base_toolbar, null);
        tvToolBarTitle = findViewById(R.id.tv_toolbar_title);
        imgMenu = findViewById(R.id.img_menu_toolbar);
        imgOtherIcon = findViewById(R.id.img_icon_toolbar);

        llToolBar = findViewById(R.id.ll_toolbar);
    }

    @Override
    public void setTitle(String title) {
        tvToolBarTitle.setText(title);
    }

    @Override
    public void setMenuVisible(int flag) {
        imgMenu.setVisibility(flag);
    }

    @Override
    public void setMenuResouce(int resouce) {
        imgMenu.setImageResource(resouce);
    }

    @Override
    public void setIconVisible(int flag) {
        imgOtherIcon.setVisibility(flag);
    }

    @Override
    public void setIconResouce(int resouce) {
        imgOtherIcon.setImageResource(resouce);
    }

    @Override
    public ISettable getSettable() {
        return this;
    }

    @Override
    public void setToolBarVisible(int flag) {
//        toolbar.setVisibility(flag);
        llToolBar.setVisibility(flag);
        if (flag == View.GONE) {
            llToolBar.setBackgroundResource(R.color.transparent_color);
        } else {
            llToolBar.setBackgroundResource(R.color.pblue_bar_color);
        }
    }


}
