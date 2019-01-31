package com.vitec.task.smartrule.bean.event;

public class LoginMsgEvent {
    private boolean isSuccess;
    private String msg;
    private String respone;

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

    public String getRespone() {
        return respone;
    }

    public void setRespone(String respone) {
        this.respone = respone;
    }

    @Override
    public String toString() {
        return "LoginMsgEvent{" +
                "isSuccess=" + isSuccess +
                ", msg='" + msg + '\'' +
                ", respone='" + respone + '\'' +
                '}';
    }
}
