package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.Objects;

/**
 * 对应数据库中的iot_ruler_check表
 */
public class RulerCheck implements Serializable{

    private static final long serialVersionUID = -7060210544600464483L;
    private int id;
    private String checkFloor;
    private User user;
    private int createTime;
    private int updateTime;
    private String createDate;
    private int serverId;
    private int upload_flag;
    private int status;//代表是否结束测量的标志，0-还可以测量，1-测量完成但标志未更新到服务器，2-测量完成且标志更新到服务器
    private RulerCheckProject project;
    private RulerUnitEngineer unitEngineer;
    private RulerEngineer engineer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public RulerCheckProject getProject() {
        return project;
    }

    public void setProject(RulerCheckProject project) {
        this.project = project;
    }

    public RulerUnitEngineer getUnitEngineer() {
        return unitEngineer;
    }

    public void setUnitEngineer(RulerUnitEngineer unitEngineer) {
        this.unitEngineer = unitEngineer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RulerCheck that = (RulerCheck) o;
        return id == that.id &&
                createTime == that.createTime &&
                updateTime == that.updateTime &&
                serverId == that.serverId &&
                upload_flag == that.upload_flag &&
                status == that.status &&
                Objects.equals(checkFloor, that.checkFloor) &&
                Objects.equals(engineer, that.engineer) &&
                Objects.equals(user, that.user) &&
                Objects.equals(createDate, that.createDate);
    }


    @Override
    public String toString() {
        return "RulerCheck{" +
                "id=" + id +
                ", checkFloor='" + checkFloor + '\'' +
//                ", user=" + user +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createDate='" + createDate + '\'' +
                ", serverId=" + serverId +
                ", upload_flag=" + upload_flag +
                ", status=" + status +
                "\n"+
//                ", project=" + project +
//                "\n"+
//                ", unitEngineer=" + unitEngineer +
//                "\n"+
//                ", engineer=" + engineer +
                '}';
    }

    @Override
    public int hashCode() {

        return Objects.hash(id,  checkFloor, engineer, user, createTime, updateTime, createDate, serverId, upload_flag, status);
    }
}
