package com.til.car_service.data;

import android.graphics.Bitmap;
import com.hyperai.hyperlpr3.bean.Plate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicensePlatesRecognitionResult {
    private Plate[] plates;
    private String[] licensePlates;
    private Bitmap outBitmap;
    private String total;
}
