package com.til.car_service.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencv.core.Scalar;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScalarRange {
    private Scalar lower;
    private Scalar upper;
}
