package com.vitec.task.smartrule.bean;

/**
 * 靠尺设备蓝牙信息
 */
public class BleDevice {

    private String bleName;//蓝牙名称
    private String bleMac;//蓝牙mac地址

    public BleDevice(String bleName, String bleMac) {
        this.bleName = bleName;
        this.bleMac = bleMac;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    public String getBleMac() {
        return bleMac;
    }

    public void setBleMac(String bleMac) {
        this.bleMac = bleMac;
    }
}
