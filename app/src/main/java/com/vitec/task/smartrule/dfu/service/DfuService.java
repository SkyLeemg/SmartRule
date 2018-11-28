package com.vitec.task.smartrule.dfu.service;

import android.app.Activity;

import com.vitec.task.smartrule.dfu.NotificationActivity;

import no.nordicsemi.android.dfu.BuildConfig;
import no.nordicsemi.android.dfu.DfuBaseService;

public class DfuService extends DfuBaseService {


    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return NotificationActivity.class;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;

    }


}
