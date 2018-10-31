package com.vitec.task.smartrule.db;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyDbFileFromAsset {


    private Context context;
    public CopyDbFileFromAsset(Context context){
        this.context = context;
    }

    // 复制和加载区域数据库中的数据
    public  String  CopySqliteFileFromRawToDatabases(String SqliteFileName) throws IOException {

        // 第一次运行应用程序时，加载数据库到data/data/当前包的名称/database/<db_name>

        File dir = new File("data/data/" + context.getPackageName() + "/databases");
        Log.i("dd","!dir.exists()=" + !dir.exists()+",,"+dir.getPath());
        Log.i("dd","!dir.isDirectory()=" + !dir.isDirectory());

        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        File file= new File(dir, SqliteFileName);
        InputStream inputStream = null;
        OutputStream outputStream =null;

        //通过IO流的方式，将assets目录下的数据库文件，写入到SD卡中。
        if (!file.exists()) {
            try {
                file.createNewFile();

                inputStream = context.getClass().getClassLoader().getResourceAsStream("assets/" + SqliteFileName);
                outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int len ;

                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer,0,len);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            finally {

                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }

            }

        }

        return file.getPath();

    }
}
