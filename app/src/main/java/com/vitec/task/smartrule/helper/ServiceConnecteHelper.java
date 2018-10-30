package com.vitec.task.smartrule.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vitec.task.smartrule.interfaces.IDialogCommunicableWithDevice;
import com.vitec.task.smartrule.service.ConnectDeviceService;

public class ServiceConnecteHelper {

    private static final String TAG = "ServiceConnecteHelper";
    private Context mContext;
    private ConnectDeviceService mService = null;
    private String macAddress;
    private IDialogCommunicableWithDevice communicable;

    public ServiceConnecteHelper(Context context,String macAddress) {
        mContext = context;
        this.macAddress = macAddress;
        service_init();
    }

    public ServiceConnecteHelper(Context context,String macAddress,IDialogCommunicableWithDevice communicable) {
        mContext = context;
        this.macAddress = macAddress;
        this.communicable = communicable;
        service_init();

    }

    public ServiceConnecteHelper(Context context) {
        mContext = context;
    }


    public void service_init() {
        Intent bindIntent = new Intent(mContext, ConnectDeviceService.class);
        boolean isSuccess=mContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "service_init: 查看是发绑定成功："+isSuccess );
        ConnectDeviceService.startDeviceService(mContext.getApplicationContext());
    }


    private ServiceConnection mServiceConnection=new ServiceConnection() {
        //        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((ConnectDeviceService.LocalBinder) iBinder).getService();
            Log.e(TAG, "onServiceConnected: mService=" + mService );
            if (!mService.initialize()) {
                Log.e(TAG, "onServiceConnected: 不能初始化蓝牙" );
            }
            Log.e(TAG, "onServiceConnected: 服务绑定完成");
            mService.connect(macAddress);
//            context.unbindService(mServiceConnection)
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected: 连接断开了" );
            mService = null;
        }
    };


    public void stopServiceConnection() {
        mContext.unbindService(mServiceConnection);
    }
}
