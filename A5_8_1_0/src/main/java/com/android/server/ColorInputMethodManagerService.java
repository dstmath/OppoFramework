package com.android.server;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.view.IInputMethodClient;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorScreenshotManager;

public class ColorInputMethodManagerService extends InputMethodManagerService {
    private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodManager";

    public ColorInputMethodManagerService(Context context) {
        super(context);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 10002:
                data.enforceInterface(DESCRIPTOR);
                hideCurrentInputMethod();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (isLongshotMode()) {
            return hideCurrentInputLocked(flags, resultReceiver);
        }
        return super.hideSoftInput(client, flags, resultReceiver);
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        if (isLongshotMode()) {
            return false;
        }
        return super.showCurrentInputLocked(flags, resultReceiver);
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
}
