package com.vitec.task.smartrule.bean;

import java.io.Serializable;

/**
 * 对应数据库中的iot_ruler_check表
 */
public class RulerCheck implements Serializable{

    private static final long serialVersionUID = -7060210544600464483L;
    private int id;
    private String projectName;
    private String checkFloor;
    private RulerEngineer engineer;
    private User user;
    private int createTime;
    private int updateTime;
    private String createDate;
    private int serverId;
    private int upload_flag;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCheckFloor() {
        return checkFloor;
    }

    public void setCheckFloor(String checkFloor) {
        this.checkFloor = checkFloor;
    }

    public RulerEngineer getEngineer() {
        return engineer;
    }

    public void setEngineer(RulerEngineer engineer) {
        this.engineer = engineer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {

        this.serverId = serverId;
    }

    public int getUpload_flag() {
        return upload_flag;
    }

    public void setUpload_flag(int upload_flag) {
        this.upload_flag = upload_flag;
    }

    @Override
    public String toString() {
        return "RulerCheck{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", checkFloor='" + checkFloor + '\'' +
                ", engineer=" + engineer +
                ", user=" + user +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createDate='" + createDate + '\'' +
                ", serverId=" + serverId +
                ", upload_flag=" + upload_flag +
                '}';
    }
}
