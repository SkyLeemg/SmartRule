package com.vitec.task.smartrule.bean;

public class BleMessage {

    private String action;

    private String bleData;

    private int connectState;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBleData() {
        return bleData;
    }

    public void setBleData(String bleData) {
        this.bleData = bleData;
    }

    public int getConnectState() {
        return connectState;
    }

    public void setConnectState(int connectState) {
        this.connectState = connectState;
    }
}
