package com.vitec.task.smartrule.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luck.picture.lib.tools.ScreenUtils;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ZXingUtils;

public class QrCodeDialog extends Dialog implements View.OnClickListener {

    private TextView tvProjectName;
    private ImageView imgQrCode;
    private TextView tvCancel;
    private String qrString;
    private RulerCheckProject project;
    private int size;


    public QrCodeDialog(@NonNull Context context) {
        super(context);
    }

    public QrCodeDialog(@NonNull Context context, int themeResId, RulerCheckProject project, int size) {
        super(context, themeResId);
        this.qrString = project.getQrCode();
        this.project = project;
        this.size = size;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_show_qr);
        //获取当前Activity所在的窗体
        Window dialogWindow = getWindow();
        if(dialogWindow == null)
        { return; }
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.width = WindowManager.LayoutParams.;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
////        lp.x = 0;
//        lp.y = 20;


        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity( Gravity.CENTER);
        tvCancel = findViewById(R.id.tv_cancel);
        tvProjectName = findViewById(R.id.tv_project_name);
        imgQrCode = findViewById(R.id.img_qr_code);
        tvCancel.setOnClickListener(this);
        int wsize = (int) (size * 0.75);
        int left = (int)(size * 0.05) ;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(wsize,wsize);
        params.setMargins(left,0,left,0);
        imgQrCode.setLayoutParams(params);
        LogUtils.show("打印查看二维码宽度:" + wsize);
        Bitmap bitmap = ZXingUtils.createQRImage(qrString, 500,  500);
        imgQrCode.setImageBitmap(bitmap);
        tvProjectName.setText(project.getProjectName()+"项目测量组");

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
        }
    }
}
