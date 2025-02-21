package com.til.car_service.data;

import android.graphics.Bitmap;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindCornerResult {
    private MLDocumentSkewDetectResult mlDocumentSkewDetectResult;
    private MLDocumentSkewCorrectionResult mlDocumentSkewCorrectionResult;
    private Bitmap outBitmap;
}
