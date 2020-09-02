package cm.android.mdm.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HarmonyNetUtil {
    private static final String HARMONY_NET_MODE = "harmony_net_mode";
    private static final int MODE_BLACK_LIST = 2;
    private static final int MODE_NORMAL = 0;
    private static final int MODE_WHITE_LIST = 1;
    public static final String[] PROJECTION = {"_id", "date", "title", "url", "visits"};
    private static final String TAG = HarmonyNetUtil.class.getSimpleName();

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

    private static class Impl {
        /* access modifiers changed from: private */
        public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.browser");
        /* access modifiers changed from: private */
        public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS", Locale.getDefault());

        private Impl() {
        }
    }

    private static class History implements HistoryColumn {
        /* access modifiers changed from: private */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Impl.AUTHORITY_URI, "history");

        private History() {
        }
    }

    private static class HarmonyNet implements HarmonyNetColumn {
        /* access modifiers changed from: private */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Impl.AUTHORITY_URI, "harmony_net");

        private HarmonyNet() {
        }
    }

    private HarmonyNetUtil() {
    }

    public static void setHarmonyNetMode(ContentResolver resolver, int mode) {
        ContentValues values = buildContentValues(mode);
        try {
            if (resolver.update(HarmonyNet.CONTENT_URI, values, "rule=?", new String[]{HARMONY_NET_MODE}) <= 0) {
                values.put(HarmonyNetColumn.RULE, HARMONY_NET_MODE);
                resolver.insert(HarmonyNet.CONTENT_URI, values);
            }
        } catch (Throwable e) {
            Log.w(TAG, "setHarmonyNetMode", e);
        }
    }

    public static void addHarmonyNetRule(ContentResolver resolver, String rule, boolean whiteList) {
        if (rule != null && rule.length() != 0 && !HARMONY_NET_MODE.equals(rule)) {
            String rule2 = fixRule(rule);
            ContentValues values = buildContentValues(whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST);
            values.put(HarmonyNetColumn.RULE, rule2);
            try {
                Uri access$100 = HarmonyNet.CONTENT_URI;
                String[] strArr = new String[MODE_WHITE_LIST];
                strArr[MODE_NORMAL] = rule2;
                if (resolver.update(access$100, values, "rule=?", strArr) <= 0) {
                    resolver.insert(HarmonyNet.CONTENT_URI, values);
                }
            } catch (Throwable e) {
                Log.w(TAG, "addHarmonyNetRule", e);
            }
        }
    }

    public static void addHarmonyNetRules(ContentResolver resolver, List<String> rules, boolean whiteList) {
        if (rules != null && !rules.isEmpty()) {
            List<ContentValues> list = new ArrayList<>();
            try {
                for (String rule : rules) {
                    if (!HARMONY_NET_MODE.equals(rule)) {
                        String rule2 = fixRule(rule);
                        ContentValues values = buildContentValues(whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST);
                        values.put(HarmonyNetColumn.RULE, rule2);
                        Uri access$100 = HarmonyNet.CONTENT_URI;
                        String[] strArr = new String[MODE_WHITE_LIST];
                        strArr[MODE_NORMAL] = rule2;
                        if (resolver.update(access$100, values, "rule=?", strArr) <= 0) {
                            list.add(values);
                        }
                    }
                }
                resolver.bulkInsert(HarmonyNet.CONTENT_URI, (ContentValues[]) list.toArray(new ContentValues[MODE_NORMAL]));
            } catch (Throwable e) {
                Log.w(TAG, "addHarmonyNetRules", e);
            }
        }
    }

    public static int delHarmonyNetRule(ContentResolver resolver, String rule, boolean whiteList) {
        if (rule == null || rule.length() == 0 || HARMONY_NET_MODE.equals(rule)) {
            return MODE_NORMAL;
        }
        String rule2 = fixRule(rule);
        try {
            Uri access$100 = HarmonyNet.CONTENT_URI;
            StringBuilder sb = new StringBuilder();
            sb.append("rule='");
            sb.append(rule2);
            sb.append("' and ");
            sb.append("type");
            sb.append("='");
            sb.append(whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST);
            sb.append("'");
            return resolver.delete(access$100, sb.toString(), null);
        } catch (Throwable e) {
            Log.w(TAG, "delHarmonyNetRule", e);
            return MODE_NORMAL;
        }
    }

    public static int delHarmonyNetRules(ContentResolver resolver, List<String> rules, boolean whiteList) {
        if (rules == null || rules.isEmpty()) {
            return MODE_NORMAL;
        }
        try {
            Uri access$100 = HarmonyNet.CONTENT_URI;
            StringBuilder sb = new StringBuilder();
            sb.append("type='");
            sb.append(whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST);
            sb.append("' and ");
            sb.append(HarmonyNetColumn.RULE);
            sb.append(" in (");
            sb.append(joinRules(rules));
            sb.append(")");
            return resolver.delete(access$100, sb.toString(), null);
        } catch (Throwable e) {
            Log.w(TAG, "delHarmonyNetRules", e);
            return MODE_NORMAL;
        }
    }

    public static int clearHarmonyNetRules(ContentResolver resolver, boolean whiteList) {
        try {
            Uri access$100 = HarmonyNet.CONTENT_URI;
            StringBuilder sb = new StringBuilder();
            sb.append("type='");
            sb.append(whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST);
            sb.append("' and ");
            sb.append(HarmonyNetColumn.RULE);
            sb.append(" <> '");
            sb.append(HARMONY_NET_MODE);
            sb.append("'");
            return resolver.delete(access$100, sb.toString(), null);
        } catch (Throwable e) {
            Log.w(TAG, "clearHarmonyNetRules", e);
            return MODE_NORMAL;
        }
    }

    public static Cursor queryBrowserHistory(ContentResolver resolver, int position, int pageSize) {
        Object[] objArr = new Object[3];
        objArr[MODE_NORMAL] = "date";
        objArr[MODE_WHITE_LIST] = String.valueOf(position);
        objArr[MODE_BLACK_LIST] = String.valueOf(pageSize);
        try {
            return resolver.query(History.CONTENT_URI, PROJECTION, "visits > 0", null, String.format("%s DESC limit %s,%s", objArr));
        } catch (Throwable e) {
            Log.w(TAG, "queryBrowserHistory", e);
            return null;
        }
    }

    private static ContentValues buildContentValues(int mode) {
        String timeText = Impl.TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        ContentValues values = new ContentValues();
        if (mode == MODE_WHITE_LIST) {
            values.put("type", Integer.valueOf((int) MODE_WHITE_LIST));
        } else if (mode != MODE_BLACK_LIST) {
            values.put("type", Integer.valueOf((int) MODE_NORMAL));
        } else {
            values.put("type", Integer.valueOf((int) MODE_BLACK_LIST));
        }
        values.put(HarmonyNetColumn.UPDATE_TIME, timeText);
        return values;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.String.replace(char, char):java.lang.String}
     arg types: [int, int]
     candidates:
      ClspMth{java.lang.String.replace(java.lang.CharSequence, java.lang.CharSequence):java.lang.String}
      ClspMth{java.lang.String.replace(char, char):java.lang.String} */
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
        for (int i = MODE_NORMAL; i < length; i += MODE_WHITE_LIST) {
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
}
