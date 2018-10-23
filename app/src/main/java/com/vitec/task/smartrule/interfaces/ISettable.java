package com.vitec.task.smartrule.interfaces;

public interface  ISettable {
    void setTitle(String title);

    void setMenuVisible(int flag);

    void setMenuResouce(int resouce);

    void setIconVisible(int flag);

    void setIconResouce(int resouce);

    ISettable getSettable();

    void setToolBarVisible(int flag);
}
