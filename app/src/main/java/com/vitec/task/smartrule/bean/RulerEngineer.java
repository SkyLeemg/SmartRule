package com.vitec.task.smartrule.bean;


import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 对应iot_ruler_engineer表中的内容项
 */
public class RulerEngineer implements Serializable{
    private int id;
    private int serverID;
    private String engineerName;
    private String engineerDescription;
    private int createTime;
    private int updateTime;
    private String chooseOptions;
    private List<RulerOptions> optionsList;

    public RulerEngineer() {

    }

    public RulerEngineer(String engineerName) {
        this.engineerName = engineerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public String getEngineerName() {
        return engineerName;
    }

    public void setEngineerName(String engineerName) {
        this.engineerName = engineerName;
    }

    public String getEngineerDescription() {
        return engineerDescription;
    }

    public void setEngineerDescription(String engineerDescription) {
        this.engineerDescription = engineerDescription;
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

    public String getChooseOptions() {
        return chooseOptions;
    }

    public void setChooseOptions(String chooseOptions) {
        this.chooseOptions = chooseOptions;
    }


    public List<RulerOptions> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(List<RulerOptions> optionsList) {
        this.optionsList = optionsList;
    }

    @Override
    public String toString() {
        return "RulerEngineer{" +
                "id=" + id +
                ", serverID=" + serverID +
                ", engineerName='" + engineerName + '\'' +
                ", engineerDescription='" + engineerDescription + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", chooseOptions='" + chooseOptions + '\'' +
                ", optionsList=" + optionsList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RulerEngineer engineer = (RulerEngineer) o;
        return id == engineer.id &&
                serverID == engineer.serverID &&
                createTime == engineer.createTime &&
                updateTime == engineer.updateTime &&
                Objects.equals(engineerName, engineer.engineerName) &&
                Objects.equals(engineerDescription, engineer.engineerDescription) &&
                Objects.equals(chooseOptions, engineer.chooseOptions) &&
                Objects.equals(optionsList, engineer.optionsList);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, serverID, engineerName, engineerDescription, createTime, updateTime, chooseOptions, optionsList);
    }
}
