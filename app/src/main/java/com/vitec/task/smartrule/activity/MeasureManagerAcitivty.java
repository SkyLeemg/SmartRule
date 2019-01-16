package com.vitec.task.smartrule.activity;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragAndDropPermissions;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.MeasureFragmentPagerAdapter;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.fragment.MeasureFragment;
import com.vitec.task.smartrule.fragment.MeasureFragmentControllerImpl;
import com.vitec.task.smartrule.fragment.MeasureFragmentOthers;
import com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeasureManagerAcitivty extends BaseFragmentActivity {

    private static final String TAG = "MeasureManagerAcitivty";
    private BottomNavigationBar bottomNavigationBar;
    private MeasureFragmentControllerImpl controller;
    public ImageView imgMenu;
    public TextView tvTitle;
    public ImageView imgIcon;
//    private MKLoader mkLoader;
    private RelativeLayout llToolBar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private RulerCheck rulerCheck;//接收上个页面传来的rulercheck对象
    private List<RulerCheckOptions> checkOptionsList;//rulerCheckOptions集合，一个rulerCheckOption对象对应接下来的一个fragment页面
    private RulerCheckOptions rulerCheckOption;//
    private BleDataDbHelper bleDataDbHelper;//数据库查询helper
    private List<android.support.v4.app.Fragment> fragments;
    private List<String> tags;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        initView();
    }

    private void initView() {
        initToolBarView();
//        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar_container);
//        mkLoader = findViewById(R.id.loading);
        tabLayout = findViewById(R.id.tablayout_meausre);
        viewPager = findViewById(R.id.viewpage_measure);
        initData();
    }


    private void initData() {
        /**
         * 下面的每一个MeasureBean的数据，就是每一个Fragment页面上需要显示的数据
         * int projectID, String projectName, String projectType, String checkPositon,
         String checkPerson, String checkTime, String measureItemName
         */
//        获取上一个类（ChooseMeasureProjectAdapter）传过来的数据
        rulerCheck = (RulerCheck) getIntent().getSerializableExtra("projectMsg");
//        int chooseIndex = getIntent().getIntExtra("floor_height", 0);
        OptionMeasure chooseFloorHeight = (OptionMeasure) getIntent().getSerializableExtra("floor_height");
        Log.e(TAG, "initData: 查看收到的rulercheck:"+rulerCheck.toString() );
        checkOptionsList = new ArrayList<>();
//        rulerCheckOption = new RulerCheckOptions();
//        rulerCheckOption.setRulerCheck(rulerCheck);
        bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        tvTitle.setText(rulerCheck.getEngineer().getEngineerName());

        /**
         * 先查找Iot_ruler_check_options表格有没有同样checkid的，如果有则，不创建直接用之前的
         */
        if (!queryData()) {
            /**
             * 1.根据engineer_id查找该ID对应的Options
             * 2.遍历返回的rulerOptions集合，创建rulerCheckOptions对象，填充对应的信息
             * 3.将集合中的每个rulerCheckOptions对象添加到数据库，返回id更新到对应的对象中
             */
            Log.e(TAG, "initData: 查找之前查看rulerCheck对象：" + rulerCheck.toString());
//            String where = " where " + DataBaseParams.options_engin_id + " = " + rulerCheck.getEngineer().getServerID();
//            Log.e(TAG, "initData: 查看where语句：" + where);
            List<RulerOptions> optionsList = rulerCheck.getEngineer().getOptionsList();
            Log.e(TAG, "initData: 打印根据ID搜出来RulerOptions模板:" + optionsList.size() + ",内容：" + optionsList.toString());
            for (RulerOptions rulerOption : optionsList) {
                RulerCheckOptions rulerCheckOption = new RulerCheckOptions();
                rulerCheckOption.setCreateTime((int) System.currentTimeMillis());
                rulerCheckOption.setRulerCheck(rulerCheck);
                rulerCheckOption.setRulerOptions(rulerOption);
                rulerCheckOption.setUpload_flag(0);
//                rulerCheckOption.setFloorHeight(chooseFloorHeight);
                List<OptionMeasure> measureList = OptionsMeasureUtils.getOptionMeasure(rulerOption.getMeasure());
                if (measureList.size() > 0) {
                    for (OptionMeasure measure : measureList) {
                        rulerCheckOption.setFloorHeight(measure);
                        if (measure.getId() == chooseFloorHeight.getId()) {
                            break;
                        }
                    }
                }
                rulerCheckOption.setServerId(0);
                int id = OperateDbUtil.addMeasureOptionsDataToSqlite(getApplicationContext(), rulerCheckOption);
                rulerCheckOption.setId(id);
                checkOptionsList.add(rulerCheckOption);
                Log.e(TAG, "initData: 新增的RulerCheckOptions:" + rulerCheckOption.toString());
            }
            if (optionsList.size() == 0) {
                RulerCheckOptions rulerCheckOption = new RulerCheckOptions();
                rulerCheckOption.setCreateTime((int) System.currentTimeMillis());
                rulerCheckOption.setRulerCheck(rulerCheck);
                rulerCheckOption.setFloorHeight(chooseFloorHeight);
                checkOptionsList.add(rulerCheckOption);
            }
            startRequestServer();
            initFragmentData();

        } else {
            initFragmentData();
            /**
             * 还有一种情况之前创建过，但是没有网络，所以未请求服务器，现在就要补交
             *  根据其upload_flag来进行判断，1-代表已经请求过，0-代表未请求过
             */
        }

//        controller = new MeasureFragmentControllerImpl(this,bottomNavigationBar,checkOptionsList);
//        controller.initBottomNav();
        HandleBleMeasureDataReceiverService.startHandleService(getApplicationContext(),checkOptionsList);
//        controller.addBottomNav();

    }



    private void initFragmentData() {
        fragments = new ArrayList<>();
        tags = new ArrayList<>();
        LogUtils.show("initFragmentData-----进入了初始化fragment方法");
        List<RulerCheckOptions> optionsList = new ArrayList<>();
        List<String> tagList = new ArrayList<>();
        List<android.support.v4.app.Fragment> fragmentList = new ArrayList<>();
        for (int i = 0; i< checkOptionsList.size(); i++) {

            if (checkOptionsList.get(i).getRulerOptions().getType() == 1 || checkOptionsList.get(i).getRulerOptions().getType() == 2) {
                optionsList.add(checkOptionsList.get(i));
                LogUtils.show("initFragmentData----打印查看管控要点的值：" + optionsList.get(i));
                continue;
            }
//            MeasureFragmentOthers fragment = new MeasureFragmentOthers();
//            RulerCheckOptions checkOptions = checkOptionsList.get(i);
//            Bundle bundle = new Bundle();
////            此id对应iot_ruler_check_options表的id
//            bundle.putInt(DataBaseParams.options_data_check_options_id, checkOptions.getId());
//            bundle.putSerializable("checkoptions",checkOptions);
//            fragment.setArguments(bundle);
//            fragmentList.add(fragment);
//            if (checkOptions.getRulerOptions()!=null)
//                tagList.add(checkOptions.getRulerOptions().getOptionsName());
        }

        if (optionsList.size() > 0) {
            MeasureFragment fragment = new MeasureFragment();
            Bundle bundle = new Bundle();
//            此id对应iot_ruler_check_options表的id
            bundle.putInt(DataBaseParams.options_data_check_options_id, optionsList.get(0).getId());
//                    bundle.putInt("floor_height", chooseIndex);
            bundle.putSerializable("checkoptions", (Serializable) optionsList);
            fragment.setArguments(bundle);
            fragments.add(fragment);
            LogUtils.show("垂直/水平度-----的管控要点："+optionsList.toString());
            tags.add("垂直/水平度");
        }
        fragments.addAll(fragmentList);
        tags.addAll(tagList);

        if (checkOptionsList.size() == 0) {
            MeasureFragment fragment = new MeasureFragment();
            fragments.add(fragment);
        }
        LogUtils.show("-initFragmentData-----初始化完成-----共"+fragments.size()+"个界面");
        MeasureFragmentPagerAdapter fragmentPagerAdapter = new MeasureFragmentPagerAdapter(getSupportFragmentManager(), fragments, tags);
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    /**
     * 在使用该方法前，checkOptionsList和rulerCheck两个对象的数据要先初始化好
     */
    private void startRequestServer() {
        Intent intent = new Intent(this, PerformMeasureNetIntentService.class);
        intent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_CREATE_RECORD);
        intent.putExtra(PerformMeasureNetIntentService.GET_CREATE_OPTIONS_DATA_KEY, (Serializable) checkOptionsList);
        intent.putExtra(PerformMeasureNetIntentService.GET_CREATE_RULER_DATA_KEY, rulerCheck);
        startService(intent);
    }


    /**
     * 在iot_ruler_check_options表格中查找是否已经有上个页面传过来的checkID的数据
     *      如果有 则说明之前已经添加过数据，则不再做添加
     *      如果无 则说明是需要添加到数据库的数据
     * @return true-有数据，false-无数据
     */
    private boolean queryData() {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        /**
         * 查找刚添加的管控要点的id
         */
        String where = " where " + DataBaseParams.measure_option_check_id + "=" + rulerCheck.getId() + " ;";
        Log.e(TAG, "添加管控要点: 查看where条件：" + where);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.measure_option_table_name, " * ", where);
        int index = 0;
        int resultId = 0;
        if (cursor.moveToFirst()) {
            Log.e(TAG, "queryData: 不是新创建的");
            do {
                RulerCheckOptions checkOption = new RulerCheckOptions();
                checkOption.setRulerCheck(rulerCheck);
                checkOption.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_create_time)));
                checkOption.setUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_update_time)));
                checkOption.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));

                checkOption.setImgPath(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_img_path)));
                checkOption.setImgUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_time)));
                checkOption.setImg_upload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_upload_flag)));
                checkOption.setServerImgUrl(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_server_img_url)));
                int optionId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_options_id));
                checkOption.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                checkOption.setServerId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                checkOption.setImgNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_number)));
//                根据optionid查询iot_ruler_options模板表里对应的数据
                List<RulerOptions> optionsList = bleDataDbHelper.queryOptionsAllDataFromSqlite(" where id=" + optionId);
                if (optionsList.size() > 0) {
                    checkOption.setRulerOptions(optionsList.get(0));
                }

                List<OptionMeasure> measureList = OptionsMeasureUtils.getOptionMeasure(checkOption.getRulerOptions().getMeasure());
                if (measureList.size() > 0) {
                    for (OptionMeasure measure : measureList) {
                        checkOption.setFloorHeight(measure);
                        if (measure.getId() == cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height))) {
                            break;
                        }
                    }
                }

                checkOptionsList.add(checkOption);
                Log.e(TAG, "queryData: 查询历史的RulerCheckOption:"+checkOption.toString() );
            } while (cursor.moveToNext());
        }
        return checkOptionsList.size() > 0 ? true : false;
    }

    public void initToolBarView() {
        tvTitle = findViewById(R.id.tv_toolbar_title);
        imgMenu = findViewById(R.id.img_menu_toolbar);
        imgIcon = findViewById(R.id.img_icon_toolbar);
        llToolBar = findViewById(R.id.ll_toolbar);

        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}
