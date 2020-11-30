package com.android.server.am;

import android.os.SystemClock;
import android.os.UserHandle;
import android.util.SparseArray;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansPackageSelector;
import com.android.server.am.HansLcdOnScene;
import java.util.Iterator;
import java.util.Objects;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public class HansLcdOnScene extends HansSceneBase implements IHansScene {
    private static final int SCREEN_ON_GLOBAL_RESTRICTIONS = 0;
    private static final int SCREEN_ON_RESTRICTIONS = 238;
    protected final String FZ_REASON_LCD_ON = "LcdOn";
    int HANS_LCD_ON_SCENE_FROZEN_STATE_IMPORTANCE_FLAG = ColorHansImportance.HANS_IMPORTANT_FOR_FROZEN_STATE;
    int HANS_LCD_ON_SCENE_IMPORTANCE_FLAG = 221183;
    int HANS_LCD_ON_SCENE_TARGET_FLAG = 1;

    /* access modifiers changed from: package-private */
    /* compiled from: ColorHansManager */
    public interface IState {
        void stateDToR(ColorHansPackageSelector.HansPackage hansPackage, String str);

        void stateFToMUFZ(ColorHansPackageSelector.HansPackage hansPackage, String str);

        void stateFToRUFZ(ColorHansPackageSelector.HansPackage hansPackage, String str);

        void stateMToFFreeze(ColorHansPackageSelector.HansPackage hansPackage, String str);

        void stateRToFFreeze(ColorHansPackageSelector.HansPackage hansPackage, String str);

        void stateToD(int i);

        String toStr();
    }

    @Override // com.android.server.am.HansSceneBase, com.android.server.am.IHansScene
    public boolean isFreezed(int uid) {
        return super.isFreezed(uid);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze(int uid) {
        freeze(uid, "LcdOn");
    }

    public void hansFreeze(int uid, String reason) {
        if (ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansLcdOnScene)) {
            freeze(uid, reason);
        }
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(int uid, String reason) {
        unfreeze(uid, reason);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze() {
        if (ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansLcdOnScene)) {
            freeze("LcdOn");
        }
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(String reason) {
        unfreeze(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public void postUnFreeze(int uid, String pkgName, String reason) {
        this.mMainHandler.post(new Runnable(uid) {
            /* class com.android.server.am.$$Lambda$HansLcdOnScene$QRFgQZOWAqYGKbFXJrfIiPE1jyk */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                HansLcdOnScene.this.lambda$postUnFreeze$0$HansLcdOnScene(this.f$1);
            }
        });
        this.mMainHandler.post(new Runnable(uid, pkgName) {
            /* class com.android.server.am.$$Lambda$HansLcdOnScene$FFlKeU9u3jm2dG2BoF2O0zxzmfA */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                HansLcdOnScene.this.lambda$postUnFreeze$1$HansLcdOnScene(this.f$1, this.f$2);
            }
        });
        synchronized (this.mHansLock) {
            handleStateMachine(uid, reason);
        }
    }

    public /* synthetic */ void lambda$postUnFreeze$0$HansLcdOnScene(int uid) {
        handleWakeLockForHans(uid, false);
    }

    public /* synthetic */ void lambda$postUnFreeze$1$HansLcdOnScene(int uid, String pkgName) {
        unproxyAlarms(uid);
        ColorHansManager.getInstance().getHansBroadcastProxy().unProxyBroadcast(pkgName, UserHandle.getUserId(uid));
    }

    public boolean isLcdOnSceneTarget(int uid) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
            if (hansPackage == null) {
                return false;
            }
            hansPackage.setLastUsedTime(SystemClock.elapsedRealtime());
            return true;
        }
    }

    public void resetStateMachineState(int uid) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
            if (hansPackage != null && hansPackage.getScene() == 1 && "R".equals(hansPackage.getStateMachine().getState().toStr())) {
                this.mHansLogger.i("resetStateMachineState...");
                hansPackage.getStateMachine().stopStateMachine(hansPackage);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public int getScene() {
        return 1;
    }

    private void handleStateMachine(int uid, String reason) {
        ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
        if (hansPackage != null) {
            HansStateMachine stateMachine = hansPackage.getStateMachine();
            if (ColorHansManager.getInstance().getStateToMList().contains(reason)) {
                stateMachine.stateFToMUFZ(hansPackage, reason);
            } else if (ColorHansManager.getInstance().getStateToRList().contains(reason)) {
                stateMachine.stateFToRUFZ(hansPackage, reason);
            } else if (ColorHansManager.getInstance().getStateToDList().contains(reason)) {
                stateMachine.stateToD(uid);
            }
        }
    }

    @Override // com.android.server.am.IHansScene
    public void onInit() {
        this.mManagedMap = ColorHansPackageSelector.getInstance().getHansPackageMap(this.HANS_LCD_ON_SCENE_TARGET_FLAG);
        if (ColorHansManager.getInstance().getCommonConfig().isChinaRegion()) {
            this.mRestriction = new HansLcdOnRestriction(SCREEN_ON_RESTRICTIONS);
        } else {
            this.mRestriction = new HansLcdOnRestriction(0);
        }
        this.mImportantFlag = this.HANS_LCD_ON_SCENE_IMPORTANCE_FLAG;
    }

    @Override // com.android.server.am.IHansScene
    public ColorHansRestriction getHansRestriction() {
        return this.mRestriction;
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
                        ColorHansPackageSelector.HansPackage oleHansPkg = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid2);
                        if (oleHansPkg != null && oleHansPkg.getFreezed()) {
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

    public void enterStateMachine(int uid, String reason) {
        if (ColorHansManager.getInstance().getCommonConfig().hasHansEnable() && !ColorHansManager.getInstance().getCommonConfig().isCharging()) {
            synchronized (this.mHansLock) {
                if (uid == -1) {
                    Iterator<Integer> it = ColorHansManager.getInstance().getHansRunningList().iterator();
                    while (it.hasNext()) {
                        ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(it.next().intValue());
                        if (hansPackage != null && !hansPackage.getFreezed()) {
                            hansPackage.mStateMachine.enterStateMachine(hansPackage, reason);
                        }
                    }
                } else {
                    ColorHansPackageSelector.HansPackage hansPackage2 = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
                    if (hansPackage2 != null) {
                        hansPackage2.mStateMachine.enterStateMachine(hansPackage2, reason);
                    }
                }
            }
        }
    }

    public void stopStateMachine(int uid) {
        synchronized (this.mHansLock) {
            if (uid == -1) {
                for (int i = 0; i < this.mManagedMap.size(); i++) {
                    ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) this.mManagedMap.valueAt(i);
                    if (hansPackage != null) {
                        hansPackage.mStateMachine.stopStateMachine(hansPackage);
                    }
                }
                ColorHansManager.StateMachineHandler stateMachineHandler = this.mStateMachineHandler;
                Objects.requireNonNull(this.mStateMachineHandler);
                stateMachineHandler.removeMessages(1);
            } else {
                ColorHansPackageSelector.HansPackage hansPackage2 = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
                if (hansPackage2 != null) {
                    hansPackage2.mStateMachine.stopStateMachine(hansPackage2);
                    ColorHansManager.StateMachineHandler stateMachineHandler2 = this.mStateMachineHandler;
                    Objects.requireNonNull(this.mStateMachineHandler);
                    stateMachineHandler2.removeMessages(1, hansPackage2.getStrUid());
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0042, code lost:
        if (r4.hasMessages(1, r3) == false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0044, code lost:
        r4 = r6.mStateMachineHandler;
        java.util.Objects.requireNonNull(r6.mStateMachineHandler);
        r4.removeMessages(1, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004e, code lost:
        r6.mStateMachineHandler.sendMessageDelayed(r1, com.android.server.am.ColorHansPackageSelector.getInstance().getStateChangeDelayTime());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        r1 = android.os.Message.obtain();
        java.util.Objects.requireNonNull(r6.mStateMachineHandler);
        r1.what = 1;
        r1.obj = r3;
        r3 = new android.os.Bundle();
        r3.putInt(com.android.server.am.ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, r7);
        r3.putString("reason", r8);
        r1.setData(r3);
        r4 = r6.mStateMachineHandler;
        java.util.Objects.requireNonNull(r6.mStateMachineHandler);
     */
    public void sendEnterStateMachineMsg(int uid, String reason) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
            if (hansPackage != null) {
                String strUid = hansPackage.getStrUid();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* compiled from: ColorHansManager */
    public class HansStateMachine {
        static final String TAG = "HansStateMachine";
        IState defaultState = null;
        IState frozenState = null;
        IState middleState = null;
        IState runningState = null;
        IState state = null;
        ColorHansManager.StateMachineHandler stateMachineHandler = null;

        public HansStateMachine() {
            this.defaultState = new HansDefaultState(this);
            this.runningState = new HansRunningState(this);
            this.middleState = new HansMiddleState(this);
            this.frozenState = new HansFrozenState(this);
            this.state = this.defaultState;
            this.stateMachineHandler = ColorHansManager.getInstance().getStateMachineHandler();
        }

        public IState getState() {
            return this.state;
        }

        public IState getDefaultState() {
            return this.defaultState;
        }

        public IState getRunningState() {
            return this.runningState;
        }

        public IState getMiddleState() {
            return this.middleState;
        }

        public IState getFrozenState() {
            return this.frozenState;
        }

        public void setState(IState state2) {
            this.state = state2;
        }

        public void enterStateMachine(ColorHansPackageSelector.HansPackage ps, String reason) {
            stateDToR(ps, reason);
        }

        public void stopStateMachine(ColorHansPackageSelector.HansPackage ps) {
            setState(getDefaultState());
        }

        /* access modifiers changed from: package-private */
        public void stateRToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            this.state.stateRToFFreeze(ps, reason);
        }

        /* access modifiers changed from: package-private */
        public void stateMToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            this.state.stateMToFFreeze(ps, reason);
        }

        /* access modifiers changed from: package-private */
        public void stateFToMUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            this.state.stateFToMUFZ(ps, reason);
        }

        /* access modifiers changed from: package-private */
        public void stateFToRUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            this.state.stateFToRUFZ(ps, reason);
        }

        /* access modifiers changed from: package-private */
        public void stateToD(int uid) {
            this.state.stateToD(uid);
        }

        /* access modifiers changed from: package-private */
        public void stateDToR(ColorHansPackageSelector.HansPackage ps, String reason) {
            this.state.stateDToR(ps, reason);
        }
    }

    /* compiled from: ColorHansManager */
    class HansDefaultState implements IState {
        HansStateMachine stateMachine = null;

        public HansDefaultState(HansStateMachine stateMachine2) {
            this.stateMachine = stateMachine2;
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateRToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call StateRToFFreeze");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateMToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call StateMToFFreeze");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToMUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateFToMUFZ");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToRUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateFToRUFZ");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateToD(int uid) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",uid: " + uid + ",not allowed to call stateToD");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateDToR(ColorHansPackageSelector.HansPackage ps, String reason) {
            HansStateMachine hansStateMachine = this.stateMachine;
            hansStateMachine.setState(hansStateMachine.getRunningState());
            this.stateMachine.stateRToFFreeze(ps, reason);
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public String toStr() {
            return "D";
        }
    }

    /* compiled from: ColorHansManager */
    class HansRunningState implements IState {
        HansStateMachine stateMachine = null;

        public HansRunningState(HansStateMachine stateMachine2) {
            this.stateMachine = stateMachine2;
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateRToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            this.stateMachine.stateMachineHandler.post(new Runnable(ps, reason) {
                /* class com.android.server.am.$$Lambda$HansLcdOnScene$HansRunningState$VweouL2ulcKIRh56__hrMPNg8Fs */
                private final /* synthetic */ ColorHansPackageSelector.HansPackage f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    HansLcdOnScene.HansRunningState.this.lambda$stateRToFFreeze$1$HansLcdOnScene$HansRunningState(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$stateRToFFreeze$1$HansLcdOnScene$HansRunningState(ColorHansPackageSelector.HansPackage ps, String reason) {
            DynamicImportantAppList dynamicImportantAppList = new DynamicImportantAppList();
            dynamicImportantAppList.setAudioList(ColorCommonListManager.getInstance().getAudioFocus());
            dynamicImportantAppList.setNavigationList(ColorCommonListManager.getInstance().getNavigationList());
            synchronized (HansLcdOnScene.this.mHansLock) {
                String preState = this.stateMachine.getState().toStr();
                if (ColorHansImportance.getInstance().isHansImportantCase(ps, HansLcdOnScene.this.mImportantFlag, dynamicImportantAppList)) {
                    ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
                    hansLogger.i("stateRToFFreeze--imporant case: pkgName: " + ps.getPkgName() + ", reason: " + ps.getImportantReason());
                    this.stateMachine.stateMachineHandler.postDelayed(new Runnable(ps, reason) {
                        /* class com.android.server.am.$$Lambda$HansLcdOnScene$HansRunningState$mO10Kpr4SlvKy95t22c6BYUllmQ */
                        private final /* synthetic */ ColorHansPackageSelector.HansPackage f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            HansLcdOnScene.HansRunningState.this.lambda$stateRToFFreeze$0$HansLcdOnScene$HansRunningState(this.f$1, this.f$2);
                        }
                    }, ColorHansPackageSelector.getInstance().getImportantRepeatTime());
                    ColorHansManager.getInstance().notifyNotFreezeReason(ps.getUid(), ps.getPkgName(), ps.getImportantReason());
                } else {
                    this.stateMachine.setState(this.stateMachine.getFrozenState());
                    String curState = this.stateMachine.getState().toStr();
                    ColorHansManager.HansLogger hansLogger2 = HansLcdOnScene.this.mHansLogger;
                    hansLogger2.i("pkg: " + ps.getPkgName() + ",state: " + preState + " -> " + curState);
                    HansLcdOnScene.this.hansFreeze(ps.getUid(), reason);
                }
            }
        }

        public /* synthetic */ void lambda$stateRToFFreeze$0$HansLcdOnScene$HansRunningState(ColorHansPackageSelector.HansPackage ps, String reason) {
            synchronized (HansLcdOnScene.this.mHansLock) {
                this.stateMachine.stateRToFFreeze(ps, reason);
            }
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateMToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call StateMToFFreeze");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToMUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateFToMUFZ");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToRUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateFToMUFZ");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateToD(int uid) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("uid: " + uid + ", state: " + this.stateMachine.getState().toStr() + " -> D");
            HansStateMachine hansStateMachine = this.stateMachine;
            hansStateMachine.setState(hansStateMachine.getDefaultState());
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateDToR(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call StateDToR");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public String toStr() {
            return "R";
        }
    }

    /* compiled from: ColorHansManager */
    class HansMiddleState implements IState {
        HansStateMachine stateMachine = null;

        public HansMiddleState(HansStateMachine stateMachine2) {
            this.stateMachine = stateMachine2;
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateRToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call StateRToFFreeze");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateMToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            this.stateMachine.stateMachineHandler.post(new Runnable(ps, reason) {
                /* class com.android.server.am.$$Lambda$HansLcdOnScene$HansMiddleState$GMuYjKrC99nUX5FkGDHn_inJWM */
                private final /* synthetic */ ColorHansPackageSelector.HansPackage f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    HansLcdOnScene.HansMiddleState.this.lambda$stateMToFFreeze$1$HansLcdOnScene$HansMiddleState(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$stateMToFFreeze$1$HansLcdOnScene$HansMiddleState(ColorHansPackageSelector.HansPackage ps, String reason) {
            DynamicImportantAppList dynamicImportantAppList = new DynamicImportantAppList();
            dynamicImportantAppList.setAudioList(ColorCommonListManager.getInstance().getAudioFocus());
            dynamicImportantAppList.setNavigationList(ColorCommonListManager.getInstance().getNavigationList());
            synchronized (HansLcdOnScene.this.mHansLock) {
                ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
                hansLogger.i("pkg: " + ps.getPkgName() + ",state: M -> F");
                if (ColorHansImportance.getInstance().isHansImportantCase(ps, HansLcdOnScene.this.HANS_LCD_ON_SCENE_FROZEN_STATE_IMPORTANCE_FLAG, dynamicImportantAppList)) {
                    this.stateMachine.stateMachineHandler.postDelayed(new Runnable(ps, reason) {
                        /* class com.android.server.am.$$Lambda$HansLcdOnScene$HansMiddleState$RG0YTOpUMnwDYsPcd3WNgKwvjw */
                        private final /* synthetic */ ColorHansPackageSelector.HansPackage f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            HansLcdOnScene.HansMiddleState.this.lambda$stateMToFFreeze$0$HansLcdOnScene$HansMiddleState(this.f$1, this.f$2);
                        }
                    }, ColorHansPackageSelector.getInstance().getStateChangeDelayTime());
                    ColorHansManager.getInstance().notifyNotFreezeReason(ps.getUid(), ps.getPkgName(), ps.getImportantReason());
                } else {
                    this.stateMachine.setState(this.stateMachine.getFrozenState());
                    HansLcdOnScene.this.hansFreeze(ps.getUid(), reason);
                }
            }
        }

        public /* synthetic */ void lambda$stateMToFFreeze$0$HansLcdOnScene$HansMiddleState(ColorHansPackageSelector.HansPackage ps, String reason) {
            synchronized (HansLcdOnScene.this.mHansLock) {
                this.stateMachine.stateMToFFreeze(ps, reason);
            }
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToMUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateFToMUFZ");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToRUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateFToRUFZ");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateToD(int uid) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.i("uid: " + uid + ", state: " + this.stateMachine.getState().toStr() + " -> D");
            HansStateMachine hansStateMachine = this.stateMachine;
            hansStateMachine.setState(hansStateMachine.getDefaultState());
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateDToR(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call StateDToR");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public String toStr() {
            return "M";
        }
    }

    /* compiled from: ColorHansManager */
    class HansFrozenState implements IState {
        HansStateMachine stateMachine = null;

        public HansFrozenState(HansStateMachine stateMachine2) {
            this.stateMachine = stateMachine2;
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateRToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateRToFFreeze");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateMToFFreeze(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call stateMToFFreeze");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToMUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            HansStateMachine hansStateMachine = this.stateMachine;
            hansStateMachine.setState(hansStateMachine.getMiddleState());
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.i("pkg: " + ps.getPkgName() + ",state: F -> M");
            this.stateMachine.stateMachineHandler.postDelayed(new Runnable(ps, reason) {
                /* class com.android.server.am.$$Lambda$HansLcdOnScene$HansFrozenState$1h8bU1aVV3wIdH6ZLYr3i9FNgvw */
                private final /* synthetic */ ColorHansPackageSelector.HansPackage f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    HansLcdOnScene.HansFrozenState.this.lambda$stateFToMUFZ$0$HansLcdOnScene$HansFrozenState(this.f$1, this.f$2);
                }
            }, ColorHansPackageSelector.getInstance().getStateChangeDelayTime());
        }

        public /* synthetic */ void lambda$stateFToMUFZ$0$HansLcdOnScene$HansFrozenState(ColorHansPackageSelector.HansPackage ps, String reason) {
            synchronized (HansLcdOnScene.this.mHansLock) {
                this.stateMachine.stateMToFFreeze(ps, reason);
            }
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateFToRUFZ(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.i("pkg: " + ps.getPkgName() + ",state: F -> R");
            HansStateMachine hansStateMachine = this.stateMachine;
            hansStateMachine.setState(hansStateMachine.getRunningState());
            this.stateMachine.stateMachineHandler.postDelayed(new Runnable(ps, reason) {
                /* class com.android.server.am.$$Lambda$HansLcdOnScene$HansFrozenState$BAGjxRBcGmU8YZJ8hr73AqFcA */
                private final /* synthetic */ ColorHansPackageSelector.HansPackage f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    HansLcdOnScene.HansFrozenState.this.lambda$stateFToRUFZ$1$HansLcdOnScene$HansFrozenState(this.f$1, this.f$2);
                }
            }, ColorHansPackageSelector.getInstance().getStateChangeDelayTime());
        }

        public /* synthetic */ void lambda$stateFToRUFZ$1$HansLcdOnScene$HansFrozenState(ColorHansPackageSelector.HansPackage ps, String reason) {
            synchronized (HansLcdOnScene.this.mHansLock) {
                this.stateMachine.stateRToFFreeze(ps, reason);
            }
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateToD(int uid) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.i("uid: " + uid + ", state: " + this.stateMachine.getState().toStr() + " -> D");
            HansStateMachine hansStateMachine = this.stateMachine;
            hansStateMachine.setState(hansStateMachine.getDefaultState());
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public void stateDToR(ColorHansPackageSelector.HansPackage ps, String reason) {
            ColorHansManager.HansLogger hansLogger = HansLcdOnScene.this.mHansLogger;
            hansLogger.d("state: " + this.stateMachine.getState().toStr() + ",package: " + ps.getPkgName() + ",not allowed to call StateDToR");
        }

        @Override // com.android.server.am.HansLcdOnScene.IState
        public String toStr() {
            return "F";
        }
    }

    /* compiled from: ColorHansManager */
    class HansLcdOnRestriction extends ColorHansRestriction {
        public HansLcdOnRestriction(int restrictions) {
            super(restrictions);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedServicePolicy(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind, int freezeLevel) {
            boolean ret = super.isBlockedServicePolicy(callingUid, callingPackage, uid, pkgName, cpnName, isBind, freezeLevel);
            if (!ret) {
                return ret;
            }
            if (freezeLevel == 1 || callingUid < 10000 || callingUid == ColorHansManager.getInstance().getCurResumeUid() || callingUid == uid) {
                return false;
            }
            return ret;
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedProviderPolicy(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, int freezeLevel) {
            boolean ret = super.isBlockedProviderPolicy(callingUid, callingPackage, uid, pkgName, cpnName, freezeLevel);
            if (!ret) {
                return ret;
            }
            if (freezeLevel == 1 || callingUid < 10000 || callingUid == ColorHansManager.getInstance().getCurResumeUid()) {
                return false;
            }
            return ret;
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedBroadcastPolicy(int callingUid, String callingPackage, int uid, String pkgName, String action, boolean order, int freezeLevel) {
            boolean ret = super.isBlockedBroadcastPolicy(callingUid, callingPackage, uid, pkgName, action, order, freezeLevel);
            if (!ret) {
                return ret;
            }
            if (freezeLevel == 1 || callingUid < 10000 || callingUid == ColorHansManager.getInstance().getCurResumeUid() || callingUid == uid) {
                return false;
            }
            return ret;
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedBinderPolicy(int callingPid, int uid, String pkgName, String aidlName, int code, boolean oneway) {
            boolean ret = super.isBlockedBinderPolicy(callingPid, uid, pkgName, aidlName, code, oneway);
            if (!ret || !isFromControlCenterPkg(pkgName)) {
                return ret;
            }
            return false;
        }
    }
}
