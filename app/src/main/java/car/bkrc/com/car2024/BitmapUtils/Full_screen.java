package car.bkrc.com.car2024.BitmapUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import car.bkrc.com.car2024.R;

public class Full_screen {
    public static void bigImageLoader(Context context, Bitmap bitmap){
        LayoutInflater inflater = LayoutInflater.from(context);
        View imgEntryView = inflater.inflate(R.layout.dialog_photo, null);
        // 加载自定义的布局文件
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        ImageView img = imgEntryView.findViewById(R.id.large_image);
        img.setImageBitmap(bitmap);
        dialog.setView(imgEntryView); // 自定义dialog
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        img.setOnClickListener(v -> dialog.cancel());
    }

}
