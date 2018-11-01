package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.ChooseMeasureProjectAdapter;
import com.vitec.task.smartrule.bean.EngineerBean;
import com.vitec.task.smartrule.bean.NetCallBackMessage;
import com.vitec.task.smartrule.bean.OptionBean;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.net.NetParams;
import com.vitec.task.smartrule.net.OkHttpHelper;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.OperateDbUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class ChooseMeasureMsgActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG ="ChooseMeasureMsgActivity";
    private ListView lvChoose;
    private ImageView imgAddProject;
    private ChooseMeasureProjectAdapter projectAdapter;
    private List<OptionBean> projects;
    private int projectCount = 1;

    private OkHttpHelper okHttpHelper;
    private List<EngineerBean> engineers;
    private List<OptionBean> optionBeans;

    private BleDataDbHelper dataDbHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_measure_msg2);
        EventBus.getDefault().register(this);
        initView();
        initData();


    }

    private void initData() {
        projects = new ArrayList<>();

        okHttpHelper = new OkHttpHelper();
        engineers = new ArrayList<>();
        optionBeans = new ArrayList<>();
//        初始化数据库helper
        dataDbHelper = new BleDataDbHelper(getApplicationContext());
//        获取本地数据库的工程表格的所有数据
        optionBeans = dataDbHelper.queryOptionsAllDataFromSqlite();
        engineers = dataDbHelper.queryEnginAllDataFromSqlite();
//        如果engineers的个数为0，则说明数据库中没有数据，则向服务器发起请求
        if (optionBeans.size() == 0 || engineers.size() == 0) {
            /**获取管控要点的Json字符串
             */
            okHttpHelper.getDataFromServer(NetParams.getOptionInfoUrl, 2);
        } else {
            for (EngineerBean engineerBean : engineers) {
                List<OptionBean> optionList = new ArrayList<>();
                for (OptionBean optionBean : optionBeans) {
                    if (engineerBean.getProjectID() == optionBean.getEnginId()) {
                        optionList.add(optionBean);
                    }
                }
                if (optionList.size() > 0) {
                    engineerBean.setMeasureBeanList(optionList);
                }
            }
        }
        projectAdapter = new ChooseMeasureProjectAdapter(this, engineers);
        lvChoose.setAdapter(projectAdapter);
        if (engineers.size() > 0) {
//            HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
        }

    }

    private void initView() {
        lvChoose = findViewById(R.id.lv_choose_msg);
        imgAddProject = findViewById(R.id.img_add_project);
        imgAddProject.setOnClickListener(this);
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
            String engineerJson = message.getResultJson();
            Log.e("", "netBussCallBack: 查看返回的工程："+engineerJson );
            if (!engineerJson.equals("")) {
                JSONObject optionJsons = null;
                try {
                    optionJsons = new JSONObject(engineerJson);
                    String dataJson = optionJsons.optString("data");
                    JSONArray jsonArray = new JSONArray(dataJson);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        EngineerBean engineerBean = new EngineerBean();
                        engineerBean.setCheckPerson("张三");
                        engineerBean.setPersonId(2);
                        engineerBean.setProjectID(Integer.parseInt(object.optString("id")));
                        engineerBean.setProjectEngineer(object.optString("name"));
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        engineerBean.setCheckTime(currentDateTimeString);
                        if (engineerBean.getProjectID() == 1) {
                            engineerBean.setMeasureBeanList(optionBeans);
                        }
                        engineers.add(engineerBean);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
//            将获取到的数据保存到本地数据库
            OperateDbUtil.addEnginAndOptionsData(getApplicationContext(), engineers);
            projectAdapter.setEngineerBeanList(engineers);
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
            String optionJson = message.getResultJson();
            Log.e("", "netBussCallBack: 查看返回的管控要点："+optionJson );
            if (!optionJson.equals("")) {
                try {
                    JSONObject optionJsons = new JSONObject(optionJson);
                    String dataJson = optionJsons.optString("data");
                    JSONArray jsonArray = new JSONArray(dataJson);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        OptionBean bean = new OptionBean();
                        bean.setMeasureItemName(object.optString("name"));
                        bean.setPassStandard(object.optString("standard"));
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(object.optString("methods"));
                        bean.setCheckWay(stringBuffer);
                        bean.setResourceID(R.mipmap.icon_data_selected);
                        bean.setEnginId(Integer.parseInt(object.optString("engin_id")));
                        if (bean.getEnginId() == 1) {
                            optionBeans.add(bean);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /**获取工程的Json字符串
             */
            okHttpHelper.getDataFromServer(NetParams.getEngineerInfoUrl,1);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_add_project:
//                projects.add(new OptionBean());
//                projectAdapter.setProjects(projects);
//                projectAdapter.notifyDataSetChanged();
                HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
                Log.e("aaa", "onClick: 点击了添加按钮" );
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
