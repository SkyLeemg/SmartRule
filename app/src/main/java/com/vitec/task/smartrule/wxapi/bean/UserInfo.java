package com.vitec.task.smartrule.wxapi.bean;

public class UserInfo {

   private String nickName ;
   private String sex;
   private String city;
   private String province;
   private String country;
   private String headImgUrl;
   private String openid;
   private String unionid;

    public UserInfo(String nickName, String sex, String city, String province, String country, String headImgUrl, String openid, String unionid) {
        this.nickName = nickName;
        this.sex = sex;
        this.city = city;
        this.province = province;
        this.country = country;
        this.headImgUrl = headImgUrl;
        this.openid = openid;
        this.unionid = unionid;
    }

    public UserInfo() {

    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }


    @Override
    public String toString() {
        return "UserInfo{" +
                "nickName='" + nickName + '\'' +
                ", sex='" + sex + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", country='" + country + '\'' +
                ", headImgUrl='" + headImgUrl + '\'' +
                ", openid='" + openid + '\'' +
                ", unionid='" + unionid + '\'' +
                '}';
    }
}


