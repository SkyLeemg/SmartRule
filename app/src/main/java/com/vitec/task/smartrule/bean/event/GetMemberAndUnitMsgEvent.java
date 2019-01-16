package com.vitec.task.smartrule.bean.event;

public class GetMemberAndUnitMsgEvent {

    private boolean isSuccess;
    private String msg;

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
        return "GetMemberAndUnitMsgEvent{" +
                "isSuccess=" + isSuccess +
                ", msg='" + msg + '\'' +
                '}';
    }
}
