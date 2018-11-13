package com.color.view.inputmethod;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.view.IInputMethodManager.Stub;

public final class ColorInputMethodManager {
    private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodManager";
    public static final int HIDE_CURRENT_INPUTMETHOD = 10002;
    public static final int OPPO_CALL_TRANSACTION_INDEX = 10000;
    public static final int OPPO_FIRST_CALL_TRANSACTION = 10001;
    private IBinder mRemote;

    public ColorInputMethodManager() {
        this.mRemote = null;
        this.mRemote = ServiceManager.getService("input_method");
    }

    public void hideCurrentInputMethod() {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(10002, data, null, 1);
        } catch (RemoteException e) {
        } finally {
            data.recycle();
        }
    }

    public static void hideSoftInput(Context context) {
        try {
            Stub.asInterface(ServiceManager.getService("input_method")).hideSoftInput(((InputMethodManager) context.getSystemService("input_method")).getClient(), 0, null);
        } catch (RemoteException e) {
        }
    }
}
