package com.vitec.task.smartrule.bean;

public class MeasureBean {

    private int projectID;//项目编号
    private String projectName;//项目名，例如P31,由用户手动填写
    private String projectType;//项目类型,例如混凝土工程，固定选项
    private String checkPositon;//检查位置，例如A栋2层，手动填写
    private String checkPerson;//检查人，例如张三，从账号中读取
    private String checkTime;//检查时间，自动获取系统时间
    private int resourceID;//底部导航的图标资源
    private String measureItemName;//底部导航的名称，例如垂直度，平整度，为管控要点的简称
    private String measureItem;//管控要点，例如立面垂直度

    public MeasureBean(String projectName, String projectType, String checkPositon, String checkPerson,
                       String checkTime) {
        this.projectName = projectName;
        this.projectType = projectType;
        this.checkPositon = checkPositon;
        this.checkPerson = checkPerson;
        this.checkTime = checkTime;
    }

    /**
     * 适合于新建测量的fragment使用
     * @param projectID
     * @param projectName
     * @param projectType
     * @param checkPositon
     * @param checkPerson
     * @param checkTime
     * @param measureItemName
     */
    public MeasureBean(int projectID, String projectName, String projectType, String checkPositon,
                       String checkPerson, String checkTime, String measureItemName) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.projectType = projectType;
        this.checkPositon = checkPositon;
        this.checkPerson = checkPerson;
        this.checkTime = checkTime;
        this.measureItemName = measureItemName;
    }

    public MeasureBean() {

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

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
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

    public String getMeasureItemName() {
        return measureItemName;
    }

    public void setMeasureItemName(String measureItemName) {
        this.measureItemName = measureItemName;
    }

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public String getMeasureItem() {
        return measureItem;
    }

    public void setMeasureItem(String measureItem) {
        this.measureItem = measureItem;
    }
}
