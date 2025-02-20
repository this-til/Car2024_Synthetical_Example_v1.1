package car.bkrc.com.car2024.Utils.OtherUtil;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.CameraUtils.XcApplication;
import car.bkrc.com.car2024.ViewAdapter.InfrareAdapter;
import car.bkrc.com.car2024.DataProcessingModule.ConnectTransport;
import car.bkrc.com.car2024.FragmentView.RightMoveControlFragment;


public class CodeConversion {

    private static Dialog loadingDialog;
    private static boolean sendState = false;

    /**
     * 关闭dialog
     */
    public static void closeDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog.cancel();
        } else {
            System.out.println("空!!!!!!!!!!");
        }
    }

    @SuppressLint("StaticFieldLeak")
    /**
     * 弹出输入窗口
     *
     * @param context 当前显示的activity
     */
    public static void createLoadingDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.postionlits_item, null);// 得到加载view
        LinearLayout layout = v
                .findViewById(R.id.showList);// 加载布局
        ClearEditText editText = v.findViewById(R.id.edit);
        editText.setText(R.string.slogan);
        v.findViewById(R.id.left_button).setOnClickListener(v1 -> {
            if (ConnectTransport.socket != null && ConnectTransport.socket.isConnected()) {
                String str = editText.getText().toString();
                if (!str.equals("")) {
                    sendChar(str, false);
                } else {
                    ToastUtil.ShowToast(context,"请输入文字！");
                }
            }else {
                ToastUtil.ShowToast(context,"当前未连接到设备，请连接后重试！");
            }
            closeDialog();
        });
        v.findViewById(R.id.right_button).setOnClickListener(v1 -> {
            if (ConnectTransport.socket != null && ConnectTransport.socket.isConnected()) {
                String str = editText.getText().toString();
                if (!str.equals("")) {
                    sendChar(str, true);
                } else {
                    ToastUtil.ShowToast(context,"请输入文字！");
                }
            }else {
                ToastUtil.ShowToast(context,"当前未连接到设备，请连接后重试！");
            }
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

    /**
     * 子线程发送数据并弹出窗口提示
     *
     * @param data 汉字信息
     */
    private static void sendChar(String data, Boolean isZigBee) {
            Log.e(RightMoveControlFragment.TAG, "senddata: " + data.length());
            if (!sendState) {
                sendState = true; // 处于发送数据状态，开启发送拦截
                XcApplication.executorServicetor.execute(() -> {
                    int charLenth = 0; // 记录字符串长度
                    for (char chardata : data.toCharArray()) {
                        byte[] shorts;
                        shorts = new byte[0];
                        try {
                            shorts = (chardata + "").getBytes("GBK");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            InfrareAdapter.handler.sendEmptyMessage(20);
                        }
                        Log.i("得到的32位字节: ", Arrays.toString(shorts));
                        short[] bytes = new short[4];
                        bytes[0] = (byte) (0x31);//自定义显示的协议
                        int solt = 1;
                        charLenth++;
                        Log.e(RightMoveControlFragment.TAG, "sendshort: " + shorts.length);
                        if (shorts.length <= 1) {
                            bytes[1] = shorts[0];
                            bytes[2] = (byte) 0x00;
                            if (charLenth == data.length()) {
                                bytes[3] = (byte) 0x55;//当数据发送完成时 发送0x55结束
                                sendState = false; // 恢复数据发送通道
                                InfrareAdapter.handler.sendEmptyMessage(10); // 数据发送完毕
                            } else bytes[3] = (byte) (0x00);
                            if (isZigBee) {
                                FirstActivity.Connect_Transport.zigbeeSendData(bytes);
                            } else {
                                FirstActivity.Connect_Transport.sendData(bytes);
                            }
                            bytes = new short[4];
                            bytes[0] = (byte) (0x31);//自定义显示的协议
                        } else {
                            for (byte m : shorts) {
                                bytes[solt++] = m;
                                if (solt == 3) {
                                    Log.e(RightMoveControlFragment.TAG, "sendChar: " + shorts.length);
                                    if (charLenth == data.length()) {
                                        bytes[3] = (byte) 0x55;//当数据发送完成时 发送0x55结束
                                        sendState = false; // 恢复数据发送通道
                                        InfrareAdapter.handler.sendEmptyMessage(10); // 数据发送完毕
                                    } else bytes[3] = (byte) (0x00);
                                    if (isZigBee) {
                                        FirstActivity.Connect_Transport.zigbeeSendData(bytes);
                                    } else {
                                        FirstActivity.Connect_Transport.sendData(bytes);
                                    }
                                    solt = 1;
                                    bytes = new short[4];
                                    bytes[0] = (byte) (0x31);//自定义显示的协议
                                }
                            }
                        }

                    }
                });
            } else InfrareAdapter.handler.sendEmptyMessage(30);


    }
}
