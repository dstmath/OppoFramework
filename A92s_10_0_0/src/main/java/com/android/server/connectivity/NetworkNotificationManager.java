package com.android.server.connectivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.net.wifi.WifiInfo;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.OppoConnectivityServiceHelper;

public class NetworkNotificationManager {
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
        LOGGED_IN(744),
        PARTIAL_CONNECTIVITY(745),
        SIGN_IN(740);
        
        public final int eventId;

        private NotificationType(int eventId2) {
            this.eventId = eventId2;
            Holder.sIdToTypeMap.put(eventId2, this);
        }

        private static class Holder {
            /* access modifiers changed from: private */
            public static SparseArray<NotificationType> sIdToTypeMap = new SparseArray<>();

            private Holder() {
            }
        }

        public static NotificationType getFromId(int id) {
            return (NotificationType) Holder.sIdToTypeMap.get(id);
        }
    }

    public NetworkNotificationManager(Context c, TelephonyManager t, NotificationManager n) {
        this.mContext = c;
        this.mTelephonyManager = t;
        this.mNotificationManager = n;
    }

    private static int getFirstTransportType(NetworkAgentInfo nai) {
        if (nai == null) {
            return -1;
        }
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
            return r.getStringArray(17236104)[transportType];
        } catch (IndexOutOfBoundsException e) {
            return r.getString(17040465);
        }
    }

    private static int getIcon(int transportType, NotificationType notifyType) {
        if (transportType != 1 && transportType != 8) {
            return 17303515;
        }
        if (notifyType == NotificationType.LOGGED_IN) {
            return 17302850;
        }
        return 17303519;
    }

    public void showNotification(int id, NotificationType notifyType, NetworkAgentInfo nai, NetworkAgentInfo switchToNai, PendingIntent intent, boolean highPriority) {
        String name;
        int transportType;
        int subId;
        CharSequence details;
        CharSequence title;
        String channelId;
        String tag = tagFor(id);
        int eventId = notifyType.eventId;
        if (nai != null) {
            int transportType2 = getFirstTransportType(nai);
            String extraInfo = nai.networkInfo.getExtraInfo();
            name = TextUtils.isEmpty(extraInfo) ? nai.networkCapabilities.getSSID() : extraInfo;
            if (nai.networkCapabilities.hasCapability(12)) {
                transportType = transportType2;
            } else {
                return;
            }
        } else {
            name = null;
            transportType = 0;
        }
        NotificationType previousNotifyType = NotificationType.getFromId(this.mNotificationTypeMap.get(id));
        if (priority(previousNotifyType) > priority(notifyType)) {
            Slog.d(TAG, String.format("ignoring notification %s for network %s with existing notification %s", notifyType, Integer.valueOf(id), previousNotifyType));
            return;
        }
        clearNotification(id);
        Slog.d(TAG, String.format("showNotification tag=%s event=%s transport=%s name=%s highPriority=%s", tag, nameOf(eventId), getTransportName(transportType), name, Boolean.valueOf(highPriority)));
        Resources r = Resources.getSystem();
        int icon = getIcon(transportType, notifyType);
        if (notifyType == NotificationType.NO_INTERNET && (transportType == 1 || transportType == 8)) {
            CharSequence title2 = r.getString(17041285, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            details = r.getString(17041286);
            subId = 0;
            title = title2;
        } else if (notifyType == NotificationType.PARTIAL_CONNECTIVITY && transportType == 1) {
            CharSequence title3 = r.getString(17040460, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            details = r.getString(17040461);
            subId = 0;
            title = title3;
        } else if (notifyType == NotificationType.LOST_INTERNET && (transportType == 1 || transportType == 8)) {
            CharSequence title4 = r.getString(17041285, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            details = r.getString(17041286);
            subId = 0;
            title = title4;
        } else if (notifyType == NotificationType.SIGN_IN) {
            if (transportType == 0) {
                CharSequence title5 = r.getString(17040456, 0);
                NetworkSpecifier specifier = nai.networkCapabilities.getNetworkSpecifier();
                int subId2 = Integer.MAX_VALUE;
                if (specifier instanceof StringNetworkSpecifier) {
                    try {
                        subId2 = Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
                    } catch (NumberFormatException e) {
                        Slog.e(TAG, "NumberFormatException on " + ((StringNetworkSpecifier) specifier).specifier);
                    }
                }
                title = title5;
                subId = 0;
                details = this.mTelephonyManager.createForSubscriptionId(subId2).getNetworkOperatorName();
            } else if (transportType == 1 || transportType == 8) {
                CharSequence title6 = r.getString(17041275, 0);
                details = r.getString(17040457, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
                subId = 0;
                title = title6;
            } else {
                CharSequence title7 = r.getString(17040456, 0);
                details = r.getString(17040457, name);
                subId = 0;
                title = title7;
            }
        } else if (notifyType == NotificationType.LOGGED_IN) {
            CharSequence title8 = WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID());
            details = r.getString(17039641);
            title = title8;
            subId = 0;
        } else if (notifyType == NotificationType.NETWORK_SWITCH) {
            String fromTransport = getTransportName(transportType);
            String toTransport = getTransportName(getFirstTransportType(switchToNai));
            subId = 0;
            title = r.getString(17040462, toTransport);
            details = r.getString(17040463, toTransport, fromTransport);
        } else if (notifyType != NotificationType.NO_INTERNET && notifyType != NotificationType.PARTIAL_CONNECTIVITY) {
            Slog.wtf(TAG, "Unknown notification type " + notifyType + " on network transport " + getTransportName(transportType));
            return;
        } else {
            return;
        }
        int hasPreviousNotification = previousNotifyType != null ? 1 : subId;
        if (!highPriority || hasPreviousNotification != 0) {
            channelId = SystemNotificationChannels.NETWORK_STATUS;
        } else {
            channelId = SystemNotificationChannels.NETWORK_ALERTS;
        }
        Notification.Builder builder = new Notification.Builder(this.mContext, channelId).setWhen(System.currentTimeMillis()).setShowWhen(notifyType == NotificationType.NETWORK_SWITCH).setSmallIcon(icon).setAutoCancel(true).setTicker(title).setColor(this.mContext.getColor(17170460)).setContentTitle(title).setContentIntent(intent).setLocalOnly(true).setOnlyAlertOnce(true);
        if (notifyType == NotificationType.NETWORK_SWITCH) {
            builder.setStyle(new Notification.BigTextStyle().bigText(details));
        } else {
            builder.setContentText(details);
        }
        if (notifyType == NotificationType.SIGN_IN) {
            builder.extend(new Notification.TvExtender().setChannelId(channelId));
        }
        Notification notification = builder.build();
        this.mNotificationTypeMap.put(id, eventId);
        try {
            this.mNotificationManager.notifyAsUser(tag, eventId, notification, UserHandle.ALL);
        } catch (NullPointerException npe) {
            Slog.d(TAG, "setNotificationVisible: visible notificationManager error", npe);
        }
    }

    public void clearNotification(int id, NotificationType notifyType) {
        if (notifyType == NotificationType.getFromId(this.mNotificationTypeMap.get(id))) {
            clearNotification(id);
        }
    }

    public void clearNotification(int id) {
        if (this.mNotificationTypeMap.indexOfKey(id) >= 0) {
            String tag = tagFor(id);
            int eventId = this.mNotificationTypeMap.get(id);
            Slog.d(TAG, String.format("clearing notification tag=%s event=%s", tag, nameOf(eventId)));
            try {
                this.mNotificationManager.cancelAsUser(tag, eventId, UserHandle.ALL);
            } catch (NullPointerException npe) {
                Slog.d(TAG, String.format("failed to clear notification tag=%s event=%s", tag, nameOf(eventId)), npe);
            }
            this.mNotificationTypeMap.delete(id);
        }
    }

    public void setProvNotificationVisible(boolean visible, int id, String action) {
        if (visible) {
            showNotification(id, NotificationType.SIGN_IN, null, null, PendingIntent.getBroadcast(this.mContext, 0, new Intent(action), 0), false);
            return;
        }
        clearNotification(id);
    }

    public void showToast(NetworkAgentInfo fromNai, NetworkAgentInfo toNai) {
        String fromTransport = getTransportName(getFirstTransportType(fromNai));
        String toTransport = getTransportName(getFirstTransportType(toNai));
        Toast.makeText(this.mContext, this.mContext.getResources().getString(17040464, fromTransport, toTransport), 1).show();
    }

    @VisibleForTesting
    static String tagFor(int id) {
        return String.format("ConnectivityNotification:%d", Integer.valueOf(id));
    }

    @VisibleForTesting
    static String nameOf(int eventId) {
        NotificationType t = NotificationType.getFromId(eventId);
        return t != null ? t.name() : "UNKNOWN";
    }

    private static int priority(NotificationType t) {
        if (t == null) {
            return 0;
        }
        switch (t) {
            case SIGN_IN:
                return 5;
            case PARTIAL_CONNECTIVITY:
                return 4;
            case NO_INTERNET:
                return 3;
            case NETWORK_SWITCH:
                return 2;
            case LOST_INTERNET:
            case LOGGED_IN:
                return 1;
            default:
                return 0;
        }
    }

    /* JADX INFO: Multiple debug info for r0v38 java.lang.CharSequence: [D('details' java.lang.CharSequence), D('fromTransport' java.lang.String)] */
    public void showOppoNotification(int id, NotificationType notifyType, NetworkAgentInfo nai, NetworkAgentInfo switchToNai, PendingIntent intent, boolean highPriority) {
        String name;
        int transportType;
        boolean z;
        CharSequence title;
        String name2;
        String channelId;
        int subId;
        String tag = tagFor(id);
        int eventId = notifyType.eventId;
        if (nai != null) {
            int transportType2 = getFirstTransportType(nai);
            String extraInfo = nai.networkInfo.getExtraInfo();
            name = TextUtils.isEmpty(extraInfo) ? nai.networkCapabilities.getSSID() : extraInfo;
            if (nai.networkCapabilities.hasCapability(12)) {
                transportType = transportType2;
            } else {
                return;
            }
        } else {
            name = null;
            transportType = 0;
        }
        NotificationType previousNotifyType = NotificationType.getFromId(this.mNotificationTypeMap.get(id));
        if (priority(previousNotifyType) > priority(notifyType)) {
            Slog.d(TAG, String.format("ignoring notification %s for network %s with existing notification %s", notifyType, Integer.valueOf(id), previousNotifyType));
            return;
        }
        clearNotification(id);
        Slog.d(TAG, String.format("showNotification tag=%s event=%s transport=%s name=%s highPriority=%s", tag, nameOf(eventId), getTransportName(transportType), name, Boolean.valueOf(highPriority)));
        Resources r = Resources.getSystem();
        int icon = getIcon(transportType, notifyType);
        if (notifyType == NotificationType.NO_INTERNET && (transportType == 1 || transportType == 8)) {
            CharSequence title2 = r.getString(17041285, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            name2 = r.getString(17041286);
            z = false;
            title = title2;
        } else if (notifyType == NotificationType.PARTIAL_CONNECTIVITY && transportType == 1) {
            CharSequence title3 = r.getString(17040460, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            name2 = r.getString(17040461);
            z = false;
            title = title3;
        } else if (notifyType == NotificationType.LOST_INTERNET && (transportType == 1 || transportType == 8)) {
            CharSequence title4 = r.getString(17041285, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            name2 = r.getString(17041286);
            z = false;
            title = title4;
        } else if (notifyType == NotificationType.SIGN_IN) {
            if (transportType == 0) {
                title = r.getString(17040456, 0);
                NetworkSpecifier specifier = nai.networkCapabilities.getNetworkSpecifier();
                if (specifier instanceof StringNetworkSpecifier) {
                    try {
                        subId = Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
                    } catch (NumberFormatException e) {
                        Slog.e(TAG, "NumberFormatException on " + ((StringNetworkSpecifier) specifier).specifier);
                    }
                } else {
                    subId = Integer.MAX_VALUE;
                }
                name2 = this.mTelephonyManager.createForSubscriptionId(subId).getNetworkOperatorName();
                z = false;
            } else if (transportType != 1 && transportType != 8) {
                CharSequence title5 = r.getString(17040456, 0);
                name2 = r.getString(17040457, name);
                z = false;
                title = title5;
            } else if (OppoConnectivityServiceHelper.isDualWifiSta2Network(nai)) {
                CharSequence title6 = r.getString(201653630);
                name2 = r.getString(201653631, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
                z = false;
                title = title6;
            } else {
                CharSequence title7 = r.getString(17041275, 0);
                name2 = r.getString(17040457, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
                z = false;
                title = title7;
            }
        } else if (notifyType == NotificationType.LOGGED_IN) {
            CharSequence title8 = WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID());
            name2 = r.getString(17039641);
            z = false;
            title = title8;
        } else if (notifyType == NotificationType.NETWORK_SWITCH) {
            String fromTransport = getTransportName(transportType);
            String toTransport = getTransportName(getFirstTransportType(switchToNai));
            z = false;
            title = r.getString(17040462, toTransport);
            name2 = r.getString(17040463, toTransport, fromTransport);
        } else {
            Slog.wtf(TAG, "Unknown notification type " + notifyType + " on network transport " + getTransportName(transportType));
            return;
        }
        if (highPriority) {
            channelId = SystemNotificationChannels.NETWORK_ALERTS;
        } else {
            channelId = SystemNotificationChannels.NETWORK_STATUS;
        }
        Notification.Builder builder = new Notification.Builder(this.mContext, channelId).setWhen(System.currentTimeMillis()).setShowWhen(notifyType == NotificationType.NETWORK_SWITCH ? true : z).setSmallIcon(icon).setAutoCancel(true).setTicker(title).setColor(this.mContext.getColor(17170460)).setContentTitle(title).setContentIntent(intent).setLocalOnly(true).setOnlyAlertOnce(true);
        if (notifyType == NotificationType.NETWORK_SWITCH) {
            builder.setStyle(new Notification.BigTextStyle().bigText(name2));
        } else {
            builder.setContentText(name2);
        }
        if (notifyType == NotificationType.SIGN_IN) {
            builder.extend(new Notification.TvExtender().setChannelId(channelId));
        }
        if (OppoConnectivityServiceHelper.isDualWifiSta2Network(nai)) {
            builder.setStyle(new Notification.BigTextStyle().bigText(name2));
        }
        Notification notification = builder.build();
        notification.flags |= 32;
        this.mNotificationTypeMap.put(id, eventId);
        try {
            this.mNotificationManager.notifyAsUser(tag, eventId, notification, UserHandle.ALL);
        } catch (NullPointerException npe) {
            Slog.d(TAG, "setNotificationVisible: visible notificationManager error", npe);
        }
    }
}
