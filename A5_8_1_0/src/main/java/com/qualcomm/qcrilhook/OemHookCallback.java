package com.qualcomm.qcrilhook;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.qualcomm.qcrilhook.IOemHookCallback.Stub;
import com.qualcomm.qcrilhook.QmiOemHookConstants.ResponseType;

public class OemHookCallback extends Stub {
    Message mAppMessage;

    public OemHookCallback(Message msg) {
        this.mAppMessage = msg;
    }

    public void onOemHookException(int phoneId) throws RemoteException {
        Log.w("onOemHookException", "mPhoneId: " + phoneId);
    }

    public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
        Log.w("OemHookCallback", "mPhoneId: " + phoneId);
        QmiOemHook.receive(response, this.mAppMessage, ResponseType.IS_ASYNC_RESPONSE, phoneId);
    }
}
