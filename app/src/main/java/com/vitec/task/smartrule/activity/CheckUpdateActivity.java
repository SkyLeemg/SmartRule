package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.bean.CheckUpdataMsg;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.dfu.service.UpdateFirmIntentService;
import com.vitec.task.smartrule.helper.UpdateHelper;
import com.vitec.task.smartrule.interfaces.IBleCallBackResult;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.service.intentservice.UpdateAppVersionIntentService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.VersionUtil;
import com.vitec.task.smartrule.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.vitec.task.smartrule.utils.BleParam.STATE_CONNECTED;

public class CheckUpdateActivity extends BaseActivity implements View.OnClickListener,IBleCallBackResult{

    private TextView tvAppVer;
    private TextView tvDevVer;
    private TextView tvAppCheckUpdate;
    private TextView tvDevCheckUpdate;
    private List<BleDevice> devices;
    private BleDevice currentDevice;
    private ConnectDeviceService mService = null;
    private LoadingDialog loadingDialog;
    private boolean isStart = false;//判断是否已经启动升级服务

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);
        requestLocationPermissions();
        registerBleRecevier(this);
        bindBleService();
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        initWidget();
        setTvTitle("检查更新");
//        imgIcon.setImageResource(R.mipmap.icon_back);
//        imgIcon.setVisibility(View.VISIBLE);
//        imgIcon.setOnClickListener(this);

        tvAppVer = findViewById(R.id.tv_app_ver);
        tvDevVer = findViewById(R.id.tv_dev_ver);
        tvAppCheckUpdate = findViewById(R.id.tv_app_check_update);
        tvDevCheckUpdate = findViewById(R.id.tv_dev_check_update);
        tvDevCheckUpdate.setOnClickListener(this);
        tvAppCheckUpdate.setOnClickListener(this);

        initData();
    }

    private void initData() {
        tvAppVer.setText(VersionUtil.getLocalVersionName(CheckUpdateActivity.this));
        BleDeviceDbHelper bleDeviceDbHelper = new BleDeviceDbHelper(CheckUpdateActivity.this);
        currentDevice = new BleDevice();
        if (ConnectDeviceService.mConnectionState == STATE_CONNECTED) {
            devices = new ArrayList<>();
            devices = bleDeviceDbHelper.queryAllDevice();
            if (devices.size() > 0) {
                for (int i=0;i<devices.size();i++) {
                    if (devices.get(i).getBleMac().equals(ConnectDeviceService.current_connecting_mac_address)) {
                        currentDevice = devices.get(i);
                        tvDevVer.setText(currentDevice.getBleVerName());
                        tvDevCheckUpdate.setVisibility(View.VISIBLE);
                        break;
                    }

                }
            }
        } else {
            tvDevCheckUpdate.setVisibility(View.GONE);
            tvDevVer.setText("靠尺未连接");
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_icon_toolbar:
                CheckUpdateActivity.this.finish();
                break;

            /**
             * 检查靠尺版本
             */
            case R.id.tv_dev_check_update:
                loadingDialog = new LoadingDialog(this, "正在检查...");
                loadingDialog.show();
                if (bleService != null && ConnectDeviceService.mConnectionState == BleParam.STATE_CONNECTED) {
                    /**
                     * CD01---请求靠尺版本信息
                     */
                    LogUtils.show("在主界面中写入数据：");
                    bleService.writeRxCharacteristic(ConnectDeviceService.SYSTEM_RX_SERVICE_UUID, ConnectDeviceService.SYSTEM_RX_CHAR_UUID, "CD02".getBytes());

                } else {
                    if (loadingDialog.isShowing())
                    loadingDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"靠尺未连接",Toast.LENGTH_SHORT).show();
                }
                break;

            /**
             * 检查APP版本
             */
            case R.id.tv_app_check_update:
                loadingDialog = new LoadingDialog(this, "正在检查...");
                loadingDialog.show();
                startService(new Intent(this, UpdateAppVersionIntentService.class));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netBussCallBack(final CheckUpdataMsg checkUpdataMsg) {
        if (checkUpdataMsg.isSuccess()) {
            if (checkUpdataMsg.isNeedUpdate()) {
                /*****************靠尺升级回调********************/
                if (checkUpdataMsg.getUpdate_flag() == 1) {
                    LogUtils.show("查看收到的更新对象：" + checkUpdataMsg.toString());
//            if (loadingDialog.isShowing())
//                loadingDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("检测到靠尺固件升级");
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("检测到靠尺固件新版本：");
                    stringBuffer.append("\n");
                    stringBuffer.append(checkUpdataMsg.getUpdateLog());
                    stringBuffer.append("\n");
                    stringBuffer.append("固件大小：" + checkUpdataMsg.getFileSize());
                    stringBuffer.append("\n");
                    stringBuffer.append("是否立即下载更新？");
                    builder.setMessage(stringBuffer.toString());
                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isStart = false;
                        }
                    });
                    builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    UpdateHelper updateHelper = new UpdateHelper(CheckUpdateActivity.this);
                                    updateHelper.readyDownloadZip(checkUpdataMsg);
                                }
                            }).start();

                        }
                    });
                    builder.show();
                }

                /*******************软件升级回调******************/
                if (checkUpdataMsg.getUpdate_flag() == 2) {
                    final   UpdateHelper updateHelper = new UpdateHelper(getApplicationContext());
                    final String name = checkUpdataMsg.getFileName() +checkUpdataMsg.getVerName()+ ".apk";
                    //如果升级包已经下载
                    if (updateHelper.hasDownload(name)) {
                        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("软件更新")
                                .setMessage("新版本已经下载好，是否立即更新安装？")
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        updateHelper.installApp(name);
                                    }
                                })
                                .setNegativeButton("下次安装", null)
                                .create();
                        dialog.show();
                    } else {
                        //如果未下载
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("检测到软件升级");
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("新版本介绍：");
                        stringBuffer.append("\n");
                        stringBuffer.append(checkUpdataMsg.getUpdateLog());
                        stringBuffer.append("\n");
                        stringBuffer.append("APK大小：" + checkUpdataMsg.getFileSize());
                        stringBuffer.append("\n");
                        stringBuffer.append("是否立即下载更新？");
                        builder.setMessage(stringBuffer.toString());
                        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                isStart = false;
                            }
                        });
                        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateHelper.readyDownloadApk(checkUpdataMsg);

                            }
                        });
                        builder.show();
                    }


                }

            } else {
                isStart = false;
                Toast.makeText(getApplicationContext(),"已是最新版本",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),checkUpdataMsg.getMsg(),Toast.LENGTH_SHORT).show();
        }

        if (loadingDialog!=null&&loadingDialog.isShowing())
            loadingDialog.dismiss();


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        unbindBleService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindBleService();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void bleConnectSuccess() {

    }

    @Override
    public void bleDisconnected() {

    }

    @Override
    public void bleDiscoveredService() {

    }

    @Override
    public void bleReceviceData(String data, String uuid) {
        if (uuid.equalsIgnoreCase(ConnectDeviceService.SYSTEM_TX_CHAR_UUID.toString()) && !isStart) {
            isStart = true;
            Intent updateIntent = new Intent(this, UpdateFirmIntentService.class);
            updateIntent.putExtra(UpdateFirmIntentService.DEAL_FLAG_KEY, UpdateFirmIntentService.DEAL_FLAG_CHECK_UPDATE);
            updateIntent.putExtra(UpdateFirmIntentService.VERSION_FLAG, data);
            startService(updateIntent);
        }
    }

    @Override
    public void bleBindSuccess() {

    }
}
