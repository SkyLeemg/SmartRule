package com.vitec.task.smartrule.bean;

public class WxResultMessage {

    private int flag;//标志，1-微信登录，2-绑定微信
    private String uionId;
    private String data;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getUionId() {
        return uionId;
    }

    public void setUionId(String uionId) {
        this.uionId = uionId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WxResultMessage{" +
                "flag=" + flag +
                ", uionId='" + uionId + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
