package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.Objects;

/**
 * 对应数据库重点iot_ruler_check_options，存储正在测量表格中的一行，也就是一个正在测量的管控要点的数据
 */
public class RulerCheckOptions implements Serializable{
    private int id;
    private int serverId;
    private OptionMeasure floorHeight;
    private int measuredNum;
    private int qualifiedNum;
    private float qualifiedRate;
    private int createTime;
    private int updateTime;
    private int upload_flag;
    private int img_upload_flag;//0代表当前图片未上传，1代表已经上传
    private String imgPath;//图纸的本地地址
    private int imgUpdateTime;
    private int imgNumber;
    private String serverImgUrl;//图纸的网络链接
    private RulerCheck rulerCheck;
    private RulerOptions rulerOptions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RulerCheck getRulerCheck() {
        return rulerCheck;
    }

    public void setRulerCheck(RulerCheck rulerCheck) {
        this.rulerCheck = rulerCheck;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public RulerOptions getRulerOptions() {
        return rulerOptions;
    }

    public void setRulerOptions(RulerOptions rulerOptions) {
        this.rulerOptions = rulerOptions;
    }

    public OptionMeasure getFloorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(OptionMeasure floorHeight) {
        this.floorHeight = floorHeight;
    }

    public int getMeasuredNum() {
        return measuredNum;
    }

    public void setMeasuredNum(int measuredNum) {
        this.measuredNum = measuredNum;
    }

    public int getQualifiedNum() {
        return qualifiedNum;
    }

    public void setQualifiedNum(int qualifiedNum) {
        this.qualifiedNum = qualifiedNum;
    }

    public float getQualifiedRate() {
        return qualifiedRate;
    }

    public void setQualifiedRate(float qualifiedRate) {
        this.qualifiedRate = qualifiedRate;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getUpload_flag() {
        return upload_flag;
    }

    public void setUpload_flag(int upload_flag) {

        this.upload_flag = upload_flag;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public int getImgUpdateTime() {
        return imgUpdateTime;
    }

    public void setImgUpdateTime(int imgUpdateTime) {
        this.imgUpdateTime = imgUpdateTime;
    }

    public String getServerImgUrl() {
        return serverImgUrl;
    }

    public void setServerImgUrl(String serverImgUrl) {
        this.serverImgUrl = serverImgUrl;
    }

    public int getImg_upload_flag() {
        return img_upload_flag;
    }

    public void setImg_upload_flag(int img_upload_flag) {
        this.img_upload_flag = img_upload_flag;
    }

    public int getImgNumber() {
        return imgNumber;
    }

    public void setImgNumber(int imgNumber) {
        this.imgNumber = imgNumber;
    }

    @Override
    public String toString() {
        return "RulerCheckOptions{" +
                "id=" + id +
                ", serverId=" + serverId +
                ", floorHeight='" + floorHeight + '\'' +
                ", measuredNum=" + measuredNum +
                ", qualifiedNum=" + qualifiedNum +
                ", qualifiedRate=" + qualifiedRate +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", upload_flag=" + upload_flag +
                ", imgNumber=" + imgNumber +
                ", img_upload_flag=" + img_upload_flag +
                ", imgPath='" + imgPath + '\'' +
                ", imgUpdateTime=" + imgUpdateTime +
                ", serverImgUrl='" + serverImgUrl + '\'' +
                ", rulerCheck=" + rulerCheck +
                ", rulerOptions=" + rulerOptions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RulerCheckOptions that = (RulerCheckOptions) o;
        return id == that.id &&
                serverId == that.serverId &&
                measuredNum == that.measuredNum &&
                qualifiedNum == that.qualifiedNum &&
                Float.compare(that.qualifiedRate, qualifiedRate) == 0 &&
                createTime == that.createTime &&
                updateTime == that.updateTime &&
                upload_flag == that.upload_flag &&
                Objects.equals(rulerCheck, that.rulerCheck) &&
                Objects.equals(rulerOptions, that.rulerOptions) &&
                Objects.equals(floorHeight, that.floorHeight);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, rulerCheck, serverId, rulerOptions, floorHeight, measuredNum, qualifiedNum, qualifiedRate, createTime, updateTime, upload_flag);
    }
}
