package com.vitec.task.smartrule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.MainActivity;
import com.vitec.task.smartrule.interfaces.IFragmentGettable;
import com.vitec.task.smartrule.interfaces.ISettable;


public class HomePageFragment extends Fragment  {
    private static final String TAG = "首页";
    public View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_page,null);
        Log.e(TAG, "onCreateView: 加载了首页" );
        return view;

    }

}
