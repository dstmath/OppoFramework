package com.android.server.fingerprint.tool;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ExHandler extends Handler {
    public ExHandler(Looper l) {
        super(l);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }

    public void sendMessage(int event) {
        sendMessage(obtainMessage(event));
    }

    public void sendMessageWithArg(int event, int arg) {
        sendMessage(obtainMessage(event, arg, 0));
    }

    public void removeMessage(int event) {
        if (hasMessages(event)) {
            removeMessages(event);
        }
    }

    public void sendSyncMessage(int event) {
        removeMessage(event);
        sendMessage(event);
    }

    public void sendMessageDelayed(int event, long delay) {
        sendMessageDelayed(obtainMessage(event), delay);
    }

    public void sendSyncMessageDelayed(int event, long delay) {
        removeMessage(event);
        sendMessageDelayed(event, delay);
    }

    public void sendMessageWithObject(int event, Object object) {
        sendMessage(obtainMessage(event, object));
    }
}
