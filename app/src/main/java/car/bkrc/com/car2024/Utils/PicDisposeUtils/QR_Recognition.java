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

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

import car.bkrc.com.car2024.Utils.dialog.RecDialog;

/**
 * 二维码识别工具类
 */
public class QR_Recognition {

    static Handler handler = new Handler(Looper.getMainLooper());

    public static String result_qr;
    public static int i;

    public static Bitmap mutableBitmap;

    public static void QRRecognition(Bitmap bitmap,  Context context,TextView textView, ImageView imageView){
        i = 0;result_qr = "";
        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        new Thread(()->{
            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                    Barcode.FORMAT_QR_CODE,
                                    Barcode.FORMAT_AZTEC)
                            .build();
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            BarcodeScanner scanner = BarcodeScanning.getClient();
            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        // 识别到二维码进行处理
                        Canvas canvas = new Canvas(mutableBitmap);
                        for (Barcode barcode: barcodes) {
                            i++;
                            Rect rect = barcode.getBoundingBox();
                            // 画框
                            Paint paint = new Paint();
                            paint.setColor(Color.GREEN);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(5);
                            canvas.drawRect(rect, paint);
                            assert rect != null;
                            // 绘制二维码顺序编号
                            Paint textPaint = new Paint();
                            textPaint.setColor(Color.RED);
                            textPaint.setTextSize(30f);
                            textPaint.setStyle(Paint.Style.FILL);
                            canvas.drawText("二维码：" + i , rect.left, rect.top-10, textPaint); // 绘制文字
                            String rawValue = barcode.getRawValue();
                            result_qr = result_qr + "二维码"+i+"："+rawValue+"\n";
                        }
                        handler.post(() -> {
                            // 在UI线程更新UI
                            if (result_qr.length()!= 0) {
                                if (textView!=null){
                                    textView.setText(result_qr);
                                    imageView.setImageBitmap(mutableBitmap);
                                }else {
                                    RecDialog.createLoadingDialog(context, mutableBitmap, "二维码识别", result_qr);
                                }
                            } else {
                                if (textView!=null){
                                    textView.setText("未识别到二维码！");
                                    imageView.setImageBitmap(mutableBitmap);
                                }else {
                                    RecDialog.createLoadingDialog(context, mutableBitmap, "二维码识别", "未识别到二维码！");
                                }
                            }
                        });
                    }).addOnFailureListener(Throwable::printStackTrace);
        }).start();
    }
}