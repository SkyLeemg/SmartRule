package com.vitec.task.smartrule.utils;

import android.content.Context;

public class TokenUtil {
    private static String token;

    public static String getToken(Context context) {
        if (token == null|| token.equals("")) {
            token = SharePreferenceUtils.getToken(context);
        }
        return token;
    }

    public static void setToken(String token) {
        TokenUtil.token = token;
    }
}
