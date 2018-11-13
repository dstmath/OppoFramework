package android.net;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;

@Deprecated
public class NetworkBadging {
    public static final int BADGING_4K = 30;
    public static final int BADGING_HD = 20;
    public static final int BADGING_NONE = 0;
    public static final int BADGING_SD = 10;

    private NetworkBadging() {
    }

    public static Drawable getWifiIcon(int signalLevel, int badging, Theme theme) {
        return Resources.getSystem().getDrawable(getWifiSignalResource(signalLevel), theme);
    }

    private static int getWifiSignalResource(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return 17302763;
            case 1:
                return 17302764;
            case 2:
                return 17302765;
            case 3:
                return 17302766;
            case 4:
                return 17302767;
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }

    private static int getBadgedWifiSignalResource(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return 17302726;
            case 1:
                return 17302727;
            case 2:
                return 17302728;
            case 3:
                return 17302729;
            case 4:
                return 17302730;
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }
}
