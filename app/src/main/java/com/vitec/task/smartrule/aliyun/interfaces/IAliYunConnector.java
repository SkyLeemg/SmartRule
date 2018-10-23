package com.vitec.task.smartrule.aliyun.interfaces;

import android.content.Context;

import com.vitec.task.smartrule.aliyun.DeviceBean;
import com.vitec.task.smartrule.interfaces.MainActivityGettable;

public interface IAliYunConnector {


    void connect(Context context, MainActivityGettable gettable);

    void setDeviceMsg(DeviceBean deviceMsg);

    String getResult();
}
