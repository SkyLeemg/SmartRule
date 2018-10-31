package com.vitec.task.smartrule.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vitec.task.smartrule.bean.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class BleDeviceDbHelper {

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
        int renum = (int) sqLiteDatabase.insert(tableName, "", values);
        if (renum!=-1)
            return true;
        else return false;
    }

    public List<BleDevice> queryAllDevice() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM iot_ruler_ble_device", null);
        List<BleDevice> devices = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                BleDevice device = new BleDevice();
                device.setBleMac(cursor.getString(cursor.getColumnIndex("ble_mac")));
                device.setBleName(cursor.getString(cursor.getColumnIndex("ble_name")));
                device.setLastConnectTime(cursor.getInt(cursor.getColumnIndex("last_connect_time")));
                devices.add(device);
            } while (cursor.moveToNext());
        }
        return devices;

    }

}
