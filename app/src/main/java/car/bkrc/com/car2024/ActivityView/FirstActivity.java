package car.bkrc.com.car2024.ActivityView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hyperai.hyperlpr3.HyperLPR3;
import com.hyperai.hyperlpr3.bean.HyperLPRParameter;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import car.bkrc.com.car2024.DataProcessingModule.ConnectTransport;
import car.bkrc.com.car2024.FragmentView.LeftFragment;
import car.bkrc.com.car2024.FragmentView.RightMoveControlFragment;
import car.bkrc.com.car2024.FragmentView.RightInfraredFragment;
import car.bkrc.com.car2024.FragmentView.RightOtherFragment;
import car.bkrc.com.car2024.FragmentView.RightZigbeeFragment;
import car.bkrc.com.car2024.MessageBean.StateChangeBean;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.CameraUtils.XcApplication;
import car.bkrc.com.car2024.Utils.OtherUtil.CameraConnectUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.Utils.dialog.ShowDialog;
import car.bkrc.com.car2024.Utils.dialog.Transparent;
import car.bkrc.com.car2024.ViewAdapter.ViewPagerAdapter;

public class FirstActivity extends AppCompatActivity {
    private ViewPager viewPager;
    @SuppressLint("StaticFieldLeak")
    public static FirstActivity INSTANCE;
    @SuppressLint("StaticFieldLeak")
    public static ConnectTransport Connect_Transport;
    // 设备ip和子网掩码
    public static String IPCar, IPmask;
    // 带端口号IP地址
    public static String IPCamera = null;
    // 不带端口号IP地址
    public static String purecameraip = null;
    // 主从车控制标志位
    public static boolean chief_control_flag = true;
    // 串口通信广播
    public static Handler recvhandler = null;
    // 按钮和menu同步广播，确保滑动页面时和点击menu时均可切换对方的状态
    public static Handler but_handler;
    // 适配器，进行选择填充
    private ViewPager mLateralViewPager;
    private CameraConnectUtil cameraConnectUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_first);
        INSTANCE = this;
        // 加载页面
        initAll();
        }
    private final LeftFragment leftFragment = new LeftFragment();

    private void initAll() {
        but_handler = button_handler;  //用于leftfragment中隐藏的按钮和标题栏中的menu同步
        if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL) {  //竞赛平台和a72通过usb转串口通信
            mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS); //启动usb的识别和获取
            Transparent.showLoadingMessage(this, "正在拼命追赶串口……", false);//启动旋转效果的对话框，实现usb的识别和获取
        }
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Button auto_btn = findViewById(R.id.auto_drive_btn);
        ImageView logo_view = findViewById(R.id.up_loading_iv);
        auto_btn.setOnClickListener(v -> {
            autoDriveAction();
            vSimple(getApplicationContext(),30); // 控制手机震动进行反馈
        });
        logo_view.setOnClickListener(v -> {
            ShowDialog showDialog = new ShowDialog();
            showDialog.show(FirstActivity.this, "应用更新说明");
            vSimple(getApplicationContext(),30); // 控制手机震动进行反馈
        });
        viewPager = findViewById(R.id.viewpager);//使用viewPager实现页面滑动效果
        viewPager.setOffscreenPageLimit(3);

        // 底部导航栏
        nativeView();
        Connect_Transport = new ConnectTransport();    //实例化连接类
        cameraConnectUtil = new CameraConnectUtil(this);
        addFragment(leftFragment, R.id.left_fragment);
        //初始化车牌识别
        // 车牌识别算法配置参数
        HyperLPRParameter parameter = new HyperLPRParameter()
                .setDetLevel(HyperLPR3.DETECT_LEVEL_LOW)
                .setMaxNum(1)
                .setRecConfidenceThreshold(0.85f);
        // 初始化(仅执行一次生效)
        HyperLPR3.getInstance().init(this, parameter);
        // 进行手机访问权限的校验
        // Check if the READ_EXTERNAL_STORAGE permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            initOpenCV();
        }
    }


    /**
     * 加载页面的fragment
     */
    private void addFragment(Fragment showFragment, int view) {
        FragmentManager manager = this.getSupportFragmentManager();
        if (!showFragment.isAdded()) {
            manager.beginTransaction().setCustomAnimations(R.anim.across_translate_into, R.anim.across_translate_out)
                    .replace(view, showFragment, "leftFragment").show(showFragment).commit();
        } else {
            manager.beginTransaction().setCustomAnimations(R.anim.across_translate_into, R.anim.across_translate_out).
                    show(showFragment).commit();
        }
    }

    /**
     * 初始化OpenCV
     */
    private void initOpenCV() {
        if (OpenCVLoader.initDebug()) {
            // 使用JniLibs文件夹下的动态库初始化OpenCV
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 拿到权限
                initOpenCV();
            } else {
                Log.e("TAG", "onRequestPermissionsResult: 没拿到权限");
                initOpenCV();
            }
        }

    }


    /**
     * 设置底部导航栏
     */
    private void nativeView() {
        BottomNavigationView navigation = findViewById(R.id.bottomNavigation);
        navigation.setItemIconTintList(null);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);
        mLateralViewPager = findViewById(R.id.viewpager);//获取到ViewPager
        setupViewPager(viewPager);                      //加载对应页面的fragment
        //ViewPager的监听
        final BottomNavigationView finalNavigation = navigation;
        mLateralViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                finalNavigation.getMenu().getItem(position).setChecked(true);
                //写滑动页面后做的事，使每一个fragmen与一个page相对应
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * menu点击监听，点击对应的item切换不同页面
     */
    private final NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = new NavigationBarView.OnItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            vSimple(getApplicationContext(),30); // 控制手机震动进行反馈
            switch (item.getItemId()) {
                case R.id.home_page_item:
                    mLateralViewPager.setCurrentItem(0);
                    return true;
                case R.id.scene_setting_item:
                    mLateralViewPager.setCurrentItem(1);
                    return true;
                case R.id.device_manage_item:
                    mLateralViewPager.setCurrentItem(2);
                    return true;
                case R.id.personal_center_item:
                    mLateralViewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        }
    };

    /**
     * 填充fragment到对应的适配器
     * @param viewPager 需要填充数据的适配器
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(RightMoveControlFragment.getInstance());
        adapter.addFragment(RightZigbeeFragment.getInstance());
        adapter.addFragment(RightInfraredFragment.getInstance());
        adapter.addFragment(RightOtherFragment.getInstance());
        viewPager.setAdapter(adapter);
    }


    private Menu toolmenu;
    /**
     * activity创建时创建菜单Menu，固定写法
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_rightitem, menu);
        toolmenu = menu;
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //菜单项监听
        vSimple(getApplicationContext(),30); // 控制手机震动进行点击反馈
        int id = item.getItemId();
        switch (id) {
            case R.id.car_status:
                if (item.getTitle().equals(getResources().getText(R.string.main_status))) {
                    item.setTitle(getResources().getText(R.string.follow_status));
                    Connect_Transport.stateChange(2);
                    EventBus.getDefault().post(new StateChangeBean(0));
                } else if (item.getTitle().equals(getResources().getText(R.string.follow_status))) {
                    item.setTitle(getResources().getText(R.string.main_status));
                    Connect_Transport.stateChange(1);
                    EventBus.getDefault().post(new StateChangeBean(1));
                }
                break;
            case R.id.car_control:
                if (item.getTitle().equals(getResources().getText(R.string.main_control))) {
                    chief_control_flag = true;
                    item.setTitle(getResources().getText(R.string.follow_control));
                    EventBus.getDefault().post(new StateChangeBean(2));
                    Connect_Transport.TYPE = 0xAA;
                } else if (item.getTitle().equals(getResources().getText(R.string.follow_control))) {
                    chief_control_flag = false;
                    item.setTitle(getResources().getText(R.string.main_control));
                    EventBus.getDefault().post(new StateChangeBean(3));
                    Connect_Transport.TYPE = 0x02;
                }
                break;
            case R.id.clear_coded_disc:
                Connect_Transport.clear();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // 让菜单同时显示图标和文字
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * 自动驾驶的点击弹窗提示
     */
    private void autoDriveAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置Title的内容
        builder.setIcon(R.mipmap.rc_logo);
        builder.setTitle("温馨提示");
        // 设置Content来显示一个信息
        builder.setMessage("请确认是否开始自动驾驶！\n (请确认平台程序中是否包含自动驾驶接口程序！若无接口程序，可忽略此操作)");
        // 设置一个PositiveButton
        builder.setPositiveButton("开始", (dialog, which) -> {
            Connect_Transport.autoDrive();
            ToastUtil.ShowToast(this,"开始自动驾驶，请检查车辆周围环境！");
            vSimple(getApplicationContext(),30); // 控制手机震动进行反馈
        });
        // 设置一个NegativeButton
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
            vSimple(getApplicationContext(),30); // 控制手机震动进行反馈
        });
        builder.show();
    }

    @SuppressLint("HandlerLeak")
    private final Handler button_handler = new Handler(Looper.getMainLooper())  //实现menu和leftfragment中的三个按钮同步
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            vSimple(getApplicationContext(),30); // 控制手机震动进行反馈
            switch (msg.what) {
                case 11:
                    toolmenu.getItem(1).setTitle(getResources().getText(R.string.follow_status));
                    break;
                case 22:
                    toolmenu.getItem(1).setTitle(getResources().getText(R.string.main_status));
                    break;
                case 33:
                    toolmenu.getItem(2).setTitle(getResources().getText(R.string.follow_control));
                    break;
                case 44:
                    toolmenu.getItem(2).setTitle(getResources().getText(R.string.main_control));
                    break;
                default:
                    break;

            }
        }
    };


    //------------------------------------------------------------------------------------------
    //获取和实现usb转串口的通信，实现A72和竞赛平台的串口通信
    public static UsbSerialPort sPort = null;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.e(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {   //新的数据
                    FirstActivity.this.runOnUiThread(() -> {
                        Message msg = recvhandler.obtainMessage(1, data);
                        msg.sendToTarget();
                        FirstActivity.this.updateReceivedData();
                    });
                }
            };

    protected void controlusb() {
        Log.e(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            ToastUtil.ShowToast(this,"没有串口驱动！");
        } else {
            openUsbDevice();
            if (connection == null) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                ToastUtil.ShowToast(this,"串口驱动失败！");
                return;
            }
            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                ToastUtil.ShowToast(this,"串口驱动错误！");
                try {
                    sPort.close();
                } catch (IOException ignored) {
                }
                sPort = null;
                return;
            }
        }
        onDeviceStateChange();
        Transparent.dismiss();//关闭加载对话框
    }


    // 在打开usb设备前，弹出选择对话框，尝试获取usb权限
    private void openUsbDevice() {
        tryGetUsbPermission();
    }
    // 定义USB权限请求的Action字符串
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    // USB设备连接对象
    private UsbDeviceConnection connection;
    // 尝试获取USB权限，通过注册广播接收器和请求权限意图
    private void tryGetUsbPermission() {

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionActionReceiver, filter);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);

        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            if (mUsbManager.hasPermission(usbDevice)) {
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
            } else {
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }
    // 获取到USB权限后的处理，显示设备信息并打开设备
    private void afterGetUsbPermission(UsbDevice usbDevice) {

        ToastUtil.ShowToast(this,"Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId());
        doYourOpenUsbDevice(usbDevice);
    }
    // 执行打开USB设备的操作
    private void doYourOpenUsbDevice(UsbDevice usbDevice) {
        connection = mUsbManager.openDevice(usbDevice);
    }
    // USB权限广播接收器，用于接收权限请求的结果
    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if (null != usbDevice) {
                            afterGetUsbPermission(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        ToastUtil.ShowToast(getApplicationContext(),"Permission denied for device" + usbDevice);
                    }
                }
            }
        }
    };
    // 停止串行I/O管理器
    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.e(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }
    // 启动串行I/O管理器
    private void startIoManager() {
        if (sPort != null) {
            Log.e(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener); //添加监听
            mExecutor.submit(mSerialIoManager); //在新的线程中监听串口的数据变化
        }
    }
    // 当设备状态改变时调用，停止并重新启动I/O管理器
    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
    // 更新接收到的数据
    private void updateReceivedData() {
        //  Log.e("read data is ：：","   "+message);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraConnectUtil.destroy();
        if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL) {
            try {
                unregisterReceiver(mUsbPermissionActionReceiver);
                sPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException ignored) {

            }
            sPort = null;
        } else if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            Connect_Transport.destory();
        }
    }

    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
    private UsbManager mUsbManager;
    private final List<UsbSerialPort> mEntries = new ArrayList<>();
    private final String TAG = FirstActivity.class.getSimpleName();

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_REFRESH) {
                refreshDeviceList();
            } else {
                super.handleMessage(msg);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler usbHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                try {
                    useUsbtoserial();
                } catch (IndexOutOfBoundsException e) {
                    Transparent.dismiss();//关闭加载对话框
                    ToastUtil.ShowToast(getApplicationContext(),"串口通信失败，请检查设备连接状态！");
                }
            }
        }
    };

    /**
     * 使用手机串口
     */
    private void useUsbtoserial() {
        final UsbSerialPort port = mEntries.get(0);  //A72上只有一个 usb转串口，用position =0即可
        final UsbSerialDriver driver = port.getDriver();
        final UsbDevice device = driver.getDevice();
        final String usbid = String.format("Vendor %s  ，Product %s",
                HexDump.toHexString((short) device.getVendorId()),
                HexDump.toHexString((short) device.getProductId()));
        Message msg = LeftFragment.showidHandler.obtainMessage(22, usbid);
        msg.sendToTarget();
        FirstActivity.sPort = port;
        controlusb();  //使用usb功能
    }

    @SuppressLint("StaticFieldLeak")
    private void refreshDeviceList() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params) {
                Log.e(TAG, "Refreshing device list ...");
                Log.e("mUsbManager is :", "  " + mUsbManager);
                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

                final List<UsbSerialPort> result = new ArrayList<>();
                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    Log.e(TAG, String.format("+ %s: %s port%s",
                            driver, ports.size(), ports.size() == 1 ? "" : "s"));
                    result.addAll(ports);
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {
                mEntries.clear();
                mEntries.addAll(result);
                usbHandler.sendEmptyMessage(2);
                Log.e(TAG, "Done refreshing, " + mEntries.size() + " entries found.");
            }
        }.execute((Void) null);
    }

    /**
     * 控制手机震动
     */
    public static void vSimple(Context context, int millisecond) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(millisecond);
    }

}
