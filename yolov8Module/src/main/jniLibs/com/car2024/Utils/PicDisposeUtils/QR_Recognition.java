package car.bkrc.com.car2024.Utils.PicDisposeUtils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.Utils.dialog.RecDialog;

/**
 * 二维码识别工具类
 */

public class QR_Recognition {

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static void QRRecognition(Bitmap bitmap, Context context, TextView textView, ImageView imageView) {
        executor.execute(() -> {
            if (!OpenCVLoader.initDebug()) {
                // OpenCV initialization failed
                return;
            }

            Mat imgMat = new Mat();
            Utils.bitmapToMat(bitmap, imgMat);

            Mat binaryMat = new Mat();
            Imgproc.cvtColor(imgMat, binaryMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(binaryMat, binaryMat, 128, 255, Imgproc.THRESH_BINARY);

            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                    Barcode.FORMAT_QR_CODE,
                                    Barcode.FORMAT_AZTEC)
                            .build();

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            BarcodeScanner scanner = BarcodeScanning.getClient();

            int[] qrCounter = {0}; // 用于记录二维码的序号

            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        Canvas canvas = new Canvas(bitmap);
                        for (Barcode barcode : barcodes) {
                            qrCounter[0]++; // 增加二维码的序号
                            Rect rect = barcode.getBoundingBox();
                            Paint paint = new Paint();
                            paint.setColor(Color.GREEN);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(5);
                            canvas.drawRect(rect, paint);

                            Scalar color = getAverageColor(imgMat, rect);
                            int qrColor = Color.rgb((int) color.val[2], (int) color.val[1], (int) color.val[0]);

                            Paint textPaint = new Paint();
                            textPaint.setColor(Color.RED);
                            textPaint.setTextSize(30f);
                            textPaint.setStyle(Paint.Style.FILL);
                            canvas.drawText(String.format(Locale.getDefault(), "二维码 %d, 颜色: %s", qrCounter[0], getRGBString(qrColor)), rect.left, rect.top - 10, textPaint);

                            String rawValue = barcode.getRawValue();
                            handler.post(() -> {
                                if (textView != null) {
                                    textView.append(String.format(Locale.getDefault(), "二维码 %d: %s, 颜色: %s\n", qrCounter[0], rawValue, getRGBString(qrColor)));
                                    imageView.setImageBitmap(bitmap);
                                } else {
                                    RecDialog.createLoadingDialog(context, bitmap, "二维码识别", String.format(Locale.getDefault(), "二维码 %d: %s, 颜色: %s", qrCounter[0], rawValue, getRGBString(qrColor)));
                                }
                            });
                        }
                    }).addOnFailureListener(Throwable::printStackTrace);

            // Shutdown the executor service after all tasks are completed
            try {
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static Scalar getAverageColor(Mat imgMat, Rect rect) {
        Mat roi = new Mat(imgMat, new org.opencv.core.Rect(rect.left, rect.top, rect.width(), rect.height()));
        Scalar color = Core.mean(roi);
        roi.release();
        return color;
    }

    private static String getRGBString(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return String.format(Locale.getDefault(), "RGB(%d, %d, %d)", red, green, blue);
    }
}






