package com.vitec.task.smartrule.bean.event;

/**
 * 通用响应回调
 */
public class CostomMsgEvent {

    private int flag;//1-提交意见返回
    private boolean isSuccess;
    private String msg;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
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
}
