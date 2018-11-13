package android.app;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Binder;
import android.os.Debug.MemoryInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractor.Stub;
import com.android.internal.content.ReferrerIntent;
import java.io.IOException;
import java.util.List;

public abstract class ApplicationThreadNative extends Binder implements IApplicationThread {
    public static IApplicationThread asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IApplicationThread in = (IApplicationThread) obj.queryLocalInterface(IApplicationThread.descriptor);
        if (in != null) {
            return in;
        }
        return new ApplicationThreadProxy(obj);
    }

    public ApplicationThreadNative() {
        attachInterface(this, IApplicationThread.descriptor);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        IBinder b;
        Configuration configuration;
        ParcelFileDescriptor fd;
        IBinder service;
        String[] args;
        switch (code) {
            case 1:
                data.enforceInterface(IApplicationThread.descriptor);
                schedulePauseActivity(data.readStrongBinder(), data.readInt() != 0, data.readInt() != 0, data.readInt(), data.readInt() != 0);
                return true;
            case 3:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleStopActivity(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                return true;
            case 4:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleWindowVisibility(data.readStrongBinder(), data.readInt() != 0);
                return true;
            case 5:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleResumeActivity(data.readStrongBinder(), data.readInt(), data.readInt() != 0, data.readBundle());
                return true;
            case 6:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleSendResult(data.readStrongBinder(), data.createTypedArrayList(ResultInfo.CREATOR));
                return true;
            case 7:
                data.enforceInterface(IApplicationThread.descriptor);
                Intent intent = (Intent) Intent.CREATOR.createFromParcel(data);
                b = data.readStrongBinder();
                int ident = data.readInt();
                ActivityInfo info = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(data);
                Configuration curConfig = (Configuration) Configuration.CREATOR.createFromParcel(data);
                configuration = null;
                if (data.readInt() != 0) {
                    configuration = (Configuration) Configuration.CREATOR.createFromParcel(data);
                }
                scheduleLaunchActivity(intent, b, ident, info, curConfig, configuration, (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data), data.readString(), Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readBundle(), data.readPersistableBundle(), data.createTypedArrayList(ResultInfo.CREATOR), data.createTypedArrayList(ReferrerIntent.CREATOR), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0 ? (ProfilerInfo) ProfilerInfo.CREATOR.createFromParcel(data) : null);
                return true;
            case 8:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleNewIntent(data.createTypedArrayList(ReferrerIntent.CREATOR), data.readStrongBinder(), data.readInt() == 1);
                return true;
            case 9:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleDestroyActivity(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                return true;
            case 10:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleReceiver((Intent) Intent.CREATOR.createFromParcel(data), (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(data), (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data), data.readInt(), data.readString(), data.readBundle(), data.readInt() != 0, data.readInt(), data.readInt(), data.readInt());
                return true;
            case 11:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCreateService(data.readStrongBinder(), (ServiceInfo) ServiceInfo.CREATOR.createFromParcel(data), (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data), data.readInt());
                return true;
            case 12:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleStopService(data.readStrongBinder());
                return true;
            case 13:
                ComponentName testName;
                data.enforceInterface(IApplicationThread.descriptor);
                String packageName = data.readString();
                ApplicationInfo info2 = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(data);
                List<ProviderInfo> providers = data.createTypedArrayList(ProviderInfo.CREATOR);
                if (data.readInt() != 0) {
                    ComponentName componentName = new ComponentName(data);
                } else {
                    testName = null;
                }
                bindApplication(packageName, info2, providers, testName, data.readInt() != 0 ? (ProfilerInfo) ProfilerInfo.CREATOR.createFromParcel(data) : null, data.readBundle(), IInstrumentationWatcher.Stub.asInterface(data.readStrongBinder()), IUiAutomationConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, (Configuration) Configuration.CREATOR.createFromParcel(data), (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data), data.readHashMap(null), data.readBundle());
                return true;
            case 14:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleExit();
                return true;
            case 16:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleConfigurationChanged((Configuration) Configuration.CREATOR.createFromParcel(data));
                return true;
            case 17:
                Intent args2;
                data.enforceInterface(IApplicationThread.descriptor);
                IBinder token = data.readStrongBinder();
                boolean taskRemoved = data.readInt() != 0;
                int startId = data.readInt();
                int fl = data.readInt();
                if (data.readInt() != 0) {
                    args2 = (Intent) Intent.CREATOR.createFromParcel(data);
                } else {
                    args2 = null;
                }
                scheduleServiceArgs(token, taskRemoved, startId, fl, args2);
                return true;
            case 18:
                data.enforceInterface(IApplicationThread.descriptor);
                updateTimeZone();
                return true;
            case 19:
                data.enforceInterface(IApplicationThread.descriptor);
                processInBackground();
                return true;
            case 20:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleBindService(data.readStrongBinder(), (Intent) Intent.CREATOR.createFromParcel(data), data.readInt() != 0, data.readInt());
                return true;
            case 21:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleUnbindService(data.readStrongBinder(), (Intent) Intent.CREATOR.createFromParcel(data));
                return true;
            case 22:
                data.enforceInterface(IApplicationThread.descriptor);
                fd = data.readFileDescriptor();
                service = data.readStrongBinder();
                args = data.readStringArray();
                if (fd != null) {
                    dumpService(fd.getFileDescriptor(), service, args);
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
                return true;
            case 23:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleRegisteredReceiver(IIntentReceiver.Stub.asInterface(data.readStrongBinder()), (Intent) Intent.CREATOR.createFromParcel(data), data.readInt(), data.readString(), data.readBundle(), data.readInt() != 0, data.readInt() != 0, data.readInt(), data.readInt());
                return true;
            case 24:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleLowMemory();
                return true;
            case 25:
                data.enforceInterface(IApplicationThread.descriptor);
                b = data.readStrongBinder();
                configuration = null;
                if (data.readInt() != 0) {
                    configuration = (Configuration) Configuration.CREATOR.createFromParcel(data);
                }
                scheduleActivityConfigurationChanged(b, configuration, data.readInt() == 1);
                return true;
            case 26:
                data.enforceInterface(IApplicationThread.descriptor);
                b = data.readStrongBinder();
                List<ResultInfo> ri = data.createTypedArrayList(ResultInfo.CREATOR);
                List<ReferrerIntent> pi = data.createTypedArrayList(ReferrerIntent.CREATOR);
                int configChanges = data.readInt();
                boolean notResumed = data.readInt() != 0;
                Configuration config = (Configuration) Configuration.CREATOR.createFromParcel(data);
                configuration = null;
                if (data.readInt() != 0) {
                    configuration = (Configuration) Configuration.CREATOR.createFromParcel(data);
                }
                scheduleRelaunchActivity(b, ri, pi, configChanges, notResumed, config, configuration, data.readInt() == 1);
                return true;
            case 27:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleSleeping(data.readStrongBinder(), data.readInt() != 0);
                return true;
            case 28:
                data.enforceInterface(IApplicationThread.descriptor);
                profilerControl(data.readInt() != 0, data.readInt() != 0 ? (ProfilerInfo) ProfilerInfo.CREATOR.createFromParcel(data) : null, data.readInt());
                return true;
            case 29:
                data.enforceInterface(IApplicationThread.descriptor);
                setSchedulingGroup(data.readInt());
                return true;
            case 30:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCreateBackupAgent((ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(data), (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data), data.readInt());
                return true;
            case 31:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleDestroyBackupAgent((ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(data), (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data));
                return true;
            case 32:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleOnNewActivityOptions(data.readStrongBinder(), new ActivityOptions(data.readBundle()));
                reply.writeNoException();
                return true;
            case 33:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleSuicide();
                return true;
            case 34:
                data.enforceInterface(IApplicationThread.descriptor);
                dispatchPackageBroadcast(data.readInt(), data.readStringArray());
                return true;
            case 35:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCrash(data.readString());
                return true;
            case 36:
                data.enforceInterface(IApplicationThread.descriptor);
                dumpHeap(data.readInt() != 0, data.readString(), data.readInt() != 0 ? (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data) : null);
                return true;
            case 37:
                data.enforceInterface(IApplicationThread.descriptor);
                fd = data.readFileDescriptor();
                IBinder activity = data.readStrongBinder();
                String prefix = data.readString();
                args = data.readStringArray();
                if (fd != null) {
                    dumpActivity(fd.getFileDescriptor(), activity, prefix, args);
                    try {
                        fd.close();
                    } catch (IOException e2) {
                    }
                }
                return true;
            case 38:
                data.enforceInterface(IApplicationThread.descriptor);
                clearDnsCache();
                return true;
            case 39:
                data.enforceInterface(IApplicationThread.descriptor);
                setHttpProxy(data.readString(), data.readString(), data.readString(), (Uri) Uri.CREATOR.createFromParcel(data));
                return true;
            case 40:
                data.enforceInterface(IApplicationThread.descriptor);
                setCoreSettings(data.readBundle());
                return true;
            case 41:
                data.enforceInterface(IApplicationThread.descriptor);
                updatePackageCompatibilityInfo(data.readString(), (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data));
                return true;
            case 42:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleTrimMemory(data.readInt());
                return true;
            case 43:
                data.enforceInterface(IApplicationThread.descriptor);
                fd = data.readFileDescriptor();
                MemoryInfo mi = (MemoryInfo) MemoryInfo.CREATOR.createFromParcel(data);
                boolean checkin = data.readInt() != 0;
                boolean dumpInfo = data.readInt() != 0;
                boolean dumpDalvik = data.readInt() != 0;
                boolean dumpSummaryOnly = data.readInt() != 0;
                boolean dumpUnreachable = data.readInt() != 0;
                args = data.readStringArray();
                if (fd != null) {
                    try {
                        dumpMemInfo(fd.getFileDescriptor(), mi, checkin, dumpInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable, args);
                        try {
                            fd.close();
                        } catch (IOException e3) {
                        }
                    } catch (Throwable th) {
                        try {
                            fd.close();
                        } catch (IOException e4) {
                        }
                        throw th;
                    }
                }
                reply.writeNoException();
                return true;
            case 44:
                data.enforceInterface(IApplicationThread.descriptor);
                fd = data.readFileDescriptor();
                args = data.readStringArray();
                if (fd != null) {
                    try {
                        dumpGfxInfo(fd.getFileDescriptor(), args);
                        try {
                            fd.close();
                        } catch (IOException e5) {
                        }
                    } catch (Throwable th2) {
                        try {
                            fd.close();
                        } catch (IOException e6) {
                        }
                        throw th2;
                    }
                }
                reply.writeNoException();
                return true;
            case 45:
                data.enforceInterface(IApplicationThread.descriptor);
                fd = data.readFileDescriptor();
                service = data.readStrongBinder();
                args = data.readStringArray();
                if (fd != null) {
                    dumpProvider(fd.getFileDescriptor(), service, args);
                    try {
                        fd.close();
                    } catch (IOException e7) {
                    }
                }
                return true;
            case 46:
                data.enforceInterface(IApplicationThread.descriptor);
                fd = data.readFileDescriptor();
                args = data.readStringArray();
                if (fd != null) {
                    try {
                        dumpDbInfo(fd.getFileDescriptor(), args);
                        try {
                            fd.close();
                        } catch (IOException e8) {
                        }
                    } catch (Throwable th22) {
                        try {
                            fd.close();
                        } catch (IOException e9) {
                        }
                        throw th22;
                    }
                }
                reply.writeNoException();
                return true;
            case 47:
                data.enforceInterface(IApplicationThread.descriptor);
                unstableProviderDied(data.readStrongBinder());
                reply.writeNoException();
                return true;
            case 48:
                data.enforceInterface(IApplicationThread.descriptor);
                requestAssistContextExtras(data.readStrongBinder(), data.readStrongBinder(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case 49:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleTranslucentConversionComplete(data.readStrongBinder(), data.readInt() == 1);
                reply.writeNoException();
                return true;
            case 50:
                data.enforceInterface(IApplicationThread.descriptor);
                setProcessState(data.readInt());
                reply.writeNoException();
                return true;
            case 51:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleInstallProvider((ProviderInfo) ProviderInfo.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case 52:
                data.enforceInterface(IApplicationThread.descriptor);
                updateTimePrefs(data.readByte() == (byte) 1);
                reply.writeNoException();
                return true;
            case 53:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCancelVisibleBehind(data.readStrongBinder());
                reply.writeNoException();
                return true;
            case 54:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleBackgroundVisibleBehindChanged(data.readStrongBinder(), data.readInt() > 0);
                reply.writeNoException();
                return true;
            case 55:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleEnterAnimationComplete(data.readStrongBinder());
                reply.writeNoException();
                return true;
            case 56:
                data.enforceInterface(IApplicationThread.descriptor);
                notifyCleartextNetwork(data.createByteArray());
                reply.writeNoException();
                return true;
            case 57:
                data.enforceInterface(IApplicationThread.descriptor);
                startBinderTracking();
                return true;
            case 58:
                data.enforceInterface(IApplicationThread.descriptor);
                fd = data.readFileDescriptor();
                if (fd != null) {
                    stopBinderTrackingAndDump(fd.getFileDescriptor());
                    try {
                        fd.close();
                    } catch (IOException e10) {
                    }
                }
                return true;
            case 59:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleMultiWindowModeChanged(data.readStrongBinder(), data.readInt() != 0);
                return true;
            case 60:
                data.enforceInterface(IApplicationThread.descriptor);
                schedulePictureInPictureModeChanged(data.readStrongBinder(), data.readInt() != 0);
                return true;
            case 61:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleLocalVoiceInteractionStarted(data.readStrongBinder(), Stub.asInterface(data.readStrongBinder()));
                return true;
            case 102:
                dumpMessageHistory();
                return true;
            case 103:
                enableLooperLog();
                return true;
            case 107:
                data.enforceInterface(IApplicationThread.descriptor);
                configActivityLogTag(data.readString(), data.readInt() != 0);
                return true;
            case 401:
                data.enforceInterface(IApplicationThread.descriptor);
                int state = getBroadcastState(data.readInt());
                reply.writeNoException();
                reply.writeInt(state);
                return true;
            case 402:
                data.enforceInterface(IApplicationThread.descriptor);
                openActivityLog(data.readInt() != 0);
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public IBinder asBinder() {
        return this;
    }
}
