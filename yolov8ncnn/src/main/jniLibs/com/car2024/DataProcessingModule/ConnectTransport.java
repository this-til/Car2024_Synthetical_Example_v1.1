package car.bkrc.com.car2024.DataProcessingModule;


import static android.content.ContentValues.TAG;

import static java.lang.Thread.sleep;
import static car.bkrc.com.car2024.ActivityView.FirstActivity.IPCamera;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.FragmentView.LeftFragment;
import car.bkrc.com.car2024.MessageBean.DataRefreshBean;
import car.bkrc.com.car2024.Utils.CameraUtils.XcApplication;
import car.bkrc.com.car2024.Utils.OtherUtil.SerialPort;
import car.bkrc.com.car2024.ViewAdapter.InfrareAdapter;


/**
 * Socket数据处理类
 */
public class ConnectTransport {
    public static DataInputStream bInputStream = null;
    public static DataOutputStream bOutputStream = null;
    public static Socket socket = null;
    public byte[] rbyte = new byte[50];
    private Handler reHandler;
    public short TYPE = 0xAA;
    public short MAJOR = 0x00;
    public short FIRST = 0x00;
    public short SECOND = 0x00;
    public short THRID = 0x00;
    public short CHECKSUM = 0x00;

    private static OutputStream SerialOutputStream;
    private InputStream SerialInputStream;
    private boolean Firstdestroy = false;  ////Firstactivity 是否已销毁了

    public void destory() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                bInputStream.close();
                bOutputStream.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void connect(Handler reHandler, String IP) {
        try {
            this.reHandler = reHandler;
            Firstdestroy = false;
            int port = 60000;
            socket = new Socket(IP, port);
            bInputStream = new DataInputStream(socket.getInputStream());
            bOutputStream = new DataOutputStream(socket.getOutputStream());
            if (!inputDataState) {
                reThread();
            }
            EventBus.getDefault().post(new DataRefreshBean(3));
        } catch (SocketException ignored) {
            EventBus.getDefault().post(new DataRefreshBean(4));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void serial_connect(Handler reHandler) {
        this.reHandler = reHandler;
        try {
            int baudrate = 115200;
            String path = "/dev/ttyS4";
            SerialPort mSerialPort = new SerialPort(new File(path), baudrate, 0);
            SerialOutputStream = mSerialPort.getOutputStream();
            SerialInputStream = mSerialPort.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        XcApplication.executorServicetor.execute(new SerialRunnable());
        // new Thread(new serialRunnable()).start();
    }

    byte[] serialreadbyte = new byte[50];

    class SerialRunnable implements Runnable {
        @Override
        public void run() {
            while (SerialInputStream != null) {
                try {
                    int num = SerialInputStream.read(serialreadbyte);
                    // String  readserialstr =new String(serialreadbyte);
                    String readserialstr = new String(serialreadbyte, 0, num, StandardCharsets.UTF_8);
                    Log.e("----serialreadbyte----", "******" + readserialstr);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = serialreadbyte;
                    reHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean inputDataState = false;

    private void reThread() {
        new Thread(() -> {
            // TODO Auto1-generated method stub
            while (socket != null && !socket.isClosed()) {
                if (Firstdestroy)  //Firstactivity 已销毁了
                {
                    break;
                }
                try {
                    inputDataState = true;
                    bInputStream.read(rbyte);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = rbyte;
                    reHandler.sendMessage(msg);
                    Log.e("AUTO", "上传协议: " + Arrays.toString(rbyte));
                } catch (SocketException ignored) {
                    EventBus.getDefault().post(new DataRefreshBean(4));
                    destory();
                    inputDataState = false;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    EventBus.getDefault().post(new DataRefreshBean(4));
                    destory();
                    inputDataState = false;
                } catch (UnsupportedOperationException ignored) {
                    inputDataState = false;
                }
            }
        }).start();

    }

    private void send() {
        Log.d("test", "发送数据");
        CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THRID) % 256);
        // 发送数据字节数组

        final byte[] sbyte = {0x55, (byte) TYPE, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THRID, (byte) CHECKSUM, (byte) 0xBB};

        Log.e(TAG, "send: " + byteToString(sbyte));
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(() -> {
                // TODO Auto-generated method stub
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(sbyte, 0, sbyte.length);
                        bOutputStream.flush();
                    } else {
                        Message msg = new Message();
                        msg.what = 2;
                        reHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.SERIAL) {
            XcApplication.executorServicetor.execute(() -> {
                try {
                    SerialOutputStream.write(sbyte, 0, sbyte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL)
            try {
                FirstActivity.sPort.write(sbyte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {

            }
    }

    /**
     * byte转字符串
     */
    public String byteToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%X", b));
        }
        return stringBuilder.toString();
    }

    /**
     * 发送语音转换信息
     * @param textbyte
     */
    public void send_voice(final byte[] textbyte) {
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(() -> {
                // TODO Auto-generated method stub
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(textbyte, 0, textbyte.length);
                        bOutputStream.flush();
                    } else {
                        Message msg = new Message();
                        msg.what = 2;
                        reHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.SERIAL) {

            XcApplication.executorServicetor.execute(() -> {
                try {
                    SerialOutputStream.write(textbyte, 0, textbyte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL) {
            try {
                FirstActivity.sPort.write(textbyte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
                Log.e("UART:", "unline");
            }
        }
    }

    // 前进
    public void go(int sp_n, int en_n) {
        MAJOR = 0x02;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) (en_n & 0xff);
        THRID = (byte) (en_n >> 8);
        send();
    }

    // 后退
    public void back(int sp_n, int en_n) {
        MAJOR = 0x03;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) (en_n & 0xff);
        THRID = (byte) (en_n >> 8);
        send();
    }


    //左转
    public void left(int sp_n) {
        MAJOR = 0x04;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) 0x00;
        THRID = (byte) 0x00;
        send();
    }


    // 右转
    public void right(int sp_n) {
        MAJOR = 0x05;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) 0x00;
        THRID = (byte) 0x00;
        send();
    }

    // 停车
    public void stop() {
        MAJOR = 0x01;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    // 程序自动执行
    public void autoDrive() {
        MAJOR = 0xA0;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }


    // 循迹
    public void line(int sp_n) {  //寻迹
        MAJOR = 0x06;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    //清除码盘值
    public void clear() {
        MAJOR = 0x07;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    //平台数据接收状态转换
    public void stateChange(final int i) {
        final short temp = TYPE;
        new Thread(() -> {
            if (socket != null && socket.isConnected()) {
                if (i == 1) {//从车状态
                    TYPE = 0x02;
                    MAJOR = 0x80;
                    FIRST = 0x01;
                    SECOND = 0x00;
                    THRID = 0x00;
                    send();
                    yanchi(500);
                    TYPE = (byte) 0xAA;
                    MAJOR = 0x80;
                    FIRST = 0x01;
                    SECOND = 0x00;
                    THRID = 0x00;
                    send();
                    TYPE = 0x02;
                } else if (i == 2) {// 主车状态
                    TYPE = 0x02;
                    MAJOR = 0x80;
                    FIRST = 0x00;
                    SECOND = 0x00;
                    THRID = 0x00;
                    send();
                    yanchi(500);
                    TYPE = (byte) 0xAA;
                    MAJOR = 0x80;
                    FIRST = 0x00;
                    SECOND = 0x00;
                    THRID = 0x00;
                    send();
                    TYPE = 0xAA;
                }
                TYPE = temp;
            } else {
                Message msg = new Message();
                msg.what = 2;
                reHandler.sendMessage(msg);
            }
        }).start();
    }

    // 红外
    public void infrared(final byte one, final byte two, final byte thrid, final byte four, final byte five,
                         final byte six) {
        new Thread(() -> {
            MAJOR = 0x10;
            FIRST = one;
            SECOND = two;
            THRID = thrid;
            send();
            yanchi(500);
            MAJOR = 0x11;
            FIRST = four;
            SECOND = five;
            THRID = six;
            send();
            yanchi(500);
            MAJOR = 0x12;
            FIRST = 0x00;
            SECOND = 0x00;
            THRID = 0x00;
            send();
            yanchi(1000);
        }).start();
    }

    /**
     * 发送文本信息专用
     */
    public void sendData(final short[] data) {
        MAJOR = 0x10;
        FIRST = 0xff;
        SECOND = data[0];
        THRID = data[1];
        send();
        yanchi(200);//延时 如果觉得发过去单片机接收不全 就调高一点
        MAJOR = 0x11;
        FIRST = data[2];
        SECOND = data[3];
        THRID = 0x00;
        send();
        yanchi(200);//延时 如果觉得发过去单片机接收不全 就调高一点
        MAJOR = 0x12;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        yanchi(200);//延时 如果觉得发过去单片机接收不全 就调高一点
    }

    /**
     * ZigBee发送文本信息专用
     */
    public void zigbeeSendData(final short[] data) {
        TYPE = 0x11;
        MAJOR = data[0];
        FIRST = data[1];
        SECOND = data[2];
        THRID = data[3];
        send();
        TYPE = 0xAA;
        yanchi(600);
    }

    // 程序自动执行
    public void getID() {
        TYPE = 0x07;
        MAJOR = 0x09;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = 0xAA;
    }

    // 指示灯
    public void light(int left, int right) {
        if (left == 1 && right == 1) {
            MAJOR = 0x20;
            FIRST = 0x01;
            SECOND = 0x01;
            THRID = 0x00;
            send();
        } else if (left == 1 && right == 0) {
            MAJOR = 0x20;
            FIRST = 0x01;
            SECOND = 0x00;
            THRID = 0x00;
            send();
        } else if (left == 0 && right == 1) {
            MAJOR = 0x20;
            FIRST = 0x00;
            SECOND = 0x01;
            THRID = 0x00;
            send();
        } else if (left == 0 && right == 0) {
            MAJOR = 0x20;
            FIRST = 0x00;
            SECOND = 0x00;
            THRID = 0x00;
            send();
        }
    }


    // 蜂鸣器
    public void buzzer(int i) {
        if (i == 1)
            FIRST = 0x01;
        else if (i == 0)
            FIRST = 0x00;
        MAJOR = 0x30;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    /**
     * 从车二维码识别
     */
    public void qr_rec(int state) {
        byte temp = (byte) TYPE;
        TYPE = 0x02;
        MAJOR = 0x92;
        FIRST = (byte) state;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;

    }

    /**
     * 从车摄像头俯仰角控制
     */
    public void rb_cameraControl(int state) {
        byte temp = (byte) TYPE;
        TYPE = 0x02;
        MAJOR = 0x91;
        FIRST = 0x03;
        SECOND = (byte) state;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    /**
     * 光照档位控制
     *
     * @param gear 档位信息
     */
    public void gear(int gear) {
        if (gear == 1)
            MAJOR = 0x61;
        else if (gear == 2)
            MAJOR = 0x62;
        else if (gear == 3)
            MAJOR = 0x63;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    private static boolean sendState = false; // 数据发送状态记录，为true时正在发送，为false时发送结束/关闭

    //立体显示
    public boolean infrared_stereo(final short[] data, boolean tip) {
        if (socket.isConnected()) {
            if (!sendState && data != null) {
                sendState = true; // 处于发送数据状态，开启发送拦截
                XcApplication.executorServicetor.execute(() -> {
                    MAJOR = 0x10;
                    FIRST = 0xff;
                    SECOND = data[0];
                    THRID = data[1];
                    send();
                    yanchi(500);
                    MAJOR = 0x11;
                    FIRST = data[2];
                    SECOND = data[3];
                    THRID = data[4];
                    send();
                    yanchi(500);
                    MAJOR = 0x12;
                    FIRST = 0x00;
                    SECOND = 0x00;
                    THRID = 0x00;
                    send();
                    yanchi(500);
                    if (tip) InfrareAdapter.handler.sendEmptyMessage(40); // 数据发送完毕
                    sendState = false;
                });
            } else {
                if (tip) InfrareAdapter.handler.sendEmptyMessage(30); // 数据还未发送完毕
            }
        } else {
            Message msg = new Message();
            msg.what = 2;
            reHandler.sendMessage(msg);
        }
        return sendState;
    }


    //智能交通灯
    public void traffic_control(int type, int major, int first) {
        byte temp = (byte) TYPE;
        TYPE = (short) type;
        MAJOR = (byte) major;
        FIRST = (byte) first;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    /**
     * 舵机角度控制
     *
     * @param major 左侧舵机
     * @param first 右侧舵机
     */
    public void rudder_control(int major, int first) {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x0C;
        MAJOR = (byte) 0x08;
        FIRST = (byte) major;
        SECOND = (byte) first;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    //立体车库控制
    public void garage_control(int type, int major, int first) {
        byte temp = (byte) TYPE;
        TYPE = (short) type;
        MAJOR = (byte) major;
        FIRST = (byte) first;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void gate(int major, int first, int second, int third) {// 闸门
        byte temp = (byte) TYPE;
        TYPE = 0x03;
        MAJOR = (byte) major;
        FIRST = (byte) first;
        SECOND = (byte) second;
        THRID = (byte) third;
        send();
        TYPE = temp;
    }

    //LCD 显示标志物进入计时模式
    public void digital_close() {//数码管关闭
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        MAJOR = 0x03;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void digital_open() {//数码管打开
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        MAJOR = 0x03;
        FIRST = 0x01;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void digital_clear() {//数码管清零
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        MAJOR = 0x03;
        FIRST = 0x02;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void digital_dic(int dis) {//LCD显示标志物第二排显示距离

        byte temp = (byte) TYPE;
        int a, b, c;
        a = (dis / 100) & (0xF);
        b = (dis % 100 / 10) & (0xF);
        c = (dis % 10) & (0xF);
        b = b << 4;
        b = b | c;
        TYPE = 0x04;
        MAJOR = 0x04;
        FIRST = 0x00;
        SECOND = (short) (a);
        THRID = (short) (b);
        send();
        TYPE = temp;
    }

    public void digital(int i, int one, int two, int three) {// 数码管
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        if (i == 1) {//数据写入第一排数码管
            MAJOR = 0x01;
            FIRST = (byte) one;
            SECOND = (byte) two;
            THRID = (byte) three;
        } else if (i == 2) {//数据写入第二排数码管
            MAJOR = 0x02;
            FIRST = (byte) one;
            SECOND = (byte) two;
            THRID = (byte) three;
        }
        send();
        TYPE = temp;
    }

    public void VoiceBroadcast()  //语音播报随机指令
    {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x06;
        MAJOR = (short) 0x20;
        FIRST = (byte) 0x01;
        SECOND = (byte) 0x00;
        THRID = (byte) 0x00;
        send();
        TYPE = temp;
    }

    public void voiceWeather(int[] weather)  //语音播报随机指令
    {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x06;
        MAJOR = (short) 0x42;
        FIRST = (byte) weather[0];
        SECOND = (byte) weather[1];
        THRID = (byte) 0x00;
        send();
        TYPE = temp;
    }

    public void TFT_LCD(int type, int MAIN, int KIND, int COMMAD, int DEPUTY)  //tft lcd
    {
        byte temp = (byte) TYPE;
        TYPE = (short) type;
        MAJOR = (short) MAIN;
        FIRST = (byte) KIND;
        SECOND = (byte) COMMAD;
        THRID = (byte) DEPUTY;
        send();
        TYPE = temp;
    }

    public void magnetic_suspension(int MAIN, int KIND, int COMMAD, int DEPUTY) //无线充电
    {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x0A;
        MAJOR = (short) MAIN;
        FIRST = (byte) KIND;
        SECOND = (byte) COMMAD;
        THRID = (byte) DEPUTY;
        send();
        TYPE = temp;
    }

    // 沉睡
    public void yanchi(int time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 摄像头角度控制，可发送自定协议
     *
     * @param state_camera 方向/预设位
     * @param number       调用次数
     */
    public void camerastate_control(int state_camera, int number) {
        XcApplication.executorServicetor.execute(() -> {
            for (int i = 0; i < number; i++)
                switch (state_camera) {
                    //上下左右转动
                    case 1:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 0 + "&onestep=" + 1);  //向上
                        break;
                    case 2:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 2 + "&onestep=" + 1);  //向下
                        break;
                    case 3:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 4 + "&onestep=" + 1);  //向左
                        break;
                    case 4:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 6 + "&onestep=" + 1);  //向右
                        break;
                    // / 5-7   设置预设位1到3
                    case 5:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 30 + "&onestep=" + 0);
                        break;
                    case 6:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 32 + "&onestep=" + 0);
                        break;
                    case 7:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 34 + "&onestep=" + 0);
                        break;
                    //调用预设位1-3
                    case 8:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 31 + "&onestep=" + 0);
                        break;
                    case 9:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 33 + "&onestep=" + 0);
                        break;
                    case 10:
                        LeftFragment.cameraConntrol.cameraMiscControlPostHttp(IPCamera, LeftFragment.cameraConntrol.DECODER_CONTROL, "command=" + 35 + "&onestep=" + 0);
                        break;
                    default:
                        break;
                }
            try {
                sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
