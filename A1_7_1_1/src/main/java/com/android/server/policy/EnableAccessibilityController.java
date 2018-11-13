package com.android.server.policy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.WindowManagerInternal;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.IAccessibilityManager;
import android.view.accessibility.IAccessibilityManager.Stub;
import com.android.server.LocalServices;
import com.android.server.lights.OppoLightsService.ButtonLight;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EnableAccessibilityController {
    private static final int ENABLE_ACCESSIBILITY_DELAY_MILLIS = 6000;
    public static final int MESSAGE_ENABLE_ACCESSIBILITY = 3;
    public static final int MESSAGE_SPEAK_ENABLE_CANCELED = 2;
    public static final int MESSAGE_SPEAK_WARNING = 1;
    private static final int SPEAK_WARNING_DELAY_MILLIS = 2000;
    private static final String TAG = "EnableAccessibilityController";
    private final IAccessibilityManager mAccessibilityManager = Stub.asInterface(ServiceManager.getService("accessibility"));
    private boolean mCanceled;
    private final Context mContext;
    private boolean mDestroyed;
    private float mFirstPointerDownX;
    private float mFirstPointerDownY;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    EnableAccessibilityController.this.mTts.speak(EnableAccessibilityController.this.mContext.getString(17040698), 0, null);
                    return;
                case 2:
                    EnableAccessibilityController.this.mTts.speak(EnableAccessibilityController.this.mContext.getString(17040700), 0, null);
                    return;
                case 3:
                    EnableAccessibilityController.this.enableAccessibility();
                    EnableAccessibilityController.this.mTone.play();
                    EnableAccessibilityController.this.mTts.speak(EnableAccessibilityController.this.mContext.getString(17040699), 0, null);
                    return;
                default:
                    return;
            }
        }
    };
    private final Runnable mOnAccessibilityEnabledCallback;
    private float mSecondPointerDownX;
    private float mSecondPointerDownY;
    private final Ringtone mTone;
    private final float mTouchSlop;
    private final TextToSpeech mTts;
    private final UserManager mUserManager;

    public EnableAccessibilityController(Context context, Runnable onAccessibilityEnabledCallback) {
        this.mContext = context;
        this.mOnAccessibilityEnabledCallback = onAccessibilityEnabledCallback;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mTts = new TextToSpeech(context, new OnInitListener() {
            public void onInit(int status) {
                if (EnableAccessibilityController.this.mDestroyed) {
                    EnableAccessibilityController.this.mTts.shutdown();
                }
            }
        });
        this.mTone = RingtoneManager.getRingtone(context, System.DEFAULT_NOTIFICATION_URI);
        this.mTone.setStreamType(3);
        this.mTouchSlop = (float) context.getResources().getDimensionPixelSize(17105039);
    }

    public static boolean canEnableAccessibilityViaGesture(Context context) {
        boolean z = false;
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(context);
        if (accessibilityManager.isEnabled() && !accessibilityManager.getEnabledAccessibilityServiceList(1).isEmpty()) {
            return false;
        }
        if (Global.getInt(context.getContentResolver(), "enable_accessibility_global_gesture_enabled", 0) == 1 && !getInstalledSpeakingAccessibilityServices(context).isEmpty()) {
            z = true;
        }
        return z;
    }

    public static List<AccessibilityServiceInfo> getInstalledSpeakingAccessibilityServices(Context context) {
        List<AccessibilityServiceInfo> services = new ArrayList();
        services.addAll(AccessibilityManager.getInstance(context).getInstalledAccessibilityServiceList());
        Iterator<AccessibilityServiceInfo> iterator = services.iterator();
        while (iterator.hasNext()) {
            if ((((AccessibilityServiceInfo) iterator.next()).feedbackType & 1) == 0) {
                iterator.remove();
            }
        }
        return services;
    }

    public void onDestroy() {
        this.mDestroyed = true;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != 5 || event.getPointerCount() != 2) {
            return false;
        }
        this.mFirstPointerDownX = event.getX(0);
        this.mFirstPointerDownY = event.getY(0);
        this.mSecondPointerDownX = event.getX(1);
        this.mSecondPointerDownY = event.getY(1);
        this.mHandler.sendEmptyMessageDelayed(1, 2000);
        this.mHandler.sendEmptyMessageDelayed(3, ButtonLight.TIMEOUT_DEFAULT);
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        if (this.mCanceled) {
            if (action == 1) {
                this.mCanceled = false;
            }
            return true;
        }
        switch (action) {
            case 2:
                if (Math.abs(MathUtils.dist(event.getX(0), event.getY(0), this.mFirstPointerDownX, this.mFirstPointerDownY)) > this.mTouchSlop) {
                    cancel();
                }
                if (Math.abs(MathUtils.dist(event.getX(1), event.getY(1), this.mSecondPointerDownX, this.mSecondPointerDownY)) > this.mTouchSlop) {
                    cancel();
                    break;
                }
                break;
            case 3:
            case 6:
                cancel();
                break;
            case 5:
                if (pointerCount > 2) {
                    cancel();
                    break;
                }
                break;
        }
        return true;
    }

    private void cancel() {
        this.mCanceled = true;
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        } else if (this.mHandler.hasMessages(3)) {
            this.mHandler.sendEmptyMessage(2);
        }
        this.mHandler.removeMessages(3);
    }

    private void enableAccessibility() {
        if (enableAccessibility(this.mContext)) {
            this.mOnAccessibilityEnabledCallback.run();
        }
    }

    public static boolean enableAccessibility(Context context) {
        IAccessibilityManager accessibilityManager = Stub.asInterface(ServiceManager.getService("accessibility"));
        WindowManagerInternal windowManager = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        UserManager userManager = (UserManager) context.getSystemService("user");
        ComponentName componentName = getInstalledSpeakingAccessibilityServiceComponent(context);
        if (componentName == null) {
            return false;
        }
        boolean keyguardLocked = windowManager.isKeyguardLocked();
        boolean hasMoreThanOneUser = userManager.getUsers().size() > 1;
        if (!keyguardLocked || !hasMoreThanOneUser) {
            accessibilityManager.enableAccessibilityService(componentName, ActivityManager.getCurrentUser());
        } else if (keyguardLocked) {
            try {
                accessibilityManager.temporaryEnableAccessibilityStateUntilKeyguardRemoved(componentName, true);
            } catch (RemoteException e) {
                Log.e(TAG, "cannot enable accessibilty: " + e);
            }
        }
        return true;
    }

    public static void disableAccessibility(Context context) {
        IAccessibilityManager accessibilityManager = Stub.asInterface(ServiceManager.getService("accessibility"));
        ComponentName componentName = getInstalledSpeakingAccessibilityServiceComponent(context);
        if (componentName != null) {
            try {
                accessibilityManager.disableAccessibilityService(componentName, ActivityManager.getCurrentUser());
            } catch (RemoteException e) {
                Log.e(TAG, "cannot disable accessibility " + e);
            }
        }
    }

    public static boolean isAccessibilityEnabled(Context context) {
        List enabledServices = ((AccessibilityManager) context.getSystemService(AccessibilityManager.class)).getEnabledAccessibilityServiceList(1);
        if (enabledServices == null || enabledServices.isEmpty()) {
            return false;
        }
        return true;
    }

    public static ComponentName getInstalledSpeakingAccessibilityServiceComponent(Context context) {
        List<AccessibilityServiceInfo> services = getInstalledSpeakingAccessibilityServices(context);
        if (services.isEmpty()) {
            return null;
        }
        ServiceInfo serviceInfo = ((AccessibilityServiceInfo) services.get(0)).getResolveInfo().serviceInfo;
        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
    }
}
