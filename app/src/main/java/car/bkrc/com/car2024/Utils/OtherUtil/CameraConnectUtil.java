package car.bkrc.com.car2024.Utils.OtherUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.MessageBean.DataRefreshBean;
import car.bkrc.com.car2024.Utils.CameraUtils.CameraSearchService;


public class CameraConnectUtil {

    public CameraConnectUtil(Context context){
        this.context = context;
    }

    private Context context;

    public void cameraInit() {
        //广播接收器注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(A_S);
        context.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    public void cameraStopService(){
        Intent intent = new Intent(context, CameraSearchService.class);
        context.stopService(intent);
    }

    public static final String A_S = "com.a_s";
    public BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            FirstActivity.IPCamera = arg1.getStringExtra("IP");
            FirstActivity.purecameraip = arg1.getStringExtra("pureip");
            Log.e("camera ip::", "  " + FirstActivity.IPCamera);
            EventBus.getDefault().post(new DataRefreshBean(2));
            context.unregisterReceiver(this);
            if (arg1 != null) {
                Bundle extras = arg1.getExtras();
                if (extras != null) {
                    for (String key : extras.keySet()) {
                        if (extras.get(key) instanceof String) {
                            String value = extras.getString(key);
                            Log.d("IntentData", key + ": " + value);
                        }
                    }
                }
            }
        }
    };


    // 搜索摄像cameraIP
    public void search() {
        Intent intent = new Intent(context, CameraSearchService.class);
        context.startService(intent);
    }

    public void destroy(){
        try {
            context.unregisterReceiver(myBroadcastReceiver);
        }catch (RuntimeException ignored){

        }
    }

}
