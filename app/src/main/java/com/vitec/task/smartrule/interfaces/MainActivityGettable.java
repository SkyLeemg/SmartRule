package com.vitec.task.smartrule.interfaces;

import org.altbeacon.beacon.Beacon;

import java.util.List;

/**
 * Created by skyel on 2018/10/11.
 */
public interface MainActivityGettable {

    public List<Beacon> getDevices();

    void handleConectResult(boolean isDone);

    void handleServerMessage(String result);
}
