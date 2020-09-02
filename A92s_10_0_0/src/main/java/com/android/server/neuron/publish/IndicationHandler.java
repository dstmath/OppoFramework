package com.android.server.neuron.publish;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.neuron.publish.Channel;
import com.android.server.neuron.publish.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class IndicationHandler {
    private static final String TAG = "NeuronSystem";
    private Context mContext;
    private List<String> mIconApp = null;
    private Channel.RequestSender mSender;

    public IndicationHandler(Context context, Channel.RequestSender sender) {
        this.mContext = context;
        this.mSender = sender;
    }

    public void handle(Response.NativeIndication indication) {
        int i = indication.command;
        switch (i) {
            case 101:
                handleGetBackgroundApp();
                return;
            case 102:
                return;
            case 103:
                handleGetInstalledApp();
                return;
            case 104:
                handleGetRecentApp();
                return;
            default:
                switch (i) {
                    case ProtocolConstants.UNSOL_SET_RSSI_UPDATE_FREQ:
                        handleRssiUpdateFreq(indication.arg1);
                        return;
                    case ProtocolConstants.UNSOL_SET_GPS_UPDATE_FREQ:
                        handleGpsUpdateFreq(indication.arg1);
                        return;
                    case ProtocolConstants.UNSOL_SET_SENSOR_UPDATE_FREQ:
                        handleSensorUpdateFreq(indication.arg1);
                        return;
                    default:
                        Slog.e("NeuronSystem", "IndicationHandler handle unknown command: " + indication.command);
                        return;
                }
        }
    }

    private void handleGetBackgroundApp() {
        List<ActivityManager.RunningAppProcessInfo> lists;
        getAllInstalledWithIconApp();
        ActivityManager manager = (ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
        if (manager != null && (lists = manager.getRunningAppProcesses()) != null && lists.size() != 0) {
            removeNonIconApp(lists);
            removeForegroundApp(lists);
            Request req = Request.obtain();
            Parcel parcel = req.prepare();
            parcel.writeInt(2);
            parcel.writeInt(lists.size());
            for (ActivityManager.RunningAppProcessInfo info : lists) {
                parcel.writeString(info.pkgList[0]);
                parcel.writeInt(0);
                parcel.writeString(ProtocolConstants.DEFAULT_VERSION);
                parcel.writeInt(info.uid);
                parcel.writeInt(info.pid);
            }
            req.commit();
            this.mSender.sendRequest(req);
        }
    }

    private void handleGetInstalledApp() {
        getAllInstalledWithIconApp();
        Request req = Request.obtain();
        Parcel parcel = req.prepare();
        parcel.writeInt(13);
        parcel.writeInt(2);
        parcel.writeInt(this.mIconApp.size());
        for (String appName : this.mIconApp) {
            parcel.writeString(appName);
        }
        req.commit();
        this.mSender.sendRequest(req);
    }

    private void handleGetRecentApp() {
        List<ActivityManager.RunningAppProcessInfo> lists;
        ActivityManager manager = (ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
        if (manager != null && (lists = manager.getRunningAppProcesses()) != null && lists.size() != 0) {
            removeNonIconApp(lists);
            try {
                List<ActivityManager.RecentTaskInfo> tasks = ActivityManagerNative.getDefault().getRecentTasks(32, 0, 0).getList();
                ArrayList<ActivityManager.RunningAppProcessInfo> runningRecentApp = new ArrayList<>();
                for (ActivityManager.RecentTaskInfo task : tasks) {
                    String pkg = getTaskPackageName(task);
                    if (pkg != null) {
                        for (ActivityManager.RunningAppProcessInfo info : lists) {
                            if (info.pkgList[0].equals(pkg)) {
                                runningRecentApp.add(info);
                            }
                        }
                    }
                }
                if (runningRecentApp.size() > 0) {
                    runningRecentApp.remove(0);
                }
                if (runningRecentApp.size() > 0) {
                    Request req = Request.obtain();
                    Parcel parcel = req.prepare();
                    parcel.writeInt(14);
                    parcel.writeInt(runningRecentApp.size());
                    Iterator<ActivityManager.RunningAppProcessInfo> it = runningRecentApp.iterator();
                    while (it.hasNext()) {
                        ActivityManager.RunningAppProcessInfo info2 = it.next();
                        parcel.writeString(info2.pkgList[0]);
                        parcel.writeInt(0);
                        parcel.writeString(ProtocolConstants.DEFAULT_VERSION);
                        parcel.writeInt(info2.uid);
                        parcel.writeInt(info2.pid);
                    }
                    req.commit();
                    this.mSender.sendRequest(req);
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void handleRssiUpdateFreq(int freq) {
        NeuronContext.getNeoConfig().setRssiUpdatePeriod(freq);
    }

    private void handleGpsUpdateFreq(int freq) {
        NeuronContext.getNeoConfig().setGpsUpdatePeriod(freq);
    }

    private void handleSensorUpdateFreq(int freq) {
        NeuronContext.getNeoConfig().setSensorUpdatePeriod(freq);
    }

    private void handleElsaModeUpdate(int mode) {
        Slog.d("NeuronSystem", "handleElsaModeUpdate, mode:" + mode);
    }

    private void getAllInstalledWithIconApp() {
        if (this.mIconApp == null) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            List<ResolveInfo> resolvies = this.mContext.getPackageManager().queryIntentActivities(intent, 0);
            this.mIconApp = new ArrayList(resolvies.size());
            for (ResolveInfo resolve : resolvies) {
                if (!(resolve.activityInfo == null || resolve.activityInfo.packageName == null)) {
                    this.mIconApp.add(resolve.activityInfo.packageName);
                }
            }
        }
    }

    private void removeNonIconApp(List<ActivityManager.RunningAppProcessInfo> infos) {
        Iterator<ActivityManager.RunningAppProcessInfo> it = infos.iterator();
        while (it.hasNext()) {
            boolean hasIcon = false;
            String[] strArr = it.next().pkgList;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (this.mIconApp.contains(strArr[i])) {
                    hasIcon = true;
                    break;
                }
                i++;
            }
            if (!hasIcon) {
                it.remove();
            }
        }
    }

    private void removeForegroundApp(List<ActivityManager.RunningAppProcessInfo> infos) {
        String foregroundApp = NeuronContext.getSystemStatus().getForegroundApp();
        Iterator<ActivityManager.RunningAppProcessInfo> it = infos.iterator();
        while (it.hasNext()) {
            String[] strArr = it.next().pkgList;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i < length) {
                    if (foregroundApp.equals(strArr[i])) {
                        it.remove();
                        return;
                    }
                    i++;
                }
            }
        }
    }

    private String getTaskPackageName(ActivityManager.RecentTaskInfo task) {
        if (task.id <= 0) {
            return null;
        }
        if (task.origActivity != null) {
            return task.origActivity.getPackageName();
        }
        if (task.realActivity != null) {
            return task.realActivity.getPackageName();
        }
        if (task.baseActivity != null) {
            return task.baseActivity.getPackageName();
        }
        if (task.topActivity != null) {
            return task.topActivity.getPackageName();
        }
        return null;
    }
}
