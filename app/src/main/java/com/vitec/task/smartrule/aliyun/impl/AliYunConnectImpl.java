package com.vitec.task.smartrule.aliyun.impl;

import android.content.Context;
import android.util.Log;

import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.tools.AError;
import com.vitec.task.smartrule.aliyun.DeviceBean;
import com.vitec.task.smartrule.aliyun.interfaces.IAliYunConnector;
import com.vitec.task.smartrule.aliyun.manager.IDemoCallback;
import com.vitec.task.smartrule.aliyun.manager.InitManager;
import com.vitec.task.smartrule.interfaces.MainActivityGettable;

import org.greenrobot.eventbus.EventBus;

public class AliYunConnectImpl implements IAliYunConnector {

    public  String productKey ;
    private String deviceName ;
    private String deviceSecret ;
    private String TAG = "AliYunConnectImpl";
    public  boolean isInitDone = false;
    private String resultMsg;
    private DeviceBean deviceBean;
    private MainActivityGettable gettable;
    @Override
    public void connect(Context context, final MainActivityGettable gettable) {
        showLog("连接被调用");
        productKey = deviceBean.getProductKey();
        deviceName = deviceBean.getDeviceName();
        deviceSecret = deviceBean.getDeviceSecret();
        InitManager.init(context, productKey, deviceName, deviceSecret, new IDemoCallback() {
            @Override
            public void onNotify(String connectId, String topic, AMessage aMessage) {
                // 云端下行数据回调
                // connectId 连接类型 topic 下行 topic aMessage 下行数据
//                Log.d(TAG, "onNotify() called with: s = [" + s + "], s1 = [" + s1 + "], aMessage = [" + aMessage + "]");
                resultMsg = new String((byte[]) aMessage.data);

                showLog("云端下行数据回调onNotify() called with: s = [" + connectId + "], s1 = [" + topic + "], aMessage = [" + aMessage + "]"+"，getData():"+new String((byte[]) aMessage.data));
                showLog("打印aMessage:"+aMessage);
                EventBus.getDefault().post(resultMsg);
            }

            @Override
            public boolean shouldHandle(String connectId, String topic) {
                // 选择是否不处理某个 topic 的下行数据
                // 如果不处理某个topic，则onNotify不会收到对应topic的下行数据
//                Log.d(TAG, "shouldHandle() called with: s = [" + s + "], s1 = [" + s1 + "]");
                showLog("shouldHandle() called with: s = [" + connectId + "], s1 = [" + topic + "]");
                return true;
            }

            @Override
            public void onConnectStateChange(String s, ConnectState connectState) {
                Log.d(TAG, "onConnectStateChange() called with: s = [" + s + "], connectState = [" + connectState + "]");
                if (connectState == ConnectState.CONNECTED) {
                    showLog("长链接已建联");
                } else if (connectState == ConnectState.CONNECTFAIL) {
                    showLog("长链接建联失败");
                } else if (connectState == ConnectState.DISCONNECTED) {
                    showLog("长链接已断连");
                }
            }

            @Override
            public void onError(AError aError) {
                Log.d(TAG, "onError() called with: aError = [" + aError + "]");
                showLog("初始化失败");
            }

            @Override
            public void onInitDone(Object data) {
                Log.d(TAG, "onInitDone() called with: data = [" + data + "]");
                showLog("初始化成功");
                isInitDone = true;
                gettable.handleConectResult(isInitDone);
//                EventBus.getDefault().post(new ResultMessage(isInitDone));
            }
        });
    }

    @Override
    public void setDeviceMsg(DeviceBean deviceMsg) {
        this.deviceBean = deviceMsg;
    }

    @Override
    public String getResult() {
        return resultMsg;
    }

    private void showLog(String msg) {
        Log.e(TAG, "I want to see -- showLog: "+msg );
    }

}
