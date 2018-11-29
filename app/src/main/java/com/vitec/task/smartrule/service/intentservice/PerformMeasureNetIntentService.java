package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
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

    public static final int FLAG_CREATE_RECORD = 1;//创建记录表的标志
    public static final int FLAG_UPDATE_RECORD = 2;//修改记录表的标志
    public static final int FLAG_GET_MEASURE_DATA = 3;//获取测量数据的标志
    public static final int FLAG_UPDATE_DATA = 4;//更新测量数据的标志

    public static final String GET_FLAG_KEY = "flag";
    public static final String GET_DATA_KEY = "data";
    public static final String GET_CREATE_RULER_DATA_KEY = "ruler_check";
    public static final String GET_CREATE_OPTIONS_DATA_KEY = "ruler_options_list";
    public static final String GET_UPDATE_DATA_KEY = "ruler_options_data_list";




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

                break;

            /**
             * 更新测量数据的标志
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
        }
    }

    /**********************************************更新测量数据接口开始***************************************************/
    private void initUpdateMeasureDataJson(List<RulerCheckOptions> checkOptionsList, List<RulerCheckOptionsData> checkOptionsDataList) {
        LogUtils.show("initUpdateMeasureDataJson----查看收到的checkOptionsList："+checkOptionsList);
        LogUtils.show("initUpdateMeasureDataJson----查看收到的checkOptionsDataList："+checkOptionsDataList);
        try {
            if (checkOptionsList.size() > 0) {
                JSONArray rootJsArray = new JSONArray();

                RulerCheckOptions rulerCheckOptions = checkOptionsList.get(0);
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
                if (rulerCheckOptions.getRulerCheck().getServerId() > 0 && rulerCheckOptions.getServerId() > 0) {
                    for (int j=0;j<checkOptionsList.size();j++) {
                        JSONObject dataJson = new JSONObject();
                        dataJson.put(DataBaseParams.measure_id, rulerCheckOptions.getServerId());
                        dataJson.put(DataBaseParams.measure_option_floor_height, rulerCheckOptions.getFloorHeight());
                        dataJson.put(DataBaseParams.measure_option_measured_points, rulerCheckOptions.getMeasuredNum());
                        dataJson.put(DataBaseParams.measure_option_qualified_points, rulerCheckOptions.getQualifiedNum());
                        dataJson.put(DataBaseParams.measure_option_percent_pass, rulerCheckOptions.getQualifiedRate());
                        dataJson.put(DataBaseParams.measure_update_time, rulerCheckOptions.getUpdateTime());
                        JSONArray dataArray = new JSONArray();
                        for (RulerCheckOptionsData rulerCheckOptionData : checkOptionsDataList) {
                            if (rulerCheckOptionData.getRulerCheckOptions().getId() == rulerCheckOptions.getId()) {
                                LogUtils.show("initUpdateMeasureDataJson----查看收到的rulerCheckOptionData：" + rulerCheckOptionData);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(DataBaseParams.options_data_check_options_id, rulerCheckOptionData.getRulerCheckOptions().getServerId());
                                jsonObject.put(DataBaseParams.options_data_content, Float.valueOf(rulerCheckOptionData.getData()));
                                jsonObject.put(DataBaseParams.options_data_create_time, rulerCheckOptionData.getCreateTime());
                                jsonObject.put(DataBaseParams.local_id, rulerCheckOptionData.getId());
                                dataArray.put(jsonObject);
                            }
                        }
                        dataJson.put(DataBaseParams.options_data_content, dataArray);
                        rootJsArray.put(dataJson);

                    }

                } else {
                    JSONObject seJson = new JSONObject();
                    seJson.put(DataBaseParams.local_id, rulerCheckOptions.getRulerCheck().getId());
                    seJson.put(DataBaseParams.measure_project_name, rulerCheckOptions.getRulerCheck().getProjectName());
                    seJson.put(DataBaseParams.measure_engin_id, rulerCheckOptions.getRulerCheck().getEngineer().getServerID());
                    seJson.put(DataBaseParams.user_user_id, rulerCheckOptions.getRulerCheck().getUser().getUserID());
                    seJson.put(DataBaseParams.user_wid, rulerCheckOptions.getRulerCheck().getUser().getWid());
                    seJson.put(DataBaseParams.measure_create_date, rulerCheckOptions.getRulerCheck().getCreateDate());
                    seJson.put(DataBaseParams.measure_create_time, rulerCheckOptions.getRulerCheck().getCreateTime());
                    seJson.put(DataBaseParams.measure_update_time, rulerCheckOptions.getRulerCheck().getUpdateTime());
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
                LogUtils.show("initUpdateMeasureDataJson----查看更新测量数据请求的JSON：" + rootJsArray.toString());
                requestUpdateMeasureData(rootJsArray.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void requestUpdateMeasureData(final String jsonData) {
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
