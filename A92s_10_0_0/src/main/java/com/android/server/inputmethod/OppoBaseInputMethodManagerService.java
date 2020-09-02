package com.android.server.inputmethod;

import android.os.IBinder;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodManager;

public abstract class OppoBaseInputMethodManagerService extends IInputMethodManager.Stub {
    public IColorInputMethodManagerServiceEx mColorImmsEx = null;

    /* access modifiers changed from: protected */
    public boolean shouldBuildInputMethodList(String methodId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getLayoutResIdForInputmethodWwitch(int resId) {
        IColorInputMethodManagerServiceEx iColorInputMethodManagerServiceEx = this.mColorImmsEx;
        if (iColorInputMethodManagerServiceEx != null) {
            return iColorInputMethodManagerServiceEx.getLayoutResIdForInputmethodWwitch(resId);
        }
        return resId;
    }

    /* access modifiers changed from: protected */
    public boolean onSetInputMethod(IBinder token, String id) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onCalledWithValidTokenLocked(IBinder token) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onShowMySoftInput(IBinder token, int flags) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onHideMySoftInput(IBinder token, int flags) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onSetSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId, boolean setSubtypeOnly) {
        return false;
    }

    /* access modifiers changed from: protected */
    public IInputContext getCorrectInputContext(IInputContext ic) {
        return ic;
    }

    /* access modifiers changed from: protected */
    public String getDebugFlag(String s) {
        return s;
    }
}
