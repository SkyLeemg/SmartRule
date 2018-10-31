package com.vitec.task.smartrule.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.vitec.task.smartrule.service.ConnectDeviceService;

import java.util.ArrayList;

public class ServiceUtils {
    /**
     * 判断服务是否开启
     *
     * @return 开启返回true,未开启返回false
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(100);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    public static void startConnectDeviceSerivce(Context context) {
        if (!ServiceUtils.isServiceRunning(context, "com.vitec.task.smartrule.service.ConnectDeviceService")) {
            Intent intent1 = new Intent(context, ConnectDeviceService.class);
            context.startService(intent1);
        }
    }

    public static void stopConnectDeviceSerivce(Context context) {
//        if (ServiceUtils.isServiceRunning(context, "com.vitec.vitecbeacon.service.MyBeaconService")) {
            Intent intent1 = new Intent(context, ConnectDeviceService.class);
            context.stopService(intent1);
//        }
    }


}
