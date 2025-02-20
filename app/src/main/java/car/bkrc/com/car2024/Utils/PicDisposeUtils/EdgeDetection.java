package car.bkrc.com.car2024.Utils.PicDisposeUtils;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.widget.ImageView;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult;

import java.util.ArrayList;
import java.util.List;

public class EdgeDetection {

    MLDocumentSkewCorrectionAnalyzerSetting setting = new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create();
    MLDocumentSkewCorrectionAnalyzer analyzer = MLDocumentSkewCorrectionAnalyzerFactory.getInstance().getDocumentSkewCorrectionAnalyzer(setting);

    public void findCorner(Bitmap bitmap,ImageView imageView){
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        // asyncDocumentSkewDetect异步调用。
        Task<MLDocumentSkewDetectResult> detectTask = analyzer.asyncDocumentSkewDetect(frame);
        detectTask.addOnSuccessListener(detectResult -> {
            // 检测成功。
            android.graphics.Point leftTop = detectResult.getLeftTopPosition();
            android.graphics.Point rightTop = detectResult.getRightTopPosition();
            android.graphics.Point leftBottom = detectResult.getLeftBottomPosition();
            android.graphics.Point rightBottom = detectResult.getRightBottomPosition();
            List<Point> coordinates = new ArrayList<>();
            coordinates.add(leftTop);
            coordinates.add(rightTop);
            coordinates.add(rightBottom);
            coordinates.add(leftBottom);
            appliance(frame,new MLDocumentSkewCorrectionCoordinateInput(coordinates),imageView);
        }).addOnFailureListener(e -> {
            // 检测失败。
        });
    }

    private void appliance(MLFrame frame, MLDocumentSkewCorrectionCoordinateInput coordinateData, ImageView picrec_iv){
        // asyncDocumentSkewCorrect异步调用。
        try{
            Task<MLDocumentSkewCorrectionResult> correctionTask = analyzer.asyncDocumentSkewCorrect(frame, coordinateData);
            correctionTask.addOnSuccessListener(refineResult -> {
                // 检测成功。
                picrec_iv.setImageBitmap(refineResult.getCorrected());
            }).addOnFailureListener(e -> {
                // 检测失败。
            });
        }catch (Exception ignored){
        }

    }
}
