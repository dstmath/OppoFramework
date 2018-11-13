package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager.LayoutParams;
import android.widget.ColorPopupWindow.OnAnimateDismissListener;
import android.widget.ColorPopupWindow.OnPreInvokePopupListener;
import com.android.internal.R;

public class ColorListPopupWindow extends ListPopupWindow implements OnPreInvokePopupListener, OnAnimateDismissListener {
    private static final boolean DBG = true;
    private static final String TAG = "ColorListPopupWindow";

    public ColorListPopupWindow(Context context) {
        this(context, null, R.attr.listPopupWindowStyle, 0);
    }

    public ColorListPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listPopupWindowStyle, 0);
    }

    public ColorListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    PopupWindow createPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        ColorPopupWindow popup = new ColorPopupWindow(context, attrs, defStyleAttr, defStyleRes);
        popup.setOnPreInvokePopupListener(this);
        popup.setOnAnimateDismissListener(this);
        return popup;
    }

    public void onPreInvokePopup(LayoutParams p) {
    }

    public void onAnimateDismissStart(ColorPopupWindow popup) {
        popup.superDismiss();
    }

    public void onAnimateDismissEnd(ColorPopupWindow popup) {
    }
}
