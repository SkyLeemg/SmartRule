package com.vitec.task.smartrule.bean;

import java.util.Objects;

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
    private int bleVerCode;
    private String bleVerName;


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

    public int getBleVerCode() {
        return bleVerCode;
    }

    public void setBleVerCode(int bleVerCode) {
        this.bleVerCode = bleVerCode;
    }

    public String getBleVerName() {
        return bleVerName;
    }

    public void setBleVerName(String bleVerName) {
        this.bleVerName = bleVerName;
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
                ", bleVerCode=" + bleVerCode +
                ", bleVerName='" + bleVerName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BleDevice bleDevice = (BleDevice) o;
        return id == bleDevice.id &&
                lastConnectTime == bleDevice.lastConnectTime &&
                imgResouce == bleDevice.imgResouce &&
                bleVerCode == bleDevice.bleVerCode &&
                Objects.equals(bleName, bleDevice.bleName) &&
                Objects.equals(bleMac, bleDevice.bleMac) &&
                Objects.equals(bleAlias, bleDevice.bleAlias) &&
                Objects.equals(bleVerName, bleDevice.bleVerName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, bleName, bleMac, lastConnectTime, imgResouce, bleAlias, bleVerCode, bleVerName);
    }
}
