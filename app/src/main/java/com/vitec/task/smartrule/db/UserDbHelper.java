package com.vitec.task.smartrule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vitec.task.smartrule.bean.User;

import java.util.ArrayList;
import java.util.List;

public class UserDbHelper {

    private static final String TAG = "UserDbHelper";
    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public UserDbHelper(Context context) {
        this.context = context;
        sqLiteDatabase = SQLiteDatabase.openDatabase("data/data/" +context.getPackageName() +
                "/databases/"+DataBaseParams.databaseName, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void close() {
        sqLiteDatabase.close();

    }


    public List<User> queryUserDataFromSqlite(String where) {
        User user = null;
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM iot_ruler_user "+where;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(DataBaseParams.measure_id)));
                user.setUserName(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_user_name)));
                user.setLoginName(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_login_name)));
                user.setMobile(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_mobile)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_password)));
                user.setUserID(cursor.getInt(cursor.getColumnIndex(DataBaseParams.user_user_id)));
                user.setWxUnionId(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_wx_unionid)));
                user.setWid(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_wid)));
                user.setWxData(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_data)));
                user.setUserJob(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_job)));
                user.setToken(cursor.getString(cursor.getColumnIndex(DataBaseParams.user_token)));
                Log.e(TAG, "queryUserDataFromSqlite: 搜索到的用户:"+user );
                userList.add(user);
            } while (cursor.moveToNext());
        }
        return userList;
    }

    /**
     * 想表格中插入数据
     * @param values
     * @return
     */
    public boolean insertUserToSqlite(String tableName,ContentValues values) {
        int renum = (int) sqLiteDatabase.insert(tableName, "", values);
        Log.e(TAG, "insertDevToSqlite: 打印插入数据库后返回的数字："+renum );
        if (renum!=-1)
            return true;
        else return false;
    }


    /**
     * 根据Userid更新用户数据
     * @param values 需要更新的数据内容
     * @param id 更新的条件
     * @return
     */
    public boolean updateUserData(ContentValues values,String[] id) {
        int result = sqLiteDatabase.update(DataBaseParams.user_table_name, values, new String(DataBaseParams.measure_id + "=?"), id);
        Log.e(TAG, "insertDevToSqlite: 打印插入数据库后返回的数字："+result );
        if (result!=-1)
            return true;
        else return false;
    }


}
