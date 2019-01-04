package com.vitec.task.smartrule.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.vitec.task.smartrule.R;

public class BottomDialog extends Dialog implements View.OnClickListener{

    private Button takePhoto;
    private Button choosePhoto;
    private Button cancel;
    private Activity activity;
    private Fragment fragment;

    public BottomDialog(@NonNull Context context) {
        super(context);
    }

    public BottomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BottomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bottom);
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
        lp.y = 20;

        //设置Dialog距离底部的距离
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);

        takePhoto = findViewById(R.id.take_photo);
        choosePhoto = findViewById(R.id.choose_photo);
        cancel = findViewById(R.id.btn_cancel);

        takePhoto.setOnClickListener(this);
        choosePhoto.setOnClickListener(this);
        cancel.setOnClickListener(this);

    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 拍照
             */
            case R.id.take_photo:
                PictureSelector.create(fragment)
                        .openCamera(PictureMimeType.ofImage())
                        .previewImage(true)
                        .isCamera(true)
                        .openClickSound(false)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
                dismiss();
                break;

            /**
             * 相册选择
             */
            case R.id.choose_photo:
                PictureSelector.create(fragment)
                        .openGallery(PictureMimeType.ofImage())//打开相册
                        .maxSelectNum(1)//最多选择一张
                        .minSelectNum(1)
                        .imageSpanCount(4)//每行显示4张
                        .selectionMode(PictureConfig.SINGLE)//单选
                        .previewImage(true)//可以预览图片
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                dismiss();
                break;
            /**
             * 取消
             */
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }
}
