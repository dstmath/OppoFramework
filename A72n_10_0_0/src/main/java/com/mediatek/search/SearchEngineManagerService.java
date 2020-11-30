package com.mediatek.search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import com.mediatek.common.regionalphone.RegionalPhone;
import com.mediatek.common.search.SearchEngine;
import com.mediatek.search.ISearchEngineManagerService;
import java.util.ArrayList;
import java.util.List;

public class SearchEngineManagerService extends ISearchEngineManagerService.Stub {
    private static final String TAG = "SearchEngineManagerService";
    private final Context mContext;
    private SearchEngine mDefaultSearchEngine;
    private ContentObserver mSearchEngineObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.search.SearchEngineManagerService.AnonymousClass1 */

        public void onChange(boolean selfChange) {
            SearchEngineManagerService.this.initSearchEngines();
            SearchEngineManagerService searchEngineManagerService = SearchEngineManagerService.this;
            searchEngineManagerService.broadcastSearchEngineChangedInternal(searchEngineManagerService.mContext);
        }
    };
    private List<SearchEngine> mSearchEngines;

    public SearchEngineManagerService(Context context) {
        this.mContext = context;
        this.mContext.registerReceiver(new BootCompletedReceiver(), new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        this.mContext.getContentResolver().registerContentObserver(RegionalPhone.SEARCHENGINE_URI, true, this.mSearchEngineObserver);
    }

    private final class BootCompletedReceiver extends BroadcastReceiver {
        private BootCompletedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            new Thread() {
                /* class com.mediatek.search.SearchEngineManagerService.BootCompletedReceiver.AnonymousClass1 */

                public void run() {
                    Process.setThreadPriority(10);
                    SearchEngineManagerService.this.mContext.unregisterReceiver(BootCompletedReceiver.this);
                    SearchEngineManagerService.this.initSearchEngines();
                    SearchEngineManagerService.this.mContext.registerReceiver(new LocaleChangeReceiver(), new IntentFilter("android.intent.action.LOCALE_CHANGED"));
                }
            }.start();
        }
    }

    public synchronized List<SearchEngine> getAvailables() {
        Log.i(TAG, "get avilable search engines");
        if (this.mSearchEngines == null) {
            initSearchEngines();
        }
        return this.mSearchEngines;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initSearchEngines() throws IllegalArgumentException {
        this.mSearchEngines = new ArrayList();
        String[] searchEngines = this.mContext.getResources().getStringArray(134479882);
        if (searchEngines == null || 1 >= searchEngines.length) {
            throw new IllegalArgumentException("No data found for ");
        }
        String sp = searchEngines[0];
        for (int i = 1; i < searchEngines.length; i++) {
            this.mSearchEngines.add(SearchEngine.parseFrom(searchEngines[i], sp));
        }
        SearchEngine searchEngine = this.mDefaultSearchEngine;
        if (searchEngine != null) {
            this.mDefaultSearchEngine = getBestMatch(searchEngine.getName(), this.mDefaultSearchEngine.getFaviconUri());
        }
        if (this.mDefaultSearchEngine == null) {
            this.mDefaultSearchEngine = this.mSearchEngines.get(0);
        }
    }

    private final class LocaleChangeReceiver extends BroadcastReceiver {
        private LocaleChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            SearchEngineManagerService.this.initSearchEngines();
            SearchEngineManagerService searchEngineManagerService = SearchEngineManagerService.this;
            searchEngineManagerService.broadcastSearchEngineChangedInternal(searchEngineManagerService.mContext);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        if (field == -1) {
            return getByName(value);
        }
        if (field != 2) {
            return null;
        }
        return getByFavicon(value);
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
