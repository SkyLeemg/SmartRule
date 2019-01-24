package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.bean.NetCallBackMessage;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.db.OperateDbUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetMudelIntentService extends IntentService {

    private List<RulerOptions> optionsList;
    private List<RulerEngineer> engineerList;
    private BleDataDbHelper dataDbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        optionsList = new ArrayList<>();
        engineerList = new ArrayList<>();
        dataDbHelper = new BleDataDbHelper(getApplicationContext());
    }

    public GetMudelIntentService(String name) {
        super(name);
    }

    public GetMudelIntentService() {
        super("GetMudelIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//        engineerList = dataDbHelper.queryEnginDataFromSqlite("");
//        if (engineerList.size() == 0) {
            String url = NetConstant.baseUrl + NetConstant.getEngineerInfoUrl;
            OkHttpUtils.get(url, new OkHttpUtils.ResultCallback<String>() {
                @Override
                public void onSuccess(String response) {
                    LogUtils.show("查看服务器返回的工程模板Json："+response);
                    /**
                     * {
                     "status":"success",
                     "code":200,
                     "data":[
                     {
                     "id":1,
                     "name":"混凝土工程",
                     "description":null,
                     "options_choose":"1,2,10",
                     "create_time":"2018-10-31 14:33:28",
                     "update_time":"2018-11-30 09:58:51"
                     }
                     ],
                     "msg":"查询成功"}

                     */
                    if (!response.equals("")) {
                        try {
                            JSONObject optionJsons = new JSONObject(response);
                            int code = optionJsons.optInt("code");
                            if (code == 200) {
                                String dataJson = optionJsons.optString("data");
                                JSONArray jsonArray = new JSONArray(dataJson);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int server_id = object.optInt("id");
                                    String where = " where " + DataBaseParams.server_id + "=" + server_id;
                                    BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                                    List<RulerEngineer> enginList = bleDataDbHelper.queryEnginDataFromSqlite(where);
                                    if (enginList.size() == 0) {
//                        将服务器中engineers表里面的数据存到rulerEngineer对象
                                        RulerEngineer engineer = new RulerEngineer();
                                        engineer.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                        engineer.setEngineerName(object.optString("name"));
                                        engineer.setServerID(Integer.parseInt(object.optString("id")));
                                        engineer.setEngineerDescription(object.optString("description"));
                                        engineer.setChooseOptions(object.optString("options_choose"));
                                        engineer.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                        engineerList.add(engineer);
//                                        LogUtils.show("netBussCallBack: 从服务器下载下来保存成为对象后的数据："+engineer.toString() );
                                    }

                                }
                                //                    将数据保存到数据库

                                OperateDbUtil.addEngineerMudelData(getApplicationContext(),engineerList);
                                NetCallBackMessage netCallBackMessage = new NetCallBackMessage("", 1);
                                EventBus.getDefault().post(netCallBackMessage);
                            }

                            /**
                             * 根据engineer的server_id去获取对应的管控要点
                             */
//                            for (int i = 0; i < engineerList.size(); i++) {
//                                String url = NetConstant.getOptionInfoUrl ;
//
//                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(Exception e) {
                    LogUtils.show("onFailure: 工程模板获取失败");
                }
            });
//        }

//        List<RulerOptions> optionsList = dataDbHelper.queryOptionsAllDataFromSqlite("");
//        if (optionsList.size() == 0) {
            String optionUrl =NetConstant.baseUrl+ NetConstant.getOptionInfoUrl ;
//            LogUtils.show("查看管控要点请求连接：" + optionUrl);
            OkHttpUtils.get(optionUrl,optionsResultCallback);
//        }
    }

    OkHttpUtils.ResultCallback<String> optionsResultCallback = new OkHttpUtils.ResultCallback<String>() {
        @Override
        public void onSuccess(String response) {
//            LogUtils.show("查看服务器返回的管控要点模板信息："+response);
            if (!response.equals("")) {
                try {
                    JSONObject optionJsons = new JSONObject(response);
                    String dataJson = optionJsons.optString("data");
                    int code = optionJsons.optInt("code");
                    if (code == 200) {
                        JSONArray jsonArray = new JSONArray(dataJson);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            int server_id = object.optInt("id");
                            String where = " where " + DataBaseParams.server_id + "=" + server_id;
                            List<RulerOptions> rulerOptionsList = dataDbHelper.queryOptionsAllDataFromSqlite(where);
                            if (rulerOptionsList.size() == 0) {
                                RulerOptions option = new RulerOptions();
                                option.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                option.setServerID(Integer.parseInt(object.optString("id")));
                                option.setMethods(object.optString("methods"));
                                option.setStandard(object.optString("standard"));
                                option.setOptionsName(object.optString("name"));
                                option.setMeasure(object.optString(DataBaseParams.options_measure));
                                option.setType(object.optInt("type"));
//                            LogUtils.show("获取管控要点模板接口--------: 服务器下载下来的管控要点："+option);
                                optionsList.add(option);
                            }

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (optionsList.size() > 0) {
                //            将获取到的数据保存到本地数据库
                OperateDbUtil.addOptionsMudelData(getApplicationContext(), optionsList);
                NetCallBackMessage netCallBackMessage = new NetCallBackMessage("", 2);
                EventBus.getDefault().post(netCallBackMessage);
            }


        }

        @Override
        public void onFailure(Exception e) {
            LogUtils.show("onFailure: 管控要点获取失败");
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
//        LogUtils.show("GetMudelIntentService--onDestroy--模板获取完成");
//        LogUtils.show("GetMudelIntentService--onDestroy--查看所有的工程模板大小："+engineerList.size()+",内容:"+engineerList);
//        LogUtils.show("GetMudelIntentService--onDestroy--查看所有的管控要点大小："+optionsList.size()+",内容："+optionsList);
    }
}
