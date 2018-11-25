package com.vitec.task.smartrule.bean;


import java.io.Serializable;

/**
 * 对应iot_ruler_engineer表中的内容项
 */
public class RulerEngineer implements Serializable{
    private int id;
    private int serverID;
    private String engineerName;
    private String engineerDescription;
    private int createTime;
    private int updateTime;

    public RulerEngineer() {

    }

    public RulerEngineer(String engineerName) {
        this.engineerName = engineerName;
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

    public String getEngineerName() {
        return engineerName;
    }

    public void setEngineerName(String engineerName) {
        this.engineerName = engineerName;
    }

    public String getEngineerDescription() {
        return engineerDescription;
    }

    public void setEngineerDescription(String engineerDescription) {
        this.engineerDescription = engineerDescription;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "RulerEngineer{" +
                "id=" + id +
                ", serverID=" + serverID +
                ", engineerName='" + engineerName + '\'' +
                ", engineerDescription='" + engineerDescription + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
