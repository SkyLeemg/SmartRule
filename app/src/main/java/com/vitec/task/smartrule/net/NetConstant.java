package com.vitec.task.smartrule.net;

public class NetConstant {

    public static final String baseUrl = "http://iot.vkforest.com";
    /**
     * 获取手机验证码的接口
     */
    public static final String getMobileCodeUrl = "/api/user/getMobileCode";
    public static final String mobile_param = "mobile";//需要请求的参数

    /**
     * 注册接口
     */

    public static final String registerUrl = "/api/user/register";
    public static final String unionId = "unionId";//微信登录返回的unionId
    public static final String register_username = "username";//登录名
    public static final String register_password = "password";//密码
    public static final String register_password_confirm = "password_confirm";//确认密码
    public static final String register_name = "name";//姓名
    public static final String register_mobile = "mobile";//手机号码
    public static final String register_code = "code";//手机验证码

    /**
     * 登录接口
     */
    public static final String loginUrl = "/api/user/login";
    public static final String login_username = "username";
    public static final String login_password = "password";
    public static final String login_unionId = "unionId";
    public static final String login_mobile = "mobile";
    public static final String login_code = "code";
    public static final String login_data = "data";//微信登录返回的所有资料

    /**
     * 修改密码接口
     */
    public static final String change_pwd_url = "/api/user/changePasswordByOldPassword";
    public static final String change_pwd_new_password = "new_password";//新密码
    public static final String change_pwd_token = "token";//登录返回的token
    public static final String change_pwd_password = "password";//旧密码


    /**
     * 绑定账号的接口，绑定手机号码请求字段为：mobile和code，绑定微信请求字段为：data
     */
    public static final String bind_account_url = "/api/user/bindAccount";


    //获取所有工程信息的get请求地址
    public static final String getEngineerInfoUrl = "http://iot.vkforest.com/api/ruler/showEngineerInfo";

    //    获取 工程id为1的所有管控要点
    public static final String getOptionInfoUrl = "http://iot.vkforest.com/api/ruler/showOptionsInfo?engin_id=";

    /**
     * 很多网络请求都需要用到userid和wid
     */
    public static final String user_id = "user_id";
    public static final String wid = "wid";
    public static final String engin_id = "engin_id";


    /**
     * 创建记录表接口
     */
    public static final String create_record_url = "/api/ruler/createRecord";
    public static final String create_record_data = "data";
//    public static final String record_project_name = "project_name";
//    public static final String record_check_floor = "check_floor";

    /**
     * 更新记录表接口,传参与创建记录表的一样
     */
    public static final String update_record_url = "/api/ruler/updateRecord";

    /**
     * 更新测量数据的接口
     */
    public static final String update_data_url = "/api/ruler/updateData";
    public static final String update_data_param = "data";


    /**
     * 获取测量数据
     */
    public static final String get_measure_data_url = "/api/ruler/getMeasureRecordData";
    public static final String get_measure_data_check_id = "check_id";


    /**
     * 检查更新
     */
    public static final String check_update_url = "/api/user/checkUpdate";
    public static final String check_update_type = "type";
    public static final String check_update_app_name = "app_name";





}
