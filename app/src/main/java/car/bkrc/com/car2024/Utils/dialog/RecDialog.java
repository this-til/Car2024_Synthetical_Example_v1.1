package car.bkrc.com.car2024.Utils.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import car.bkrc.com.car2024.R;


public class RecDialog {

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
     * 识别结果弹窗
     * @param context
     * @param bitmap 传入图片
     * @param data 传入识别结果
     */
    @SuppressLint("StaticFieldLeak")
    public static void createLoadingDialog(Context context, Bitmap bitmap,String tit,String data) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.pic_recong_dialog_item, null);// 得到加载view
        LinearLayout layout = v
                .findViewById(R.id.showList);// 加载布局
        ImageView qrImageView = v.findViewById(R.id.qr_rec_iv);
        TextView textView = v.findViewById(R.id.qr_rec_tv);
        TextView tittle = v.findViewById(R.id.tips);
        textView.setText(data);
        tittle.setText(tit);
        qrImageView.setImageBitmap(bitmap);
        v.findViewById(R.id.ok_button).setOnClickListener(v1 -> {
            closeDialog();
        });
        loadingDialog = new Dialog(context, R.style.MyDialogStyle); // 创建自定义样式dialog
        loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
        loadingDialog.setCanceledOnTouchOutside(true); // 点击加载框以外的区域
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)); // 设置布局
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
}
