package com.vitec.task.smartrule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.DeviceGridViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceManagerFragment extends Fragment implements View.OnClickListener{

    private View view;
    private LinearLayout llAddDev;
    private TextView tvNoRuleDev;
    private TextView tvNoLaserDev;
    private GridView gvRule;
    private GridView gvLaser;
    private List<String> rules;
    private DeviceGridViewAdapter ruleDevAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_manager_page,null);
        initView();
        initViewData();
        return view;

    }

    private void initView() {
        llAddDev = view.findViewById(R.id.ll_add_dev);
        tvNoLaserDev = view.findViewById(R.id.tv_no_laser_dev);
        tvNoRuleDev = view.findViewById(R.id.tv_no_rule_dev);
        gvRule = view.findViewById(R.id.gv_rule);
        gvLaser = view.findViewById(R.id.gv_laser);
    }

    private void initViewData() {
        rules = new ArrayList<>();
        for (int i=0;i<5;i++) {
            rules.add(i + "号设备");
        }

        ruleDevAdapter = new DeviceGridViewAdapter(getActivity(), rules);
        gvRule.setAdapter(ruleDevAdapter);
        tvNoRuleDev.setVisibility(View.GONE);
        gvLaser.setVisibility(View.GONE);
        setListViewHeighBaseOnChildren(gvRule);
    }

    private void setListViewHeighBaseOnChildren(GridView gridView) {
        if (gridView == null) {
            return;
        }
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        int count = listAdapter.getCount() / 3;
        if (listAdapter.getCount() % 3 != 0) {
            count++;
        }
        for (int i = 0; i < count; i++) {
            View listItem = listAdapter.getView(i, null, gridView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();

        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_add_dev:

                break;
        }
    }
}
