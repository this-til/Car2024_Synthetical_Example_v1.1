package com.til.util;

import android.graphics.Bitmap;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatUtil {

    /***
     * 合并多个颜色掩膜
     */
    public static Mat combineMasks(Mat... masks) {
        Mat combined = new Mat();
        Core.bitwise_or(masks[0], masks[1], combined);
        for(int i = 2; i < masks.length; i++) {
            Core.bitwise_or(combined, masks[i], combined);
        }
        return combined;
    }


    /***
     * 综合平滑方法（推荐）
     * @param binaryMask
     * @return
     */
    public static Mat smoothEdges(Mat binaryMask) {
        return smoothEdges(binaryMask, 3, 1.5, 1.0);
    }

    /**
     * 综合平滑方法（形态学+高斯+轮廓优化）
     *
     * @param binaryMask 输入的二值化掩膜（单通道）
     * @param morphSize  形态学操作核尺寸（奇数）
     * @param sigma      高斯模糊强度
     * @param epsilon    轮廓近似精度（0~1，值越小越精细）
     */
    public static Mat smoothEdges(Mat binaryMask, int morphSize, double sigma, double epsilon) {
        Mat result = new Mat();


        // 1. 形态学平滑
        Mat morphKernel = null;


        try {

            morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
                    new Size(morphSize, morphSize));

            // 闭运算填充小孔
            Imgproc.morphologyEx(binaryMask, result, Imgproc.MORPH_CLOSE, morphKernel);

            // 开运算去除小噪点
            Imgproc.morphologyEx(result, result, Imgproc.MORPH_OPEN, morphKernel);

            // 2. 高斯模糊（保留边缘）
            Imgproc.GaussianBlur(result, result,
                    new Size(2 * Math.round(sigma) + 1, 2 * Math.round(sigma) + 1),
                    sigma);

            // 3. 轮廓近似优化
            result = refineContours(result, epsilon);

            // 4. 最终膨胀保持连续性
            Imgproc.dilate(result, result, morphKernel);


        } finally {
            if (morphKernel != null) {
                morphKernel.release();
            }
        }


        return result;
    }

    // 轮廓优化核心方法
    private static Mat refineContours(Mat mask, double epsilon) {
        Mat refined = Mat.zeros(mask.size(), CvType.CV_8UC1);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        try {
            // 查找轮廓
            Imgproc.findContours(mask, contours, hierarchy,
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            for(MatOfPoint contour : contours) {
                MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                double arcLength = Imgproc.arcLength(contour2f, true);

                // 轮廓近似
                MatOfPoint2f approx2f = new MatOfPoint2f();
                Imgproc.approxPolyDP(contour2f, approx2f, epsilon * arcLength, true);

                // 绘制平滑后的轮廓
                MatOfPoint approx = new MatOfPoint(approx2f.toArray());
                Imgproc.drawContours(refined, Collections.singletonList(approx), -1, new Scalar(255), Imgproc.COLOR_RGB2HLS_FULL);

                contour2f.release();
                approx2f.release();
                approx.release();
            }
        } finally {
            hierarchy.release();
            for(MatOfPoint c : contours) c.release();
        }
        return refined;
    }

    // 快速形态学平滑（性能优先）
    public static Mat fastMorphSmooth(Mat binaryMask) {
        return fastMorphSmooth(binaryMask, 3, 2);
    }

    /**
     * 快速形态学平滑
     *
     * @param iterations 迭代次数（通常1-3）
     */
    public static Mat fastMorphSmooth(Mat binaryMask, int kernelSize, int iterations) {
        Mat result = new Mat();

        Mat kernel = null;
        try {

            kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
                    new Size(kernelSize, kernelSize));

            // 先腐蚀后膨胀（开运算）
            Imgproc.morphologyEx(binaryMask, result, Imgproc.MORPH_OPEN, kernel,
                    new Point(-1, -1), iterations);

            // 再膨胀后腐蚀（闭运算）
            Imgproc.morphologyEx(result, result, Imgproc.MORPH_CLOSE, kernel,
                    new Point(-1, -1), iterations);
        } finally {
            if (kernel != null) {
                kernel.release();
            }
        }
        return result;
    }
    
    public Bitmap toBitmap(Mat mat) {
        
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
        
    }
    
}
