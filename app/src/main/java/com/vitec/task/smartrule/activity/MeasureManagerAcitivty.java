package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.EngineerBean;
import com.vitec.task.smartrule.bean.OptionBean;
import com.vitec.task.smartrule.fragment.MeasureFragmentControllerImpl;

import java.util.ArrayList;
import java.util.List;

public class MeasureManagerAcitivty extends BaseFragmentActivity {

    private static final String TAG = "MeasureManagerAcitivty";
    private BottomNavigationBar bottomNavigationBar;
    private MeasureFragmentControllerImpl controller;
    public TextView tvToolBarTitle;
    public ImageView imgMenu;
    public ImageView imgOtherIcon;
    private MKLoader mkLoader;
    private LinearLayout llToolBar;
    private List<OptionBean> measureBeanList;

    private EngineerBean engineerBean;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        initView();
    }

    private void initView() {
        initToolBarView();
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar_container);
        mkLoader = findViewById(R.id.loading);
        initData();
    }

    private void initData() {
        measureBeanList = new ArrayList<>();
        /**
         * 下面的每一个MeasureBean的数据，就是每一个Fragment页面上需要显示的数据
         * int projectID, String projectName, String projectType, String checkPositon,
         String checkPerson, String checkTime, String measureItemName
         */
//        获取上一个类（ChooseMeasureProjectAdapter）传过来的数据
        engineerBean = (EngineerBean) getIntent().getSerializableExtra("projectMsg");
//        OptionBean measureBean1 = (OptionBean) getIntent().getSerializableExtra("projectMsg");
//        OptionBean measureBean = new OptionBean(1, "混凝土工程测量", "立面垂直度", "A栋2层",
//                "张三", "2018-10-20", "垂直度");
//        measureBean.setResourceID(R.mipmap.icon_intro_selected);
//        measureBean.setMeasureItem("立面垂直度");
//        OptionBean measureBean2 = new OptionBean(1, "混凝土工程测量", "平整度", "A栋2层",
//                "张三", "2018-10-20", "平整度");
//        measureBean2.setResourceID(R.mipmap.icon_intro_selected);
//        measureBean2.setMeasureItem("表面平整度");
//        measureBeanList.add(measureBean);
//        measureBeanList.add(measureBean2);
        Log.e(TAG, "initData: 测量界面接受到的数据信息：" + engineerBean.toString());
        measureBeanList = engineerBean.getMeasureBeanList();
        controller = new MeasureFragmentControllerImpl(this,bottomNavigationBar,engineerBean);
        controller.initBottomNav();
        controller.addBottomNav();
    }

    public void initToolBarView() {
        tvToolBarTitle = findViewById(R.id.tv_toolbar_title);
        imgMenu = findViewById(R.id.img_menu_toolbar);
        imgOtherIcon = findViewById(R.id.img_icon_toolbar);
        llToolBar = findViewById(R.id.ll_toolbar);
    }
}
