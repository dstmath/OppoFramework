package com.mediatek.custom;

import android.content.res.Resources;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomPropInterface {
    private static String getBrowserVersion() {
        String result = null;
        try {
            Matcher matcher = Pattern.compile("\\s+AppleWebKit\\/(\\d+\\.?\\d*)\\s+").matcher(Resources.getSystem().getText(Class.forName("com.android.internal.R$string").getDeclaredField("web_user_agent").getInt(null)).toString());
            if (matcher.find()) {
                Log.i("CustomProp", "getBrowserVersion->matcher.find:true matcher.group(0):" + matcher.group(0) + " matcher.group(1):" + matcher.group(1));
                result = matcher.group(1);
            } else {
                Log.i("CustomProp", "getBrowserVersion->matcher.find:false");
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | RuntimeException e) {
        }
        Log.i("CustomProp", "getBrowserVersion->result:" + result);
        return result;
    }

    /* JADX INFO: Multiple debug info for r1v5 java.lang.String: [D('calendar' java.util.Calendar), D('result' java.lang.String)] */
    private static String getReleaseDate(String buildDate) {
        Date date;
        Log.i("CustomProp", "getReleaseDate->buildDate[" + buildDate + "]");
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", new Locale("en", "CN"));
        try {
            date = format.parse(buildDate);
        } catch (ParseException e) {
            date = null;
        }
        if (date == null) {
            return null;
        }
        Log.i("CustomProp", "date: " + date);
        Calendar calendar = format.getCalendar();
        return String.format("%02d.%02d.%d", Integer.valueOf(calendar.get(2) + 1), Integer.valueOf(calendar.get(5)), Integer.valueOf(calendar.get(1)));
    }
}
