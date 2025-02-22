package com.til.car_service.data;

import android.graphics.Bitmap;
import com.google.mlkit.vision.barcode.common.Barcode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class QrRecognitionResult {
    Barcode[] barcodes;
    String[] rawValues;
    String total;
    Bitmap outBitmap;
}
