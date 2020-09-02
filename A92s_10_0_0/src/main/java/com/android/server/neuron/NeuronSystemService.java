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
import com.android.server.neuron.publish.Publisher;
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
    /* access modifiers changed from: private */
    public AppUsage mAppUsage = null;
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.neuron.NeuronSystemService.AnonymousClass2 */

        public void binderDied() {
            synchronized (NeuronSystemService.this) {
                INeuronSystemEventListener unused = NeuronSystemService.this.mListener = null;
            }
        }
    };
    /* access modifiers changed from: private */
    public INeuronSystemEventListener mListener = null;
    private Handler mNsHandler;
    private HandlerThread mNsHandlerThread;
    private ProtectedAppUtils mProtectedAppUtils;
    /* access modifiers changed from: private */
    public Publisher mPublisher = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.neuron.NeuronSystemService.AnonymousClass3 */

        public void onReceive(Context c, Intent intent) {
            if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("date", Long.valueOf(System.currentTimeMillis()));
                NeuronSystemService.this.notifyEventToListener(16, contentValues);
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
        if (isPublishEnable()) {
            this.mPublisher = new Publisher(this.mContext, this.mNsHandlerThread);
        }
        initNsPushWorker();
        this.mAppUsage = new AppUsage();
        Log.d("NeuronSystem", "neuronsystem published");
    }

    /* access modifiers changed from: private */
    public boolean isPublishEnable() {
        return ((NeuronSystemManager.sNsProp & 1) == 0 || (NeuronSystemManager.sNsProp & 8) == 0) ? false : true;
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
        String[] apps = this.mAppUsage.appPreloadPredict();
        if (apps == null || apps.length == 0) {
            Slog.d("NeuronSystem", "PredictionTask get empty result");
        } else {
            for (String app : apps) {
                if (iconApp.contains(app) && !result.contains(app)) {
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
                    Slog.d("NeuronSystem", "getRecommendedApps result add " + app2);
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
                    if (NeuronSystemService.this.isPublishEnable() && NeuronSystemService.this.mPublisher != null) {
                        NeuronSystemService.this.mPublisher.publishEvent(eventType, contentValues);
                    }
                    if (eventType == 1) {
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
        this.mProtectedAppUtils = new ProtectedAppUtils(this.mContext, this.mNsHandler);
        this.mSystemBooted = true;
    }

    public void shutdown() {
    }

    public void publishEvent(int eventType, ContentValues contentValues) {
        if (Binder.getCallingUid() <= 10000 || this.mContext.checkCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE") == 0) {
            long time0 = System.currentTimeMillis();
            Handler handler = this.mNsHandler;
            if (handler != null) {
                Message msg = handler.obtainMessage(1, contentValues);
                msg.arg1 = eventType;
                msg.arg2 = 0;
                msg.sendToTarget();
            }
            if (NeuronSystemManager.LOG_ON) {
                Log.d("NeuronSystem", contentValues.toString());
                long time = System.currentTimeMillis() - time0;
                if (time > 1) {
                    Log.w("NeuronSystem", "warning: push into queue" + eventType + " cost " + time + "ms");
                }
            }
        }
    }

    public synchronized void registerEventListener(INeuronSystemEventListener listener) {
        if (Binder.getCallingUid() <= 10000 || this.mContext.checkCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE") == 0) {
            this.mListener = listener;
            try {
                this.mListener.asBinder().linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public synchronized void notifyEventToListener(int eventType, ContentValues contentValues) {
        if (this.mListener != null && (eventType == 1 || eventType == 6 || eventType == 11 || eventType == 16 || eventType == 17 || eventType == 18 || eventType == 12 || eventType == 19)) {
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
