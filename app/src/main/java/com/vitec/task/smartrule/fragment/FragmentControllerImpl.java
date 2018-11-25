package com.vitec.task.smartrule.fragment;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.interfaces.IFragmentController;
import com.vitec.task.smartrule.interfaces.ISettable;

import java.util.ArrayList;
import java.util.List;

public class FragmentControllerImpl implements IFragmentController, BottomNavigationBar.OnTabSelectedListener {

    private static final String TAG = "FragmentControllerImpl";
    private BottomNavigationBar bottomNavigationBar;
    private int lastSelectedPosition = 1;
    private Context context;
    private FragmentActivity activity;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private List<Fragment> fragments;
    private List<String> tags;
    private ISettable settable;

    public FragmentControllerImpl(FragmentActivity activity, BottomNavigationBar bottomNavigationBar,ISettable settable) {
        this.activity = activity;
        this.bottomNavigationBar = bottomNavigationBar;
        this.settable = settable;
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
        fragments.add(new HomePageFragment());
        fragments.add(new DeviceManagerFragment());
        fragments.add(new CheckDataFragment());
        fragments.add(new InstructionsFragment());
        fragments.add(new UserCenterFragment());
        tags = new ArrayList<>();
        tags.add("home");
        tags.add("device");
        tags.add("check");
        tags.add("instructions");
        tags.add("user");
    }


    @Override
    public void addBottomNav() {
        bottomNavigationBar.addItem(new BottomNavigationItem(R.mipmap.icon_home_unselected, "首页"))
                .addItem(new BottomNavigationItem(R.mipmap.icon_manager_unselected, "设备管理"))
                .addItem(new BottomNavigationItem(R.mipmap.icon_data_unselected, "查看数据"))
                .addItem(new BottomNavigationItem(R.mipmap.icon_intro_unselected, "使用说明"))
                .addItem(new BottomNavigationItem(R.mipmap.icon_user_unselect, "个人中心"))
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();//定要放在 所有设置的最后一项
        setDefaultFragment();

    }

    @Override
    public void setDefaultFragment() {
        fragmentManager = activity.getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
//        myTasksFragment = new MyTasksFragment();
        fragmentManager.executePendingTransactions();
        for (int i=0;i<fragments.size();i++) {
            transaction.add(R.id.rl_content, fragments.get(i),tags.get(i));
        }
        for (int i=0;i<fragments.size();i++) {
            transaction.hide(fragments.get(i));
        }
        transaction.show(fragments.get(lastSelectedPosition));
        transaction.commit();
        Log.e(TAG, "setDefaultFragment: 设置默认的faragment，"+lastSelectedPosition );
        setCurrentTitle();
    }

    private void setCurrentTitle() {
        switch (lastSelectedPosition) {
            case 0:
                settable.setTitle("首页");
                settable.setToolBarVisible(View.VISIBLE);
                settable.setIconVisible(View.VISIBLE);
                break;
            case 1:
                settable.setTitle("设备管理");
                settable.setToolBarVisible(View.VISIBLE);
                break;
            case 2:
                settable.setTitle("查看数据");
                settable.setToolBarVisible(View.VISIBLE);
                break;
            case 3:
                settable.setTitle("使用说明");
                settable.setToolBarVisible(View.VISIBLE);
                break;
            case 4:
                settable.setTitle("个人中心");
                settable.setToolBarVisible(View.GONE);
                break;
            default:
                break;
        }
    }



    @Override
    public void onTabSelected(int position) {
        Log.e("TAG", "onTabSelected: 未选中->选中" );
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
            Log.e(TAG, "onTabSelected: 查看isAdded："+fragment.isAdded() +"，查看是否被隐藏："+fragment.isHidden());
//            if (fragment.isAdded() || fm.findFragmentByTag(tags.get(position))!=null) {
////                ft.replace(R.id.ll_content, fragment);
////                如果fragment已经被添加过了，则隐藏上一次fragment 显示现在这个fragment
//                ft.hide(fragments.get(lastSelectedPosition));
                ft.show(fragment);
//                Log.e(TAG, "onTabSelected: 是已经添加过的" );
//            } else {
////                ft.add(R.id.ll_content, fragment);
////                如果fragment还未被添加，则隐藏上一个fragment，添加现在的fragment
//
//                ft.hide(fragments.get(lastSelectedPosition)).add(R.id.rl_content, fragment,tags.get(position));
//            }
            ft.commit();

        }
        lastSelectedPosition = position;
        setCurrentTitle();

    }

    /**
     * //选中->未选中
     * @param position
     */
    @Override
    public void onTabUnselected(int position) {
        Log.e(TAG, "onTabUnselected: //选中->未选中");
        if (fragments != null) {
            if (position < fragments.size()) {
                FragmentManager manager = activity.getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                Fragment fragment = fragments.get(position);
                ft.hide(fragment);
                ft.commitAllowingStateLoss();
                Log.e(TAG, "onTabUnselected: 隐藏成功："+fragments.size()+",当前fragment:"+fragment );
            }
        }
    }

    @Override
    public void onTabReselected(int position) {

    }
}
