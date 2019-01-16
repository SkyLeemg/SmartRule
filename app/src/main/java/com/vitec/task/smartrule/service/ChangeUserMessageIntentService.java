package com.vitec.task.smartrule.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.UpdateUserMessageResutCallBack;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ChangeUserMessageIntentService extends IntentService {

    public static final String PARAM_LIST_KEY = "com.vitec.task.smartrule.service.param.list";
    public ChangeUserMessageIntentService() {
        super("ChangeUserMessageIntentService");
    }

    public ChangeUserMessageIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        List<OkHttpUtils.Param> paramList = (List<OkHttpUtils.Param>) intent.getSerializableExtra(PARAM_LIST_KEY);
        requestUpdateUserMsg(paramList);

    }

    private void requestUpdateUserMsg(List<OkHttpUtils.Param> paramList) {
        User user = OperateDbUtil.getUser(getApplicationContext());
        LogUtils.show("requestUpdateUserMsg---更新用户信息接口："+user.toString());
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.change_pwd_token, user.getToken());
        paramList.add(param);
        String url = NetConstant.baseUrl + NetConstant.update_user_msg;
        LogUtils.show("更新用户信息的请求链接："+url+",参数："+paramList.toString());
        final UpdateUserMessageResutCallBack message = new UpdateUserMessageResutCallBack();

        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                LogUtils.show("requestUpdateUserMsg---查看更新用户信息接口响应的信息："+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    if (code == 200) {
                        message.setSuccess(true);
                        message.setMsg(msg);
                        EventBus.getDefault().post(message);
                    } else {
                        message.setMsg(msg);
                        message.setSuccess(false);
                        EventBus.getDefault().post(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    message.setMsg("数据解析异常");
                    message.setSuccess(false);
                    EventBus.getDefault().post(message);
                }


            }

            @Override
            public void onFailure(Exception e) {
                message.setMsg("网络请求失败");
                message.setSuccess(false);
                EventBus.getDefault().post(message);
            }
        },paramList);

    }
}
