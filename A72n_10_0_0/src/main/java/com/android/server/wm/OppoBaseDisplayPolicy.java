package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import com.android.server.am.IColorGameSpaceManager;

public abstract class OppoBaseDisplayPolicy {
    protected IColorDisplayPolicyEx mColorDpEx = null;
    protected IColorDisplayPolicyInner mColorDpInner = null;

    public IColorDisplayPolicyEx getDisplayPolicyEx() {
        return this.mColorDpEx;
    }

    /* access modifiers changed from: protected */
    public boolean requestGameDockIfNecessary() {
        return OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).requestGameDockIfNecessary();
    }

    /* access modifiers changed from: protected */
    public int getSystemUIFlagAfterGesture(int sysuiFlags) {
        return OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).getSystemUIFlagAfterGesture(sysuiFlags);
    }

    /* access modifiers changed from: package-private */
    public void adjustOppoWindowFrame(Rect pf, Rect df, Rect of, Rect cf, Rect dcf, Rect vf, WindowManager.LayoutParams attrs, DisplayFrames displayFrames) {
    }

    public boolean isGlobalActionVisible() {
        return false;
    }

    /* access modifiers changed from: protected */
    public Handler createPolicyHandlerWrapper(Looper looper) {
        return new Handler(looper);
    }
}
