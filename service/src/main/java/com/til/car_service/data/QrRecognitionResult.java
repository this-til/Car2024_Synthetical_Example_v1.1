package com.til.car_service.data;

import android.graphics.Bitmap;
import com.google.mlkit.vision.barcode.common.Barcode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@AllArgsConstructor
public class QrRecognitionResult {
    QeCell[] qeCells;
    String total;
    Bitmap outBitmap;

    public QrRecognitionResult(QeCell[] qeCells, Bitmap outBitmap) {
        this.qeCells = qeCells;
        total = qeCells.length == 0
                ? "未识别二维码"
                : IntStream
                        .range(0, qeCells.length)
                        .mapToObj(i -> "二维码" + i + ":" + qeCells[i].getValue())
                        .collect(Collectors.joining("\n"));
        this.outBitmap = outBitmap;
    }

    @Data
    @AllArgsConstructor
    public static class QeCell {
        Barcode barcode;
        String value;
        @Nullable
        ColorType colorType;
    }
}
