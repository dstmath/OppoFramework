package com.qti.snapdragon.sdk.display;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.qti.snapdragon.sdk.display.IColorService.Stub;
import com.qti.snapdragon.sdk.display.MemoryColorConfig.MEMORY_COLOR_PARAMS;
import com.qti.snapdragon.sdk.display.MemoryColorConfig.MEMORY_COLOR_TYPE;
import com.qti.snapdragon.sdk.display.PictureAdjustmentConfig.PICTURE_ADJUSTMENT_PARAMS;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;

public class ColorManager {
    public static final long BITFLAG_COLOR_BALANCE = Long.parseLong("1", 2);
    public static final long BITFLAG_GLOBAL_PICTURE_ADJUSTMENT = Long.parseLong("10", 2);
    public static final long BITFLAG_MEMORY_COLOR_FOLIAGE = Long.parseLong("10000", 2);
    public static final long BITFLAG_MEMORY_COLOR_SKIN = Long.parseLong("100", 2);
    public static final long BITFLAG_MEMORY_COLOR_SKY = Long.parseLong("1000", 2);
    public static final int COLOR_BALANCE_WARMTH_LOWER_BOUND = -100;
    public static final int COLOR_BALANCE_WARMTH_UPPER_BOUND = 100;
    private static final int INIT_VALUE = -999;
    public static final int MODE_NAME_MAX_LENGTH = 32;
    private static final int NUM_DISPLAY_TYPES = 3;
    private static int PA_GLOBAL_CON = 8;
    private static int PA_GLOBAL_DESAT = 32;
    private static int PA_GLOBAL_DISABLE = 64;
    private static int PA_GLOBAL_HUE = 1;
    private static int PA_GLOBAL_SAT = 2;
    private static int PA_GLOBAL_SAT_THRESH = 16;
    private static int PA_GLOBAL_VAL = 4;
    private static final String REMOTE_SERVICE_NAME = IColorService.class.getName();
    public static final int RET_FAILURE = -999;
    public static final int RET_FEATURE_DISABLED = -905;
    public static final int RET_ILLEGAL_ARGUMENT = -904;
    public static final int RET_NOT_SUPPORTED = -901;
    public static final int RET_PERMISSION_DENIED = -903;
    public static final int RET_SUCCESS = 0;
    public static final int RET_VALUE_OUT_OF_RANGE = -902;
    private static final String SERVICE_INTENT_NAME = "com.qti.service.colorservice.ColorServiceApp";
    private static final String SERVICE_INTENT_PACKAGE = "com.qti.service.colorservice";
    private static final String SERVICE_PKG_NAME = "com.qti.service.colorservice";
    private static String TAG = "ColorManager";
    private static boolean VERBOSE_ENABLED = true;
    private static ColorManagerListener colorMgrListener = null;
    private static DisplayConn conn = new DisplayConn();
    private static boolean isConnecting = false;
    private static ColorManager[] myInstance = new ColorManager[NUM_DISPLAY_TYPES];
    private static final String permission = "com.qti.snapdragon.sdk.permission.DISPLAY_SETTINGS";
    private static IColorService service;
    private static Context serviceContext = null;
    private int displayId;
    private boolean isSystemApp = false;
    HashMap<String, Integer> memColorRanges = new HashMap();
    private Application myApplication;
    HashMap<String, Integer> paRanges = new HashMap();

    private enum ACTIVE_FEATURE_TYPE {
        FEATURE_ADAPTIVE_BACKLIGHT(0),
        FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT(1);
        
        private int value;

        private ACTIVE_FEATURE_TYPE(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    public enum ADAPTIVE_BACKLIGHT_QUALITY_LEVEL {
        LOW(0),
        MEDIUM(1),
        HIGH(2),
        AUTO(ColorManager.NUM_DISPLAY_TYPES);
        
        private int value;

        private ADAPTIVE_BACKLIGHT_QUALITY_LEVEL(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    private enum CONTROL_REQUEST_TYPE {
        ON(0),
        OFF(1);
        
        private int value;

        private CONTROL_REQUEST_TYPE(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    public interface ColorManagerListener {
        void onConnected();
    }

    public enum DCM_DISPLAY_TYPE {
        DISP_PRIMARY(0),
        DISP_EXTERNAL(1),
        DISP_WIFI(2);
        
        private int value;

        private DCM_DISPLAY_TYPE(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    public enum DCM_FEATURE {
        FEATURE_COLOR_BALANCE(0),
        FEATURE_COLOR_MODE_SELECTION(1),
        FEATURE_COLOR_MODE_MANAGEMENT(2),
        FEATURE_ADAPTIVE_BACKLIGHT(ColorManager.NUM_DISPLAY_TYPES),
        FEATURE_GLOBAL_PICTURE_ADJUSTMENT(4),
        FEATURE_MEMORY_COLOR_ADJUSTMENT(5),
        FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT(6);
        
        private int value;

        private DCM_FEATURE(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    private static class DisplayConn implements ServiceConnection {
        /* synthetic */ DisplayConn(DisplayConn -this0) {
            this();
        }

        private DisplayConn() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (ColorManager.colorMgrListener == null) {
                Log.e(ColorManager.TAG, "Listener is null");
                return;
            }
            ColorManager.service = Stub.asInterface(ServiceManager.getService(ColorManager.REMOTE_SERVICE_NAME));
            if (ColorManager.isConnecting) {
                Log.v(ColorManager.TAG, "Callback called");
                ColorManager.colorMgrListener.onConnected();
            } else {
                Log.v(ColorManager.TAG, "Callback not called");
            }
            ColorManager.isConnecting = false;
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    protected static class Log {
        protected Log() {
        }

        protected static void i(String tag, String text) {
            if (ColorManager.VERBOSE_ENABLED) {
                android.util.Log.i(tag, text);
            }
        }

        protected static void d(String tag, String text) {
            if (ColorManager.VERBOSE_ENABLED) {
                android.util.Log.d(tag, text);
            }
        }

        protected static void e(String tag, String text) {
            android.util.Log.e(tag, text);
        }

        protected static void v(String tag, String text) {
            if (ColorManager.VERBOSE_ENABLED) {
                android.util.Log.v(tag, text);
            }
        }
    }

    public enum MODE_TYPE {
        MODE_SYSTEM(0),
        MODE_USER(1),
        MODE_ALL(2);
        
        private int value;

        private MODE_TYPE(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    public static int connect(Context context, ColorManagerListener colorListener) {
        if (context == null || colorListener == null) {
            Log.e(TAG, "One of the parmeter passed is null");
            return RET_ILLEGAL_ARGUMENT;
        } else if (context.getApplicationContext().checkCallingOrSelfPermission(permission) != 0) {
            Log.e(TAG, "Required permission 'com.qti.snapdragon.sdk.permission.DISPLAY_SETTINGS' is missing");
            return -999;
        } else {
            colorMgrListener = colorListener;
            serviceContext = context;
            Intent serviceIntent;
            if (isConnecting) {
                Log.v(TAG, "Connection already in progress");
                return -999;
            } else if (isServiceRunning()) {
                Log.v(TAG, "Service running");
                serviceIntent = new Intent();
                serviceIntent.setClassName("com.qti.service.colorservice", SERVICE_INTENT_NAME);
                if (context.getApplicationContext().bindService(serviceIntent, conn, 1)) {
                    Log.v(TAG, "Running service bound");
                    colorMgrListener.onConnected();
                    return 0;
                }
                Log.e(TAG, "Bind failed even when service is running");
                return -999;
            } else {
                Log.v(TAG, "Service is not running");
                try {
                    isConnecting = true;
                    try {
                        context.getApplicationContext().unbindService(conn);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    serviceIntent = new Intent();
                    serviceIntent.setClassName("com.qti.service.colorservice", SERVICE_INTENT_NAME);
                    if (context.getApplicationContext().bindService(serviceIntent, conn, 1)) {
                        return 0;
                    }
                    Log.e(TAG, "Failed to connect to remote service");
                    isConnecting = false;
                    return -999;
                } catch (SecurityException e2) {
                    isConnecting = false;
                    e2.printStackTrace();
                    return -999;
                }
            }
        }
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private ColorManager(Application application, int displayId) {
        boolean z = false;
        service = Stub.asInterface(ServiceManager.getService(REMOTE_SERVICE_NAME));
        if (service == null) {
            throw new IllegalStateException("Failed to find IService by name [" + REMOTE_SERVICE_NAME + "]");
        }
        this.myApplication = application;
        if ((application.getApplicationInfo().flags & 129) != 0) {
            z = true;
        }
        this.isSystemApp = z;
        Log.v(TAG, "System app? " + this.isSystemApp);
        this.displayId = displayId;
    }

    public static ColorManager getInstance(Application application, Context context, DCM_DISPLAY_TYPE displayId) throws IllegalArgumentException {
        if (application == null || context == null) {
            Log.e(TAG, "Application or context passed is null");
            throw new IllegalArgumentException("Null passed for Application or context");
        } else if (displayId == null) {
            Log.e(TAG, "Display Id passed is null");
            throw new IllegalArgumentException("Display ID passed is null");
        } else if (application.checkCallingOrSelfPermission(permission) != 0) {
            Log.e(TAG, "Required permission 'com.qti.snapdragon.sdk.permission.DISPLAY_SETTINGS' is missing");
            return null;
        } else if (myInstance[displayId.getValue()] == null) {
            try {
                myInstance[displayId.getValue()] = new ColorManager(application, displayId.getValue());
                Log.v(TAG, "Instance created for display type " + displayId);
                return myInstance[displayId.getValue()];
            } catch (Exception e) {
                e.printStackTrace();
                myInstance[displayId.getValue()] = null;
                return null;
            }
        } else {
            Log.v(TAG, "Returning existing instance");
            return myInstance[displayId.getValue()];
        }
    }

    public boolean isFeatureSupported(DCM_FEATURE feature) throws IllegalArgumentException {
        if (this.myApplication == null) {
            return false;
        }
        if (feature == null) {
            Log.e(TAG, "Feature id passed is null");
            throw new IllegalArgumentException("Feature ID passed is null");
        }
        try {
            PackageInfo info = this.myApplication.getPackageManager().getPackageInfo("com.qti.service.colorservice", 128);
            try {
                return service.isFeatureSupported(this.displayId, feature.getValue());
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "Service isFeatureSupported crashed");
                return false;
            }
        } catch (NameNotFoundException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public int setColorBalance(int warmth) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_BALANCE)) {
                    return RET_NOT_SUPPORTED;
                }
                if (warmth < -100 || warmth > 100) {
                    Log.e(TAG, "Warmth given is outside the range (-100, 100)");
                    return RET_VALUE_OUT_OF_RANGE;
                }
                try {
                    Log.v(TAG, "Calling setColorBalance for display " + this.displayId);
                    int retVal = service.setColorBalance(this.displayId, warmth);
                    if (retVal == 0) {
                        Log.v(TAG, "SetColorBalance() worked");
                        return retVal;
                    }
                    Log.e(TAG, "Service setColorBalance failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service set color balance failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getColorBalance() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_BALANCE)) {
                    return RET_NOT_SUPPORTED;
                }
                try {
                    Log.v(TAG, "Calling getColorBalance for display " + this.displayId);
                    int retVal = service.getColorBalance(this.displayId);
                    if (retVal >= -100) {
                        return retVal;
                    }
                    Log.e(TAG, "Service getColorBalance failed with return value " + (retVal + 100));
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service get color balance failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getNumOfModes(MODE_TYPE type) throws IllegalArgumentException {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_SELECTION)) {
                    return RET_NOT_SUPPORTED;
                }
                if (type == null) {
                    Log.e(TAG, "Feature id passed is null");
                    throw new IllegalArgumentException("Type passed is null");
                }
                try {
                    Log.v(TAG, "Calling getNumOfModes for display " + this.displayId);
                    int retVal = service.getNumModes(this.displayId, type.getValue());
                    if (retVal >= 0) {
                        return retVal;
                    }
                    Log.e(TAG, "Service getNumModes failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service get num modes failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int[] getActiveMode() {
        int[] retArray = new int[]{-999, 0};
        if (myInstance[this.displayId] != null) {
            try {
                if (isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_SELECTION)) {
                    try {
                        Log.v(TAG, "Calling getActiveMode() for display " + this.displayId);
                        long[] retVal = new long[]{-999, 0};
                        retVal = service.getActiveMode(this.displayId);
                        if (retVal == null || retVal.length <= 0) {
                            Log.e(TAG, "getActive service returned null ");
                            retArray[0] = -999;
                            return retArray;
                        } else if (retVal[0] < 0) {
                            Log.e(TAG, "Service getActiveMode failed with return value " + retVal[0]);
                            retArray[0] = -999;
                            return retArray;
                        } else {
                            retArray[0] = (int) retVal[0];
                            retArray[1] = (int) retVal[1];
                            return retArray;
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service get active mode failed");
                        retArray[0] = -999;
                        return retArray;
                    }
                }
                retArray[0] = RET_NOT_SUPPORTED;
                return retArray;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                retArray[0] = RET_NOT_SUPPORTED;
                return retArray;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        retArray[0] = -999;
        return retArray;
    }

    public int setActiveMode(int modeId) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_SELECTION)) {
                    return RET_NOT_SUPPORTED;
                }
                if (modeId < 0) {
                    Log.e(TAG, "Mode ID provided is less than 0");
                    return RET_VALUE_OUT_OF_RANGE;
                }
                try {
                    Log.v(TAG, "Calling setActiveMode for display " + this.displayId);
                    int retVal = service.setActiveMode(this.displayId, modeId);
                    if (retVal == 0) {
                        Log.v(TAG, "SetActiveMode() worked");
                        return retVal;
                    }
                    Log.e(TAG, "Service setActiveMode failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service set active mode failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public ModeInfo[] getModes(MODE_TYPE type) throws IllegalArgumentException {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_SELECTION)) {
                    Log.e(TAG, "FEATURE_COLOR_MODE_SELECTION is not supported for display " + this.displayId);
                    return null;
                } else if (type == null) {
                    Log.e(TAG, "Mode Type passed is null");
                    throw new IllegalArgumentException("Type passed is null");
                } else {
                    try {
                        return service.getModes(this.displayId, type.getValue());
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service get modes failed");
                        e.printStackTrace();
                        return null;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return null;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return null;
    }

    public int deleteMode(int modeId) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_MANAGEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (!this.isSystemApp) {
                    Log.e(TAG, "You do not have permission to perform this operation");
                    return RET_PERMISSION_DENIED;
                } else if (modeId < 0) {
                    Log.e(TAG, "Mode ID provided is less than 0");
                    return RET_VALUE_OUT_OF_RANGE;
                } else {
                    try {
                        int retVal = service.deleteMode(this.displayId, modeId);
                        if (retVal == 0) {
                            Log.v(TAG, "deleteMode() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service deleteMode failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service set active mode failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int createNewMode(String modeName, EnumSet<DCM_FEATURE> featureList, int colorBalance) {
        if (myInstance[this.displayId] != null) {
            long FLAG = 0;
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_MANAGEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (!this.isSystemApp) {
                    Log.e(TAG, "You do not have permission to perform this operation");
                    return RET_PERMISSION_DENIED;
                } else if (modeName == null || modeName.equals("") || modeName.length() > 32) {
                    Log.e(TAG, "Mode name is missing or crossing max length");
                    return -999;
                } else if (featureList == null || featureList.isEmpty()) {
                    Log.e(TAG, "FeatureList missing");
                    return -999;
                } else {
                    if (featureList.contains(DCM_FEATURE.FEATURE_COLOR_BALANCE)) {
                        if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_BALANCE)) {
                            Log.e(TAG, "FEATURE_COLOR_BALANCE is not supported");
                            return RET_NOT_SUPPORTED;
                        } else if (colorBalance < -100 || colorBalance > 100) {
                            Log.e(TAG, "Color balance value passed is out of range");
                            return RET_VALUE_OUT_OF_RANGE;
                        } else {
                            FLAG = 0 | BITFLAG_COLOR_BALANCE;
                        }
                    }
                    if (FLAG == 0) {
                        Log.e(TAG, "FeatureList is incomplete. Colorbalance missing");
                        return -999;
                    }
                    try {
                        Log.v(TAG, "For createNewMode- " + modeName + " " + FLAG + " " + colorBalance);
                        int retVal = service.createNewMode(this.displayId, modeName, FLAG, colorBalance);
                        if (retVal >= 0) {
                            Log.v(TAG, "createNewMode() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service createNewMode failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service create new mode failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int modifyMode(int modeId, String newName, EnumSet<DCM_FEATURE> featureList, int colorBalance) {
        if (myInstance[this.displayId] != null) {
            long FLAG = 0;
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_MANAGEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (!this.isSystemApp) {
                    Log.e(TAG, "You do not have permission to perform this operation");
                    return RET_PERMISSION_DENIED;
                } else if (modeId < 0) {
                    Log.e(TAG, "Mode id passed is negative");
                    return -999;
                } else if (newName == null || newName.equals("") || newName.length() > 32) {
                    Log.e(TAG, "Mode name is missing or crossing max length");
                    return -999;
                } else if (featureList == null || featureList.isEmpty()) {
                    Log.e(TAG, "FeatureList missing");
                    return -999;
                } else {
                    if (featureList.contains(DCM_FEATURE.FEATURE_COLOR_BALANCE)) {
                        if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_BALANCE)) {
                            Log.e(TAG, "FEATURE_COLOR_BALANCE is not supported");
                            return RET_NOT_SUPPORTED;
                        } else if (colorBalance < -100 || colorBalance > 100) {
                            Log.e(TAG, "Color balance value passed is out of range");
                            return RET_VALUE_OUT_OF_RANGE;
                        } else {
                            FLAG = 0 | BITFLAG_COLOR_BALANCE;
                        }
                    }
                    if (FLAG == 0) {
                        Log.e(TAG, "FeatureList is incomplete. Colorbalance missing");
                        return -999;
                    }
                    try {
                        Log.v(TAG, "For modifyMode- " + modeId + " " + newName + " " + FLAG + " " + colorBalance);
                        int retVal = service.modifyModeAllFeatures(this.displayId, modeId, newName);
                        if (retVal == 0) {
                            Log.v(TAG, "modifyMode() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service modifyMode failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service modify mode failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getDefaultMode() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_SELECTION)) {
                    return RET_NOT_SUPPORTED;
                }
                try {
                    Log.v(TAG, "Calling getDefaultMode() for display " + this.displayId);
                    int retVal = service.getDefaultMode(this.displayId);
                    if (retVal >= 0) {
                        return retVal;
                    }
                    Log.e(TAG, "Service getDefaultMode failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service get default mode failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int setDefaultMode(int modeId) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_SELECTION)) {
                    return RET_NOT_SUPPORTED;
                }
                if (!this.isSystemApp) {
                    Log.e(TAG, "You do not have permission to perform this operation");
                    return RET_PERMISSION_DENIED;
                } else if (modeId < 0) {
                    Log.e(TAG, "Mode ID provided is less than 0");
                    return RET_VALUE_OUT_OF_RANGE;
                } else {
                    try {
                        Log.v(TAG, "Calling setDefaultMode for display " + this.displayId);
                        int retVal = service.setDefaultMode(this.displayId, modeId);
                        if (retVal == 0) {
                            Log.v(TAG, "SetDefaultMode() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service setDefaultMode failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service set default mode failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int createNewMode(String modeName) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_MANAGEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (!this.isSystemApp) {
                    Log.e(TAG, "You do not have permission to perform this operation");
                    return RET_PERMISSION_DENIED;
                } else if (modeName == null || modeName.equals("") || modeName.length() > 32) {
                    Log.e(TAG, "Mode name is missing or crossing max length");
                    return -999;
                } else {
                    try {
                        Log.v(TAG, "For createNewMode- " + modeName);
                        int retVal = service.createNewModeAllFeatures(this.displayId, modeName);
                        if (retVal >= 0) {
                            Log.v(TAG, "createNewMode() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service createNewMode failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service create new mode failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int modifyMode(int modeId, String newName) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_COLOR_MODE_MANAGEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (!this.isSystemApp) {
                    Log.e(TAG, "You do not have permission to perform this operation");
                    return RET_PERMISSION_DENIED;
                } else if (modeId < 0) {
                    Log.e(TAG, "Mode id passed is negative");
                    return -999;
                } else if (newName == null || newName.equals("") || newName.length() > 32) {
                    Log.e(TAG, "Mode name is missing or crossing max length");
                    return -999;
                } else {
                    try {
                        Log.v(TAG, "For modifyMode- " + modeId + " " + newName);
                        int retVal = service.modifyModeAllFeatures(this.displayId, modeId, newName);
                        if (retVal == 0) {
                            Log.v(TAG, "modifyMode() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service modifyMode failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service modify mode failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getMaxSunlightVisibilityStrength() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (isSunlightVisibilityEnabled()) {
                    try {
                        Log.v(TAG, "Calling getMaxSVI for display " + this.displayId);
                        return service.getRangeSunlightVisibilityStrength(this.displayId, 1);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service getMaxSVI failed");
                        return -999;
                    }
                }
                Log.e(TAG, "FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT is disabled");
                return RET_FEATURE_DISABLED;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getMinSunlightVisibilityStrength() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (isSunlightVisibilityEnabled()) {
                    try {
                        Log.v(TAG, "Calling getMinSVI for display " + this.displayId);
                        return service.getRangeSunlightVisibilityStrength(this.displayId, 0);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service getMinSVI failed");
                        return -999;
                    }
                }
                Log.e(TAG, "FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT is disabled");
                return RET_FEATURE_DISABLED;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int setSunlightVisibilityStrength(int strengthVal) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (!isSunlightVisibilityEnabled()) {
                    Log.e(TAG, "FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT is disabled");
                    return RET_FEATURE_DISABLED;
                } else if (strengthVal < getMinSunlightVisibilityStrength() || strengthVal > getMaxSunlightVisibilityStrength()) {
                    Log.e(TAG, "strengthVal given is outside the range (" + getMinSunlightVisibilityStrength() + "," + getMaxSunlightVisibilityStrength() + ")");
                    return RET_VALUE_OUT_OF_RANGE;
                } else {
                    try {
                        Log.v(TAG, "Calling setSVI for display " + this.displayId);
                        int retVal = service.setSunlightVisibilityStrength(this.displayId, strengthVal);
                        if (retVal == 0) {
                            Log.v(TAG, "setSunlightVisibilityStrength() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service setSunlightVisibilityStrength failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service setSunlightVisibilityStrength failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getSunlightVisibilityStrength() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (isSunlightVisibilityEnabled()) {
                    try {
                        Log.v(TAG, "Calling getSunlightVisibilityStrength for display " + this.displayId);
                        int retVal = service.getSunlightVisibilityStrength(this.displayId);
                        if (retVal >= getMinSunlightVisibilityStrength()) {
                            return retVal;
                        }
                        Log.e(TAG, "Service getSunlightVisibilityStrength failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service getSunlightVisibilityStrength failed");
                        return -999;
                    }
                }
                Log.e(TAG, "FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT is disabled");
                return RET_FEATURE_DISABLED;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public ADAPTIVE_BACKLIGHT_QUALITY_LEVEL getBacklightQualityLevel() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_ADAPTIVE_BACKLIGHT)) {
                    Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is not supported");
                    return null;
                } else if (isAdaptiveBacklightEnabled()) {
                    try {
                        Log.v(TAG, "Calling getBacklightQualityLevel for display " + this.displayId);
                        int retVal = service.getBacklightQualityLevel(this.displayId);
                        if (retVal < 0) {
                            Log.v(TAG, "getBacklightQualityLevel returned error value " + retVal);
                            return null;
                        }
                        try {
                            return ADAPTIVE_BACKLIGHT_QUALITY_LEVEL.values()[retVal];
                        } catch (Exception e) {
                            Log.e(TAG, "Level type mismatch");
                            return null;
                        }
                    } catch (RemoteException e2) {
                        Log.e(TAG, "Service getBacklightQualityLevel failed");
                        return null;
                    }
                } else {
                    Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is disabled");
                    return null;
                }
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is not supported");
                return null;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return null;
    }

    public int setBacklightQualityLevel(ADAPTIVE_BACKLIGHT_QUALITY_LEVEL level) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_ADAPTIVE_BACKLIGHT)) {
                    Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is not supported");
                    return RET_NOT_SUPPORTED;
                } else if (!isAdaptiveBacklightEnabled()) {
                    Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is disabled");
                    return RET_FEATURE_DISABLED;
                } else if (level == null) {
                    Log.e(TAG, "level passed is null");
                    return RET_ILLEGAL_ARGUMENT;
                } else {
                    try {
                        Log.v(TAG, "Calling setBacklightQualityLevel for display " + this.displayId);
                        int retVal = service.setBacklightQualityLevel(this.displayId, level.getValue());
                        if (retVal == 0) {
                            Log.v(TAG, "setBacklightQualityLevel() worked");
                            return retVal;
                        }
                        Log.e(TAG, "Service setBacklightQualityLevel failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service setBacklightQualityLevel failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is not supported");
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getAdaptiveBacklightScale() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_ADAPTIVE_BACKLIGHT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (isAdaptiveBacklightEnabled()) {
                    try {
                        Log.v(TAG, "Calling getAdaptiveBacklightScale for display " + this.displayId);
                        return service.getAdaptiveBacklightScale(this.displayId);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service getAdaptiveBacklightScale failed");
                        return -999;
                    }
                }
                Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is disabled");
                return RET_FEATURE_DISABLED;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public boolean isSunlightVisibilityEnabled() {
        if (myInstance[this.displayId] != null) {
            try {
                if (isFeatureSupported(DCM_FEATURE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT)) {
                    try {
                        Log.v(TAG, "Calling isSunlightVisibilityEnabled for display " + this.displayId);
                        if (service.isActiveFeatureOn(this.displayId, ACTIVE_FEATURE_TYPE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT.getValue()) != 1) {
                            return false;
                        }
                        Log.v(TAG, "isActiveFeatureOn returned retVal");
                        return true;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service isActiveFeatureOn failed");
                        return false;
                    }
                }
                Log.e(TAG, "FEATURE_SUNLIGHT_VISIBILITY_IMPROVEMENT is not supported");
                return false;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return false;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return false;
    }

    public boolean isAdaptiveBacklightEnabled() {
        if (myInstance[this.displayId] != null) {
            try {
                if (isFeatureSupported(DCM_FEATURE.FEATURE_ADAPTIVE_BACKLIGHT)) {
                    try {
                        Log.v(TAG, "Calling isAdaptiveBacklightEnabled for display " + this.displayId);
                        if (service.isActiveFeatureOn(this.displayId, ACTIVE_FEATURE_TYPE.FEATURE_ADAPTIVE_BACKLIGHT.getValue()) != 1) {
                            return false;
                        }
                        Log.v(TAG, "isActiveFeatureOn returned retVal");
                        return true;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service isActiveFeatureOn failed");
                        return false;
                    }
                }
                Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is not supported");
                return false;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return false;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return false;
    }

    public int setSunlightVisibilityEnabled(boolean enable) {
        if (myInstance[this.displayId] == null) {
            Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
            return -999;
        } else if (isFeatureSupported(DCM_FEATURE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT)) {
            try {
                CONTROL_REQUEST_TYPE request;
                Log.v(TAG, "Calling setActiveFeatureControl for display " + this.displayId);
                if (enable) {
                    request = CONTROL_REQUEST_TYPE.ON;
                } else {
                    request = CONTROL_REQUEST_TYPE.OFF;
                }
                int retVal = service.setActiveFeatureControl(this.displayId, ACTIVE_FEATURE_TYPE.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT.getValue(), request.getValue());
                if (retVal == 0) {
                    Log.v(TAG, "setActiveFeatureControl() worked");
                    return retVal;
                }
                Log.e(TAG, "Service setActiveFeatureControl failed with return value " + retVal);
                return -999;
            } catch (RemoteException e) {
                Log.e(TAG, "Service setActiveFeatureControl failed");
                return -999;
            }
        } else {
            Log.e(TAG, "FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT is not supported");
            return RET_NOT_SUPPORTED;
        }
    }

    public int setAdaptiveBacklightEnabled(boolean enable) {
        if (myInstance[this.displayId] == null) {
            Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
            return -999;
        } else if (isFeatureSupported(DCM_FEATURE.FEATURE_ADAPTIVE_BACKLIGHT)) {
            try {
                CONTROL_REQUEST_TYPE request;
                Log.v(TAG, "Calling setActiveFeatureControl for display " + this.displayId);
                if (enable) {
                    request = CONTROL_REQUEST_TYPE.ON;
                } else {
                    request = CONTROL_REQUEST_TYPE.OFF;
                }
                int retVal = service.setActiveFeatureControl(this.displayId, ACTIVE_FEATURE_TYPE.FEATURE_ADAPTIVE_BACKLIGHT.getValue(), request.getValue());
                if (retVal == 0) {
                    Log.v(TAG, "setActiveFeatureControl() worked");
                    return retVal;
                }
                Log.e(TAG, "Service setActiveFeatureControl failed with return value " + retVal);
                return -999;
            } catch (RemoteException e) {
                Log.e(TAG, "Service setActiveFeatureControl failed");
                return -999;
            }
        } else {
            Log.e(TAG, "FEATURE_ADAPTIVE_BACKLIGHT is not supported");
            return RET_NOT_SUPPORTED;
        }
    }

    public int getMaxLimitMemoryColor(MEMORY_COLOR_TYPE type, MEMORY_COLOR_PARAMS parameter) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_MEMORY_COLOR_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (type == null || parameter == null) {
                    Log.e(TAG, "Feature type or request passed is null");
                    return RET_ILLEGAL_ARGUMENT;
                }
                try {
                    String key = type + "" + parameter + "max";
                    Integer maxVal = (Integer) this.memColorRanges.get(key);
                    if (maxVal != null) {
                        return maxVal.intValue();
                    }
                    Log.v(TAG, "Calling getMaxLimitMemoryColor for display " + this.displayId);
                    int[] retVal = service.getRangeMemoryColorParameter(this.displayId, type.getValue());
                    if (retVal == null || retVal.length < 6) {
                        Log.e(TAG, "Service did not return proper parameter");
                        return -999;
                    }
                    int index = (parameter.getValue() * 2) + 1;
                    this.memColorRanges.put(key, Integer.valueOf(retVal[index]));
                    return retVal[index];
                } catch (RemoteException e) {
                    Log.e(TAG, "Service getMaxLimitMemoryColor failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getMinLimitMemoryColor(MEMORY_COLOR_TYPE type, MEMORY_COLOR_PARAMS parameter) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_MEMORY_COLOR_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (type == null || parameter == null) {
                    Log.e(TAG, "Feature type or request passed is null");
                    return RET_ILLEGAL_ARGUMENT;
                }
                try {
                    String key = type + "" + parameter + "min";
                    Integer minVal = (Integer) this.memColorRanges.get(key);
                    if (minVal != null) {
                        return minVal.intValue();
                    }
                    Log.v(TAG, "Calling getMinLimitMemoryColor for display " + this.displayId);
                    int[] retVal = service.getRangeMemoryColorParameter(this.displayId, type.getValue());
                    if (retVal == null || retVal.length < 6) {
                        Log.e(TAG, "Service did not return proper parameter");
                        return -999;
                    }
                    int index = parameter.getValue() * 2;
                    this.memColorRanges.put(key, Integer.valueOf(retVal[index]));
                    return retVal[index];
                } catch (RemoteException e) {
                    Log.e(TAG, "Service getMinLimitMemoryColor failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int setMemoryColorParams(MemoryColorConfig memConfig) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_MEMORY_COLOR_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (memConfig == null || memConfig.getMemoryColorType() == null) {
                    Log.e(TAG, "MemoryColorConfig instance or the Memory color type passed is null");
                    return RET_ILLEGAL_ARGUMENT;
                } else if (memConfig.getHue() < getMinLimitMemoryColor(memConfig.getMemoryColorType(), MEMORY_COLOR_PARAMS.HUE) || memConfig.getHue() > getMaxLimitMemoryColor(memConfig.getMemoryColorType(), MEMORY_COLOR_PARAMS.HUE)) {
                    Log.e(TAG, "Hue is out of range");
                    return RET_VALUE_OUT_OF_RANGE;
                } else if (memConfig.getSaturation() < getMinLimitMemoryColor(memConfig.getMemoryColorType(), MEMORY_COLOR_PARAMS.SATURATION) || memConfig.getSaturation() > getMaxLimitMemoryColor(memConfig.getMemoryColorType(), MEMORY_COLOR_PARAMS.SATURATION)) {
                    Log.e(TAG, "Saturation is out of range");
                    return RET_VALUE_OUT_OF_RANGE;
                } else if (memConfig.getIntensity() < getMinLimitMemoryColor(memConfig.getMemoryColorType(), MEMORY_COLOR_PARAMS.INTENSITY) || memConfig.getIntensity() > getMaxLimitMemoryColor(memConfig.getMemoryColorType(), MEMORY_COLOR_PARAMS.INTENSITY)) {
                    Log.e(TAG, "Intensity value is out of range");
                    return RET_VALUE_OUT_OF_RANGE;
                } else {
                    try {
                        Log.v(TAG, "Calling setMemoryColorParams for display " + this.displayId);
                        int retVal = service.setMemoryColorParameters(this.displayId, memConfig.getMemoryColorType().getValue(), memConfig.getHue(), memConfig.getSaturation(), memConfig.getIntensity());
                        if (retVal == 0) {
                            return retVal;
                        }
                        Log.e(TAG, "setMemorycolorParams failed with return value " + retVal);
                        return -999;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service setMemoryColorParams failed");
                        return -999;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public MemoryColorConfig getMemoryColorParams(MEMORY_COLOR_TYPE type) {
        boolean z = false;
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_MEMORY_COLOR_ADJUSTMENT)) {
                    Log.e(TAG, "FEATURE_MEMORY_COLOR_ADJUSTMENT is not supported");
                    return null;
                } else if (type == null) {
                    Log.e(TAG, "Mem color type passed is null");
                    return null;
                } else {
                    try {
                        Log.v(TAG, "Calling getMemoryColorParams for display " + this.displayId);
                        int[] retVal = service.getMemoryColorParameters(this.displayId, type.getValue());
                        if (retVal == null || retVal.length < 4) {
                            Log.e(TAG, "getMemorycolorParams failed ");
                            return null;
                        }
                        MemoryColorConfig memConfig = new MemoryColorConfig(type, retVal[0], retVal[1], retVal[2]);
                        if (retVal[NUM_DISPLAY_TYPES] != 0) {
                            z = true;
                        }
                        memConfig.isEnabled = z;
                        return memConfig;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Service getMemoryColorParams failed");
                        return null;
                    }
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                Log.e(TAG, "FEATURE_MEMORY_COLOR_ADJUSTMENT is not supported");
                return null;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return null;
    }

    public int disableMemoryColorConfig(MEMORY_COLOR_TYPE type) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_MEMORY_COLOR_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (type == null) {
                    Log.e(TAG, "Memory color type passed is null");
                    return RET_ILLEGAL_ARGUMENT;
                }
                try {
                    Log.v(TAG, "Calling disableMemoryColorConfig for display " + this.displayId);
                    int retVal = service.disableMemoryColorConfiguration(this.displayId, type.getValue());
                    if (retVal == 0) {
                        return retVal;
                    }
                    Log.e(TAG, "disableMemoryColorConfig failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service disableMemoryColorConfig failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getMaxLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS parameter) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_GLOBAL_PICTURE_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (parameter == null) {
                    Log.e(TAG, "Parameter passed is null");
                    return RET_ILLEGAL_ARGUMENT;
                }
                try {
                    String key = parameter + "max";
                    Integer maxVal = (Integer) this.paRanges.get(key);
                    if (maxVal != null) {
                        return maxVal.intValue();
                    }
                    Log.v(TAG, "Calling getMaxLimitPictureAdjustment for display " + this.displayId);
                    int[] retVal = service.getRangePAParameter(this.displayId);
                    if (retVal == null || retVal.length < 10) {
                        Log.e(TAG, "Service did not return proper parameter");
                        return -999;
                    }
                    int index = (parameter.getValue() * 2) + 1;
                    this.paRanges.put(key, Integer.valueOf(retVal[index]));
                    return retVal[index];
                } catch (RemoteException e) {
                    Log.e(TAG, "Service getMaxLimitPictureAdjustment failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int getMinLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS parameter) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_GLOBAL_PICTURE_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (parameter == null) {
                    Log.e(TAG, "Parameter passed is null");
                    return RET_ILLEGAL_ARGUMENT;
                }
                try {
                    String key = parameter + "min";
                    Integer minVal = (Integer) this.paRanges.get(key);
                    if (minVal != null) {
                        return minVal.intValue();
                    }
                    Log.v(TAG, "Calling getMinLimitPictureAdjustment for display " + this.displayId);
                    int[] retVal = service.getRangePAParameter(this.displayId);
                    if (retVal == null || retVal.length < 10) {
                        Log.e(TAG, "Service did not return proper parameter");
                        return -999;
                    }
                    int index = parameter.getValue() * 2;
                    this.paRanges.put(key, Integer.valueOf(retVal[index]));
                    return retVal[index];
                } catch (RemoteException e) {
                    Log.e(TAG, "Service getMaxLimitPictureAdjustment failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int setPictureAdjustmentParams(PictureAdjustmentConfig paConfig) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_GLOBAL_PICTURE_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (paConfig == null || paConfig.getParamFlags() == null) {
                    Log.e(TAG, "Parameter passed is null or the enumset of parameters is null");
                    return RET_ILLEGAL_ARGUMENT;
                }
                int flag = 0;
                EnumSet<PICTURE_ADJUSTMENT_PARAMS> dataSet = paConfig.getParamFlags();
                if (dataSet.contains(PICTURE_ADJUSTMENT_PARAMS.HUE)) {
                    flag = PA_GLOBAL_HUE | 0;
                    if (paConfig.getHue() < getMinLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.HUE) || paConfig.getHue() > getMaxLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.HUE)) {
                        Log.e(TAG, "Hue is out of range");
                        return RET_VALUE_OUT_OF_RANGE;
                    }
                }
                if (dataSet.contains(PICTURE_ADJUSTMENT_PARAMS.SATURATION)) {
                    flag |= PA_GLOBAL_SAT;
                    if (paConfig.getSaturation() < getMinLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.SATURATION) || paConfig.getSaturation() > getMaxLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.SATURATION)) {
                        Log.e(TAG, "Saturation is out of range");
                        return RET_VALUE_OUT_OF_RANGE;
                    }
                }
                if (dataSet.contains(PICTURE_ADJUSTMENT_PARAMS.INTENSITY)) {
                    flag |= PA_GLOBAL_VAL;
                    if (paConfig.getIntensity() < getMinLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.INTENSITY) || paConfig.getIntensity() > getMaxLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.INTENSITY)) {
                        Log.e(TAG, "Intensity is out of range");
                        return RET_VALUE_OUT_OF_RANGE;
                    }
                }
                if (dataSet.contains(PICTURE_ADJUSTMENT_PARAMS.CONTRAST)) {
                    flag |= PA_GLOBAL_CON;
                    if (paConfig.getContrast() < getMinLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.CONTRAST) || paConfig.getContrast() > getMaxLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.CONTRAST)) {
                        Log.e(TAG, "Contrast is out of range");
                        return RET_VALUE_OUT_OF_RANGE;
                    }
                }
                if (dataSet.contains(PICTURE_ADJUSTMENT_PARAMS.SATURATION_THRESHOLD)) {
                    flag |= PA_GLOBAL_SAT_THRESH;
                    if (paConfig.getSaturationThreshold() < getMinLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.SATURATION_THRESHOLD) || paConfig.getSaturationThreshold() > getMaxLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.SATURATION_THRESHOLD)) {
                        Log.e(TAG, "Saturation threshold is out of range");
                        return RET_VALUE_OUT_OF_RANGE;
                    }
                }
                if (flag == 0) {
                    Log.e(TAG, "The enumset passed is empty");
                    return RET_ILLEGAL_ARGUMENT;
                }
                try {
                    Log.v(TAG, "Calling setPAParameters for display " + this.displayId);
                    int retVal = service.setPAParameters(this.displayId, flag, paConfig.getHue(), paConfig.getSaturation(), paConfig.getIntensity(), paConfig.getContrast(), paConfig.getSaturationThreshold());
                    if (retVal == 0) {
                        return retVal;
                    }
                    Log.e(TAG, "setPAParameters failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service setPAParameters failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int setPictureAdjustmentSaturationThreshold(int thresholdVal) {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_GLOBAL_PICTURE_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                if (thresholdVal < getMinLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.SATURATION_THRESHOLD) || thresholdVal > getMaxLimitPictureAdjustment(PICTURE_ADJUSTMENT_PARAMS.SATURATION_THRESHOLD)) {
                    Log.e(TAG, "Saturation threshold is out of range");
                    return RET_VALUE_OUT_OF_RANGE;
                }
                try {
                    Log.v(TAG, "Calling setPictureAdjustmentSaturationThreshold for display " + this.displayId);
                    int retVal = service.setPAParameters(this.displayId, PA_GLOBAL_SAT_THRESH, 0, 0, 0, 0, thresholdVal);
                    if (retVal == 0) {
                        return retVal;
                    }
                    Log.e(TAG, "setPictureAdjustmentSaturationThreshold failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service setPictureAdjustmentSaturationThreshold failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int enablePictureAdjustmentDesaturation() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_GLOBAL_PICTURE_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                try {
                    Log.v(TAG, "Calling enablePictureAdjustmentDesaturation for display " + this.displayId);
                    int retVal = service.setPAParameters(this.displayId, PA_GLOBAL_DESAT, 0, 0, 0, 0, 0);
                    if (retVal == 0) {
                        return retVal;
                    }
                    Log.e(TAG, "enablePictureAdjustmentDesaturation failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service enablePictureAdjustmentDesaturation failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public int disablePictureAdjustmentConfig() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_GLOBAL_PICTURE_ADJUSTMENT)) {
                    return RET_NOT_SUPPORTED;
                }
                try {
                    Log.v(TAG, "Calling disablePictureAdjustmentConfig for display " + this.displayId);
                    int retVal = service.setPAParameters(this.displayId, PA_GLOBAL_DISABLE, 0, 0, 0, 0, 0);
                    if (retVal == 0) {
                        return retVal;
                    }
                    Log.e(TAG, "disablePictureAdjustmentConfig failed with return value " + retVal);
                    return -999;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service disablePictureAdjustmentConfig failed");
                    return -999;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return RET_NOT_SUPPORTED;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return -999;
    }

    public PictureAdjustmentConfig getPictureAdjustmentParams() {
        if (myInstance[this.displayId] != null) {
            try {
                if (!isFeatureSupported(DCM_FEATURE.FEATURE_GLOBAL_PICTURE_ADJUSTMENT)) {
                    return null;
                }
                try {
                    Log.v(TAG, "Calling getPictureAdjustmentParams for display " + this.displayId);
                    int[] retVal2 = service.getRangePAParameter(this.displayId);
                    if (retVal2 == null || retVal2.length < 10) {
                        Log.e(TAG, "getRanagePAParameter failed");
                        return null;
                    }
                    int[] retVal = service.getPAParameters(this.displayId);
                    if (retVal == null || retVal.length < 6) {
                        Log.e(TAG, "getPictureAdjustmentParams failed");
                        return null;
                    }
                    boolean deSatEnabled;
                    boolean paDisabled;
                    EnumSet<PICTURE_ADJUSTMENT_PARAMS> params = EnumSet.allOf(PICTURE_ADJUSTMENT_PARAMS.class);
                    if ((retVal[0] & PA_GLOBAL_DESAT) != 0) {
                        deSatEnabled = true;
                    } else {
                        deSatEnabled = false;
                    }
                    if ((retVal[0] & PA_GLOBAL_DISABLE) != 0) {
                        paDisabled = true;
                    } else {
                        paDisabled = false;
                    }
                    PictureAdjustmentConfig paConfig = new PictureAdjustmentConfig(params, retVal[1], retVal[2], retVal[NUM_DISPLAY_TYPES], retVal[4], retVal[5]);
                    paConfig.isDesaturation = deSatEnabled;
                    paConfig.isGlobalPADisabled = paDisabled;
                    return paConfig;
                } catch (RemoteException e) {
                    Log.e(TAG, "Service getPictureAdjustmentParams failed");
                    return null;
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return null;
            }
        }
        Log.e(TAG, "Instance for the display type " + this.displayId + " doesnt exist");
        return null;
    }

    public void release() {
        if (myInstance[this.displayId] != null) {
            myInstance[this.displayId] = null;
            this.myApplication = null;
        }
    }

    private int checkModeValidity(ModeInfo pf) {
        if (pf == null) {
            Log.e(TAG, "Mode data missing");
            return -999;
        } else if (pf.getId() >= 0) {
            return 0;
        } else {
            Log.e(TAG, "ID out of range");
            return RET_VALUE_OUT_OF_RANGE;
        }
    }

    private static boolean isServiceRunning() {
        boolean serviceFound = false;
        InputStream inputStream = null;
        try {
            String[] args = new String[NUM_DISPLAY_TYPES];
            args[0] = "/system/bin/sh";
            args[1] = "-c";
            args[2] = "ps";
            inputStream = new ProcessBuilder(args).start().getInputStream();
            byte[] re = new byte[1024];
            while (inputStream.read(re) != -1) {
                if (new String(re).contains("colorservice")) {
                    serviceFound = true;
                    break;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    android.util.Log.d(TAG, "Harmless exception on close!");
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex2) {
            ex2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex22) {
                    android.util.Log.d(TAG, "Harmless exception on close!");
                    ex22.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex222) {
                    android.util.Log.d(TAG, "Harmless exception on close!");
                    ex222.printStackTrace();
                }
            }
        }
        return serviceFound;
    }
}
