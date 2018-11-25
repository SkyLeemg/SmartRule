package com.vitec.task.smartrule.utils;

import android.content.Context;
import android.view.WindowManager;

public class ScreenSizeUtil {

    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = manager.getDefaultDisplay().getWidth();
        return width;
    }


    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int height = manager.getDefaultDisplay().getHeight();
        return height;
    }
}
