package com.vitec.task.smartrule.utils;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

public class Base64Utils {

//    /***
//     * 编码
//     * encode by Base64
//     */
//    public static String encodeBase64(byte[]input) throws Exception{
//        Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
//        Method mainMethod= clazz.getMethod("encode", byte[].class);
//        mainMethod.setAccessible(true);
//        Object retObj=mainMethod.invoke(null, new Object[]{input});
//        return (String)retObj;
//    }
//    /***
//     * 解码
//     * decode by Base64
//     */
//    public static byte[] decodeBase64(String input) throws Exception{
//        Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
//        Method mainMethod= clazz.getMethod("decode", String.class);
//        mainMethod.setAccessible(true);
//        Object retObj=mainMethod.invoke(null, input);
//        return (byte[])retObj;
//    }

    /**
     * 编码
     * @param str
     * @return
     */
    public static String encodeBase64(String str) {
        byte[] b = null;
        String s = null;

        try {
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            s =Base64.encodeToString(b, Base64.DEFAULT);
        }
        Log.e("在base里查看"+str, s);
        return s;
    }

    /**
     * 解码
     * @param s
     * @return
     */
    public static String decodeBase64(String s) {
        byte[] b = null;
        String result = null;
        if (s != null) {

            try {
                b = Base64.decode(s, Base64.DEFAULT);
                result = new String(b, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
