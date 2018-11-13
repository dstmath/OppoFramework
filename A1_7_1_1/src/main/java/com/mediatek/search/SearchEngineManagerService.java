package com.mediatek.search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import com.mediatek.common.MPlugin;
import com.mediatek.common.regionalphone.RegionalPhone;
import com.mediatek.common.search.IRegionalPhoneSearchEngineExt;
import com.mediatek.common.search.SearchEngine;
import com.mediatek.search.ISearchEngineManagerService.Stub;
import java.util.ArrayList;
import java.util.List;

public class SearchEngineManagerService extends Stub {
    private static final String TAG = "SearchEngineManagerService";
    private final Context mContext;
    private SearchEngine mDefaultSearchEngine;
    private ContentObserver mSearchEngineObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SearchEngineManagerService.this.initSearchEngines();
        }
    };
    private List<SearchEngine> mSearchEngines;

    private final class BootCompletedReceiver extends BroadcastReceiver {
        /* synthetic */ BootCompletedReceiver(SearchEngineManagerService this$0, BootCompletedReceiver bootCompletedReceiver) {
            this();
        }

        private BootCompletedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            new Thread() {
                public void run() {
                    Process.setThreadPriority(10);
                    SearchEngineManagerService.this.mContext.unregisterReceiver(BootCompletedReceiver.this);
                    SearchEngineManagerService.this.initSearchEngines();
                    SearchEngineManagerService.this.mContext.registerReceiver(new LocaleChangeReceiver(SearchEngineManagerService.this, null), new IntentFilter("android.intent.action.LOCALE_CHANGED"));
                }
            }.start();
        }
    }

    private final class LocaleChangeReceiver extends BroadcastReceiver {
        /* synthetic */ LocaleChangeReceiver(SearchEngineManagerService this$0, LocaleChangeReceiver localeChangeReceiver) {
            this();
        }

        private LocaleChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            SearchEngineManagerService.this.initSearchEngines();
        }
    }

    public SearchEngineManagerService(Context context) {
        this.mContext = context;
        this.mContext.registerReceiver(new BootCompletedReceiver(this, null), new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        this.mContext.getContentResolver().registerContentObserver(RegionalPhone.SEARCHENGINE_URI, true, this.mSearchEngineObserver);
    }

    public synchronized List<SearchEngine> getAvailables() {
        Log.i(TAG, "get avilable search engines");
        if (this.mSearchEngines == null) {
            initSearchEngines();
        }
        return this.mSearchEngines;
    }

    private void initSearchEngines() throws IllegalArgumentException {
        IRegionalPhoneSearchEngineExt regionalPhoneSearchEngineExt = (IRegionalPhoneSearchEngineExt) MPlugin.createInstance(IRegionalPhoneSearchEngineExt.class.getName(), this.mContext);
        if (regionalPhoneSearchEngineExt != null) {
            this.mSearchEngines = regionalPhoneSearchEngineExt.initSearchEngineInfosFromRpm(this.mContext);
            if (this.mSearchEngines != null) {
                this.mDefaultSearchEngine = (SearchEngine) this.mSearchEngines.get(0);
                Log.d(TAG, "RegionalPhone Search engine init");
                return;
            }
        }
        this.mSearchEngines = new ArrayList();
        String[] searchEngines = this.mContext.getResources().getStringArray(134479882);
        if (searchEngines == null || 1 >= searchEngines.length) {
            throw new IllegalArgumentException("No data found for ");
        }
        String sp = searchEngines[0];
        for (int i = 1; i < searchEngines.length; i++) {
            this.mSearchEngines.add(SearchEngine.parseFrom(searchEngines[i], sp));
        }
        if (this.mDefaultSearchEngine != null) {
            this.mDefaultSearchEngine = getBestMatch(this.mDefaultSearchEngine.getName(), this.mDefaultSearchEngine.getFaviconUri());
        }
        if (this.mDefaultSearchEngine == null) {
            this.mDefaultSearchEngine = (SearchEngine) this.mSearchEngines.get(0);
        }
        broadcastSearchEngineChangedInternal(this.mContext);
    }

    private void broadcastSearchEngineChangedInternal(Context context) {
        context.sendBroadcast(new Intent("com.mediatek.search.SEARCH_ENGINE_CHANGED"));
        Log.d(TAG, "broadcast serach engine changed");
    }

    public SearchEngine getBestMatch(String name, String favicon) {
        SearchEngine engine = getByName(name);
        return engine != null ? engine : getByFavicon(favicon);
    }

    private SearchEngine getByFavicon(String favicon) {
        for (SearchEngine engine : getAvailables()) {
            if (favicon.equals(engine.getFaviconUri())) {
                return engine;
            }
        }
        return null;
    }

    private SearchEngine getByName(String name) {
        for (SearchEngine engine : getAvailables()) {
            if (name.equals(engine.getName())) {
                return engine;
            }
        }
        return null;
    }

    public SearchEngine getSearchEngine(int field, String value) {
        switch (field) {
            case -1:
                return getByName(value);
            case 2:
                return getByFavicon(value);
            default:
                return null;
        }
    }

    public SearchEngine getDefault() {
        return this.mDefaultSearchEngine;
    }

    public boolean setDefault(SearchEngine engine) {
        for (SearchEngine eng : getAvailables()) {
            if (eng.getName().equals(engine.getName())) {
                this.mDefaultSearchEngine = engine;
                return true;
            }
        }
        return false;
    }
}
