package com.vitec.task.smartrule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 导出的表格格式中的一行，
 * 包括一个管控要点的模板信息，该要点所测量出来的所以数据内容和计算结果
 */
public class MeasureTableRow implements Serializable {

    private int id;
    private String optionName;
    private String standard;
    private String checkMethod;
    private List<MeasureData> datalist;
    private int realMeasureNum;
    private int qualifiedNum;
    private String qualifiedRate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getCheckMethod() {
        return checkMethod;
    }

    public void setCheckMethod(String checkMethod) {
        this.checkMethod = checkMethod;
    }

    public List<MeasureData> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<MeasureData> datalist) {
        this.datalist = datalist;
    }

    public int getRealMeasureNum() {
        return realMeasureNum;
    }

    public void setRealMeasureNum(int realMeasureNum) {
        this.realMeasureNum = realMeasureNum;
    }

    public int getQualifiedNum() {
        return qualifiedNum;
    }

    public void setQualifiedNum(int qualifiedNum) {
        this.qualifiedNum = qualifiedNum;
    }

    public String getQualifiedRate() {
        return qualifiedRate;
    }

    public void setQualifiedRate(String qualifiedRate) {
        this.qualifiedRate = qualifiedRate;
    }

    @Override
    public String toString() {
        return "MeasureTableRow{" +
                "id=" + id +
                ", optionName='" + optionName + '\'' +
                ", standard='" + standard + '\'' +
                ", checkMethod='" + checkMethod + '\'' +
                ", datalist=" + datalist +
                ", realMeasureNum=" + realMeasureNum +
                ", qualifiedNum=" + qualifiedNum +
                ", qualifiedRate='" + qualifiedRate + '\'' +
                '}';
    }
}
