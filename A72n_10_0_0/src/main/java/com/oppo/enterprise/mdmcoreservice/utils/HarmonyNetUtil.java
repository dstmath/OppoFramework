package com.oppo.enterprise.mdmcoreservice.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HarmonyNetUtil {
    private static final String HARMONY_NET_MODE = "harmony_net_mode";
    public static final int MODE_BLACK_LIST = 2;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_WHITE_LIST = 1;
    private static final String PKG_ANDROID_BROWSER = "com.android.browser";
    private static final String PKG_COLOROS_BROWSER = "com.coloros.browser";
    private static final String PKG_HEYTAP_BROWSER = "com.heytap.browser";
    private static final String PKG_NEARME_BROWSER = "com.nearme.browser";
    public static final String[] PROJECTION_BOOKMARK = {"_id", BookmarkColumn.DATE_CREATED, "title", "url"};
    private static final String[] PROJECTION_HARMONY_NET = {HarmonyNetColumn.RULE, "type", HarmonyNetColumn.UPDATE_TIME};
    public static final String[] PROJECTION_HISTORY = {"_id", HistoryColumn.DATE_LAST_VISITED, "title", "url", HistoryColumn.VISITS};
    private static final String TAG = "HarmonyNetUtil";
    private static volatile HarmonyNetUtil sInstance = null;
    private final String BOOKMARK_WHERE;
    private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS", Locale.getDefault());
    private final Uri mContentUriBookmarks;
    private final Uri mContentUriHarmonyNet;
    private final Uri mContentUriHistory;
    private final ContentResolver mResolver;

    public interface BookmarkColumn {
        public static final String DATE_CREATED = "created";
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String _ID = "_id";
    }

    private interface HarmonyNetColumn {
        public static final String RULE = "rule";
        public static final String TYPE = "type";
        public static final String UPDATE_TIME = "update_time";
    }

    public interface HistoryColumn {
        public static final String DATE_LAST_VISITED = "date";
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String VISITS = "visits";
        public static final String _ID = "_id";
    }

    public static HarmonyNetUtil getInstance(Context context) {
        if (sInstance == null) {
            synchronized (HarmonyNetUtil.class) {
                if (sInstance == null) {
                    sInstance = new HarmonyNetUtil(context);
                }
            }
        }
        return sInstance;
    }

    private HarmonyNetUtil(Context context) {
        String browserPkg;
        Context context2 = context.getApplicationContext();
        this.mResolver = context2.getContentResolver();
        if (isApkInstalled(context2, PKG_HEYTAP_BROWSER)) {
            browserPkg = PKG_HEYTAP_BROWSER;
        } else if (isApkInstalled(context2, PKG_COLOROS_BROWSER)) {
            browserPkg = PKG_COLOROS_BROWSER;
        } else if (isApkInstalled(context2, PKG_NEARME_BROWSER)) {
            browserPkg = PKG_NEARME_BROWSER;
        } else {
            browserPkg = PKG_ANDROID_BROWSER;
        }
        this.mContentUriHarmonyNet = Uri.withAppendedPath(Uri.parse("content://" + browserPkg), "harmony_net");
        if (PKG_ANDROID_BROWSER.equals(browserPkg)) {
            Uri parse = Uri.parse(String.format(Locale.US, "content://%s/bookmarks", browserPkg));
            this.mContentUriBookmarks = parse;
            this.mContentUriHistory = parse;
            this.BOOKMARK_WHERE = "bookmark > 0 AND deleted == 0";
            return;
        }
        this.mContentUriHistory = Uri.parse(String.format(Locale.US, "content://%s/history", browserPkg));
        this.mContentUriBookmarks = Uri.parse(String.format(Locale.US, "content://%s/bookmarks", browserPkg));
        this.BOOKMARK_WHERE = "folder == 0 AND deleted == 0";
    }

    public void setHarmonyNetMode(int mode) {
        ContentValues values = buildContentValues(mode);
        try {
            if (this.mResolver.update(this.mContentUriHarmonyNet, values, "rule=?", new String[]{HARMONY_NET_MODE}) <= 0) {
                values.put(HarmonyNetColumn.RULE, HARMONY_NET_MODE);
                this.mResolver.insert(this.mContentUriHarmonyNet, values);
            }
        } catch (Throwable e) {
            Log.w(TAG, "setHarmonyNetMode", e);
        }
    }

    public int getHarmonyNetMode() {
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(this.mContentUriHarmonyNet, PROJECTION_HARMONY_NET, "rule == 'harmony_net_mode'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                switch (type) {
                    case 1:
                    case 2:
                        closeQuietly(cursor);
                        return type;
                    default:
                        closeQuietly(cursor);
                        return 0;
                }
            }
        } catch (Throwable th) {
            closeQuietly(null);
            throw th;
        }
        closeQuietly(cursor);
        return 0;
    }

    public List<String> getHarmonyNetRules(int mode) {
        List<String> rules = new ArrayList<>();
        if (mode != 1 && mode != 2) {
            return rules;
        }
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(this.mContentUriHarmonyNet, PROJECTION_HARMONY_NET, "type == '" + mode + "'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int indexOfRule = cursor.getColumnIndex(HarmonyNetColumn.RULE);
                do {
                    String rule = cursor.getString(indexOfRule);
                    if (!HARMONY_NET_MODE.equals(rule)) {
                        rules.add(rule);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Throwable th) {
            closeQuietly(null);
            throw th;
        }
        closeQuietly(cursor);
        return rules;
    }

    public void addHarmonyNetRule(String rule, boolean whiteList) {
        if (rule != null && rule.length() != 0 && !HARMONY_NET_MODE.equals(rule)) {
            String rule2 = fixRule(rule);
            ContentValues values = buildContentValues(whiteList ? 1 : 2);
            values.put(HarmonyNetColumn.RULE, rule2);
            try {
                if (this.mResolver.update(this.mContentUriHarmonyNet, values, "rule=?", new String[]{rule2}) <= 0) {
                    this.mResolver.insert(this.mContentUriHarmonyNet, values);
                }
            } catch (Throwable e) {
                Log.w(TAG, "addHarmonyNetRule", e);
            }
        }
    }

    public void addHarmonyNetRules(List<String> rules, boolean whiteList) {
        if (rules != null && !rules.isEmpty()) {
            List<ContentValues> list = new ArrayList<>();
            try {
                for (String rule : rules) {
                    if (!HARMONY_NET_MODE.equals(rule)) {
                        String rule2 = fixRule(rule);
                        ContentValues values = buildContentValues(whiteList ? 1 : 2);
                        values.put(HarmonyNetColumn.RULE, rule2);
                        if (this.mResolver.update(this.mContentUriHarmonyNet, values, "rule=?", new String[]{rule2}) <= 0) {
                            list.add(values);
                        }
                    }
                }
                this.mResolver.bulkInsert(this.mContentUriHarmonyNet, (ContentValues[]) list.toArray(new ContentValues[0]));
            } catch (Throwable e) {
                Log.w(TAG, "addHarmonyNetRules", e);
            }
        }
    }

    public int delHarmonyNetRule(String rule, boolean whiteList) {
        if (rule == null || rule.length() == 0 || HARMONY_NET_MODE.equals(rule)) {
            return 0;
        }
        String rule2 = fixRule(rule);
        try {
            ContentResolver contentResolver = this.mResolver;
            Uri uri = this.mContentUriHarmonyNet;
            StringBuilder sb = new StringBuilder();
            sb.append("rule='");
            sb.append(rule2);
            sb.append("' and ");
            sb.append("type");
            sb.append("='");
            sb.append(whiteList ? 1 : 2);
            sb.append("'");
            return contentResolver.delete(uri, sb.toString(), null);
        } catch (Throwable e) {
            Log.w(TAG, "delHarmonyNetRule", e);
            return 0;
        }
    }

    public int delHarmonyNetRules(List<String> rules, boolean whiteList) {
        if (rules == null || rules.isEmpty()) {
            return 0;
        }
        try {
            ContentResolver contentResolver = this.mResolver;
            Uri uri = this.mContentUriHarmonyNet;
            StringBuilder sb = new StringBuilder();
            sb.append("type='");
            sb.append(whiteList ? 1 : 2);
            sb.append("' and ");
            sb.append(HarmonyNetColumn.RULE);
            sb.append(" in (");
            sb.append(joinRules(rules));
            sb.append(")");
            return contentResolver.delete(uri, sb.toString(), null);
        } catch (Throwable e) {
            Log.w(TAG, "delHarmonyNetRules", e);
            return 0;
        }
    }

    public int clearHarmonyNetRules(boolean whiteList) {
        try {
            ContentResolver contentResolver = this.mResolver;
            Uri uri = this.mContentUriHarmonyNet;
            StringBuilder sb = new StringBuilder();
            sb.append("type='");
            sb.append(whiteList ? 1 : 2);
            sb.append("' and ");
            sb.append(HarmonyNetColumn.RULE);
            sb.append(" <> '");
            sb.append(HARMONY_NET_MODE);
            sb.append("'");
            return contentResolver.delete(uri, sb.toString(), null);
        } catch (Throwable e) {
            Log.w(TAG, "clearHarmonyNetRules", e);
            return 0;
        }
    }

    public Cursor queryBrowserBookmarks(int position, int pageSize) {
        try {
            return this.mResolver.query(this.mContentUriBookmarks, PROJECTION_BOOKMARK, this.BOOKMARK_WHERE, null, String.format("%s DESC limit %s,%s", BookmarkColumn.DATE_CREATED, String.valueOf(position), String.valueOf(pageSize)));
        } catch (Throwable e) {
            Log.w(TAG, "queryBrowserBookmarks", e);
            return null;
        }
    }

    public Cursor queryBrowserHistory(int position, int pageSize) {
        try {
            return this.mResolver.query(this.mContentUriHistory, PROJECTION_HISTORY, "visits > 0", null, String.format("%s DESC limit %s,%s", HistoryColumn.DATE_LAST_VISITED, String.valueOf(position), String.valueOf(pageSize)));
        } catch (Throwable e) {
            Log.w(TAG, "queryBrowserHistory", e);
            return null;
        }
    }

    private ContentValues buildContentValues(int mode) {
        String timeText = this.TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        ContentValues values = new ContentValues();
        switch (mode) {
            case 1:
                values.put("type", (Integer) 1);
                break;
            case 2:
                values.put("type", (Integer) 2);
                break;
            default:
                values.put("type", (Integer) 0);
                break;
        }
        values.put(HarmonyNetColumn.UPDATE_TIME, timeText);
        return values;
    }

    private static String fixRule(String rule) {
        if (rule == null) {
            return rule;
        }
        String rule2 = rule.replace('*', '%');
        if (rule2.startsWith("%") || rule2.endsWith("%")) {
            return rule2;
        }
        return "%" + rule2 + "%";
    }

    private static String joinRules(List<String> rules) {
        StringBuilder builder = new StringBuilder();
        int length = rules.size();
        for (int i = 0; i < length; i++) {
            String rule = rules.get(i);
            if (rule != null && rule.length() > 0 && !rule.contains(HARMONY_NET_MODE)) {
                String rule2 = fixRule(rule);
                if (i > 0) {
                    builder.append(",");
                }
                builder.append("'");
                builder.append(rule2);
                builder.append("'");
            }
        }
        return builder.toString();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable th) {
            }
        }
    }

    public static boolean isApkInstalled(Context context, String pkgName) {
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 128) != null) {
                return true;
            }
            return false;
        } catch (Throwable th) {
            return false;
        }
    }
}
