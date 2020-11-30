package com.android.server.am;

import android.app.ActivityOptions;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.TimeUtils;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.wm.SafeActivityOptions;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Objects;

public final class PendingIntentRecord extends IIntentSender.Stub {
    public static final int FLAG_ACTIVITY_SENDER = 1;
    public static final int FLAG_BROADCAST_SENDER = 2;
    public static final int FLAG_SERVICE_SENDER = 4;
    private static final String TAG = "ActivityManager";
    boolean canceled = false;
    final PendingIntentController controller;
    final Key key;
    String lastTag;
    String lastTagPrefix;
    private ArraySet<IBinder> mAllowBgActivityStartsForActivitySender = new ArraySet<>();
    private ArraySet<IBinder> mAllowBgActivityStartsForBroadcastSender = new ArraySet<>();
    private ArraySet<IBinder> mAllowBgActivityStartsForServiceSender = new ArraySet<>();
    private RemoteCallbackList<IResultReceiver> mCancelCallbacks;
    public final WeakReference<PendingIntentRecord> ref;
    boolean sent = false;
    String stringName;
    final int uid;
    private ArrayMap<IBinder, Long> whitelistDuration;

    /* access modifiers changed from: package-private */
    public static final class Key {
        private static final int ODD_PRIME_NUMBER = 37;
        final IBinder activity;
        Intent[] allIntents;
        String[] allResolvedTypes;
        final int flags;
        final int hashCode;
        final SafeActivityOptions options;
        final String packageName;
        final int requestCode;
        final Intent requestIntent;
        final String requestResolvedType;
        final int type;
        final int userId;
        final String who;

        Key(int _t, String _p, IBinder _a, String _w, int _r, Intent[] _i, String[] _it, int _f, SafeActivityOptions _o, int _userId) {
            this.type = _t;
            this.packageName = _p;
            this.activity = _a;
            this.who = _w;
            this.requestCode = _r;
            String str = null;
            this.requestIntent = _i != null ? _i[_i.length - 1] : null;
            this.requestResolvedType = _it != null ? _it[_it.length - 1] : str;
            this.allIntents = _i;
            this.allResolvedTypes = _it;
            this.flags = _f;
            this.options = _o;
            this.userId = _userId;
            int hash = (((((23 * 37) + _f) * 37) + _r) * 37) + _userId;
            hash = _w != null ? (hash * 37) + _w.hashCode() : hash;
            hash = _a != null ? (hash * 37) + _a.hashCode() : hash;
            Intent intent = this.requestIntent;
            hash = intent != null ? (hash * 37) + intent.filterHashCode() : hash;
            String str2 = this.requestResolvedType;
            this.hashCode = ((((str2 != null ? (hash * 37) + str2.hashCode() : hash) * 37) + (_p != null ? _p.hashCode() : 0)) * 37) + _t;
        }

        public boolean equals(Object otherObj) {
            if (otherObj == null) {
                return false;
            }
            try {
                Key other = (Key) otherObj;
                if (this.type != other.type || this.userId != other.userId || !Objects.equals(this.packageName, other.packageName) || this.activity != other.activity || !Objects.equals(this.who, other.who) || this.requestCode != other.requestCode) {
                    return false;
                }
                Intent intent = this.requestIntent;
                Intent intent2 = other.requestIntent;
                if (intent != intent2) {
                    if (this.requestIntent != null) {
                        if (!this.requestIntent.filterEquals(intent2)) {
                            return false;
                        }
                    } else if (intent2 != null) {
                        return false;
                    }
                }
                if (Objects.equals(this.requestResolvedType, other.requestResolvedType) && this.flags == other.flags) {
                    return true;
                }
                return false;
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int hashCode() {
            return this.hashCode;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Key{");
            sb.append(typeName());
            sb.append(" pkg=");
            sb.append(this.packageName);
            sb.append(" intent=");
            Intent intent = this.requestIntent;
            sb.append(intent != null ? intent.toShortString(false, true, false, false) : "<null>");
            sb.append(" flags=0x");
            sb.append(Integer.toHexString(this.flags));
            sb.append(" u=");
            sb.append(this.userId);
            sb.append("}");
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public String typeName() {
            int i = this.type;
            if (i == 1) {
                return "broadcastIntent";
            }
            if (i == 2) {
                return "startActivity";
            }
            if (i == 3) {
                return "activityResult";
            }
            if (i == 4) {
                return "startService";
            }
            if (i != 5) {
                return Integer.toString(i);
            }
            return "startForegroundService";
        }
    }

    PendingIntentRecord(PendingIntentController _controller, Key _k, int _u) {
        this.controller = _controller;
        this.key = _k;
        this.uid = _u;
        this.ref = new WeakReference<>(this);
    }

    /* access modifiers changed from: package-private */
    public void setWhitelistDurationLocked(IBinder whitelistToken, long duration) {
        if (duration > 0) {
            if (this.whitelistDuration == null) {
                this.whitelistDuration = new ArrayMap<>();
            }
            this.whitelistDuration.put(whitelistToken, Long.valueOf(duration));
        } else {
            ArrayMap<IBinder, Long> arrayMap = this.whitelistDuration;
            if (arrayMap != null) {
                arrayMap.remove(whitelistToken);
                if (this.whitelistDuration.size() <= 0) {
                    this.whitelistDuration = null;
                }
            }
        }
        this.stringName = null;
    }

    /* access modifiers changed from: package-private */
    public void setAllowBgActivityStarts(IBinder token, int flags) {
        if (token != null) {
            if ((flags & 1) != 0) {
                this.mAllowBgActivityStartsForActivitySender.add(token);
            }
            if ((flags & 2) != 0) {
                this.mAllowBgActivityStartsForBroadcastSender.add(token);
            }
            if ((flags & 4) != 0) {
                this.mAllowBgActivityStartsForServiceSender.add(token);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAllowBgActivityStarts(IBinder token) {
        if (token != null) {
            this.mAllowBgActivityStartsForActivitySender.remove(token);
            this.mAllowBgActivityStartsForBroadcastSender.remove(token);
            this.mAllowBgActivityStartsForServiceSender.remove(token);
        }
    }

    public void registerCancelListenerLocked(IResultReceiver receiver) {
        if (this.mCancelCallbacks == null) {
            this.mCancelCallbacks = new RemoteCallbackList<>();
        }
        this.mCancelCallbacks.register(receiver);
    }

    public void unregisterCancelListenerLocked(IResultReceiver receiver) {
        RemoteCallbackList<IResultReceiver> remoteCallbackList = this.mCancelCallbacks;
        if (remoteCallbackList != null) {
            remoteCallbackList.unregister(receiver);
            if (this.mCancelCallbacks.getRegisteredCallbackCount() <= 0) {
                this.mCancelCallbacks = null;
            }
        }
    }

    public RemoteCallbackList<IResultReceiver> detachCancelListenersLocked() {
        RemoteCallbackList<IResultReceiver> listeners = this.mCancelCallbacks;
        this.mCancelCallbacks = null;
        return listeners;
    }

    public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        sendInner(code, intent, resolvedType, whitelistToken, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options);
    }

    public int sendWithResult(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        return sendInner(code, intent, resolvedType, whitelistToken, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0198, code lost:
        r2.append(r7.getAction());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x01a4, code lost:
        if (r7.getComponent() == null) goto L_0x01ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x01a6, code lost:
        r7.getComponent().appendShortString(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x01b2, code lost:
        if (r7.getData() == null) goto L_0x01bf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x01b4, code lost:
        r2.append(r7.getData().toSafeString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x01bf, code lost:
        r38.controller.mAmInternal.tempWhitelistForPendingIntent(r31, r11, r38.uid, r28.longValue(), r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x01d9, code lost:
        android.util.Slog.w(com.android.server.am.PendingIntentRecord.TAG, "Not doing whitelist " + r38 + ": caller state=" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01f8, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x01fe, code lost:
        if (r43 == null) goto L_0x0202;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0200, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0202, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0203, code lost:
        r35 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:?, code lost:
        r1 = r38.key.userId;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x020a, code lost:
        if (r1 != -2) goto L_0x0218;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x020c, code lost:
        r36 = r38.controller.mUserController.getCurrentOrTargetUserId();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0218, code lost:
        r36 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x021c, code lost:
        if (r38.uid == r11) goto L_0x022a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x0226, code lost:
        if (r38.controller.mAtmInternal.isUidForeground(r11) == false) goto L_0x022a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x0228, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x022a, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x022b, code lost:
        r1 = r38.key.type;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x0231, code lost:
        if (r1 == 1) goto L_0x0361;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x0233, code lost:
        if (r1 == 2) goto L_0x02be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0236, code lost:
        if (r1 == 3) goto L_0x0297;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x023a, code lost:
        if (r1 == 4) goto L_0x0244;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x023c, code lost:
        if (r1 == 5) goto L_0x0244;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x023e, code lost:
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:?, code lost:
        r1 = r38.controller.mAmInternal;
        r2 = r38.uid;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x024e, code lost:
        if (r38.key.type != 5) goto L_0x0253;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x0250, code lost:
        r20 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x0253, code lost:
        r20 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0255, code lost:
        r3 = r38.key.packageName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x025f, code lost:
        if (r38.mAllowBgActivityStartsForServiceSender.contains(r42) != false) goto L_0x0267;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x0261, code lost:
        if (r1 == false) goto L_0x0264;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x0264, code lost:
        r23 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x0267, code lost:
        r23 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x0269, code lost:
        r1.startServiceInPackage(r2, r7, r24, r20, r3, r36, r23);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0278, code lost:
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x027f, code lost:
        r19 = r7;
        r9 = -96;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0289, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x028a, code lost:
        android.util.Slog.w(com.android.server.am.PendingIntentRecord.TAG, "Unable to send startService intent", r0);
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0297, code lost:
        r38.controller.mAtmInternal.sendActivityResult(-1, r38.key.activity, r38.key.who, r38.key.requestCode, r39, r7);
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x02c2, code lost:
        if (r38.key.allIntents == null) goto L_0x030b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x02c9, code lost:
        if (r38.key.allIntents.length <= 1) goto L_0x030b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x02dc, code lost:
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x02f7, code lost:
        r34 = r38.controller.mAtmInternal.startActivitiesInPackage(r38.uid, r31, r11, r38.key.packageName, r29, r30, r45, r27, r36, false, r38, r38.mAllowBgActivityStartsForActivitySender.contains(r42));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x02fa, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x02fe, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x0304, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x0305, code lost:
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x030b, code lost:
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x0343, code lost:
        r34 = r38.controller.mAtmInternal.startActivityInPackage(r38.uid, r31, r11, r38.key.packageName, r19, r24, r45, r46, r47, 0, r27, r36, null, "PendingIntentRecord", false, r38, r38.mAllowBgActivityStartsForActivitySender.contains(r42));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x0345, code lost:
        r9 = r34;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x0349, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x034b, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x0352, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0353, code lost:
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:?, code lost:
        android.util.Slog.w(com.android.server.am.PendingIntentRecord.TAG, "Unable to send startActivity intent", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x0361, code lost:
        r19 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:?, code lost:
        r1 = r38.controller.mAmInternal;
        r2 = r38.key.packageName;
        r3 = r38.uid;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x0373, code lost:
        if (r43 == null) goto L_0x0377;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x0375, code lost:
        r14 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0377, code lost:
        r14 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x0382, code lost:
        if (r38.mAllowBgActivityStartsForBroadcastSender.contains(r42) != false) goto L_0x038a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x0384, code lost:
        if (r1 == false) goto L_0x0387;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:194:0x0387, code lost:
        r17 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x038a, code lost:
        r17 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x03a4, code lost:
        if (r1.broadcastIntentInPackage(r2, r3, r11, r31, r19, r24, r43, r39, (java.lang.String) null, (android.os.Bundle) null, r44, r50, r14, false, r36, r17) != 0) goto L_0x03a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x03a6, code lost:
        r35 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x03a8, code lost:
        r9 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x03ab, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x03b2, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:?, code lost:
        android.util.Slog.w(com.android.server.am.PendingIntentRecord.TAG, "Unable to send startActivity intent", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x03bb, code lost:
        r9 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x03bd, code lost:
        if (r35 != false) goto L_0x03bf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:?, code lost:
        r43.performReceive(new android.content.Intent(r19), 0, (java.lang.String) null, (android.os.Bundle) null, false, false, r38.key.userId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x03db, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x03df, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x03e9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x03fb, code lost:
        android.os.Binder.restoreCallingIdentity(r32);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x03ff, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x0400, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x0406, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x040a, code lost:
        android.os.Binder.restoreCallingIdentity(r32);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x040d, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x015f, code lost:
        r11 = android.os.Binder.getCallingUid();
        r31 = android.os.Binder.getCallingPid();
        r32 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x016d, code lost:
        if (r28 == null) goto L_0x01fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:?, code lost:
        r1 = r38.controller.mAmInternal.getUidProcessState(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x017b, code lost:
        if (android.app.ActivityManager.isProcStateBackground(r1) != false) goto L_0x01d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x017d, code lost:
        r2 = new java.lang.StringBuilder(64);
        r2.append("pendingintent:");
        android.os.UserHandle.formatUid(r2, r11);
        r2.append(":");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0196, code lost:
        if (r7.getAction() == null) goto L_0x01a0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x03bf  */
    public int sendInner(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options) {
        Throwable th;
        String resolvedType2;
        SafeActivityOptions mergedOptions;
        Long duration;
        String[] allResolvedTypes;
        Intent[] allIntents;
        String resolvedType3;
        String resolvedType4;
        if (intent != null) {
            intent.setDefusable(true);
        }
        if (options != null) {
            options.setDefusable(true);
        }
        synchronized (this.controller.mLock) {
            try {
                if (this.canceled) {
                    try {
                        return -96;
                    } catch (Throwable th2) {
                        th = th2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        throw th;
                    }
                } else {
                    this.sent = true;
                    if ((this.key.flags & 1073741824) != 0) {
                        this.controller.cancelIntentSender(this, true);
                    }
                    Intent finalIntent = this.key.requestIntent != null ? new Intent(this.key.requestIntent) : new Intent();
                    try {
                        if (!((this.key.flags & 67108864) != 0)) {
                            if (intent != null) {
                                try {
                                    if ((finalIntent.fillIn(intent, this.key.flags) & 2) == 0) {
                                        resolvedType4 = this.key.requestResolvedType;
                                    } else {
                                        resolvedType4 = resolvedType;
                                    }
                                    resolvedType3 = resolvedType4;
                                } catch (Throwable th4) {
                                    th = th4;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } else {
                                try {
                                    resolvedType3 = this.key.requestResolvedType;
                                } catch (Throwable th5) {
                                    th = th5;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                            int flagsMask2 = flagsMask & -196;
                            try {
                                finalIntent.setFlags((finalIntent.getFlags() & (~flagsMask2)) | (flagsValues & flagsMask2));
                                resolvedType2 = resolvedType3;
                            } catch (Throwable th6) {
                                th = th6;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } else {
                            try {
                                resolvedType2 = this.key.requestResolvedType;
                            } catch (Throwable th7) {
                                th = th7;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                    try {
                        ActivityOptions opts = ActivityOptions.fromBundle(options);
                        if (opts != null) {
                            try {
                                finalIntent.addFlags(opts.getPendingIntentLaunchFlags());
                            } catch (Throwable th9) {
                                th = th9;
                            }
                        }
                        SafeActivityOptions mergedOptions2 = this.key.options;
                        if (mergedOptions2 == null) {
                            mergedOptions = new SafeActivityOptions(opts);
                        } else {
                            mergedOptions2.setCallerOptions(opts);
                            mergedOptions = mergedOptions2;
                        }
                        try {
                            if (this.whitelistDuration != null) {
                                try {
                                    duration = this.whitelistDuration.get(whitelistToken);
                                } catch (Throwable th10) {
                                    th = th10;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } else {
                                duration = null;
                            }
                        } catch (Throwable th11) {
                            th = th11;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th12) {
                        th = th12;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                    try {
                        if (this.key.type == 2) {
                            try {
                                if (this.key.allIntents != null && this.key.allIntents.length > 1) {
                                    Intent[] allIntents2 = new Intent[this.key.allIntents.length];
                                    String[] allResolvedTypes2 = new String[this.key.allIntents.length];
                                    System.arraycopy(this.key.allIntents, 0, allIntents2, 0, this.key.allIntents.length);
                                    if (this.key.allResolvedTypes != null) {
                                        System.arraycopy(this.key.allResolvedTypes, 0, allResolvedTypes2, 0, this.key.allResolvedTypes.length);
                                    }
                                    allIntents2[allIntents2.length - 1] = finalIntent;
                                    allResolvedTypes2[allResolvedTypes2.length - 1] = resolvedType2;
                                    allIntents = allIntents2;
                                    allResolvedTypes = allResolvedTypes2;
                                }
                            } catch (Throwable th13) {
                                th = th13;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        allIntents = null;
                        allResolvedTypes = null;
                        try {
                        } catch (Throwable th14) {
                            th = th14;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th15) {
                        th = th15;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th16) {
                th = th16;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (!this.canceled) {
                this.controller.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$PendingIntentRecord$hlEHdgdG_SS5n3v7IRr7e6QZgLQ.INSTANCE, this));
            }
        } finally {
            PendingIntentRecord.super.finalize();
        }
    }

    /* access modifiers changed from: private */
    public void completeFinalize() {
        synchronized (this.controller.mLock) {
            if (this.controller.mIntentSenderRecords.get(this.key) == this.ref) {
                this.controller.mIntentSenderRecords.remove(this.key);
            }
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("uid=");
        pw.print(this.uid);
        pw.print(" packageName=");
        pw.print(this.key.packageName);
        pw.print(" type=");
        pw.print(this.key.typeName());
        pw.print(" flags=0x");
        pw.println(Integer.toHexString(this.key.flags));
        if (!(this.key.activity == null && this.key.who == null)) {
            pw.print(prefix);
            pw.print("activity=");
            pw.print(this.key.activity);
            pw.print(" who=");
            pw.println(this.key.who);
        }
        if (!(this.key.requestCode == 0 && this.key.requestResolvedType == null)) {
            pw.print(prefix);
            pw.print("requestCode=");
            pw.print(this.key.requestCode);
            pw.print(" requestResolvedType=");
            pw.println(this.key.requestResolvedType);
        }
        if (this.key.requestIntent != null) {
            pw.print(prefix);
            pw.print("requestIntent=");
            pw.println(this.key.requestIntent.toShortString(false, true, true, true));
        }
        if (this.sent || this.canceled) {
            pw.print(prefix);
            pw.print("sent=");
            pw.print(this.sent);
            pw.print(" canceled=");
            pw.println(this.canceled);
        }
        if (this.whitelistDuration != null) {
            pw.print(prefix);
            pw.print("whitelistDuration=");
            for (int i = 0; i < this.whitelistDuration.size(); i++) {
                if (i != 0) {
                    pw.print(", ");
                }
                pw.print(Integer.toHexString(System.identityHashCode(this.whitelistDuration.keyAt(i))));
                pw.print(":");
                TimeUtils.formatDuration(this.whitelistDuration.valueAt(i).longValue(), pw);
            }
            pw.println();
        }
        if (this.mCancelCallbacks != null) {
            pw.print(prefix);
            pw.println("mCancelCallbacks:");
            for (int i2 = 0; i2 < this.mCancelCallbacks.getRegisteredCallbackCount(); i2++) {
                pw.print(prefix);
                pw.print("  #");
                pw.print(i2);
                pw.print(": ");
                pw.println(this.mCancelCallbacks.getRegisteredCallbackItem(i2));
            }
        }
    }

    public String toString() {
        String str = this.stringName;
        if (str != null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("PendingIntentRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.key.packageName);
        sb.append(' ');
        sb.append(this.key.typeName());
        if (this.whitelistDuration != null) {
            sb.append(" (whitelist: ");
            for (int i = 0; i < this.whitelistDuration.size(); i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(Integer.toHexString(System.identityHashCode(this.whitelistDuration.keyAt(i))));
                sb.append(":");
                TimeUtils.formatDuration(this.whitelistDuration.valueAt(i).longValue(), sb);
            }
            sb.append(")");
        }
        sb.append('}');
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }
}
