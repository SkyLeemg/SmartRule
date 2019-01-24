package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.List;

public class RulerCheckProject implements Serializable{

    private int id;
    private int server_id;
    private String projectName;
    private String qrCode;
    private int createTime;
    private int updateTime;
    private User user;
    private List<RulerUnitEngineer> unitList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServer_id() {
        return server_id;
    }

    public void setServer_id(int server_id) {
        this.server_id = server_id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public List<RulerUnitEngineer> getUnitList() {
        return unitList;
    }

    public void setUnitList(List<RulerUnitEngineer> unitList) {
        this.unitList = unitList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    @Override
    public String toString() {
        return "RulerCheckProject{" +
                "id=" + id +
                ", server_id=" + server_id +
                ", projectName='" + projectName + '\'' +
                ", qrCode='" + qrCode + '\'' +
//                ", createTime=" + createTime +
//                ", updateTime=" + updateTime +
//                ", user=" + user +
                ", unitList=" + unitList +
                '}';
    }
}
