package com.vitec.task.smartrule.interfaces;

import org.altbeacon.beacon.Beacon;

public interface IDialogCommunicableWithDevice {

//    void dissDialog();

    void connectingDevice();

    void connectSuccess();

    void setConnectingDevice(Beacon beacon);



}
