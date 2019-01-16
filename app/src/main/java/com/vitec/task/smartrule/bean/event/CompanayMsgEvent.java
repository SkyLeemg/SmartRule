package com.vitec.task.smartrule.bean.event;

import com.vitec.task.smartrule.bean.CompanyMessage;

/**
 * 公司资料
 */
public class CompanayMsgEvent {
    private Object object;
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

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
