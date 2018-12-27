package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.Objects;

/**
 * 相当于数据库中的iot_ruler_check_options_data表，主要存放蓝牙发过来的数据
 */
public class RulerCheckOptionsData implements Serializable{

    private static final long serialVersionUID = -7060210544600464482L;
    private int id;
    private int serverId;
    private RulerCheckOptions rulerCheckOptions;
    private String data;
    private boolean isQualified;//是否合格
    private int createTime;
    private int updateFlag;
    private int upload_flag;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public RulerCheckOptions getRulerCheckOptions() {
        return rulerCheckOptions;
    }

    public void setRulerCheckOptions(RulerCheckOptions rulerCheckOptions) {
        this.rulerCheckOptions = rulerCheckOptions;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getUpdateFlag() {
        return updateFlag;
    }

    public void setUpdateFlag(int updateFlag) {
        this.updateFlag = updateFlag;
    }

    public int getUpload_flag() {
        return upload_flag;
    }

    public void setUpload_flag(int upload_flag) {


        this.upload_flag = upload_flag;
    }


    public boolean isQualified() {
        return isQualified;
    }

    public void setQualified(boolean qualified) {
        isQualified = qualified;
    }

    @Override
    public String toString() {
        return "RulerCheckOptionsData{" +
                "id=" + id +
                ", serverId=" + serverId +
                ", data='" + data + '\'' +
                ", createTime=" + createTime +
                ", updateFlag=" + updateFlag +
                ", upload_flag=" + upload_flag +
                ", rulerCheckOptions=" + rulerCheckOptions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RulerCheckOptionsData data1 = (RulerCheckOptionsData) o;
        return id == data1.id &&
                serverId == data1.serverId &&
                createTime == data1.createTime &&
                updateFlag == data1.updateFlag &&
                upload_flag == data1.upload_flag &&
                Objects.equals(rulerCheckOptions, data1.rulerCheckOptions) &&
                Objects.equals(data, data1.data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, serverId, rulerCheckOptions, data, createTime, updateFlag, upload_flag);
    }
}
