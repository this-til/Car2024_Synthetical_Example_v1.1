package com.til.device;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.ActivityView.LoginActivity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/***
 * 灯杆
 */
public class TrafficLight extends DeviceBase {

    /***
     * 请求进入识别模式
     */
    void requestToEnterRecognitionMode() {
        FirstActivity.Connect_Transport.sendTrafficLight();
    }

    /***
     * 请求确认识别结果
     */
    void requestConfirmationOfIdentificationResults(TrafficLightModel trafficLightModel) {

    }

    public TrafficLight(byte id) {
        super(id);
    }


    @Getter
    @AllArgsConstructor
    @ToString
    enum TrafficLightModel {

        RED((byte) 0x01),
        GREEN((byte) 0x02),
        YELLOW((byte) 0x03);

        final byte id;
    }


}
