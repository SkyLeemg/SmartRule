package com.vitec.task.smartrule.helper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.vitec.task.smartrule.bean.CheckUpdataMsg;
import com.vitec.task.smartrule.dfu.DfuDialog;
import com.vitec.task.smartrule.utils.LogUtils;

import java.io.File;

public class UpdateHelper {

    private Context context;
    private DownloadReceiver downloadRevevier;
    private CheckUpdataMsg checkUpdataMsg;

    public UpdateHelper(Context context) {
        this.context = context;
    }


    public  long startDownload(CheckUpdataMsg checkUpdataMsg,String name) {
        this.checkUpdataMsg = checkUpdataMsg;
//        获取系统下载器
//        DownloadManager dm = new DownloadManager();
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        设置下载地址

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(checkUpdataMsg.getDownloadUrl()));
//        设置下载文件的类型
        request.setMimeType("application/vnd.android.package-archive");
//        checkUpdataMsg.getFileName()+".zip"
//        设置下载存放的文件夹和文件名字
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
//        设置下载时或者下载完成时，通知栏是否显示
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        设置标题
        request.setTitle("正在下载"+checkUpdataMsg.getAppName());

//        执行下载，并返回任务唯一id
        return dm.enqueue(request);

    }

    public void readyDownloadZip(CheckUpdataMsg checkUpdataMsg) {
        String name = "update" +checkUpdataMsg.getVerCode()+ ".zip";
        downloadRevevier = new DownloadReceiver(startDownload(checkUpdataMsg, name));
        registerDownloadRecevier();
    }
    public void readyDownloadApk(CheckUpdataMsg checkUpdataMsg) {
        this.checkUpdataMsg = checkUpdataMsg;
        String name = checkUpdataMsg.getFileName() +checkUpdataMsg.getVerName()+ ".apk";
        downloadRevevier = new DownloadReceiver(startDownload(checkUpdataMsg, name));
        registerDownloadRecevier();
    }




    public void registerDownloadRecevier() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(downloadRevevier, intentFilter);
    }
    public void unregisterDownloadRecevier() {
        context.unregisterReceiver(downloadRevevier);
    }

    public class DownloadReceiver extends BroadcastReceiver {

        private long downloadId;
        public DownloadReceiver(long downloadId) {
            this.downloadId = downloadId;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager dm = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
            long id = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId != id) {
                return;
            }
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            LogUtils.show("接到升级包下载完成的广播");
            Cursor c = dm.query(query);
            if (c != null && c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
//                下载失败也会返回这个广播，所以要判断下是否真的下载成功
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    //靠尺固件下载完成
                    if (checkUpdataMsg.getUpdate_flag() == 1) {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update" +checkUpdataMsg.getVerCode()+ ".zip");
                        String path = file.getPath();
                        DfuDialog dfuActivity = new DfuDialog(context, path);
                        dfuActivity.show();
                    } else if (checkUpdataMsg.getUpdate_flag() == 2) {
                        //软件APK下载完成
                        final String name = checkUpdataMsg.getFileName() +checkUpdataMsg.getVerName()+ ".apk";
                        installApp(name);
                    }

                    unregisterDownloadRecevier();
                }
            }
        }
    }


    /********************************软件更新部分***********************************/


    /**
     * 获取软件版本号
     * @return
     */
    public  int getVercode() {
        int verCode = -1;

        try {
            verCode = context.getPackageManager().getPackageInfo("com.vitec.vitecbeacon", 0).versionCode;
//            Log.e(TAG, "getVercode: 本机软件版本号："+verCode );
        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "getVercode: "+e.getMessage() );
        }
        return verCode;
    }
    /**
     * 安装filePath里的应用
     * 关于在代码中安装 APK 文件，在 Android N 以后，为了安卓系统为了安全考虑，
     * 不能直接访问软件，需要使用 fileprovider 机制来访问、打开 APK 文件。
     //     * @param filePath apk文件的路径字符串
     */
    public  void installApp(String apkName) {
        File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),apkName);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//大于等于android 7.0的时候
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    "com.vitec.task.smartrule.fileprovider",
                    apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");

        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
//        unregisterDownloadRecevier();
    }


    /**
     * 判断新版本的APP是否已经下载了
     * @param apkname apk的文件名
     * @return true-已经下载好了，false-还未下载
     */
    public boolean hasDownload(String apkname) {
        File apkFile= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),apkname);
        return apkFile.exists();
    }



}
