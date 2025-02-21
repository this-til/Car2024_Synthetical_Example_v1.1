package com.til.car_service.data;

import android.graphics.Bitmap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficLightCheckResult {
    private TrafficLightState trafficLightState;
    private Bitmap outImage;
    private String total;
}
