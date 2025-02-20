package car.bkrc.com.car2024.FragmentView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.ActivityView.LoginActivity;
import car.bkrc.com.car2024.MessageBean.DataRefreshBean;
import car.bkrc.com.car2024.MessageBean.StateChangeBean;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.CameraUtils.XcApplication;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.WiFiStateUtil;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.CarPlate;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.QR_Recognition;
import car.bkrc.com.car2024.Utils.PicDisposeUtils.TrafficUtils;
import car.bkrc.com.car2024.Utils.drawUtils.DLRoundMenuView;
import car.bkrc.com.car2024.Utils.drawUtils.OnMenuClickAllListener;

/**
 * 运动控制页面，控制小车前进后退和转弯
 */
public class RightMoveControlFragment extends Fragment {

    String Camera_show_ip = null;

    private TextView Data_show = null;
    private EditText wheel_speed_edit = null;
    private EditText coded_disc_edit = null;
    private EditText tracking_speed_edit = null;
    private TextView stateTV, psStatusTV, codedDiskTV, lightTV, ultraSonicTV;

    public static final String TAG = "RightFragment1";
    private View view = null;
    private boolean dateGetState = true; // 平台接收状态切换

    public static RightMoveControlFragment getInstance() {
        return RightFragment1Holder.sInstance;
    }

    private static class RightFragment1Holder {
        @SuppressLint("StaticFieldLeak")
        private static final RightMoveControlFragment sInstance = new RightMoveControlFragment();
    }

    /**
     * 接受并显示设备发送的数据
     */
    @SuppressLint("HandlerLeak")
    private final Handler rehHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                byte[] mByte = (byte[]) msg.obj;
                if (mByte[0] == 0x55) {
                    show_ID(mByte[10] & 0xff);// 获取标志位
                    // 光敏状态
                    long psStatus = 0;
                    if ((mByte[2] & 0xff) != 0xA7) {
                        psStatus = mByte[3] & 0xff;
                    }
                    // 超声波数据
                    long ultraSonic = mByte[5] & 0xff;
                    ultraSonic = ultraSonic << 8;
                    ultraSonic += mByte[4] & 0xff;
                    // 光照强度
                    long light = mByte[7] & 0xff;
                    light = light << 8;
                    light += mByte[6] & 0xff;
                    // 码盘
                    long codedDisk = mByte[9] & 0xff;
                    codedDisk = codedDisk << 8;
                    codedDisk += mByte[8] & 0xff;
                    Camera_show_ip = FirstActivity.IPCamera;
                    if (mByte[1] == (byte) 0xaa) {  //主车
                        if (dateGetState) {
                            // 显示数据
                            stateTV.setText(mByte[2] + "");              // 运行状态
                            if (psStatus != 1) {
                                psStatusTV.setText("ON");        // 光敏电阻
                            } else psStatusTV.setText("OFF");        // 光敏电阻
                            codedDiskTV.setText(codedDisk + "");      // 码盘
                            lightTV.setText(light + " lx");           // 光照度
                            ultraSonicTV.setText(ultraSonic + " mm"); // 超声波
                        }
                    }
                    if (mByte[1] == (byte) 0x02) //移动机器人
                    {
                        if (!dateGetState) {
                            if (mByte[2] == -110) {
                                byte[] newData;
                                Log.e("data", "" + mByte[4]);
                                newData = Arrays.copyOfRange(mByte, 5, mByte[4] + 5);
                                Log.e("data", "" + "长度" + newData.length);
                                String str = new String(newData, StandardCharsets.US_ASCII);//第二个参数指定编码方式
                                Toast.makeText(getActivity(), "" + str, Toast.LENGTH_LONG).show();
                            } else {
                                // 显示数据
                                stateTV.setText(mByte[2] + "");              // 运行状态
                                if (psStatus != 1) {
                                    psStatusTV.setText("ON");        // 光敏电阻
                                } else psStatusTV.setText("OFF");        // 光敏电阻
                                codedDiskTV.setText(codedDisk + "");      // 码盘
                                lightTV.setText(light + " lx");           // 光照度
                                ultraSonicTV.setText(ultraSonic + " mm"); // 超声波
                            }
                        }
                    }
                    if (mByte[1] == (byte) 0x03) {
                        if (mByte[2] == (byte) 0x01) {
                            // 目标检测接口，包含交通灯、形状,形状颜色、交通标识、车型、口罩
                            Bitmap picBitmap = LeftFragment.INSTANCE.getBitmap();
                            if (mByte[3] == (byte) 0x01) {
                                // 交通灯识别
                                TrafficUtils.HoughCircleCheck(picBitmap, getContext(), mByte[4], null, null);
                            } else if (mByte[3] == (byte) 0x02) {
                                // 识别图形形状
                            } else if (mByte[3] == (byte) 0x03) {
                                // 识别图形颜色
                            } else if (mByte[3] == (byte) 0x04) {
                                // 主车识别交通标志
                            } else if (mByte[3] == (byte) 0x05) {
                                // 主车识别车型
                            } else if (mByte[3] == (byte) 0x06) {
                                // 主车识别行人及口罩
                            }
                        } else if (mByte[2] == (byte) 0x02) {
                            // 二维码识别接口
                            QR_Recognition.QRRecognition(LeftFragment.INSTANCE.getBitmap(), getContext(), null, null);
                        } else if (mByte[2] == (byte) 0x03) {
                            // 车牌识别接口
                            CarPlate.carTesseract(LeftFragment.INSTANCE.getBitmap(), getContext(), null, null);
                        } else if (mByte[2] == (byte) 0x04) {
                            // 文字识别接口
                        }
                    }

                    //自定义指令
                    if (mByte[1] == (byte) 0xcc) {
                        switch (mByte[2]) {
                            case 0x01: //不定识别
                                //TODO
                                break;
                            case 0x02: //交通灯
                                //TODO
                                break;
                            case 0x03: // 二维码
                                //TODO
                                break;
                        }
                    }
                }
            } else if (msg.what == 2) {
                ToastUtil.ShowToast(getContext(), "请连接设备！");
            }
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        } else {
            if (LoginActivity.isPad(requireContext()))
                view = inflater.inflate(R.layout.right_fragment1, container, false);
            else
                view = inflater.inflate(R.layout.right_fragment1_mobilephone, container, false);
        }
        FirstActivity.recvhandler = rehHandler;
        EventBus.getDefault().register(this); // EventBus消息注册
        control_init();
        connect_Open();
        return view;
    }

    /**
     * 页面初始化
     */
    @SuppressLint("ClickableViewAccessibility")
    private void control_init() {
        Data_show = view.findViewById(R.id.rvdata);
        wheel_speed_edit = view.findViewById(R.id.wheel_speed_data);
        coded_disc_edit = view.findViewById(R.id.coded_disc_data);
        tracking_speed_edit = view.findViewById(R.id.tracking_speed_data);
        stateTV = view.findViewById(R.id.stateTV);
        psStatusTV = view.findViewById(R.id.psStatusTV);
        codedDiskTV = view.findViewById(R.id.codedDiskTV);
        lightTV = view.findViewById(R.id.lightTV);
        ultraSonicTV = view.findViewById(R.id.ultraSonicTV);
        // 控制按钮菜单
        DLRoundMenuView dlRoundMenuView = view.findViewById(R.id.dirve_Menu);
        dlRoundMenuView.setOnMenuClickAllListener(new OnMenuClickAllListener() {
            @Override
            public void OnMenuClick(int position, int motionEvent) {
                Log.d("test", "触发点击");
                int speed = getSpeed();
                int encoder = getEncoder();
                int wheelSpeed = getWheelSpeed();
                vSimple(requireContext(), 30); // 控制手机震动进行反馈
                switch (position) {
                    case 0: // 点击上键
                        FirstActivity.Connect_Transport.go(speed, encoder);
                        break;
                    case 3: // 点击左键
                        FirstActivity.Connect_Transport.left(wheelSpeed);
                        break;
                    case 1: // 点击右键
                        FirstActivity.Connect_Transport.right(wheelSpeed);
                        break;
                    case 2: // 点击下键
                        FirstActivity.Connect_Transport.back(speed, encoder);
                        break;
                    case -1: // 点击中键
                        FirstActivity.Connect_Transport.stop();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void OnMenuLongClick(int position, int motionEvent) {
                Log.d("test", "触发长按");
                int speed = getSpeed();
                int encoder = getEncoder();
                int wheelSpeed = getWheelSpeed();
                vSimple(requireContext(), 30); // 控制手机震动进行反馈
                if (motionEvent == MotionEvent.ACTION_DOWN) {
                    switch (position) {
                        case 0: // 长按上键
                            FirstActivity.Connect_Transport.line(speed);
                            break;
                        case 3: // 长按左键
                            FirstActivity.Connect_Transport.left(wheelSpeed);
                            break;
                        case 1: // 长按右键
                            FirstActivity.Connect_Transport.right(wheelSpeed);
                            break;
                        case 2: // 长按下键
                            FirstActivity.Connect_Transport.back(speed, encoder);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    /**
     * 接收平台连接相关的Eventbus消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 1 && new WiFiStateUtil(getActivity()).wifiInit()) {
            connect_Open();
        } else if (refresh.getRefreshState() == 3) {
            ToastUtil.ShowToast(getContext(), "平台已连接");
        } else if (refresh.getRefreshState() == 4) {
            ToastUtil.ShowToast(getContext(), "平台连接失败！");
        }
    }

    /**
     * 接收平台信息切换时接收状态Eventbus消息
     */
    @SuppressLint("ResourceAsColor")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventStateThread(StateChangeBean stateChangeBean) {
        if (stateChangeBean.getStateChange() == 0) {
            dateGetState = true;// 显示主车数据
            stateTV.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));                // 运行状态
            psStatusTV.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));             // 光敏电阻
            codedDiskTV.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));            // 码盘
            lightTV.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));                // 光照度
            ultraSonicTV.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));           // 超声波
            ToastUtil.ShowToast(getContext(), "相关数据显示为黑色");
        } else if (stateChangeBean.getStateChange() == 1) {
            dateGetState = false;// 显示主车数据
            stateTV.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));             // 运行状态
            psStatusTV.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));         // 光敏电阻
            codedDiskTV.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));        // 码盘
            lightTV.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));             // 光照度
            ultraSonicTV.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));       // 超声波
            ToastUtil.ShowToast(getContext(), "相关数据显示为白色");
        } else if (stateChangeBean.getStateChange() == 2) {
            Data_show.setText(getResources().getText(R.string.car_name));
            wheel_speed_edit.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
            coded_disc_edit.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
            tracking_speed_edit.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
            Data_show.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
        } else if (stateChangeBean.getStateChange() == 3) {
            Data_show.setText(getResources().getText(R.string.vga_name));
            wheel_speed_edit.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
            coded_disc_edit.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
            tracking_speed_edit.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
            Data_show.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
        }
    }

    /**
     * 获取标志位
     *
     * @param id 坐标信息
     */
    private void show_ID(int id) {
        if (id > 0xA0) {
            ToastUtil.ShowToast(getContext(), "随机救援坐标为：" + Integer.toHexString(id).toUpperCase()); // 显示坐标信息
        }
    }

    /**
     * 车辆连接状态判断
     */
    private void connect_Open() {
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            connect_thread();                            //开启网络连接线程
        } else if (XcApplication.isserial == XcApplication.Mode.SERIAL) {
            serial_thread();   //使用纯串口uart4
        }
    }

    /**
     * 建立Socket连接
     */
    private void connect_thread() {
        XcApplication.executorServicetor.execute(() -> FirstActivity.Connect_Transport.connect(rehHandler, FirstActivity.IPCar));
    }

    private void serial_thread() {
        XcApplication.executorServicetor.execute(() -> FirstActivity.Connect_Transport.serial_connect(rehHandler));
    }

    // 获取转弯速度
    private int getWheelSpeed() {
        String src = wheel_speed_edit.getText().toString();
        int speed = 90;
        if (!src.equals("")) {
            speed = Integer.parseInt(src);
        } else {
            ToastUtil.ShowToast(getContext(), "请输入转弯速度！");
        }
        return speed;
    }

    // 获取前进后退码盘值
    private int getEncoder() {
        String src = coded_disc_edit.getText().toString();
        int encoder = 20;
        if (!src.equals("")) {
            encoder = Integer.parseInt(src);
        } else {
            ToastUtil.ShowToast(getContext(), "请输入码盘值！");
        }
        return encoder;
    }

    // 循迹速度
    private int getSpeed() {
        String src = tracking_speed_edit.getText().toString();
        int angle = 50;
        if (!src.equals("")) {
            angle = Integer.parseInt(src);
        } else {
            ToastUtil.ShowToast(getContext(), "请输入循迹速度值！");
        }
        return angle;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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


