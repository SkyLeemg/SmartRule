package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrinterId;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.vitec.task.smartrule.bean.DownloadedImg;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
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
import com.vitec.task.smartrule.net.FileOkHttpUtils;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static final int FLAG_UPLOAD_OPTION_PIC = 8;//上传管控要点的图纸


    public static final String GET_DATA_KEY = "data";
    public static final String GET_CREATE_RULER_DATA_KEY = "ruler_check";
    public static final String GET_CREATE_OPTIONS_DATA_KEY = "ruler_options_list";
    public static final String GET_UPDATE_DATA_KEY = "ruler_options_data_list";
    public static final String GET_FINISH_MEASURE_KEY = "finish_measure";//获取停止测量接口的参数值
    public static final String VALUE_UPLOAD_PIC_CHECK_OPTIONS_LIST = "check_options_list";



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
                RulerCheck rulerCheck1 = (RulerCheck) intent.getSerializableExtra(GET_CREATE_RULER_DATA_KEY);
                updateRulerCheckRecord(rulerCheck1);
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

            /**
             * 上传图纸
             */
            case FLAG_UPLOAD_OPTION_PIC:
                Bundle bundle = intent.getBundleExtra(VALUE_UPLOAD_PIC_CHECK_OPTIONS_LIST);
                requestUploadOptionPic(bundle);
                break;
        }
    }

    /*********************修改记录表开始***************************/
    private void updateRulerCheckRecord(final RulerCheck rulerCheck1) {
        LogUtils.show("打印接受到的rucheck:"+rulerCheck1);
        OkHttpUtils.Param idParam = new OkHttpUtils.Param(DataBaseParams.measure_id,String.valueOf(rulerCheck1.getServerId()));
        final OkHttpUtils.Param checkFloor = new OkHttpUtils.Param(DataBaseParams.measure_check_floor, rulerCheck1.getCheckFloor());
//        OkHttpUtils.Param engin_idParam = new OkHttpUtils.Param(DataBaseParams.measure_engin_id, String.valueOf(rulerCheck1.getEngineer().getServerID()));
        OkHttpUtils.Param userParam = new OkHttpUtils.Param(DataBaseParams.user_user_id, String.valueOf(rulerCheck1.getUser().getUserID()));
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(idParam);
        paramList.add(checkFloor);
//        paramList.add(engin_idParam);
        paramList.add(userParam);
        if (rulerCheck1.getProject().getServer_id() > 0) {
            OkHttpUtils.Param projectParam = new OkHttpUtils.Param("project", String.valueOf(rulerCheck1.getProject().getServer_id()));
            paramList.add(projectParam);
        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(DataBaseParams.local_id, rulerCheck1.getProject().getId());
                jsonObject.put(DataBaseParams.check_project_name, rulerCheck1.getProject().getProjectName());
                jsonObject.put(DataBaseParams.user_user_id, rulerCheck1.getUser().getUserID());
                jsonObject.put(DataBaseParams.measure_create_time, rulerCheck1.getUpdateTime());
                jsonObject.put(DataBaseParams.measure_update_time, rulerCheck1.getUpdateTime());
                OkHttpUtils.Param projecP = new OkHttpUtils.Param("project", jsonObject.toString());
                paramList.add(projecP);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (rulerCheck1.getUnitEngineer().getServer_id() > 0) {
            OkHttpUtils.Param uP = new OkHttpUtils.Param("unit", String.valueOf(rulerCheck1.getUnitEngineer().getServer_id()));
            paramList.add(uP);
        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(DataBaseParams.local_id, rulerCheck1.getUnitEngineer().getId());
                jsonObject.put(DataBaseParams.unit_engineer_location, rulerCheck1.getUnitEngineer().getLocation());
                jsonObject.put(DataBaseParams.measure_create_time, rulerCheck1.getUpdateTime());
                jsonObject.put(DataBaseParams.measure_update_time, rulerCheck1.getUpdateTime());
                OkHttpUtils.Param projecP = new OkHttpUtils.Param("unit", jsonObject.toString());
                paramList.add(projecP);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String url = NetConstant.baseUrl + NetConstant.update_record_url;
        LogUtils.show("更新测量记录接口----查看连接：" + url + ",参数：" + paramList);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                /**
                 * {"status":"success","code":200,
                 * "data":{
                 *  "project":{"local_id":11,"project_name":"新项目","user_id":9,"create_time":1548137780,"update_time":1548137780,"id":"48"},
                 *  "unit":{"local_id":18,"location":"看咯","create_time":1548137780,"update_time":1548137780,"id":"63"}
                 *  },
                 *  "msg":"更新记录表成功"}
                 */
                try {
                    LogUtils.show("更新测量记录接口----打印查看返回的信息："+response);
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    if (code == 200) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DataBaseParams.upload_flag, 1);
                        String where = DataBaseParams.measure_id + "=?";
                        int resutl = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_table_name, where, contentValues, new String[]{String.valueOf(rulerCheck1.getId())});
                        LogUtils.show("更新测量记录接口---更新标志到数据库是否成功："+resutl);
                        String data = jsonObject.optString("data");
                        if (data.length() > 5 && data.contains("{") && data.contains("}")) {
                            JSONObject dataJson = new JSONObject(data);
                            /******更新项目名到数据库*******/
                            if (dataJson.has("project")) {
                                JSONObject projectJson = dataJson.getJSONObject("project");
                                int p_local_id = projectJson.getInt(DataBaseParams.local_id);
                                int server_id = projectJson.optInt(DataBaseParams.measure_id);
                                ContentValues values = new ContentValues();
                                values.put(DataBaseParams.server_id, server_id);
                                int r = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.check_project_table_name, where, values, new String[]{String.valueOf(p_local_id)});
                                LogUtils.show("更新测量记录接口---查看更新项目ID到数据库是否成功：" + r);

                                //更新成员表的数据
//                                User user = OperateDbUtil.getUser(getApplicationContext());
                                String mWhere = " where " + DataBaseParams.user_user_id + "=" + projectJson.optInt(DataBaseParams.user_user_id) + " and " + DataBaseParams.measure_project_id + "=" + p_local_id;
                                List<ProjectUser> projectUserList = OperateDbUtil.queryProjectUserFromSqlite(getApplicationContext(), mWhere);
                                if (projectUserList.size() > 0) {
                                    ContentValues contentValues1 = new ContentValues();
                                    contentValues1.put(DataBaseParams.project_server_id, server_id);
                                    int p_r = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.project_user_table_name, "id=?", contentValues1, new String[]{String.valueOf(projectUserList.get(0).getId())});
                                    LogUtils.show("更新测量记录接口---更新项目ID到成员表，查看是否更新成功：" + p_r);
                                }
                            }

                            /****更新单位工程到数据库*******/
                            if (dataJson.has("unit")) {
                                JSONObject unitJson = dataJson.getJSONObject("unit");
                                int u_local_id = unitJson.getInt(DataBaseParams.local_id);
                                int server_id = unitJson.optInt(DataBaseParams.measure_id);
                                ContentValues values = new ContentValues();
                                values.put(DataBaseParams.server_id, server_id);
                                int r = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.unit_engineer_table_name, where, values, new String[]{String.valueOf(u_local_id)});
                                LogUtils.show("更新测量记录接口---查看更新单位工程ID到数据库是否成功：" + r);
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
        }, paramList);
    }
    /*********************修改记录表结束***************************/


    /************************上传图纸接口开始**************************************/
    private void requestUploadOptionPic(Bundle bundle) {
        final String url = bundle.getString(NetConstant.upload_option_pic_url_key,"");
        final String ids = bundle.getString(NetConstant.upload_option_pic_check_options_list,"");
        String number_list = bundle.getString(NetConstant.upload_option_pic_number_list,"");
//        int createTime = bundle.getInt(DataBaseParams.options_create_time, DateFormatUtil.transForMilliSecond(new Date()));
        OkHttpUtils.Param urlParam = new OkHttpUtils.Param(NetConstant.upload_option_pic_url_key, url);
        OkHttpUtils.Param idsParam = new OkHttpUtils.Param(NetConstant.upload_option_pic_check_options_list, ids);
        OkHttpUtils.Param numberParam = new OkHttpUtils.Param(NetConstant.upload_option_pic_number_list, number_list);
//        OkHttpUtils.Param timeParam = new OkHttpUtils.Param(DataBaseParams.options_create_time, String.valueOf(createTime));
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(urlParam);
        paramList.add(idsParam);
        paramList.add(numberParam);
//        paramList.add(timeParam);
        final String requestUrl = NetConstant.baseUrl + NetConstant.upload_option_pic_url;
        LogUtils.show("requestUploadOptionPic----上传图纸接口，查看链接：" + requestUrl + "，参数信息："+paramList);
        OkHttpUtils.post(requestUrl, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("requestUploadOptionPic----上传图纸接口,查看接口返回数据："+response);
                /**
                 * {"status":"error","code":400,"msg":"测量管控要点列表规则错误"}
                 */
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    if (code == 200) {
                        ContentValues values = new ContentValues();
                        values.put(DataBaseParams.measure_option_img_upload_flag, 1);
                        values.put(DataBaseParams.measure_option_server_img_url, url);
                        String where = DataBaseParams.server_id + "=?";
                        if (ids.contains(",")) {
                            String[] options = ids.split(",");
                            for (int i = 0; i < options.length; i++) {
                                OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_option_table_name, where,values, new String[]{options[i]});
                            }
                        } else {
                            OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_option_table_name, where,values, new String[]{ids});
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }

            @Override
            public void onFailure(Exception e) {

            }
        }, paramList);

    }
    /************************上传图纸接口结束**************************************/


    /****************************TODO 删除记录表接口请求开始***********************************/
    private void requestDelRecord(final String check_list) {
//        User user = OperateDbUtil.getUser(getApplicationContext());
//        int user_id = user.getUserID();
//        OkHttpUtils.Param userIdParam = new OkHttpUtils.Param(DataBaseParams.user_user_id, String.valueOf(user_id));
        OkHttpUtils.Param checkListParam = new OkHttpUtils.Param(NetConstant.del_check_list, check_list);
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
//        paramList.add(userIdParam);
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




    /**************************todo 获取测量数据接口请求开始*********************************/
    private void requestQueryMeasureData(final RulerCheck check) {
//        User user = OperateDbUtil.getUser(getApplicationContext());
//        int user_id = user.getUserID();
//        String wid = user.getWid();
//        if (wid == null || wid.equals("null")) {
//            wid = "0";
//        }
//        链接参数为：   ?check_id=1
        StringBuffer sUrl = new StringBuffer();
        sUrl.append(NetConstant.baseUrl);
        sUrl.append(NetConstant.get_measure_data_url);
        sUrl.append("?");
        sUrl.append(NetConstant.get_measure_data_check_id);
        sUrl.append("=");
        sUrl.append(check.getServerId());
//        sUrl.append("&");
//        sUrl.append(NetConstant.user_id);
//        sUrl.append("=");
//        sUrl.append(user_id);
//        sUrl.append("&");
//        sUrl.append(NetConstant.wid);
//        sUrl.append("=");
//        sUrl.append(wid);
        LogUtils.show("requestQueryMeasureData---获取测量数据的请求链接：" + sUrl.toString());
        OkHttpUtils.get(sUrl.toString(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                /**
                 * 响应的数据样本

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
                        JSONArray dataArray = responeJson.getJSONArray("data");
                        if (dataArray.length() > 0) {
                            LogUtils.show("requestQueryMeasureData---查看管控要点的个数："+dataArray.length());
                            //有的管控要点图纸地址一样，为了避免重复下载，则做一个过滤
                            Set<String> imgUrls = new HashSet<>();
                            //存储已经下载完成对象
                            List<DownloadedImg> downloadedList = new ArrayList<>();
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
//                                    rulerCheckOptions.setImgNumber(optionJson.getInt());
                                    OptionMeasure measure = new OptionMeasure();
                                    measure.setId(optionJson.optInt("floor_height"));
                                    rulerCheckOptions.setFloorHeight(measure);
                                    rulerCheckOptions.setUpload_flag(1);
                                    rulerCheckOptions.setImg_upload_flag(1);
                                    String options_picture = optionJson.getString("options_picture");
                                    if (options_picture != null && !options_picture.equals("null") && options_picture.contains("{")) {
                                        JSONObject picJson = new JSONObject(options_picture);
                                        String pic = picJson.optString("url");
                                        if (pic == null || pic.equals("null")) {
                                            pic = "";
                                        }
                                        rulerCheckOptions.setImgNumber(picJson.getInt(DataBaseParams.options_data_number));
                                        rulerCheckOptions.setServerImgUrl(pic);

                                        //判断图片有没有下载过
                                        if (imgUrls.add(pic)) {
                                            //没有则调用下载
                                            String imgFileName = DateFormatUtil.formatDate(new Date(), "yyyyMMddHHmmSS") + ".png";
                                            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
                                            File file = new File(path, imgFileName);
                                            FileOkHttpUtils.downloadPFile(pic, file.getPath(), rulerCheckOptions, getApplicationContext());
                                            DownloadedImg downloadedImg = new DownloadedImg();
                                            downloadedImg.setPath(file.getPath());
                                            downloadedImg.setUrl(pic);
                                            downloadedList.add(downloadedImg);
                                        } else {
                                            //有则直接保存找出历史下载过的对象保存
                                            for (DownloadedImg downloadedImg : downloadedList) {
                                                if (downloadedImg.getUrl().equals(pic)) {
                                                    rulerCheckOptions.setImgPath(downloadedImg.getPath());
                                                    LogUtils.show("获取测量数据接口-----管控要点图片已经下载完成，直接复用");
                                                }
                                            }
//
                                        }

                                    }

                                    BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                                    String optionWhere = " where " + DataBaseParams.server_id + " = " + optionJson.getJSONObject("option_template").optInt("id");
                                    List<RulerOptions> rulerOptionsList = bleDataDbHelper.queryOptionsAllDataFromSqlite(optionWhere);
                                    if (rulerOptionsList.size() > 0) {
                                        rulerCheckOptions.setRulerOptions(rulerOptionsList.get(0));
                                    } else {
                                        rulerCheckOptions.setRulerOptions(new RulerOptions());
                                    }
                                    bleDataDbHelper.close();
                                    int index=OperateDbUtil.addMeasureOptionsDataToSqlite(getApplicationContext(), rulerCheckOptions);
                                    rulerCheckOptions.setId(index);

                                    LogUtils.show("requestQueryMeasureData---查看即将保存的数据内容："+rulerCheckOptions.toString());
                                }
//                                3.2 有则继续往下走

//                                4.取出options_data字段的Json数组，该字段的数据就是我们需要的ruler_check_data表格的数据，即对应管控要点的测量数据
                                JSONArray optionsDataArray = optionJson.getJSONArray("options_data");
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
                                            List<RulerCheckOptions> rulerOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), check, optionWhere);
                                            if (rulerOptionsList.size() > 0) {
                                                rulerCheckOptionsData.setRulerCheckOptions(rulerOptionsList.get(0));
                                            } else {
                                                rulerCheckOptionsData.setRulerCheckOptions(new RulerCheckOptions());
                                            }
                                            rulerCheckOptionsData.setUpload_flag(1);
                                            rulerCheckOptionsData.setData(optionDataJson.optString("data"));
                                            rulerCheckOptionsData.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                            rulerCheckOptionsData.setNumber(optionDataJson.optInt(DataBaseParams.options_data_number));
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


    /******************************* TODO 获取测量记录表接口请求开始 **********************************/
    private void requestQueryMeasureRecord(int finish_flag,int currentPage,int pageSize) {
        final User user = OperateDbUtil.getUser(getApplicationContext());
        int user_id = user.getUserID();
//        String wid = user.getWid();
//        if (wid == null || wid.equals("null")) {
//            wid = "0";
//        }
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
                 "data":[
                 {
                    "id":15,
                    "check_floor":"农业",
                    "engin_id":1,
                    "user_id":9,
                    "status":"测量完成",
                    "delete":0,
                    "delete_time":0,
                    "create_date":1547395200,
                    "create_time":1547452345,
                    "update_time":1547452692,
                    "engin_text":"混凝土工程",
                    "project_id":
                 {
                    "id":29,
                    "project_name":"祝姑姑岁",
                    "user_id":9,
                    "delete":0,
                    "create_time":"2019-01-13 14:36:08",
                    "update_time":"2019-01-13 15:08:46",
                    "delete_time":0
                 },
                 "unit_id":
                 {
                    "id":33,
                    "check_project_id":29,
                    "location":"马路牙子",
                    "delete":0,
                    "create_time":"2019-01-13 15:05:16",
                    "update_time":"2019-01-13 15:05:16",
                    "delete_time":0
                 }
                 }]}
                 */
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    if (code == 200) {
                        JSONArray dataArray = object.getJSONArray("data");
                        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
                        List<RulerCheck> checkList = new ArrayList<>();
                        LogUtils.show("获取记录表接口------查看总共有"+dataArray.length()+"条记录");
                        if (dataArray.length() > 0) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataJson = dataArray.getJSONObject(i);
                                /**********设置项目信息project*****************/
                                JSONObject projectJson = dataJson.getJSONObject("project_id");
                                int project_server_id = projectJson.optInt("id");
                                String projectWhere = " where " + DataBaseParams.server_id + "=" + project_server_id;
                                List<RulerCheckProject> projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), projectWhere);
                                RulerCheckProject project = new RulerCheckProject();
                                //如果数据库中已经存在有，则直接赋值
                                if (projectList.size() > 0) {
                                    project = projectList.get(0);
                                } else {
//                                        如果数据库中不存在，则保存到数据库
                                    project.setServer_id(project_server_id);
                                    project.setCreateTime(projectJson.getInt(DataBaseParams.measure_create_time));
                                    project.setProjectName(projectJson.optString(DataBaseParams.check_project_name));
                                    project.setUser(user);
                                    project.setUpdateTime(projectJson.optInt(DataBaseParams.measure_update_time));
                                    int index = OperateDbUtil.addProjectNameDataToSqlite(getApplicationContext(), project);
                                    project.setId(index);
                                }
//                                LogUtils.show("查找记录表----最终的项目信息："+check.getProject());

                                /*************设置UnitEngineer*************/
                                String s_u_id = dataJson.getString("unit_id");
                                RulerUnitEngineer unitEngineer = new RulerUnitEngineer();
                                if (s_u_id != null && s_u_id.contains("{") && s_u_id.contains("}")) {
                                    JSONObject unitJson = dataJson.getJSONObject("unit_id");
                                    int unit_server_id = unitJson.optInt(DataBaseParams.measure_id);
                                    String unitWhere = " where " + DataBaseParams.server_id + "=" + unit_server_id;
                                    List<RulerUnitEngineer> unitList = OperateDbUtil.queryUnitEngineerDataFromSqlite(getApplicationContext(), unitWhere);

                                    if (unitList.size() > 0) {
                                        unitEngineer = unitList.get(0);
                                    } else {
                                        RulerUnitEngineer unit = new RulerUnitEngineer();
                                        unit.setServer_id(unit_server_id);
                                        unit.setUpdateTime(unitJson.optInt(DataBaseParams.measure_update_time));
                                        unit.setLocation(unitJson.getString(DataBaseParams.unit_engineer_location));
                                        unit.setCreateTime(unitJson.getInt(DataBaseParams.measure_create_time));
                                        unit.setProject_id(project.getId());
                                        unit.setProject_server_id(project.getServer_id());
                                        int index = OperateDbUtil.addUnitPositionDataToSqlite(getApplicationContext(), unit);
                                        unit.setId(index);
                                        unitEngineer = unit;
                                    }
                                }



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
                                    LogUtils.show("server_id为：" + server_id + ",状态标志为：" + status + "的数据已经存在");
                                    int finish_flag = rulerCheckList.get(0).getStatus();
//                                    我们只处理一种情况，如果服务器的标志为0，而本地的标志为1，则补服务器的更新状态标志
                                    if (status == 0 && finish_flag > 0) {
                                        requestStopMeasure(server_id);
                                    } else if (status == 1 && finish_flag == 0) {
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.measure_is_finish, 2);
                                        values.put(DataBaseParams.upload_flag, 1);
                                        String uwhere =DataBaseParams.server_id + "=?" ;
                                        OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_table_name, uwhere, values, new String[]{String.valueOf(server_id)});

                                    } else {
                                        //  如果都是一样的标志，则添加到备用的集合中
                                        checkList.add(rulerCheckList.get(0));
                                    }
                                    //判断项目名是否有修改
                                    if (projectJson.optInt("id") != rulerCheckList.get(0).getProject().getServer_id()) {
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.project_server_id, projectJson.optInt(DataBaseParams.measure_id));
                                        String upwhere = DataBaseParams.server_id + "=?";
                                        int res = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_table_name, upwhere, values, new String[]{String.valueOf(server_id)});
                                        LogUtils.show("查询记录表接口-----项目名有改变----查看更新后的项目id：" + projectJson.optInt(DataBaseParams.measure_id) + ",更新标志：" + res);
                                    }

                                    if (s_u_id != null && s_u_id.contains("{") && s_u_id.contains("}")) {
                                        //判断单位工程是否有修改
                                        JSONObject unitJson = dataJson.getJSONObject("unit_id");
                                        if (unitJson.optInt("id") != rulerCheckList.get(0).getUnitEngineer().getServer_id()) {
                                            ContentValues values = new ContentValues();
                                            values.put(DataBaseParams.measure_unit_id, unitEngineer.getId());
                                            String upwhere = DataBaseParams.server_id + "=?";
                                            int res = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_table_name, upwhere, values, new String[]{String.valueOf(server_id)});
                                            LogUtils.show("查询记录表接口-----单位工程名有改变----查看更新后的单位工程名id：" + unitJson.optInt(DataBaseParams.measure_id) + ",更新标志：" + res);
                                        }
                                    }


                                } else {
                                    /**
                                     * 如果本条数据，本地没有存储，则添加到本地数据库中
                                     */
                                    RulerCheck check = new RulerCheck();
//                                    check.setProjectName(dataJson.optString("project_name"));
                                    check.setCheckFloor(dataJson.optString("check_floor"));
                                    check.setUnitEngineer(unitEngineer);
                                    check.setProject(project);
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
                                    /*********设置engineer********/
                                    int engin_id = dataJson.optInt("engin_id");
                                    LogUtils.show("server_id为：" + server_id + ",状态标志为：" + status + "的数据不存在，现在开始保存");
                                    String engin_where = " where " + DataBaseParams.server_id + "=" + engin_id;
                                    List<RulerEngineer> engineerList = dataDbHelper.queryEnginDataFromSqlite(engin_where);
                                    if (engineerList.size() > 0) {
                                        check.setEngineer(engineerList.get(0));
                                    } else {
                                        /**
                                         * 获取模板信息
                                         */
                                        Intent intent = new Intent(getApplicationContext(), GetMudelIntentService.class);
                                        startService(intent);

                                        RulerEngineer engineer = new RulerEngineer();
                                        engineer.setServerID(engin_id);
                                        check.setEngineer(engineer);
                                    }
                                    /*********设置用户信息user************/
                                    check.setUser(user);


                                    LogUtils.show("查找记录表----最终的单位工程信息：" + check.getUnitEngineer());
                                    int check_local_id = OperateDbUtil.addMeasureDataToSqlite(getApplicationContext(), check);
                                    if (check_local_id > 0) {
                                        check.setId(check_local_id);
                                        LogUtils.show("服务器的check，保存本地成功：" + check);
                                    }
                                    checkList.add(check);
                                }
                            }

                        } else {
                            List<RulerCheck> rulerCheckList = dataDbHelper.queryRulerCheckTableDataFromSqlite("");

                            /**
                             * 如果本条数据已经存有一条，则查看服务器端和本地端的完成标志是否一样
                             */
                           for (int i=0;i<rulerCheckList.size();i++) {
                               int finish_flag = rulerCheckList.get(i).getStatus();
                               if (finish_flag == 0 && rulerCheckList.get(i).getServerId() > 0) {
                                   ContentValues values = new ContentValues();
                                   values.put(DataBaseParams.measure_is_finish, 2);
                                   values.put(DataBaseParams.upload_flag, 1);
                                  String uwhere =DataBaseParams.server_id + "=?" ;
                                   OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.measure_table_name, uwhere, values, new String[]{String.valueOf(rulerCheckList.get(i).getId())});

                               }
                           }


                        }
                        MeasureRecordMsgEvent event = new MeasureRecordMsgEvent();
                        event.setCheckList(checkList);
                        LogUtils.show("服务器端-------查看最终的CheckList-----"+checkList.toString());
                        event.setCurrentPage(object.optInt("currentPage"));
                        event.setPageSize(object.optInt("pageSize"));
                        event.setTotal(object.optInt("total"));
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    MeasureRecordMsgEvent event = new MeasureRecordMsgEvent();
                    List<RulerCheck> checkList = new ArrayList<>();
                    event.setCheckList(checkList);
                    event.setCurrentPage(1);
                    event.setPageSize(20);
                    event.setTotal(0);
                    EventBus.getDefault().post(event);
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



    /********************************TODO 结束/停止测量接口开始*****************************************/
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

    /*********************************************TODO *更新测量数据接口开始***************************************************/
    private void initUpdateMeasureDataJson(List<RulerCheckOptions> checkOptionsList, List<RulerCheckOptionsData> checkOptionsDataList) {
        LogUtils.show("initUpdateMeasureDataJson----查看收到的checkOptionsList："+checkOptionsList);
        LogUtils.show("initUpdateMeasureDataJson----查看收到的checkOptionsDataList："+checkOptionsDataList);
        boolean isFinish = false;
        try {
            if (checkOptionsList.size() > 0) {
                JSONArray rootJsArray = new JSONArray();
                Set<RulerCheck> rulerCheckSet = new HashSet<>();
                for (int n=0;n<checkOptionsList.size();n++) {
                    if (!rulerCheckSet.add(checkOptionsList.get(n).getRulerCheck())) {
                        continue;
                    }
                    RulerCheckOptions rulerCheckOptions = checkOptionsList.get(n);
                    rulerCheckSet.add(rulerCheckOptions.getRulerCheck());
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
                            dataJson.put(DataBaseParams.measure_option_floor_height, checkOptions.getFloorHeight().getId());
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
                                    jsonObject.put(DataBaseParams.options_data_number, rulerCheckOptionData.getNumber());
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
//                        seJson.put(DataBaseParams.measure_project_name, rulerCheckOptions.getRulerCheck().getProjectName());
                        seJson.put(DataBaseParams.measure_engin_id, rulerCheckOptions.getRulerCheck().getEngineer().getServerID());
                        seJson.put(DataBaseParams.user_user_id, rulerCheckOptions.getRulerCheck().getUser().getUserID());
//                        seJson.put(DataBaseParams.user_wid, rulerCheckOptions.getRulerCheck().getUser().getWid());
                        seJson.put(DataBaseParams.measure_create_date, rulerCheckOptions.getRulerCheck().getCreateDate());
                        seJson.put(DataBaseParams.measure_create_time, rulerCheckOptions.getRulerCheck().getCreateTime());
                        seJson.put(DataBaseParams.measure_update_time, rulerCheckOptions.getRulerCheck().getUpdateTime());
                        seJson.put(DataBaseParams.measure_check_floor, rulerCheckOptions.getRulerCheck().getCheckFloor());
//                        seJson.put(DataBaseParams.user_status, rulerCheckOptions.getRulerCheck().getStatus());
                        int is_finish = rulerCheckOptions.getRulerCheck().getStatus();
                        if (is_finish > 0) {
                            seJson.put(DataBaseParams.user_status, 1);
                            isFinish = true;
                        } else {
                            seJson.put(DataBaseParams.user_status, 0);
                        }

//                    options字段的JSON数组，初始化完要加入seJson
                        /***********check_options：所有管控要点的JSON数组*****************/
                        JSONArray optionsArray = new JSONArray();
                        for (RulerCheckOptions checkOptions : checkOptionsList) {
                            JSONObject dataJson = new JSONObject();
                            dataJson.put(DataBaseParams.local_id, checkOptions.getId());
                            dataJson.put(DataBaseParams.measure_option_options_id, checkOptions.getRulerOptions().getServerID());
//                            dataJson.put(DataBaseParams.measure_option_floor_height, checkOptions.getFloorHeight());
                            dataJson.put(DataBaseParams.measure_option_measured_points, checkOptions.getMeasuredNum());
                            dataJson.put(DataBaseParams.measure_option_qualified_points, checkOptions.getQualifiedNum());
                            dataJson.put(DataBaseParams.measure_option_percent_pass, checkOptions.getQualifiedRate());
                            dataJson.put(DataBaseParams.measure_update_time, checkOptions.getUpdateTime());
                            dataJson.put(DataBaseParams.measure_option_floor_height, checkOptions.getFloorHeight().getId());
                            dataJson.put(DataBaseParams.measure_create_time, checkOptions.getCreateTime());
//                        data字段的JSON数组，初始化完，要加入dataJson
                            JSONArray optionDataArray = new JSONArray();
                            for (RulerCheckOptionsData data : checkOptionsDataList) {
                                if (data.getRulerCheckOptions().getId() == checkOptions.getId()) {
                                    JSONObject object = new JSONObject();
                                    object.put(DataBaseParams.user_data, data.getData());
                                    object.put(DataBaseParams.local_id, data.getId());
                                    object.put(DataBaseParams.options_data_number, data.getNumber());
                                    optionDataArray.put(object);
                                }
                            }
                            dataJson.put(DataBaseParams.user_data, optionDataArray);
                            optionsArray.put(dataJson);
                        }
                        seJson.put("check_options", optionsArray);
                        /****************check_project：项目名称****************/
                        JSONObject projectJson = new JSONObject();
                        RulerCheckProject project = rulerCheckOptions.getRulerCheck().getProject();
                        if (project.getServer_id() > 0) {
                            projectJson.put(DataBaseParams.measure_id, project.getServer_id());
                        } else {
                            projectJson.put(DataBaseParams.local_id, project.getId());
                        }
                        projectJson.put(DataBaseParams.check_project_name, project.getProjectName());
                        projectJson.put(DataBaseParams.user_user_id, rulerCheckOptions.getRulerCheck().getUser().getUserID());
                        projectJson.put(DataBaseParams.measure_create_time, project.getCreateTime());
                        projectJson.put(DataBaseParams.measure_update_time, project.getUpdateTime());
//            添加到父级Json
                        seJson.put("check_project", projectJson);

                        /****************unit_engineer检查位置******************/
                        JSONObject unitJson = new JSONObject();

                        RulerUnitEngineer unitEngineer = rulerCheckOptions.getRulerCheck().getUnitEngineer();
                        LogUtils.show("一直没网络---查看单位工程："+unitEngineer);
                        if (unitEngineer.getServer_id() > 0) {
                            unitJson.put(DataBaseParams.measure_id, unitEngineer.getServer_id());
                        } else {
                            unitJson.put(DataBaseParams.local_id, unitEngineer.getId());
                        }
                        unitJson.put(DataBaseParams.unit_engineer_location, unitEngineer.getLocation());
                        unitJson.put(DataBaseParams.measure_create_time, unitEngineer.getCreateTime());
                        unitJson.put(DataBaseParams.measure_update_time, unitEngineer.getUpdateTime());
                        seJson.put("unit_engineer", unitJson);
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
        LogUtils.show("更新测量数据接口，查看连接："+url+",参数："+dataList);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("requestUpdateMeasureData---查看更新测量数据服务器返回的数据内容："+response);
                /**
                 * 之前有网和没网返回的数据内容不一样，
                 *    无网 一级data下面有options数组，且没有data数组
                 *    有网 一级data下面有个二级data数组，且没有options数组
                 */
                try {
                    JSONObject rootJson = new JSONObject(response);
                    int code = rootJson.optInt("code");
                    if (code == 200) {
                        JSONArray dataRootArray = rootJson.getJSONArray("data");
                        if (dataRootArray.length() > 0) {
                            for (int i=0;i<dataRootArray.length();i++) {
                                JSONObject dataJson = dataRootArray.getJSONObject(i);

                                /**
                                 * 处理有网络情况下数据
                                 */
                                if (dataJson.has("data") && !dataJson.has("options")) {
                                    LogUtils.show("requestUpdateMeasureData----进入有网络模式");
                                    JSONArray optionDatas = dataJson.getJSONArray("data");
                                    if (optionDatas.length() > 0) {
                                        /**之前有网络的情况下，只需要把check_options_data里面的server_id更新到数据库即可***/
                                        for (int j=0;j<optionDatas.length();j++) {
                                            int local_id = optionDatas.getJSONObject(j).optInt(DataBaseParams.local_id);
                                            int server_id = optionDatas.getJSONObject(j).optInt(DataBaseParams.measure_id);
//                                        获取到两个数据后，开始更新到数据库
                                            ContentValues values = new ContentValues();
                                            values.put(DataBaseParams.server_id, server_id);
                                            values.put(DataBaseParams.upload_flag, 1);
                                            int res=OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), values, new String[]{String.valueOf(local_id)});
                                            LogUtils.show("更新数据接口---有网络模式--查看更新数据是否成功："+server_id+",返回值："+res);

                                        }
                                        EventBus.getDefault().post(new HandleDataResultMsgEvent(flag));
                                    }
                                }
                                /**
                                 * 处理无网络情况下的数据
                                 */
                                if (dataJson.has(NetConstant.update_check_options) && dataJson.has(DataBaseParams.local_id)) {
                                    LogUtils.show("requestUpdateMeasureData----进入无网络模式");

                                    /**
                                     * 没网时，之前的ruler_check表、ruler_check_options表都没有在服务器创建一条数据。
                                     * 所以现在一次性提交就会一次行返回这几个表的数据内容，包括ruler_check_options_data也会一起创建
                                     * 依次将这三个表的server_id和upload_flag更新到数据库
                                     */
                                    /****更新check表的数据*****/
//                                1.获取ruler_check里的本地id和server_id
                                    int ruler_check_local_id = dataJson.getInt(DataBaseParams.local_id);
                                    int ruler_check_server_id = dataJson.getInt(DataBaseParams.measure_id);

                                    ContentValues rulerCheckValues = new ContentValues();
                                    rulerCheckValues.put(DataBaseParams.server_id, ruler_check_server_id);
                                    rulerCheckValues.put(DataBaseParams.upload_flag, 1);
//                                1.1将server_id和upload_flag更新到数据库
                                    BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                                    int checkR = bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, rulerCheckValues, " id =? ", new String[]{String.valueOf(ruler_check_local_id)});
                                    LogUtils.show("更新测量数据返回----查看更新Checkd server_id是否成功：" + ruler_check_server_id + ",返回值：" + checkR);
                                    //   如果发过来的请求是已经测量完成的，则更新测量状态为2
                                    if (isFinish) {
                                        savaCheckStatus(ruler_check_server_id);
                                    }
                                    /*******更新管控要点的数据*******/
                                    JSONArray optionsArray = dataJson.getJSONArray(NetConstant.update_check_options);
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
                                            int result= bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_option_table_name, checkOptionsValues, " id=? ", new String[]{String.valueOf(check_option_local_id)});
                                            LogUtils.show("更新测量数据返回----查看更新管控要点server_id是否成功：" + check_option_server_id + ",返回值：" + result);
                                            JSONArray optionDataArray = object.getJSONArray("data");

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
                                                    int dataResult= bleDataDbHelper.updateDataToSqlite(DataBaseParams.options_data_table_name, checkOptionsDataValues, " id=?", new String[]{String.valueOf(check_options_data_local_id)});
                                                    LogUtils.show("更新测量数据返回----查看数据内容server_id是否成功：" + check_options_data_server_id + ",返回值：" + dataResult);

                                                }
                                            }

                                        }

                                    }

                                    /***********更新check_project数据**************/
                                    JSONObject projectJson = dataJson.getJSONObject("check_project");
                                    if (projectJson.has(DataBaseParams.local_id)) {
                                        int p_local_id = projectJson.getInt(DataBaseParams.local_id);
                                        int p_server_id = projectJson.getInt(DataBaseParams.measure_id);
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.server_id, p_server_id);
                                        int result= bleDataDbHelper.updateDataToSqlite(DataBaseParams.check_project_table_name, values, " id=? ", new String[]{String.valueOf(p_local_id)});
                                        LogUtils.show("更新测量数据返回----查看更项目Project_server_id是否成功：" + p_server_id + ",返回值：" + result);
                                    }

                                    /*************更新unit_engineer熟***********/
                                    JSONObject unitJson = dataJson.getJSONObject("unit_engineer");
                                    if (unitJson.has(DataBaseParams.local_id)) {
                                        int u_local_id = projectJson.getInt(DataBaseParams.local_id);
                                        int u_server_id = projectJson.getInt(DataBaseParams.measure_id);
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.server_id, u_server_id);
                                        int result= bleDataDbHelper.updateDataToSqlite(DataBaseParams.unit_engineer_table_name, values, " id=? ", new String[]{String.valueOf(u_local_id)});
                                        LogUtils.show("更新测量数据返回----查看更新单位工程unit_engineer server_id是否成功：" + u_server_id + ",返回值：" + result);
                                    }

                                    EventBus.getDefault().post(new HandleDataResultMsgEvent(flag));
                                }
                            }
                        }
                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {LogUtils.show("requestUpdateMeasureData----网络请求失败");
            }
        },dataList);

    }
    /**********************************************更新测量数据接口结束***************************************************/








    /************************************************TODO 创建记录表接口开始****************************************************************/

    /**
     * TODO 创建记录表
     * 初始化请求“创建记录表”接口所需要的json数据
     * @param rulerCheck
     * @param checkOptionsList
     */
    private void initCreateRecordJsonData(RulerCheck rulerCheck, List<RulerCheckOptions> checkOptionsList) {
        /**
         * 发送的模板：
         *
         */
        try {
//            LogUtils.show("initJsonData---查看上传服务器之前RulerCHeck数据：" + rulerCheck.toString());
            /********jsonObject：第一级************/
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(DataBaseParams.local_id, rulerCheck.getId());
            jsonObject.put(DataBaseParams.measure_check_floor, rulerCheck.getCheckFloor());
            jsonObject.put(DataBaseParams.options_engin_id, rulerCheck.getEngineer().getServerID());
            jsonObject.put(DataBaseParams.user_user_id, rulerCheck.getUser().getUserID());
            jsonObject.put(DataBaseParams.user_status, rulerCheck.getStatus());
//
//            long create_time = DateFormatUtil.transForMilliSecond(new Date());
//            String date = DateFormatUtil.getDate("yyyy-MM-dd");
//            LogUtils.show("initJsonData---查看日期：" + date + ",查看时间戳：" + create_time);
//            int create_date = DateFormatUtil.transForMilliSecondByTim(date, "yyyy-MM-dd");
//            LogUtils.show("initJsonData---查看日期时间戳：" + create_date);

            jsonObject.put(DataBaseParams.measure_create_date, rulerCheck.getCreateDate());
            jsonObject.put(DataBaseParams.measure_create_time, rulerCheck.getCreateTime());
            jsonObject.put(DataBaseParams.enginer_update_time, rulerCheck.getUpdateTime());
            /*********第二级：check_options的JSON数组************/
            JSONArray jsonArray = new JSONArray();
            LogUtils.show("initJsonData---查看管控要点：" + checkOptionsList.toString());
            for (int i = 0; i < checkOptionsList.size(); i++) {
                JSONObject optionJson = new JSONObject();
                optionJson.put(DataBaseParams.local_id, checkOptionsList.get(i).getId());
                optionJson.put(DataBaseParams.measure_option_options_id, checkOptionsList.get(i).getRulerOptions().getServerID());
                optionJson.put(DataBaseParams.measure_option_measured_points, checkOptionsList.get(i).getMeasuredNum());
                optionJson.put(DataBaseParams.measure_option_floor_height, checkOptionsList.get(i).getFloorHeight().getId());
                optionJson.put(DataBaseParams.measure_option_qualified_points, checkOptionsList.get(i).getQualifiedNum());
                optionJson.put(DataBaseParams.measure_option_percent_pass, checkOptionsList.get(i).getQualifiedRate());
                optionJson.put(DataBaseParams.measure_create_time, checkOptionsList.get(i).getCreateTime());
                optionJson.put(DataBaseParams.measure_update_time, checkOptionsList.get(i).getUpdateTime());
                jsonArray.put(optionJson);
            }
            jsonObject.put(DataBaseParams.check_options, jsonArray);

            /***************第二级：check_project项目名*******************/
            JSONObject projectJson = new JSONObject();
            RulerCheckProject project = rulerCheck.getProject();
            if (project.getServer_id() > 0) {
                projectJson.put(DataBaseParams.measure_id, project.getServer_id());
            } else {
                projectJson.put(DataBaseParams.local_id, project.getId());
            }
            projectJson.put(DataBaseParams.check_project_name, project.getProjectName());
            projectJson.put(DataBaseParams.user_user_id, rulerCheck.getUser().getUserID());
            projectJson.put(DataBaseParams.measure_create_time, project.getCreateTime());
            projectJson.put(DataBaseParams.measure_update_time, project.getUpdateTime());
//            添加到父级Json
            jsonObject.put("check_project", projectJson);

            /****************第二级：unit_engineer检查位置******************/
            JSONObject unitJson = new JSONObject();
            RulerUnitEngineer unitEngineer = rulerCheck.getUnitEngineer();
            if (unitEngineer.getServer_id() > 0) {
                unitJson.put(DataBaseParams.measure_id, unitEngineer.getServer_id());
            } else {
                unitJson.put(DataBaseParams.local_id, unitEngineer.getId());
            }
            unitJson.put(DataBaseParams.unit_engineer_location, unitEngineer.getLocation());
            unitJson.put(DataBaseParams.measure_create_time, unitEngineer.getCreateTime());
            unitJson.put(DataBaseParams.measure_update_time, unitEngineer.getUpdateTime());
//            添加到主JSON
            jsonObject.put("unit_engineer", unitJson);

            LogUtils.show("创建记录表接口：initJsonData---查看最终创建记录表的请求Json：" + jsonObject.toString());
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
                LogUtils.show("requestCreateRecord 创建记录表接口----查看返回的数据："+response);
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
                        /********解析保存check表的数据*******/
                        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
                        //                        讲server_id更新到ruler_check表格中
                        ContentValues checkValues = new ContentValues();
                        checkValues.put(DataBaseParams.server_id, check_server_id);
                        checkValues.put(DataBaseParams.upload_flag, 1);
                        int r=bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, checkValues, "id =?",new String[]{String.valueOf(check_local_id)});
                        LogUtils.show("requestCreateRecord---查看ruler_check更新状态：" + r);
                        LogUtils.show("requestCreateRecord---查看服务器返回的管控要点个数:"+checkOptionsArray.length());
                        int result = 0;
                        /********解析管控要点的数据***********/
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
                        /**************解析保存单位工程的数据****************/
                        if (dataJson.has("unit_engineer")) {
                            JSONObject unitJson = dataJson.getJSONObject("unit_engineer");
                            int unit_local_id = unitJson.optInt(DataBaseParams.local_id);
                            int unit_server_id = unitJson.optInt(DataBaseParams.measure_id);
                            ContentValues unitValues = new ContentValues();
                            unitValues.put(DataBaseParams.server_id, unit_server_id);
                            int ur = bleDataDbHelper.updateDataToSqlite(DataBaseParams.unit_engineer_table_name, unitValues, "id=?", new String[]{String.valueOf(unit_local_id)});
                            LogUtils.show("requestCreateRecord---查看单位工程的数据id："+unit_server_id+",本地id："+unit_local_id+",更新状态：" + ur);
                        }


                        /**************解析保存项目名称**************/
                        if (dataJson.has("check_project")) {
                            JSONObject projectJson = dataJson.getJSONObject("check_project");
                            int project_local_id = projectJson.optInt(DataBaseParams.local_id);
                            int project_server_id = projectJson.optInt(DataBaseParams.measure_id);
                            ContentValues projectValues = new ContentValues();
                            projectValues.put(DataBaseParams.server_id, project_server_id);
                            int pr=bleDataDbHelper.updateDataToSqlite(DataBaseParams.check_project_table_name, projectValues, "id=?", new String[]{String.valueOf(project_local_id)});
                            LogUtils.show("requestCreateRecord---查看解析保存项目名称的数据id："+project_server_id+",本地id："+project_local_id+",更新状态：" + pr);
                            //更新成员表的数据
                            User user = OperateDbUtil.getUser(getApplicationContext());
                            String mWhere = " where " + DataBaseParams.user_user_id + "=" + user.getUserID() + " and " + DataBaseParams.measure_project_id + "=" + project_local_id;
                            List<ProjectUser> projectUserList = OperateDbUtil.queryProjectUserFromSqlite(getApplicationContext(), mWhere);
                            if (projectUserList.size() > 0) {
                                ContentValues values = new ContentValues();
                                values.put(DataBaseParams.project_server_id, project_server_id);
                                int user_r = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.project_user_table_name, "id=?", values, new String[]{String.valueOf(projectUserList.get(0).getId())});
                                LogUtils.show("创建记录表接口------更新成员表的project_server_id是否成功：" + user_r);
                                ///向服务器更新该成员ID到数据库
                                Bundle b = new Bundle();
                                b.putInt(DataBaseParams.measure_project_id, project_server_id);
                                Intent intent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
                                intent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_get_member_and_unit);
                                intent.putExtra(ProjectManageRequestIntentService.key_get_value, b);
                                startService(intent);
                            }
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
                LogUtils.show("创建记录表----请求失败："+e.getMessage());

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
