package com.vitec.task.smartrule.bean.event;

import com.vitec.task.smartrule.bean.OptionMeasure;

public class HeightFloorMsgEvent {

    private int flag;
    private OptionMeasure measure;

    public HeightFloorMsgEvent(int flag, OptionMeasure measure) {
        this.flag = flag;
        this.measure = measure;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public OptionMeasure getMeasure() {
        return measure;
    }

    public void setMeasure(OptionMeasure measure) {
        this.measure = measure;
    }

    @Override
    public String toString() {
        return "HeightFloorMsgEvent{" +
                "flag=" + flag +
                ", measure=" + measure +
                '}';
    }
}
