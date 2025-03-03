package com.til.car_service.data;

import androidx.annotation.Nullable;
import com.til.util.ColorUtil;
import com.til.util.tuple.Ptr;
import lombok.Getter;
import lombok.ToString;
import org.opencv.core.Scalar;

@Getter
@ToString
public enum ColorType {
    red(
            new Scalar(255, 0, 0),
            new ScalarRange(new Scalar(141, 43, 46), new Scalar(180, 255, 255)),
            "红色"
    ),
    green(
            new Scalar(0, 255, 0),
            new ScalarRange(new Scalar(57, 77, 18), new Scalar(79, 255, 255)),
            "绿色"
    ),
    blue(
            new Scalar(0, 0, 255),
            new ScalarRange(new Scalar(97, 196, 200), new Scalar(127, 255, 255)),
            "蓝色"
    ),
    yellow(
            new Scalar(0, 255, 255),
            new ScalarRange(new Scalar(12, 85, 104), new Scalar(44, 255, 255)),
            "黄色"
    ),
    magenta(
            new Scalar(255, 0, 255),
            new ScalarRange(new Scalar(110, 50, 50), new Scalar(130, 255, 255)),
            "品色"
    ),
    cyan(
            new Scalar(255, 255, 0),
            new ScalarRange(new Scalar(77, 103, 150), new Scalar(96, 255, 255)),
            "青色"
    ),
    black(
            new Scalar(0, 0, 0),
            new ScalarRange(new Scalar(0, 0, 0), new Scalar(255, 255, 20)),
            "黑色"
    ),
    white(
            new Scalar(255, 255, 255),
            new ScalarRange(new Scalar(0, 0, 200), new Scalar(255, 255, 255)),
            "白色"
    ),
    ;

    ColorType(Scalar rgbColor, ScalarRange hsvRange, String cnName) {
        this.rgbColor = rgbColor;
        this.bgrColor = new Scalar(rgbColor.val[2], rgbColor.val[1], rgbColor.val[0]);
        this.hsvRange = hsvRange;
        this.cnName = cnName;
    }

    private final Scalar bgrColor;
    private final Scalar rgbColor;
    private final ScalarRange hsvRange;
    private final String cnName;

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
