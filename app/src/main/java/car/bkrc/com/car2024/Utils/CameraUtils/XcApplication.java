package car.bkrc.com.car2024.Utils.CameraUtils;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bkrc_logobkrc on 2018/1/10.
 */

public class XcApplication extends Application {

    public enum Mode {
        SOCKET, SERIAL, USB_SERIAL
    }
    public  static String cameraip="192.168.1.101:81";

    private static XcApplication app;

    public static ExecutorService executorServicetor = Executors.newCachedThreadPool();

    public static Mode isserial =Mode.SOCKET;

    public static XcApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app =this;
        Intent ipintent = new Intent();
        //ComponentName的参数1:目标app的包名,参数2:目标app的Service完整类名
        ipintent.setComponent(new ComponentName("com.android.settings", "com.android.settings.ethernet.CameraInitService"));
        //设置要传送的数据
        ipintent.putExtra("purecameraip", "0.0.0.0");
        startService(ipintent);   //摄像头设为静态192.168.16.20时，可以不用发送
    }
}
