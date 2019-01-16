package com.vitec.task.smartrule.bean;

public class DownloadedImg {
    /**
     * 已下载的图纸对象
     */
    private int id;
    private String url;//网络地址
    private String path;//本地地址

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
