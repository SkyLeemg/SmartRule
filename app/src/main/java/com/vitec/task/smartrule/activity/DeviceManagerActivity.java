package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.ConnectionService;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.DisplayBleDeviceAdapter;
import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.helper.ServiceConnecteHelper;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.interfaces.IDevManager;
import com.vitec.task.smartrule.interfaces.IDialogCommunicableWithDevice;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.service.intentservice.GetMudelIntentService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.view.ConnectDialog;
import com.vitec.task.smartrule.view.LoadingDialog;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

public class DeviceManagerActivity extends BaseActivity implements View.OnClickListener,IDialogCommunicableWithDevice,IDevManager{

    private static final String TAG = "DeviceManagerActivity";
    private LinearLayout llAddDev;
    private TextView tvNoRuleDev;
    private TextView tvNoLaserDev;
    private GridView gvRule;
    private GridView gvLaser;
    private List<BleDevice> rules;
    private DisplayBleDeviceAdapter ruleDevAdapter;
    private ConnectDialog mDialog;
    private LoadingDialog mLoadingDialog;
    private boolean hasUnbind = false;
    private Beacon beacon;//点击添加设备时，用户点击的那个蓝牙设备
    private BleDeviceDbHelper deviceDbHelper;
    private ServiceConnecteHelper serviceConnecteHelper;
    private TextToSpeechHelper mTextToSpeechHelper;
    private int current_connected_device_id;

    private ConnectDeviceService mService;
    private ConnectionService connectionService;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_device_manager_page);
        initView();
        initViewData();

        requestLocationPermissions();
    }

    private void initView() {
        initWidget();
        setTvTitle("设备管理");
        setImgSource(R.mipmap.icon_back,R.mipmap.icon_back);
        imgMenu.setVisibility(View.VISIBLE);
        imgMenu.setOnClickListener(this);

        llAddDev = findViewById(R.id.ll_add_dev);
        tvNoLaserDev = findViewById(R.id.tv_no_laser_dev);
        tvNoRuleDev = findViewById(R.id.tv_no_rule_dev);
        gvRule = findViewById(R.id.gv_rule);
        gvLaser = findViewById(R.id.gv_laser);

        llAddDev.setOnClickListener(this);
        mTextToSpeechHelper = new TextToSpeechHelper(getApplicationContext(),"");
    }

    private void initViewData() {
        /**
         * 从服务器获取工程和管控要点的模板数据
         */
        Intent intent = new Intent(this, GetMudelIntentService.class);
        startService(intent);

        rules = new ArrayList<>();
        deviceDbHelper = new BleDeviceDbHelper(getApplicationContext());
//        for (int i=0;i<5;i++) {
//            rules.add(i + "号设备");
//        }
        getRuleDevicefromDB();
        ruleDevAdapter = new DisplayBleDeviceAdapter(this ,rules,this);
        gvRule.setAdapter(ruleDevAdapter);
        if (rules.size() != 0) {
            tvNoRuleDev.setVisibility(View.GONE);
        }

        gvLaser.setVisibility(View.GONE);
        setListViewHeighBaseOnChildren(gvRule);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
        gvRule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                current_connected_device_id = i;
                if (ConnectDeviceService.current_connecting_mac_address.equals(rules.get(i).getBleMac())) {
                    Toast.makeText(getApplicationContext(), "蓝牙已连接", Toast.LENGTH_SHORT).show();
                } else {
                    mLoadingDialog = new LoadingDialog(DeviceManagerActivity.this, "正在连接");
                    mLoadingDialog.show();
                    serviceConnecteHelper = new ServiceConnecteHelper(getApplicationContext(),rules.get(i).getBleMac());
                }

            }
        });

    }


    private ServiceConnection mServiceConnection=new ServiceConnection() {
        //        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtils.show("service连接开始。。。。。。");
            mService = ((ConnectDeviceService.LocalBinder) iBinder).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected: 连接断开了" );
            mService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleParam.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    /**
     * 从sqlite中获取所有的设备
     */
    private void getRuleDevicefromDB() {
        rules = deviceDbHelper.queryAllDevice();
        updateDateState();
    }

    private void updateDateState() {
        int currentnum = -1;
        for (int i=0;i<rules.size();i++) {
            if (ConnectDeviceService.current_connecting_mac_address.equals(rules.get(i).getBleMac())) {
                currentnum = i;
            }
        }
        setDeviceImg(currentnum);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDateState();
    }

    private void showlog(String msg) {
        Log.e(TAG, "showlog: 查看eventbus里面的log："+msg );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(getActivity());
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(UARTStatusChangeReceiver);
        mTextToSpeechHelper.stopSpeech();
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();

        }
        if (!hasUnbind) {
            hasUnbind = true;
//            ConnectDialog.serviceConnecteHelper.stopServiceConnection();
            if (mDialog != null) {
                mDialog.unBindConnectService();
            }
            Log.e(TAG, "onDestroy: 连接解绑了" );
        }

        if (mService != null) {
            unbindService(mServiceConnection);
        }
    }

    /**
     * 设置显示的设备的图片的颜色，未连接的为灰白，连接成功的为彩色
     * @param flag 为连接成功的设备序号，序号为list集合的序号
     */
    private void setDeviceImg(int flag) {
        Log.e(TAG, "setDeviceImg: 修改图片的标志："+flag +",rulers内容："+rules.toString());
        for (int i=0;i<rules.size();i++) {
            rules.get(i).setImgResouce(R.mipmap.rule_unconnected);
        }
        if (flag >= 0 && rules.size() > 0) {
            rules.get(flag).setImgResouce(R.mipmap.rule);
        }
    }

    private void setListViewHeighBaseOnChildren(GridView gridView) {
        if (gridView == null) {
            return;
        }
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        int count = listAdapter.getCount() / 3;
        if (listAdapter.getCount() % 3 != 0) {
            count++;
        }
        for (int i = 0; i < count; i++) {
            View listItem = listAdapter.getView(i, null, gridView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();

        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_add_dev:
                Log.e(TAG, "onClick: 点击了添加设备按钮" );
                mDialog = new ConnectDialog(this,this);
                mDialog.show();
                break;
            case R.id.img_menu_toolbar:
                Log.e(TAG, "onClick: 点击了首页按钮" );
                Intent intent = new Intent(DeviceManagerActivity.this, MainHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                DeviceManagerActivity.this.finish();
                break;
        }
    }
    boolean flag = true;
    /**
     * 接受蓝牙服务返回的连接
     */
    public final BroadcastReceiver UARTStatusChangeReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
            if (mDialog != null) {
                if (!hasUnbind) { //判断对话框的连接断开了没有
                    hasUnbind = true;
//                    ConnectDialog.serviceConnecteHelper.stopServiceConnection();

                    mDialog.unBindConnectService();
                    Log.e(TAG, "onReceive: 服务解绑了" );
                }
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }

            }

            //*********************//
            if (action.equals(BleParam.ACTION_GATT_CONNECTED)) {
                Toast.makeText(context,"连接成功", Toast.LENGTH_SHORT).show();
                Intent bindIntent = new Intent(getApplicationContext(), ConnectDeviceService.class);
                bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                mTextToSpeechHelper.speakChinese("蓝牙连接成功");
                /**
                 * 连接成功后，判断该设备是否已经保存过数据库，如果没有则将该设备保存到数据库
                 */

                boolean isExist = false;//判断当前数据是否已经保存到数据库
                if (beacon != null) {//beacon不为空，说明是搜索设备进行连接的
                    Log.e(TAG, "onReceive: 蓝牙连接成功后，beacon不为null" );
//                    有时候搜索连接的设备是以前连接过的
                    for (int i = 0; i < rules.size(); i++) {
                        if (rules.get(i).getBleMac().equals(beacon.getBluetoothAddress())) {
                            isExist = true;
                            setDeviceImg(i);
                        }
                    }
                    if (!isExist) {//如果没有保存，则保存一份
                        final ContentValues values = new ContentValues();
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceManagerActivity.this);
                        final EditText bleAlias = new EditText(DeviceManagerActivity.this);
                        builder.setView(bleAlias);
                        builder.setTitle("请输入设备名称：");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String alias = bleAlias.getText().toString().trim();
                                values.put(DataBaseParams.ble_alias, alias);
                                values.put(DataBaseParams.ble_device_name, beacon.getBluetoothName());
                                values.put(DataBaseParams.ble_device_mac, beacon.getBluetoothAddress());
                                values.put(DataBaseParams.ble_device_last_connect_time, System.currentTimeMillis());
                                deviceDbHelper.insertDevToSqlite(values);
                                BleDevice bleDevice = new BleDevice();
                                bleDevice.setBleName(beacon.getBluetoothName());
                                bleDevice.setLastConnectTime((int) System.currentTimeMillis());
                                bleDevice.setBleMac(beacon.getBluetoothAddress());
                                bleDevice.setBleAlias(alias);

                                rules.add(bleDevice);
                                setDeviceImg(rules.size() - 1);
                                ruleDevAdapter.setDevs(rules);
                                ruleDevAdapter.notifyDataSetChanged();
                                setListViewHeighBaseOnChildren(gvRule);

                            }
                        });
                        builder.show();


                    }

                } else {
                    Log.e(TAG, "onReceive: 蓝牙连接成功后，beacon为null" );
                    setDeviceImg(current_connected_device_id);
                }

//                更新一下列表
//                getRuleDevicefromDB();
                Log.e(TAG, "onReceive: 查看Rulers集合："+rules.size()+",内容："+rules.toString() );
                gvRule.setVisibility(View.VISIBLE);
                tvNoRuleDev.setVisibility(View.GONE);
                ruleDevAdapter.setDevs(rules);
                ruleDevAdapter.notifyDataSetChanged();
                setListViewHeighBaseOnChildren(gvRule);

            }

            //*********************//
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
                mTextToSpeechHelper.speakChinese("蓝牙连接失败");
                Toast.makeText(context,"连接失败", Toast.LENGTH_SHORT).show();
                setDeviceImg(current_connected_device_id);
                ruleDevAdapter.setDevs(rules);
                ruleDevAdapter.notifyDataSetChanged();
                if (!hasUnbind) {
                    hasUnbind = true;
                }

            }

//            //*********************//
//            /**
//             * 发现服务
//             */

            if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
                if (mService != null) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (flag) {
                                mService.enableTXNotification(ConnectDeviceService.LEVELNESS_SERVICE_UUID, ConnectDeviceService.LEVELNESS_TX_CHAR_UUID);
                                LogUtils.show("在Actvity中收到一个发现水平服务的广播");
                            }

                        }
                    }, 2000);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (flag) {
                                mService.enableTXNotification(ConnectDeviceService.VERTICALITY_SERVICE_UUID, ConnectDeviceService.VERTICALITY_TX_CHAR_UUID);

                                LogUtils.show("在Actvity中收到一个发现垂直服务的广播");
                            }


                        }
                    }, 4000);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (flag) {
                                mService.writeRxCharacteristic(ConnectDeviceService.SYSTEM_RX_SERVICE_UUID, ConnectDeviceService.SYSTEM_RX_CHAR_UUID, "CD01".getBytes());
                                flag = false;
                            }

                        }
                    }, 5000);

                }
//                发现一个服务
                Log.e(TAG, "测量页面中。onReceive: 发现一个服务" );
            }

        }
    };

    @Override
    public void connectingDevice() {
        mDialog.dismiss();
        mLoadingDialog = new LoadingDialog(this, "正在连接");
        mLoadingDialog.show();
    }

    @Override
    public void connectSuccess() {
        mLoadingDialog.dismiss();
    }

    @Override
    public void setConnectingDevice(Beacon beacon) {
        this.beacon = beacon;
    }

    @Override
    public void setDevs(List<BleDevice> bleDevices) {
        rules = bleDevices;
        ruleDevAdapter.notifyDataSetChanged();
    }

    @Override
    public List<BleDevice> getDevs() {
        return rules;
    }
}
