package com.til.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

public class BitmapUtil {

    /**
     * 旋转Bitmap
     *
     * @param source 源Bitmap
     * @param angle  旋转角度（以度为单位），应为90的倍数（0, 90, 180, 270）
     * @return 旋转后的Bitmap
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        /*if (source == null) {
            return null;
        }

        // 创建一个Matrix对象
        Matrix matrix = new Matrix();
        // 设置旋转角度和旋转中心（这里以Bitmap的中心为旋转中心）
        matrix.postRotate(angle, source.getWidth() / 2.0f, source.getHeight() / 2.0f);

        // 创建一个RectF对象，用于计算旋转后的Bitmap边界
        RectF rectF = new RectF();
        // 将源Bitmap的边界映射到rectF中
        rectF.set(0, 0, source.getWidth(), source.getHeight());
        // 使用Matrix计算旋转后的边界
        matrix.mapRect(rectF);

        // 根据旋转后的边界创建一个新的Bitmap
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                (int) Math.ceil(rectF.width()),
                (int) Math.ceil(rectF.height()),
                source.getConfig()
        );

        // 使用Canvas将旋转后的图像绘制到新的Bitmap上
        Canvas canvas = new Canvas(rotatedBitmap);
        canvas.concat(matrix);
        canvas.drawBitmap(source, 0, 0, null);

        return rotatedBitmap;*/

        if (angle == 0) {
            return source;
        }
        Matrix m = new Matrix();
        m.setRotate(angle, (float) source.getWidth() / 2, (float) source.getHeight() / 2);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), m, true);
    }
}
