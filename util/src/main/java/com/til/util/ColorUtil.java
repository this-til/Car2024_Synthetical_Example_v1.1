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


}
