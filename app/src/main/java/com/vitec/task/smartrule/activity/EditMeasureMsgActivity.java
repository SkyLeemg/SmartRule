package com.vitec.task.smartrule.activity;

import android.content.ContentValues;
import android.content.Intent;
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
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.interfaces.ISelectorResultCallBack;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;
import com.vitec.task.smartrule.view.BottomSelectorAndInputDialog;
import com.vitec.task.smartrule.view.BottomSelectorDialog;

import java.util.ArrayList;
import java.util.Date;
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
    //管控要点的数据
    private List<RulerCheckOptions> checkOptionsList;
    /*************各个对话框选中的序号****************/
    private int choosePorjectIndex = -2;//选中的项目名称定位，小于0代表是自定义的，其他对应projectNameList的序号
    private int chooseUnitIndex = -2;//选中的检查位置定位，同上，对应checkFloorList
    private int chooseEngineerIndex = -1;//spinner下拉框中选中的工程编号
    private int chooseFloorHeightIndex = 0;//层高选择定位

    private boolean hasUpdate=false;//判断页面数据是否有修改
    private Intent mIntent;
    private User user;

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
        mIntent = getIntent();
        Bundle bundle = mIntent.getBundleExtra("rulerCheck");
        rulerCheck = (RulerCheck) bundle.getSerializable("rulerCheck");
        LogUtils.show("编辑页面----查看收到的项目："+rulerCheck.getProject());
        /*******************初始化默认数据*************************/
        tvCheckPerson.setText(rulerCheck.getUser().getUserName());
        tvCheckPosition.setText(rulerCheck.getUnitEngineer().getLocation());
        tvCheckTime.setText(DateFormatUtil.stampToDateString(rulerCheck.getCreateTime(),"yyyy年MM月dd日 HH:mm"));
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
        checkOptionsList = new ArrayList<>();

        /**
         * 工程名的下级关联是层高
         * 项目名的下级关联是单位工程
         * 所以先初始化两个一级数据源
         * 用户选择之后再根据一级数据源初始化两个二级数据源
         */
//        String projectWhere = " where " + DataBaseParams.user_user_id + " = " + user.getUserID();
        projectList = OperateDbUtil.queryAllProjectOrderMember(getApplicationContext(), user.getUserID());
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
        engineerList = dataDbHelper.queryEnginDataFromSqlite("");
        //初始化工程名数据源
        for (RulerEngineer engineer : engineerList) {
            String enginName = engineer.getEngineerName();
            if (enginName != null) {
                engineerNameList.add(enginName);
            }
        }

        floorHeightStringlist = initFloorHeightData(rulerCheck.getEngineer());

        //初始化项目名数据源
        projectNameList.clear();
        for (RulerCheckProject project : projectList) {
            projectNameList.add(project.getProjectName());
        }

        getUnitData(rulerCheck.getProject());

        /********初始化层高数据源**********/
        //获取管控要点的数据
        checkOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rulerCheck);
        for (int i=0;i<checkOptionsList.size();i++) {
            String measure = checkOptionsList.get(i).getRulerOptions().getMeasure();
            List<OptionMeasure> measureList = OptionsMeasureUtils.getOptionMeasure(measure);
            tvFloorHeight.setText(checkOptionsList.get(i).getFloorHeight().getData());
            tvFloorHeight.setTextColor(Color.rgb(51,51,51));
            if (measureList.size() > 1) {
                break;
            }
        }

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
//        tvEngineer.setOnClickListener(this);
        tvCheckPosition.setOnClickListener(this);
//        rlFloorHeight.setOnClickListener(this);
        btnEnterMeasure.setOnClickListener(this);

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
                                LogUtils.show("编辑页面------直接获取---查看项目：" + projectList.get(index));
                            } else {
                                int j = 0;
                                for (RulerCheckProject rulerPorject : projectList) {
                                    if (rulerPorject.getProjectName().equals(item)) {
                                        getUnitData(rulerPorject);
                                        choosePorjectIndex = j;
                                        LogUtils.show("编辑页面------循环获取---查看项目：" + rulerPorject);
                                        break;
                                    }
                                    j++;
                                 }
                            }
                        }
//                        重置下级的显示状态
                        if (!unitNameList.contains(tvCheckPosition.getText().toString())) {
                            chooseUnitIndex = -3;
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
                final BottomSelectorDialog selectorDialog = new BottomSelectorDialog(EditMeasureMsgActivity.this, R.style.BottomDialog);
                selectorDialog.setDatalist(engineerNameList);
                selectorDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        tvEngineer.setText(item);
                        chooseEngineerIndex = index;
                        if (chooseEngineerIndex < engineerList.size()) {
                            if (engineerList.get(chooseEngineerIndex).getEngineerName().equals(item)) {
                                floorHeightStringlist = initFloorHeightData(engineerList.get(chooseEngineerIndex));
                            } else {
                                for (int i=0;i<engineerList.size();i++) {
                                    if (engineerList.get(i).getEngineerName().equals(item)) {
                                        floorHeightStringlist = initFloorHeightData(engineerList.get(i));
                                        chooseEngineerIndex = i;
                                        break;
                                    }
                                }
                            }
                        }
                        selectorDialog.dismiss();
                    }
                });
                selectorDialog.show();
                break;

            /**
             * TODO 选择层高
             */
            case R.id.rl_floor_height:
                final BottomSelectorDialog fhselectorDialog = new BottomSelectorDialog(EditMeasureMsgActivity.this,R.style.BottomDialog);
//                floorHeightDatalist = new ArrayList<>();
//                datalist.add(context.getString(R.string.floor_height_1));
//                datalist.add(context.getString(R.string.floor_height_2));
                fhselectorDialog.setDatalist(floorHeightStringlist);

                fhselectorDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        tvFloorHeight.setText(item+"");
                        tvFloorHeight.setTextColor(Color.rgb(51,51,51));
                        chooseFloorHeightIndex = index;
                        fhselectorDialog.dismiss();
                    }
                });
                fhselectorDialog.show();
                break;

            /**
             * TODO 保存按钮
             */
            case R.id.btn_enter_measure:
                if (sencondPosition.getText().toString().equals(rulerCheck.getCheckFloor())&& choosePorjectIndex < -1 && chooseUnitIndex < -1 ) {
                    Toast.makeText(getApplicationContext(), "无任何修改", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (chooseUnitIndex == -3) {
                    Toast.makeText(getApplicationContext(), "请选择单位工程", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (sencondPosition.getText().toString().trim().equals("")) {
                    if (sencondPosition.getText().toString().length() > 0) {
                        Toast.makeText(getApplicationContext(), "具体位置不能是空格", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "具体位置不能为空", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                //                    设置项目名称：如果 == -1则说明是新建的，保存到本地数据库
                if (choosePorjectIndex == -1) {
                    RulerCheckProject project = createProjectName(tvProjectName.getText().toString().trim());
                    rulerCheck.setProject(project);
                } else if (choosePorjectIndex >= 0 && choosePorjectIndex<projectList.size()) {
                    rulerCheck.setProject(projectList.get(choosePorjectIndex));
                }

//                    设置检查位置：如果小于0则说明是新建的，保存到本地数据库
                if (chooseUnitIndex < 0) {
                    RulerUnitEngineer unit = createUnit(tvCheckPosition.getText().toString(), rulerCheck.getProject().getId());
                    rulerCheck.setUnitEngineer(unit);
                } else {
                    List<RulerUnitEngineer> unitList = rulerCheck.getProject().getUnitList();
                    if (unitList != null && unitList.size() > 0) {
                        if (chooseUnitIndex < unitList.size()) {
                            if (unitList.get(chooseUnitIndex).equals(tvCheckPosition.getText().toString())) {
                                rulerCheck.setUnitEngineer(unitList.get(chooseUnitIndex));
                            } else {
                                for (RulerUnitEngineer engineer1  : unitList) {
                                    if (engineer1.getLocation().equals(tvCheckPosition.getText().toString())) {
                                        rulerCheck.setUnitEngineer(engineer1);
                                    }
                                }
                            }
                        }
                    }
                }
//                if (chooseEngineerIndex < engineerList.size() && chooseEngineerIndex > 0) {
//                    if (engineerList.get(chooseEngineerIndex).equals(tvEngineer.getText().toString())) {
//                        rulerCheck.setEngineer(engineerList.get(chooseEngineerIndex));
//                    } else {
//                        for (RulerEngineer engineer1 : engineerList) {
//                            if (engineer1.getEngineerName().equals(tvEngineer.getText().toString())) {
//                                rulerCheck.setEngineer(engineer1);
//                            }
//                        }
//                    }
//                }
                rulerCheck.setCheckFloor(sencondPosition.getText().toString().trim());
                //更新数据到rulercheck表
                ContentValues values = new ContentValues();
                values.put(DataBaseParams.measure_project_id, rulerCheck.getProject().getId());
                values.put(DataBaseParams.project_server_id, 0);
                values.put(DataBaseParams.measure_unit_id,rulerCheck.getUnitEngineer().getId());
//                values.put(DataBaseParams.measure_engin_id,rulerCheck.getEngineer().getServerID());
                values.put(DataBaseParams.options_update_time,DateFormatUtil.transForMilliSecond(new Date()));
                values.put(DataBaseParams.measure_check_floor,sencondPosition.getText().toString().trim());
                //暂时改成5，代表本地数据库已经修改，服务器还没修改
                values.put(DataBaseParams.upload_flag, 5);
                String where = DataBaseParams.measure_id + "=?";
                int result = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_table_name, where, values, new String[]{String.valueOf(rulerCheck.getId())});
                if (result > 0) {
                    //更新层高数据到管控要点
//                    if (chooseFloorHeightIndex > 0) {
//                        for (int i = 0; i < checkOptionsList.size(); i++) {
//                            String measure = checkOptionsList.get(i).getRulerOptions().getMeasure();
//                            List<OptionMeasure> measureList = OptionsMeasureUtils.getOptionMeasure(measure);
//                            if (measureList.size() > 1) {
//                                for (OptionMeasure m : measureList) {
//                                    if (m.getData().equals(tvFloorHeight.getText().toString().trim())) {
//                                        ContentValues contentValues = new ContentValues();
//                                        contentValues.put(DataBaseParams.measure_option_floor_height, m.getId());
//                                        String oWhere = DataBaseParams.measure_id + "=?";
//                                        int or = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_option_table_name, oWhere, contentValues, new String[]{String.valueOf(checkOptionsList.get(i))});
//                                    }
//                                }
//                            }
//                        }
//                    }
                    hasUpdate = true;
                    Intent intent = new Intent(getApplicationContext(),PerformMeasureNetIntentService.class);
                    intent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPDATE_RECORD);
                    intent.putExtra(PerformMeasureNetIntentService.GET_CREATE_RULER_DATA_KEY, rulerCheck);
                    startService(intent);

                    Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(getApplicationContext(), "修改失败", Toast.LENGTH_SHORT).show();
                }




                break;
        }
    }

    /**
     * TODO 返回键
     */
    @Override
    public void onBackPressed() {

        LogUtils.show("编辑信息页面-----准备返回一个数据给等待测量页面");
        if (hasUpdate) {
            mIntent.putExtra("flag", 1);
        } else {
            mIntent.putExtra("flag", 0);
        }
        setResult(11, mIntent);
        //下面这行代码一定要放在下面，不然resultcode会返回0
        super.onBackPressed();
    }

    /**
     * 新建检查位置
     * @param unitName 需要新建的检查位置
     * @param project_id 对应的项目_id
     * @return
     */
    private RulerUnitEngineer createUnit(String unitName, int project_id) {
        RulerUnitEngineer unit = new RulerUnitEngineer();
        unit.setProject_id(project_id);
        unit.setLocation(unitName);
        unit.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
        unit.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
        unit.setServer_id(0);
        int index = OperateDbUtil.addUnitPositionDataToSqlite(getApplicationContext(), unit);
        unit.setId(index);
        return unit;
    }

    /**
     * TODO 新建项目名
     * @param projectName
     * @return
     */
    private RulerCheckProject createProjectName(String projectName) {
        RulerCheckProject project = new RulerCheckProject();
        project.setProjectName(projectName);
        project.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
        project.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
        project.setServer_id(0);
        project.setUser(OperateDbUtil.getUser(getApplicationContext()));
        project.setQrCode("");
        int index = OperateDbUtil.addProjectNameDataToSqlite(getApplicationContext(), project);
        project.setId(index);
        //还要在成员表中添加一条数据
        ProjectUser projectUser = new ProjectUser();
        if (user == null) {
            user = OperateDbUtil.getUser(getApplicationContext());
        }
        projectUser.setUser_id(user.getUserID());
        projectUser.setUserName(user.getUserName());
        projectUser.setcId(user.getChildId());
        projectUser.setProjectId(project.getId());
        projectUser.setProjectServerId(project.getServer_id());
        int u_result = OperateDbUtil.addProjectUserToSqlite(getApplicationContext(), projectUser);
        projectUser.setcId(u_result);

        LogUtils.show("编辑页面------创建项目---查看是否创建成功："+project);
        return project;
    }

    /**
     * 选择完项目名称后，根据所选的项目名称，获取检查位置的数据源
     * @param project
     * @return
     */
    private List<String> getUnitData(RulerCheckProject project) {
        unitEngineerList.clear();
        unitNameList.clear();
        //不能使用 unitEngineerList=project.getUnitList()，会把=project.getUnitList()的集合也清空
        unitEngineerList.addAll(project.getUnitList());
        LogUtils.show("编辑页面-----查看更新后的单位名称个数："+unitEngineerList.size());
        for (RulerUnitEngineer unit : unitEngineerList) {
            unitNameList.add(unit.getLocation());
        }
        return unitNameList;
    }

    /**
     * 选择完工程类型之后，根据所选的工程类型初始化层高的数据源
     * @param rulerEngineer
     * @return
     */
    private List<String> initFloorHeightData(RulerEngineer rulerEngineer) {
        List<String> datalist = new ArrayList<>();
        if (rulerEngineer.getOptionsList() == null || rulerEngineer.getOptionsList().size() == 0) {
            return datalist;
        }
        List<RulerOptions> optionsList = rulerEngineer.getOptionsList();
        for (RulerOptions options : optionsList) {
            if (options.getType() == 1) {
                measureList = OptionsMeasureUtils.getOptionMeasure(options.getMeasure());
                for (OptionMeasure measure : measureList) {
                    datalist.add(measure.getData());
                }
                break;
            }
        }
        return datalist;
    }

}
