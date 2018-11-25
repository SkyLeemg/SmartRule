package com.vitec.task.smartrule.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.activity.CaptureActivity;
import com.vitec.task.smartrule.R;

public class BaseActivity extends Activity  {

    public ImageView imgMenu;
    public TextView tvTitle;
    public ImageView imgIcon;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        initWidget();
    }
    /**
     * 动态申请蓝牙定位权限
     */
    public void requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("请求位置权限");
                builder.setMessage("该APP需要定位权限");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });

                builder.show();

            }

            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("请求读写权限");
                builder.setMessage("该读写文件");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });

                builder.show();

            }

        }
    }



    /**
     * 动态权限回调方法
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:

                break;
        }
    }
    public void initWidget() {
        imgIcon = (ImageView) findViewById(R.id.img_icon_toolbar);
        imgMenu = findViewById(R.id.img_menu_toolbar);
        tvTitle = findViewById(R.id.tv_toolbar_title);

//        imgIcon.setOnClickListener(this);
    }

    public void setTvTitle(String title) {
        tvTitle.setText(title);
    }

    public void setImgSource(int menuSource, int iconSource) {
        imgMenu.setImageResource(menuSource);
        imgIcon.setImageResource(iconSource);
    }



}
