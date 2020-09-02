package com.mediatek.search;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.mediatek.common.search.SearchEngine;
import com.mediatek.search.ISearchEngineManagerService;
import java.util.List;

public class SearchEngineManager {
    public static final String ACTION_SEARCH_ENGINE_CHANGED = "com.mediatek.search.SEARCH_ENGINE_CHANGED";
    private static final boolean DBG = false;
    public static final String SEARCH_ENGINE_SERVICE = "search_engine_service";
    private static final String TAG = "SearchEngineManager";
    private static ISearchEngineManagerService mService;
    private final Context mContext;

    public SearchEngineManager(Context context) {
        this.mContext = context;
        mService = ISearchEngineManagerService.Stub.asInterface(ServiceManager.getService(SEARCH_ENGINE_SERVICE));
    }

    public List<SearchEngine> getAvailables() {
        try {
            return mService.getAvailables();
        } catch (RemoteException e) {
            Slog.e(TAG, "getSearchEngineInfos() failed: " + e);
            return null;
        }
    }

    public SearchEngine getBestMatch(String name, String favicon) {
        try {
            return mService.getBestMatch(name, favicon);
        } catch (RemoteException e) {
            Slog.e(TAG, "getBestMatch() failed: " + e);
            return null;
        }
    }

    public SearchEngine getByFavicon(String favicon) {
        return getSearchEngine(2, favicon);
    }

    public SearchEngine getByName(String name) {
        return getSearchEngine(-1, name);
    }

    public SearchEngine getSearchEngine(int field, String value) {
        try {
            return mService.getSearchEngine(field, value);
        } catch (RemoteException e) {
            Slog.e(TAG, "getSearchEngine(int field, String value) failed: " + e);
            return null;
        }
    }

    public SearchEngine getDefault() {
        try {
            return mService.getDefault();
        } catch (RemoteException e) {
            Slog.e(TAG, "getSystemDefaultSearchEngine() failed: " + e);
            return null;
        }
    }

    public boolean setDefault(SearchEngine engine) {
        try {
            return mService.setDefault(engine);
        } catch (RemoteException e) {
            Slog.e(TAG, "getSystemDefaultSearchEngine() failed: " + e);
            return DBG;
        }
    }
}
