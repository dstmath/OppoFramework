package android.app;

import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentReceiver.Stub;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.AndroidException;

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
public final class PendingIntent implements Parcelable {
    public static final Creator<PendingIntent> CREATOR = null;
    public static final int FLAG_CANCEL_CURRENT = 268435456;
    public static final int FLAG_IMMUTABLE = 67108864;
    public static final int FLAG_NO_CREATE = 536870912;
    public static final int FLAG_ONE_SHOT = 1073741824;
    public static final int FLAG_UPDATE_CURRENT = 134217728;
    private static final ThreadLocal<OnMarshaledListener> sOnMarshaledListener = null;
    private final IIntentSender mTarget;

    public interface OnMarshaledListener {
        void onMarshaled(PendingIntent pendingIntent, Parcel parcel, int i);
    }

    public static class CanceledException extends AndroidException {
        public CanceledException(String name) {
            super(name);
        }

        public CanceledException(Exception cause) {
            super(cause);
        }
    }

    private static class FinishedDispatcher extends Stub implements Runnable {
        private static Handler sDefaultSystemHandler;
        private final Handler mHandler;
        private Intent mIntent;
        private final PendingIntent mPendingIntent;
        private int mResultCode;
        private String mResultData;
        private Bundle mResultExtras;
        private final OnFinished mWho;

        FinishedDispatcher(PendingIntent pi, OnFinished who, Handler handler) {
            this.mPendingIntent = pi;
            this.mWho = who;
            if (handler == null && ActivityThread.isSystem()) {
                if (sDefaultSystemHandler == null) {
                    sDefaultSystemHandler = new Handler(Looper.getMainLooper());
                }
                this.mHandler = sDefaultSystemHandler;
                return;
            }
            this.mHandler = handler;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean serialized, boolean sticky, int sendingUser) {
            this.mIntent = intent;
            this.mResultCode = resultCode;
            this.mResultData = data;
            this.mResultExtras = extras;
            if (this.mHandler == null) {
                run();
            } else {
                this.mHandler.post(this);
            }
        }

        public void run() {
            this.mWho.onSendFinished(this.mPendingIntent, this.mIntent, this.mResultCode, this.mResultData, this.mResultExtras);
        }
    }

    public interface OnFinished {
        void onSendFinished(PendingIntent pendingIntent, Intent intent, int i, String str, Bundle bundle);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.PendingIntent.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.PendingIntent.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.PendingIntent.<clinit>():void");
    }

    public static void setOnMarshaledListener(OnMarshaledListener listener) {
        sOnMarshaledListener.set(listener);
    }

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        return getActivity(context, requestCode, intent, flags, null);
    }

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags, Bundle options) {
        String resolvedType;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        if (intent == null) {
            return null;
        }
        try {
            String[] strArr;
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(context);
            IActivityManager iActivityManager = ActivityManagerNative.getDefault();
            Intent[] intentArr = new Intent[1];
            intentArr[0] = intent;
            if (resolvedType != null) {
                strArr = new String[1];
                strArr[0] = resolvedType;
            } else {
                strArr = null;
            }
            IIntentSender target = iActivityManager.getIntentSender(2, packageName, null, null, requestCode, intentArr, strArr, flags, options, UserHandle.myUserId());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static PendingIntent getActivityAsUser(Context context, int requestCode, Intent intent, int flags, Bundle options, UserHandle user) {
        String resolvedType;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        if (intent == null) {
            return null;
        }
        try {
            String[] strArr;
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(context);
            IActivityManager iActivityManager = ActivityManagerNative.getDefault();
            Intent[] intentArr = new Intent[1];
            intentArr[0] = intent;
            if (resolvedType != null) {
                strArr = new String[1];
                strArr[0] = resolvedType;
            } else {
                strArr = null;
            }
            IIntentSender target = iActivityManager.getIntentSender(2, packageName, null, null, requestCode, intentArr, strArr, flags, options, user.getIdentifier());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags) {
        return getActivities(context, requestCode, intents, flags, null);
    }

    public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags, Bundle options) {
        String packageName = context.getPackageName();
        String[] resolvedTypes = new String[intents.length];
        for (int i = 0; i < intents.length; i++) {
            intents[i].migrateExtraStreamToClipData();
            intents[i].prepareToLeaveProcess(context);
            resolvedTypes[i] = intents[i].resolveTypeIfNeeded(context.getContentResolver());
        }
        try {
            IIntentSender target = ActivityManagerNative.getDefault().getIntentSender(2, packageName, null, null, requestCode, intents, resolvedTypes, flags, options, UserHandle.myUserId());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static PendingIntent getActivitiesAsUser(Context context, int requestCode, Intent[] intents, int flags, Bundle options, UserHandle user) {
        String packageName = context.getPackageName();
        String[] resolvedTypes = new String[intents.length];
        for (int i = 0; i < intents.length; i++) {
            intents[i].migrateExtraStreamToClipData();
            intents[i].prepareToLeaveProcess(context);
            resolvedTypes[i] = intents[i].resolveTypeIfNeeded(context.getContentResolver());
        }
        try {
            IIntentSender target = ActivityManagerNative.getDefault().getIntentSender(2, packageName, null, null, requestCode, intents, resolvedTypes, flags, options, user.getIdentifier());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
        return getBroadcastAsUser(context, requestCode, intent, flags, new UserHandle(UserHandle.myUserId()));
    }

    public static PendingIntent getBroadcastAsUser(Context context, int requestCode, Intent intent, int flags, UserHandle userHandle) {
        String resolvedType;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        try {
            String[] strArr;
            PendingIntent pendingIntent;
            intent.prepareToLeaveProcess(context);
            IActivityManager iActivityManager = ActivityManagerNative.getDefault();
            Intent[] intentArr = new Intent[1];
            intentArr[0] = intent;
            if (resolvedType != null) {
                strArr = new String[1];
                strArr[0] = resolvedType;
            } else {
                strArr = null;
            }
            IIntentSender target = iActivityManager.getIntentSender(1, packageName, null, null, requestCode, intentArr, strArr, flags, null, userHandle.getIdentifier());
            if (target != null) {
                pendingIntent = new PendingIntent(target);
            } else {
                pendingIntent = null;
            }
            return pendingIntent;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
        String resolvedType;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        try {
            String[] strArr;
            PendingIntent pendingIntent;
            intent.prepareToLeaveProcess(context);
            IActivityManager iActivityManager = ActivityManagerNative.getDefault();
            Intent[] intentArr = new Intent[1];
            intentArr[0] = intent;
            if (resolvedType != null) {
                strArr = new String[1];
                strArr[0] = resolvedType;
            } else {
                strArr = null;
            }
            IIntentSender target = iActivityManager.getIntentSender(4, packageName, null, null, requestCode, intentArr, strArr, flags, null, UserHandle.myUserId());
            if (target != null) {
                pendingIntent = new PendingIntent(target);
            } else {
                pendingIntent = null;
            }
            return pendingIntent;
        } catch (RemoteException e) {
            return null;
        }
    }

    public IntentSender getIntentSender() {
        return new IntentSender(this.mTarget);
    }

    public void cancel() {
        try {
            ActivityManagerNative.getDefault().cancelIntentSender(this.mTarget);
        } catch (RemoteException e) {
        }
    }

    public void send() throws CanceledException {
        send(null, 0, null, null, null, null, null);
    }

    public void send(int code) throws CanceledException {
        send(null, code, null, null, null, null, null);
    }

    public void send(Context context, int code, Intent intent) throws CanceledException {
        send(context, code, intent, null, null, null, null);
    }

    public void send(int code, OnFinished onFinished, Handler handler) throws CanceledException {
        send(null, code, null, onFinished, handler, null, null);
    }

    public void send(Context context, int code, Intent intent, OnFinished onFinished, Handler handler) throws CanceledException {
        send(context, code, intent, onFinished, handler, null, null);
    }

    public void send(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission) throws CanceledException {
        send(context, code, intent, onFinished, handler, requiredPermission, null);
    }

    public void send(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission, Bundle options) throws CanceledException {
        String resolvedType;
        IIntentReceiver finishedDispatcher;
        if (intent != null) {
            try {
                resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
            } catch (Exception e) {
                throw new CanceledException(e);
            }
        }
        resolvedType = null;
        IActivityManager iActivityManager = ActivityManagerNative.getDefault();
        IIntentSender iIntentSender = this.mTarget;
        if (onFinished != null) {
            finishedDispatcher = new FinishedDispatcher(this, onFinished, handler);
        } else {
            finishedDispatcher = null;
        }
        if (iActivityManager.sendIntentSender(iIntentSender, code, intent, resolvedType, finishedDispatcher, requiredPermission, options) < 0) {
            throw new CanceledException();
        }
    }

    @Deprecated
    public String getTargetPackage() {
        try {
            return ActivityManagerNative.getDefault().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getCreatorPackage() {
        try {
            return ActivityManagerNative.getDefault().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getCreatorUid() {
        try {
            return ActivityManagerNative.getDefault().getUidForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public UserHandle getCreatorUserHandle() {
        try {
            int uid = ActivityManagerNative.getDefault().getUidForIntentSender(this.mTarget);
            return uid > 0 ? new UserHandle(UserHandle.getUserId(uid)) : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean isTargetedToPackage() {
        try {
            return ActivityManagerNative.getDefault().isIntentSenderTargetedToPackage(this.mTarget);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isActivity() {
        try {
            return ActivityManagerNative.getDefault().isIntentSenderAnActivity(this.mTarget);
        } catch (RemoteException e) {
            return false;
        }
    }

    public Intent getIntent() {
        try {
            return ActivityManagerNative.getDefault().getIntentForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getTag(String prefix) {
        try {
            return ActivityManagerNative.getDefault().getTagForIntentSender(this.mTarget, prefix);
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean equals(Object otherObj) {
        if (otherObj instanceof PendingIntent) {
            return this.mTarget.asBinder().equals(((PendingIntent) otherObj).mTarget.asBinder());
        }
        return false;
    }

    public int hashCode() {
        return this.mTarget.asBinder().hashCode();
    }

    public String toString() {
        Object obj = null;
        StringBuilder sb = new StringBuilder(128);
        sb.append("PendingIntent{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(": ");
        if (this.mTarget != null) {
            obj = this.mTarget.asBinder();
        }
        sb.append(obj);
        sb.append('}');
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mTarget.asBinder());
        OnMarshaledListener listener = (OnMarshaledListener) sOnMarshaledListener.get();
        if (listener != null) {
            listener.onMarshaled(this, out, flags);
        }
    }

    public static void writePendingIntentOrNullToParcel(PendingIntent sender, Parcel out) {
        IBinder iBinder = null;
        if (sender != null) {
            iBinder = sender.mTarget.asBinder();
        }
        out.writeStrongBinder(iBinder);
    }

    public static PendingIntent readPendingIntentOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        if (b != null) {
            return new PendingIntent(b);
        }
        return null;
    }

    PendingIntent(IIntentSender target) {
        this.mTarget = target;
    }

    PendingIntent(IBinder target) {
        this.mTarget = IIntentSender.Stub.asInterface(target);
    }

    public IIntentSender getTarget() {
        return this.mTarget;
    }
}
