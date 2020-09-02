package cn.teddymobile.free.anteater.rule.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import cn.teddymobile.free.anteater.resources.UriConstants;
import cn.teddymobile.free.anteater.rule.Rule;
import com.color.util.ColorLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class RuleUtils {
    public static final boolean DBG;
    public static final boolean LOG_DEBUG = SystemProperties.getBoolean("log.favorite.debug", false);
    public static final boolean LOG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String TAG = "RuleUtils";
    private static final Uri URI_RULE = new Uri.Builder().scheme("content").authority(UriConstants.AUTHORITY).path(UriConstants.PATH_RULE).build();

    static {
        boolean z = false;
        if (LOG_PANIC || LOG_DEBUG) {
            z = true;
        }
        DBG = z;
    }

    public static String queryRuleFromProvider(Context context) {
        StringBuilder sb;
        long spend;
        boolean z;
        long start = SystemClock.uptimeMillis();
        String result = null;
        Cursor cursor = null;
        try {
            String[] whereArgs = {context.getPackageName()};
            Cursor cursor2 = context.getContentResolver().query(URI_RULE, new String[]{"data"}, "package_name = ?", whereArgs, null);
            if (cursor2 != null && cursor2.moveToFirst()) {
                result = cursor2.getString(cursor2.getColumnIndex("data"));
            }
            if (cursor2 != null) {
                cursor2.close();
            }
            spend = SystemClock.uptimeMillis() - start;
            z = DBG;
            sb = new StringBuilder();
        } catch (Exception e) {
            ColorLog.w(DBG, TAG, e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            spend = SystemClock.uptimeMillis() - start;
            z = DBG;
            sb = new StringBuilder();
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            boolean z2 = DBG;
            ColorLog.i(z2, TAG, "queryRuleFromProvider: spend=" + (SystemClock.uptimeMillis() - start) + "ms");
            throw th;
        }
        sb.append("queryRuleFromProvider: spend=");
        sb.append(spend);
        sb.append("ms");
        ColorLog.i(z, TAG, sb.toString());
        return result;
    }

    public static List<Rule> parseRule(String data) {
        long spend;
        boolean z;
        StringBuilder sb;
        List<Rule> ruleList = new ArrayList<>();
        if (!TextUtils.isEmpty(data)) {
            long start = SystemClock.uptimeMillis();
            try {
                JSONArray ruleArray = new JSONArray(data);
                for (int i = 0; i < ruleArray.length(); i++) {
                    JSONObject ruleObject = ruleArray.getJSONObject(i);
                    Rule rule = new Rule();
                    rule.loadFromJSON(ruleObject);
                    ruleList.add(rule);
                }
                spend = SystemClock.uptimeMillis() - start;
                z = DBG;
                sb = new StringBuilder();
            } catch (Exception e) {
                ColorLog.w(DBG, TAG, e.getMessage());
                spend = SystemClock.uptimeMillis() - start;
                z = DBG;
                sb = new StringBuilder();
            } catch (Throwable th) {
                boolean z2 = DBG;
                ColorLog.i(z2, TAG, "parseRule=" + (SystemClock.uptimeMillis() - start) + "ms");
                throw th;
            }
            sb.append("parseRule=");
            sb.append(spend);
            sb.append("ms");
            ColorLog.i(z, TAG, sb.toString());
        }
        return ruleList;
    }
}
