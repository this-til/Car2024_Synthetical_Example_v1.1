package com.til.car_service;

import android.content.Intent;
import android.graphics.*;
import android.os.Binder;
import android.os.IBinder;
import com.benjaminwan.ocrlibrary.OcrEngine;
import com.benjaminwan.ocrlibrary.OcrResult;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.til.car_service.data.CharacterRecognitionInput;
import com.til.car_service.data.CharacterRecognitionResult;
import com.til.car_service.data.QrRecognitionResult;
import com.til.car_service.util.TaskToFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Service extends android.app.Service {

    /***
     * ocr引擎
     */
    protected OcrEngine ocrEngine;

    /***
     * 二维码扫描
     */
    protected BarcodeScanner scanner;

    @Override
    public void onCreate() {
        super.onCreate();
        ocrEngine = new OcrEngine(this);
        scanner = BarcodeScanning.getClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    public class ServiceBinder extends Binder {
        public Service getService() {
            return Service.this;
        }
    }


    /***
     * ocr文字识别-异步
     */
    public CompletableFuture<CharacterRecognitionResult> ocrAsync(CharacterRecognitionInput characterRecognitionInput) {
        return CompletableFuture.supplyAsync(() -> ocr(characterRecognitionInput));
    }

    /***
     * ocr文字识别
     */
    public CharacterRecognitionResult ocr(CharacterRecognitionInput characterRecognitionInput) {
        Bitmap bitmap = characterRecognitionInput.getBitmap();
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        OcrResult detect = ocrEngine.detect(
                bitmap,
                outBitmap,
                characterRecognitionInput.getPadding(),
                characterRecognitionInput.getMaxSideLen(),
                characterRecognitionInput.getBoxScoreThresh(),
                characterRecognitionInput.getBoxThresh(),
                characterRecognitionInput.getUnClipRatio(),
                characterRecognitionInput.isDoAngle(),
                characterRecognitionInput.isMostAngle()
        );
        return new CharacterRecognitionResult(outBitmap, detect);
    }

    /***
     * 二维码
     */
    public CompletableFuture<QrRecognitionResult> qrRecognitionAsync(Bitmap bitmap) {
        return CompletableFuture
                .supplyAsync(() -> InputImage.fromBitmap(bitmap, 0))
                .thenCompose(image -> TaskToFuture.convert(scanner.process(image)))
                .thenApply(list -> {
                    if (list == null) {
                        return null;
                    }

                    List<String> rawValueList = new ArrayList<>(list.size());
                    StringBuilder stringBuilder = new StringBuilder();
                    Bitmap outBitmap = Bitmap.createBitmap(bitmap);
                    Canvas canvas = new Canvas(outBitmap);

                    for(int i = 0; i < list.size(); i++) {
                        Barcode barcode = list.get(i);

                        rawValueList.add(barcode.getRawValue());
                        stringBuilder.append("二维码").append(i).append(':').append(' ').append(barcode.getRawValue()).append('\n');


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
                    }

                    return new QrRecognitionResult(
                            list,
                            rawValueList,
                            stringBuilder.toString(),
                            outBitmap
                    );

                });
    }


}
