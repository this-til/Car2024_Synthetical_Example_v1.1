package com.til.car_service.data;

import com.til.util.PointUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencv.core.Point;

import java.util.function.Supplier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointLink {
    private Point previous;
    private Point current;
    private Point next;

    private Supplier<PointLink> previousPointLink;
    private Supplier<PointLink> nextPointLink;

    private double includedAngle;
    private double previousDistance;
    private double nextDistance;

    private double previousSlope;
    private double nextSlope;

    public static PointLink create(Point previous, Point current, Point next, Supplier<PointLink> previousPointLink, Supplier<PointLink> nextPointLink) {

        return new PointLink(previous, current, next,
                previousPointLink,
                nextPointLink,
                PointUtil.calculateAngle(previous, current, next),
                PointUtil.calculateDistance(previous, current),
                PointUtil.calculateDistance(current, next),
                PointUtil.slope(current, previous),
                PointUtil.slope(current, next));

    }

}
