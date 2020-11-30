package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cat.ResponseData;
import java.io.ByteArrayOutputStream;

/* access modifiers changed from: package-private */
/* compiled from: MtkResponseData */
public class MtkProvideLocalInformationResponseData extends ResponseData {
    private int day;
    private int hour;
    private byte[] language;
    private int mBatteryState;
    private boolean mIsBatteryState = true;
    private boolean mIsDate = false;
    private boolean mIsLanguage = false;
    private int minute;
    private int month;
    private int second;
    private int timezone;
    private int year;

    public MtkProvideLocalInformationResponseData(int year2, int month2, int day2, int hour2, int minute2, int second2, int timezone2) {
        this.year = year2;
        this.month = month2;
        this.day = day2;
        this.hour = hour2;
        this.minute = minute2;
        this.second = second2;
        this.timezone = timezone2;
    }

    public MtkProvideLocalInformationResponseData(byte[] language2) {
        this.language = language2;
    }

    public MtkProvideLocalInformationResponseData(int batteryState) {
        this.mBatteryState = batteryState;
    }

    public void format(ByteArrayOutputStream buf) {
        if (this.mIsDate) {
            buf.write(ComprehensionTlvTag.DATE_TIME_AND_TIMEZONE.value() | 128);
            buf.write(7);
            buf.write(this.year);
            buf.write(this.month);
            buf.write(this.day);
            buf.write(this.hour);
            buf.write(this.minute);
            buf.write(this.second);
            buf.write(this.timezone);
        } else if (this.mIsLanguage) {
            buf.write(ComprehensionTlvTag.LANGUAGE.value() | 128);
            buf.write(2);
            for (byte b : this.language) {
                buf.write(b);
            }
        } else if (this.mIsBatteryState) {
            buf.write(ComprehensionTlvTag.BATTERY_STATE.value() | 128);
            buf.write(1);
            buf.write(this.mBatteryState);
        }
    }
}
