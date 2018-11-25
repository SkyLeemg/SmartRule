package com.vitec.task.smartrule.bean;

/**
 * 靠尺设备蓝牙信息
 */
public class BleDevice {
    private int id;
    private String bleName;//蓝牙名称
    private String bleMac;//蓝牙mac地址
    private int lastConnectTime;
    private int imgResouce;
    private String bleAlias;//蓝牙别名，用户输入的

    public BleDevice(String bleName, String bleMac) {
        this.bleName = bleName;
        this.bleMac = bleMac;
    }

    public BleDevice() {

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

    public int getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(int lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public int getImgResouce() {
        return imgResouce;
    }

    public void setImgResouce(int imgResouce) {
        this.imgResouce = imgResouce;
    }

    public String getBleAlias() {
        return bleAlias;
    }

    public void setBleAlias(String bleAlias) {
        this.bleAlias = bleAlias;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "id=" + id +
                ", bleName='" + bleName + '\'' +
                ", bleMac='" + bleMac + '\'' +
                ", lastConnectTime=" + lastConnectTime +
                ", imgResouce=" + imgResouce +
                ", bleAlias='" + bleAlias + '\'' +
                '}';
    }
}
