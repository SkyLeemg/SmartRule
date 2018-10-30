package com.vitec.task.smartrule.wxapi.bean;

public class ResultInfo {
    /**
     成功返回：
     {
     "access_token":"ACCESS_TOKEN",  //access_token接口调用凭证
     "expires_in":7200,expires_in access_token接口调用凭证超时时间，单位（秒）
     "refresh_token":"REFRESH_TOKEN",refresh_token 用户刷新access_token
     "openid":"OPENID",openid 授权用户唯一标识
     "scope":"SCOPE",scope 用户授权的作用域，使用逗号（,）分隔
     "unionid":"o6_bmasdasdsad6_2sgVt7hMZOPfL"unionid 当且仅当该移动应用已获得该用户的userinfo授权时，才会出现该字段
     }
     错误返回样例：
     {"errcode":40029,"errmsg":"invalid code"}
     */

    private String accessToken ;
    private String openId ;
    private String expireIn;
    private String refreshToken;
    private String scope;
    private String unionId;

    public ResultInfo() {

    }

    public ResultInfo(String accessToken, String openId) {
        this.accessToken = accessToken;
        this.openId = openId;
    }

    public ResultInfo(String accessToken, String openId, String expireIn, String refreshToken, String scope, String unionId) {
        this.accessToken = accessToken;
        this.openId = openId;
        this.expireIn = expireIn;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.unionId = unionId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(String expireIn) {
        this.expireIn = expireIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "accessToken='" + accessToken + '\'' +
                ", openId='" + openId + '\'' +
                ", expireIn='" + expireIn + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", scope='" + scope + '\'' +
                ", unionId='" + unionId + '\'' +
                '}';
    }
}
