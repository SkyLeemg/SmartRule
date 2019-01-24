package com.vitec.task.smartrule.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.activity.CaptureActivity;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.ChooseMeasureMsgActivity;
import com.vitec.task.smartrule.activity.DeviceManagerActivity;
import com.vitec.task.smartrule.activity.MeasureFileActivity;
import com.vitec.task.smartrule.activity.MeasureRecordActivity;
import com.vitec.task.smartrule.activity.MeasureTeamManagerActivity;
import com.vitec.task.smartrule.activity.WaitingMeasureActivity;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.GetMemberAndUnitMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.List;


public class HomePageFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "首页";
    private View view;
    private LinearLayout llCreateMeasure;
    private LinearLayout llMeasureRecord;
    private LinearLayout llWaitingMeasure;
    private LinearLayout llMeasureFile;
    private LinearLayout llDevManager;
    private ImageView imgQr;

    private static final int REQUEST_CODE = 0x01;
    //打开扫描界面请求码
    //扫描成功返回码
    private int RESULT_OK = 0xA1;
    private boolean isCurrentPage = false;
    private boolean isRegister = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: 首页的onCreate" );

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_page,null);
        Log.e(TAG, "onCreateView: 加载了首页" );
        initView();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!this.isHidden())
        isCurrentPage = true;
    }

    private void initView() {
        llCreateMeasure = view.findViewById(R.id.ll_create_measure);
        llMeasureRecord = view.findViewById(R.id.ll_meausre_record);
        llWaitingMeasure = view.findViewById(R.id.ll_waiting_measure);
        llMeasureFile = view.findViewById(R.id.ll_measure_file);
        llDevManager = view.findViewById(R.id.ll_dev_manager);
        imgQr = view.findViewById(R.id.img_scan_qr);

        llCreateMeasure.setOnClickListener(this);
        llMeasureRecord.setOnClickListener(this);
        llWaitingMeasure.setOnClickListener(this);
        llMeasureFile.setOnClickListener(this);
        llDevManager.setOnClickListener(this);
        imgQr.setOnClickListener(this);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.show("onDestroyView");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            LogUtils.show("HomePageFragment-----onHiddenChanged");
            isCurrentPage = false;
        } else {
            isCurrentPage = true;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("onDestroy");
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**新建测量**/
           case  R.id.ll_create_measure:
               startActivity(new Intent(getActivity(),ChooseMeasureMsgActivity.class));
               isCurrentPage = false;
            break;

            /**测量记录**/
            case R.id.ll_meausre_record:
                startActivity(new Intent(getActivity(), MeasureRecordActivity.class));
                isCurrentPage = false;
                break;

            /**等待测量**/
            case R.id.ll_waiting_measure:
                startActivity(new Intent(getActivity(), WaitingMeasureActivity.class));
                isCurrentPage = false;
                break;

            /**测量文件**/
            case R.id.ll_measure_file:
                startActivity(new Intent(getActivity(), MeasureFileActivity.class));
                isCurrentPage = false;
                break;

            /**设备管理**/
            case R.id.ll_dev_manager:
                startActivity(new Intent(getActivity(), DeviceManagerActivity.class));
                isCurrentPage = false;
                break;

                /******打开扫一扫******/
            case R.id.img_scan_qr:
                //打开二维码扫描界面
                if (!isRegister) {
                    EventBus.getDefault().register(this);
                    isRegister = true;
                }

                Intent arIntent = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(arIntent,REQUEST_CODE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMemberAndUnitCallBack(GetMemberAndUnitMsgEvent event) {
        if (isCurrentPage ) {
            if (event.isSuccess()) {
                queryProjectGroupData();
            } else {
                Toast.makeText(getContext(),event.getMsg(),Toast.LENGTH_SHORT).show();
            }
        }
        if (isRegister) {
            EventBus.getDefault().unregister(this);
        }

    }


    /**
     * 查找数据库中的项目组
     */
    private int queryProjectGroupData() {
        LogUtils.show("首页-------收到更新测量组消息；");
        User user = OperateDbUtil.getUser(getContext());
//        String where = " where " + DataBaseParams.user_user_id + '=' + user.getUserID()+" ORDER BY id DESC";
        List<RulerCheckProject> projectList = OperateDbUtil.queryAllProjectOrderMember(getContext(),user.getUserID());
        if (isCurrentPage && projectList.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(DataBaseParams.check_project_name, (Serializable) projectList);
            Intent intent = new Intent(getActivity(), MeasureTeamManagerActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            isCurrentPage = false;
        }
        return projectList.size();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
            //http://iot-test.vkforest.com/api/ruler/addGroupMemberByQRCode?project_id=25
            if (scanResult.contains("http://") && scanResult.contains("project_id")) {
                User user = OperateDbUtil.getUser(getContext());
                StringBuffer sb = new StringBuffer();
                sb.append(scanResult);
                sb.append("&");
                sb.append(DataBaseParams.user_user_id);
                sb.append("=");
                sb.append(user.getUserID());
                LogUtils.show("打印扫码出来的字符串:" + sb.toString());
                Bundle bundle1 = new Bundle();
                bundle1.putString(DataBaseParams.check_project_qrcode, sb.toString());
                Intent enterIntent = new Intent(getActivity(), ProjectManageRequestIntentService.class);
                enterIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle1);
                enterIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_scan_qr_enter_project);
                getActivity().startService(enterIntent);
            } else {
                Toast.makeText(getContext(),"进组失败",Toast.LENGTH_SHORT).show();
            }

        }
    }
}
