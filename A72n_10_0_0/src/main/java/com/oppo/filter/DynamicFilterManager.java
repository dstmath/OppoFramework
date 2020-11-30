package com.oppo.filter;

import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;

public class DynamicFilterManager {
    public static final String FILTER_BRK_SEARCH_2_WAY = "brk_s2w";
    public static final String FILTER_GL_OOM = "gl_oom";
    public static final String SERVICE_NAME = "dynamic_filter";
    private static final String TAG = "DynamicFilterManager";
    private Context mContext;
    private IDynamicFilterService mService;

    public DynamicFilterManager(Context context, IDynamicFilterService service) {
        this.mContext = context;
        this.mService = service;
        if (service == null) {
            Slog.e(TAG, "DynamicFilterService was null!");
        }
    }

    public boolean hasFilter(String name) {
        try {
            return this.mService.hasFilter(name);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean inFilter(String name, String tag) {
        try {
            return this.mService.inFilter(name, tag);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void addToFilter(String name, String tag, String value) {
        try {
            this.mService.addToFilter(name, tag, value);
        } catch (RemoteException e) {
        }
    }

    public void removeFromFilter(String name, String tag) {
        try {
            this.mService.removeFromFilter(name, tag);
        } catch (RemoteException e) {
        }
    }

    public String getFilterTagValue(String name, String tag) {
        try {
            return this.mService.getFilterTagValue(name, tag);
        } catch (RemoteException e) {
            return null;
        }
    }
}
