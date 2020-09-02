package com.mediatek.location;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.mediatek.cta.CtaManager;
import com.mediatek.cta.CtaManagerFactory;
import com.mediatek.internal.R;
import java.util.Calendar;

public class MtkLocationExt {
    private static final boolean DEBUG = true;
    private static final String TAG = "MtkLocationExt";

    public static class GnssLocationProvider {
        private static final int EVENT_GPS_TIME_SYNC_CHANGED = 4;
        private static final int UPDATE_LOCATION = 7;
        /* access modifiers changed from: private */
        public final Context mContext;
        private Handler mGpsHandler;
        private GpsTimeSyncObserver mGpsTimeSyncObserver;
        /* access modifiers changed from: private */
        public Thread mGpsTimerThread;
        /* access modifiers changed from: private */
        public Handler mGpsToastHandler = new Handler() {
            /* class com.mediatek.location.MtkLocationExt.GnssLocationProvider.AnonymousClass4 */

            public void handleMessage(Message msg) {
                Toast.makeText(GnssLocationProvider.this.mContext, (String) msg.obj, 1).show();
            }
        };
        private final Handler mHandler;
        /* access modifiers changed from: private */
        public boolean mIsGpsTimeSyncRunning = false;
        private Location mLastLocation;
        /* access modifiers changed from: private */
        public LocationListener mLocationListener = new LocationListener() {
            /* class com.mediatek.location.MtkLocationExt.GnssLocationProvider.AnonymousClass5 */

            public void onLocationChanged(Location location) {
                GnssLocationProvider.this.mGpsTimerThread.interrupt();
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        /* access modifiers changed from: private */
        public LocationManager mLocationManager;
        private LocationListener mPassiveLocationListener = new LocationListener() {
            /* class com.mediatek.location.MtkLocationExt.GnssLocationProvider.AnonymousClass2 */

            public void onLocationChanged(Location location) {
                if ("gps".equals(location.getProvider())) {
                    GnssLocationProvider.this.doSystemTimeSyncByGps((location.getLatitude() == 0.0d || location.getLongitude() == 0.0d) ? false : MtkLocationExt.DEBUG, location.getTime());
                }
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        public GnssLocationProvider(Context context, Handler handler) {
            Log.d(MtkLocationExt.TAG, "MtkLocationExt GnssLocationProvider()");
            this.mContext = context;
            this.mHandler = handler;
            registerIntentReceiver();
            Log.d(MtkLocationExt.TAG, "add GPS time sync handler and looper");
            this.mGpsHandler = new MyHandler(this.mHandler.getLooper());
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            this.mGpsTimeSyncObserver = new GpsTimeSyncObserver(this.mGpsHandler, 4);
            this.mGpsTimeSyncObserver.observe(this.mContext);
        }

        private void launchLPPeService() {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.mediatek.location.lppe.main", "com.mediatek.location.lppe.main.LPPeServiceWrapper"));
            this.mContext.startService(intent);
        }

        private void registerIntentReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
                /* class com.mediatek.location.MtkLocationExt.GnssLocationProvider.AnonymousClass1 */

                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                        boolean gpsTimeSyncStatus = GnssLocationProvider.this.getGpsTimeSyncState();
                        Log.d(MtkLocationExt.TAG, "GPS Time sync is set to " + gpsTimeSyncStatus);
                        GnssLocationProvider.this.setGpsTimeSyncFlag(gpsTimeSyncStatus);
                        Log.d(MtkLocationExt.TAG, "Skip luaunch lppe service");
                    }
                }
            }, UserHandle.ALL, intentFilter, null, this.mHandler);
        }

        private class MyHandler extends Handler {
            public MyHandler(Looper l) {
                super(l);
            }

            public void handleMessage(Message msg) {
                if (msg.what == 4) {
                    boolean gpsTimeSyncStatus = GnssLocationProvider.this.getGpsTimeSyncState();
                    Log.d(MtkLocationExt.TAG, "GPS Time sync is changed to " + gpsTimeSyncStatus);
                    GnssLocationProvider.this.onGpsTimeChanged(gpsTimeSyncStatus);
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean getGpsTimeSyncState() {
            try {
                if (Settings.Global.getInt(this.mContext.getContentResolver(), "auto_time_gps") > 0) {
                    return MtkLocationExt.DEBUG;
                }
                return false;
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }
        }

        private static class GpsTimeSyncObserver extends ContentObserver {
            private Handler mHandler;
            private int mMsg;

            GpsTimeSyncObserver(Handler handler, int msg) {
                super(handler);
                this.mHandler = handler;
                this.mMsg = msg;
            }

            /* access modifiers changed from: package-private */
            public void observe(Context context) {
                context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("auto_time_gps"), false, this);
            }

            public void onChange(boolean selfChange) {
                this.mHandler.obtainMessage(this.mMsg).sendToTarget();
            }
        }

        public void onGpsTimeChanged(boolean enable) {
            if (enable) {
                startUsingGpsWithTimeout(180000, this.mContext.getString(R.string.gps_time_sync_fail_str));
            } else {
                Thread thread = this.mGpsTimerThread;
                if (thread != null) {
                    thread.interrupt();
                }
            }
            setGpsTimeSyncFlag(enable);
        }

        /* access modifiers changed from: private */
        public void setGpsTimeSyncFlag(boolean flag) {
            Log.d(MtkLocationExt.TAG, "setGpsTimeSyncFlag: " + flag);
            if (flag) {
                this.mLocationManager.requestLocationUpdates("passive", 0, 0.0f, this.mPassiveLocationListener);
            } else {
                this.mLocationManager.removeUpdates(this.mPassiveLocationListener);
            }
        }

        /* access modifiers changed from: private */
        public void doSystemTimeSyncByGps(boolean hasLatLong, long timestamp) {
            if (hasLatLong) {
                Log.d(MtkLocationExt.TAG, " ########## Auto-sync time with GPS: timestamp = " + timestamp + " ########## ");
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(timestamp);
                long when = c.getTimeInMillis();
                if (when / 1000 < 2147483647L) {
                    SystemClock.setCurrentTimeMillis(when);
                }
                this.mLocationManager.removeUpdates(this.mPassiveLocationListener);
            }
        }

        public void startUsingGpsWithTimeout(final int milliseconds, final String timeoutMsg) {
            if (this.mIsGpsTimeSyncRunning) {
                Log.d(MtkLocationExt.TAG, "WARNING: Gps Time Sync is already run");
                return;
            }
            this.mIsGpsTimeSyncRunning = MtkLocationExt.DEBUG;
            Log.d(MtkLocationExt.TAG, "start using GPS for GPS time sync timeout=" + milliseconds + " timeoutMsg=" + timeoutMsg);
            this.mLocationManager.requestLocationUpdates("gps", 1000, 0.0f, this.mLocationListener);
            this.mGpsTimerThread = new Thread() {
                /* class com.mediatek.location.MtkLocationExt.GnssLocationProvider.AnonymousClass3 */

                public void run() {
                    boolean isTimeout = false;
                    try {
                        Thread.sleep((long) milliseconds);
                        isTimeout = MtkLocationExt.DEBUG;
                    } catch (InterruptedException e) {
                    }
                    Log.d(MtkLocationExt.TAG, "isTimeout=" + isTimeout);
                    if (isTimeout) {
                        Message m = new Message();
                        m.obj = timeoutMsg;
                        GnssLocationProvider.this.mGpsToastHandler.sendMessage(m);
                    }
                    GnssLocationProvider.this.mLocationManager.removeUpdates(GnssLocationProvider.this.mLocationListener);
                    boolean unused = GnssLocationProvider.this.mIsGpsTimeSyncRunning = false;
                }
            };
            this.mGpsTimerThread.start();
        }
    }

    public static class LocationManagerService {
        private final Context mContext;
        private CtaManager mCtaManager = CtaManagerFactory.getInstance().makeCtaManager();
        private final Handler mHandler;
        private LocationManager mLocationManager = ((LocationManager) this.mContext.getSystemService("location"));

        public LocationManagerService(Context context, Handler handler) {
            Log.d(MtkLocationExt.TAG, "MtkLocationExt LocationManagerService()");
            this.mContext = context;
            this.mHandler = handler;
        }

        public boolean isCtaFeatureSupport() {
            return this.mCtaManager.isCtaSupported();
        }

        public void printCtaLog(int callingPid, int callingUid, String functionName, String strActionType, String parameter) {
            CtaManager.ActionType actionType = CtaManager.ActionType.USE_LOCATION;
            if ("USE_LOCATION".equals(strActionType)) {
                actionType = CtaManager.ActionType.USE_LOCATION;
            } else if ("READ_LOCATION_INFO".equals(strActionType)) {
                actionType = CtaManager.ActionType.READ_LOCATION_INFO;
            }
            this.mCtaManager.printCtaInfor(callingPid, callingUid, CtaManager.KeywordType.LOCATION, functionName, actionType, parameter);
        }

        public void showNlpNotInstalledToast(String provider) {
            try {
                Log.d(MtkLocationExt.TAG, "showNlpNotInstalledToast provider: " + provider);
                if ("network".equals(provider)) {
                    Toast.makeText(this.mContext, "No Network Location Provider is installed!NLP is necessary for network location fixes.", 1).show();
                }
            } catch (Exception e) {
                Log.w(MtkLocationExt.TAG, "Failed to show toast ", e);
            }
        }
    }
}
