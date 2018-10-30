package com.vitec.task.smartrule.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.vitec.task.smartrule.R;

public class LoadingDialog extends Dialog {


    private String tipMsg;
    private TextView tvTip;

    public LoadingDialog(@NonNull Context context,String tipMsg) {
        super(context);
        this.tipMsg = tipMsg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        initView();
    }

    private void initView() {
        tvTip = findViewById(R.id.dialog_tv_loading_tip);
        tvTip.setText(tipMsg);
    }
}
