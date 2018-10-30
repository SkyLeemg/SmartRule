package com.vitec.task.smartrule.listener;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.vitec.task.smartrule.activity.DealDeviceDataActivity;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.interfaces.MainActivityGettable;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.BleParam;

/**
 * Created by admin on 2018/10/10.
 */
public class DeviceItemClickListener implements AdapterView.OnItemClickListener ,View.OnClickListener{

    private static final String TAG = "DeviceItemClickListener";
    private Context mContext;
    private Dialog bottomDialog;
    private View contentView;
    private Button btnConnectDevice;
//    private List<Beacon> devices;
    private String macAddress;
    private MainActivityGettable
            getter;
    private BluetoothAdapter mBluetooth;
    private ConnectDeviceService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private boolean hasUnbind = false;


    public DeviceItemClickListener(Context context, MainActivityGettable getter) {
        this.mContext = context;
        this.getter = getter;
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        macAddress =getter.getDevices().get(i).getBluetoothAddress();
        showBottomDialog();


    }


    private void showBottomDialog() {
        bottomDialog = new Dialog(mContext, R.style.BottomDialog);
        contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_bottom_content_normal, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = mContext.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();

        initView();
    }

    private void initView() {
        btnConnectDevice = (Button) contentView.findViewById(R.id.btn_connect_device);
        btnConnectDevice.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect_device:
//                EventBus.getDefault().register(context);
                Toast.makeText(mContext,"正在连接", Toast.LENGTH_SHORT).show();
                service_init();
                bottomDialog.dismiss();
//                context.unbindService(mServiceConnection);
                break;
        }
    }

    private void service_init() {
        Intent bindIntent = new Intent(mContext, ConnectDeviceService.class);
        boolean isSuccess=mContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "service_init: 查看是发绑定成功："+isSuccess );
        LocalBroadcastManager.getInstance(mContext).registerReceiver(UARTStatusChangeReceiver,makeGattUpdateIntentFilter());
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
//            context.unbindService(mServiceConnection);
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
        return intentFilter;
    }


    public final BroadcastReceiver UARTStatusChangeReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            final Intent mIntent = intent;
            //*********************//
            if (action.equals(BleParam.ACTION_GATT_CONNECTED)) {
                Toast.makeText(context,"连接成功", Toast.LENGTH_SHORT).show();
                Intent startIntent = new Intent(context, DealDeviceDataActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startIntent);
                if (!hasUnbind) {
                    hasUnbind = true;
                    mContext.unbindService(mServiceConnection);
                }

            }

            //*********************//
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
                Toast.makeText(context,"连接失败", Toast.LENGTH_SHORT).show();
                if (!hasUnbind) {
                    hasUnbind = true;
                    mContext.unbindService(mServiceConnection);
                }

            }


        }
    };


}
