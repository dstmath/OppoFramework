package android.app;

import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnail;
import android.app.ApplicationErrorReport.CrashInfo;
import android.app.IActivityManager.ContentProviderHolder;
import android.app.IActivityManager.WaitResult;
import android.app.IAppTask.Stub;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.UriPermission;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.StrictMode.ViolationInfo;
import android.os.Trace;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.IResultReceiver;
import java.util.ArrayList;
import java.util.List;

/* compiled from: ActivityManagerNative */
class ActivityManagerProxy implements IActivityManager {
    private IBinder mRemote;

    public ActivityManagerProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        Trace.traceBegin(64, "amStartActivity");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(3, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        Trace.traceEnd(64);
        return result;
    }

    public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(153, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public int startActivityAsCaller(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options, boolean ignoreTargetSecurity, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(ignoreTargetSecurity ? 1 : 0);
        data.writeInt(userId);
        this.mRemote.transact(233, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(105, data, reply, 0);
        reply.readException();
        WaitResult result = (WaitResult) WaitResult.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return result;
    }

    public int startActivityWithConfig(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Configuration config, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        config.writeToParcel(data, 0);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(3, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public int startActivityIntentSender(IApplicationThread caller, IntentSender intent, Intent fillInIntent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        intent.writeToParcel(data, 0);
        if (fillInIntent != null) {
            data.writeInt(1);
            fillInIntent.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(flagsMask);
        data.writeInt(flagsValues);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(100, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public int startVoiceActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, IVoiceInteractionSession session, IVoiceInteractor interactor, int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(callingPackage);
        data.writeInt(callingPid);
        data.writeInt(callingUid);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(session.asBinder());
        data.writeStrongBinder(interactor.asBinder());
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(219, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public void startLocalVoiceInteraction(IBinder callingActivity, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(callingActivity);
        data.writeBundle(options);
        this.mRemote.transact(IActivityManager.START_LOCAL_VOICE_INTERACTION_TRANSACTION, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public void stopLocalVoiceInteraction(IBinder callingActivity) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(callingActivity);
        this.mRemote.transact(IActivityManager.STOP_LOCAL_VOICE_INTERACTION_TRANSACTION, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public boolean supportsLocalVoiceInteraction() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.SUPPORTS_LOCAL_VOICE_INTERACTION_TRANSACTION, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        if (result != 0) {
            return true;
        }
        return false;
    }

    public boolean startNextMatchingActivity(IBinder callingActivity, Intent intent, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(callingActivity);
        intent.writeToParcel(data, 0);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(67, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        if (result != 0) {
            return true;
        }
        return false;
    }

    public int startActivityFromRecents(int taskId, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        if (options == null) {
            data.writeInt(0);
        } else {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        }
        this.mRemote.transact(230, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public boolean finishActivity(IBinder token, int resultCode, Intent resultData, int finishTask) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(resultCode);
        if (resultData != null) {
            data.writeInt(1);
            resultData.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(finishTask);
        this.mRemote.transact(11, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void finishSubActivity(IBinder token, String resultWho, int requestCode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        this.mRemote.transact(32, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean finishActivityAffinity(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(149, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void finishVoiceTask(IVoiceInteractionSession session) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(session.asBinder());
        this.mRemote.transact(224, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean releaseActivityInstance(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(236, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void releaseSomeActivities(IApplicationThread app) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app.asBinder());
        this.mRemote.transact(237, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean willActivityBeVisible(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(106, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public Intent registerReceiver(IApplicationThread caller, String packageName, IIntentReceiver receiver, IntentFilter filter, String perm, int userId) throws RemoteException {
        IBinder asBinder;
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            asBinder = caller.asBinder();
        } else {
            asBinder = null;
        }
        data.writeStrongBinder(asBinder);
        data.writeString(packageName);
        if (receiver != null) {
            iBinder = receiver.asBinder();
        }
        data.writeStrongBinder(iBinder);
        filter.writeToParcel(data, 0);
        data.writeString(perm);
        data.writeInt(userId);
        this.mRemote.transact(12, data, reply, 0);
        reply.readException();
        Intent intent = null;
        if (reply.readInt() != 0) {
            intent = (Intent) Intent.CREATOR.createFromParcel(reply);
        }
        reply.recycle();
        data.recycle();
        return intent;
    }

    public void unregisterReceiver(IIntentReceiver receiver) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(receiver.asBinder());
        this.mRemote.transact(13, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int broadcastIntent(IApplicationThread caller, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle map, String[] requiredPermissions, int appOp, Bundle options, boolean serialized, boolean sticky, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo != null ? resultTo.asBinder() : null);
        data.writeInt(resultCode);
        data.writeString(resultData);
        data.writeBundle(map);
        data.writeStringArray(requiredPermissions);
        data.writeInt(appOp);
        data.writeBundle(options);
        data.writeInt(serialized ? 1 : 0);
        data.writeInt(sticky ? 1 : 0);
        data.writeInt(userId);
        this.mRemote.transact(14, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        reply.recycle();
        data.recycle();
        return res;
    }

    public void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        intent.writeToParcel(data, 0);
        data.writeInt(userId);
        this.mRemote.transact(15, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map, boolean abortBroadcast, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(who);
        data.writeInt(resultCode);
        data.writeString(resultData);
        data.writeBundle(map);
        data.writeInt(abortBroadcast ? 1 : 0);
        data.writeInt(flags);
        this.mRemote.transact(16, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void finishNotOrderReceiver(IBinder who, int hasCode, int resultCode, String resultData, Bundle map, boolean abortBroadcast) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(who);
        data.writeInt(hasCode);
        data.writeInt(resultCode);
        data.writeString(resultData);
        data.writeBundle(map);
        data.writeInt(abortBroadcast ? 1 : 0);
        this.mRemote.transact(803, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void attachApplication(IApplicationThread app) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app.asBinder());
        this.mRemote.transact(17, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void activityIdle(IBinder token, Configuration config, boolean stopProfiling) throws RemoteException {
        int i = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (config != null) {
            data.writeInt(1);
            config.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (stopProfiling) {
            i = 1;
        }
        data.writeInt(i);
        this.mRemote.transact(18, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void activityResumed(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(39, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void activityPaused(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(19, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void activityStopped(IBinder token, Bundle state, PersistableBundle persistentState, CharSequence description) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeBundle(state);
        data.writePersistableBundle(persistentState);
        TextUtils.writeToParcel(description, data, 0);
        this.mRemote.transact(20, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void activitySlept(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(123, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void activityDestroyed(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(62, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void activityRelaunched(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.ACTIVITY_RELAUNCHED_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public String getCallingPackage(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(21, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    public ComponentName getCallingActivity(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(22, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    public List<IAppTask> getAppTasks(String callingPackage) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(callingPackage);
        this.mRemote.transact(221, data, reply, 0);
        reply.readException();
        List<IAppTask> list = null;
        int N = reply.readInt();
        if (N >= 0) {
            list = new ArrayList();
            while (N > 0) {
                list.add(Stub.asInterface(reply.readStrongBinder()));
                N--;
            }
        }
        data.recycle();
        reply.recycle();
        return list;
    }

    public int addAppTask(IBinder activityToken, Intent intent, TaskDescription description, Bitmap thumbnail) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        intent.writeToParcel(data, 0);
        description.writeToParcel(data, 0);
        thumbnail.writeToParcel(data, 0);
        this.mRemote.transact(234, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public Point getAppTaskThumbnailSize() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(235, data, reply, 0);
        reply.readException();
        Point size = (Point) Point.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return size;
    }

    public List<RunningTaskInfo> getTasks(int maxNum, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(maxNum);
        data.writeInt(flags);
        this.mRemote.transact(23, data, reply, 0);
        reply.readException();
        List<RunningTaskInfo> list = null;
        int N = reply.readInt();
        if (N >= 0) {
            list = new ArrayList();
            while (N > 0) {
                list.add((RunningTaskInfo) RunningTaskInfo.CREATOR.createFromParcel(reply));
                N--;
            }
        }
        data.recycle();
        reply.recycle();
        return list;
    }

    public ParceledListSlice<RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(maxNum);
        data.writeInt(flags);
        data.writeInt(userId);
        this.mRemote.transact(60, data, reply, 0);
        reply.readException();
        ParceledListSlice<RecentTaskInfo> list = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return list;
    }

    public TaskThumbnail getTaskThumbnail(int id) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(id);
        this.mRemote.transact(82, data, reply, 0);
        reply.readException();
        TaskThumbnail taskThumbnail = null;
        if (reply.readInt() != 0) {
            taskThumbnail = (TaskThumbnail) TaskThumbnail.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return taskThumbnail;
    }

    public List<RunningServiceInfo> getServices(int maxNum, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(maxNum);
        data.writeInt(flags);
        this.mRemote.transact(81, data, reply, 0);
        reply.readException();
        List<RunningServiceInfo> list = null;
        int N = reply.readInt();
        if (N >= 0) {
            list = new ArrayList();
            while (N > 0) {
                list.add((RunningServiceInfo) RunningServiceInfo.CREATOR.createFromParcel(reply));
                N--;
            }
        }
        data.recycle();
        reply.recycle();
        return list;
    }

    public List<ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(77, data, reply, 0);
        reply.readException();
        ArrayList<ProcessErrorStateInfo> list = reply.createTypedArrayList(ProcessErrorStateInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    public List<RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(83, data, reply, 0);
        reply.readException();
        ArrayList<RunningAppProcessInfo> list = reply.createTypedArrayList(RunningAppProcessInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    public List<ApplicationInfo> getRunningExternalApplications() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(108, data, reply, 0);
        reply.readException();
        ArrayList<ApplicationInfo> list = reply.createTypedArrayList(ApplicationInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    public void moveTaskToFront(int task, int flags, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(task);
        data.writeInt(flags);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(24, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (nonRoot) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(75, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void moveTaskBackwards(int task) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(task);
        this.mRemote.transact(26, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void moveTaskToStack(int taskId, int stackId, boolean toTop) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        data.writeInt(stackId);
        if (toTop) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(169, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean moveTaskToDockedStack(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean moveHomeStackFront) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        data.writeInt(createMode);
        if (toTop) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (animate) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (initialBounds != null) {
            data.writeInt(1);
            initialBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (!moveHomeStackFront) {
            i2 = 0;
        }
        data.writeInt(i2);
        this.mRemote.transact(IActivityManager.MOVE_TASK_TO_DOCKED_STACK_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean moveTopActivityToPinnedStack(int stackId, Rect r) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackId);
        r.writeToParcel(data, 0);
        this.mRemote.transact(IActivityManager.MOVE_TOP_ACTIVITY_TO_PINNED_STACK_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void resizeStack(int stackId, Rect r, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackId);
        if (r != null) {
            data.writeInt(1);
            r.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (allowResizeInDockedMode) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (preserveWindows) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (!animate) {
            i2 = 0;
        }
        data.writeInt(i2);
        data.writeInt(animationDuration);
        this.mRemote.transact(170, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void swapDockedAndFullscreenStack() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.SWAP_DOCKED_AND_FULLSCREEN_STACK, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (dockedBounds != null) {
            data.writeInt(1);
            dockedBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (tempDockedTaskBounds != null) {
            data.writeInt(1);
            tempDockedTaskBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (tempDockedTaskInsetBounds != null) {
            data.writeInt(1);
            tempDockedTaskInsetBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (tempOtherTaskBounds != null) {
            data.writeInt(1);
            tempOtherTaskBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (tempOtherTaskInsetBounds != null) {
            data.writeInt(1);
            tempOtherTaskInsetBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(IActivityManager.RESIZE_DOCKED_STACK_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void resizePinnedStack(Rect pinnedBounds, Rect tempPinnedTaskBounds) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (pinnedBounds != null) {
            data.writeInt(1);
            pinnedBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (tempPinnedTaskBounds != null) {
            data.writeInt(1);
            tempPinnedTaskBounds.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(IActivityManager.RESIZE_PINNED_STACK_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void positionTaskInStack(int taskId, int stackId, int position) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        data.writeInt(stackId);
        data.writeInt(position);
        this.mRemote.transact(IActivityManager.POSITION_TASK_IN_STACK_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public List<StackInfo> getAllStackInfos() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(171, data, reply, 0);
        reply.readException();
        ArrayList<StackInfo> list = reply.createTypedArrayList(StackInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    public StackInfo getStackInfo(int stackId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackId);
        this.mRemote.transact(173, data, reply, 0);
        reply.readException();
        StackInfo stackInfo = null;
        if (reply.readInt() != 0) {
            stackInfo = (StackInfo) StackInfo.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return stackInfo;
    }

    public boolean isInHomeStack(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(213, data, reply, 0);
        reply.readException();
        boolean isInHomeStack = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return isInHomeStack;
    }

    public void setFocusedStack(int stackId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackId);
        this.mRemote.transact(172, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getFocusedStackId() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.GET_FOCUSED_STACK_ID_TRANSACTION, data, reply, 0);
        reply.readException();
        int focusedStackId = reply.readInt();
        data.recycle();
        reply.recycle();
        return focusedStackId;
    }

    public void setFocusedTask(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(131, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void registerTaskStackListener(ITaskStackListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(listener.asBinder());
        this.mRemote.transact(243, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getTaskForActivity(IBinder token, boolean onlyRoot) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (onlyRoot) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(27, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public ContentProviderHolder getContentProvider(IApplicationThread caller, String name, int userId, boolean stable) throws RemoteException {
        int i;
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeString(name);
        data.writeInt(userId);
        if (stable) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(29, data, reply, 0);
        reply.readException();
        ContentProviderHolder contentProviderHolder = null;
        if (reply.readInt() != 0) {
            contentProviderHolder = (ContentProviderHolder) ContentProviderHolder.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return contentProviderHolder;
    }

    public ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(name);
        data.writeInt(userId);
        data.writeStrongBinder(token);
        this.mRemote.transact(141, data, reply, 0);
        reply.readException();
        ContentProviderHolder contentProviderHolder = null;
        if (reply.readInt() != 0) {
            contentProviderHolder = (ContentProviderHolder) ContentProviderHolder.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return contentProviderHolder;
    }

    public void publishContentProviders(IApplicationThread caller, List<ContentProviderHolder> providers) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeTypedList(providers);
        this.mRemote.transact(30, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean refContentProvider(IBinder connection, int stable, int unstable) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        data.writeInt(stable);
        data.writeInt(unstable);
        this.mRemote.transact(31, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void unstableProviderDied(IBinder connection) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        this.mRemote.transact(151, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void appNotRespondingViaProvider(IBinder connection) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        this.mRemote.transact(183, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void removeContentProvider(IBinder connection, boolean stable) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        if (stable) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(69, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void removeContentProviderExternal(String name, IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(name);
        data.writeStrongBinder(token);
        this.mRemote.transact(142, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public PendingIntent getRunningServiceControlPanel(ComponentName service) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        service.writeToParcel(data, 0);
        this.mRemote.transact(33, data, reply, 0);
        reply.readException();
        PendingIntent res = PendingIntent.readPendingIntentOrNullFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, String callingPackage, int userId) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeString(callingPackage);
        data.writeInt(userId);
        this.mRemote.transact(34, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeInt(userId);
        this.mRemote.transact(35, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        reply.recycle();
        data.recycle();
        return res;
    }

    public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        ComponentName.writeToParcel(className, data);
        data.writeStrongBinder(token);
        data.writeInt(startId);
        this.mRemote.transact(48, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        ComponentName.writeToParcel(className, data);
        data.writeStrongBinder(token);
        data.writeInt(id);
        if (notification != null) {
            data.writeInt(1);
            notification.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(flags);
        this.mRemote.transact(74, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeStrongBinder(token);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(connection.asBinder());
        data.writeInt(flags);
        data.writeString(callingPackage);
        data.writeInt(userId);
        this.mRemote.transact(36, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean unbindService(IServiceConnection connection) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection.asBinder());
        this.mRemote.transact(37, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        intent.writeToParcel(data, 0);
        data.writeStrongBinder(service);
        this.mRemote.transact(38, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void unbindFinished(IBinder token, Intent intent, boolean doRebind) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        intent.writeToParcel(data, 0);
        if (doRebind) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(72, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(type);
        data.writeInt(startId);
        data.writeInt(res);
        this.mRemote.transact(61, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public IBinder peekService(Intent service, String resolvedType, String callingPackage) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeString(callingPackage);
        this.mRemote.transact(85, data, reply, 0);
        reply.readException();
        IBinder binder = reply.readStrongBinder();
        reply.recycle();
        data.recycle();
        return binder;
    }

    public boolean bindBackupAgent(String packageName, int backupRestoreMode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(backupRestoreMode);
        data.writeInt(userId);
        this.mRemote.transact(90, data, reply, 0);
        reply.readException();
        boolean success = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return success;
    }

    public void clearPendingBackup() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(160, data, reply, 0);
        reply.recycle();
        data.recycle();
    }

    public void backupAgentCreated(String packageName, IBinder agent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeStrongBinder(agent);
        this.mRemote.transact(91, data, reply, 0);
        reply.recycle();
        data.recycle();
    }

    public void unbindBackupAgent(ApplicationInfo app) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        app.writeToParcel(data, 0);
        this.mRemote.transact(92, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public boolean startInstrumentation(ComponentName className, String profileFile, int flags, Bundle arguments, IInstrumentationWatcher watcher, IUiAutomationConnection connection, int userId, String instructionSet) throws RemoteException {
        IBinder asBinder;
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        ComponentName.writeToParcel(className, data);
        data.writeString(profileFile);
        data.writeInt(flags);
        data.writeBundle(arguments);
        if (watcher != null) {
            asBinder = watcher.asBinder();
        } else {
            asBinder = null;
        }
        data.writeStrongBinder(asBinder);
        if (connection != null) {
            iBinder = connection.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeInt(userId);
        data.writeString(instructionSet);
        this.mRemote.transact(44, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return res;
    }

    public void finishInstrumentation(IApplicationThread target, int resultCode, Bundle results) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (target != null) {
            iBinder = target.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeInt(resultCode);
        data.writeBundle(results);
        this.mRemote.transact(45, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public Configuration getConfiguration() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(46, data, reply, 0);
        reply.readException();
        Configuration res = (Configuration) Configuration.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return res;
    }

    public void updateConfiguration(Configuration values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        values.writeToParcel(data, 0);
        this.mRemote.transact(47, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setRequestedOrientation(IBinder token, int requestedOrientation) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(requestedOrientation);
        this.mRemote.transact(70, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getRequestedOrientation(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(71, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public ComponentName getActivityClassForToken(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(49, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    public String getPackageForToken(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(50, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(type);
        data.writeString(packageName);
        data.writeStrongBinder(token);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        if (intents != null) {
            data.writeInt(1);
            data.writeTypedArray(intents, 0);
            data.writeStringArray(resolvedTypes);
        } else {
            data.writeInt(0);
        }
        data.writeInt(flags);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(63, data, reply, 0);
        reply.readException();
        IIntentSender res = IIntentSender.Stub.asInterface(reply.readStrongBinder());
        data.recycle();
        reply.recycle();
        return res;
    }

    public void cancelIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(64, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public String getPackageForIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(65, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    public int getUidForIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(93, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(callingPid);
        data.writeInt(callingUid);
        data.writeInt(userId);
        if (allowAll) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (!requireFull) {
            i2 = 0;
        }
        data.writeInt(i2);
        data.writeString(name);
        data.writeString(callerPackage);
        this.mRemote.transact(94, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public void setProcessLimit(int max) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(max);
        this.mRemote.transact(51, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getProcessLimit() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(52, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public void setProcessForeground(IBinder token, int pid, boolean isForeground) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(pid);
        if (isForeground) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(73, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int checkPermission(String permission, int pid, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(permission);
        data.writeInt(pid);
        data.writeInt(uid);
        this.mRemote.transact(53, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public int checkPermissionWithToken(String permission, int pid, int uid, IBinder callerToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(permission);
        data.writeInt(pid);
        data.writeInt(uid);
        data.writeStrongBinder(callerToken);
        this.mRemote.transact(242, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        if (observer != null) {
            iBinder = observer.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeInt(userId);
        this.mRemote.transact(78, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId, IBinder callerToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(pid);
        data.writeInt(uid);
        data.writeInt(mode);
        data.writeInt(userId);
        data.writeStrongBinder(callerToken);
        this.mRemote.transact(54, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller.asBinder());
        data.writeString(targetPkg);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(55, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void revokeUriPermission(IApplicationThread caller, Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller.asBinder());
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(56, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void takePersistableUriPermission(Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(180, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void releasePersistableUriPermission(Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(181, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public ParceledListSlice<UriPermission> getPersistedUriPermissions(String packageName, boolean incoming) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        if (incoming) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(182, data, reply, 0);
        reply.readException();
        ParceledListSlice<UriPermission> perms = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return perms;
    }

    public ParceledListSlice<UriPermission> getGrantedUriPermissions(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        this.mRemote.transact(IActivityManager.GET_GRANTED_URI_PERMISSIONS_TRANSACTION, data, reply, 0);
        reply.readException();
        ParceledListSlice<UriPermission> perms = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return perms;
    }

    public void clearGrantedUriPermissions(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        this.mRemote.transact(IActivityManager.CLEAR_GRANTED_URI_PERMISSIONS_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void showWaitingForDebugger(IApplicationThread who, boolean waiting) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(who.asBinder());
        if (waiting) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(58, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void getMemoryInfo(MemoryInfo outInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(76, data, reply, 0);
        reply.readException();
        outInfo.readFromParcel(reply);
        data.recycle();
        reply.recycle();
    }

    public void unhandledBack() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(4, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public ParcelFileDescriptor openContentUri(Uri uri) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(5, data, reply, 0);
        reply.readException();
        ParcelFileDescriptor parcelFileDescriptor = null;
        if (reply.readInt() != 0) {
            parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return parcelFileDescriptor;
    }

    public void setLockScreenShown(boolean showing, boolean occluded) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (showing) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (!occluded) {
            i2 = 0;
        }
        data.writeInt(i2);
        this.mRemote.transact(148, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        if (waitForDebugger) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (!persistent) {
            i2 = 0;
        }
        data.writeInt(i2);
        this.mRemote.transact(42, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setAlwaysFinish(boolean enabled) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(43, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setActivityController(IActivityController watcher, boolean imAMonkey) throws RemoteException {
        int i;
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (watcher != null) {
            iBinder = watcher.asBinder();
        }
        data.writeStrongBinder(iBinder);
        if (imAMonkey) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(57, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setOppoKinectActivityController(IOppoKinectActivityController watcher) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (watcher != null) {
            iBinder = watcher.asBinder();
        }
        data.writeStrongBinder(iBinder);
        this.mRemote.transact(805, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setLenientBackgroundCheck(boolean enabled) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(IActivityManager.SET_LENIENT_BACKGROUND_CHECK_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getMemoryTrimLevel() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.GET_MEMORY_TRIM_LEVEL_TRANSACTION, data, reply, 0);
        reply.readException();
        int level = reply.readInt();
        data.recycle();
        reply.recycle();
        return level;
    }

    public void enterSafeMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(66, data, null, 0);
        data.recycle();
    }

    public void noteWakeupAlarm(IIntentSender sender, int sourceUid, String sourcePkg, String tag) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        data.writeInt(sourceUid);
        data.writeString(sourcePkg);
        data.writeString(tag);
        this.mRemote.transact(68, data, null, 0);
        data.recycle();
    }

    public void noteAlarmStart(IIntentSender sender, int sourceUid, String tag) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        data.writeInt(sourceUid);
        data.writeString(tag);
        this.mRemote.transact(IActivityManager.NOTE_ALARM_START_TRANSACTION, data, null, 0);
        data.recycle();
    }

    public void noteAlarmFinish(IIntentSender sender, int sourceUid, String tag) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        data.writeInt(sourceUid);
        data.writeString(tag);
        this.mRemote.transact(IActivityManager.NOTE_ALARM_FINISH_TRANSACTION, data, null, 0);
        data.recycle();
    }

    public boolean killPids(int[] pids, String reason, boolean secure) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeIntArray(pids);
        data.writeString(reason);
        if (secure) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(80, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean killProcessesBelowForeground(String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(reason);
        this.mRemote.transact(144, data, reply, 0);
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean testIsSystemReady() {
        return true;
    }

    public void handleApplicationCrash(IBinder app, CrashInfo crashInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app);
        crashInfo.writeToParcel(data, 0);
        this.mRemote.transact(2, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public boolean handleApplicationWtf(IBinder app, String tag, boolean system, CrashInfo crashInfo) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app);
        data.writeString(tag);
        if (system) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        crashInfo.writeToParcel(data, 0);
        this.mRemote.transact(102, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return res;
    }

    public void handleApplicationStrictModeViolation(IBinder app, int violationMask, ViolationInfo info) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app);
        data.writeInt(violationMask);
        info.writeToParcel(data, 0);
        this.mRemote.transact(110, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public void signalPersistentProcesses(int sig) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(sig);
        this.mRemote.transact(59, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void killBackgroundProcesses(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        this.mRemote.transact(103, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void killAllBackgroundProcesses() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(140, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void killPackageDependents(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        this.mRemote.transact(IActivityManager.KILL_PACKAGE_DEPENDENTS_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void forceStopPackage(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        this.mRemote.transact(79, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void getMyMemoryState(RunningAppProcessInfo outInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(143, data, reply, 0);
        reply.readException();
        outInfo.readFromParcel(reply);
        reply.recycle();
        data.recycle();
    }

    public ConfigurationInfo getDeviceConfigurationInfo() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(84, data, reply, 0);
        reply.readException();
        ConfigurationInfo res = (ConfigurationInfo) ConfigurationInfo.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return res;
    }

    public boolean profileControl(String process, int userId, boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(process);
        data.writeInt(userId);
        if (start) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeInt(profileType);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(86, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return res;
    }

    public boolean shutdown(int timeout) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(timeout);
        this.mRemote.transact(87, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return res;
    }

    public void stopAppSwitches() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(88, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public void resumeAppSwitches() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(89, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public void addPackageDependency(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        this.mRemote.transact(95, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void killApplication(String pkg, int appId, int userId, String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(pkg);
        data.writeInt(appId);
        data.writeInt(userId);
        data.writeString(reason);
        this.mRemote.transact(96, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void closeSystemDialogs(String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(reason);
        this.mRemote.transact(97, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeIntArray(pids);
        this.mRemote.transact(98, data, reply, 0);
        reply.readException();
        Debug.MemoryInfo[] res = (Debug.MemoryInfo[]) reply.createTypedArray(Debug.MemoryInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return res;
    }

    public void killApplicationProcess(String processName, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(processName);
        data.writeInt(uid);
        this.mRemote.transact(99, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeString(packageName);
        data.writeInt(enterAnim);
        data.writeInt(exitAnim);
        this.mRemote.transact(101, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isUserAMonkey() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(104, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void setUserIsMonkey(boolean monkey) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (monkey) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(166, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void finishHeavyWeightApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(109, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean convertFromTranslucent(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(174, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean convertToTranslucent(IBinder token, ActivityOptions options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (options == null) {
            data.writeInt(0);
        } else {
            data.writeInt(1);
            data.writeBundle(options.toBundle());
        }
        this.mRemote.transact(175, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public ActivityOptions getActivityOptions(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(220, data, reply, 0);
        reply.readException();
        ActivityOptions options = ActivityOptions.fromBundle(reply.readBundle());
        data.recycle();
        reply.recycle();
        return options;
    }

    public void setImmersive(IBinder token, boolean immersive) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (immersive) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(112, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isImmersive(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(111, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean isTopOfTask(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(225, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean isTopActivityImmersive() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(113, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void crashApplication(int uid, int initialPid, String packageName, String message) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(uid);
        data.writeInt(initialPid);
        data.writeString(packageName);
        data.writeString(message);
        this.mRemote.transact(114, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public String getProviderMimeType(Uri uri, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(userId);
        this.mRemote.transact(115, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    public IBinder newUriPermissionOwner(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(name);
        this.mRemote.transact(116, data, reply, 0);
        reply.readException();
        IBinder res = reply.readStrongBinder();
        data.recycle();
        reply.recycle();
        return res;
    }

    public IBinder getUriPermissionOwnerForActivity(IBinder activityToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        this.mRemote.transact(IActivityManager.GET_URI_PERMISSION_OWNER_FOR_ACTIVITY_TRANSACTION, data, reply, 0);
        reply.readException();
        IBinder res = reply.readStrongBinder();
        data.recycle();
        reply.recycle();
        return res;
    }

    public void grantUriPermissionFromOwner(IBinder owner, int fromUid, String targetPkg, Uri uri, int mode, int sourceUserId, int targetUserId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(owner);
        data.writeInt(fromUid);
        data.writeString(targetPkg);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(sourceUserId);
        data.writeInt(targetUserId);
        this.mRemote.transact(55, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void revokeUriPermissionFromOwner(IBinder owner, Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(owner);
        if (uri != null) {
            data.writeInt(1);
            uri.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(56, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri, int modeFlags, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(callingUid);
        data.writeString(targetPkg);
        uri.writeToParcel(data, 0);
        data.writeInt(modeFlags);
        data.writeInt(userId);
        this.mRemote.transact(119, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean dumpHeap(String process, int userId, boolean managed, String path, ParcelFileDescriptor fd) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(process);
        data.writeInt(userId);
        if (managed) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeString(path);
        if (fd != null) {
            data.writeInt(1);
            fd.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(120, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return res;
    }

    public int startActivities(IApplicationThread caller, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle options, int userId) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeString(callingPackage);
        data.writeTypedArray(intents, 0);
        data.writeStringArray(resolvedTypes);
        data.writeStrongBinder(resultTo);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(121, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public int getFrontActivityScreenCompatMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(124, data, reply, 0);
        reply.readException();
        int mode = reply.readInt();
        reply.recycle();
        data.recycle();
        return mode;
    }

    public void setFrontActivityScreenCompatMode(int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(mode);
        this.mRemote.transact(125, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public int getPackageScreenCompatMode(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        this.mRemote.transact(126, data, reply, 0);
        reply.readException();
        int mode = reply.readInt();
        reply.recycle();
        data.recycle();
        return mode;
    }

    public void setPackageScreenCompatMode(String packageName, int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(mode);
        this.mRemote.transact(127, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public boolean getPackageAskScreenCompat(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        this.mRemote.transact(128, data, reply, 0);
        reply.readException();
        boolean ask = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return ask;
    }

    public void setPackageAskScreenCompat(String packageName, boolean ask) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        if (ask) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(129, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public boolean switchUser(int userid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        this.mRemote.transact(130, data, reply, 0);
        reply.readException();
        boolean result = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return result;
    }

    public boolean startUserInBackground(int userid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        this.mRemote.transact(212, data, reply, 0);
        reply.readException();
        boolean result = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return result;
    }

    public boolean unlockUser(int userId, byte[] token, byte[] secret, IProgressListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userId);
        data.writeByteArray(token);
        data.writeByteArray(secret);
        data.writeStrongInterface(listener);
        this.mRemote.transact(IActivityManager.UNLOCK_USER_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean result = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return result;
    }

    public int stopUser(int userid, boolean force, IStopUserCallback callback) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        if (force) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeStrongInterface(callback);
        this.mRemote.transact(154, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public UserInfo getCurrentUser() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(145, data, reply, 0);
        reply.readException();
        UserInfo userInfo = (UserInfo) UserInfo.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return userInfo;
    }

    public boolean isUserRunning(int userid, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        data.writeInt(flags);
        this.mRemote.transact(122, data, reply, 0);
        reply.readException();
        boolean result = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return result;
    }

    public int[] getRunningUserIds() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(157, data, reply, 0);
        reply.readException();
        int[] result = reply.createIntArray();
        reply.recycle();
        data.recycle();
        return result;
    }

    public boolean removeTask(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(132, data, reply, 0);
        reply.readException();
        boolean result = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return result;
    }

    public void registerProcessObserver(IProcessObserver observer) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (observer != null) {
            iBinder = observer.asBinder();
        }
        data.writeStrongBinder(iBinder);
        this.mRemote.transact(133, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void unregisterProcessObserver(IProcessObserver observer) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (observer != null) {
            iBinder = observer.asBinder();
        }
        data.writeStrongBinder(iBinder);
        this.mRemote.transact(134, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void registerUidObserver(IUidObserver observer, int which) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (observer != null) {
            iBinder = observer.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeInt(which);
        this.mRemote.transact(IActivityManager.REGISTER_UID_OBSERVER_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void unregisterUidObserver(IUidObserver observer) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (observer != null) {
            iBinder = observer.asBinder();
        }
        data.writeStrongBinder(iBinder);
        this.mRemote.transact(IActivityManager.UNREGISTER_UID_OBSERVER_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isIntentSenderTargetedToPackage(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(135, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean isIntentSenderAnActivity(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(152, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public Intent getIntentForIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(161, data, reply, 0);
        reply.readException();
        Intent res = reply.readInt() != 0 ? (Intent) Intent.CREATOR.createFromParcel(reply) : null;
        data.recycle();
        reply.recycle();
        return res;
    }

    public String getTagForIntentSender(IIntentSender sender, String prefix) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        data.writeString(prefix);
        this.mRemote.transact(211, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    public void updatePersistentConfiguration(Configuration values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        values.writeToParcel(data, 0);
        this.mRemote.transact(136, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public long[] getProcessPss(int[] pids) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeIntArray(pids);
        this.mRemote.transact(137, data, reply, 0);
        reply.readException();
        long[] res = reply.createLongArray();
        data.recycle();
        reply.recycle();
        return res;
    }

    public ComponentName getTopAppName() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(802, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    public long[] getProcessPswap(int[] pids) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeIntArray(pids);
        this.mRemote.transact(406, data, reply, 0);
        reply.readException();
        long[] res = reply.createLongArray();
        data.recycle();
        reply.recycle();
        return res;
    }

    public void showBootMessage(CharSequence msg, boolean always) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        TextUtils.writeToParcel(msg, data, 0);
        if (always) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(138, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void keyguardWaitingForActivityDrawn() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(232, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void keyguardGoingAway(int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(flags);
        this.mRemote.transact(IActivityManager.KEYGUARD_GOING_AWAY_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean shouldUpRecreateTask(IBinder token, String destAffinity) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeString(destAffinity);
        this.mRemote.transact(146, data, reply, 0);
        reply.readException();
        boolean result = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return result;
    }

    public boolean navigateUpTo(IBinder token, Intent target, int resultCode, Intent resultData) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        target.writeToParcel(data, 0);
        data.writeInt(resultCode);
        if (resultData != null) {
            data.writeInt(1);
            resultData.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(147, data, reply, 0);
        reply.readException();
        boolean result = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return result;
    }

    public int getLaunchedFromUid(IBinder activityToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        this.mRemote.transact(150, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        data.recycle();
        reply.recycle();
        return result;
    }

    public String getLaunchedFromPackage(IBinder activityToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        this.mRemote.transact(164, data, reply, 0);
        reply.readException();
        String result = reply.readString();
        data.recycle();
        reply.recycle();
        return result;
    }

    public void registerUserSwitchObserver(IUserSwitchObserver observer, String name) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (observer != null) {
            iBinder = observer.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeString(name);
        this.mRemote.transact(155, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void unregisterUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (observer != null) {
            iBinder = observer.asBinder();
        }
        data.writeStrongBinder(iBinder);
        this.mRemote.transact(156, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void requestBugReport(int bugreportType) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(bugreportType);
        this.mRemote.transact(158, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(pid);
        if (aboveSystem) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeString(reason);
        this.mRemote.transact(159, data, reply, 0);
        reply.readException();
        long res = (long) reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public Bundle getAssistContextExtras(int requestType) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(requestType);
        this.mRemote.transact(162, data, reply, 0);
        reply.readException();
        Bundle res = reply.readBundle();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean requestAssistContextExtras(int requestType, IResultReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(requestType);
        data.writeStrongBinder(receiver.asBinder());
        data.writeBundle(receiverExtras);
        data.writeStrongBinder(activityToken);
        if (focused) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (!newSessionId) {
            i2 = 0;
        }
        data.writeInt(i2);
        this.mRemote.transact(IActivityManager.REQUEST_ASSIST_CONTEXT_EXTRAS_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void reportAssistContextExtras(IBinder token, Bundle extras, AssistStructure structure, AssistContent content, Uri referrer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeBundle(extras);
        structure.writeToParcel(data, 0);
        content.writeToParcel(data, 0);
        if (referrer != null) {
            data.writeInt(1);
            referrer.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(163, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle, Bundle args) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        intent.writeToParcel(data, 0);
        data.writeInt(requestType);
        data.writeString(hint);
        data.writeInt(userHandle);
        data.writeBundle(args);
        this.mRemote.transact(240, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean isAssistDataAllowedOnCurrentActivity() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(300, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean showAssistFromActivity(IBinder token, Bundle args) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeBundle(args);
        this.mRemote.transact(301, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public void killUid(int appId, int userId, String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(appId);
        data.writeInt(userId);
        data.writeString(reason);
        this.mRemote.transact(165, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void hang(IBinder who, boolean allowRestart) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(who);
        if (allowRestart) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(167, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void reportActivityFullyDrawn(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(177, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void notifyActivityDrawn(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(176, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void restart() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(178, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void performIdleMaintenance() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(179, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void sendIdleJobTrigger() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.SEND_IDLE_JOB_TRIGGER_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public IActivityContainer createVirtualActivityContainer(IBinder parentActivityToken, IActivityContainerCallback callback) throws RemoteException {
        IActivityContainer res;
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(parentActivityToken);
        if (callback != null) {
            iBinder = callback.asBinder();
        }
        data.writeStrongBinder(iBinder);
        this.mRemote.transact(168, data, reply, 0);
        reply.readException();
        if (reply.readInt() == 1) {
            res = IActivityContainer.Stub.asInterface(reply.readStrongBinder());
        } else {
            res = null;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    public void deleteActivityContainer(IActivityContainer activityContainer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityContainer.asBinder());
        this.mRemote.transact(186, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean startBinderTracking() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.START_BINDER_TRACKING_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return res;
    }

    public boolean stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (fd != null) {
            data.writeInt(1);
            fd.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(IActivityManager.STOP_BINDER_TRACKING_AND_DUMP_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() != 0;
        reply.recycle();
        data.recycle();
        return res;
    }

    public int setVrMode(IBinder token, boolean enabled, ComponentName packageName) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        packageName.writeToParcel(data, 0);
        this.mRemote.transact(IActivityManager.SET_VR_MODE_TRANSACTION, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean isVrModePackageEnabled(ComponentName packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        packageName.writeToParcel(data, 0);
        this.mRemote.transact(IActivityManager.IS_VR_PACKAGE_ENABLED_TRANSACTION, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        if (res == 1) {
            return true;
        }
        return false;
    }

    public IActivityContainer createStackOnDisplay(int displayId) throws RemoteException {
        IActivityContainer res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(displayId);
        this.mRemote.transact(IActivityManager.CREATE_STACK_ON_DISPLAY, data, reply, 0);
        reply.readException();
        if (reply.readInt() == 1) {
            res = IActivityContainer.Stub.asInterface(reply.readStrongBinder());
        } else {
            res = null;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    public int getActivityDisplayId(IBinder activityToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        this.mRemote.transact(185, data, reply, 0);
        reply.readException();
        int displayId = reply.readInt();
        data.recycle();
        reply.recycle();
        return displayId;
    }

    public void startLockTaskMode(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(214, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void startLockTaskMode(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(215, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void startSystemLockTaskMode(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(222, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void stopLockTaskMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(216, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void stopSystemLockTaskMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(223, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isInLockTaskMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(217, data, reply, 0);
        reply.readException();
        boolean isInLockTaskMode = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return isInLockTaskMode;
    }

    public int getLockTaskModeState() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.GET_LOCK_TASK_MODE_STATE_TRANSACTION, data, reply, 0);
        reply.readException();
        int lockTaskModeState = reply.readInt();
        data.recycle();
        reply.recycle();
        return lockTaskModeState;
    }

    public void showLockTaskEscapeMessage(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.SHOW_LOCK_TASK_ESCAPE_MESSAGE_TRANSACTION, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setTaskDescription(IBinder token, TaskDescription values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        values.writeToParcel(data, 0);
        this.mRemote.transact(218, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setTaskResizeable(int taskId, int resizeableMode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        data.writeInt(resizeableMode);
        this.mRemote.transact(IActivityManager.SET_TASK_RESIZEABLE_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void resizeTask(int taskId, Rect r, int resizeMode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        data.writeInt(resizeMode);
        r.writeToParcel(data, 0);
        this.mRemote.transact(IActivityManager.RESIZE_TASK_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public Rect getTaskBounds(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(184, data, reply, 0);
        reply.readException();
        Rect rect = (Rect) Rect.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return rect;
    }

    public Bitmap getTaskDescriptionIcon(String filename, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(filename);
        data.writeInt(userId);
        this.mRemote.transact(239, data, reply, 0);
        reply.readException();
        Bitmap icon = reply.readInt() == 0 ? null : (Bitmap) Bitmap.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return icon;
    }

    public void startInPlaceAnimationOnFrontMostApplication(ActivityOptions options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (options == null) {
            data.writeInt(0);
        } else {
            data.writeInt(1);
            data.writeBundle(options.toBundle());
        }
        this.mRemote.transact(241, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean requestVisibleBehind(IBinder token, boolean visible) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (visible) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(226, data, reply, 0);
        reply.readException();
        boolean success = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return success;
    }

    public boolean isBackgroundVisibleBehind(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(227, data, reply, 0);
        reply.readException();
        boolean visible = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return visible;
    }

    public void backgroundResourcesReleased(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(228, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void notifyLaunchTaskBehindComplete(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(229, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void notifyEnterAnimationComplete(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(231, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void bootAnimationComplete() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(238, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void notifyCleartextNetwork(int uid, byte[] firstPacket) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(uid);
        data.writeByteArray(firstPacket);
        this.mRemote.transact(IActivityManager.NOTIFY_CLEARTEXT_NETWORK_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setDumpHeapDebugLimit(String processName, int uid, long maxMemSize, String reportPackage) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(processName);
        data.writeInt(uid);
        data.writeLong(maxMemSize);
        data.writeString(reportPackage);
        this.mRemote.transact(IActivityManager.SET_DUMP_HEAP_DEBUG_LIMIT_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void dumpHeapFinished(String path) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(path);
        this.mRemote.transact(IActivityManager.DUMP_HEAP_FINISHED_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setVoiceKeepAwake(IVoiceInteractionSession session, boolean keepAwake) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(session.asBinder());
        if (keepAwake) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(IActivityManager.SET_VOICE_KEEP_AWAKE_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void updateLockTaskPackages(int userId, String[] packages) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userId);
        data.writeStringArray(packages);
        this.mRemote.transact(IActivityManager.UPDATE_LOCK_TASK_PACKAGES_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void updateDeviceOwner(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        this.mRemote.transact(IActivityManager.UPDATE_DEVICE_OWNER_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getPackageProcessState(String packageName, String callingPackage) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeString(callingPackage);
        this.mRemote.transact(IActivityManager.GET_PACKAGE_PROCESS_STATE_TRANSACTION, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean setProcessMemoryTrimLevel(String process, int userId, int level) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(process);
        data.writeInt(userId);
        data.writeInt(level);
        this.mRemote.transact(187, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        if (res != 0) {
            return true;
        }
        return false;
    }

    public boolean isRootVoiceInteraction(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(302, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        if (res != 0) {
            return true;
        }
        return false;
    }

    public void exitFreeformMode(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.EXIT_FREEFORM_MODE_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getActivityStackId(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.GET_ACTIVITY_STACK_ID_TRANSACTION, data, reply, 0);
        reply.readException();
        int stackId = reply.readInt();
        data.recycle();
        reply.recycle();
        return stackId;
    }

    public void reportSizeConfigurations(IBinder token, int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        writeIntArray(horizontalSizeConfiguration, data);
        writeIntArray(verticalSizeConfigurations, data);
        writeIntArray(smallestSizeConfigurations, data);
        this.mRemote.transact(IActivityManager.REPORT_SIZE_CONFIGURATIONS, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private static void writeIntArray(int[] array, Parcel data) {
        if (array == null) {
            data.writeInt(0);
            return;
        }
        data.writeInt(array.length);
        data.writeIntArray(array);
    }

    public void suppressResizeConfigChanges(boolean suppress) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (suppress) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(IActivityManager.SUPPRESS_RESIZE_CONFIG_CHANGES_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void moveTasksToFullscreenStack(int fromStackId, boolean onTop) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(fromStackId);
        if (onTop) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(IActivityManager.MOVE_TASKS_TO_FULLSCREEN_STACK_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int getAppStartMode(int uid, String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(uid);
        data.writeString(packageName);
        this.mRemote.transact(IActivityManager.GET_APP_START_MODE_TRANSACTION, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean isInMultiWindowMode(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.IN_MULTI_WINDOW_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean multiWindowMode = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return multiWindowMode;
    }

    public boolean isInPictureInPictureMode(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.IN_PICTURE_IN_PICTURE_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean pipMode = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return pipMode;
    }

    public void enterPictureInPictureMode(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.ENTER_PICTURE_IN_PICTURE_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isAppForeground(int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(uid);
        this.mRemote.transact(IActivityManager.IS_APP_FOREGROUND_TRANSACTION, data, reply, 0);
        boolean isForeground = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return isForeground;
    }

    public void notifyPinnedStackAnimationEnded() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.NOTIFY_PINNED_STACK_ANIMATION_ENDED_TRANSACTION, data, reply, 0);
        data.recycle();
        reply.recycle();
    }

    public void removeStack(int stackId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackId);
        this.mRemote.transact(IActivityManager.REMOVE_STACK, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void notifyLockedProfile(int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userId);
        this.mRemote.transact(IActivityManager.NOTIFY_LOCKED_PROFILE, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void startConfirmDeviceCredentialIntent(Intent intent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        intent.writeToParcel(data, 0);
        this.mRemote.transact(IActivityManager.START_CONFIRM_DEVICE_CREDENTIAL_INTENT, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public int sendIntentSender(IIntentSender target, int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(target.asBinder());
        data.writeInt(code);
        if (intent != null) {
            data.writeInt(1);
            intent.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeString(resolvedType);
        if (finishedReceiver != null) {
            iBinder = finishedReceiver.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeString(requiredPermission);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(IActivityManager.SEND_INTENT_SENDER_TRANSACTION, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean adjustSplitScreen(int action, int from) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(action);
        data.writeInt(from);
        this.mRemote.transact(807, data, reply, 0);
        boolean isForeground = reply.readInt() == 1;
        data.recycle();
        reply.recycle();
        return isForeground;
    }

    public ComponentName getDockTopAppName() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(808, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    public void setVrThread(int tid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(tid);
        this.mRemote.transact(IActivityManager.SET_VR_THREAD_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setRenderThread(int tid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(tid);
        this.mRemote.transact(IActivityManager.SET_RENDER_THREAD_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setHasTopUi(boolean hasTopUi) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(hasTopUi ? 1 : 0);
        this.mRemote.transact(IActivityManager.SET_HAS_TOP_UI, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean canBypassWorkChallenge(PendingIntent intent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        intent.writeToParcel(data, 0);
        this.mRemote.transact(IActivityManager.CAN_BYPASS_WORK_CHALLENGE, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        data.recycle();
        reply.recycle();
        if (result != 0) {
            return true;
        }
        return false;
    }

    public void forceKillPackage(String packageName, int userId, String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        data.writeString(reason);
        this.mRemote.transact(501, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setWallpaperProcess(ComponentName className) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        ComponentName.writeToParcel(className, data);
        this.mRemote.transact(502, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void updateWallpaperState(boolean isForeground) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (isForeground) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(503, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void stickWindow(IBinder token, boolean isSticky) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (isSticky) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(504, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isStickyByMtk(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(IActivityManager.GET_STICKY_WINDOW_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean isSticky = reply.readInt() != 0;
        data.recycle();
        reply.recycle();
        return isSticky;
    }

    public void restoreWindow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.RESTORE_WINDOW_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public String[] getPackageListFromPid(int pid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(pid);
        this.mRemote.transact(IActivityManager.GET_INFO_PACKAGE_LIST_FROM_PID_TRANSACTION, data, reply, 0);
        reply.readException();
        String[] result = reply.readStringArray();
        data.recycle();
        reply.recycle();
        return result;
    }

    public ArrayMap<Integer, ArrayList<Integer>> getProcessesWithAdj() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.GET_INFO_PROCESSES_WITH_ADJ_TRANSACTION, data, reply, 0);
        reply.readException();
        ArrayMap<Integer, ArrayList<Integer>> result = null;
        int size = reply.readInt();
        if (size > 0) {
            result = new ArrayMap();
            for (int i = 0; i < size; i++) {
                int key = reply.readInt();
                int listSize = reply.readInt();
                Object entryList = null;
                if (listSize > 0) {
                    entryList = new ArrayList();
                    for (int j = 0; j < listSize; j++) {
                        entryList.add(Integer.valueOf(reply.readInt()));
                    }
                }
                result.put(Integer.valueOf(key), entryList);
            }
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public int readyToGetContentProvider(IApplicationThread caller, String name, int userId) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        }
        data.writeStrongBinder(iBinder);
        data.writeString(name);
        data.writeInt(userId);
        this.mRemote.transact(IActivityManager.READY_TO_GET_CONTENT_PROVIDER_TRANSACTION, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        data.recycle();
        reply.recycle();
        return result;
    }

    public void reportJunkFromApp(String type, String packageName, String content, boolean forcePersist) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(type);
        data.writeString(packageName);
        data.writeString(content);
        if (forcePersist) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(806, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isTopRunningTaskDockable() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(809, data, reply, 0);
        reply.readException();
        int isDockable = reply.readInt();
        data.recycle();
        reply.recycle();
        return isDockable == 1;
    }

    public void appDexOpt(boolean start) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (start) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(804, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setAalMode(int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(mode);
        this.mRemote.transact(IActivityManager.AAL_SET_AAL_MODE, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void setAalEnabled(boolean enabled) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(IActivityManager.AAL_SET_AAL_ENABLED, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean addDisallowedRunningApp(List<String> appPkgNamesList) throws RemoteException {
        if (appPkgNamesList == null || appPkgNamesList.size() <= 0) {
            Log.w("ActivityManagerNative", "addDisallowedRunningApp:appPkgNamesList is empty!");
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStringList(appPkgNamesList);
        this.mRemote.transact(IActivityManager.ADD_DISALLOWED_RUNNING_APP_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public boolean removeDisallowedRunningApp(List<String> appPkgNamesList) throws RemoteException {
        if (appPkgNamesList == null || appPkgNamesList.size() <= 0) {
            Log.w("ActivityManagerNative", "removeDisallowedRunningApp:appPkgNamesList is empty!");
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStringList(appPkgNamesList);
        this.mRemote.transact(IActivityManager.REMOVE_DISALLOWED_RUNNING_APP_TRANSACTION, data, reply, 0);
        reply.readException();
        boolean res = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return res;
    }

    public List<String> getDisallowedRunningApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(IActivityManager.GET_DISALLOWED_RUNNING_APP_TRANSACTION, data, reply, 0);
        reply.readException();
        List<String> list = reply.createStringArrayList();
        data.recycle();
        reply.recycle();
        return list;
    }

    public void allowToUseSdcard(boolean allow) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (allow) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(IActivityManager.ALLOW_TO_USE_SDCARD_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }
}
