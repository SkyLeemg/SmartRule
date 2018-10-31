package com.vitec.task.smartrule.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vitec.task.smartrule.bean.BleMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static com.vitec.task.smartrule.utils.BleParam.ACTION_DATA_AVAILABLE;
import static com.vitec.task.smartrule.utils.BleParam.ACTION_GATT_CONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.ACTION_GATT_DISCONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.ACTION_GATT_SERVICES_DISCOVERED;
import static com.vitec.task.smartrule.utils.BleParam.DEVICE_DOES_NOT_SUPPORT_UART;
import static com.vitec.task.smartrule.utils.BleParam.EXTRA_DATA;
import static com.vitec.task.smartrule.utils.BleParam.STAAE_DISCONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.STATE_CONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.STATE_CONNECTING;

/**
 * Created by skyel on 2018/10/9.
 */
public class ConnectDeviceService extends Service {
    private static final String TAG = "ConnectDeviceService";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STAAE_DISCONNECTED;


    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");



    private final IBinder mBinder = new LocalBinder();
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public ConnectDeviceService getService() {
            return ConnectDeviceService.this;
        }
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: 解绑了服务" );

        return super.onUnbind(intent);
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: 蓝牙服务类销毁了" );
        close();
    }

    //    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean initialize() {

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "initialize: Unable to initialize BluetoothManager" );
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "initialize: unable to obtain a BluetoothAdapter" );
            return false;
        }

        return true;
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.e(TAG, "close: mBluetoothGatt closed" );
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "connect: BluetoothAdapter not initialized or unspecified address." );
            return false;
        }
        Log.e(TAG, "connect: 准备连接" );
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt!=null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice deivce = mBluetoothAdapter.getRemoteDevice(address);
        if (deivce == null) {
            Log.e(TAG, "connect: Device not found. unable to connect." );
            return false;
        }


        mBluetoothGatt = deivce.connectGatt(this, true, mGattCallback);
        Log.e(TAG, "connect: trying to create a new connection" );
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "disconnect: BluetoothAdapter not initialized" );
            return;
        }
        mBluetoothGatt.disconnect();

    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态改变
         * @param gatt
         * @param status
         * @param newState
         */
//        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                BleMessage message = new BleMessage();
                message.setAction(ACTION_GATT_CONNECTED);
                message.setConnectState(STATE_CONNECTED);
                Log.e(TAG, "vitec 连接成功onConnectionStateChange: Connected to gatt server" );
//                EventBus.getDefault().post(message);
                Log.e(TAG, "vitec 连接成功onConnectionStateChange: attempting to start service discovery:"+mBluetoothGatt.discoverServices() );
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STAAE_DISCONNECTED;
                Log.e(TAG, "vitec 断开连接onConnectionStateChange: Disconnect from gatt server" );
                broadcastUpdate(intentAction);
//                EventBus.getDefault().post(false);
            }
        }

        /**
         * 发现服务
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onServicesDiscovered: mBluetoothGatt=" + mBluetoothGatt);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e(TAG, "onServicesDiscovered: received:" + status);
            }
        }

        /**
         * 读取特征值
         * @param gatt
         * @param characteristic
         * @param status
         */
//        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Log.e(TAG, "onCharacteristicRead: 当前读取到的特征值为："+  new String(characteristic.getValue()));
            }
        }

        /**
         * 写入特征值
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        /**
         * 特征值改变啦
         * @param gatt
         * @param characteristic
         */
//        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }
    };

    private void broadcastUpdate(String intentAction) {
        final Intent intent = new Intent(intentAction);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            final byte[] txValue = characteristic.getValue();
            intent.putExtra(EXTRA_DATA,txValue );
            String text = null;
            try {
                text = new String(txValue, "UTF-8");
                Log.e(TAG, "broadcastUpdate: 服务端收到一个数据："+text);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Enable TXNotification
     *
     * @return
     */
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void enableTXNotification()
    {

    	if (mBluetoothGatt == null) {

    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
//            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
//            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar,true);

//            Log.e(TAG, "enableTXNotification: 查看TxChar值："+TxChar.getValue().length);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }


//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void writeRxCharacteristic(byte[] value) {
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        Log.e(TAG, "writeRxCharacteristic: m"+"mBluetoothGatt null"+ mBluetoothGatt);
        if (RxService == null) {
            Log.e(TAG, "writeRxCharacteristic: Rx Service 没有找到" );
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            Log.e(TAG, "writeRxCharacteristic: Rx没有找到" );
            return;
        }

        RxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
        Log.e(TAG, "writeRxCharacteristic: 写入TXchar-status="+status );
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "readCharacteristic: BluetoothAdapter 没有初始化" );
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);

    }

    public static void startDeviceService(Context context) {
        context.startService(new Intent(context, ConnectDeviceService.class));
    }

    public static void stopDeviceService(Context context) {
        context.stopService(new Intent(context, ConnectDeviceService.class));
    }

}
