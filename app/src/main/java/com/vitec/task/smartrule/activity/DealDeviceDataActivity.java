package com.vitec.task.smartrule.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.aliyun.TopicHelper;
import com.vitec.task.smartrule.bean.TestInfo;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.BleParam;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by skyel on 2018/10/11.
 */
//@SmartTable(name = "devicedata" )
public class DealDeviceDataActivity extends BaseActivity {

    private static final String TAG = "DealDeviceDataActivity";
    private TextView tvMsg;
    private SmartTable table;

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    //    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;


    private BluetoothAdapter mBluetooth;
    private ConnectDeviceService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private int mState = UART_PROFILE_DISCONNECTED;
    private StringBuffer stringBuffer=new StringBuffer();
    private int positon = 0;
    private List<TestInfo> lists;
    private TopicHelper topic;

    private TextToSpeechHelper mTextToSpeechHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_device_data);
        mTextToSpeechHelper = new TextToSpeechHelper(getApplicationContext(),"蓝牙连接成功");
        initView();
        service_init();
        initData();
        initDev();
    }

    private void initDev() {
        topic = new TopicHelper();
        topic.publishRequest("I am from android ");
        topic.listenerSubscribeRequest();

    }

    private void initData() {
//        Column<String> column1 = new Column<String>("垂直度");
//        Column<String> column2 = new Column<String>("水平度");
//        Column<String> column3 = new Column<String>("误差");
//        List<String> datas = new ArrayList<>();
//        datas.add("11.1");
//        datas.add("121.1");
//        datas.add("992");
//        TableData<String> tableData = new TableData<String>("data", datas, column1);
        lists = new ArrayList<>();
        for (int i=0;i<15;i++) {
            lists.add(new TestInfo("", "", ""));
        }

        table.setData(lists);

        table.getConfig().setMinTableWidth(getScreenWidth());


    }


    public void initView() {
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        table = (SmartTable) findViewById(R.id.table);
    }

    public int getScreenWidth() {
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        return manager.getDefaultDisplay().getWidth();
    }

    private void service_init() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Intent bindIntent = new Intent(this, ConnectDeviceService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(BleDeviceReceiver,makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleParam.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleParam.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleParam.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }


    private ServiceConnection mServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((ConnectDeviceService.LocalBinder) iBinder).getService();
            Log.e(TAG, "onServiceConnected: mService=" + mService );
            if (!mService.initialize()) {
                Log.e(TAG, "onServiceConnected: 不能初始化蓝牙" );
                finish();
            }
            byte[] bytes = "abcd".getBytes();
            mService.writeRxCharacteristic(bytes);
            Log.e(TAG, "onServiceConnected: 服务绑定完成");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.disconnect();
        unbindService(mServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BleDeviceReceiver);
        mTextToSpeechHelper.stopSpeech();
    }

    private final BroadcastReceiver BleDeviceReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            //*********************//
            if (action.equals(BleParam.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.e(TAG, "广播收到了UART_CONNECT_MSG");
                        stringBuffer.append("[" + currentDateTimeString + "]Connected to: " + mDevice.getName());
                        stringBuffer.append("\n");

                        tvMsg.setText(stringBuffer.toString());
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.e(TAG, "广播收到了UART_DISCONNECT_MSG");
                        stringBuffer.append("[" + currentDateTimeString + "]" + "连接失败");
                        stringBuffer.append("\n");
                        tvMsg.setText(stringBuffer.toString());
                        mState = UART_PROFILE_DISCONNECTED;
                        mTextToSpeechHelper.speakChinese("蓝牙连接断开");
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(BleParam.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(BleParam.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            Log.e(TAG, "run: 收到蓝牙数据："+text );
                            stringBuffer.append("[" + currentDateTimeString + "]收到的数据长度：" + text.length() + "，数据内容：" + text);
                            stringBuffer.append("\n");
//                            tvMsg.setText(stringBuffer.toString());
                            topic.publishRequest("蓝牙数据："+text);
                            mTextToSpeechHelper.speakChinese("收到数据"+text);
                            if (positon < lists.size()) {
                                lists.get(positon).setColumn(text);
                                positon++;
                                table.setData(lists);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(BleParam.DEVICE_DOES_NOT_SUPPORT_UART)){
//                showMessage("Device doesn't support UART. Disconnecting");
                Log.e(TAG, "onReceive: 设备不支持UART,");
//                mService.disconnect();
            }
        }
    };


}
