package car.bkrc.com.car2024.Utils.CameraUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import car.bkrc.com.car2024.FragmentView.LeftFragment;

/**
 * .
 * <p>
 * Created by bkrcd on 2023/8/24.
 */
public class CameraConntrol {

    // 网络摄像头CGI接口详细操作文档请查阅CGI接口手册，部分功能厂商未开放则无法实现
    public final String REBOOT = "reboot"; // 重启设备  CGI接口：/reboot.cgi[?user=&pwd=&loginuse=&loginpas=]
    public final String RESTORE_FACTORY = "restore_factory"; // 恢复出厂设置  CGI接口：/restore_factory.cgi[?user=&pwd=&loginuse=&loginpas=]
    public final String SET_MISC = "set_misc"; // 设置摄像机的云台杂项参数   CGI接口：/set_misc.cgi?led_mode=&ptz_preset=&ptz_run_times=&ptz_patrol_rate=
    // &ptz_patrol_up_rate=&ptz_patrol_down_rate=&ptz_patrol_left_rate=&ptz_patrol_right_rate=&ptz_
    // dispreset=[&loginuse=&loginpas=&next_url=]
    public final String GET_MISC = "get_misc"; // 获取设备云台相关参数  CGI接口：/get_misc.cgi[?user=&pwd=&loginuse=&loginpas=]
    public final String GET_CAMERA_PARAMS = "get_camera_params";  // 获取设备视频图像相关参数  CGI接口：/get_camera_params.cgi[?user=&pwd=&loginuse=&loginpas=]
    public final String GET_PARAMS = "get_params"; // 获取设备参数的接口 CGI接口：/get_params.cgi[?user=&pwd=&loginuse=&loginpas=]
    public final String CAMERA_CONTROL = "camera_control"; // 图像传感器参数控制   CGI接口：/get_misc.cgi[?user=&pwd=&loginuse=&loginpas=]
    public final String DECODER_CONTROL = "decoder_control"; // 云台控制  CGI接口：/decoder_control.cgi?command=&onestep=&sit=[&loginuse=&loginpas=&next_url=]
    public final String TRANS_CMD_STRING = "trans_cmd_string";// 人脸检测接口数据回传头

    public final String CMD_CHANNEL_HEAD = "cmd_channel_head";// 通用控制接口数据发送头
    public final String SET_NETWORK = "set_network";// 设置摄像头网络接口包头  CGI接口：/set_network.cgi?ipaddr=&mask=&gateway=&dns1=&dns2=&dhcp=&port=[&loginuse=&loginpas=&next_url=]
    private Context context;
    private Handler reHandler;
    public CameraConntrol(Context context,Handler handler){
        this.context = context;
        this.reHandler = handler;
    }


    // 数据输入流接收
    InputStreamReader inputStreamReader;
    BufferedReader bufferedReader;

    /**
     * 摄像头控制函数
     *
     * @param IP      IP地址
     * @param command 控制命令 可设置指令和参数
     */
    public void cameraMiscControlPostHttp(String IP, String cgi, String command) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("IPCheck", 0);
        String cameraUser = sharedPreferences.getString("CameraUser", "");
        String cameraUserPs = sharedPreferences.getString("CameraPs", "");
        if (LeftFragment.isCameraConnectState()) {
            String httpUrl2 = "http://" + IP + "/" + cgi + ".cgi?" + command;
            if (Objects.equals(cgi, DECODER_CONTROL) || Objects.equals(cgi, SET_NETWORK)) {
                httpUrl2 += "&loginuse=" + cameraUser + "&loginpas=" + cameraUserPs;
            } else httpUrl2 += "&user=" + cameraUser + "&pwd=" + cameraUserPs + "&loginuse=" + cameraUser + "&loginpas=" + cameraUserPs ;
            String finalHttpUrl = httpUrl2;
            Log.e("TAG", "cameraMiscControlPostHttp: " + httpUrl2);
            new Thread(() -> {
                URL getUrl = null;
                try { // 转换为url数据
                    getUrl = new URL(finalHttpUrl);
                } catch (MalformedURLException var9) {
                    var9.printStackTrace();
                }
                try { // 发起请求并对返回数据进行读取
                    assert getUrl != null;
                    HttpURLConnection urlConnection = (HttpURLConnection) getUrl.openConnection();
                    urlConnection.connect();
                    InputStream in = urlConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String patternString  = "var\\s+(\\w+)=(\\d+|\"[^\"]*\");";
                        // 在当前行中查找匹配项
                        Pattern pattern = Pattern.compile(patternString);
                        Matcher matcher = pattern.matcher(line);
                        // 如果找到匹配项，则提取属性名称和属性值
                        if (matcher.find()) {
                            String propertyName = matcher.group(1);
                            String value = matcher.group(2);
                            Thread.sleep(500);
                            Message message = Message.obtain();
                            // 判断值的类型并存储
                            Bundle bundle = new Bundle();
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                bundle.putString(propertyName, value.substring(1, value.length() - 1));
                            } else if (value.matches("\\d+")) {
                                bundle.putInt(propertyName, Integer.parseInt(value));
                            } else {
                                // 其他类型的值可以根据需要进行处理
                                bundle.putString(propertyName, value);
                            }
                            if (Objects.equals(cgi, CAMERA_CONTROL)) {
                                message.what = 10;
                            } else if (Objects.equals(cgi, SET_MISC)) {
                                if (command.contains("osdenable")){
                                    message.what = 10;
                                }else {
                                    message.what = 20;
                                }
                            } else if (Objects.equals(cgi, SET_NETWORK)){
                                message.what = 30;
                            }else if (Objects.equals(cgi, TRANS_CMD_STRING)){
                                message.what = 40;
                            }else message.what = 100;
                            message.setData(bundle);
                            reHandler.sendMessage(message);
                            // 打印属性信息到Logcat或做其他处理
                            android.util.Log.d("MainActivity", "Property Name: " +
                                    propertyName + ", Property Value: " + value);
                        }
                    }
                    urlConnection.disconnect();
                } catch (IOException | InterruptedException var8) {
                    var8.printStackTrace();
                }
            }).start();
        }
    }


    /**
     * 获取摄像头状态函数
     *
     * @param IP IP地址
     */
    public void getCameraStatePostHttp(String IP, String cgi,String other) {
        if (LeftFragment.isCameraConnectState()) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("IPCheck", 0);
            String cameraUser = sharedPreferences.getString("CameraUser", "");
            String cameraUserPs = sharedPreferences.getString("CameraPs", "");
            String httpUrl;
            if (!Objects.equals(other, "")){
                httpUrl = "http://" + IP + "/" + cgi + ".cgi?" + other + "&loginuse=" + cameraUser + "&loginpas=" + cameraUserPs;
            }else {
                httpUrl = "http://" + IP + "/" + cgi + ".cgi?user=" + cameraUser + "&pwd=" + cameraUserPs +
                        "&loginuse=" + cameraUser + "&loginpas=" + cameraUserPs;
            }
            String finalHttpUrl = httpUrl;
            new Thread(() -> {
                URL getUrl = null;
                try {  // 转换为url数据
                    getUrl = new URL(finalHttpUrl);
                } catch (MalformedURLException var9) {
                    var9.printStackTrace();
                }
                try { // 发起请求并对返回数据进行读取
                    HttpURLConnection urlConnection = (HttpURLConnection) getUrl.openConnection();
                    urlConnection.connect();
                    InputStream in = urlConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        // 匹配属性行的正则表达式
                        String patternString  = "var\\s+(\\w+)=(\\d+|\"[^\"]*\");";
                        // 在当前行中查找匹配项
                        Pattern pattern = Pattern.compile(patternString);
                        Matcher matcher = pattern.matcher(line);
                        // 如果找到匹配项，则提取属性名称和属性值
                        if (matcher.find()) {
                            String propertyName = matcher.group(1);
                            String value = matcher.group(2);
                            for (int i = 1; i <= matcher.groupCount(); i++) {
                                android.util.Log.d("MainActivity", "Property Name: " + propertyName +
                                        ", Property Value: " + value);
                                // 判断值的类型并存储
                                Bundle bundle = new Bundle();
                                if (value.startsWith("\"") && value.endsWith("\"")) {
                                    bundle.putString(propertyName, value.substring(1, value.length() - 1));
                                } else if (value.matches("\\d+")) {
                                    bundle.putInt(propertyName, Integer.parseInt(value));
                                } else {
                                    // 其他类型的值可以根据需要进行处理
                                    bundle.putString(propertyName, value);
                                }
                                Message message = Message.obtain();
                                if (Objects.equals(cgi, GET_CAMERA_PARAMS)) {
                                    message.what = 0;
                                } else if (Objects.equals(cgi, GET_PARAMS)){
                                    message.what = 1;
                                }else if (Objects.equals(cgi,TRANS_CMD_STRING)){
                                    message.what = 40;
                                }else message.what = 2;
                                message.setData(bundle);
                                reHandler.sendMessage(message);
                                // 打印属性信息到Logcat或做其他处理
                            }
                        }
                    }
                    urlConnection.disconnect();
                } catch (IOException var8) {
                    var8.printStackTrace();
                } finally {
                    // 关闭流和相关资源
                    try {
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

}
