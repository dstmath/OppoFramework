package com.google.android.maps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android_maps_conflict_avoidance.com.google.android.gsf.GoogleSettingsContract.Partner;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.android.AndroidConfig;
import android_maps_conflict_avoidance.com.google.common.io.android.AndroidHttpConnectionFactory;
import android_maps_conflict_avoidance.com.google.common.io.android.GoogleHttpClient;
import android_maps_conflict_avoidance.com.google.googlenav.android.TaskRunnerManager;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher;
import android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficService;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import android_maps_conflict_avoidance.com.google.map.Zoom;
import com.google.android.maps.NetworkConnectivityListener.State;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.http.conn.ClientConnectionManager;

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
public abstract class MapActivity extends Activity {
    protected static final int MAP_DATA_SOURCE_CHINA = 1;
    protected static final int MAP_DATA_SOURCE_DEFAULT = 0;
    private static final Map<String, Integer> drawableIdMap = null;
    private static volatile WeakReference<MapActivity> sActivityReference;
    private static volatile WeakReference<android_maps_conflict_avoidance.com.google.googlenav.map.Map> sMapReference;
    private AndroidConfig mConfig;
    private DataRequestDispatcher mDataRequestDispatcher;
    private android_maps_conflict_avoidance.com.google.googlenav.map.Map mMap;
    private MapView mMapView;
    private final Handler mNetworkHandler;
    private NetworkConnectivityListener mNetworkWatcher;
    private TrafficService mTrafficService;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.google.android.maps.MapActivity.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.google.android.maps.MapActivity.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.maps.MapActivity.<clinit>():void");
    }

    protected abstract boolean isRouteDisplayed();

    public MapActivity() {
        this.mMap = null;
        this.mNetworkHandler = new Handler() {
            public void handleMessage(Message message) {
                if (MapActivity.this.mNetworkWatcher != null) {
                    State state = MapActivity.this.mNetworkWatcher.getState();
                    Log.i("MapActivity", "Handling network change notification:" + state.toString());
                    if (MapActivity.this.mDataRequestDispatcher != null) {
                        MapActivity.this.mDataRequestDispatcher.stop();
                        if (state == State.CONNECTED) {
                            MapActivity.this.mDataRequestDispatcher.start();
                        }
                    }
                    try {
                        AndroidHttpConnectionFactory factory = MapActivity.this.mConfig.getConnectionFactory();
                        if (factory == null) {
                            Log.e("MapActivity", "Couldn't get connection factory");
                            return;
                        }
                        GoogleHttpClient client = factory.getClient();
                        if (client == null) {
                            Log.e("MapActivity", "Couldn't get connection factory client");
                            return;
                        }
                        ClientConnectionManager manager = client.getConnectionManager();
                        if (manager == null) {
                            Log.e("MapActivity", "Couldn't get client connection manager");
                        } else {
                            manager.closeIdleConnections(1, TimeUnit.MILLISECONDS);
                        }
                    } catch (Exception e) {
                        Log.e("MapActivity", "Couldn't reset connection pool.", e);
                    }
                }
            }
        };
    }

    void setupMapView(MapView mapView) {
        if (this.mMapView != null) {
            throw new IllegalStateException("You are only allowed to have a single MapView in a MapActivity");
        }
        this.mDataRequestDispatcher.setAndroidMapKey(mapView.mKey);
        this.mDataRequestDispatcher.setAndroidLoggingId2(Partner.getString(getContentResolver(), "logging_id2"));
        this.mMapView = mapView;
        this.mMapView.setup(this.mMap, this.mTrafficService, this.mDataRequestDispatcher);
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mNetworkWatcher = new NetworkConnectivityListener();
        this.mNetworkWatcher.registerHandler(this.mNetworkHandler, 0);
        this.mConfig = new AndroidConfig(this);
        this.mConfig.getImageFactory().setStringIdMap(drawableIdMap);
        setupTileDensity();
        createMap();
        if (icicle == null) {
            sendStartSession();
        }
    }

    private void setupTileDensity() {
        if (getResources().getDisplayMetrics().densityDpi > 200) {
            MapTile.setTextSize(3);
        }
    }

    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        sendStartSession();
    }

    protected void onResume() {
        super.onResume();
        restoreGlobalState();
        this.mTrafficService.start();
        this.mDataRequestDispatcher.start();
        this.mNetworkWatcher.startListening(this);
        this.mMap.resume();
        sActivityReference = new WeakReference(this);
    }

    private void restoreGlobalState() {
        Config.setConfig(this.mConfig);
        this.mDataRequestDispatcher.resetConnectionFactory();
        if (this.mMapView != null) {
            this.mMapView.restoreMapReferences(this.mDataRequestDispatcher);
        }
    }

    protected void onPause() {
        super.onPause();
        if (sActivityReference.get() == this) {
            this.mTrafficService.stop();
            this.mMap.pause();
            this.mDataRequestDispatcher.stop();
            this.mNetworkWatcher.stopListening();
            this.mMap.saveState();
            this.mConfig.getPersistentStore().savePreferences();
            return;
        }
        Log.d("MapActivity", "onPause leaving the lights on for " + sActivityReference.get());
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mMapView != null) {
            this.mMapView.cleanupMapReferences(this.mDataRequestDispatcher);
        }
        if (sActivityReference.get() == this) {
            this.mNetworkWatcher.unregisterHandler(this.mNetworkHandler);
            this.mNetworkWatcher = null;
            this.mTrafficService.close();
            this.mMap.close(false);
        } else {
            Log.d("MapActivity", "onDestroy leaving the lights on for " + sActivityReference.get());
        }
        this.mConfig.getConnectionFactory().close();
    }

    private String getServerUrl() {
        switch (onGetMapDataSource()) {
            case 1:
                return "http://www.google.cn/glm/mmap/a";
            default:
                return "http://www.google.com/glm/mmap/a";
        }
    }

    private void createMap() {
        this.mDataRequestDispatcher = DataRequestDispatcher.getInstance();
        if (this.mDataRequestDispatcher != null) {
            Log.w("MapActivity", "Recycling dispatcher " + this.mDataRequestDispatcher);
            this.mDataRequestDispatcher.resetConnectionFactory();
        } else {
            this.mDataRequestDispatcher = DataRequestDispatcher.createInstance(getServerUrl(), "android:" + SystemProperties.get("ro.product.brand", "unknown").replace('-', '_') + "-" + SystemProperties.get("ro.product.device", "unknown").replace('-', '_') + "-" + SystemProperties.get("ro.product.model", "unknown").replace('-', '_'), "1.6", "gmm-" + Partner.getString(getContentResolver(), "client_id", "unknown"), true);
        }
        this.mDataRequestDispatcher.setAndroidSignature(KeyHelper.getSignatureFingerprint(super.getPackageManager(), super.getPackageName()));
        this.mDataRequestDispatcher.setApplicationName(getClass().getName());
        int[] startingLatLng = getResources().getIntArray(array.maps_starting_lat_lng);
        MapPoint startPoint = new MapPoint(startingLatLng[0], startingLatLng[1]);
        Zoom zoom = Zoom.getZoom(getResources().getIntArray(array.maps_starting_zoom)[0]);
        this.mMap = (android_maps_conflict_avoidance.com.google.googlenav.map.Map) sMapReference.get();
        if (this.mMap == null) {
            this.mMap = new android_maps_conflict_avoidance.com.google.googlenav.map.Map(-1, -1, 409600, startPoint, zoom, 10);
        } else {
            Log.v("MapActivity", "Recycling map object.");
        }
        sMapReference = new WeakReference(this.mMap);
        sActivityReference = new WeakReference(this);
        this.mTrafficService = new TrafficService(120000, TaskRunnerManager.getTaskRunner());
    }

    protected boolean isLocationDisplayed() {
        boolean z = false;
        if (this.mMapView != null) {
            List<Overlay> overlays = this.mMapView.getOverlays();
            synchronized (overlays) {
                for (Overlay overlay : overlays) {
                    if ((overlay instanceof MyLocationOverlay) && ((MyLocationOverlay) overlay).isMyLocationEnabled()) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    private void sendStartSession() {
        int startType;
        if (getIntent() == null || "android.intent.action.MAIN".equals(getIntent().getAction())) {
            startType = 0;
        } else {
            startType = 1;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new DataOutputStream(baos).writeInt(startType);
            DataRequestDispatcher.getInstance().addSimpleRequest(16, baos.toByteArray(), false, false);
        } catch (IOException e) {
            Log.e("MapActivity", "Error sending start session request", e);
        }
    }

    protected int onGetMapDataSource() {
        return 0;
    }
}
