package mediatek.content.res;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.TypedValue;
import com.mediatek.powerhalmgr.PowerHalMgr;
import com.mediatek.powerhalmgr.PowerHalMgrFactory;
import com.oppo.luckymoney.LMManager;
import java.util.Iterator;

public class MtkBoostDrawableCache {
    private static final boolean DEBUG_CONFIG = false;
    static final String TAG = "MtkBoostDrawableCache";
    private static final ArrayMap<String, LongSparseArray<Drawable.ConstantState>> sBoostDrawableCache = new ArrayMap<>();
    private String mBoostKey = "";
    private PowerHalMgr mPowerHalService;
    private int mPowerHandle = -1;

    public void onConfigurationChange(int configChanges) {
        LongSparseArray<Drawable.ConstantState> boostCache;
        if (isBoostApp(this.mBoostKey) && (boostCache = sBoostDrawableCache.get(this.mBoostKey)) != null) {
            clearBoostDrawableCacheLocked(boostCache, configChanges);
            Slog.w(TAG, "Clear boost cache");
        }
    }

    public void clearBoostDrawableCacheLocked(LongSparseArray<Drawable.ConstantState> cache, int configChanges) {
        int N = cache.size();
        for (int i = 0; i < N; i++) {
            if (cache.valueAt(i) != null) {
                cache.setValueAt(i, null);
            }
        }
    }

    public Drawable getBoostCachedDrawable(Resources wrapper, long key) {
        LongSparseArray<Drawable.ConstantState> boostCache;
        Drawable boostDrawable;
        this.mBoostKey = wrapper.toString().split("@")[0];
        String boostKey = this.mBoostKey;
        if (!isBoostApp(boostKey) || (boostCache = sBoostDrawableCache.get(boostKey)) == null || (boostDrawable = getBoostCachedDrawableLocked(wrapper, key, boostCache)) == null) {
            return null;
        }
        return boostDrawable;
    }

    public Drawable getBoostCachedDrawableLocked(Resources wrapper, long key, LongSparseArray<Drawable.ConstantState> drawableCache) {
        Drawable.ConstantState entry = drawableCache.get(key);
        if (entry != null) {
            return entry.newDrawable(wrapper);
        }
        drawableCache.delete(key);
        return null;
    }

    public boolean isBoostApp(String appname) {
        if (appname.equals("android.content.res.Resources")) {
            return false;
        }
        for (String name : new String[]{LMManager.MM_PACKAGENAME}) {
            if (appname.contains(name)) {
                return true;
            }
        }
        return false;
    }

    public void putBoostCache(long key, Drawable.ConstantState cs) {
        if (isBoostApp(this.mBoostKey)) {
            LongSparseArray<Drawable.ConstantState> boostCache = sBoostDrawableCache.get(this.mBoostKey);
            if (boostCache == null) {
                boostCache = new LongSparseArray<>(1);
                sBoostDrawableCache.put(this.mBoostKey, boostCache);
                Iterator<String> it = sBoostDrawableCache.keySet().iterator();
                while (it.hasNext()) {
                    Slog.w(TAG, "ResourceKey:" + it.next());
                }
            }
            boostCache.put(key, cs);
            Slog.w(TAG, "CacheKey:" + key + " Resource:" + this.mBoostKey);
        }
    }

    private void boostSysRes() {
        int i;
        if (this.mPowerHalService == null) {
            this.mPowerHalService = PowerHalMgrFactory.getInstance().makePowerHalMgr();
        }
        PowerHalMgr powerHalMgr = this.mPowerHalService;
        if (powerHalMgr != null && -1 == this.mPowerHandle) {
            this.mPowerHandle = powerHalMgr.scnReg();
        }
        PowerHalMgr powerHalMgr2 = this.mPowerHalService;
        if (powerHalMgr2 != null && (i = this.mPowerHandle) != -1) {
            powerHalMgr2.scnConfig(i, 3, 0, 3000000, 0, 0);
            this.mPowerHalService.scnConfig(this.mPowerHandle, 3, 1, 3000000, 0, 0);
            this.mPowerHalService.scnConfig(this.mPowerHandle, 35, 99, 0, 0, 0);
            this.mPowerHalService.scnConfig(this.mPowerHandle, 62, 0, 0, 0, 0);
            this.mPowerHalService.scnConfig(this.mPowerHandle, 70, 1, 0, 0, 0);
            this.mPowerHalService.scnEnable(this.mPowerHandle, 30000);
        }
    }

    public void hbBoost(Resources wrapper, TypedValue value) {
        if (value.string != null) {
            String file = value.string.toString();
            String boostKey = wrapper.toString().split("@")[0];
            Trace.traceBegin(8192, file + SettingsStringUtil.DELIMITER + boostKey);
            if (file.contains("hongbao") && file.endsWith(".png") && isBoostApp(boostKey)) {
                boostSysRes();
            }
            Trace.traceEnd(8192);
        }
    }
}
