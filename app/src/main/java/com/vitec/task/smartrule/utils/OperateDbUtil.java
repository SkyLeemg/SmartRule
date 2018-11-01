package com.vitec.task.smartrule.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.vitec.task.smartrule.bean.EngineerBean;
import com.vitec.task.smartrule.bean.MeasureData;
import com.vitec.task.smartrule.bean.OptionBean;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;

import java.util.ArrayList;
import java.util.List;

public class OperateDbUtil {


    private static final String TAG = "OperateDbUtil";

    /**
     * 添加工程模板和管控要点模板到sqlite
     * @param context
     * @param engineerBeanList
     */
    public static void addEnginAndOptionsData(Context context, List<EngineerBean> engineerBeanList) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        for (int i = 0; i < engineerBeanList.size(); i++) {
            ContentValues values = new ContentValues();
            EngineerBean bean = engineerBeanList.get(i);
            values.put(DataBaseParams.server_id, bean.getProjectID());
            values.put(DataBaseParams.enginer_name,bean.getProjectEngineer());
            values.put(DataBaseParams.enginer_create_name,bean.getCheckTime());
            boolean isSuccess = bleDataDbHelper.insertDevToSqlite(DataBaseParams.engineer_table_name, values);
            if (isSuccess) {
                Log.e(TAG, "addEnginAndOptionsData: 工程保存成功"+isSuccess );
                List<OptionBean> optionBeans = bean.getMeasureBeanList();
                for (OptionBean optionBean : optionBeans) {
                    ContentValues optionsValues = new ContentValues();
                    optionsValues.put(DataBaseParams.server_id, optionBean.getOptionId());
                    optionsValues.put(DataBaseParams.options_name, optionBean.getMeasureItemName());
                    optionsValues.put(DataBaseParams.options_standard, optionBean.getPassStandard());
                    optionsValues.put(DataBaseParams.options_methods, optionBean.getCheckWay().toString());
                    optionsValues.put(DataBaseParams.options_engin_id, optionBean.getEnginId());
                    boolean flag = bleDataDbHelper.insertDevToSqlite(DataBaseParams.options_table_name, optionsValues);
                    Log.e(TAG, "addEnginAndOptionsData: 查看管控要点是否添加成功："+flag+"," + optionsValues);
                }
            }
        }
    }

    /**
     * 点击进入测量后，添加一个测量表格的数据到Iot_ruler_check表
     * 相当于创建了表格的表头信息
     * @param context
     * @param engineerBean
     * @return -1添加失败，0查找id失败，>0返回正确的id
     */
    public static int addMeasureDataToSqlite(Context context,EngineerBean engineerBean) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.measure_engin_id,engineerBean.getProjectID());
        values.put(DataBaseParams.measure_check_floor, engineerBean.getCheckPositon());
        values.put(DataBaseParams.measure_create_date,engineerBean.getCheckTime());
        values.put(DataBaseParams.measure_create_time,System.currentTimeMillis());
        values.put(DataBaseParams.measure_user_id,engineerBean.getPersonId());
        values.put(DataBaseParams.measure_project_name,engineerBean.getProjectEngineer());
        boolean flag = bleDataDbHelper.insertDevToSqlite(DataBaseParams.measure_table_name, values);
        Log.e(TAG, "addMeasureDataToSqlite: 查看测量表头是否添加成功："+flag+",查看内容：" +values);
//        如果添加成功，则返回添加的哪一行的id
        if (flag) {
            String where = "where " + DataBaseParams.measure_engin_id + "=" + engineerBean.getProjectID() + " AND " + 
                    DataBaseParams.measure_user_id + "=" + engineerBean.getPersonId()+" AND " +
                    DataBaseParams.measure_project_name+"= \""+engineerBean.getProjectEngineer()+"\" AND "+
                    DataBaseParams.measure_check_floor+"= \""+engineerBean.getCheckPositon()+"\" ;"
                    ;
            Log.e(TAG, "addMeasureDataToSqlite: 查看where条件：" + where);
            Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite("id", where);
            int resultId = 0;
            if (cursor.moveToFirst()) {
                do {
                    resultId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                } while (cursor.moveToNext());
            }
            return resultId;
        }
        return -1;
    }

    /**
     * 添加要进行测量的管控要点，
     * @param context
     * @param engineerBean
     */
    public static List<OptionBean> addMeasureOptionsDataToSqlite(Context context,EngineerBean engineerBean) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        List<OptionBean> optionlist = engineerBean.getMeasureBeanList();
        for (int i=0;i<optionlist.size();i++) {
            ContentValues values = new ContentValues();
            OptionBean option = optionlist.get(i);
            values.put(DataBaseParams.measure_option_check_id, option.getCheckId());
            values.put(DataBaseParams.measure_option_options_id,option.getOptionId());
            boolean isSuccess = bleDataDbHelper.insertDevToSqlite(DataBaseParams.measure_option_table_name, values);

            if (isSuccess) {
                /**
                 * 查找刚添加的管控要点的id
                 */
                String where = " where " + DataBaseParams.measure_option_check_id + "=" + option.getCheckId()+" ;";
                Log.e(TAG, "添加管控要点: 查看where条件：" + where);
                Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.measure_option_table_name,"id", where);
                int resultId = 0;
                if (cursor.moveToFirst()) {
                    do {
                        resultId = cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id));
                        optionlist.get(i).setCheckOptionId(resultId);
                    } while (cursor.moveToNext());
                }
            }
        }

        return optionlist;
    }

    public static void addRealMeasureDataToSqlite(Context context,MeasureData data) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.options_data_check_options_id, data.getCheckOptionsId());
        values.put(DataBaseParams.options_data_content, data.getData());
        values.put(DataBaseParams.options_data_create_time, data.getCreateTime());
        values.put(DataBaseParams.options_data_update_flag, data.getUpdateFlag());
        boolean isSuccess = bleDataDbHelper.insertDevToSqlite(DataBaseParams.options_data_table_name, values);
        Log.e(TAG, "addMeasureDataToSqlite: 蓝牙数据更新到数据库：" + isSuccess + ",neirong:" + values);
    }

    public static List<MeasureData> queryMeasureDataFromSqlite(Context context,int checkid) {
        List<MeasureData> measurelist = new ArrayList<>();
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(context);
        Cursor cursor = bleDataDbHelper.queryMeasureOptionsFromSqlite(DataBaseParams.options_data_table_name,"*", " where " + DataBaseParams.options_data_check_options_id + "=" + checkid);
        if (cursor.moveToFirst()) {
            do {
                MeasureData data = new MeasureData();
                data.setCheckOptionsId(checkid);
                data.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_create_time)));
                data.setData(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_data_content)));
                data.setUpdateFlag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_data_update_flag)));
                measurelist.add(data);
            } while (cursor.moveToNext());
        }
        return measurelist;
    }

    public static void queryDataFromSqlite(Context context, String tableName) {

    }
}
