package com.vitec.task.smartrule.service.intentservice;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.vitec.task.smartrule.bean.CompanyMessage;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.CompanayMsgEvent;
import com.vitec.task.smartrule.bean.event.CostomMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;

import javax.xml.validation.Validator;

/**
 * 个人中心相关的网络请求：
 * 1.获取公司资料
 * 2.提交意见
 */
public class AboutUserIntentService extends IntentService {

    public static final String TYPE_FLAG = "about.user.type.flag";
    //获取公司资料请求类型
    public static final String FLAG_GET_COMPANY_MESSAGE = "about.user.get.company_message";
    //提交意见类型
    public static final String FLAG_POST_ADVICE = "about.user.post.advice";
    //获取参数值
    public static final String VALUE_BUNDLE = "about.user.value.bundle";


    public AboutUserIntentService() {
        super("AboutUserIntentService");
    }

    public AboutUserIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String type_flag = intent.getStringExtra(TYPE_FLAG);
        switch (type_flag) {
            /**
             * 获取公司资料
             */
            case FLAG_GET_COMPANY_MESSAGE:
                requestCompanyMessage();
                break;

            /**
             * 提交意见
             */
            case FLAG_POST_ADVICE:
                Bundle bundle = intent.getBundleExtra(VALUE_BUNDLE);
                postAdvice(bundle);

                break;
        }

    }

    /**
     * 提交意见
     * @param bundle
     */
    private void postAdvice(Bundle bundle) {
        OkHttpUtils.Param content = new OkHttpUtils.Param(NetConstant.post_submit_advice_content, bundle.getString(NetConstant.post_submit_advice_content,""));
        User user = OperateDbUtil.getUser(getApplicationContext());
        OkHttpUtils.Param token = new OkHttpUtils.Param(DataBaseParams.user_token, user.getToken());
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(content);
        paramList.add(token);
        String url = NetConstant.baseUrl + NetConstant.post_submit_advice_url;
        LogUtils.show("提交意见接口----打印查看提交链接："+url+",查看参数："+paramList);
        final CostomMsgEvent event = new CostomMsgEvent();
        event.setFlag(1);
        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("提交意见接口-----查看响应返回信息：" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
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
                    event.setMsg("未知错误");
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


    /**
     * 请求公司资料
     */
    private void requestCompanyMessage() {
        String url = NetConstant.baseUrl + NetConstant.get_company_frofile_url;
        final CompanayMsgEvent event = new CompanayMsgEvent();
        OkHttpUtils.get(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("获取公司资料接口----查看返回信息：" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    event.setMsg(msg);
                    if (code == 200) {
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        List<CompanyMessage> companyMessageList = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataJson = dataArray.getJSONObject(i);
                            CompanyMessage message = new CompanyMessage();
                            int server_id = dataJson.optInt("id");

                            message.setServer_id(server_id);
                            message.setName(dataJson.optString("name"));
                            message.setContent(dataJson.optString("content"));
                            companyMessageList.add(message);
                            String where = " where " + DataBaseParams.server_id + "=" + server_id;
                            List<CompanyMessage> messages = OperateDbUtil.queryCompanyMsgFromSqlite(getApplicationContext(), where);
                            if (messages.size() == 0) {
                                OperateDbUtil.addCompanyToSqlite(getApplicationContext(), message);
                            }
                        }
                        event.setSuccess(true);
                        event.setObject(companyMessageList);
                        EventBus.getDefault().post(event);
                    } else {
                        event.setSuccess(false);
                        EventBus.getDefault().post(event);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    event.setSuccess(false);
                    event.setMsg("数据解析失败");
                    EventBus.getDefault().post(event);
                }

            }

            @Override
            public void onFailure(Exception e) {
                event.setSuccess(false);
                event.setMsg("网络请求失败");
                EventBus.getDefault().post(event);
            }
        });
    }
}
