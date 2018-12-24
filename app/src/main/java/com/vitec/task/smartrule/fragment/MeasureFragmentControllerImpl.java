package com.vitec.task.smartrule.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.interfaces.IFragmentController;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MeasureFragmentControllerImpl implements IFragmentController,BottomNavigationBar.OnTabSelectedListener {

    private static final String TAG = "MeasureFragmentControllerImpl";
    private BottomNavigationBar bottomNavigationBar;
    private int lastSelectedPosition = 0;
    private FragmentActivity activity;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private List<Fragment> fragments;
    private List<String> tags;

    private List<RulerCheckOptions> checkOptionsList;

    public MeasureFragmentControllerImpl(FragmentActivity activity, BottomNavigationBar bottomNavigationBar, List<RulerCheckOptions> checkOptionsList) {

        this.activity = activity;
        this.bottomNavigationBar = bottomNavigationBar;
        this.checkOptionsList = checkOptionsList;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void initBottomNav() {
        bottomNavigationBar.setTabSelectedListener(this)
                .setMode(BottomNavigationBar.MODE_FIXED)
                .setBarBackgroundColor(R.color.white)//选中颜色
                .setInActiveColor(R.color.color_unselect_bar)//未选中颜色
                .setActiveColor(R.color.title_color)//导航栏背景颜色
                .setElevation(0f);//设置阴影
        initFragmentData();
    }

    private void initFragmentData() {
        fragments = new ArrayList<>();
        tags = new ArrayList<>();
        Log.e("sssd", "initFragmentData: 查看checkoptionsList:"+checkOptionsList.size()+",内容："+checkOptionsList.toString() );
        for (int i = 0; i< checkOptionsList.size(); i++) {
            MeasureFragment fragment = new MeasureFragment();
            RulerCheckOptions checkOptions = checkOptionsList.get(i);
            Bundle bundle = new Bundle();
//            bundle.putString(ParameterKey.projectNameKey,checkOptions.getRulerCheck().getProjectName());
//            bundle.putString(ParameterKey.checkPersonKey, checkOptions.getRulerCheck().getUser().getUserName());
//            bundle.putString(ParameterKey.checkPositonKey, checkOptions.getRulerCheck().getCheckFloor());
//            bundle.putString(ParameterKey.projectTypeKey, checkOptions.getRulerCheck().getEngineer().getEngineerName());
//            bundle.putString(ParameterKey.measureItemKey, checkOptions.getRulerOptions().getOptionsName());
//            bundle.putString(ParameterKey.standardKey,checkOptions.getRulerOptions().getStandard());
//            此id对应iot_ruler_check_options表的id
            bundle.putInt(DataBaseParams.options_data_check_options_id, checkOptions.getId());
//            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//            bundle.putString(DataBaseParams.options_data_create_time, currentDateTimeString);
//            bundle.putInt(ParameterKey.resourceIDKey,R.mipmap.icon_data_selected);
            bundle.putSerializable("checkoptions",checkOptions);
            fragment.setArguments(bundle);
            fragments.add(fragment);
            if (checkOptions.getRulerOptions()!=null)
            tags.add(checkOptions.getRulerOptions().getOptionsName());
        }
        if (checkOptionsList.size() == 0) {
            MeasureFragment fragment = new MeasureFragment();
            fragments.add(fragment);
        }
        addBottomNav();
    }

    @Override
    public void addBottomNav() {

        for (int i = 0; i < checkOptionsList.size(); i++) {
            bottomNavigationBar.addItem(new BottomNavigationItem(R.mipmap.icon_black, checkOptionsList.get(i).getRulerOptions().getOptionsName()));
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
        for (int i=0;i<fragments.size();i++) {
            transaction.add(R.id.rl_content, fragments.get(i),tags.get(i));
        }
        for (int i=0;i<fragments.size();i++) {
            transaction.hide(fragments.get(i));
        }
        transaction.show(fragments.get(lastSelectedPosition));
        transaction.commit();
        Log.e("", "setDefaultFragment: 设置默认的faragment，"+lastSelectedPosition );
    }

    /**
     * //未选中->选中
     * @param position
     */
    @Override
    public void onTabSelected(int position) {
          /*
        一般我们来进行fragment页面切换的时候，都是采用replace方法，进行切换，其实replace方就是remove方法和add方法的一个合体，
        使我们的代码变得简单了。可是这里就出现一个问题，这个方法，是移除与添加，也就是说，
        我们在切换的时候，它会重新加载，也就是说如果是读取数据，它就会重新去读数据，重新加载。
        这个在读本地数据库的时候，可能不算什么，可是在读网络数据的时候，这就浪费了流量，所以我们不能使用这个方法，
        我们这里使用hide,show,方法，用隐藏和显示的方法来进行切换。下面是一个通用的方法，代码如下：
         */
        Log.e("TAG", "onTabSelected: 未选中->选中" );
        if (fragments != null && position < fragments.size()) {
            FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            fm.executePendingTransactions();
            Fragment fragment = fragments.get(position);
            Log.e("", "onTabSelected: 查看isAdded："+fragment.isAdded() +"，查看是否被隐藏："+fragment.isHidden());
//            ft.hide(fragments.get(lastSelectedPosition));
            ft.show(fragment);
            Log.e("aaa", "onTabSelected: 修改后。是已经添加过的" );
            ft.commit();

        }
        lastSelectedPosition = position;

    }

    /**
     *   //选中->未选中
     * @param position
     */
    @Override
    public void onTabUnselected(int position) {
        Log.e("onTabUnselected", "onTabUnselected:选中->未选中 ");
        if (fragments != null) {
            if (position < fragments.size()) {
                FragmentManager manager = activity.getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                Fragment fragment = fragments.get(position);
                ft.hide(fragment);
                ft.commitAllowingStateLoss();
                Log.e("aaa", "onTabUnselected:隐藏了一个fragment:"+fragment );

            }
        }

    }

    /**
     * //选中->选中
     * @param position
     */
    @Override
    public void onTabReselected(int position) {

    }
}
