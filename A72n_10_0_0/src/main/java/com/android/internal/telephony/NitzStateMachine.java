package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.TimestampedValue;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface NitzStateMachine {
    void dumpLogs(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr);

    void dumpState(PrintWriter printWriter);

    NitzData getCachedNitzData();

    String getSavedTimeZoneId();

    void handleNetworkAvailable();

    void handleNetworkCountryCodeSet(boolean z);

    void handleNetworkCountryCodeUnavailable();

    void handleNitzReceived(TimestampedValue<NitzData> timestampedValue);

    public static class DeviceState {
        private static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
        private static final int NITZ_UPDATE_SPACING_DEFAULT = 600000;
        private final ContentResolver mCr;
        private final int mNitzUpdateDiff = SystemProperties.getInt("ro.nitz_update_diff", 2000);
        private final int mNitzUpdateSpacing = SystemProperties.getInt("ro.nitz_update_spacing", (int) NITZ_UPDATE_SPACING_DEFAULT);
        private final GsmCdmaPhone mPhone;
        private final TelephonyManager mTelephonyManager;

        public DeviceState(GsmCdmaPhone phone) {
            this.mPhone = phone;
            Context context = phone.getContext();
            this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            this.mCr = context.getContentResolver();
        }

        public int getNitzUpdateSpacingMillis() {
            return Settings.Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
        }

        public int getNitzUpdateDiffMillis() {
            return Settings.Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
        }

        public boolean getIgnoreNitz() {
            String ignoreNitz = SystemProperties.get("gsm.ignore-nitz");
            return ignoreNitz != null && ignoreNitz.equals("yes");
        }

        public String getNetworkCountryIsoForPhone() {
            return this.mTelephonyManager.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
        }
    }
}
