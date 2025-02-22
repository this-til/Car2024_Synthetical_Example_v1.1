package com.til.car_service.data;

import com.til.util.PointUtil;
import lombok.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public enum ShapeType {
    square("正方形") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            if (pointList.size() != 4) {
                return false;
            }
            for(PointLink pointLink : pointLinkList) {
                if (Math.abs(pointLink.getIncludedAngle() - 90) > shapeDetectionInput.getAngleTolerableError()) {
                    return false;
                }
                if (Math.abs(pointLink.getPreviousDistance() - pointLink.getNextDistance()) > shapeDetectionInput.getDistanceTolerableError()) {
                    return false;
                }
            }
            return true;
        }
    },
    rectangle("长方形") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            if (pointList.size() != 4) {
                return false;
            }
            for(PointLink pointLink : pointLinkList) {
                if (Math.abs(pointLink.getIncludedAngle() - 90) > shapeDetectionInput.getAngleTolerableError()) {
                    return false;
                }
            }
            return true;
        }
    },
    trapezoid("梯形") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            if (pointList.size() != 4) {
                return false;
            }

            return Math.abs(pointLinkList.get(0).getPreviousSlope() - pointLinkList.get(2).getPreviousSlope()) > shapeDetectionInput.getSlopeTolerableError()
                    || Math.abs(pointLinkList.get(0).getNextSlope() - pointLinkList.get(2).getNextSlope()) > shapeDetectionInput.getSlopeTolerableError();
        }
    }, rhombus("菱形") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            if (pointList.size() != 4) {
                return false;
            }
            for(PointLink pointLink : pointLinkList) {
                if (Math.abs(pointLink.getPreviousDistance() - pointLink.getNextDistance()) > shapeDetectionInput.getDistanceTolerableError()) {
                    return false;
                }
            }
            return true;
        }
    },
    rhomboid("平行四边形") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            if (pointList.size() != 4) {
                return false;
            }
            if (Math.abs(pointLinkList.get(0).getIncludedAngle() - pointLinkList.get(2).getIncludedAngle()) > shapeDetectionInput.getAngleTolerableError()) {
                return false;
            }
            if (Math.abs(pointLinkList.get(1).getIncludedAngle() - pointLinkList.get(3).getIncludedAngle()) > shapeDetectionInput.getAngleTolerableError()) {
                return false;
            }
            return true;
        }
    },
    round("圆形") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            if (pointList.size() < 8) {
                return false;
            }
            double tag = 0;
            for(int i = 0; i < pointList.size(); i++) {
                if (i == 0) {
                    tag = PointUtil.calculateDistance(pointList.get(i), center);
                    continue;
                }
                double _tag = PointUtil.calculateDistance(pointList.get(i), center);
                if (Math.abs(tag - _tag) > shapeDetectionInput.getDistanceTolerableError()) {
                    return false;
                }
                tag = (tag + _tag) / 2;
            }
            return true;
        }
    },
    triangle("三角形") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            return pointList.size() == 3;
        }
    },
    fivePointedStar("五角星") {
        @Override
        public boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center) {
            return pointList.size() >= 10;
        }
    },
    ;
    private String name;

    public abstract boolean recognition(Mat mat, MatOfPoint matOfPoint, List<Point> pointList, List<PointLink> pointLinkList, ShapeDetectionInput shapeDetectionInput, Point center);
}
