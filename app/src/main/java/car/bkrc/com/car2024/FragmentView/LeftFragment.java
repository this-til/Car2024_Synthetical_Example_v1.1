package car.bkrc.com.car2024.FragmentView;


import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPCamera;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.purecameraip;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.ActivityView.LoginActivity;
import car.bkrc.com.car2024.MessageBean.DataRefreshBean;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.CameraUtils.CameraConntrol;
import car.bkrc.com.car2024.Utils.CameraUtils.XcApplication;
import car.bkrc.com.car2024.Utils.OtherUtil.CameraConnectUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.VLCPlayer;

/**
 * 左侧视频预览页面
 */
public class LeftFragment extends Fragment implements VLCPlayer.VLCPlayerCallback, View.OnClickListener {

    public static final String TAG = "LeftFragment";
    private float x1 = 0;
    private float y1 = 0;
    private ImageButton refershImageButton;

    // 摄像头工具
    @SuppressLint("StaticFieldLeak")
    private static TextView showip = null;
    private CameraConnectUtil cameraConnectUtil;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private TextureView textureView, cameraView;
    private ImageButton camera_imbtn;

    private VLCPlayer vlcPlayer;

    private Button scale_num_btn, switchover_phpto_btn;
    @SuppressLint("StaticFieldLeak")
    public static LeftFragment INSTANCE;
    @SuppressLint("StaticFieldLeak")
    public static CameraConntrol cameraConntrol;
    View view = null;

    public static boolean playRTSPstate = false; // VLC加载状态
    boolean phone_cameraState = false; // 相机启用状态
    boolean vlcPlayState = false;  // VLC播放状态,false为未暂停，true为暂停

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // 判断设备
        if (LoginActivity.isPad(requireActivity())) {
            view = inflater.inflate(R.layout.left_fragment, container, false);
        } else {
            view = inflater.inflate(R.layout.left_fragment_mobilephone, container, false);
        }
        EventBus.getDefault().register(this);
        // 在创建时设置不息屏
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView(view);
        INSTANCE = this;
        requestStoragePermission();
        cameraConnectUtil = new CameraConnectUtil(getContext());
        return view;
    }


    /**
     * 判断IP地址是否在同一网段
     * @param ipAddress 地址
     * @param subnetAddress 对比的地址
     * @param subnetMask 掩码
     * @return 返回是否在同一网段
     * @throws UnknownHostException 防止未知错误
     */
    public static boolean isIpAddressInSubnet(String ipAddress, String subnetAddress, String subnetMask) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(ipAddress);
        InetAddress subnet = InetAddress.getByName(subnetAddress);
        InetAddress mask = InetAddress.getByName(subnetMask);

        byte[] inetAddressBytes = inetAddress.getAddress();
        byte[] subnetBytes = subnet.getAddress();
        byte[] maskBytes = mask.getAddress();

        for (int i = 0; i < inetAddressBytes.length; i++) {
            int addressByte = inetAddressBytes[i] & 0xFF;
            int subnetByte = subnetBytes[i] & 0xFF;
            int maskByte = maskBytes[i] & 0xFF;
            if ((addressByte & maskByte) != (subnetByte & maskByte)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 页面控件初始化
     */
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initView(View view) {

        //保存图片按键
        Button saveImage_Btn = view.findViewById(R.id.save_phpto);
        refershImageButton = view.findViewById(R.id.refresh_img_btn);
        Button reference_Btn = view.findViewById(R.id.refresh_btn);
        cameraView = new TextureView(requireContext());
        cameraView = view.findViewById(R.id.camera_view);
        camera_imbtn = view.findViewById(R.id.camera_imbtn);
        camera_imbtn.setOnClickListener(this);
        cameraView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                texture_surface = new Surface(cameraView.getSurfaceTexture());
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }
        });
        reference_Btn.setOnClickListener(this);
        refershImageButton.setOnClickListener(this);
        showip = view.findViewById(R.id.showip);
        scale_num_btn = view.findViewById(R.id.scale_phpto_btn);
        scale_num_btn.setOnClickListener(this);

        // 镜头切换按钮
        switchover_phpto_btn = view.findViewById(R.id.switchover_phpto_btn);
        switchover_phpto_btn.setOnClickListener(this);
        cameraConntrol = new CameraConntrol(requireContext(), myHandler);
        scaleGestureDetector = new ScaleGestureDetector(requireContext(), new MyScaleGestureListener());
        matrix = new Matrix();
        initTextureView();

        saveImage_Btn.setOnClickListener(this);
        if (XcApplication.isserial == XcApplication.Mode.SOCKET && !IPCamera.equals("null:81")) {
            setCameraConnectState(true);
            showip.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "Camera-IP:" + FirstActivity.purecameraip);
        } else if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            showip.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "摄像头未连接！");
        } else if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL) {
            FirstActivity.purecameraip = ipCheck();
            showip.setText("Camera-IP:" + FirstActivity.purecameraip);
            if (FirstActivity.purecameraip.equals("")) {
                ToastUtil.ShowToast(requireContext(), "请检查摄像头IP地址是否配置");
            }
        }
    }

    /**
     * 初始化textureview，加载vlc播放器
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initTextureView() {
        textureView = new TextureView(requireContext());
        textureView = view.findViewById(R.id.video_view);
        textureView.setOnTouchListener(new ontouchlistener1());
        // 视频流显示区域
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                if (!Objects.equals(purecameraip, null)) {
                    initVLCPlayer(true);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }
        });
    }

    public static final String PIC_DIR_NAME = "BKRC_Car";

    public static String getSDPath() {
        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PIC_DIR_NAME);
        Log.d(TAG, "sdDir:" + mPicDir.getAbsolutePath());
        if (!mPicDir.exists()) {
            if (mPicDir.mkdirs()) {
                Log.d(TAG, "文件夹创建成功！！");
            } else Log.d(TAG, "sdDir: 文件夹创建失败！！");
        }
        return mPicDir.getAbsolutePath();
    }


    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void requestPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                ToastUtil.ShowToast(getContext(), "未获取到权限");
                requireActivity().finish();
            }
        }
    }

    /**
     * 视频流数据加载播放
     */
    private void initVLCPlayer(Boolean isFirst) {
        scale_num_btn.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("IPCheck", 0);
        String cameraUser = sharedPreferences.getString("CameraUser", "");
        String cameraUserPs = sharedPreferences.getString("CameraPs", "");
        if (purecameraip == null) {
            ToastUtil.ShowToast(requireContext(), "数据异常，请再次刷新！");
            return;
        }
        String url = "rtsp://" + cameraUser + ":" + cameraUserPs + "@" + purecameraip + ":10554/tcp/av0_0";
        if (isFirst) {
            vlcPlayer = new VLCPlayer(getContext());
            vlcPlayer.setVideoSurface(textureView);
        }
        XcApplication.executorServicetor.execute(() -> {
            try {
                vlcPlayer.setDataSource(url);
                vlcPlayer.setCallback(LeftFragment.INSTANCE);
                vlcPlayer.play();
                vlcPlayState = true;
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * 接收Eventbus消息
     */
    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 1) {
            Log.e(TAG, "onEventMainThread: " + IPCamera);
            if (IPCamera.contains("null")) {
                Log.e(TAG, "onEventMainThread: " + IPCamera);
                showip.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "IP获取失败，请检查网络！");
                cameraConnectUtil.cameraStopService();
                cameraConnectUtil.cameraInit();
                cameraConnectUtil.search();
            } else {
                cameraConnectUtil.cameraStopService();
                cameraConnectUtil.cameraInit();
                cameraConnectUtil.search();
                if (phone_cameraState) phone_cameraState = closeCamera(true);
                // 初始化视频流
                initVLCPlayer(!playRTSPstate);
            }
        } else if (refresh.getRefreshState() == 2) {
            if (XcApplication.isserial == XcApplication.Mode.SOCKET && !IPCamera.contains("null")) {
                setCameraConnectState(true);
                showip.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "Camera-IP:" + FirstActivity.purecameraip);
                try {
                    if (!isIpAddressInSubnet(FirstActivity.IPCar, FirstActivity.purecameraip, FirstActivity.IPmask)) {
                        ToastUtil.ShowToast(requireContext(), "请检查摄像头配置，当前摄像头地址与小车不在同一网段内，请重新配置");
                    } else initVLCPlayer(false); // 初始化视频流
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            } else if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
                if (isWiFiEnable(requireContext()) && !isMobileEnableReflex(requireContext())) {
                    setCameraConnectState(false);
                    showip.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "请重启您的平台！（检查WiFi连接是否正确）");
                } else if (isAllEnable(requireContext())) {
                    setCameraConnectState(false);
                    showip.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "请关闭蜂窝数据后刷新！");
                }
            } else if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL) {
                FirstActivity.purecameraip = ipCheck();
                showip.setText("Camera-IP:" + FirstActivity.purecameraip);
                if (FirstActivity.purecameraip.equals("")) {
                    ToastUtil.ShowToast(requireContext(), "当前摄像头地址与小车不在同一网段内，请重新配置");
                } else {
                    if (phone_cameraState) {
                        closeCamera(true);
                    }
                    initVLCPlayer(false); // 初始化视频流
                }
            }
        }
    }

    /**
     * 获取数据流量连接状态
     */
    public static boolean isMobileEnableReflex(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("DiscouragedPrivateApi") Method getMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取WiFi连接状态
     */
    public static boolean isWiFiEnable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 判断WiFi与蜂窝数据是否同时打开
     */
    public static boolean isAllEnable(Context context) {
        return isWiFiEnable(context) && isMobileEnableReflex(context);
    }

    /**
     * 判断本地保存了摄像头地址
     */
    private String ipCheck() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("IPCheck", 0);
        String first_run = sharedPreferences.getString("CameraIP", "");
        if (first_run.equals("")) {
            return "";
        } else {
            return first_run;
        }
    }

    @SuppressLint("HandlerLeak")
    public static Handler showidHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 22) {
                showip.setText(msg.obj + "\n" + "Camera-IP：" + IPCamera);
            }
        }
    };

    // 摄像头连接状态
    private static boolean cameraConnectState = false;

    public static boolean isCameraConnectState() {
        return cameraConnectState;
    }

    public void setCameraConnectState(boolean cameraConnectState) {
        LeftFragment.cameraConnectState = cameraConnectState;
    }

    // 得到当前摄像头的图片信息，转换为Bitmap
    @SuppressLint("ResourceType")
    public Bitmap getBitmap() {
        if (playRTSPstate && scaleFactor > 1) {
            Matrix matrix = new Matrix();
            textureView.getTransform(matrix);
            int width = textureView.getWidth();
            int height = textureView.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(textureView.getBitmap(), matrix, null);
            return bitmap;
        } else if (phone_cameraState) {
            // 从 TextureView 获取 Bitmap
            Bitmap textureBitmap = cameraView.getBitmap();
            // 获取当前 TextureView 的变换矩阵
            Matrix matrix = new Matrix();
            cameraView.getTransform(matrix);
            // 对 Bitmap 进行旋转
            return Bitmap.createBitmap(textureBitmap, 0, 0, cameraView.getWidth(), cameraView.getHeight(), matrix, true);
        } else return textureView.getBitmap();
    }

    /**
     * 将bitmap保存为图片
     */
    public void saveBitmapAsJpg(String path, Bitmap bitmap) {
        // 生成图片名称
        String imageName = generateImageName();
        // 指定保存路径和文件名
        File file = new File(path, imageName);
        try {
            // 创建文件输出流
            FileOutputStream fos = new FileOutputStream(file);
            // 将 Bitmap 压缩为 JPG 格式并写入文件输出流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // 关闭文件输出流
            fos.close();
            // 保存成功
            Log.d("Save Image", "Image saved successfully. Name: " + imageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加图片名称
     */
    public String generateImageName() {
        // 获取当前时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timeStamp = dateFormat.format(new Date());
        // 生成6位随机编号
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000; // 生成100000到999999之间的随机数
        // 组合图片名称
        return timeStamp + "_" + randomNum + ".jpg";
    }

    @Override
    public void onBuffering(float bufferPercent) {
        playRTSPstate = true;
        Log.e(TAG, "onBuffering: 播放缓冲");
    }

    @Override
    public void onEndReached() {
        playRTSPstate = false;
        Log.e(TAG, "onError: 播放结束");
    }

    @Override
    public void onError() {
        if (cameraConnectState) {
            try {
                if (!isIpAddressInSubnet(FirstActivity.IPCar, FirstActivity.purecameraip, FirstActivity.IPmask)) {
                    ToastUtil.ShowToast(getContext(), "当前摄像头地址与小车不在同一网段内，请重新配置摄像头！");
                } else {
                    ToastUtil.ShowToast(getContext(), "摄像头连接出错，请重新连接设备");
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        playRTSPstate = false;
        Log.e(TAG, "onError: 播放出错");
    }

    @Override
    public void onTimeChanged(long currentTime) {
    }

    @Override
    public void onPositionChanged(float position) {
        playRTSPstate = true;
        Log.e(TAG, "onPositionChanged: 进度有变化");
    }

    /**
     * 触摸监听，设置缩放
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return false;
    }

    private int CAMERA_STATE = 0;
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switchover_phpto_btn: // 相机开关
                if (playRTSPstate && !phone_cameraState) { // 播放状态下被点击，开启相机
                    vlcPlayer.stop();
                    vlcPlayer.pause();
                    vlcPlayState = true;
                    switchover_phpto_btn.setText(R.string.cut_car_camera);
                    scale_num_btn.setVisibility(View.GONE); // 设置放大按钮为隐藏
                    if (checkPermission()) {
                        phone_cameraState = openCamera(cameraView,1);
                    }
                } else {
                    if (phone_cameraState && vlcPlayState) { // 暂停状态下启动相机后被点击，关闭相机，继续播放视频流
                        switchover_phpto_btn.setText(R.string.cut_camera);
                        scale_num_btn.setVisibility(View.VISIBLE); // 设置放大按钮为显示
                        phone_cameraState = closeCamera(true);
                        vlcPlayer.play();
                        vlcPlayState = false;
                    } else if (phone_cameraState) { // RTSP未播放状态下相机启动后被点击，关闭相机
                        switchover_phpto_btn.setText(R.string.cut_car_camera);
                        scale_num_btn.setVisibility(View.GONE); // 设置放大按钮为隐藏
                        phone_cameraState = closeCamera(true);
                    } else { // 未播放任何视频或图像内容时被点击，开启相机
                        switchover_phpto_btn.setText(R.string.cut_car_camera);
                        scale_num_btn.setVisibility(View.GONE); // 设置放大按钮为隐藏
                        if (checkPermission()) {
                            phone_cameraState = openCamera(cameraView,0);
                        }
                    }
                }
                break;
            case R.id.camera_imbtn:
                // 翻转摄像头
                if (phone_cameraState){
                    if (CAMERA_STATE == 0){
                        phone_cameraState = closeCamera(false);
                        phone_cameraState = openCamera(cameraView,1);
                    }else {
                        phone_cameraState = closeCamera(false);
                        phone_cameraState = openCamera(cameraView,0);
                    }
                }else {
                    phone_cameraState = openCamera(cameraView,1);
                    switchover_phpto_btn.setText(R.string.cut_car_camera);
                    scale_num_btn.setVisibility(View.GONE); // 设置放大按钮为隐藏
                }
                break;
            case R.id.scale_phpto_btn:  // 缩放点击控制
                if (centerX == 0 && centerY == 0) {
                    centerX = textureView.getWidth() / 2;
                    centerY = textureView.getHeight() / 2;
                }
                if (scaleFactor > 4 && scaleFactor <= 5) {
                    if (scaleFactor == 5) {
                        scaleFactor = 1;
                    } else {
                        scaleFactor = 5;
                    }
                } else if (scaleFactor > 3 && scaleFactor <= 4) {
                    if (scaleFactor == 4) {
                        scaleFactor = 5;
                    } else {
                        scaleFactor = 4;
                    }
                } else if (scaleFactor > 2 && scaleFactor <= 3) {
                    if (scaleFactor == 3) {
                        scaleFactor = 4;
                    } else {
                        scaleFactor = 3;
                    }
                } else if (scaleFactor > 1 && scaleFactor <= 2) {
                    if (scaleFactor == 2) {
                        scaleFactor = 3;
                    } else {
                        scaleFactor = 2;
                    }
                } else if (scaleFactor == 1) {
                    scaleFactor = 2;
                }
                matrix.setScale(scaleFactor, scaleFactor, centerX, centerY);
                textureView.setTransform(matrix);
                setScaler(scaleFactor);
                vSimple(requireContext(), 30); // 控制手机震动进行反馈
                break;
            case R.id.refresh_btn:
            case R.id.refresh_img_btn:  // 刷新页面
                ObjectrotationAnim(refershImageButton);
                vSimple(requireContext(), 30); // 控制手机震动进行反馈
                break;
            case R.id.save_phpto:  // 保存图片信息
                vSimple(requireContext(), 30); // 控制手机震动进行反馈
                XcApplication.executorServicetor.execute(() -> {
                    if (getBitmap() != null && !IPCamera.contains("null") && !phone_cameraState) {
                        if (scaleFactor > 1) {
                            saveBitmapAsJpg(getSDPath(), getBitmap());
                        } else vlcPlayer.takeSnapShot(0, getSDPath(), 0, 0);
                        FirstActivity.INSTANCE.runOnUiThread(() -> ToastUtil.ShowToast(getContext(), "图片成功保存在 " + getSDPath() + " 路径下"));
                    } else if (phone_cameraState) {
                        saveBitmapAsJpg(getSDPath(), getBitmap());
                        FirstActivity.INSTANCE.runOnUiThread(() -> ToastUtil.ShowToast(getContext(), "图片成功保存在 " + getSDPath() + " 路径下"));
                    } else {
                        FirstActivity.INSTANCE.runOnUiThread(() -> ToastUtil.ShowToast(getContext(), "请连接设备或打开相机！"));
                    }

                });
                break;
            default:
                break;
        }
    }

    static CameraManager cameraManager;
    static CameraDevice.StateCallback cam_stateCallback;
    static CameraDevice opened_camera;
    static CameraCaptureSession.StateCallback cam_capture_session_stateCallback;
    static CameraCaptureSession cameraCaptureSession;
    static CaptureRequest.Builder requestBuilder;
    static CaptureRequest request;
    static Surface texture_surface;

    public boolean openCamera(TextureView cameraTextureView,int id) {
        // 将显示区域设置为可见，避免残影与RTSP冲突显示
        CAMERA_STATE = id;
        cameraTextureView.setVisibility(View.VISIBLE);
        // 锁定屏幕方向，防止旋转后异常旋转，关闭相机后恢复正常
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // 1 创建相机管理器，调用系统相机
        cameraManager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        // 2 准备 相机状态回调对象为后面用
        cam_stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                // 2.1 保存已开启的相机对象
                opened_camera = camera;
                try {
                    // 2.2 构建请求对象（设置预览参数，和输出对象）
                    requestBuilder = opened_camera.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG); // 设置参数：预览
                    requestBuilder.addTarget(texture_surface); // 设置参数：目标容器
                    request = requestBuilder.build();
                    //2.3 创建会话的回调函数，后面用
                    cam_capture_session_stateCallback = new CameraCaptureSession.StateCallback() {
                        @Override  //2.3.1  会话准备好了，在里面创建 预览或拍照请求
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            try {
                                // 2.3.2 预览请求
                                session.setRepeatingRequest(request, null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    };
                    // 设置矩阵变换，将相机获取到的图片进行旋转
                    cameraTextureView.setTransform(changeSize(cameraTextureView));
                    // 2.3 创建会话
                    opened_camera.createCaptureSession(Collections.singletonList(texture_surface), cam_capture_session_stateCallback, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        };
        // 4 检查相机权限并开启相机（传入：要开启的相机ID，ID的第一个一般为后置主摄，状态回调对象）
        if (checkPermission()) {
            try {
                cameraManager.openCamera(cameraManager.getCameraIdList()[id], cam_stateCallback, null);
                return true;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 关闭移动设备相机
     */
    public boolean closeCamera(boolean anima) {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
        }
        // 再关闭相机
        if (null != opened_camera) {
            opened_camera.close();
            clearSurface(cameraView.getSurfaceTexture());
        }
        if (anima){
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(500); // 设置动画时长
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // 动画开始时的操作
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // 动画结束时的操作
                    cameraView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // 动画重复时的操作
                }
            });
            cameraView.startAnimation(alphaAnimation);
            switchover_phpto_btn.setText(R.string.cut_camera);
            // 设置屏幕横向可旋转
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        return false;
    }

    /**
     * 关闭camera的时候，清空TextureView最后一帧，设置颜色为黑色（再次打开相机可正常显示出画面）
     */
    private void clearSurface(SurfaceTexture surface) {
        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        EGL14.eglInitialize(display, version, 0, version, 1);
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE, 0,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(display, attribList, 0, configs, 0, configs.length, numConfigs, 0);

        EGLConfig config = configs[0];
        EGLContext context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        }, 0);
        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(display, config, surface,
                new int[]{
                        EGL14.EGL_NONE
                }, 0);
        EGL14.eglMakeCurrent(display, eglSurface, eglSurface, context);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        EGL14.eglSwapBuffers(display, eglSurface);
        EGL14.eglDestroySurface(display, eglSurface);
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(display, context);
        EGL14.eglTerminate(display);
    }

    /**
     * 设置相机预览页面旋转
     */
    private Matrix changeSize(TextureView textureView) throws CameraAccessException {
        Matrix matrix_camera = new Matrix();
        // 设置旋转，合适角度为270°
        int rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        Log.e(TAG, "changeSize: " + rotation);
        if (rotation == 1) {
            rotation = 270;
        } else rotation = 90;
        matrix_camera.postRotate(rotation, textureView.getWidth() / 2f, textureView.getHeight() / 2f);
        // 获取原图的宽高
        String cameraId = cameraManager.getCameraIdList()[0]; // 获取第一个相机的ID，这里假设只有一个相机
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size previewSize = map.getOutputSizes(SurfaceTexture.class)[0]; // 获取预览尺寸
        // 计算宽高比例
        float originalAspectRatio = (float) textureView.getWidth() / previewSize.getHeight();
        float textureViewAspectRatio = (float) textureView.getHeight() / textureView.getHeight();

        float scaleX = textureViewAspectRatio / originalAspectRatio;
        float scaleY = 1f;

        // 根据缩放比例和旋转进行变换
        matrix_camera.postScale(scaleX, scaleY, textureView.getWidth() / 2f, textureView.getHeight() / 2f);
        return matrix_camera;
    }


    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private boolean checkPermission() {
        if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        } else {
            // 已经有相机权限，可以执行打开相机的操作
            return true;
        }
    }

    /**
     * 用于记录拖拉图片移动的坐标位置
     */
    private Matrix matrix = new Matrix();

    /**
     * 判断屏幕图像区域触摸滑动区域方向，控制摄像头转动
     */
    private class ontouchlistener1 implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO 自动生成的方法存根
            if (cameraConnectState) {
                if ((event.getPointerCount() == 1) && !isScaling && scaleFactor == 1.0f) {
                    int MINLEN = 40;
                    switch (event.getAction()) {
                        // 点击位置坐标
                        case MotionEvent.ACTION_DOWN:
                            // 记录ImageView当前的移动位置
                            x1 = event.getX();
                            y1 = event.getY();
                            break;
                        // 弹起坐标
                        case MotionEvent.ACTION_UP:
                            float x2 = event.getX();
                            float y2 = event.getY();
                            float xx = x1 > x2 ? x1 - x2 : x2 - x1;
                            float yy = y1 > y2 ? y1 - y2 : y2 - y1;
                            float udyy = y2 - y1; // 判断上下正负值确定方向
                            // 判断滑屏趋势
                            if (xx > yy) {
                                if ((x1 > x2) && (yy < MINLEN) && (xx > MINLEN)) {        // left
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 4 + "&onestep=" + 1); //左
                                        }
                                    });
                                } else if ((x1 < x2) && (yy < MINLEN) && (xx > MINLEN)) { // right
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 6 + "&onestep=" + 1); //右
                                        }
                                    });
                                } else if (x1 > x2 && yy > MINLEN && udyy < 0) { // Left-up
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 90 + "&onestep=" + 1);  //左上
                                        }
                                    });
                                } else if (x1 > x2 && yy > MINLEN && udyy > 0) { // Left-down
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 92 + "&onestep=" + 1); //左下
                                        }
                                    });
                                } else if (x1 < x2 && yy > MINLEN && udyy < 0) { // Right-up
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 91 + "&onestep=" + 1);  //右上
                                        }
                                    });
                                } else if (x1 < x2 && yy > MINLEN && udyy > 0) { // Right-down
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 93 + "&onestep=" + 1);  //右下
                                        }
                                    });
                                }
                            } else {
                                if ((y1 > y2) && (yy > MINLEN)) {        // up
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 0 + "&onestep=" + 1);  //上
                                        }
                                    });
                                } else if ((y1 < y2) && (yy > MINLEN)) { // down
                                    XcApplication.executorServicetor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraConntrol.cameraMiscControlPostHttp(IPCamera, cameraConntrol.DECODER_CONTROL, "command=" + 2 + "&onestep=" + 1);  //下
                                        }
                                    });
                                }
                            }
                            x1 = 0;
                            y1 = 0;
                            break;
                    }
                } else if (event.getPointerCount() == 1 && !isScaling && scaleFactor != 1.0f) {
                    switch (event.getAction()) {
                        // 点击位置坐标
                        case MotionEvent.ACTION_DOWN:
                            x1 = event.getX();
                            y1 = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            handleDragGesture(event);
                            break;
                    }
                } else if (event.getPointerCount() == 2) {
                    scaleGestureDetector.onTouchEvent(event);
                }
            }
            return true;
        }
    }

    /**
     * 单个手指平移手势检测
     */
    private void handleDragGesture(MotionEvent event) {
        // 获取触摸点坐标变化值
        float deltaX = event.getX() - x1;
        float deltaY = event.getY() - y1;
        // 获取TextureView的尺寸
        int width = textureView.getWidth();
        int height = textureView.getHeight();
        // 获取实际显示内容的大小
        float dataWidth = width * scaleFactor;
        float dataHeight = height * scaleFactor;
        float[] matrixValues = new float[9];
        Matrix transformMatrix = textureView.getTransform(matrix);
        transformMatrix.getValues(matrixValues);
        // 获取显示内容左上角坐标位置
        float translateX = matrixValues[Matrix.MTRANS_X];
        float translateY = matrixValues[Matrix.MTRANS_Y];
        // 间隔宽度
        float intervalX = (dataWidth - Math.abs(translateX) - width);
        float intervalY = (dataHeight - Math.abs(translateY) - height);
        if (intervalX < Math.abs(deltaX)) { // 右侧防溢出
            if (deltaX < 0) {
                deltaX = -intervalX;
            }
        }
        if (intervalY < Math.abs(deltaY)) {  // 底部防溢出
            if (deltaY < 0) {
                deltaY = -intervalY;
            }
        }
        if ((dataWidth - width) > Math.abs(translateX)) { // 左侧防溢出
            if (deltaX > 0 && (Math.abs(translateX) - deltaX) < 0) {
                deltaX = Math.abs(translateX);
            }
        }
        if ((dataHeight - height) > Math.abs(translateY)) { // 顶部防溢出
            if (deltaY > 0 && (Math.abs(translateY) - deltaY) < 0) {
                deltaY = Math.abs(translateY);
            }
        }
        matrix.postTranslate(deltaX / 15, deltaY / 15);
        textureView.setTransform(matrix);
    }


    private ScaleGestureDetector scaleGestureDetector; // 手势检测类：缩放动作
    private float scaleFactor = 1.0f; // 基础放大值
    boolean isScaling = false; // 记录是否有缩放


    private float centerX; // 获取两个手指的中心点X坐标
    private float centerY; // 获取两个手指的中心点Y坐标

    /**
     * 缩放手势监听
     */
    private class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) { // 开始缩放
            centerX = detector.getFocusX();
            centerY = detector.getFocusY();
            if (scaleFactor < (scaleFactor + scaleFactor * detector.getScaleFactor())) {
                scaleFactor *= detector.getScaleFactor();
            } else {
                scaleFactor += scaleFactor * detector.getScaleFactor();
            }
            scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor, centerX, centerY);
            textureView.setTransform(matrix);
            setScaler(scaleFactor);
            isScaling = true;
            return true;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onScale(ScaleGestureDetector detector) { // 缩放过程中
            if (scaleFactor < (scaleFactor + scaleFactor * detector.getScaleFactor())) {
                scaleFactor *= detector.getScaleFactor();
            } else {
                scaleFactor += scaleFactor * detector.getScaleFactor();
            }
            scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor, centerX, centerY);
            textureView.setTransform(matrix);
            setScaler(scaleFactor);
            isScaling = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) { // 缩放结束
            new Thread(() -> {
                try {
                    // 防止误触
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                isScaling = false;
            }).start();
        }
    }

    /**
     * 设置放大倍数
     */
    @SuppressLint("SetTextI18n")
    private void setScaler(float scaleFactor) {
        if (scaleFactor != 1.0f) {
            if (scaleFactor > 4.5 && scaleFactor <= 5) {
                scale_num_btn.setText("放大×5");
            } else if (scaleFactor > 3.5 && scaleFactor <= 4) {
                scale_num_btn.setText("放大×4");
            } else if (scaleFactor > 2.5 && scaleFactor <= 3) {
                scale_num_btn.setText("放大×3");
            } else if (scaleFactor > 1.5 && scaleFactor <= 2) {
                scale_num_btn.setText("放大×2");
            }
        } else {
            scale_num_btn.setText("放大×1");
        }
    }

    /**
     * 刷新按钮实现顺时针360度
     */
    private void ObjectrotationAnim(View view) {
        //构造ObjectAnimator对象的方法
        EventBus.getDefault().post(new DataRefreshBean(1));
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0.0F, 360.0F);// 设置顺时针360度旋转
        animator.setDuration(1500);//设置旋转时间
        animator.start();//开始执行动画（顺时针旋转动画）
    }

    //这里处理传过来的数据
    @SuppressLint("HandlerLeak")
    public Handler myHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle.getString("result").equals("ok")) {
                vSimple(requireContext(), 30); // 控制手机震动进行反馈
            }
        }
    };


    /**
     * 控制手机震动
     */
    public static void vSimple(Context context, int millisecond) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(millisecond);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vlcPlayState) {  // 进入暂停状态后，屏幕还原时进行恢复播放
            vlcPlayer.play();
        }
        // 在活动处于前台时继续保持屏幕常亮
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged: " + newConfig.orientation);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (phone_cameraState) { // 屏幕关闭时，关闭相机预览
            phone_cameraState = closeCamera(true);
        } else if (playRTSPstate && !vlcPlayState) { // 屏幕关闭时，暂停视频流数据播放
            vlcPlayer.stop();
            vlcPlayer.pause();
            vlcPlayState = true;
        }
        // 在活动不可见时清除不息屏标志
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (vlcPlayer != null) {
            vlcPlayer.stop();
            vlcPlayer.release();
        }
    }


}
