package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.bean.event.DelMeasureRecordMsgEvent;
import com.vitec.task.smartrule.bean.event.HandleDataResultMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureDataMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureRecordMsgEvent;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.bean.event.StopMeasureMsgEvent;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 进入测量需要向服务器发起的一些网络请求操作，分多种情况：
 * 一、网络畅通的情况：
 *   1.创建记录表→更新测量数据
 *   2.用户退出记录表然后重新进入：获取数据→更新测量数据
 *   3.用户修改了记录表：修改记录表→获取数据→更新测量数据
 * 二、网络不畅通时：
 *   1.过程与上面相同，但会屡次经历请求失败。
 *   2.补上传时 更新测量数据模块请求的数据内容不一样
 *
 */
public class PerformMeasureNetIntentService extends IntentService {

    public static final String GET_FLAG_KEY = "flag";
    public static final int FLAG_CREATE_RECORD = 1;//创建记录表的标志
    public static final int FLAG_UPDATE_RECORD = 2;//修改记录表的标志
    public static final int FLAG_GET_MEASURE_DATA = 3;//获取测量数据的标志
    public static final int FLAG_UPDATE_DATA = 4;//更新测量数据的标志
    public static final int FLAG_FINISH_MEASURE = 5;//停止测量的标志
    public static final int FLAG_QUERY_MEASURE_RECORD = 6;//查询测量记录表的标志
    public static final int FLAG_DEL_RECORD = 7;//删除记录表的标志


    public static final String GET_DATA_KEY = "data";
    public static final String GET_CREATE_RULER_DATA_KEY = "ruler_check";
    public static final String GET_CREATE_OPTIONS_DATA_KEY = "ruler_options_list";
    public static final String GET_UPDATE_DATA_KEY = "ruler_options_data_list";
    public static final String GET_FINISH_MEASURE_KEY = "finish_measure";//获取停止测量接口的参数值




    public PerformMeasureNetIntentService() {
        super("PerformMeasureNetIntentService");
    }

    public PerformMeasureNetIntentService(String name) {
        super(name);
    }

    /**
     * 一共有4个网络请求，包括：创建记录表、更新测量数据、获取数据、修改记录表
     * 根据FLAG标志来判断是哪一个网络请求，根据
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int flag = intent.getIntExtra(GET_FLAG_KEY, 0);
        String data = intent.getStringExtra(GET_DATA_KEY);
        LogUtils.show("onHandleIntent---init 查看data：" + data);
        switch (flag) {
            /**
             * 创建记录表的标志
             */
            case FLAG_CREATE_RECORD:
                RulerCheck rulerCheck = (RulerCheck) intent.getSerializableExtra(GET_CREATE_RULER_DATA_KEY);
                List<RulerCheckOptions> checkOptionsList = (List<RulerCheckOptions>) intent.getSerializableExtra(GET_CREATE_OPTIONS_DATA_KEY);
                initCreateRecordJsonData(rulerCheck,checkOptionsList);
                break;

            /**
             * 获取测量数据的标志
             */
            case FLAG_GET_MEASURE_DATA:
                RulerCheck getDataRulerCheck = (RulerCheck) intent.getSerializableExtra(GET_DATA_KEY);
                requestQueryMeasureData(getDataRulerCheck);
                break;

            /**
             * 更新(上传)测量数据的标志
             */
            case FLAG_UPDATE_DATA:
                List<RulerCheckOptions> checkOptionsList1 = (List<RulerCheckOptions>) intent.getSerializableExtra(GET_CREATE_OPTIONS_DATA_KEY);
                List<RulerCheckOptionsData> checkOptionsDataList = (List<RulerCheckOptionsData>) intent.getSerializableExtra(GET_UPDATE_DATA_KEY);
                initUpdateMeasureDataJson(checkOptionsList1, checkOptionsDataList);
                break;

            /**
             * 修改记录表的标志
             */
            case FLAG_UPDATE_RECORD:

                break;

            /**
             * 停止测量的标志
             */
            case FLAG_FINISH_MEASURE:
                List<RulerCheck> rulerCheckList = (List<RulerCheck>) intent.getSerializableExtra(GET_FINISH_MEASURE_KEY);
                LogUtils.show("PerformMeasureNetIntentService----结束测量----查看收到的总数："+rulerCheckList.size());
                for (int i=0;i<rulerCheckList.size();i++) {
                    int check_server_id = rulerCheckList.get(i).getServerId();
                    LogUtils.show("查看需要结束的server_id:" + check_server_id);
                    /**
                     * 如果check_server_id大于0说明之前创建记录表有成功请求到服务器
                     * 否则说明之前一直处于无网络状态，所以没有check_server_id,的先请求一遍
                     */
                    if (check_server_id > 0) {
//                        有server_id的时候请求停止测量的接口
                        requestStopMeasure(check_server_id);
                    } else {
//                        没有server_id的时候，请求更新测量数据的接口，更新测量数据请求的参数格式走无网络的格式
                        String where =" where "+ DataBaseParams.measure_option_check_id + " = " + rulerCheckList.get(i).getId();
                        List<RulerCheckOptions> checkOptionsList2 = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rulerCheckList.get(i), where);
                        List<RulerCheckOptionsData> dataList = new ArrayList<>();
                        for (RulerCheckOptions rulerCheckOptions : checkOptionsList2) {
                            List<RulerCheckOptionsData> rulerCheckOptionsDataList = OperateDbUtil.queryMeasureDataFromSqlite(getApplicationContext(), rulerCheckOptions);
                            dataList.addAll(rulerCheckOptionsDataList);
                        }
                        initUpdateMeasureDataJson(checkOptionsList2, dataList);
                    }
                }

                break;

            /**
             * 查询测量记录表的标志
             */
            case FLAG_QUERY_MEASURE_RECORD:
                /********获取查询标志，0 未测量完成的，1 测量完成的****************/
                int finish_flag = intent.getIntExtra(GET_DATA_KEY, -1);
                if (finish_flag >= 0) {
                    int currentPage = intent.getIntExtra(NetConstant.current_Page,1);
                    int pageSize = intent.getIntExtra(NetConstant.page_size, 20);
                    requestQueryMeasureRecord(finish_flag,currentPage,pageSize);
                }
                break;

            /**
             * 删除记录表
             */
            case FLAG_DEL_RECORD:
                String check_id_list = intent.getStringExtra(GET_DATA_KEY);
                requestDelRecord(check_id_list);

                break;
        }
    }

    /****************************删除记录表接口请求开始***********************************/
    private void requestDelRecord(final String check_list) {
        User user = OperateDbUtil.getUser(getApplicationContext());
        int user_id = user.getUserID();
        String wid = user.getWid();
        if (wid == null || wid.equals("null")) {
            wid = "0";
        }
        OkHttpUtils.Param userIdParam = new OkHttpUtils.Param(DataBaseParams.user_user_id, String.valueOf(user_id));
        OkHttpUtils.Param widParam = new OkHttpUtils.Param(DataBaseParams.user_wid, wid);
        OkHttpUtils.Param checkListParam = new OkHttpUtils.Param(NetConstant.del_check_list, check_list);
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(userIdParam);
        paramList.add(widParam);
        paramList.add(checkListParam);
        String url = NetConstant.baseUrl + NetConstant.del_record_url;
        LogUtils.show("删除记录表请求的URL："+url+"，参数："+paramList.toString());
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("查看删除记录表服务器响应的数据："+response);
                /*
                {"status":"success","code":200,"msg":"删除记录表成功"}
                 */
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    if (code == 200) {

                        String[] ids = check_list.split(",");
                        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                        boolean result = false;
                        for (int j=0;j<ids.length;j++) {
                            /**
                             * 1.删除ruler_check对应的ruler_check_options和ruler_check_options_data的数据
                             */
                           delLocalData(ids[j]);
                        }



                        EventBus.getDefault().post(new DelMeasureRecordMsgEvent(true));
                    } else if (code == 404) {
                        if (check_list.contains(",")) {
                            String[] ids = check_list.split(",");
                            for (int i = 0; i < ids.length; i++) {
                                LogUtils.show("开始挨个删除：" + ids[i]);
                                requestDelRecord(ids[i]);
                            }
                        } else {
                            delLocalData(check_list);
                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Exception e) {
                EventBus.getDefault().post(new DelMeasureRecordMsgEvent(false));
            }
        },paramList);

    }

    private void delLocalData(String server_id) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        /**
         * 1.删除ruler_check对应的ruler_check_options和ruler_check_options_data的数据
         */
        String checkWhere = " where " + DataBaseParams.server_id + " = " + server_id;
        List<RulerCheck> rc = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(checkWhere);
        if (rc.size() > 0) {
            String where = " where " + DataBaseParams.measure_option_check_id + " = " + rc.get(0).getId();
            List<RulerCheckOptions> checkOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rc.get(0), where);
            if (checkOptionsList.size() > 0) {
                for (RulerCheckOptions checkOptions : checkOptionsList) {
                    String delWhere = DataBaseParams.options_data_check_options_id + " = ?";
                    boolean delResult = bleDataDbHelper.delData(DataBaseParams.options_data_table_name, delWhere, new String[]{String.valueOf(checkOptions.getId())});
                    LogUtils.show( "管控要点ID为："+checkOptions.getId()+"的测量数据删除结果："+delResult);
                }
            }

            String optionWhere = DataBaseParams.measure_option_check_id + " = ?";
            boolean optionsResult = bleDataDbHelper.delData(DataBaseParams.measure_option_table_name, optionWhere, new String[]{String.valueOf(rc.get(0).getId())});
        }

        /**
         * 2. 删除本地数据库的ruler_check数据
         */
        String delWhere = DataBaseParams.server_id + "=?";
        boolean result = bleDataDbHelper.delData(DataBaseParams.measure_table_name, delWhere, new String[]{String.valueOf(server_id)});
        LogUtils.show("在服务中删除-----服务id为"+server_id+"的删除结果："+result);
        bleDataDbHelper.close();
    }

    /****************************删除记录表接口请求结束***********************************/




    /**************************获取测量数据接口请求开始*********************************/
    private void requestQueryMeasureData(final RulerCheck check) {
        User user = OperateDbUtil.getUser(getApplicationContext());
        int user_id = user.getUserID();
        String wid = user.getWid();
        if (wid == null || wid.equals("null")) {
            wid = "0";
        }
//        链接参数为：   ?check_id=1&user_id=1&wid=0
        StringBuffer sUrl = new StringBuffer();
        sUrl.append(NetConstant.baseUrl);
        sUrl.append(NetConstant.get_measure_data_url);
        sUrl.append("?");
        sUrl.append(NetConstant.get_measure_data_check_id);
        sUrl.append("=");
        sUrl.append(check.getServerId());
        sUrl.append("&");
        sUrl.append(NetConstant.user_id);
        sUrl.append("=");
        sUrl.append(user_id);
        sUrl.append("&");
        sUrl.append(NetConstant.wid);
        sUrl.append("=");
        sUrl.append(wid);
        LogUtils.show("requestQueryMeasureData---获取测量数据的请求链接：" + sUrl.toString());
        OkHttpUtils.get(sUrl.toString(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                /**
                 * 响应的数据样本
                 * {
                 "status":"success",
                 "code":200,
                 "data":[
                 {
                 "id":156,
                 "options_id":1,
                 "floor_height":0,
                 "measured_points":6,
                 "qualified_points":6,
                 "percent_pass":100,
                 "local_id":61,
                 "option_template":{
                 "id":1,"name":"立面垂直度","standard":"≤8mm/≤10mm(≤6m取8)","methods":"2米靠尺测量,每个柱选取相邻2面测量;当所选墙面长度小于3m时,两端测量2尺;当所选墙面长度大于 3m时,两端测量2尺、中间水平测量1尺,每个测量值作为1个计算点",
                 "type":1,
                 "measure":[
                 {"id":1,"operate":2,"standard":8,"data":"<=6mm"},
                 {"id":2,"operate":2,"standard":10,"data":">=6mm"}
                 ]
                 },
                 "options_data":[
                 {"id":63,"data":4.2000000000000002},
                 {"id":64,"data":4.2000000000000002},
                 {"id":65,"data":4.2000000000000002},
                 {"id":66,"data":2},
                 {"id":67,"data":5},
                 {"id":68,"data":5}
                 ]
                 }
                 */
                LogUtils.show("requestQueryMeasureData---查看获取测量数据的响应数据内容："+response.toString());
                /**
                 * 响应数据处理流程：
                 *  1. 根据code是否等于200判断是否响应成功
                 *  2.获取data字段的jsonArray，取出每一个管控要点的id(即本地数据库中的server_id)
                 *  3.去查找本地数据库中的check_options中是否有相同的server_id
                 *    3.1 无则保存到本地数据库
                 *    3.2 有则继续往下走
                 *  4.取出options_data字段的Json数组，该字段的数据就是我们需要的ruler_check_data表格的数据，即对应管控要点的测量数据
                 *  5.取出每一个data的id(即本地数据库中的server_id)
                 *  6.去本地数据库中的check_options_data中查找是否有相同的server_id
                 *    6.1 无则保存到本地数据库
                 *    6.2 有则继续往下走
                 *  7.全部处理完成后，使用EventBus通知Activity已经处理完成
                 */
                JSONObject responeJson = null;
                try {
                    responeJson = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int code = responeJson.optInt("code");
//                1. 根据code是否等于200判断是否响应成功
                if (code == 200) {
                    try {
//                        2.获取data字段的jsonArray，
                        JSONArray dataArray = new JSONArray(responeJson.optString("data"));
                        if (dataArray.length() > 0) {
                            LogUtils.show("requestQueryMeasureData---查看管控要点的个数："+dataArray.length());
                            for (int i=0;i<dataArray.length();i++) {
                                JSONObject optionJson = dataArray.getJSONObject(i);
                                //  取出每一个管控要点的id(即本地数据库中的server_id)
                                int server_id = optionJson.optInt(DataBaseParams.measure_id);
//                                3.去查找本地数据库中的check_options中是否有相同的server_id
                                String where = " where " + DataBaseParams.server_id + " = " + server_id;
                                LogUtils.show("requestQueryMeasureData---查看本地数据库搜索条件："+where);
                                List<RulerCheckOptions> optionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), check, where);
                                LogUtils.show("requestQueryMeasureData---查看本地数据库搜索出管控要点的结果："+optionsList.size());

//                                3.1 无则保存到本地数据库
                                if (optionsList.size() == 0) {
                                    RulerCheckOptions rulerCheckOptions = new RulerCheckOptions();
                                    rulerCheckOptions.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                    rulerCheckOptions.setServerId(server_id);
                                    rulerCheckOptions.setRulerCheck(check);
                                    rulerCheckOptions.setQualifiedRate(optionJson.optLong("percent_pass"));
                                    rulerCheckOptions.setQualifiedNum(optionJson.optInt("qualified_points"));
                                    rulerCheckOptions.setMeasuredNum(optionJson.optInt("measured_points"));
                                    rulerCheckOptions.setFloorHeight(optionJson.optString("floor_height"));
                                    rulerCheckOptions.setUpload_flag(1);
                                    BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                                    String optionWhere = " where " + DataBaseParams.server_id + " = " + new JSONObject(optionJson.optString("option_template")).optInt("id");
                                    List<RulerOptions> rulerOptionsList = bleDataDbHelper.queryOptionsAllDataFromSqlite(optionWhere);
                                    if (rulerOptionsList.size() > 0) {
                                        rulerCheckOptions.setRulerOptions(rulerOptionsList.get(0));
                                    }
                                    LogUtils.show("requestQueryMeasureData---查看即将保存的数据内容："+rulerCheckOptions.toString());
                                    bleDataDbHelper.close();
                                    OperateDbUtil.addMeasureOptionsDataToSqlite(getApplicationContext(), rulerCheckOptions);
                                }
//                                3.2 有则继续往下走

//                                4.取出options_data字段的Json数组，该字段的数据就是我们需要的ruler_check_data表格的数据，即对应管控要点的测量数据
                                JSONArray optionsDataArray = new JSONArray(optionJson.optString("options_data"));
                                if (optionsDataArray.length() > 0) {
                                    for (int j=0;j<optionsDataArray.length();j++) {
                                        JSONObject optionDataJson = optionsDataArray.getJSONObject(j);
//                                        5.取出每一个data的id(即本地数据库中的server_id)
                                        int data_server_id = optionDataJson.optInt("id");
                                        String dataWhere = " where " + DataBaseParams.server_id + " = " + data_server_id;
//                                        6.去本地数据库中的check_options_data中查找是否有相同的server_id
                                        List<RulerCheckOptionsData> dataList = OperateDbUtil.queryMeasureDataFromSqlite(getApplicationContext(), new RulerCheckOptions(), dataWhere);
//                                        6.1 无则保存到本地数据库
                                        if (dataList.size() == 0) {
                                            RulerCheckOptionsData rulerCheckOptionsData = new RulerCheckOptionsData();
                                            String optionWhere = " where " + DataBaseParams.server_id + " = " + server_id;
                                            List<RulerCheckOptions> rulerOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), check, where);
                                            if (rulerOptionsList.size() > 0) {
                                                rulerCheckOptionsData.setRulerCheckOptions(rulerOptionsList.get(0));
                                            } else {
                                                rulerCheckOptionsData.setRulerCheckOptions(new RulerCheckOptions());
                                            }
                                            rulerCheckOptionsData.setUpload_flag(1);
                                            rulerCheckOptionsData.setData(optionDataJson.optString("data"));
                                            rulerCheckOptionsData.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                            rulerCheckOptionsData.setServerId(optionDataJson.optInt("id"));
                                            OperateDbUtil.addRealMeasureDataToSqlite(getApplicationContext(), rulerCheckOptionsData);
                                        }
                                    }
                                }

                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
//                全部处理完成后 通知界面
                EventBus.getDefault().post(new MeasureDataMsgEvent(1));

            }

            @Override
            public void onFailure(Exception e) {
                LogUtils.show("onFailure---测量数据请求失败："+e.getMessage());
//                处理失败也要 通知界面
                EventBus.getDefault().post(new MeasureDataMsgEvent(0));

            }
        });
    }
    /**************************获取测量数据接口请求结束*********************************/


    /******************************* 获取测量记录表接口请求开始 **********************************/
    private void requestQueryMeasureRecord(int finish_flag,int currentPage,int pageSize) {
        User user = OperateDbUtil.getUser(getApplicationContext());
        int user_id = user.getUserID();
        String wid = user.getWid();
        if (wid == null || wid.equals("null")) {
            wid = "0";
        }
//        链接参数为：   ?currentPage=1&pageSize=50&user_id=1&wid=0&status=
        StringBuffer sUrl = new StringBuffer();
        sUrl.append(NetConstant.baseUrl);
        sUrl.append(NetConstant.get_measure_record_url);
        sUrl.append("?");
        sUrl.append(NetConstant.current_Page);
        sUrl.append("=");
        sUrl.append(currentPage);
        sUrl.append("&");
        sUrl.append(NetConstant.page_size);
        sUrl.append("=");
        sUrl.append(pageSize);
        sUrl.append("&");
        sUrl.append(NetConstant.user_id);
        sUrl.append("=");
        sUrl.append(user_id);
        sUrl.append("&");
        sUrl.append(NetConstant.wid);
        sUrl.append("=");
        sUrl.append(wid);
        sUrl.append("&");
        sUrl.append(NetConstant.record_status);
        sUrl.append("=");
        sUrl.append(finish_flag);
        LogUtils.show("requestQueryMeasureRecord---查询记录表的请求链接：" + sUrl.toString());
        OkHttpUtils.get(sUrl.toString(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("requestQueryMeasureRecord----c查看响应："+response);
                /**
                 * {
                 "status":"success",
                 "code":200,
                 "msg":"查询成功",
                 "total":98,
                 "currentPage":1,
                 "pageSize":3
                 "data":[
                 {
                 "id":120,
                 "project_name":"那裤子",
                 "check_floor":"www",
                 "engin_id":1,
                 "user_id":471,
                 "wid":0,
                 "local_id":26,
                 "status":0,
                 "create_date":"2018-12-04",
                 "create_time":"2018-12-04 17:09:25",
                 "update_time":"",
                 "engin_text":"混凝土工程"
                 }]}
                 */
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    if (code == 200) {
                        JSONArray dataArray = new JSONArray(object.optString("data"));
                        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
                        List<RulerCheck> checkList = new ArrayList<>();
                        if (dataArray.length() > 0) {
                            for (int i=0;i<dataArray.length();i++) {
                                JSONObject dataJson = dataArray.getJSONObject(i);
//                                取出ruler_check的server_id
                                int server_id = dataJson.getInt(DataBaseParams.measure_id);
                                int status = dataJson.getInt("status");
//                                根据server_id在本地数据库中查找
                                String where = " where " + DataBaseParams.server_id + " = " + server_id;
                                List<RulerCheck> rulerCheckList = dataDbHelper.queryRulerCheckTableDataFromSqlite(where);
                                /**
                                 * 如果本条数据已经存有一条，则查看服务器端和本地端的完成标志是否一样
                                  */
                                if (rulerCheckList.size() > 0) {
                                    LogUtils.show("server_id为："+server_id+",状态标志为："+status+"的数据已经存在");
                                    int finish_flag = rulerCheckList.get(0).getStatus();
//                                    我们只处理一种情况，如果服务器的标志为0，而本地的标志为1，则补服务器的更新状态标志
                                    if (status == 0 && finish_flag > 0) {
                                        requestStopMeasure(server_id);
                                    } else {
//                                        如果都是一样的标志，则添加到备用的集合中
                                        checkList.add(rulerCheckList.get(0));
                                    }
                                } else {
                                    /**
                                     * 如果本条数据，本地没有存储，则添加到本地数据库中
                                     */
                                    RulerCheck check = new RulerCheck();
                                    check.setProjectName(dataJson.optString("project_name"));
                                    check.setCheckFloor(dataJson.optString("check_floor"));
                                    check.setServerId(server_id);
                                    if (status == 0) {
                                        check.setStatus(0);
                                    } else {
                                        check.setStatus(2);
                                    }
                                    check.setCreateTime(dataJson.getInt("create_time"));
                                    check.setCreateDate(dataJson.optString("create_date"));
                                    check.setUpdateTime(dataJson.optInt("update_time"));
                                    check.setUpload_flag(1);
                                    int engin_id = dataJson.optInt("engin_id");
                                    LogUtils.show("server_id为："+server_id+",状态标志为："+status+"的数据不存在，现在开始保存");
                                    String engin_where = " where " + DataBaseParams.server_id + "=" + engin_id;
                                    List<RulerEngineer> engineerList = dataDbHelper.queryEnginDataFromSqlite(engin_where);
                                    if (engineerList.size() > 0) {
                                        check.setEngineer(engineerList.get(0));
                                    }
                                    check.setUser(OperateDbUtil.getUser(getApplicationContext()));
                                    int check_local_id = OperateDbUtil.addMeasureDataToSqlite(getApplicationContext(), check);
                                    if (check_local_id > 0) {
                                        check.setId(check_local_id);
                                        LogUtils.show("服务器的check，保存本地成功："+check);
                                    }
                                    checkList.add(check);
                                }
                            }


                        }
                        MeasureRecordMsgEvent event = new MeasureRecordMsgEvent();
                        event.setCheckList(checkList);
                        event.setCurrentPage(object.optInt("currentPage"));
                        event.setPageSize(object.optInt("pageSize"));
                        event.setTotal(object.optInt("total"));
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Exception e) {
                LogUtils.show("记录表请求失败-----"+e.getMessage());
                MeasureRecordMsgEvent event = new MeasureRecordMsgEvent();
                List<RulerCheck> checkList = new ArrayList<>();
                event.setCheckList(checkList);
                event.setCurrentPage(0);
                event.setPageSize(0);
                event.setTotal(0);
                EventBus.getDefault().post(event);
            }
        });

    }
    /******************************* 获取测量记录表接口请求结束 **********************************/



    /********************************结束/停止测量接口开始*****************************************/
    private void requestStopMeasure(final int check_server_id) {
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.stop_measure_check_id, String.valueOf(check_server_id));
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        String url = NetConstant.baseUrl + NetConstant.stop_measure_url;
        LogUtils.show("结束测量接口链接："+url+",参数："+param);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("requestStopMeasure----查看server_id为" + check_server_id + ",的结束测量的响应：" + response);
                EventBus.getDefault().post(new StopMeasureMsgEvent(1));
                /**
                 * 服务器状态更新成功后，将status值修改为2
                 */
                savaCheckStatus(check_server_id);
            }

            @Override
            public void onFailure(Exception e) {
                LogUtils.show("requestStopMeasure----请求失败了");
                EventBus.getDefault().post(new StopMeasureMsgEvent(0));
            }
        },paramList);
    }

    private void savaCheckStatus(int check_server_id) {
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.measure_is_finish, 2);
        String where = DataBaseParams.server_id+" = ?";
        String[] whereValues = new String[]{String.valueOf(check_server_id)};
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
        int result = dataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, values, where, whereValues);
        LogUtils.show("完成测量，更新数据是否成功："+check_server_id+",更新状态："+result);
        dataDbHelper.close();
    }

    /********************************结束/停止测量接口结束*****************************************/

    /**********************************************更新测量数据接口开始***************************************************/
    private void initUpdateMeasureDataJson(List<RulerCheckOptions> checkOptionsList, List<RulerCheckOptionsData> checkOptionsDataList) {
        LogUtils.show("initUpdateMeasureDataJson----查看收到的checkOptionsList："+checkOptionsList);
        LogUtils.show("initUpdateMeasureDataJson----查看收到的checkOptionsDataList："+checkOptionsDataList);
        boolean isFinish = false;
        try {
            if (checkOptionsList.size() > 0) {
                JSONArray rootJsArray = new JSONArray();
                for (int n=0;n<checkOptionsList.size();n++) {
                    RulerCheckOptions rulerCheckOptions = checkOptionsList.get(n);
                    /**
                     * 如果rulercheck有server_id则说明之前网络状况良好，按照有网络的格式转化为以下格式的Json数据
                     *   {
                     "id": 24,
                     "floor_height": 2,
                     "measured_points": 50,
                     "qualified_points": 40,
                     "percent_pass": 80,
                     "update_time": 1542246440,
                     "data": [
                     {
                     "check_options_id": 25,
                     "data": 4,
                     "create_time": 1542245467,
                     "local_id": 3
                     },
                     {
                     "check_options_id": 25,
                     "data": 5,
                     "create_time": 1542245488,
                     "local_id": 4
                     }
                     ]
                     }
                     */

                    /**************************有网络的请求格式**********************************/
                    if (rulerCheckOptions.getRulerCheck().getServerId() > 0 && rulerCheckOptions.getServerId() > 0) {
//                        for (int j=0;j<checkOptionsList.size();j++) {
                            JSONObject dataJson = new JSONObject();
                            RulerCheckOptions checkOptions = rulerCheckOptions;
                            dataJson.put(DataBaseParams.measure_id, checkOptions.getServerId());
                            dataJson.put(DataBaseParams.measure_option_floor_height, checkOptions.getFloorHeight());
                            dataJson.put(DataBaseParams.measure_option_measured_points, checkOptions.getMeasuredNum());
                            dataJson.put(DataBaseParams.measure_option_qualified_points, checkOptions.getQualifiedNum());
                            dataJson.put(DataBaseParams.measure_option_percent_pass, checkOptions.getQualifiedRate());
                            dataJson.put(DataBaseParams.measure_update_time, checkOptions.getUpdateTime());
                            JSONArray dataArray = new JSONArray();
                            List<RulerCheckOptionsData> hasAddDataList = new ArrayList<>();
                            for (RulerCheckOptionsData rulerCheckOptionData : checkOptionsDataList) {
                                if (rulerCheckOptionData.getRulerCheckOptions().getId() == checkOptions.getId()) {
                                    LogUtils.show("initUpdateMeasureDataJson----查看收到的rulerCheckOptionData：" + rulerCheckOptionData);
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(DataBaseParams.options_data_check_options_id, rulerCheckOptionData.getRulerCheckOptions().getServerId());
                                    jsonObject.put(DataBaseParams.options_data_content, Float.valueOf(rulerCheckOptionData.getData()));
                                    jsonObject.put(DataBaseParams.options_data_create_time, rulerCheckOptionData.getCreateTime());
                                    jsonObject.put(DataBaseParams.local_id, rulerCheckOptionData.getId());
                                    dataArray.put(jsonObject);
                                    hasAddDataList.add(rulerCheckOptionData);
                                }
                            }
                            checkOptionsDataList.removeAll(hasAddDataList);
                            dataJson.put(DataBaseParams.options_data_content, dataArray);
                            rootJsArray.put(dataJson);

//                        }

                    } else {
                        /*****************************之前无网络的请求格式***********************************/
                        JSONObject seJson = new JSONObject();
                        seJson.put(DataBaseParams.local_id, rulerCheckOptions.getRulerCheck().getId());
                        seJson.put(DataBaseParams.measure_project_name, rulerCheckOptions.getRulerCheck().getProjectName());
                        seJson.put(DataBaseParams.measure_engin_id, rulerCheckOptions.getRulerCheck().getEngineer().getServerID());
                        seJson.put(DataBaseParams.user_user_id, rulerCheckOptions.getRulerCheck().getUser().getUserID());
                        seJson.put(DataBaseParams.user_wid, rulerCheckOptions.getRulerCheck().getUser().getWid());
                        seJson.put(DataBaseParams.measure_create_date, rulerCheckOptions.getRulerCheck().getCreateDate());
                        seJson.put(DataBaseParams.measure_create_time, rulerCheckOptions.getRulerCheck().getCreateTime());
                        seJson.put(DataBaseParams.measure_update_time, rulerCheckOptions.getRulerCheck().getUpdateTime());
                        int is_finish = rulerCheckOptions.getRulerCheck().getStatus();
                        if (is_finish > 0) {
                            seJson.put(DataBaseParams.user_status, 1);
                            isFinish = true;
                        } else {
                            seJson.put(DataBaseParams.user_status, 0);
                        }

//                    options字段的JSON数组，初始化完要加入seJson
                        JSONArray optionsArray = new JSONArray();
                        for (RulerCheckOptions checkOptions : checkOptionsList) {
                            JSONObject dataJson = new JSONObject();
                            dataJson.put(DataBaseParams.local_id, checkOptions.getId());
                            dataJson.put(DataBaseParams.measure_option_options_id, checkOptions.getRulerOptions().getServerID());
                            dataJson.put(DataBaseParams.measure_option_floor_height, checkOptions.getFloorHeight());
                            dataJson.put(DataBaseParams.measure_option_measured_points, checkOptions.getMeasuredNum());
                            dataJson.put(DataBaseParams.measure_option_qualified_points, checkOptions.getQualifiedNum());
                            dataJson.put(DataBaseParams.measure_option_percent_pass, checkOptions.getQualifiedRate());
                            dataJson.put(DataBaseParams.measure_update_time, checkOptions.getUpdateTime());
                            dataJson.put(DataBaseParams.measure_create_time, checkOptions.getCreateTime());
//                        data字段的JSON数组，初始化完，要加入dataJson
                            JSONArray optionDataArray = new JSONArray();
                            for (RulerCheckOptionsData data : checkOptionsDataList) {
                                if (data.getRulerCheckOptions().getId() == checkOptions.getId()) {
                                    JSONObject object = new JSONObject();
                                    object.put(DataBaseParams.user_data, data.getData());
                                    object.put(DataBaseParams.local_id, data.getId());
                                    optionDataArray.put(object);
                                }
                            }
                            dataJson.put(DataBaseParams.user_data, optionDataArray);
                            optionsArray.put(dataJson);
                            seJson.put("options", optionsArray);
                        }
                        rootJsArray.put(seJson);
                    }


                }
                LogUtils.show("initUpdateMeasureDataJson----查看更新测量数据请求的JSON：" + rootJsArray.toString());
                requestUpdateMeasureData(rootJsArray.toString(),checkOptionsList.get(0).getRulerOptions().getType(),isFinish);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void requestUpdateMeasureData(final String jsonData, final int flag,final boolean isFinish) {
        OkHttpUtils.Param param = new OkHttpUtils.Param(DataBaseParams.options_data_content, jsonData);
        List<OkHttpUtils.Param> dataList = new ArrayList<>();
        dataList.add(param);
        String url = NetConstant.baseUrl + NetConstant.update_data_url;
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("requestUpdateMeasureData---查看新测量数据服务器返回的数据内容："+response);
                /**
                 * 之前有网和没网返回的数据内容不一样，
                 *    无网 一级data下面有options数组，且没有data数组
                 *    有网 一级data下面有个二级data数组，且没有options数组
                 */
                try {
                    JSONObject rootJson = new JSONObject(response);
                    int code = rootJson.optInt("code");
                    JSONArray dataRootArray = new JSONArray(rootJson.optString("data"));
                    if (dataRootArray.length() > 0) {
                        for (int i=0;i<dataRootArray.length();i++) {
                            JSONObject dataJson = dataRootArray.getJSONObject(i);

                            /**
                             * 处理有网络情况下数据
                             */
                            if (dataJson.has("data") && !dataJson.has("options")) {
                                LogUtils.show("requestUpdateMeasureData----进入有网络模式");
                                JSONArray optionDatas = new JSONArray(dataJson.optString("data"));
                                if (optionDatas.length() > 0) {
                                    /**之前有网络的情况下，只需要把check_options_data里面的server_id更新到数据库即可***/
                                    for (int j=0;j<optionDatas.length();j++) {
                                        int local_id = optionDatas.getJSONObject(j).optInt(DataBaseParams.local_id);
                                        int server_id = optionDatas.getJSONObject(j).optInt(DataBaseParams.measure_id);
//                                        获取到两个数据后，开始更新到数据库
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.server_id, server_id);
                                        values.put(DataBaseParams.upload_flag, 1);
                                        OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), values, new String[]{String.valueOf(local_id)});

                                    }
                                    EventBus.getDefault().post(new HandleDataResultMsgEvent(flag));
                                }
                            }
                            /**
                             * 处理无网络情况下的数据
                             */
                            if (dataJson.has("options") && dataJson.has(DataBaseParams.local_id)) {
                                LogUtils.show("requestUpdateMeasureData----进入无网络模式");
                                JSONArray optionsArray = new JSONArray(dataJson.optString("options"));
                                /**
                                 * 没网时，之前的ruler_check表、ruler_check_options表都没有在服务器创建一条数据。
                                 * 所以现在一次性提交就会一次行返回这几个表的数据内容，包括ruler_check_options_data也会一起创建
                                 * 依次将这三个表的server_id和upload_flag更新到数据库
                                 */
//                                1.获取ruler_check里的本地id和server_id
                                int ruler_check_local_id = dataJson.getInt(DataBaseParams.local_id);
                                int ruler_check_server_id = dataJson.getInt(DataBaseParams.measure_id);

                                ContentValues rulerCheckValues = new ContentValues();
                                rulerCheckValues.put(DataBaseParams.server_id, ruler_check_server_id);
                                rulerCheckValues.put(DataBaseParams.upload_flag, 1);
//                                1.1将server_id和upload_flag更新到数据库
                                BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                                bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, rulerCheckValues, " id =? ", new String[]{String.valueOf(ruler_check_local_id)});
                                //   如果发过来的请求是已经测量完成的，则更新测量状态为2
                                if (isFinish) {
                                    savaCheckStatus(ruler_check_server_id);
                                }
                                if (optionsArray.length() > 0) {
                                    for (int j = 0; j < optionsArray.length(); j++) {
//                                        2.获取ruler_check_options的本地id和server_id
                                        JSONObject object = optionsArray.getJSONObject(j);
                                        int check_option_local_id = object.optInt(DataBaseParams.local_id);
                                        int check_option_server_id = object.optInt(DataBaseParams.measure_id);
                                        ContentValues checkOptionsValues = new ContentValues();
                                        checkOptionsValues.put(DataBaseParams.server_id, check_option_server_id);
                                        checkOptionsValues.put(DataBaseParams.upload_flag, 1);
//                                        2.1将server_id和upload_flag更新到ruler_check_options表格
                                        bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_option_table_name, checkOptionsValues, " id=? ", new String[]{String.valueOf(check_option_local_id)});
                                        JSONArray optionDataArray = new JSONArray(object.optString("data"));
                                        if (optionDataArray.length() > 0) {
                                            LogUtils.show("查看optionDataArray的总数："+optionDataArray.length()+",内容");
                                            for (int a = 0; a < optionDataArray.length(); a++) {
//                                                3.获取ruler_check_options_data里的本地id和server_id
                                                JSONObject jsonObject = optionDataArray.getJSONObject(a);
                                                LogUtils.show("查看该条返回的data：" + jsonObject);
                                                int check_options_data_local_id = jsonObject.optInt(DataBaseParams.local_id);
                                                int check_options_data_server_id = jsonObject.optInt(DataBaseParams.measure_id);
//                                                3.1 封装更新的数据内容
                                                ContentValues checkOptionsDataValues = new ContentValues();
                                                checkOptionsDataValues.put(DataBaseParams.server_id, check_options_data_server_id);
                                                checkOptionsDataValues.put(DataBaseParams.upload_flag, 1);
//                                                3.2 将server_id和upload_flag更新到ruler_check_options_data表格
                                                bleDataDbHelper.updateDataToSqlite(DataBaseParams.options_data_table_name, checkOptionsDataValues, " id=?", new String[]{String.valueOf(check_options_data_local_id)});
                                            }
                                        }

                                    }
                                    EventBus.getDefault().post(new HandleDataResultMsgEvent(flag));
                                }
                            }

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        },dataList);

    }
    /**********************************************更新测量数据接口结束***************************************************/








    /************************************************创建记录表接口开始****************************************************************/

    /**
     * 初始化请求“创建记录表”接口所需要的json数据
     * @param rulerCheck
     * @param checkOptionsList
     */
    private void initCreateRecordJsonData(RulerCheck rulerCheck, List<RulerCheckOptions> checkOptionsList) {
        /**
         * 发送的模板：
         * {
         "local_id": 1,
         "project_name": "vitec",
         "check_floor": "floor",
         "engin_id": 1,
         "user_id": 1,
         "wid": 0,
         "create_date": 1542816000,
         "create_time": 1542870680,
         "update_time": 0,
         "check_options": [
         {
         "local_id": 1,
         "options_id": 1,
         "floor_height": 1,
         "measured_points": 30,
         "qualified_points": 20,
         "percent_pass": 80,
         "create_time": 1542870680,
         "update_time": 0
         },
         {
         "local_id": 2,
         "options_id": 2,
         "floor_height": 1,
         "measured_points": 30,
         "qualified_points": 20,
         "percent_pass": 80,
         "create_time": 1542870680,
         "update_time": 0
         }
         ]
         }
         */
        try {
            LogUtils.show("initJsonData---查看上传服务器之前RulerCHeck数据：" + rulerCheck.toString());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(DataBaseParams.local_id, rulerCheck.getId());
            jsonObject.put(DataBaseParams.measure_project_name, rulerCheck.getProjectName());
            jsonObject.put(DataBaseParams.measure_check_floor, rulerCheck.getCheckFloor());
            jsonObject.put(DataBaseParams.options_engin_id, rulerCheck.getEngineer().getServerID());
            jsonObject.put(DataBaseParams.user_user_id, rulerCheck.getUser().getUserID());
            String wid = rulerCheck.getUser().getWid();
            if (wid == null || wid.equals("null") || wid.equals("") || wid.equals("0")) {

                jsonObject.put(DataBaseParams.user_wid, 0);
            } else {
                jsonObject.put(DataBaseParams.user_wid, wid);
            }


            long create_time = DateFormatUtil.transForMilliSecond(new Date());
            String date = DateFormatUtil.getDate("yyyy-MM-dd");
            LogUtils.show("initJsonData---查看日期：" + date + ",查看时间戳：" + create_time);
            int create_date = DateFormatUtil.transForMilliSecondByTim(date, "yyyy-MM-dd");
            LogUtils.show("initJsonData---查看日期时间戳：" + create_date);

            jsonObject.put(DataBaseParams.measure_create_date, create_date);
            jsonObject.put(DataBaseParams.measure_create_time, create_time);
            jsonObject.put(DataBaseParams.enginer_update_time, 0);
            JSONArray jsonArray = new JSONArray();
            LogUtils.show("initJsonData---查看管控要点：" + checkOptionsList.toString());
            for (int i = 0; i < checkOptionsList.size(); i++) {
                JSONObject optionJson = new JSONObject();
                optionJson.put(DataBaseParams.local_id, checkOptionsList.get(i).getId());
                optionJson.put(DataBaseParams.measure_option_options_id, checkOptionsList.get(i).getRulerOptions().getServerID());
                optionJson.put(DataBaseParams.measure_option_measured_points, checkOptionsList.get(i).getMeasuredNum());
                optionJson.put(DataBaseParams.measure_option_qualified_points, checkOptionsList.get(i).getQualifiedNum());
                optionJson.put(DataBaseParams.measure_option_percent_pass, checkOptionsList.get(i).getQualifiedRate());
                optionJson.put(DataBaseParams.measure_create_time, create_time);
                optionJson.put(DataBaseParams.measure_update_time, 0);
                jsonArray.put(optionJson);
            }
            jsonObject.put(DataBaseParams.check_options, jsonArray);
            LogUtils.show("initJsonData---查看最终的Json：" + jsonObject.toString());
            requestCreateRecord(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestCreateRecord(final String data) {
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.create_record_data,data);
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        String url = NetConstant.baseUrl + NetConstant.create_record_url;
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("init 创建记录表----查看返回的数据："+response);
                /**
                 * 主要是获取服务器返回的两个ID更新到本地数据库
                 * 返回的数据样本（省略了一些用不到的）：
                 * {
                   "status": "success",
                   "code": 200,
                   "data": {
                   "id": 14,
                   "local_id": 1,
                   "checkOptions": [
                      {
                      "id": 24,
                      "local_id": 1,
                      },
                   "options_data": []
                    ]
                   },
                   "msg": "创建记录表成功"
                   }
                 */
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    /**
                     * 200-请求成功
                     */
                    if (code == 200) {
                        JSONObject dataJson = new JSONObject(jsonObject.optString("data"));
//                        ruler_check表格的server_id
                        int check_server_id = dataJson.getInt("id");
//                        ruler_check表格的本地id
                        int check_local_id = dataJson.optInt(DataBaseParams.local_id);
                        JSONArray checkOptionsArray = new JSONArray(dataJson.optString(DataBaseParams.check_options));
                        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                        //                        讲server_id更新到ruler_check表格中
                        ContentValues checkValues = new ContentValues();
                        checkValues.put(DataBaseParams.server_id, check_server_id);
                        checkValues.put(DataBaseParams.upload_flag, 1);
                        int r=bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, checkValues, "id =?",new String[]{String.valueOf(check_local_id)});
                        LogUtils.show("requestCreateRecord---查看ruler_check更新状态：" + r);
                        LogUtils.show("requestCreateRecord---查看服务器返回的管控要点个数:"+checkOptionsArray.length());
                        int result = 0;
                        for (int i = 0; i < checkOptionsArray.length(); i++) {
                            JSONObject checkOptionJson = checkOptionsArray.getJSONObject(i);
//                            ruler_check_options表格的server_id
                            int check_options_server_id = checkOptionJson.optInt("id");
//                            ruler_check_options表格的本地id
                            int check_options_local_id = checkOptionJson.optInt("local_id");

//                        更新ruler_check_options表格中的server_id
                            ContentValues optionsValues = new ContentValues();
                            optionsValues.put(DataBaseParams.server_id, check_options_server_id);
                            optionsValues.put(DataBaseParams.upload_flag, 1);
                             result = bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_option_table_name, optionsValues, "id=?", new String[]{String.valueOf(check_options_local_id)});

                            LogUtils.show("requestCreateRecord---查看服务id："+check_options_server_id+",本地id："+check_options_local_id+",ruler_check_options更新状态：" + result);
                        }
                        if (result > 0) {
                            EventBus.getDefault().post("1");
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        },paramList);

    }

    /************************************************创建记录表接口结束****************************************************************/


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("PerformMeasureNetIntentService---销毁了");
    }
}
