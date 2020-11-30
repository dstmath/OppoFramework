package com.mediatek.hdmilocalservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.SystemService;
import com.mediatek.omadm.PalConstDefs;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HdmiLocalService extends SystemService {
    private static final boolean HDMI_TB_SUPPORT = (!PalConstDefs.EMPTY_STRING.equals(SystemProperties.get("ro.vendor.mtk_tb_hdmi")));
    private final String TAG = "HdmiLocalService";
    private Context mContext;
    private HdmiObserver mHdmiObserver;

    public HdmiLocalService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        Slog.d("HdmiLocalService", "Start HdmiLocalService");
    }

    public void onBootPhase(int phase) {
        if (phase == 1000) {
            Slog.d("HdmiLocalService", "Do something in this phase(1000)");
            if (HDMI_TB_SUPPORT && this.mHdmiObserver == null) {
                this.mHdmiObserver = new HdmiObserver(this.mContext);
                this.mHdmiObserver.startObserve();
            }
        }
    }

    private class HdmiObserver extends UEventObserver {
        private static final String HDMI_NAME_PATH = "/sys/class/switch/hdmi/name";
        private static final String HDMI_NOTIFICATION_CHANNEL_ID = "hdmi_notification_channel";
        private static final String HDMI_NOTIFICATION_NAME = "HDMI";
        private static final String HDMI_STATE_PATH = "/sys/class/switch/hdmi/state";
        private static final String HDMI_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/hdmi";
        private static final int MSG_HDMI_PLUG_IN = 10;
        private static final int MSG_HDMI_PLUG_OUT = 11;
        private static final String TAG = "HdmiLocalService.HdmiObserver";
        private final Context mCxt;
        private String mHdmiName;
        private int mHdmiState;
        private int mPrevHdmiState;
        private final PowerManager.WakeLock mWakeLock;

        public HdmiObserver(Context context) {
            this.mCxt = context;
            this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(26, "HdmiObserver");
            this.mWakeLock.setReferenceCounted(false);
            init();
        }

        public void startObserve() {
            startObserving(HDMI_UEVENT_MATCH);
        }

        public void stopObserve() {
            stopObserving();
        }

        public void onUEvent(UEventObserver.UEvent event) {
            Slog.d(TAG, "HdmiObserver: onUEvent: " + event.toString());
            String name = event.get("SWITCH_NAME");
            int state = 0;
            try {
                state = Integer.parseInt(event.get("SWITCH_STATE"));
            } catch (NumberFormatException e) {
                Slog.w(TAG, "HdmiObserver: Could not parse switch state from event " + event);
            }
            Slog.d(TAG, "HdmiObserver.onUEvent(), name=" + name + ", state=" + state);
            update(name, state);
        }

        private void init() {
            String str = this.mHdmiName;
            int i = this.mHdmiState;
            this.mPrevHdmiState = this.mHdmiState;
            try {
                update(getContentFromFile(HDMI_NAME_PATH), Integer.parseInt(getContentFromFile(HDMI_STATE_PATH)));
            } catch (NumberFormatException e) {
                Slog.w(TAG, "HDMI state fail");
            }
        }

        private String getContentFromFile(String filePath) {
            StringBuilder sb;
            char[] buffer = new char[1024];
            FileReader reader = null;
            String content = null;
            try {
                reader = new FileReader(filePath);
                content = String.valueOf(buffer, 0, reader.read(buffer, 0, buffer.length)).trim();
                Slog.d(TAG, filePath + " content is " + content);
                try {
                    reader.close();
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            } catch (FileNotFoundException e2) {
                Slog.w(TAG, "can't find file " + filePath);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                }
            } catch (IOException e4) {
                Slog.w(TAG, "IO exception when read file " + filePath);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                }
            } catch (IndexOutOfBoundsException e6) {
                Slog.w(TAG, "index exception: " + e6.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e7) {
                        e = e7;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e8) {
                        Slog.w(TAG, "close reader fail: " + e8.getMessage());
                    }
                }
                throw th;
            }
            return content;
            sb.append("close reader fail: ");
            sb.append(e.getMessage());
            Slog.w(TAG, sb.toString());
            return content;
        }

        private void update(String newName, int newState) {
            Slog.d(TAG, "HDMIOberver.update(), oldState=" + this.mHdmiState + ", newState=" + newState);
            int i = this.mHdmiState;
            int i2 = newState | i;
            this.mHdmiName = newName;
            this.mPrevHdmiState = i;
            this.mHdmiState = newState;
            if (this.mHdmiState == 0) {
                this.mWakeLock.release();
                handleNotification(false);
                Slog.d(TAG, "HDMIOberver.update(), release");
                return;
            }
            this.mWakeLock.acquire();
            handleNotification(true);
            Slog.d(TAG, "HDMIOberver.update(), acquire");
        }

        private void handleNotification(boolean showNoti) {
            NotificationManager notificationManager = (NotificationManager) this.mCxt.getSystemService("notification");
            if (notificationManager == null) {
                Slog.w(TAG, "Fail to get NotificationManager");
            } else if (showNoti) {
                Slog.d(TAG, "Show notification now");
                notificationManager.createNotificationChannel(new NotificationChannel(HDMI_NOTIFICATION_CHANNEL_ID, HDMI_NOTIFICATION_NAME, 2));
                Notification notification = new Notification.Builder(this.mCxt, HDMI_NOTIFICATION_CHANNEL_ID).build();
                String titleStr = this.mCxt.getResources().getString(134545619);
                String contentStr = this.mCxt.getResources().getString(134545618);
                notification.icon = 134348898;
                notification.tickerText = titleStr;
                notification.flags = 35;
                notification.setLatestEventInfo(this.mCxt, titleStr, contentStr, PendingIntent.getActivityAsUser(this.mCxt, 0, Intent.makeRestartActivityTask(new ComponentName("com.android.settings", "com.android.settings.HdmiSettings")), 0, null, UserHandle.CURRENT));
                notificationManager.notifyAsUser(null, 134348898, notification, UserHandle.CURRENT);
            } else {
                Slog.d(TAG, "Clear notification now");
                notificationManager.cancelAsUser(null, 134348898, UserHandle.CURRENT);
            }
        }
    }
}
