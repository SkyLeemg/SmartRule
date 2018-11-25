package com.vitec.task.smartrule.bean;

import java.util.List;

/**
 * ChooseMeasureProjectAdapter里面一个Item的信息
 */
public class ChooseMeasureMsg {

    private String projectName;
    private String checkFloor;
    private User user;
//    private String userName;
    private String createDate;
    private List<RulerEngineer> engineerList;
    private RulerCheck rulerCheck;//用于存储进入测量后的表头内容

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCheckFloor() {
        return checkFloor;
    }

    public void setCheckFloor(String checkFloor) {
        this.checkFloor = checkFloor;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public List<RulerEngineer> getEngineerList() {
        return engineerList;
    }

    public void setEngineerList(List<RulerEngineer> engineerList) {
        this.engineerList = engineerList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RulerCheck getRulerCheck() {
        return rulerCheck;
    }

    public void setRulerCheck(RulerCheck rulerCheck) {
        this.rulerCheck = rulerCheck;
    }

    @Override
    public String toString() {
        return "ChooseMeasureMsg{" +
                "projectName='" + projectName + '\'' +
                ", checkFloor='" + checkFloor + '\'' +
                ", user=" + user +
                ", createDate='" + createDate + '\'' +
                ", engineerList=" + engineerList +
                '}';
    }
}
