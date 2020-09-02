package com.android.server.inputmethod;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.InputBindResult;

public class ColorSecureInputMethodManagerService extends InputMethodManagerService {
    public static final String SEC_IMS_SERVICE_NAME = "com.coloros.securitykeyboard/.InputService";
    public static boolean isExist = false;
    private ColorInputMethodManagerService mColorInputMethodManagerService;

    public ColorSecureInputMethodManagerService(Context context) {
        super(context);
    }

    public void setInputMethodManagerService(ColorInputMethodManagerService colorInputMethodManagerService) {
        this.mColorInputMethodManagerService = colorInputMethodManagerService;
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        if (!isExist) {
            return InputBindResult.NULL;
        }
        this.mCurMethodId = SEC_IMS_SERVICE_NAME;
        try {
            return ColorSecureInputMethodManagerService.super.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
        } catch (IllegalArgumentException e) {
            Log.d(this.TAG, "startInputOrWindowGainedFocus:error ");
            return InputBindResult.NULL;
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldBuildInputMethodList(String methodId) {
        if (!SEC_IMS_SERVICE_NAME.equals(methodId)) {
            return false;
        }
        isExist = true;
        return false;
    }

    /* access modifiers changed from: package-private */
    public void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        isExist = false;
        ColorSecureInputMethodManagerService.super.buildInputMethodListLocked(resetDefaultEnabledIme);
    }

    /* access modifiers changed from: package-private */
    public void setInputMethodLocked(String id, int subtypeId) {
        if (SEC_IMS_SERVICE_NAME.equals(id)) {
            ColorSecureInputMethodManagerService.super.setInputMethodLocked(id, subtypeId);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onSetSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId, boolean setSubtypeOnly) {
        if (imi == null || SEC_IMS_SERVICE_NAME.equals(imi.getId())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void executeOrSendMessage(IInterface target, Message msg) {
        ColorInputMethodManagerService colorInputMethodManagerService = this.mColorInputMethodManagerService;
        if (colorInputMethodManagerService == null || colorInputMethodManagerService.useSecure || msg.what != 3010) {
            ColorSecureInputMethodManagerService.super.executeOrSendMessage(target, msg);
        } else {
            Log.d(this.TAG, "error: executeOrSendMessage: MSG_BIND_CLIENT");
        }
    }

    /* access modifiers changed from: protected */
    public boolean onCalledWithValidTokenLocked(IBinder token) {
        ColorInputMethodManagerService colorInputMethodManagerService = this.mColorInputMethodManagerService;
        return colorInputMethodManagerService != null && token == colorInputMethodManagerService.mCurToken;
    }

    /* access modifiers changed from: protected */
    public boolean onSetInputMethod(IBinder token, String id) {
        ColorInputMethodManagerService colorInputMethodManagerService = this.mColorInputMethodManagerService;
        return colorInputMethodManagerService != null && colorInputMethodManagerService.onSetInputMethod(token, id);
    }

    /* access modifiers changed from: package-private */
    public boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        synchronized (this.mMethodMap) {
            if (this.mColorInputMethodManagerService != null && !this.mColorInputMethodManagerService.useSecure) {
                return false;
            }
            boolean hideCurrentInputLocked = ColorSecureInputMethodManagerService.super.hideCurrentInputLocked(flags, resultReceiver);
            return hideCurrentInputLocked;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        synchronized (this.mMethodMap) {
            if (this.mColorInputMethodManagerService != null && !this.mColorInputMethodManagerService.useSecure) {
                return false;
            }
            boolean showCurrentInputLocked = ColorSecureInputMethodManagerService.super.showCurrentInputLocked(flags, resultReceiver);
            return showCurrentInputLocked;
        }
    }

    /* access modifiers changed from: package-private */
    public void onActionLocaleChanged() {
        Log.d(this.TAG, "onActionLocaleChanged: no change");
    }

    /* access modifiers changed from: protected */
    public String getDebugFlag(String s) {
        return "InputMethodManagerService_Secure";
    }

    /* access modifiers changed from: protected */
    public boolean hideCurrentInputLockedInternal() {
        boolean hideCurrentInputLocked;
        synchronized (this.mMethodMap) {
            hideCurrentInputLocked = ColorSecureInputMethodManagerService.super.hideCurrentInputLocked(0, (ResultReceiver) null);
        }
        return hideCurrentInputLocked;
    }
}
