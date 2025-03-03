package car.bkrc.com.car2024.ActivityView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import car.bkrc.com.car2024.MessageBean.DataRefreshBean;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.CameraUtils.CameraSearchService;
import car.bkrc.com.car2024.Utils.CameraUtils.XcApplication;
import car.bkrc.com.car2024.Utils.OtherUtil.CameraConnectUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.WiFiStateUtil;
import car.bkrc.com.car2024.Utils.dialog.DialogUtils;
import car.bkrc.com.car2024.Utils.dialog.ShowDialog;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText device_edit = null;
    private EditText login_edit = null;
    private EditText passwd_edit = null;
    private LinearLayout wifi_back, uart_back;

    private Button bt_connect = null;
    private ImageView rememberbox = null, uart_state_image, wifi_state_image;
    private TextView wifi_box = null, uart_box = null;
    private boolean passwordState = false;
    private ProgressDialog dialog = null;

    void Request() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Log.e("WIFISSID", getConnectWifiSsid());
            if ("<unknown ssid>".equals(getConnectWifiSsid())) {
                ToastUtil.ShowToast(this, "当前未接入到Wi-Fi网络中，请连接平台Wi-Fi");
            } else if (getConnectWifiSsid().contains("BKRC")) {
                ToastUtil.ShowToast(this, "当前连接WiFi：" + getConnectWifiSsid().replaceAll("\"", ""));
            } else {
                ToastUtil.ShowToast(this, "请检查WiFi是否连接正确！");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Request30();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    void Request30() {
        if (checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 4);
        }
    }

    /**
     * 判断是否首次启动
     */
    private boolean firstRun() {
        SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
        String first_run = sharedPreferences.getString("First", "2023-V2.0");
        if (first_run.equals("2024-V2.0")) {
            return false;
        } else {
            sharedPreferences.edit().putString("First", "2024-V2.0").apply();
            return true;
        }
    }

    /**
     * 应用升级提醒弹窗
     */
    private void upDialog() {
        ShowDialog showDialog = new ShowDialog();
        showDialog.show(LoginActivity.this, "应用更新说明");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO request success
                if (Objects.equals(getConnectWifiSsid(), "<unknown ssid>")) {
                    ToastUtil.ShowToast(this, "当前未接入到Wi-Fi网络中，请连接平台Wi-Fi");
                } else if (getConnectWifiSsid().contains("BKRC")) {
                    ToastUtil.ShowToast(this, "当前连接WiFi：" + getConnectWifiSsid().replaceAll("\"", ""));
                } else {
                    ToastUtil.ShowToast(this, "请检查WiFi是否连接正确！");
                }
            }
        }
    }

    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowInsetsControllerCompat wic = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (wic != null) {
            // true表示Light Mode，状态栏字体呈黑色，反之呈白色
            wic.setAppearanceLightStatusBars(true);
        }

        // 判断是否是平板
        if (isPad(this)) {
            setContentView(R.layout.activity_login);
        } else {
            setContentView(R.layout.activity_login_mobilephone);
        }
        EventBus.getDefault().register(this); // EventBus消息注册
        CameraConnectUtil cameraConnectUtil = new CameraConnectUtil(this);
        findViews();  //控件初始化
        cameraConnectUtil.cameraInit();//摄像头初始化
        Request();
        if (firstRun()) {
            upDialog();
        }
    }


    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     *
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void findViews() {
        device_edit = findViewById(R.id.deviceid);
        login_edit = findViewById(R.id.loginname);
        passwd_edit = findViewById(R.id.loginpasswd);
        Button bt_reset = findViewById(R.id.reset);
        bt_connect = findViewById(R.id.connect);
        rememberbox = findViewById(R.id.remember);
        wifi_state_image = findViewById(R.id.wifi_image);
        uart_state_image = findViewById(R.id.uart_image);
        wifi_box = findViewById(R.id.wifi_each);
        uart_box = findViewById(R.id.uart_each);
        wifi_back = findViewById(R.id.wifi_back);
        uart_back = findViewById(R.id.uart_back);
        if (cameraDataCheck(CAMERAUSER).equals("")) {
            login_edit.setText("admin");
            passwd_edit.setText("888888");
        } else {
            login_edit.setText("admin");
            passwd_edit.setText(cameraDataCheck(CAMERAPS));
        }
        bt_reset.setOnClickListener(this);
        bt_connect.setOnClickListener(this);
        rememberbox.setOnClickListener(v -> {
            vSimple(getApplicationContext(), 30); // 控制手机震动进行反馈
            setPasswordState(!passwordState);
        });

        wifi_back.setOnClickListener(v -> {
            vSimple(getApplicationContext(), 30); // 控制手机震动进行反馈
            wifi_box.setTextColor(getResources().getColor(R.color.switch_black));
            uart_box.setTextColor(getResources().getColor(R.color.shift_color_gray));
            uart_state_image.setBackground(getResources().getDrawable(R.drawable.ic_uart_off));
            wifi_state_image.setBackground(getResources().getDrawable(R.drawable.ic_wifi));
            wifi_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_on));
            uart_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_off));
            XcApplication.isserial = XcApplication.Mode.SOCKET;
        });
        uart_back.setOnClickListener(v -> {
            vSimple(getApplicationContext(), 30); // 控制手机震动进行反馈
            uart_box.setTextColor(getResources().getColor(R.color.switch_black));
            wifi_box.setTextColor(getResources().getColor(R.color.shift_color_gray));
            uart_state_image.setBackground(getResources().getDrawable(R.drawable.ic_uart));
            wifi_state_image.setBackground(getResources().getDrawable(R.drawable.ic_wifi_off));
            wifi_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_off));
            uart_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_on));
            XcApplication.isserial = XcApplication.Mode.USB_SERIAL;
            if (cameraDataCheck(CAMERAIP).equals("")) {
                ToastUtil.ShowToast(this, "请设置CameraIP");
                DialogUtils.showCompleteDialog(this, "设置CameraIP", cameraDataCheck(CAMERAIP));
            }
        });
        uart_back.setOnLongClickListener(v -> {
            vSimple(getApplicationContext(), 30); // 控制手机震动进行反馈
            DialogUtils.showCompleteDialog(LoginActivity.this, "设置CameraIP", cameraDataCheck(CAMERAIP));
            return false;
        });
    }

    private final String CAMERAIP = "CameraIP";
    private final String CAMERAUSER = "CameraUser";
    private final String CAMERAPS = "CameraPs";

    /**
     * 判断本地保存摄像头的相关信息
     *
     * @param key CameraIP:查询本地是否保存了串口通信模式的IP地址
     *            CameraUser：查询是否保存了摄像头访问账户
     *            CameraPS：查询是否保存了摄像头访问账户的密码
     */
    private String cameraDataCheck(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences("IPCheck", 0);
        String first_run = sharedPreferences.getString(key, "");
        if (first_run.equals("")) {
            return "";
        } else {
            return first_run;
        }
    }

    /**
     * 保存指定数据到本地
     */
    private void rememberData(String key, String data) {
        SharedPreferences sharedPreferences = getSharedPreferences("IPCheck", 0);
        sharedPreferences.edit().putString(key, data).apply();
    }

    /**
     * 设置密码隐藏/显示状态
     *
     * @param state state = true : 显示
     *              state = false ： 隐藏
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void setPasswordState(boolean state) {
        if (state) {
            passwd_edit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            rememberbox.setBackground(getResources().getDrawable(R.drawable.ic_on));
            passwordState = true;
        } else {
            passwd_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
            rememberbox.setBackground(getResources().getDrawable(R.drawable.ic_off));
            passwordState = false;
        }
    }

    private String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.reset) {
            vSimple(getApplicationContext(), 30); // 控制手机震动进行反馈
            device_edit.setText("");
            setPasswordState(false);
        } else if (view.equals(bt_connect)) {
            vSimple(getApplicationContext(), 30); // 控制手机震动进行反馈
            if ((XcApplication.isserial == XcApplication.Mode.USB_SERIAL) && cameraDataCheck(CAMERAIP).equals("")) {
                ToastUtil.ShowToast(this, "请设置CameraIP");
                DialogUtils.showCompleteDialog(this, "设置CameraIP", cameraDataCheck(CAMERAIP));
            } else {
                if (passwd_edit.getText().toString().equals("")) {
                    ToastUtil.ShowToast(this, "请输入账户密码（摄像头密码，默认为：888888）！");
                } else {
                    new Thread(() -> {
                        rememberData(CAMERAUSER, login_edit.getText().toString());
                        rememberData(CAMERAPS, passwd_edit.getText().toString());
                    }).start();
                    dialog = new ProgressDialog(this);
                    dialog.setMessage("撸起袖子加载中...");
                    dialog.show();
                    if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
                        useNetwork();
                    } else {
                        useUart();
                    }
                }
            }
        }
    }

    /**
     * 接收Eventbus消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 2) {
            startFirstActivity();
        }
    }

    // 搜索摄像cameraIP
    private void search() {
        Intent intent = new Intent(LoginActivity.this, CameraSearchService.class);
        startService(intent);
    }

    private void useUart() {
        // 搜索摄像头然后启动摄像头
        search();
    }

    private void useNetwork() {
        //2.
        if (new WiFiStateUtil(this).wifiInit()) {
            //WiFi初始化成功
            search();
        } else {
            dialog.cancel();
            ToastUtil.ShowToast(this, "平台未连接，正在跳过...");
            new WiFiStateUtil(this).openWifi();
            search();
        }
    }

    private void startFirstActivity() {
        dialog.cancel();
        startActivity(new Intent(LoginActivity.this, FirstActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this); // EventBus消息注销
        if (dialog != null) {
            dialog.cancel();
        }
        Log.e("LoginActivity", "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("LoginActivity", "onRestart");
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

