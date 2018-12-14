package com.vitec.task.smartrule.bean.event;

import com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService;

public class HandleDataResultMsgEvent {

    private int flag;

    public HandleDataResultMsgEvent(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
