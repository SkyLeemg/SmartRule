package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.bean.CheckUpdataMsg;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.helper.UpdateHelper;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 检查APP更新
 */
public class UpdateAppVersionIntentService extends IntentService {

    public UpdateAppVersionIntentService() {
        super("UpdateAppVersionIntentService");
    }

    public UpdateAppVersionIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        StringBuffer url = new StringBuffer();
        url.append(NetConstant.baseUrl);
        url.append(NetConstant.check_update_url);
        url.append("?");
        url.append(NetConstant.check_update_type);
        url.append("=");
        url.append("app");
        url.append("&");
        url.append(NetConstant.check_update_app_name);
        url.append("=");
        url.append("app");
        LogUtils.show("更新固件，查看请求链接：" + url.toString());
        OkHttpUtils.get(url.toString(), resultCallback);
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
                    checkUpdataMsg.setVerCode(serviceVerCode);
                    checkUpdataMsg.setVerName(object.optString("version_number"));
                    checkUpdataMsg.setAppName(object.optString("app_name"));
                    checkUpdataMsg.setDownloadUrl(object.optString("apk_url"));
                    checkUpdataMsg.setFileSize(object.optString("file_size"));
                    checkUpdataMsg.setFileName(object.optString("filename"));
                    checkUpdataMsg.setUpdateLog(object.optString("update_log"));
                    checkUpdataMsg.setUpdate_flag(2);
                    /**
                     * ……
                     * 暂时省略判断是否更新
                     */
                    LogUtils.show("查看发送前的checkupdateMsg对象："+checkUpdataMsg);
                    UpdateHelper updateHelper = new UpdateHelper(getApplicationContext());
                    checkUpdataMsg.setSuccess(true);
                    if (serviceVerCode >updateHelper.getVercode()) {
                        checkUpdataMsg.setNeedUpdate(true);
                        EventBus.getDefault().post(checkUpdataMsg);
                    } else {
                        checkUpdataMsg.setNeedUpdate(false);
                        EventBus.getDefault().post(checkUpdataMsg);
                    }
//                    bleDeviceDbHelper.close();
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
            checkUpdataMsg.setSuccess(true);
            checkUpdataMsg.setNeedUpdate(false);
            checkUpdataMsg.setMsg("网络请求失败");
            EventBus.getDefault().post(checkUpdataMsg);
        }
    };

}

