package com.color.view.inputmethod;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.view.IInputMethodManager;

public final class ColorInputMethodManager {
    private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodManager";
    public static final int GET_DEFULT_INPUTMETHOD = 10004;
    public static final int HIDE_CURRENT_INPUTMETHOD = 10002;
    public static final int OPPO_CALL_TRANSACTION_INDEX = 10000;
    public static final int OPPO_FIRST_CALL_TRANSACTION = 10001;
    public static final int RESET_DEFULT_INPUTMETHOD = 10005;
    public static final int SET_DEFULT_INPUTMETHOD = 10003;
    private IBinder mRemote;

    public ColorInputMethodManager() {
        this.mRemote = null;
        this.mRemote = ServiceManager.getService(Context.INPUT_METHOD_SERVICE);
    }

    public void hideCurrentInputMethod() {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(10002, data, null, 1);
        } catch (RemoteException e) {
        } catch (Throwable th) {
            data.recycle();
            throw th;
        }
        data.recycle();
    }

    public static void hideSoftInput(Context context) {
        try {
            IInputMethodManager.Stub.asInterface(ServiceManager.getService(Context.INPUT_METHOD_SERVICE)).hideSoftInput(((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).getClient(), 0, null);
        } catch (RemoteException e) {
        }
    }

    public void setDefaultInputMethod(String methodId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(methodId);
            this.mRemote.transact(10003, data, reply, 1);
            reply.readException();
        } catch (RemoteException e) {
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
    }

    public String getDefaultInputMethod() {
        String result = "";
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(10004, data, reply, 1);
            reply.readException();
            result = reply.readString();
        } catch (RemoteException e) {
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public void clearDefaultInputMethod() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(10005, data, reply, 1);
            reply.readException();
        } catch (RemoteException e) {
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
    }
}
