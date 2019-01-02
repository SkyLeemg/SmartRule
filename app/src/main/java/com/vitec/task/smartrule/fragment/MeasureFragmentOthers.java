package com.vitec.task.smartrule.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import com.vitec.task.smartrule.adapter.DisplayMeasureDataAdapter;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.bean.event.HeightFloorMsgEvent;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.interfaces.IEditPicControler;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;
import com.vitec.task.smartrule.utils.ServiceUtils;
import com.vitec.task.smartrule.view.BottomDialog;
import com.vitec.task.smartrule.view.CommonEditPicView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
public class MeasureFragmentOthers extends Fragment implements View.OnClickListener ,IEditPicControler{

    private static final String TAG = "MeasureFragment";
    private View view;
    private GridView gvMeasureData;
    private TextView tvProjectName;//项目类型
    private TextView tvMeasureItem;//管控要点
//    private TextView tvQualifiedStandard;
//    private EditText etStandardRate;
//    private EditText etStandartNum;
//    private EditText etRealMeasureNum;
//    private Spinner spinnerFloorHeight;
//    private ImageView imgAdd;

    /****编辑图片的布局****/
    private View layoutEditPic;
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
    private LinearLayout llFloorHeight;//选择层高的模块，当合格标准只有以项的时候需要隐藏这个模块

    private RulerCheckOptionsData checkOptionsData;//一个空的数据的模板
    private List<RulerCheckOptionsData> checkOptionsDataList;//蓝牙发过来的数据集合
    private List<RulerCheckOptionsData> uploadOptionsDataList;//待发送给服务器的数据集合
    private RulerCheckOptions checkOptions;//一个测量的管控要点
    private List<OptionMeasure> optionMeasures;//该管控要点可选的层高，还要测量数据标准都在这里
    private OptionMeasure optionMeasure;//上面是该管控要点所有的层高，这个是用户当前选择的层高


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_measure_other, null);
        EventBus.getDefault().register(this);
        initView();
        initData();
        return view;
    }

    private void initView() {
//        gvMeasureData = view.findViewById(R.id.gv_measure_data);
        tvProjectName = view.findViewById(R.id.tv_project_type);
//        tvMeasureItem = view.findViewById(R.id.tv_measure_item);
        mTextToSpeechHelper = new TextToSpeechHelper(getActivity(),"");
//        tvQualifiedStandard = view.findViewById(R.id.tv_qualified_flag);
//        etStandardRate = view.findViewById(R.id.et_standard_rate);
//        etStandartNum = view.findViewById(R.id.et_standard_num);
//        etRealMeasureNum = view.findViewById(R.id.et_real_measure_num);
//        spinnerFloorHeight = view.findViewById(R.id.spinner_floor_height);
//        llFloorHeight = view.findViewById(R.id.ll_floor_height);
//        imgAdd = view.findViewById(R.id.img_add);
        tvAddmPic = view.findViewById(R.id.tv_add_mpic);

//        layoutEditPic = view.findViewById(R.id.layout_edit_pic);
        rlEditPic = view.findViewById(R.id.rl_edit_pic);
        llDisplayData = view.findViewById(R.id.ll_display_mdata);

        tvAddmPic.setOnClickListener(this);


//        layoutEditPic.setVisibility(View.VISIBLE);

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
        LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+
                "调用了onHiddenChanged方法："+hidden);
        if (hidden) {
            unregisterBleRecevier();
        } else {
            registerBleRecevier();
            checkOptionsDataList = OperateDbUtil.queryMeasureDataFromSqlite(getActivity(), checkOptions);
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

    @Override
    public List<RulerCheckOptions> getCheckOptions() {
        List<RulerCheckOptions> list = new ArrayList<>();
        list.add(checkOptions);
        return list;
    }

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
                                commonEditPicView = new CommonEditPicView(getActivity(),this);

                                rlEditPic.addView(commonEditPicView);
                            } else {
                                commonEditPicView.setVisibility(View.VISIBLE);
                            }
//                            commonEditPicView.setFragment(MeasureFragmentOthers.this);
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
        checkOptionsData = new RulerCheckOptionsData();
        /**
         * checkOptions里面包含了项目信息、工程和管控要点的模板信息
         */
        checkOptions = (RulerCheckOptions) bundle.getSerializable("checkoptions");
        checkOptionsData.setCreateTime((int) System.currentTimeMillis());
        checkOptionsData.setRulerCheckOptions(checkOptions);
//        初始化optionMeasures
        optionMeasures = new ArrayList<>();
        final String measures = checkOptions.getRulerOptions().getMeasure();
        optionMeasures = OptionsMeasureUtils.getOptionMeasure(measures);
        LogUtils.show("查看"+checkOptions.getRulerOptions().getOptionsName()+"模块的optionMeasures："+optionMeasures.toString());

        Log.e(TAG, "initData: 查看MeasureFragment收到的checkoptions:"+ checkOptions);
        tvProjectName.setText(checkOptions.getRulerCheck().getProjectName()+":");
        tvMeasureItem.setText("管控要点："+ checkOptions.getRulerOptions().getOptionsName());
//        此id对应iot_ruler_check_options表的id
        check_option_id = checkOptions.getId();
        standard = checkOptions.getRulerOptions().getStandard();
//        tvQualifiedStandard.setText(standard);




        /**
         * 初始化保存蓝牙数据的集合对象
         */
        checkOptionsDataList = OperateDbUtil.queryMeasureDataFromSqlite(getActivity(), checkOptions);
        currentDataNum = checkOptionsDataList.size();
        if (checkOptionsDataList.size() == 0) {
            for (int i = 0; i < 12; i++) {
                RulerCheckOptionsData data = new RulerCheckOptionsData();
                data.setRulerCheckOptions(checkOptions);
                checkOptionsDataList.add(data);
            }
        } else {
            completeResult();
        }

        updateCompleteResult();
        service_init();

        /********************初始化ImgAdd按钮**********************/
//        if (checkOptions.getRulerOptions().getType() < 3) {
//          imgAdd.setVisibility(View.GONE);
//        } else {
//          imgAdd.setVisibility(View.VISIBLE);
//        }

        /**
         * 初始化接受数据的gridview
         */
        measureDataAdapter = new DisplayMeasureDataAdapter(getActivity(),checkOptionsDataList);
        gvMeasureData.setAdapter(measureDataAdapter);
        HeightUtils.setGridViewHeighBaseOnChildren(gvMeasureData,6);

        /********************层高选择框部分开始**************************/

//        floodHeights = new ArrayList<>();
//        for (OptionMeasure measure : optionMeasures) {
//            floodHeights.add(measure.getData());
//        }
////        初始化默认选择的层高及运算标准
//        optionMeasure = new OptionMeasure();
//        if (optionMeasures.size() > 0) {
//            optionMeasure = optionMeasures.get(0);
//        }else {
//            optionMeasure.setData("≤6");
//            optionMeasure.setStandard(8);
//            optionMeasure.setOperate(1);
//            optionMeasure.setId(1);
//        }
//        LogUtils.show("查看"+checkOptions.getRulerOptions().getOptionsName()+"模块的floodHeights："+floodHeights.toString());
//        if (floodHeights.size() > 1) {
//            llFloorHeight.setVisibility(View.VISIBLE);
//            spinnerAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, floodHeights);
////            spinnerFloorHeight.setAdapter(spinnerAdapter);
//        } else {
//            llFloorHeight.setVisibility(View.GONE);
//        }
//
//        spinnerFloorHeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.e(TAG, "onItemSelected: 查看当前选择的：" + i + ",内容：" + floodHeights.get(i) + ",查看当前选择的标准：" + optionMeasures.get(i));
//                floodHeight = floodHeights.get(i);
//                if (floodHeights.get(i).equals(optionMeasures.get(i).getData())) {
//                    optionMeasure = optionMeasures.get(i);
//                } else {
//                    for (OptionMeasure measure : optionMeasures) {
//                        if (floodHeights.get(i).equals(measure.getData())) {
//                            optionMeasure = measure;
//                        }
//                    }
//                }
//                EventBus.getDefault().post(new HeightFloorMsgEvent(checkOptions.getRulerOptions().getType(),optionMeasure));
//                completeResult();
//                Log.e(TAG, "onItemSelected: 查看最终参看值："+standartNum );
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
        /********************层高选择框部分结束**************************/

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
            checkOptions.setFloorHeight(floodHeight);
            checkOptions.setMeasuredNum(realNum);
            checkOptions.setQualifiedNum(qualifiedNum);
            checkOptions.setQualifiedRate(Float.parseFloat(String.format("%.2f",qualifiedRate*100)));
            updateCompleteResult();
        }
    }

    private void updateCompleteResult() {
//        etRealMeasureNum.setText(realNum+"");
//        etStandartNum.setText(qualifiedNum+"");
//        if (qualifiedRate >=0) {
//            etStandardRate.setText(String.format("%.2f",qualifiedRate*100));
//        }else etStandardRate.setText("0.00");
//
////        if (currentDataNum > 0 && checkOptionsDataList.size() > 0) {
////            uploadOptionsDataList.add(checkOptionsDataList.get(currentDataNum - 1));
////        }
        updateMeasureDataToServer();
    }

    /******************************更新测量数据到服务器*************************/
    private void updateMeasureDataToServer() {
        /**
         * 1.获取测量管控要点的server_id，
         *   如果有值，代表之前有网络，按照有网的格式发送
         *   如果无值，代表之前没有网络，按照没网的格式发送
         */
//        先打印出管控要点的对象查看一下
//        LogUtils.show("updateMeasureDataToServer----查看测量管控要点的数据："+checkOptions);
//        LogUtils.show("updateMeasureDataToServer----查看测量管控要点的uploadOptionsDataList数据："+uploadOptionsDataList.size()+",内容:"+uploadOptionsDataList);
//        if (uploadOptionsDataList.size() > 5) {
//            Intent intent = new Intent(getActivity(), PerformMeasureNetIntentService.class);
//            List<RulerCheckOptions> list = new ArrayList<>();
//            checkOptions.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
//            list.add(checkOptions);
//            intent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPDATE_DATA);
//            intent.putExtra(PerformMeasureNetIntentService.GET_CREATE_OPTIONS_DATA_KEY, (Serializable) list);
//            intent.putExtra(PerformMeasureNetIntentService.GET_UPDATE_DATA_KEY, (Serializable) uploadOptionsDataList);
//            getActivity().startService(intent);
//            uploadOptionsDataList.clear();
//        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netBussCallBack(String flag) {
        LogUtils.show("netBussCallBack---查看创建好记录表后返回的标志:"+flag);
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getActivity());
//        先更新RulerCheck的server_id
        String where = " where id = " + checkOptions.getRulerCheck().getId();
        List<RulerCheck> rulerCheckList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(where);
        if (rulerCheckList.size() > 0) {
            LogUtils.show("netBussCallBack====查看数据库查询出来的Rulercheck：" + rulerCheckList.get(0));
            RulerCheck rulerCheck = checkOptions.getRulerCheck();
            rulerCheck.setServerId(rulerCheckList.get(0).getServerId());
            checkOptions.setRulerCheck(rulerCheck);
        }
        bleDataDbHelper.close();
        //        再更新RulerCheckOption的Server_id
        String optionWhere = " where id = " + checkOptions.getId();
        List<RulerCheckOptions> rulerCheckOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getActivity(), checkOptions.getRulerCheck(), optionWhere);
        if (rulerCheckOptionsList.size() > 0) {
            checkOptions.setServerId(rulerCheckOptionsList.get(0).getServerId());
            LogUtils.show("netBussCallBack====查看数据库查询出来的RrulerCheckOptionsList：" + rulerCheckOptionsList.get(0));
        }
    }



    @Override
    public void onStop() {
        super.onStop();
        LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+"的stop方法调用了" );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
       LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+"的onDestroyView调用了");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+"的onDestroy调用了");
//        退出前将数据更新到数据库
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getActivity());
        bleDataDbHelper.updateMeasureOptonsToSqlite(checkOptions);
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
                            LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+" 收到蓝牙数据："+text);
                            switch (checkOptions.getRulerOptions().getType()) {
                                case 1:
                                    if (uuid.equalsIgnoreCase(ConnectDeviceService.VERTICALITY_TX_CHAR_UUID.toString())) {
                                        dealData(text);
                                    }
                                    break;
                                case 2:
                                    if (uuid.equalsIgnoreCase(ConnectDeviceService.LEVELNESS_TX_CHAR_UUID.toString())) {
                                        dealData(text);
                                    }
                                    break;
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

    private void dealData(String text) {
        if (currentDataNum < checkOptionsDataList.size()) {
            Log.e(TAG, "run: 之前无数值，current:" + currentDataNum + ",dalist.size():" + checkOptionsDataList.size());
            checkOptionsDataList.get(currentDataNum).setData(text.trim());
            checkOptionsDataList.get(currentDataNum).setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
            checkOptionsDataList.get(currentDataNum).setUpdateFlag(0);
            uploadOptionsDataList.add(checkOptionsDataList.get(currentDataNum));

        } else {
            Log.e(TAG, "run: 之前有数值，current:" + currentDataNum + ",dalist.size():" + checkOptionsDataList.size());
            RulerCheckOptionsData data = new RulerCheckOptionsData();
            data.setData(text.trim());
            data.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
            data.setUpdateFlag(0);
            data.setRulerCheckOptions(checkOptions);
            checkOptionsDataList.add(data);
            uploadOptionsDataList.add(data);
        }
        currentDataNum++;
        measureDataAdapter.notifyDataSetChanged();
        HeightUtils.setGridViewHeighBaseOnChildren(gvMeasureData,6);
        completeResult();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_add_mpic:
//
                BottomDialog bottomDialog = new BottomDialog(getActivity(), R.style.BottomDialog);
                bottomDialog.setFragment(MeasureFragmentOthers.this);
                bottomDialog.show();
                break;
        }
    }
//
//    class MeasureDataAdapter extends BaseAdapter {
//
//        @Override
//        public int getCount() {
//            return checkOptionsDataList.size();
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return checkOptionsDataList.get(i);
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return i;
//        }
//
//        @Override
//        public View getView(int i, View view, ViewGroup viewGroup) {
//            LayoutInflater inflater = LayoutInflater.from(getActivity());
//            ViewHolder holder;
//            if (view == null) {
//                view = inflater.inflate(R.layout.item_gridview_measure_data, null);
//                holder = new ViewHolder();
//                holder.etData = view.findViewById(R.id.et_measure_data);
//                holder.tvContent = view.findViewById(R.id.tv_measure_content);
//                holder.tvTitleIndex = view.findViewById(R.id.tv_title_index);
//                view.setTag(holder);
//
//            } else {
//                holder = (ViewHolder) view.getTag();
//            }
//
//            holder.etData.setText(checkOptionsDataList.get(i).getData());
//            holder.tvContent.setText(checkOptionsDataList.get(i).getData());
//            holder.tvTitleIndex.setText(i+1);
////
//            if (checkOptions.getRulerOptions().getType() == 1 || checkOptions.getRulerOptions().getType() == 2) {
//                holder.etData.setEnabled(false);
//            } else {
//                holder.etData.setEnabled(true);
//            }
//            return view;
//        }
//    }
//
//    class ViewHolder {
//        EditText etData;
//        TextView tvTitleIndex;
//        TextView tvContent;
//    }

}

