package com.til.car_service.data;

import android.graphics.Bitmap;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OcrResult {
    private Bitmap outBitmap;
    private com.benjaminwan.ocrlibrary.OcrResult ocrResult;
}
