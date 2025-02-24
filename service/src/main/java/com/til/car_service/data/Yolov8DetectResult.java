package com.til.car_service.data;

import android.graphics.Bitmap;
import com.yolov8ncnn.IItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Yolov8DetectResult<I extends IItem> {

    private IItem.ItemCell<I>[] itemCells;
    private Bitmap outBitmap;
    private String statisticalDescription;

}
