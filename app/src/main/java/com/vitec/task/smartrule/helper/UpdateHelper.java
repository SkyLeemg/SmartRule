package com.vitec.task.smartrule.helper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

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


    public  long startDownload(CheckUpdataMsg checkUpdataMsg) {
        this.checkUpdataMsg = checkUpdataMsg;
//        获取系统下载器
//        DownloadManager dm = new DownloadManager();
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        设置下载地址

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(checkUpdataMsg.getDownloadUrl()));
//        设置下载文件的类型
        request.setMimeType("application/vnd.android.package-archive");
//        设置下载存放的文件夹和文件名字
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, checkUpdataMsg.getFileName()+".zip");
//        设置下载时或者下载完成时，通知栏是否显示
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        设置标题
        request.setTitle("正在下载"+checkUpdataMsg.getAppName()+"升级包");

//        执行下载，并返回任务唯一id
        return dm.enqueue(request);

    }

    public void readyDownload(CheckUpdataMsg checkUpdataMsg) {
        downloadRevevier=new DownloadReceiver(startDownload(checkUpdataMsg));
        registerDownloadRecevier();
    }



    public void registerDownloadRecevier() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(downloadRevevier, intentFilter);
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
//                    Intent updateIntent = new Intent(context, DfuDialog.class);
//                    updateIntent.putExtra(UpdateFirmIntentService.DEAL_FLAG_KEY, UpdateFirmIntentService.DEAL_FLAG_UPDATE_FIRM);

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),checkUpdataMsg.getFileName() + ".zip");
                    String path = file.getPath();
                    DfuDialog dfuActivity = new DfuDialog(context, path);
                    dfuActivity.show();
//                    updateIntent.putExtra(UpdateFirmIntentService.PATH_KEY, path);
////                    context.startService(updateIntent);
//                    context.startActivity(updateIntent);
                }
            }
        }
    }


}
