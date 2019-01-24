package com.vitec.task.smartrule.interfaces;

import com.vitec.task.smartrule.bean.ChooseMeasureMsg;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;

import java.util.List;

public interface IChooseGetter {

    List<RulerEngineer> getEngineerList();

    List<RulerOptions> getOptionsList();

    List<ChooseMeasureMsg> getChooseMeasureMsgList();

    List<RulerCheckProject> getCheckProjectList();

    void finishActivity();

    void updateChooseMeasureMsgList(int index, ChooseMeasureMsg chooseMeasureMsg);

    void updateProjectList();

}
