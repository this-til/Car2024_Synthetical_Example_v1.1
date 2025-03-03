package com.til.car_service.data;

import android.graphics.Bitmap;
import com.til.util.BitmapUtil;
import com.yolov8ncnn.IItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicensePlatesRecognitionEnhancementResult {
    Yolov8DetectResult<IItem.CardType> yolov8DetectResult;
    LicensePlate[] licensePlates;
    private Bitmap outBitmap;
    private String total;
    
    public LicensePlatesRecognitionEnhancementResult(Yolov8DetectResult<IItem.CardType> yolov8DetectResult, LicensePlate[] licensePlates) {
        this.yolov8DetectResult = yolov8DetectResult;
        this.licensePlates = licensePlates;
    
        outBitmap = yolov8DetectResult.getOutBitmap();
        total = Arrays.stream(licensePlates).map(LicensePlate::getLicensePlate).collect(Collectors.joining("\n"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LicensePlate {
        IItem.ItemCell<IItem.CardType> yolov8Item;
        String licensePlate;
    }
}
