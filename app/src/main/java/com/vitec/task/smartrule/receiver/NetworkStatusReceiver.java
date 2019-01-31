package com.vitec.task.smartrule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatusReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的networkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
        }
    }
}
