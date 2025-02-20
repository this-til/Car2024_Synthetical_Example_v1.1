package car.bkrc.com.car2024.ViewAdapter;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.OtherUtil.CodeConversion;
import car.bkrc.com.car2024.Utils.OtherUtil.RadiusUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.DataProcessingModule.ConnectTransport;


public class InfrareAdapter extends RecyclerView.Adapter<InfrareAdapter.ViewHolder> {
    public static final String TAG = "InfrareAdapter";

    private final List<Infrared_Landmark> mInfrareLandmarkList;
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    /**
     * 界面列表适配器，用于加载列表中的选项，如文字、图片等内容
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        View InfrareView;
        ImageView InfrareImage;
        TextView InfrareName;

        public ViewHolder(View view) {
            super(view);
            InfrareView = view;
            InfrareImage = view.findViewById(R.id.infrared_image);
            InfrareName = view.findViewById(R.id.infrared_name);
        }
    }

    public InfrareAdapter(List<Infrared_Landmark> InfrareLandmarkList, Context context) {
        mInfrareLandmarkList = InfrareLandmarkList;
        InfrareAdapter.context = context;
    }

    /**
     * 加载子项布局内容
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.infrared_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.InfrareView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            Infrared_Landmark InfrareLandmark = mInfrareLandmarkList.get(position);
            Infrare_select(InfrareLandmark);
        });
        holder.InfrareImage.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            Infrared_Landmark InfrareLandmark = mInfrareLandmarkList.get(position);
            Infrare_select(InfrareLandmark);
        });
        return holder;
    }

    /**
     * 加载子项布局内容
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Infrared_Landmark InfrareLandmark = mInfrareLandmarkList.get(position);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), InfrareLandmark.getImageId(), null);
        bitmap = RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 10);
        holder.InfrareImage.setImageBitmap(bitmap);
        holder.InfrareName.setText(InfrareLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mInfrareLandmarkList.size();
    }

    private void Infrare_select(Infrared_Landmark InfrareLandmark) {
        vSimple(context,30); // 控制手机震动进行反馈
        switch (InfrareLandmark.getName()) {
            case "智能报警台标志物":
                policeController();
                break;
            case "智能路灯标志物":
                gearController();
                break;
            case "智能立体显示标志物":
                threeDisplay();
                break;
            default:
                break;
        }

    }

    // 报警器
    private void policeController() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("智能报警台标志物");
        String[] item2 = {"打开", "关闭", "获取随机救援坐标","演示模式"};
        builder.setSingleChoiceItems(item2, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (ConnectTransport.socket != null && ConnectTransport.socket.isConnected()) {
                        if (which == 0) {
                            FirstActivity.Connect_Transport.infrared((byte) 0x03, (byte) 0x05,
                                    (byte) 0x14, (byte) 0x45, (byte) 0xDE,
                                    (byte) 0x92);

                        } else if (which == 1) {
                            FirstActivity.Connect_Transport.infrared((byte) 0x67, (byte) 0x34,
                                    (byte) 0x78, (byte) 0xA2, (byte) 0xFD,
                                    (byte) 0x27);
                        } else if (which == 2) {
                            getIDTipAction();
                        }else if (which == 3){
                            policedisplayController();
                        }
                    } else {
                        ToastUtil.ShowToast(context,"当前未连接到设备，请连接后重试！");
                    }

                });
        builder.create().show();
    }


    // 报警器演示模式
    private void policedisplayController() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("智能报警台演示模式");
        String[] item2 = {"开始演示", "演示结束"};
        builder.setSingleChoiceItems(item2, 0,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (ConnectTransport.socket != null && ConnectTransport.socket.isConnected()) {
                        if (which == 0) {
                            FirstActivity.Connect_Transport.infrared((byte) 0xff, (byte) 0x01,
                                    (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                    (byte) 0xff);
                        } else if (which == 1) {
                            FirstActivity.Connect_Transport.infrared((byte) 0xff, (byte) 0x00,
                                    (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                    (byte) 0xff);
                        }
                    } else {
                        ToastUtil.ShowToast(context,"当前未连接到设备，请连接后重试！");
                    }

                });
        builder.create().show();
    }

    /**
     * 获取随机坐标点
     */
    private void getIDTipAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 设置Title的内容
        builder.setIcon(R.mipmap.rc_logo);
        builder.setTitle("温馨提示");
        // 设置Content来显示一个信息
        builder.setMessage("随机救援坐标为地图中心9个随机坐标点");
        // 设置一个PositiveButton
        builder.setPositiveButton("立刻获取", (dialog, which) -> {
            FirstActivity.Connect_Transport.getID();
            vSimple(context,30); // 控制手机震动进行反馈
        });
        // 设置一个NegativeButton
        builder.setNegativeButton("取消", (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            dialog.dismiss();
        });
        builder.show();
    }

    private void gearController() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("智能路灯标志物");
        String[] gr_item = {"光源挡位加一档", "光源挡位加二档", "光源挡位加三档"};
        builder.setSingleChoiceItems(gr_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    if (which == 0) {// 加一档
                        FirstActivity.Connect_Transport.gear(1);
                    } else if (which == 1) {// 加二档
                        FirstActivity.Connect_Transport.gear(2);
                    } else if (which == 2) {// 加三档
                        FirstActivity.Connect_Transport.gear(3);
                    }
                });
        builder.create().show();
    }

    private short[] data = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private void threeDisplay() {
        AlertDialog.Builder Builder = new AlertDialog.Builder(context);
        Builder.setTitle("智能立体显示标志物");
        String[] three_item = {"基本信息显示", "设置文字显示颜色", "清空当前显示内容", "自定义文字显示", "自定义文字注意事项"};
        Builder.setSingleChoiceItems(three_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    switch (which) {
                        case 0:
                            threeBasicsDisplay();
                            break;
                        case 1:
                            textColorSet();
                            break;
                        case 2: // 清除全部文本内容
                            threeClearnDisplay();
                            break;
                        case 3: // 自定义文本发送
                            CodeConversion.createLoadingDialog(context);
                            break;
                        case 4:
                            tipAction();
                            break;
                        default:
                            break;
                    }
                });
        Builder.create().show();
    }

    private void threeBasicsDisplay() {
        AlertDialog.Builder Builder = new AlertDialog.Builder(context);
        Builder.setTitle("基本信息显示");
        String[] three_item = {"颜色信息显示模式", "图形信息显示模式", "距离信息显示模式", "车牌信息显示模式",
                "交通警示牌信息显示模式", "交通标志信息显示模式", "显示默认信息"};
        Builder.setSingleChoiceItems(three_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    switch (which) {
                        case 0:
                            color();
                            break;
                        case 1:
                            shape();
                            break;
                        case 2:
                            dis();
                            break;
                        case 3:
                            lic();
                            break;
                        case 4:
                            road();
                            break;
                        case 5:
                            traffic_flag();
                            break;
                        case 6:
                            if (ConnectTransport.socket.isConnected()){
                                data[0] = 0x16;
                                data[1] = 0x01;
                                FirstActivity.Connect_Transport.infrared_stereo(data, true);
                            }
                            break;
                        default:
                            break;
                    }
                });
        Builder.create().show();
    }

    private void threeClearnDisplay() {
        AlertDialog.Builder Builder = new AlertDialog.Builder(context);
        Builder.setTitle("清除选项");
        String[] three_item = {"清除累加数据", "清除所有数据"};
        Builder.setSingleChoiceItems(three_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    switch (which) {
                        case 0:
                            clearData((short) 0x01);
                            break;
                        case 1:
                            clearData((short) 0x02);
                            break;
                        default:
                            break;
                    }
                });
        Builder.create().show();
    }

    /**
     * 数据清理协议
     *
     */
    private void clearData(short id) {
        new Thread(() -> {
            if (ConnectTransport.socket != null && ConnectTransport.socket.isConnected()) {
                data = new short[8];
                data[0] = 0x32;
                data[1] = id;
                FirstActivity.Connect_Transport.zigbeeSendData(data);
                FirstActivity.Connect_Transport.sendData(data);
                handler.sendEmptyMessage(50); // 数据发送完毕
            } else {
                handler.sendEmptyMessage(60); // 未连接设备

            }
        }).start();
    }

    private void color() {
        AlertDialog.Builder colorBuilder = new AlertDialog.Builder(context);
        colorBuilder.setTitle("颜色信息显示模式");
        String[] lg_item = {"红色", "绿色", "蓝色", "黄色", "品色", "青色", "黑色", "白色"};
        colorBuilder.setSingleChoiceItems(lg_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    data[0] = 0x13;
                    data[1] = (short) (which + 0x01);
                    FirstActivity.Connect_Transport.infrared_stereo(data, true);
                });
        colorBuilder.create().show();
    }

    private void shape() {
        AlertDialog.Builder shapeBuilder = new AlertDialog.Builder(context);
        shapeBuilder.setTitle("图形信息显示模式");
        String[] shape_item = {"矩形", "圆形", "三角形", "菱形", "五角星"};
        shapeBuilder.setSingleChoiceItems(shape_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    data[0] = 0x12;
                    data[1] = (short) (which + 0x01);
                    FirstActivity.Connect_Transport.infrared_stereo(data, true);
                });
        shapeBuilder.create().show();
    }

    private void road() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(context);
        roadBuilder.setTitle("交通警示牌信息显示模式");
        String[] road_item = {"前方学校 减速慢行", "前方施工 禁止通行", "塌方路段 注意安全", "追尾危险 保持车距", "严禁 酒后驾车", "严禁 乱扔垃圾"};
        roadBuilder.setSingleChoiceItems(road_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    data[0] = 0x14;
                    data[1] = (short) (which + 0x01);
                    FirstActivity.Connect_Transport.infrared_stereo(data, true);
                });
        roadBuilder.create().show();
    }

    private void traffic_flag() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(context);
        roadBuilder.setTitle("交通标志信息显示模式");
        String[] road_item = {"直行", "左转", "右转", "掉头", "禁止直行", "禁止通行"};
        roadBuilder.setSingleChoiceItems(road_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    data[0] = 0x15;
                    data[1] = (short) (which + 0x01);
                    FirstActivity.Connect_Transport.infrared_stereo(data, true);
                });
        roadBuilder.create().show();
    }

    private void textColorSet() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(context);
        roadBuilder.setTitle("设置文字显示颜色");
        String[] road_item = {"中国红", "青草绿", "原色蓝", "自定义颜色"};
        roadBuilder.setSingleChoiceItems(road_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    data[0] = 0x17;
                    data[1] = 0x01;
                    switch (which) {
                        case 0: // 红
                            data[2] = 0xC8;
                            data[3] = 0x10;
                            data[4] = 0x2E;
                            break;
                        case 1: // 绿
                            data[2] = 0x00;
                            data[3] = 0xff;
                            data[4] = 0x00;
                            break;
                        case 2: // 蓝
                            data[2] = 0x00;
                            data[3] = 0x00;
                            data[4] = 0xff;
                            break;
                        case 3:
                            customColorSend();
                            break;
                        default:
                            break;
                    }
                    FirstActivity.Connect_Transport.infrared_stereo(data, true);
                });
        roadBuilder.create().show();
    }


    private void dis() {
        AlertDialog.Builder disBuilder = new AlertDialog.Builder(context);
        disBuilder.setTitle("距离信息显示模式");
        final String[] road_item = {"10cm", "15cm", "20cm", "28cm", "39cm"};
        disBuilder.setSingleChoiceItems(road_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    int disNum = Integer.parseInt(road_item[which]
                            .substring(0, 2));
                    data[0] = 0x11;
                    data[1] = (short) (disNum / 10 + 0x30);
                    data[2] = (short) (disNum % 10 + 0x30);
                    FirstActivity.Connect_Transport.infrared_stereo(data, true);
                });
        disBuilder.create().show();
    }

    //从string中得到short数据数组
    private short[] StringToBytes(String licString) {
        if (licString == null || licString.equals("")) {
            return null;
        }
        licString = licString.toUpperCase();
        int length = licString.length();
        char[] hexChars = licString.toCharArray();
        short[] d = new short[length];
        for (int i = 0; i < length; i++) {
            d[i] = (short) hexChars[i];
        }
        return d;
    }

    @SuppressLint("HandlerLeak")
    private final Handler licHandler = new Handler() {
        public void handleMessage(Message msg) {
            short[] li = StringToBytes(lic_item[msg.what]);
            new Thread(() -> {

                boolean sendState = true;
                int num = 0;
                while (sendState) {
                    if (!FirstActivity.Connect_Transport.infrared_stereo(null, false)) {
                        if (num == 0) {
                            data[0] = 0x20;
                            data[1] = (li[0]);
                            data[2] = (li[1]);
                            data[3] = (li[2]);
                            data[4] = (li[3]);
                            FirstActivity.Connect_Transport.infrared_stereo(data, false);
                            num++;
                        } else if (num == 1) {
                            data[0] = 0x10;
                            data[1] = (li[4]);
                            data[2] = (li[5]);
                            data[3] = (li[6]);
                            data[4] = (li[7]);
                            FirstActivity.Connect_Transport.infrared_stereo(data, true);
                            sendState = false;
                        }
                    }
                }
            }).start();
        }

    };
    private int lic = -1;
    private final String[] lic_item = {"N300Y7A4", "N600H5B4", "N400Y6G6",
            "J888B8C8"};

    private void lic() {
        AlertDialog.Builder licBuilder = new AlertDialog.Builder(context);
        licBuilder.setTitle("车牌信息显示模式");
        licBuilder.setSingleChoiceItems(lic_item, lic,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    Log.e(TAG, "lic: " + which);
                    lic = which;
                    licHandler.sendEmptyMessage(which);
                });
        licBuilder.create().show();
    }

    @SuppressLint("SetTextI18n")
    private void customColorSend() {
        AlertDialog.Builder TFT_Hex_builder = new AlertDialog.Builder(
                context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_hex, null);
        TFT_Hex_builder.setTitle("自定义文字颜色");
        TFT_Hex_builder.setView(view);
        // 下拉列表
        final EditText editText1 = view.findViewById(R.id.editText1);
        final EditText editText2 = view.findViewById(R.id.editText2);
        final EditText editText3 = view.findViewById(R.id.editText3);
        editText1.setText("FF");
        editText2.setText("00");
        editText3.setText("FF");
        data[0] = 0x17;
        data[1] = 0x01;
        TFT_Hex_builder.setPositiveButton("确定",
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    String ones = editText1.getText().toString();
                    String twos = editText2.getText().toString();
                    String threes = editText3.getText().toString();
                    // 显示数据，一个文本编译框最多两个数据显示数目管中两个数据
                    data[2] = (short) (ones.equals("") ? 0x00 : Integer.parseInt(ones, 16));
                    data[3] = (short) (twos.equals("") ? 0x00 : Integer.parseInt(twos, 16));
                    data[4] = (short) (threes.equals("") ? 0x00 : Integer.parseInt(threes, 16));
                    FirstActivity.Connect_Transport.infrared_stereo(data, true);
                });
        TFT_Hex_builder.setNegativeButton("取消",
                (dialog, which) -> {
                    // TODO Auto-generated method stub
                    dialog.cancel();
                });
        TFT_Hex_builder.create().show();
    }

    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("echo", Arrays.toString((byte[]) msg.obj));
            switch (msg.what) {
                case 10:
                    ToastUtil.ShowToast(context,"文本已发送完毕！");
                    break;
                case 20:
                    ToastUtil.ShowToast(context,"请输入中文！");
                    break;
                case 30:
                    ToastUtil.ShowToast(context,"上一条数据正在发送中，请稍后！");
                    break;
                case 40:
                    ToastUtil.ShowToast(context,"数据发送完毕，您可发送下一条指令！");
                    break;
                case 50:
                    ToastUtil.ShowToast(context,"数据清空指令已发送！");
                    break;
                case 60:
                    ToastUtil.ShowToast(context,"当前未连接到设备，请连接后重试！");
                    break;
                default:
                    break;
            }
        }
    };

    private void tipAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 设置Title的内容
        builder.setIcon(R.mipmap.rc_logo);
        builder.setTitle("温馨提示");
        // 设置Content来显示一个信息
        builder.setMessage("1. 使用全量字库时，可发送任意文本，请耐心等待发送完毕！\n" +
                "2. 使用非全量字库时，可发送有限文字，可能会出现文字显示不全的情况。\n" +
                "注：使用全量字库时，需要将字库文件保存在FAT 32格式的Micro SD卡中，并将其插入立体显示标志物的卡槽中。");
        // 设置一个PositiveButton
        builder.setPositiveButton("确定", (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
        });
        builder.show();
    }

    /**
     * 控制手机震动
     * @param context     上下文
     * @param millisecond 震动时间，毫秒为单位
     */
    public static void vSimple(Context context, int millisecond) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(millisecond);
    }
}