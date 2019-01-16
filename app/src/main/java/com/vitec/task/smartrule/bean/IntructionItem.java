package com.vitec.task.smartrule.bean;

public class IntructionItem {

    private String title;
    private String intruction;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntruction() {
        return intruction;
    }

    public void setIntruction(String intruction) {
        this.intruction = intruction;
    }

    @Override
    public String toString() {
        return "IntructionItem{" +
                "title='" + title + '\'' +
                ", intruction='" + intruction + '\'' +
                '}';
    }
}
