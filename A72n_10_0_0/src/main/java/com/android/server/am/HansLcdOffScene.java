package com.android.server.am;

import android.app.AlarmManager;
import android.os.SystemClock;
import android.util.SparseArray;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansPackageSelector;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public class HansLcdOffScene extends HansSceneBase implements IHansScene {
    private static final int SCREEN_OFF_GLOBAL_RESTRICTIONS = 0;
    private static final int SCREEN_OFF_RESTRICTIONS = 254;
    private final String FZ_REASON_LCD_OFF = "LcdOff";
    int HANS_LCD_OFF_SCENE_IMPORTANCE_FLAG = ColorHansImportance.HANS_IMPORTANT_SCENE_FOR_LCD_OFF_FREEZE;
    int HANS_LCD_OFF_SCENE_SPECIAL_CASE_IMPORTANCE_FLAG = ColorHansImportance.HANS_IMPORTANT_FOR_SPECIAL_APPS;
    int HANS_LCD_OFF_SCENE_TARGET_FLAG = 1;
    private Runnable lcdOffRunnable = new Runnable() {
        /* class com.android.server.am.$$Lambda$HansLcdOffScene$YoWFWgQlqloO72sx5kMyAv1VhaY */

        public final void run() {
            HansLcdOffScene.this.lambda$new$0$HansLcdOffScene();
        }
    };
    private final AlarmManager.OnAlarmListener mAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.am.HansLcdOffScene.AnonymousClass1 */

        public void onAlarm() {
            if (!HansLcdOffScene.this.mCommonConfig.isCharging() && !HansLcdOffScene.this.mCommonConfig.isScreenOn()) {
                HansLcdOffScene.this.mHansLogger.addSYSInfo("lcdOff freeze for alarm");
                HansLcdOffScene.this.mHansLogger.fullLog("onAlarm");
                HansLcdOffScene.this.hansFreeze();
                HansLcdOffScene.this.setAlarm(ColorHansPackageSelector.getInstance().getLcdOffRepeatTime());
            }
        }
    };
    AlarmManager mAlarmManager;
    long mLastFreezeTriggerTime = 0;

    public /* synthetic */ void lambda$new$0$HansLcdOffScene() {
        if (!this.mCommonConfig.isCharging() && !this.mCommonConfig.isScreenOn()) {
            this.mHansLogger.addSYSInfo("lcdOff freeze for handler");
            this.mHansLogger.fullLog("lcdOffRunnable");
            hansFreeze();
            sendRepeatFreeze();
        }
    }

    @Override // com.android.server.am.HansSceneBase, com.android.server.am.IHansScene
    public boolean isFreezed(int uid) {
        return super.isFreezed(uid);
    }

    public boolean isSpecialImportantCase(ColorHansPackageSelector.HansPackage hansPackage) {
        return ColorHansImportance.getInstance().isHansImportantCase(hansPackage, this.HANS_LCD_OFF_SCENE_SPECIAL_CASE_IMPORTANCE_FLAG, new DynamicImportantAppList());
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze(int uid) {
        if (ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansLcdOffScene)) {
            freeze(uid, "LcdOff");
        }
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(int uid, String reason) {
        unfreeze(uid, reason);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze() {
        if (ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansLcdOffScene)) {
            long curTime = SystemClock.elapsedRealtime();
            long j = this.mLastFreezeTriggerTime;
            if (j == 0 || curTime - j >= ColorHansPackageSelector.getInstance().getLcdOffRepeatTime()) {
                this.mLastFreezeTriggerTime = curTime;
                freeze("LcdOff");
            }
        }
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(String reason) {
        unfreeze(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public int getScene() {
        return 2;
    }

    @Override // com.android.server.am.IHansScene
    public void onInit() {
        if (ColorHansManager.getInstance().getCommonConfig().isChinaRegion()) {
            this.mRestriction = new ColorHansRestriction(SCREEN_OFF_RESTRICTIONS);
        } else {
            this.mRestriction = new ColorHansRestriction(0);
        }
        this.mManagedMap = ColorHansPackageSelector.getInstance().getHansPackageMap(this.HANS_LCD_OFF_SCENE_TARGET_FLAG);
        this.mImportantFlag = this.HANS_LCD_OFF_SCENE_IMPORTANCE_FLAG;
    }

    @Override // com.android.server.am.IHansScene
    public ColorHansRestriction getHansRestriction() {
        return this.mRestriction;
    }

    public void sendFirstFreeze() {
        this.mLastFreezeTriggerTime = 0;
        this.mMainHandler.postDelayed(this.lcdOffRunnable, ColorHansPackageSelector.getInstance().getLcdOffSceneInterval());
        setAlarm(ColorHansPackageSelector.getInstance().getLcdOffSceneInterval());
    }

    public void sendRepeatFreeze() {
        this.mMainHandler.postDelayed(this.lcdOffRunnable, ColorHansPackageSelector.getInstance().getLcdOffRepeatTime());
        setAlarm(ColorHansPackageSelector.getInstance().getLcdOffRepeatTime());
    }

    public void stopLcdOffTrigger() {
        this.mMainHandler.removeCallbacks(this.lcdOffRunnable);
        cancelAlarm();
    }

    public void setAlarm(long alarmTime) {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        }
        if (this.mAlarmManager != null) {
            ColorHansManager.HansLogger hansLogger = this.mHansLogger;
            hansLogger.fullLog("setAlarm " + alarmTime);
            cancelAlarm();
            this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + alarmTime, "hans", this.mAlarmListener, this.mMainHandler);
        }
    }

    public void cancelAlarm() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        }
        AlarmManager alarmManager = this.mAlarmManager;
        if (alarmManager != null) {
            alarmManager.cancel(this.mAlarmListener);
        }
    }

    @Override // com.android.server.am.IHansScene
    public void updateTargetMap(int updateType, ColorHansPackageSelector.HansPackage hansPackage) {
        synchronized (this.mHansLock) {
            int i = 0;
            if (1 == updateType) {
                try {
                    SparseArray<ColorHansPackageSelector.HansPackage> addList = ColorHansPackageSelector.getInstance().mTmpAddThirdAppList;
                    SparseArray<ColorHansPackageSelector.HansPackage> rmList = ColorHansPackageSelector.getInstance().mTmpRmThirdAppList;
                    for (int i2 = 0; i2 < addList.size(); i2++) {
                        int uid = addList.keyAt(i2);
                        if (this.mManagedMap.get(uid) == null) {
                            this.mManagedMap.put(uid, addList.valueAt(i2));
                        }
                    }
                    while (i < rmList.size()) {
                        int uid2 = rmList.keyAt(i);
                        ColorHansPackageSelector.HansPackage oldHansPkg = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid2);
                        if (oldHansPkg != null && oldHansPkg.getFreezed()) {
                            hansUnFreeze(uid2, ColorHansManager.HANS_UFZ_REASON_REMOVE_APP);
                        }
                        this.mManagedMap.remove(uid2);
                        i++;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                if (!(8 == updateType || 2 == updateType)) {
                    if (4 != updateType) {
                        if (16 == updateType) {
                            SparseArray<ColorHansPackageSelector.HansPackage> gmsList = ColorHansPackageSelector.getInstance().getHansPackageMap(256);
                            while (i < gmsList.size()) {
                                int uid3 = gmsList.keyAt(i);
                                ColorHansPackageSelector.HansPackage gmsHansPackage = gmsList.valueAt(i);
                                if (gmsHansPackage != null) {
                                    if (ColorHansManager.getInstance().getCommonConfig().isRestrictGms()) {
                                        if (this.mManagedMap.get(uid3) == null) {
                                            this.mManagedMap.put(uid3, gmsHansPackage);
                                        }
                                    } else if (this.mManagedMap.get(uid3) != null) {
                                        hansUnFreeze(uid3, ColorHansManager.HANS_UFZ_REASON_GMS);
                                        this.mManagedMap.remove(uid3);
                                    }
                                }
                                i++;
                            }
                        }
                    }
                }
                int mode = hansPackage.getSaveMode();
                int classType = hansPackage.getAppClass();
                if (1 != classType) {
                    if (2 != classType) {
                        if (3 == classType) {
                            if ((mode == 4 || mode == 2) && this.mManagedMap.get(hansPackage.getUid()) == null) {
                                this.mManagedMap.put(hansPackage.getUid(), hansPackage);
                            }
                            if (mode == 8) {
                                this.mManagedMap.remove(hansPackage.getUid());
                            }
                        }
                    }
                }
                ColorHansPackageSelector.HansPackage localHansPkg = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(hansPackage.getUid());
                if (localHansPkg != null) {
                    if (localHansPkg.getFreezed()) {
                        hansUnFreeze(localHansPkg.getUid(), ColorHansManager.HANS_UFZ_REASON_REMOVE_APP);
                    }
                    this.mManagedMap.remove(localHansPkg.getUid());
                }
            }
        }
    }
}
