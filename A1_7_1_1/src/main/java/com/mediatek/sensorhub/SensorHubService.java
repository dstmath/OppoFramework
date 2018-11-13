package com.mediatek.sensorhub;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import com.mediatek.sensorhub.ISensorHubService.Stub;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class SensorHubService extends Stub {
    static final boolean LOG = false;
    private static final int POST_EVENT_ACTION_DATA = 1;
    private static final String TAG = "SensorHubService";
    private CopyOnWriteArrayList<ActionHolder> mActionIntents;
    private int mBroadcastRefCount;
    private final Context mContext;
    private CopyOnWriteArrayList<MappingHolder> mIntent;
    private long mListenerContext;
    private Object mLock;
    private long mNativeContext;
    private final ResultReceiver mResultReceiver;
    private WakeLock mWakeLock;

    private static class Holder {
        public final int pid = Binder.getCallingPid();
        public final int uid = Binder.getCallingUid();
    }

    private static class ActionHolder extends Holder {
        public final PendingIntent intent;
        public final boolean repeat;
        public final int rid;

        public ActionHolder(int requestId, PendingIntent intent, boolean repeat) {
            this.rid = requestId;
            this.intent = intent;
            this.repeat = repeat;
        }
    }

    private static class MappingHolder extends Holder {
        public int cgesture;
        public int gesture;
        public int mCount;

        public MappingHolder(int gesture, int cgesture, int count) {
            this.gesture = gesture;
            this.cgesture = cgesture;
            this.mCount = count;
        }
    }

    class ResultReceiver implements OnFinished {
        ResultReceiver() {
        }

        public void onSendFinished(PendingIntent pi, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (SensorHubService.this.mLock) {
                SensorHubService sensorHubService = SensorHubService.this;
                sensorHubService.mBroadcastRefCount = sensorHubService.mBroadcastRefCount - 1;
                if (SensorHubService.LOG) {
                    Log.v(SensorHubService.TAG, "onSendFinished: wlCount=" + SensorHubService.this.mBroadcastRefCount);
                }
                if (SensorHubService.this.mBroadcastRefCount == 0) {
                    SensorHubService.this.mWakeLock.release();
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.sensorhub.SensorHubService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.sensorhub.SensorHubService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.sensorhub.SensorHubService.<clinit>():void");
    }

    private native void nativeAddConGesture(int i, int i2);

    private native boolean nativeCancelAction(int i);

    private native void nativeCancelConGesture(int i, int i2);

    private native boolean nativeEnableGestureWakeup(boolean z);

    private native void nativeFinalize();

    private static native void nativeInit();

    private native int nativeRequestAction(Condition condition, Action action);

    private native void nativeSetup(Object obj);

    private native boolean nativeUpdateCondition(int i, Condition condition);

    public native int[] nativeGetContextList();

    public SensorHubService(Context context) {
        this.mBroadcastRefCount = 0;
        this.mResultReceiver = new ResultReceiver();
        this.mLock = new Object();
        this.mActionIntents = new CopyOnWriteArrayList();
        this.mIntent = new CopyOnWriteArrayList();
        this.mContext = context;
        nativeSetup(new WeakReference(this));
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
    }

    public ParcelableListInteger getContextList() throws RemoteException {
        return new ParcelableListInteger(nativeGetContextList());
    }

    public int requestAction(Condition condition, Action action) throws RemoteException {
        if (this.mContext.checkCallingOrSelfPermission(SensorHubManager.WAKE_DEVICE_SENSORHUB) != 0) {
            throw new SensorHubPermissionException("Need permission " + SensorHubManager.WAKE_DEVICE_SENSORHUB);
        }
        long origId = Binder.clearCallingIdentity();
        int rid = nativeRequestAction(condition, action);
        Binder.restoreCallingIdentity(origId);
        if (LOG) {
            Log.v(TAG, "requestAction: rid=" + rid + ", " + action);
        }
        if (rid > 0) {
            ActionHolder ah = new ActionHolder(rid, action.getIntent(), action.isRepeatable());
            this.mActionIntents.add(ah);
            if (LOG) {
                Log.v(TAG, "requestAction: add client[rid=" + rid + ", pid=" + ah.pid + ", uid=" + ah.uid + "]");
            }
        }
        return rid;
    }

    public boolean cancelAction(int requestId) throws RemoteException {
        ActionHolder find = null;
        for (ActionHolder holder : this.mActionIntents) {
            if (holder.rid == requestId) {
                find = holder;
                break;
            }
        }
        if (find != null) {
            if (!(find.pid == Binder.getCallingPid() && find.uid == Binder.getCallingUid())) {
                Log.w(TAG, "cancelAction: current[pid=" + Binder.getCallingPid() + ",uid=" + Binder.getCallingUid() + "], old[pid=" + find.pid + ",uid=" + find.uid + "]");
            }
            long origId = Binder.clearCallingIdentity();
            boolean removed = nativeCancelAction(requestId);
            Binder.restoreCallingIdentity(origId);
            if (LOG) {
                Log.v(TAG, "cancelAction: rid=" + requestId + (removed ? " succeed." : " failed!"));
            }
            if (!removed) {
                return false;
            }
            this.mActionIntents.remove(find);
            return true;
        }
        if (LOG) {
            Log.v(TAG, "cancelAction: succeed due to no client. rid=" + requestId);
        }
        return true;
    }

    public boolean updateCondition(int requestId, Condition condition) throws RemoteException {
        if (this.mContext.checkCallingOrSelfPermission(SensorHubManager.WAKE_DEVICE_SENSORHUB) != 0) {
            throw new SensorHubPermissionException("Need permission " + SensorHubManager.WAKE_DEVICE_SENSORHUB);
        }
        long origId = Binder.clearCallingIdentity();
        boolean result = nativeUpdateCondition(requestId, condition);
        Binder.restoreCallingIdentity(origId);
        if (LOG) {
            Log.v(TAG, "updateCondition: rid=" + requestId + (result ? " succeed." : " failed!"));
        }
        return result;
    }

    public boolean enableGestureWakeup(boolean enabled) throws RemoteException {
        if (this.mContext.checkCallingOrSelfPermission(SensorHubManager.WAKE_DEVICE_SENSORHUB) == 0) {
            return nativeEnableGestureWakeup(enabled);
        }
        throw new SensorHubPermissionException("Need permission " + SensorHubManager.WAKE_DEVICE_SENSORHUB);
    }

    public void addConGesture(int gesture, int cgesture) throws RemoteException {
        if (this.mContext.checkCallingOrSelfPermission(SensorHubManager.WAKE_DEVICE_SENSORHUB) != 0) {
            throw new SensorHubPermissionException("Need permission " + SensorHubManager.WAKE_DEVICE_SENSORHUB);
        }
        MappingHolder mh = new MappingHolder(gesture, cgesture, 0);
        if (mh == null) {
            return;
        }
        if (this.mIntent.size() == 0) {
            mh.mCount = 1;
            this.mIntent.add(mh);
            nativeAddConGesture(gesture, cgesture);
            return;
        }
        Iterator holder$iterator = this.mIntent.iterator();
        if (holder$iterator.hasNext()) {
            MappingHolder holder = (MappingHolder) holder$iterator.next();
            if (holder.gesture == mh.gesture && holder.cgesture == mh.cgesture) {
                holder.mCount++;
                return;
            }
            mh.mCount = 1;
            this.mIntent.add(mh);
            nativeAddConGesture(gesture, cgesture);
        }
    }

    public void cancelConGesture(int gesture, int cgesture) throws RemoteException {
        if (this.mContext.checkCallingOrSelfPermission(SensorHubManager.WAKE_DEVICE_SENSORHUB) != 0) {
            throw new SensorHubPermissionException("Need permission " + SensorHubManager.WAKE_DEVICE_SENSORHUB);
        } else if (this.mIntent.size() != 0) {
            for (MappingHolder holder : this.mIntent) {
                if (holder.gesture == gesture && holder.cgesture == cgesture) {
                    holder.mCount--;
                    if (holder.mCount == 0) {
                        nativeCancelConGesture(gesture, cgesture);
                        this.mIntent.remove(holder);
                        return;
                    }
                    return;
                }
            }
        }
    }

    private ArrayList<DataCell> buildData(Object[] data) {
        ArrayList<DataCell> list = new ArrayList();
        if (data != null) {
            DataCell previousClock = null;
            DataCell currentClock = null;
            DataCell previousActivityTime = null;
            DataCell currentActivityTime = null;
            for (DataCell item : data) {
                if (12 == item.getIndex()) {
                    if (item.isPrevious()) {
                        previousClock = item;
                    } else {
                        currentClock = item;
                    }
                } else if (33 != item.getIndex()) {
                    list.add(item);
                } else if (item.isPrevious()) {
                    previousActivityTime = item;
                } else {
                    currentActivityTime = item;
                }
            }
            if (previousClock != null && previousActivityTime != null) {
                list.add(new DataCell(34, true, previousClock.getLongValue() - previousActivityTime.getLongValue()));
            } else if (currentClock == null || currentActivityTime == null) {
                if (previousClock != null) {
                    list.add(previousClock);
                }
                if (currentClock != null) {
                    list.add(currentClock);
                }
                if (previousActivityTime != null) {
                    list.add(previousActivityTime);
                }
                if (currentActivityTime != null) {
                    list.add(currentActivityTime);
                }
            } else {
                list.add(new DataCell(34, false, currentClock.getLongValue() - currentActivityTime.getLongValue()));
            }
        }
        return list;
    }

    private void handleNativeMessage(int msg, int ext1, int ext2, Object[] data) {
        if (LOG) {
            Log.v(TAG, "handleNativeMessage: msg=" + msg + ",arg1=" + ext1 + ", arg2=" + ext2);
        }
        if (msg == 1) {
            int rid = ext1;
            for (ActionHolder holder : this.mActionIntents) {
                if (holder.rid == ext1) {
                    if (!holder.repeat) {
                        this.mActionIntents.remove(holder);
                    }
                    ArrayList<DataCell> list = buildData(data);
                    try {
                        if (holder.intent == null) {
                            Log.w(TAG, "handleNativeMessage: null pendingintent!");
                            return;
                        }
                        synchronized (this.mLock) {
                            if (this.mBroadcastRefCount == 0) {
                                this.mWakeLock.acquire();
                            }
                            this.mBroadcastRefCount++;
                            if (LOG) {
                                Log.v(TAG, "handleNativeMessage: sending intent=" + holder.intent + ", wlCount=" + this.mBroadcastRefCount);
                            }
                        }
                        ActionDataResult result = new ActionDataResult(ext1, list, SystemClock.elapsedRealtime());
                        Intent intent = new Intent();
                        intent.putExtra("com.mediatek.sensorhub.EXTRA_ACTION_DATA_RESULT", result);
                        holder.intent.send(this.mContext, 0, intent, this.mResultReceiver, null);
                    } catch (CanceledException e) {
                        Log.e(TAG, "handleNativeMessage: exception for rid " + ext1, e);
                    }
                }
            }
        }
    }

    private static void postEventFromNative(Object selfRef, int msg, int ext1, int ext2, Object[] data) {
        SensorHubService service = (SensorHubService) ((WeakReference) selfRef).get();
        if (service == null) {
            Log.e(TAG, "postEventFromNative: Null SensorHubService! msg=" + msg + ", arg1=" + ext1 + ", arg2=" + ext2);
        } else {
            service.handleNativeMessage(msg, ext1, ext2, data);
        }
    }
}
