package com.vitec.task.smartrule.dfu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.BaseActivity;
import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.dfu.service.DfuService;
import com.vitec.task.smartrule.dfu.service.UpdateFirmIntentService;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.LogUtils;

import java.io.File;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DfuDialog extends Dialog {
    private String path;
    private ProgressBar progressBar;
    private TextView tvTip;

    public DfuDialog(@NonNull Context context, String path) {
        super(context);
        this.path = path;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfu);
        LogUtils.show("调用了对话框的create方法");
        initView();
        initData();

    }

    private void initView() {
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(100);
        tvTip = findViewById(R.id.tv_dialog_tip);
        tvTip.setText("获取升级包中...");
        setTitle("正在更新...请勿关闭窗口和蓝牙");
//        设置禁止点击外部消失
        setCanceledOnTouchOutside(false);
//        设置禁止点击返回键
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                return false;
            }
        });
    }

    private void initData() {
        DfuServiceListenerHelper.registerProgressListener(getContext(), dfuProgressListener);
//        Intent intent = getIntent();
//        String path = intent.getStringExtra(UpdateFirmIntentService.PATH_KEY);
        BleDeviceDbHelper bleDeviceDbHelper = new BleDeviceDbHelper(getContext());
        List<BleDevice> bleDevices = bleDeviceDbHelper.queryAllDevice();
//        String mac = ConnectDeviceService.current_connecting_mac_address;
        BleDevice currentDev = null;
        for (int i=0;i<bleDevices.size();i++) {
            if (ConnectDeviceService.current_connecting_mac_address.equals(bleDevices.get(i).getBleMac())) {
                currentDev = bleDevices.get(i);
            }
        }
        if (currentDev != null) {
            LogUtils.show("升级服务----正在启动");
            DfuServiceInitiator starter = new DfuServiceInitiator(currentDev.getBleMac())
                    .setDeviceName(currentDev.getBleName())
                    .setKeepBond(true);
            starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
//            File file = new File(path);
            LogUtils.show("升级服务----查看升级包路径："+path);
//            Uri contentUri;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                contentUri = FileProvider.getUriForFile(getContext(), "com.vitec.task.smartrule.fileprovider", file);
//            } else {
//                contentUri = Uri.fromFile(file);
//            }
//            LogUtils.show("查看升级的文件路径："+path);
            starter.setZip(path);
            DfuServiceController controller = starter.start(getContext(), DfuService.class);

        }
    }


    private final DfuProgressListener dfuProgressListener = new DfuProgressListener() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            tvTip.setText("正在连接蓝牙...");
            LogUtils.show("DfuProgressListener----正在连接"+deviceAddress);
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            LogUtils.show("DfuProgressListener----已连接"+deviceAddress);
            tvTip.setText("蓝牙连接成功...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            LogUtils.show("DfuProgressListener----进度开始中。。。"+deviceAddress);
            tvTip.setText("准备升级...");
        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            LogUtils.show("DfuProgressListener----进度已经开始。。。"+deviceAddress);
            tvTip.setText("开始升级...");
        }

        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onEnablingDfuMode。。。"+deviceAddress);
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            LogUtils.show("升级进度: " + deviceAddress + "百分比" + percent + ",speed "
                    + speed + ",avgSpeed " + avgSpeed + ",currentPart " + currentPart
                    + ",partTotal " + partsTotal);
            progressBar.setProgress(percent);
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onFirmwareValidating。。。"+deviceAddress);
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDeviceDisconnecting。。。"+deviceAddress);
        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDeviceDisconnected。。。"+deviceAddress);
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDfuCompleted。。。"+deviceAddress);
            Toast.makeText(getContext(),"升级成功",Toast.LENGTH_SHORT).show();
            tvTip.setText("升级成功...");
            File file = new File(path);
            file.delete();

            dismiss();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            LogUtils.show("DfuProgressListener----onDfuAborted。。。"+deviceAddress);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            LogUtils.show("DfuProgressListener----错误："+deviceAddress+",错误原因编号："+error+",信息："+message);
            if (error == 4096) {
                Toast.makeText(getContext(),"更新失败，蓝牙连接中断",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(),"更新失败，"+message,Toast.LENGTH_SHORT).show();
            }

            dismiss();
        }
    };


    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        super.setOnDismissListener(listener);
        DfuServiceListenerHelper.unregisterProgressListener(getContext(), dfuProgressListener);
        LogUtils.show("注册取消");
    }
}
