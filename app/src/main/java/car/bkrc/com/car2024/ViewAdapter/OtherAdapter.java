package car.bkrc.com.car2024.ViewAdapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.FragmentView.CameraControlFragment;
import car.bkrc.com.car2024.FragmentView.LeftFragment;
import car.bkrc.com.car2024.FragmentView.PicInformationProcess;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.OtherUtil.RadiusUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.ShapeRecognizeUtil;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.CarPlate;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.QR_Recognition;
import car.bkrc.com.car2024.Utils.dialog.RecDialog;

public class OtherAdapter extends RecyclerView.Adapter<OtherAdapter.ViewHolder> {

    private final List<Other_Landmark> mOtherLandmarkList;
    Context context;
    /**
     * 界面列表适配器，用于加载列表中的选项，如文字、图片等内容
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        View InfrareView;
        ImageView OtherImage;
        TextView OtherName;

        public ViewHolder(View view) {
            super(view);
            InfrareView = view;
            OtherImage = view.findViewById(R.id.landmark_image);
            OtherName = view.findViewById(R.id.landmark_name);
        }
    }

    public OtherAdapter(List<Other_Landmark> InfrareLandmarkList, Context context) {
        mOtherLandmarkList = InfrareLandmarkList;
        this.context = context;
    }

    /**
     * 加载子项布局内容
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.OtherName.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            Other_Landmark otherLandmark = mOtherLandmarkList.get(position);
            Other_select(otherLandmark);
        });
        holder.OtherImage.setOnClickListener(v -> {

            int position = holder.getBindingAdapterPosition();
            Other_Landmark otherLandmark = mOtherLandmarkList.get(position);
            Other_select(otherLandmark);
        });

        return holder;
    }

    /**
     * 负责将每个子项holder绑定数据。参数是“RecyclerView.ViewHolder holder”、“int position”
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Other_Landmark InfrareLandmark = mOtherLandmarkList.get(position);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), InfrareLandmark.getImageId(), null);
        bitmap = RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 10);
        holder.OtherImage.setImageBitmap(bitmap);
        holder.OtherName.setText(InfrareLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mOtherLandmarkList.size();
    }

    // 加载camera控制界面的Fragment
    private final CameraControlFragment cameraControlFragment = new CameraControlFragment();
    // 加载图像处理控制页面的Fragment
    private final PicInformationProcess picInformationProcess = new PicInformationProcess();

    private void Other_select(Other_Landmark InfrareLandmark) {
        vSimple(context, 30); // 控制手机震动进行反馈
        switch (InfrareLandmark.getName()) {
            case "图像检测与识别":
                addFragment(picInformationProcess, R.id.safety_fragment, null);
                break;
            case "流媒体摄像头调节":
                addFragment(cameraControlFragment, R.id.safety_fragment, null);
                break;
            case "智能视觉摄像头俯仰调节":
                position_robotDialog();
                break;
            case "蜂鸣器控制":
                buzzerController();
                break;
            case "转向灯控制":
                lightController();
                break;
            default:
                break;
        }
    }

    /**
     * 在帧布局中加载Fragment
     *
     * @param showFragment
     * @param view
     * @param tag
     */
    private void addFragment(Fragment showFragment, int view, String tag) {
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        if (!showFragment.isAdded()) {
            manager.beginTransaction().setCustomAnimations(R.anim.across_translate_into, R.anim.across_translate_out)
                    .add(view, showFragment, tag).show(showFragment).commit();
        } else {
            manager.beginTransaction().setCustomAnimations(R.anim.across_translate_into, R.anim.across_translate_out).
                    show(showFragment).commit();
        }
    }

    // 二维码、车牌处理
    @SuppressLint("HandlerLeak")
    public Handler qrHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    tipAction("移动机器人摄像头仅支持俯仰角度控制，可通过当前页面完成摄像头的俯仰角度调节。 \n" +
                            "为保障设备安全，摄像头俯仰角仅限于固定范围，每次点击调节范围为 ±5°！");
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 从车摄像头角度调节对话框
     */
    private void position_robotDialog()  //预设位对话框
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("摄像头俯仰角调节");
        builder.setIcon(R.mipmap.rc_logo);
        String[] set_item = {"抬头", "低头", "复位", "俯仰角控制说明"};
        builder.setSingleChoiceItems(set_item, -1, (dialog, which) -> {
            vSimple(context, 30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which + 1) {
                //上下调节
                case 1:
                    FirstActivity.Connect_Transport.rb_cameraControl(0x2B); //向上
                    break;
                case 2:
                    FirstActivity.Connect_Transport.rb_cameraControl(0x2D);  //向下
                    break;
                case 3:
                    FirstActivity.Connect_Transport.rb_cameraControl(0x2C);  //复位
                    break;
                case 4:
                    qrHandler.sendEmptyMessage(10); // 弹窗提醒
                    break;
                default:
                    break;
            }
        });
        builder.create().show();
    }

    private void tipAction(String tip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 设置Title的内容
        builder.setIcon(R.mipmap.rc_logo);
        builder.setTitle("温馨提示");
        // 设置Content来显示一个信息
        builder.setMessage(tip);
        // 设置一个PositiveButton
        builder.setPositiveButton("确定", (dialog, which) -> {
            vSimple(context, 30); // 控制手机震动进行反馈
        });
        builder.show();
    }

    // 蜂鸣器
    private void buzzerController() {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setTitle("蜂鸣器控制");
        String[] im = {"打开", "关闭"};
        build.setSingleChoiceItems(im, -1,
                (dialog, which) -> {
                    vSimple(context, 30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (which == 0) {
                        // 打开蜂鸣器
                        FirstActivity.Connect_Transport.buzzer(1);
                    } else if (which == 1) {
                        // 关闭蜂鸣器
                        FirstActivity.Connect_Transport.buzzer(0);
                    }
                });
        build.create().show();
    }

    // 指示灯遥控器
    private void lightController() {
        AlertDialog.Builder lt_builder = new AlertDialog.Builder(context);
        lt_builder.setTitle("转向灯控制");
        String[] item = {"左转", "右转", "停车", "临时停车"};
        lt_builder.setSingleChoiceItems(item, -1,
                (dialog, which) -> {
                    vSimple(context, 30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (which == 0) {
                        FirstActivity.Connect_Transport.light(1, 0);
                    } else if (which == 1) {
                        FirstActivity.Connect_Transport.light(0, 1);
                    } else if (which == 2) {
                        FirstActivity.Connect_Transport.light(0, 0);
                    } else if (which == 3) {
                        FirstActivity.Connect_Transport.light(1, 1);
                    }
                });
        lt_builder.create().show();
    }


    /**
     * 控制手机震动
     *
     * @param context     上下文
     * @param millisecond 震动时间，毫秒为单位
     */
    public static void vSimple(Context context, int millisecond) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(millisecond);
    }
}