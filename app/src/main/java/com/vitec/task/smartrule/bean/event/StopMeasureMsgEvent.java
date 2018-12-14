package com.vitec.task.smartrule.bean.event;

/**
 * 停止测量响应对象
 */
public class StopMeasureMsgEvent {

    private int flag;//0 请求失败，1请求成功

    public StopMeasureMsgEvent(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
