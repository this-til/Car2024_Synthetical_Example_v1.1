package com.til.car_service.data;

import android.graphics.Bitmap;
import lombok.Data;
import org.opencv.core.Scalar;

@Data
public class ShapeDetectionInput {

    private Bitmap bitmap;

    /***
     * 放大比例
     */
    private Scalar enlarge = new Scalar(4, 4);

    /***
     * 最小判定范围
     */
    private float minJudgmentRange = 0.01f;

    /***
     * 最小形状范围
     * 轮廓的面积/最小外接矩形面积 必须大于该值才能判定成一个形状
     */
    private float minShapeRange = 0.1f;

    /***
     * 约束宽高比
     * 宽高比大于该值或小于(1/该值)时都不会被视作一个形状
     */
    private float constraintAspectRatio = 8f;

    /***
     * 最小形状面积
     */
    private double minShapeArea = 50;

    /***
     * 最小颜色距离(0,1)
     * 值越小表示颜色越接近
     */
    private float minColorDistance = 255f;

    /***
     * 最大重叠面积(0,1)
     */
    private float maxOverlapArea = 0.9f;

    /***
     * 二值化的底阈值
     */
    private double minThreshold = 50;

    /***
     * 二值化的高阈值
     */
    private double maxThreshold = 150;

    /***
     * 两个点之间最小的间隔
     */
    private double minPosInterval = 15;

    /***
     * 角度可容忍误差
     */
    private double angleTolerableError = 10;

    /***
     * 距离可容忍误差
     */
    private double distanceTolerableError = 30;

    /***
     * 斜率误差
     */
    private double slopeTolerableError = 30;

    public ShapeDetectionInput(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
