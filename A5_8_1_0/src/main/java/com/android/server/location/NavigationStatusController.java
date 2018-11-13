package com.android.server.location;

import android.content.Context;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.display.OppoBrightUtils;

public class NavigationStatusController {
    private static final int GPS_POWER_INTERVAL = 1000;
    private static final int MSG_CONTROL_START = 103;
    private static final int MSG_CONTROL_STOP = 105;
    private static final int NAVIGATION_STATUS_OFF = 2;
    private static final int NAVIGATION_STATUS_ON = 1;
    private static final String PASSIVE_PROVIDER = "passive";
    private static final String TAG = "NavigationStatusController";
    private static final long TIME_DELAY_STOP_GPS = 20000;
    private boolean DEBUG = false;
    private final Context mContext;
    private final GnssLocationProvider mGnssLocationProvider;
    private boolean mHasStart = false;
    private final ILocationManager mILocationManager;
    private final LocationManager mLocMgr;
    private final NavigationStatusMonitor mNavigationMonitor;
    private int mNavigationStatus = 1;
    private NavigationStatusListener mNavigationStatusListener = new NavigationStatusListener() {
        public void onNavigationStatusChanged(int status) {
            switch (status) {
                case 1:
                    NavigationStatusController.this.printLog("GPL.status NAVIGATION_STATUS_ON");
                    NavigationStatusController.this.exitPowerSavingMode();
                    NavigationStatusController.this.mNavigationStatus = 1;
                    return;
                case 2:
                    NavigationStatusController.this.printLog("GPL.status NAVIGATION_STATUS_OFF");
                    NavigationStatusController.this.enterPowerSavingMode();
                    NavigationStatusController.this.mNavigationStatus = 2;
                    return;
                default:
                    return;
            }
        }
    };
    private Handler myHander = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 103:
                    NavigationStatusController.this.mNavigationMonitor.startMonitor();
                    return;
                case 105:
                    NavigationStatusController.this.mNavigationMonitor.stopMonitor();
                    return;
                default:
                    return;
            }
        }
    };
    private LocationListener myLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (!"gps".equals(location.getProvider()) && location.getAccuracy() <= 100.0f) {
                try {
                    location.setProvider("gps");
                    location.setSpeed(-1.0f);
                    NavigationStatusController.this.mILocationManager.reportLocation(location, false);
                } catch (RemoteException e) {
                    Log.e(NavigationStatusController.TAG, "RemoteException reporting location!!!");
                }
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public NavigationStatusController(Context context, GnssLocationProvider provider, ILocationManager iLocationManager) {
        this.mContext = context;
        this.mGnssLocationProvider = provider;
        this.mILocationManager = iLocationManager;
        this.mLocMgr = (LocationManager) this.mContext.getSystemService("location");
        this.mNavigationMonitor = new NavigationStatusMonitor(this.mContext, this.mGnssLocationProvider, this.mNavigationStatusListener);
    }

    public void init() {
        this.mNavigationMonitor.init();
        setUp();
    }

    public void startController() {
        printLog("----startController----");
        setUp();
        this.mHasStart = true;
        this.myHander.sendEmptyMessage(103);
    }

    public void stopController() {
        printLog("----stopController----");
        tearDown();
        this.mHasStart = false;
        this.myHander.sendEmptyMessage(105);
    }

    public void setUp() {
        printLog("Set up the running environment!");
        this.mNavigationStatus = 1;
        this.mNavigationMonitor.resetStatus();
    }

    private void tearDown() {
        printLog("Tear down the running environment!");
        this.mLocMgr.removeUpdates(this.myLocationListener);
        this.mNavigationStatus = 2;
    }

    public void setDebug(boolean isDebug) {
        this.DEBUG = isDebug;
        if (this.mNavigationMonitor != null) {
            this.mNavigationMonitor.setDebug(isDebug);
        }
    }

    public void dealFlpSessionOnEvent() {
        if (2 == this.mNavigationStatus) {
            this.mNavigationMonitor.doFlpSessionOn();
        }
    }

    public boolean resistStartGps() {
        printLog("running resistStartGps mNavigationStatus: " + this.mNavigationStatus);
        if (this.mHasStart && 2 == this.mNavigationStatus) {
            return true;
        }
        return false;
    }

    private void enterPowerSavingMode() {
        this.mGnssLocationProvider.enterPSMode();
        if (this.mGnssLocationProvider.isFakeReport()) {
            this.mLocMgr.requestLocationUpdates(PASSIVE_PROVIDER, 1000, OppoBrightUtils.MIN_LUX_LIMITI, this.myLocationListener);
        }
    }

    private void exitPowerSavingMode() {
        printLog("user wake mode : " + this.mNavigationStatus);
        this.mNavigationStatus = 1;
        this.mGnssLocationProvider.wakeGps();
        this.mLocMgr.removeUpdates(this.myLocationListener);
    }

    private void printLog(String log) {
        if (this.DEBUG) {
            Log.d(TAG, log);
        }
    }
}
