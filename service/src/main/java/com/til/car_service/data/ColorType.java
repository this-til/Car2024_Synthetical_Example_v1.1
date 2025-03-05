package com.til.car_service.data;

import androidx.annotation.Nullable;
import com.til.util.ColorUtil;
import com.til.util.tuple.Ptr;
import lombok.Getter;
import lombok.ToString;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

@Getter
@ToString
public enum ColorType {
    red(
            new Scalar(255, 0, 0),
            "红色"
    ),
    green(
            new Scalar(0, 255, 0),
            "绿色"
    ),
    blue(
            new Scalar(0, 0, 255),
            "蓝色"
    ),
    yellow(
            new Scalar(0, 255, 255),
            "黄色"
    ),
    magenta(
            new Scalar(255, 0, 255),
            "品色"
    ),
    cyan(
            new Scalar(255, 255, 0),
            "青色"
    ),
    black(
            new Scalar(0, 0, 0),
            "黑色"
    ),
    white(
            new Scalar(255, 255, 255),
            "白色"
    ),
    ;

    private final Scalar bgrColor;
    private final Scalar rgbColor;
    private final String cnName;
    private final Scalar hsvColor;

    
    ColorType(Scalar rgbColor, String cnName) {
        this.rgbColor = rgbColor;
        this.bgrColor = new Scalar(rgbColor.val[2], rgbColor.val[1], rgbColor.val[0]);
        this.cnName = cnName;
        this.hsvColor = ColorUtil.rgbToHsv(rgbColor);
    }

    // 新增方法：生成二值化掩膜
    public Mat getBinaryMask(Mat hsv) {
        Mat mask = new Mat();

        if (this == black || this == white) {
            handleAchromaticColors(hsv, mask);
        } else {
            handleChromatedColors(hsv, mask);
        }
        
        return mask;
    }

    private void handleChromatedColors(Mat hsv, Mat mask) {
        // 色相容差 (±15)，饱和度和明度下限
        double hueTolerance = 15;
        Scalar lower1, upper1, lower2 = null, upper2 = null;

        // 计算基础范围
        double baseHue = hsvColor.val[0];
        double lowerH = baseHue - hueTolerance;
        double upperH = baseHue + hueTolerance;

        // 处理红色色相环绕
        if (this == red) {
            lower1 = new Scalar(0, 50, 50);
            upper1 = new Scalar(15, 255, 255);
            lower2 = new Scalar(165, 50, 50);
            upper2 = new Scalar(180, 255, 255);
        }
        // 处理其他彩色
        else {
            if (lowerH < 0) {
                lower1 = new Scalar(lowerH + 180, 50, 50);
                upper1 = new Scalar(180, 255, 255);
                lower2 = new Scalar(0, 50, 50);
                upper2 = new Scalar(upperH, 255, 255);
            } else if (upperH > 180) {
                lower1 = new Scalar(lowerH, 50, 50);
                upper1 = new Scalar(180, 255, 255);
                lower2 = new Scalar(0, 50, 50);
                upper2 = new Scalar(upperH - 180, 255, 255);
            } else {
                lower1 = new Scalar(lowerH, 50, 50);
                upper1 = new Scalar(upperH, 255, 255);
            }
        }

        // 生成掩膜
        if (lower2 != null) {
            Mat mask1 = new Mat(), mask2 = new Mat();
            Core.inRange(hsv, lower1, upper1, mask1);
            Core.inRange(hsv, lower2, upper2, mask2);
            Core.bitwise_or(mask1, mask2, mask);
            mask1.release();
            mask2.release();
        } else {
            Core.inRange(hsv, lower1, upper1, mask);
        }
    }

    private void handleAchromaticColors(Mat hsv, Mat mask) {
        if (this == black) {
            Core.inRange(hsv, new Scalar(0, 0, 0), new Scalar(180, 255, 50), mask);
        } else if (this == white) {
            Core.inRange(hsv, new Scalar(0, 0, 200), new Scalar(180, 50, 255), mask);
        }
    }

    public static ColorType typGetSimilarColor(Scalar color, @Nullable Ptr<Float> similarPtr) {
        ColorType similarColor = null;
        float similar = Float.MAX_VALUE;
        for(ColorType value : values()) {
            float v = ColorUtil.calculateColorDistance(value.rgbColor, color);
            if (v < similar) {
                similar = v;
                similarColor = value;
            }
        }
        if (similarPtr != null) {
            similarPtr.setT(similar);
        }
        return similarColor;
    }
}
