package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;

public class ColorPopupWindow extends PopupWindow {
    private static final boolean DBG = true;
    private static final String TAG = "ColorPopupWindow";
    private boolean mIsDismissing;
    private OnAnimateDismissListener mOnAnimateDismissListener;
    private OnPreInvokePopupListener mOnPreInvokePopupListener;

    public interface OnPreInvokePopupListener {
        void onPreInvokePopup(LayoutParams layoutParams);
    }

    public interface OnAnimateDismissListener {
        void onAnimateDismissEnd(ColorPopupWindow colorPopupWindow);

        void onAnimateDismissStart(ColorPopupWindow colorPopupWindow);
    }

    public ColorPopupWindow(Context context) {
        this(context, null);
    }

    public ColorPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, (int) R.attr.popupWindowStyle);
    }

    public ColorPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mOnPreInvokePopupListener = null;
        this.mOnAnimateDismissListener = null;
        this.mIsDismissing = false;
    }

    public ColorPopupWindow() {
        this(null, 0, 0);
    }

    public ColorPopupWindow(View contentView) {
        this(contentView, 0, 0);
    }

    public ColorPopupWindow(int width, int height) {
        this(null, width, height);
    }

    public ColorPopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    public ColorPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
        this.mOnPreInvokePopupListener = null;
        this.mOnAnimateDismissListener = null;
        this.mIsDismissing = false;
    }

    public void dismiss() {
        if (!this.mIsDismissing) {
            this.mIsDismissing = true;
            if (this.mOnAnimateDismissListener != null) {
                this.mOnAnimateDismissListener.onAnimateDismissStart(this);
            } else {
                superDismiss();
            }
        }
    }

    void invokePopup(LayoutParams p) {
        if (this.mOnPreInvokePopupListener != null) {
            this.mOnPreInvokePopupListener.onPreInvokePopup(p);
        }
        super.invokePopup(p);
    }

    public void setOnPreInvokePopupListener(OnPreInvokePopupListener listener) {
        this.mOnPreInvokePopupListener = listener;
    }

    public void setOnAnimateDismissListener(OnAnimateDismissListener listener) {
        this.mOnAnimateDismissListener = listener;
    }

    public void superDismiss() {
        try {
            super.dismiss();
            if (this.mOnAnimateDismissListener != null) {
                this.mOnAnimateDismissListener.onAnimateDismissEnd(this);
            }
        } catch (IllegalArgumentException e) {
            if (this.mOnAnimateDismissListener != null) {
                this.mOnAnimateDismissListener.onAnimateDismissEnd(this);
            }
        } catch (Throwable th) {
            if (this.mOnAnimateDismissListener != null) {
                this.mOnAnimateDismissListener.onAnimateDismissEnd(this);
            }
            this.mIsDismissing = false;
        }
        this.mIsDismissing = false;
    }
}
