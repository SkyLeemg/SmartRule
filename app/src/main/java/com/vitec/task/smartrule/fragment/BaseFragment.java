package com.vitec.task.smartrule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;

import org.w3c.dom.Text;

public class BaseFragment extends Fragment {

    public View view;
    public TextView tvToolBarTitle;
    public ImageView imgMenu;
    public ImageView imgOtherIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void initFragmentView() {
        tvToolBarTitle = view.findViewById(R.id.tv_toolbar_title);
        imgMenu = view.findViewById(R.id.img_menu_toolbar);
        imgOtherIcon = view.findViewById(R.id.img_icon_toolbar);
    }
}
