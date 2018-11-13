package com.android.server.am;

import android.app.IActivityContainer;
import android.content.IIntentReceiver;
import android.content.IIntentSender.Stub;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.util.Slog;
import android.util.TimeUtils;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Objects;

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
final class PendingIntentRecord extends Stub {
    private static final String TAG = null;
    boolean canceled;
    final Key key;
    String lastTag;
    String lastTagPrefix;
    final ActivityManagerService owner;
    final WeakReference<PendingIntentRecord> ref;
    boolean sent;
    String stringName;
    final int uid;
    private long whitelistDuration;

    static final class Key {
        private static final int ODD_PRIME_NUMBER = 37;
        final ActivityRecord activity;
        Intent[] allIntents;
        String[] allResolvedTypes;
        final int flags;
        final int hashCode;
        final Bundle options;
        final String packageName;
        final int requestCode;
        final Intent requestIntent;
        final String requestResolvedType;
        final int type;
        final int userId;
        final String who;

        Key(int _t, String _p, ActivityRecord _a, String _w, int _r, Intent[] _i, String[] _it, int _f, Bundle _o, int _userId) {
            Intent intent;
            String str = null;
            this.type = _t;
            this.packageName = _p;
            this.activity = _a;
            this.who = _w;
            this.requestCode = _r;
            if (_i != null) {
                intent = _i[_i.length - 1];
            } else {
                intent = null;
            }
            this.requestIntent = intent;
            if (_it != null) {
                str = _it[_it.length - 1];
            }
            this.requestResolvedType = str;
            this.allIntents = _i;
            this.allResolvedTypes = _it;
            this.flags = _f;
            this.options = _o;
            this.userId = _userId;
            int hash = ((((_f + 851) * 37) + _r) * 37) + _userId;
            if (_w != null) {
                hash = (hash * 37) + _w.hashCode();
            }
            if (_a != null) {
                hash = (hash * 37) + _a.hashCode();
            }
            if (this.requestIntent != null) {
                hash = (hash * 37) + this.requestIntent.filterHashCode();
            }
            if (this.requestResolvedType != null) {
                hash = (hash * 37) + this.requestResolvedType.hashCode();
            }
            this.hashCode = (((hash * 37) + (_p != null ? _p.hashCode() : 0)) * 37) + _t;
            if (this.packageName == null) {
                Slog.d(PendingIntentRecord.TAG, "special case: packageName is null for " + this + " hashCode=0x" + Integer.toHexString(this.hashCode) + " ar=" + this.activity + " who=" + this.who + " r= " + this.requestCode + " rt= " + this.requestResolvedType);
            }
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
                if (this.requestIntent != other.requestIntent) {
                    if (this.requestIntent != null) {
                        if (!this.requestIntent.filterEquals(other.requestIntent)) {
                            return false;
                        }
                    } else if (other.requestIntent != null) {
                        return false;
                    }
                }
                if (!Objects.equals(this.requestResolvedType, other.requestResolvedType) || this.flags != other.flags) {
                    return false;
                }
                if (this.packageName == null) {
                    Slog.d(PendingIntentRecord.TAG, "special case: packageName is null for " + this + " hashCode=0x" + Integer.toHexString(this.hashCode));
                }
                return true;
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int hashCode() {
            return this.hashCode;
        }

        public String toString() {
            return "Key{" + typeName() + " pkg=" + this.packageName + " intent=" + (this.requestIntent != null ? this.requestIntent.toShortString(false, true, false, false) : "<null>") + " flags=0x" + Integer.toHexString(this.flags) + " u=" + this.userId + "}";
        }

        String typeName() {
            switch (this.type) {
                case 1:
                    return "broadcastIntent";
                case 2:
                    return "startActivity";
                case 3:
                    return "activityResult";
                case 4:
                    return "startService";
                default:
                    return Integer.toString(this.type);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.PendingIntentRecord.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.PendingIntentRecord.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.PendingIntentRecord.<clinit>():void");
    }

    PendingIntentRecord(ActivityManagerService _owner, Key _k, int _u) {
        this.sent = false;
        this.canceled = false;
        this.whitelistDuration = 0;
        this.owner = _owner;
        this.key = _k;
        this.uid = _u;
        this.ref = new WeakReference(this);
    }

    void setWhitelistDuration(long duration) {
        this.whitelistDuration = duration;
        this.stringName = null;
    }

    public void send(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        sendInner(code, intent, resolvedType, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options, null);
    }

    public int sendWithResult(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        return sendInner(code, intent, resolvedType, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options, null);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    int sendInner(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options, IActivityContainer container) {
        if (intent != null) {
            intent.setDefusable(true);
        }
        if (options != null) {
            options.setDefusable(true);
        }
        if (this.whitelistDuration > 0 && !this.canceled) {
            this.owner.tempWhitelistAppForPowerSave(Binder.getCallingPid(), Binder.getCallingUid(), this.uid, this.whitelistDuration);
        }
        synchronized (this.owner) {
            ActivityManagerService.boostPriorityForLockedSection();
            ActivityContainer activityContainer = (ActivityContainer) container;
            if (activityContainer == null || activityContainer.mParentActivity == null || activityContainer.mParentActivity.state == ActivityState.RESUMED) {
                try {
                } catch (Throwable e) {
                    Slog.w(TAG, "Unable to send startActivity intent", e);
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
                if (this.canceled) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return -6;
                }
                Intent finalIntent;
                this.sent = true;
                if ((this.key.flags & 1073741824) != 0) {
                    this.owner.cancelIntentSenderLocked(this, true);
                    this.canceled = true;
                }
                if (this.key.requestIntent != null) {
                    finalIntent = new Intent(this.key.requestIntent);
                } else {
                    finalIntent = new Intent();
                }
                if ((this.key.flags & 67108864) != 0) {
                    resolvedType = this.key.requestResolvedType;
                } else {
                    if (intent != null) {
                        if ((finalIntent.fillIn(intent, this.key.flags) & 2) == 0) {
                            resolvedType = this.key.requestResolvedType;
                        }
                    } else {
                        resolvedType = this.key.requestResolvedType;
                    }
                    flagsMask &= -196;
                    finalIntent.setFlags((finalIntent.getFlags() & (~flagsMask)) | (flagsValues & flagsMask));
                }
                long origId = Binder.clearCallingIdentity();
                boolean sendFinish = finishedReceiver != null;
                int userId = this.key.userId;
                if (userId == -2) {
                    userId = this.owner.mUserController.getCurrentOrTargetUserIdLocked();
                }
                int res = 0;
                switch (this.key.type) {
                    case 1:
                        try {
                            if (this.owner.broadcastIntentInPackage(this.key.packageName, this.uid, finalIntent, resolvedType, finishedReceiver, code, null, null, requiredPermission, options, finishedReceiver != null, false, userId) == 0) {
                                sendFinish = false;
                            }
                        } catch (Throwable e2) {
                            Slog.w(TAG, "Unable to send startActivity intent", e2);
                        }
                    case 2:
                        if (options == null) {
                            options = this.key.options;
                        } else if (this.key.options != null) {
                            Bundle bundle = new Bundle(this.key.options);
                            bundle.putAll(options);
                            options = bundle;
                        }
                        if (this.key.allIntents == null || this.key.allIntents.length <= 1) {
                            this.owner.startActivityInPackage(this.uid, this.key.packageName, finalIntent, resolvedType, resultTo, resultWho, requestCode, 0, options, userId, container, null);
                        } else {
                            Intent[] allIntents = new Intent[this.key.allIntents.length];
                            String[] allResolvedTypes = new String[this.key.allIntents.length];
                            System.arraycopy(this.key.allIntents, 0, allIntents, 0, this.key.allIntents.length);
                            if (this.key.allResolvedTypes != null) {
                                System.arraycopy(this.key.allResolvedTypes, 0, allResolvedTypes, 0, this.key.allResolvedTypes.length);
                            }
                            allIntents[allIntents.length - 1] = finalIntent;
                            allResolvedTypes[allResolvedTypes.length - 1] = resolvedType;
                            this.owner.startActivitiesInPackage(this.uid, this.key.packageName, allIntents, allResolvedTypes, resultTo, options, userId);
                        }
                        break;
                    case 3:
                        if (this.key.activity.task.stack != null) {
                            this.key.activity.task.stack.sendActivityResultLocked(-1, this.key.activity, this.key.who, this.key.requestCode, code, finalIntent);
                        }
                    case 4:
                        try {
                            this.owner.startServiceInPackage(this.uid, finalIntent, resolvedType, this.key.packageName, userId);
                        } catch (Throwable e22) {
                            Slog.w(TAG, "Unable to send startService intent", e22);
                        } catch (TransactionTooLargeException e3) {
                            res = -6;
                        }
                    default:
                        if (sendFinish && res != -6) {
                            try {
                                finishedReceiver.performReceive(new Intent(finalIntent), 0, null, null, false, false, this.key.userId);
                            } catch (RemoteException e4) {
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return res;
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            } else {
                ActivityManagerService.resetPriorityAfterLockedSection();
                return -6;
            }
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.canceled) {
                this.owner.mHandler.sendMessage(this.owner.mHandler.obtainMessage(23, this));
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void completeFinalize() {
        synchronized (this.owner) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (((WeakReference) this.owner.mIntentSenderRecords.get(this.key)) == this.ref) {
                    this.owner.mIntentSenderRecords.remove(this.key);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void dump(PrintWriter pw, String prefix) {
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
        if (this.whitelistDuration != 0) {
            pw.print(prefix);
            pw.print("whitelistDuration=");
            TimeUtils.formatDuration(this.whitelistDuration, pw);
            pw.println();
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("PendingIntentRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.key.packageName);
        sb.append(' ');
        sb.append(this.key.typeName());
        if (this.whitelistDuration > 0) {
            sb.append(" (whitelist: ");
            TimeUtils.formatDuration(this.whitelistDuration, sb);
            sb.append(")");
        }
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }
}
