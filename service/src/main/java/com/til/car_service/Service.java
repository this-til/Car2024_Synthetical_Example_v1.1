package com.til.car_service;

import android.content.Intent;
import android.graphics.*;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.benjaminwan.ocrlibrary.OcrEngine;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.dsc.*;
import com.hyperai.hyperlpr3.HyperLPR3;
import com.hyperai.hyperlpr3.bean.Plate;
import com.til.car_service.data.*;
import com.til.util.PointUtil;
import com.til.util.TaskUtil;
import com.til.util.tuple.Ptr;
import com.yolov8ncnn.IItem;
import com.yolov8ncnn.IModel;
import com.yolov8ncnn.Yolov8Ncnn;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        //ocrEngine = new OcrEngine(this);
        Yolov8Ncnn.init(getAssets());

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
    public CompletionStage<OcrResult> ocrAsync(OcrInput characterRecognitionInput) {
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
    public CompletionStage<QrRecognitionResult> qrRecognitionAsync(Bitmap bitmap) {
        return CompletableFuture
                .supplyAsync(() -> InputImage.fromBitmap(bitmap, 0))
                .thenComposeAsync(image -> TaskUtil.convert(scanner.process(image)))
                .thenApplyAsync(list -> {
                    if (list == null) {
                        return null;
                    }

                    Barcode[] barcodes = new Barcode[list.size()];
                    String[] rawValues = new String[list.size()];

                    StringBuilder stringBuilder = new StringBuilder();
                    Bitmap outBitmap = Bitmap.createBitmap(bitmap);
                    Canvas canvas = new Canvas(outBitmap);

                    for(int i = 0; i < list.size(); i++) {
                        Barcode barcode = list.get(i);

                        barcodes[i] = barcode;
                        rawValues[i] = barcode.getRawValue();

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
                            barcodes,
                            rawValues,
                            barcodes.length == 0 ? "未识别到二维码" : stringBuilder.toString(),
                            outBitmap
                    );

                });
    }

    /***
     * 车牌识别同步(官方示例)
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

        return new LicensePlatesRecognitionResult(
                plates,
                licensePlates,
                copyShow,
                licensePlates.length > 0
                        ? String.join("\n", licensePlates)
                        : "未识别到车牌"
        );
    }

    /***
     * 车牌识别异步
     */
    public CompletionStage<LicensePlatesRecognitionResult> carTesseractAsync(Bitmap bitmap) {
        return CompletableFuture.supplyAsync(() -> licensePlatesRecognition(bitmap));
    }

    /***
     * 边缘检测
     */
    public CompletionStage<FindCornerResult> findCornerAsync(Bitmap bitmap) {
        
        
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
     * 红绿灯检测
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
            total = "结果：红色";
        } else if (yellow > red && yellow > green) {
            trafficLightState = TrafficLightState.YELLOW;
            total = "结果：黄色";
        } else if (green > red && green > yellow) {
            trafficLightState = TrafficLightState.GREEN;
            total = "结果：绿色";
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

    /***
     * 红绿灯检测异步
     */
    public CompletionStage<TrafficLightCheckResult> trafficLightCheckAsync(Bitmap bitmap) {
        return CompletableFuture.supplyAsync(() -> trafficLightCheck(bitmap));
    }

    /***
     * 形状检测
     */
    public ShapeColorDetectionResult shapeColorDetection(ShapeDetectionInput shapeDetectionInput) {
        int width = shapeDetectionInput.getBitmap().getWidth();
        int height = shapeDetectionInput.getBitmap().getHeight();

        Mat mat = new Mat(width, height, CvType.CV_8UC4);
        Mat outMat = new Mat(width, height, CvType.CV_8UC4);
        org.opencv.android.Utils.bitmapToMat(shapeDetectionInput.getBitmap(), mat);
        org.opencv.android.Utils.bitmapToMat(shapeDetectionInput.getBitmap(), outMat);

        Mat fgMask = new Mat();
        BackgroundSubtractorMOG2 bgSubtractor = Video.createBackgroundSubtractorMOG2();
        bgSubtractor.apply(mat, fgMask);

        Mat prospect = new Mat();
        Core.bitwise_and(mat, fgMask, prospect);

        //灰度图
        Mat grayscale = new Mat();
        Imgproc.cvtColor(prospect, grayscale, Imgproc.COLOR_BGR2GRAY);

        //高斯模糊
        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(grayscale, blurredMat, new Size(5, 5), 0);

        //二值化
        Mat binarization = new Mat();
        Imgproc.Canny(blurredMat, binarization, shapeDetectionInput.getMinThreshold(), shapeDetectionInput.getMaxThreshold(), 3);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));//  指定腐蚀膨胀核
        Mat kerneled = new Mat();
        Imgproc.dilate(binarization, kerneled, kernel);


        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> _contours = new ArrayList<>();
        List<ShapeColorDetectionResult.ShapeDescribe> resultList = new ArrayList<>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(kerneled, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);//查找轮廓
        double maxSize = width * height;

        for(int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (area / maxSize > shapeDetectionInput.getMinJudgmentRange()) {
                continue;
            }
            if (area > shapeDetectionInput.getMinShapeArea()) {
                continue;
            }
            contours.remove(i);
            i--;
        }

        Mat repeatMask = new Mat(mat.size(), CvType.CV_8UC1);

        for(MatOfPoint contour : contours) {
            Core.multiply(contour, shapeDetectionInput.getEnlarge(), contour);

            MatOfPoint2f newcoutour = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f resultcoutour = new MatOfPoint2f();
            Imgproc.approxPolyDP(newcoutour, resultcoutour, 0.01 * Imgproc.arcLength(newcoutour, true), true);
            contour = new MatOfPoint(resultcoutour.toArray());
            // 进行修正，缩小4倍改变联通区域大小
            Core.multiply(contour, new Scalar(1 / shapeDetectionInput.getEnlarge().val[0], 1 / shapeDetectionInput.getEnlarge().val[1]), contour);
            //轮廓的面积
            double area = Imgproc.contourArea(contour);


            // 求取中心点
            Moments mm = Imgproc.moments(contour);
            int center_x = (int) (mm.get_m10() / (mm.get_m00()));
            int center_y = (int) (mm.get_m01() / (mm.get_m00()));
            org.opencv.core.Point center = new org.opencv.core.Point(center_x, center_y);

            //最小外接矩形
            org.opencv.core.Rect rect = Imgproc.boundingRect(contour);
            //最小外接矩形面积
            //轮廓的面积/最小外接矩形面积(一个圆和一个圆的外接矩形)  一小于1
            if (Math.abs((area / rect.area())) < shapeDetectionInput.getMinShapeRange()) {
                continue;
            }

            double wh = rect.size().width / rect.size().height;//宽高比值

            if (wh > shapeDetectionInput.getConstraintAspectRatio() || wh < 1 / shapeDetectionInput.getConstraintAspectRatio()) {
                continue;
            }

            Mat mask = Mat.zeros(mat.size(), CvType.CV_8UC1);
            Imgproc.drawContours(mask, List.of(contour), -1, new Scalar(255), Core.FILLED);

            Mat overlapMask = new Mat(mat.size(), CvType.CV_8UC1);
            Core.bitwise_and(repeatMask, mask, overlapMask);
            double overlapArea = Core.countNonZero(overlapMask);
            if (overlapArea / Core.countNonZero(mask) > shapeDetectionInput.getMaxOverlapArea()) {
                continue;
            }

            Core.bitwise_or(repeatMask, mask, repeatMask);

            Scalar shapeColor = Core.mean(mat, mask);

            Ptr<Float> similarPtr = new Ptr<>(Float.MAX_VALUE);
            ColorType colorType = ColorType.typGetSimilarColor(shapeColor, similarPtr);

            if (similarPtr.getT() > shapeDetectionInput.getMinColorDistance()) {
                continue;
            }


            List<org.opencv.core.Point> list = contour.toList();

            list = PointUtil.removeClosePoints(list, shapeDetectionInput.getMinPosInterval());

            if (list.size() < 3) {
                continue;
            }


            List<PointLink> pointLinkList = new ArrayList<>();
            for(int i = 0; i < list.size(); i++) {
                int previous_i = (i - 1) < 0 ? list.size() - 1 : i - 1;
                int next_i = (i + 1) >= list.size() ? 0 : (i + 1);
                pointLinkList.add(PointLink.create(list.get(previous_i), list.get(i), list.get(next_i), () -> pointLinkList.get(previous_i), () -> pointLinkList.get(next_i)));
            }

            ShapeType shapeType = null;
            for(ShapeType value : ShapeType.values()) {
                if (value.recognition(mat, contour, list, pointLinkList, shapeDetectionInput, center)) {
                    shapeType = value;
                    break;
                }
            }
            if (shapeType == null) {
                continue;
            }
            resultList.add(new ShapeColorDetectionResult.ShapeDescribe(shapeType, colorType, contour));
            _contours.add(contour);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Imgproc.drawContours(outMat, _contours, -1, new Scalar(255, 0, 0), 5);
        org.opencv.android.Utils.matToBitmap(outMat, bitmap);
        ShapeColorDetectionResult.ShapeDescribe[] array = resultList.toArray(new ShapeColorDetectionResult.ShapeDescribe[0]);
        return new ShapeColorDetectionResult(array, bitmap, handleStatisticalDescription(array));

    }

    /***
     * 形状检测-异步
     */
    public CompletionStage<ShapeColorDetectionResult> shapeColorDetectionAsync(ShapeDetectionInput shapeDetectionInput) {
        return CompletableFuture.supplyAsync(() -> shapeColorDetection(shapeDetectionInput));
    }


    /***
     * 使用模型识别
     */
    public <I extends IItem> CompletionStage<Yolov8DetectResult<I>> yolov8Detect(Bitmap bitmap, IModel<I> model) {
        if (!model.isLoaded()) {
            model.loadModel();
        }

        CompletionStage<Object> completableFuture = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? CompletableFuture.completedStage(null)
                : CompletableFuture.supplyAsync(() -> null);

        //if (!model.isLoaded()) {
        //    completableFuture.thenRunAsync(model::loadModel, ContextCompat.getMainExecutor(this));
        //}

        return completableFuture
                .thenApplyAsync(_void -> model.detect(bitmap))
                .thenApplyAsync(itemCells -> {
                    Bitmap outBitmap = Bitmap.createBitmap(bitmap);

                    final Canvas canvas = new Canvas(outBitmap);
                    final Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2.0f);

                    for(IItem.ItemCell<I> itemCell : itemCells) {
                        float x = itemCell.getX();
                        float y = itemCell.getY();
                        float w = itemCell.getW();
                        float h = itemCell.getH();
                        final RectF rect = new RectF(
                                /*Math.max(0, x - w / 2),*/
                                /*Math.max(0, y - h / 2),*/
                                /*Math.min(bitmap.getWidth() - 1, x + w / 2),*/
                                /*Math.min(bitmap.getHeight() - 1, y + h / 2)*/
                                x, y, x + w, y + h
                        );
                        final RectF location = new RectF(rect);
                        canvas.drawRect(location, paint);
                    }
                    return new Yolov8DetectResult<>(itemCells, outBitmap, handleStatisticalDescription(itemCells));
                });

    }

    /***
     * 为识别结果生成描述
     */
    public static <I extends IItem> String handleStatisticalDescription(IItem.ItemCell<I>[] results) {
        if (results.length == 0) {
            return "未识别到";
        }
        Map<I, Integer> count = new HashMap<>();
        for(IItem.ItemCell<I> result : results) {
            count.put(result.getItem(), Objects.requireNonNullElse(count.get(result.getItem()), 0) + 1);
        }
        return count.entrySet().stream()
                .map(entry -> entry.getKey().getName() + ":" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public static String handleStatisticalDescription(ShapeColorDetectionResult.ShapeDescribe[] shapeDescribes) {
        if (shapeDescribes.length == 0) {
            return "未识别到";
        }
        Map<ShapeColorDetectionResult.ShapeDescribe, Integer> count = new HashMap<>();
        for(ShapeColorDetectionResult.ShapeDescribe shapeDescribe : shapeDescribes) {
            count.put(shapeDescribe, Objects.requireNonNullElse(count.get(shapeDescribe), 0) + 1);
        }
        return count.entrySet().stream()
                .map(entry -> entry.getKey().getColorType().getCnName() + "的" + entry.getKey().getShapeType().getName() + ":" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }


}
