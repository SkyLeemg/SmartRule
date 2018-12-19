package com.vitec.task.smartrule.interfaces;

public interface ViewTouchCallBack {

    void onScroll(float distanceX, float distanceY);

    void onScroll(float distanceX, float distanceY, int flag);

    void onScale(float preScale,float scale, float px, float py);
}
