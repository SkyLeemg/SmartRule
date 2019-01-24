package com.vitec.task.smartrule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.vitec.task.smartrule.bean.CompanyMessage;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.ProjectGroup;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;
import com.vitec.task.smartrule.utils.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OperateDbUtil {


    private static final String TAG = "OperateDbUtil";
    /**
     * 添加工程模板和管控要点模板到sqlite
     * @param context
     * @param engineerList
     */
    public static void addEngineerMudelData(Context context, List<RulerEngineer> engineerList) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
       LogUtils.show("addEngineerMudelData: 保存模板数据到数据库："+engineerList.toString() );
        for (int i = 0; i < engineerList.size(); i++) {
//            先存储工程（iot_ruler_engineer）表格的数据
            ContentValues values = new ContentValues();
            RulerEngineer rulerEngineer = engineerList.get(i);
            values.put(DataBaseParams.server_id, rulerEngineer.getServerID());
            values.put(DataBaseParams.enginer_name, rulerEngineer.getEngineerName());
            values.put(DataBaseParams.enginer_create_name, rulerEngineer.getCreateTime());
            values.put(DataBaseParams.enginer_description, rulerEngineer.getEngineerDescription());
            values.put(DataBaseParams.enineer_options_choose,rulerEngineer.getChooseOptions());
            boolean isSuccess = bleDataDbHelper.insertDevToSqlite(DataBaseParams.engineer_table_name, values);
            LogUtils.show("addEngineerMudelData: 查看工程模板是否保存成功：" + isSuccess+",数据内容："+values.toString());
        }
        bleDataDbHelper.close();
    }


    /**
     * 添加工程模板和管控要点模板到sqlite
     * @param context
     * @param rulerOptions
     */
    public static void addOptionsMudelData(Context context, List<RulerOptions> rulerOptions) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
//        Log.e(TAG, "addOptionsMudelData: 保存模板数据到数据库："+rulerOptions.toString() );
        for (int i = 0; i < rulerOptions.size(); i++) {
//            先存储工程（iot_ruler_engineer）表格的数据
//            RulerEngineer rulerEngineer = rulerOptions.get(i).getEngineer();
            RulerOptions option = rulerOptions.get(i);
//                List<OptionBean> optionBeans = bean.getMeasureBeanList();
//                for (OptionBean optionBean : optionBeans) {
            ContentValues optionsValues = new ContentValues();
            optionsValues.put(DataBaseParams.server_id, option.getServerID());
            optionsValues.put(DataBaseParams.options_name, option.getOptionsName());
            optionsValues.put(DataBaseParams.options_standard, option.getStandard());
            optionsValues.put(DataBaseParams.options_methods, option.getMethods());
            optionsValues.put(DataBaseParams.options_type,option.getType());
//            optionsValues.put(DataBaseParams.options_engin_id, rulerEngineer.getServerID());
            optionsValues.put(DataBaseParams.options_create_time,option.getCreateTime());
            optionsValues.put(DataBaseParams.options_measure, option.getMeasure());
            boolean flag = bleDataDbHelper.insertDevToSqlite(DataBaseParams.options_table_name, optionsValues);
//            Log.e(TAG, "addOptionsMudelData: 查看管控要点是否添加成功："+flag+"," + optionsValues);
//                }
        }
        bleDataDbHelper.close();
    }

    /**
     * 点击进入测量后，添加一个测量表格的数据到Iot_ruler_check表
     * 相当于创建了表格的表头信息
     * @param context
     * @param rulerCheck
     * @return -1添加失败，0查找id失败，>0返回正确的id
     */
    public static int addMeasureDataToSqlite(Context context,RulerCheck rulerCheck) {
//        Log.e(TAG, "addMeasureDataToSqlite: 查看数据库添加这里收到的rulercheck："+rulerCheck.toString() );
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.measure_engin_id,rulerCheck.getEngineer().getServerID());
        values.put(DataBaseParams.measure_check_floor, rulerCheck.getCheckFloor());
        values.put(DataBaseParams.measure_create_date,rulerCheck.getCreateDate());
        values.put(DataBaseParams.measure_create_time,rulerCheck.getCreateTime());
        values.put(DataBaseParams.measure_update_time, rulerCheck.getUpdateTime());
        values.put(DataBaseParams.measure_user_id,rulerCheck.getUser().getUserID());
        values.put(DataBaseParams.measure_project_id, rulerCheck.getProject().getId());
        values.put(DataBaseParams.measure_unit_id, rulerCheck.getUnitEngineer().getId());
        values.put(DataBaseParams.project_server_id, rulerCheck.getProject().getServer_id());
        values.put(DataBaseParams.upload_flag,rulerCheck.getUpload_flag());
        values.put(DataBaseParams.measure_is_finish,rulerCheck.getStatus());
        values.put(DataBaseParams.server_id,rulerCheck.getServerId());
        boolean flag = bleDataDbHelper.insertDevToSqlite(DataBaseParams.measure_table_name, values);

        Log.e(TAG, "addMeasureDataToSqlite: 查看测量表头是否添加成功："+flag+",查看内容：" +values);
//        如果添加成功，则返回添加的哪一行的id
        if (flag) {
            String where = "where " + DataBaseParams.measure_engin_id + "=" + rulerCheck.getEngineer().getServerID() + " AND " +
                    DataBaseParams.measure_user_id + "=" + rulerCheck.getUser().getUserID()+" AND " +
                    DataBaseParams.measure_project_id+"= \""+rulerCheck.getProject().getId()+"\" AND "+
                    DataBaseParams.measure_unit_id+"= \""+rulerCheck.getUnitEngineer().getId()+"\" AND "+
                    DataBaseParams.measure_check_floor+"= \""+rulerCheck.getCheckFloor()+"\" ;"
                    ;
//            Log.e(TAG, "addMeasureDataToSqlite: 查看where条件：" + where);
            Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite("id", where);

            int resultId = 0;
            if (cursor.moveToFirst()) {
                do {
                    resultId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                } while (cursor.moveToNext());
            }
            bleDataDbHelper.close();
            return resultId;
        }
        bleDataDbHelper.close();
        return -1;
    }

    /**
     * 添加要进行测量的管控要点，
     * @param context
     * @param rulerCheckOption
     * @return 返回新增的id号
     */
    public static int  addMeasureOptionsDataToSqlite(Context context,RulerCheckOptions rulerCheckOption) {
        int resultId = 0;
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
//        List<OptionBean> optionlist = engineerBean.getMeasureBeanList();
//        for (int i=0;i<optionlist.size();i++) {
        ContentValues values = new ContentValues();
//            OptionBean option = optionlist.get(i);
        values.put(DataBaseParams.server_id, rulerCheckOption.getServerId());
        values.put(DataBaseParams.measure_option_check_id, rulerCheckOption.getRulerCheck().getId());
        values.put(DataBaseParams.measure_option_options_id, rulerCheckOption.getRulerOptions().getId());
        values.put(DataBaseParams.upload_flag,rulerCheckOption.getUpload_flag());
        values.put(DataBaseParams.measure_option_floor_height, rulerCheckOption.getFloorHeight().getId());
        values.put(DataBaseParams.measure_option_measured_points,rulerCheckOption.getMeasuredNum());
        values.put(DataBaseParams.measure_option_qualified_points, rulerCheckOption.getQualifiedNum());
        values.put(DataBaseParams.measure_option_percent_pass,rulerCheckOption.getQualifiedRate());
        values.put(DataBaseParams.options_create_time, rulerCheckOption.getCreateTime());
        values.put(DataBaseParams.measure_option_img_time,rulerCheckOption.getImgUpdateTime());
        values.put(DataBaseParams.measure_option_img_upload_flag,rulerCheckOption.getImg_upload_flag());
        values.put(DataBaseParams.measure_option_img_path, rulerCheckOption.getImgPath());
        values.put(DataBaseParams.measure_option_img_number,rulerCheckOption.getImgNumber());
        boolean isSuccess = bleDataDbHelper.insertDevToSqlite(DataBaseParams.measure_option_table_name, values);

            if (isSuccess) {
                /**
                 * 查找刚添加的管控要点的id
                 */
                String where = " where " + DataBaseParams.measure_option_check_id + "=" + rulerCheckOption.getRulerCheck().getId()+" ;";
//                Log.e(TAG, "添加管控要点: 查看where条件：" + where);
                Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.measure_option_table_name,"id", where);

                if (cursor.moveToFirst()) {
                    do {
                        resultId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                    } while (cursor.moveToNext());
                }
            }
//        }
        bleDataDbHelper.close();
        return resultId;
    }

    public static int addRealMeasureDataToSqlite(Context context,RulerCheckOptionsData data) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.options_data_check_options_id, data.getRulerCheckOptions().getId());
        values.put(DataBaseParams.options_data_content, data.getData());
        values.put(DataBaseParams.options_data_create_time, data.getCreateTime());
        values.put(DataBaseParams.options_data_update_flag, data.getUpdateFlag());
        values.put(DataBaseParams.server_id,data.getServerId());
        values.put(DataBaseParams.upload_flag,data.getUpload_flag());
        values.put(DataBaseParams.options_data_number,data.getNumber());
        boolean isSuccess = bleDataDbHelper.insertDevToSqlite(DataBaseParams.options_data_table_name, values);
        String where = " where " + DataBaseParams.options_data_check_options_id + " = " + data.getRulerCheckOptions().getId() + " and " + DataBaseParams.options_data_content + " = \""+
                data.getData()+"\" and "+DataBaseParams.options_data_create_time+" = " +data.getCreateTime();
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.options_data_table_name, " * " ,where);
        int index = 0;
        if (cursor.moveToFirst()) {
            do {
                index = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        bleDataDbHelper.close();
//        LogUtils.show("保存测量数据------数据ID："+index+",对应的管控要点ID："+data.getRulerCheckOptions().getId());
        return index;
    }


    /**
     * 添加项目名到数据库
     * @param context
     * @param project
     * @return
     */
    public static int addProjectNameDataToSqlite(Context context, RulerCheckProject project) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.check_project_name, project.getProjectName());
        values.put(DataBaseParams.options_create_time, project.getCreateTime());
        values.put(DataBaseParams.options_update_time, project.getUpdateTime());
        values.put(DataBaseParams.server_id, project.getServer_id());
        values.put(DataBaseParams.user_user_id,project.getUser().getUserID());
        values.put(DataBaseParams.check_project_qrcode, project.getQrCode());
        boolean result = bleDataDbHelper.insertDevToSqlite(DataBaseParams.check_project_table_name, values);
        int index = 0;
        if (result) {
            String where = " where " + DataBaseParams.check_project_name + " = \"" + project.getProjectName() + "\"" +
                    " AND " + DataBaseParams.measure_create_time + " = " + project.getCreateTime();
            Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.check_project_table_name, " * " ,where);
            if (cursor.moveToFirst()) {
                do {
                    index = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        bleDataDbHelper.close();

        return index;
    }


    public static int addUnitPositionDataToSqlite(Context context, RulerUnitEngineer unitEngineer) {
        int index = 0;
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.unit_engineer_location, unitEngineer.getLocation());
        values.put(DataBaseParams.options_create_time, unitEngineer.getCreateTime());
        values.put(DataBaseParams.options_update_time, unitEngineer.getUpdateTime());
        values.put(DataBaseParams.measure_project_id,unitEngineer.getProject_id());
        values.put(DataBaseParams.server_id,unitEngineer.getServer_id());
        values.put(DataBaseParams.project_server_id, unitEngineer.getProject_server_id());
        boolean result = bleDataDbHelper.insertDevToSqlite(DataBaseParams.unit_engineer_table_name, values);
        if (result) {
            String where = " where " + DataBaseParams.unit_engineer_location + " = \"" + unitEngineer.getLocation() + "\"" +
                    " AND " + DataBaseParams.measure_create_time + " = " + unitEngineer.getCreateTime()+
                    " AND " + DataBaseParams.options_update_time + " = " + unitEngineer.getUpdateTime()+
                    " AND " + DataBaseParams.measure_project_id + " = " + unitEngineer.getProject_id();
            Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.unit_engineer_table_name, " * " ,where);
            if (cursor.moveToFirst()) {
                do {
                    index = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        bleDataDbHelper.close();
        return index;
    }

    /**
     * 添加测量组成员到数据库
     */
    public static int addProjectUserToSqlite(Context context, ProjectUser user) {
        int index = 0;
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.user_user_id, user.getUser_id());
        values.put(DataBaseParams.user_mobile, user.getMobile());
        values.put(DataBaseParams.user_user_name, user.getUserName());
//        values.put(DataBaseParams.user_position, user.getPosition());
        values.put(DataBaseParams.measure_project_id, user.getProjectId());
        values.put(DataBaseParams.cid, user.getcId());
        values.put(DataBaseParams.server_id,user.getServer_id());
        values.put(DataBaseParams.project_server_id, user.getProjectServerId());
        boolean result = bleDataDbHelper.insertDevToSqlite(DataBaseParams.project_user_table_name, values);
        if (result) {
            String where = " where " + DataBaseParams.user_user_name + " = \"" + user.getUserName() + "\"" +
                    " AND " + DataBaseParams.user_mobile + " = \"" + user.getMobile() + "\"" +
//                    " AND " + DataBaseParams.user_position + " = \"" + user.getPosition() + "\"" +
                    " AND " + DataBaseParams.user_user_id + " = " + user.getUser_id() +
                    " AND " + DataBaseParams.server_id + " = " + user.getServer_id() +
                    " AND " + DataBaseParams.measure_project_id + " = " + user.getProjectId() +
                    " AND " + DataBaseParams.project_server_id + " = " + user.getProjectServerId();
            Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.project_user_table_name, " * " ,where);
            if (cursor.moveToFirst()) {
                do {
                    index = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        bleDataDbHelper.close();

        return index;
    }


    public static int addCompanyToSqlite(Context context, CompanyMessage companyMessage) {
        int index = 0;
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.server_id,companyMessage.getServer_id());
        values.put(DataBaseParams.iot_company_title, companyMessage.getName());
        values.put(DataBaseParams.iot_company_content, companyMessage.getContent());
        boolean result = bleDataDbHelper.insertDevToSqlite(DataBaseParams.iot_company_table_name, values);
        if (result) {
            String where = " where "
                    + DataBaseParams.iot_company_content + " = \"" + companyMessage.getContent() + "\"" +
                    " AND " + DataBaseParams.iot_company_title + " = \"" + companyMessage.getName() + "\"" +
                    " AND " + DataBaseParams.server_id + " = " + companyMessage.getServer_id();
            Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.iot_company_table_name, " * " ,where);
            if (cursor.moveToFirst()) {
                do {
                    index = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        bleDataDbHelper.close();

        return index;

    }


    /**
     * 将项目添加到 用户项目表，用户所在的项目集合
     * @param context
     * @param project
     * @return
     */
    public static int addProjectGroupToSqlite(Context context, ProjectGroup project) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.server_id, project.getServer_id());
        values.put(DataBaseParams.user_user_id,project.getUser_id());
        values.put(DataBaseParams.measure_project_id,project.getProject_id());
        values.put(DataBaseParams.project_server_id,project.getProject_server_id());
        boolean result = bleDataDbHelper.insertDevToSqlite(DataBaseParams.iot_ruler_check_group_table_name, values);
        int index = 0;
        if (result) {
            String where = " where " + DataBaseParams.server_id + " = " + project.getServer_id() +
                    " AND " + DataBaseParams.user_user_id + " = " + project.getUser_id()+
                    " AND " + DataBaseParams.measure_project_id + " = " + project.getProject_id()+
                    " AND " + DataBaseParams.project_server_id + " = " + project.getProject_server_id();
            Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.iot_ruler_check_group_table_name, " * " ,where);
            if (cursor.moveToFirst()) {
                do {
                    index = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        bleDataDbHelper.close();

        return index;
    }

    /**
     *  根据成员表搜索出所有的项目
     * @param context
     * @return
     */
    public static List<RulerCheckProject> queryAllProjectOrderMember(Context context,int user_id) {
        List<RulerCheckProject> projectList = new ArrayList<>();
        String where = " where " + DataBaseParams.user_user_id + "=" + user_id+"  ORDER BY id DESC";
        List<ProjectUser> memberList = queryProjectUserFromSqlite(context, where);
        for (int i=0;i<memberList.size();i++) {
            String pWhere;
            if (memberList.get(i).getProjectServerId() != 0) {
                pWhere = " where " + DataBaseParams.server_id + "=" + memberList.get(i).getProjectServerId() + " or " + DataBaseParams.measure_id + "=" + memberList.get(i).getProjectId();
            }else{
                pWhere = " where " +  DataBaseParams.measure_id + "=" + memberList.get(i).getProjectId();
            }
            List<RulerCheckProject> list = queryProjectDataFromSqlite(context, pWhere);
            if (list.size() > 0) {
                LogUtils.show("根据成员表查询所有项目----查看当前成员查询出来的项目：" +list.get(0).getProjectName() + ",查看条件语句：" + pWhere);
            }

            projectList.addAll(list);
        }
        return projectList;
    }

//    public static List<ProjectGroup> queryProjectGroupFromSqlit(Context context, String where) {
//        List<ProjectGroup> groupList = new ArrayList<>();
//        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
//        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.iot_ruler_check_group_table_name, " * ", where);
//        if (cursor.moveToFirst()) {
//            do {
//                ProjectGroup group = new ProjectGroup();
//                group.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
//                group.setProject_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_project_id)));
//                group.setProject_server_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.project_server_id)));
//                group.setUser_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.user_user_id)));
//                groupList.add(group);
//            } while (cursor.moveToNext());
//        }
//        bleDataDbHelper.close();
//        return groupList;
//    }
//
//
    public static List<CompanyMessage> queryCompanyMsgFromSqlite(Context context, String where) {
        List<CompanyMessage> companyMessageList = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.iot_company_table_name, " * ", where);
        if (cursor.moveToFirst()) {
            do {
                CompanyMessage message = new CompanyMessage();
                message.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                message.setServer_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                message.setContent(cursor.getString(cursor.getColumnIndex(DataBaseParams.iot_company_content)));
                message.setName(cursor.getString(cursor.getColumnIndex(DataBaseParams.iot_company_title)));
                companyMessageList.add(message);
            } while (cursor.moveToNext());
        }
        bleDataDbHelper.close();
        return companyMessageList;
    }


    public static List<ProjectUser> queryProjectUserFromSqlite(Context context, String where) {
        List<ProjectUser> users = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.project_user_table_name, " * ", where);
        if (cursor.moveToFirst()) {
            do {
                ProjectUser user = new ProjectUser();
                user.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                user.setMobile(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_mobile)));
                user.setProjectId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_project_id)));
                user.setProjectServerId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.project_server_id)));
                user.setUserName(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_user_name)));
                user.setServer_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                user.setUser_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.user_user_id)));
                user.setcId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.cid)));
                users.add(user);
            } while (cursor.moveToNext());
        }
        bleDataDbHelper.close();
        return users;
    }


    /**
     * 获取项目表的数据
     * @param context
     * @param where
     * @return
     */
    public static List<RulerCheckProject> queryProjectDataFromSqlite(Context context, String where) {
        List<RulerCheckProject> projectList = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.check_project_table_name, " * ", where);
        if (cursor.moveToFirst()) {
            do {
                RulerCheckProject project = new RulerCheckProject();
                project.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                project.setServer_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                project.setProjectName(cursor.getString(cursor.getColumnIndex(DataBaseParams.check_project_name)));
                project.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_create_time)));
                project.setUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_update_time)));
                project.setQrCode(cursor.getString(cursor.getColumnIndex(DataBaseParams.check_project_qrcode)));
                project.setUser(getUser(context));
//                查询当前项目对应的单位工程
                String unitWhere;

                if (project.getServer_id() > 0) {
                    unitWhere = " where (" + DataBaseParams.measure_project_id + "=" + project.getId() + " or " +
                            DataBaseParams.project_server_id + "=" + project.getServer_id() + ") and " + DataBaseParams.delete_flag + "=0";
                } else {
                    unitWhere = " where " + DataBaseParams.measure_project_id + "=" + project.getId() + " and " + DataBaseParams.delete_flag + "=0";
                }
                List<RulerUnitEngineer> unitList = queryUnitEngineerDataFromSqlite(context, unitWhere);
                project.setUnitList(unitList);
                projectList.add(project);
//                LogUtils.show("查找出的项目信息----"+project);
            } while (cursor.moveToNext());
        }
        bleDataDbHelper.close();
        return projectList;
    }

    public static List<RulerUnitEngineer> queryUnitEngineerDataFromSqlite(Context context, String where) {
        List<RulerUnitEngineer> unitList = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.unit_engineer_table_name, " * ", where);
        if (cursor.moveToFirst()) {
            do {
                RulerUnitEngineer unit = new RulerUnitEngineer();
                unit.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                unit.setServer_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                unit.setLocation(cursor.getString(cursor.getColumnIndex(DataBaseParams.unit_engineer_location)));
                unit.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_create_time)));
                unit.setUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_update_time)));
                unit.setProject_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_project_id)));
                unit.setProject_server_id(cursor.getInt(cursor.getColumnIndex(DataBaseParams.project_server_id)));
//                LogUtils.show("查找出的单位工程----"+unit);
                unitList.add(unit);
            } while (cursor.moveToNext());
        }
        bleDataDbHelper.close();
        return unitList;
    }


    /**
     * 查询RulerCheckOptionData表格
     * @param context
     * @param checkOption
     * @return
     */
    public static List<RulerCheckOptionsData> queryMeasureDataFromSqlite(Context context, RulerCheckOptions checkOption) {
        List<RulerCheckOptionsData> checkOptionsDataList = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        String where = " where " + DataBaseParams.options_data_check_options_id + "=" + checkOption.getId();
        StringBuffer sb = new StringBuffer();
        sb.append(where);
        sb.append(" ");
        sb.append(" ORDER BY number ASC ");
//        Log.e(TAG, "queryMeasureDataFromSqlite: 查看option_data表格中的where语句："+ where);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.options_data_table_name,"*", sb.toString());
        if (cursor.moveToFirst()) {
            do {
//                MeasureData data = new MeasureData();
                RulerCheckOptionsData data = new RulerCheckOptionsData();
                data.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                data.setRulerCheckOptions(checkOption);
                data.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_create_time)));
                data.setData(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_data_content)));
                data.setUpdateFlag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_update_flag)));
                data.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                data.setNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_number)));

                checkOptionsDataList.add(data);
            } while (cursor.moveToNext());
        }
//        Log.e(TAG, "queryMeasureDataFromSqlite: 查看返回的数据内容："+checkOptionsDataList );
        bleDataDbHelper.close();
        return checkOptionsDataList;
    }

    /**
     * 查询RulerCheckOptionData表格
     * @param context
     * @param where
     * @return
     */
    public static List<RulerCheckOptionsData> queryMeasureDataFromSqlite(Context context,RulerCheckOptions checkOption, String where) {
        List<RulerCheckOptionsData> checkOptionsDataList = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        StringBuffer sb = new StringBuffer();
        sb.append(where);
        sb.append(" ");
        sb.append(" ORDER BY number ASC ");
//        Log.e(TAG, "queryMeasureDataFromSqlite: 查看option_data表格中的where语句："+ where);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.options_data_table_name,"*", sb.toString());
        if (cursor.moveToFirst()) {
            do {
//                MeasureData data = new MeasureData();
                RulerCheckOptionsData data = new RulerCheckOptionsData();
                data.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                checkOption.setId(cursor.getColumnIndex(DataBaseParams.options_data_check_options_id));
                data.setRulerCheckOptions(checkOption);
                data.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_create_time)));
                data.setData(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_data_content)));
                data.setUpdateFlag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_update_flag)));
                data.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                data.setNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_number)));
                checkOptionsDataList.add(data);
            } while (cursor.moveToNext());
        }
//        Log.e(TAG, "queryMeasureDataFromSqlite: 查看返回的数据内容："+checkOptionsDataList );
        bleDataDbHelper.close();
        return checkOptionsDataList;
    }

    /**
     * 查询RulerCheckOptionData表格
     * @param context
     * @param where
     * @return
     */
    public static List<RulerCheckOptionsData> queryMeasureDataFromSqlite(Context context, String where) {
        List<RulerCheckOptionsData> checkOptionsDataList = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        StringBuffer sb = new StringBuffer();
        sb.append(where);
        sb.append(" ");
        sb.append(" ORDER BY number ASC ");
//        Log.e(TAG, "queryMeasureDataFromSqlite: 查看option_data表格中的where语句："+ where);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.options_data_table_name,"*", sb.toString());
        if (cursor.moveToFirst()) {
            do {
//                MeasureData data = new MeasureData();
                RulerCheckOptionsData data = new RulerCheckOptionsData();
                data.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
//                LogUtils.show("查看搜索到的options_data_check_options_id：" + cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_check_options_id)));
                String optionWhere = " where id =" + cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_check_options_id));
                List<RulerCheckOptions> optionsList = queryCheckOptionFromSqlite(context, optionWhere);
                if (optionsList.size() > 0) {
                    data.setRulerCheckOptions(optionsList.get(0));
                } else {
                    RulerCheckOptions checkOption = new RulerCheckOptions();
                    data.setRulerCheckOptions(checkOption);
                }

                data.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_create_time)));
                data.setData(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_data_content)));
                data.setUpdateFlag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_update_flag)));
                data.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                data.setNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_number)));
                checkOptionsDataList.add(data);
            } while (cursor.moveToNext());
        }
//        Log.e(TAG, "queryMeasureDataFromSqlite: 查看返回的数据内容："+checkOptionsDataList );
        bleDataDbHelper.close();
        return checkOptionsDataList;
    }


    public static void queryDataFromSqlite(Context context, String tableName) {

    }

    /**
     *
     * @param context
     * @param rulerCheck
     * @return
     */
    public static List<RulerCheckOptions> queryCheckOptionFromSqlite(Context context, RulerCheck rulerCheck) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        List<RulerCheckOptions> checkOptionsList = new ArrayList<>();
        String where = " where " + DataBaseParams.measure_option_check_id + "=" + rulerCheck.getId() ;
//        Log.e(TAG, "添加管控要点: 查看where条件：" + where);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.measure_option_table_name, " * ", where);
        int index = 0;
        int resultId = 0;
        if (cursor.moveToFirst()) {
//            Log.e(TAG, "queryData: 不是新创建的");
            do {
                RulerCheckOptions checkOption = new RulerCheckOptions();
                checkOption.setRulerCheck(rulerCheck);
                checkOption.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_create_time)));
                checkOption.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                int optionId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_options_id));
                checkOption.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                checkOption.setServerId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                checkOption.setMeasuredNum(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_measured_points)));
                checkOption.setQualifiedNum(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_qualified_points)));
                checkOption.setQualifiedRate(cursor.getFloat(cursor.getColumnIndex(DataBaseParams.measure_option_percent_pass)));
//                checkOption.setFloorHeight(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height)));
                checkOption.setImg_upload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_upload_flag)));
                checkOption.setImgPath(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_img_path)));
                checkOption.setImgUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_time)));
                checkOption.setImgNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_number)));
//                checkOption.setFloorHeight(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height)));

//                根据optionid查询iot_ruler_options模板表里对应的数据
                List<RulerOptions> optionsList = bleDataDbHelper.queryOptionsAllDataFromSqlite(" where id=" + optionId);
                if (optionsList.size() > 0) {
                    checkOption.setRulerOptions(optionsList.get(0));
                }

                List<OptionMeasure> measureList = OptionsMeasureUtils.getOptionMeasure(checkOption.getRulerOptions().getMeasure());
                if (measureList.size() > 0) {
                    for (OptionMeasure measure : measureList) {
                        checkOption.setFloorHeight(measure);
                        if (measure.getId() == cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height))) {
                            break;
                        }
                    }
                }

                checkOptionsList.add(checkOption);
//                Log.e(TAG, "queryData: 查询历史的RulerCheckOption:"+checkOption.toString() );
            } while (cursor.moveToNext());
        }

        return checkOptionsList;
    }


    public static List<RulerCheckOptions> queryCheckOptionFromSqlite(Context context, RulerCheck rulerCheck,String where) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        List<RulerCheckOptions> checkOptionsList = new ArrayList<>();
//        Log.e(TAG, "添加管控要点: 查看where条件：" + where);

        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.measure_option_table_name, " * ", where);
        int index = 0;
        int resultId = 0;
        if (cursor.moveToFirst()) {
//            Log.e(TAG, "queryData: 不是新创建的");
            do {
                RulerCheckOptions checkOption = new RulerCheckOptions();
                checkOption.setRulerCheck(rulerCheck);
                checkOption.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_create_time)));
                checkOption.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                int optionId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_options_id));
                checkOption.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                checkOption.setServerId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                checkOption.setMeasuredNum(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_measured_points)));
                checkOption.setQualifiedNum(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_qualified_points)));
                checkOption.setQualifiedRate(cursor.getFloat(cursor.getColumnIndex(DataBaseParams.measure_option_percent_pass)));
                checkOption.setImg_upload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_upload_flag)));
//                checkOption.setFloorHeight(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height)));
                checkOption.setImgPath(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_img_path)));
                checkOption.setImgNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_number)));
                checkOption.setImgUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_time)));
//                根据optionid查询iot_ruler_options模板表里对应的数据
                List<RulerOptions> optionsList = bleDataDbHelper.queryOptionsAllDataFromSqlite(" where id=" + optionId);
                if (optionsList.size() > 0) {
                    checkOption.setRulerOptions(optionsList.get(0));
                }

                List<OptionMeasure> measureList = OptionsMeasureUtils.getOptionMeasure(checkOption.getRulerOptions().getMeasure());
                if (measureList.size() > 0) {
                    for (OptionMeasure measure : measureList) {
                        checkOption.setFloorHeight(measure);
                        if (measure.getId() == cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height))) {
                            break;
                        }
                    }
                }


                checkOptionsList.add(checkOption);
//                Log.e(TAG, "queryData: 查询历史的RulerCheckOption:"+checkOption.toString() );
            } while (cursor.moveToNext());
        }
        bleDataDbHelper.close();
        return checkOptionsList;
    }


    public static List<RulerCheckOptions> queryCheckOptionFromSqlite(Context context, String where) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        List<RulerCheckOptions> checkOptionsList = new ArrayList<>();
//        Log.e(TAG, "添加管控要点: 查看where条件：" + where);

        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.measure_option_table_name, " * ", where);
        int index = 0;
        int resultId = 0;
        if (cursor.moveToFirst()) {
//            Log.e(TAG, "queryData: 不是新创建的");
            do {
                RulerCheckOptions checkOption = new RulerCheckOptions();
                String checkWhere = " where id=" + cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_check_id));
                List<RulerCheck> checkList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(checkWhere);
                if (checkList.size() > 0) {
                    checkOption.setRulerCheck(checkList.get(0));
                }
                checkOption.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_create_time)));
                checkOption.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                int optionId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_options_id));
                checkOption.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                checkOption.setServerId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                checkOption.setMeasuredNum(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_measured_points)));
                checkOption.setQualifiedNum(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_qualified_points)));
                checkOption.setQualifiedRate(cursor.getFloat(cursor.getColumnIndex(DataBaseParams.measure_option_percent_pass)));
                checkOption.setImg_upload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_upload_flag)));
                checkOption.setImgPath(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_img_path)));
                checkOption.setImgNumber(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_number)));
//                checkOption.setFloorHeight(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height)));
                checkOption.setImgUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_img_time)));
//                根据optionid查询iot_ruler_options模板表里对应的数据
                List<RulerOptions> optionsList = bleDataDbHelper.queryOptionsAllDataFromSqlite(" where id=" + optionId);
                if (optionsList.size() > 0) {
                    checkOption.setRulerOptions(optionsList.get(0));
                }

                List<OptionMeasure> measureList = OptionsMeasureUtils.getOptionMeasure(checkOption.getRulerOptions().getMeasure());
                if (measureList.size() > 0) {
                    for (OptionMeasure measure : measureList) {
                        checkOption.setFloorHeight(measure);
                        if (measure.getId() == cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_option_floor_height))) {
                            break;
                        }
                    }
                }

                checkOptionsList.add(checkOption);
//                Log.e(TAG, "queryData: 查询历史的RulerCheckOption:"+checkOption.toString() );
            } while (cursor.moveToNext());
        }
        bleDataDbHelper.close();
        return checkOptionsList;
    }


    public static User getUser(Context context) {
        User user = new User();
        Set<String> keySet = SharePreferenceUtils.getKeySet();
        Map<String, String> valueMap = SharePreferenceUtils.getData(context, keySet, SharePreferenceUtils.user_table);
        if (valueMap.get(SharePreferenceUtils.user_id).equals("")) {
            user.setUserID(0);
        } else {
            user.setUserID(Integer.parseInt(valueMap.get(SharePreferenceUtils.user_id)));
        }

//        String wid = valueMap.get(SharePreferenceUtils.user_wid);
        user.setUser_type(valueMap.get(SharePreferenceUtils.user_type));
        user.setUserName(valueMap.get(SharePreferenceUtils.user_real_name));
        user.setMobile(valueMap.get(SharePreferenceUtils.user_real_name));
        user.setPassword(valueMap.get(SharePreferenceUtils.user_pwd));
        user.setWxData(valueMap.get(SharePreferenceUtils.user_wx_data));
        user.setToken(valueMap.get(SharePreferenceUtils.user_token));
//        LogUtils.show("查看获取的当前用户信息："+user);
        return user;
    }

    public static int updateOptionsDataToSqlite(Context context,ContentValues values,String[] id) {
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(context);
        int result = dataDbHelper.updateDataToSqlite(DataBaseParams.options_data_table_name, values, " id =? ", id);
//        LogUtils.show("updateOptionsDataToSqlite----查看更新返回值："+result+",更新的值："+values);
        dataDbHelper.close();
        return result;
    }

    public static int updateOptionsDataToSqlite(Context context,String tablename,ContentValues values,String[] id) {
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(context);
        int result = dataDbHelper.updateDataToSqlite(tablename, values, " id =? ", id);
//        LogUtils.show("updateOptionsDataToSqlite----查看更新返回值："+result+",更新的值："+values);
        dataDbHelper.close();
        return result;
    }

    public static int updateOptionsDataToSqlite(Context context,String tablename,String where,ContentValues values,String[] id) {
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(context);
        int result = dataDbHelper.updateDataToSqlite(tablename, values, where, id);
//        LogUtils.show("updateOptionsDataToSqlite----查看更新返回值："+result+",更新的值："+values);
        dataDbHelper.close();
        return result;
    }

    /**
     *
     * @param context
     * @param tableName
     * @param where
     * @param id
     * @return
     */
    public static boolean delData(Context context,String tableName, String where, String[] id) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        boolean resutl = bleDataDbHelper.delData(tableName, where, id);
        bleDataDbHelper.close();
        return resutl;

    }




}
