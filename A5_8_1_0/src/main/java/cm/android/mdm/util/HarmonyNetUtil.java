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
    public static final String[] PROJECTION = new String[]{HistoryColumn._ID, "date", "title", "url", "visits"};
    private static final String TAG = HarmonyNetUtil.class.getSimpleName();

    private interface HarmonyNetColumn {
        public static final String RULE = "rule";
        public static final String TYPE = "type";
        public static final String UPDATE_TIME = "update_time";
    }

    private static class HarmonyNet implements HarmonyNetColumn {
        private static final Uri CONTENT_URI = Uri.withAppendedPath(Impl.AUTHORITY_URI, "harmony_net");

        private HarmonyNet() {
        }
    }

    public interface HistoryColumn {
        public static final String DATE_LAST_VISITED = "date";
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String VISITS = "visits";
        public static final String _ID = "_id";
    }

    private static class History implements HistoryColumn {
        private static final Uri CONTENT_URI = Uri.withAppendedPath(Impl.AUTHORITY_URI, "history");

        private History() {
        }
    }

    private static class Impl {
        private static final Uri AUTHORITY_URI = Uri.parse("content://com.android.browser");
        private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS", Locale.getDefault());

        private Impl() {
        }
    }

    private HarmonyNetUtil() {
    }

    public static void setHarmonyNetMode(ContentResolver resolver, int mode) {
        ContentValues values = buildContentValues(mode);
        try {
            String[] strArr = new String[MODE_WHITE_LIST];
            strArr[MODE_NORMAL] = HARMONY_NET_MODE;
            if (resolver.update(HarmonyNet.CONTENT_URI, values, "rule=?", strArr) <= 0) {
                values.put(HarmonyNetColumn.RULE, HARMONY_NET_MODE);
                resolver.insert(HarmonyNet.CONTENT_URI, values);
            }
        } catch (Throwable e) {
            Log.w(TAG, "setHarmonyNetMode", e);
        }
    }

    public static void addHarmonyNetRule(ContentResolver resolver, String rule, boolean whiteList) {
        int i = MODE_WHITE_LIST;
        if (rule != null && rule.length() != 0 && !HARMONY_NET_MODE.equals(rule)) {
            rule = fixRule(rule);
            if (!whiteList) {
                i = MODE_BLACK_LIST;
            }
            ContentValues values = buildContentValues(i);
            values.put(HarmonyNetColumn.RULE, rule);
            try {
                String[] strArr = new String[MODE_WHITE_LIST];
                strArr[MODE_NORMAL] = rule;
                if (resolver.update(HarmonyNet.CONTENT_URI, values, "rule=?", strArr) <= 0) {
                    resolver.insert(HarmonyNet.CONTENT_URI, values);
                }
            } catch (Throwable e) {
                Log.w(TAG, "addHarmonyNetRule", e);
            }
        }
    }

    public static void addHarmonyNetRules(ContentResolver resolver, List<String> rules, boolean whiteList) {
        if (rules != null && !rules.isEmpty()) {
            List<ContentValues> list = new ArrayList();
            try {
                for (String rule : rules) {
                    String rule2;
                    if (!HARMONY_NET_MODE.equals(rule2)) {
                        rule2 = fixRule(rule2);
                        ContentValues values = buildContentValues(whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST);
                        values.put(HarmonyNetColumn.RULE, rule2);
                        String[] strArr = new String[MODE_WHITE_LIST];
                        strArr[MODE_NORMAL] = rule2;
                        if (resolver.update(HarmonyNet.CONTENT_URI, values, "rule=?", strArr) <= 0) {
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
        try {
            return resolver.delete(HarmonyNet.CONTENT_URI, "rule='" + fixRule(rule) + "' and " + "type" + "='" + (whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST) + "'", null);
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
            return resolver.delete(HarmonyNet.CONTENT_URI, "type='" + (whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST) + "' and " + HarmonyNetColumn.RULE + " in (" + joinRules(rules) + ")", null);
        } catch (Throwable e) {
            Log.w(TAG, "delHarmonyNetRules", e);
            return MODE_NORMAL;
        }
    }

    public static int clearHarmonyNetRules(ContentResolver resolver, boolean whiteList) {
        try {
            return resolver.delete(HarmonyNet.CONTENT_URI, "type='" + (whiteList ? MODE_WHITE_LIST : MODE_BLACK_LIST) + "' and " + HarmonyNetColumn.RULE + " <> '" + HARMONY_NET_MODE + "'", null);
        } catch (Throwable e) {
            Log.w(TAG, "clearHarmonyNetRules", e);
            return MODE_NORMAL;
        }
    }

    public static Cursor queryBrowserHistory(ContentResolver resolver, int position, int pageSize) {
        try {
            return resolver.query(History.CONTENT_URI, PROJECTION, "visits > 0", null, String.format("%s DESC limit %s,%s", new Object[]{"date", String.valueOf(position), String.valueOf(pageSize)}));
        } catch (Throwable e) {
            Log.w(TAG, "queryBrowserHistory", e);
            return null;
        }
    }

    private static ContentValues buildContentValues(int mode) {
        String timeText = Impl.TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        ContentValues values = new ContentValues();
        switch (mode) {
            case MODE_WHITE_LIST /*1*/:
                values.put("type", Integer.valueOf(MODE_WHITE_LIST));
                break;
            case MODE_BLACK_LIST /*2*/:
                values.put("type", Integer.valueOf(MODE_BLACK_LIST));
                break;
            default:
                values.put("type", Integer.valueOf(MODE_NORMAL));
                break;
        }
        values.put(HarmonyNetColumn.UPDATE_TIME, timeText);
        return values;
    }

    private static String fixRule(String rule) {
        if (rule == null) {
            return rule;
        }
        rule = rule.replace('*', '%');
        if (!rule.startsWith("%") ? rule.endsWith("%") : true) {
            return rule;
        }
        return "%" + rule + "%";
    }

    private static String joinRules(List<String> rules) {
        StringBuilder builder = new StringBuilder();
        int length = rules.size();
        for (int i = MODE_NORMAL; i < length; i += MODE_WHITE_LIST) {
            String rule = (String) rules.get(i);
            if (!(rule == null || rule.length() <= 0 || (rule.contains(HARMONY_NET_MODE) ^ MODE_WHITE_LIST) == 0)) {
                rule = fixRule(rule);
                if (i > 0) {
                    builder.append(",");
                }
                builder.append("'");
                builder.append(rule);
                builder.append("'");
            }
        }
        return builder.toString();
    }
}
