package com.android.server.search;

import android.app.AppGlobals;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
public class Searchables {
    public static String ENHANCED_GOOGLE_SEARCH_COMPONENT_NAME = null;
    private static final Comparator<ResolveInfo> GLOBAL_SEARCH_RANKER = null;
    public static String GOOGLE_SEARCH_COMPONENT_NAME = null;
    private static final String LOG_TAG = "Searchables";
    private static final String MD_LABEL_DEFAULT_SEARCHABLE = "android.app.default_searchable";
    private static final String MD_SEARCHABLE_SYSTEM_SEARCH = "*";
    private Context mContext;
    private ComponentName mCurrentGlobalSearchActivity;
    private List<ResolveInfo> mGlobalSearchActivities;
    private final IPackageManager mPm;
    private ArrayList<SearchableInfo> mSearchablesInGlobalSearchList;
    private ArrayList<SearchableInfo> mSearchablesList;
    private HashMap<ComponentName, SearchableInfo> mSearchablesMap;
    private int mUserId;
    private ComponentName mWebSearchActivity;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.search.Searchables.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.search.Searchables.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.search.Searchables.<clinit>():void");
    }

    public Searchables(Context context, int userId) {
        this.mSearchablesMap = null;
        this.mSearchablesList = null;
        this.mSearchablesInGlobalSearchList = null;
        this.mCurrentGlobalSearchActivity = null;
        this.mWebSearchActivity = null;
        this.mContext = context;
        this.mUserId = userId;
        this.mPm = AppGlobals.getPackageManager();
    }

    /* JADX WARNING: Missing block: B:10:?, code:
            r1 = r12.mPm.getActivityInfo(r13, 128, r12.mUserId);
     */
    /* JADX WARNING: Missing block: B:11:0x0019, code:
            r5 = null;
            r2 = r1.metaData;
     */
    /* JADX WARNING: Missing block: B:12:0x001d, code:
            if (r2 == null) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:13:0x001f, code:
            r5 = r2.getString(MD_LABEL_DEFAULT_SEARCHABLE);
     */
    /* JADX WARNING: Missing block: B:14:0x0026, code:
            if (r5 != null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:15:0x0028, code:
            r2 = r1.applicationInfo.metaData;
     */
    /* JADX WARNING: Missing block: B:16:0x002c, code:
            if (r2 == null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:17:0x002e, code:
            r5 = r2.getString(MD_LABEL_DEFAULT_SEARCHABLE);
     */
    /* JADX WARNING: Missing block: B:18:0x0035, code:
            if (r5 == null) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:20:0x003e, code:
            if (r5.equals(MD_SEARCHABLE_SYSTEM_SEARCH) == false) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:21:0x0040, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:25:0x0044, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x0045, code:
            android.util.Log.e(LOG_TAG, "Error getting activity info " + r4);
     */
    /* JADX WARNING: Missing block: B:27:0x005f, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:28:0x0060, code:
            r3 = r13.getPackageName();
     */
    /* JADX WARNING: Missing block: B:29:0x006b, code:
            if (r5.charAt(0) != '.') goto L_0x0097;
     */
    /* JADX WARNING: Missing block: B:30:0x006d, code:
            r6 = new android.content.ComponentName(r3, r3 + r5);
     */
    /* JADX WARNING: Missing block: B:31:0x0083, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            r7 = (android.app.SearchableInfo) r12.mSearchablesMap.get(r6);
     */
    /* JADX WARNING: Missing block: B:34:0x008e, code:
            if (r7 == null) goto L_0x009d;
     */
    /* JADX WARNING: Missing block: B:35:0x0090, code:
            r12.mSearchablesMap.put(r13, r7);
     */
    /* JADX WARNING: Missing block: B:36:0x0095, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:37:0x0096, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:38:0x0097, code:
            r6 = new android.content.ComponentName(r3, r5);
     */
    /* JADX WARNING: Missing block: B:39:0x009d, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:40:0x009e, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SearchableInfo getSearchableInfo(ComponentName activity) {
        synchronized (this) {
            SearchableInfo result = (SearchableInfo) this.mSearchablesMap.get(activity);
            if (result != null) {
                return result;
            }
        }
    }

    public void updateSearchableList() {
        HashMap<ComponentName, SearchableInfo> newSearchablesMap = new HashMap();
        ArrayList<SearchableInfo> newSearchablesList = new ArrayList();
        ArrayList<SearchableInfo> newSearchablesInGlobalSearchList = new ArrayList();
        Intent intent = new Intent("android.intent.action.SEARCH");
        long ident = Binder.clearCallingIdentity();
        try {
            List<ResolveInfo> searchList = queryIntentActivities(intent, 268435584);
            List<ResolveInfo> webSearchInfoList = queryIntentActivities(new Intent("android.intent.action.WEB_SEARCH"), 268435584);
            if (!(searchList == null && webSearchInfoList == null)) {
                int search_count = searchList == null ? 0 : searchList.size();
                int count = search_count + (webSearchInfoList == null ? 0 : webSearchInfoList.size());
                for (int ii = 0; ii < count; ii++) {
                    ResolveInfo info;
                    if (ii < search_count) {
                        info = (ResolveInfo) searchList.get(ii);
                    } else {
                        info = (ResolveInfo) webSearchInfoList.get(ii - search_count);
                    }
                    ActivityInfo ai = info.activityInfo;
                    if (newSearchablesMap.get(new ComponentName(ai.packageName, ai.name)) == null) {
                        SearchableInfo searchable = SearchableInfo.getActivityMetaData(this.mContext, ai, this.mUserId);
                        if (searchable != null) {
                            newSearchablesList.add(searchable);
                            newSearchablesMap.put(searchable.getSearchActivity(), searchable);
                            if (searchable.shouldIncludeInGlobalSearch()) {
                                newSearchablesInGlobalSearchList.add(searchable);
                            }
                        }
                    }
                }
            }
            List<ResolveInfo> newGlobalSearchActivities = findGlobalSearchActivities();
            ComponentName newGlobalSearchActivity = findGlobalSearchActivity(newGlobalSearchActivities);
            ComponentName newWebSearchActivity = findWebSearchActivity(newGlobalSearchActivity);
            synchronized (this) {
                this.mSearchablesMap = newSearchablesMap;
                this.mSearchablesList = newSearchablesList;
                this.mSearchablesInGlobalSearchList = newSearchablesInGlobalSearchList;
                this.mGlobalSearchActivities = newGlobalSearchActivities;
                this.mCurrentGlobalSearchActivity = newGlobalSearchActivity;
                this.mWebSearchActivity = newWebSearchActivity;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private List<ResolveInfo> findGlobalSearchActivities() {
        List<ResolveInfo> activities = queryIntentActivities(new Intent("android.search.action.GLOBAL_SEARCH"), 268500992);
        if (!(activities == null || activities.isEmpty())) {
            Collections.sort(activities, GLOBAL_SEARCH_RANKER);
        }
        return activities;
    }

    private ComponentName findGlobalSearchActivity(List<ResolveInfo> installed) {
        String searchProviderSetting = getGlobalSearchProviderSetting();
        if (!TextUtils.isEmpty(searchProviderSetting)) {
            ComponentName globalSearchComponent = ComponentName.unflattenFromString(searchProviderSetting);
            if (globalSearchComponent != null && isInstalled(globalSearchComponent)) {
                return globalSearchComponent;
            }
        }
        return getDefaultGlobalSearchProvider(installed);
    }

    private boolean isInstalled(ComponentName globalSearch) {
        Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
        intent.setComponent(globalSearch);
        List<ResolveInfo> activities = queryIntentActivities(intent, DumpState.DUMP_INSTALLS);
        if (activities == null || activities.isEmpty()) {
            return false;
        }
        return true;
    }

    private static final boolean isSystemApp(ResolveInfo res) {
        return (res.activityInfo.applicationInfo.flags & 1) != 0;
    }

    private ComponentName getDefaultGlobalSearchProvider(List<ResolveInfo> providerList) {
        if (providerList == null || providerList.isEmpty()) {
            Log.w(LOG_TAG, "No global search activity found");
            return null;
        }
        ActivityInfo ai = ((ResolveInfo) providerList.get(0)).activityInfo;
        return new ComponentName(ai.packageName, ai.name);
    }

    private String getGlobalSearchProviderSetting() {
        return Secure.getString(this.mContext.getContentResolver(), "search_global_search_activity");
    }

    private ComponentName findWebSearchActivity(ComponentName globalSearchActivity) {
        if (globalSearchActivity == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.WEB_SEARCH");
        intent.setPackage(globalSearchActivity.getPackageName());
        List<ResolveInfo> activities = queryIntentActivities(intent, DumpState.DUMP_INSTALLS);
        if (activities == null || activities.isEmpty()) {
            Log.w(LOG_TAG, "No web search activity found");
            return null;
        }
        ActivityInfo ai = ((ResolveInfo) activities.get(0)).activityInfo;
        return new ComponentName(ai.packageName, ai.name);
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        List<ResolveInfo> activities = null;
        try {
            return this.mPm.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, this.mUserId).getList();
        } catch (RemoteException e) {
            return activities;
        }
    }

    public synchronized ArrayList<SearchableInfo> getSearchablesList() {
        return new ArrayList(this.mSearchablesList);
    }

    public synchronized ArrayList<SearchableInfo> getSearchablesInGlobalSearchList() {
        return new ArrayList(this.mSearchablesInGlobalSearchList);
    }

    public synchronized ArrayList<ResolveInfo> getGlobalSearchActivities() {
        return new ArrayList(this.mGlobalSearchActivities);
    }

    public synchronized ComponentName getGlobalSearchActivity() {
        return this.mCurrentGlobalSearchActivity;
    }

    public synchronized ComponentName getWebSearchActivity() {
        return this.mWebSearchActivity;
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Searchable authorities:");
        synchronized (this) {
            if (this.mSearchablesList != null) {
                for (SearchableInfo info : this.mSearchablesList) {
                    pw.print("  ");
                    pw.println(info.getSuggestAuthority());
                }
            }
        }
    }
}
