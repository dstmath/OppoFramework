package com.android.server.wifi;

import android.content.pm.UserInfo;
import android.net.wifi.WifiConfiguration;
import android.os.UserHandle;
import java.util.List;

public class WifiConfigurationUtil {
    public static boolean isVisibleToAnyProfile(WifiConfiguration config, List<UserInfo> profiles) {
        if (config.shared) {
            return true;
        }
        int creatorUserId = UserHandle.getUserId(config.creatorUid);
        for (UserInfo profile : profiles) {
            if (profile.id == creatorUserId) {
                return true;
            }
        }
        return false;
    }

    public static int getIntSimSlot(WifiConfiguration config) {
        String simSlot = config.simSlot;
        if (simSlot == null) {
            return 0;
        }
        String[] simSlots = simSlot.split("\"");
        if (simSlots.length > 1) {
            return Integer.parseInt(simSlots[1]);
        }
        if (simSlots.length != 1 || simSlots[0].length() <= 0) {
            return 0;
        }
        return Integer.parseInt(simSlots[0]);
    }
}
