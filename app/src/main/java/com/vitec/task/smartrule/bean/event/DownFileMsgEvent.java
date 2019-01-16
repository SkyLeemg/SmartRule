package com.vitec.task.smartrule.bean.event;

/**
 * 下载图片回调
 */
public class DownFileMsgEvent {

    private boolean isSuccess;
    private String path;
    private int type;//1-头像，2-图纸
    private Object object;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
