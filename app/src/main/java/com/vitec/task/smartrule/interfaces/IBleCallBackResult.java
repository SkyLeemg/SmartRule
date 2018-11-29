package com.vitec.task.smartrule.interfaces;

/**
 * 蓝牙连接服务收到各种蓝牙状态或者信息的时候，回调此接口
 */
public interface IBleCallBackResult {

//    蓝牙连接成功
    void bleConnectSuccess();

//    蓝牙连接断开
    void bleDisconnected();

//    发现蓝牙服务
    void bleDiscoveredService();

//    收到蓝牙数据
    void bleReceviceData(String data,String uuid);

    //    蓝牙服务绑定成功
    void bleBindSuccess();
}
