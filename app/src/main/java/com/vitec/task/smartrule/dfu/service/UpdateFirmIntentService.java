package com.vitec.task.smartrule.dfu.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.bean.CheckUpdataMsg;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * 检查靠尺的更新，固件版本信息，
 */
public class UpdateFirmIntentService extends IntentService {

    public static final String DEAL_FLAG_KEY = "deal_flag";
    public static final int DEAL_FLAG_CHECK_UPDATE = 1;
    public static final int DEAL_FLAG_UPDATE_FIRM = 2;

    public static final String VERSION_FLAG = "deal_version";
    public static final String DEVICE_FLAG = "current_device";


    public static final String PATH_KEY = "path";


    public UpdateFirmIntentService() {
        super("UpdateFirmIntentService");
    }

    public UpdateFirmIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int deal_flag = intent.getIntExtra(DEAL_FLAG_KEY, 0);
        switch (deal_flag) {
            /**
             * 向服务器请求检查更新接口
             */
            case DEAL_FLAG_CHECK_UPDATE:
                /******************接收本地靠尺蓝牙的版本号***********************/
                String data = intent.getStringExtra(VERSION_FLAG);
                if (data != null) {
                    String head = data.substring(0, 3);
                    String codeString = data.substring(data.indexOf(':') + 1, data.indexOf(','));
                    String verName = data.substring(data.indexOf(',') + 1);
                    LogUtils.show("分别打印出头部：" + head + ",版本号：" + codeString + ",版本名：" + verName);
                    BleDeviceDbHelper bleDeviceDbHelper = new BleDeviceDbHelper(getApplicationContext());
                    BleDevice bleDevice = bleDeviceDbHelper.getCurrentConnectDevice();
                    LogUtils.show("在更新服务里面，查看当前连接的设备：" + bleDevice);
                    int verCode = Integer.parseInt(codeString);
                    ContentValues values = new ContentValues();
                    values.put(DataBaseParams.ble_ver_name, verName);
                    values.put(DataBaseParams.ble_ver_code, verCode);
                    bleDeviceDbHelper.updateDevice(values, new String[]{String.valueOf(bleDevice.getId())});



                    /*******************请求服务器靠尺的版本信息*************************/
                    StringBuffer url = new StringBuffer();
                    url.append(NetConstant.baseUrl);
                    url.append(NetConstant.check_update_url);
                    url.append("?");
                    url.append(NetConstant.check_update_type);
                    url.append("=");
                    url.append("hardware");
                    url.append("&");
                    url.append(NetConstant.check_update_app_name);
                    url.append("=");
                    url.append("hardware");
                    LogUtils.show("更新固件，查看请求链接："+url.toString());
                    OkHttpUtils.get(url.toString(),resultCallback);
                    bleDeviceDbHelper.close();
                }




                break;

            /**
             * 确定要更新时，向靠尺发送升级请求
             */
            case DEAL_FLAG_UPDATE_FIRM:

        }

    }

    OkHttpUtils.ResultCallback<String> resultCallback=new OkHttpUtils.ResultCallback<String>() {
        @Override
        public void onSuccess(String response) {
            LogUtils.show("查看更新软件的返回信息："+response);
            /**
             * {
             *   "status":"success",
             *   "code":200,
             *   "data":{
             *      "version_number":"1.0.1",
             *      "version_code":2,
             *      "apk_url":"http:\/\/iot.vkforest.com\/Version\/20181126\/413e6f82aa228ae651ff9b01d0ef7ca8.zip",
             *      "app_name":"kaochi",
             *      "filename":"updata",
             *      "file_size":"23",
             *      "update_log":"1.更新了‘\r\n2.更新了啊啊啊\r\n3.更新了呵呵呵呵",
             *      "create_time":"2018-11-26 16:15:25"},
             *    "msg":"查询成功"}
             */

            /**
             * 1.获取本地靠尺固件的版本号
             * 2.将服务器的靠尺版本号与本地的进行对比
             *   2.1 服务器的版本号比较大--更新靠尺固件
             *   2.2 否则不做更新
             */
            try {
                JSONObject rootJson = new JSONObject(response);
                if (rootJson.optInt("code") == 200) {
                    JSONObject object = new JSONObject(rootJson.optString("data"));
                    CheckUpdataMsg checkUpdataMsg = new CheckUpdataMsg();
                    int serviceVerCode = object.optInt("version_code");
                    checkUpdataMsg.setUpdate_flag(1);
                    checkUpdataMsg.setVerCode(serviceVerCode);
                    checkUpdataMsg.setVerName(object.optString("version_number"));
                    checkUpdataMsg.setAppName(object.optString("app_name"));
                    checkUpdataMsg.setDownloadUrl(object.optString("apk_url"));
                    checkUpdataMsg.setFileSize(object.optString("file_size"));
                    checkUpdataMsg.setFileName(object.optString("filename"));
                    checkUpdataMsg.setUpdateLog(object.optString("update_log"));
                    /**
                     * ……
                     * 暂时省略判断是否更新
                     */
                    LogUtils.show("查看发送前的checkupdateMsg对象："+checkUpdataMsg);
                    BleDeviceDbHelper bleDeviceDbHelper = new BleDeviceDbHelper(getApplicationContext());
                    BleDevice currentBle = bleDeviceDbHelper.getCurrentConnectDevice();
                    LogUtils.show("获取更新后的当前设备信息："+currentBle);
                    checkUpdataMsg.setSuccess(true);
                    checkUpdataMsg.setMsg("返回成功");
                    if (serviceVerCode > currentBle.getBleVerCode()) {
                        checkUpdataMsg.setNeedUpdate(true);
                        EventBus.getDefault().post(checkUpdataMsg);
                    } else {
                        checkUpdataMsg.setNeedUpdate(false);
                        EventBus.getDefault().post(checkUpdataMsg);
                    }
                    bleDeviceDbHelper.close();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                CheckUpdataMsg checkUpdataMsg = new CheckUpdataMsg();
                checkUpdataMsg.setSuccess(true);
                checkUpdataMsg.setMsg("数据解析失败");
                checkUpdataMsg.setNeedUpdate(false);
                EventBus.getDefault().post(checkUpdataMsg);
            }


        }

        @Override
        public void onFailure(Exception e) {
            CheckUpdataMsg checkUpdataMsg = new CheckUpdataMsg();
            checkUpdataMsg.setSuccess(false);
            checkUpdataMsg.setMsg("网络请求失败");
            checkUpdataMsg.setNeedUpdate(false);
            EventBus.getDefault().post(checkUpdataMsg);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("升级服务销毁了");
    }
}
