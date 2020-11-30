package com.android.server.wm;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import android.view.InputChannel;
import android.view.InputMonitor;

/* access modifiers changed from: package-private */
public class ColorInputConsumer implements IBinder.DeathRecipient {
    private static final String TAG = "ColorInputConsumer";
    final Callback mCallback;
    final InputMonitor mInputMonitor;
    final String mName;
    final WindowManagerService mService;
    final IBinder mToken;

    /* access modifiers changed from: package-private */
    public interface Callback {
        void onBinderDied(String str);
    }

    public ColorInputConsumer(WindowManagerService service, IBinder token, String name, InputChannel inputChannel, Callback callback) {
        this.mService = service;
        this.mToken = token;
        this.mName = name;
        this.mCallback = callback;
        this.mInputMonitor = this.mService.mInputManager.monitorGestureInput(name, service.getDefaultDisplayContentLocked().getDisplayId());
        InputChannel client = this.mInputMonitor.getInputChannel();
        if (inputChannel != null) {
            client.transferTo(inputChannel);
        } else {
            Slog.e(TAG, "Has no input channel in InputMonitor, init ColorInputConsumer failed");
        }
        linkToDeathRecipient();
    }

    public void pilferPointers() {
        InputMonitor inputMonitor = this.mInputMonitor;
        if (inputMonitor != null) {
            inputMonitor.pilferPointers();
        } else {
            Slog.e(TAG, "Has no input monitor, pilferPointers failed");
        }
    }

    public void dispose() {
        InputMonitor inputMonitor = this.mInputMonitor;
        if (inputMonitor != null) {
            inputMonitor.dispose();
        }
        unlinkFromDeathRecipient();
    }

    public void binderDied() {
        Slog.i(TAG, "binderDied mName = " + this.mName);
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onBinderDied(this.mName);
        }
    }

    private void linkToDeathRecipient() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                iBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }
    }

    private void unlinkFromDeathRecipient() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            iBinder.unlinkToDeath(this, 0);
        }
    }
}
