package com.vitec.task.smartrule.view;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.BleDeviceAdapter;
import com.vitec.task.smartrule.helper.ServiceConnecteHelper;
import com.vitec.task.smartrule.interfaces.IDialogCommunicableWithDevice;
import com.vitec.task.smartrule.service.BleScanService;
import com.vitec.task.smartrule.service.ConnectDeviceService;

import org.altbeacon.beacon.Beacon;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ConnectDialog extends Dialog {

    private static final String TAG = "ConnectDialog";
    private ListView listView;
    private BleScanService bleScanService;
    private BleDeviceAdapter mBleDeviceAdapter;
    private List<Beacon> devices;
    private MKLoader mkLoader;
    private TextView tvloaderTip;
    public   static ServiceConnecteHelper serviceConnecteHelper;

    private ConnectDeviceService mService = null;
    private IDialogCommunicableWithDevice communicableWithDevice;

    public ConnectDialog(@NonNull Context context, IDialogCommunicableWithDevice communicable) {
        super(context);
        this.communicableWithDevice = communicable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_device);
        initView();
        EventBus.getDefault().register(this);
//        BleScanService.startScanService(getContext());

        initData();
    }



    private void initView() {
        listView = findViewById(R.id.lv_bledev_list);
        mkLoader = findViewById(R.id.mkloader);
        tvloaderTip = findViewById(R.id.tv_loading_tip);
        mkLoader.setVisibility(View.VISIBLE);
    }

    private void initData() {
        Intent bindIntent = new Intent(getContext(), BleScanService.class);
        getContext().bindService(bindIntent, scanServiceConnection, Context.BIND_AUTO_CREATE);
        devices = new ArrayList<>();
        mBleDeviceAdapter = new BleDeviceAdapter(devices, getContext());
        listView.setAdapter(mBleDeviceAdapter);
        listView.setOnItemClickListener(itemClickListener);
    }

    private ServiceConnection scanServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Beacon beacon = devices.get(i);
            String macAddress = beacon.getBluetoothAddress();

            serviceConnecteHelper = new ServiceConnecteHelper(getContext(), macAddress);
            communicableWithDevice.connectingDevice();
            communicableWithDevice.setConnectingDevice(beacon);//将用户点击的设备发给管理界面
        }
    };



    /**
     * 接收从搜索服务里发送过来的数据
     * @param
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void beaconBussCallBack(List<Beacon> beacons) {
        mkLoader.setVisibility(View.GONE);
        tvloaderTip.setVisibility(View.GONE);
        Log.e(TAG, "beaconBussCallBack: 查看回调的beacon:"+beacons.get(0).toString() );
        displayBleDevice(beacons);
    }

    /**
     * 显示搜索到的设备信息
     * @param beacons
     */
    private void displayBleDevice(List<Beacon> beacons) {
        if ( mBleDeviceAdapter== null) {
            mBleDeviceAdapter = new BleDeviceAdapter(beacons, getContext());
            listView.setAdapter(mBleDeviceAdapter);
        } else {
            devices = beacons;
            mBleDeviceAdapter.setBeacons(devices);
            mBleDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BleScanService.stopScanService(getContext());

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "vitec onStop: 连接对话框断开");
//        BleScanService.stopScanService(getContext());
//        getContext().unbindService(scanServiceConnection);
//        serviceConnecteHelper.stopServiceConnection();
    }
}
