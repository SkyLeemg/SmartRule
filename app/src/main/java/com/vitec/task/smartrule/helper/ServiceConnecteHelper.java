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
import com.vitec.task.smartrule.utils.ServiceUtils;

import static com.aliyun.alink.linksdk.tmp.TmpSdk.getContext;

public class ServiceConnecteHelper {

    private static final String TAG = "ServiceConnecteHelper";
    private Context mContext;
    private ConnectDeviceService mService = null;
    private String macAddress;
    private IDialogCommunicableWithDevice communicable;
    private boolean isbound = false;

    public ServiceConnecteHelper(Context context,String macAddress) {
        mContext = context;
        this.macAddress = macAddress;
        service_init();
    }

    public ServiceConnecteHelper(Context context,String macAddress,IDialogCommunicableWithDevice communicable) {
        mContext = context;
        this.macAddress = macAddress;
        this.communicable = communicable;
        ServiceUtils.startConnectDeviceSerivce(context);
        service_init();

    }

    public ServiceConnecteHelper(Context context) {
        mContext = context;
    }


    public void service_init() {
        Intent bindIntent = new Intent(mContext, ConnectDeviceService.class);
        isbound=mContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "service_init: 查看是发绑定成功："+isbound );
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

            boolean connectResult = mService.connect(macAddress);
//            context.unbindService(mServiceConnection)
            Log.e(TAG, "onServiceConnected: 服务绑定完成,查看是否连接成功：" + connectResult);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected: 连接断开了" );
            mService = null;
        }
    };


    public void stopServiceConnection() {
        if (isbound) {
            mContext.unbindService(mServiceConnection);
            isbound = false;
        }

    }
}
