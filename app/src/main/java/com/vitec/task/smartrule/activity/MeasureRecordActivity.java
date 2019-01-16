package com.vitec.task.smartrule.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.MeasureProjectListAdapter;
import com.vitec.task.smartrule.adapter.MySpinnerAdapter;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.event.DelMeasureRecordMsgEvent;
import com.vitec.task.smartrule.bean.event.ExportMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureDataMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureRecordMsgEvent;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.ExportDataToExcelIntentService;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.helper.ExportMeaureDataHelper;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 测量记录
 */
public class MeasureRecordActivity extends BaseActivity implements View.OnClickListener {

    //    下拉框
    private Spinner spinnerProjectName;
    private Spinner spinnerCheckPosition;
    private Spinner spinnerEngineer;
    //    展示所有项目的清单
    private ListView lvRecordList;
    private MKLoader mkLoader;
    private TextView tvTotal;
    private TextView tvLastPage, tvNextPage, tvCurrentPage;
    private TextView tvChoose;
    private ImageView imgBack;

    //    底部栏
    private RelativeLayout rlSelectable;
    private TextView tvHasChoose;
    private Button btnExport;
    private Button btnDel;

    private int total = 0,//总数
            currentPage = 1, //当前页码
            pageSize = 20;//当前页总数

    //    项目清单的Adapter
    private MeasureProjectListAdapter projectListAdapter;
    //    下拉框的Adapter
    private MySpinnerAdapter projectNameAdapter;
    private MySpinnerAdapter checkPositionAdapter;
    private MySpinnerAdapter engineerSpinnerAdapter;
    //    所有测量完成的集合,未筛选的所有RulerCheck集合
    private List<RulerCheck> allRulerCheckList;//算是一个基类集合，每个spinner的数据都从这里获取
    //    经过spinner筛选后的项目清单集合
    private List<RulerCheck> selectRulerCheckList;
    private List<RulerCheck> selectCurrentPageCheckList;//筛选后的当前页的Rulercheck清单集合
    private List<RulerCheck> checkedRulerCheckList;//listview中的复选框中选中的集合
    //    过spinner筛选后的下拉框Adapter的集合
    private List<String> selectProjectNameList;
    private List<String> selectPositionList;
    private List<String> selectEnginnerList;
    //    每个spinner对应选中的data序号
    private int projectIndex = 0;
    private int engineerIndex = 0;
    private int positionIndex = 0;

    private int chooseIndex = 0;//用户点击需要跳转的item的序号
    //顶部“选择”图标的标志状态，0-图标显示为选择，1-图标显示为取消
    private int chooseBtnStatus = 0;
    //向服务器获取数据的标志状态，0-默认，1-点击项目需要跳转页面的时候获取的数据请求类型，2-导出文件时请求请求类型
    private int data_status = 0;
    //    导出文件时，会连续请求多次测量数据，这做个统计，直到最后一个我们才做处理
    private int dataResponeCount = 0;
    private String newFileName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_record);
        requestLocationPermissions();
        EventBus.getDefault().register(this);
        initView();
    }

    /**
     * TODO getUnFinishServerCheckData 从服务器获取未完成的ruler_check数据
     */
    private void getUnFinishServerCheckData(int pageNum) {
        Intent serviceIntent = new Intent(MeasureRecordActivity.this, PerformMeasureNetIntentService.class);
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_QUERY_MEASURE_RECORD);
//        传完成测量的标志，1 代表请求的是已经完成测量的测量记录。
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, 1);
//        传当前页数
        serviceIntent.putExtra(NetConstant.current_Page, pageNum);
//        传当前页的条数
        serviceIntent.putExtra(NetConstant.page_size, 20);
        startService(serviceIntent);
    }

    /**
     * TODO getUnFinishLocalCheck从本地获取数据：
     * 获取ruler_check表格中，所有is_finish==0和user_id=当前登录用户的数据
     */
    private void getUnFinishLocalCheck() {
        mkLoader.setVisibility(View.VISIBLE);
        User user = OperateDbUtil.getUser(getApplicationContext());
        String where = " where " + DataBaseParams.user_user_id + " = " + user.getUserID() + " and " + DataBaseParams.measure_is_finish + "=1 or "+  DataBaseParams.measure_is_finish + "=2 ORDER BY " + DataBaseParams.measure_create_time + " DESC;";
        LogUtils.show("查看本地搜索的条件：" + where);
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
        allRulerCheckList.clear();
        allRulerCheckList = dataDbHelper.queryRulerCheckTableDataFromSqlite(where);
//        LogUtils.show("查看本地搜索到的测量记录："+allRulerCheckList.size()+",内容："+allRulerCheckList);
        dataDbHelper.close();
        total = allRulerCheckList.size();
        initData();
    }

    private void updateAdapterData() {
        engineerSpinnerAdapter.setDataList(selectEnginnerList);
        checkPositionAdapter.setDataList(selectPositionList);
        projectListAdapter.setRulerCheckList(selectCurrentPageCheckList);

        spinnerEngineer.setSelection(engineerIndex);
        spinnerCheckPosition.setSelection(positionIndex);

        engineerSpinnerAdapter.notifyDataSetChanged();
        checkPositionAdapter.notifyDataSetChanged();
        projectListAdapter.notifyDataSetChanged();

        HeightUtils.setListViewHeighBaseOnChildren(lvRecordList);
    }

    /**
     * TODO updatePageData 更新页码
     */
    private void updatePageData() {
        int endIndex = currentPage * pageSize;
        int startIndex = endIndex - pageSize;
        if (endIndex > selectRulerCheckList.size()) {
            endIndex = selectRulerCheckList.size();
        }
        selectCurrentPageCheckList.clear();
        for (int i = startIndex; i < endIndex; i++) {
            selectCurrentPageCheckList.add(selectRulerCheckList.get(i));
        }
        total = selectRulerCheckList.size();
        if (currentPage == 1) {
            tvLastPage.setEnabled(false);
        } else {
            tvLastPage.setEnabled(true);
        }

        int totalPage = total / pageSize;
        if (total % pageSize > 0) {
            totalPage += 1;
        }
        if (currentPage == totalPage) {
            tvNextPage.setEnabled(false);
        } else {
            tvNextPage.setEnabled(true);
        }

        tvCurrentPage.setText("" + currentPage);
        tvTotal.setText("总数：" + total);
    }


    private void initView() {
        tvChoose = findViewById(R.id.tv_choose);
        imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(this);
        tvChoose.setOnClickListener(this);

        spinnerCheckPosition = findViewById(R.id.spinner_check_position);
        spinnerProjectName = findViewById(R.id.spinner_project_name);
        lvRecordList = findViewById(R.id.lv_finish_measure_list);
        spinnerEngineer = findViewById(R.id.spinner_engineer);
        mkLoader = findViewById(R.id.mkloader);
        tvLastPage = findViewById(R.id.tv_last_page);
        tvNextPage = findViewById(R.id.tv_next_page);
        tvTotal = findViewById(R.id.tv_total);
        tvCurrentPage = findViewById(R.id.tv_current_page);

        rlSelectable = findViewById(R.id.rl_selectable);
        tvHasChoose = findViewById(R.id.tv_has_choose);
        btnDel = findViewById(R.id.btn_del_record);
        btnExport = findViewById(R.id.btn_export_record);

        rlSelectable.setVisibility(View.GONE);

        allRulerCheckList = new ArrayList<>();
        selectRulerCheckList = new ArrayList<>();
//        initData();
        getUnFinishServerCheckData(1);

        tvLastPage.setOnClickListener(this);
        tvNextPage.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnDel.setOnClickListener(this);

        /**
         * 下拉框的偏移
         */
        spinnerProjectName.setDropDownVerticalOffset(100);
        spinnerCheckPosition.setDropDownVerticalOffset(100);
        spinnerEngineer.setDropDownVerticalOffset(100);

    }

    /**
     * TODO initData
     */
    private void initData() {
        /**********************从数据库获取所有完成的集合*******************************/

//        User user = OperateDbUtil.getUser(getApplicationContext());
//        String where = " where " + DataBaseParams.user_user_id + " = " + user.getUserID() + " and " + DataBaseParams.measure_is_finish + "=1 ORDER BY "+ DataBaseParams.measure_id+" DESC;" ;
//        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
//        allRulerCheckList = dataDbHelper.queryRulerCheckTableDataFromSqlite(where);
//        selectRulerCheckList = dataDbHelper.queryRulerCheckTableDataFromSqlite(where);
//        dataDbHelper.close();

        /**********************初始化所有下拉框的数据集合*****************************/
//        allProjectNameList = new ArrayList<>();
//        allPositionList = new ArrayList<>();
        selectEnginnerList = new ArrayList<>();
        selectPositionList = new ArrayList<>();
        selectProjectNameList = new ArrayList<>();
        selectCurrentPageCheckList = new ArrayList<>();
        checkedRulerCheckList = new ArrayList<>();

        selectProjectNameList.add("全部");
        selectEnginnerList.add("全部");
        selectPositionList.add("全部");
//        使用set来过滤相同String
        Set<String> projectNameSet = new HashSet<>();

        if (allRulerCheckList.size() > 0) {
            for (int i = 0; i < allRulerCheckList.size(); i++) {
                RulerCheck rulerCheck = allRulerCheckList.get(i);
                if (projectNameSet.add(rulerCheck.getProject().getProjectName())) {
                    selectProjectNameList.add(rulerCheck.getProject().getProjectName());
                }
            }
            selectRulerCheckList.addAll(allRulerCheckList);
        }

        total = selectRulerCheckList.size();
        currentPage = 1;
        updatePageData();
        LogUtils.show("查看当前PageSize：" + pageSize);

        /*******************初始化下拉框的Adapter************************/
        projectNameAdapter = new MySpinnerAdapter(MeasureRecordActivity.this, selectProjectNameList);
        checkPositionAdapter = new MySpinnerAdapter(MeasureRecordActivity.this, selectPositionList);
        engineerSpinnerAdapter = new MySpinnerAdapter(MeasureRecordActivity.this, selectEnginnerList);

        /*******************设置下拉框的adapter***********************/
        spinnerProjectName.setAdapter(projectNameAdapter);
        spinnerCheckPosition.setAdapter(checkPositionAdapter);
        spinnerEngineer.setAdapter(engineerSpinnerAdapter);

        /********************初始化和设置完成测量的清单listview***********************/
        projectListAdapter = new MeasureProjectListAdapter(getApplicationContext(), selectCurrentPageCheckList, -1);
        lvRecordList.setAdapter(projectListAdapter);
        HeightUtils.setListViewHeighBaseOnChildren(lvRecordList);
        mkLoader.setVisibility(View.GONE);
//        projectListAdapter.setShowCheckBox(true);


        lvRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                1-点击项目需要跳转页面的时候获取的数据请求类型
                data_status = 1;
                mkLoader.setVisibility(View.VISIBLE);
                Intent serviceIntent = new Intent(MeasureRecordActivity.this, PerformMeasureNetIntentService.class);
                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_GET_MEASURE_DATA);
                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, selectCurrentPageCheckList.get(i));
                startService(serviceIntent);
                chooseIndex = i;
            }
        });

        /**
         * 下拉框选中事件处理原理：
         * 1.先判断其他两个下拉框的序号是否等于0（即是否为“全部”）
         *   1.1 是则筛选条件只有当前选中下拉框的条件
         *       根据当前条件更新筛选后的rulercheck清单，和其他两个spinner的数据集合
         *   1.2 否 则筛选条件为多个条件，包括其他两个“非全部”的条件
         *       然后根据当前条件更新筛选后的rulercheck清单，和其他两个spinner的数据集合
         */
        spinnerProjectName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                projectIndex = i;
//                if (i == 0) {
//                    positionIndex = 0;
//                    engineerIndex = 0;
//                }
                filtCheckList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerCheckPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtils.show("点击了检查位置-----查看数据源个数："+selectPositionList.size()+",当前选择的序号："+i);
                positionIndex = i;
//                if (i > 0) {
//                    /**
//                     * 从已经筛选后的rulercheck集合中再次筛选，
//                     */
//                } else {
//                    /**
//                     * 如果选中的全部，则从所有的rulercheck中 根据另外两个条件筛选
//                     */
//                }
//                filtCheckList();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerEngineer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtils.show("点击了工程类型-----查看数据源个数："+selectEnginnerList.size()+",当前选择的序号："+i);
                engineerIndex = i;
                filtCheckList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        /*********************监听listview中的checkbox选择事件*************************/
        projectListAdapter.setChecked(new MeasureProjectListAdapter.OnChecked() {
            @Override
            public void onCheckedChanged(int position, boolean ishChecked) {
                if (ishChecked) {
                    LogUtils.show(selectCurrentPageCheckList.get(position).getProject().getProjectName() + "==被选中了，选中的position为：" + position);
                    checkedRulerCheckList.add(selectCurrentPageCheckList.get(position));
                    tvHasChoose.setText("已选：" + checkedRulerCheckList.size());

                } else {
                    LogUtils.show(selectCurrentPageCheckList.get(position).getProject().getProjectName() + "==被取消了，取消的position为：" + position);
                    checkedRulerCheckList.remove(selectCurrentPageCheckList.get(position));
                    tvHasChoose.setText("已选：" + checkedRulerCheckList.size());
                }
            }
        });
    }


    /**
     * TODO filtCheckList 过滤spinner的数据源
     * 把每个选中spinner的事件都当成是一个搜索点击按钮的响应事件
     * 1.每次有搜索响应事件发生的时候，都先筛选projectName
     * 2.然后再经过projectName筛选后的结果中 去筛选position检查位置的条件
     * 3.然后再经过前面两个筛选的条件中，去筛选engineer 工程类型的条件
     * 4.
     */
    private void filtCheckList() {
//        第一次筛选结果的集合
        List<RulerCheck> firstFiltList = new ArrayList<>();
//        第二次筛选结果的集合
        List<RulerCheck> sencondFiltList = new ArrayList<>();
        if (positionIndex >= selectPositionList.size()) {
            return;
        }
        if (engineerIndex >= selectEnginnerList.size()) {
            return;
        }
        LogUtils.show("filtCheckList-----查看已经选择的检查位置的个数：" + selectPositionList.size() + ",定位：" + positionIndex + "，打印数据源：" + selectPositionList);
        String choosePosition = selectPositionList.get(positionIndex);
        String chooseEnginner = selectEnginnerList.get(engineerIndex);
        Set<String> projectNameSet = new HashSet<>();


//        1.每次有搜索响应事件发生的时候，都先筛选projectName
        if (projectIndex != 0) {
            for (int j = 0; j < allRulerCheckList.size(); j++) {
                if (allRulerCheckList.get(j).getProject().getProjectName().equals(selectProjectNameList.get(projectIndex))) {
                    firstFiltList.add(allRulerCheckList.get(j));
                }
            }
        } else {
            selectProjectNameList.clear();
            selectProjectNameList.add("全部");
            for (int j = 0; j < allRulerCheckList.size(); j++) {
                if (projectNameSet.add(allRulerCheckList.get(j).getProject().getProjectName())) {
                    selectProjectNameList.add(allRulerCheckList.get(j).getProject().getProjectName());
                }
            }
            firstFiltList.addAll(allRulerCheckList);
        }

//        2.然后再经过projectName筛选后的结果中 去筛选position检查位置的条件
        if (positionIndex != 0) {
            for (int j = 0; j < firstFiltList.size(); j++) {
                if (firstFiltList.get(j).getCheckFloor().equals(choosePosition)) {
                    sencondFiltList.add(firstFiltList.get(j));
                }
            }
        } else {
            sencondFiltList.addAll(firstFiltList);
        }

        selectRulerCheckList.clear();
//        3.然后再经过前面两个筛选的条件中，去筛选engineer 工程类型的条件
        if (engineerIndex != 0) {
            for (int j = 0; j < sencondFiltList.size(); j++) {
                if (sencondFiltList.get(j).getEngineer().getEngineerName().equals(chooseEnginner)) {
                    selectRulerCheckList.add(sencondFiltList.get(j));
                }
            }
        } else {
            selectRulerCheckList.addAll(sencondFiltList);
        }


        selectPositionList.clear();
        selectEnginnerList.clear();
        selectCurrentPageCheckList.clear();
        selectPositionList.add("全部");
        selectEnginnerList.add("全部");
//        positionIndex = 0;
//        engineerIndex = 0;
        Set<String> positonSet = new HashSet<>();
        Set<String> engineerSet = new HashSet<>();
//                更新spinner的数据
        for (int n = 0; n < selectRulerCheckList.size(); n++) {
            if (engineerSet.add(selectRulerCheckList.get(n).getEngineer().getEngineerName())) {
                selectEnginnerList.add(selectRulerCheckList.get(n).getEngineer().getEngineerName());
            }
            if (positonSet.add(selectRulerCheckList.get(n).getCheckFloor())) {
                selectPositionList.add(selectRulerCheckList.get(n).getUnitEngineer().getLocation());
            }
        }
        for (int i = 0; i < selectEnginnerList.size(); i++) {
            if (selectEnginnerList.get(i).equals(chooseEnginner)) {
                engineerIndex = i;
            }
        }

        for (int i = 0; i < selectPositionList.size(); i++) {
            if (selectPositionList.get(i).equals(choosePosition)) {
                positionIndex = i;
//                spinnerCheckPosition.setSelection(i);
            }
        }
        currentPage = 1;
//                    更新页数
        total = selectRulerCheckList.size();
        updatePageData();
        updateAdapterData();

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            /**
             * 选择按钮
             */
            case R.id.tv_choose:
                if (selectRulerCheckList.size() != 0) {
                    if (chooseBtnStatus == 0) {
                        projectListAdapter.setShowCheckBox(true);
                        projectListAdapter.notifyDataSetChanged();
                        tvChoose.setText("取消");
                        chooseBtnStatus = 1;
                        rlSelectable.setVisibility(View.VISIBLE);
                    } else if (chooseBtnStatus == 1) {
                        projectListAdapter.setShowCheckBox(false);
                        projectListAdapter.setAllChecked(false);
                        projectListAdapter.notifyDataSetChanged();
                        tvChoose.setText("选择");
                        chooseBtnStatus = 0;
                        rlSelectable.setVisibility(View.GONE);

                    }
                } else {
                    Toast.makeText(getApplicationContext(),"无可选择项目",Toast.LENGTH_SHORT).show();
                }

                break;
            /**
             * 返回按钮
             */
            case R.id.img_back:
                onBackPressed();
                break;
            /**
             * 上一页
             */
            case R.id.tv_last_page:
                if (currentPage > 1) {
                    currentPage--;
                    updatePageData();
                    updateAdapterData();
                } else {
                    Toast.makeText(getApplicationContext(), "已是第一页", Toast.LENGTH_SHORT).show();
                }
                break;

            /**
             * 下一页
             */
            case R.id.tv_next_page:
                int totalPage = total / pageSize;
                if (total % pageSize > 0) {
                    totalPage += 1;
                }
                if (currentPage < totalPage) {
                    currentPage++;
                    updatePageData();
                    updateAdapterData();
                } else {
                    Toast.makeText(getApplicationContext(), "已是最后一页", Toast.LENGTH_SHORT).show();
                }
                break;

            /**
             * TODO 导出记录表按钮点击事件
             */
            case R.id.btn_export_record:

                if (checkedRulerCheckList.size() > 0) {
                    dataResponeCount = 0;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MeasureRecordActivity.this);
                    builder.setTitle("导出" + checkedRulerCheckList.size() + "条记录为Excel表");
                    StringBuffer msg = new StringBuffer();
                    msg.append("是否确定导出为Excel表？");
                    msg.append("\n");
                    msg.append("所选的记录将合并到一个Excel表格，默认文件名为第一个勾选的项目日期+项目名");
                    msg.append("\n");
                    msg.append("您可以自定义文件名");
                    File dir = new File(ExportMeaureDataHelper.path);
                    if (!dir.exists() || !dir.isDirectory()) {
                        dir.mkdir();
                    }
                    String fileName = DateFormatUtil.stampToDateString(checkedRulerCheckList.get(0).getCreateTime(), "yyyyMMddHHmm") + checkedRulerCheckList.get(0).getProject().getProjectName();
                    File file = new File(ExportMeaureDataHelper.path, fileName + ".xls");
                    if (file.exists()) {
                        int random = (int) (Math.random() * 100);
                        fileName = fileName +random;
                    }
                    final EditText fileNameEt = new EditText(MeasureRecordActivity.this);
                    fileNameEt.setText(fileName);
                    builder.setMessage(msg.toString());
                    builder.setView(fileNameEt);


                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int p) {
                            newFileName = fileNameEt.getText().toString().trim();
                            File newfile = new File(ExportMeaureDataHelper.path, newFileName + ".xls");
                            if (newfile.exists()) {
                                AlertDialog.Builder tipBulder = new AlertDialog.Builder(MeasureRecordActivity.this);
                                tipBulder.setMessage("该文件已经存在，是否覆盖原文件？");

                                tipBulder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int p) {
                                        mkLoader.setVisibility(View.VISIBLE);
                                        //                    2-导出文件时请求请求类型
                                        //
                                        data_status = 2;
                                        /**
                                         * 从服务器获取测量数据
                                         */
                                        for (int i = 0; i < checkedRulerCheckList.size(); i++) {
                                            Intent serviceIntent = new Intent(MeasureRecordActivity.this, PerformMeasureNetIntentService.class);
                                            serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_GET_MEASURE_DATA);
                                            serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, checkedRulerCheckList.get(i));
                                            startService(serviceIntent);
                                        }
                                    }
                                });

                                tipBulder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                tipBulder.show();
                            } else {
                                mkLoader.setVisibility(View.VISIBLE);
                                data_status = 2;
                                /**
                                 * 从服务器获取测量数据
                                 */
                                for (int i = 0; i < checkedRulerCheckList.size(); i++) {
                                    Intent serviceIntent = new Intent(MeasureRecordActivity.this, PerformMeasureNetIntentService.class);
                                    serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_GET_MEASURE_DATA);
                                    serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, checkedRulerCheckList.get(i));
                                    startService(serviceIntent);
                                }
                            }
                        }
                    });
                    builder.show();


                } else {
                    Toast.makeText(getApplicationContext(), "未有选中的项目", Toast.LENGTH_SHORT).show();
                }


                break;

            /**
             * 删除记录表
             */
            case R.id.btn_del_record:
                mkLoader.setVisibility(View.VISIBLE);
                StringBuffer check_ids = new StringBuffer();
                for (int i = 0; i < checkedRulerCheckList.size(); i++) {
                    check_ids.append(checkedRulerCheckList.get(i).getServerId());
                    if (i < (checkedRulerCheckList.size() - 1)) {
                        check_ids.append(",");
                    }
                }
                Intent serviceIntent = new Intent(MeasureRecordActivity.this, PerformMeasureNetIntentService.class);
                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_DEL_RECORD);
                //        传完成测量的标志，。
                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, check_ids.toString());
                startService(serviceIntent);

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    /**
     * TODO delMeasureRecordCallBack 请求删除记录表的响应回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void delMeasureRecordCallBack(DelMeasureRecordMsgEvent event) {
        mkLoader.setVisibility(View.GONE);
        LogUtils.show("删除记录回调了：" + event.isSuccess());
        if (event.isSuccess()) {
            delLocalData();
        } else {
            /**
             * 如果服务器删除失败，则将本地数据库的upload_flag改成4
             */
            BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
            for (int i=0;i<checkedRulerCheckList.size();i++) {
                ContentValues values = new ContentValues();
                values.put(DataBaseParams.upload_flag, 4);
                values.put(DataBaseParams.measure_is_finish, 4);
                String where = " id =?";
                int result = bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, values, where, new String[]{String.valueOf(checkedRulerCheckList.get(i).getId())});
                if (result > 0) {
                    delOptionsAndData(checkedRulerCheckList.get(i));
                    LogUtils.show("状态数据修改为4成功啦");
                }

            }
            allRulerCheckList.removeAll(checkedRulerCheckList);
            selectRulerCheckList.removeAll(checkedRulerCheckList);
            selectCurrentPageCheckList.removeAll(checkedRulerCheckList);
            filtCheckList();
            if (selectCurrentPageCheckList.size() == 0) {
                spinnerProjectName.setSelection(0);
            }
            checkedRulerCheckList.clear();
            tvHasChoose.setText("已选：0");
            LogUtils.show("本地数据库修改成功");
        }

    }

    /**
     * 删除要给rulercheck数据后，要将rulercheck对应的ruler_check_options和ruler_check_options_data表格中的数据也删除了
     * @param rulerCheck
     */
    private void delOptionsAndData(RulerCheck rulerCheck) {
        String where = " where " + DataBaseParams.measure_option_check_id + " = " + rulerCheck.getId();
        List<RulerCheckOptions> checkOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rulerCheck, where);
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        if (checkOptionsList.size() > 0) {
            for (RulerCheckOptions checkOptions : checkOptionsList) {
                String delWhere = DataBaseParams.options_data_check_options_id + " = ?";
                boolean delResult = bleDataDbHelper.delData(DataBaseParams.options_data_table_name, delWhere, new String[]{String.valueOf(checkOptions.getId())});
//                if (delResult) {
                    LogUtils.show( "管控要点ID为："+checkOptions.getId()+"的测量数据删除结果："+delResult);
//                }
            }
        }
        String optionWhere = DataBaseParams.measure_option_check_id + " = ?";
        boolean optionsResult = bleDataDbHelper.delData(DataBaseParams.measure_option_table_name, optionWhere, new String[]{String.valueOf(rulerCheck.getId())});
        LogUtils.show("check_id为："+rulerCheck.getId()+"的所有管控要点数据删除结果："+optionsResult);

    }

    /**
     * TODO delLocalData 从本地的数据库中删除选中的数据
     */
    private void delLocalData() {
            Toast.makeText(MeasureRecordActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            allRulerCheckList.removeAll(checkedRulerCheckList);
            selectRulerCheckList.removeAll(checkedRulerCheckList);
            selectCurrentPageCheckList.removeAll(checkedRulerCheckList);
            filtCheckList();
            if (selectCurrentPageCheckList.size() == 0) {
                spinnerProjectName.setSelection(0);
            }
            checkedRulerCheckList.clear();
            tvHasChoose.setText("已选：0");
            LogUtils.show("本地数据库删除成功");
    }

    /**
     * TODO 接受请求服务器记录表响应的数据
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void serverMeasureRecordCallBack(MeasureRecordMsgEvent event) {

        List<RulerCheck> checkList = event.getCheckList();
        LogUtils.show("serverMeasureRecordCallBack---查看从服务器收到的测量记录：" + checkList.size() + "，内容：" + checkList);
//        如果checklist大小大于0，则说明请求成功，为0则说明网络请求失败，或者是真的为0
        if (checkList.size() > 0) {
            if (event.getCurrentPage() == 1) {
                total = event.getTotal();
            }
            allRulerCheckList.addAll(checkList);
            int totalPage = total / pageSize;
            if (total % pageSize > 0) {
                totalPage += 1;
            }

//           如果当前请求页码小于总页码，继续请求
            if (currentPage < totalPage) {
                LogUtils.show("serverMeasureRecordCallBack---查看从服务器收到的测量记录总数：" + event.getTotal() + "，请求的页数：" + currentPage + ",返回的页数：" + event.getCurrentPage() + "，每页条数：" + event.getPageSize() + ",总的页码：" + totalPage);
                currentPage++;
                getUnFinishServerCheckData(currentPage);

            }
//           直到所有都请求完毕了，再初始化各个控件的数据
            else if (currentPage == totalPage) {
                initData();
            }

        } else {
            getUnFinishLocalCheck();
        }
    }

    /**
     * TODO 导出表格响应 获取测量数据接口，接受服务器响应数据处理完毕的信号
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMeasureDataCallBack(MeasureDataMsgEvent event) {
        LogUtils.show("getMeasureDataCallBack----收到一个数据保存成功,查看状态值："+data_status);

//        1-点击项目需要跳转页面的时候获取的数据请求类型
        if (data_status == 1) {
            mkLoader.setVisibility(View.GONE);
            Intent intent = new Intent(MeasureRecordActivity.this, MeasureRecordManagerAcitivty.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("projectMsg", selectCurrentPageCheckList.get(chooseIndex));
            startActivity(intent);
        } else if (data_status == 2) {
//            2为导出文件的请求标志类型
            dataResponeCount++;
            LogUtils.show("查看dataResponeCount："+dataResponeCount+"查看checkedRulerCheckList.size()："+checkedRulerCheckList.size());
            if (dataResponeCount == checkedRulerCheckList.size()) {
                LogUtils.show("启动导出文件的服务");
                Intent exportIntent = new Intent(MeasureRecordActivity.this, ExportDataToExcelIntentService.class);
                exportIntent.putExtra(ExportDataToExcelIntentService.GET_DATA_KEY, (Serializable) checkedRulerCheckList);
                exportIntent.putExtra(ExportDataToExcelIntentService.GET_FILE_NAME, newFileName + ".xls");
                startService(exportIntent);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void exportToExcelCallBack(ExportMsgEvent exportMsgEvent) {
        LogUtils.show("收到导出成功的响应："+exportMsgEvent.toString());
        if (exportMsgEvent.isSuccess()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MeasureRecordActivity.this);
            builder.setTitle("导出成功");
            builder.setMessage("是否立即发送文件给微信好友？\n 您也可以在【文件管理】中查看所有导出的文件");
            builder.setNegativeButton("否", null);
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MeasureRecordActivity.this);
            builder.setTitle("导出失败");
            builder.setNegativeButton("否", null);
            builder.show();
        }
        mkLoader.setVisibility(View.GONE);
    }

//    private void initViewData() {
//
//    }
}
