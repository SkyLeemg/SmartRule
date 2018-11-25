package com.vitec.task.smartrule.interfaces;

import com.vitec.task.smartrule.bean.BleDevice;

import java.util.List;

public interface IDevManager {

    void setDevs(List<BleDevice> bleDevices);

    List<BleDevice> getDevs();
}
