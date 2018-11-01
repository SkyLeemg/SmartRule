package com.vitec.task.smartrule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vitec.task.smartrule.bean.EngineerBean;
import com.vitec.task.smartrule.bean.OptionBean;

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


    /**
     * 想表格中插入数据
     * @param values
     * @return
     */
    public boolean insertDevToSqlite(String tableName,ContentValues values) {
        int renum = (int) sqLiteDatabase.insert(tableName, "", values);
        if (renum!=-1)
            return true;
        else return false;
    }

    /**
     * 查询工程表格中所有的数据
     */
    public List<EngineerBean> queryEnginAllDataFromSqlite() {
        List<EngineerBean> engineerBeanList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM iot_ruler_engineer", null);
        if (cursor.moveToFirst()) {
            do {
                EngineerBean bean = new EngineerBean();
                bean.setProjectID(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                bean.setProjectEngineer(cursor.getString(cursor.getColumnIndex(DataBaseParams.enginer_name)));
                engineerBeanList.add(bean);
            } while (cursor.moveToNext());
        }
        Log.e(TAG, "queryEnginAllDataFromSqlite: 查看搜索到的所有数据："+engineerBeanList.toString() );
        return engineerBeanList;
    }


    /**
     * 查询管控要点的表格中所有的数据
     * @return
     */
    public List<OptionBean> queryOptionsAllDataFromSqlite() {
        List<OptionBean> optionBeans = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM iot_ruler_options", null);
        if (cursor.moveToFirst()) {
            do {
                OptionBean bean = new OptionBean();
                bean.setOptionId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.server_id)));
                bean.setEnginId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.options_engin_id)));
                bean.setMeasureItemName(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_name)));
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_methods)));
                bean.setCheckWay(stringBuffer);
                bean.setPassStandard(cursor.getString(cursor.getColumnIndex(DataBaseParams.options_standard)));
                optionBeans.add(bean);
            } while (cursor.moveToNext());
        }
        Log.e(TAG, "queryOptionsAllDataFromSqlite: 查看本地数据库中查询到的所有管控要点：\n"+optionBeans.toString() );
        return optionBeans;
    }

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


}
