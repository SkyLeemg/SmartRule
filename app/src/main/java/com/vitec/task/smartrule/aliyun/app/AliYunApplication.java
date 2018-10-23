package com.vitec.task.smartrule.aliyun.app;

import android.app.Application;
import android.util.Log;

import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.google.gson.Gson;
import com.vitec.task.smartrule.aliyun.manager.IDemoCallback;
import com.vitec.task.smartrule.aliyun.manager.InitManager;

public class AliYunApplication extends Application {

    private static final String TAG = "DeviceApplication";
    public static boolean isInitDone = false;
    public  String productKey = "a1UNvLhFILp", deviceName = "testS",
            deviceSecret = "AO3zGlGP6ivc0JHHyY5Si5mS40UpGcco";

    @Override
    public void onCreate() {
        super.onCreate();
        ALog.setLevel(ALog.LEVEL_DEBUG);
//        从raw读取指定测试文件
//        String testData = getFromRaw();
//        解析数据
//        getDeivceInfoFrom(testData);
//        connect();
    }

    public void connect() {
        showLog("连接被调用");
        InitManager.init(this, productKey, deviceName, deviceSecret, new IDemoCallback() {
            @Override
            public void onNotify(String connectId, String topic, AMessage aMessage) {
                // 云端下行数据回调
                // connectId 连接类型 topic 下行 topic aMessage 下行数据
//                Log.d(TAG, "onNotify() called with: s = [" + s + "], s1 = [" + s1 + "], aMessage = [" + aMessage + "]");
                showLog("云端下行数据回调onNotify() called with: s = [" + connectId + "], s1 = [" + topic + "], aMessage = [" + aMessage + "]"+"，getData():"+new String((byte[]) aMessage.data));
                showLog("打印aMessage:"+aMessage);
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
            }
        });

    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    private void getDeivceInfoFrom(String testData) {
        showLog("查看需要解析的数据："+testData);
        try {
            Gson mGson = new Gson();

        } catch (Exception e) {

        }
    }

    public static AliYunApplication getAppObject() {
        return new AliYunApplication();
    }

    private void showLog(String msg) {
        Log.e(TAG, "I want to see -- showLog: "+msg );
    }

}
