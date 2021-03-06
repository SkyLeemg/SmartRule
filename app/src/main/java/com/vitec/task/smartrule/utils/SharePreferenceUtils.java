package com.vitec.task.smartrule.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.zxing.decoding.Intents;
import com.vitec.task.smartrule.db.DataBaseParams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2018/4/4.
 */
public class SharePreferenceUtils {

    /**
     * 存储用户数据的key
     */
    public static final String user_table = "iot_ruler_user";
    public static final String user_real_name = "real_name";
    public static final String user_pwd = "password";
    public static final String user_id = "user_id";
    public static final String user_wx_data = "data";
    public static final String user_mobile = "mobile";
    public static final String user_token = "token";
    public static final String user_type = "user_type";//账号类型，1-手机号码，2-微信号，3两者都有


    public static Set<String> getKeySet() {
        Set<String> keySet = new HashSet<>();
        keySet.add(SharePreferenceUtils.user_real_name);
        keySet.add(user_pwd);
        keySet.add(user_id);
        keySet.add(user_wx_data);
        keySet.add(user_mobile);
        keySet.add(user_token);
        keySet.add(user_type);
        return keySet;
    }


    /**
     * 用于保存数据到SharePreference中
     * @param context 传递一个上下文对象，用于初次实例化sharedPreferences对象
     * @param map 传递一个Stirng类型的map集合，集合中存放着需要保存到sharedPreferences中的数据
     */
    public static void savaData(Context context,Map<String,String> map,String tableName) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(tableName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        Set<String> keySet = map.keySet();
        for (String keyString:keySet) {
            editor.putString(keyString, map.get(keyString));
        }
        editor.commit();

    }



    /**
     * 用于从sharedPreferences中获取对象
     * @param context 传递一个上下文对象，用于初次实例化sharedPreferences对象
     * @param keySet 传递一个set集合，集合中string都是要从sharePreferences中获取的key
     * @return
     */
    public static Map<String, String> getData(Context context,Set<String> keySet,String tableName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(tableName, Context.MODE_PRIVATE);

        Map<String, String> map = new HashMap<>();
        for (String keyString:keySet) {
            map.put(keyString, sharedPreferences.getString(keyString,""));
        }

        return map;
    }

    /**
     * 存储token
     * @param context
     * @param token
     */
    public static void saveToken(Context context, String token) {
        Map<String, String> map = new HashMap<>();
        map.put(DataBaseParams.user_token, token);
        savaData(context,map,"tokenSave");
    }

    public static String getToken(Context context){
        Set<String> setString = new HashSet<>();
        setString.add(DataBaseParams.user_token);
        Map<String, String> map = getData(context, setString, "tokenSave");
        String token = map.get(DataBaseParams.user_token);
        return token;
    }



}
