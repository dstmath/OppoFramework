package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.display.OppoBrightUtils;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class GpsController {
    private static final String ACTION_BIND_POWER_SERVICE = "com.gnss.power.service";
    private static final String ACTION_POWER_SAVER_NOTIFICATION = "com.oppo.power.saver.notification";
    private static final String ACTION_START_NOTIFICATION = "com.oppo.start.notification";
    private static final String ACTION_STOP_NAVIGATING_NOTIFICATION = "com.oppo.stop.navigating.notification";
    private static final String ACTION_USER_AGREE = "com.oppo.user.agree";
    private static final String ACTION_USER_DISCARD = "com.oppo.user.discard";
    private static final String ACTION_USER_WAKE_GPS = "com.oppo.user.wake.gps";
    private static boolean DEBUG = false;
    private static final String GPS_OPCUSTOM_POWERSAVE_OFF_FEATURE = "oppo.customize.function.gps_powersave_off";
    private static final int GPS_POWER_INTERVAL = 1000;
    private static final int GPS_POWER_ON = 0;
    private static final int GPS_POWER_ON_WITH_DISCARD = 2;
    private static final int GPS_POWER_ON_WITH_WAKE = 1;
    private static final int GPS_POWER_SAVER_WITH_AGREE = 4;
    private static final int GPS_POWER_SAVER_WITH_DELAY = 3;
    private static final int MSG_CONTROL_RUNNING = 104;
    private static final int MSG_CONTROL_START = 103;
    private static final int MSG_CONTROL_STOP = 105;
    private static final int MSG_STOP_AFTER_DELAY = 102;
    private static final int MSG_WILL_STOP_GPS = 101;
    private static final String PASSIVE_PROVIDER = "passive";
    private static final String SERVICE_PACKAGE_NAME = "com.gnss.power";
    private static final String TAG = "GpsController";
    private static final long TIME_DELAY_STOP_GPS = 60000;
    private final Context mContext;
    private final GnssLocationProvider mGlp;
    private int mGpsMode;
    private final GpsMonitor mGpsMonitor;
    private boolean mGpsPowerSaveFeatureDisable;
    private final ILocationManager mILocationManager;
    private boolean mIsDoingStop;
    private final LocationManager mLocMgr;
    private boolean mNeedRebind;
    private boolean mNeedStopGps;
    private ServiceConnection myConnection;
    private Handler myHander;
    private LocationListener myLocationListener;
    private BroadcastReceiver myReceiver;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.location.GpsController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.location.GpsController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsController.<clinit>():void");
    }

    public GpsController(Context context, GnssLocationProvider glp, ILocationManager iLocationManager) {
        this.mNeedStopGps = false;
        this.mIsDoingStop = false;
        this.mNeedRebind = true;
        this.mGpsPowerSaveFeatureDisable = false;
        this.myHander = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        GpsController.this.willStopGps();
                        return;
                    case 102:
                        if (GpsController.this.mNeedStopGps) {
                            GpsController.this.mGpsMode = 3;
                            GpsController.this.enterPowerSavingMode();
                            return;
                        }
                        return;
                    case 103:
                        sendEmptyMessage(104);
                        return;
                    case 104:
                        GpsController.this.changeGpsMode(GpsController.this.mGlp.getSpeed(), GpsController.this.mGlp.getCn0s());
                        sendEmptyMessageDelayed(104, 1000);
                        return;
                    case 105:
                        removeMessages(104);
                        GpsController.this.mIsDoingStop = false;
                        removeMessages(102);
                        return;
                    default:
                        return;
                }
            }
        };
        this.myConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (GpsController.DEBUG) {
                    Log.d(GpsController.TAG, "GnssPowerSaver notification service is connected!!");
                }
                GpsController.this.mNeedRebind = false;
            }

            public void onServiceDisconnected(ComponentName name) {
                if (GpsController.DEBUG) {
                    Log.d(GpsController.TAG, "GnssPowerSaver notification service is disconnected!!");
                }
                GpsController.this.mNeedRebind = true;
            }
        };
        this.myReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(GpsController.ACTION_USER_WAKE_GPS)) {
                    GpsController.this.wakeGps();
                } else if (action.equals(GpsController.ACTION_USER_AGREE)) {
                    GpsController.this.userAgreed();
                } else if (action.equals(GpsController.ACTION_USER_DISCARD)) {
                    GpsController.this.userDiscard();
                }
            }
        };
        this.myLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                location.setProvider("gps");
                try {
                    GpsController.this.mILocationManager.reportLocation(location, false);
                } catch (RemoteException e) {
                    Log.e(GpsController.TAG, "RemoteException reporting location!!!");
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        this.mContext = context;
        this.mGpsMonitor = new GpsMonitor();
        this.mGlp = glp;
        this.mILocationManager = iLocationManager;
        this.mLocMgr = (LocationManager) this.mContext.getSystemService("location");
        registBroadcast();
        this.mGpsPowerSaveFeatureDisable = this.mContext.getPackageManager().hasSystemFeature(GPS_OPCUSTOM_POWERSAVE_OFF_FEATURE);
    }

    public void startController() {
        if (DEBUG) {
            Log.d(TAG, "----startController----");
        }
        if (this.mGpsPowerSaveFeatureDisable) {
            Log.e(TAG, "GNSS Power Save Disable!!!");
            return;
        }
        setUp();
        stopNavigatingNotify();
        this.myHander.sendEmptyMessage(103);
    }

    public void stopController() {
        if (DEBUG) {
            Log.d(TAG, "----stopController----");
        }
        if (this.mGpsPowerSaveFeatureDisable) {
            Log.e(TAG, "GNSS Power Save Disable!!!");
            return;
        }
        tearDown();
        stopNavigatingNotify();
        this.myHander.sendEmptyMessage(105);
    }

    private void bindNotificationService() {
        Intent intent = new Intent(ACTION_BIND_POWER_SERVICE);
        intent.setPackage(SERVICE_PACKAGE_NAME);
        this.mContext.bindService(intent, this.myConnection, 1);
    }

    private void unBindNotificationService() {
        this.mContext.unbindService(this.myConnection);
    }

    private void changeGpsMode(float speed, float[] snrs) {
        this.mNeedStopGps = this.mGpsMonitor.needStopGps(speed, snrs);
        if (this.mNeedStopGps && shouldStopGps()) {
            if (DEBUG) {
                Log.d(TAG, "will stop gps");
            }
            this.myHander.sendEmptyMessage(101);
        } else if (!this.mNeedStopGps && this.mIsDoingStop) {
            if (DEBUG) {
                Log.d(TAG, "--remove message MSG_STOP_AFTER_DELAY--");
            }
            this.mIsDoingStop = false;
            this.myHander.removeMessages(102);
            stopNavigatingNotify();
        }
    }

    private void setUp() {
        if (DEBUG) {
            Log.d(TAG, "Set up the running environment!");
        }
        this.mIsDoingStop = false;
        this.mGpsMode = 0;
        if (this.mNeedRebind) {
            bindNotificationService();
        }
        this.mGpsMonitor.resetStatus();
    }

    private void tearDown() {
        if (DEBUG) {
            Log.d(TAG, "Tear down the running environment!");
        }
        this.mGpsMode = 0;
        if (!this.mNeedRebind) {
            unBindNotificationService();
        }
        exitPowerSavingMode();
    }

    public int getGpsPowerMode() {
        return this.mGpsMode;
    }

    public boolean resistStartGps() {
        if (DEBUG) {
            Log.d(TAG, "running resistStartGps mGpsMode: " + this.mGpsMode);
        }
        return 4 == this.mGpsMode;
    }

    private boolean shouldStopGps() {
        if (DEBUG) {
            Log.d(TAG, "running shouldStopGps mIsDoingStop: " + this.mIsDoingStop);
        }
        if (!this.mIsDoingStop || 2 == this.mGpsMode) {
            return true;
        }
        return false;
    }

    private void willStopGps() {
        this.mIsDoingStop = true;
        startNotify();
        if (DEBUG) {
            Log.d(TAG, "running willstopGps Message  mGpsMode " + this.mGpsMode);
        }
        if (this.mGpsMode == 0) {
            this.myHander.sendEmptyMessageDelayed(102, 60000);
        }
    }

    private void enterPowerSavingMode() {
        this.mIsDoingStop = false;
        powerSaverNotify();
        this.mGlp.enterPSMode();
        this.myHander.sendEmptyMessage(105);
        this.mLocMgr.requestLocationUpdates(PASSIVE_PROVIDER, 1000, OppoBrightUtils.MIN_LUX_LIMITI, this.myLocationListener);
    }

    private void exitPowerSavingMode() {
        this.mLocMgr.removeUpdates(this.myLocationListener);
    }

    private void startNotify() {
        this.mContext.sendBroadcast(new Intent(ACTION_START_NOTIFICATION));
    }

    private void powerSaverNotify() {
        this.mContext.sendBroadcast(new Intent(ACTION_POWER_SAVER_NOTIFICATION));
    }

    public void stopNavigatingNotify() {
        this.mContext.sendBroadcast(new Intent(ACTION_STOP_NAVIGATING_NOTIFICATION));
    }

    private void wakeGps() {
        if (DEBUG) {
            Log.d(TAG, "user wake mode : " + this.mGpsMode);
        }
        this.mIsDoingStop = false;
        this.mGpsMode = 1;
        this.mGpsMonitor.resetStatus();
        this.mGlp.wakeGps();
    }

    private void userAgreed() {
        if (DEBUG) {
            Log.d(TAG, "user agree mode : " + this.mGpsMode);
        }
        if (this.mGpsMode == 0) {
            this.myHander.removeMessages(102);
        }
        this.mIsDoingStop = false;
        this.mGpsMode = 4;
        enterPowerSavingMode();
    }

    private void userDiscard() {
        if (DEBUG) {
            Log.d(TAG, "user discard mode : " + this.mGpsMode);
        }
        if (this.mGpsMode == 0) {
            this.myHander.removeMessages(102);
        }
        this.mIsDoingStop = false;
        this.mGpsMonitor.resetStatus();
        this.mGpsMode = 2;
        this.myHander.sendEmptyMessage(105);
    }

    private void registBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USER_WAKE_GPS);
        filter.addAction(ACTION_USER_DISCARD);
        filter.addAction(ACTION_USER_AGREE);
        this.mContext.registerReceiver(this.myReceiver, filter);
    }

    public void enableLog(boolean verbose) {
        DEBUG = verbose;
        this.mGpsMonitor.enableLog(verbose);
    }

    private void unRegistBroadcast() {
        this.mContext.unregisterReceiver(this.myReceiver);
    }
}
