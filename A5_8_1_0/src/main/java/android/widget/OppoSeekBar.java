package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;

public class OppoSeekBar extends SeekBar {
    private OnOppoSeekBarFromUserChangeListener mOppoSeekBarFromUserChangeListener;
    private int mSafeMediaVolumeIndex;
    private boolean safeMediaVolumeEnabled;

    public interface OnOppoSeekBarFromUserChangeListener {
        void onOppoSeekBarProgressrFromUserChanged(SeekBar seekBar, int i, boolean z);
    }

    public OppoSeekBar(Context context) {
        this(context, null);
    }

    public OppoSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842875);
    }

    public OppoSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.safeMediaVolumeEnabled = false;
        this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(202179609);
    }

    @RemotableViewMethod
    synchronized boolean setProgressInternal(int progress, boolean fromUser, boolean animate) {
        boolean on = false;
        if (this.safeMediaVolumeEnabled) {
            on = true;
            if (progress < this.mSafeMediaVolumeIndex) {
                on = false;
            }
        }
        if (on) {
            if (this.mSafeMediaVolumeIndex + 1 == progress && this.mOppoSeekBarFromUserChangeListener != null) {
                this.mOppoSeekBarFromUserChangeListener.onOppoSeekBarProgressrFromUserChanged(this, progress, fromUser);
            }
            progress = this.mSafeMediaVolumeIndex;
        }
        return super.setProgressInternal(progress, fromUser, animate);
    }

    public void setSafeMediaVolumeEnabled(boolean enable) {
        if (enable) {
            this.safeMediaVolumeEnabled = this.mContext.getResources().getBoolean(202114056);
        } else {
            this.safeMediaVolumeEnabled = false;
        }
    }

    public void setOppoSeekBarFromUserChangeListener(OnOppoSeekBarFromUserChangeListener listener) {
        this.mOppoSeekBarFromUserChangeListener = listener;
    }
}
