package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 一个需要导出的表格对象
 * 按照表格模板来写的对象
 * 包括工程名、测量信息，每个管控要点的测量数据内容和计算结果
 */
public class MeasureTable implements Serializable{

//    RulerEnginner的数据
    private String engineerName;
    //    RulerCheck的部分数据
    private String projectName;
    private String checkFloor;
    private String checkPerson;
    private String checkDate;
    //    表头对应的一行的数据内容
    private List<MeasureTableRow> rowList;

    public String getEngineerName() {
        return engineerName;
    }

    public void setEngineerName(String engineerName) {
        this.engineerName = engineerName;
    }

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

    public String getCheckPerson() {
        return checkPerson;
    }

    public void setCheckPerson(String checkPerson) {
        this.checkPerson = checkPerson;
    }

    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }

    public List<MeasureTableRow> getRowList() {
        return rowList;
    }

    public void setRowList(List<MeasureTableRow> rowList) {
        this.rowList = rowList;
    }

    @Override
    public String toString() {
        return "MeasureTable{" +
                "engineerName='" + engineerName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", checkFloor='" + checkFloor + '\'' +
                ", checkPerson='" + checkPerson + '\'' +
                ", checkDate='" + checkDate + '\'' +
                ", rowList=" + rowList +
                '}';
    }
}
