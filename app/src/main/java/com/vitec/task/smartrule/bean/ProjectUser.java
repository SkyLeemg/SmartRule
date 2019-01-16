package com.vitec.task.smartrule.bean;

import java.util.Objects;

public class ProjectUser {

    private int id;
    private int server_id;
    private int user_id;
    private int cId;
    private String mobile;
    private String userName;
    private String position;
    private int projectId;
    private int projectServerId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getProjectServerId() {
        return projectServerId;
    }

    public void setProjectServerId(int projectServerId) {
        this.projectServerId = projectServerId;
    }

    public int getcId() {
        return cId;
    }

    public void setcId(int cId) {
        this.cId = cId;
    }

    public int getServer_id() {
        return server_id;
    }

    public void setServer_id(int server_id) {
        this.server_id = server_id;
    }

    @Override
    public String toString() {
        return "ProjectUser{" +
                "id=" + id +
                ", server_id=" + server_id +
                ", user_id=" + user_id +
                ", cId=" + cId +
                ", mobile='" + mobile + '\'' +
                ", userName='" + userName + '\'' +
                ", position='" + position + '\'' +
                ", projectId=" + projectId +
                ", projectServerId=" + projectServerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectUser user = (ProjectUser) o;
        return user_id == user.user_id &&
                Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user_id, userName);
    }
}
