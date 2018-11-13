package com.android.server.connectivity;

import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.widget.Toast;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class NetworkNotificationManager {
    private static final boolean DBG = true;
    private static final String NOTIFICATION_ID = "Connectivity.Notification";
    private static final String TAG = null;
    private static final boolean VDBG = false;
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final TelephonyManager mTelephonyManager;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum NotificationType {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkNotificationManager.NotificationType.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkNotificationManager.NotificationType.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkNotificationManager.NotificationType.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.NetworkNotificationManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.NetworkNotificationManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkNotificationManager.<clinit>():void");
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
            return r.getStringArray(17236073)[transportType];
        } catch (IndexOutOfBoundsException e) {
            return r.getString(17040360);
        }
    }

    private static int getIcon(int transportType) {
        if (transportType == 1) {
            return 17303261;
        }
        return 17303257;
    }

    public void showNotification(int id, NotificationType notifyType, NetworkAgentInfo nai, NetworkAgentInfo switchToNai, PendingIntent intent, boolean highPriority) {
        int transportType;
        String extraInfo;
        CharSequence title;
        CharSequence details;
        Builder builder;
        if (nai != null) {
            transportType = getFirstTransportType(nai);
            extraInfo = nai.networkInfo.getExtraInfo();
            if (!nai.networkCapabilities.hasCapability(12)) {
                return;
            }
        }
        transportType = 0;
        extraInfo = null;
        Slog.d(TAG, "showNotification " + notifyType + " transportType=" + getTransportName(transportType) + " extraInfo=" + extraInfo + " highPriority=" + highPriority);
        Resources r = Resources.getSystem();
        int icon = getIcon(transportType);
        Object[] objArr;
        if (notifyType == NotificationType.NO_INTERNET && transportType == 1) {
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(0);
            title = r.getString(17040355, objArr);
            details = r.getString(17040356);
        } else if (notifyType == NotificationType.LOST_INTERNET && transportType == 1) {
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(0);
            title = r.getString(17040355, objArr);
            details = r.getString(17040356);
        } else if (notifyType == NotificationType.SIGN_IN) {
            switch (transportType) {
                case 0:
                    objArr = new Object[1];
                    objArr[0] = Integer.valueOf(0);
                    title = r.getString(17040353, objArr);
                    details = this.mTelephonyManager.getNetworkOperatorName();
                    break;
                case 1:
                    objArr = new Object[1];
                    objArr[0] = Integer.valueOf(0);
                    title = r.getString(17040352, objArr);
                    objArr = new Object[1];
                    objArr[0] = extraInfo;
                    details = r.getString(17040354, objArr);
                    break;
                default:
                    objArr = new Object[1];
                    objArr[0] = Integer.valueOf(0);
                    title = r.getString(17040353, objArr);
                    objArr = new Object[1];
                    objArr[0] = extraInfo;
                    details = r.getString(17040354, objArr);
                    break;
            }
        } else if (notifyType == NotificationType.NETWORK_SWITCH) {
            String fromTransport = getTransportName(transportType);
            String toTransport = getTransportName(getFirstTransportType(switchToNai));
            objArr = new Object[1];
            objArr[0] = toTransport;
            title = r.getString(17040357, objArr);
            objArr = new Object[2];
            objArr[0] = toTransport;
            objArr[1] = fromTransport;
            details = r.getString(17040358, objArr);
        } else {
            Slog.wtf(TAG, "Unknown notification type " + notifyType + "on network transport " + getTransportName(transportType));
            return;
        }
        Builder localOnly;
        int i;
        if (transportType == 1) {
            localOnly = new Builder(this.mContext).setWhen(System.currentTimeMillis()).setShowWhen(notifyType == NotificationType.NETWORK_SWITCH).setSmallIcon(201852005).setLargeIcon(BitmapFactory.decodeResource(this.mContext.getResources(), 201852113)).setAutoCancel(true).setTicker(title).setColor(this.mContext.getColor(17170523)).setContentTitle(title).setContentIntent(intent).setLocalOnly(true);
            if (highPriority) {
                i = 1;
            } else {
                i = 0;
            }
            localOnly = localOnly.setPriority(i);
            if (highPriority) {
                i = -1;
            } else {
                i = 0;
            }
            builder = localOnly.setDefaults(i).setOnlyAlertOnce(true);
        } else {
            localOnly = new Builder(this.mContext).setWhen(System.currentTimeMillis()).setShowWhen(notifyType == NotificationType.NETWORK_SWITCH).setSmallIcon(icon).setAutoCancel(true).setTicker(title).setColor(this.mContext.getColor(17170523)).setContentTitle(title).setContentIntent(intent).setLocalOnly(true);
            if (highPriority) {
                i = 1;
            } else {
                i = 0;
            }
            localOnly = localOnly.setPriority(i);
            if (highPriority) {
                i = -1;
            } else {
                i = 0;
            }
            builder = localOnly.setDefaults(i).setOnlyAlertOnce(true);
        }
        if (notifyType == NotificationType.NETWORK_SWITCH) {
            builder.setStyle(new BigTextStyle().bigText(details));
        } else {
            builder.setContentText(details);
        }
        try {
            this.mNotificationManager.notifyAsUser(NOTIFICATION_ID, id, builder.build(), UserHandle.ALL);
        } catch (NullPointerException npe) {
            Slog.d(TAG, "setNotificationVisible: visible notificationManager npe=" + npe);
        }
    }

    public void clearNotification(int id) {
        Slog.d(TAG, "clearNotification id=" + id);
        try {
            this.mNotificationManager.cancelAsUser(NOTIFICATION_ID, id, UserHandle.ALL);
        } catch (NullPointerException npe) {
            Slog.d(TAG, "setNotificationVisible: cancel notificationManager npe=" + npe);
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
        Resources resources = this.mContext.getResources();
        Object[] objArr = new Object[2];
        objArr[0] = fromTransport;
        objArr[1] = toTransport;
        Toast.makeText(this.mContext, resources.getString(17040359, objArr), 1).show();
    }
}
