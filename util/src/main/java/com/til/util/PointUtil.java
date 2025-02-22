package com.til.util;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class PointUtil {

    /**
     * 删除列表中过于接近的点。
     *
     * @param pointList         原始点列表
     * @param distanceThreshold 点之间的最大距离阈值，如果两点之间的距离小于此值，则视为过于接近
     * @return 处理后的点列表
     */
    public static List<Point> removeClosePoints(List<Point> pointList, double distanceThreshold) {
        List<Point> resultList = new ArrayList<>(pointList); // 创建一个副本，以免修改原始列表

        for(int i = 0; i < resultList.size(); i++) {
            for(int ii = i + 1; ii < resultList.size(); ii++) {
                if (calculateDistance(resultList.get(i), resultList.get(ii)) < distanceThreshold) {
                    resultList.remove(ii);
                    ii--;
                }
            }
        }

        return resultList;
    }
    
    /***
     * 求两点之间的距离
     */
    public static double calculateDistance(Point p1, Point p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /***
     * 求三点组成角的角度
     */
    public static double calculateAngle(org.opencv.core.Point previous, org.opencv.core.Point current, org.opencv.core.Point next) {
        double angle = Math.acos(((previous.x - current.x) * (next.x - current.x) + (previous.y - current.y) * (next.y - current.y)) /
                (Math.sqrt(Math.pow(previous.x - current.x, 2) + Math.pow(previous.y - current.y, 2)) *
                        Math.sqrt(Math.pow(next.x - current.x, 2) + Math.pow(next.y - current.y, 2))));
        return Math.toDegrees(angle);
    }

    /***
     * 求两点组成直线的斜率
     */
    public static double slope(org.opencv.core.Point a, org.opencv.core.Point b) {
        double tag = (b.x - a.x);
        if (tag == 0) {
            return Double.MIN_VALUE;
        }
        return (b.y - a.y) / tag;
    }
    
    
}
