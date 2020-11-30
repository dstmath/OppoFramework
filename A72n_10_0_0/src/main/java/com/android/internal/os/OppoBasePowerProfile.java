package com.android.internal.os;

import android.os.OppoBaseEnvironment;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public abstract class OppoBasePowerProfile {
    static final String TAG = "PowerProfile";
    FileInputStream fis = null;
    String mProjectPowerProfile = (OppoBaseEnvironment.getOppoVersionDirectory().getAbsolutePath() + "/etc/power_profile/power_profile.xml");
    String mStrProjectVersion = SystemProperties.get("ro.product.prjversion");
    XmlPullParser parser = null;

    /* access modifiers changed from: package-private */
    public void getOppoPowerProfileXmlParser() {
        Log.i(TAG, "target project version: " + this.mStrProjectVersion + "  power profile : " + this.mProjectPowerProfile);
        String str = this.mProjectPowerProfile;
        if (str == null || !new File(str).canRead()) {
            this.parser = null;
            return;
        }
        try {
            this.parser = XmlPullParserFactory.newInstance().newPullParser();
            this.fis = new FileInputStream(this.mProjectPowerProfile);
            this.parser.setInput(this.fis, "UTF-8");
        } catch (Exception e) {
            Log.d(TAG, "access power profile exception caught : " + e.getMessage());
            FileInputStream fileInputStream = this.fis;
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                    this.fis = null;
                } catch (IOException ex) {
                    Log.d(TAG, "access power profile exception caught : " + ex.getMessage());
                }
            }
            this.parser = null;
        }
    }
}
