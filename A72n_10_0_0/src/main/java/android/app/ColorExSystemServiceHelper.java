package android.app;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public class ColorExSystemServiceHelper {
    public static final String EX_SYSTEM_SERVICE_CLASSNAME = "com.coloros.exsystemservice.ColorSharedSystemService";
    public static final String EX_SYSTEM_SERVICE_PKGNAME = "com.coloros.exsystemservice";
    private static final String TAG = ColorExSystemServiceHelper.class.getSimpleName();
    private static volatile ColorExSystemServiceHelper sIntance;

    public static ColorExSystemServiceHelper getInstance() {
        if (sIntance == null) {
            synchronized (ColorExSystemServiceHelper.class) {
                if (sIntance == null) {
                    sIntance = new ColorExSystemServiceHelper();
                }
            }
        }
        return sIntance;
    }

    public boolean checkColorExSystemService(boolean systemThread, String className) {
        if (TextUtils.isEmpty(className)) {
            Log.w(TAG, "checkColorExSystemService className is null or empty str");
            return false;
        } else if (!systemThread) {
            return false;
        } else {
            try {
                if (EX_SYSTEM_SERVICE_CLASSNAME.equals(className)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "checkColorExSystemService e = " + e);
                return false;
            }
        }
    }

    public boolean checkColorExSystemService(boolean systemThread, Intent intent) {
        if (intent == null) {
            Log.w(TAG, "checkColorExSystemService intent is null");
            return false;
        } else if (intent.getComponent() != null) {
            return checkColorExSystemService(systemThread, intent.getComponent().getClassName());
        } else {
            Log.w(TAG, "checkColorExSystemService intent getComponent is null");
            return false;
        }
    }

    public boolean checkColorExSystemService(Handler handler, String className) {
        return checkColorExSystemService(true, className);
    }

    public ComponentName getComponentName() {
        return new ComponentName(EX_SYSTEM_SERVICE_PKGNAME, EX_SYSTEM_SERVICE_CLASSNAME);
    }
}
