package com.til.car_service.data;

import android.graphics.Bitmap;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OcrInput {
    private Bitmap bitmap;
    private int maxSideLen;
    private int padding = 50;
    private float boxScoreThresh = 0.6f;
    private float boxThresh = 0.3f;
    private float unClipRatio = 2;
    private boolean doAngle = true;
    private boolean mostAngle = true;

    public OcrInput(Bitmap bitmap) {
        this(bitmap, bitmap.getWidth() * bitmap.getHeight());
    }

    public OcrInput(Bitmap bitmap, int maxSideLen) {
        this.bitmap = bitmap;
        this.maxSideLen = maxSideLen;
    }

}
