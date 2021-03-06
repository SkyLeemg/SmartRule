package com.vitec.task.smartrule.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.ConnectionService;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.vitec.task.smartrule.activity.ChooseMeasureMsgActivity;
import com.vitec.task.smartrule.activity.MeasureManagerAcitivty;
import com.vitec.task.smartrule.adapter.DisplayBleDeviceAdapter;
import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.helper.ServiceConnecteHelper;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.interfaces.IDialogCommunicableWithDevice;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.view.ConnectDialog;
import com.vitec.task.smartrule.view.LoadingDialog;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备管理的Fragment，包括以下功能：
 * 1.添加设备后弹出搜索到的所以蓝牙点击后连接该蓝牙
 * 2.分开显示靠尺设备和激光测量仪设备，将历史连接过的设备列出来
 * 3.设备连接成功后弹出输入设备名称的框，确定后更新显示列表
 * 4.可以点击设备切换连接
 */
public class DeviceManagerFragment extends Fragment implements View.OnClickListener,IDialogCommunicableWithDevice{

    private static final String TAG = "DeviceManagerFragment";
    private View view;
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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_manager_page,null);
//        EventBus.getDefault().register(getActivity());

        initView();
        initViewData();
        return view;

    }

    private void initView() {
        llAddDev = view.findViewById(R.id.ll_add_dev);
//        tvNoLaserDev = view.findViewById(R.id.tv_no_laser_dev);
//        tvNoRuleDev = view.findViewById(R.id.tv_no_rule_dev);
        gvRule = view.findViewById(R.id.gv_rule);
        gvLaser = view.findViewById(R.id.gv_laser);

        llAddDev.setOnClickListener(this);
        mTextToSpeechHelper = new TextToSpeechHelper(getActivity(),"");
    }



    private void initViewData() {
        rules = new ArrayList<>();
        deviceDbHelper = new BleDeviceDbHelper(getActivity());
//        for (int i=0;i<5;i++) {
//            rules.add(i + "号设备");
//        }
        getRuleDevicefromDB();
        ruleDevAdapter = new DisplayBleDeviceAdapter(getActivity(), rules);
        gvRule.setAdapter(ruleDevAdapter);
        if (rules.size() != 0) {
            tvNoRuleDev.setVisibility(View.GONE);
        }

        gvLaser.setVisibility(View.GONE);
        setListViewHeighBaseOnChildren(gvRule);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
        gvRule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                current_connected_device_id = i;
                if (ConnectDeviceService.current_connecting_mac_address.equals(rules.get(i).getBleMac())) {
                    Toast.makeText(getActivity(), "蓝牙已连接", Toast.LENGTH_SHORT).show();
                } else {
                    mLoadingDialog = new LoadingDialog(getActivity(), "正在连接");
                    mLoadingDialog.show();
                    serviceConnecteHelper = new ServiceConnecteHelper(getActivity(),rules.get(i).getBleMac());
                }

            }
        });

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
                mDialog = new ConnectDialog(getActivity(),this);
                mDialog.show();
                break;
        }
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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(UARTStatusChangeReceiver);
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
    }

    @Override
    public void connectingDevice() {
        mDialog.dismiss();
        mLoadingDialog = new LoadingDialog(getActivity(), "正在连接");
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleParam.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }



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
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        final EditText bleAlias = new EditText(getActivity());
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
//            if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
////                发现一个服务
//                Log.e(TAG, "测量页面中。onReceive: 发现一个服务" );
//                mService.enableTXNotification();
//            }

        }
    };
}
