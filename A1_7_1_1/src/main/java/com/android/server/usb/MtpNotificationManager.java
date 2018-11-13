package com.android.server.usb;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.os.UserHandle;

class MtpNotificationManager {
    private static final String ACTION_OPEN_IN_APPS = "com.android.server.usb.ACTION_OPEN_IN_APPS";
    private static final int PROTOCOL_MTP = 0;
    private static final int PROTOCOL_PTP = 1;
    private static final int SUBCLASS_MTP = 255;
    private static final int SUBCLASS_STILL_IMAGE_CAPTURE = 1;
    private static final String TAG = "UsbMtpNotificationManager";
    private final Context mContext;
    private final OnOpenInAppListener mListener;

    interface OnOpenInAppListener {
        void onOpenInApp(UsbDevice usbDevice);
    }

    private class Receiver extends BroadcastReceiver {
        /* synthetic */ Receiver(MtpNotificationManager this$0, Receiver receiver) {
            this();
        }

        private Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            UsbDevice device = (UsbDevice) intent.getExtras().getParcelable("device");
            if (device != null && intent.getAction().equals(MtpNotificationManager.ACTION_OPEN_IN_APPS)) {
                MtpNotificationManager.this.mListener.onOpenInApp(device);
            }
        }
    }

    MtpNotificationManager(Context context, OnOpenInAppListener listener) {
        this.mContext = context;
        this.mListener = listener;
        context.registerReceiver(new Receiver(this, null), new IntentFilter(ACTION_OPEN_IN_APPS));
    }

    void showNotification(UsbDevice device) {
        Resources resources = this.mContext.getResources();
        String title = resources.getString(17040893, new Object[]{device.getProductName()});
        Builder builder = new Builder(this.mContext).setContentTitle(title).setContentText(resources.getString(17040894)).setSmallIcon(17303293).setCategory("sys");
        Intent intent = new Intent(ACTION_OPEN_IN_APPS);
        intent.putExtra("device", device);
        intent.addFlags(1342177280);
        builder.setContentIntent(PendingIntent.getBroadcastAsUser(this.mContext, device.getDeviceId(), intent, 134217728, UserHandle.SYSTEM));
        Notification notification = builder.build();
        notification.flags |= 256;
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).notify(TAG, device.getDeviceId(), notification);
    }

    void hideNotification(int deviceId) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).cancel(TAG, deviceId);
    }

    static boolean shouldShowNotification(PackageManager packageManager, UsbDevice device) {
        if (packageManager.hasSystemFeature("android.hardware.type.automotive")) {
            return false;
        }
        return isMtpDevice(device);
    }

    private static boolean isMtpDevice(UsbDevice device) {
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbInterface = device.getInterface(i);
            if (usbInterface.getInterfaceClass() == 6 && usbInterface.getInterfaceSubclass() == 1 && usbInterface.getInterfaceProtocol() == 1) {
                return true;
            }
            if (usbInterface.getInterfaceClass() == 255 && usbInterface.getInterfaceSubclass() == 255 && usbInterface.getInterfaceProtocol() == 0 && "MTP".equals(usbInterface.getName())) {
                return true;
            }
        }
        return false;
    }
}
