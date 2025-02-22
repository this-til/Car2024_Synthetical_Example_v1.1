package com.til.car_service;

import android.content.Intent;
import android.graphics.*;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.benjaminwan.ocrlibrary.OcrEngine;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.dsc.*;
import com.hyperai.hyperlpr3.HyperLPR3;
import com.hyperai.hyperlpr3.bean.Plate;
import com.til.car_service.data.*;
import com.til.car_service.tuple.ThreeTuple;
import com.til.car_service.tuple.TwoTuple;
import com.til.car_service.util.TaskUtil;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Service extends android.app.Service {

    /***
     * ocr引擎
     */
    protected OcrEngine ocrEngine;

    /***
     * 二维码扫描
     */
    protected BarcodeScanner scanner;

    protected MLDocumentSkewCorrectionAnalyzerSetting setting;


    protected MLDocumentSkewCorrectionAnalyzer analyzer;


    @Override
    public void onCreate() {
        super.onCreate();
        ocrEngine = new OcrEngine(this);
        scanner = BarcodeScanning.getClient();
        setting = new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create();
        analyzer = MLDocumentSkewCorrectionAnalyzerFactory.getInstance().getDocumentSkewCorrectionAnalyzer(setting);
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
    public CompletableFuture<OcrResult> ocrAsync(OcrInput characterRecognitionInput) {
        return CompletableFuture.supplyAsync(() -> ocr(characterRecognitionInput));
    }

    /***
     * ocr文字识别
     */
    public OcrResult ocr(OcrInput characterRecognitionInput) {
        Bitmap bitmap = characterRecognitionInput.getBitmap();
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        com.benjaminwan.ocrlibrary.OcrResult detect = ocrEngine.detect(
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
        return new OcrResult(outBitmap, detect);
    }

    /***
     * 二维码
     */
    public CompletableFuture<QrRecognitionResult> qrRecognitionAsync(Bitmap bitmap) {
        return CompletableFuture
                .supplyAsync(() -> InputImage.fromBitmap(bitmap, 0))
                .thenCompose(image -> TaskUtil.convert(scanner.process(image)))
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

    /***
     * 车牌识别同步
     */
    public LicensePlatesRecognitionResult licensePlatesRecognition(Bitmap bitmap) {
        Bitmap bcopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap copyShow = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvasShow = new Canvas(copyShow);
        Paint paintShow = new Paint();
        paintShow.setColor(Color.RED);
        paintShow.setStyle(Paint.Style.STROKE);
        paintShow.setStrokeWidth(2);


        Plate[] plates = HyperLPR3.getInstance().plateRecognition(
                bcopy,
                HyperLPR3.CAMERA_ROTATION_0,
                HyperLPR3.STREAM_BGRA
        );
        String[] licensePlates = new String[plates.length];

        for(int i = 0; i < plates.length; i++) {
            Plate plate = plates[i];
            canvasShow.drawRect(
                    new Rect(
                            (int) plate.getX1() + 5,
                            (int) plate.getY1() + 5,
                            (int) plate.getX2() + 5,
                            (int) plate.getY2() + 5),
                    paintShow
            );

            licensePlates[i] = "国" + plate.getCode().substring(1) + switch (plate.getType()) {
                case 0 -> "（燃油）";
                case 3 -> "（电车）";
                default -> "（其他）";
            };
        }

        return new LicensePlatesRecognitionResult(plates, licensePlates, copyShow, String.join("\n", licensePlates));
    }

    /***
     * 车牌识别异步
     */
    public CompletableFuture<LicensePlatesRecognitionResult> carTesseractAsync(Bitmap bitmap) {
        return CompletableFuture.supplyAsync(() -> licensePlatesRecognition(bitmap));
    }

    /***
     * 边缘检测
     */
    public CompletableFuture<FindCornerResult> findCornerAsync(Bitmap bitmap) {
        
        
       /* return CompletableFuture.supplyAsync(() -> MLFrame.fromBitmap(bitmap))
                .thenApply(frame -> new TwoTuple<>(frame, analyzer.asyncDocumentSkewDetect(frame).getResult()))
                .thenApply(tuple -> {
                    MLDocumentSkewDetectResult result = tuple.getB();
                    android.graphics.Point leftTop = result.getLeftTopPosition();
                    android.graphics.Point rightTop = result.getRightTopPosition();
                    android.graphics.Point leftBottom = result.getLeftBottomPosition();
                    android.graphics.Point rightBottom = result.getRightBottomPosition();
                    List<android.graphics.Point> coordinates = new ArrayList<>();
                    coordinates.add(leftTop);
                    coordinates.add(rightTop);
                    coordinates.add(rightBottom);
                    coordinates.add(leftBottom);
                    return new ThreeTuple<>(tuple.getA(), result, new MLDocumentSkewCorrectionCoordinateInput(coordinates));
                })
                .thenApply(tuple -> new ThreeTuple<>(tuple.getA(), tuple.getB(), analyzer.asyncDocumentSkewCorrect(tuple.getA(), tuple.getC()).getResult()))
                .thenApply(tuple -> new FindCornerResult(tuple.getB(), tuple.getC(), tuple.getC().getCorrected()));*/

        return CompletableFuture.supplyAsync(() -> {
            try {
                MLFrame frame = MLFrame.fromBitmap(bitmap);
                MLDocumentSkewDetectResult mlDocumentSkewDetectResult = TaskUtil.getResultSync(analyzer.asyncDocumentSkewDetect(frame), 5, TimeUnit.SECONDS);
                MLDocumentSkewCorrectionResult mlDocumentSkewCorrectionResult = TaskUtil.getResultSync(
                        analyzer.asyncDocumentSkewCorrect(
                                frame,
                                new MLDocumentSkewCorrectionCoordinateInput(
                                        List.of(
                                                mlDocumentSkewDetectResult.getLeftTopPosition(),
                                                mlDocumentSkewDetectResult.getRightTopPosition(),
                                                mlDocumentSkewDetectResult.getLeftBottomPosition(),
                                                mlDocumentSkewDetectResult.getRightBottomPosition()
                                        )
                                )
                        ),
                        5,
                        TimeUnit.SECONDS);
                
                return new FindCornerResult(frame, mlDocumentSkewDetectResult, mlDocumentSkewCorrectionResult, mlDocumentSkewCorrectionResult.getCorrected());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
      /*  return CompletableFuture.supplyAsync(() -> MLFrame.fromBitmap(bitmap))
                .thenApply(frame -> {
                    Task<MLDocumentSkewDetectResult> mlDocumentSkewDetectResultTask = analyzer.asyncDocumentSkewDetect(frame);
                    try {
                        mlDocumentSkewDetectResultTask.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new TwoTuple<>(frame, mlDocumentSkewDetectResultTask.getResult());
                })
                .thenApply(tuple -> {
                    MLDocumentSkewDetectResult result = tuple.getB();
                    android.graphics.Point leftTop = result.getLeftTopPosition();
                    android.graphics.Point rightTop = result.getRightTopPosition();
                    android.graphics.Point leftBottom = result.getLeftBottomPosition();
                    android.graphics.Point rightBottom = result.getRightBottomPosition();
                    List<android.graphics.Point> coordinates = new ArrayList<>();
                    coordinates.add(leftTop);
                    coordinates.add(rightTop);
                    coordinates.add(rightBottom);
                    coordinates.add(leftBottom);
                    return new ThreeTuple<>(tuple.getA(), result, new MLDocumentSkewCorrectionCoordinateInput(coordinates));
                })
                .thenApply(tuple -> {
                    Task<MLDocumentSkewCorrectionResult> mlDocumentSkewCorrectionResultTask = analyzer.asyncDocumentSkewCorrect(tuple.getA(), tuple.getC());
                    try {
                        mlDocumentSkewCorrectionResultTask.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new ThreeTuple<>(tuple.getA(), tuple.getB(), mlDocumentSkewCorrectionResultTask.getResult());
                })
                .thenApply(tuple -> new FindCornerResult(tuple.getB(), tuple.getC(), tuple.getC().getCorrected()));*/
    }

    /***
     * 红路灯检测
     */
    public TrafficLightCheckResult trafficLightCheck(Bitmap bitmap) {
        int red = 0, yellow = 0, green = 0;
        // 进行霍夫圆检测
        Mat grayImage = new Mat();
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        // 将图片转换为单通道GRAY
        Imgproc.cvtColor(mat, grayImage, Imgproc.COLOR_BGR2GRAY);
        Mat circles = new Mat();
        /* 霍夫圆检测 Imgproc.HoughCircles()
         * grayImage：输入图像，必须是单通道的灰度图像。
         * circles：输出参数，用于存储检测到的圆的结果。它是一个Mat类型的变量，每一行包含了一个检测到的圆的信息，包括圆心的坐标和半径。
         * method：霍夫圆检测的方法。在OpenCV中，只提供了一种方法，即Imgproc.CV_HOUGH_GRADIENT。它基于梯度信息来进行圆检测。
         * dp：累加器分辨率与图像分辨率的比值。默认值为1，表示两者相等。较小的值可以提高检测的精度，但会增加计算量。
         * minDist：检测到的圆之间的最小距离。如果设置为太小的值，可能会导致检测到重复的圆。如果设置为太大的值，可能会错过一些圆。
         * param1：边缘检测阈值。边缘像素的梯度值高于该阈值才会被认为是有效的边缘。较大的值可以过滤掉较弱的边缘，较小的值可以检测到更多的圆。
         * param2：圆心累加器阈值。检测到的圆心区域的累加器值高于该阈值才会被认为是有效的圆心。较大的值可以过滤掉较弱的圆，较小的值可以检测到更多的圆。
         * minRadius：圆的最小半径。如果设置为0，则没有最小半径限制。
         * maxRadius：圆的最大半径。如果设置为0，则没有最大半径限制。
         */
        Imgproc.HoughCircles(grayImage, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 20, 100, 20, 35);
        // 创建掩膜图像
        Mat mask = Mat.zeros(mat.size(), CvType.CV_8U);
        // 绘制检测到的圆形区域到掩膜图像上
        for(int i = 0; i < circles.cols(); i++) {
            double[] circleData = circles.get(0, i);
            org.opencv.core.Point center = new org.opencv.core.Point(circleData[0], circleData[1]);
            int radius = (int) circleData[2];
            Imgproc.circle(mask, center, radius, new Scalar(255), -1);
            // 提取圆形区域的颜色
            org.opencv.core.Rect roi = new org.opencv.core.Rect((int) (center.x - radius), (int) (center.y - radius), radius * 2, radius * 2);
            Mat roiImage = new Mat(mat, roi);
            Scalar meanColor = Core.mean(roiImage);
            if (meanColor.val[0] > 180 && meanColor.val[1] < 180 && meanColor.val[2] < 180) {
                red++;
            } else if (meanColor.val[0] > 180 && meanColor.val[1] > 180 && meanColor.val[2] < 180) {
                yellow++;
            } else if (meanColor.val[0] < 180 && meanColor.val[1] > 180 && meanColor.val[2] > 180) {
                green++;
            }
            Log.d("color", meanColor.val[0] + " " + meanColor.val[1] + " " + meanColor.val[2]);
            // 在圆的中心位置绘制颜色标记
            Imgproc.circle(mat, center, 50, meanColor, -1);
        }

        TrafficLightState trafficLightState;
        String total;

        if (red > yellow && red > green) {
            trafficLightState = TrafficLightState.RED;
            total = "交通信号灯识别结果：红色";
        } else if (yellow > red && yellow > green) {
            trafficLightState = TrafficLightState.YELLOW;
            total = "交通信号灯识别结果：黄色";
        } else if (green > red && green > yellow) {
            trafficLightState = TrafficLightState.GREEN;
            total = "交通信号灯识别结果：绿色";
        } else {
            trafficLightState = TrafficLightState.NULL;
            total = "未识别到交通灯";
        }

        // 将原始图像与掩膜图像进行按位与运算，只保留圆形区域
        Mat result = new Mat();
        Core.bitwise_and(mat, mat, result, mask);
        // 显示结果
        Bitmap bitmapResult = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bitmapResult);

        return new TrafficLightCheckResult(trafficLightState, bitmapResult, total);


    }


    public CompletableFuture<TrafficLightCheckResult> trafficLightCheckAsync(Bitmap bitmap) {
        return CompletableFuture.supplyAsync(() -> trafficLightCheck(bitmap));
    }

}
