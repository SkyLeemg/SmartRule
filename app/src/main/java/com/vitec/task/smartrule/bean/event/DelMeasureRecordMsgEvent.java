package com.vitec.task.smartrule.bean.event;

public class DelMeasureRecordMsgEvent {

    private boolean isSuccess;

    public DelMeasureRecordMsgEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
