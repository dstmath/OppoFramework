package com.oppo.common;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.common.OppoFeatureManager;
import android.common.PswFrameworkFactory;
import android.content.Context;
import android.media.IPswMediaPlayerUtils;
import android.media.PswMediaPlayerUtils;
import android.net.wifi.IWifiRomUpdateHelper;
import android.net.wifi.OppoWifiRomUpdateHelper;
import android.util.Log;
import android.view.IPswFeatureDemo;
import android.view.PswFeatureDemo;
import com.oppo.screenmode.IPswScreenModeFeature;
import com.oppo.screenmode.PswScreenModeFeature;

public class PswFrameworkFactoryImpl extends PswFrameworkFactory {
    private static final String TAG = "PswFrameworkFactoryImpl";

    public <T extends IOppoCommonFeature> T getFeature(T def, Object... vars) {
        verityParams(def);
        if (!OppoFeatureManager.isSupport(def)) {
            return def;
        }
        int i = AnonymousClass1.$SwitchMap$android$common$OppoFeatureList$OppoIndex[def.index().ordinal()];
        if (i == 1) {
            return OppoFeatureManager.getTraceMonitor(getPswFeatureDemo(vars));
        }
        if (i == 2) {
            Log.i(TAG, "get feature:" + def.index().name());
            return OppoFeatureManager.getTraceMonitor(getPswMediaPlayerUtils(vars));
        } else if (i == 3) {
            return OppoFeatureManager.getTraceMonitor(getWifiRomUpdateHelper(vars));
        } else {
            if (i != 4) {
                Log.i(TAG, "Unknow feature:" + def.index().name());
                return def;
            }
            Log.i(TAG, "get feature:" + def.index().name());
            return OppoFeatureManager.getTraceMonitor(getPswScreenModeFeature(vars));
        }
    }

    /* renamed from: com.oppo.common.PswFrameworkFactoryImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$common$OppoFeatureList$OppoIndex = new int[OppoFeatureList.OppoIndex.values().length];

        static {
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswFeatureDemo.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswMediaPlayerUtils.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IWifiRomUpdateHelper.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswScreenModeFeature.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private IPswFeatureDemo getPswFeatureDemo(Object... vars) {
        return PswFeatureDemo.getInstance();
    }

    private IPswMediaPlayerUtils getPswMediaPlayerUtils(Object... vars) {
        return new PswMediaPlayerUtils();
    }

    private IWifiRomUpdateHelper getWifiRomUpdateHelper(Object... vars) {
        return OppoWifiRomUpdateHelper.getInstance((Context) vars[0]);
    }

    private IPswScreenModeFeature getPswScreenModeFeature(Object... vars) {
        return PswScreenModeFeature.getInstance();
    }
}
