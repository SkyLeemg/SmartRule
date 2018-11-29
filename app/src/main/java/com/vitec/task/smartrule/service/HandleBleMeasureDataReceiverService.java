package com.vitec.task.smartrule.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.interfaces.IBleCallBackResult;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.LogUtils;

import java.io.UnsupportedEncodingException;

/**
 * 在该服务注册一个广播接收者，用于处理靠尺的测量数据-保存到数据库
 * 问题：
 1.要从进入测量开始注册，点击结束测量才注销。实现此功能只能再开一个service与Recevice绑定，
 2.处理数据需要获取点击进入测量后的RulerCheckOptions集合的对象，实现此功能需要解决Service与Activity的通信问题；
 3.上传完数据后需要更新数据里面的server_id字段。实现此功能有两种方案：解决service与service的通信问题，把id直接传过来，或者通知recevice数据更新完毕，自己去数据库获取
 4.要将蓝牙数据保存到哪个管控要点下面，需要等服务器将模板数据做好才能继续往下做
 */
public class HandleBleMeasureDataReceiverService extends Service {


    public ConnectDeviceService bleService = null;
    private TextToSpeechHelper textToSpeechHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.show("HandleBleMeasureDataReceiverService创建了");
        textToSpeechHelper = new TextToSpeechHelper(this, "");
        textToSpeechHelper.speakChinese("初始化成功");
        registerBleRecevier();
        bindBleService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    public static void startHandleService(Context context) {
        Intent serviceIntent = new Intent(context, HandleBleMeasureDataReceiverService.class);
        context.startService(serviceIntent);
    }

    public static void stopHandleService(Context context) {
        Intent serviceIntent = new Intent(context, HandleBleMeasureDataReceiverService.class);
        context.stopService(serviceIntent);
    }

    /**
     * 由于很多activity都会用到绑定服务，所以做在BaseActivity,需要用到的类直接调用此方法进行绑定
     */
    private void bindBleService() {
        Intent bindIntent = new Intent(this, ConnectDeviceService.class);
        boolean isSuccess = bindService(bindIntent, mBleServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 绑定了服务的类，在销毁之前要解绑
     */
    private void unbindBleService() {
        unbindService(mBleServiceConnection);
    }

    private ServiceConnection mBleServiceConnection=new ServiceConnection() {
        //        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)1
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bleService = ((ConnectDeviceService.LocalBinder) iBinder).getService();
            LogUtils.show("HandleBleMeasure---绑定成功，正在监听水平度的通知。。。");
            bleService.enableTXNotification(ConnectDeviceService.LEVELNESS_SERVICE_UUID,ConnectDeviceService.LEVELNESS_TX_CHAR_UUID);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };


    /**
     * 注册接收蓝牙状态的广播接收器
     * @param
     */
    private void registerBleRecevier() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(BleDeviceReceiver,makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBleRecevier();
        unbindBleService();
    }

    /**
     * 注销广播接受者
     */
    public void unregisterBleRecevier() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(BleDeviceReceiver);
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleParam.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleParam.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleParam.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }


    private final BroadcastReceiver BleDeviceReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            //*********************//
            /**
             * 蓝牙连接成功
             */
            if (action.equals(BleParam.ACTION_GATT_CONNECTED)) {
                textToSpeechHelper.speakChinese("蓝牙连接成功");
            }

            //*********************//
            /**
             * 蓝牙连接断开
             */
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
//                bleCallBackResult.bleDisconnected();
                textToSpeechHelper.speakChinese("蓝牙连接断开");
            }


            //*********************//
            /**
             * 发现服务
             */
            if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
//                发现一个服务
//                bleCallBackResult.bleDiscoveredService();
            }
            //*********************//
            /**
             * 收到蓝牙数据
             */
            if (action.equals(BleParam.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(BleParam.EXTRA_DATA);
                final String uuid = intent.getStringExtra(BleParam.EXTRA_UUID);

                try {
                    String text = new String(txValue, "UTF-8");
                    /**
                     * 收到水平度的数据
                     */
                    if (uuid.equalsIgnoreCase(ConnectDeviceService.LEVELNESS_TX_CHAR_UUID.toString())) {
                        textToSpeechHelper.speakChinese("收到水平度数据"+text);
                    }

                    /**
                     * 收到垂直度的数据
                     */
                    else if (uuid.equalsIgnoreCase(ConnectDeviceService.VERTICALITY_TX_CHAR_UUID.toString())) {
                        textToSpeechHelper.speakChinese("收到垂直度数据"+text);
                    }

                    LogUtils.show("HandleBleMeasure---在服务中收到蓝牙数据：" + text);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            //*********************//
            if (action.equals(BleParam.DEVICE_DOES_NOT_SUPPORT_UART)){
//
//                mService.disconnect();
            }
        }
    };

}
