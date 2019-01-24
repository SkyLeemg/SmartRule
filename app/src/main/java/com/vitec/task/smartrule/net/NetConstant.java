package com.vitec.task.smartrule.net;

public class NetConstant {

    public static final String baseUrl = "http://iot-test.vkforest.com";
//    public static final String baseUrl = "http://iot.vkforest.com";
    /**
     * 获取手机验证码的接口
     */
    public static final String getMobileCodeUrl = "/api/user/getMobileCode";
    public static final String mobile_param = "mobile";//需要请求的参数

    /**
     * 验证手机验证码
     */
    public static final String validateMobileCodeUrl = "/api/user/validateMobileCode";

    /**
     * 根据手机号修改密码
     */
    public static final String change_password_by_mobile_url = "/api/user/changePasswordByMobile";


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
    public static final String getEngineerInfoUrl = "/api/ruler/showEngineerInfo";

    //    获取 工程id为1的所有管控要点
    public static final String getOptionInfoUrl = "/api/ruler/showOptionsInfo";

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
     * 查询测量记录表接口
     */
    public static final String get_measure_record_url = "/api/ruler/getMeasureRecordList";
    public static final String record_status = "status";
    public static final String current_Page = "currentPage";
    public static final String page_size = "pageSize";



    /**
     * 更新测量数据的接口
     */
    public static final String update_data_url = "/api/ruler/updateData";
    public static final String update_data_param = "data";
    public static final String update_check_options = "check_options";



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

    /**
     * 停止测量
     */
    public static final String stop_measure_url = "/api/ruler/stopMeasure";
    public static final String stop_measure_check_id = "check_id";


    /**
     * 删除记录表
     */
    public static final String del_record_url = "/api/ruler/deleteRecord";
    public static final String del_check_list = "check_list";

    /**
     * 更新用户信息
     */
    public static final String update_user_msg = "/api/user/updateUserInfo";


    /**
     * 通用上传文件
     */
    public static final String upload_file_url = "/api/oss/upload";
    public static final String upload_file_key = "file";

    /**
     * 上传管控要点图纸
     */
    public static final String upload_option_pic_url = "/api/ruler/uploadPicture";
    public static final String upload_option_pic_check_options_list = "check_options_list";
    public static final String upload_option_pic_url_key= "url";
    public static final String upload_option_pic_number_list= "number_list";

    /**
     * 获取公司资料接口
     */
    public static final String get_company_frofile_url = "/api/ruler/getCompanyProfile";

    /**
     * 提交意见
     */
    public static final String post_submit_advice_url = "/api/ruler/submitAdvice";
    public static final String post_submit_advice_content = "content";




    /*******************************测量组相关的接口*************************************/
    //创建测量组接口
    public static final String group_create_project_url = "/api/ruler/createProject";
    //更新测量组
    public static final String group_update_proect_url = "/api/ruler/updateProject";
    //删除测量组
    public static final String group_del_project_url = "/api/ruler/deleteProject";
    //查询测量组
    public static final String group_get_project_list_url = "/api/ruler/getProjectList";
    //添加成员
    public static final String group_add_member_url = "/api/ruler/addGroupMemberByMobile";
    //删除成员
    public static final String group_del_member_url = "/api/ruler/deleteGroupMember";
    //添加单位工程
    public static final String group_add_unit_engineer_url = "/api/ruler/addUnitEngineer";
    //更新单位工程
    public static final String group_update_unit_engineer_url = "/api/ruler/updateUnitEngineer";
    //删除单位工程
    public static final String group_del_unit_engineer_url = "/api/ruler/deleteUnitEngineer";
    //查询测量组成员和单位工程
    public static final String group_get_member_and_unit_url = "/api/ruler/getGroupMemberAndUnitEngineer";

    //通用参数
    public static final String group_project_list = "project_list";
    public static final String group_group_list = "group_list";
    public static final String group_unit_list = "unit_list";


}
