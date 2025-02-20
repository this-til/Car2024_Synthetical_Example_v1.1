package car.bkrc.com.car2024.Utils.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import car.bkrc.com.car2024.R;

class TransparentDialog extends Dialog {

	public TransparentDialog(Context context, int theme) {
		super(context, theme);
	}
	
	public static TransparentDialog createDialog(Context context) {
		TransparentDialog dialog = new TransparentDialog(context, R.style.Transparent);
		dialog.setContentView(R.layout.transparent);
		dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		return dialog;
	}

	public void setMessage(String message) {
		TextView msgView = findViewById(R.id.transparent_message);
		msgView.setText(message);
	}
	
	public void setImage(Context ctx, int resId) {
		ImageView image = findViewById(R.id.transparent_image);
		image.setImageResource(resId);
		
		if(resId==R.drawable.transparent_spinner) {
			Animation anim = AnimationUtils.loadAnimation(ctx,R.anim.progressbar);
			anim.start();
			image.startAnimation(anim);
		}

	}

}
