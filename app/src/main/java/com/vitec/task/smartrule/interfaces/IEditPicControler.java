package com.vitec.task.smartrule.interfaces;

import com.vitec.task.smartrule.bean.RulerCheckOptions;

import java.util.List;

public interface IEditPicControler {

    /**
     * 获取图纸对应的管控要点数据
     * @return
     */
    List<RulerCheckOptions> getCheckOptions();

    /**
     * 设置添加图纸按钮是否显示出来
     * @param flag
     */
    void setTvAddmPicVisibale(int flag);

}
