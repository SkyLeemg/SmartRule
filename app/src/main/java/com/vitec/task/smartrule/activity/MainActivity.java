package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.google.zxing.activity.CaptureActivity;
import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.aliyun.DeviceBean;
import com.vitec.task.smartrule.bean.BleMessage;
import com.vitec.task.smartrule.fragment.FragmentControllerImpl;
import com.vitec.task.smartrule.interfaces.IFragmentController;
import com.vitec.task.smartrule.interfaces.ISettable;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.ParameterKey;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseFragmentActivity implements ISettable,View.OnClickListener {

    private static final String TAG = "MainActivity";
    private BottomNavigationBar bottomNavigationBar;
    private IFragmentController controller;
    public TextView tvToolBarTitle;
    public ImageView imgMenu;
    public ImageView imgOtherIcon;
    private MKLoader mkLoader;
    private RelativeLayout llToolBar;

    private static final int REQUEST_CODE = 0x01;
    //打开扫描界面请求码
    //扫描成功返回码
    private int RESULT_OK = 0xA1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initView();
        requestLocationPermissions();
    }

    private void initView() {
        initToolBarView();
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar_container);
        controller = new FragmentControllerImpl(this, bottomNavigationBar,this);
        controller.initBottomNav();
        controller.addBottomNav();
        mkLoader = findViewById(R.id.loading);

    }

    public void initToolBarView() {
        View layout = getLayoutInflater().inflate(R.layout.base_toolbar, null);
        tvToolBarTitle = findViewById(R.id.tv_toolbar_title);
        imgMenu = findViewById(R.id.img_menu_toolbar);
        imgOtherIcon = findViewById(R.id.img_icon_toolbar);

        llToolBar = findViewById(R.id.ll_toolbar);
    }

    @Override
    public void setTitle(String title) {
        tvToolBarTitle.setText(title);
    }

    @Override
    public void setMenuVisible(int flag) {
        imgMenu.setVisibility(flag);
    }

    @Override
    public void setMenuResouce(int resouce) {
        imgMenu.setImageResource(resouce);
    }

    @Override
    public void setIconVisible(int flag) {
        imgOtherIcon.setVisibility(flag);
        imgOtherIcon.setOnClickListener(this);
    }

    @Override
    public void setIconResouce(int resouce) {
        imgOtherIcon.setImageResource(resouce);
    }

    @Override
    public ISettable getSettable() {
        return this;
    }

    @Override
    public void setToolBarVisible(int flag) {
//        toolbar.setVisibility(flag);
        llToolBar.setVisibility(flag);
        if (flag == View.GONE) {
            llToolBar.setBackgroundResource(R.color.transparent_color);
        } else {
            llToolBar.setBackgroundResource(R.color.pblue_bar_color);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_icon_toolbar:
                //打开二维码扫描界面
                Intent arIntent = new Intent(this, CaptureActivity.class);
                startActivityForResult(arIntent,REQUEST_CODE);
                break;
        }
    }

    /**
     * 蓝牙服务返回的数据
     * @param
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connectBussCallBack(BleMessage message) {
//        mLoadingDialog.dismiss();
        if (message.getAction().equals(BleParam.ACTION_GATT_CONNECTED)) {
            Log.e(TAG, "connectBussCallBack: 主界面收到连接成功的提示" );
            Toast.makeText(this, "连接成功",Toast.LENGTH_SHORT).show();
        }

        if (message.getAction().equals(BleParam.ACTION_GATT_DISCONNECTED)) {
            Toast.makeText(this, "连接断开",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * {
     "productKey":"a1UNvLhFILp",
     "deviceName":"testS",
     "deviceSecret":"AO3zGlGP6ivc0JHHyY5Si5mS40UpGcco",
     "bleMac":"FC:04:35:B7:9A:D8"
     }
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            mkLoader.setVisibility(View.VISIBLE);
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
//            tvMsg.setText(scanResult);
            try {
                JSONObject json = new JSONObject(scanResult);
                String productKey = json.optString(ParameterKey.product_key);
                String deviceName = json.optString(ParameterKey.device_name);
                String deviceSecret = json.optString(ParameterKey.device_secret);
                String bleMac = json.optString(ParameterKey.ble_mac);
                DeviceBean deviceBean = new DeviceBean(productKey, deviceName, deviceSecret);
//                mConnect.setDeviceMsg(deviceBean);
//                mConnect.connect(getApplicationContext(),this);

            } catch (JSONException e) {
                e.printStackTrace();
                mkLoader.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"错误的二维码",Toast.LENGTH_SHORT).show();
            }

            Log.e(TAG, "onActivityResult: 查看扫码返回值："+scanResult );
        }
    }
}
