package com.vitec.task.smartrule.dfu.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.bean.CheckUpdataMsg;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
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

public class UpdateFirmIntentService extends IntentService {

    public static final String DEAL_FLAG_KEY = "deal_flag";
    public static final int DEAL_FLAG_CHECK_UPDATE = 1;
    public static final int DEAL_FLAG_UPDATE_FIRM = 2;

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
                url.append("靠尺");
                LogUtils.show("更新固件，查看请求链接："+url.toString());
                OkHttpUtils.get(url.toString(),resultCallback);

                break;

            /**
             * 确定要更新时，向靠尺发送升级请求
             */
            case DEAL_FLAG_UPDATE_FIRM:
                DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);
                String path = intent.getStringExtra(PATH_KEY);
                BleDeviceDbHelper bleDeviceDbHelper = new BleDeviceDbHelper(getApplicationContext());
                List<BleDevice> bleDevices = bleDeviceDbHelper.queryAllDevice();
                String mac = ConnectDeviceService.current_connecting_mac_address;
                BleDevice currentDev = null;
                for (int i=0;i<bleDevices.size();i++) {
                    if (ConnectDeviceService.current_connecting_mac_address.equals(bleDevices.get(i).getBleMac())) {
                        currentDev = bleDevices.get(i);
                    }
                }
                if (currentDev != null) {
                    LogUtils.show("升级服务----正在启动");
                    DfuServiceInitiator starter = new DfuServiceInitiator(currentDev.getBleMac())
                            .setDeviceName(currentDev.getBleName())
                            .setKeepBond(true);
                    starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
                    File file = new File(path);
                    LogUtils.show("升级服务----查看升级包路径："+path);
                    Uri contentUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.vitec.task.smartrule.fileprovider", file);
                    } else {
                        contentUri = Uri.fromFile(file);
                    }
                    starter.setZip(contentUri,path);
                    DfuServiceController controller = starter.start(getApplicationContext(), DfuService.class);

                }

                break;
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
                    checkUpdataMsg.setVerCode(object.optInt("version_code"));
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
                    EventBus.getDefault().post(checkUpdataMsg);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onFailure(Exception e) {

        }
    };


    private final DfuProgressListener dfuProgressListener = new DfuProgressListener() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            LogUtils.show("DfuProgressListener----正在连接"+deviceAddress);
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            LogUtils.show("DfuProgressListener----已连接"+deviceAddress);
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            LogUtils.show("DfuProgressListener----进度开始中。。。"+deviceAddress);
        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            LogUtils.show("DfuProgressListener----进度已经开始。。。"+deviceAddress);
        }

        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onEnablingDfuMode。。。"+deviceAddress);
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
           LogUtils.show("升级进度: " + deviceAddress + "百分比" + percent + ",speed "
                    + speed + ",avgSpeed " + avgSpeed + ",currentPart " + currentPart
                    + ",partTotal " + partsTotal);
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onFirmwareValidating。。。"+deviceAddress);
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDeviceDisconnecting。。。"+deviceAddress);
        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDeviceDisconnected。。。"+deviceAddress);
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDfuCompleted。。。"+deviceAddress);
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDfuAborted。。。"+deviceAddress);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            LogUtils.show("DfuProgressListener----错误："+deviceAddress+",");
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        DfuServiceListenerHelper.unregisterProgressListener(UpdateFirmIntentService.this, dfuProgressListener);
        LogUtils.show("升级服务销毁了");
    }
}
