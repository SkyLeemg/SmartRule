package com.vitec.task.smartrule.utils;

public class BleParam {

    public static final int STAAE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.vitec.smart.rule.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.vitec.smart.rule.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.vitec.smart.rule.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.vitec.smart.rule.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.vitec.smart.rule.EXTRA_DATA";
    public final static String EXTRA_UUID = "com.vitec.smart.rule.EXTRA_UUID";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART = "com.vitec.smart.rule.DEVICE_DOES_NOT_SUPPORT_UART";


    public static final int REQUEST_SELECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int UART_PROFILE_READY = 10;
    public static final int UART_PROFILE_CONNECTED = 20;
    public static final int UART_PROFILE_DISCONNECTED = 21;
    public static final int STATE_OFF = 10;
}
