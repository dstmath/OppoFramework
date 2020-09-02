package com.android.server.location;

import android.content.Context;
import android.location.Country;
import android.location.CountryListener;
import android.location.Geocoder;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocationManagerService;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComprehensiveCountryDetector extends CountryDetectorBase {
    static final boolean DEBUG = LocationManagerService.D;
    private static final long LOCATION_REFRESH_INTERVAL = 86400000;
    private static final int MAX_LENGTH_DEBUG_LOGS = 20;
    private static final String TAG = "CountryDetector";
    private String localeCountryIso = null;
    private int mCountServiceStateChanges;
    private Country mCountry;
    /* access modifiers changed from: private */
    public Country mCountryFromLocation;
    private final ConcurrentLinkedQueue<Country> mDebugLogs = new ConcurrentLinkedQueue<>();
    private Country mLastCountryAddedToLogs;
    private CountryListener mLocationBasedCountryDetectionListener = new CountryListener() {
        /* class com.android.server.location.ComprehensiveCountryDetector.AnonymousClass1 */

        public void onCountryDetected(Country country) {
            if (ComprehensiveCountryDetector.DEBUG) {
                Slog.d(ComprehensiveCountryDetector.TAG, "Country detected via LocationBasedCountryDetector");
            }
            Country unused = ComprehensiveCountryDetector.this.mCountryFromLocation = country;
            if (ComprehensiveCountryDetector.this.isSoftCardPrepared()) {
                Country unused2 = ComprehensiveCountryDetector.this.detectCountry(true, false);
            }
            ComprehensiveCountryDetector.this.stopLocationBasedDetector();
        }
    };
    protected CountryDetectorBase mLocationBasedCountryDetector;
    protected Timer mLocationRefreshTimer;
    private final Object mObject = new Object();
    private PhoneStateListener mPhoneStateListener;
    private long mStartTime;
    private long mStopTime;
    private boolean mStopped = false;
    private final TelephonyManager mTelephonyManager;
    private int mTotalCountServiceStateChanges;
    private long mTotalTime;

    static /* synthetic */ int access$308(ComprehensiveCountryDetector x0) {
        int i = x0.mCountServiceStateChanges;
        x0.mCountServiceStateChanges = i + 1;
        return i;
    }

    static /* synthetic */ int access$408(ComprehensiveCountryDetector x0) {
        int i = x0.mTotalCountServiceStateChanges;
        x0.mTotalCountServiceStateChanges = i + 1;
        return i;
    }

    public boolean isSoftCardPrepared() {
        int softSlotId = ColorOSTelephonyManager.getDefault(this.mContext).colorGetSoftSimCardSlotId();
        String countryIso = TelephonyManager.getTelephonyProperty(softSlotId, "gsm.operator.iso-country", "");
        if (softSlotId == -1 || !TextUtils.isEmpty(countryIso)) {
            return true;
        }
        Log.d(TAG, "soft sim card is not prepared, softSlotId = " + softSlotId);
        return false;
    }

    public ComprehensiveCountryDetector(Context context) {
        super(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    @Override // com.android.server.location.CountryDetectorBase
    public Country detectCountry() {
        return detectCountry(false, !this.mStopped);
    }

    @Override // com.android.server.location.CountryDetectorBase
    public void stop() {
        Slog.i(TAG, "Stop the detector.");
        cancelLocationRefresh();
        removePhoneStateListener();
        stopLocationBasedDetector();
        this.mListener = null;
        this.mStopped = true;
    }

    private Country getCountry() {
        Country result = getNetworkBasedCountry();
        if (result == null) {
            result = getLastKnownLocationBasedCountry();
        }
        if (result == null) {
            result = getSimBasedCountry();
        }
        if (result == null && (result = getLocaleCountry()) != null) {
            String str = this.localeCountryIso;
            if (str == null) {
                this.localeCountryIso = result.getCountryIso();
            } else if (!str.equals(result.getCountryIso())) {
                notifyListener(result);
                this.localeCountryIso = result.getCountryIso();
            }
        }
        addToLogs(result);
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        if (r2.mDebugLogs.size() < 20) goto L_0x0026;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        r2.mDebugLogs.poll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0028, code lost:
        if (com.android.server.location.ComprehensiveCountryDetector.DEBUG == false) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002a, code lost:
        android.util.Slog.d(com.android.server.location.ComprehensiveCountryDetector.TAG, r3.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0033, code lost:
        r2.mDebugLogs.add(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        return;
     */
    private void addToLogs(Country country) {
        if (country != null) {
            synchronized (this.mObject) {
                if (this.mLastCountryAddedToLogs == null || !this.mLastCountryAddedToLogs.equals(country)) {
                    this.mLastCountryAddedToLogs = country;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isNetworkCountryCodeAvailable() {
        int phoneType = this.mTelephonyManager.getPhoneType();
        if (DEBUG) {
            Slog.v(TAG, "    phonetype=" + phoneType);
        }
        return phoneType == 1;
    }

    /* access modifiers changed from: protected */
    public Country getNetworkBasedCountry() {
        if (!isNetworkCountryCodeAvailable()) {
            return null;
        }
        String countryIso = this.mTelephonyManager.getNetworkCountryIso();
        if (!TextUtils.isEmpty(countryIso)) {
            return new Country(countryIso, 0);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Country getLastKnownLocationBasedCountry() {
        return this.mCountryFromLocation;
    }

    /* access modifiers changed from: protected */
    public Country getSimBasedCountry() {
        String countryIso = this.mTelephonyManager.getSimCountryIso();
        if (!TextUtils.isEmpty(countryIso)) {
            return new Country(countryIso, 2);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Country getLocaleCountry() {
        Locale defaultLocale = Locale.getDefault();
        if (defaultLocale != null) {
            return new Country(defaultLocale.getCountry(), 3);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public Country detectCountry(boolean notifyChange, boolean startLocationBasedDetection) {
        Country country = getCountry();
        Country country2 = this.mCountry;
        if (country2 != null) {
            country2 = new Country(country2);
        }
        runAfterDetectionAsync(country2, country, notifyChange, startLocationBasedDetection);
        this.mCountry = country;
        return this.mCountry;
    }

    /* access modifiers changed from: protected */
    public void runAfterDetectionAsync(final Country country, final Country detectedCountry, final boolean notifyChange, final boolean startLocationBasedDetection) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.location.ComprehensiveCountryDetector.AnonymousClass2 */

            public void run() {
                ComprehensiveCountryDetector.this.runAfterDetection(country, detectedCountry, notifyChange, startLocationBasedDetection);
            }
        });
    }

    @Override // com.android.server.location.CountryDetectorBase
    public void setCountryListener(CountryListener listener) {
        CountryListener prevListener = this.mListener;
        this.mListener = listener;
        if (this.mListener == null) {
            removePhoneStateListener();
            stopLocationBasedDetector();
            cancelLocationRefresh();
            this.mStopTime = SystemClock.elapsedRealtime();
            this.mTotalTime += this.mStopTime;
        } else if (prevListener == null) {
            addPhoneStateListener();
            detectCountry(false, true);
            this.mStartTime = SystemClock.elapsedRealtime();
            this.mStopTime = 0;
            this.mCountServiceStateChanges = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void runAfterDetection(Country country, Country detectedCountry, boolean notifyChange, boolean startLocationBasedDetection) {
        String str;
        notifyIfCountryChanged(country, detectedCountry);
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("startLocationBasedDetection=");
            sb.append(startLocationBasedDetection);
            sb.append(" detectCountry=");
            if (detectedCountry == null) {
                str = null;
            } else {
                str = "(source: " + detectedCountry.getSource() + ", countryISO: " + detectedCountry.getCountryIso() + ")";
            }
            sb.append(str);
            sb.append(" isAirplaneModeOff()=");
            sb.append(isAirplaneModeOff());
            sb.append(" mListener=");
            sb.append(this.mListener);
            sb.append(" isGeoCoderImplemnted()=");
            sb.append(isGeoCoderImplemented());
            Slog.d(TAG, sb.toString());
        }
        if (startLocationBasedDetection && ((detectedCountry == null || detectedCountry.getSource() > 1) && isAirplaneModeOff() && this.mListener != null && isGeoCoderImplemented())) {
            if (DEBUG) {
                Slog.d(TAG, "run startLocationBasedDetector()");
            }
            startLocationBasedDetector(this.mLocationBasedCountryDetectionListener);
        }
        if (detectedCountry == null || detectedCountry.getSource() >= 1) {
            scheduleLocationRefresh();
            return;
        }
        cancelLocationRefresh();
        stopLocationBasedDetector();
    }

    private synchronized void startLocationBasedDetector(CountryListener listener) {
        if (this.mLocationBasedCountryDetector == null) {
            if (DEBUG) {
                Slog.d(TAG, "starts LocationBasedDetector to detect Country code via Location info (e.g. GPS)");
            }
            this.mLocationBasedCountryDetector = createLocationBasedCountryDetector();
            this.mLocationBasedCountryDetector.setCountryListener(listener);
            this.mLocationBasedCountryDetector.detectCountry();
        }
    }

    /* access modifiers changed from: private */
    public synchronized void stopLocationBasedDetector() {
        if (DEBUG) {
            Slog.d(TAG, "tries to stop LocationBasedDetector (current detector: " + this.mLocationBasedCountryDetector + ")");
        }
        if (this.mLocationBasedCountryDetector != null) {
            this.mLocationBasedCountryDetector.stop();
            this.mLocationBasedCountryDetector = null;
        }
    }

    /* access modifiers changed from: protected */
    public CountryDetectorBase createLocationBasedCountryDetector() {
        return new LocationBasedCountryDetector(this.mContext);
    }

    /* access modifiers changed from: protected */
    public boolean isAirplaneModeOff() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0;
    }

    private void notifyIfCountryChanged(Country country, Country detectedCountry) {
        if (detectedCountry != null && this.mListener != null) {
            if (country == null || !country.equals(detectedCountry)) {
                if (DEBUG) {
                    Slog.d(TAG, "" + country + " --> " + detectedCountry);
                }
                notifyListener(detectedCountry);
            }
        }
    }

    private synchronized void scheduleLocationRefresh() {
        if (this.mLocationRefreshTimer == null) {
            if (DEBUG) {
                Slog.d(TAG, "start periodic location refresh timer. Interval: 86400000");
            }
            this.mLocationRefreshTimer = new Timer();
            this.mLocationRefreshTimer.schedule(new TimerTask() {
                /* class com.android.server.location.ComprehensiveCountryDetector.AnonymousClass3 */

                public void run() {
                    if (ComprehensiveCountryDetector.DEBUG) {
                        Slog.d(ComprehensiveCountryDetector.TAG, "periodic location refresh event. Starts detecting Country code");
                    }
                    ComprehensiveCountryDetector comprehensiveCountryDetector = ComprehensiveCountryDetector.this;
                    comprehensiveCountryDetector.mLocationRefreshTimer = null;
                    Country unused = comprehensiveCountryDetector.detectCountry(false, true);
                }
            }, 86400000);
        }
    }

    private synchronized void cancelLocationRefresh() {
        if (this.mLocationRefreshTimer != null) {
            this.mLocationRefreshTimer.cancel();
            this.mLocationRefreshTimer = null;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void addPhoneStateListener() {
        if (this.mPhoneStateListener == null) {
            this.mPhoneStateListener = new PhoneStateListener() {
                /* class com.android.server.location.ComprehensiveCountryDetector.AnonymousClass4 */

                public void onServiceStateChanged(ServiceState serviceState) {
                    ComprehensiveCountryDetector.access$308(ComprehensiveCountryDetector.this);
                    ComprehensiveCountryDetector.access$408(ComprehensiveCountryDetector.this);
                    if (ComprehensiveCountryDetector.this.isNetworkCountryCodeAvailable()) {
                        if (ComprehensiveCountryDetector.DEBUG) {
                            Slog.d(ComprehensiveCountryDetector.TAG, "onServiceStateChanged: " + serviceState.getState());
                        }
                        if (ComprehensiveCountryDetector.this.isSoftCardPrepared()) {
                            Country unused = ComprehensiveCountryDetector.this.detectCountry(true, true);
                        }
                    }
                }
            };
            this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void removePhoneStateListener() {
        if (this.mPhoneStateListener != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mPhoneStateListener = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isGeoCoderImplemented() {
        return Geocoder.isPresent();
    }

    public String toString() {
        long currentTime = SystemClock.elapsedRealtime();
        long currentSessionLength = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("ComprehensiveCountryDetector{");
        if (this.mStopTime == 0) {
            currentSessionLength = currentTime - this.mStartTime;
            sb.append("timeRunning=" + currentSessionLength + ", ");
        } else {
            sb.append("lastRunTimeLength=" + (this.mStopTime - this.mStartTime) + ", ");
        }
        sb.append("totalCountServiceStateChanges=" + this.mTotalCountServiceStateChanges + ", ");
        sb.append("currentCountServiceStateChanges=" + this.mCountServiceStateChanges + ", ");
        sb.append("totalTime=" + (this.mTotalTime + currentSessionLength) + ", ");
        sb.append("currentTime=" + currentTime + ", ");
        sb.append("countries=");
        Iterator<Country> it = this.mDebugLogs.iterator();
        while (it.hasNext()) {
            sb.append("\n   " + it.next().toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
