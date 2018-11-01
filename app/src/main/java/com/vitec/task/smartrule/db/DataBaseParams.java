package com.vitec.task.smartrule.db;

/**
 * 数据库中的表名和列名
 */
public class DataBaseParams {

    public static final String databaseName = "rule_data.db";

    public static final String server_id = "server_id";
    /**
     * iot_ruler_ble_device的表名和列名
     * 靠尺蓝牙设备的信息表
     */
    public static final String ble_device_table_name = "iot_ruler_ble_device";
    public static final String ble_device_name = "ble_name";
    public static final String ble_device_mac = "ble_mac";
    public static final String ble_device_last_connect_time = "last_connect_time";


    /**
     * iot_ruler_engineer:测量工程模板，包括工程名称、工程描述、创建时间、更新时间
     */
    public static final String  engineer_table_name = "iot_ruler_engineer";
    public static final String  enginer_id_key = "id";
    public static final String  enginer_name = "name";
    public static final String  enginer_description = "description";
    public static final String  enginer_create_name = "create_time";
    public static final String  enginer_update_time = "update_time";


    /**
     * Iot_ruler_options:测量管控要点模板，包括管控要点名称、测量标准、检查方法、工程id、创建时间、更新时间
     */
    public static final String options_table_name = "iot_ruler_options";
    public static final String options_id = "id";
    public static final String options_name = "name";
    public static final String options_standard = "standard";
    public static final String options_methods = "methods";
    public static final String options_engin_id = "engin_id";
    public static final String options_create_time = "create_time";
    public static final String options_update_time = "update_time";

    /**
     * Iot_ruler_check: 测量的时候填写的信息表格，包括项目名、测量位置、工程id、用户id、创建时间、更新时间、创建日期
     */
    public static final String measure_table_name = "iot_ruler_check";
    public static final String measure_id = "id";
    public static final String measure_project_name = "project_name";
    public static final String measure_check_floor = "check_floor";
    public static final String measure_engin_id = "engin_id";
    public static final String measure_user_id = "user_id";
    public static final String measure_create_time = "create_time";
    public static final String measure_update_time = "update_time";
    public static final String measure_create_date = "create_date";

    /**
     * Iot_ruler_check_options: 测量的时候填写的管控要点信息，
     * 包括表头id、管控要点id、层高、实测点数、合格点数、合格率、创建时间、更新时间
     */
    public static final String measure_option_table_name = "iot_ruler_check_options";
    public static final String measure_option_id = "id";
    public static final String measure_option_check_id = "check_id";
    public static final String measure_option_options_id = "options_id";
    public static final String measure_option_floor_height = "floor_height";
    public static final String measure_option_measured_points = "measured_points";
    public static final String measure_option_qualitied_points = "qualitied_points";
    public static final String measure_option_percent_pass = "percent_pass";
    public static final String measure_option_create_time = "create_time";
    public static final String measure_option_update_time = "update_time";


    /**
     * Iot_ruler_check_options_data:测量时候存储管控要点测量数据，包括测量管控要点id、数据、创建时间
     */

    public static final String options_data_table_name = "iot_ruler_check_options_data";
    public static final String options_data_id = "id";
    public static final String options_data_check_options_id = "check_options_id";
    public static final String options_data_content = "data";
    public static final String options_data_create_time = "create_time";
    public static final String options_data_update_flag = "update_flag";

}
