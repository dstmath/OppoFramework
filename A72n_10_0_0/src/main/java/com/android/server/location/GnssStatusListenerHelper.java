package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.IGnssStatusListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.PswServiceFactory;
import com.android.server.location.RemoteListenerHelper;
import com.android.server.location.interfaces.IPswLbsRomUpdateUtil;
import java.util.ArrayList;

public abstract class GnssStatusListenerHelper extends RemoteListenerHelper<IGnssStatusListener> {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String NMEA_DISPATCH_OPT_KEY = "config_nmeaDispatchOptimizationEnable";
    private static final String OPPO_LBS_CONFIG_UPDATE_ACTION = "com.android.location.oppo.lbsconfig.update.success";
    private static final int PERMISSION_CHECK_PERIOD = 1000000000;
    private static final String TAG = "GnssStatusListenerHelper";
    private Context mContext;
    private long mLastCheckTime = 0;
    private final Object mLock = new Object();
    private boolean mNmeaDispatchOptimizationFlag = true;
    private IPswLbsRomUpdateUtil mOppoLbsRomUpdateUtil = null;
    private final ArrayList<IBinder> mPermissionAllowedList = new ArrayList<>();
    private BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.GnssStatusListenerHelper.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (GnssStatusListenerHelper.OPPO_LBS_CONFIG_UPDATE_ACTION.equals(intent.getAction())) {
                if (GnssStatusListenerHelper.this.mOppoLbsRomUpdateUtil == null) {
                    GnssStatusListenerHelper.this.mOppoLbsRomUpdateUtil = PswServiceFactory.getInstance().getFeature(IPswLbsRomUpdateUtil.DEFAULT, new Object[]{GnssStatusListenerHelper.this.mContext});
                }
                if (GnssStatusListenerHelper.this.mOppoLbsRomUpdateUtil != null) {
                    GnssStatusListenerHelper gnssStatusListenerHelper = GnssStatusListenerHelper.this;
                    gnssStatusListenerHelper.setNmeaDispatchOptFlagLocked(gnssStatusListenerHelper.mOppoLbsRomUpdateUtil.getBoolean(GnssStatusListenerHelper.NMEA_DISPATCH_OPT_KEY));
                }
            }
        }
    };

    protected GnssStatusListenerHelper(Context context, Handler handler) {
        super(context, handler, TAG);
        setSupported(GnssLocationProvider.isSupported());
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OPPO_LBS_CONFIG_UPDATE_ACTION);
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", handler);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public int registerWithService() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public void unregisterFromService() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public RemoteListenerHelper.ListenerOperation<IGnssStatusListener> getHandlerOperation(int result) {
        return null;
    }

    public void onStatusChanged(boolean isNavigating) {
        if (isNavigating) {
            foreach($$Lambda$GnssStatusListenerHelper$H9Tg_OtCE9BSJiAQYs_ITHFpiHU.INSTANCE);
        } else {
            foreach($$Lambda$GnssStatusListenerHelper$6s2HBSMgP5pXrugfCvtIf9QHndI.INSTANCE);
        }
    }

    public void onFirstFix(int timeToFirstFix) {
        foreach(new RemoteListenerHelper.ListenerOperation(timeToFirstFix) {
            /* class com.android.server.location.$$Lambda$GnssStatusListenerHelper$0MNjUouf1HJVcFD10rzoJIkzCrw */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
            public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                ((IGnssStatusListener) iInterface).onFirstFix(this.f$0);
            }
        });
    }

    public void onSvStatusChanged(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) {
        foreach(new RemoteListenerHelper.ListenerOperation(svCount, prnWithFlags, cn0s, elevations, azimuths, carrierFreqs) {
            /* class com.android.server.location.$$Lambda$GnssStatusListenerHelper$68FOYPQxCAVSdtoWmmZNfYGGIJE */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int[] f$2;
            private final /* synthetic */ float[] f$3;
            private final /* synthetic */ float[] f$4;
            private final /* synthetic */ float[] f$5;
            private final /* synthetic */ float[] f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
            public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                GnssStatusListenerHelper.this.lambda$onSvStatusChanged$3$GnssStatusListenerHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, (IGnssStatusListener) iInterface, callerIdentity);
            }
        });
    }

    public /* synthetic */ void lambda$onSvStatusChanged$3$GnssStatusListenerHelper(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs, IGnssStatusListener listener, CallerIdentity callerIdentity) throws RemoteException {
        if (!hasPermission(this.mContext, callerIdentity)) {
            logPermissionDisabledEventNotReported(TAG, callerIdentity.mPackageName, "GNSS status");
        } else {
            listener.onSvStatusChanged(svCount, prnWithFlags, cn0s, elevations, azimuths, carrierFreqs);
        }
    }

    public void onNmeaReceived(long timestamp, String nmea) {
        if (this.mOppoLbsRomUpdateUtil == null) {
            this.mOppoLbsRomUpdateUtil = PswServiceFactory.getInstance().getFeature(IPswLbsRomUpdateUtil.DEFAULT, new Object[]{this.mContext});
            IPswLbsRomUpdateUtil iPswLbsRomUpdateUtil = this.mOppoLbsRomUpdateUtil;
            if (iPswLbsRomUpdateUtil != null) {
                setNmeaDispatchOptFlagLocked(iPswLbsRomUpdateUtil.getBoolean(NMEA_DISPATCH_OPT_KEY));
            }
        }
        if (getNmeaDispatchOptFlagLocked()) {
            foreachNmea(new RemoteListenerHelper.ListenerOperation(timestamp, nmea) {
                /* class com.android.server.location.$$Lambda$GnssStatusListenerHelper$AtHI8E6PAjonHH1N0ZGabW0VF6c */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                }

                @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
                public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                    GnssStatusListenerHelper.this.lambda$onNmeaReceived$4$GnssStatusListenerHelper(this.f$1, this.f$2, (IGnssStatusListener) iInterface, callerIdentity);
                }
            });
        } else {
            foreach(new RemoteListenerHelper.ListenerOperation(timestamp, nmea) {
                /* class com.android.server.location.$$Lambda$GnssStatusListenerHelper$dTRNrotpfcQCwcPOcuYMY2WIPL8 */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                }

                @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
                public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                    GnssStatusListenerHelper.this.lambda$onNmeaReceived$5$GnssStatusListenerHelper(this.f$1, this.f$2, (IGnssStatusListener) iInterface, callerIdentity);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onNmeaReceived$4$GnssStatusListenerHelper(long timestamp, String nmea, IGnssStatusListener listener, CallerIdentity callerIdentity) throws RemoteException {
        if (!hasPermission(this.mContext, callerIdentity)) {
            logPermissionDisabledEventNotReported(TAG, callerIdentity.mPackageName, "NMEA");
        } else {
            listener.onNmeaReceived(timestamp, nmea);
        }
    }

    public /* synthetic */ void lambda$onNmeaReceived$5$GnssStatusListenerHelper(long timestamp, String nmea, IGnssStatusListener listener, CallerIdentity callerIdentity) throws RemoteException {
        if (!hasPermission(this.mContext, callerIdentity)) {
            logPermissionDisabledEventNotReported(TAG, callerIdentity.mPackageName, "NMEA");
        } else {
            listener.onNmeaReceived(timestamp, nmea);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNmeaDispatchOptFlagLocked(boolean nmeaDispatchOptFlag) {
        synchronized (this.mLock) {
            this.mNmeaDispatchOptimizationFlag = nmeaDispatchOptFlag;
        }
    }

    private boolean getNmeaDispatchOptFlagLocked() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mNmeaDispatchOptimizationFlag;
        }
        return z;
    }
}
