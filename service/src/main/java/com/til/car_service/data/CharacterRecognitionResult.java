package com.til.car_service.data;

import android.graphics.Bitmap;
import com.benjaminwan.ocrlibrary.OcrResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CharacterRecognitionResult {
    private Bitmap outBitmap;
    private OcrResult ocrResult;
}
