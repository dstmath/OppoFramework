package android.view;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.view.RootViewSurfaceTaker;
import com.oppo.screenmode.IOppoScreenMode;
import java.util.Arrays;

public class OppoRefreshRateInjector {
    private static final int REFRESH_RATE_60 = 2;
    private ArraySet<String> mDisableOverrideViewList;
    private boolean mFeatureEnable;
    private boolean mInit;

    public static OppoRefreshRateInjector getInstance() {
        return LazyHolder.INSTANCE;
    }

    static ArraySet<String> parseOverrideViewList(String viewList) {
        if (TextUtils.isEmpty(viewList)) {
            return null;
        }
        try {
            ArraySet<String> result = new ArraySet<>();
            result.addAll(Arrays.asList(viewList.split(SmsManager.REGEX_PREFIX_DELIMITER)));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private void init(Context context) {
        this.mFeatureEnable = context.getPackageManager().hasSystemFeature("oppo.display.screen.90hz.support") || context.getPackageManager().hasSystemFeature("oppo.display.screen.120hz.support");
        this.mDisableOverrideViewList = getDisableOverrideViewList(context);
        this.mInit = true;
    }

    public void setRefreshRateIfNeed(Context context, ViewGroup viewGroup, ViewRootImpl viewRoot) {
        if (!this.mInit) {
            init(context);
        }
        if (this.mFeatureEnable) {
            int rateId = 0;
            try {
                if (viewGroup instanceof RootViewSurfaceTaker) {
                    rateId = ((RootViewSurfaceTaker) viewGroup).willYouTakeTheSurface() != null ? 2 : 0;
                }
                if (rateId == 0) {
                    rateId = getRefreshRateId(viewGroup, getAreaThreshold(viewRoot.getWidth(), viewRoot.getHeight()));
                }
                if (viewRoot.mOverrideRefreshRateId != rateId) {
                    viewRoot.mWindowSession.setOverrideRefreshRate(viewRoot.mWindow, rateId);
                    viewRoot.mOverrideRefreshRateId = rateId;
                }
            } catch (Exception e) {
            }
        }
    }

    private int getAreaThreshold(int windowW, int windowH) {
        int shortSide = Math.min(windowW, windowH);
        return shortSide * shortSide;
    }

    private int getRefreshRateId(ViewGroup viewGroup, int threshold) {
        int rateId;
        if (viewGroup.getVisibility() == 0 && viewGroup.getWidth() * viewGroup.getHeight() >= threshold) {
            int rateId2 = getRefreshRateIdFromView(viewGroup, threshold);
            if (rateId2 != 0) {
                return rateId2;
            }
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup) {
                    rateId = getRefreshRateId((ViewGroup) child, threshold);
                } else {
                    rateId = getRefreshRateIdFromView(child, threshold);
                }
                if (rateId != 0) {
                    return rateId;
                }
            }
        }
        return 0;
    }

    private int getRefreshRateIdFromView(View view, int threshold) {
        boolean useLowRate = view.getVisibility() == 0 && ((view instanceof SurfaceView) || (view instanceof TextureView)) && view.getWidth() * view.getHeight() >= threshold && !disableViewOverride(view.getClass().getSimpleName());
        if (useLowRate && ViewRootImpl.DEBUG_LAYOUT) {
            Log.v("OppoRefreshRateInjector", view + " request low refresh rate");
        }
        if (useLowRate) {
            return 2;
        }
        return 0;
    }

    private boolean disableViewOverride(String key) {
        ArraySet<String> arraySet = this.mDisableOverrideViewList;
        return arraySet != null && (arraySet.contains("All") || this.mDisableOverrideViewList.contains(key));
    }

    private ArraySet<String> getDisableOverrideViewList(Context context) {
        IBinder binder = ServiceManager.getService("opposcreenmode");
        if (binder == null) {
            Log.e("OppoRefreshRateInjector", "get opposcreenmode service failed");
            return null;
        }
        try {
            return parseOverrideViewList(IOppoScreenMode.Stub.asInterface(binder).getDisableOverrideViewList(context.getBasePackageName()));
        } catch (Exception e) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static class LazyHolder {
        private static final OppoRefreshRateInjector INSTANCE = new OppoRefreshRateInjector();

        private LazyHolder() {
        }
    }
}
