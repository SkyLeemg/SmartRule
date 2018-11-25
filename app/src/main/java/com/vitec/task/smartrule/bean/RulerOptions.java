package com.vitec.task.smartrule.bean;

import java.io.Serializable;

/**
 * 对应数据库中的iot_ruler_options中的内容
 */
public class RulerOptions implements Serializable{

    private int id;
    private int serverID;
    private String optionsName;
    private String Standard;
    private String methods;
    private RulerEngineer engineer;
    private int createTime;
    private int updateTime;
    private String measure;

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

    public String getOptionsName() {
        return optionsName;
    }

    public void setOptionsName(String optionsName) {
        this.optionsName = optionsName;
    }

    public String getStandard() {
        return Standard;
    }

    public void setStandard(String standard) {
        Standard = standard;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public RulerEngineer getEngineer() {
        return engineer;
    }

    public void setEngineer(RulerEngineer engineer) {
        this.engineer = engineer;
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

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    @Override
    public String toString() {
        return "RulerOptions{" +
                "id=" + id +
                ", serverID=" + serverID +
                ", optionsName='" + optionsName + '\'' +
                ", Standard='" + Standard + '\'' +
                ", methods='" + methods + '\'' +
                ", engineer=" + engineer +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", measure='" + measure + '\'' +
                '}';
    }
}
