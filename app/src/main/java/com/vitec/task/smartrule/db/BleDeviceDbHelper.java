package com.vitec.task.smartrule.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vitec.task.smartrule.bean.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class BleDeviceDbHelper {

    private static final String TAG = "BleDeviceDbHelper";
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private final String tableName = "iot_ruler_ble_device";

    public BleDeviceDbHelper(Context context) {
        this.context = context;
        sqLiteDatabase = SQLiteDatabase.openDatabase("data/data/" +context.getPackageName() +
                "/databases/"+DataBaseParams.databaseName, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * 想表格中插入数据
     * @param values
     * @return
     */
    public boolean insertDevToSqlite(ContentValues values) {
        try {
            int renum = (int) sqLiteDatabase.insert(tableName, "", values);
            if (renum!=-1)
                return true;
            else return false;
        } catch (Exception e) {
            Log.e(TAG, "insertDevToSqlite: 查看插入错误的信息:" + e.getMessage());
        }
        return false;

    }

    public List<BleDevice> queryAllDevice() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM iot_ruler_ble_device", null);
        List<BleDevice> devices = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                BleDevice device = new BleDevice();
                device.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                device.setBleMac(cursor.getString(cursor.getColumnIndex("ble_mac")));
                device.setBleName(cursor.getString(cursor.getColumnIndex("ble_name")));
                device.setLastConnectTime(cursor.getInt(cursor.getColumnIndex("last_connect_time")));
                device.setBleAlias(cursor.getString(cursor.getColumnIndex(DataBaseParams.ble_alias)));
                device.setBleVerCode(cursor.getInt(cursor.getColumnIndex(DataBaseParams.ble_ver_code)));
                device.setBleVerName(cursor.getString(cursor.getColumnIndex(DataBaseParams.ble_ver_name)));
                devices.add(device);
                Log.e(TAG, "queryAllDevice: 查看查询出来的设备："+device );
            } while (cursor.moveToNext());
        }
        return devices;

    }

    public boolean updateDevice(ContentValues values,String[] id) {
        int result = sqLiteDatabase.update(DataBaseParams.ble_device_table_name, values, new String(DataBaseParams.measure_id + "=?"), id);
        Log.e(TAG, "updateDevice: 更新设备信息："+result );
        if (result!=-1)
            return true;
        else return false;
    }

    public boolean delDevice(String[] id) {
        int result = sqLiteDatabase.delete(DataBaseParams.ble_device_table_name, "id=?", id);
        Log.e(TAG, "updateDevice: 更新设备信息："+result );
        if (result!=-1)
            return true;
        else return false;
    }

}
