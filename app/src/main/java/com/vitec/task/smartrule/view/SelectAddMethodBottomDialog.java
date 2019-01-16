package com.vitec.task.smartrule.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.AddMemberActivity;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.db.DataBaseParams;

public class SelectAddMethodBottomDialog extends Dialog implements View.OnClickListener{

    private LinearLayout llManually;
    private LinearLayout llFaceToFace;
    private Button btnCancel;
    private RulerCheckProject project;
    private int size;


    public SelectAddMethodBottomDialog(@NonNull Context context) {
        super(context);
    }

    public SelectAddMethodBottomDialog(@NonNull Context context, int themeResId,RulerCheckProject project,int size) {
        super(context, themeResId);
        this.project = project;
        this.size = size;
    }

    protected SelectAddMethodBottomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bottom_add_member_selector);
        //获取当前Activity所在的窗体
        Window dialogWindow = getWindow();
        if(dialogWindow == null)
        { return; }
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //设置Dialog距离底部的距离
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);

        llFaceToFace = findViewById(R.id.ll_face_to_face);
        llManually = findViewById(R.id.ll_manually);
        btnCancel = findViewById(R.id.btn_cancel);

        llManually.setOnClickListener(this);
        llFaceToFace.setOnClickListener(this);
        btnCancel.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 取消
             */
            case R.id.btn_cancel:
                dismiss();
                break;

            /**
             * 手动输入添加按钮
             */
            case R.id.ll_manually:
                Intent intent = new Intent(getContext(), AddMemberActivity.class);
                intent.putExtra(DataBaseParams.measure_project_id, project);
                getContext().startActivity(intent);
                dismiss();
                break;

            /**
             * 面对面扫二维码添加按钮
             */
            case R.id.ll_face_to_face:
                QrCodeDialog dialog = new QrCodeDialog(getContext(),R.style.BottomDialog,project.getQrCode(),size);
                dialog.show();
                dismiss();
                break;
        }
    }
}
