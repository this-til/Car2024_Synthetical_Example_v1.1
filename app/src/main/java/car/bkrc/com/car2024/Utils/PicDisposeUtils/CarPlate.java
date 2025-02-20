package car.bkrc.com.car2024.Utils.PicDisposeUtils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyperai.hyperlpr3.HyperLPR3;
import com.hyperai.hyperlpr3.bean.Plate;

import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.Utils.dialog.RecDialog;

public class CarPlate {

    static Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 车牌识别示例，依赖HyperLPR3的SDK
     *
     * @param bitmap
     */
    @SuppressLint("SetTextI18n")
    public static void carTesseract(Bitmap bitmap, Context context, TextView textView, ImageView imageView) {
        new Thread(() -> {
            StringBuilder car;
            if (bitmap != null) {
                // 使用Bitmap作为图片参数进行车牌识别
                Bitmap bcopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Bitmap copyShow = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                // 绘制显示图像的边框基本参数
                Canvas canvasShow = new Canvas(copyShow);
                Paint paintShow = new Paint();
                paintShow.setColor(Color.RED);
                paintShow.setStyle(Paint.Style.STROKE);
                paintShow.setStrokeWidth(2);
                // 记录车牌数据
                car = new StringBuilder();
                // 用于存储检测到的车牌
                try {
                    // 进行车牌检测
                    Plate[] plates = HyperLPR3.getInstance().plateRecognition(bcopy, HyperLPR3.CAMERA_ROTATION_0, HyperLPR3.STREAM_BGRA);
                    // 对显示图像进行边框绘制
                    canvasShow.drawRect(new Rect((int) plates[0].getX1() + 5, (int) plates[0].getY1() + 5, (int) plates[0].getX2() + 5, (int) plates[0].getY2() + 5), paintShow);
                    // 进行文本替换和补充，根据type类型获取车牌类型
                    if (plates[0].getType() == 0) {
                        car.append("国").append(plates[0].getCode().substring(1)).append("（燃油）").append("\n");
                    } else if (plates[0].getType() == 3) {
                        car.append("国").append(plates[0].getCode().substring(1)).append("（电车）").append("\n");
                    } else {
                        car.append("国").append(plates[0].getCode().substring(1)).append("（其他）").append("\n");
                    }
                    // 对识别内容进行数据合并输出
                    Log.e("carTesseract: ", plates[0].toString());
                } catch (Exception ignored) {
                    // 抛出数据处理异常
                }
                StringBuilder finalCar = car;
                handler.post(() -> {
                    // 在UI线程更新UI
                    if (finalCar.length() > 5) {
                        if (textView != null) {
                            textView.setText(finalCar + "（识别结果仅供参考）\n");
                            imageView.setImageBitmap(copyShow);
                        } else {
                            // 进行内容弹窗显示，此处可替换为数据发送
                            RecDialog.createLoadingDialog(context, copyShow, "车牌识别", finalCar + "（识别结果仅供参考）");
                        }
                    } else {
                        if (textView!=null){
                            textView.setText("未识别到车牌！\n");
                            ToastUtil.ShowToast(context, "未识别到车牌");
                        }else {
                            RecDialog.createLoadingDialog(context, copyShow, "二维码识别", "未识别到二维码！");
                        }

                    }
                });
            }
        }).start();
    }
}
