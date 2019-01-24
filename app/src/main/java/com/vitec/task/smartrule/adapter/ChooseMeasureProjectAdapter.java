package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.MeasureManagerAcitivty;
import com.vitec.task.smartrule.bean.ChooseMeasureMsg;
import com.vitec.task.smartrule.bean.MeasureData;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.interfaces.IChooseGetter;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.interfaces.ISelectorResultCallBack;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;
import com.vitec.task.smartrule.view.BottomSelectorAndInputDialog;
import com.vitec.task.smartrule.view.BottomSelectorDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  选择测量工程的页面的Adapter，当用户点击新增项目的时候，Adapter数据增多
 */
public class ChooseMeasureProjectAdapter extends BaseAdapter {

    private static final String TAG = "ChooseMeasureProjectAdapter";
    private Context context;
    private int count;
//    private List<EngineerBean> engineerBeanList;
    private List<String> spinnerList;//
    private String chooseEngineerName;//选中的工程名称
    private int choosePorjectIndex = -2;//选中的项目名称定位，小于0代表是自定义的，其他对应projectNameList的序号
    private int chooseUnitIndex = -2;//选中的检查位置定位，同上，对应checkFloorList
    private int chooseEngineerIndex = -2;//spinner下拉框中选中的工程编号
    private List<ChooseMeasureMsg> chooseMeasureMsgList;
    private List<RulerEngineer> engineerList;
    private List<RulerOptions> optionsList;
    private IChooseGetter getter;

    //从数据库获取的iot_ruler_check表格的所有数据，用于tvProjectName和tvCheckFloor的数据源
    private List<RulerCheck> checkList;
    private List<String> projectNameList;//tvProjectName控件的数据源
    private List<String> checkFloorList;//tvCheckFloor控件的数据源
    private BleDataDbHelper dataDbHelper;
    private List<RulerCheckProject> projectList;

    private ArrayAdapter projectNameAdapter;//项目名的adapter
    private ArrayAdapter checkFloorAdapter;//检查位置的adapter
    private int chooseIndex = -2;//层高选择定位，0代表≤6，1代表＞6
    private List<String> floorHeightDatalist;
    private List<OptionMeasure> measureList;


//    private List<String> engineers;

    public ChooseMeasureProjectAdapter(Context context,IChooseGetter getter) {
        this.context = context;
        this.getter = getter;
        initData();

    }

    public void initData() {
        chooseMeasureMsgList = getter.getChooseMeasureMsgList();
        engineerList = getter.getEngineerList();
        optionsList = getter.getOptionsList();
        projectList = getter.getCheckProjectList();
        initSpinnerData();
    }

    private void initSpinnerData() {
        /**
         *获取集合中所有的工程名字，用于选择工程的spinner控件
         */
        spinnerList = new ArrayList<>();
        for (RulerEngineer engineer : engineerList) {
            String enginName = engineer.getEngineerName();
            if (enginName != null) {
                spinnerList.add(enginName);
            }
        }
        Log.e("2aaa", "initSpinnerData: 查看spinnerList:"+spinnerList.toString()+"，engineerList："+ engineerList.toString());

        /**
         * 从sqlite数据库中获取所有的项目名和测量位置，这些信息在iot_ruler_check表格中
         */
        checkList = new ArrayList<>();
        checkFloorList = new ArrayList<>();
        projectNameList = new ArrayList<>();
        dataDbHelper = new BleDataDbHelper(context);
        checkList = dataDbHelper.queryRulerCheckTableDataFromSqlite("");
//        初始化数据源
//        for (int i=0;i<checkList.size();i++) {
//            checkFloorList.add(checkList.get(i).getCheckFloor());
////            projectNameList.add(checkList.get(i).getProjectName());
//        }
       initProjectName();

//        projectNameAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, projectNameList);
//        checkFloorAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, checkFloorList);

    }

    private void initProjectName() {
        projectList = getter.getCheckProjectList();
        for (RulerCheckProject project : projectList) {
            projectNameList.add(project.getProjectName());
        }
    }


    @Override
    public int getCount() {
        return chooseMeasureMsgList.size();
    }

    @Override
    public Object getItem(int i) {
        return chooseMeasureMsgList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_single_project_choose, null);
            holder = new ViewHolder();
            holder.btnEnterMeasure = view.findViewById(R.id.btn_enter_measure);
            holder.tvCheckPerson = view.findViewById(R.id.tv_check_person);
            holder.tvCheckTime = view.findViewById(R.id.tv_check_time);
            holder.autoTvCheckPosition = view.findViewById(R.id.tv_check_position);
            holder.autoTvProjectName = view.findViewById(R.id.tv_project_type);
//            holder.spinnerCheckProjectType = view.findViewById(R.id.spinner_project_type);
            holder.tvEngineer = view.findViewById(R.id.tv_engineer_choose);
            holder.rlFloorHeight = view.findViewById(R.id.rl_floor_height);
            holder.tvFloorHeight = view.findViewById(R.id.tv_floor_height);
            holder.sencondPosition = view.findViewById(R.id.tv_sencond_position);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        /*********************TODO 项目名称的点击事件-弹窗****************************/
        holder.autoTvProjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSelectorAndInputDialog inputDialog = new BottomSelectorAndInputDialog(context, R.style.BottomDialog);
                if (projectNameList.size() == 0) {
                    initProjectName();
                }
                inputDialog.setDatalist(projectNameList);
                inputDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        holder.autoTvProjectName.setText(item);
                        holder.autoTvProjectName.setTextColor(Color.rgb(51,51,51));
                        choosePorjectIndex = index;
                        checkFloorList.clear();
                        if (index >= 0) {
                            if (index < projectList.size()) {
                                if (projectList.get(index).getProjectName().equals(item)) {
                                    checkFloorList = getUnitData(projectList.get(index));
                                } else {
                                    int j = 0;
                                    for (RulerCheckProject rulerPorject : projectList) {
                                        if (rulerPorject.getProjectName().equals(item)) {
                                            checkFloorList = getUnitData(rulerPorject);
                                            choosePorjectIndex = j;
                                        }
                                        j++;
                                    }
                                }
                            }
                        }
                        if (!checkFloorList.contains(holder.autoTvCheckPosition.getText().toString())) {
                            chooseUnitIndex = -2;
                            holder.autoTvCheckPosition.setText("请选择  >");
                            holder.autoTvCheckPosition.setTextColor(Color.rgb(201,201,201));
                        }
                        inputDialog.dismiss();

                    }
                });
                inputDialog.show();
            }
        });

        /******************TODO 检查位置的点击事件-弹窗*******************/
        holder.autoTvCheckPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (choosePorjectIndex == -2) {
                    Toast.makeText(context, "请先选择项目名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                final BottomSelectorAndInputDialog inputDialog = new BottomSelectorAndInputDialog(context);
                inputDialog.setDatalist(checkFloorList);
                inputDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        holder.autoTvCheckPosition.setText(item);
                        holder.autoTvCheckPosition.setTextColor(Color.rgb(51,51,51));
                        chooseUnitIndex = index;
                        inputDialog.dismiss();
                    }
                });
                inputDialog.show();
            }
        });



        if (spinnerList.size() > 0) {
            /**
             * 初始化工程类型的选择
             */
            holder.tvEngineer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final BottomSelectorDialog selectorDialog = new BottomSelectorDialog(context, R.style.BottomDialog);
                    selectorDialog.setDatalist(spinnerList);
                    selectorDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                        @Override
                        public void onSelectCallBack(String item, int index) {
                            holder.tvEngineer.setText(item);
                            holder.tvEngineer.setTextColor(Color.rgb(51,51,51));
                            /********大于等于0说明事选择中的，*****/
                            if (index >= 0) {
                                if (index < spinnerList.size()) {
                                    chooseEngineerName = item;
                                    chooseEngineerIndex = index;
                                }

                                if (chooseEngineerIndex < engineerList.size()) {
                                    if (engineerList.get(chooseEngineerIndex).equals(chooseEngineerName)) {
                                        floorHeightDatalist = initFloorHeightData(engineerList.get(chooseEngineerIndex));
                                    } else {
                                        int j = 0;
                                        for (RulerEngineer engineer1 : engineerList) {
                                            if (engineer1.getEngineerName().equals(chooseEngineerName)) {
                                                floorHeightDatalist = initFloorHeightData(engineer1);
                                                chooseEngineerIndex = j;
                                            }
                                            j++;
                                        }
                                    }
                                }

                                if (!floorHeightDatalist.contains(holder.tvFloorHeight.getText().toString())) {
                                    chooseIndex = -2;
                                    holder.tvFloorHeight.setText("请选择  >");
                                    holder.tvFloorHeight.setTextColor(Color.rgb(201,201,201));
                                }

                            } else {
                                /*********小于0说明是新建的***********/
                                chooseEngineerName = item;
                                chooseEngineerIndex = index;
                            }

                            selectorDialog.dismiss();
                        }
                    });
                    selectorDialog.show();
                }
            });
        }
        Log.e("aaa", "getView: Adapter中收到的集合对象："+ chooseMeasureMsgList.size()+",内容"+chooseMeasureMsgList.get(i).toString());
//        如果是之前就测量过的项目则不可更改
        if (chooseMeasureMsgList.get(i).getRulerCheck() != null && chooseMeasureMsgList.get(i).getRulerCheck().getId() > 0) {
            holder.sencondPosition.setText(chooseMeasureMsgList.get(i).getRulerCheck().getCheckFloor());
            holder.autoTvProjectName.setText(chooseMeasureMsgList.get(i).getRulerCheck().getProject().getProjectName());
            holder.autoTvCheckPosition.setText(chooseMeasureMsgList.get(i).getRulerCheck().getUnitEngineer().getLocation());
//            holder.autoTvProjectName.setClickable(false);
//            holder.autoTvCheckPosition.setClickable(false);
        }
        holder.tvCheckPerson.setText(chooseMeasureMsgList.get(i).getUser().getUserName());
        holder.tvCheckTime.setText(chooseMeasureMsgList.get(i).getCreateDate());

//        holder.autoTvCheckPosition.setAdapter(checkFloorAdapter);
//        holder.autoTvProjectName.setAdapter(projectNameAdapter);



        /**
         * TODO  层高点击事件
         * 点击层高，弹出层高选择框
         */
        holder.rlFloorHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (floorHeightDatalist == null || floorHeightDatalist.size() == 0) {
                    Toast.makeText(context,"请先选择工程类型",Toast.LENGTH_SHORT).show();
                    return;
                }
                final BottomSelectorDialog selectorDialog = new BottomSelectorDialog(context,R.style.BottomDialog);
//                floorHeightDatalist = new ArrayList<>();
//                datalist.add(context.getString(R.string.floor_height_1));
//                datalist.add(context.getString(R.string.floor_height_2));
                selectorDialog.setDatalist(floorHeightDatalist);

                selectorDialog.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        holder.tvFloorHeight.setText(item+"");
                        holder.tvFloorHeight.setTextColor(Color.rgb(51,51,51));
                        chooseIndex = index;
                        selectorDialog.dismiss();
                    }
                });
                selectorDialog.show();

            }
        });

        /***进入测量点击事件**/
        holder.btnEnterMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 在进入测量时主要包括以下操作：
                 * 1.判断ChooseMeasureMsg中是否已经有rulercheck数据内容
                 *      有则说明该项时之前测量过的，直接引用之前的rulercheck
                 *      无则说明是需要新添加的
                 *  新添加的项目还需要做以下几点：
                 *   1.新添加的需要更新数据，做备份，测量页面返回时会用到。
                 *   2.把数据更新到iot_ruler_check表格中
                 *   3.把数据上传到服务器
                 *   4.全部操作完成后跳转页面，同时传一个rulerCheck对象给下一个页面
                 */
                RulerCheck rulerCheck = new RulerCheck();

//                判断ChooseMeasureMsg中是否已经有rulercheck数据内容
                if (chooseMeasureMsgList.get(i).getRulerCheck() != null && chooseMeasureMsgList.get(i).getRulerCheck().getId() > 0) {
                    Log.e("aaa", "onClick: 之前引用之前的" );
//                    有则说明该项时之前测量过的，直接引用之前的rulercheck
                    rulerCheck = chooseMeasureMsgList.get(i).getRulerCheck();
//                    rulerCheck.setProjectName(holder.autoTvProjectName.getText().toString());
                    rulerCheck.setCheckFloor(holder.sencondPosition.getText().toString());

//                    设置项目名称：如果小于0则说明是新建的，保存到本地数据库
                    if (choosePorjectIndex < 0) {
                        RulerCheckProject  project = createProjectName(holder.autoTvProjectName.getText().toString());
                        rulerCheck.setProject(project);
                    } else {
                        rulerCheck.setProject(projectList.get(choosePorjectIndex));
                    }


//                    设置检查位置：如果小于0则说明是新建的，保存到本地数据库
                    if (chooseUnitIndex < 0) {
                        RulerUnitEngineer unit = createUnit(holder.autoTvCheckPosition.getText().toString(), rulerCheck.getProject().getId());
                        rulerCheck.setUnitEngineer(unit);
                    } else {
                        List<RulerUnitEngineer> unitList = rulerCheck.getProject().getUnitList();
                        if (unitList != null && unitList.size() > 0) {
                            if (chooseUnitIndex < unitList.size()) {
                                if (unitList.get(chooseUnitIndex).equals(holder.autoTvCheckPosition.getText().toString())) {
                                    rulerCheck.setUnitEngineer(unitList.get(chooseUnitIndex));
                                } else {
                                    for (RulerUnitEngineer engineer1  : unitList) {
                                        if (engineer1.getLocation().equals(holder.autoTvCheckPosition.getText().toString())) {
                                            rulerCheck.setUnitEngineer(engineer1);
                                        }
                                    }
                                }
                            }
                        }
                    }

//
                } else {
                    if (chooseIndex < -1 || choosePorjectIndex < -1 || chooseUnitIndex < -1 || chooseEngineerIndex < -1) {
                        Toast.makeText(context, "信息填写不完整", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (holder.sencondPosition.getText().toString().trim().equals("")) {
                        if (holder.sencondPosition.getText().toString().length() > 0) {
                            Toast.makeText(context, "具体位置不能为是空格",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "具体位置不能为空",Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

//                    无则说明是需要新添加的,把数据添加到对象中
                    Log.e("aaa", "onClick: 新添加的,项目名："+ holder.autoTvProjectName.getText().toString().trim()+
                            ",楼层："+holder.autoTvCheckPosition.getText().toString().trim());
//                    设置项目名称：如果小于0则说明是新建的，保存到本地数据库
                    if (choosePorjectIndex < 0) {
                        RulerCheckProject  project = createProjectName(holder.autoTvProjectName.getText().toString());
                        rulerCheck.setProject(project);
                    } else {
                        rulerCheck.setProject(projectList.get(choosePorjectIndex));
                    }

//                    设置检查位置：如果小于0则说明是新建的，保存到本地数据库
                    if (chooseUnitIndex < 0) {
                        RulerUnitEngineer unit = createUnit(holder.autoTvCheckPosition.getText().toString(), rulerCheck.getProject().getId());
                        rulerCheck.setUnitEngineer(unit);
                    } else {
                        List<RulerUnitEngineer> unitList = rulerCheck.getProject().getUnitList();
                        if (unitList != null && unitList.size() > 0) {
                            if (chooseUnitIndex < unitList.size()) {
                                if (unitList.get(chooseUnitIndex).equals(holder.autoTvCheckPosition.getText().toString())) {
                                   rulerCheck.setUnitEngineer(unitList.get(chooseUnitIndex));
                                } else {
                                    for (RulerUnitEngineer engineer1  : unitList) {
                                        if (engineer1.getLocation().equals(holder.autoTvCheckPosition.getText().toString())) {
                                            rulerCheck.setUnitEngineer(engineer1);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    rulerCheck.setCheckFloor(holder.sencondPosition.getText().toString().trim());
                    rulerCheck.setUser(chooseMeasureMsgList.get(i).getUser());
                    rulerCheck.setCreateDate(String.valueOf(DateFormatUtil.getDate()));
                    rulerCheck.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                    rulerCheck.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
//                    LogUtils.show("保存下来的createtime时间戳："+DateFormatUtil.transForMilliSecond(new Date()));
                    rulerCheck.setServerId(0);
                    rulerCheck.setUpload_flag(0);
                    rulerCheck.setStatus(0);
                    if (chooseEngineerIndex < engineerList.size()) {
                        if (engineerList.get(chooseEngineerIndex).equals(chooseEngineerName)) {
                            rulerCheck.setEngineer(engineerList.get(chooseEngineerIndex));
                        } else {
                            for (RulerEngineer engineer1 : engineerList) {
                                if (engineer1.getEngineerName().equals(chooseEngineerName)) {
                                    rulerCheck.setEngineer(engineer1);
                                }
                            }
                        }
                    }
//                   把数据更新到iot_ruler_check表格中,返回表格的表头ID
//                    Log.e("aaa", "onClick: 查看adapter这里收到的rulerCheck:"+rulerCheck.toString() );
                    int checkid = OperateDbUtil.addMeasureDataToSqlite(context, rulerCheck);
                    rulerCheck.setId(checkid);
                    //新添加的需要更新数据，做备份，测量页面返回时会用到。同时将此item的数据更新，返回到此界面时需要记录数据
                    chooseMeasureMsgList.get(i).setCheckFloor(holder.autoTvCheckPosition.getText().toString().trim());
                    chooseMeasureMsgList.get(i).setProjectName(holder.autoTvProjectName.getText().toString().trim());
                    chooseMeasureMsgList.get(i).setRulerCheck(rulerCheck);
                    getter.updateChooseMeasureMsgList(i, chooseMeasureMsgList.get(i));

                }
                OptionMeasure chooseM=new OptionMeasure();
                chooseM.setData(holder.tvFloorHeight.getText().toString().trim());
                if (chooseIndex < measureList.size()) {
                    if (measureList.get(chooseIndex).getData().equals(holder.tvFloorHeight.getText().toString())) {
                        chooseM = measureList.get(chooseIndex);
                    } else {
                        for (OptionMeasure measure : measureList) {
                            if (measure.getData().equals(holder.tvFloorHeight.getText().toString().trim())) {
                                chooseM = measure;
                            }
                        }
                    }
                }
                Intent startIntent = new Intent(context, MeasureManagerAcitivty.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.putExtra("projectMsg", rulerCheck);
                startIntent.putExtra("floor_height", (Serializable) chooseM);
                getter.finishActivity();
//                Log.e("chakabiaozhi", "onClick: 查看准备发给另外一个界面的数据信息："+rulerCheck.toString() );

                context.startActivity(startIntent);
            }
        });


        return view;
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
        int index = OperateDbUtil.addUnitPositionDataToSqlite(context, unit);
        LogUtils.show("测量选择页面----查看新建单位工程是否成功："+index);
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
        project.setUser(OperateDbUtil.getUser(context));
        int index = OperateDbUtil.addProjectNameDataToSqlite(context, project);
        project.setId(index);
        //还要在成员表中添加一条数据
        ProjectUser projectUser = new ProjectUser();
        User user = OperateDbUtil.getUser(context);
        projectUser.setUser_id(user.getUserID());
        projectUser.setUserName(user.getUserName());
        projectUser.setcId(user.getChildId());
        projectUser.setMobile(user.getMobile());
        projectUser.setProjectId(project.getId());
        projectUser.setProjectServerId(project.getServer_id());
        int u_result = OperateDbUtil.addProjectUserToSqlite(context, projectUser);
        projectUser.setId(u_result);

        LogUtils.show("测量选择页面----查看新建项目名是否成功：" + index + "------查看成员表是否添加成功：" + u_result);
        return project;
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

    /**
     * 选择完项目名称后，根据所选的项目名称，获取检查位置的数据源
     * @param project
     * @return
     */
    private List<String> getUnitData(RulerCheckProject project) {
        List<RulerUnitEngineer> unitEngineerList = project.getUnitList();
        if (unitEngineerList.size() == 0) {
            String where = " where " + DataBaseParams.project_server_id + "=" + project.getServer_id() + " and " + DataBaseParams.delete_flag + "=0";
            unitEngineerList = OperateDbUtil.queryUnitEngineerDataFromSqlite(context, where);
            if (unitEngineerList.size() > 0) {
                getter.updateProjectList();
                initProjectName();
            }

        }
        List<String> unitNameList = new ArrayList<>();
        for (RulerUnitEngineer unit : unitEngineerList) {
            unitNameList.add(unit.getLocation());
        }
        return unitNameList;
    }

    public List<ChooseMeasureMsg> getChooseMeasureMsgList() {
        return chooseMeasureMsgList;
    }

    public void setChooseMeasureMsgList(List<ChooseMeasureMsg> chooseMeasureMsgList) {
        this.chooseMeasureMsgList = chooseMeasureMsgList;
    }



    class ViewHolder{
        Button btnEnterMeasure;
        TextView tvCheckTime;
        TextView tvCheckPerson;
        TextView tvEngineer;
        TextView autoTvCheckPosition;
        TextView autoTvProjectName;
        EditText sencondPosition;
//        Spinner spinnerCheckProjectType;
        RelativeLayout rlFloorHeight;//层高选择
        TextView tvFloorHeight;//层高显示

    }
}
