package com.vitec.task.smartrule.bean;

/**
 * 发送get网络请求，服务器返回的数据对象
 */
public class NetCallBackMessage {

    private String resultJson;//服务器返回的字符串
    private int urlFlag;//发送get请求时，附送的url标志

    public NetCallBackMessage(String resultJson, int urlFlag) {
        this.resultJson = resultJson;
        this.urlFlag = urlFlag;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public int getUrlFlag() {
        return urlFlag;
    }

    public void setUrlFlag(int urlFlag) {
        this.urlFlag = urlFlag;
    }
}
