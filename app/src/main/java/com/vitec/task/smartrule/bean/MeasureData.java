package com.vitec.task.smartrule.bean;

public class MeasureData {

    private int id;
    private int serverID;
    private int checkOptionsId;
    private String data;
    private int createTime;
    private int updateFlag;

    public MeasureData() {
    }

    public MeasureData(int id, int serverID, int checkOptionsId, String data, int createTime, int updateFlag) {
        this.id = id;
        this.serverID = serverID;
        this.checkOptionsId = checkOptionsId;
        this.data = data;
        this.createTime = createTime;
        this.updateFlag = updateFlag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public int getCheckOptionsId() {
        return checkOptionsId;
    }

    public void setCheckOptionsId(int checkOptionsId) {
        this.checkOptionsId = checkOptionsId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getUpdateFlag() {
        return updateFlag;
    }

    public void setUpdateFlag(int updateFlag) {
        this.updateFlag = updateFlag;
    }

    @Override
    public String toString() {
        return "MeasureData{" +
                "id=" + id +
                ", serverID=" + serverID +
                ", checkOptionsId=" + checkOptionsId +
                ", data='" + data + '\'' +
                ", createTime=" + createTime +
                ", updateFlag=" + updateFlag +
                '}';
    }
}
