package car.bkrc.com.car2024.Utils.PicDisposeUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.Utils.dialog.RecDialog;

public class TrafficUtils {

    // 霍夫圆检测
    public static void HoughCircleCheck(Bitmap bitmap, Context context,int id, TextView textView, ImageView imageView) {
        int red = 0,yellow = 0, green = 0;
        // 进行霍夫圆检测
        Mat grayImage = new Mat();
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        // 将图片转换为单通道GRAY
        Imgproc.cvtColor(mat, grayImage, Imgproc.COLOR_BGR2GRAY);
        Mat circles = new Mat();
        /* 霍夫圆检测 Imgproc.HoughCircles()
        * grayImage：输入图像，必须是单通道的灰度图像。
        * circles：输出参数，用于存储检测到的圆的结果。它是一个Mat类型的变量，每一行包含了一个检测到的圆的信息，包括圆心的坐标和半径。
        * method：霍夫圆检测的方法。在OpenCV中，只提供了一种方法，即Imgproc.CV_HOUGH_GRADIENT。它基于梯度信息来进行圆检测。
        * dp：累加器分辨率与图像分辨率的比值。默认值为1，表示两者相等。较小的值可以提高检测的精度，但会增加计算量。
        * minDist：检测到的圆之间的最小距离。如果设置为太小的值，可能会导致检测到重复的圆。如果设置为太大的值，可能会错过一些圆。
        * param1：边缘检测阈值。边缘像素的梯度值高于该阈值才会被认为是有效的边缘。较大的值可以过滤掉较弱的边缘，较小的值可以检测到更多的圆。
        * param2：圆心累加器阈值。检测到的圆心区域的累加器值高于该阈值才会被认为是有效的圆心。较大的值可以过滤掉较弱的圆，较小的值可以检测到更多的圆。
        * minRadius：圆的最小半径。如果设置为0，则没有最小半径限制。
        * maxRadius：圆的最大半径。如果设置为0，则没有最大半径限制。
        */
        Imgproc.HoughCircles(grayImage, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 20, 100, 20, 35);
        // 创建掩膜图像
        Mat mask = Mat.zeros(mat.size(), CvType.CV_8U);
        // 绘制检测到的圆形区域到掩膜图像上
        for (int i = 0; i < circles.cols(); i++) {
            double[] circleData = circles.get(0, i);
            Point center = new Point(circleData[0], circleData[1]);
            int radius = (int) circleData[2];
            Imgproc.circle(mask, center, radius, new Scalar(255), -1);
            // 提取圆形区域的颜色
            Rect roi = new Rect((int)(center.x - radius), (int)(center.y - radius), radius * 2, radius * 2);
            Mat roiImage = new Mat(mat, roi);
            Scalar meanColor = Core.mean(roiImage);
            if (meanColor.val[0] > 180 && meanColor.val[1] < 180 && meanColor.val[2] < 180 ){
                red++;
            } else if (meanColor.val[0] > 180 && meanColor.val[1] > 180 && meanColor.val[2] < 180 ) {
                yellow++;
            } else if (meanColor.val[0] < 180 && meanColor.val[1] > 180 && meanColor.val[2] > 180 ) {
               green++;
            }
            Log.d("color", meanColor.val[0]+" "+meanColor.val[1]+" "+meanColor.val[2]);
            // 在圆的中心位置绘制颜色标记
            Imgproc.circle(mat, center, 50, meanColor, -1);
        }
        String circleColor = "交通信号灯识别结果：";
        if (red > yellow && red > green){
            circleColor = "交通信号灯识别结果：红色";
        }else if(yellow > red && yellow > green){
            circleColor = "交通信号灯识别结果：黄色";
        }else if(green > red && green > yellow) {
            circleColor = "交通信号灯识别结果：绿色";
        }else {
            circleColor = "未识别到交通灯";
        }
        // 将原始图像与掩膜图像进行按位与运算，只保留圆形区域
        Mat result = new Mat();
        Core.bitwise_and(mat, mat, result, mask);
        // 显示结果
        Bitmap bitmapResult = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bitmapResult);
        if (textView != null) {
            textView.append(circleColor);
            imageView.setImageBitmap(bitmapResult);
        } else {
            RecDialog.createLoadingDialog(context, bitmapResult, "交通灯识别", circleColor);
            if (circleColor.contains("交通信号灯识别结果：红色")) {
                FirstActivity.Connect_Transport.traffic_control(id, 0x02, 0x01);
            } else if (circleColor.contains("交通信号灯识别结果：黄色")) {
                FirstActivity.Connect_Transport.traffic_control(id, 0x02, 0x02);
            } else if (circleColor.contains("交通信号灯识别结果：绿色")) {
                FirstActivity.Connect_Transport.traffic_control(id, 0x02, 0x03);
            }
        }
    }

}