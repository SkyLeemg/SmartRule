package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luck.picture.lib.tools.ScreenUtils;
import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.GroupUnitAdapter;
import com.vitec.task.smartrule.adapter.TeamMemberAdapter;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.GetMemberAndUnitMsgEvent;
import com.vitec.task.smartrule.bean.event.ProjectGroupAnyMsgEvent;
import com.vitec.task.smartrule.bean.event.QueryProjectGroupMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.interfaces.ISelectorResultCallBack;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.view.QrCodeDialog;
import com.vitec.task.smartrule.view.SelectAddMethodBottomDialog;
import com.vitec.task.smartrule.view.large_img.SelectorBottomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeasureTeamManagerActivity extends BaseActivity implements View.OnClickListener{

    private RelativeLayout rlChangeProject;//切换测量组框，点击切换到其他项目
//    以下三个框在未点击编辑时，隐藏状态
    private RelativeLayout rlEditProjectName;//编辑项目名称框
    private RelativeLayout rlMenHead;//成员头部框
    private RelativeLayout rlUnitHead;//单位工程头部框
//    三个提示
    private TextView tvSencondTip;//默认隐藏
    private TextView tvThirdTip;
    private TextView tvFourthTip;

    private TextView tvFirstProjectName;//切换项目组里的项目名
    private TextView tvAddMen;//添加测量组成员按钮
    private TextView tvAddUnit;//添加单位工程按钮
    private TextView tvSubmitAddUnit;//确定添加单位工程按钮
    private TextView tvSubmitAndEdit;//最底下的编辑和保存按钮

    private ImageView imgAddMen;//添加测量组成员图片按钮
    private ImageView imgQrCode;//显示二维码按钮
    private ImageView imgAddUnit;//添加单位工程图片按钮

    private EditText etProjectName;//项目名称的输入框
    private EditText etInputUnitName;//添加单位工程的输入框

    private ListView lvUnit;//单位工程列表
    private ListView lvMen;//组成员列表
    private MKLoader mkLoader;

    /*******数据部分********/
    private List<RulerCheckProject> projectList;//所有测量组
    private RulerCheckProject cProject;//当前显示的测量组
    private User user;

    /***组成员部分**/
    private List<ProjectUser> memberList;
    private TeamMemberAdapter memberAdapter;
    /****单位工程部分****/
    private List<RulerUnitEngineer> unitEngineerList;
    private GroupUnitAdapter unitAdapter;
    private boolean isEdit = false;
    /*****为了避免添加，给几个数据源添加一个Set集合******/
    private Set<RulerUnitEngineer> unitSet;
    private Set<ProjectUser> projectUserSet;
    private Set<RulerCheckProject> projectSet;
    private List<SelectorBottomDialog.DataRes> projectDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_team);
        EventBus.getDefault().register(this);
        initView();
        initData();

    }

    private void initData() {
        user = OperateDbUtil.getUser(getApplicationContext());
        unitSet = new HashSet<>();
        projectSet = new HashSet<>();
        projectUserSet = new HashSet<>();
        /*********项目名部分的数据***********/
        projectList = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        LogUtils.show("查看收到的bundle："+bundle.toString());
        if (bundle != null) {
            projectList.addAll((Collection<? extends RulerCheckProject>) bundle.getSerializable(DataBaseParams.check_project_name));
        } else {
            String where = " where " + DataBaseParams.user_user_id + "=" + user.getUserID();
            projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), where);
        }
        projectSet.addAll(projectList);
        LogUtils.show("查看初始化的项目：" + projectList.toString());
        cProject = projectList.get(0);
        tvFirstProjectName.setText(cProject.getProjectName());
        etProjectName.setText(cProject.getProjectName());
        //更新项目切换的数据源
        updateProjectViewData();


        /************向服务器请求当前测量组的成员信息和单位工程信息*****************/
        Bundle b = new Bundle();
        b.putInt(DataBaseParams.measure_project_id, cProject.getServer_id());
        Intent intent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
        intent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_get_member_and_unit);
        intent.putExtra(ProjectManageRequestIntentService.key_get_value, b);
        startService(intent);

        /*****************测量组成员模块的数据*********************/
//        将自己添加到第一个
        memberList = new ArrayList<>();
//        ProjectUser mine = new ProjectUser();
//        mine.setUserName(user.getUserName());
//        mine.setProjectServerId(cProject.getServer_id());
//        mine.setUser_id(user.getUserID());
//        mine.setProjectId(cProject.getId());
//        mine.setMobile(user.getMobile());
//        memberList.add(mine);
        LogUtils.show("查看第一个人员："+memberList);
//        projectUserSet.add(mine);
        memberAdapter = new TeamMemberAdapter(this, memberList);
        memberAdapter.setProject(cProject);
        lvMen.setAdapter(memberAdapter);
        searchProjectUserFromSqlite();


        /**************单位工程模块的数据*************/
        unitEngineerList = new ArrayList<>();
        unitAdapter = new GroupUnitAdapter(this, unitEngineerList);
        lvUnit.setAdapter(unitAdapter);
        searchUnitEngineerFromSqlite();

    }

    @Override
    protected void onResume() {
        super.onResume();
        searchProjectUserFromSqlite();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        initWidget();
        setTvTitle("测量组");
        tvChoose.setText("新建");
        tvChoose.setOnClickListener(this);
        tvChoose.setVisibility(View.VISIBLE);

        mkLoader = findViewById(R.id.mkloader);
        rlUnitHead = findViewById(R.id.rl_add_unit_head);
        rlEditProjectName = findViewById(R.id.rl_edit_project);
        rlMenHead = findViewById(R.id.rl_men_head);
        rlChangeProject = findViewById(R.id.rl_change_project);
        tvSencondTip = findViewById(R.id.tv_sencond_tip);
        tvThirdTip = findViewById(R.id.tv_third_tip);
        tvFourthTip = findViewById(R.id.tv_fourth_tip);
        tvSubmitAddUnit = findViewById(R.id.tv_submit_add_unit);
        tvSubmitAndEdit = findViewById(R.id.tv_submit);
        imgAddMen = findViewById(R.id.img_add_men);
        imgAddUnit = findViewById(R.id.img_add_unit);
        imgQrCode = findViewById(R.id.img_qr_code);
        etProjectName = findViewById(R.id.et_project_name);
        etInputUnitName = findViewById(R.id.et_input_unit_name);
        tvAddMen = findViewById(R.id.tv_add_men);
        lvMen = findViewById(R.id.lv_men);
        lvUnit = findViewById(R.id.lv_unit);
        tvAddUnit = findViewById(R.id.tv_add_unit);
        tvFirstProjectName = findViewById(R.id.tv_change_project_name);


        tvSubmitAndEdit.setOnClickListener(this);
        tvAddMen.setOnClickListener(this);
        imgAddMen.setOnClickListener(this);
        imgQrCode.setOnClickListener(this);
        tvSubmitAddUnit.setOnClickListener(this);
        rlChangeProject.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMemberAndUnitCallBack(GetMemberAndUnitMsgEvent event) {
        if (event.isSuccess()) {
            searchProjectUserFromSqlite();
            /**********搜索其他单位工程信息***********/
            searchUnitEngineerFromSqlite();
        } else {
            Toast.makeText(getApplicationContext(),event.getMsg(),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 请求获取项目信息的回调
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void projectGroupEventCallBack(QueryProjectGroupMsgEvent event) {
        if (event.isSuccess()) {
            queryProjectGroupData();
            LogUtils.show("projectGroupEventCallBack--请求获取项目信息的回调");
        }
    }


    /**
     * 查找数据库中的项目组
     */
    private int queryProjectGroupData() {

        String where = " where " + DataBaseParams.user_user_id + '=' + user.getUserID() +" ORDER BY id DESC";
        List<RulerCheckProject> projectList = OperateDbUtil.queryProjectDataFromSqlite(this, where);
        if (projectList.size() > 0) {
            for (RulerCheckProject project : projectList) {
                if (projectSet.add(project)) {
                    this.projectList.add(project);
                }
            }
        }
        return projectList.size();
    }

    /**
     * 从数据库中搜索单位工程信息，并显示到界面
     */
    private void searchUnitEngineerFromSqlite() {
        String unitWhere= " where " + DataBaseParams.project_server_id + " = " + cProject.getServer_id();
        unitEngineerList.clear();
        unitEngineerList = OperateDbUtil.queryUnitEngineerDataFromSqlite(getApplicationContext(), unitWhere);
//        if (unitEngineerList.size() > 0) {
//            for (int i = 0; i < unitEngineerList.size(); i++) {
//                if (unitSet.add(unitEngineerList.get(i))) {
//                    this.unitEngineerList.add(unitEngineerList.get(i));
//                }
//            }
//        }
        unitAdapter.setUnitEngineerList(this.unitEngineerList);
        unitAdapter.notifyDataSetChanged();
        LogUtils.show("searchUnitEngineerFromSqlite---查看搜索到的单位工程信息：" + unitEngineerList);
        if (this.unitEngineerList.size() == 0) {
            rlUnitHead.setVisibility(View.VISIBLE);
            tvFourthTip.setText("添加单位工程用于统计单位工程质量统计");
        }
        if (unitEngineerList.size() < 5) {
            HeightUtils.setListViewHeighBaseOnChildren(lvUnit);
        } else {
            HeightUtils.setListViewHeighBaseOnChildren(lvUnit,5);
        }
    }


    /**
     * 从数据库中搜索其他成员信息，并显示到界面
     */
    private void searchProjectUserFromSqlite() {
        /********搜索其他成员信息***********/
        String memWhere = " where " + DataBaseParams.project_server_id + " = " + cProject.getServer_id();
        memberList.clear();
         memberList = OperateDbUtil.queryProjectUserFromSqlite(getApplicationContext(), memWhere);

        //避免重复添加
//        for (ProjectUser user : users) {
//            if (projectUserSet.add(user) && user.getServer_id() > 0) {
//                memberList.add(user);
//            }
//        }
//        LogUtils.show("查看第一个人员："+memberList);
        updateMemberViewData();
    }

    /**
     * TODO 各种服务请求的回调
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void projectGroupAnyEventCallBack(ProjectGroupAnyMsgEvent event) {
        LogUtils.show("projectGroupAnyEventCallBack---收到一个响应信息:"+event);
        if (event.isSuccess()) {
            switch (event.getRequst_flag()) {
                /**请求类型区分,
                 * 1-修改项目项目更新,
                 * 2-添加成员信息,
                 * 3-添加单位工程
                 * 4-删除单位工程
                 * 5-删除成员
                 * 6-删除测量组
                 */
                /********1-修改项目名***************/
                case 1:
                    tvFirstProjectName.setText(etProjectName.getText().toString().trim());
                    projectList.remove(cProject);
                    cProject.setProjectName(etProjectName.getText().toString().trim());
                    projectList.add(cProject);
                    tvSencondTip.setVisibility(View.GONE);
                    rlEditProjectName.setVisibility(View.GONE);
                    tvThirdTip.setText("测量组成员");
                    rlMenHead.setVisibility(View.GONE);
                    rlUnitHead.setVisibility(View.GONE);
                    tvFourthTip.setText("单位工程");
                    tvSubmitAndEdit.setText("编辑测量组");
                    unitAdapter.setShowDel(false);
                    memberAdapter.setShowDel(false);
                    unitAdapter.notifyDataSetChanged();
                    memberAdapter.notifyDataSetChanged();
                    updateProjectViewData();
                    isEdit = false;
                    break;

                case 2:
                    break;

                    /*****************3-添加单位工程********************/
                case 3:

                    RulerUnitEngineer engineer = (RulerUnitEngineer) event.getObject();
                    if (unitSet.add(engineer)) {
                        this.unitEngineerList.add(engineer);
                    }
                    updateUnitEngineerViewData();
                    break;

                    /*****************4-删除单位工程**********************/
                case 4:
                    RulerUnitEngineer rulerUnitEngineer = (RulerUnitEngineer) event.getObject();
                    for (int i=0;i<unitEngineerList.size();i++) {
                        if (unitEngineerList.get(i).getServer_id() == rulerUnitEngineer.getServer_id()) {
                            unitEngineerList.remove(i);
                            break;
                        }
                    }
                    updateUnitEngineerViewData();
                    break;

                    /***************5-删除成员******************/
                case 5:
                    ProjectUser user = (ProjectUser) event.getObject();
                    //如果它删除的是自己，则退出测量组。并删除测量组
                   if (this.user.getUserID()==user.getUser_id()){
                       String where = DataBaseParams.server_id + "=?";
                       OperateDbUtil.delData(getApplicationContext(), DataBaseParams.check_project_table_name, where, new String[]{String.valueOf(cProject.getServer_id())});
                       projectList.remove(cProject);
                       if (projectList.size() == 0) {
                           startActivity(new Intent(this, CreateMeasureTeamActivity.class));
                           this.finish();
                       } else {
                           changeProject(projectList.get(0));
                       }

                   }else{
                       for (int i=0;i<memberList.size();i++) {
                           if (memberList.get(i).getServer_id() == user.getServer_id()) {
                               memberList.remove(i);
                               break;
                           }
                       }
                       updateMemberViewData();
                   }

                    break;


                    /************6-删除测量组*******************/
                case 6:
                    LogUtils.show("收到删除测量组");
                    RulerCheckProject project = (RulerCheckProject) event.getObject();
                    for (int i=0;i<projectList.size();i++) {
                        if (projectList.get(i).getServer_id() == project.getServer_id()) {
                            projectList.remove(i);
                            break;
                        }
                    }
                    if (projectList.size() == 0) {
                        startActivity(new Intent(this, CreateMeasureTeamActivity.class));
                        this.finish();
                    } else {
                        changeProject(projectList.get(0));
                    }
                    break;
            }
            mkLoader.setVisibility(View.GONE);
        } else {
            Toast.makeText(getApplicationContext(),event.getMsg(),Toast.LENGTH_SHORT).show();
        }
    }





    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 点击新建按钮
             */
            case R.id.tv_choose:
                startActivity(new Intent(this, CreateMeasureTeamActivity.class));
                break;

            /**
             * 点击进入编辑界面
             */
            case R.id.tv_submit:
//                是否进入编辑页面
                if (!isEdit) {
                    tvSencondTip.setVisibility(View.VISIBLE);
                    rlEditProjectName.setVisibility(View.VISIBLE);
                    tvThirdTip.setText("添加成员");
                    rlMenHead.setVisibility(View.VISIBLE);
                    rlUnitHead.setVisibility(View.VISIBLE);
                    tvFourthTip.setText("添加单位工程用于统计单位工程质量统计");
                    tvSubmitAndEdit.setText("保存并返回");
                    unitAdapter.setShowDel(true);
                    memberAdapter.setShowDel(true);
                    unitAdapter.notifyDataSetChanged();
                    memberAdapter.notifyDataSetChanged();
                    isEdit = true;
                } else {
                    if (etProjectName.getText().toString().trim().equals(cProject.getProjectName())) {
                        tvSencondTip.setVisibility(View.GONE);
                        rlEditProjectName.setVisibility(View.GONE);
                        tvThirdTip.setText("测量组成员");
                        rlMenHead.setVisibility(View.GONE);
                        rlUnitHead.setVisibility(View.GONE);
                        tvFourthTip.setText("单位工程");
                        tvSubmitAndEdit.setText("编辑测量组");
                        unitAdapter.setShowDel(false);
                        memberAdapter.setShowDel(false);
                        unitAdapter.notifyDataSetChanged();
                        memberAdapter.notifyDataSetChanged();
                        isEdit = false;
                    } else {
                        mkLoader.setVisibility(View.VISIBLE);
                        Bundle bundle = new Bundle();
                        bundle.putString(DataBaseParams.check_project_name, etProjectName.getText().toString().trim());
                        bundle.putInt(DataBaseParams.user_user_id, user.getUserID());
                        bundle.putInt(DataBaseParams.measure_id, cProject.getServer_id());
                        Intent addUnitIntent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
                        addUnitIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_update_proect);
                        addUnitIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                        startService(addUnitIntent);

                    }


                }
                break;

            /**
             * 点击添加测量组成员,弹出选择添加的方式
             */
            case R.id.img_add_men:
            case R.id.tv_add_men:
                LogUtils.show("查看当前测量组:"+cProject);
                SelectAddMethodBottomDialog bottomDialog = new SelectAddMethodBottomDialog(this, R.style.BottomDialog, cProject, ScreenUtils.getScreenWidth(this));
                bottomDialog.show();
                break;

            /**
             * 显示二维码
             */
            case R.id.img_qr_code:
                LogUtils.show("查看当前测量组:"+cProject);
                QrCodeDialog dialog = new QrCodeDialog(MeasureTeamManagerActivity.this,R.style.BottomDialog,cProject.getQrCode(), ScreenUtils.getScreenWidth(this));
                dialog.show();
                break;

            /**
             * TODO 添加单位工程
             */
            case R.id.tv_submit_add_unit:
                if (etInputUnitName.getText().toString().trim().length() < 1) {
                    Toast.makeText(getApplicationContext(),"单位工程不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                mkLoader.setVisibility(View.VISIBLE);
                Bundle bundle = new Bundle();
                bundle.putString(DataBaseParams.unit_engineer_location, etInputUnitName.getText().toString().trim());
                bundle.putInt(NetConstant.group_project_list, cProject.getServer_id());
                Intent addUnitIntent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
                addUnitIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_add_unit_engineer);
                addUnitIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                startService(addUnitIntent);
                break;

            /**
             * TODO 切换项目
             */
            case R.id.rl_change_project:
                final SelectorBottomDialog bottomDialog1 = new SelectorBottomDialog(this, R.style.BottomDialog, projectDataList);
                bottomDialog1.setSelectorResultCallBack(new ISelectorResultCallBack() {
                    @Override
                    public void onSelectCallBack(String item, int index) {
                        LogUtils.show("查看选中的：" + item + ",序号：" + index+",,,,数据源："+projectDataList.get(index));
                        SelectorBottomDialog.DataRes dataRes = projectDataList.get(index);
                        for (RulerCheckProject project : projectList) {
                            if (dataRes.getId() == project.getId()) {
                                changeProject(project);
                                break;
                            }
                        }
                        bottomDialog1.dismiss();
                    }
                });
                bottomDialog1.show();
                break;
        }
    }

    private void updateProjectViewData() {
        projectDataList = new ArrayList<>();
        for (RulerCheckProject project : projectList) {
            SelectorBottomDialog.DataRes dataRes = new SelectorBottomDialog.DataRes();
            dataRes.setId(project.getId());
            dataRes.setData(project.getProjectName());
            projectDataList.add(dataRes);
        }
    }

    /**
     * TODO 切换项目组
     */
    private void changeProject(RulerCheckProject project) {
        cProject = project;
        /************向服务器请求当前测量组的成员信息和单位工程信息*****************/
        Bundle b = new Bundle();
        b.putInt(DataBaseParams.measure_project_id, cProject.getServer_id());
        Intent intent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
        intent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_get_member_and_unit);
        intent.putExtra(ProjectManageRequestIntentService.key_get_value, b);
        startService(intent);
        memberAdapter.setProject(cProject);
        tvFirstProjectName.setText(cProject.getProjectName());
        etProjectName.setText(cProject.getProjectName());
//        ProjectUser user = memberList.get(0);
        memberList.clear();
        projectUserSet.clear();
//        projectUserSet.add(user);
//        memberList.add(user);
        unitSet.clear();
        unitEngineerList.clear();
        searchUnitEngineerFromSqlite();
        searchProjectUserFromSqlite();

    }

    /**
     * Todo 更新成员的控件数据
     */
    private void updateMemberViewData() {
        memberAdapter.setMemberList(memberList);
        memberAdapter.notifyDataSetChanged();
        if (memberList.size() < 5) {
            HeightUtils.setListViewHeighBaseOnChildren(lvMen);
        } else {
            HeightUtils.setListViewHeighBaseOnChildren(lvMen,5);
        }
    }

    /**
     * TODO 更新单位工程的控件数据
     */
    private void updateUnitEngineerViewData() {
        unitAdapter.setUnitEngineerList(this.unitEngineerList);
        LogUtils.show("开始更新单位工程--:"+unitEngineerList.size());
        etInputUnitName.setText("");
        unitAdapter.notifyDataSetChanged();
        if (this.unitEngineerList.size() == 0) {
            rlUnitHead.setVisibility(View.VISIBLE);
            tvFourthTip.setText("添加单位工程用于统计单位工程质量统计");
        }
        if (unitEngineerList.size() < 5) {
            HeightUtils.setListViewHeighBaseOnChildren(lvUnit);
        } else {
            HeightUtils.setListViewHeighBaseOnChildren(lvUnit,4);
        }
    }




}
