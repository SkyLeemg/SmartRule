package com.vitec.task.smartrule.bean;

public class ViewLayout {
    int top;
    int left;
    int bottom;
    int right;

    public ViewLayout(int left,int top,   int right, int bottom) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }
}
