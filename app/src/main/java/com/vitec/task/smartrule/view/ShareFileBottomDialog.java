package com.vitec.task.smartrule.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.TextView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.MeasureFileActivity;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ShareFileToQQ;

import java.io.File;

public class ShareFileBottomDialog extends Dialog implements View.OnClickListener{

    private TextView tvCancel;
    private LinearLayout llShareToWx;
    private LinearLayout llShareToQQ;

    private File chooseFile;

    public ShareFileBottomDialog(@NonNull Context context) {
        super(context);
    }

    public ShareFileBottomDialog(@NonNull Context context, int themeResId,File chooseFile) {
        super(context, themeResId);
        this.chooseFile = chooseFile;
    }

    protected ShareFileBottomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share_file);
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
        tvCancel = findViewById(R.id.tv_cancel);
        llShareToQQ = findViewById(R.id.ll_share_qq);
        llShareToWx = findViewById(R.id.ll_share_wx);

        llShareToWx.setOnClickListener(this);
        llShareToQQ.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 取消
             */
            case R.id.tv_cancel:
                dismiss();
                break;

            /**
             * 分享到QQ
             */
            case R.id.ll_share_qq:
                LogUtils.show("onClick----点击了分享到QQ");
                ShareFileToQQ.sendToQQ(getContext(),chooseFile.getPath());
                dismiss();
                break;

            /**
             * 分享到微信
             */
            case R.id.ll_share_wx:
                LogUtils.show("onClick-----点击了分享到微信");
                WeChatHelper helper = new WeChatHelper(getContext());
                helper.regToWx();
                helper.shareFileToWx(chooseFile);
                dismiss();
                break;
        }
    }
}
