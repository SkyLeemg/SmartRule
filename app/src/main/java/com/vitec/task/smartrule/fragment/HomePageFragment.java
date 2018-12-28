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
import android.widget.LinearLayout;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.ChooseMeasureMsgActivity;
import com.vitec.task.smartrule.activity.DeviceManagerActivity;
import com.vitec.task.smartrule.activity.MeasureFileActivity;
import com.vitec.task.smartrule.activity.MeasureRecordActivity;
import com.vitec.task.smartrule.activity.WaitingMeasureActivity;


public class HomePageFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "首页";
    private View view;
    private LinearLayout llCreateMeasure;
    private LinearLayout llMeasureRecord;
    private LinearLayout llWaitingMeasure;
    private LinearLayout llMeasureFile;
    private LinearLayout llDevManager;

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

    private void initView() {
        llCreateMeasure = view.findViewById(R.id.ll_create_measure);
        llMeasureRecord = view.findViewById(R.id.ll_meausre_record);
        llWaitingMeasure = view.findViewById(R.id.ll_waiting_measure);
        llMeasureFile = view.findViewById(R.id.ll_measure_file);
        llDevManager = view.findViewById(R.id.ll_dev_manager);

        llCreateMeasure.setOnClickListener(this);
        llMeasureRecord.setOnClickListener(this);
        llWaitingMeasure.setOnClickListener(this);
        llMeasureFile.setOnClickListener(this);
        llDevManager.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**新建测量**/
           case  R.id.ll_create_measure:
               startActivity(new Intent(getActivity(),ChooseMeasureMsgActivity.class));
            break;

            /**测量记录**/
            case R.id.ll_meausre_record:
                startActivity(new Intent(getActivity(), MeasureRecordActivity.class));
                break;

            /**等待测量**/
            case R.id.ll_waiting_measure:
                startActivity(new Intent(getActivity(), WaitingMeasureActivity.class));
                break;

            /**测量文件**/
            case R.id.ll_measure_file:
                startActivity(new Intent(getActivity(), MeasureFileActivity.class));
                break;

            /**设备管理**/
            case R.id.ll_dev_manager:
                startActivity(new Intent(getActivity(), DeviceManagerActivity.class));
                break;
        }
    }
}
