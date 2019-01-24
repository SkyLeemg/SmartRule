package com.vitec.task.smartrule.bean.event;

/**
 * 导出excel表的响应事件的传递对象
 */
public class ExportMsgEvent {

    private boolean isSuccess;//是否导出成功

    private String msg;//信息
    private String filePath;
    private Object object;

    public ExportMsgEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "ExportMsgEvent{" +
                "isSuccess=" + isSuccess +
                ", msg='" + msg + '\'' +
                '}';
    }
}
