package com.vitec.task.smartrule.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.event.HandleDataResultMsgEvent;
import com.vitec.task.smartrule.bean.event.HeightFloorMsgEvent;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 在该服务注册一个广播接收者，用于处理靠尺的测量数据-保存到数据库
 * 问题：
 * 1.要从进入测量开始注册，点击结束测量才注销。
 *   1.1 由于recevier的存活周期要大于一个activity的存活周期，小于应用的存活周期
 *   1.2 所以不适合做静态广播，也不适合在某个activity注册动态广播
 *   1.3 实现此功能只能再开一个service与Recevice绑定，使得recevice的存活周期跟随service的存活周期
 * 2.处理数据需要获取点击进入测量后的RulerCheckOptions集合的对象，实现此功能需要解决Service与Activity的通信问题；
 * 3.上传完数据后需要更新数据里面的server_id字段。实现此功能有两种方案：解决service与service的通信问题，把id直接传过来，或者通知recevice数据更新完毕，自己去数据库获取
 * 4.要将蓝牙数据保存到哪个管控要点下面，需要等服务器将模板数据做好才能继续往下做
 */
public class HandleBleMeasureDataReceiverService extends Service {


    public ConnectDeviceService bleService = null;//
    private TextToSpeechHelper textToSpeechHelper;//语音朗读

    private List<RulerCheckOptions> checkOptionsList;//开启测量的之后启动该服务，并将checkOptionsList传递过来
    public static final String GET_CHECK_OPTIONS_LIST = "com.vitec.getcheckoptionslist";//接收checkOptionsList值的key
    private RulerCheckOptions levelOption;//水平度的管控要点，从checkOptionsList中获取
    private RulerCheckOptions verticalOption;//垂直度的管控要点，从checkOptionsList中获取
    private List<RulerCheckOptionsData> levelOptionsDataList;//所有水平度的测量数据集合
    private List<RulerCheckOptionsData> verticalOptinsDataList;//所有垂直度的测量数据集合
    private OptionMeasure levelOptionMeasure;//用户当前选择的层高
    private OptionMeasure verticalOptionMeasure;//用户当前选择的层高

    private final static int DAEMON_SERVICE_ID = -5121;
    public static int check_id = 0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.show("HandleBleMeasureDataReceiverService创建了");
        EventBus.getDefault().register(this);
        textToSpeechHelper = new TextToSpeechHelper(this, "");
        textToSpeechHelper.speakChinese("初始化成功");
        registerBleRecevier();
        bindBleService();
        checkOptionsList = new ArrayList<>();
        levelOptionsDataList = new ArrayList<>();
        verticalOptinsDataList = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            checkOptionsList = (List<RulerCheckOptions>) intent.getSerializableExtra(GET_CHECK_OPTIONS_LIST);
            if (checkOptionsList.size() > 0) {
                check_id = checkOptionsList.get(0).getRulerCheck().getId();
                StringBuffer speakContent = new StringBuffer();
                speakContent.append(checkOptionsList.get(0).getRulerCheck().getProjectName());
                speakContent.append("项目");
                speakContent.append(checkOptionsList.get(0).getRulerCheck().getEngineer().getEngineerName());
                speakContent.append("开始测量");
                textToSpeechHelper.speakChinese(speakContent.toString());
                LogUtils.show("查看朗读语句："+speakContent.toString());
                for (RulerCheckOptions options : checkOptionsList) {
                    if (options.getRulerOptions().getType() == 1) {//type---1代表的立面垂直度的管控要点
                        LogUtils.show("打印查看Type");
                        verticalOption = options;
                        List<OptionMeasure> optionMeasureList = OptionsMeasureUtils.getOptionMeasure(verticalOption.getRulerOptions().getMeasure());
                        verticalOptionMeasure = new OptionMeasure();
                        if (optionMeasureList.size() > 0) {
                            verticalOptionMeasure = optionMeasureList.get(0);
                        } else {
                            verticalOptionMeasure.setData("≤6");
                            verticalOptionMeasure.setStandard(8);
                            verticalOptionMeasure.setOperate(1);
                            verticalOptionMeasure.setId(1);
                        }
                    } else if (options.getRulerOptions().getType() == 2) {//type---2代表的是表面平整度的管控要点
                        levelOption = options;
                        List<OptionMeasure> optionMeasureList = OptionsMeasureUtils.getOptionMeasure(levelOption.getRulerOptions().getMeasure());
                        levelOptionMeasure = new OptionMeasure();
                        if (optionMeasureList.size() > 0) {
                            levelOptionMeasure = optionMeasureList.get(0);
                        } else {
                            levelOptionMeasure.setData("≤6");
                            levelOptionMeasure.setStandard(8);
                            levelOptionMeasure.setOperate(1);
                            levelOptionMeasure.setId(1);
                        }

                    }

                }
            }
        } catch (Exception e) {
            Log.e("aaa", "获取list异常，查看异常信息：" + e.getMessage());
        }

        /************************做服务保活---设置为前台服务***************************/
        flags = START_STICKY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle("Beacon正在运行");
            builder.setContentText("");
            builder.setAutoCancel(false);
            builder.setSmallIcon(R.mipmap.vlogo);
            startForeground(DAEMON_SERVICE_ID, builder.build());
            Log.e("onStartCommand", "版本号大于等于6.0");
            Intent innerIntent = new Intent(this,DaemonInnerService.class);
            startService(innerIntent);
        }else {
            startForeground(DAEMON_SERVICE_ID,new Notification());
        }

        return super.onStartCommand(intent, flags, startId);
    }




    public static void startHandleService(Context context,List<RulerCheckOptions> optionsList) {
        Intent serviceIntent = new Intent(context, HandleBleMeasureDataReceiverService.class);
        serviceIntent.putExtra(GET_CHECK_OPTIONS_LIST, (Serializable) optionsList);
        context.startService(serviceIntent);
    }

    public static void stopHandleService(Context context) {
        Intent serviceIntent = new Intent(context, HandleBleMeasureDataReceiverService.class);
        context.stopService(serviceIntent);
    }

    /**
     * 由于很多activity都会用到绑定服务，所以做在BaseActivity,需要用到的类直接调用此方法进行绑定
     */
    private void bindBleService() {
        Intent bindIntent = new Intent(this, ConnectDeviceService.class);
        boolean isSuccess = bindService(bindIntent, mBleServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 绑定了服务的类，在销毁之前要解绑
     */
    private void unbindBleService() {
        unbindService(mBleServiceConnection);
    }

    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        //        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)1
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bleService = ((ConnectDeviceService.LocalBinder) iBinder).getService();
            LogUtils.show("HandleBleMeasure---绑定成功，正在监听水平度的通知。。。");
            bleService.enableTXNotification(ConnectDeviceService.LEVELNESS_SERVICE_UUID, ConnectDeviceService.LEVELNESS_TX_CHAR_UUID);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };


    /**
     * 注册接收蓝牙状态的广播接收器
     *
     * @param
     */
    private void registerBleRecevier() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(BleDeviceReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("HandleBleMeasureDataReceiverService----onDestroy----销毁了");
        unregisterBleRecevier();
        unbindBleService();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 注销广播接受者
     */
    public void unregisterBleRecevier() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(BleDeviceReceiver);
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleParam.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleParam.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleParam.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleParam.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    /**
     * 测量数据上传服务器成功后，回调此方法，此时可以将数据清空
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void uploadResponeCallBack(HandleDataResultMsgEvent event) {
        LogUtils.show("在后台服务-----收到上次成功的回调");
        switch (event.getFlag()) {
            case 1:
                verticalOptinsDataList.clear();
                LogUtils.show("uploadResponeCallBack-----垂直度数据清空了");

                break;

            case 2:
                levelOptionsDataList.clear();
                LogUtils.show("uploadResponeCallBack-----平整度数据清空了");
                break;


        }
    }


    /**
     * 由点击开始测量按钮后，MeasureManagerActivity向服务器请求创建记录表和对应的管控要点
     * 服务器返回的数据处理完之后，调用此方法通知service已经请求完毕
     * 即可在这里从数据库中将我们需要的两种server_id拿过来（RulerCheck和每一个rulerCheckOption）
     * 如果没有server_id那么接下来上传蓝牙测量数据的时候会走无网络的请求格式
     * @param flag
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void netBussCallBack(String flag) {
        LogUtils.show("netBussCallBack---查看创建好记录表后返回的标志:"+flag);
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
//        先更新RulerCheck的server_id
        String where = " where id = " + verticalOption.getRulerCheck().getId() + " or id = " + levelOption.getRulerCheck().getId();
        List<RulerCheck> rulerCheckList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(where);
        if (rulerCheckList.size() > 0) {
            for (RulerCheck check : rulerCheckList) {
                if (check.getId() == verticalOption.getRulerCheck().getId()) {
                    RulerCheck rulerCheck = verticalOption.getRulerCheck();
                    rulerCheck.setServerId(check.getServerId());
                    verticalOption.setRulerCheck(rulerCheck);
                } else if (check.getId() == levelOption.getRulerCheck().getId()) {
                    RulerCheck rulerCheck = levelOption.getRulerCheck();
                    rulerCheck.setServerId(check.getServerId());
                    levelOption.setRulerCheck(rulerCheck);
                }
            }

        }
        bleDataDbHelper.close();
        //        再更新RulerCheckOption的Server_id
        String optionWhere = " where id = " + verticalOption.getId() + " or " + levelOption.getId();
        List<RulerCheckOptions> rulerCheckOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), verticalOption.getRulerCheck(), optionWhere);
        if (rulerCheckOptionsList.size() > 0) {
            for (RulerCheckOptions options : rulerCheckOptionsList) {
                if (options.getId() == verticalOption.getId()) {
                    verticalOption.setServerId(options.getServerId());
                } else if (options.getId() == levelOption.getId()) {
                    levelOption.setServerId(options.getServerId());
                }

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void heightFloorCallBack(HeightFloorMsgEvent event) {
        LogUtils.show("heightFloorCallBack----层高改变了："+event.toString());
        if (event.getFlag() == 1) {
            verticalOptionMeasure = event.getMeasure();
        } else if (event.getFlag() == 2) {
            levelOptionMeasure = event.getMeasure();
        }
    }

    private final BroadcastReceiver BleDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            //*********************//
            /**
             * 蓝牙连接成功
             */
            if (action.equals(BleParam.ACTION_GATT_CONNECTED)) {
                textToSpeechHelper.speakChinese("蓝牙连接成功");
            }

            //*********************//
            /**
             * 蓝牙连接断开
             */
            if (action.equals(BleParam.ACTION_GATT_DISCONNECTED)) {
//                bleCallBackResult.bleDisconnected();
                textToSpeechHelper.speakChinese("蓝牙连接断开");
            }


            //*********************//
            /**
             * 发现服务
             */
            if (action.equals(BleParam.ACTION_GATT_SERVICES_DISCOVERED)) {
//                发现一个服务
//                bleCallBackResult.bleDiscoveredService();
            }
            //*********************//
            /**
             * 收到蓝牙数据,数据处理流程：
             * 1.先判断测量中的所有管控要点的数据获取是否成功
             * 2.根据特征值的UUID判断收到的数据是水平度的还是垂直度的数据
             *  2.1 提前把水平度和垂直度的管控要点从集合里独立出来
             *    2.1.1 根据管控要点模板中的flag去判断该管控要点是属于什么
             *    2.1.2 flg为1-垂直度，2-水平度，3-其他
             * 3.知道该数据对应哪个管控要点后，就可以将数据变为一个checkOptionData对象并添加到集合中
             * 4.
             */
            if (action.equals(BleParam.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(BleParam.EXTRA_DATA);
                final String uuid = intent.getStringExtra(BleParam.EXTRA_UUID);

                try {
                    String text = new String(txValue, "UTF-8");
                    LogUtils.show("HandleBleMeasure---在服务中收到蓝牙数据：" + text);
                    /**
                     * 要先判断获取到的checkoptionslist的大小是否大于0
                     * 大于0说明进入测量后，界面传过来的数据成功获取，
                     * 才能将收到的数据保存到对应的管控要点上
                     */
                    RulerCheckOptionsData optionsData = new RulerCheckOptionsData();
                    optionsData.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                    optionsData.setData(text);
                    optionsData.setUpload_flag(0);

                    /**
                     * 收到水平度的数据
                     */
                    if (levelOption!=null && uuid.equalsIgnoreCase(ConnectDeviceService.LEVELNESS_TX_CHAR_UUID.toString())) {
                        textToSpeechHelper.speakChinese("收到"+levelOption.getRulerOptions().getOptionsName()+"数据" + text);
                        optionsData.setRulerCheckOptions(levelOption);
                        LogUtils.show("查看当前水平度管控要点的Id："+levelOption.getId());
                        int id = OperateDbUtil.addRealMeasureDataToSqlite(getApplicationContext(), optionsData);
                        optionsData.setId(id);
                        levelOptionsDataList.add(optionsData);
                        LogUtils.show("查看收到水平度数据，保存到数据库后的对象："+optionsData);
                        if (levelOptionsDataList.size() >= 5 && levelOptionsDataList.size()%2==1) {
                            completeResult(levelOptionsDataList,levelOption,levelOptionMeasure);
                            uploadDataToServer(levelOptionsDataList,levelOption);
                        }
//                        bleService.writeRxCharacteristic(ConnectDeviceService.LEVELNESS_SERVICE_UUID,ConnectDeviceService.LEVELNESS_RX_CHAR_UUID,"111".getBytes());
                    }

                    /**
                     * 收到垂直度的数据
                     */
                    else if (verticalOption != null && uuid.equalsIgnoreCase(ConnectDeviceService.VERTICALITY_TX_CHAR_UUID.toString())) {
                        textToSpeechHelper.speakChinese("收到"+verticalOption.getRulerOptions().getOptionsName()+"数据" + text);
                        optionsData.setRulerCheckOptions(verticalOption);
                        int id = OperateDbUtil.addRealMeasureDataToSqlite(getApplicationContext(), optionsData);
                        optionsData.setId(id);
                        verticalOptinsDataList.add(optionsData);
                        LogUtils.show("查看收到垂直度数据，保存到数据库后的对象："+optionsData);
                        if (verticalOptinsDataList.size() >= 5 && verticalOptinsDataList.size()%2==1) {
                            completeResult(verticalOptinsDataList,verticalOption,verticalOptionMeasure);
                            uploadDataToServer(verticalOptinsDataList,verticalOption);
                        }
//                        bleService.writeRxCharacteristic(ConnectDeviceService.VERTICALITY_SERVICE_UUID, ConnectDeviceService.VERTICALITY_RX_CHAR_UUID, "222".getBytes());
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            //*********************//
            if (action.equals(BleParam.DEVICE_DOES_NOT_SUPPORT_UART)) {
//
//                mService.disconnect();
            }
        }
    };

    /**
     * 将数据执行上传到服务器
     *
     * @param dataList
     */
    private void uploadDataToServer(List<RulerCheckOptionsData> dataList,RulerCheckOptions checkOptions) {
        Intent intent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
        List<RulerCheckOptions> list = new ArrayList<>();
        checkOptions.setUpdateTime(DateFormatUtil.transForMilliSecond(new Date()));
        LogUtils.show("查看上传到服务器之前的管控要点信息："+checkOptions);
        list.add(checkOptions);
        intent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPDATE_DATA);
        intent.putExtra(PerformMeasureNetIntentService.GET_CREATE_OPTIONS_DATA_KEY, (Serializable) list);
        intent.putExtra(PerformMeasureNetIntentService.GET_UPDATE_DATA_KEY, (Serializable) dataList);
        startService(intent);
    }

    private void completeResult(List<RulerCheckOptionsData> dataList,RulerCheckOptions checkOptions,OptionMeasure optionMeasure) {
        if (optionMeasure != null) {

            int realNum = 0;
            int qualifiedNum = 0;
            float frealnum = 0.0f;
            float fq = 0.0f;
            for (int i=0; i<dataList.size();i++) {
                String data = dataList.get(i).getData().trim();
                try {
                    float datanum = Float.valueOf(data);
                    /**
                     * 根据操作标志来计算结果，
                     * 1 =, 2 <=, 3 >=, 4 +, 5 -, 6 普通, 7 高级
                     */
                    switch (optionMeasure.getOperate()) {
                        case 1:
                            if (datanum == optionMeasure.getStandard() ) {
                                qualifiedNum++;
                            }
                            break;
                        case 2:
                            if (datanum < optionMeasure.getStandard() || datanum == optionMeasure.getStandard()) {
                                qualifiedNum++;
                            }
                            break;
                        case 3:
                            if (datanum > optionMeasure.getStandard() || datanum == optionMeasure.getStandard()) {
                                qualifiedNum++;
                            }
                            break;
                    }
                    realNum++;
                } catch (Exception e) {
                    Log.e("exceptions", "completeResult: 错误原因："+e.getMessage() );
                }
            }
            frealnum = realNum;
            fq = qualifiedNum;
            float qualifiedRate = (fq / frealnum);
            LogUtils.show("completeResult: 查看计算出来的实测点数："+realNum+",合格点数："+qualifiedNum+",合格率："+qualifiedRate );
            checkOptions.setFloorHeight(optionMeasure.getData());
            checkOptions.setMeasuredNum(realNum);
            checkOptions.setQualifiedNum(qualifiedNum);
            checkOptions.setQualifiedRate(Float.parseFloat(String.format("%.2f",qualifiedRate*100)));
        }

    }


    /**
     * 实现一个内部的 Service，实现让后台服务的优先级提高到前台服务，这里利用了 android 系统的漏洞，
     * 不保证所有系统可用，测试在7.1.1 之前大部分系统都是可以的，不排除个别厂商优化限制
     */
    public static class DaemonInnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.e("DaemonInnerService", "ceshi,DaemonInnerService.CLASS开启");
            startForeground(DAEMON_SERVICE_ID,new Notification());
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.e("DaemonInnerService", "ceshi,DaemonInnerService销毁了");
        }
    }

}
