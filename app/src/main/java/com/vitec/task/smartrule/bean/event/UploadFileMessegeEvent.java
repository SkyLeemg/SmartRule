package com.vitec.task.smartrule.bean.event;

/**
 * 上传文件接口 通过 eventbus传递的信息
 */
public class UploadFileMessegeEvent {

    private boolean isSuccess;//是否上传成功
    private String newUrl;//服务器返回的url地址
    private String oldLocalPath;//本地地址
    private String msg;//提示信息
    private String upload_flag;//上传标志，用于区分是 上传头像还是 上传图纸 还是其他等

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getNewUrl() {
        return newUrl;
    }

    public void setNewUrl(String newUrl) {
        this.newUrl = newUrl;
    }

    public String getOldLocalPath() {
        return oldLocalPath;
    }

    public void setOldLocalPath(String oldLocalPath) {
        this.oldLocalPath = oldLocalPath;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUpload_flag() {
        return upload_flag;
    }

    public void setUpload_flag(String upload_flag) {
        this.upload_flag = upload_flag;
    }

    @Override
    public String toString() {
        return "UploadFileMessegeEvent{" +
                "isSuccess=" + isSuccess +
                ", newUrl='" + newUrl + '\'' +
                ", oldLocalPath='" + oldLocalPath + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
