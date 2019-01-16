package com.vitec.task.smartrule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;

import java.text.DateFormat;
import java.util.Date;

import static com.aliyun.alink.linksdk.tools.ThreadTools.runOnUiThread;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_CONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_DISCONNECTED;

public class HandleMeasureDataReceiver extends BroadcastReceiver {

    public  HandleMeasureDataReceiver (RulerCheck rulerCheck) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        final Intent mIntent = intent;
        //*********************//
        /**
//         * 蓝牙连接成功
//         */
//        if (action.equals(BleParam.ACTION_GATT_CONNECTED)) {
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    Log.e(TAG, "广播收到了UART_CONNECT_MSG");
//                    mTextToSpeechHelper.speakChinese("蓝牙连接成功");
//                    mState = UART_PROFILE_CONNECTED;
//                }
//            });
//        }
//
//        //*********************//
//        /**
//         * 蓝牙连接断开
//         */
//        if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    Log.e(TAG, "广播收到了UART_DISCONNECT_MSG");
//                    mState = UART_PROFILE_DISCONNECTED;
//                    mTextToSpeechHelper.speakChinese("蓝牙连接断开");
////                        mService.connect();
////                        mService.close();
//                    //setUiState();
//
//                }
//            });
//        }
//
//
//        //*********************//
//        /**
//         * 发现服务
//         */
//        if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
////                发现一个服务
//            Log.e(TAG, "测量页面中。onReceive: 发现一个服务" );
//            mService.enableTXNotification();
//        }
//        //*********************//
//        /**
//         * 收到蓝牙数据
//         */
//        if (action.equals(BleParam.ACTION_DATA_AVAILABLE)) {
//
//            final byte[] txValue = intent.getByteArrayExtra(BleParam.EXTRA_DATA);
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    try {
////                            FragmentManager manager = getFragmentManager();
////                            FragmentTransaction transaction = manager.beginTransaction();
//
//                        String text = new String(txValue, "UTF-8");
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+" 收到蓝牙数据："+text);
//                        if (currentDataNum < checkOptionsDataList.size()) {
////                                dataList.get(currentDataNum).setDataContent(text);
//                            Log.e(TAG, "run: 之前无数值，current:" + currentDataNum + ",dalist.size():" + checkOptionsDataList.size());
//                            checkOptionsDataList.get(currentDataNum).setData(text.trim());
//                            checkOptionsDataList.get(currentDataNum).setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
//                            checkOptionsDataList.get(currentDataNum).setUpdateFlag(0);
////                                checkOptionsDataList.get(currentDataNum).setCheckOptionsId(check_option_id);
//                            OperateDbUtil.addRealMeasureDataToSqlite(getActivity(),checkOptionsDataList.get(currentDataNum));
//
//                        } else {
////                                dataList.add(new OnceMeasureData(text));
//                            Log.e(TAG, "run: 之前有数值，current:" + currentDataNum + ",dalist.size():" + checkOptionsDataList.size());
//                            RulerCheckOptionsData data = new RulerCheckOptionsData();
//                            data.setData(text.trim());
//                            data.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
//                            data.setUpdateFlag(0);
//                            data.setRulerCheckOptions(checkOptions);
//                            checkOptionsDataList.add(data);
//                            OperateDbUtil.addRealMeasureDataToSqlite(getActivity(),data);
//                        }
//                        currentDataNum++;
//                        measureDataAdapter.notifyDataSetChanged();
////                            topic.publishRequest("蓝牙数据："+text);
//                        HeightUtils.setGridViewHeighBaseOnChildren(gvMeasureData,6);
//                        mTextToSpeechHelper.speakChinese("收到数据"+text);
//                        completeResult();
//                    } catch (Exception e) {
//                        Log.e(TAG, e.toString());
//                    }
//                }
//            });
//        }
//        //*********************//
//        if (action.equals(BleParam.DEVICE_DOES_NOT_SUPPORT_UART)){
////                showMessage("Device doesn't support UART. Disconnecting");
//            Log.e(TAG, "onReceive: 设备不支持UART,");
////                mService.disconnect();
//        }

    }

//    public static void registerBleRecevier(Context context) {
//        LocalBroadcastManager.getInstance(context).registerReceiver(handleMeasureDataReceiver,makeGattUpdateIntentFilter());
//    }
//
//    public static void unregisterBleRecevier(Context context) {
//        LocalBroadcastManager.getInstance(context).unregisterReceiver(handleMeasureDataReceiver);
//    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleParam.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleParam.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleParam.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

}
