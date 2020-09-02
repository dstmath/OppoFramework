package com.android.server.display.ai;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.server.display.ai.bean.BrightnessPoint;
import com.android.server.display.ai.bean.ModelConfig;
import com.android.server.display.ai.bean.SplineModel;
import com.android.server.display.ai.broadcastreceiver.AIBrightnessTrainSwitch;
import com.android.server.display.ai.broadcastreceiver.BootCompletedReceiver;
import com.android.server.display.ai.mode.DayNightMode;
import com.android.server.display.ai.utils.AppSwitchDetectorUtil;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.display.ai.utils.DataHelper;
import com.android.server.display.ai.utils.ModelConfigUtil;
import com.android.server.display.ai.utils.MonotoneCubicSplineUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonotoneSplineManager {
    private static final String TAG = "MonotoneSplineManager";
    private static volatile MonotoneSplineManager sMonotoneSplineManager;
    private AppSwitchDetectorUtil mAppSwitchDetectorUtil;
    private AppSwitchDetectorUtil.IAppSwitchListener mAppSwitchListener = new AppSwitchDetectorUtil.IAppSwitchListener() {
        /* class com.android.server.display.ai.MonotoneSplineManager.AnonymousClass3 */

        @Override // com.android.server.display.ai.utils.AppSwitchDetectorUtil.IAppSwitchListener
        public void onAppSwitchToForeground(String pkgName) {
            if (TextUtils.isEmpty(pkgName) || !MonotoneSplineManager.this.mSplines.containsKey(pkgName)) {
                String unused = MonotoneSplineManager.this.mCurrentSplineName = BrightnessConstants.DEFAULT_SPLINE;
            } else {
                String unused2 = MonotoneSplineManager.this.mCurrentSplineName = pkgName;
            }
            ColorAILog.i(MonotoneSplineManager.TAG, "current spline:" + MonotoneSplineManager.this.mCurrentSplineName);
            if (MonotoneSplineManager.this.mSplineListener != null) {
                MonotoneSplineManager.this.mSplineListener.onSplineChanged(MonotoneSplineManager.this.mCurrentSplineName);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public String mCurrentSplineName = BrightnessConstants.DEFAULT_SPLINE;
    /* access modifiers changed from: private */
    public AtomicBoolean mIsBooted = new AtomicBoolean();
    /* access modifiers changed from: private */
    public AtomicBoolean mIsInitiated = new AtomicBoolean();
    /* access modifiers changed from: private */
    public ModelConfig mModelConfig;
    /* access modifiers changed from: private */
    public ISplineListener mSplineListener;
    /* access modifiers changed from: private */
    public Map<String, Spline> mSplines = new ConcurrentHashMap();

    public interface ISplineListener {
        void onSplineChanged(String str);

        void onSplineUpdated(String str);
    }

    private MonotoneSplineManager(Context context) {
        this.mContext = context;
        registerBootBroadcast();
        initSplines();
    }

    public static MonotoneSplineManager getInstance(Context context) {
        if (sMonotoneSplineManager == null) {
            synchronized (MonotoneSplineManager.class) {
                if (sMonotoneSplineManager == null) {
                    sMonotoneSplineManager = new MonotoneSplineManager(context);
                }
            }
        }
        return sMonotoneSplineManager;
    }

    public void onDestroy() {
        synchronized (MonotoneSplineManager.class) {
            sMonotoneSplineManager = null;
        }
    }

    private void initSplines() {
        if (this.mSplines.isEmpty()) {
            this.mModelConfig = ModelConfigUtil.getInstance().getModelConfig(this.mContext);
            if (!this.mSplines.containsKey(BrightnessConstants.DEFAULT_SPLINE)) {
                addSplines();
            }
        }
    }

    public void updateDayNightMode() {
        if (!this.mSplines.isEmpty()) {
            for (Spline spline : this.mSplines.values()) {
                spline.updateDayNightMode();
            }
        }
    }

    public synchronized void updateSpline(Context context, Map<String, List<BrightnessPoint>> appCentralPoints) {
        if (!AIBrightnessTrainSwitch.getInstance().isTrainEnable()) {
            ColorAILog.d(TAG, "Train strategy is disabled. Do nothing.");
            return;
        }
        boolean hasNewSplines = false;
        StringBuilder splineBuilder = new StringBuilder();
        ColorAILog.i(TAG, "Start updating splines after trained---------------------");
        for (Map.Entry<String, List<BrightnessPoint>> entry : appCentralPoints.entrySet()) {
            String pkgName = entry.getKey();
            List<BrightnessPoint> centralPoints = entry.getValue();
            ColorAILog.i(TAG, "name:" + pkgName + " central points:" + centralPoints);
            Spline spline = getSplineByName(pkgName);
            if (spline == null) {
                hasNewSplines = true;
                spline = new Spline(this.mModelConfig.getXs(), this.mModelConfig.getYs());
                spline.setName(pkgName);
                this.mSplines.put(pkgName, spline);
                ColorAILog.i(TAG, "create spline:" + pkgName);
            } else {
                ColorAILog.i(TAG, "update spline:" + pkgName);
            }
            splineBuilder.append(spline.updateSpline(context, pkgName, centralPoints));
        }
        if (splineBuilder.length() > 0) {
            ColorAILog.i(TAG, splineBuilder.toString());
            Settings.System.putString(context.getContentResolver(), BrightnessConstants.SETTINGS_NEW_POINTS, splineBuilder.toString());
        }
        if (hasNewSplines) {
            if (this.mAppSwitchDetectorUtil != null) {
                this.mAppSwitchDetectorUtil.unregister();
            }
            registerAppSwitchEvent();
        }
        ColorAILog.i(TAG, "Splines have been updated---------------------");
        saveSplinesToXml();
    }

    private void adjustPoint(Map<String, List<BrightnessPoint>> points) {
        Spline defaultSpline = new Spline(this.mModelConfig.getXs(), this.mModelConfig.getYs());
        for (Map.Entry<String, List<BrightnessPoint>> entry : points.entrySet()) {
            List<BrightnessPoint> appPoints = entry.getValue();
            ColorAILog.i(TAG, "name:" + entry.getKey() + "\tpoints:" + appPoints);
            if (appPoints != null && !appPoints.isEmpty()) {
                for (BrightnessPoint point : appPoints) {
                    BrightnessPoint defaultPoint = defaultSpline.findPointByX(point.x);
                    if (defaultPoint != null) {
                        float maxY = defaultPoint.y * 1.4f;
                        if (point.y > maxY) {
                            ColorAILog.i(TAG, "Current(" + point.x + ", " + point.y + ") Default(" + defaultSpline.restoreTransX(defaultPoint.x) + ", " + defaultPoint.y + ") \tAdjust brightness to:" + maxY);
                            point.y = maxY;
                        } else {
                            ColorAILog.i(TAG, "No need to adjust: Central point(" + point.x + ", " + point.y + ") Default point:" + defaultPoint);
                        }
                    } else {
                        ColorAILog.i(TAG, "No need to adjust: defaultPoint is null.");
                    }
                }
            } else {
                return;
            }
        }
    }

    private void saveSplinesToXml() {
        Map<String, List<BrightnessPoint>> splines = new HashMap<>(this.mSplines.size());
        for (Map.Entry<String, Spline> entry : this.mSplines.entrySet()) {
            String pkgName = entry.getKey();
            Spline spline = entry.getValue();
            if (spline != null && !spline.isTrained()) {
                ColorAILog.i(TAG, pkgName + " is not trained. So we don't save it to local file.");
            } else if (!(TextUtils.isEmpty(pkgName) || spline == null || spline.mPoints == null)) {
                List<BrightnessPoint> pointList = new ArrayList<>(spline.mPoints.size());
                for (BrightnessPoint point : spline.mPoints) {
                    if (point.x >= 0.0f) {
                        if (pointList.isEmpty() && point.x > 0.0f) {
                            pointList.add(spline.getMonotonePoints().get(0));
                        }
                        BrightnessPoint restoredPoint = new BrightnessPoint();
                        restoredPoint.x = spline.restoreTransX(point.x);
                        restoredPoint.y = point.y;
                        pointList.add(restoredPoint);
                    }
                }
                if (!pointList.isEmpty()) {
                    splines.put(pkgName, pointList);
                }
            }
        }
        ColorAILog.i(TAG, "Save restored spline:" + splines);
        DataHelper.getInstance().saveAppSplineToXml(splines);
    }

    public synchronized void resetSplines() {
        ColorAILog.d(TAG, "resetSplines:");
        Spline defaultSpline = this.mSplines.get(BrightnessConstants.DEFAULT_SPLINE);
        if (defaultSpline == null) {
            ColorAILog.e(TAG, "Oops! The default spline is gone.");
            return;
        }
        boolean needReset = false;
        Iterator<Spline> it = this.mSplines.values().iterator();
        while (true) {
            if (it.hasNext()) {
                Spline spline = it.next();
                if (spline != null && spline.isTrained()) {
                    needReset = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (needReset) {
            ConcurrentHashMap<String, Spline> newHashMap = new ConcurrentHashMap<>();
            BrightnessPoint dragPoint = defaultSpline.getRealDragPoint();
            if (defaultSpline.isTrained()) {
                defaultSpline = new Spline(this.mModelConfig.getXs(), this.mModelConfig.getYs());
                defaultSpline.setName(BrightnessConstants.DEFAULT_SPLINE);
                ColorAILog.i(TAG, "resetSplines---create default spline.");
                if (dragPoint != null) {
                    BrightnessPoint unused = defaultSpline.drag(dragPoint.x, dragPoint.y);
                    ColorAILog.i(TAG, "resetSplines---apply drag point to the default spline:" + dragPoint);
                }
            }
            newHashMap.put(BrightnessConstants.DEFAULT_SPLINE, defaultSpline);
            HashMap<String, SplineModel> splineModelHashMap = this.mModelConfig.getSplineModelHashMap();
            if (splineModelHashMap != null) {
                ColorAILog.i(TAG, "resetSplines---splineModelHashMap size:" + splineModelHashMap.size());
                if (!splineModelHashMap.isEmpty()) {
                    for (Map.Entry<String, SplineModel> splineEntry : splineModelHashMap.entrySet()) {
                        String name = splineEntry.getKey();
                        SplineModel splineModel = splineEntry.getValue();
                        Spline spline2 = this.mSplines.get(name);
                        if ((spline2 == null || spline2.isTrained()) && !TextUtils.isEmpty(name) && splineModel != null && splineModel.getYs() != null && splineModel.getXs() != null && splineModel.getXs().length == splineModel.getYs().length) {
                            spline2 = new Spline(splineModel.getXs(), splineModel.getYs());
                            spline2.setName(name);
                            ColorAILog.i(TAG, "resetSplines---create rus spline:" + name);
                            if (dragPoint != null) {
                                BrightnessPoint unused2 = defaultSpline.drag(dragPoint.x, dragPoint.y);
                                ColorAILog.i(TAG, "resetSplines---apply drag point to the rus spline:" + dragPoint);
                            }
                        }
                        if (spline2 != null) {
                            newHashMap.put(name, spline2);
                        }
                    }
                }
            }
            this.mSplines = newHashMap;
            if (!this.mSplines.containsKey(this.mCurrentSplineName)) {
                this.mCurrentSplineName = BrightnessConstants.DEFAULT_SPLINE;
            }
            if (this.mSplineListener != null) {
                this.mSplineListener.onSplineChanged(this.mCurrentSplineName);
            }
            DataHelper.getInstance().deleteAppSpline();
        }
    }

    public Spline getCurrentSpline() {
        return getSplineByName(this.mCurrentSplineName);
    }

    public BrightnessPoint drag(float x, float y) {
        BrightnessPoint dragPoint = null;
        if (!this.mSplines.isEmpty()) {
            for (Spline spline : this.mSplines.values()) {
                if (ColorAILog.sIsLogOn) {
                    ColorAILog.d(TAG, "drag(), spline: " + spline);
                }
                dragPoint = spline.drag(x, y);
            }
        }
        return dragPoint;
    }

    public void reset() {
        if (!this.mSplines.isEmpty()) {
            for (Spline spline : this.mSplines.values()) {
                spline.resetSpline();
            }
        }
    }

    private Spline getSplineByName(String splineName) {
        if (this.mSplines.isEmpty()) {
            ColorAILog.e(TAG, "Oops! No splines available, There must be something wrong with initSplines().");
            return null;
        } else if (this.mSplines.containsKey(splineName)) {
            return this.mSplines.get(splineName);
        } else {
            return null;
        }
    }

    private void addSplines() {
        addDefaultSpline();
        if (AIBrightnessTrainSwitch.getInstance().isTrainEnable()) {
            new Thread(new Runnable() {
                /* class com.android.server.display.ai.MonotoneSplineManager.AnonymousClass1 */

                public void run() {
                    MonotoneSplineManager.this.addAppSplines();
                    MonotoneSplineManager.this.mIsInitiated.set(true);
                    MonotoneSplineManager.this.registerAppSwitchEvent();
                }
            }).start();
            return;
        }
        ColorAILog.d(TAG, "Register AppSwitchEvent directly for the train strategy is disabled and we needn't add app splines.");
        this.mIsInitiated.set(true);
        registerAppSwitchEvent();
    }

    private boolean addDefaultSpline() {
        boolean hasNewSplines = false;
        ModelConfig modelConfig = this.mModelConfig;
        if (modelConfig == null) {
            return false;
        }
        Map<String, SplineModel> splineModelMap = modelConfig.getSplineModelHashMap();
        if (splineModelMap == null) {
            splineModelMap = new HashMap<>();
        }
        SplineModel defaultSplineModel = new SplineModel();
        defaultSplineModel.setXs(this.mModelConfig.getXs());
        defaultSplineModel.setYs(this.mModelConfig.getYs());
        splineModelMap.put(BrightnessConstants.DEFAULT_SPLINE, defaultSplineModel);
        ColorAILog.d(TAG, "Init default spline ---------- Start");
        for (Map.Entry<String, SplineModel> splineEntry : splineModelMap.entrySet()) {
            String name = splineEntry.getKey();
            SplineModel splineModel = splineEntry.getValue();
            if (!(TextUtils.isEmpty(name) || splineModel.getYs() == null || splineModel.getXs() == null)) {
                ColorAILog.d(TAG, "addDefaultSpline(),  name: " + name);
                Spline spline = new Spline(splineModel.getXs(), splineModel.getYs());
                spline.setName(name);
                if (!this.mSplines.containsKey(name)) {
                    hasNewSplines = true;
                }
                if (!this.mSplines.containsKey(name) || !this.mSplines.get(name).isTrained()) {
                    this.mSplines.put(name, spline);
                } else {
                    ColorAILog.d(TAG, "Ignore the " + name + " spline for it's already trained by user.");
                }
            }
        }
        ColorAILog.d(TAG, "Init default spline ---------- End");
        return hasNewSplines;
    }

    /* access modifiers changed from: private */
    public void addAppSplines() {
        Map<String, List<BrightnessPoint>> appSplines = DataHelper.getInstance().getTrainedAppSplinesFromXml();
        if (appSplines != null && appSplines.size() > 0) {
            ColorAILog.i(TAG, "Init app Spline ---------- Start");
            ColorAILog.d(TAG, "Adjust app points from local file:");
            adjustPoint(appSplines);
            for (Map.Entry<String, List<BrightnessPoint>> points : appSplines.entrySet()) {
                String pkgName = points.getKey();
                List<BrightnessPoint> pointList = points.getValue();
                MonotoneCubicSplineUtil.checkMonotone(this.mContext, pointList, null, MonotoneCubicSplineUtil.SOURCE_INIT);
                Spline spline = new Spline(pointList);
                spline.setTrained();
                spline.setName(pkgName);
                ColorAILog.d(TAG, "addAppSplines(), pkgName: " + pkgName + ", spline: " + spline.getPoints());
                this.mSplines.put(pkgName, spline);
            }
            ISplineListener iSplineListener = this.mSplineListener;
            if (iSplineListener != null) {
                iSplineListener.onSplineUpdated(this.mCurrentSplineName);
            }
            ColorAILog.i(TAG, "Init app Spline ---------- End");
        }
    }

    public void setIsBooted(boolean isBooted) {
        ColorAILog.d(TAG, "setIsBooted:" + isBooted);
        this.mIsBooted.set(isBooted);
        registerAppSwitchEvent();
    }

    private void registerBootBroadcast() {
        BootCompletedReceiver.getInstance(this.mContext).setBootListener(new BootCompletedReceiver.IBootListener() {
            /* class com.android.server.display.ai.MonotoneSplineManager.AnonymousClass2 */

            @Override // com.android.server.display.ai.broadcastreceiver.BootCompletedReceiver.IBootListener
            public void onBootCompleted() {
                MonotoneSplineManager.this.mIsBooted.set(true);
                MonotoneSplineManager.this.registerAppSwitchEvent();
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005c, code lost:
        return;
     */
    public synchronized void registerAppSwitchEvent() {
        if (this.mIsBooted.get() && this.mIsInitiated.get()) {
            try {
                if (this.mAppSwitchDetectorUtil == null) {
                    this.mAppSwitchDetectorUtil = AppSwitchDetectorUtil.getInstance(this.mContext);
                }
                Set<String> keySet = this.mSplines.keySet();
                if (keySet.size() > 1) {
                    this.mAppSwitchDetectorUtil.addListener(this.mAppSwitchListener);
                    List<String> pkgNames = new ArrayList<>(keySet);
                    pkgNames.addAll(this.mAppSwitchDetectorUtil.getLauncherAppList(this.mContext));
                    ColorAILog.d(TAG, "Register app switch event!");
                    this.mAppSwitchDetectorUtil.register(pkgNames);
                }
            } catch (Exception e) {
                ColorAILog.e(TAG, e.getMessage());
            }
        }
    }

    public void addSplineListener(ISplineListener listener) {
        this.mSplineListener = listener;
    }

    public class Spline {
        private DayNightMode mDayNightMode;
        private DragSpline mDragSpline;
        private AtomicBoolean mIsTrained;
        private volatile List<BrightnessPoint> mMonotonePoints;
        private String mName;
        /* access modifiers changed from: private */
        public volatile List<BrightnessPoint> mPoints;

        private Spline(float[] xs, float[] ys) {
            this.mIsTrained = new AtomicBoolean(false);
            ArrayList<BrightnessPoint> points = new ArrayList<>();
            float[] xsTrans = new float[xs.length];
            int length = xs.length;
            for (int i = 0; i < length; i++) {
                xsTrans[i] = transX(xs[i]);
            }
            int length2 = xsTrans.length;
            for (int i2 = 0; i2 < length2; i2++) {
                points.add(new BrightnessPoint(xsTrans[i2], ys[i2]));
                ColorAILog.d(MonotoneSplineManager.TAG, "(" + xsTrans[i2] + ", " + ys[i2] + ")");
            }
            init(points);
        }

        /* access modifiers changed from: private */
        public void setTrained() {
            this.mIsTrained.set(true);
        }

        public boolean isTrained() {
            return this.mIsTrained.get();
        }

        private Spline(List<BrightnessPoint> points) {
            this.mIsTrained = new AtomicBoolean(false);
            if (points == null) {
                ColorAILog.d(MonotoneSplineManager.TAG, "Fail to initiate Spline for poins are null.");
                return;
            }
            ArrayList<BrightnessPoint> transferedPoints = new ArrayList<>();
            for (int i = 0; i < points.size(); i++) {
                transferedPoints.add(new BrightnessPoint(transX(points.get(i).x), points.get(i).y));
            }
            init(transferedPoints);
        }

        private void init(List<BrightnessPoint> points) {
            this.mPoints = points;
            this.mMonotonePoints = generateMonotonePoints(this.mPoints);
            this.mDayNightMode = new DayNightMode(MonotoneSplineManager.this.mModelConfig);
            updateDayNightMode();
        }

        /* access modifiers changed from: private */
        public void updateDayNightMode() {
            DayNightMode dayNightMode = this.mDayNightMode;
            if (dayNightMode != null) {
                dayNightMode.updateDayNightMode(this.mMonotonePoints);
            }
            DragSpline dragSpline = this.mDragSpline;
            if (dragSpline != null) {
                dragSpline.updateDayNightMode();
            }
        }

        public synchronized List<BrightnessPoint> getPoints() {
            return this.mPoints;
        }

        public synchronized List<BrightnessPoint> getMonotonePoints() {
            return this.mMonotonePoints;
        }

        public synchronized List<BrightnessPoint> getCurrFinalPoints() {
            List<BrightnessPoint> monotonePoint;
            monotonePoint = this.mMonotonePoints;
            if (!(this.mDragSpline == null || this.mDragSpline.mDragUniformPoints == null)) {
                monotonePoint = this.mDragSpline.mDragUniformPoints;
            }
            return monotonePoint;
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x0101, code lost:
            return null;
         */
        public synchronized String updateSpline(Context context, String name, List<BrightnessPoint> trainedPoints) {
            if (trainedPoints != null) {
                if (!trainedPoints.isEmpty()) {
                    setTrained();
                    String originalPointsStr = convertPointsToStr(this.mPoints);
                    for (BrightnessPoint trainedPoint : trainedPoints) {
                        List<BrightnessPoint> points = updateSplineWithPoint(trainedPoint);
                        if (points != null) {
                            MonotoneCubicSplineUtil.checkMonotone(MonotoneSplineManager.this.mContext, points, null, MonotoneCubicSplineUtil.SOURCE_TRAIN);
                            if (points.size() < BrightnessConstants.DefaultConfig.XS.length) {
                                this.mMonotonePoints = generateMonotonePoints(points);
                                List<BrightnessPoint> newPoints = new ArrayList<>(BrightnessConstants.DefaultConfig.XS.length + 1);
                                float[] fArr = BrightnessConstants.DefaultConfig.XS;
                                int length = fArr.length;
                                boolean z = false;
                                boolean isTrainedPointAdded = false;
                                int i = 0;
                                while (i < length) {
                                    float transX = transX(fArr[i]);
                                    if (!isTrainedPointAdded && transX > trainedPoint.x) {
                                        newPoints.add(trainedPoint);
                                        isTrainedPointAdded = true;
                                    }
                                    if (((double) Math.abs(transX - trainedPoint.x)) >= 0.01d) {
                                        BrightnessPoint point = findPointByTransferedX(transX, z);
                                        if (point != null) {
                                            newPoints.add(point);
                                        } else {
                                            ColorAILog.e(MonotoneSplineManager.TAG, "Oops! The point is null!");
                                        }
                                    }
                                    i++;
                                    z = false;
                                }
                                if (newPoints.size() >= BrightnessConstants.DefaultConfig.XS.length) {
                                    this.mPoints = newPoints;
                                }
                            } else {
                                ColorAILog.d(MonotoneSplineManager.TAG, "Update new points directly after trained!");
                                this.mPoints = points;
                            }
                        }
                    }
                    String newPointsStr = convertPointsToStr(this.mPoints);
                    this.mMonotonePoints = generateMonotonePoints(this.mPoints);
                    String statistics = "[" + name + " " + originalPointsStr + " " + newPointsStr + "]";
                    ColorAILog.i(MonotoneSplineManager.TAG, "Statistic-Trained Spline:" + statistics);
                    return statistics;
                }
            }
        }

        /* access modifiers changed from: private */
        public BrightnessPoint drag(float x, float y) {
            if (this.mDragSpline == null) {
                this.mDragSpline = new DragSpline();
            }
            return this.mDragSpline.drag(x, y);
        }

        public String getDragPointDumpString() {
            DragSpline dragSpline = this.mDragSpline;
            if (dragSpline != null) {
                return dragSpline.getDragPointDumpString();
            }
            return "mDragSrcPoints is null";
        }

        private BrightnessPoint findPointByY(float y, boolean needDragSpline) {
            DragSpline dragSpline;
            List<BrightnessPoint> monotonePoint = this.mMonotonePoints;
            if (!(!needDragSpline || (dragSpline = this.mDragSpline) == null || dragSpline.mDragUniformPoints == null)) {
                monotonePoint = this.mDragSpline.mDragUniformPoints;
            }
            if (monotonePoint == null) {
                return null;
            }
            BrightnessPoint minPoint = monotonePoint.get(0);
            BrightnessPoint maxPoint = monotonePoint.get(monotonePoint.size() - 1);
            if (y <= minPoint.y) {
                return BrightnessPoint.createPoint(minPoint);
            }
            if (y >= maxPoint.y) {
                return BrightnessPoint.createPoint(maxPoint);
            }
            BrightnessPoint point = maxPoint;
            int length = monotonePoint.size() - 1;
            for (int i = 0; i < length; i++) {
                BrightnessPoint prePoint = monotonePoint.get(i);
                point = monotonePoint.get(i + 1);
                if (prePoint.y < y && y <= point.y) {
                    break;
                }
            }
            return BrightnessPoint.createPoint(point);
        }

        private BrightnessPoint findPointByTransferedX(float x, boolean needDragSpline) {
            DragSpline dragSpline;
            List<BrightnessPoint> monotonePoint = this.mMonotonePoints;
            if (!(!needDragSpline || (dragSpline = this.mDragSpline) == null || dragSpline.mDragUniformPoints == null)) {
                monotonePoint = this.mDragSpline.mDragUniformPoints;
            }
            if (monotonePoint == null) {
                return null;
            }
            BrightnessPoint minPoint = monotonePoint.get(0);
            BrightnessPoint maxPoint = monotonePoint.get(monotonePoint.size() - 1);
            if (x <= minPoint.x) {
                return BrightnessPoint.createPoint(minPoint);
            }
            if (x >= maxPoint.x) {
                return BrightnessPoint.createPoint(maxPoint);
            }
            BrightnessPoint rightPoint = null;
            int length = monotonePoint.size() - 1;
            for (int i = 0; i < length; i++) {
                BrightnessPoint leftPoint = monotonePoint.get(i);
                rightPoint = monotonePoint.get(i + 1);
                if (leftPoint.x < x && x <= rightPoint.x) {
                    break;
                }
            }
            if (rightPoint != null) {
                return BrightnessPoint.createPoint(rightPoint);
            }
            return BrightnessPoint.createPoint(maxPoint);
        }

        public BrightnessPoint findPointByX(float x) {
            return findPointByTransferedX(transX(x), true);
        }

        public BrightnessPoint findPointByY(float y) {
            return findPointByY(y, true);
        }

        public BrightnessPoint getDragPoint() {
            DragSpline dragSpline = this.mDragSpline;
            if (dragSpline == null) {
                return null;
            }
            return dragSpline.getDragPoint();
        }

        public BrightnessPoint getRealDragPoint() {
            BrightnessPoint dragPoint = getDragPoint();
            if (dragPoint == null) {
                return null;
            }
            BrightnessPoint point = new BrightnessPoint();
            point.x = restoreTransX(dragPoint.x);
            point.y = dragPoint.y;
            return point;
        }

        public List<BrightnessPoint> getDragPoints() {
            DragSpline dragSpline = this.mDragSpline;
            if (dragSpline == null) {
                return null;
            }
            return dragSpline.getDragPoints();
        }

        public List<BrightnessPoint> getDragUniformPoints() {
            DragSpline dragSpline = this.mDragSpline;
            if (dragSpline == null) {
                return null;
            }
            return dragSpline.getDragUniformPoints();
        }

        public float transX(float lux) {
            if (lux < MonotoneSplineManager.this.mModelConfig.getReviseXChangePoint()) {
                return (float) Math.sqrt((double) lux);
            }
            return ((float) (Math.sqrt((double) lux) + ((double) MonotoneSplineManager.this.mModelConfig.getReviseX()))) / MonotoneSplineManager.this.mModelConfig.getReviseXMultiple();
        }

        /* JADX INFO: Multiple debug info for r0v7 float: [D('tmp' float), D('lux' float)] */
        public float restoreTransX(float x) {
            if (((double) x) < Math.sqrt((double) MonotoneSplineManager.this.mModelConfig.getReviseXChangePoint())) {
                return x >= 0.0f ? x * x : -(x * x);
            }
            float tmp = (MonotoneSplineManager.this.mModelConfig.getReviseXMultiple() * x) - MonotoneSplineManager.this.mModelConfig.getReviseX();
            return tmp * tmp;
        }

        /* access modifiers changed from: private */
        public void resetSpline() {
            this.mDragSpline = null;
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x0224, code lost:
            return null;
         */
        public synchronized List<BrightnessPoint> updateSplineWithPoint(BrightnessPoint point) {
            float leftX;
            float rightX;
            BrightnessPoint leftPoint;
            ColorAILog.i(MonotoneSplineManager.TAG, "updateSplineWithPoint: (" + point.x + ", " + point.y + ")");
            point.x = transX(point.x);
            float y = point.y;
            float lastPointY = this.mPoints.get(this.mPoints.size() + -1).y;
            if (y > 0.0f) {
                point.y = y > lastPointY ? lastPointY : y;
            } else {
                point.y = 0.0f;
            }
            BrightnessPoint pointByY = findPointByY(point.y, false);
            if (pointByY == null) {
                ColorAILog.w(MonotoneSplineManager.TAG, "updateSplineWithPoint return null because findPointByY is null.");
                return null;
            }
            float deltaX = point.x - pointByY.x;
            ColorAILog.i(MonotoneSplineManager.TAG, "transfered point: (" + point.x + ", " + point.y + ")findPointByY:" + pointByY.x);
            if (deltaX > 0.0f) {
                leftX = point.x - (MonotoneSplineManager.this.mModelConfig.getDragExpandMultipleLeft() * deltaX);
                rightX = point.x + (MonotoneSplineManager.this.mModelConfig.getDragExpandMultipleRight() * deltaX);
                ColorAILog.d(MonotoneSplineManager.TAG, "leftx: " + leftX + " rightx:" + rightX);
            } else {
                leftX = point.x + (MonotoneSplineManager.this.mModelConfig.getDragExpandMultipleLeft() * deltaX);
                rightX = point.x - (MonotoneSplineManager.this.mModelConfig.getDragExpandMultipleRight() * deltaX);
                ColorAILog.d(MonotoneSplineManager.TAG, "leftx: " + leftX + " rightx:" + rightX);
            }
            if (leftX < 0.0f) {
                leftPoint = new BrightnessPoint(leftX, 0.0f);
            } else {
                leftPoint = findPointByTransferedX(leftX, false);
            }
            BrightnessPoint rightPoint = findPointByTransferedX(rightX, false);
            if (leftPoint != null && rightPoint != null) {
                ColorAILog.d(MonotoneSplineManager.TAG, "left: (" + restoreTransX(leftPoint.x) + ", " + leftPoint.y + ")");
                ColorAILog.d(MonotoneSplineManager.TAG, "right: (" + restoreTransX(rightPoint.x) + ", " + rightPoint.y + ")");
                ArrayList<BrightnessPoint> newPoints = new ArrayList<>();
                if (Float.compare(leftPoint.x, rightPoint.x) == 0) {
                    newPoints.addAll(this.mPoints);
                    if (point.x == 0.0f && !newPoints.isEmpty()) {
                        newPoints.get(0).y = point.y;
                        ColorAILog.i(MonotoneSplineManager.TAG, "drag in the darkest case, target y is " + y);
                    }
                } else {
                    int index = 0;
                    int length = this.mPoints.size();
                    while (index < length) {
                        BrightnessPoint srcPoint = this.mPoints.get(index);
                        if (srcPoint.x >= leftPoint.x) {
                            break;
                        }
                        newPoints.add(srcPoint);
                        index++;
                    }
                    newPoints.add(leftPoint);
                    newPoints.add(point);
                    newPoints.add(rightPoint);
                    for (int i = index; i < length; i++) {
                        BrightnessPoint srcPoint2 = this.mPoints.get(i);
                        if (srcPoint2.x > rightPoint.x) {
                            newPoints.add(srcPoint2);
                        }
                    }
                }
                ColorAILog.i(MonotoneSplineManager.TAG, "New points:" + newPoints);
                return newPoints;
            }
        }

        /* access modifiers changed from: private */
        public void setName(String name) {
            this.mName = name;
        }

        public String getName() {
            return this.mName;
        }

        private String convertPointsToStr(List<BrightnessPoint> points) {
            StringBuilder originalPointsBuilder = new StringBuilder();
            originalPointsBuilder.append("[");
            for (BrightnessPoint point : points) {
                originalPointsBuilder.append("(");
                originalPointsBuilder.append(Math.round(restoreTransX(point.x)));
                originalPointsBuilder.append(", ");
                originalPointsBuilder.append((int) point.y);
                originalPointsBuilder.append("), ");
            }
            originalPointsBuilder.deleteCharAt(originalPointsBuilder.length() - 2);
            originalPointsBuilder.append("]");
            return originalPointsBuilder.toString();
        }

        /* access modifiers changed from: private */
        public List<BrightnessPoint> generateMonotonePoints(List<BrightnessPoint> points) {
            if (points == null || points.size() < 2) {
                return null;
            }
            return MonotoneCubicSplineUtil.spline(points, MonotoneSplineManager.this.mModelConfig.getSplinePointSize());
        }

        private class DragSpline {
            private static final String EMPTY_DRAG_LOG_STRING = "mDragSrcPoints is null";
            private DayNightMode mDayNightMode;
            private BrightnessPoint mDragPoint = new BrightnessPoint();
            private List<BrightnessPoint> mDragPoints = null;
            /* access modifiers changed from: private */
            public List<BrightnessPoint> mDragUniformPoints = null;

            public DragSpline() {
                this.mDayNightMode = new DayNightMode(MonotoneSplineManager.this.mModelConfig);
            }

            /* access modifiers changed from: private */
            public BrightnessPoint drag(float x, float y) {
                BrightnessPoint brightnessPoint = this.mDragPoint;
                brightnessPoint.x = x;
                brightnessPoint.y = y;
                ColorAILog.d(MonotoneSplineManager.TAG, "Drag " + Spline.this.getName() + " (" + x + ", " + y + ")");
                this.mDragPoints = Spline.this.updateSplineWithPoint(this.mDragPoint);
                MonotoneCubicSplineUtil.checkMonotone(MonotoneSplineManager.this.mContext, this.mDragPoints, this.mDragPoint, MonotoneCubicSplineUtil.SOURCE_DRAG);
                List<BrightnessPoint> list = this.mDragPoints;
                if (list != null) {
                    this.mDragUniformPoints = Spline.this.generateMonotonePoints(list);
                }
                return this.mDragPoint;
            }

            public void updateDayNightMode() {
                this.mDayNightMode.updateDayNightMode(this.mDragUniformPoints);
            }

            /* access modifiers changed from: private */
            public BrightnessPoint getDragPoint() {
                return this.mDragPoint;
            }

            /* access modifiers changed from: private */
            public List<BrightnessPoint> getDragPoints() {
                return this.mDragPoints;
            }

            /* access modifiers changed from: private */
            public List<BrightnessPoint> getDragUniformPoints() {
                return this.mDragUniformPoints;
            }

            /* access modifiers changed from: private */
            public String getDragPointDumpString() {
                if (this.mDragPoints == null) {
                    return EMPTY_DRAG_LOG_STRING;
                }
                StringBuilder sb = new StringBuilder("mDragSrcPoints:");
                for (BrightnessPoint point : this.mDragPoints) {
                    sb.append("(");
                    sb.append(Spline.this.restoreTransX(point.x));
                    sb.append(",");
                    sb.append(point.y);
                    sb.append("),");
                }
                sb.append("mDragPoint:");
                sb.append(Spline.this.restoreTransX(this.mDragPoint.x));
                sb.append(", ");
                sb.append(this.mDragPoint.y);
                return sb.toString();
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append(", sourcePoint: ");
            if (this.mPoints != null) {
                for (BrightnessPoint point : this.mPoints) {
                    sb.append("(");
                    sb.append(restoreTransX(point.x));
                    sb.append(",");
                    sb.append(point.y);
                    sb.append("),");
                }
            } else {
                sb.append("null");
            }
            return sb.toString();
        }
    }
}
