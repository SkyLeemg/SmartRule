package com.vitec.task.smartrule.bean;

import java.io.Serializable;

public class User implements Serializable{

    private int id;
    private int userID;//服务器的userid
    private int status;//用户状态，1-登录使用中，0-本机未登录
    private String wid;
    private String userName;//用户的真是姓名
    private String loginName;//用户的登陆名
    private String wxUnionId;//微信的unionID
    private String password;//登陆密码
    private String repeatPassword;//注册用到的重复密码
    private String mobile;//手机号码
    private String mobileCode;//手机验证码
    private String wxData;//微信登录的请求数据
    private String userJob;
    private String token;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getWxUnionId() {
        return wxUnionId;
    }

    public void setWxUnionId(String wxUnionId) {
        this.wxUnionId = wxUnionId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobileCode() {
        return mobileCode;
    }

    public void setMobileCode(String mobileCode) {
        this.mobileCode = mobileCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWxData() {
        return wxData;
    }

    public void setWxData(String wxData) {

        this.wxData = wxData;
    }

    public String getUserJob() {
        return userJob;
    }

    public void setUserJob(String userJob) {
        this.userJob = userJob;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userID=" + userID +
                ", status=" + status +
                ", wid='" + wid + '\'' +
                ", userName='" + userName + '\'' +
                ", loginName='" + loginName + '\'' +
                ", wxUnionId='" + wxUnionId + '\'' +
                ", password='" + password + '\'' +
                ", repeatPassword='" + repeatPassword + '\'' +
                ", mobile='" + mobile + '\'' +
                ", mobileCode='" + mobileCode + '\'' +
                ", wxData='" + wxData + '\'' +
                ", userJob='" + userJob + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
