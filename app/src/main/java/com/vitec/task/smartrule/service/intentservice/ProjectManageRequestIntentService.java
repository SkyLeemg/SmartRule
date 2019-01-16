package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.GetMemberAndUnitMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureDataMsgEvent;
import com.vitec.task.smartrule.bean.event.MoblieRequestResutEvent;
import com.vitec.task.smartrule.bean.event.ProjectGroupAnyMsgEvent;
import com.vitec.task.smartrule.bean.event.QueryProjectGroupMsgEvent;
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

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 项目管理相关的各种接口请求
 * 1.创建测量组
 * 2.更新测量组
 * 3.删除测量组
 * 4.查询测量组
 * 5.添加成员
 * 6.删除成员
 * 7.添加单位工程
 * 8.更新单位工程
 * 9.删除单位工程
 * 10.查询测量组成员和单位工程
 */
public class ProjectManageRequestIntentService extends IntentService {

    public static final String REQUEST_FLAG = "REQUEST_FLAG";
    //创建测量组接口
    public static final String flag_group_create_project = "smart.ruler.createProject";
    //更新测量组
    public static final String flag_group_update_proect = "smart.ruler.updateProject";
    //删除测量组
    public static final String flag_group_del_project = "smart.ruler.deleteProject";
    //查询测量组
    public static final String flag_group_get_project_list = "smart.ruler.getProjectList";
    //添加成员
    public static final String flag_group_add_member = "smart.ruler.addGroupMember";
    //删除成员
    public static final String flag_group_del_member = "smart.ruler.deleteGroupMember";
    //添加单位工程
    public static final String flag_group_add_unit_engineer = "smart.ruler.addUnitEngineer";
    //更新单位工程
    public static final String flag_group_update_unit_engineer = "smart.ruler.updateUnitEngineer";
    //删除单位工程
    public static final String flag_group_del_unit_engineer= "smart.ruler.deleteUnitEngineer";
    //查询测量组成员和单位工程
    public static final String flag_group_get_member_and_unit = "smart.ruler.getGroupMemberAndUnitEngineer";
    //扫码进群
    public static final String flag_group_scan_qr_enter_project = "smart.ruler.scanQrEnterProject";
    //请求所有的测量组和单位工程/成员信息
    public static final String flag_group_quest_all_msg = "mart.ruler.request.all.msg";
//    获取参数内容
    public static final String key_get_value = "smart.ruler.get.value";



    public ProjectManageRequestIntentService() {
        super("ProjectManageRequestIntentService");
    }

    public ProjectManageRequestIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String flag = intent.getStringExtra(REQUEST_FLAG);
        Bundle bundle = intent.getBundleExtra(key_get_value);
        switch (flag) {
            /**********创建测量组接口**************/
            case flag_group_create_project:
                requestCreateProecjt(bundle);
                break;


            /**********更新测量组**************/
            case flag_group_update_proect:
                updateGroupProjectName(bundle);
                break;


            /**********删除测量组**************/
            case flag_group_del_project:
                delProjectGroupFromServer(bundle);
                break;


            /**********查询测量组**************/
            case flag_group_get_project_list:
                queryProjectGroupFromServer(bundle);
                break;

            /**********添加成员**************/
            case flag_group_add_member:
                addMemberToServer(bundle);
                break;


            /**********删除成员**************/
            case flag_group_del_member:
                delMemberFromServer(bundle);
                break;


            /**********添加单位工程**************/
            case flag_group_add_unit_engineer:
                addUnitEngineerToServer(bundle);

                break;

            /**********更新单位工程**************/
            case flag_group_update_unit_engineer:

                break;

            /**********删除单位工程**************/
            case flag_group_del_unit_engineer:
                delUnitEngineerFromServer(bundle);
                break;

            /**********查询测量组成员和单位工程**************/
            case flag_group_get_member_and_unit:
                queryMemberAndUnit(bundle);
                break;

                /************扫码进群****************/
            case flag_group_scan_qr_enter_project:
                scanQrEnterProject(bundle);
                break;

                /***********请求所有的测量组和单位工程/成员信息**************/
            case flag_group_quest_all_msg:
                requestAllMsg();
                break;
        }

    }



    /***********请求所有的测量组和单位工程/成员信息开始**************/
    private void requestAllMsg() {
        User user = OperateDbUtil.getUser(getApplicationContext());
        Bundle bundle = new Bundle();
        bundle.putInt(DataBaseParams.user_user_id, user.getUserID());
        bundle.putInt(NetConstant.page_size, 50);
        bundle.putInt(NetConstant.current_Page, 1);
        queryProjectGroupFromServer(bundle);

    }
    /***********请求所有的测量组和单位工程/成员信息结束**************/


    /*********************TODO 扫码进群开始***************************/
    private void scanQrEnterProject(Bundle bundle) {
        String url = bundle.getString(DataBaseParams.check_project_qrcode);
        LogUtils.show("scanQrEnterProject----扫码进群链接："+url);
        OkHttpUtils.get(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response)  {
                /**
                 *{"status":"success","code":200,"data":{"group_id":"20","name":"123","project_id":"23"},"msg":"添加成员成功"}
                 */
                LogUtils.show("扫码进群----查看返回值："+response);
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    String msg = object.optString("msg");
                    if (code == 200) {
                        JSONObject dataJson = object.getJSONObject("data");
                        int project_id = dataJson.getInt(DataBaseParams.measure_project_id);
                        Bundle bundle1 = new Bundle();
                        User user = OperateDbUtil.getUser(getApplicationContext());
                        bundle1.putInt(DataBaseParams.user_user_id, user.getUserID());
                        bundle1.putInt(NetConstant.page_size, 50);
                        bundle1.putInt(NetConstant.current_Page, 1);
                        queryProjectGroupFromServer(bundle1);

                        Bundle newBun = new Bundle();
                        newBun.putInt(DataBaseParams.measure_project_id, project_id);
                        queryMemberAndUnit(newBun);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    /*********************扫码进群结束***************************/



    /****************TODO 删除测量组开始*********************/
    private void delProjectGroupFromServer(Bundle bundle) {
        final String project_list = bundle.getString(NetConstant.group_project_list);
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.group_project_list, project_list);
        String url = NetConstant.baseUrl + NetConstant.group_del_project_url;
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        LogUtils.show("删除测量组开始:"+url+",参数:"+param);
        final ProjectGroupAnyMsgEvent event = new ProjectGroupAnyMsgEvent();
        event.setRequst_flag(6);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("删除测量组开始-----打印查看返回的参数:"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        RulerCheckProject project = new RulerCheckProject();
                        String where = DataBaseParams.server_id + "=?";
                        if (!project_list.contains(",")) {
                            OperateDbUtil.delData(getApplicationContext(), DataBaseParams.check_project_table_name, where, new String[]{project_list});
                            project.setServer_id(Integer.parseInt(project_list));
                        } else {

                        }
                        LogUtils.show("删除测量组开始---正在发送通知");
                        event.setObject(project);
                        event.setSuccess(true);
                        EventBus.getDefault().post(event);
                    } else {
//                        event.setObject(project);
                        event.setSuccess(false);
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setMsg("数据解析失败");
                    event.setSuccess(false);
                    EventBus.getDefault().post(event);
                }

            }

            @Override
            public void onFailure(Exception e) {
                event.setMsg("网络请求失败");
                event.setSuccess(false);
                EventBus.getDefault().post(event);
            }
        },paramList);

    }
    /******************删除测量组结束*********************/

    /****************TODO 删除成员开始*********************/
    private void delMemberFromServer(Bundle bundle) {
        final String group_id = bundle.getString(NetConstant.group_group_list);
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.group_group_list, group_id);
        final String url = NetConstant.baseUrl + NetConstant.group_del_member_url;
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        LogUtils.show("删除成员接口:"+url+",参数:"+param);
        final ProjectGroupAnyMsgEvent event = new ProjectGroupAnyMsgEvent();
        event.setRequst_flag(5);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("删除成员-----打印查看返回的参数:"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        ProjectUser user = new ProjectUser();
                        String where = DataBaseParams.server_id + "=?";
                        if (!group_id.contains(",")) {
                            String qw = " where " + DataBaseParams.server_id + "=?" + group_id;
                            List<ProjectUser> userList = OperateDbUtil.queryProjectUserFromSqlite(getApplicationContext(), qw);
                            if (userList.size() > 0) {
                                user = userList.get(0);
                            }
                            OperateDbUtil.delData(getApplicationContext(), DataBaseParams.project_user_table_name, where, new String[]{group_id});
                            user.setServer_id(Integer.parseInt(group_id));
                        } else {
                            user.setServer_id(0);
                        }
                        event.setObject(user);
                        event.setSuccess(true);
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setMsg("数据解析失败");
                    event.setSuccess(false);
                    EventBus.getDefault().post(event);
                }

            }

            @Override
            public void onFailure(Exception e) {
                event.setMsg("网络请求失败");
                event.setSuccess(false);
                EventBus.getDefault().post(event);
            }
        },paramList);
    }
    /****************删除成员结束*********************/



    /**********************TODO 删除单位工程开始**************************/
    private void delUnitEngineerFromServer(Bundle bundle) {
        final String unit_id = bundle.getString(NetConstant.group_unit_list);
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.group_unit_list, unit_id);
        String url = NetConstant.baseUrl + NetConstant.group_del_unit_engineer_url;
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        LogUtils.show("删除单位工程接口:"+url+",参数:"+param);
        final ProjectGroupAnyMsgEvent event = new ProjectGroupAnyMsgEvent();
        event.setRequst_flag(4);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("删除单位工程-----打印查看返回的参数:"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        RulerUnitEngineer engineer = new RulerUnitEngineer();

                        String where = DataBaseParams.server_id + "=?";
                        if (!unit_id.contains(",")) {
                            OperateDbUtil.delData(getApplicationContext(), DataBaseParams.unit_engineer_table_name, where, new String[]{unit_id});
                            engineer.setServer_id(Integer.parseInt(unit_id));
                        } else {

                        }
                        event.setObject(engineer);
                        event.setSuccess(true);
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setMsg("数据解析失败");
                    event.setSuccess(false);
                    EventBus.getDefault().post(event);
                }

            }

            @Override
            public void onFailure(Exception e) {
                event.setMsg("网络请求失败");
                event.setSuccess(false);
                EventBus.getDefault().post(event);
            }
        },paramList);

    }
    /**********************删除单位工程结束**************************/





    /***************TODO 添加单位工程开始********************/
    private void addUnitEngineerToServer(Bundle bundle) {

        final int project_id = bundle.getInt(NetConstant.group_project_list);
        final String location = bundle.getString(DataBaseParams.unit_engineer_location);
        OkHttpUtils.Param idParam = new OkHttpUtils.Param(DataBaseParams.measure_project_id, String.valueOf(project_id));
        OkHttpUtils.Param locationParam =new OkHttpUtils.Param(DataBaseParams.unit_engineer_location, location);
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(idParam);
        paramList.add(locationParam);
        String url = NetConstant.baseUrl + NetConstant.group_add_unit_engineer_url;
        LogUtils.show("添加单位工程接口:" + url + ",参数:" + paramList);
        final ProjectGroupAnyMsgEvent event = new ProjectGroupAnyMsgEvent();
        event.setRequst_flag(3);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("添加单位工程-----打印查看返回的参数:"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        JSONObject dataJson = jsonObject.getJSONObject("data");
                        int unit_id = dataJson.getInt("unit_id");
                        RulerUnitEngineer unitEngineer = new RulerUnitEngineer();
                        String where = " where " + DataBaseParams.server_id + "=" + unit_id;
                        List<RulerCheckProject> projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), where);
                        if (projectList.size() > 0) {
                            unitEngineer.setProject_id(projectList.get(0).getId());
                        }
                        unitEngineer.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                        unitEngineer.setProject_server_id(project_id);
                        unitEngineer.setLocation(location);
                        unitEngineer.setServer_id(unit_id);
                        unitEngineer.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
                        int result = OperateDbUtil.addUnitPositionDataToSqlite(getApplicationContext(), unitEngineer);
                        unitEngineer.setId(result);
                        event.setSuccess(true);
                        event.setObject(unitEngineer);
                        EventBus.getDefault().post(event);
                    } else {
                        event.setSuccess(false);
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setMsg("数据解析失败");
                    event.setSuccess(false);
                    EventBus.getDefault().post(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                event.setMsg("网络请求失败");
                event.setSuccess(false);
                EventBus.getDefault().post(event);
            }
        },paramList);


    }
    /***************添加单位工程结束********************/

    /***********************TODO 添加成员接口开始*********************/
    private void addMemberToServer(Bundle bundle) {
        String mobile = bundle.getString(DataBaseParams.user_mobile);
        int project_id = bundle.getInt(DataBaseParams.measure_project_id);
        OkHttpUtils.Param mobleParam = new OkHttpUtils.Param(DataBaseParams.user_mobile, mobile);
        OkHttpUtils.Param idParam = new OkHttpUtils.Param(DataBaseParams.measure_project_id, String.valueOf(project_id));
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(mobleParam);
        paramList.add(idParam);
        String url = NetConstant.baseUrl + NetConstant.group_add_member_url;
        LogUtils.show("添加成员接口链接："+url+",参数："+paramList);
        final ProjectGroupAnyMsgEvent event = new ProjectGroupAnyMsgEvent();
        event.setRequst_flag(2);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("查看用户返回的信息："+response);
                /**
                 * {
                 "status": "success",
                 "code": "200",
                 "data": {
                 "group_id": 83626,
                 "name": "6q4hDxfzCA"
                 },
                 "msg": "添加成员成功"
                 }
                 */
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    String msg = object.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        JSONObject dataJson = object.getJSONObject("data");
                        int project_id = dataJson.getInt(DataBaseParams.measure_project_id);
                        ProjectUser projectUser = new ProjectUser();
                        projectUser.setServer_id(dataJson.optInt("group_id"));
                        projectUser.setUserName(dataJson.optString("name"));
                        Bundle newBun = new Bundle();
                        newBun.putInt(DataBaseParams.measure_project_id, project_id);
                        queryMemberAndUnit(newBun);
                        event.setObject(projectUser);
                        event.setSuccess(true);
                        EventBus.getDefault().post(event);
                    } else {
                        event.setSuccess(false);
                        EventBus.getDefault().post(event);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setMsg("数据解析失败");
                    event.setSuccess(false);
                    EventBus.getDefault().post(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                event.setMsg("网络请求失败");
                event.setSuccess(false);
                EventBus.getDefault().post(event);
            }
        },paramList);
    }
    /***********************加成员接口结束*********************/



    /***********************TODO 更新项目组接口开始************************/
    private void updateGroupProjectName(Bundle bundle) {
        final int project_id = bundle.getInt(DataBaseParams.measure_id);
        int user_id = bundle.getInt(DataBaseParams.user_user_id);
        final String project_name = bundle.getString(DataBaseParams.check_project_name);
        OkHttpUtils.Param idP = new OkHttpUtils.Param(DataBaseParams.measure_id, String.valueOf(project_id));
        OkHttpUtils.Param user_idP = new OkHttpUtils.Param(DataBaseParams.user_user_id, String.valueOf(user_id));
        OkHttpUtils.Param projectNameP = new OkHttpUtils.Param(DataBaseParams.check_project_name, project_name);
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(idP);
        paramList.add(user_idP);
        paramList.add(projectNameP);
        String url = NetConstant.baseUrl + NetConstant.group_update_proect_url;
        LogUtils.show("updateGroupProjectName---更新项目名称：" + url + ",参数：" + paramList);
        final ProjectGroupAnyMsgEvent event = new ProjectGroupAnyMsgEvent();
        event.setRequst_flag(1);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("updateGroupProjectName---更新项目名称,打印返回信息：" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        ContentValues values = new ContentValues();
                        values.put(DataBaseParams.check_project_name, project_name);
                        String where = DataBaseParams.server_id + " =?";
                        int result = OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(), DataBaseParams.check_project_table_name, where, values, new String[]{String.valueOf(project_id)});
                        LogUtils.show("查看更新返回值：" + result);

                        event.setSuccess(true);
                        EventBus.getDefault().post(event);

                    } else {
                        event.setSuccess(false);
                        EventBus.getDefault().post(event);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setMsg("数据解析失败");
                    event.setSuccess(false);
                    EventBus.getDefault().post(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                event.setMsg("网络请求失败");
                event.setSuccess(false);
                EventBus.getDefault().post(event);
            }
        },paramList);


    }
    /***********************更新项目组接口结束************************/



    /***********************TODO 查询项目组接口接口开始************************/
    private void queryProjectGroupFromServer(Bundle bundle) {
        final int user_id = bundle.getInt(DataBaseParams.user_user_id);
        int currPage = bundle.getInt(NetConstant.current_Page);
        int pageSize = bundle.getInt(NetConstant.page_size);
        StringBuffer urlString = new StringBuffer();
        urlString.append(NetConstant.baseUrl);
        urlString.append(NetConstant.group_get_project_list_url);
        urlString.append("?");
        urlString.append(NetConstant.current_Page);
        urlString.append("=");
        urlString.append(currPage);
        urlString.append("&");
        urlString.append(NetConstant.page_size);
        urlString.append("=");
        urlString.append(pageSize);
        urlString.append("&");
        urlString.append(DataBaseParams.user_user_id);
        urlString.append("=");
        urlString.append(user_id);
        LogUtils.show("queryProjectGroupFromServer----请求查询项目组信息接口："+urlString.toString());
        final QueryProjectGroupMsgEvent event = new QueryProjectGroupMsgEvent();
        OkHttpUtils.get(urlString.toString(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("queryProjectGroupFromServer----请求查询项目组信息接口返回的信息："+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        JSONArray dataArray = jsonObject.getJSONArray("data");
//                        String delw = DataBaseParams.user_user_id + "=? and "+DataBaseParams.server_id+"!=?";
//                        OperateDbUtil.delData(getApplicationContext(), DataBaseParams.check_project_table_name, delw, new String[]{String.valueOf(user_id),String.valueOf(0)});
                        if (dataArray.length() > 0) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataJson = dataArray.getJSONObject(i);
                                int project_server_id = dataJson.optInt("id");
                                String where = " where " + DataBaseParams.server_id + "=" + project_server_id;
                                List<RulerCheckProject> projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), where);
                                if (projectList.size() == 0) {
                                    RulerCheckProject project = new RulerCheckProject();
                                    project.setServer_id(project_server_id);
                                    project.setProjectName(dataJson.optString(DataBaseParams.check_project_name));
                                    project.setQrCode(dataJson.optString(DataBaseParams.check_project_qrcode));
                                    User user = new User();
                                    user.setUserID(user_id);
                                    project.setUser(user);
                                    project.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                    project.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
                                    int result = OperateDbUtil.addProjectNameDataToSqlite(getApplicationContext(), project);
                                    LogUtils.show("查看是否新增成功：" + result + "，" + project);
                                } else {
                                    if (!projectList.get(0).getProjectName().equals(dataJson.optString(DataBaseParams.check_project_name))) {
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.check_project_name, dataJson.optString(DataBaseParams.check_project_name));
                                        String upwhere = DataBaseParams.server_id + "=?";
                                        int res=OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(),DataBaseParams.project_user_table_name,upwhere, values, new String[]{String.valueOf(project_server_id)});
                                        LogUtils.show("查询项目数据接口---查看prject_server_id："+project_server_id+",查看更新返回值："+res);
                                    }
                                }

                            }
                        }
                        event.setSuccess(true);
                        EventBus.getDefault().post(event);
                    } else {
                        event.setSuccess(false);
                        EventBus.getDefault().post(event);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setSuccess(false);
                    event.setMsg("数据解析失败");
                    EventBus.getDefault().post(event);
                }

            }

            @Override
            public void onFailure(Exception e) {
                event.setSuccess(false);
                event.setMsg("网络请求失败");
                EventBus.getDefault().post(event);
            }
        });
    }
    /***********************查询项目组接口结束************************/


    /***********************查询测量组成员和单位工程接口开始************************/
    private void queryMemberAndUnit(Bundle bundle) {
        int project_id = bundle.getInt(DataBaseParams.measure_project_id);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(NetConstant.baseUrl);
        stringBuffer.append(NetConstant.group_get_member_and_unit_url);
        stringBuffer.append("?");
        stringBuffer.append(DataBaseParams.measure_project_id);
        stringBuffer.append("=");
        stringBuffer.append(project_id);
        LogUtils.show("queryMemberAndUnit----查询测量组成员和单位工程---链接：" + stringBuffer.toString());
        final GetMemberAndUnitMsgEvent event = new GetMemberAndUnitMsgEvent();
        OkHttpUtils.get(stringBuffer.toString(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("queryMemberAndUnit----查询测量组成员和单位工程,返回值：：" +response);
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.optInt("code");
                    String msg = object.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        JSONObject dataJson = object.getJSONObject("data");
                        int project_server_id = dataJson.optInt("id");
                        /************初始化项目对象************/
                        String projectWhere = " where " + DataBaseParams.server_id + "=" + project_server_id;
                        List<RulerCheckProject> projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), projectWhere);
                        RulerCheckProject checkProject = new RulerCheckProject();
                        if (projectList.size() > 0) {
                            checkProject = projectList.get(0);
                        } else {
                            checkProject.setServer_id(project_server_id);
                            checkProject.setId(0);
                        }
                        /****************解析成员数组group_member*******************/
                        /**
                         *  "group_member": [
                         {
                         "id": 2,
                         "project_id": 18,
                         "user_id": 2,
                         "create_time": "2019-01-11 14:48:40",
                         "update_time": "1547196167571",
                         "name": "123",
                         "cid": 4
                         },
                         */
                        JSONArray groupArray = dataJson.getJSONArray("group_member");
                        if (groupArray.length() > 0) {
                            String delw = DataBaseParams.project_server_id + "=?";
//                            OperateDbUtil.delData(getApplicationContext(), DataBaseParams.project_user_table_name, delw, new String[]{String.valueOf(project_server_id)});
                            for (int i=0;i<groupArray.length();i++) {

                                JSONObject userJson = groupArray.getJSONObject(i);
                                int server_id = userJson.optInt(DataBaseParams.measure_id);
                                int user_id = userJson.optInt(DataBaseParams.user_user_id);
                                String userWhere = " where " + DataBaseParams.server_id + "=" + server_id;
                                List<ProjectUser> userList = OperateDbUtil.queryProjectUserFromSqlite(getApplicationContext(), userWhere);
                                if (userList.size() > 0) {
//                                    continue;
                                    if (!userList.get(0).getUserName().equals(userJson.optString("name"))) {
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.user_user_name, userJson.optString("name"));
                                        String upwhere = DataBaseParams.server_id + "=?";
                                        int res=OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(),DataBaseParams.project_user_table_name,upwhere, values, new String[]{String.valueOf(server_id)});
                                        LogUtils.show("更新用户数据接口---查看group_id："+server_id+",查看更新返回值："+res);
                                    }
                                    continue;
                                }
                                ProjectUser user = new ProjectUser();
                                user.setServer_id(server_id);
                                user.setUser_id(user_id);
                                user.setcId(userJson.optInt("cid"));
                                user.setProjectId(checkProject.getId());
                                user.setProjectServerId(project_server_id);
                                user.setUserName(userJson.optString("name"));
                                user.setPosition("");
                                user.setMobile("");
                                OperateDbUtil.addProjectUserToSqlite(getApplicationContext(), user);
                            }
                        }

                        /*****************解析单位工程信息*******************/
                        JSONArray unitArray = dataJson.getJSONArray("unit_engineer");
                        if (unitArray.length() > 0) {
//                            String delw = DataBaseParams.project_server_id + "=? and "+DataBaseParams.server_id+"!=?";
//                            OperateDbUtil.delData(getApplicationContext(), DataBaseParams.unit_engineer_table_name, delw, new String[]{String.valueOf(project_server_id),String.valueOf(0)});
                            for (int i=0;i<unitArray.length();i++) {
                                JSONObject unitJson = unitArray.getJSONObject(i);
                                int server_id = unitJson.optInt("id");
                                String unitWhere = " where " + DataBaseParams.server_id + "=" + server_id;
                                List<RulerUnitEngineer> unitList = OperateDbUtil.queryUnitEngineerDataFromSqlite(getApplicationContext(), unitWhere);
                                if (unitList.size() > 0) {
                                    if (!unitList.get(0).getLocation().equals(unitJson.optString(DataBaseParams.unit_engineer_location))) {
                                        ContentValues values = new ContentValues();
                                        values.put(DataBaseParams.unit_engineer_location, unitJson.optString(DataBaseParams.unit_engineer_location));
                                        String upwhere = DataBaseParams.server_id + "=?";
                                        int res=OperateDbUtil.updateOptionsDataToSqlite(getApplicationContext(),DataBaseParams.unit_engineer_table_name,upwhere, values, new String[]{String.valueOf(server_id)});
                                        LogUtils.show("查找单位工程数据接口---查看unit_id："+server_id+",查看更新返回值："+res);
                                    }
                                    continue;
                                }
                                RulerUnitEngineer unit = new RulerUnitEngineer();
                                unit.setServer_id(server_id);
                                unit.setLocation(unitJson.optString("location"));
                                unit.setProject_server_id(unitJson.optInt("check_project_id"));
                                unit.setProject_id(checkProject.getId());
                                OperateDbUtil.addUnitPositionDataToSqlite(getApplicationContext(), unit);
                            }
                        }
                        event.setSuccess(true);
                        EventBus.getDefault().post(event);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setSuccess(false);
                    event.setMsg("数据解析失败");
                    EventBus.getDefault().post(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                event.setSuccess(false);
                event.setMsg("网络请求失败");
                EventBus.getDefault().post(event);
            }
        });
    }
    /***********************查询测量组成员和单位工程接口结束************************/



    /************************TODO 创建测量组接口开始******************/
    private void requestCreateProecjt(Bundle bundle) {
        final String proejct_name = bundle.getString(DataBaseParams.check_project_name);
        final String user_id = bundle.getString(DataBaseParams.user_user_id);
        OkHttpUtils.Param param = new OkHttpUtils.Param(DataBaseParams.check_project_name, proejct_name);
        OkHttpUtils.Param userParam = new OkHttpUtils.Param(DataBaseParams.user_user_id, user_id);
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        paramList.add(userParam);
        String url = NetConstant.baseUrl + NetConstant.group_create_project_url;
        LogUtils.show("requestCreateProecjt---创建测量组接口--链接："+url+",参数:"+paramList);
        final MoblieRequestResutEvent event = new MoblieRequestResutEvent();
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("requestCreateProecjt---创建测量组接口---打印返回的信息："+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");

                    if (code == 200) {
                        JSONObject dataJson = jsonObject.getJSONObject("data");
                        RulerCheckProject project = new RulerCheckProject();
                        User user = new User();
                        user.setUserID(Integer.parseInt(user_id));
                        project.setUser(user);
                        project.setProjectName(proejct_name);
                        project.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
                        project.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                        project.setServer_id(dataJson.optInt(DataBaseParams.measure_project_id));
//                        project.setServer_id();
                        OperateDbUtil.addProjectNameDataToSqlite(getApplicationContext(), project);
                        Bundle bundle1 = new Bundle();
                        bundle1.getInt(DataBaseParams.measure_project_id,dataJson.optInt(DataBaseParams.measure_project_id));
                        queryMemberAndUnit(bundle1);
                        event.setSuccess(true);
                        event.setMsg(msg);

                        EventBus.getDefault().post(event);
                    } else {
                        event.setSuccess(false);
                        event.setMsg(msg);
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setSuccess(false);
                    event.setMsg("数据解析失败");
                    EventBus.getDefault().post(event);
                }

            }

            @Override
            public void onFailure(Exception e) {
                event.setSuccess(false);
                event.setMsg("网络请求失败");
                EventBus.getDefault().post(event);
            }
        },paramList);

    }
    /************************创建测量组接口结束*************************/

}
