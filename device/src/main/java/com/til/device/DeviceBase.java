package com.til.device;


public class DeviceBase {

    public static final byte DATA_FRAME_HEADER = (byte) 0x55;

    public static final byte DATA_FRAME_TAIL = (byte) 0xBB;

    byte id;

    public DeviceBase(byte id) {
        this.id = id;
    }
}
