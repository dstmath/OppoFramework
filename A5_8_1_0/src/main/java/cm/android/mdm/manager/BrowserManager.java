package cm.android.mdm.manager;

import android.content.Context;
import cm.android.mdm.interfaces.IBrowserManager;
import cm.android.mdm.util.HarmonyNetUtil;
import cm.android.mdm.util.MethodSignature;
import java.util.List;

public class BrowserManager implements IBrowserManager {
    private static final String TAG = "BrowserManager";
    private Context mContext;

    public BrowserManager(Context context) {
        this.mContext = context;
    }

    public void setBrowserRestriction(int pattern) {
        if (pattern == 1) {
            HarmonyNetUtil.setHarmonyNetMode(this.mContext.getContentResolver(), 2);
        } else if (pattern == 2) {
            HarmonyNetUtil.setHarmonyNetMode(this.mContext.getContentResolver(), 1);
        } else if (pattern == 0) {
            HarmonyNetUtil.setHarmonyNetMode(this.mContext.getContentResolver(), 0);
        }
    }

    public void addBrowserRestriction(int pattern, List<String> urls) {
        if (pattern == 1) {
            HarmonyNetUtil.addHarmonyNetRules(this.mContext.getContentResolver(), urls, false);
        } else if (pattern == 2) {
            HarmonyNetUtil.addHarmonyNetRules(this.mContext.getContentResolver(), urls, true);
        }
    }

    public void removeBrowserRestriction(int pattern, List<String> urls) {
        if (pattern == 1) {
            HarmonyNetUtil.delHarmonyNetRules(this.mContext.getContentResolver(), urls, false);
        } else if (pattern == 2) {
            HarmonyNetUtil.delHarmonyNetRules(this.mContext.getContentResolver(), urls, true);
        }
    }

    public void removeBrowserRestriction(int pattern) {
        if (pattern == 1) {
            HarmonyNetUtil.clearHarmonyNetRules(this.mContext.getContentResolver(), false);
        } else if (pattern == 2) {
            HarmonyNetUtil.clearHarmonyNetRules(this.mContext.getContentResolver(), true);
        }
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(BrowserManager.class);
    }
}
