/*
package util;

//import static car.bkrc.com.car2024.FragmentView.PicInformationProcess.picrec_iv;
//import static car.bkrc.com.car2024.FragmentView.PicInformationProcess.picrectext_tv;
//import static car.bkrc.com.car2024.ViewAdapter.InfrareAdapter.handler;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.hyperai.hyperlpr3.HyperLPR3;
import com.hyperai.hyperlpr3.bean.Plate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import com.yolov8ncnn.Yolov8Ncnn;

public class LicensePlatRecognition {

    ExecutorService executorService = Executors.newSingleThreadExecutor();


    public CompletionStage<List<RecognitionOut>> openRecognition(Bitmap bitmap) {
        Supplier<List<RecognitionOut>> task = () -> {
            List<RecognitionOut> recognitionOutList = new ArrayList<>();

            Yolov8Ncnn.Obj[] objects = Yolov8Ncnn.Detect(bitmap, false, 8);

            for (Yolov8Ncnn.Obj obj : objects) {

                if (obj.prob < 0.2) {
                    continue;
                }

                double nmd1, nmd2, mndh, nmdc, nmdjg;
                int dusu = 0;
                nmd1 = obj.w / obj.h;
                nmd2 = 316f / 104f;
                mndh = nmd1 * nmd2;
                nmdc = nmd2 - nmd1;
                if (Math.abs(nmd2 - 1) >= 0.1) {
                    nmdjg = nmdc / (mndh - 1);
                    dusu = (int) Math.toDegrees(Math.atan(nmdjg));
                    Log.e("度数", String.valueOf(dusu));
                }
                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);
                RectF rect = new RectF(obj.x, obj.y, obj.x + obj.w, obj.y + obj.h); // 使用RectF类型
                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (int) obj.x - 50, (int) obj.y - 50, (int) obj.w + 100, (int) obj.h + 100);

                Map<String, Integer> plateCountMap = new HashMap<>();
                for (int c = 0; c <= 72; c++) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(c * 5);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
                    Plate[] plates = HyperLPR3.getInstance().plateRecognition(rotatedBitmap, HyperLPR3.CAMERA_ROTATION_0, HyperLPR3.STREAM_BGRA);
                    for (Plate plate : plates) {
                        String plateCode = plate.getCode().substring(1);
                        plateCountMap.put(plateCode, plateCountMap.getOrDefault(plateCode, 0) + 1);
                        Log.e("carTesseract: ", plate.toString());
                    }
                }

                Optional<Map.Entry<String, Integer>> first = plateCountMap
                        .entrySet()
                        .stream()
                        .min(Comparator.comparingInt(Map.Entry::getValue));

                if (!first.isPresent()) {
                    continue;
                }
                recognitionOutList.add(new RecognitionOut(obj, Yolov8Ncnn.CpaType.values()[obj.label], first.get().getKey()));
            }
            return recognitionOutList;
        };

        return CompletableFuture.supplyAsync(task, executorService);
    }


    public static class RecognitionOut{
        public final Yolov8Ncnn.Obj obj;
        public final Yolov8Ncnn.CpaType cpaType;
        public final String text;

        public RecognitionOut(Yolov8Ncnn.Obj obj, Yolov8Ncnn.CpaType cpaType, String text) {
            this.obj = obj;
            this.cpaType = cpaType;
            this.text = text;
        }
    }
}
*/
