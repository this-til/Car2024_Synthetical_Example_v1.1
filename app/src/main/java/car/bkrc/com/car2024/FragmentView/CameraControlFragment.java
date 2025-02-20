package car.bkrc.com.car2024.FragmentView;

import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPCamera;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPCar;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPmask;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.purecameraip;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.CameraUtils.CameraConntrol;
import car.bkrc.com.car2024.Utils.OtherUtil.ClearEditText;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;

/**
 * 摄像头控制页面
 */
public class CameraControlFragment extends Fragment implements View.OnClickListener {


    private final int LUM_PARAM = 1; // 亮度接口参数
    private final int CONTRAST_PARAM = 2; // 对比度接口参数
    private final int SAT_PARAM = 8; // 饱和度接口参数
    private final int CHROMA_PARAM = 9; // 色度接口参数

    Context minstance = null;

    // 图像亮度、饱和度、对比度控制条
    private SeekBar lum_bar, sat_seekbar, contrast_bar, chroma_bar;
    // 图像亮度、饱和度、对比度数值
    private TextView lum_tv, sat_tv, contrast_tv, chroma_tv;
    // 退出本页面按钮
    private ImageButton back_imbtn;
    // 预设位设置及调用按钮
    private Button setlocation_bt, uselocation_bt, deletelocation_bt;
    // 控制摄像头云台巡航
    private Button cruise_leri_bt, cruise_updown_bt, cruise_stop_bt, cruise_speed_bt,cruise_center_bt;
    // 摄像头启动位置居中设置
    private Button start_center_bt, start_center_close_bt, start_setlocation_bt;
    // 工作频率选择
    private Button flick50_bt, flick60_bt;
    // 镜像和翻转
    private Button turn_0_bt, turn_1_bt, turn_2_bt, turn_3_bt;
    // 红外监测开关
    private Button ircut_off_bt, ircut_auto_btn;
    // 时间水印
    private Button time_open_btn, time_off_btn;
    // 获取摄像头状态，控制摄像头启动和复位
    private Button rest_btn, camera_status_btn, restart_btn, resetcamerapw_camera_btn;
    // 人脸检测跟踪
    private Button findface_open_btn, findface_off_btn, findface_sen_btn;
    // 摄像头控制接口对象
    private CameraConntrol cameraConntrol;
    private int cruise_gear; // 自动巡航方向，1为左右巡航，2为上下巡航，0为停止

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_imbtn:
                exitFragment();
                break;
            case R.id.setlocation_btn:
                // 处理设置预设位点击事件的代码
                position_onvifItemDialog("设置预设位", 30, 2);
                break;
            case R.id.uselocation_btn:
                // 处理调用预设位点击事件的代码
                position_onvifItemDialog("调用预设位", 31, 2);
                break;
            case R.id.deletelocation_btn:
                // 处理删除预设位点击事件的代码
                position_onvifItemDialog("删除预设位", 62, 1);
                break;
            case R.id.cruise_leri_btn:
                // 处理左右巡航点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.DECODER_CONTROL, "command=" +
                        28 + "&onestep=" + 1);
                cruise_gear = 1;
                cruise_leri_bt.setTextColor(Color.WHITE);
                cruise_updown_bt.setTextColor(Color.BLACK);
                cruise_center_bt.setTextColor(Color.BLACK);
                break;
            case R.id.cruise_updown__btn:
                // 处理上下巡航点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.DECODER_CONTROL, "command=" +
                        26 + "&onestep=" + 1);
                cruise_gear = 2;
                cruise_leri_bt.setTextColor(Color.BLACK);
                cruise_updown_bt.setTextColor(Color.WHITE);
                cruise_center_bt.setTextColor(Color.BLACK);
                break;
            case R.id.cruise_center_btn:
                // 处理巡航后居中点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.DECODER_CONTROL, "command=" +
                        25 + "&onestep=" + 1);
                cruise_gear = 3;
                cruise_leri_bt.setTextColor(Color.BLACK);
                cruise_updown_bt.setTextColor(Color.BLACK);
                cruise_center_bt.setTextColor(Color.WHITE);
                break;
            case R.id.cruise_stop_btn:
                // 处理巡航停止点击事件的代码
                if (cruise_gear == 1) {
                    cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.DECODER_CONTROL, "command=" +
                            29 + "&onestep=" + 0);
                } else if (cruise_gear == 2) {
                    cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.DECODER_CONTROL, "command=" +
                            27 + "&onestep=" + 0);
                }
                cruise_leri_bt.setTextColor(Color.BLACK);
                cruise_updown_bt.setTextColor(Color.BLACK);
                break;
            case R.id.cruise_speed_btn:
                // 处理巡航速度点击事件的代码
                setlocation_SpeedDialog();
                break;
            case R.id.start_center_btn:
                // 自动居中点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.SET_MISC, "ptz_center_onstart=" + 1);
                ToastUtil.ShowToast(getContext(),"启动后自动居中");
                break;
            case R.id.start_center_close_btn:
                // 取消居中点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.SET_MISC, "ptz_center_onstart=" + 0);
                ToastUtil.ShowToast(getContext(),"启动后恢复关闭前位置");
                break;
            case R.id.start_setlocation_btn:
                position_onvifItemDialog("启动后调用预设位", 0, 0);
                ToastUtil.ShowToast(getContext(),"请先设置对应预设位后进行调用！");
                break;
            case R.id.flick50_btn:
                // 处理工作模式50 Hz点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 3 + "&value=" + 0);  // 设置镜像翻转
                break;
            case R.id.flick60_btn:
                // 处理工作模式60 Hz点击事件的代码
                checkWorkMode(getContext(), "温馨提示", "摄像头切换为60Hz工作时可能会导致视频流卡顿或加载缓慢，建议使用50Hz");
                break;
            case R.id.turn_0_btn:
                // 处理原始图像点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 5 + "&value=" + 0);  // 设置原图
                break;
            case R.id.turn_1_btn:
                // 处理镜像图像点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 5 + "&value=" + 1);  // 设置镜像
                break;
            case R.id.turn_2_btn:
                // 处理翻转图像点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 5 + "&value=" + 2);  // 设置翻转
                break;
            case R.id.turn_3_btn:
                // 处理镜像翻转图像点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 5 + "&value=" + 3);  // 设置镜像翻转
                break;
            case R.id.ircut_off_btn:
                // 处理红外监测关闭点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 14 + "&value=" + 0);  // 设置红外关闭
                break;
            case R.id.ircut_auto_btn:
                // 处理红外自动开启点击事件的代码
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 14 + "&value=" + 1);  // 设置红外自动开启
                break;
            case R.id.restart_camera_btn:
                // 重启摄像头提示
                tipAction("点击确认重启后30S左右再次点击刷新即可恢复图像页面！", "重启");
                break;
            case R.id.rest_camera_btn:
                // 重置摄像头提示
                resetIPcamera();
                break;
            case R.id.camera_status_btn:
                // 获取摄像头状态数据
                loadingALL();
                break;
            case R.id.lum_tv:
                // 还原亮度
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + LUM_PARAM + "&value=" + 128);  // 恢复出厂亮度
                break;
            case R.id.contrast_tv:
                // 还原对比度
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + CONTRAST_PARAM + "&value=" + 128);  // 恢复出厂对比度
                break;
            case R.id.chroma_tv:
                // 还原色度系数
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + CHROMA_PARAM + "&value=" + 128);  // 恢复出厂色度系数
                break;
            case R.id.sat_tv:
                // 还原饱和度
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + SAT_PARAM + "&value=" + 128);  // 恢复出厂饱和度
                break;
            case R.id.setcamerapw_camera_btn:
                ToastUtil.ShowToast(getContext(),"请在登录页面输入即可！");
                break;
            case R.id.time_open_btn:
                // 设置开启时间水印
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.SET_MISC, "osdenable=" + 1);  // 设置时间水印开启
                break;
            case R.id.time_off_btn:
                // 设置关闭时间水印
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.SET_MISC, "osdenable=" + 0);  // 设置时间水印关闭
                break;
            case R.id.findface_open_btn:
                // 开启人形追踪
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.TRANS_CMD_STRING, "cmd=2127&command=0&enable=1");  // 关闭人形追踪
                break;
            case R.id.findface_off_btn:
                // 关闭人形追踪
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.TRANS_CMD_STRING, "cmd=2127&command=0&enable=0");  // 关闭人形追踪
                break;
            case R.id.findface_sen_btn:
                // 设置人脸检测灵敏度
                setFindfacesen();
                break;
            default:
                break;
        }
        vSimple(getContext(), 10);
    }


    /**
     * 设置工作模式
     *
     * @param context
     * @param title
     * @param msg
     */
    private void checkWorkMode(final Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 设置Title的内容
        builder.setIcon(R.mipmap.rc_logo);
        builder.setTitle(title);
        // 设置Content来显示一个信息
        builder.setMessage(msg);
        // 设置一个PositiveButton
        builder.setPositiveButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setNegativeButton("确认使用60Hz", ((dialog, which) -> {
            cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + 3 + "&value=" + 1);  // 设置工作模式
        }));
        builder.show();
    }

    /**
     * 重置选项
     */
    private void resetIPcamera() {
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(
                getContext());
        dg_timeBuilder.setTitle("请选择重置选项");
        String[] dgtime_item = {"重新输入并配置IP地址信息", "重置流媒体摄像头"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, -1,
                (dialog, which) -> {
                    vSimple(getContext(), 30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (which == 0) {// 计时结束
                        if (!IPCamera.contains("null")) {
                            setCameraIP();
                        } else
                            ToastUtil.ShowToast(getContext(),"请开启摄像头并刷新，若复位后首次启动请通过电脑配置！");
                    } else if (which == 1) {// 计时开启
                        tipAction("摄像头能正常获取图像时无需重置！！！网络摄像头重置后需要重新配置，需要查阅相关文档手册进行配置！", "重置");
                    }
                });
        dg_timeBuilder.create().show();
    }

    /**
     * 重置选项
     */
    private void setFindfacesen() {
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(
                getContext());
        dg_timeBuilder.setTitle("人脸检测灵敏度");
        String[] dgtime_item = {"高", "中", "低"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, findface_sen-1,
                (dialog, which) -> {
                    vSimple(getContext(), 30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.TRANS_CMD_STRING, "cmd=2126&command=0&sensitive=" + (which + 1));  // 发送人脸检测灵敏度结果
                });
        dg_timeBuilder.create().show();
    }

    /**
     * 摄像头复位提示
     *
     * @param tip
     */
    private void tipAction(String tip, String btntext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // 设置Title的内容
        builder.setIcon(R.mipmap.rc_logo);
        builder.setTitle("网络摄像头操作提示");
        // 设置Content来显示一个信息
        builder.setMessage(tip);
        // 设置一个PositiveButton
        builder.setPositiveButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setNegativeButton("确认" + btntext, ((dialog, which) -> {
            if (btntext.equals("重启")) {
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.REBOOT, "");
            } else createLoadingDialog(getContext());
        }));
        builder.show();
    }

    private static Dialog loadingDialog;

    /**
     * 关闭dialog
     */
    public static void closeDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog.cancel();
        } else {
            System.out.println("空!!!");
        }
    }


    /**
     * 弹出摄像头密码输入窗口
     *
     * @param context 当前显示的activity
     */
    private void createLoadingDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.postionlits_item, null);// 得到加载view
        LinearLayout layout = v
                .findViewById(R.id.showList);// 加载布局
        TextView tittle_tv = v.findViewById(R.id.tips);
        tittle_tv.setText("请输入摄像头管理员密码");
        ClearEditText editText = v.findViewById(R.id.edit);
        editText.setText("");
        editText.setHint("请输入6位数密码");
        Button ok_btn = v.findViewById(R.id.left_button);
        ok_btn.setText("确认重置");
        Button close_btn = v.findViewById(R.id.right_button);
        close_btn.setText("取消重置");
        close_btn.setOnClickListener(v1 -> closeDialog());
        ok_btn.setOnClickListener(v1 -> {
            String str = editText.getText().toString();
            if (!str.equals("888888")) {
                ToastUtil.ShowToast(getContext(),"密码输入错误，请重试！");
            } else
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.RESTORE_FACTORY, "");
            closeDialog();
        });
        loadingDialog = new Dialog(context, R.style.MyDialogStyle);// 创建自定义样式dialog
        loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
        loadingDialog.setCanceledOnTouchOutside(true); // 点击加载框以外的区域
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局

        /*
         *显示Dialog的方法
         */
        Window window = loadingDialog.getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.PopWindowAnimStyle);
        try {
            loadingDialog.show();
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        minstance = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_control_fragment, container, false);
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
     * @param view 传入需要初始化的页面
     */
    private void initView(View view) {
        lum_bar = view.findViewById(R.id.lum_seekbar);
        sat_seekbar = view.findViewById(R.id.sat_seekbar);
        contrast_bar = view.findViewById(R.id.contrast_seekbar);
        chroma_bar = view.findViewById(R.id.chroma_seekbar);

        lum_tv = view.findViewById(R.id.lum_tv);
        lum_tv.setOnClickListener(this);
        sat_tv = view.findViewById(R.id.sat_tv);
        sat_tv.setOnClickListener(this);
        contrast_tv = view.findViewById(R.id.contrast_tv);
        contrast_tv.setOnClickListener(this);
        chroma_tv = view.findViewById(R.id.chroma_tv);
        chroma_tv.setOnClickListener(this);
        back_imbtn = view.findViewById(R.id.back_imbtn);
        back_imbtn.setOnClickListener(this);
        setlocation_bt = view.findViewById(R.id.setlocation_btn);
        setlocation_bt.setOnClickListener(this);
        uselocation_bt = view.findViewById(R.id.uselocation_btn);
        uselocation_bt.setOnClickListener(this);
        deletelocation_bt = view.findViewById(R.id.deletelocation_btn);
        deletelocation_bt.setOnClickListener(this);
        cruise_leri_bt = view.findViewById(R.id.cruise_leri_btn);
        cruise_leri_bt.setOnClickListener(this);
        cruise_updown_bt = view.findViewById(R.id.cruise_updown__btn);
        cruise_updown_bt.setOnClickListener(this);
        cruise_center_bt = view.findViewById(R.id.cruise_center_btn);
        cruise_center_bt.setOnClickListener(this);
        cruise_stop_bt = view.findViewById(R.id.cruise_stop_btn);
        cruise_stop_bt.setOnClickListener(this);
        cruise_speed_bt = view.findViewById(R.id.cruise_speed_btn);
        cruise_speed_bt.setOnClickListener(this);
        start_center_bt = view.findViewById(R.id.start_center_btn);
        start_center_bt.setOnClickListener(this);
        start_center_close_bt = view.findViewById(R.id.start_center_close_btn);
        start_center_close_bt.setOnClickListener(this);
        start_setlocation_bt = view.findViewById(R.id.start_setlocation_btn);
        start_setlocation_bt.setOnClickListener(this);
        flick50_bt = view.findViewById(R.id.flick50_btn);
        flick50_bt.setOnClickListener(this);
        flick60_bt = view.findViewById(R.id.flick60_btn);
        flick60_bt.setOnClickListener(this);
        turn_0_bt = view.findViewById(R.id.turn_0_btn);
        turn_0_bt.setOnClickListener(this);
        turn_1_bt = view.findViewById(R.id.turn_1_btn);
        turn_1_bt.setOnClickListener(this);
        turn_2_bt = view.findViewById(R.id.turn_2_btn);
        turn_2_bt.setOnClickListener(this);
        turn_3_bt = view.findViewById(R.id.turn_3_btn);
        turn_3_bt.setOnClickListener(this);
        ircut_off_bt = view.findViewById(R.id.ircut_off_btn);
        ircut_off_bt.setOnClickListener(this);
        ircut_auto_btn = view.findViewById(R.id.ircut_auto_btn);
        ircut_auto_btn.setOnClickListener(this);
        rest_btn = view.findViewById(R.id.rest_camera_btn);
        rest_btn.setOnClickListener(this);
        restart_btn = view.findViewById(R.id.restart_camera_btn);
        restart_btn.setOnClickListener(this);
        camera_status_btn = view.findViewById(R.id.camera_status_btn);
        camera_status_btn.setOnClickListener(this);
        resetcamerapw_camera_btn = view.findViewById(R.id.setcamerapw_camera_btn);
        resetcamerapw_camera_btn.setOnClickListener(this);
        time_open_btn = view.findViewById(R.id.time_open_btn);
        time_open_btn.setOnClickListener(this);
        time_off_btn = view.findViewById(R.id.time_off_btn);
        time_off_btn.setOnClickListener(this);
        findface_open_btn = view.findViewById(R.id.findface_open_btn);
        findface_open_btn.setOnClickListener(this);
        findface_off_btn = view.findViewById(R.id.findface_off_btn);
        findface_off_btn.setOnClickListener(this);
        findface_sen_btn = view.findViewById(R.id.findface_sen_btn);
        findface_sen_btn.setOnClickListener(this);

        cameraConntrol = new CameraConntrol(getContext(), myHandler);

        lum_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lum_tv.setText(String.valueOf(progress));
                vSimple(getContext(), 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + LUM_PARAM + "&value=" + Integer.parseInt(lum_tv.getText().toString()));
            }
        });

        sat_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sat_tv.setText(String.valueOf(progress));
                vSimple(getContext(), 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + SAT_PARAM + "&value=" + Integer.parseInt(sat_tv.getText().toString()));
            }
        });
        contrast_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                contrast_tv.setText(String.valueOf(progress));
                vSimple(getContext(), 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + CONTRAST_PARAM + "&value=" + Integer.parseInt(contrast_tv.getText().toString()));
            }
        });
        chroma_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                chroma_tv.setText(String.valueOf(progress));
                vSimple(getContext(), 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.CAMERA_CONTROL, "param=" + CHROMA_PARAM + "&value=" + Integer.parseInt(chroma_tv.getText().toString()));
            }
        });
        loadingALL();
    }

    /**
     * 获取各个功能块状态
     */
    private void loadingALL() {
        new Thread(() -> { // 加载设备属性信息
            try {
                cameraConntrol.getCameraStatePostHttp(FirstActivity.IPCamera, cameraConntrol.GET_CAMERA_PARAMS, "");  // 获取视频图像参数
                Thread.sleep(1000);
                cameraConntrol.getCameraStatePostHttp(FirstActivity.IPCamera, cameraConntrol.GET_MISC, "");  // 获取云台参数
                Thread.sleep(1000);
                cameraConntrol.getCameraStatePostHttp(FirstActivity.IPCamera, cameraConntrol.GET_PARAMS, "");  // 获取设备参数
                Thread.sleep(1000);
                cameraConntrol.getCameraStatePostHttp(FirstActivity.IPCamera, cameraConntrol.TRANS_CMD_STRING, "&cmd=2127&command=1"); // 查询人脸检测状态
                Thread.sleep(1000);
                cameraConntrol.getCameraStatePostHttp(FirstActivity.IPCamera, cameraConntrol.TRANS_CMD_STRING, "&cmd=2126&command=1"); // 查询人脸检测灵敏度
                Thread.sleep(1000);
                ipCamera = IPtoSection(purecameraip);
                Thread.sleep(1000);
                ipGateWay = IPtoSection(IPCar);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
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
        try{
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(millisecond);
        }catch (Exception ignored){

        }

    }

    private byte[] ipCamera = new byte[4];
    private byte[] ipGateWay = new byte[4];

    /**
     * 摄像头地址输入框，用于串口通信
     */
    @SuppressLint("SetTextI18n")
    private void setCameraIP() {

        AlertDialog.Builder dg_Builder = new AlertDialog.Builder(
                getContext());
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.ip_item_hex, null);
        dg_Builder.setTitle("配置流媒体摄像头地址信息");
        dg_Builder.setView(view);
        // IP地址输入框
        final EditText ip_editText1 = view.findViewById(R.id.ip_editText1);
        final EditText ip_editText2 = view.findViewById(R.id.ip_editText2);
        final EditText ip_editText3 = view.findViewById(R.id.ip_editText3);
        final EditText ip_editText4 = view.findViewById(R.id.ip_editText4);
        final EditText port_editText = view.findViewById(R.id.camera_port_editText);
        if (port != 0) {
            ip_editText1.setText("" + (ipCamera[0] & 0xFF));
            ip_editText2.setText("" + (ipCamera[1] & 0xFF));
            ip_editText3.setText("" + (ipCamera[2] & 0xFF));
            ip_editText4.setText("" + (ipCamera[3] & 0xFF));
            port_editText.setText("" + port);
        }
        // port网络访问端口
        final EditText this_IpEditText = view.findViewById(R.id.this_ipText);
        this_IpEditText.setText(IPCar);
        final Button oneSet_Btn = view.findViewById(R.id.recommend_ip_btn);
        oneSet_Btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                // 一键配置IP地址
                ip_editText1.setText("" + (ipGateWay[0] & 0xFF));
                ip_editText2.setText("" + (ipGateWay[1] & 0xFF));
                ip_editText3.setText("" + (ipGateWay[2] & 0xFF));
                ip_editText4.setText("100");
                // 一键配置端口号
                port_editText.setText("81");
            }
        });
        dg_Builder.setPositiveButton("确定",
                (dialog, which) -> {
                    vSimple(getContext(), 30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    String cameraIP = ip_editText1.getText().toString() + "." + ip_editText2.getText().toString() + "." + ip_editText3.getText().toString() + "." + ip_editText4.getText().toString();
                    cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.SET_NETWORK, "ipaddr=" + cameraIP +
                            "&mask=" + IPmask + "&gateway=" + IPCar + "&dns=" + "8.8.8.8"
                            + "&dns2=" + IPCar + "&dhcp=0" + "&port=" + port_editText.getText().toString());
                });
        dg_Builder.setNegativeButton("取消",
                (dialog, which) -> {
                    vSimple(getContext(), 30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    dialog.cancel();
                });
        dg_Builder.create().show();
    }

    public byte[] IPtoSection(String ipAddress) {
        byte[] addressBytes = new byte[4];
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            addressBytes = inetAddress.getAddress();
            System.out.println("IP Address: " + Arrays.toString(addressBytes));
            // 切片IPv4地址
            for (byte b : addressBytes) {
                System.out.print((b & 0xFF) + "\n");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addressBytes;
    }

    /**
     * 预设位控制调节处理
     *
     * @param tittle  标题
     * @param command 控制指令 ：
     *                30~60设置预设位 间隔为2 即30,32,34,36
     *                31~61调用预设位 间隔为2 即31,33,35,37
     *                62~77删除预设位 间隔为1 即62,63,64,65
     * @param tip     设置间隔 1 || 2
     */
    private void position_onvifItemDialog(String tittle, int command, int tip)  //预设位对话框
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(tittle);
        // 获取窗口对象
        String[] set_item;
        if (command == 0) {
            set_item = new String[17];
            for (int i = 0; i <= 16; i++) {
                if (i == 0) {
                    set_item[i] = "取消启动预设";
                } else set_item[i] = tittle + " " + i;
            }
        } else {
            set_item = new String[16];
            for (int i = 0; i <= 15; i++) {
                set_item[i] = tittle + " " + (i + 1);
            }
        }
        builder.setSingleChoiceItems(set_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            new Thread(() -> {
                if (FirstActivity.IPCamera != null) {
                    if (command == 0) {
                        cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.SET_MISC, "preset_onstart=" + which);// 摄像头启动时的预设位控制
                    } else {
                        cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + (command + which * tip) + "&onestep=" + 0); // 摄像头预设位控制
                    }
                }
            }).start();
        });
        builder.show();
    }

    /**
     * 云台速度控制调节处理
     */
    private void setlocation_SpeedDialog()  //预设位对话框
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("云台运行速度档位");
        // 获取窗口对象
        String[] set_item = new String[11];
        for (int i = 0; i <= 10; i++) {
            set_item[i] = "云台运行速度档位" + " " + i + " 档";
        }
        builder.setSingleChoiceItems(set_item, ptz_patrol_rate, (dialog, which) -> {
            // TODO 自动生成的方法存根
            new Thread(() -> {
                if (FirstActivity.IPCamera != null) {
                    cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.SET_MISC, "ptz_patrol_rate=" + which + "&ptz_patrol_up_rate=" +
                            which + "&ptz_patrol_down_rate=" + which + "&ptz_patrol_left_rate=" + which + "&ptz_patrol_right_rate=" + which);  // 设置红外关闭
                }
            }).start();
        });
        builder.show();
    }

    int ptz_patrol_rate = 0;
    int findface_sen = 0;
    private int port;
    //这里处理传过来的数据，调整页面控件状态
    @SuppressLint("HandlerLeak")
    public Handler myHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Bundle bundle = msg.getData();
                Set<String> getKey = bundle.keySet();
                for (String key : getKey) {
                    if ("vbright".equals(key)) {
                        lum_bar.setProgress(bundle.getInt(key), true);
                        lum_tv.setText(String.valueOf(bundle.getInt(key)));
                    } else if ("vcontrast".equals(key)) {
                        contrast_bar.setProgress(bundle.getInt(key), true);
                        contrast_tv.setText(String.valueOf(bundle.getInt(key)));
                    } else if ("vsaturation".equals(key)) {
                        sat_seekbar.setProgress(bundle.getInt(key), true);
                        sat_tv.setText(String.valueOf(bundle.getInt(key)));
                    } else if ("vhue".equals(key)) {
                        chroma_bar.setProgress(bundle.getInt(key), true);
                        chroma_tv.setText(String.valueOf(bundle.getInt(key)));
                    } else if ("mode".equals(key)) {
                        if (bundle.getInt(key) == 1) {
                            flick50_bt.setTextColor(Color.BLACK);
                            flick60_bt.setTextColor(Color.WHITE);
                        } else {
                            flick50_bt.setTextColor(Color.WHITE);
                            flick60_bt.setTextColor(Color.BLACK);
                        }
                    } else if ("flip".equals(key)) {
                        if (bundle.getInt(key) == 0) {
                            turn_0_bt.setTextColor(Color.WHITE);
                            turn_1_bt.setTextColor(Color.BLACK);
                            turn_2_bt.setTextColor(Color.BLACK);
                            turn_3_bt.setTextColor(Color.BLACK);
                        } else if (bundle.getInt(key) == 1) {
                            turn_0_bt.setTextColor(Color.BLACK);
                            turn_1_bt.setTextColor(Color.WHITE);
                            turn_2_bt.setTextColor(Color.BLACK);
                            turn_3_bt.setTextColor(Color.BLACK);
                        } else if (bundle.getInt(key) == 2) {
                            turn_0_bt.setTextColor(Color.BLACK);
                            turn_1_bt.setTextColor(Color.BLACK);
                            turn_2_bt.setTextColor(Color.WHITE);
                            turn_3_bt.setTextColor(Color.BLACK);
                        } else if (bundle.getInt(key) == 3) {
                            turn_0_bt.setTextColor(Color.BLACK);
                            turn_1_bt.setTextColor(Color.BLACK);
                            turn_2_bt.setTextColor(Color.BLACK);
                            turn_3_bt.setTextColor(Color.WHITE);
                        }
                    } else if ("ircut".equals(key)) {
                        if (bundle.getInt(key) == 0) {
                            ircut_off_bt.setTextColor(Color.WHITE);
                            ircut_auto_btn.setTextColor(Color.BLACK);
                        } else if (bundle.getInt(key) == 1) {
                            ircut_off_bt.setTextColor(Color.BLACK);
                            ircut_auto_btn.setTextColor(Color.WHITE);
                        }
                    } else if ("OSDEnable".equals(key)) {
                        if (bundle.getInt(key) == 0) {
                            time_off_btn.setTextColor(Color.WHITE);
                            time_open_btn.setTextColor(Color.BLACK);
                        } else if (bundle.getInt(key) == 1) {
                            time_off_btn.setTextColor(Color.BLACK);
                            time_open_btn.setTextColor(Color.WHITE);
                        }
                    }
                }
            } else if (msg.what == 1) {
                Bundle bundle = msg.getData();
                Set<String> getKey = bundle.keySet();
                for (String key : getKey) {
                    if ("port".equals(key)) {
                        port = bundle.getInt(key);
                    }
                }
                vSimple(getContext(), 30);
            } else if (msg.what == 2) { // 设置图像结果回传
                Bundle bundle = msg.getData();
                Set<String> getKey = bundle.keySet();
                for (String key : getKey) {
                    if ("ptz_patrol_rate".equals(key)) {
                        ptz_patrol_rate = bundle.getInt(key);
                    } else if ("ptz_center_onstart".equals(key)) {
                        if (bundle.getInt(key) == 1) {
                            start_center_bt.setTextColor(Color.WHITE);
                            start_center_close_bt.setTextColor(Color.BLACK);
                        } else {
                            start_center_bt.setTextColor(Color.BLACK);
                            start_center_close_bt.setTextColor(Color.WHITE);
                        }
                    }
                }
            } else if (msg.what == 10) { // 设置图像结果回传
                Bundle bundle = msg.getData();
                if (bundle.getString("result").equals("ok")) {
                    vSimple(getContext(), 30);
                    cameraConntrol.getCameraStatePostHttp(FirstActivity.IPCamera, cameraConntrol.GET_CAMERA_PARAMS, "");
                }
            } else if (msg.what == 20) { // 设置云台结果回传
                Bundle bundle = msg.getData();
                if (bundle.getString("result").equals("ok")) {
                    vSimple(getContext(), 30);
                    cameraConntrol.getCameraStatePostHttp(FirstActivity.IPCamera, cameraConntrol.GET_MISC, "");
                }
            } else if (msg.what == 30) {
                Bundle bundle = msg.getData();
                if (bundle.getString("result").equals("ok")) {
                    ToastUtil.ShowToast(getContext(),"设置成功，请等待摄像头重启成功后重新启动平台！");
                    cameraConntrol.cameraMiscControlPostHttp(FirstActivity.IPCamera, cameraConntrol.REBOOT, "");
                    vSimple(getContext(), 30);
                }
            } else if (msg.what == 40) { // 普通设置项结果回传
                Bundle bundle = msg.getData();
                Set<String> getKey = bundle.keySet();
                for (String key : getKey) {
                    if ("cmd".equals(key)) {
                        vSimple(getContext(), 30);
                    }else if ("enable".equals(key)){
                        if (bundle.getInt("enable") == 1){
                            findface_open_btn.setTextColor(Color.WHITE);
                            findface_off_btn.setTextColor(Color.BLACK);
                        }else if (bundle.getInt("enable") == 0){
                            findface_open_btn.setTextColor(Color.BLACK);
                            findface_off_btn.setTextColor(Color.WHITE);
                        }
                    }else if ("sensitive".equals(key)){
                        findface_sen = bundle.getInt("sensitive");
                    }
                }
            } else if (msg.what == 100) { // 普通设置项结果回传
                Bundle bundle = msg.getData();
                if (bundle.getString("result").equals("ok")) {
                    vSimple(getContext(), 30);
                }
            }
        }
    };


}
