package com.vitec.task.smartrule.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class ShareFileToQQ {

    public static void sendToQQ(Context context,String fileUrl) {
        Intent share = new Intent(Intent.ACTION_SEND);
        ComponentName componentName = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
        share.setComponent(componentName);
        File file = new File(fileUrl);//这里是文件的路径

        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        share.setType("*/*");
        context.startActivity(Intent.createChooser(share,"发送"));
    }
}
