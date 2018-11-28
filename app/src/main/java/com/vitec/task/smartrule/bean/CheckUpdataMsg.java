package com.vitec.task.smartrule.bean;

import java.io.Serializable;

/**
 * 与服务器返回的检查更新的数据对应
 */
public class CheckUpdataMsg implements Serializable{
    /**
     * {
     * "status":"success",
     * "code":200,
     * "data":{
     * "version_number":"1.0.1",
     * "version_code":2,
     * "apk_url":"http:\/\/iot.vkforest.com\/Version\/20181126\/413e6f82aa228ae651ff9b01d0ef7ca8.zip",
     * "app_name":"kaochi",
     * "filename":"updata",
     * "file_size":"23",
     * "update_log":"1.更新了‘\r\n2.更新了啊啊啊\r\n3.更新了呵呵呵呵",
     * "create_time":"2018-11-26 16:15:25"},
     * "msg":"查询成功"}
     */
    private int verCode;
    private String verName;
    private String downloadUrl;
    private String appName;
    private String fileName;
    private String fileSize;
    private String updateLog;

    public int getVerCode() {
        return verCode;
    }

    public void setVerCode(int verCode) {
        this.verCode = verCode;
    }

    public String getVerName() {
        return verName;
    }

    public void setVerName(String verName) {
        this.verName = verName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    @Override
    public String toString() {
        return "CheckUpdataMsg{" +
                "verCode=" + verCode +
                ", verName='" + verName + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", appName='" + appName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", updateLog='" + updateLog + '\'' +
                '}';
    }
}
