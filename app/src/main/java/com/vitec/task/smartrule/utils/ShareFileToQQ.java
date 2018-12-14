package com.vitec.task.smartrule.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareFileToQQ {

    public static void sendToQQ(Context context,String fileUrl) {
        Intent share = new Intent(Intent.ACTION_SEND);
        ComponentName componentName = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
        share.setComponent(componentName);
        File file = new File(fileUrl);//这里是文件的路径
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
             contentUri = FileProvider.getUriForFile(context, "com.vitec.task.smartrule.fileprovider", file);
//            share.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            contentUri = Uri.fromFile(file);

        }
        share.putExtra(Intent.EXTRA_STREAM, contentUri);


        share.setType("*/*");
        context.startActivity(Intent.createChooser(share,"发送"));
    }

    public static void sendFile(Context context, List<File> fileList) {
        ArrayList<Uri> uriList = new ArrayList<>();
        Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);//发送多个文件
        for (File file : fileList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, "com.vitec.task.smartrule.fileprovider", file);
                uriList.add(contentUri);
            } else {
                Uri contentUri = Uri.fromFile(file);
                uriList.add(contentUri);
            }
        }
        share.setType("*/*");
        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        context.startActivity(Intent.createChooser(share,"分享文件"));
    }
}
