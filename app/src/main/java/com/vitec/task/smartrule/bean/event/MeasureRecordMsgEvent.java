package com.vitec.task.smartrule.bean.event;

import com.vitec.task.smartrule.bean.RulerCheck;

import java.util.List;

public class MeasureRecordMsgEvent {

    private boolean isSuccess;
    private int code;
    private List<RulerCheck> checkList;
    private int total;
    private int currentPage;
    private int pageSize;

    public List<RulerCheck> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<RulerCheck> checkList) {
        this.checkList = checkList;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "MeasureRecordMsgEvent{" +
                "checkList=" + checkList +
                ", total=" + total +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                '}';
    }
}
