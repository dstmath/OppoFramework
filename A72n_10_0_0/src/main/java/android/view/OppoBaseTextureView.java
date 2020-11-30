package android.view;

import android.common.PswFrameworkFactory;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import com.oppo.screenmode.IPswScreenModeFeature;

public class OppoBaseTextureView extends View {
    private static final boolean DEBUG = true;
    private static final String LOCAL_TAG = "OppoBaseTextureView";
    protected boolean mCallBackSizeChangeWhenLayerUpdate = false;
    private IPswScreenModeFeature mIPswScreenModeFeature = null;
    protected boolean mReleaseTextureWhenDestory = false;

    public void setReleaseTextureWhenDestory(boolean release) {
        this.mReleaseTextureWhenDestory = release;
    }

    public OppoBaseTextureView(Context context) {
        super(context);
    }

    public OppoBaseTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OppoBaseTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OppoBaseTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isReleaseTextureWhenDestory() {
        return this.mReleaseTextureWhenDestory;
    }

    public void setCallBackSizeChangeWhenLayerUpdate(boolean doCallBack) {
        this.mCallBackSizeChangeWhenLayerUpdate = doCallBack;
    }

    public boolean isCallBackSizeChangeWhenLayerUpdate() {
        return this.mCallBackSizeChangeWhenLayerUpdate;
    }

    private IPswScreenModeFeature getOppoScreenModeFeature() {
        if (this.mIPswScreenModeFeature == null) {
            this.mIPswScreenModeFeature = (IPswScreenModeFeature) PswFrameworkFactory.getInstance().getFeature(IPswScreenModeFeature.DEFAULT, this.mContext);
        }
        return this.mIPswScreenModeFeature;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindowInternal() {
        Log.d(LOCAL_TAG, "onDetachedFromWindowInternal");
        getOppoScreenModeFeature().setRefreshRate(this, 0);
        super.onDetachedFromWindowInternal();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getOppoScreenModeFeature().setRefreshRate(this, -1);
    }

    /* access modifiers changed from: protected */
    public void onSetRefreshRate(int rate) {
        getOppoScreenModeFeature().setRefreshRate(this, rate);
    }
}
