package com.til.util;

import android.graphics.Color;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class ColorUtil {

    public static void colorToGrayscaleOptimized(Mat image, Scalar targetColor, Mat out) {

        // 转换到HSV空间
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        // 将目标颜色转换为HSV空间
       /* Scalar targetHsv = new Scalar(
                targetColor.val[0] * 179.0 / 255.0, // Hue
                targetColor.val[1] * 255.0 / 255.0, // Saturation
                targetColor.val[2] * 255.0 / 255.0  // Value
        );*/

        Mat hsvDiff = new Mat();
        Core.absdiff(hsvImage, targetColor, hsvDiff);

        Mat h = new Mat();
        Mat s = new Mat();
        Mat v = new Mat();
        Core.extractChannel(hsvDiff, h, 0);
        Core.extractChannel(hsvDiff, s, 0);
        Core.extractChannel(hsvDiff, v, 0);

        float ta = 1f / 3f;
        Core.addWeighted(h, ta, s, ta, 0.0, out);
        Core.addWeighted(out, ta * 2, v, ta, 0.0, out);

        // 将差异值反转，使其更接近目标颜色的像素具有更大的灰度值
        Core.bitwise_not(out, out);

        // 归一化差异值到0-255范围，以得到灰度图像
        //out.convertTo(out, CvType.CV_8UC1, 255);
    }

    /***
     * 判断两个颜色相近程度
     * 返回值越小表示颜色越接近
     */
    public static float calculateColorDistance(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);

        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);

        // 计算欧几里得距离

        return (float) Math.sqrt(
                Math.pow(r1 - r2, 2) +
                        Math.pow(g1 - g2, 2) +
                        Math.pow(b1 - b2, 2)
        );
    }

    /***
     * 判断两个颜色相近程度
     * 返回值越小表示颜色越接近
     */
    public static float calculateColorDistance(Scalar color1, Scalar color2) {
        double r1 = color1.val[2];
        double g1 = color1.val[1];
        double b1 = color1.val[0];

        double r2 = color2.val[2];
        double g2 = color2.val[1];
        double b2 = color2.val[0];

        return (float) Math.sqrt(
                Math.pow(r1 - r2, 2) +
                        Math.pow(g1 - g2, 2) +
                        Math.pow(b1 - b2, 2)
        );
    }

    public static Scalar calculateAverageColor(Mat mat) {
        return Core.mean(mat);
    }

    /***
     * RGB转HSV工具方法
     */
    public static Scalar rgbToHsv(Scalar rgb) {
        Mat rgbMat = new Mat(1, 1, CvType.CV_8UC3);
        rgbMat.put(0, 0, rgb.val[0], rgb.val[1], rgb.val[2]);
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_RGB2HSV);
        double[] hsv = hsvMat.get(0, 0);
        rgbMat.release();
        hsvMat.release();
        return new Scalar(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * 将BGR格式的Scalar转换为ARGB整型颜色
     * @param bgrScalar OpenCV标准的BGR三通道颜色（值范围0-255）
     * @return ARGB8888格式的颜色整数
     */
    public static int bgrScalarToArgb(Scalar bgrScalar) {
        int blue = clamp((int) bgrScalar.val[0]);
        int green = clamp((int) bgrScalar.val[1]);
        int red = clamp((int) bgrScalar.val[2]);
        return 0xFF << 24 | (red << 16) | (green << 8) | blue;
    }

    /**
     * 将RGB格式的Scalar转换为ARGB整型颜色
     * @param rgbScalar RGB三通道颜色（值范围0-255）
     * @return ARGB8888格式的颜色整数
     */
    public static int rgbScalarToArgb(Scalar rgbScalar) {
        int red = clamp((int) rgbScalar.val[0]);
        int green = clamp((int) rgbScalar.val[1]);
        int blue = clamp((int) rgbScalar.val[2]);
        return 0xFF << 24 | (red << 16) | (green << 8) | blue;
    }

    /**
     * 带透明度的转换（RGBA转ARGB）
     * @param rgbaScalar 四通道颜色（R, G, B, A）
     * @return ARGB8888格式的颜色整数
     */
    public static int rgbaScalarToArgb(Scalar rgbaScalar) {
        int red = clamp((int) rgbaScalar.val[0]);
        int green = clamp((int) rgbaScalar.val[1]);
        int blue = clamp((int) rgbaScalar.val[2]);
        int alpha = clamp((int) rgbaScalar.val[3]);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    // 确保颜色值在0-255范围内
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * 从ARGB整型颜色转换回BGR格式Scalar
     * @param argbColor ARGB8888格式的颜色整数
     * @return OpenCV标准的BGR三通道Scalar
     */
    public static Scalar argbToBgrScalar(int argbColor) {
        int red = (argbColor >> 16) & 0xFF;
        int green = (argbColor >> 8) & 0xFF;
        int blue = argbColor & 0xFF;
        return new Scalar(blue, green, red);
    }
}
