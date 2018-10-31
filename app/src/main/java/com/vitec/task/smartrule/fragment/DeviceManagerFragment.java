package com.vitec.task.smartrule.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.MeasureManagerAcitivty;
import com.vitec.task.smartrule.adapter.DisplayBleDeviceAdapter;
import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.helper.ServiceConnecteHelper;
import com.vitec.task.smartrule.interfaces.IDialogCommunicableWithDevice;
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
        tvNoLaserDev = view.findViewById(R.id.tv_no_laser_dev);
        tvNoRuleDev = view.findViewById(R.id.tv_no_rule_dev);
        gvRule = view.findViewById(R.id.gv_rule);
        gvLaser = view.findViewById(R.id.gv_laser);

        llAddDev.setOnClickListener(this);
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
                serviceConnecteHelper = new ServiceConnecteHelper(getActivity(),rules.get(i).getBleMac());
            }
        });

    }

    private void getRuleDevicefromDB() {
        rules = deviceDbHelper.queryAllDevice();
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



    private void showlog(String msg) {
        Log.e(TAG, "showlog: 查看eventbus里面的log："+msg );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(UARTStatusChangeReceiver);
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
            String action = intent.getAction();
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(BleParam.ACTION_GATT_CONNECTED)) {
                Toast.makeText(context,"连接成功", Toast.LENGTH_SHORT).show();
                /**
                 * 连接成功后，判断该设备是否已经保存过数据库，如果没有则将该设备保存到数据库
                 */
                boolean isExist = false;
                if (beacon != null) {
                    for(int i=0;i<rules.size();i++) {
                        if (rules.get(i).getBleMac().equals(beacon.getBluetoothAddress())) {
                            isExist = true;
                        }
                    }
                    if (!isExist) {
                        ContentValues values = new ContentValues();
                        values.put(DataBaseParams.ble_device_name,beacon.getBluetoothName());
                        values.put(DataBaseParams.ble_device_mac, beacon.getBluetoothAddress());
                        values.put(DataBaseParams.ble_device_last_connect_time,System.currentTimeMillis());
                        deviceDbHelper.insertDevToSqlite(values);
                    }
                    if (!hasUnbind) {
                        hasUnbind = true;
//                    ServiceConnecteHelper serviceConnecteHelper = new ServiceConnecteHelper(getActivity());
//                    serviceConnecteHelper.stopServiceConnection();
                        ConnectDialog.serviceConnecteHelper.stopServiceConnection();
//                    getActivity().unbindService(mServiceConnection);
                    }
                }



//                Intent startIntent = new Intent(context, MeasureManagerAcitivty.class);
//                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(startIntent);
//                更新一下列表
                getRuleDevicefromDB();
                ruleDevAdapter.setDevs(rules);
//                rules.add(new BleDevice(beacon.getBluetoothName(),beacon.getBluetoothAddress()));
                ruleDevAdapter.notifyDataSetChanged();


            }

            //*********************//
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
                Toast.makeText(context,"连接失败", Toast.LENGTH_SHORT).show();
                if (!hasUnbind) {
                    hasUnbind = true;
//                    getActivity().unbindService(mServiceConnection);
                }

            }

        }
    };
}
