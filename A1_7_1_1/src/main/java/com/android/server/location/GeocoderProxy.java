package com.android.server.location;

import android.content.ComponentName;
import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.location.IGeocodeProvider.Stub;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.ServiceWatcher;
import java.util.List;

public class GeocoderProxy {
    private static final String SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String TAG = "GeocoderProxy";
    private final ServiceWatcher mCnServiceWatcher;
    private final Context mContext;
    private final ServiceWatcher mGmsServiceWatcher;
    private ServiceWatcher mServiceWatcher;

    public static GeocoderProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        GeocoderProxy proxy = new GeocoderProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private GeocoderProxy(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        ServiceWatcher serviceWatcher;
        this.mContext = context;
        Handler mHandler = handler;
        final Handler handler2 = handler;
        this.mCnServiceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, 17956943, 17039427, initialPackageNamesResId, null, handler) {
            public void onServiceDisconnected(ComponentName name) {
                super.onServiceDisconnected(name);
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        GeocoderProxy.this.unbind();
                        GeocoderProxy.this.bind();
                    }
                }, 3000);
            }
        };
        handler2 = handler;
        this.mGmsServiceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, 17956943, 17039424, initialPackageNamesResId, null, handler) {
            public void onServiceDisconnected(ComponentName name) {
                super.onServiceDisconnected(name);
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        GeocoderProxy.this.unbind();
                        GeocoderProxy.this.bind();
                    }
                }, 3000);
            }
        };
        if (!SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN")) {
            serviceWatcher = this.mGmsServiceWatcher;
        } else {
            serviceWatcher = this.mCnServiceWatcher;
        }
        this.mServiceWatcher = serviceWatcher;
    }

    private boolean bind() {
        return this.mServiceWatcher.start();
    }

    public static GeocoderProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, int vendorPackageNamesResId, int preferPackageNamesResId, Handler handler) {
        GeocoderProxy proxy = new GeocoderProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, vendorPackageNamesResId, preferPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private GeocoderProxy(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, int vendorPackageNamesResId, int preferPackageNamesResId, Handler handler) {
        ServiceWatcher serviceWatcher;
        this.mContext = context;
        Handler mHandler = handler;
        final Handler handler2 = handler;
        this.mCnServiceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, 17956943, 17039427, initialPackageNamesResId, null, handler) {
            public void onServiceDisconnected(ComponentName name) {
                super.onServiceDisconnected(name);
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        GeocoderProxy.this.unbind();
                        GeocoderProxy.this.bind();
                    }
                }, 3000);
            }
        };
        handler2 = handler;
        this.mGmsServiceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, 17956943, 17039424, initialPackageNamesResId, null, handler) {
            public void onServiceDisconnected(ComponentName name) {
                super.onServiceDisconnected(name);
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        GeocoderProxy.this.unbind();
                        GeocoderProxy.this.bind();
                    }
                }, 3000);
            }
        };
        if (!SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN")) {
            serviceWatcher = this.mGmsServiceWatcher;
        } else {
            serviceWatcher = this.mCnServiceWatcher;
        }
        this.mServiceWatcher = serviceWatcher;
    }

    public void unbind() {
        if (this.mServiceWatcher != null) {
            this.mServiceWatcher.stop();
        }
    }

    public boolean isServiceBinded() {
        if (getService() != null) {
            return true;
        }
        return false;
    }

    private IGeocodeProvider getService() {
        return Stub.asInterface(this.mServiceWatcher.getBinder());
    }

    public void switchToNLP(boolean isGms) {
        if (isGms) {
            if (this.mServiceWatcher != this.mGmsServiceWatcher && this.mGmsServiceWatcher.start()) {
                this.mServiceWatcher = this.mGmsServiceWatcher;
                this.mCnServiceWatcher.stop();
            }
        } else if (this.mServiceWatcher != this.mCnServiceWatcher && this.mCnServiceWatcher.start()) {
            this.mServiceWatcher = this.mCnServiceWatcher;
            this.mGmsServiceWatcher.stop();
        }
    }

    public String getConnectedPackageName() {
        return this.mServiceWatcher.getBestPackageName();
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocation(latitude, longitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }
}
