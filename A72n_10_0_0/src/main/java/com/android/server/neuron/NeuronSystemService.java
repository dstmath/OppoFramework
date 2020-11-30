package com.android.server.neuron;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.oppo.neuron.INeuronSystemEventListener;
import com.oppo.neuron.INeuronSystemService;
import com.oppo.neuron.NeuronSystemManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class NeuronSystemService extends INeuronSystemService.Stub {
    public static final long ONE_DAYS_MS = 86400000;
    private static final int PUSH_INTO_QUEUE = 1;
    public static final String TAG = "NeuronSystem";
    private final boolean LOG_ON = SystemProperties.getBoolean("persist.vendor.ns_logon", false);
    private final double NS_VERSION = 1.0d;
    private ActivityManagerService mAms = null;
    private AppUsage mAppUsage = null;
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.neuron.NeuronSystemService.AnonymousClass2 */

        public void binderDied() {
            synchronized (NeuronSystemService.this) {
                NeuronSystemService.this.mListener = null;
            }
        }
    };
    private INeuronSystemEventListener mListener = null;
    private Handler mNsHandler;
    private HandlerThread mNsHandlerThread;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.neuron.NeuronSystemService.AnonymousClass3 */

        public void onReceive(Context c, Intent intent) {
            if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("date", Long.valueOf(System.currentTimeMillis()));
                NeuronSystemService.this.notifyEventToListener(104, contentValues);
            }
        }
    };
    private boolean mSystemBooted = false;

    public NeuronSystemService(Context context) {
        this.mContext = context;
    }

    public void publish(ActivityManagerService ams) {
        ServiceManager.addService("neuronsystem", asBinder());
        this.mAms = ams;
        this.mNsHandlerThread = new HandlerThread("ns");
        this.mNsHandlerThread.start();
        initNsPushWorker();
        this.mAppUsage = new AppUsage();
        Log.d("NeuronSystem", "neuronsystem published");
    }

    private boolean isPublishEnable() {
        return ((NeuronSystemManager.sNsProp & 1) == 0 || (NeuronSystemManager.sNsProp & 8) == 0) ? false : true;
    }

    public void enableRecommendedApps(boolean enable, List<String> pkgs) {
        this.mAppUsage.enableRecommendedApps(enable, pkgs);
    }

    public List<String> getRecommendedApps(int topK) {
        if ((Binder.getCallingUid() > 10000 && this.mContext.checkCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE") != 0) || topK <= 0) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> resolvies = this.mContext.getPackageManager().queryIntentActivities(intent, 0);
        Set<String> iconApp = new TreeSet<>();
        for (ResolveInfo resolve : resolvies) {
            if (!(resolve.activityInfo == null || resolve.activityInfo.packageName == null)) {
                iconApp.add(resolve.activityInfo.packageName);
            }
        }
        Slog.d("NeuronSystem", "iconApp size" + iconApp.size() + " topK " + topK);
        List<String> result = new ArrayList<>();
        List<String> apps = this.mAppUsage.appPreloadPredict();
        if (apps == null || apps.size() == 0) {
            Slog.d("NeuronSystem", "PredictionTask get empty result");
        } else {
            for (String app : apps) {
                if (iconApp.contains(app)) {
                    Slog.d("NeuronSystem", "getRecommendedApps result1 add " + app);
                    result.add(app);
                    if (result.size() == topK) {
                        return result;
                    }
                }
            }
        }
        if (result.size() < topK) {
            Object[] iconAppArray = iconApp.toArray();
            int i = 0;
            while (result.size() < topK && i < iconAppArray.length) {
                int i2 = i + 1;
                String app2 = (String) iconAppArray[i];
                if (!result.contains(app2)) {
                    result.add(app2);
                }
                i = i2;
            }
        }
        return result;
    }

    private void initNsPushWorker() {
        this.mNsHandler = new Handler(this.mNsHandlerThread.getLooper()) {
            /* class com.android.server.neuron.NeuronSystemService.AnonymousClass1 */

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    int eventType = msg.arg1;
                    ContentValues contentValues = (ContentValues) msg.obj;
                    if (eventType == 101) {
                        NeuronSystemService.this.mAppUsage.onAppForeground(contentValues.getAsString("pkgname"));
                    }
                    NeuronSystemService.this.notifyEventToListener(eventType, contentValues);
                }
            }
        };
    }

    public void systemReady() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mSystemBooted = true;
    }

    public void shutdown() {
    }

    public void publishEvent(int eventType, ContentValues contentValues) {
        Handler handler = this.mNsHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage(1, contentValues);
            msg.arg1 = eventType;
            msg.arg2 = 0;
            msg.sendToTarget();
        }
        if (NeuronSystemManager.LOG_ON) {
            Log.d("NeuronSystem", contentValues.toString());
        }
    }

    public synchronized void registerEventListener(INeuronSystemEventListener listener) {
        if (listener != null) {
            if (Binder.getCallingUid() <= 10000 || this.mContext.checkCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE") == 0) {
                this.mListener = listener;
                try {
                    this.mListener.asBinder().linkToDeath(this.mDeathRecipient, 0);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void notifyEventToListener(int eventType, ContentValues contentValues) {
        if (this.mListener != null && eventType >= 101 && eventType <= 199) {
            try {
                this.mListener.onEvent(eventType, contentValues);
            } catch (Exception e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length != 0) {
        }
    }
}
