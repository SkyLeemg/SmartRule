package com.vitec.task.smartrule.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.OptionBean;
import com.vitec.task.smartrule.bean.OnceMeasureData;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.ParameterKey;
import com.vitec.task.smartrule.utils.ServiceUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aliyun.alink.linksdk.tools.ThreadTools.runOnUiThread;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_CONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_DISCONNECTED;

public class MeasureFragment extends Fragment{

    private static final String TAG = "MeasureFragment";
    private View view;
    private List<OptionBean> measureBeanList;
    private List<OnceMeasureData> dataList;
    private GridView gvMeasureData;
    private TextView tvProjectName;//项目类型
    private TextView tvMeasureItem;//管控要点
    private MeasureDataAdapter measureDataAdapter;
    private Bundle bundle;
    private int mState = UART_PROFILE_DISCONNECTED;
    private TextToSpeechHelper mTextToSpeechHelper;
    private ConnectDeviceService mService = null;
    private int currentDataNum = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_measure, null);
        initView();
        initData();
        return view;

    }

    private void initView() {
        gvMeasureData = view.findViewById(R.id.gv_measure_data);
        tvProjectName = view.findViewById(R.id.tv_project_type);
        tvMeasureItem = view.findViewById(R.id.tv_measure_item);
        mTextToSpeechHelper = new TextToSpeechHelper(getActivity(),"");
    }

    private void service_init() {
        Intent bindIntent = new Intent(getActivity(), ConnectDeviceService.class);
        getActivity().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BleDeviceReceiver,makeGattUpdateIntentFilter());
        ServiceUtils.startConnectDeviceSerivce(getActivity());
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


    private void initData() {
        /**
         * TODO 还需要接受传来的数据，显示在对应的控件上
         */
        dataList = new ArrayList<>();
        for (int i =0;i<12 ;i++) {
            dataList.add(new OnceMeasureData(""));
        }
        /**
         * 初始化接受数据的gridview
         */
        measureDataAdapter = new MeasureDataAdapter();
        gvMeasureData.setAdapter(measureDataAdapter);
        HeightUtils.setGridViewHeighBaseOnChildren(gvMeasureData,6);
        /**
         * 接收在创建Fragment时发来的数据
         */
        bundle = getArguments();
        tvProjectName.setText(bundle.getString(ParameterKey.projectNameKey)+":");
        tvMeasureItem.setText("管控要点："+bundle.getString(ParameterKey.measureItemKey));
        service_init();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mServiceConnection);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(BleDeviceReceiver);
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.e(TAG, "广播收到了UART_CONNECT_MSG");
                        mTextToSpeechHelper.speakChinese("蓝牙连接成功");
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            /**
             * 蓝牙连接断开
             */
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.e(TAG, "广播收到了UART_DISCONNECT_MSG");
                        mState = UART_PROFILE_DISCONNECTED;
                        mTextToSpeechHelper.speakChinese("蓝牙连接断开");
//                        mService.connect();
//                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            /**
             * 发现服务
             */
            if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            /**
             * 收到蓝牙数据
             */
            if (action.equals(BleParam.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(BleParam.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            Log.e(TAG, "run: 收到蓝牙数据："+text );
                            if (currentDataNum < dataList.size()) {
                                dataList.get(currentDataNum).setDataContent(text);
                            } else {
                                dataList.add(new OnceMeasureData(text));
                            }
                            currentDataNum++;
                            measureDataAdapter.notifyDataSetChanged();
//                            topic.publishRequest("蓝牙数据："+text);
                            HeightUtils.setGridViewHeighBaseOnChildren(gvMeasureData,6);
                            mTextToSpeechHelper.speakChinese("收到数据"+text);
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


    class MeasureDataAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int i) {
            return dataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.item_gridview_measure_data, null);
                holder = new ViewHolder();
                holder.etData = view.findViewById(R.id.et_measure_data);
                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.etData.setText(dataList.get(i).getDataContent());
            return view;
        }
    }

    class ViewHolder {
        EditText etData;
    }

}

