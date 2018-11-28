package com.vitec.task.smartrule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class BleDataDbHelper {

    private static final String TAG = "BleDataDbHelper";
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
//    private final String tableName = "iot_ruler_ble_device";

    public BleDataDbHelper(Context context) {
        this.context = context;
        sqLiteDatabase = SQLiteDatabase.openDatabase("data/data/" +context.getPackageName() +
                "/databases/"+DataBaseParams.databaseName, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void close() {
        sqLiteDatabase.close();
    }

    /**
     * 想表格中插入数据
     * @param values
     * @return
     */
    public boolean insertDevToSqlite(String tableName,ContentValues values) {
        int renum = (int) sqLiteDatabase.insert(tableName, "", values);
        Log.e(TAG, "insertDevToSqlite: 打印插入数据库后返回的数字："+renum );
        if (renum!=-1)
            return true;
        else return false;
    }

    /**
     * 查询工程表格中所有的数据
     */
    public List<RulerEngineer> queryEnginAllDataFromSqlite() {
        List<RulerEngineer> engineerList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM iot_ruler_engineer", null);
        if (cursor.moveToFirst()) {
            do {
                RulerEngineer engineer = new RulerEngineer();
                engineer.setServerID(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                engineer.setEngineerName(cursor.getString(cursor.getColumnIndex(DataBaseParams.enginer_name)));
                engineerList.add(engineer);
            } while (cursor.moveToNext());
        }
        Log.e(TAG, "queryEnginAllDataFromSqlite: 查看搜索到的所有数据："+engineerList.toString() );
        return engineerList;
    }

    /**
     * 查询工程表格中所有的数据
     */
    public List<RulerEngineer> queryEnginDataFromSqlite(String where) {
        List<RulerEngineer> engineerList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM iot_ruler_engineer " + where, null);
        if (cursor.moveToFirst()) {
            do {
                RulerEngineer engineer = new RulerEngineer();
                engineer.setServerID(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                engineer.setEngineerName(cursor.getString(cursor.getColumnIndex(DataBaseParams.enginer_name)));
                engineerList.add(engineer);
            } while (cursor.moveToNext());
        }
        Log.e(TAG, "queryEnginAllDataFromSqlite: 查看搜索到的所有数据："+engineerList.toString() );
        return engineerList;
    }


    /**
     * 查询管控要点的表格中所有的数据
     * @return
     */
    public List<RulerOptions> queryOptionsAllDataFromSqlite(String where) {
        List<RulerOptions> optionsList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM iot_ruler_options " + where, null);
        if (cursor.moveToFirst()) {
            do {
                RulerOptions option = new RulerOptions();
                option.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                option.setServerID(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                RulerEngineer engineer = new RulerEngineer();
                engineer.setServerID(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_engin_id)));
                option.setEngineer(engineer);
                option.setOptionsName(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_name)));
                option.setMethods(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_methods)));
                option.setStandard(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_standard)));
                option.setMeasure(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_measure)));
                optionsList.add(option);
            } while (cursor.moveToNext());
        }
        Log.e(TAG, "queryOptionsAllDataFromSqlite: 查看本地数据库中查询到的所有管控要点：\n"+optionsList.toString() );
        return optionsList;
    }

    /**
     * 查找iot_ruler_check表格的所有数据
     * @param where
     * @return
     */
    public List<RulerCheck> queryRulerCheckTableDataFromSqlite(String where) {
        List<RulerCheck> checkList = new ArrayList<>();
        String sql = "SELECT * FROM iot_ruler_check " +where;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                RulerCheck rulerCheck = new RulerCheck();
                rulerCheck.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                rulerCheck.setProjectName(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_project_name)));
                rulerCheck.setCreateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_create_time)));
                rulerCheck.setCreateDate(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_create_date)));
                rulerCheck.setCheckFloor(cursor.getString(cursor.getColumnIndex(DataBaseParams.measure_check_floor)));
                rulerCheck.setUpdateTime(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_update_time)));
                rulerCheck.setServerId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                rulerCheck.setUpload_flag(cursor.getInt(cursor.getColumnIndex(DataBaseParams.upload_flag)));
                List<RulerEngineer> engineerList = queryEnginDataFromSqlite(" where " + DataBaseParams.server_id + " = " + cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_engin_id)));
                if (engineerList.size() > 0) {
                    RulerEngineer engineer = engineerList.get(0);
                    rulerCheck.setEngineer(engineer);
                } else {
                    RulerEngineer engineer = new RulerEngineer();
                    engineer.setServerID(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_engin_id)));
                    rulerCheck.setEngineer(engineer);
                }
                User user = OperateDbUtil.getUser(context);
                rulerCheck.setUser(user);
                checkList.add(rulerCheck);
            } while (cursor.moveToNext());
        }

        return checkList;
    }



    /**
     *
     * @param cols
     * @param where
     * @return
     */
    public Cursor queryMeasureOptionsFromSqlite(String cols,String where) {
        String sql = "SELECT " + cols + "  FROM iot_ruler_check " + where;
        Log.e(TAG, "queryMeasureOptionsFromSqlite: 查看最终的sql语句："+sql );
        Cursor cursor = sqLiteDatabase.rawQuery(sql,null);

        return cursor;
    }

    public Cursor queryMeasureOptionsFromSqlite(String tableName,String cols,String where) {
        String sql = "SELECT " + cols + "  FROM "+tableName + where;
        Log.e(TAG, "queryMeasureOptionsFromSqlite: 查看最终的sql语句："+sql );
        Cursor cursor = sqLiteDatabase.rawQuery(sql,null);

        return cursor;
    }

    /**
     * 更新iot_ruler_check_options表中的测量个数、合格个数、合格率、层高等信息
     */
    public int updateMeasureOptonsToSqlite(RulerCheckOptions rulerCheckOptions) {
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.measure_option_floor_height,rulerCheckOptions.getFloorHeight());
        values.put(DataBaseParams.measure_option_measured_points,rulerCheckOptions.getMeasuredNum());
        values.put(DataBaseParams.measure_option_qualified_points,rulerCheckOptions.getQualifiedNum());
        values.put(DataBaseParams.measure_option_percent_pass,rulerCheckOptions.getQualifiedRate());
        values.put(DataBaseParams.measure_option_update_time,(int)System.currentTimeMillis());
        int result = sqLiteDatabase.update(DataBaseParams.measure_option_table_name, values, DataBaseParams.measure_id + "=?", new String[]{String.valueOf(rulerCheckOptions.getId())});
        Log.e(TAG, "updateMeasureOptonsToSqlite: 合格点数更新状态：" + result + ",更新的数据内容：" + rulerCheckOptions.toString());
        return result;
    }

    public int updateDataToSqlite(String table, ContentValues values, String where, String[] whereValues) {
        int result = sqLiteDatabase.update(table, values, where, whereValues);
        LogUtils.show("查看"+table+",表格的"+values+",值，更新是否成功："+result);
        return result;
    }


}
