package com.vitec.task.smartrule.bean;

import java.io.Serializable;

public class UserWx implements Serializable{

    private int id;
    private int user_id;
    private String nickName;
    private String data;
    private String headImgUrl;

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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    @Override
    public String toString() {
        return "UserWx{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", nickName='" + nickName + '\'' +
                ", data='" + data + '\'' +
                ", headImgUrl='" + headImgUrl + '\'' +
                '}';
    }
}
