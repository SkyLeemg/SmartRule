package com.vitec.task.smartrule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vitec.task.smartrule.R;

public class CheckDataFragment extends Fragment {

    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_check_data,null);
//        initFragmentView();
//        initViewData();
        return view;

    }

    private void initViewData() {
//        tvToolBarTitle.setText("查看数据");
//        imgOtherIcon.setVisibility(View.GONE);
    }
}
