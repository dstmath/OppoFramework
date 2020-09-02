package com.oppo.crmodel;

import android.app.OppoMirrorActivityThread;
import android.os.Build;
import android.os.SystemProperties;
import android.telecom.Logging.Session;
import com.android.internal.logging.nano.MetricsProto;
import java.lang.reflect.Field;

public class ConfidentialRealModel {
    private static final String TAG = "CRmodel";
    private String mFactoryProductName = SystemProperties.get("ro.build.display.full_id");
    private String mMtkFactoryProductName = SystemProperties.get("ro.mediatek.version.release");

    public boolean ConfidentialRealModelOk(String cmPackageName) {
        boolean isConfVersion = "true".equals(SystemProperties.get("persist.version.confidential"));
        boolean cmWhiteList = false;
        if (OppoMirrorActivityThread.inCptWhiteList != null) {
            cmWhiteList = OppoMirrorActivityThread.inCptWhiteList.call(null, Integer.valueOf((int) MetricsProto.MetricsEvent.ACTION_PERMISSION_GRANT_READ_EXTERNAL_STORAGE), cmPackageName).booleanValue();
        }
        if (!isConfVersion || !cmWhiteList) {
            return false;
        }
        return true;
    }

    public void changeToRealModel() {
        String realModelName = null;
        String str = this.mFactoryProductName;
        if (str == null || str.length() == 0) {
            String str2 = this.mMtkFactoryProductName;
            if (!(str2 == null || str2.length() == 0)) {
                String str3 = this.mMtkFactoryProductName;
                realModelName = str3.substring(0, str3.indexOf(Session.SESSION_SEPARATION_CHAR_CHILD));
            }
        } else {
            String str4 = this.mFactoryProductName;
            realModelName = str4.substring(0, str4.indexOf(Session.SESSION_SEPARATION_CHAR_CHILD));
        }
        if (realModelName != null && realModelName.length() != 0) {
            try {
                Field field = Build.class.getField("MODEL");
                field.setAccessible(true);
                field.set(Build.class, realModelName);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }
}
