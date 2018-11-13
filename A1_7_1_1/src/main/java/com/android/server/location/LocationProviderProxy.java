package com.android.server.location;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.location.ILocationProvider;
import com.android.internal.location.ILocationProvider.Stub;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.ServiceWatcher;
import com.android.server.oppo.IElsaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class LocationProviderProxy implements LocationProviderInterface {
    private static final boolean D = false;
    private static final String TAG = "LocationProviderProxy";
    private final Context mContext;
    private boolean mEnabled;
    private Object mLock;
    private final String mName;
    private Runnable mNewServiceWork;
    private ProviderProperties mProperties;
    private ProviderRequest mRequest;
    private final ServiceWatcher mServiceWatcher;
    private WorkSource mWorksource;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.location.LocationProviderProxy.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.location.LocationProviderProxy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.LocationProviderProxy.<clinit>():void");
    }

    public static LocationProviderProxy create(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        return new LocationProviderProxy(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    public static LocationProviderProxy createAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        LocationProviderProxy proxy = new LocationProviderProxy(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private LocationProviderProxy(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        this.mLock = new Object();
        this.mEnabled = false;
        this.mRequest = null;
        this.mWorksource = new WorkSource();
        this.mNewServiceWork = new Runnable() {
            public void run() {
                boolean enabled;
                ProviderRequest request;
                WorkSource source;
                ILocationProvider service;
                if (LocationProviderProxy.D) {
                    Log.d(LocationProviderProxy.TAG, "applying state to connected service");
                }
                ProviderProperties properties = null;
                synchronized (LocationProviderProxy.this.mLock) {
                    enabled = LocationProviderProxy.this.mEnabled;
                    request = LocationProviderProxy.this.mRequest;
                    source = LocationProviderProxy.this.mWorksource;
                    service = LocationProviderProxy.this.getService();
                }
                if (service != null) {
                    try {
                        properties = service.getProperties();
                        if (properties == null) {
                            Log.e(LocationProviderProxy.TAG, LocationProviderProxy.this.mServiceWatcher.getBestPackageName() + " has invalid locatino provider properties");
                        }
                        if (enabled) {
                            service.enable();
                            if (request != null) {
                                service.setRequest(request, source);
                            }
                        }
                    } catch (RemoteException e) {
                        Log.w(LocationProviderProxy.TAG, e);
                    } catch (Exception e2) {
                        Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                    }
                    synchronized (LocationProviderProxy.this.mLock) {
                        LocationProviderProxy.this.mProperties = properties;
                    }
                }
            }
        };
        this.mContext = context;
        this.mName = name;
        Handler mHandler = handler;
        final Handler handler2 = handler;
        this.mServiceWatcher = new ServiceWatcher(this.mContext, "LocationProviderProxy-" + name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, this.mNewServiceWork, handler) {
            public void onServiceDisconnected(ComponentName name) {
                super.onServiceDisconnected(name);
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        LocationProviderProxy.this.unbind();
                        LocationProviderProxy.this.bind();
                    }
                }, 3000);
            }
        };
    }

    public static LocationProviderProxy createAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, int vendorPackageNamesResId, int preferPackageNamesResId, Handler handler) {
        LocationProviderProxy proxy = new LocationProviderProxy(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, vendorPackageNamesResId, preferPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private LocationProviderProxy(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, int vendorPackageNamesResId, int preferPackageNamesResId, Handler handler) {
        this.mLock = new Object();
        this.mEnabled = false;
        this.mRequest = null;
        this.mWorksource = new WorkSource();
        this.mNewServiceWork = /* anonymous class already generated */;
        this.mContext = context;
        this.mName = name;
        this.mServiceWatcher = new ServiceWatcher(this.mContext, "LocationProviderProxy-" + name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, vendorPackageNamesResId, preferPackageNamesResId, this.mNewServiceWork, handler);
    }

    public void unbind() {
        this.mServiceWatcher.stop();
    }

    public boolean bind() {
        return this.mServiceWatcher.start();
    }

    private ILocationProvider getService() {
        return Stub.asInterface(this.mServiceWatcher.getBinder());
    }

    public String getConnectedPackageName() {
        return this.mServiceWatcher.getBestPackageName();
    }

    public String getName() {
        return this.mName;
    }

    public ProviderProperties getProperties() {
        ProviderProperties providerProperties;
        synchronized (this.mLock) {
            providerProperties = this.mProperties;
        }
        return providerProperties;
    }

    public void enable() {
        synchronized (this.mLock) {
            this.mEnabled = true;
        }
        ILocationProvider service = getService();
        if (service != null) {
            try {
                service.enable();
            } catch (RemoteException e) {
                Log.w(TAG, e);
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
            }
        }
    }

    public void disable() {
        synchronized (this.mLock) {
            this.mEnabled = false;
        }
        ILocationProvider service = getService();
        if (service != null) {
            try {
                service.disable();
            } catch (RemoteException e) {
                Log.w(TAG, e);
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
            }
        }
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabled;
        }
        return z;
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        synchronized (this.mLock) {
            this.mRequest = request;
            this.mWorksource = source;
        }
        ILocationProvider service = getService();
        if (service != null) {
            try {
                service.setRequest(request, source);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
            }
        }
    }

    public static void close(LocationProviderProxy proxy) {
        if (proxy != null) {
            proxy.unbind();
        }
    }

    protected void dumpRequestStateTo(LocationProviderProxy des) {
        des.mEnabled = this.mEnabled;
        des.mRequest = this.mRequest;
        des.mWorksource = this.mWorksource;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.append("REMOTE SERVICE");
        pw.append(" name=").append(this.mName);
        pw.append(" pkg=").append(this.mServiceWatcher.getBestPackageName());
        pw.append(" version=").append(IElsaManager.EMPTY_PACKAGE + this.mServiceWatcher.getBestVersion());
        pw.append(10);
        ILocationProvider service = getService();
        if (service == null) {
            pw.println("service down (null)");
            return;
        }
        pw.flush();
        try {
            service.asBinder().dump(fd, args);
        } catch (RemoteException e) {
            pw.println("service down (RemoteException)");
            Log.w(TAG, e);
        } catch (Exception e2) {
            pw.println("service down (Exception)");
            Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
        }
    }

    public int getStatus(Bundle extras) {
        ILocationProvider service = getService();
        if (service == null) {
            return 1;
        }
        try {
            return service.getStatus(extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
        }
        return 1;
    }

    public long getStatusUpdateTime() {
        ILocationProvider service = getService();
        if (service == null) {
            return 0;
        }
        try {
            return service.getStatusUpdateTime();
        } catch (RemoteException e) {
            Log.w(TAG, e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
        }
        return 0;
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        ILocationProvider service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.sendExtraCommand(command, extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
        }
        return false;
    }
}
