package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable{

    private int id;
    private int userID;//服务器的userid
    private int childId;//绑定的用户的ID
    private int status;//用户状态，1-登录使用中，0-本机未登录
    private String userName;//用户的真是姓名
    private String position;
//    private String loginName;//用户的登陆名
    private String wxUnionId;//微信的unionID
    private String password;//登陆密码
    private String repeatPassword;//注册用到的重复密码
    private String mobile;//手机号码
    private String mobileCode;//手机验证码
    private String wxData;//微信登录的请求数据
    private String token;
    private String imgUrl;//网络头像地址
    private String localImgUrl;//本地头像地址
    private String user_type;////账号类型，1-手机号码，2-微信号，3两者都有

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLocalImgUrl() {
        return localImgUrl;
    }

    public void setLocalImgUrl(String localImgUrl) {
        this.localImgUrl = localImgUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userID=" + userID +
                ", childId=" + childId +
                ", status=" + status +
                ", userName='" + userName + '\'' +
                ", position='" + position + '\'' +
                ", wxUnionId='" + wxUnionId + '\'' +
                ", password='" + password + '\'' +
                ", repeatPassword='" + repeatPassword + '\'' +
                ", mobile='" + mobile + '\'' +
                ", mobileCode='" + mobileCode + '\'' +
                ", wxData='" + wxData + '\'' +
                ", token='" + token + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", localImgUrl='" + localImgUrl + '\'' +
                ", user_type='" + user_type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                userID == user.userID &&
                status == user.status &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(wxUnionId, user.wxUnionId) &&
                Objects.equals(password, user.password) &&
                Objects.equals(repeatPassword, user.repeatPassword) &&
                Objects.equals(mobile, user.mobile) &&
                Objects.equals(mobileCode, user.mobileCode) &&
                Objects.equals(wxData, user.wxData) &&
                Objects.equals(token, user.token) &&
                Objects.equals(imgUrl, user.imgUrl) &&
                Objects.equals(localImgUrl, user.localImgUrl);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, userID, status, userName, wxUnionId, password, repeatPassword, mobile, mobileCode, wxData, token, imgUrl, localImgUrl);
    }
}
