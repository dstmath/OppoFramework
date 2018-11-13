package com.android.server.connectivity;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.Notification.TvExtender;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import com.android.internal.notification.SystemNotificationChannels;

public class NetworkNotificationManager {
    /* renamed from: -com-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues */
    private static final /* synthetic */ int[] f66x6e7dafb6 = null;
    private static final boolean DBG = true;
    private static final String TAG = NetworkNotificationManager.class.getSimpleName();
    private static final boolean VDBG = false;
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final SparseIntArray mNotificationTypeMap = new SparseIntArray();
    private final TelephonyManager mTelephonyManager;

    public enum NotificationType {
        LOST_INTERNET(742),
        NETWORK_SWITCH(743),
        NO_INTERNET(741),
        SIGN_IN(740);
        
        public final int eventId;

        private static class Holder {
            private static SparseArray<NotificationType> sIdToTypeMap;

            private Holder() {
            }

            static {
                sIdToTypeMap = new SparseArray();
            }
        }

        private NotificationType(int eventId) {
            this.eventId = eventId;
            Holder.sIdToTypeMap.put(eventId, this);
        }

        public static NotificationType getFromId(int id) {
            return (NotificationType) Holder.sIdToTypeMap.get(id);
        }
    }

    /* renamed from: -getcom-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues */
    private static /* synthetic */ int[] m100x28b55b5a() {
        if (f66x6e7dafb6 != null) {
            return f66x6e7dafb6;
        }
        int[] iArr = new int[NotificationType.values().length];
        try {
            iArr[NotificationType.LOST_INTERNET.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[NotificationType.NETWORK_SWITCH.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[NotificationType.NO_INTERNET.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[NotificationType.SIGN_IN.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f66x6e7dafb6 = iArr;
        return iArr;
    }

    public NetworkNotificationManager(Context c, TelephonyManager t, NotificationManager n) {
        this.mContext = c;
        this.mTelephonyManager = t;
        this.mNotificationManager = n;
    }

    private static int getFirstTransportType(NetworkAgentInfo nai) {
        for (int i = 0; i < 64; i++) {
            if (nai.networkCapabilities.hasTransport(i)) {
                return i;
            }
        }
        return -1;
    }

    private static String getTransportName(int transportType) {
        Resources r = Resources.getSystem();
        try {
            return r.getStringArray(17236061)[transportType];
        } catch (IndexOutOfBoundsException e) {
            return r.getString(17040351);
        }
    }

    private static int getIcon(int transportType) {
        if (transportType == 1) {
            return 17303428;
        }
        return 17303424;
    }

    public void showNotification(int id, NotificationType notifyType, NetworkAgentInfo nai, NetworkAgentInfo switchToNai, PendingIntent intent, boolean highPriority) {
        int transportType;
        String extraInfo;
        String tag = tagFor(id);
        int eventId = notifyType.eventId;
        if (nai != null) {
            transportType = getFirstTransportType(nai);
            extraInfo = nai.networkInfo.getExtraInfo();
            if (!nai.networkCapabilities.hasCapability(12)) {
                return;
            }
        }
        transportType = 0;
        extraInfo = null;
        if (priority(NotificationType.getFromId(this.mNotificationTypeMap.get(id))) > priority(notifyType)) {
            Slog.d(TAG, String.format("ignoring notification %s for network %s with existing notification %s", new Object[]{notifyType, Integer.valueOf(id), NotificationType.getFromId(this.mNotificationTypeMap.get(id))}));
            return;
        }
        CharSequence title;
        CharSequence details;
        String channelId;
        clearNotification(id);
        Slog.d(TAG, String.format("showNotification tag=%s event=%s transport=%s extraInfo=%s highPrioriy=%s", new Object[]{tag, nameOf(eventId), getTransportName(transportType), extraInfo, Boolean.valueOf(highPriority)}));
        Resources r = Resources.getSystem();
        int icon = getIcon(transportType);
        if (notifyType == NotificationType.NO_INTERNET && transportType == 1) {
            title = r.getString(17041118, new Object[]{Integer.valueOf(0)});
            details = r.getString(17041119);
        } else if (notifyType == NotificationType.LOST_INTERNET && transportType == 1) {
            title = r.getString(17041118, new Object[]{Integer.valueOf(0)});
            details = r.getString(17041119);
        } else if (notifyType == NotificationType.SIGN_IN) {
            switch (transportType) {
                case 0:
                    title = r.getString(17040344, new Object[]{Integer.valueOf(0)});
                    details = this.mTelephonyManager.getNetworkOperatorName();
                    break;
                case 1:
                    title = r.getString(17041106, new Object[]{Integer.valueOf(0)});
                    details = r.getString(17040345, new Object[]{extraInfo});
                    break;
                default:
                    title = r.getString(17040344, new Object[]{Integer.valueOf(0)});
                    details = r.getString(17040345, new Object[]{extraInfo});
                    break;
            }
        } else if (notifyType == NotificationType.NETWORK_SWITCH) {
            String fromTransport = getTransportName(transportType);
            String toTransport = getTransportName(getFirstTransportType(switchToNai));
            title = r.getString(17040348, new Object[]{toTransport});
            details = r.getString(17040349, new Object[]{toTransport, fromTransport});
        } else {
            Slog.wtf(TAG, "Unknown notification type " + notifyType + " on network transport " + getTransportName(transportType));
            return;
        }
        if (highPriority) {
            channelId = SystemNotificationChannels.NETWORK_ALERTS;
        } else {
            channelId = SystemNotificationChannels.NETWORK_STATUS;
        }
        Builder builder = new Builder(this.mContext, channelId).setWhen(System.currentTimeMillis()).setShowWhen(notifyType == NotificationType.NETWORK_SWITCH).setSmallIcon(icon).setAutoCancel(true).setTicker(title).setColor(this.mContext.getColor(17170763)).setContentTitle(title).setContentIntent(intent).setLocalOnly(true).setOnlyAlertOnce(true);
        if (notifyType == NotificationType.NETWORK_SWITCH) {
            builder.setStyle(new BigTextStyle().bigText(details));
        } else {
            builder.setContentText(details);
        }
        if (notifyType == NotificationType.SIGN_IN) {
            builder.extend(new TvExtender().setChannelId(channelId));
        }
        Notification notification = builder.build();
        this.mNotificationTypeMap.put(id, eventId);
        try {
            this.mNotificationManager.notifyAsUser(tag, eventId, notification, UserHandle.ALL);
        } catch (NullPointerException npe) {
            Slog.d(TAG, "setNotificationVisible: visible notificationManager error", npe);
        }
    }

    public void clearNotification(int id) {
        if (this.mNotificationTypeMap.indexOfKey(id) >= 0) {
            String tag = tagFor(id);
            int eventId = this.mNotificationTypeMap.get(id);
            Slog.d(TAG, String.format("clearing notification tag=%s event=%s", new Object[]{tag, nameOf(eventId)}));
            try {
                this.mNotificationManager.cancelAsUser(tag, eventId, UserHandle.ALL);
            } catch (NullPointerException npe) {
                Slog.d(TAG, String.format("failed to clear notification tag=%s event=%s", new Object[]{tag, nameOf(eventId)}), npe);
            }
            this.mNotificationTypeMap.delete(id);
        }
    }

    public void setProvNotificationVisible(boolean visible, int id, String action) {
        if (visible) {
            int i = id;
            showNotification(i, NotificationType.SIGN_IN, null, null, PendingIntent.getBroadcast(this.mContext, 0, new Intent(action), 0), false);
            return;
        }
        clearNotification(id);
    }

    public void showToast(NetworkAgentInfo fromNai, NetworkAgentInfo toNai) {
        String fromTransport = getTransportName(getFirstTransportType(fromNai));
        String toTransport = getTransportName(getFirstTransportType(toNai));
        Toast.makeText(this.mContext, this.mContext.getResources().getString(17040350, new Object[]{fromTransport, toTransport}), 1).show();
    }

    static String tagFor(int id) {
        return String.format("ConnectivityNotification:%d", new Object[]{Integer.valueOf(id)});
    }

    static String nameOf(int eventId) {
        NotificationType t = NotificationType.getFromId(eventId);
        return t != null ? t.name() : "UNKNOWN";
    }

    private static int priority(NotificationType t) {
        if (t == null) {
            return 0;
        }
        switch (m100x28b55b5a()[t.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                return 0;
        }
    }
}
