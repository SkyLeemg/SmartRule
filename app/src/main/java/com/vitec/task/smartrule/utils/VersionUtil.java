package com.vitec.task.smartrule.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VersionUtil {

    public static int getLocalVersionCode(Context context) {
        int versionCode = 1;

        try {
            PackageInfo packageInfo = context.getApplicationContext().getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getLocalVersionName(Context context) {
        String verName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext().getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            verName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return verName;
    }
}
