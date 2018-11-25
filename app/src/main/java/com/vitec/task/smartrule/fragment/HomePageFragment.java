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

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.ChooseMeasureMsgActivity;


public class HomePageFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "首页";
    private View view;
    private android.widget.Button btnEnterMeasure;

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
        btnEnterMeasure = view.findViewById(R.id.btn_enter_measure);

        btnEnterMeasure.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_enter_measure:
                Intent intent = new Intent(getActivity(), ChooseMeasureMsgActivity.class);
                startActivity(intent);
                break;
        }
    }
}
