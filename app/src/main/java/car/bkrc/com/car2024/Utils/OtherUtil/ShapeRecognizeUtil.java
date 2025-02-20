package car.bkrc.com.car2024.Utils.OtherUtil;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.cvtColor;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：OpenCV
 */
public class ShapeRecognizeUtil {

    private static final String TAG = "ShapeRecognizeUtil";
    Mat mat = new Mat();
    Mat rgbmat = new Mat();
    Mat closed = new Mat();
    List<Shape> shapeList = new ArrayList<>();

    public ShapeRecognizeUtil(Bitmap resource) {
        Utils.bitmapToMat(resource, rgbmat, true);
        cvtColor(rgbmat, mat, COLOR_BGR2RGB); // 颜色空间转换，需根据实际需要进行调整
        Log.e(TAG, "ShapeRecognizeUtil: " + mat.width() + " " + mat.height());
    }

    public Mat getClosed() {
        return closed;
    }

    public List<Shape> getShapeList() {
        return shapeList;
    }

    public void imgCapture() {
        // 将原图像灰度化
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        // 平均滤波
        Mat blur = new Mat();
        Imgproc.blur(gray, blur, new Size(5, 5));
        // 简单阈值的二值化
        Mat thresh = new Mat();
        Imgproc.threshold(blur, thresh, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // 提取最大外接矩形进行裁切
        double area_max = 0;
        MatOfPoint _cnt = null;
        for (MatOfPoint cnt : contours) {   // 循环提取轮廓
            double area = Imgproc.contourArea(cnt); // 计算轮廓面积后带入计算
            if (area > area_max) {  // 计算最大轮廓
                area_max = area;
                _cnt = cnt;
            }
        }
        if (_cnt == null)
            return;
        Rect rect = Imgproc.boundingRect(_cnt); // 计算点集最外面的矩形边界
        mat = new Mat(mat, rect);  // 利用计算出的数据重新裁剪图片生成新图
    }

    public Mat cv2ImgAddRect() {
        Mat mat1 = mat.clone();
        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_BGR2RGB);
        for (Shape shape : shapeList) {
            Log.e(TAG, "cv2ImgAddRect: " + shape.x + " " + shape.y + " " + shape.w + " " + shape.h);
            if (shape.name != null) {
                Imgproc.rectangle(mat1, new Point(shape.x, shape.y),
                        new Point(shape.x + shape.w, shape.y + shape.h),
                        new Scalar(225, 0, 0), 1);
            }
        }
        return mat1;
    }

    public static class Shape {

        public String name;
        public String shape;
        int x;
        int y;
        public int w;
        public int h;

        Shape(String name, String shape, int x, int y, int w, int h) {
            this.name = name;
            this.shape = shape;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public String toString() {
            return "Shape{" +
                    "name='" + name + '\'' +
                    ", shape='" + shape + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", w=" + w +
                    ", h=" + h +
                    '}';
        }
    }
}
