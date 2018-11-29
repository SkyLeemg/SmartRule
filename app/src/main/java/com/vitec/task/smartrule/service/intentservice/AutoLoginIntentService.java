package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * 当用户第一次登录成功后，之后每次打开软件都执行自动跳转，
 * 由于登录的token字段时效是2小时
 * 为了更新token，执行此intentService，自动在后台服务请求一次登录接口，更新token
 */
public class AutoLoginIntentService extends IntentService {

    public AutoLoginIntentService() {
        super("AutoLoginIntentService");
    }

    public AutoLoginIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        /**
         * 1.获取当前登录的用户信息
         * 2.根据user_id和wid，还有数据库是否存有wx_data来判断上一次是使用什么登录方式
         *  2.1 如果是用户名和密码登录、或者微信登录都可以自己请求
         *  2.2 如果是手机验证码。。。暂时服务器还没对接
         * 3.请求成功后 直接将数据保存到数据库
         */
//        1.获取当前登录的用户信息


    }
}
