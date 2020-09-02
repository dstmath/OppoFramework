package com.android.server.wm;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import android.view.InputChannel;

class ColorInputConsumer implements IBinder.DeathRecipient {
    private static final String TAG = "ColorInputConsumer";
    final Callback mCallback;
    final InputChannel mClientChannel;
    final String mName;
    final WindowManagerService mService;
    final IBinder mToken;

    interface Callback {
        void onBinderDied(String str);
    }

    public ColorInputConsumer(WindowManagerService service, IBinder token, String name, InputChannel inputChannel, Callback callback) {
        this.mService = service;
        this.mToken = token;
        this.mName = name;
        this.mCallback = callback;
        InputChannel client = this.mService.mInputManager.monitorInput(name, service.getDefaultDisplayContentLocked().getDisplayId());
        if (inputChannel != null) {
            client.transferTo(inputChannel);
            this.mClientChannel = inputChannel;
        } else {
            this.mClientChannel = client;
        }
        linkToDeathRecipient();
    }

    public void disposeChannelsLw() {
        InputChannel inputChannel = this.mClientChannel;
        if (inputChannel != null) {
            inputChannel.dispose();
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
