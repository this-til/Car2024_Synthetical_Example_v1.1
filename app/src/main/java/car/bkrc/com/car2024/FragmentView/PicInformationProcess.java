package car.bkrc.com.car2024.FragmentView;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult;

import com.til.car_service.Service;
import com.til.car_service.data.OcrInput;
import com.til.car_service.data.ShapeDetectionInput;
import com.til.util.CharactersUtil;
import com.yolov8ncnn.IModel;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionStage;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.BitmapUtils.Full_screen;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.OtherUtil.ShapeRecognizeUtil;

/**
 * 图像处理控制页面
 */
public class PicInformationProcess extends Fragment {


    // 识别结果文本
    private TextView picrectext_tv;
    // 识别结果图像
    private ImageView picrec_iv;

    private Service service;

    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Service.ServiceBinder binder = (Service.ServiceBinder) service;
            PicInformationProcess.this.service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };

    /**
     * 界面控件初始化
     *
     * @param view
     */
    @SuppressLint("SetTextI18n")
    private void initView(View view) {
        view.findViewById(R.id.landmark_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {

                        picrectext_tv.setText("正在识别标识物...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    //TODO
                    return picBitmap;
                })));
        view.findViewById(R.id.mask_all_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别人脸佩戴口罩...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenComposeAsync(picBitmap -> service.yolov8DetectAsync(picBitmap, IModel.MASK_MODEL))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getStatisticalDescription());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.qr_all_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别二维码...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.qrRecognitionAsync(picBitmap))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getTotal());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));        
        view.findViewById(R.id.qr_overlap_all_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别二维码(重叠)...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.qrColorRecognitionAsync(picBitmap))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getTotal());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.carplate_all_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别车牌号...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.carTesseractAsync(picBitmap))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getTotal());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));   
        view.findViewById(R.id.carplate_all_btn_yolo).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别车牌号(yolo)...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.yolov8DetectAsync(picBitmap, IModel.CARD_MODEL))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getStatisticalDescription());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.carplate_all_btn_enhance).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别车牌号(增强)...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.licensePlatesRecognitionEnhancement(picBitmap))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getTotal());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.ocrrec_all_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在进行ocr识别...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.ocrAsync(new OcrInput(picBitmap)))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                    picrectext_tv.setText(CharactersUtil.removeSpecialCharactersExceptChinese(result.getOcrResult().getStrRes()));
                }))));
        view.findViewById(R.id.tracfficrec_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别交通灯颜色...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.trafficLightCheckAsync(picBitmap))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrec_iv.setImageBitmap(result.getOutImage());
                    picrectext_tv.setText(result.getTotal());
                }))));
        view.findViewById(R.id.tracfficrec_btn_yolo).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别交通灯颜色...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.yolov8DetectAsync(picBitmap, IModel.TRAFFIC_LIGHT_MODEL))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getStatisticalDescription());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.cartype_all_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别车辆类型...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenCompose(picBitmap -> service.yolov8DetectAsync(picBitmap, IModel.VEHICLE_MODEL))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getStatisticalDescription());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.tracfficsign_all_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别交通识别标志物...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenComposeAsync(picBitmap -> service.yolov8DetectAsync(picBitmap, IModel.TRAFFIC_SIGN_MODEL))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getStatisticalDescription());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.shape_color_detection).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别形状和颜色...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenComposeAsync(picBitmap -> service.shapeColorDetectionAsync(new ShapeDetectionInput(picBitmap)))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getTotal());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.shape_color_detection_yolo).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别形状和颜色...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenComposeAsync(picBitmap -> service.yolov8DetectAsync(picBitmap, IModel.SHAPE_AND_COLOR_MODEL))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> {
                    picrectext_tv.setText(result.getStatisticalDescription());
                    picrec_iv.setImageBitmap(result.getOutBitmap());
                }))));
        view.findViewById(R.id.graphic_color_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("正在识别图形颜色...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    //TODO
                    return picBitmap;
                })));
        view.findViewById(R.id.graphic_shape_btn).setOnClickListener(v -> addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("识别图形形状...");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    //TODO
                    return picBitmap;
                })));
        view.findViewById(R.id.opencv_shape_btn).setOnClickListener(v -> addExceptionally(addExceptionally(LeftFragment.INSTANCE.getHDBitmapAsync()
                .thenApplyAsync(picBitmap -> {
                    requireActivity().runOnUiThread(() -> {
                        picrectext_tv.setText("边缘检测");
                        picrec_iv.setImageBitmap(picBitmap);
                    });
                    return picBitmap;
                })
                .thenComposeAsync(picBitmap -> service.findCornerAsync(picBitmap))
                .thenAcceptAsync(result -> requireActivity().runOnUiThread(() -> picrec_iv.setImageBitmap(result.getOutBitmap()))))));
        view.findViewById(R.id.vga_qr_btn).setOnClickListener(v -> FirstActivity.Connect_Transport.qr_rec(1));
        view.findViewById(R.id.refresh_btn).setOnClickListener(v -> {
            picrectext_tv.setText("结果仅供参考！");
            picrec_iv.setImageBitmap(null);
        });
        view.findViewById(R.id.back_imbtn).setOnClickListener(v -> {
            if (((BitmapDrawable) picrec_iv.getDrawable()).getBitmap() != null) {
                Full_screen.bigImageLoader(this.getContext(), ((BitmapDrawable) picrec_iv.getDrawable()).getBitmap());
            }
        });
        picrectext_tv = view.findViewById(R.id.picrectext_tv);
        picrec_iv = view.findViewById(R.id.picrec_iv);
        //picrec_iv.setOnClickListener(this);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picinformation_fragment, container, false);
        initView(view);
        // 获取摄像头状态数据
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(requireActivity(), Service.class);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    private CompletionStage<?> addExceptionally(CompletionStage<?> completionStage) {
        return completionStage
                .thenRun(() -> vSimple(getContext(), 10))
                .exceptionally(e -> {
                    Log.e("Recognize", "recognition failed:", e);
                    requireActivity().runOnUiThread(() -> picrectext_tv.setText("出现错误：" + e.getMessage()));
                    return null;
                });
    }


    /**
     * 退出到当前Fragment
     */
    private void exitFragment() {
        Fragment current = getParentFragmentManager().findFragmentById(R.id.safety_fragment);
        getParentFragmentManager().beginTransaction().setCustomAnimations(R.anim.across_translate_into, R.anim.across_translate_out).remove(current).commit();
    }

    /**
     * 控制手机震动
     *
     * @param context     上下文
     * @param millisecond 震动时间，毫秒为单位
     */
    public static void vSimple(Context context, int millisecond) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(millisecond);
        } catch (Exception ignored) {

        }
    }

    /**
     * 查找最大轮廓
     *
     * @param bitmap
     * @return
     */
    @SuppressLint("SuspiciousIndentation")
    private void recognitionShae(Bitmap bitmap) {
        Mat mat;
        ShapeRecognizeUtil sru = new ShapeRecognizeUtil(bitmap);
        sru.imgCapture();  // 计算途中包含的形状及最大轮廓
        mat = sru.cv2ImgAddRect(); // 将计算后的图形输出
        Bitmap showbitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);  // 生成新bitmap图像
        Utils.matToBitmap(mat, showbitmap);
        StringBuilder stringBuilder = new StringBuilder();
        for(ShapeRecognizeUtil.Shape shape : sru.getShapeList()) {
            stringBuilder.append(shape.name + shape.shape + " " + shape.w + " " + shape.h + "\n");
        }
        picrec_iv.setImageBitmap(showbitmap);
    }

    private Mat mSource;

    /**
     * canny算法，边缘检测
     */
    public Mat canny(Bitmap bitmap) {
        mSource = new Mat();
        Utils.bitmapToMat(bitmap, mSource);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(mSource, grayMat, Imgproc.COLOR_BGR2GRAY);//转换成灰度图
        Mat mat = mSource.clone();
        Imgproc.Canny(mSource, mat, 50, 200);
        return mat;
    }

    /**
     * 返回边缘检测之后的最大矩形,并返回
     *
     * @param cannyMat Canny之后的mat矩阵
     * @return
     */
    public Rect findMaxRect(Mat cannyMat) {
        Mat tmp = mSource.clone();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        // 寻找轮廓
        Imgproc.findContours(cannyMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        int index = 0;
        double perimeter = 0;
        // 找出匹配到的最大轮廓
        for(int i = 0; i < contours.size(); i++) {
            // 最大面积,进行轮廓准确提取后，判断面积是较为理想的方案
            // double area = Imgproc.contourArea(contours.get(i));
            //最大周长
            MatOfPoint2f source = new MatOfPoint2f();
            source.fromList(contours.get(i).toList());
            // 检查轮廓是否闭合
            if (!contours.isEmpty() && !contours.get(0).equals(contours.get(contours.size() - 1))) {
                // 如果轮廓不是闭合的，将其闭合
                contours.add(contours.get(0)); // 将第一个点复制到末尾
            }
            double area = Imgproc.contourArea(contours.get(i));
            double length = Imgproc.arcLength(source, true);
            if (area > perimeter) {
                perimeter = area;
                index = i;
            }
        }

        /**
         * 参数一：image，待绘制轮廓的图像。
         *
         * 参数二：contours，待绘制的轮廓集合。
         *
         * 参数三：contourIdx，要绘制的轮廓在contours中的索引，若为负数，表示绘制全部轮廓。
         *
         * 参数四：color，绘制轮廓的颜色。
         *
         * 参数五：thickness，绘制轮廓的线条粗细。若为负数，那么绘制轮廓的内部。
         *
         * 参数六：lineType，线条类型。FILLED   LINE_4   4连通   LINE_8   8连通  LINE_AA  抗锯齿
         */
        Imgproc.drawContours(
                tmp,
                contours,
                index,
                new Scalar(0.0, 0.0, 255.0),
                9,
                Imgproc.LINE_8

        );

        Rect rect = Imgproc.boundingRect(contours.get(index));
        // 在原图上绘制边界矩形
        Imgproc.rectangle(tmp,
                new Point(rect.x, rect.y), // 矩形左上角的点
                new Point(rect.x + rect.width, rect.y + rect.height), // 矩形右下角的点
                new Scalar(0, 0, 255), // 红色
                2, // 线条粗细
                Imgproc.LINE_AA); // 抗锯齿线条

        // 根据边界矩形裁剪图像
        Mat croppedMat = new Mat(tmp, rect); // 裁剪图像
        showImg(croppedMat);
        return rect;
    }

    /**
     * 显示图像
     *
     * @param mat
     */
    private void showImg(Mat mat) {

        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        picrec_iv.setImageBitmap(bitmap);
        mat.release();
    }

    public Mat scanDocument(Bitmap bitmap) {
        // 加载图片
        mSource = new Mat();
        Utils.bitmapToMat(bitmap, mSource);

        // 灰度化
        Mat gray = new Mat();
        Imgproc.cvtColor(mSource, gray, Imgproc.COLOR_BGR2GRAY);

        // 高斯模糊去噪
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Canny边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, 50, 200, 3);

        // 查找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 对找到的轮廓进行多边形拟合，并寻找最大的四边形
        double maxArea = 0;
        MatOfPoint2f biggest = new MatOfPoint2f();
        for(int i = 0; i < contours.size(); i++) {
            if (!contours.get(0).equals(contours.get(contours.size() - 1))) {
                // 如果轮廓不是闭合的，将其闭合
                contours.add(contours.get(0)); // 将第一个点复制到末尾
            }
            MatOfPoint contour = new MatOfPoint();
            contour.fromList(contours.get(i).toList());
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea > maxArea) {
                // 多边形拟合
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(contour2f, approx, 0.02 * Imgproc.arcLength(contour2f, true), true);
                if (approx.total() == 4) {
                    biggest = approx;
                    maxArea = contourArea;
                }
            }
        }
        // 使用透视变换来纠正图像
        Mat result = new Mat();
        if (biggest.total() == 4) {
            result = fourPointTransform(mSource, biggest);
        }

        return result;
    }

    private Mat fourPointTransform(Mat src, MatOfPoint2f pts) {
        Point[] ptsArray = pts.toArray();

        // 计算质心
        Point center = new Point(0, 0);
        for(Point pt : ptsArray) {
            center.x += pt.x;
            center.y += pt.y;
        }
        center.x /= ptsArray.length;
        center.y /= ptsArray.length;

        // 根据点到中心的相对位置排序点
        Point[] sortedPoints = new Point[4];
        List<Point> leftPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();

        for(Point pt : ptsArray) {
            if (pt.x < center.x) {
                leftPoints.add(pt);
            } else {
                rightPoints.add(pt);
            }
        }

        Comparator<Point> sortByY = Comparator.comparingDouble(p -> p.y);
        sortedPoints[0] = Collections.min(leftPoints, sortByY);  // 左上
        sortedPoints[3] = Collections.max(leftPoints, sortByY);  // 左下
        sortedPoints[1] = Collections.min(rightPoints, sortByY); // 右上
        sortedPoints[2] = Collections.max(rightPoints, sortByY); // 右下

        // 创建目标点，和源图像相对应
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(src.width() - 1, 0),
                new Point(src.width() - 1, src.height() - 1),
                new Point(0, src.height() - 1)
        );

        // 计算透视变换矩阵
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(new MatOfPoint2f(sortedPoints), dst);

        // 应用透视变换
        Mat transformed = new Mat();
        Imgproc.warpPerspective(src, transformed, perspectiveTransform, new Size(src.width(), src.height()));

        return transformed;
    }

    MLDocumentSkewCorrectionAnalyzerSetting setting = new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create();
    MLDocumentSkewCorrectionAnalyzer analyzer = MLDocumentSkewCorrectionAnalyzerFactory.getInstance().getDocumentSkewCorrectionAnalyzer(setting);

    private void findCorner(Bitmap bitmap) {
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        // asyncDocumentSkewDetect异步调用。
        Task<MLDocumentSkewDetectResult> detectTask = analyzer.asyncDocumentSkewDetect(frame);
        detectTask.addOnSuccessListener(detectResult -> {
            // 检测成功。
            android.graphics.Point leftTop = detectResult.getLeftTopPosition();
            android.graphics.Point rightTop = detectResult.getRightTopPosition();
            android.graphics.Point leftBottom = detectResult.getLeftBottomPosition();
            android.graphics.Point rightBottom = detectResult.getRightBottomPosition();
            List<android.graphics.Point> coordinates = new ArrayList<>();
            coordinates.add(leftTop);
            coordinates.add(rightTop);
            coordinates.add(rightBottom);
            coordinates.add(leftBottom);
            appliance(frame, new MLDocumentSkewCorrectionCoordinateInput(coordinates));
        }).addOnFailureListener(e -> {
            // 检测失败。
        });
    }

    private void appliance(MLFrame frame, MLDocumentSkewCorrectionCoordinateInput coordinateData) {
        // asyncDocumentSkewCorrect异步调用。
        try {
            Task<MLDocumentSkewCorrectionResult> correctionTask = analyzer.asyncDocumentSkewCorrect(frame, coordinateData);
            correctionTask.addOnSuccessListener(refineResult -> {
                // 检测成功。
                picrec_iv.setImageBitmap(refineResult.getCorrected());
            }).addOnFailureListener(e -> {
                // 检测失败。
            });
        } catch (Exception ignored) {
        }

    }
}
