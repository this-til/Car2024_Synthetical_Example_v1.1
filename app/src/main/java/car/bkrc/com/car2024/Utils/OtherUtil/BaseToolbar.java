package car.bkrc.com.car2024.Utils.OtherUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import androidx.appcompat.widget.Toolbar;

import car.bkrc.com.car2024.R;


public abstract class BaseToolbar extends Toolbar {

    protected OnOptionItemClickListener mOnOptionItemClickListener;

    public BaseToolbar(Context context) {
        this(context, null);
    }

    public BaseToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.toolbarStyle);
    }

    public BaseToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    protected void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        initCustomView(context, attrs, defStyleAttr);
    }

    protected abstract void initCustomView(Context context, AttributeSet attrs, int defStyleAttr);

    public boolean isChild(View view) {
        return view != null && view.getParent() == this;
    }

    public boolean isChild(View view, ViewParent parent) {
        return view != null && view.getParent() == parent;
    }

    public void setOnOptionItemClickListener(OnOptionItemClickListener listener) {
        mOnOptionItemClickListener = listener;
    }

    public interface OnOptionItemClickListener {
        void onOptionItemClick(View v);
    }

    public int dp2px(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
