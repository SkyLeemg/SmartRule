package com.vitec.task.smartrule.fragment;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.WaitingMeasureActivity;
import com.vitec.task.smartrule.adapter.DisplayMeasureDataAdapter;
import com.vitec.task.smartrule.bean.event.HeightFloorMsgEvent;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;
import com.vitec.task.smartrule.utils.ServiceUtils;
import com.vitec.task.smartrule.view.BottomDialog;
import com.vitec.task.smartrule.view.CommonEditPicView;
import com.vitec.task.smartrule.view.MeasureDataView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aliyun.alink.linksdk.tools.ThreadTools.runOnUiThread;
import static com.vitec.task.smartrule.utils.BleParam.STATE_CONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_CONNECTED;
import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_DISCONNECTED;

/**
 * 真正测量的fragment。一个管控要点为一个fragment
 * 多个管控要点重复new MeasureFragment
 */
public class MeasureFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MeasureFragment";
    private View view;
    private TextView tvFinishMeasure;
    private TextView tvPauseMeasure;

    /****编辑图片的布局****/
//    private View layoutEditPic;
    private RelativeLayout rlEditPic;//图纸编辑的占位RL
    private CommonEditPicView commonEditPicView;

    private TextView tvAddmPic;//添加图纸按钮
    private LinearLayout llDisplayData;//显示测量数据的占位LL，


    private DisplayMeasureDataAdapter measureDataAdapter;
    private Bundle bundle;
    private int mState = UART_PROFILE_DISCONNECTED;
    private TextToSpeechHelper mTextToSpeechHelper;
    private ConnectDeviceService mService = null;
    private int currentDataNum = 0;

    private int check_option_id;//此id对应iot_ruler_check_options表的id

    private List<String> floodHeights;
    private String floodHeight;
    private ArrayAdapter spinnerAdapter;
    private String standard;//合格标准
    private int standartNum = 8;
    private int realNum = 0;//实测点数
    private int qualifiedNum = 0;//合格点数
    private float qualifiedRate = 0.0f;//合格率

    private List<RulerCheckOptionsData> checkOptionsDataList;//蓝牙发过来的数据集合
    private List<RulerCheckOptionsData> uploadOptionsDataList;//待发送给服务器的数据集合
    private RulerCheckOptions levelCheckOption;//一个水平度测量的管控要点
    private RulerCheckOptions verticalCheckOption;//一个测量的管控要点

    private List<OptionMeasure> optionMeasures;//该管控要点可选的层高，还要测量数据标准都在这里
    private OptionMeasure optionMeasure;//上面是该管控要点所有的层高，这个是用户当前选择的层高
    private MeasureDataView verticalMeasureView;
    private MeasureDataView levelMeausreView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_measure, null);
        EventBus.getDefault().register(this);
        initView();
        initData();
        return view;
    }

    private void initView() {
        mTextToSpeechHelper = new TextToSpeechHelper(getActivity(),"");
        tvAddmPic = view.findViewById(R.id.tv_add_mpic);

//        layoutEditPic = view.findViewById(R.id.layout_edit_pic);
        rlEditPic = view.findViewById(R.id.rl_edit_pic);
        llDisplayData = view.findViewById(R.id.ll_display_mdata);
        tvFinishMeasure = view.findViewById(R.id.tv_finish_measure);
        tvPauseMeasure = view.findViewById(R.id.tv_pause_measure);

        tvAddmPic.setOnClickListener(this);
        tvPauseMeasure.setOnClickListener(this);
        tvFinishMeasure.setOnClickListener(this);

    }

    private void service_init() {
        Intent bindIntent = new Intent(getActivity(), ConnectDeviceService.class);
        getActivity().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        registerBleRecevier();
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            unregisterBleRecevier();
        } else {
            registerBleRecevier();
            checkOptionsDataList = OperateDbUtil.queryMeasureDataFromSqlite(getActivity(), levelCheckOption);

            if (measureDataAdapter != null) {
                measureDataAdapter.notifyDataSetChanged();
            }
        }
    }


    public void registerBleRecevier() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BleDeviceReceiver,makeGattUpdateIntentFilter());
    }

    public void unregisterBleRecevier() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(BleDeviceReceiver);
    }

    private ServiceConnection mServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((ConnectDeviceService.LocalBinder) iBinder).getService();
            Log.e(TAG, "onServiceConnected: mService=" + mService );
            if (!mService.initialize()) {
                Log.e(TAG, "onServiceConnected: 不能初始化蓝牙" );

            }
            if (mService.mConnectionState != STATE_CONNECTED) {
                Toast.makeText(getActivity(), "蓝牙未连接", Toast.LENGTH_SHORT).show();
            } else {
                byte[] bytes = "abcd".getBytes();
            }

            Log.e(TAG, "onServiceConnected: 服务绑定完成");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    public void setTvAddmPicVisibale(int flag) {
        if (flag == 1) {
            tvAddmPic.setVisibility(View.VISIBLE);
            commonEditPicView.setVisibility(View.GONE);
        } else if (flag == 0) {
            tvAddmPic.setVisibility(View.GONE);
            commonEditPicView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 相册或者拍照返回的照片
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    if (selectList.size() > 0) {
                        for (final LocalMedia media : selectList) {
                            LogUtils.show("onActivityResult---打印查看返回的原图片路径：" + media.getPath() + ",长宽：" + media.getWidth() + "," + media.getHeight());
                            LogUtils.show("onActivityResult---打印查看返回裁剪后的图片路径：" + media.getCutPath());
                            LogUtils.show("onActivityResult---打印查看压缩后的图片路径：" + media.getCompressPath());

                            //        将编辑图纸的页面添加到rlEditPic中
                            if (commonEditPicView == null) {
                                commonEditPicView = new CommonEditPicView(getActivity());

                                rlEditPic.addView(commonEditPicView);
                            } else {
                                commonEditPicView.setVisibility(View.VISIBLE);
                            }
                            commonEditPicView.setFragment(MeasureFragment.this);
                            tvAddmPic.setVisibility(View.GONE);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    commonEditPicView.setmImageView(getActivity(),media.getPath());
                                }
                            }, 100);
                        }
                    }
                    break;
            }
        }
    }

    private void initData() {
        /**
         * TODO 还需要接受传来的数据，显示在对应的控件上
         */
        checkOptionsDataList = new ArrayList<>();
        uploadOptionsDataList = new ArrayList<>();

        /**
         * 接收在创建Fragment时发来的数据
         */
        bundle = getArguments();
//        获取用户在新建界面传来的层高

        /**
         * checkOptions里面包含了项目信息、工程和管控要点的模板信息
         */
        List<RulerCheckOptions> accessOptions = (List<RulerCheckOptions>) bundle.getSerializable("checkoptions");
        if (accessOptions.size() > 0) {
            for (int k = 0; k < accessOptions.size(); k++) {
                //1是垂直度
                if (accessOptions.get(k).getRulerOptions().getType() == 1) {
                    verticalCheckOption = accessOptions.get(k);
                    verticalMeasureView = new MeasureDataView(getActivity());
                    verticalMeasureView.initData(accessOptions.get(k));
                    llDisplayData.addView(verticalMeasureView);
                } else if (accessOptions.get(k).getRulerOptions().getType() == 2) {
                    //2是水平度
                    levelCheckOption = accessOptions.get(k);
                    levelMeausreView = new MeasureDataView(getActivity());
                    levelMeausreView.initData(accessOptions.get(k));
                    llDisplayData.addView(levelMeausreView);
                }
            }
        }


//        初始化optionMeasures
        optionMeasures = new ArrayList<>();
        final String measures = levelCheckOption.getRulerOptions().getMeasure();
        optionMeasures = OptionsMeasureUtils.getOptionMeasure(measures);
        check_option_id = levelCheckOption.getId();
        standard = levelCheckOption.getRulerOptions().getStandard();

        service_init();
    }



    /**
     * 根据计算标准去计算实测数、合格数和合格率
     */
    private void completeResult() {
        if (optionMeasure != null) {
            realNum = 0;
            qualifiedNum = 0;
            float frealnum = 0.0f;
            float fq = 0.0f;
            for (int i=0; i<currentDataNum;i++) {
                String data = checkOptionsDataList.get(i).getData().trim();
                Log.e(TAG, "completeResult: 查看字符串格式的数据:"+data+",字符串长度："+data.length());
                try {
                    float datanum = Float.valueOf(data);
                    /**
                     * 根据操作标志来计算结果，
                     * 1 - 代表要 小于等于 才合格
                     * 2 -
                     */
                    switch (optionMeasure.getOperate()) {
                        case 1:
                            if (datanum < optionMeasure.getStandard() || datanum == optionMeasure.getStandard()) {
                                qualifiedNum++;
                            }
                            break;
                    }
                    realNum++;
                } catch (Exception e) {
                    Log.e(TAG, "completeResult: 错误原因："+e.getMessage() );
                }
            }
            frealnum = realNum;
            fq = qualifiedNum;
            qualifiedRate = (fq / frealnum);
            Log.e(TAG, "completeResult: 查看计算出来的实测点数："+realNum+",合格点数："+qualifiedNum+",合格率："+qualifiedRate );
            levelCheckOption.setFloorHeight(floodHeight);
            levelCheckOption.setMeasuredNum(realNum);
            levelCheckOption.setQualifiedNum(qualifiedNum);
            levelCheckOption.setQualifiedRate(Float.parseFloat(String.format("%.2f",qualifiedRate*100)));
//            updateCompleteResult();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netBussCallBack(String flag) {
        LogUtils.show("netBussCallBack---查看创建好记录表后返回的标志:"+flag);
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getActivity());
//        先更新RulerCheck的server_id
        String where = " where id = " + levelCheckOption.getRulerCheck().getId();
        List<RulerCheck> rulerCheckList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(where);
        if (rulerCheckList.size() > 0) {
            LogUtils.show("netBussCallBack====查看数据库查询出来的Rulercheck：" + rulerCheckList.get(0));
            RulerCheck rulerCheck = levelCheckOption.getRulerCheck();
            rulerCheck.setServerId(rulerCheckList.get(0).getServerId());
            levelCheckOption.setRulerCheck(rulerCheck);
        }
        bleDataDbHelper.close();
        //        再更新RulerCheckOption的Server_id
        String optionWhere = " where id = " + levelCheckOption.getId();
        List<RulerCheckOptions> rulerCheckOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getActivity(), levelCheckOption.getRulerCheck(), optionWhere);
        if (rulerCheckOptionsList.size() > 0) {
            levelCheckOption.setServerId(rulerCheckOptionsList.get(0).getServerId());
            LogUtils.show("netBussCallBack====查看数据库查询出来的RrulerCheckOptionsList：" + rulerCheckOptionsList.get(0));
        }
    }



    @Override
    public void onStop() {
        super.onStop();
        LogUtils.show("MeasureFragment--"+ levelCheckOption.getRulerOptions().getOptionsName()+"的stop方法调用了" );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
       LogUtils.show("MeasureFragment--"+ levelCheckOption.getRulerOptions().getOptionsName()+"的onDestroyView调用了");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("MeasureFragment--"+ levelCheckOption.getRulerOptions().getOptionsName()+"的onDestroy调用了");
//        退出前将数据更新到数据库
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getActivity());
        bleDataDbHelper.updateMeasureOptonsToSqlite(levelCheckOption);
        getActivity().unbindService(mServiceConnection);
        mTextToSpeechHelper.stopSpeech();
        EventBus.getDefault().unregister(this);
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
//                发现一个服务
                Log.e(TAG, "测量页面中。onReceive: 发现一个服务" );
//                mService.enableTXNotification();
            }
            //*********************//
            /**
             * 收到蓝牙数据
             */
            if (action.equals(BleParam.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(BleParam.EXTRA_DATA);
                final String uuid = intent.getStringExtra(BleParam.EXTRA_UUID);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            LogUtils.show("MeasureFragment--"+ levelCheckOption.getRulerOptions().getOptionsName()+" 收到蓝牙数据："+text);
                            LogUtils.show("Fragment-----收到垂直度数据,进入数据处理之前："+text);
                            if (uuid.equalsIgnoreCase(ConnectDeviceService.VERTICALITY_TX_CHAR_UUID.toString()) && verticalMeasureView != null) {
                                LogUtils.show("Fragment----收到垂直度数据，进入判断后。");
                                dealData(verticalMeasureView, text);
                            }
                            if (uuid.equalsIgnoreCase(ConnectDeviceService.LEVELNESS_TX_CHAR_UUID.toString()) && levelMeausreView != null) {
                                dealData(levelMeausreView, text);
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

    private void dealData(MeasureDataView measureDataView, String text) {
        if (measureDataView.getRealDataCount() < measureDataView.getUsingCheckOptionsDataList().size()) {
            measureDataView.getUsingCheckOptionsDataList().get(measureDataView.getRealDataCount()).setData(text.trim());
            measureDataView.getUsingCheckOptionsDataList().get(measureDataView.getRealDataCount()).setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
            measureDataView.getUsingCheckOptionsDataList().get(measureDataView.getRealDataCount()).setUpdateFlag(0);
//            uploadOptionsDataList.add(checkOptionsDataList.get(currentDataNum));
        } else {
            RulerCheckOptionsData data = new RulerCheckOptionsData();
            data.setData(text.trim());
            data.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
            data.setUpdateFlag(0);
            data.setRulerCheckOptions(measureDataView.getRulerCheckOptions());
            data.setQualified(true);
            measureDataView.getUsingCheckOptionsDataList().add(data);
//            uploadOptionsDataList.add(data);
        }
        measureDataView.setRealDataCount(measureDataView.getRealDataCount() + 1);
//        重新计算结果
        measureDataView.completeResult();
        measureDataView.getMeasureDataAdapter().notifyDataSetChanged();
        HeightUtils.setGridViewHeighBaseOnChildren(measureDataView.getGvDisplayData(),6);
//        HeightUtils.setGridViewHeighBaseOnChildren(gvMeasureData,6);
//        completeResult();
    }

    /**
     * TODO 按钮点击事件
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_add_mpic:
                BottomDialog bottomDialog = new BottomDialog(getActivity(), R.style.BottomDialog);
                bottomDialog.setFragment(MeasureFragment.this);
                bottomDialog.show();
                break;

            case R.id.tv_finish_measure:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("是否结束测量？");
                builder.setMessage("代表测量完成，所有管控要点均不能再继续录入数据");
                builder.setPositiveButton("结束", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

//                        mkLoader.setVisibility(View.VISIBLE);
                        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getActivity());

                        RulerCheck rulerCheck = levelCheckOption.getRulerCheck();
                            /**
                             * 更新到本地
                             */
                            ContentValues values = new ContentValues();
                            values.put(DataBaseParams.measure_is_finish, 1);
                            String where = " id = ?";
                            String[] whereValues = new String[]{String.valueOf(rulerCheck.getId())};
                            int result = dataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, values, where, whereValues);
                            LogUtils.show("完成测量，更新数据是否成功："+levelCheckOption.getRulerCheck().getProjectName()+",更新状态："+result);
//                            更新完成后，更新集合中的状态，接下来向服务器发起更新的时候会用到状态标志
                            if (result > 0) {
                                Toast.makeText(getActivity(),"测量已结束",Toast.LENGTH_SHORT).show();
                                rulerCheck.setStatus(1);
                                tvFinishMeasure.setText("测量完成");
                                tvFinishMeasure.setClickable(false);
                            }
//                            requestStopMeasure(rulerCheckList.get(j).getServerId());
//                        }
                        dataDbHelper.close();
                        List<RulerCheck> checkList = new ArrayList<>();
                        checkList.add(rulerCheck);
                        /**
                         * 更新到服务器
                         */
                        Intent serviceIntent = new Intent(getActivity(), PerformMeasureNetIntentService.class);
                        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_FINISH_MEASURE);
                        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FINISH_MEASURE_KEY, (Serializable) checkList);
                        getActivity().startService(serviceIntent);

//                        如果处理数据的服务还在运行，则停止服务
                        boolean isAlive = ServiceUtils.isServiceRunning(getActivity(), "com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService");
                        if (isAlive) {
                            HandleBleMeasureDataReceiverService.stopHandleService(getActivity());
                        }
//                        更新列表
//                        getUnFinishServerCheckData();
//                        updateAdapterData();


                    }
                });
                builder.setNegativeButton("继续测量", null);
                builder.show();
                break;

            case R.id.tv_pause_measure:
                boolean isAlive = ServiceUtils.isServiceRunning(getActivity(), "com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService");
                if (isAlive) {
                    HandleBleMeasureDataReceiverService.stopHandleService(getActivity());
                }
                break;
        }
    }

}

