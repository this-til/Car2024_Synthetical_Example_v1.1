package car.bkrc.com.car2024.FragmentView;

import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPCamera;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPCar;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPmask;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.purecameraip;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.CameraUtils.CameraConntrol;
import car.bkrc.com.car2024.Utils.OtherUtil.ClearEditText;
import car.bkrc.com.car2024.Utils.OtherUtil.ShapeRecognizeUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.CarPlate;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.QR_Recognition;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.TrafficUtils;
import car.bkrc.com.car2024.Utils.dialog.RecDialog;

public class PicInformationProcess extends Fragment implements View.OnClickListener {


    // 识别结果文本
    private TextView picrectext_tv;
    // 识别结果图像
    private ImageView picrec_iv;

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Bitmap picBitmap = LeftFragment.INSTANCE.getBitmap();
        switch (v.getId()) {
            case R.id.back_imbtn:
                exitFragment();
                break;
            case R.id.landmark_btn:
                // 处理识别标志物点击事件的代码
                picrectext_tv.setText("识别标志物,需要二次开发！");
                picrec_iv.setImageBitmap(picBitmap);
                break;
            case R.id.mask_all_btn:
                // 处理识别口罩点击事件的代码
                picrectext_tv.setText("识别人脸佩戴口罩,需要二次开发！");
                picrec_iv.setImageBitmap(picBitmap);
                break;
            case R.id.qr_all_btn:
                // 处理识别二维码点击事件的代码
                picrectext_tv.setText("识别二维码");
                picrec_iv.setImageBitmap(picBitmap);
                QR_Recognition.QRRecognition(picBitmap,getContext(), picrectext_tv, picrec_iv);
                break;
            case R.id.carplate_all_btn:
                // 处理识别车牌识别点击事件的代码
                picrectext_tv.setText("识别车牌号");
                picrec_iv.setImageBitmap(picBitmap);
                CarPlate.carTesseract(picBitmap, getContext(), picrectext_tv, picrec_iv);
                break;
            case R.id.ocrrec_all_btn:
                // 处理OCR识别点击事件的代码
                picrectext_tv.setText("OCR文字识别,需要二次开发！");
                picrec_iv.setImageBitmap(picBitmap);
                break;
            case R.id.tracfficrec_btn:
                // 处理交通灯识别点击事件的代码
                picrectext_tv.setText("识别交通灯颜色");
                picrec_iv.setImageBitmap(picBitmap);
                TrafficUtils.HoughCircleCheck(picBitmap, getContext(), 0, picrectext_tv, picrec_iv);
                break;
            case R.id.cartype_all_btn:
                // 处理车型识别点击事件的代码
                picrectext_tv.setText("识别车辆类型,需要二次开发！");
                picrec_iv.setImageBitmap(picBitmap);
                break;
            case R.id.tracfficsign_all_btn:
                // 处理交通标识识别点击事件的代码
                picrectext_tv.setText("识别交通标识,需要二次开发！");
                picrec_iv.setImageBitmap(picBitmap);
                break;
            case R.id.graphic_color_btn:
                // 处理图形颜色识别点击事件的代码
                picrectext_tv.setText("识别图形颜色,需要二次开发！");
                picrec_iv.setImageBitmap(picBitmap);
                break;
            case R.id.graphic_shape_btn:
                // 处理图形形状识别点击事件的代码
                picrectext_tv.setText("识别图形形状,需要二次开发！");
                picrec_iv.setImageBitmap(picBitmap);
                break;
            case R.id.opencv_shape_btn:
                // 处理边缘检测点击事件
                recognitionShae(picBitmap);
                break;
            case R.id.vga_qr_btn:
                FirstActivity.Connect_Transport.qr_rec(1);
                break;
            case R.id.refresh_btn:
                picrectext_tv.setText("结果仅供参考！");
                picrec_iv.setImageBitmap(null);
                break;
            default:
                break;
        }
        vSimple(getContext(), 10);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picinformation_fragment, container, false);
        initView(view);
        // 获取摄像头状态数据
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 界面控件初始化
     *
     * @param view
     */
    private void initView(View view) {
        Button landmark_btn = view.findViewById(R.id.landmark_btn);
        landmark_btn.setOnClickListener(this);
        Button mask_all_btn = view.findViewById(R.id.mask_all_btn);
        mask_all_btn.setOnClickListener(this);
        Button qr_all_btn = view.findViewById(R.id.qr_all_btn);
        qr_all_btn.setOnClickListener(this);
        Button carplate_all_btn = view.findViewById(R.id.carplate_all_btn);
        carplate_all_btn.setOnClickListener(this);
        Button ocrrec_all_btn = view.findViewById(R.id.ocrrec_all_btn);
        ocrrec_all_btn.setOnClickListener(this);
        Button tracfficrec_btn = view.findViewById(R.id.tracfficrec_btn);
        tracfficrec_btn.setOnClickListener(this);
        Button cartype_all_btn = view.findViewById(R.id.cartype_all_btn);
        cartype_all_btn.setOnClickListener(this);
        Button tracfficsign_all_btn = view.findViewById(R.id.tracfficsign_all_btn);
        tracfficsign_all_btn.setOnClickListener(this);
        Button graphic_color_btn = view.findViewById(R.id.graphic_color_btn);
        graphic_color_btn.setOnClickListener(this);
        Button graphic_shape_btn = view.findViewById(R.id.graphic_shape_btn);
        graphic_shape_btn.setOnClickListener(this);
        Button opencv_shape_btn = view.findViewById(R.id.opencv_shape_btn);
        opencv_shape_btn.setOnClickListener(this);
        Button vga_qr_btn = view.findViewById(R.id.vga_qr_btn);
        vga_qr_btn.setOnClickListener(this);
        Button refresh_btn = view.findViewById(R.id.refresh_btn);
        refresh_btn.setOnClickListener(this);
        // 返回按钮
        ImageButton back_imbtn = view.findViewById(R.id.back_imbtn);
        back_imbtn.setOnClickListener(this);
        picrectext_tv = view.findViewById(R.id.picrectext_tv);
        picrec_iv = view.findViewById(R.id.picrec_iv);
    }


    /**
     * 退出到当前Fragment
     */
    private void exitFragment() {
        Fragment current = getParentFragmentManager().findFragmentById(R.id.safety_fragment);
        getParentFragmentManager().beginTransaction().setCustomAnimations(R.anim.across_translate_into, R.anim.across_translate_out).remove(current).commit();
    }

    /**
     * 控制手机震动
     *
     * @param context     上下文
     * @param millisecond 震动时间，毫秒为单位
     */
    public static void vSimple(Context context, int millisecond) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(millisecond);
        } catch (Exception ignored) {

        }
    }

    /**
     * 查找最大轮廓
     *
     * @param bitmap
     * @return
     */
    @SuppressLint("SuspiciousIndentation")
    private void recognitionShae(Bitmap bitmap) {
        Mat mat;
        ShapeRecognizeUtil sru = new ShapeRecognizeUtil(bitmap);
        sru.imgCapture();  // 计算途中包含的形状及最大轮廓
        mat = sru.cv2ImgAddRect(); // 将计算后的图形输出
        Bitmap showbitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);  // 生成新bitmap图像
        Utils.matToBitmap(mat, showbitmap);
        StringBuilder stringBuilder = new StringBuilder();
        for (ShapeRecognizeUtil.Shape shape : sru.getShapeList()) {
            stringBuilder.append(shape.name + shape.shape + " " + shape.w + " " + shape.h + "\n");
        }
        picrec_iv.setImageBitmap(showbitmap);
    }

}
