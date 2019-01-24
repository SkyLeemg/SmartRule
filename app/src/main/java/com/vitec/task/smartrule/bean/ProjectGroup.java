package com.vitec.task.smartrule.bean;

public class ProjectGroup {

    private int id;
    private int Server_id;
    private int user_id;
    private int project_id;
    private int project_server_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServer_id() {
        return Server_id;
    }

    public void setServer_id(int server_id) {
        Server_id = server_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
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
}
