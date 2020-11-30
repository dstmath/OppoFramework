package com.oppo.enterprise.mdmcoreservice.service;

import android.content.Context;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

public class OppoMdmManagerFactory {
    private static String TAG = "OppoMdmManagerFactory";
    private static volatile OppoMdmManagerFactory sInstance;
    private Context mContext;
    private ArrayMap<String, IBinder> mManagerInstanceCache = new ArrayMap<>();

    private OppoMdmManagerFactory() {
    }

    public static final OppoMdmManagerFactory getInstance() {
        OppoMdmManagerFactory oppoMdmManagerFactory;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (OppoMdmManagerFactory.class) {
            if (sInstance == null) {
                sInstance = new OppoMdmManagerFactory();
            }
            oppoMdmManagerFactory = sInstance;
        }
        return oppoMdmManagerFactory;
    }

    public void initManager(Context context) {
        this.mContext = context;
        PermissionManager.getInstance().setContext(context);
    }

    public IBinder getManager(String strManagerName) {
        try {
            synchronized (this.mManagerInstanceCache) {
                if (this.mManagerInstanceCache.containsKey(strManagerName)) {
                    return this.mManagerInstanceCache.get(strManagerName);
                }
                IBinder obj = (IBinder) Class.forName("com.oppo.enterprise.mdmcoreservice.service.managerimpl." + strManagerName + "Impl").getConstructor(Context.class).newInstance(this.mContext);
                if (obj != null) {
                    this.mManagerInstanceCache.put(strManagerName, obj);
                }
                return obj;
            }
        } catch (Exception e) {
            Log.w(TAG, "getManager error");
            e.printStackTrace();
            return null;
        }
    }
}
