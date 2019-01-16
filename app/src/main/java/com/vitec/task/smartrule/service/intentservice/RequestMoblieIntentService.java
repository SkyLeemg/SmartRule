package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.event.MoblieRequestResutEvent;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求发送手机验证码和验证手机验证码是否正确的相关操作
 */
public class RequestMoblieIntentService extends IntentService {

    public static final String REQUEST_FLAG = "com.vitec.task.smartrule.service.intentservice.request.flag";
//    请求发送手机验证码
    public static final String FLAG_REQUEST_MOBLIE_CODE = "com.vitec.task.smartrule.service.intentservice.request_moblie_code";
//    验证输入的验证码是否正确
    public static final String FLAG_VALIDATE_MOBLIE_CODE = "com.vitec.task.smartrule.service.intentservice.validate_moblie_code";
//    手机号码
    public static final String VALUE_MOBLIE = "mobile";
//    手机验证码
    public static final String VALUE_MOBLIE_CODE = "mobile_code";



    public RequestMoblieIntentService() {
        super("RequestMoblieIntentService");
    }

    public RequestMoblieIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String flag = intent.getStringExtra(REQUEST_FLAG);
        switch (flag) {
            /**
             * 请求发送手机验证码
             */
            case FLAG_REQUEST_MOBLIE_CODE:
                String phone = intent.getStringExtra(VALUE_MOBLIE);
                requestSendMobileCode(phone);
                break;

            /**
             * 验证输入的验证码是否正确
             */
            case FLAG_VALIDATE_MOBLIE_CODE:
                String code=intent.getStringExtra(VALUE_MOBLIE_CODE);
                String phone1=intent.getStringExtra(VALUE_MOBLIE);
                validateMobileCode(phone1,code);
                break;
        }
    }

    /**
     * 验证输入的验证码是否正确
     */
    private void validateMobileCode(String phone,String code) {
        final List<OkHttpUtils.Param> paramList = new ArrayList<>();
        OkHttpUtils.Param mobileParam = new OkHttpUtils.Param(NetConstant.mobile_param, phone);
        OkHttpUtils.Param codeParam = new OkHttpUtils.Param(NetConstant.register_code, code);
        paramList.add(mobileParam);
        paramList.add(codeParam);
        final String url = NetConstant.baseUrl + NetConstant.validateMobileCodeUrl;
        LogUtils.show("请求前的参数和连接："+url+",参数："+paramList.toString());
        final MoblieRequestResutEvent event = new MoblieRequestResutEvent();
        event.setRequst_flag(FLAG_VALIDATE_MOBLIE_CODE);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    LogUtils.show("onSuccess-----打印查看返回信息："+response);
                    JSONObject jsonObject = new JSONObject(response);
                    final int code = jsonObject.optInt("code");
                    final String msg = jsonObject.optString("msg");
                   event.setMsg(msg);
                    if (code == 200) {
                        event.setSuccess(true);
                    } else {
                        event.setSuccess(false);
                    }
                    EventBus.getDefault().post(event);

                } catch (JSONException e) {
                    e.printStackTrace();
                   event.setMsg("数据解析失败");
                   event.setSuccess(false);
                   EventBus.getDefault().post(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                event.setMsg("网络请求失败");
                event.setSuccess(false);
                EventBus.getDefault().post(event);
            }
        }, paramList);
    }

    /**
     * 请求发送手机验证码
     */
    private void requestSendMobileCode(String phone) {
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.mobile_param,phone);
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        StringBuffer url = new StringBuffer();
        url.append(NetConstant.baseUrl);
        url.append(NetConstant.getMobileCodeUrl);
        final MoblieRequestResutEvent event = new MoblieRequestResutEvent();
        event.setRequst_flag(FLAG_REQUEST_MOBLIE_CODE);
        OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    /**
                     *
                     {"status":"success","code":200,"msg":"验证码下发成功"}
                     */
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    Log.e("aaa", "onSuccess: 获取验证码成功："+response );
                    final String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        event.setSuccess(true);

                    } else {
                        event.setSuccess(false);
                    }
                    EventBus.getDefault().post(event);

                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setSuccess(false);
                    event.setMsg("数据解析错误");
                    EventBus.getDefault().post(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                event.setSuccess(false);
                event.setMsg("网络请求失败");
                EventBus.getDefault().post(event);
            }
        },paramList);
    }
}
