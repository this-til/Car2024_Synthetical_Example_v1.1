package car.bkrc.com.car2024.Utils.OtherUtil;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import car.bkrc.com.car2024.ActivityView.FirstActivity;

public class WiFiStateUtil {

    private static final String TAG = "Main";

    private WifiManager wifiManager;
    private Context context;
    private WifiStateBroadcastReceive mReceive;

    public WiFiStateUtil(Context context) {

        this.context = context;
        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean getWifiState() {
        // 获取蓝牙状态
        return wifiManager.isWifiEnabled();
    }

    public boolean openWifi() {
        if (getWifiState()) return true;
        // 打开蓝牙
        return wifiManager.setWifiEnabled(true);
    }

    public boolean cloaseWifi() {
        if (!getWifiState()) return true;
        // 关闭蓝牙
        return wifiManager.setWifiEnabled(false);
    }


    public void registerWifiReceiver() {
        if (mReceive == null) {
            mReceive = new WifiStateBroadcastReceive();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(wifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(mReceive, intentFilter);
    }

    public void unregisterWifiReceiver() {
        if (mReceive != null) {
            context.unregisterReceiver(mReceive);
            mReceive = null;
        }
    }

    class WifiStateBroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiState = intent.getIntExtra(wifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    ToastUtil.ShowToast(context,"WiFi已关闭,请检查设备连接状态");
                    Log.i(TAG, "onReceive: " + "Wifi已关闭");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    ToastUtil.ShowToast(context,"WiFi正在关闭...");
                    Log.i(TAG, "onReceive: " + "WiFi正在关闭...");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.i(TAG, "onReceive: " + "WiFi已打开");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.i(TAG, "onReceive: " + "WiFi正在打开...");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:

                    break;
                default:
                    break;
            }
        }
    }

    public boolean wifiInit() {
        // 得到服务器的IP地址
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiManager != null;
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        FirstActivity.IPCar = Formatter.formatIpAddress(dhcpInfo.gateway);
        FirstActivity.IPmask = Formatter.formatIpAddress(dhcpInfo.netmask);
        if (FirstActivity.IPCar.equals("0.0.0.0")
                || FirstActivity.IPCar.equals("127.0.0.1")) return false;
        return true;
    }

}