package com.color.util;

import android.content.Context;
import android.provider.Settings.System;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import libcore.icu.LocaleData;

public class ColorDateUtils {
    private static final String EXP_VERSION_NAME = "oppo.version.exp";
    private Context mContext;
    private boolean mIsExpVersion = false;

    public ColorDateUtils(Context context) {
        this.mContext = context;
        this.mIsExpVersion = context.getPackageManager().hasSystemFeature(EXP_VERSION_NAME);
    }

    private boolean is24Hours() {
        if ("24".equals(System.getString(this.mContext.getContentResolver(), "time_12_24"))) {
            return true;
        }
        return false;
    }

    private boolean isChinese() {
        String country = this.mContext.getResources().getConfiguration().locale.toString();
        if (country == null || (!country.equalsIgnoreCase("zh_CN") && !country.equalsIgnoreCase("zh_TW") && !country.equalsIgnoreCase("zh_HK"))) {
            return false;
        }
        return true;
    }

    private boolean isUgChinese() {
        String country = this.mContext.getResources().getConfiguration().locale.toString();
        if (country == null || !country.equalsIgnoreCase("ug_CN")) {
            return false;
        }
        return true;
    }

    public String getTime(Date date) {
        return DateFormat.getTimeFormat(this.mContext).format(date);
    }

    public String getTimeToSeconds(Date date) {
        SimpleDateFormat sd;
        String time = "";
        String timeFormat = "HH:mm:ss";
        if (!isChinese()) {
            if (is24Hours()) {
                timeFormat = DateFormat.getBestDateTimePattern(this.mContext.getResources().getConfiguration().locale, "Hms");
            } else {
                timeFormat = DateFormat.getBestDateTimePattern(this.mContext.getResources().getConfiguration().locale, "hms");
            }
            sd = new SimpleDateFormat(timeFormat);
        } else if (is24Hours()) {
            sd = new SimpleDateFormat("HH:mm:ss");
        } else {
            sd = new SimpleDateFormat("ah:mm:ss ");
        }
        return sd.format(date);
    }

    public String getYMDDate(Date date, boolean hasSpace) {
        String time = "";
        if (isChinese()) {
            return new SimpleDateFormat(this.mContext.getResources().getString(201590125)).format(date);
        }
        if (isUgChinese()) {
            SimpleDateFormat sd;
            if (hasSpace) {
                sd = new SimpleDateFormat("yyyy ,dd-MMMM");
            } else {
                sd = new SimpleDateFormat("yyyy ,dd-MMM");
            }
            return sd.format(date);
        }
        int flag;
        if (hasSpace) {
            flag = 20;
        } else {
            flag = 65556;
        }
        return DateUtils.formatDateTime(this.mContext, date.getTime(), flag);
    }

    public String getYMDWDate(Date date, boolean hasSpace, boolean hasYear) {
        SimpleDateFormat sd = null;
        String time = "";
        if (isChinese()) {
            if (hasYear) {
                sd = new SimpleDateFormat(this.mContext.getResources().getString(201590123));
            } else {
                sd = new SimpleDateFormat(this.mContext.getResources().getString(201590124));
            }
            return sd.format(date);
        } else if (isUgChinese()) {
            if (hasSpace && hasYear) {
                sd = new SimpleDateFormat("yyyy ,dd-MMMM EEEE");
            } else if (!hasSpace && hasYear) {
                sd = new SimpleDateFormat("yyyy ,dd-MMM E");
            } else if (!hasSpace && (hasYear ^ 1) != 0) {
                sd = new SimpleDateFormat("dd-MMM E");
            } else if (hasSpace && (hasYear ^ 1) != 0) {
                sd = new SimpleDateFormat("dd-MMMM EEEE");
            }
            return sd.format(date);
        } else {
            int flag = 16;
            if (hasSpace && hasYear) {
                flag = 22;
            } else if (!hasSpace && hasYear) {
                flag = 98326;
            } else if (!hasSpace && (hasYear ^ 1) != 0) {
                flag = 98330;
            } else if (hasSpace && (hasYear ^ 1) != 0) {
                flag = 26;
            }
            return DateUtils.formatDateTime(this.mContext, date.getTime(), flag);
        }
    }

    public String getMDDate(Date date, boolean hasSpace) {
        String time = "";
        if (isChinese()) {
            return new SimpleDateFormat(this.mContext.getResources().getString(201590128)).format(date);
        }
        if (isUgChinese()) {
            SimpleDateFormat sd;
            if (hasSpace) {
                sd = new SimpleDateFormat("dd-MMMM");
            } else {
                sd = new SimpleDateFormat("dd-MMM");
            }
            return sd.format(date);
        }
        int flag;
        if (hasSpace) {
            flag = 24;
        } else {
            flag = 65560;
        }
        return DateUtils.formatDateTime(this.mContext, date.getTime(), flag);
    }

    public String getDashDate(Date date) {
        SimpleDateFormat sd;
        String time = "";
        if (isChinese() || isUgChinese()) {
            sd = new SimpleDateFormat("yyyy/M/d");
        } else {
            sd = new SimpleDateFormat(LocaleData.get(this.mContext.getResources().getConfiguration().locale).shortDateFormat.replaceAll("\\byy\\b", "y"));
        }
        return sd.format(date);
    }

    public String getDashFormat() {
        String formatter = "";
        if (isChinese() || isUgChinese()) {
            return "yyyy/M/d";
        }
        return LocaleData.get(this.mContext.getResources().getConfiguration().locale).shortDateFormat.replaceAll("\\byy\\b", "y");
    }

    public String getLongWeekDate(Date date) {
        String time = "";
        return new SimpleDateFormat("EEEE").format(date);
    }

    public String getShortWeekDate(Date date) {
        String time = "";
        return new SimpleDateFormat("E").format(date);
    }
}
