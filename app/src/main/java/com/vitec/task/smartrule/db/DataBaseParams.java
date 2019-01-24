package com.vitec.task.smartrule.db;

/**
 * 数据库中的表名和列名
 */
public class DataBaseParams {

    public static final String databaseName = "rule_data.db";
    /**
     * 多个表格都有的通用列名
     */
    public static final String server_id = "server_id";
    public static final String local_id = "local_id";
    public static final String check_options = "check_options";
    public static final String upload_flag = "upload_flag";
    public static final String cid = "cid";
    public static final String delete_flag = "delete_flag";

    /**
     * iot_ruler_ble_device的表名和列名
     * 靠尺蓝牙设备的信息表
     */
    public static final String ble_device_table_name = "iot_ruler_ble_device";
    public static final String ble_device_name = "ble_name";
    public static final String ble_device_mac = "ble_mac";
    public static final String ble_device_last_connect_time = "last_connect_time";
    public static final String ble_alias = "ble_alias";
    public static final String ble_ver_code = "ble_ver_code";
    public static final String ble_ver_name = "ble_ver_name";


    /**
     * iot_ruler_engineer:测量工程模板，包括工程名称、工程描述、创建时间、更新时间
     */
    public static final String engineer_table_name = "iot_ruler_engineer";
    public static final String enginer_id_key = "id";
    public static final String enginer_name = "name";
    public static final String enineer_options_choose = "options_choose";//该工程包含的管控要点
    public static final String enginer_description = "description";
    public static final String enginer_create_name = "create_time";
    public static final String enginer_update_time = "update_time";


    /**
     * Iot_ruler_options:测量管控要点模板，包括管控要点名称、测量标准、检查方法、工程id、创建时间、更新时间
     */
    public static final String options_table_name = "iot_ruler_options";
    public static final String options_id = "id";
    public static final String options_name = "name";
    public static final String options_standard = "standard";
    public static final String options_methods = "methods";
    public static final String options_engin_id = "engin_id";
    public static final String options_type = "type";
    public static final String options_create_time = "create_time";
    public static final String options_update_time = "update_time";
    public static final String options_measure = "measure";

    /**
     * Iot_ruler_check: 测量的时候填写的信息表格，包括项目名、测量位置、工程id、用户id、创建时间、更新时间、创建日期
     */
    public static final String measure_table_name = "iot_ruler_check";
    public static final String measure_id = "id";
    //    public static final String measure_project_name = "project_name";
    public static final String measure_project_id = "project_id";
    public static final String measure_unit_id = "unit_id";
    public static final String measure_check_floor = "check_floor";
    public static final String measure_engin_id = "engin_id";
    public static final String measure_user_id = "user_id";
    public static final String measure_create_time = "create_time";
    public static final String measure_update_time = "update_time";
    public static final String measure_create_date = "create_date";
    public static final String measure_is_finish = "is_finish";//是否结束测量，0-未结束，1-测量结束

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
    public static final String measure_option_qualified_points = "qualified_points";
    public static final String measure_option_percent_pass = "percent_pass";
    public static final String measure_option_create_time = "create_time";
    public static final String measure_option_update_time = "update_time";
    public static final String measure_option_img_path = "img_path";
    public static final String measure_option_img_time = "img_time";
    public static final String measure_option_server_img_url = "server_img_url";
    public static final String measure_option_img_upload_flag = "img_upload_flag";
    public static final String measure_option_img_number = "img_number";


    /**
     * Iot_ruler_check_options_data:测量时候存储管控要点测量数据，包括测量管控要点id、数据、创建时间
     */
    public static final String options_data_table_name = "iot_ruler_check_options_data";
    public static final String options_data_id = "id";
    public static final String options_data_check_options_id = "check_options_id";//此id对应iot_ruler_check_options表的id
    public static final String options_data_content = "data";
    public static final String options_data_number = "number";
    public static final String options_data_create_time = "create_time";
    public static final String options_data_update_flag = "update_flag";


    /**
     * iot_ruler_user：用户表格，包括id，登录名，用户名，微信的unionid,密码，手机号码
     */
    public static final String user_table_name = "iot_ruler_user";
    public static final String user_user_id= "user_id";
    public static final String user_child_id = "server_cid";
//    public static final String user_wid= "wid";
    public static final String user_data= "data";
//    public static final String user_login_name = "login_name";
    public static final String user_user_name = "user_name";
    public static final String user_wx_unionid = "wx_unionid";
    public static final String user_password = "password";
    public static final String user_mobile = "mobile";
    public static final String user_token = "token";
    public static final String user_img_url = "img_url";
    public static final String user_position = "position";//职位
    public static final String user_status = "status";//状态，1-正在登录使用，0-未登录
//    public static final String user_job = "job";
    public static final String user_local_img_path = "local_head_img_path";


    /**
     * iot_ruler_wx_user：用户的微信个人信息
     */
    public static final String user_wx_table_name = "iot_ruler_wx_user";
    public static final String user_wx_nick_name = "wx_nick_name";
    public static final String user_wx_access_token = "wx_access_token";
    public static final String user_wx_expires_in = "wx_expires_in";
    public static final String user_wx_refresh_token = "wx_refresh_token";
    public static final String user_wx_openid = "wx_openid";
    public static final String user_wx_headImgUrl = "headImgUrl";


    /**
     * iot_ruler_check_project
     */
    public static final String check_project_table_name = "iot_ruler_check_project";
    public static final String check_project_name = "project_name";
    public static final String check_project_qrcode = "qrcode";

    /**
     * iot_ruler_unit_engineer
     */
    public static final String unit_engineer_table_name = "iot_ruler_unit_engineer";
    public static final String unit_engineer_location = "location";
    public static final String project_server_id = "project_server_id";

    /**
     * iot_ruler_project_user  项目组的用户表- 项目组里的成员表
     */
    public static final String project_user_table_name = "iot_ruler_project_user";

    /**
     * iot_company 公司资料
     */
    public static final String iot_company_table_name = "iot_company";
    public static final String iot_company_content = "content";
    public static final String iot_company_title = "title";

    /**
     * iot_ruler_check_group 用户项目表，该用户所在的项目
     */
    public static final String iot_ruler_check_group_table_name = "iot_ruler_check_group";



}

