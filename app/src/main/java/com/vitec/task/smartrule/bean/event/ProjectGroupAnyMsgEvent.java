package com.vitec.task.smartrule.bean.event;

public class ProjectGroupAnyMsgEvent {

    /**请求类型区分,
     * 1-项目更新,
     * 2-添加成员信息,
     * 3-添加单位工程
     * 4-删除单位工程
     * 5-删除成员
     * 6-删除测量组
     */
    private int requst_flag;//
    private boolean isSuccess;
    private String msg;
    private Object object;

    public int getRequst_flag() {
        return requst_flag;
    }

    public void setRequst_flag(int requst_flag) {
        this.requst_flag = requst_flag;
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

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "ProjectGroupAnyMsgEvent{" +
                "requst_flag=" + requst_flag +
                ", isSuccess=" + isSuccess +
                ", msg='" + msg + '\'' +
                ", object=" + object +
                '}';
    }
}
