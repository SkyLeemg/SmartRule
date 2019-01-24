package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.ChooseMeasureProjectAdapter;
import com.vitec.task.smartrule.bean.ChooseMeasureMsg;
import com.vitec.task.smartrule.bean.NetCallBackMessage;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.QueryProjectGroupMsgEvent;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.interfaces.IChooseGetter;
import com.vitec.task.smartrule.net.FileOkHttpUtils;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.GetMudelIntentService;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.service.intentservice.ReplenishDataToServerIntentService;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 点击进入测量后 选择项目工程的页面
 * 待解决问题：
 * 1.解决listview和scrollview滑动冲突的问题
 *
 */
public class ChooseMeasureMsgActivity extends BaseActivity implements View.OnClickListener,IChooseGetter{

    private static final String TAG ="ChooseMeasureMsgActivity";
    private ListView lvChoose;
//    private ImageView imgAddProject;
    private ChooseMeasureProjectAdapter projectAdapter;
    private int projectCount = 1;

    private FileOkHttpUtils fileOkHttpUtils;
    private BleDataDbHelper dataDbHelper;

    private User user;

    //    新的对象
    private List<RulerEngineer> engineerList;//模板
    private List<RulerOptions> optionsList;//模板
    private List<RulerCheckProject> projectList;
    private List<ChooseMeasureMsg> chooseMeasureMsgList;//所有item的数据集合，集合engineerlist模板和optionsList模板的信息
    private ChooseMeasureMsg chooseMeasureMsg;//一个item模板
    private boolean hasUpdate = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_measure_msg2);
        EventBus.getDefault().register(this);
        initView();
        initData();
        Log.e("", "onCreate: 选择信息页面被创建了" );

    }

    private void initData() {
        user = OperateDbUtil.getUser(getApplicationContext());
        /**
         * 测试区域
         */
//        HandleBleMeasureDataReceiverService.startHandleService(getApplicationContext());

//        新数据对象初始化
        optionsList = new ArrayList<>();
        engineerList = new ArrayList<>();
        chooseMeasureMsgList = new ArrayList<>();
        chooseMeasureMsg = new ChooseMeasureMsg();
//        User user = OperateDbUtil.getUser(getApplicationContext());


        /**
         * 获取所有项目表的数据
         */
        requestProjectData();
//        String projectWhere = " where " + DataBaseParams.user_user_id + " = " + user.getUserID();
        projectList = new ArrayList<>();
        projectList = OperateDbUtil.queryAllProjectOrderMember(getApplicationContext(), user.getUserID());
        fileOkHttpUtils = new FileOkHttpUtils();
//        旧数据对象初始化
//        初始化数据库helper
        dataDbHelper = new BleDataDbHelper(getApplicationContext());
//        获取本地数据库的工程表格的所有数据
        optionsList = dataDbHelper.queryOptionsAllDataFromSqlite("");
        engineerList = dataDbHelper.queryEnginDataFromSqlite("");
//        如果engineers的个数为0，则说明数据库中没有数据，则向服务器发起请求
//        if (optionsList.size() == 0 || engineerList.size() == 0) {
            /**获取工程的Json字符串
             */
            Intent intent = new Intent(this, GetMudelIntentService.class);
            startService(intent);

//        }
        initItemModel();
        projectAdapter = new ChooseMeasureProjectAdapter(this, this);
        lvChoose.setAdapter(projectAdapter);
        HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
         /**
          * 补上传服务启动
          */
        Intent replenishIntent = new Intent(getApplicationContext(), ReplenishDataToServerIntentService.class);
        startService(replenishIntent);

    }

   private void requestProjectData() {
       Intent queryIntent = new Intent(this, ProjectManageRequestIntentService.class);
       Bundle bundle = new Bundle();
       bundle.putInt(DataBaseParams.user_user_id, user.getUserID());
       bundle.putInt(NetConstant.page_size, 50);
       bundle.putInt(NetConstant.current_Page, 1);
       queryIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_get_project_list);
       queryIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
       startService(queryIntent);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void projectGroupEventCallBack(QueryProjectGroupMsgEvent event) {
        LogUtils.show("projectGroupEventCallBack---接收到项目更新的返回:"+event.isSuccess());
        if (event.isSuccess()) {
//            String where = " where " + DataBaseParams.user_user_id + '=' + user.getUserID()+" ORDER BY id DESC";
          List<RulerCheckProject>  projectList = OperateDbUtil.queryAllProjectOrderMember(getApplicationContext(), user.getUserID());
            hasUpdate = true;
            for (RulerCheckProject project : projectList) {
                /************向服务器请求当前测量组的成员信息和单位工程信息*****************/
                Bundle b = new Bundle();
                b.putInt(DataBaseParams.measure_project_id, project.getServer_id());
                Intent intent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
                intent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_get_member_and_unit);
                intent.putExtra(ProjectManageRequestIntentService.key_get_value, b);
                startService(intent);
            }
        }
    }


    /**
     * 初始化一个空的item模板，
     */
    private void initItemModel() {

        ChooseMeasureMsg measureMsg = new ChooseMeasureMsg();
//        从sharePrefrence中获取用户数据
//        Set<String> keySet = new HashSet<>();
//        keySet.add(SharePreferenceUtils.user_id);
//        keySet.add(SharePreferenceUtils.user_login_name);
//        keySet.add(SharePreferenceUtils.user_pwd);
//        keySet.add(SharePreferenceUtils.user_real_name);
//        Map<String, String> map = SharePreferenceUtils.getData(getApplicationContext(), keySet, SharePreferenceUtils.user_table);
//        User user = new User();
//        user.setUserName(map.get(SharePreferenceUtils.user_real_name));
//        user.setUserID(Integer.parseInt(map.get(SharePreferenceUtils.user_id)));
//        user.setLoginName(map.get(SharePreferenceUtils.user_login_name));
        User user = OperateDbUtil.getUser(getApplicationContext());
        LogUtils.show("获取到的用户信息:"+user);
        measureMsg.setUser(user);
        String date = DateFormat.getDateInstance().format(new Date());
        measureMsg.setCreateDate(date);
        measureMsg.setEngineerList(engineerList);
        chooseMeasureMsgList.add(measureMsg);
        chooseMeasureMsg = measureMsg;

    }

    private void initView() {
        lvChoose = findViewById(R.id.lv_choose_msg);
        initWidget();
        imgMenu.setOnClickListener(this);
        setTvTitle("新建测量");
//        imgAddProject = findViewById(R.id.img_add_project);
//        imgAddProject.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netBussCallBack(NetCallBackMessage message) {
        /**获取工程的Json字符串
         * 返回的数据样本
         * {"status":"success",
         *  "code":200,
         *  "data":[{"id":1,"name":"混凝土工程"}],"msg":"查询成功"}
         */
        if (message.getUrlFlag() == 1) {
//            String engineerJson = message.getResultJson();
//            Log.e("aaa", "netBussCallBack: 查看返回的工程："+engineerJson );
//            if (!engineerJson.equals("")) {
//                JSONObject optionJsons = null;
//                try {
//                    optionJsons = new JSONObject(engineerJson);
//                    String dataJson = optionJsons.optString("data");
//                    JSONArray jsonArray = new JSONArray(dataJson);
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject object = jsonArray.getJSONObject(i);
////                        将服务器中engineers表里面的数据存到rulerEngineer对象
//                        RulerEngineer engineer = new RulerEngineer();
//                        engineer.setCreateTime((int) System.currentTimeMillis());
////                        rulerEngineer.setEngineerDescription(object.optString(""));
//                        engineer.setEngineerName(object.optString("name"));
//                        engineer.setServerID(Integer.parseInt(object.optString("id")));
//                        engineerList.add(engineer);
//                        Log.e("aaa", "netBussCallBack: 从服务器下载下来保存成为对象后的数据："+engineer.toString() );
////                        initItemModel();
//                    }
////                    将数据保存到数据库
//                    OperateDbUtil.addEngineerMudelData(getApplicationContext(),engineerList);
//                    /**获取管控要点的Json字符串
//                     */
//                    fileOkHttpUtils.getDataFromServer(NetParams.getOptionInfoUrl, 2);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }

            engineerList = dataDbHelper.queryEnginDataFromSqlite("");
            projectAdapter.initData();
            projectAdapter.notifyDataSetChanged();
            HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
        }
        /**获取管控要点的Json字符串
         *
         * 获取回来的信息样本：
         * {"status":"success",
         * "code":200,
         * "data":[
         * {"id":1,"name":"立面垂直度","standard":"≤8mm\/≤10mm(≤6m取8)",
         * "methods":"2米靠尺测量,每个柱选取相邻2面测量;当所选墙面长度小于3m时,两端测量2尺;当所选墙面长度大于 3m时,两端测量2尺、中间水平测量1尺,每个测量值作为1个计算点",
         * "engin_id":1},
         * {"id":2,"name":"表面平整度","standard":"≤8mm",
         * "methods":"2米靠尺测量,每个墙柱选取一面作为测量点,砼柱平整度测量对角线;当所选墙面长度小于3m时,两端45°测量2尺;当所选墙面长度大于3m时,两端45°测量2尺、中间水平测量1尺;相邻构件跨洞口必测,每个测量值作为1个计算点",
         * "engin_id":1}],"msg":"查询成功"}
         */
        else if (message.getUrlFlag() == 2) {
            /**
             * 先处理OptionJson的数据，转为optionBean对象
             */
//            String optionJson = message.getResultJson();
//            Log.e("aaa", "netBussCallBack: 查看返回的管控要点："+optionJson );
//            if (!optionJson.equals("")) {
//                try {
//                    JSONObject optionJsons = new JSONObject(optionJson);
//                    String dataJson = optionJsons.optString("data");
//                    JSONArray jsonArray = new JSONArray(dataJson);
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject object = jsonArray.getJSONObject(i);
//                        RulerOptions option = new RulerOptions();
//                        option.setCreateTime((int) System.currentTimeMillis());
//                        option.setServerID(Integer.parseInt(object.optString("id")));
//                        option.setMethods(object.optString("methods"));
//                        option.setStandard(object.optString("standard"));
//                        option.setOptionsName(object.optString("name"));
////                        for (int j=0;j<engineerList.size();j++) {
////                            if (engineerList.get(j).getServerID() == Integer.parseInt(object.optString("engin_id"))) {
////                                option.setEngineer(engineerList.get(j));
////                            }
////                        }
//                        Log.e("aaa", "netBussCallBack: 服务器下载下来的管控要点："+option);
//                        optionsList.add(option);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            initItemModel();
//            //            将获取到的数据保存到本地数据库
//            OperateDbUtil.addOptionsMudelData(getApplicationContext(), optionsList);
//            projectAdapter = new ChooseMeasureProjectAdapter(this, this);
//            lvChoose.setAdapter(projectAdapter);
////            projectAdapter.setEngineerBeanList(engineers);
////            projectAdapter.notifyDataSetChanged();
//            HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
            optionsList = dataDbHelper.queryOptionsAllDataFromSqlite("");
            projectAdapter.initData();
            projectAdapter.notifyDataSetChanged();
            HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_add_project:
                ChooseMeasureMsg measureMsg = new ChooseMeasureMsg();
                User user = new User();
                user.setUserName("张三");
                measureMsg.setUser(user);
                String date = DateFormat.getDateInstance().format(new Date());
                measureMsg.setCreateDate(date);
                measureMsg.setEngineerList(engineerList);
                chooseMeasureMsgList.add(measureMsg);
                Log.e("aaa", "onClick: 查看模板信息："+measureMsg.toString() );
                Log.e("aaa", "onClick: 查看你所有的list信息："+chooseMeasureMsgList.toString() );
                projectAdapter.setChooseMeasureMsgList(chooseMeasureMsgList);
                projectAdapter.notifyDataSetChanged();
                HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
                Log.e("aaa", "onClick: 点击了添加按钮" );
                break;

            case R.id.img_menu_toolbar:
                this.finish();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public List<RulerEngineer> getEngineerList() {
        return engineerList;
    }

    @Override
    public List<RulerOptions> getOptionsList() {
        return optionsList;
    }

    @Override
    public List<ChooseMeasureMsg> getChooseMeasureMsgList() {
        return chooseMeasureMsgList;
    }

    @Override
    public List<RulerCheckProject> getCheckProjectList() {
        if (hasUpdate) {
//            String where = " where " + DataBaseParams.user_user_id + '=' + user.getUserID()+" ORDER BY id DESC";
            projectList = OperateDbUtil.queryAllProjectOrderMember(getApplicationContext(), user.getUserID());
            hasUpdate = false;
        }
        return projectList;
    }

    /**
     * 跳转页面后。结束改页面
     */
    @Override
    public void finishActivity() {
        this.finish();
    }

    @Override
    public void updateChooseMeasureMsgList(int index, ChooseMeasureMsg chooseMeasureMsg) {
        chooseMeasureMsgList.set(index, chooseMeasureMsg);
    }

    @Override
    public void updateProjectList() {
        projectList = OperateDbUtil.queryAllProjectOrderMember(getApplicationContext(), user.getUserID());
    }
}
