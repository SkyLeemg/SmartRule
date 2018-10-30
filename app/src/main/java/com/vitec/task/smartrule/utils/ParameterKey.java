package com.vitec.task.smartrule.utils;

public class ParameterKey {

    /**
     * 物联网连接需用要到的key
     * {
     * "productKey":"a1UNvLhFILp",
     * "deviceName":"testS",
     * "deviceSecret":"AO3zGlGP6ivc0JHHyY5Si5mS40UpGcco",
     * "bleMac":"FC:04:35:B7:9A:D8"
     * }
     */
    public static final String product_key = "productKey";
    public static final String device_name = "deviceName";
    public static final String device_secret = "deviceSecret";
    public static final String ble_mac = "bleMac";

    /**
     * 测量工程各项信息的key
     */
    public static final String projectIDKey = "projectID";//项目编号
    public static final String resourceIDKey = "resourceID";//底部导航的图标资源
    public static final String projectNameKey = "projectName";//项目名，例如P31,由用户手动填写
    public static final String projectTypeKey = "projectType";//项目类型,例如混凝土工程，固定选项
    public static final String checkPositonKey = "checkPositon";//检查位置，例如A栋2层，手动填写
    public static final String checkPersonKey = "checkPerson";//检查人，例如张三，从账号中读取
    public static final String checkTimeKey = "checkTime";//检查时间，自动获取系统时间
    public static final String measureItemNameKey = "measureItemName";//底部导航的名称，例如垂直度，平整度，为管控要点的简称
    public static final String measureItemKey = "measureItem";//管控要点，例如立面垂直度

}
