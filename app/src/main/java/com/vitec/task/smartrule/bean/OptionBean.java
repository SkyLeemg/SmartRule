package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 管控要点，被工程项目包含
 */
public class OptionBean implements Serializable{


    private int resourceID;//底部导航的图标资源
    private String measureItemName;//底部导航的名称，例如垂直度，平整度，为管控要点的简称
    private String measureItem;//管控要点，例如立面垂直度
    private String passStandard;//合格标准
    private StringBuffer checkWay;//检查方法
    private int enginId;//关联的工程id

    private int actualMeasureNum;//实测数
    private int qualifiedNum;//合格数
    private int qualifiedRate;//合格率

    public OptionBean() {

    }

    public OptionBean(int resourceID, String measureItemName, String measureItem, String passStandard, StringBuffer checkWay) {
        this.resourceID = resourceID;
        this.measureItemName = measureItemName;
        this.measureItem = measureItem;
        this.passStandard = passStandard;
        this.checkWay = checkWay;
    }

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public String getMeasureItemName() {
        return measureItemName;
    }

    public void setMeasureItemName(String measureItemName) {
        this.measureItemName = measureItemName;
    }

    public String getMeasureItem() {
        return measureItem;
    }

    public void setMeasureItem(String measureItem) {
        this.measureItem = measureItem;
    }

    public String getPassStandard() {
        return passStandard;
    }

    public void setPassStandard(String passStandard) {
        this.passStandard = passStandard;
    }

    public StringBuffer getCheckWay() {
        return checkWay;
    }

    public void setCheckWay(StringBuffer checkWay) {
        this.checkWay = checkWay;
    }

    public int getActualMeasureNum() {
        return actualMeasureNum;
    }

    public void setActualMeasureNum(int actualMeasureNum) {
        this.actualMeasureNum = actualMeasureNum;
    }

    public int getQualifiedNum() {
        return qualifiedNum;
    }

    public void setQualifiedNum(int qualifiedNum) {
        this.qualifiedNum = qualifiedNum;
    }

    public int getQualifiedRate() {
        return qualifiedRate;
    }

    public void setQualifiedRate(int qualifiedRate) {
        this.qualifiedRate = qualifiedRate;
    }

    public int getEnginId() {
        return enginId;
    }

    public void setEnginId(int enginId) {
        this.enginId = enginId;
    }

    @Override
    public String toString() {
        return "OptionBean{" +
                "resourceID=" + resourceID +
                ", measureItemName='" + measureItemName + '\'' +
                ", measureItem='" + measureItem + '\'' +
                ", passStandard='" + passStandard + '\'' +
                ", checkWay=" + checkWay +
                '}';
    }
}
