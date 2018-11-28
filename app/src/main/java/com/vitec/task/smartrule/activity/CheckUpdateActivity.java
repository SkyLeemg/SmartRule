package com.vitec.task.smartrule.activity;

import android.bluetooth.BluetoothClass;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.bean.CheckUpdataMsg;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.VersionUtil;

import java.util.ArrayList;
import java.util.List;

import static com.vitec.task.smartrule.utils.BleParam.STATE_CONNECTED;

public class CheckUpdateActivity extends BaseActivity implements View.OnClickListener{

    private TextView tvAppVer;
    private TextView tvDevVer;
    private TextView tvAppCheckUpdate;
    private TextView tvDevCheckUpdate;
    private List<BleDevice> devices;
    private BleDevice currentDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);

        initView();
    }

    private void initView() {
        initWidget();
        setTvTitle("检查更新");
        imgIcon.setImageResource(R.mipmap.icon_back);
        imgIcon.setVisibility(View.VISIBLE);
        imgIcon.setOnClickListener(this);

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

            case R.id.tv_dev_check_update:

                break;

            case R.id.tv_app_check_update:

                break;
        }
    }
}
