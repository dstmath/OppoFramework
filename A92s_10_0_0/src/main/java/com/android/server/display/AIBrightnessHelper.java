package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.util.Slog;
import com.android.server.pm.Settings;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class AIBrightnessHelper {
    private static final String AI_BRIGHTNESS_MODEL_CLASS = "com.coloros.proton.brightness.AIBrightnessModel";
    private static final String AI_BRIGHTNESS_PACKAGE = "com.coloros.deepthinker";
    private static final int DEFAULT_NEXT_CHANGE = 1;
    private static final int MSG_ON_BRIGHTNESS_CHANGE = 0;
    private static final int MSG_ON_RUS_CHANGE = 1;
    private static final int MSG_ON_TARGET_BRIGHTNESS_CHANGE = 2;
    private static final String TAG = "AIBrightnessHelper";
    /* access modifiers changed from: private */
    public AIBrightnessClient mAIBrightnessClient;
    /* access modifiers changed from: private */
    public BrightnessChangeListener mBrightnessChangeListener;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final String mDeviceName;
    private Handler mHandler = new Handler() {
        /* class com.android.server.display.AIBrightnessHelper.AnonymousClass1 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i == 1) {
                    AIBrightnessHelper aIBrightnessHelper = AIBrightnessHelper.this;
                    aIBrightnessHelper.prepareUpgradeModel(aIBrightnessHelper.mContext);
                } else if (i == 2 && AIBrightnessHelper.this.mBrightnessChangeListener != null) {
                    AIBrightnessHelper.this.mBrightnessChangeListener.onTargetBrightnessChanged(((Float) msg.obj).floatValue());
                }
            } else if (AIBrightnessHelper.this.mBrightnessChangeListener != null) {
                AIBrightnessHelper.this.mBrightnessChangeListener.onBrightnessChanged(((Float) msg.obj).floatValue());
            }
        }
    };
    private boolean mIsFeatureOpen = false;
    /* access modifiers changed from: private */
    public boolean mNeedUpgradeModel = false;
    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.AIBrightnessHelper.AnonymousClass2 */

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0053 A[ADDED_TO_REGION] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0060 A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0067  */
        /* JADX WARNING: Removed duplicated region for block: B:28:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            char c;
            Uri uri;
            String action = intent.getAction();
            Slog.d(AIBrightnessHelper.TAG, "onReceive, mPackageReceiver action:" + action);
            int hashCode = action.hashCode();
            if (hashCode != -810471698) {
                if (hashCode != 525384130) {
                    if (hashCode == 1544582882 && action.equals("android.intent.action.PACKAGE_ADDED")) {
                        c = 0;
                        if (c != 0 || c == 1) {
                            if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                                return;
                            }
                        } else if (c != 2) {
                            return;
                        }
                        uri = intent.getData();
                        if (uri == null) {
                            String packageName = uri.getEncodedSchemeSpecificPart();
                            Slog.d(AIBrightnessHelper.TAG, "onReceive, packageName:" + packageName);
                            if (AIBrightnessHelper.AI_BRIGHTNESS_PACKAGE.equals(packageName)) {
                                AIBrightnessHelper.this.prepareUpgradeModel(context);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    c = 1;
                    if (c != 0) {
                    }
                    if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    }
                    uri = intent.getData();
                    if (uri == null) {
                    }
                }
            } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                c = 2;
                if (c != 0) {
                }
                if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                }
                uri = intent.getData();
                if (uri == null) {
                }
            }
            c = 65535;
            if (c != 0) {
            }
            if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
            }
            uri = intent.getData();
            if (uri == null) {
            }
        }
    };
    private UpgradeBroadcastReceiver mUpgradeReceiver = new UpgradeBroadcastReceiver();

    /* access modifiers changed from: package-private */
    public interface BrightnessChangeListener {
        void onBrightnessChanged(float f);

        void onSwitchChanged(boolean z);

        void onTargetBrightnessChanged(float f);
    }

    private class UpgradeBroadcastReceiver extends BroadcastReceiver {
        private boolean mRegistered;

        private UpgradeBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(AIBrightnessHelper.TAG, "onReceive, mUpgradeReceiver action:" + action);
            if (((action.hashCode() == -2128145023 && action.equals("android.intent.action.SCREEN_OFF")) ? (char) 0 : 65535) == 0) {
                if (AIBrightnessHelper.this.mNeedUpgradeModel) {
                    Slog.i(AIBrightnessHelper.TAG, "onReceive, ACTION_SCREEN_OFF do upgrade.");
                    boolean unused = AIBrightnessHelper.this.mNeedUpgradeModel = false;
                    AIBrightnessHelper.this.upgradeModel(context);
                }
                if (AIBrightnessHelper.this.mAIBrightnessClient != null) {
                    Slog.i(AIBrightnessHelper.TAG, "onReceive, ACTION_SCREEN_OFF do onScreenEvent.");
                    AIBrightnessHelper.this.mAIBrightnessClient.onScreenEvent(false);
                }
            }
        }

        public synchronized Intent register(Context context, IntentFilter filter) {
            Intent intent;
            try {
                if (!this.mRegistered) {
                    try {
                        intent = context.getApplicationContext().registerReceiver(this, filter);
                    } catch (Throwable th) {
                        th = th;
                    }
                } else {
                    intent = null;
                }
                this.mRegistered = true;
            } catch (Throwable th2) {
                th = th2;
                this.mRegistered = true;
                throw th;
            }
            return intent;
        }

        public synchronized void unregister(Context context) {
            try {
                if (this.mRegistered) {
                    try {
                        context.getApplicationContext().unregisterReceiver(this);
                    } catch (Throwable th) {
                        th = th;
                    }
                }
                this.mRegistered = false;
            } catch (Throwable th2) {
                th = th2;
                this.mRegistered = false;
                throw th;
            }
        }
    }

    AIBrightnessHelper(Context context, String deviceName) {
        this.mDeviceName = deviceName;
        this.mContext = context;
        initModel(context, deviceName);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addDataScheme(Settings.ATTR_PACKAGE);
        context.registerReceiver(this.mPackageReceiver, intentFilter);
        IntentFilter screenIntentFilter = new IntentFilter();
        screenIntentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mUpgradeReceiver.register(context, screenIntentFilter);
    }

    private synchronized void initModel(Context context, String deviceName) {
        this.mAIBrightnessClient = new AIBrightnessAppContext(context).createAIBrightnessClient(deviceName);
        if (this.mAIBrightnessClient != null) {
            this.mIsFeatureOpen = this.mAIBrightnessClient.getMainSwitch();
            Slog.d(TAG, "initModel mIsFeatureOpen:" + this.mIsFeatureOpen);
        } else {
            this.mIsFeatureOpen = false;
            Slog.w(TAG, "initModel failed, mIsFeatureOpen is false.");
        }
    }

    /* access modifiers changed from: private */
    public void prepareUpgradeModel(Context context) {
        this.mNeedUpgradeModel = true;
        Slog.d(TAG, "onReceive, next ACTION_SCREEN_OFF upgrade.");
    }

    /* access modifiers changed from: private */
    public synchronized void upgradeModel(Context context) {
        boolean isFeatureOpen;
        if (this.mAIBrightnessClient != null) {
            this.mAIBrightnessClient = new AIBrightnessAppContext(context).upgradeAIBrightnessClient(this.mDeviceName, this.mAIBrightnessClient.onSaveAndRelease());
            if (this.mAIBrightnessClient != null) {
                this.mAIBrightnessClient.setCallbackHandler(this.mHandler);
                isFeatureOpen = this.mAIBrightnessClient.getMainSwitch();
                Slog.i(TAG, "upgradeModel mAIBrightnessClient create success.");
            } else {
                isFeatureOpen = false;
                Slog.w(TAG, "upgradeModel failed, mIsFeatureOpen is false.");
            }
            if (isFeatureOpen != this.mIsFeatureOpen) {
                this.mIsFeatureOpen = isFeatureOpen;
                Slog.i(TAG, "upgradeModel onSwitchChanged:" + this.mIsFeatureOpen);
                if (this.mBrightnessChangeListener != null) {
                    this.mBrightnessChangeListener.onSwitchChanged(isFeatureOpen);
                }
            }
        } else {
            Slog.w(TAG, "upgradeModel, mAIBrightnessClient == null, go initModel");
            initModel(context, this.mDeviceName);
            if (this.mIsFeatureOpen) {
                Slog.i(TAG, "upgradeModel onSwitchChanged:true");
                if (this.mBrightnessChangeListener != null) {
                    this.mBrightnessChangeListener.onSwitchChanged(this.mIsFeatureOpen);
                }
            }
        }
    }

    public boolean isFeatureOpen() {
        return this.mIsFeatureOpen;
    }

    public void setLux(float lux) {
        AIBrightnessClient aIBrightnessClient = this.mAIBrightnessClient;
        if (aIBrightnessClient != null) {
            aIBrightnessClient.setLux(lux);
        }
    }

    public void setBrightnessByUser(float brightness) {
        AIBrightnessClient aIBrightnessClient = this.mAIBrightnessClient;
        if (aIBrightnessClient != null) {
            aIBrightnessClient.setBrightnessByUser(brightness);
        }
    }

    public void reset() {
        AIBrightnessClient aIBrightnessClient = this.mAIBrightnessClient;
        if (aIBrightnessClient != null) {
            aIBrightnessClient.reset();
        }
    }

    public float getNextChange(int targetY, float currY, float timeDelta) {
        AIBrightnessClient aIBrightnessClient = this.mAIBrightnessClient;
        if (aIBrightnessClient != null) {
            return aIBrightnessClient.getNextChange(targetY, currY, timeDelta);
        }
        return 1.0f;
    }

    public void setBrightnessChangeListener(BrightnessChangeListener listener) {
        this.mBrightnessChangeListener = listener;
        AIBrightnessClient aIBrightnessClient = this.mAIBrightnessClient;
        if (aIBrightnessClient != null) {
            aIBrightnessClient.setCallbackHandler(this.mHandler);
        }
    }

    public boolean setStateChanged(int msgId, Bundle extraData) {
        if (!this.mIsFeatureOpen) {
            return false;
        }
        this.mAIBrightnessClient.setStateChanged(msgId, extraData);
        return true;
    }

    /* access modifiers changed from: private */
    public static class AIBrightnessClient {
        Class<?> mClass;
        Object mInstance;
        HashMap<String, Method> mMethodHashMap = new HashMap<>();

        AIBrightnessClient(Object aiBrightnessObject, Class<?> clazz) {
            this.mInstance = aiBrightnessObject;
            this.mClass = clazz;
        }

        /* access modifiers changed from: package-private */
        public boolean getMainSwitch() {
            return ((Boolean) ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "getMainSwitch", null, null)).booleanValue();
        }

        /* access modifiers changed from: package-private */
        public void setLux(float lux) {
            Object unused = ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "setLux", new Class[]{Float.TYPE}, new Object[]{Float.valueOf(lux)});
        }

        /* access modifiers changed from: package-private */
        public void setBrightnessByUser(float brightness) {
            Object unused = ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "setBrightnessByUser", new Class[]{Float.TYPE}, new Object[]{Float.valueOf(brightness)});
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            Object unused = ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "resetByUser", null, null);
        }

        /* access modifiers changed from: package-private */
        public void setCallbackHandler(Handler handler) {
            Object unused = ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "setCallbackHandler", new Class[]{Handler.class}, new Object[]{handler});
        }

        /* access modifiers changed from: package-private */
        public Bundle onSaveAndRelease() {
            Bundle saveBundle = (Bundle) ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "onSaveAndRelease", null, null);
            Slog.i(AIBrightnessHelper.TAG, "onSaveAndRelease saveBundle:" + saveBundle);
            return saveBundle;
        }

        /* access modifiers changed from: package-private */
        public float getNextChange(int targetY, float currY, float timeDelta) {
            System.nanoTime();
            return ((Float) ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "getNextChange", new Class[]{Integer.TYPE, Float.TYPE, Float.TYPE}, new Object[]{Integer.valueOf(targetY), Float.valueOf(currY), Float.valueOf(timeDelta)})).floatValue();
        }

        /* access modifiers changed from: package-private */
        public void onScreenEvent(boolean isScreenOn) {
            Object unused = ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "onScreenEvent", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(isScreenOn)});
        }

        /* access modifiers changed from: package-private */
        public void setStateChanged(int msgId, Bundle extraData) {
            Object unused = ReflectUtils.invoke(this.mMethodHashMap, this.mInstance, this.mClass, "setStateChanged", new Class[]{Integer.TYPE, Bundle.class}, new Object[]{Integer.valueOf(msgId), extraData});
        }
    }

    private static class AIBrightnessAppContext {
        private Context mAIBrightnessAppContext;
        private ClassLoader mClassLoader;
        private Context mContext;
        private Class<?> mControllerClass;

        private AIBrightnessAppContext(Context context) {
            long startLoad = System.currentTimeMillis();
            this.mContext = context;
            this.mAIBrightnessAppContext = context.createPackageContext(AIBrightnessHelper.AI_BRIGHTNESS_PACKAGE, 3);
            Slog.i(AIBrightnessHelper.TAG, "AIBrightnessAppContext new ClassLoader:" + this.mAIBrightnessAppContext.getClassLoader());
            this.mClassLoader = this.mAIBrightnessAppContext.getClassLoader();
            try {
                this.mControllerClass = this.mClassLoader.loadClass(AIBrightnessHelper.AI_BRIGHTNESS_MODEL_CLASS);
            } catch (ClassNotFoundException e) {
                try {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException | IllegalArgumentException e2) {
                    Slog.w(AIBrightnessHelper.TAG, "AIBrightnessAppContext new, failed:" + e2);
                    return;
                }
            }
            long endLoad = System.currentTimeMillis();
            Slog.i(AIBrightnessHelper.TAG, "AIBrightnessAppContext new time:" + (endLoad - startLoad));
        }

        /* access modifiers changed from: private */
        public AIBrightnessClient createAIBrightnessClient(String deviceName) {
            AIBrightnessClient controller = null;
            if (this.mControllerClass == null) {
                return null;
            }
            try {
                Slog.i(AIBrightnessHelper.TAG, "createAIBrightnessClient loadClass for step:" + deviceName);
                long startLoad = System.currentTimeMillis();
                Constructor constructor = this.mControllerClass.getConstructor(Context.class, String.class);
                if (constructor != null) {
                    Slog.i(AIBrightnessHelper.TAG, "createAIBrightnessClient invoke Constructor: " + constructor);
                    controller = new AIBrightnessClient(constructor.newInstance(this.mContext, deviceName), this.mControllerClass);
                }
                if (controller == null) {
                    Slog.i(AIBrightnessHelper.TAG, "createAIBrightnessClient error.");
                    return null;
                }
                long endLoad = System.currentTimeMillis();
                Slog.i(AIBrightnessHelper.TAG, "createAIBrightnessClient time:" + (endLoad - startLoad));
                return controller;
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        /* access modifiers changed from: private */
        public AIBrightnessClient upgradeAIBrightnessClient(String deviceName, Bundle outsideBundle) {
            AIBrightnessClient controller = null;
            if (this.mControllerClass == null) {
                return null;
            }
            Parcel parcel = Parcel.obtain();
            parcel.writeBundle(outsideBundle);
            Slog.i(AIBrightnessHelper.TAG, "upgradeAIBrightnessClient parcel:" + parcel);
            parcel.setDataPosition(0);
            Bundle restoreBundle = parcel.readBundle(this.mClassLoader);
            parcel.recycle();
            try {
                Slog.i(AIBrightnessHelper.TAG, "upgradeAIBrightnessClient loadClass for step:" + restoreBundle);
                long startLoad = System.currentTimeMillis();
                Constructor constructor = this.mControllerClass.getConstructor(Context.class, String.class, Bundle.class);
                if (constructor != null) {
                    Slog.i(AIBrightnessHelper.TAG, "upgradeAIBrightnessClient invoke Constructor: " + constructor);
                    controller = new AIBrightnessClient(constructor.newInstance(this.mContext, deviceName, restoreBundle), this.mControllerClass);
                }
                if (controller == null) {
                    Slog.i(AIBrightnessHelper.TAG, "upgradeAIBrightnessClient error.");
                    return null;
                }
                long endLoad = System.currentTimeMillis();
                Slog.i(AIBrightnessHelper.TAG, "upgradeAIBrightnessClient time:" + (endLoad - startLoad));
                return controller;
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ReflectUtils {
        private ReflectUtils() {
        }

        /* access modifiers changed from: private */
        public static Object invoke(HashMap<String, Method> methodCacheMap, Object obj, Class<?> objClass, String methodName, Class<?>[] args, Object[] agrsObj) {
            try {
                Method method = methodCacheMap.get(methodName);
                if (method == null) {
                    method = objClass.getMethod(methodName, args);
                    methodCacheMap.put(methodName, method);
                }
                return method.invoke(obj, agrsObj);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private static Object invoke(Object obj, Class<?> objClass, String methodName, Class<?>[] args, Object[] agrsObj) {
            try {
                return objClass.getMethod(methodName, args).invoke(obj, agrsObj);
            } catch (NoSuchMethodException e) {
                Slog.i(AIBrightnessHelper.TAG, "NoSuchMethodException method : " + methodName);
                return null;
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }
}
