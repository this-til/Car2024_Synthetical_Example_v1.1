package com.til.car_service.data;

import android.graphics.Bitmap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencv.core.MatOfPoint;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShapeColorDetectionResult {

    private ShapeDescribe[] shapeDescribes;
    private Bitmap outBitmap;
    private String total;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShapeDescribe {
        private ShapeType shapeType;
        private ColorType colorType;
        private MatOfPoint contour;
    }
}
