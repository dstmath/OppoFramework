package com.qualcomm.wfd;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.qualcomm.wfd.WfdEnums.SessionState;
import com.qualcomm.wfd.WfdEnums.WfdEvent;
import com.qualcomm.wfd.service.IWfdActionListener.Stub;

/* compiled from: ExtendedRemoteDisplay */
class WfdActionListenerImpl extends Stub {
    /* renamed from: -com-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues */
    private static final /* synthetic */ int[] f0-com-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues = null;
    private static final String TAG = "ExtendedRemoteDisplay.WfdActionListenerImpl";
    Handler mCallbackHandler;

    /* renamed from: -getcom-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues */
    private static /* synthetic */ int[] m0-getcom-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues() {
        if (f0-com-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues != null) {
            return f0-com-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues;
        }
        int[] iArr = new int[SessionState.values().length];
        try {
            iArr[SessionState.ESTABLISHED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SessionState.IDLE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SessionState.INITIALIZED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SessionState.INVALID.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SessionState.PAUSE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SessionState.PAUSING.ordinal()] = 9;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SessionState.PLAY.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SessionState.PLAYING.ordinal()] = 10;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SessionState.STANDBY.ordinal()] = 11;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SessionState.STANDING_BY.ordinal()] = 7;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SessionState.TEARDOWN.ordinal()] = 8;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SessionState.TEARING_DOWN.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        f0-com-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues = iArr;
        return iArr;
    }

    WfdActionListenerImpl(Handler handler) {
        this.mCallbackHandler = handler;
    }

    public void onStateUpdate(int newState, int sessionId) throws RemoteException {
        SessionState state = SessionState.values()[newState];
        switch (m0-getcom-qualcomm-wfd-WfdEnums$SessionStateSwitchesValues()[state.ordinal()]) {
            case 1:
                Log.d(TAG, state.toString());
                this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.ESTABLISHED_CALLBACK.value()));
                return;
            case 2:
                Log.d(TAG, state.toString());
                return;
            case 3:
                if (sessionId > 0) {
                    Log.d(TAG, state.toString() + " sessionId > 0");
                    this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.TEARDOWN_CALLBACK.value()));
                    return;
                }
                Log.d(TAG, state.toString() + " , Init callback");
                this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.INIT_CALLBACK.value()));
                return;
            case 4:
                Log.d(TAG, state.toString());
                this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.INVALID_STATE_CALLBACK.value()));
                return;
            case 5:
                Log.d(TAG, state.toString());
                this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.PAUSE_CALLBACK.value()));
                return;
            case 6:
                Log.d(TAG, state.toString());
                this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.PLAY_CALLBACK.value()));
                return;
            case 7:
                Log.d(TAG, state.toString());
                this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.STANDBY_CALLBACK.value()));
                return;
            case 8:
                Log.d(TAG, state.toString());
                this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.TEARDOWN_CALLBACK.value()));
                return;
            default:
                return;
        }
    }

    public void notifyEvent(int event, int sessionId) throws RemoteException {
        if (event == WfdEvent.TEARDOWN_START.ordinal()) {
            this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(ERDConstants.TEARDOWN_START_CALLBACK.value()));
        }
    }

    public void notify(Bundle b, int sessionId) throws RemoteException {
        if (b != null) {
            Log.d(TAG, "Notify from WFDService");
            if ("MMStreamStarted".equalsIgnoreCase(b.getString("event"))) {
                Message messageEvent = this.mCallbackHandler.obtainMessage(ERDConstants.MM_STREAM_STARTED_CALLBACK.value());
                messageEvent.setData(b);
                this.mCallbackHandler.sendMessage(messageEvent);
            }
        }
    }
}
