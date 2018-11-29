package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.CheckUpdataMsg;
import com.vitec.task.smartrule.helper.UpdateHelper;
import com.vitec.task.smartrule.dfu.service.UpdateFirmIntentService;
import com.vitec.task.smartrule.interfaces.IBleCallBackResult;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.service.intentservice.GetMudelIntentService;
import com.vitec.task.smartrule.utils.BleParam;
import com.vitec.task.smartrule.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainHomeActivity extends BaseActivity implements View.OnClickListener,IBleCallBackResult{

    private GridView gvManager;
    private List<GvItem> itemList;
    private GvManagerAdapter managerAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        requestLocationPermissions();

        initView();
        initData();
    }

    private void initData() {
        itemList = new ArrayList<>();
        itemList.add(new GvItem(R.mipmap.icon_main_add, "新建测量"));
        itemList.add(new GvItem(R.mipmap.icon_main_record, "测量记录"));
        itemList.add(new GvItem(R.mipmap.icon_main_dev, "设备管理"));



        Intent intent = new Intent(this, GetMudelIntentService.class);
        startService(intent);

        managerAdapter = new GvManagerAdapter();
        gvManager.setAdapter(managerAdapter);

        gvManager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0://查询测量数据
                        startActivity(new Intent(MainHomeActivity.this,ChooseMeasureMsgActivity.class));
                        break;
                    case 1://查询文件

                        break;
                    case 2:
                        startActivity(new Intent(MainHomeActivity.this,DeviceManagerActivity.class));
                        break;

                }
            }
        });


    }

    private void initView() {
        initWidget();
        setImgSource(R.mipmap.icon_user_unselect, R.mipmap.icon_user_unselect);
        imgIcon.setVisibility(View.VISIBLE);
        setTvTitle("自动测量靠尺");
        imgIcon.setOnClickListener(this);
        gvManager = findViewById(R.id.gv_mng_list);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.img_icon_toolbar:
                Intent intent = new Intent(this, UserCenterActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        registerBleRecevier(this);
        bindBleService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        unbindBleService();
        unregisterBleRecevier();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netBussCallBack(final CheckUpdataMsg checkUpdataMsg) {
        LogUtils.show("查看收到的更新对象："+checkUpdataMsg.toString());

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
        builder.setNegativeButton("否", null);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateHelper updateHelper = new UpdateHelper(MainHomeActivity.this);
                        updateHelper.readyDownload(checkUpdataMsg);
                    }
                }).start();

            }
        });
        builder.show();
    }

    /**********************监听蓝牙状态的回调方法开始***************************/
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
        LogUtils.show("在主界面，收到蓝牙数据："+data+",UUID："+uuid);
        if (uuid.equalsIgnoreCase(ConnectDeviceService.SYSTEM_TX_CHAR_UUID.toString())) {
            Intent updateIntent = new Intent(this, UpdateFirmIntentService.class);
            updateIntent.putExtra(UpdateFirmIntentService.DEAL_FLAG_KEY, UpdateFirmIntentService.DEAL_FLAG_CHECK_UPDATE);
            updateIntent.putExtra(UpdateFirmIntentService.VERSION_FLAG, data);
            startService(updateIntent);
        }
    }

    @Override
    public void bleBindSuccess() {
        if (bleService != null && ConnectDeviceService.mConnectionState == BleParam.STATE_CONNECTED) {
            /**
             * CD01---请求靠尺版本信息
             */
            LogUtils.show("在主界面中写入数据：");
            bleService.writeRxCharacteristic(ConnectDeviceService.SYSTEM_RX_SERVICE_UUID,ConnectDeviceService.SYSTEM_RX_CHAR_UUID,"CD01".getBytes());
        }
    }


    /**********************监听蓝牙状态的回调方法结束***************************/



    class GvManagerAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int i) {
            return itemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
           ViewHolder holder;
            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.item_gridview_for_home_activity, null);
                holder = new ViewHolder();
                holder.imageView = view.findViewById(R.id.img_item_dev_pic);
                holder.tvTitle = view.findViewById(R.id.tv_item_dev_name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvTitle.setText(itemList.get(i).getTitle());
            holder.imageView.setImageResource(itemList.get(i).getImgResouse());
            return view;
        }
    }

    class ViewHolder {
        ImageView imageView;
        TextView tvTitle;
    }



    class GvItem {
        int imgResouse;
        String title;

        public GvItem(int imgResouse, String title) {
            this.imgResouse = imgResouse;
            this.title = title;
        }

        public int getImgResouse() {
            return imgResouse;
        }

        public void setImgResouse(int imgResouse) {
            this.imgResouse = imgResouse;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "GvItem{" +
                    "imgResouse=" + imgResouse +
                    ", title='" + title + '\'' +
                    '}';
        }
    }
}
