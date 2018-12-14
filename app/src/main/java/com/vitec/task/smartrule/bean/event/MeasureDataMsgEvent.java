package com.vitec.task.smartrule.bean.event;

public class MeasureDataMsgEvent {

    private int flag;

    public MeasureDataMsgEvent(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
