package com.android.server.inputmethod;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.InputBindResult;
import com.android.server.LocalServices;
import com.android.server.inputmethod.InputMethodManagerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorScreenshotManager;
import com.color.util.ColorSecureKeyboardUtils;

public class ColorInputMethodManagerService extends InputMethodManagerService {
    private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodManager";
    private static String SEC_IMS_SERVICE_NAME = ColorSecureInputMethodManagerService.SEC_IMS_SERVICE_NAME;
    private static final String SETTINGS_SECURITY_WINDOW = "security_window";
    private boolean isSwitchFormSecureIME = false;
    public InputBindResult mColorInputBindResult;
    private ColorSecureInputMethodManagerService mSecureInputMethodService;
    public boolean useSecure = false;

    public ColorInputMethodManagerService(Context context) {
        super(context);
        this.mSecureInputMethodService = new ColorSecureInputMethodManagerService(context);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != 10002) {
            return ColorInputMethodManagerService.super.onTransact(code, data, reply, flags);
        }
        data.enforceInterface(DESCRIPTOR);
        hideCurrentInputMethod();
        return true;
    }

    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (this.useSecure) {
            if (this.mInputShown) {
                ColorInputMethodManagerService.super.hideCurrentInputLocked(0, (ResultReceiver) null);
            }
            return this.mSecureInputMethodService.showSoftInput(client, flags, resultReceiver);
        }
        if (this.mSecureInputMethodService.mInputShown) {
            this.mSecureInputMethodService.hideCurrentInputLocked(0, null);
        }
        return ColorInputMethodManagerService.super.showSoftInput(client, flags, resultReceiver);
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (isLongshotMode()) {
            return hideCurrentInputLocked(flags, resultReceiver);
        }
        if (this.useSecure) {
            return this.mSecureInputMethodService.hideSoftInput(client, flags, resultReceiver);
        }
        return ColorInputMethodManagerService.super.hideSoftInput(client, flags, resultReceiver);
    }

    /* access modifiers changed from: package-private */
    public boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        synchronized (this.mMethodMap) {
            if (isLongshotMode()) {
                return false;
            }
            this.isSwitchFormSecureIME = false;
            if (this.useSecure) {
                if (this.mInputShown) {
                    ColorInputMethodManagerService.super.hideCurrentInputLocked(0, (ResultReceiver) null);
                }
                boolean showCurrentInputLocked = this.mSecureInputMethodService.showCurrentInputLocked(flags, resultReceiver);
                return showCurrentInputLocked;
            }
            if (this.mSecureInputMethodService.mInputShown) {
                this.mSecureInputMethodService.hideCurrentInputLocked(0, null);
            }
            boolean showCurrentInputLocked2 = ColorInputMethodManagerService.super.showCurrentInputLocked(flags, resultReceiver);
            return showCurrentInputLocked2;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        synchronized (this.mMethodMap) {
            if (!this.useSecure || this.isSwitchFormSecureIME) {
                boolean hideCurrentInputLocked = ColorInputMethodManagerService.super.hideCurrentInputLocked(flags, resultReceiver);
                return hideCurrentInputLocked;
            }
            boolean hideCurrentInputLocked2 = this.mSecureInputMethodService.hideCurrentInputLocked(flags, resultReceiver);
            return hideCurrentInputLocked2;
        }
    }

    private boolean isLongshotMode() {
        ColorScreenshotManager sm = ColorLongshotUtils.getScreenshotManager(this.mContext);
        if (sm != null) {
            return sm.isLongshotMode();
        }
        return false;
    }

    private void hideCurrentInputMethod() {
        InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        if (inputMethodManagerInternal != null) {
            inputMethodManagerInternal.hideCurrentInputMethod();
        }
    }

    public void onUnlockUser(int userId) {
        ColorInputMethodManagerService.super.onUnlockUser(userId);
        this.mSecureInputMethodService.onUnlockUser(userId);
        this.mSecureInputMethodService.setInputMethodManagerService(this);
    }

    /* access modifiers changed from: package-private */
    public void setInputMethodLocked(String id, int subtypeId) {
        if (!SEC_IMS_SERVICE_NAME.equals(id)) {
            ColorInputMethodManagerService.super.setInputMethodLocked(id, subtypeId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onSwitchUser(int userId) {
        ColorInputMethodManagerService.super.onSwitchUser(userId);
        this.mSecureInputMethodService.onSwitchUser(userId);
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        ColorInputMethodManagerService.super.systemRunning(statusBar);
        this.mSecureInputMethodService.systemRunning(statusBar);
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int selfReportedDisplayId) {
        ColorInputMethodManagerService.super.addClient(client, inputContext, selfReportedDisplayId);
        this.mSecureInputMethodService.addClient(client, inputContext, selfReportedDisplayId);
    }

    public void removeClient(IInputMethodClient client) {
        ColorInputMethodManagerService.super.removeClient(client);
        this.mSecureInputMethodService.removeClient(client);
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        if (isSecurity(attribute)) {
            if (this.mInputShown) {
                ColorInputMethodManagerService.super.hideSoftInput(client, 0, (ResultReceiver) null);
            }
            this.useSecure = true;
            this.mColorInputBindResult = ColorInputMethodManagerService.super.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
            if (!(client == null || this.mCurFocusedWindowClient == ((InputMethodManagerService.ClientState) this.mClients.get(client.asBinder())))) {
                if (DEBUG) {
                    Log.i(this.TAG, "refresh useSecure state after binding, last state is true");
                }
                this.useSecure = isSecurity(this.mCurAttribute);
            }
            return this.mSecureInputMethodService.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
        }
        if (this.mSecureInputMethodService.mInputShown) {
            this.mSecureInputMethodService.hideCurrentInputLockedInternal();
        }
        this.useSecure = false;
        this.mSecureInputMethodService.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
        if (!(client == null || this.mSecureInputMethodService.mCurFocusedWindowClient == ((InputMethodManagerService.ClientState) this.mSecureInputMethodService.mClients.get(client.asBinder())))) {
            if (DEBUG) {
                Log.i(this.TAG, "refresh useSecure state after binding, last state is false");
            }
            this.useSecure = isSecurity(this.mCurAttribute);
        }
        return ColorInputMethodManagerService.super.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
    }

    /* access modifiers changed from: protected */
    public boolean shouldBuildInputMethodList(String methodId) {
        return SEC_IMS_SERVICE_NAME.equals(methodId);
    }

    /* access modifiers changed from: protected */
    public boolean onSetInputMethod(IBinder token, String id) {
        String str = this.TAG;
        Log.d(str, "setInputMethod: " + id);
        if (!SEC_IMS_SERVICE_NAME.equals(id) || this.mColorInputBindResult == null) {
            return false;
        }
        synchronized (this.mMethodMap) {
            this.mSecureInputMethodService.hideCurrentInputLocked(0, null);
            try {
                String str2 = this.TAG;
                Log.d(str2, "SEC IMS switch to : " + this.mColorInputBindResult.id);
                this.mCurClient.client.onBindMethod(this.mColorInputBindResult);
                ColorInputMethodManagerService.super.showCurrentInputLocked(0, (ResultReceiver) null);
                this.isSwitchFormSecureIME = true;
            } catch (RemoteException e) {
                Log.d(this.TAG, "error: resetInputResult");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean onCalledWithValidTokenLocked(IBinder token) {
        ColorSecureInputMethodManagerService colorSecureInputMethodManagerService = this.mSecureInputMethodService;
        return colorSecureInputMethodManagerService != null && token == colorSecureInputMethodManagerService.mCurToken;
    }

    /* access modifiers changed from: protected */
    public boolean onShowMySoftInput(IBinder token, int flags) {
        if (this.useSecure) {
            if (this.mInputShown) {
                ColorInputMethodManagerService.super.hideCurrentInputLocked(0, (ResultReceiver) null);
            }
            this.mSecureInputMethodService.showCurrentInputLocked(flags, null);
            return true;
        }
        if (this.mSecureInputMethodService.mInputShown) {
            this.mSecureInputMethodService.hideCurrentInputLocked(0, null);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onHideMySoftInput(IBinder token, int flags) {
        if (!this.useSecure || this.isSwitchFormSecureIME) {
            return false;
        }
        this.mSecureInputMethodService.hideCurrentInputLocked(flags, null);
        return true;
    }

    private boolean isSecurity(EditorInfo attribute) {
        boolean isSecurity = false;
        this.isSwitchFormSecureIME = false;
        if (attribute == null || attribute.packageName == null) {
            return this.useSecure;
        }
        boolean enable = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), SETTINGS_SECURITY_WINDOW, 1, -2) == 1;
        boolean needShow = needToShowSecurityWindow(attribute);
        boolean inBlackList = this.mMethodMap.get(this.mCurMethodId) != null && ColorSecureKeyboardUtils.getInstance().inBlackList(attribute.packageName, ((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId)).getPackageName());
        if (ColorSecureInputMethodManagerService.isExist && !this.isSwitchFormSecureIME && enable && needShow && !inBlackList) {
            isSecurity = true;
        }
        Log.d(this.TAG, "isSecurity= " + isSecurity + " (  enable=" + enable + " needShow=" + needShow + " inBlackList=" + inBlackList + "  isExist=" + ColorSecureInputMethodManagerService.isExist + " )");
        return isSecurity;
    }

    private static boolean needToShowSecurityWindow(EditorInfo editorInfo) {
        if (editorInfo == null) {
            return false;
        }
        int inputType = editorInfo.inputType;
        if (isPasswordInputType(inputType) || isVisiblePasswordInputType(inputType)) {
            return true;
        }
        return false;
    }

    public static boolean isPasswordInputType(int inputType) {
        int variation = inputType & 4095;
        return variation == 129 || variation == 225 || variation == 18;
    }

    public static boolean isVisiblePasswordInputType(int inputType) {
        return (inputType & 4095) == 145;
    }

    /* access modifiers changed from: protected */
    public IInputContext getCorrectInputContext(IInputContext ic) {
        if (this.useSecure) {
            return null;
        }
        return ic;
    }
}
