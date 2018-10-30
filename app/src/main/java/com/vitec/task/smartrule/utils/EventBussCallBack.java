package com.vitec.task.smartrule.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.vitec.task.smartrule.bean.BleMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventBussCallBack {

    private static final String TAG = "EventBussCallBack";
    private Context mContext;


    public EventBussCallBack(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 蓝牙服务返回的数据
     * @param
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connectBussCallBack(BleMessage message) {
        if (message.getAction().equals(BleParam.ACTION_GATT_CONNECTED)) {
            Toast.makeText(mContext, "连接成功",Toast.LENGTH_SHORT).show();
        }

        if (message.getAction().equals(BleParam.ACTION_GATT_DISCONNECTED)) {
            Toast.makeText(mContext, "连接断开",Toast.LENGTH_SHORT).show();
        }

    }

    private void showlog(String msg) {
        Log.e(TAG, "showlog: 查看eventbus里面的log："+msg );
    }

}
