package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.activity.CaptureActivity;
import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.BleDeviceAdapter;
import com.vitec.task.smartrule.aliyun.DeviceBean;
import com.vitec.task.smartrule.aliyun.impl.AliYunConnectImpl;
import com.vitec.task.smartrule.aliyun.interfaces.IAliYunConnector;
import com.vitec.task.smartrule.bean.ResultMessage;
import com.vitec.task.smartrule.interfaces.MainActivityGettable;
import com.vitec.task.smartrule.listener.DeviceItemClickListener;
import com.vitec.task.smartrule.utils.ParameterKey;

import org.altbeacon.beacon.Beacon;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends BaseActivity implements MainActivityGettable,View.OnClickListener {

    private static final String TAG = "DeviceActivity";
    private static final int REQUEST_CODE = 0x01;
    //打开扫描界面请求码
    //扫描成功返回码
    private int RESULT_OK = 0xA1;

    private ListView lvBleDevice;
    private BleDeviceAdapter mBleDeviceAdapter;
    private List<Beacon> devices;

    private DeviceItemClickListener mDeviceItemClickListener;
    private TextView tvMsg;
    private MKLoader mkLoader;

    private IAliYunConnector mConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initView();
        EventBus.getDefault().register(this);
        checkBleEnable();
        requestLocationPermissions();
        initData();
        initService();
    }



    /**
     * 用于检查手机蓝牙设备是否可以，以及蓝牙设备是否有打开
     */
    private void checkBleEnable() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("错误").setMessage("你的设备不具备蓝牙功能!").create();
            dialog.show();
            return;
        }

        if(!adapter.isEnabled()) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("提示")
                    .setMessage("蓝牙设备未打开,请开启此功能后重试!")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(mIntent, 1);
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this,"正在搜索设备", Toast.LENGTH_SHORT).show();
//        EventBus.getDefault().register(this);
        initService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: 主界面停止了" );
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeviceItemClickListener.UARTStatusChangeReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        BleScanService.stopScanService(this);

        Log.e(TAG, "onDestroy: 主界面销毁了" );
    }


    @Override
    protected void onPause() {
        super.onPause();
//        EventBus.getDefault().unregister(this);
//        BleScanService.stopScanService(this);
    }

    private void initService() {
//        BleScanService.startScanService(this);
    }

    private void initData() {
        devices = new ArrayList<>();
        mBleDeviceAdapter = new BleDeviceAdapter(devices, this);
//        lvBleDevice.setAdapter(mBleDeviceAdapter);
        mDeviceItemClickListener = new DeviceItemClickListener(this, this);
//        lvBleDevice.setOnItemClickListener(mDeviceItemClickListener);
        mConnect = new AliYunConnectImpl();
    }

    private void initView() {
        initWidget();
        imgIcon.setVisibility(View.VISIBLE);
        tvMsg = findViewById(R.id.tv_msg);
//        lvBleDevice = (ListView) findViewById(R.id.lv_ble_device);
        imgIcon.setOnClickListener(this);
        mkLoader = findViewById(R.id.loading);
        mkLoader.setVisibility(View.GONE);

    }

    /**
     * 接收从搜索服务里发送过来的数据
     * @param result
     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void beaconBussCallBack(List<Beacon> beacons) {
//        Log.e(TAG, "beaconBussCallBack: 查看回调的beacon:"+beacons.get(0).toString() );
//        displayBleDevice(beacons);
//    }

    /**
     * 接受服务器下发的数据
     * @param result
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void beaconBussCallBack(String result) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void initDoneBussCallBack(ResultMessage message) {
        mkLoader.setVisibility(View.GONE);
        Log.e(TAG, "initDoneBussCallBack: 收到一个数据："+message.isDone() );
        if (message.isDone()) {
            Toast.makeText(getApplicationContext(),"阿里连接成功",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 显示搜索到的设备信息
     * @param beacons
     */
    private void displayBleDevice(List<Beacon> beacons) {
        if ( mBleDeviceAdapter== null) {
            mBleDeviceAdapter = new BleDeviceAdapter(beacons, this);
            lvBleDevice.setAdapter(mBleDeviceAdapter);

        } else {
            devices = beacons;
            mBleDeviceAdapter.setBeacons(devices);
            mBleDeviceAdapter.notifyDataSetChanged();
        }
    }

    public List<Beacon> getDevices() {
        return devices;
    }

    @Override
    public void handleConectResult(final boolean isDone) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mkLoader.setVisibility(View.GONE);
                if (isDone) {
                    Toast.makeText(getApplicationContext(),"连接成功",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void handleServerMessage(String result) {

    }


    public void setDevices(List<Beacon> devices) {
        this.devices = devices;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
           case  R.id.img_icon_toolbar:
               //打开二维码扫描界面
               Intent arIntent = new Intent(this, CaptureActivity.class);
               startActivityForResult(arIntent,REQUEST_CODE);
            break;
            default:break;
        }
    }

    /**
     * {
     "productKey":"a1UNvLhFILp",
     "deviceName":"testS",
     "deviceSecret":"AO3zGlGP6ivc0JHHyY5Si5mS40UpGcco",
     "bleMac":"FC:04:35:B7:9A:D8"
     }
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            mkLoader.setVisibility(View.VISIBLE);
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
            tvMsg.setText(scanResult);
            try {
                JSONObject json = new JSONObject(scanResult);
                String productKey = json.optString(ParameterKey.product_key);
                String deviceName = json.optString(ParameterKey.device_name);
                String deviceSecret = json.optString(ParameterKey.device_secret);
                String bleMac = json.optString(ParameterKey.ble_mac);
                DeviceBean deviceBean = new DeviceBean(productKey, deviceName, deviceSecret);
                mConnect.setDeviceMsg(deviceBean);
                mConnect.connect(getApplicationContext(),this);

            } catch (JSONException e) {
                e.printStackTrace();
                mkLoader.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"错误的二维码",Toast.LENGTH_SHORT).show();
            }

            Log.e(TAG, "onActivityResult: 查看扫码返回值："+scanResult );
        }
    }
}
