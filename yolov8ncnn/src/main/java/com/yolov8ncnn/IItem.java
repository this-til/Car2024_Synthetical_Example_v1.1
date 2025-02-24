package com.yolov8ncnn;

import android.graphics.RectF;
import lombok.*;
import org.jetbrains.annotations.Nullable;

public interface IItem {
    String getName();

    /***
     * 车辆类型
     */
    @Getter
    @ToString
    @AllArgsConstructor
    enum VehicleType implements IItem {
        YELLOW_TRUCK("黄色卡车"),
        YELLOW_MOTORCYCLE("黄色摩托车"),
        BICYCLE("自行车"),
        BLUE_CAR("蓝车"),
        RED_CAR("红色汽车"),
        RED_MOTORCYCLE("红色摩托车"),
        WHITE_CAR("白色汽车"),
        WHITE_TRUCK("白色卡车"),
        YELLOW_CART("黄车"),
        ;
        final String name;
    }

    /***
     * 车牌类型
     */
    @Getter
    @ToString
    @AllArgsConstructor
    enum CardType implements IItem {
        GREEN_CARD("绿牌"),
        LIGHT_BLUE_CARD("浅蓝牌"),
        BLUE_CARD("蓝牌"),
        ;
        final String name;
    }

    /***
     * 交通灯类型
     */
    @Getter
    @ToString
    @AllArgsConstructor
    enum TrafficSignType implements IItem {

        TURN_RIGHT("右转"),
        TURN_LEFT("左转"),
        TURN_ROUND("掉头"),
        TURN_STRAIGHT("直行"),
        NO_TURN_RIGHT("禁止右转"),
        NO_TURN_LEFT("禁止左转"),
        NO_TURN_ROUND("禁止掉头"),
        NO_TURN_STRAIGHT("禁止直行"),
        NO_ACCESS("禁止通行"),
        SPEED_LIMIT("限速"),
        ;
        final String name;
    }

    /***
     * 红绿灯
     */
    @Getter
    @ToString
    @AllArgsConstructor
    enum TrafficLightType implements IItem {
        RED_LIGHT("红灯"),
        GREEN_LIGHT("绿灯"),
        YELLOW_LIGHT("黄灯"),
        ;
        final String name;
    }

    /***
     * 形状和颜色
     * @noinspection NonAsciiCharacters
     */
    @Getter
    @ToString
    @AllArgsConstructor
    enum ShapeAndColorType implements IItem {
        白色三角形("白色三角形"),
        白色五角形("白色五角形"),
        白色圆形("白色圆形"),
        白色梯形("白色梯形"),
        白色矩形("白色矩形"),
        白色菱形("白色菱形"),
        紫色三角形("紫色三角形"),
        紫色五角形("紫色五角形"),
        紫色圆形("紫色圆形"),
        紫色梯形("紫色梯形"),
        紫色矩形("紫色矩形"),
        紫色菱形("紫色菱形"),
        红色三角形("红色三角形"),
        红色五角形("红色五角形"),
        红色圆形("红色圆形"),
        红色梯形("红色梯形"),
        红色矩形("红色矩形"),
        红色菱形("红色菱形"),
        绿色三角形("绿色三角形"),
        绿色五角形("绿色五角形"),
        绿色圆形("绿色圆形"),
        绿色梯形("绿色梯形"),
        绿色矩形("绿色矩形"),
        绿色菱形("绿色菱形"),
        蓝色三角形("蓝色三角形"),
        蓝色五角形("蓝色五角形"),
        蓝色圆形("蓝色圆形"),
        蓝色梯形("蓝色梯形"),
        蓝色矩形("蓝色矩形"),
        蓝色菱形("蓝色菱形"),
        青色三角形("青色三角形"),
        青色五角形("青色五角形"),
        青色圆形("青色圆形"),
        青色梯形("青色梯形"),
        青色矩形("青色矩形"),
        青色菱形("青色菱形"),
        黄色三角形("黄色三角形"),
        黄色五角形("黄色五角形"),
        黄色圆形("黄色圆形"),
        黄色梯形("黄色梯形"),
        黄色矩形("黄色矩形"),
        黄色菱形("黄色菱形"),
        黑色三角形("黑色三角形"),
        黑色五角形("黑色五角形"),
        黑色圆形("黑色圆形"),
        黑色梯形("黑色梯形"),
        黑色矩形("黑色矩形"),
        黑色菱形("黑色菱形"),

        ;
        final String name;
    }

    /***
     * 口罩识别
     */
    @Getter
    @ToString
    @AllArgsConstructor
    enum MaskType implements IItem {
        NO_MASK("无口罩"),
        WEAR_MASK("戴口罩"),
        ;
        final String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class ItemCell<I extends IItem> {
        private I item;
        private float x;
        private float y;
        private float w;
        private float h;
        private float prob;
    }


}
