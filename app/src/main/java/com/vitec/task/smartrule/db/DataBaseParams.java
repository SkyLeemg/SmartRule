package com.vitec.task.smartrule.db;

/**
 * 数据库中的表名和列名
 */
public class DataBaseParams {

    public static final String databaseName = "rule_data.db";

    /**
     * iot_ruler_ble_device的表名和列名
     * 靠尺蓝牙设备的信息表
     */
    public static final String ble_device_table_name = "iot_ruler_ble_device";
    public static final String ble_device_name = "ble_name";
    public static final String ble_device_mac = "ble_mac";
    public static final String ble_device_last_connect_time = "last_connect_time";
}
