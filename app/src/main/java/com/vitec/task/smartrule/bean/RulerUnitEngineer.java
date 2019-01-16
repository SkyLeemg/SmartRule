package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.Objects;

public class RulerUnitEngineer implements Serializable {

    private int id;
    private int server_id;
    private int project_id;
    private int project_server_id;
    private String location;
    private int createTime;
    private int updateTime;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public int getProject_server_id() {
        return project_server_id;
    }

    public void setProject_server_id(int project_server_id) {
        this.project_server_id = project_server_id;
    }

    @Override
    public String toString() {
        return "RulerUnitEngineer{" +
                "id=" + id +
                ", server_id=" + server_id +
                ", project_id=" + project_id +
                ", project_server_id=" + project_server_id +
                ", location='" + location + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RulerUnitEngineer that = (RulerUnitEngineer) o;
        return id == that.id &&
                server_id == that.server_id &&
                project_id == that.project_id &&
                project_server_id == that.project_server_id &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, server_id, project_id, project_server_id, location);
    }
}
