package com.vitec.task.smartrule.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.interfaces.ISelectorResultCallBack;
import com.vitec.task.smartrule.view.BottomSelectorAndInputDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑测量信息页面
 */
public class EditMeasureMsgActivity extends BaseActivity implements View.OnClickListener{

    private Button btnEnterMeasure;
    private TextView tvCheckTime;
    private TextView tvCheckPerson;
    private TextView tvEngineer;
    private TextView tvCheckPosition;
    private TextView tvProjectName;
    private EditText sencondPosition;
    private RelativeLayout rlFloorHeight;//层高选择
    private TextView tvFloorHeight;//层高显示

    /***主数据源部分-内部数据处理的*****/
    private RulerCheck rulerCheck;
//    项目名主数据源
    private List<RulerCheckProject> projectList;
//    工程名主数据源
    private List<RulerEngineer> engineerList;
    //    单位工程主数据源
    private List<RulerUnitEngineer> unitEngineerList;
//    层高数据源
    private List<OptionMeasure> measureList;
    /*********显示的数据源部分-传给对话框的**********/
//    项目名显示数据原
    private List<String> projectNameList;
    //    工程名显示数据源
    private List<String> engineerNameList;
    //    工程名显示数据源
    private List<String> unitNameList;
    //    层高显示数据源
    private List<String> floorHeightStringlist;
    /*************各个对话框选中的序号****************/
    private int choosePorjectIndex = -2;//选中的项目名称定位，小于0代表是自定义的，其他对应projectNameList的序号
    private int chooseUnitIndex = -2;//选中的检查位置定位，同上，对应checkFloorList
    private int chooseEngineerIndex = -1;//spinner下拉框中选中的工程编号
    private int chooseFloorHeightIndex = 0;//层高选择定位

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_measure_msg);
        initView();
        initData();
    }

    private void initData() {
        /**
         * 从上一级页面中接收数据
         */
        Bundle bundle = getIntent().getBundleExtra("rulerCheck");
        rulerCheck = (RulerCheck) bundle.getSerializable("rulerCheck");
        /*******************初始化默认数据*************************/
        tvCheckPerson.setText(rulerCheck.getUser().getUserName());
        tvCheckPosition.setText(rulerCheck.getUnitEngineer().getLocation());
        tvCheckTime.setText(rulerCheck.getUpdateTime()+"");
        tvEngineer.setText(rulerCheck.getEngineer().getEngineerName());
        tvProjectName.setText(rulerCheck.getProject().getProjectName());
        sencondPosition.setText(rulerCheck.getCheckFloor());
//        tvFloorHeight.setText(rulerCheck.);
        tvCheckPerson.setTextColor(Color.rgb(51,51,51));
        tvCheckPosition.setTextColor(Color.rgb(51,51,51));
        tvEngineer.setTextColor(Color.rgb(51,51,51));
        tvProjectName.setTextColor(Color.rgb(51,51,51));

        /*********************初始化数据源***********************/
        User user = rulerCheck.getUser();
        measureList = new ArrayList<>();
        unitEngineerList = new ArrayList<>();
        projectNameList = new ArrayList<>();
        engineerNameList = new ArrayList<>();
        unitNameList = new ArrayList<>();
        floorHeightStringlist = new ArrayList<>();

        /**
         * 工程名的下级关联是层高
         * 项目名的下级关联是单位工程
         * 所以先初始化两个一级数据源
         * 用户选择之后再根据一级数据源初始化两个二级数据源
         */
        String projectWhere = " where " + DataBaseParams.user_user_id + " = " + user.getUserID();
        projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), projectWhere);
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
        engineerList = dataDbHelper.queryEnginDataFromSqlite("");
        //初始化工程名数据源
        for (RulerEngineer engineer : engineerList) {
            String enginName = engineer.getEngineerName();
            if (enginName != null) {
                engineerNameList.add(enginName);
            }
        }

        //初始化项目名数据源
        for (RulerCheckProject project : projectList) {
            projectNameList.add(project.getProjectName());
        }

        getUnitData(rulerCheck.getProject());

    }

    private void initView() {
        initWidget();
        setTvTitle("编辑信息");
        btnEnterMeasure = findViewById(R.id.btn_enter_measure);
        tvCheckPerson = findViewById(R.id.tv_check_person);
        tvCheckTime = findViewById(R.id.tv_check_time);
        tvCheckPosition = findViewById(R.id.tv_check_position);
        tvProjectName = findViewById(R.id.tv_project_type);
        tvEngineer = findViewById(R.id.tv_engineer_choose);
        rlFloorHeight = findViewById(R.id.rl_floor_height);
        sencondPosition = findViewById(R.id.tv_sencond_position);
        tvFloorHeight = findViewById(R.id.tv_floor_height);

        tvProjectName.setOnClickListener(this);
        tvEngineer.setOnClickListener(this);
        tvCheckPosition.setOnClickListener(this);
        rlFloorHeight.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * TODO 选择项目名称
             */
            case R.id.tv_project_type:
                final BottomSelectorAndInputDialog projectDialog = new BottomSelectorAndInputDialog(EditMeasureMsgActivity.this, R.style.BottomDialog);
                projectDialog.setDatalist(projectNameList);
                projectDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        tvProjectName.setText(item);
                        choosePorjectIndex = index;
//                        初始化下级-单位工程的数据源
                        unitNameList.clear();
                        if (index >= 0 && index<projectList.size()) {
                            if (projectList.get(index).getProjectName().equals(item)) {
                                getUnitData(projectList.get(index));
                            } else {
                                int j = 0;
                                for (RulerCheckProject rulerPorject : projectList) {
                                    if (rulerPorject.getProjectName().equals(item)) {
                                        getUnitData(rulerPorject);
                                        choosePorjectIndex = j;
                                    }
                                    j++;
                                }
                            }
                        }
//                        重置下级的显示状态
                        if (!unitNameList.contains(tvCheckPosition.getText().toString())) {
                            chooseUnitIndex = -2;
                            tvCheckPosition.setText("请选择  >");
                            tvCheckPosition.setTextColor(Color.rgb(201,201,201));
                        }
                        projectDialog.dismiss();

                    }
                });
                projectDialog.show();
                break;

            /**
             * TODO 选择检查位置
             */
            case R.id.tv_check_position:
                final BottomSelectorAndInputDialog unitDialog = new BottomSelectorAndInputDialog(this, R.style.BottomDialog);
                unitDialog.setDatalist(unitNameList);
                unitDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        tvCheckPosition.setText(item);
                        chooseUnitIndex = index;
                        tvCheckPosition.setTextColor(Color.rgb(51,51,51));
                        unitDialog.dismiss();
                    }
                });
                unitDialog.show();
                break;

            /**
             * TODO 选择工程类型
             */
            case R.id.tv_engineer_choose:

                break;

            /**
             * TODO 选择层高
             */
            case R.id.rl_floor_height:

                break;

            /**
             * TODO 保存按钮
             */
            case R.id.btn_enter_measure:

                break;
        }
    }

    /**
     * 选择完项目名称后，根据所选的项目名称，获取检查位置的数据源
     * @param project
     * @return
     */
    private List<String> getUnitData(RulerCheckProject project) {
        unitEngineerList.clear();
        unitNameList.clear();
        unitEngineerList = project.getUnitList();
        for (RulerUnitEngineer unit : unitEngineerList) {
            unitNameList.add(unit.getLocation());
        }
        return unitNameList;
    }

}
