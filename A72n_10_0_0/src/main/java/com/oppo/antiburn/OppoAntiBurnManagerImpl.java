package com.oppo.antiburn;

import android.app.Activity;
import android.app.Application;
import android.graphics.Canvas;
import android.view.IColorBaseViewRoot;
import android.view.OppoBurnConfigData;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import com.color.util.ColorTypeCastingHelper;
import java.util.Iterator;
import java.util.List;

public class OppoAntiBurnManagerImpl implements IOppoAntiBurnManager {
    public static final String TAG = "OppoAntiBurnManagerImpl";
    private final OppoAntiBurnConfigHolder mConfigHolder = OppoAntiBurnConfigHolder.getInstance();
    private final OppoAntiBurnWidgetPainter mWidgetPainter = OppoAntiBurnWidgetPainter.getInstance();

    @Override // com.oppo.antiburn.IOppoAntiBurnManager
    public void init(Application application) {
    }

    @Override // com.oppo.antiburn.IOppoAntiBurnManager
    public void scheduleUpdateForceDarkConfig(String jsonStr) {
        this.mConfigHolder.updateConfig(jsonStr);
    }

    @Override // com.oppo.antiburn.IOppoAntiBurnManager
    public void executeOPFDSpecialConfigAction(View v, Canvas canvas) {
        List<String> mOPFDConfigActions = this.mWidgetPainter.fetchOPFDConfigActionsForCurView(v);
        if (mOPFDConfigActions != null && mOPFDConfigActions.size() > 0) {
            this.mWidgetPainter.executeOPFDConfigAction(v, canvas, mOPFDConfigActions);
        }
    }

    @Override // com.oppo.antiburn.IOppoAntiBurnManager
    public void initViewTreeFlag(ViewRootImpl viewRoot, View decor) {
        Activity activity = OppoAntiBurnUtils.getActivity(decor);
        if (activity != null) {
            updateBurnConfig(viewRoot, isTargetActivity(activity), this.mConfigHolder.getLatestConfigTime());
        } else {
            updateBurnConfig(viewRoot, isTargetApp(), System.currentTimeMillis());
        }
    }

    @Override // com.oppo.antiburn.IOppoAntiBurnManager
    public void onActivityResume(Activity activity) {
        if (activity != null && activity.getWindow() != null) {
            updateAffectedViewTreeIfChanged(activity);
        }
    }

    private void updateAffectedViewTreeIfChanged(final Activity activity) {
        OppoBurnConfigData config;
        Iterator<ViewRootImpl> it = WindowManagerGlobal.getInstance().getRootViews(activity.getActivityToken()).iterator();
        while (it.hasNext()) {
            final ViewRootImpl vr = it.next();
            IColorBaseViewRoot baseViewRoot = (IColorBaseViewRoot) ColorTypeCastingHelper.typeCasting(IColorBaseViewRoot.class, vr);
            if (baseViewRoot != null && (config = baseViewRoot.getOppoBurnConfigData()) != null) {
                if (this.mConfigHolder.getLatestConfigTime() != config.getBurnUpdateTime()) {
                    if (vr.getView().isAttachedToWindow()) {
                        updateBurnConfig(vr, isTargetActivity(activity), this.mConfigHolder.getLatestConfigTime());
                    } else {
                        vr.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                            /* class com.oppo.antiburn.OppoAntiBurnManagerImpl.AnonymousClass1 */

                            @Override // android.view.View.OnAttachStateChangeListener
                            public void onViewAttachedToWindow(View v) {
                                OppoAntiBurnManagerImpl oppoAntiBurnManagerImpl = OppoAntiBurnManagerImpl.this;
                                oppoAntiBurnManagerImpl.updateBurnConfig(vr, oppoAntiBurnManagerImpl.isTargetActivity(activity), OppoAntiBurnManagerImpl.this.mConfigHolder.getLatestConfigTime());
                                vr.getView().removeOnAttachStateChangeListener(this);
                            }

                            @Override // android.view.View.OnAttachStateChangeListener
                            public void onViewDetachedFromWindow(View v) {
                            }
                        });
                    }
                }
            } else {
                return;
            }
        }
    }

    public void updateBurnConfig(ViewRootImpl viewRoot, boolean hasCfg, long updateTime) {
        IColorBaseViewRoot baseViewRoot;
        OppoBurnConfigData config;
        if (viewRoot != null && (baseViewRoot = (IColorBaseViewRoot) ColorTypeCastingHelper.typeCasting(IColorBaseViewRoot.class, viewRoot)) != null && (config = baseViewRoot.getOppoBurnConfigData()) != null) {
            config.updateBurnCfg(hasCfg, updateTime);
        }
    }

    private boolean isTargetApp() {
        return this.mConfigHolder.hasSpecialViewsConfig();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTargetActivity(Activity activity) {
        return this.mConfigHolder.isTargetActivity(activity);
    }
}
