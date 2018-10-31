package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 一个工程对象，包括一张表格的表头信息
 */
public class EngineerBean implements Serializable {
    private static final long serialVersionUID = -7060210544600464481L;
    private int projectID;//项目编号
    private String projectName;//项目名，例如P31,由用户手动填写
    private String projectEngineer;//项目工程,例如混凝土工程，固定选项
    private String checkPositon;//检查位置，例如A栋2层，手动填写
    private String checkPerson;//检查人，例如张三，从账号中读取
    private String checkTime;//检查时间，自动获取系统时间
//    private List<String> engineers;//工程名的集合，用于选择工程的spinner控件的资源
    private List<OptionBean> measureBeanList;//一个工程有多个要点

    public EngineerBean() {

    }

    public EngineerBean(int projectID, String projectName, String projectEngineer, String checkPositon,
                        String checkPerson, String checkTime, List<OptionBean> measureBeanList) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.projectEngineer = projectEngineer;
        this.checkPositon = checkPositon;
        this.checkPerson = checkPerson;
        this.checkTime = checkTime;
        this.measureBeanList = measureBeanList;
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectEngineer() {
        return projectEngineer;
    }

    public void setProjectEngineer(String projectEngineer) {
        this.projectEngineer = projectEngineer;
    }

    public String getCheckPositon() {
        return checkPositon;
    }

    public void setCheckPositon(String checkPositon) {
        this.checkPositon = checkPositon;
    }

    public String getCheckPerson() {
        return checkPerson;
    }

    public void setCheckPerson(String checkPerson) {
        this.checkPerson = checkPerson;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public List<OptionBean> getMeasureBeanList() {
        return measureBeanList;
    }

    public void setMeasureBeanList(List<OptionBean> measureBeanList) {
        this.measureBeanList = measureBeanList;
    }

    @Override
    public String toString() {
        return "EngineerBean{" +
                "projectID=" + projectID +
                ", projectName='" + projectName + '\'' +
                ", projectEngineer='" + projectEngineer + '\'' +
                ", checkPositon='" + checkPositon + '\'' +
                ", checkPerson='" + checkPerson + '\'' +
                ", checkTime='" + checkTime + '\'' +
                ", measureBeanList=" + measureBeanList +
                '}';
    }
}
