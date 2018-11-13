package com.android.server.oppo;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.rutils.RUtils;

public final class OppoRootCheckHelper {
    private static final String CHECK_START_ACTION = "oppo.intent.action.CheckSystemFile";
    private static final int MSG_CANNEL_ROOT_NOTICE_START = 1002;
    private static final int MSG_ROOT_CHECK_START = 1001;
    private static final int NOTIFY_ID = 0;
    private static final String SYSTEM_ROOT_NOTIFY_CHANNEL_ID = "system_root_notify_channel_id";
    private static final String TAG = "OppoCheckHelper";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("oppo.intent.action.SYSTEM_HAVE_BEEN_BROKEN")) {
                OppoRootCheckHelper.this.sendStartCheckMessage();
            } else if (action.equals("oppo.intent.action.ABANDON_SYSTEM_REPAIR")) {
                OppoRootCheckHelper.this.sendCancelRootNoticeMessage();
            }
        }
    };
    private boolean mConnected = false;
    private Context mContext = null;
    private ExHandler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    private boolean mHasNotify = false;
    private PowerManager mPowerManager = null;

    private class ExHandler extends Handler {
        public ExHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    if (RUtils.OppoRUtilsCompareSystemMD5() == -1) {
                        OppoRootCheckHelper.this.notifyRootTips();
                        return;
                    }
                    return;
                case 1002:
                    ((NotificationManager) OppoRootCheckHelper.this.mContext.getSystemService("notification")).cancel(0);
                    return;
                default:
                    return;
            }
        }
    }

    public OppoRootCheckHelper(Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("oppo.intent.action.SYSTEM_HAVE_BEEN_BROKEN");
        intentFilter.addAction("oppo.intent.action.ABANDON_SYSTEM_REPAIR");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mHandlerThread.start();
        this.mHandler = new ExHandler(this.mHandlerThread.getLooper());
    }

    private void sendStartCheckMessage() {
        Message msg = new Message();
        msg.what = 1001;
        this.mHandler.sendMessage(msg);
    }

    private void sendCancelRootNoticeMessage() {
        Message msg = new Message();
        msg.what = 1002;
        this.mHandler.sendMessage(msg);
    }

    private void notifyRootTips() {
        Log.d(TAG, "----------- notifyRootTips");
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
        if (nm != null) {
            if (nm.getNotificationChannel(SYSTEM_ROOT_NOTIFY_CHANNEL_ID) == null) {
                createSystemRootChannel(nm);
            }
            Bitmap btm = BitmapFactory.decodeResource(this.mContext.getResources(), 201852132);
            Intent acIntent = new Intent(CHECK_START_ACTION);
            acIntent.addFlags(67108864);
            PendingIntent pIntent = PendingIntent.getActivity(this.mContext, 0, acIntent, 134217728);
            String title = this.mContext.getResources().getString(201590085);
            String subText = this.mContext.getResources().getString(201590086);
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(subText)) {
                Builder builder = new Builder(this.mContext);
                builder.setContentIntent(pIntent).setSmallIcon(201852131).setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(title).setContentText(subText).setLargeIcon(btm).setTicker(title);
                Intent intent = new Intent("com.oppo.ota.show_root_risk");
                intent.addFlags(1073741824);
                intent.addFlags(DumpState.DUMP_VOLUMES);
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent, 0));
                builder.setChannelId(SYSTEM_ROOT_NOTIFY_CHANNEL_ID);
                Notification notification = builder.build();
                notification.flags = 2;
                nm.notify(0, notification);
                this.mHasNotify = true;
            }
        }
    }

    private void createSystemRootChannel(NotificationManager notificationManager) {
        notificationManager.createNotificationChannel(new NotificationChannel(SYSTEM_ROOT_NOTIFY_CHANNEL_ID, SYSTEM_ROOT_NOTIFY_CHANNEL_ID, 4));
    }
}
