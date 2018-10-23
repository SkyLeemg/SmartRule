package com.vitec.task.smartrule.bean;

public class ResultMessage {

    private boolean isDone;
    public ResultMessage(boolean isDone) {
        this.isDone = isDone;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
