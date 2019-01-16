package com.vitec.task.smartrule.bean;

public class MeasureData {

    private int id;
    private String data;

    @Override
    public String toString() {
        return "MeasureData{" +
                "id=" + id +
                ", data='" + data + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
