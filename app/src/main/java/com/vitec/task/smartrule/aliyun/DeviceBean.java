package com.vitec.task.smartrule.aliyun;

public class DeviceBean {
    public  String productKey ;
    private String deviceName ;
    private String deviceSecret ;
    public  boolean isInitDone = false;
    private String resultMsg;

    public DeviceBean(String productKey, String deviceName, String deviceSecret) {
        this.productKey = productKey;
        this.deviceName = deviceName;
        this.deviceSecret = deviceSecret;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public boolean isInitDone() {
        return isInitDone;
    }

    public void setInitDone(boolean initDone) {
        isInitDone = initDone;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
}
