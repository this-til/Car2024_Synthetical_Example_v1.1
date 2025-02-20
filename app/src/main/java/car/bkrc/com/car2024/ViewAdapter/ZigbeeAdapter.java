package car.bkrc.com.car2024.ViewAdapter;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.UnsupportedEncodingException;
import java.util.List;

import car.bkrc.com.car2024.ActivityView.FirstActivity;
import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.OtherUtil.ClearEditText;
import car.bkrc.com.car2024.Utils.OtherUtil.RadiusUtil;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;


public class ZigbeeAdapter extends RecyclerView.Adapter<ZigbeeAdapter.ViewHolder> {

    private final List<Zigbee_Landmark> mZigbeeLandmarkList;
    @SuppressLint("StaticFieldLeak")
    private static Context context = null;


    /**
     * 界面列表适配器，用于加载列表中的选项，如文字、图片等内容
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        View zigbeeView;
        ImageView zigbeeImage;
        TextView zigbeeName;


        public ViewHolder(View view) {
            super(view);
            zigbeeView = view;
            zigbeeImage = view.findViewById(R.id.landmark_image);
            zigbeeName = view.findViewById(R.id.landmark_name);
        }
    }

    public ZigbeeAdapter(List<Zigbee_Landmark> zigbeeLandmarkList, Context context) {
        mZigbeeLandmarkList = zigbeeLandmarkList;
        ZigbeeAdapter.context = context;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.zigbee_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.zigbeeView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            Zigbee_Landmark zigbeeLandmark = mZigbeeLandmarkList.get(position);
            zigbee_select(zigbeeLandmark);
        });
        holder.zigbeeImage.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            Zigbee_Landmark zigbeeLandmark = mZigbeeLandmarkList.get(position);
            zigbee_select(zigbeeLandmark);
        });
        return holder;
    }

    private void zigbee_select(Zigbee_Landmark zigbeeLandmark) {
        vSimple(context,30); // 控制手机震动进行反馈
        switch (zigbeeLandmark.getName()) {
            case "智能道闸标志物":
                gateController();       // 原道闸标志物
                break;
            case "智能显示标志物":
                digital();              // 原LED显示标志物
                break;
            case "智能公交站标志物":
                voiceController();      // 原语音播报标志物
                break;
            case "智能无线充电标志物":
                magnetic_suspension();  // 无线充电标志物
                break;
            case "多功能信息显示标志物":
                TFT_Control();          // 原智能TFT显示标志物
                break;
            case "智能交通信号灯标志物":
                Traffic_Control();      // 原智能交通灯标志物
                break;
            case "智能立体车库标志物":
                stereo_garage_Control();// 原立体车库标志物
                break;
            case "智能ETC系统标志物":
                etc_Control();          // 原ETC系统标志物
                break;
            default:
                break;
        }

    }

    private void etc_Control() {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        garage_builder.setTitle("智能ETC系统标志物");
        String[] ga = {"左侧舵机微调", "右侧舵机微调"};
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            switch (i) {
                case 0: // 左侧
                    etc_SteeringEngine_Adjust(0);
                    break;
                case 1: // 右侧
                    etc_SteeringEngine_Adjust(1);
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    private void stereo_garage_Control() {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        garage_builder.setTitle("智能立体车库标志物");
        String[] ga = {"智能立体车库（A）", "智能立体车库（B）"};
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            switch (i) {
                case 0: // A
                    stereo_garage("智能立体车库（A）",0x0D);
                    break;
                case 1: // B
                    stereo_garage("智能立体车库（B）",0x05);
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    private void Traffic_Control() {
        AlertDialog.Builder traffic_builder = new AlertDialog.Builder(context);
        traffic_builder.setTitle("智能交通信号灯标志物");
        String[] ga = {"智能交通信号灯（A）", "智能交通信号灯（B）", "智能交通信号灯（C）", "智能交通信号灯（D）"};
        traffic_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            switch (i) {
                case 0: // A
                    Traffic_light("智能交通信号灯（A）",0x0E);
                    break;
                case 1: // B
                    Traffic_light("智能交通信号灯（B）",0x0F);
                    break;
                case 2: // B
                    Traffic_light("智能交通信号灯（C）",0x13);
                    break;
                case 3: // B
                    Traffic_light("智能交通信号灯（D）",0x14);
                    break;
                default:
                    break;
            }
        });
        traffic_builder.create().show();
    }

    private void TFT_Control() {
        AlertDialog.Builder tft_builder = new AlertDialog.Builder(context);
        tft_builder.setTitle("多功能信息显示标志物");
        String[] ga = {"多功能信息显示标志物（A）", "多功能信息显示标志物（B）", "多功能信息显示标志物（C）"};
        tft_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            switch (i) {
                case 0: // A
                    Smart_DataDisplay("多功能信息显示标志物（A）",0x0B);
                    break;
                case 1: // B
                    Smart_DataDisplay("多功能信息显示标志物（B）",0x08);
                    break;
                case 2: // C
                    Smart_DataDisplay("多功能信息显示标志物（C）",0x11);
                    break;
                default:
                    break;
            }
        });
        tft_builder.create().show();
    }

    /**
     * ETC系统标志物舵机初始角度调节
     *
     * @param rudder 选择舵机，0为左侧，1为右侧
     */
    private void etc_SteeringEngine_Adjust(final int rudder) {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        String[] ga = {"上升", "下降"};
        if (rudder != 0) {
            garage_builder.setTitle("右侧舵机");
        } else {
            garage_builder.setTitle("左侧舵机");
        }
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            switch (i) {
                case 0:  // 上调
                    if (rudder != 0) {
                        FirstActivity.Connect_Transport.rudder_control(0x00, 0x01);
                    } else {
                        FirstActivity.Connect_Transport.rudder_control(0x01, 0x00);
                    }
                    break;
                case 1:  // 下调
                    if (rudder != 0) {
                        FirstActivity.Connect_Transport.rudder_control(0x00, 0x02);
                    } else {
                        FirstActivity.Connect_Transport.rudder_control(0x02, 0x00);
                    }
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }


    private void Smart_DataDisplay(String name,int id) {
        AlertDialog.Builder TFTbuilder = new AlertDialog.Builder(context);
        TFTbuilder.setTitle(name);
        String[] TFTitem = {"图片显示模式", "车牌显示", "计时模式", "距离显示", "HEX显示模式"};
        TFTbuilder.setSingleChoiceItems(TFTitem, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    switch (which) {
                        case 0:
                            Smart_DataDisplay_Image(id);
                            break;
                        case 1:
                            Smart_DataDisplay_plate_number(id);
                            break;
                        case 2:
                            Smart_DataDisplay_Timer(id);
                            break;
                        case 3:
                            Smart_DataDisplay_Distance(id);
                            break;
                        case 4:
                            Smart_DataDisplay_Hex_show(id);
                            break;
                        case 5:
                            TFT_traffic(id);
                            break;
                    }
                });
        TFTbuilder.create().show();
    }

    private void Smart_DataDisplay_Hex_show(int id) {
        AlertDialog.Builder TFT_Hex_builder = new AlertDialog.Builder(
                context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_hex, null);
        TFT_Hex_builder.setTitle("HEX显示模式");
        TFT_Hex_builder.setView(view);
        // 下拉列表
        final EditText editText1 = view.findViewById(R.id.editText1);
        final EditText editText2 = view.findViewById(R.id.editText2);
        final EditText editText3 = view.findViewById(R.id.editText3);
        TFT_Hex_builder.setPositiveButton("确定",
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    String ones = editText1.getText().toString();
                    String twos = editText2.getText().toString();
                    String threes = editText3.getText().toString();
                    // 显示数据，一个文本编译框最多两个数据显示数目管中两个数据
                    one = ones.equals("") ? 0x00 : Integer.parseInt(ones, 16);
                    two = twos.equals("") ? 0x00 : Integer.parseInt(twos, 16);
                    three = threes.equals("") ? 0x00 : Integer.parseInt(threes, 16);
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x40, one, two, three);
                });
        TFT_Hex_builder.setNegativeButton("取消",
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    dialog.cancel();
                });
        TFT_Hex_builder.create().show();
    }

    private void Smart_DataDisplay_Distance(int id) {
        AlertDialog.Builder TFT_Distance_builder = new AlertDialog.Builder(context);
        TFT_Distance_builder.setTitle("距离显示模式");
        String[] TFT_Image_item = {"400mm", "500mm", "600mm"};
        TFT_Distance_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            if (which == 0) {
                FirstActivity.Connect_Transport.TFT_LCD(id, 0x50, 0x00, 0x04, 0x00);
            }
            if (which == 1) {
                FirstActivity.Connect_Transport.TFT_LCD(id, 0x50, 0x00, 0x05, 0x00);
            }
            if (which == 2) {
                FirstActivity.Connect_Transport.TFT_LCD(id, 0x50, 0x00, 0x06, 0x00);
            }
        });
        TFT_Distance_builder.create().show();
    }

    private void Smart_DataDisplay_plate_number(int id) {
        AlertDialog.Builder TFT_plate_builder = new AlertDialog.Builder(context);
        TFT_plate_builder.setTitle("车牌显示模式");
        final String[] TFT_Image_item = {"Z799C4", "B554H1", "D888B8"};
        TFT_plate_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x20, 'Z', '7', '9');
                    FirstActivity.Connect_Transport.yanchi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x21, '9', 'C', '4');
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x20, 'B', '5', '5');
                    FirstActivity.Connect_Transport.yanchi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x21, '4', 'H', '1');
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x20, 'D', '8', '8');
                    FirstActivity.Connect_Transport.yanchi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x21, '8', 'B', '8');
                    break;
            }
        });
        TFT_plate_builder.create().show();
    }

    private void Smart_DataDisplay_Timer(int id) {
        AlertDialog.Builder TFT_Iimer_builder = new AlertDialog.Builder(context);
        TFT_Iimer_builder.setTitle("计时模式");
        String[] TFT_Image_item = {"开始", "关闭", "停止"};
        TFT_Iimer_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x30, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x30, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x30, 0x00, 0x00, 0x00);
                    break;
            }
        });
        TFT_Iimer_builder.create().show();
    }

    private void Smart_DataDisplay_Image(int id) {
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(context);
        TFT_Image_builder.setTitle("图片显示模式");
        String[] TFT_Image_item = {"指定显示", "上翻一页", "下翻一页", "自动翻页"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    Smart_DataDisplay_Pic_show(id);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x01, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x02, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x03, 0x00, 0x00);
                    break;
            }
        });
        TFT_Image_builder.create().show();
    }

    private Bitmap bitmap;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Zigbee_Landmark zigbeeLandmark = mZigbeeLandmarkList.get(position);
        bitmap = BitmapFactory.decodeResource(context.getResources(), zigbeeLandmark.getImageId(), null);
        bitmap = RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 10);
        holder.zigbeeImage.setImageBitmap(bitmap);
        holder.zigbeeName.setText(zigbeeLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mZigbeeLandmarkList.size();
    }


    //智能交通灯标志物控制数据结构
    private void Traffic_light(String name,int id) {
        AlertDialog.Builder traffic_builder = new AlertDialog.Builder(context);
        traffic_builder.setTitle(name);
        String[] tr_light = {"进入识别模式", "进行自动识别", "当前为红灯，请求确认", "当前为绿灯，请求确认", "当前为黄灯，请求确认"};
        traffic_builder.setSingleChoiceItems(tr_light, -1, (dialog, i) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            switch (i) {
                case 0:
                    FirstActivity.Connect_Transport.traffic_control(id, 0x01, 0x00);
                    break;
                case 1:
                    break;
                case 2:
                    FirstActivity.Connect_Transport.traffic_control(id, 0x02, 0x01);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.traffic_control(id, 0x02, 0x02);
                    break;
                case 4:
                    FirstActivity.Connect_Transport.traffic_control(id, 0x02, 0x03);
                    break;
                default:
                    break;
            }

        });
        traffic_builder.create().show();
    }

    //立体车库
    private void stereo_garage(String name,int id) {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        garage_builder.setTitle(name);
        String[] ga = {"复位（第一层）", "到达第二层", "到达第三层", "到达第四层", "请求返回车库当前层数", "请求返回车库前/后侧\n" +
                "红外状态"};
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            switch (i) {
                case 0:  //到达第一层
                    FirstActivity.Connect_Transport.garage_control(id, 0x01, 0x01);
                    break;
                case 1:  //到达第二层
                    FirstActivity.Connect_Transport.garage_control(id, 0x01, 0x02);
                    break;
                case 2:  //到达第三层
                    FirstActivity.Connect_Transport.garage_control(id, 0x01, 0x03);
                    break;
                case 3:  //到达第四层
                    FirstActivity.Connect_Transport.garage_control(id, 0x01, 0x04);
                    break;
                case 4:  //请求返回车库位于第几层
                    FirstActivity.Connect_Transport.garage_control(id, 0x02, 0x01);
                    break;
                case 5:  //请求返回前后侧红外状态
                    FirstActivity.Connect_Transport.garage_control(id, 0x02, 0x02);
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    private void gateController() {
        AlertDialog.Builder gt_builder = new AlertDialog.Builder(context);
        gt_builder.setTitle("智能道闸标志物");
        String[] gt = {"开启", "关闭", "车牌显示模式", "道闸初始角度调节", "请求返回道闸状态"};
        gt_builder.setSingleChoiceItems(gt, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    switch (which) {
                        case 0:
                            // 打开道闸标志物
                            FirstActivity.Connect_Transport.gate(0x01, 0x01, 0x00, 0x00);
                            break;
                        case 1:
                            // 关闭道闸标志物
                            FirstActivity.Connect_Transport.gate(0x01, 0x02, 0x00, 0x00);
                            break;
                        case 2:
                            //显示车牌
                            gate_plate_number();
                            break;
                        case 3:
                            //调节初始角度
                            gate_angle_number();
                            break;
                        case 4:
                            //请求返回道闸标志物状态
                            FirstActivity.Connect_Transport.gate(0x20, 0x01, 0x00, 0x00);
                            break;
                        default:
                            break;
                    }
                });
        gt_builder.create().show();
    }


    private void gate_plate_number() {
        AlertDialog.Builder gate_plate_builder = new AlertDialog.Builder(context);
        gate_plate_builder.setTitle("道闸显示车牌");
        final String[] gate_Image_item = {"A123B4", "B567C8", "D910E1"};
        gate_plate_builder.setSingleChoiceItems(gate_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.gate(0x10, 'A', '1', '2');
                    FirstActivity.Connect_Transport.yanchi(500);
                    FirstActivity.Connect_Transport.gate(0x11, '3', 'B', '4');
                    break;
                case 1:
                    FirstActivity.Connect_Transport.gate(0x10, 'B', '5', '6');
                    FirstActivity.Connect_Transport.yanchi(500);
                    FirstActivity.Connect_Transport.gate(0x11, '7', 'C', '8');
                    break;
                case 2:
                    FirstActivity.Connect_Transport.gate(0x10, 'D', '9', '1');
                    FirstActivity.Connect_Transport.yanchi(500);
                    FirstActivity.Connect_Transport.gate(0x11, '0', 'E', '1');
                    break;
            }
        });
        gate_plate_builder.create().show();
    }


    private void gate_angle_number() {
        AlertDialog.Builder gate_plate_builder = new AlertDialog.Builder(context);
        gate_plate_builder.setTitle("道闸初始角度调节");
        final String[] gate_Image_item = {"上升", "下降"};
        gate_plate_builder.setSingleChoiceItems(gate_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.gate(0x09, 0x01, 0, 0);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.gate(0x09, 0x02, 0, 0);
                    break;
                default:
                    break;
            }
        });
        gate_plate_builder.create().show();
    }

    private void digital() {// 智能显示标志物
        AlertDialog.Builder dig_timeBuilder = new AlertDialog.Builder(
                context);
        dig_timeBuilder.setTitle("智能显示标志物");
        String[] dig_item = {"显示指定数据", "显示计时信息", "显示距离信息"};
        dig_timeBuilder.setSingleChoiceItems(dig_item, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (which == 0) {// LED显示标志物显示
                        digitalController();

                    } else if (which == 1) {// LED显示标志物计时
                        digital_time();

                    } else if (which == 2) {// 显示距离
                        digital_dis();

                    }
                });
        dig_timeBuilder.create().show();
    }

    // LED显示标志物显示方法
    private final String[] itmes = {"第一行", "第二行"};
    int main, one, two, three;

    private void digitalController() {

        AlertDialog.Builder dg_Builder = new AlertDialog.Builder(
                context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_digital, null);
        dg_Builder.setTitle("数码管显示指定数据");
        dg_Builder.setView(view);
        // 下拉列表
        Spinner spinner = view.findViewById(R.id.spinner);
        final EditText editText1 = view.findViewById(R.id.editText1);
        final EditText editText2 = view.findViewById(R.id.editText2);
        final EditText editText3 = view.findViewById(R.id.editText3);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, itmes);
        spinner.setAdapter(adapter);
        // 下拉列表选择监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                vSimple(context,30); // 控制手机震动进行反馈
                // TODO Auto-generated method stub
                main = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        dg_Builder.setPositiveButton("确定",
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    String ones = editText1.getText().toString();
                    String twos = editText2.getText().toString();
                    String threes = editText3.getText().toString();
                    // 显示数据，一个文本编译框最多两个数据显示数目管中两个数据
                    one = ones.equals("") ? 0x00 : Integer.parseInt(ones, 16);
                    two = twos.equals("") ? 0x00 : Integer.parseInt(twos, 16);
                    three = threes.equals("") ? 0x00 : Integer.parseInt(threes, 16);
                    FirstActivity.Connect_Transport.digital(main, one, two, three);
                });

        dg_Builder.setNegativeButton("取消",
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    dialog.cancel();
                });
        dg_Builder.create().show();
    }

    private final int dgtime_index = -1;

    private void digital_time() {// LED显示标志物计时
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(
                context);
        dg_timeBuilder.setTitle("数码管显示计时模式");
        String[] dgtime_item = {"计时结束", "计时开始", "清零"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, dgtime_index,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (which == 0) {// 计时结束
                        FirstActivity.Connect_Transport.digital_close();

                    } else if (which == 1) {// 计时开启
                        FirstActivity.Connect_Transport.digital_open();

                    } else if (which == 2) {// 计时清零
                        FirstActivity.Connect_Transport.digital_clear();

                    }
                });
        dg_timeBuilder.create().show();
    }

    private void digital_dis() {
        AlertDialog.Builder dis_timeBuilder = new AlertDialog.Builder(context);
        dis_timeBuilder.setTitle("数码管显示距离模式");
        final String[] dis_item = {"100mm", "200mm", "400mm"};
        int dgdis_index = -1;
        dis_timeBuilder.setSingleChoiceItems(dis_item, dgdis_index,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    if (which == 0) {// 距离100mm
                        FirstActivity.Connect_Transport.digital_dic(Integer.parseInt(dis_item[which]
                                .substring(0, 3)));
                    } else if (which == 1) {// 距离20mmm
                        FirstActivity.Connect_Transport.digital_dic(Integer.parseInt(dis_item[which]
                                .substring(0, 3)));
                    } else if (which == 2) {// 距离400mm
                        FirstActivity.Connect_Transport.digital_dic(Integer.parseInt(dis_item[which]
                                .substring(0, 3)));
                    }
                });
        dis_timeBuilder.create().show();
    }

    private TextView voiceText;

    private void voiceController() {
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(
                context);
        dg_timeBuilder.setTitle("智能公交站标志物");
        String[] dgtime_item = {"播报随机指令", "播报指定内容", "设置天气及温度"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, dgtime_index,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (which == 0) {// 语音播报随机指令
                        FirstActivity.Connect_Transport.VoiceBroadcast();

                    } else if (which == 1) {// 语音播报指定内容
                        createLoadingDialog(context);
                    } else if (which == 2) {
                        voiceWeather();
                    }
                });
        dg_timeBuilder.create().show();
    }

    private static Dialog loadingDialog;

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
        TextView tittle_tv = v.findViewById(R.id.tips);
        tittle_tv.setText("请输入播报内容");
        ClearEditText editText = v.findViewById(R.id.edit);
        editText.setText(R.string.voiceText);
        Button close_btn = v.findViewById(R.id.left_button);
        close_btn.setText("取消");
        Button set_btn = v.findViewById(R.id.right_button);
        set_btn.setText("播报");
        close_btn.setOnClickListener(v1 -> {
            vSimple(context,30); // 控制手机震动进行反馈
            closeDialog();
        });
        set_btn.setOnClickListener(v1 -> {
            vSimple(context,30); // 控制手机震动进行反馈
            String str = editText.getText().toString();
            if (str.equals("")) {
                str = "请输入你要播报的内容";
            }
            try {
                byte[] sbyte = bytesend(str.getBytes("GBK"));
                FirstActivity.Connect_Transport.send_voice(sbyte);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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


    private final int[] setVoiceWeather = new int[2];

    private void setDialog() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_car, null);
        voiceText = view.findViewById(R.id.voiceText);
        voiceText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        voiceText.setText("");
        voiceText.setHint("温度范围 0～60 ℃");
        TextView voiceTittle = view.findViewById(R.id.voiceTittle);
        voiceTittle.setText("请输入温度信息：");
        AlertDialog.Builder voiceBuilder = new AlertDialog.Builder(context);
        voiceBuilder.setTitle("设置温度");
        voiceBuilder.setView(view);
        voiceBuilder.setPositiveButton("发送",
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    String src = voiceText.getText().toString();
                    if (src.equals("") || (Integer.parseInt(src) < 0) || (Integer.parseInt(src) > 60)) {
                        ToastUtil.ShowToast(context,"请输入正确温度信息！");
                    } else {
                        setVoiceWeather[1] = Integer.parseInt(src);
                        FirstActivity.Connect_Transport.voiceWeather(setVoiceWeather);
                        ToastUtil.ShowToast(context,"天气 及 温度信息已设置！");
                        dialog.cancel();
                    }
                });
        voiceBuilder.setNegativeButton("取消", null);
        voiceBuilder.create().show();
    }

    /**
     * 设置天气信息
     */
    private void voiceWeather() {
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(
                context);

        dg_timeBuilder.setTitle("设置天气");
        String[] dgtime_item = {"大风", "多云", "晴", "小雪", "小雨", "阴天"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, dgtime_index,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    setVoiceWeather[0] = (short) (which);
                    setDialog();
                });
        dg_timeBuilder.create().show();
    }

    private static byte[] bytesend(byte[] sbyte) {
        byte[] textbyte = new byte[sbyte.length + 5];
        textbyte[0] = (byte) 0xFD;
        textbyte[1] = (byte) (((sbyte.length + 2) >> 8) & 0xff);
        textbyte[2] = (byte) ((sbyte.length + 2) & 0xff);
        textbyte[3] = 0x01;// 合成语音命令
        textbyte[4] = (byte) 0x01;// 编码格式
        System.arraycopy(sbyte, 0, textbyte, 5, sbyte.length);
        return textbyte;
    }

    private void magnetic_suspension() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("智能无线充电标志物");
        String[] item2 = {"开启", "关闭"};
        builder.setSingleChoiceItems(item2, -1,
                (dialog, which) -> {
                    vSimple(context,30); // 控制手机震动进行反馈
                    // TODO Auto-generated method stub
                    if (which == 0) {
                        FirstActivity.Connect_Transport.magnetic_suspension(0x01, 0x01, 0x00, 0x00);
                    } else if (which == 1) {
                        FirstActivity.Connect_Transport.magnetic_suspension(0x01, 0x02, 0x00, 0x00);
                    }
                });
        builder.create().show();
    }

    private void Smart_DataDisplay_Pic_show(int id) {
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(context);
        TFT_Image_builder.setTitle("指定图片显示");
        String[] TFT_Image_item = {"1", "2", "3", "4", "5"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x03, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x04, 0x00, 0x00);
                    break;
                case 4:
                    FirstActivity.Connect_Transport.TFT_LCD(id, 0x10, 0x05, 0x00, 0x00);
                    break;
            }
        });
        TFT_Image_builder.create().show();
    }


    private void TFT_traffic(final int type) {
        AlertDialog.Builder TFT_Iimer_builder = new AlertDialog.Builder(context);
        if (type != 0x0B) {
            TFT_Iimer_builder.setTitle("TFT-B 交通标志显示模式");
        } else TFT_Iimer_builder.setTitle("TFT-A 交通标志显示模式）");
        String[] TFT_Image_item = {"直行", "左转", "右转", "掉头", "禁止直行", "禁止通行"};
        TFT_Iimer_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            vSimple(context,30); // 控制手机震动进行反馈
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x03, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x04, 0x00, 0x00);
                    break;
                case 4:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x05, 0x00, 0x00);
                    break;
                case 5:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x06, 0x00, 0x00);
                    break;
            }
        });
        TFT_Iimer_builder.create().show();
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