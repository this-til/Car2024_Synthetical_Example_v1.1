package car.bkrc.com.car2024.Utils.OtherUtil;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void ShowToast(Context context,String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setText(msg);
        toast.show();
    }
}
