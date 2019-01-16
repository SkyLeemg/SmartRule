package com.vitec.task.smartrule.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.activity.CaptureActivity;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.interfaces.IBleCallBackResult;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

import static com.aliyun.alink.linksdk.tools.ThreadTools.runOnUiThread;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_CONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_DISCONNECTED;

public class BaseActivity extends FragmentActivity {

    public ImageView imgMenu;
    public TextView tvTitle;
    public ImageView imgIcon;
    public TextView tvChoose;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public ConnectDeviceService bleService = null;
    public IBleCallBackResult bleCallBackResult = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

    }

    /**
     * 由于很多activity都会用到绑定服务，所以做在BaseActivity,需要用到的类直接调用此方法进行绑定
     */
    public void bindBleService() {
        Intent bindIntent = new Intent(this, ConnectDeviceService.class);
        boolean isSuccess = bindService(bindIntent, mBleServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 绑定了服务的类，在销毁之前要解绑
     */
    public void unbindBleService() {
        unbindService(mBleServiceConnection);
    }


    private ServiceConnection mBleServiceConnection=new ServiceConnection() {
        //        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bleService = ((ConnectDeviceService.LocalBinder) iBinder).getService();
            if (bleCallBackResult != null) {
                bleCallBackResult.bleBindSuccess();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };



    /**
     * 动态申请蓝牙定位权限
     */
    public void requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("请求位置权限");
                builder.setMessage("该APP需要定位权限");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });

                builder.show();

            }

            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("请求读写权限");
                builder.setMessage("该读写文件");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });

                builder.show();

            }

        }
    }



    /**
     * 动态权限回调方法
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:

                break;
        }
    }
    public void initWidget() {
        imgIcon = findViewById(R.id.img_icon_toolbar);
        imgMenu = findViewById(R.id.img_menu_toolbar);
        tvTitle = findViewById(R.id.tv_toolbar_title);
        tvChoose = findViewById(R.id.tv_choose);

        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

//        imgIcon.setOnClickListener(this);
    }

    public void setTvTitle(String title) {
        tvTitle.setText(title);
    }

    public void setImgSource(int menuSource, int iconSource) {
        imgMenu.setImageResource(menuSource);
        imgIcon.setImageResource(iconSource);
    }


    /**
     * 注册接收蓝牙状态的广播接收器
     * @param iBleCallBackResult
     */
    public void registerBleRecevier(IBleCallBackResult iBleCallBackResult) {
        this.bleCallBackResult = iBleCallBackResult;
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(BleDeviceReceiver,makeGattUpdateIntentFilter());
    }


    /**
     * 注销广播接受者
     */
    public void unregisterBleRecevier() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(BleDeviceReceiver);
    }

    private   IntentFilter makeGattUpdateIntentFilter() {
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
               bleCallBackResult.bleConnectSuccess();
            }

            //*********************//
            /**
             * 蓝牙连接断开
             */
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
                bleCallBackResult.bleDisconnected();
            }


            //*********************//
            /**
             * 发现服务
             */
            if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
//                发现一个服务
                bleCallBackResult.bleDiscoveredService();
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
                    bleCallBackResult.bleReceviceData(text, uuid);
                    LogUtils.show("在base界面中收到蓝牙数据：" + text);
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
