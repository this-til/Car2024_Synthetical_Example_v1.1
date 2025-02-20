package car.bkrc.com.car2024.Utils.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.Utils.OtherUtil.ToastUtil;

public class DialogUtils {
    private static TextView tipTextView;
    private static EditText editText;
    private static Button button;
    private static Dialog ProgressDialog;


    @SuppressLint("MissingInflatedId")
    public static void showCompleteDialog(Context context, String tittle, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.toast_motify_succeed, null);// 得到加载view
        tipTextView = v.findViewById(R.id.tittle_tv);// 提示文字
        editText = v.findViewById(R.id.camera_ip_et);
        button = v.findViewById(R.id.ip_ok_bt);
        tipTextView.setText(tittle);// 设置加载信息
        editText.setText(msg);
        button.setOnClickListener(v1 -> {
            if (isIPAddressByRegex(editText.getText().toString())) {
                ipSave(context, editText.getText().toString());
                ProgressDialog.dismiss();
            } else ToastUtil.ShowToast(context,"请检查输入的IP地址是否正常！");
        });
        ProgressDialog = new Dialog(context, R.style.MyDialogStyle);// 创建自定义样式dialog
        ProgressDialog.setCancelable(true); // 是否可以按“返回键”消失
        ProgressDialog.setCanceledOnTouchOutside(true); // 点击加载框以外的区域
        ProgressDialog.setContentView(v, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        /**
         *将显示Dialog的方法封装在这里面
         */
        Window window = ProgressDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL);
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.PopWindowAnimStyle);
        ProgressDialog.show();
    }


    /**
     * 判断本地保存了摄像头地址
     */
    private static void ipSave(Context context, String ip) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("IPCheck", 0);
        sharedPreferences.edit().putString("CameraIP", ip).apply();
    }


    /**
     * 用正则表达式进行IP地址输入的判断
     */
    public static boolean isIPAddressByRegex(String str) {
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        // 判断ip地址是否与正则表达式匹配
        if (str.matches(regex)) {
            String[] arr = str.split("\\.");
            for (int i = 0; i < 4; i++) {
                int temp = Integer.parseInt(arr[i]);
                //如果某个数字不是0到255之间的数 就返回false
                if (temp < 0 || temp > 255) return false;
            }
            return true;
        } else return false;
    }

}