package com.vitec.task.smartrule.bean.event;

public class MoblieRequestResutEvent {

    private String requst_flag;//请求类型区分：发送验证码和验证是否正确
    private boolean isSuccess;
    private String msg;

    public String getRequst_flag() {
        return requst_flag;
    }

    public void setRequst_flag(String requst_flag) {
        this.requst_flag = requst_flag;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "MoblieRequestResutEvent{" +
                "requst_flag='" + requst_flag + '\'' +
                ", isSuccess=" + isSuccess +
                ", msg='" + msg + '\'' +
                '}';
    }
}
