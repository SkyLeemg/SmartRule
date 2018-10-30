package com.vitec.task.smartrule.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.MeasureBean;
import com.vitec.task.smartrule.interfaces.IFragmentController;
import com.vitec.task.smartrule.interfaces.ISettable;
import com.vitec.task.smartrule.utils.ParameterKey;

import java.util.ArrayList;
import java.util.List;

public class MeasureFragmentControllerImpl implements IFragmentController,BottomNavigationBar.OnTabSelectedListener {

    private static final String TAG = "MeasureFragmentControllerImpl";
    private BottomNavigationBar bottomNavigationBar;
    private int lastSelectedPosition = 0;
    private Context context;
    private FragmentActivity activity;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private List<Fragment> fragments;
    private List<String> tags;
    private ISettable settable;
    private List<MeasureBean> measureBeanList;

    public MeasureFragmentControllerImpl(FragmentActivity activity, BottomNavigationBar bottomNavigationBar, ISettable settable) {
        this.activity = activity;
        this.bottomNavigationBar = bottomNavigationBar;
        this.settable = settable;
    }

    public MeasureFragmentControllerImpl(FragmentActivity activity, BottomNavigationBar bottomNavigationBar, List<MeasureBean> measureBeanList) {
        this.activity = activity;
        this.bottomNavigationBar = bottomNavigationBar;
        this.measureBeanList = measureBeanList;
    }


    @Override
    public void initBottomNav() {
        bottomNavigationBar.setTabSelectedListener(this)
                .setMode(BottomNavigationBar.MODE_DEFAULT)
                .setBarBackgroundColor(R.color.pblue_bar_color)//选中颜色
                .setInActiveColor(R.color.word_color)//未选中颜色
                .setActiveColor(R.color.gray_bottom_nav_bg_color);//导航栏背景颜色
        initFragmentData();
    }

    private void initFragmentData() {
        fragments = new ArrayList<>();
        tags = new ArrayList<>();
        for (int i=0;i<measureBeanList.size();i++) {
            MeasureFragment fragment = new MeasureFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ParameterKey.projectNameKey,measureBeanList.get(i).getProjectName());
            bundle.putString(ParameterKey.checkPersonKey,measureBeanList.get(i).getCheckPerson());
            bundle.putString(ParameterKey.checkPositonKey,measureBeanList.get(i).getCheckPositon());
            bundle.putString(ParameterKey.projectTypeKey,measureBeanList.get(i).getProjectType());
            bundle.putString(ParameterKey.measureItemKey,measureBeanList.get(i).getMeasureItem());
            fragment.setArguments(bundle);
            fragments.add(fragment);
            tags.add(measureBeanList.get(i).getMeasureItemName());
        }

    }

    @Override
    public void addBottomNav() {

        for (int i=0;i<measureBeanList.size();i++) {
            bottomNavigationBar.addItem(new BottomNavigationItem(measureBeanList.get(i).getResourceID(), measureBeanList.get(i).getMeasureItemName()));
        }
        bottomNavigationBar.setFirstSelectedPosition(lastSelectedPosition)
                .initialise();//定要放在 所有设置的最后一项
        setDefaultFragment();

    }

    @Override
    public void setDefaultFragment() {
        fragmentManager = activity.getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
//        myTasksFragment = new MyTasksFragment();
        fragmentManager.executePendingTransactions();
        transaction.add(R.id.rl_content, fragments.get(lastSelectedPosition),tags.get(lastSelectedPosition));
        transaction.commit();
        Log.e("", "setDefaultFragment: 设置默认的faragment，"+lastSelectedPosition );
    }

    @Override
    public void onTabSelected(int position) {
          /*
        一般我们来进行fragment页面切换的时候，都是采用replace方法，进行切换，其实replace方就是remove方法和add方法的一个合体，
        使我们的代码变得简单了。可是这里就出现一个问题，这个方法，是移除与添加，也就是说，
        我们在切换的时候，它会重新加载，也就是说如果是读取数据，它就会重新去读数据，重新加载。
        这个在读本地数据库的时候，可能不算什么，可是在读网络数据的时候，这就浪费了流量，所以我们不能使用这个方法，
        我们这里使用hide,show,方法，用隐藏和显示的方法来进行切换。下面是一个通用的方法，代码如下：
         */
        if (fragments != null && position < fragments.size()) {
            FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            fm.executePendingTransactions();
            Fragment fragment = fragments.get(position);
            Log.e("", "onTabSelected: 查看isAdded："+fragment.isAdded() +"，查看是否被隐藏："+fragment.isHidden());
            /**
             * TODO 有一个问题isAdded()一直返回false
             */
            if (fragment.isAdded() || fm.findFragmentByTag(tags.get(position))!=null) {
//                ft.replace(R.id.ll_content, fragment);
//                如果fragment已经被添加过了，则隐藏上一次fragment 显示现在这个fragment
                ft.hide(fragments.get(lastSelectedPosition));
                ft.show(fragment);
                Log.e("", "onTabSelected: 是已经添加过的" );
            } else {
//                ft.add(R.id.ll_content, fragment);
//                如果fragment还未被添加，则隐藏上一个fragment，添加现在的fragment

                Log.e("", "onTabSelected: 没有添加过的" );
                ft.hide(fragments.get(lastSelectedPosition)).add(R.id.rl_content, fragment,tags.get(position));
            }
//            ft.commitAllowingStateLoss();
            ft.commit();
//            Log.e(TAG, "onTabSelected: 查看当前的位置："+ position+",Name:");

        }
        lastSelectedPosition = position;

    }

    @Override
    public void onTabUnselected(int position) {
        if (fragments != null) {
            if (position < fragments.size()) {
                FragmentManager manager = activity.getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                Fragment fragment = fragments.get(position);
                ft.remove(fragment);
                ft.commitAllowingStateLoss();

            }
        }
    }

    @Override
    public void onTabReselected(int position) {

    }
}
