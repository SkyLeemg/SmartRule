package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.fragment.MeasureFragment;
import com.vitec.task.smartrule.fragment.MeasureFragmentControllerImpl;
import com.vitec.task.smartrule.fragment.MeasureFragmentOthers;
import com.vitec.task.smartrule.fragment.MeasureFragmentRecordControllerImpl;
import com.vitec.task.smartrule.fragment.MeasureRecordFragment;
import com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeasureRecordManagerAcitivty extends BaseFragmentActivity {

    private static final String TAG = "MeasureManagerAcitivty";
    private BottomNavigationBar bottomNavigationBar;
    private MeasureFragmentRecordControllerImpl controller;
    public TextView tvToolBarTitle;
    public ImageView imgMenu;
    public ImageView imgOtherIcon;
    private MKLoader mkLoader;
    private RelativeLayout llToolBar;

    private RulerCheck rulerCheck;//接收上个页面传来的rulercheck对象
    private List<RulerCheckOptions> checkOptionsList;//rulerCheckOptions集合，一个rulerCheckOption对象对应接下来的一个fragment页面
    private RulerCheckOptions rulerCheckOption;//
    private BleDataDbHelper bleDataDbHelper;//数据库查询helper

    private List<android.support.v4.app.Fragment> fragments;
    private List<String> tags;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        initView();
    }

    private void initView() {
        initToolBarView();
        tabLayout = findViewById(R.id.tablayout_meausre);
        viewPager = findViewById(R.id.viewpage_measure);
//        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar_container);
        mkLoader = findViewById(R.id.loading);
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
        Log.e(TAG, "initData: 查看收到的rulercheck:"+rulerCheck.toString() );
        checkOptionsList = new ArrayList<>();
        bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        tvToolBarTitle.setText(rulerCheck.getEngineer().getEngineerName());

        /**
         * 先查找Iot_ruler_check_options表格有没有同样checkid的，如果有则，不创建直接用之前的
         */
        queryData();
//        controller = new MeasureFragmentRecordControllerImpl(this,bottomNavigationBar,checkOptionsList);
//        controller.initBottomNav();
        initFragmentData();
    }




    private void initFragmentData() {
        fragments = new ArrayList<>();
        tags = new ArrayList<>();
        Log.e("sssd", "initFragmentData: 查看checkoptionsList:"+checkOptionsList.size()+",内容："+checkOptionsList.toString() );
        List<RulerCheckOptions> optionsList = new ArrayList<>();
        for (int i = 0; i< checkOptionsList.size(); i++) {

            if (checkOptionsList.get(i).getRulerOptions().getType() == 1 || checkOptionsList.get(i).getRulerOptions().getType() == 2) {
                optionsList.add(checkOptionsList.get(i));
                if (optionsList.size() == 2) {
                    MeasureRecordFragment fragment = new MeasureRecordFragment();
                    Bundle bundle = new Bundle();
//            此id对应iot_ruler_check_options表的id
                    bundle.putInt(DataBaseParams.options_data_check_options_id, checkOptionsList.get(i).getId());
//                    bundle.putInt("floor_height", chooseIndex);
                    bundle.putSerializable("checkoptions", (Serializable) optionsList);
                    fragment.setArguments(bundle);
                    fragments.add(fragment);
                    tags.add("垂直/水平度");
                }
                continue;
            }
//            MeasureRecordFragment fragment = new MeasureRecordFragment();
//            RulerCheckOptions checkOptions = checkOptionsList.get(i);
//            List<RulerCheckOptions> otherOptionsList = new ArrayList<>();
//            Bundle bundle = new Bundle();
////            此id对应iot_ruler_check_options表的id
//            otherOptionsList.add(checkOptions);
//            bundle.putInt(DataBaseParams.options_data_check_options_id, checkOptions.getId());
//            bundle.putSerializable("checkoptions", (Serializable) otherOptionsList);
//            fragment.setArguments(bundle);
//            fragments.add(fragment);
//            if (checkOptions.getRulerOptions()!=null)
//                tags.add(checkOptions.getRulerOptions().getOptionsName());
        }
        if (checkOptionsList.size() == 0) {
            MeasureRecordFragment fragment = new MeasureRecordFragment();
            fragments.add(fragment);
            tags.add("垂直/水平度");

        }
        MeasureFragmentPagerAdapter fragmentPagerAdapter = new MeasureFragmentPagerAdapter(getSupportFragmentManager(), fragments, tags);
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
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
                checkOption.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                int optionId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_options_id));
                checkOption.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                checkOption.setServerId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                checkOption.setImgNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_number)));
//                获取本地图片地址
                checkOption.setImgPath(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_img_path)));
//                获取本地图片更新时间
                checkOption.setImgUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_time)));
//               获取图片更新标志
                checkOption.setImg_upload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_upload_flag)));
//                获取服务器的图片地址
                checkOption.setServerImgUrl(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_server_img_url)));
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
        cursor.close();
        return checkOptionsList.size() > 0 ? true : false;
    }

    public void initToolBarView() {
        tvToolBarTitle = findViewById(R.id.tv_toolbar_title);
        imgMenu = findViewById(R.id.img_menu_toolbar);
        imgOtherIcon = findViewById(R.id.img_icon_toolbar);
        llToolBar = findViewById(R.id.ll_toolbar);
        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
