package com.aiunit.aon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.aiunit.aon.utils.IAONEventListener;
import com.aiunit.aon.utils.IAONService;
import com.aiunit.aon.utils.core.FaceInfo;
import java.util.ArrayList;
import java.util.Arrays;

public class AonSmartRotation {
    private static final int AON_FACE_DIRECTION_ENGINE = 393218;
    private static final int AON_LOW_LIGHT = 10000;
    private static final String AON_PKG_NAME = "com.aiunit.aon";
    private static final String AON_SERVICE = "com.aiunit.aon.AONservice";
    public static int AonSmartRotationConnectionCount = 0;
    private static final String CLS_NAME = "com.aiunit.aon.AONService";
    private static boolean DEBUG_CONFIGURATION = false;
    public static final int DEFAULT_FACE_ANGLE = -2;
    private static final int ERR_REMOTE_EXCEPTION = 4099;
    private static int FACE_EVENT_TIME_GAP = 400;
    private static final float FACE_ORIENTATION_0 = 0.0f;
    private static final float FACE_ORIENTATION_1 = 1.0f;
    private static final float FACE_ORIENTATION_2 = 2.0f;
    private static final float FACE_ORIENTATION_3 = 3.0f;
    private static final int MSG_INIT_SUCCESS = 10001;
    private static final int MSG_REGISTER = 10003;
    private static final int MSG_START_CAMERA = 10005;
    private static final int MSG_STOP_CAMERA = 10006;
    private static final int MSG_TESTALL = 10002;
    private static final int MSG_UNREGISTER = 10004;
    private static final long PRE_OPEN_STATUS_TIME_GAP = 800;
    private static final String SMART_ROTATION_ENABLE_SWITCH = "persist.sys.oppo.smartrotation";
    private static final String SMART_ROTATION_TAG = "SmartRotationDebug";
    private static final String SMART_ROTATION_TESTEVENT = "com.aon.smartrotation.testevent";
    private static final String SMART_ROTATION_TESTMODE_STATUS = "sys.oppo.smartrotation.testmode";
    private static final String SMART_ROTATION_TIME_GAP = "persist.sys.oppo.smartrotation.timegap";
    private static final String WMS_LISTENER = "com.android.server.policy.WindowOrientationListener$OrientationSensorJudge";
    private static IAONEventListener aonlistener = new IAONEventListener.Stub() {
        /* class com.aiunit.aon.AonSmartRotation.AnonymousClass3 */

        @Override // com.aiunit.aon.utils.IAONEventListener
        public void onEvent(int eventType, int event) throws RemoteException {
            long unused = AonSmartRotation.receiveEventTime = System.currentTimeMillis();
            if (event == 10000) {
                boolean unused2 = AonSmartRotation.isLowLight = true;
                AonSmartRotation.printDetailLog("AON_LOW_LIGHT, keep waiting for second value.");
                return;
            }
            if (AonSmartRotation.isLowLight) {
                boolean unused3 = AonSmartRotation.isLowLight = false;
            }
            AonSmartRotation.printDetailLog("nash_debug, AON face angle event = " + event + ", receiveEventTime: " + AonSmartRotation.receiveEventTime);
            if (AonSmartRotation.isWaitingFaceAngle) {
                if (AonSmartRotation.launchCameraAhead) {
                    boolean unused4 = AonSmartRotation.launchCameraAhead = false;
                }
                AonSmartRotation.faceAngle = event;
                if (AonSmartRotation.isErrorCodeNeedStopCamera(AonSmartRotation.faceAngle)) {
                    Log.w(AonSmartRotation.SMART_ROTATION_TAG, "Receive AON error code:" + AonSmartRotation.faceAngle + " here, reset it as default value.");
                    AonSmartRotation.faceAngle = -2;
                }
                AonSmartRotation.releaseCamera();
                Log.d(AonSmartRotation.SMART_ROTATION_TAG, "nash_debug, get the faceAngle:" + AonSmartRotation.faceAngle + ", ready to compare with hardware value.");
            } else if (AonSmartRotation.launchCameraAhead) {
                long unused5 = AonSmartRotation.preOpenEventFetchTime = System.currentTimeMillis();
                if (AonSmartRotation.preOpenEventFetchTime < AonSmartRotation.preOpenCameraTime) {
                    Log.w(AonSmartRotation.SMART_ROTATION_TAG, "Two continuous pre-status here, it is dangerous, notice this.");
                } else if (AonSmartRotation.preOpenEventFetchTime - AonSmartRotation.preOpenCameraTime >= AonSmartRotation.PRE_OPEN_STATUS_TIME_GAP) {
                    AonSmartRotation.stuckInPreStatusCount++;
                    Log.w(AonSmartRotation.SMART_ROTATION_TAG, "We may stuck in a pre-status, release the AON camera to save power. The counts of stuck pre-status is " + AonSmartRotation.stuckInPreStatusCount);
                    AonSmartRotation.faceAngle = -2;
                    AonSmartRotation.releaseCamera();
                } else {
                    AonSmartRotation.printDetailLog("nash_debug, Open camera ahead, early data here, we may use next dace data, keep camera on.");
                }
            } else {
                AonSmartRotation.faceAngle = -2;
                AonSmartRotation.printDetailLog("nash_debug, We are not waiting, reset faceAngle to default.");
                AonSmartRotation.releaseCamera();
            }
            if (AonSmartRotation.preOpenCameraTime != 0) {
                if (AonSmartRotation.preOpenCameraTime >= AonSmartRotation.receiveEventTime) {
                    AonSmartRotation.printDetailLog("nash_debug, Time Status, We may receive more than one prestatus, ignore this time.");
                } else if (AonSmartRotation.normalStatusTime == 0) {
                    AonSmartRotation.printDetailLog("nash_debug, Time Status, Never receive a normal status, ignore this time.");
                } else {
                    long firstPicTime = AonSmartRotation.receiveEventTime - AonSmartRotation.preOpenCameraTime;
                    long currentTimeGap = AonSmartRotation.receiveEventTime - AonSmartRotation.normalStatusTime;
                    if (currentTimeGap < 0) {
                        AonSmartRotation.printDetailLog("nash_debug, Time Status, AON event faster than normal status, we see currentTimeGap as 0, firstPicTime is " + firstPicTime + ".");
                        return;
                    }
                    AonSmartRotation.printDetailLog("nash_debug, Time Status, currentTimeGap is " + currentTimeGap + ", firstPicTime is " + firstPicTime + ".");
                }
            } else if (AonSmartRotation.normalStatusTime == 0) {
                AonSmartRotation.printDetailLog("nash_debug, Time Status, Never receive a normal status, ignore this time.");
            } else {
                long currentTimeGap2 = AonSmartRotation.receiveEventTime - AonSmartRotation.normalStatusTime;
                if (currentTimeGap2 < 0) {
                    AonSmartRotation.printDetailLog("nash_debug, Time Status, AON event faster than normal status, we see currentTimeGap as 0.");
                    return;
                }
                AonSmartRotation.printDetailLog("nash_debug, Time Status, currentTimeGap is " + currentTimeGap2 + ".");
            }
        }

        @Override // com.aiunit.aon.utils.IAONEventListener
        public void onEventParam(int eventType, int event, FaceInfo faceInfo) throws RemoteException {
        }
    };
    private static boolean canUseSmartRotation = false;
    private static IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        /* class com.aiunit.aon.AonSmartRotation.AnonymousClass4 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.w(AonSmartRotation.SMART_ROTATION_TAG, "AON binderDied");
            AonSmartRotation.mAONService.asBinder().unlinkToDeath(AonSmartRotation.deathRecipient, 0);
            IAONService unused = AonSmartRotation.mAONService = null;
        }
    };
    public static int faceAngle = -2;
    public static boolean isCameraOn = false;
    private static boolean isLowLight = false;
    private static boolean isTestMode = false;
    private static boolean isWaitingFaceAngle = false;
    private static boolean launchCameraAhead = false;
    private static IAONService mAONService = null;
    private static Handler mSmartRotationHandler;
    private static HandlerThread mSmartRotationHandlerThread;
    private static Looper mSmartRotationLooper;
    private static long normalStatusTime = 0;
    private static float orientationResult = 0.0f;
    private static long preOpenCameraTime = 0;
    private static long preOpenEventFetchTime = 0;
    private static long receiveEventTime = 0;
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        /* class com.aiunit.aon.AonSmartRotation.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(AonSmartRotation.SMART_ROTATION_TAG, "AON onServiceConnected");
            IAONService unused = AonSmartRotation.mAONService = IAONService.Stub.asInterface(iBinder);
            try {
                if (AonSmartRotation.mAONService != null) {
                    AonSmartRotation.printDetailLog("nash_debug, fetch AON Service successed.");
                    AonSmartRotation.mAONService.asBinder().linkToDeath(AonSmartRotation.deathRecipient, 0);
                    if (AonSmartRotation.mSmartRotationHandlerThread == null) {
                        AonSmartRotation.initAONSubThread();
                    }
                    AonSmartRotation.askForConnection();
                    return;
                }
                Log.w(AonSmartRotation.SMART_ROTATION_TAG, "fetch AON Service failed.");
            } catch (Exception e) {
                Log.e(AonSmartRotation.SMART_ROTATION_TAG, "AON smart rotation on Service connected error e = " + e.toString());
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            IAONService unused = AonSmartRotation.mAONService = null;
            AonSmartRotation.printDetailLog("AON onServiceDisconnected");
        }
    };
    public static ArrayList<Integer> stopCameraErrorCodes = new ArrayList<>(Arrays.asList(20513, 24577, 24578, 24579, 24580, 24581, 24582, 24583, 24624, 24625));
    public static int stuckInPreStatusCount = 0;
    public static int testFaceAngle = -2;
    public static float testOrientationHardwareValue = -1.0f;
    public static float testOrientationResult = 0.0f;
    private int autoRotationSwitchValue = 0;
    private final Context mContext;
    private BroadcastReceiver smartRotationEnableReceiver = new BroadcastReceiver() {
        /* class com.aiunit.aon.AonSmartRotation.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AonSmartRotation.SMART_ROTATION_TESTEVENT)) {
                AonSmartRotation.printDetailLog("TestMode, Smart_Rotation_Testmode receive test event.");
                if (AonSmartRotation.isTestMode) {
                    try {
                        AonSmartRotation.testFaceAngle = Integer.parseInt(intent.getStringExtra("faceAngle"));
                        AonSmartRotation.testOrientationHardwareValue = Float.parseFloat(intent.getStringExtra("hardwareValue"));
                        AonSmartRotation.printDetailLog("TestMode, testFaceAngle is " + AonSmartRotation.testFaceAngle + " and  testOrientationHardwareValue is " + AonSmartRotation.testOrientationHardwareValue);
                    } catch (Exception e) {
                        Log.e(AonSmartRotation.SMART_ROTATION_TAG, "TestMode, Get test event failed.");
                    }
                } else {
                    AonSmartRotation.printDetailLog("SmartRotationDebug, Test mode is not enable, ignore this event.");
                }
            }
        }
    };
    IntentFilter smartRotationFilter;
    private int smartRotationSwitchValue = 0;
    private int switchValue = 0;

    public AonSmartRotation(Context context) {
        this.mContext = context;
        try {
            FACE_EVENT_TIME_GAP = Integer.parseInt(SystemProperties.get(SMART_ROTATION_TIME_GAP, "400"));
            if (FACE_EVENT_TIME_GAP > 1000) {
                FACE_EVENT_TIME_GAP = 400;
                Log.w(SMART_ROTATION_TAG, "FACE_EVENT_TIME_GAP can not be too long, reset it as 400ms.");
            }
            DEBUG_CONFIGURATION = SystemProperties.get("persist.sys.assert.panic", "false").equals("true");
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "Fetch time gap failed, use default value.");
        }
        Log.d(SMART_ROTATION_TAG, "Init AonSmartRotation successed.");
    }

    public int getStatus() {
        try {
            this.autoRotationSwitchValue = Settings.System.getIntForUser(this.mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0, -2);
            this.smartRotationSwitchValue = Settings.System.getIntForUser(this.mContext.getContentResolver(), "device_orientation_intelligent_auto_rotation", 0, -2);
            if (this.autoRotationSwitchValue == 1 && this.smartRotationSwitchValue == 1) {
                this.switchValue = 1;
            } else {
                this.switchValue = 0;
            }
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "getStatus failed.");
        }
        return this.switchValue;
    }

    public static void printDetailLog(String logInfo) {
        if (logInfo != null && DEBUG_CONFIGURATION) {
            Log.d(SMART_ROTATION_TAG, logInfo);
        }
    }

    public void registerSmartRotationReceiver() {
        if (this.smartRotationFilter == null) {
            this.smartRotationFilter = new IntentFilter(SMART_ROTATION_TESTEVENT);
        }
        this.mContext.registerReceiver(this.smartRotationEnableReceiver, this.smartRotationFilter);
    }

    public void unRegisterSmartRotationReceiver() {
        BroadcastReceiver broadcastReceiver = this.smartRotationEnableReceiver;
        if (broadcastReceiver != null) {
            try {
                this.mContext.unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
            }
        }
    }

    public void updateTimeGap(int newTimeGap) {
        if (newTimeGap >= 0 && newTimeGap <= 1000) {
            FACE_EVENT_TIME_GAP = newTimeGap;
            printDetailLog("Update Smart Rotation FACE_EVENT_TIME_GAP as " + FACE_EVENT_TIME_GAP);
        }
    }

    public void updateDebugConfiguration(boolean debugConfiguration) {
        DEBUG_CONFIGURATION = debugConfiguration;
        printDetailLog("Update Smart Rotation DEBUG_CONFIGURATION as " + DEBUG_CONFIGURATION);
    }

    public float createSmartRotationConnection() {
        try {
            printDetailLog("Register SmartRotation Connection.");
            FACE_EVENT_TIME_GAP = Integer.parseInt(SystemProperties.get(SMART_ROTATION_TIME_GAP, "400"));
            if (FACE_EVENT_TIME_GAP > 1000) {
                FACE_EVENT_TIME_GAP = 400;
                Log.w(SMART_ROTATION_TAG, "FACE_EVENT_TIME_GAP can not be too long, reset it as 400ms.");
            }
            DEBUG_CONFIGURATION = SystemProperties.get("persist.sys.assert.panic", "false").equals("true");
            AonSmartRotationConnectionCount++;
            if (mSmartRotationHandlerThread == null) {
                initAONSubThread();
                printDetailLog("First AonSmartRotation Connection init.");
            } else {
                printDetailLog("We have " + AonSmartRotationConnectionCount + " AonSmartRotation Connections here.");
            }
            if (mAONService == null) {
                bindAONService();
            } else {
                printDetailLog("Already have AONService here, do not bind it again.");
            }
            if (!isTestMode) {
                isTestMode = SystemProperties.get(SMART_ROTATION_TESTMODE_STATUS, "false").equals("true");
                if (isTestMode) {
                    registerSmartRotationReceiver();
                    Log.w(SMART_ROTATION_TAG, "TestMode, AON smart rotation testmode on.");
                }
            }
            printDetailLog("Finish Register SmartRotation Connection.");
            return 0.0f;
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "AON smart rotation error in createSmartRotationConnection(), e = " + e.toString());
            return 0.0f;
        }
    }

    public float destroySmartRotationConnection() {
        try {
            printDetailLog("Unregister SmartRotation Connection.");
            AonSmartRotationConnectionCount--;
            if (AonSmartRotationConnectionCount == 0) {
                if (isTestMode) {
                    isTestMode = false;
                    unRegisterSmartRotationReceiver();
                    Log.w(SMART_ROTATION_TAG, "TestMode, AON smart rotation testmode off.");
                }
                dropAONSubThread();
                unbindAONService();
            } else {
                printDetailLog("We have " + AonSmartRotationConnectionCount + " AonSmartRotation Connections left.");
            }
            printDetailLog("Finish Unregister SmartRotation Connection.");
            return 0.0f;
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "AON smart rotation error in destroySmartRotationConnection(), e = " + e.toString());
            return 0.0f;
        }
    }

    public float makeDecisionBySmartRotation(float[] values) {
        try {
            if (mAONService == null) {
                Log.d(SMART_ROTATION_TAG, "nash_debug, mAONService is not init, dispatch orientation directlly. values[0] = " + values[0]);
            } else if (isTestMode) {
                return testModeData(values);
            } else {
                if (((int) values[0]) % 5 != 4) {
                    if (((int) values[0]) % 5 != 0) {
                        if (!(((double) values[0]) == 6.0d || ((double) values[0]) == 7.0d)) {
                            if (((double) values[0]) != 8.0d) {
                                normalStatusTime = System.currentTimeMillis();
                                printDetailLog("nash_debug, normalStatusTime: " + normalStatusTime + ", orientation: " + values[0]);
                                if (!launchCameraAhead) {
                                    printDetailLog("nash_debug, begin launch camera.");
                                    launchCamera();
                                    printDetailLog("nash_debug, finish launch camera, begin wait face event.");
                                }
                                isWaitingFaceAngle = true;
                                try {
                                    Thread.sleep((long) FACE_EVENT_TIME_GAP);
                                } catch (InterruptedException e) {
                                    Log.e(SMART_ROTATION_TAG, "sleep FACE_EVENT_TIME_GAP error.");
                                }
                                if (isLowLight) {
                                    printDetailLog("AON_LOW_LIGHT, wait another 100ms for low light situation.");
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e2) {
                                        Log.e(SMART_ROTATION_TAG, "sleep for AON_LOW_LIGHT error.");
                                    }
                                    isLowLight = false;
                                }
                                isWaitingFaceAngle = false;
                                Log.d(SMART_ROTATION_TAG, "nash_debug, time is up, faceAngle is " + faceAngle);
                                if (isCameraOn) {
                                    releaseCamera();
                                    Log.w(SMART_ROTATION_TAG, "Normal Orientation: " + values[0] + " here, and we can not receive a face angle, stop camera for next data.");
                                }
                                if (faceAngle >= 0 && faceAngle <= 360) {
                                    orientationResult = values[0];
                                    if ((faceAngle >= 0 && faceAngle < 60) || (faceAngle >= 300 && faceAngle <= 360)) {
                                        Log.d(SMART_ROTATION_TAG, "nash_debug, Hardware orientation: " + values[0] + " faceAngle: " + faceAngle + ", may upload FACE_ORIENTATION_0");
                                        orientationResult = 0.0f;
                                    }
                                    faceAngle = -2;
                                    if (launchCameraAhead) {
                                        launchCameraAhead = false;
                                    }
                                    return orientationResult;
                                } else if (faceAngle == -1) {
                                    printDetailLog("nash_debug, we lost a face, maybe there is nobody here.");
                                } else if (faceAngle == -2) {
                                    printDetailLog("nash_debug, Default face angle, use hardware data.");
                                } else {
                                    Log.w(SMART_ROTATION_TAG, "Illegal faceAngle: " + faceAngle);
                                }
                            }
                        }
                        if (!launchCameraAhead) {
                            printDetailLog("nash_debug, pre status for orientation, begin launch camera.");
                            preOpenCameraTime = System.currentTimeMillis();
                            launchCamera();
                            launchCameraAhead = true;
                        }
                        return values[0];
                    }
                }
                printDetailLog("nash_debug, Ignore lay down, 0 and their pre status.");
                return values[0];
            }
            if (launchCameraAhead) {
                launchCameraAhead = false;
            }
            faceAngle = -2;
        } catch (Exception e3) {
            Log.e(SMART_ROTATION_TAG, "nash_debug, smart rotation dispatch event error e = " + e3.toString());
        }
        return values[0];
    }

    public static float testModeData(float[] values) {
        int i;
        int i2 = testFaceAngle;
        if (i2 != -2) {
            float f = testOrientationHardwareValue;
            if (f != -1.0f) {
                if (i2 < 0 || i2 > 360 || f > 9.0f || f < 0.0f) {
                    printDetailLog("TestMode, Ignore Illegal test data.");
                } else {
                    if ((i2 < 0 || i2 >= 60) && ((i = testFaceAngle) < 300 || i > 360)) {
                        int i3 = testFaceAngle;
                        if (i3 < 240 || i3 >= 300) {
                            int i4 = testFaceAngle;
                            if (i4 < 120 || i4 >= 240) {
                                Log.d(SMART_ROTATION_TAG, "TestMode, testOrientationHardwareValue: " + testOrientationHardwareValue + " testFaceAngle: " + testFaceAngle + ", may upload FACE_ORIENTATION_1");
                                testOrientationResult = 1.0f;
                            } else {
                                Log.d(SMART_ROTATION_TAG, "TestMode, testOrientationHardwareValue: " + testOrientationHardwareValue + " testFaceAngle: " + testFaceAngle + ", may upload FACE_ORIENTATION_2");
                                testOrientationResult = FACE_ORIENTATION_2;
                            }
                        } else {
                            Log.d(SMART_ROTATION_TAG, "TestMode, testOrientationHardwareValue: " + testOrientationHardwareValue + " testFaceAngle: " + testFaceAngle + ", may upload FACE_ORIENTATION_3");
                            testOrientationResult = FACE_ORIENTATION_3;
                        }
                    } else {
                        Log.d(SMART_ROTATION_TAG, "TestMode, testOrientationHardwareValue: " + testOrientationHardwareValue + " testFaceAngle: " + testFaceAngle + ", may upload FACE_ORIENTATION_0");
                        testOrientationResult = 0.0f;
                    }
                    testFaceAngle = -2;
                    testOrientationHardwareValue = -1.0f;
                    if (launchCameraAhead) {
                        launchCameraAhead = false;
                    }
                    return testOrientationResult;
                }
            }
        }
        testFaceAngle = -2;
        testOrientationHardwareValue = -1.0f;
        testOrientationResult = 0.0f;
        if (launchCameraAhead) {
            launchCameraAhead = false;
        }
        printDetailLog("TestMode, No test data, upload real hardware value: " + values[0]);
        return values[0];
    }

    private void bindAONService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(AON_PKG_NAME, CLS_NAME));
        printDetailLog("nash_debug, Bind AON service intent: " + intent);
        this.mContext.bindService(intent, serviceConnection, 1);
    }

    private void unbindAONService() {
        printDetailLog("nash_debug, Unbind AON service.");
        this.mContext.unbindService(serviceConnection);
        mAONService = null;
    }

    /* access modifiers changed from: package-private */
    public static class SmartRotationHandler extends Handler {
        SmartRotationHandler(Looper l) {
            super(l);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10001:
                    AonSmartRotation.printDetailLog("init SmartRotationHandler success");
                    return;
                case 10002:
                    AonSmartRotation.useAllProtocol();
                    return;
                case 10003:
                    AonSmartRotation.askForConnectionInner();
                    return;
                case 10004:
                    AonSmartRotation.dropConnectionInner();
                    return;
                case 10005:
                    AonSmartRotation.launchCameraInner();
                    return;
                case 10006:
                    AonSmartRotation.releaseCameraInner();
                    return;
                default:
                    return;
            }
        }
    }

    public static void initAONSubThread() {
        initHandlerThread();
        Message.obtain(mSmartRotationHandler, 10001).sendToTarget();
    }

    public static void initHandlerThread() {
        if (mSmartRotationHandlerThread == null) {
            printDetailLog("init SmartRotationHandlerThread");
            mSmartRotationHandlerThread = new HandlerThread("SmartRotationHandlerThread");
            mSmartRotationHandlerThread.start();
            mSmartRotationLooper = mSmartRotationHandlerThread.getLooper();
            mSmartRotationHandler = new SmartRotationHandler(mSmartRotationLooper);
        }
    }

    public static void dropAONSubThread() {
        printDetailLog("nash_debug, AON disconnect AON Service.");
        dropConnection();
        destroyHandlerThread();
    }

    public static void destroyHandlerThread() {
        try {
            if (mSmartRotationLooper != null) {
                mSmartRotationLooper.quitSafely();
                mSmartRotationLooper = null;
            }
            if (mSmartRotationHandlerThread != null) {
                mSmartRotationHandlerThread.quitSafely();
                mSmartRotationHandlerThread = null;
            }
        } catch (Exception e) {
            Log.w(SMART_ROTATION_TAG, "stop SmartRotationHandlerThread error e = " + e.toString());
        }
    }

    public static int registerDirection() throws RemoteException {
        printDetailLog("nash_debug, registerDirection");
        IAONService iAONService = mAONService;
        if (iAONService == null) {
            Log.w(SMART_ROTATION_TAG, "nash_debug, mAONService is null, registerDirection failed");
            return -1;
        }
        try {
            return iAONService.registerListener(aonlistener, 393218);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 4099;
        }
    }

    public static int unRegisterDirection() throws RemoteException {
        printDetailLog("nash_debug, unRegisterDirection");
        IAONService iAONService = mAONService;
        if (iAONService == null) {
            Log.w(SMART_ROTATION_TAG, "nash_debug, mAONService is null, unRegisterDirection failed");
            return -1;
        }
        try {
            return iAONService.unRegisterListener(aonlistener, 393218);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 4099;
        }
    }

    public static int start() {
        printDetailLog("nash_debug, start camera");
        IAONService iAONService = mAONService;
        if (iAONService == null) {
            Log.w(SMART_ROTATION_TAG, "nash_debug, mAONService is null, start failed");
            return -1;
        }
        try {
            isCameraOn = true;
            return iAONService.start(393218);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 4099;
        }
    }

    public static int stop() {
        printDetailLog("nash_debug, stop camera");
        IAONService iAONService = mAONService;
        if (iAONService == null) {
            Log.w(SMART_ROTATION_TAG, "nash_debug, mAONService is null, stop failed");
            return -1;
        }
        try {
            isCameraOn = false;
            return iAONService.stop(393218);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 4099;
        }
    }

    public static void testAllProtocol() {
        printDetailLog("nash_debug, test All protocols.");
        Message.obtain(mSmartRotationHandler, 10002).sendToTarget();
    }

    /* access modifiers changed from: private */
    public static void useAllProtocol() {
        try {
            int result = registerDirection();
            printDetailLog("nash_debug, registerDirection result is " + result);
            int result2 = start();
            printDetailLog("nash_debug, start result is " + result2);
            int result3 = stop();
            printDetailLog("nash_debug, stop result is " + result3);
            int result4 = unRegisterDirection();
            printDetailLog("nash_debug, unRegisterDirection result is " + result4);
        } catch (RemoteException e) {
            Log.e(SMART_ROTATION_TAG, "use All protocols catch RemoteException.");
            e.printStackTrace();
        }
        printDetailLog("nash_debug, use All protocols finished.");
    }

    public static void askForConnection() {
        printDetailLog("nash_debug, ask for connection.");
        Message.obtain(mSmartRotationHandler, 10003).sendToTarget();
    }

    /* access modifiers changed from: private */
    public static void askForConnectionInner() {
        try {
            int result = registerDirection();
            printDetailLog("nash_debug, registerDirection result is " + result);
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "nash_debug, AON smart rotation askForConnectionInner error e = " + e.toString());
        }
    }

    public static void dropConnection() {
        printDetailLog("nash_debug, drop connection.");
        Message.obtain(mSmartRotationHandler, 10004).sendToTarget();
    }

    /* access modifiers changed from: private */
    public static void dropConnectionInner() {
        try {
            if (isCameraOn) {
                Log.w(SMART_ROTATION_TAG, "nash_debug, About to dropConnection but camera still on, stop it.");
                stop();
            }
            int result = unRegisterDirection();
            printDetailLog("nash_debug, unRegisterDirection result is " + result);
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "nash_debug, AON smart rotation dropConnectionInner error e = " + e.toString());
        }
    }

    public static void launchCamera() {
        printDetailLog("nash_debug, launch Camera.");
        Message.obtain(mSmartRotationHandler, 10005).sendToTarget();
    }

    /* access modifiers changed from: private */
    public static void launchCameraInner() {
        try {
            int result = start();
            printDetailLog("nash_debug, start result is " + result);
            if (isErrorCodeNeedStopCamera(result)) {
                releaseCamera();
                Log.w(SMART_ROTATION_TAG, "Stop camera for special AON error code.");
            }
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "nash_debug, AON smart rotation launchCameraInner error e = " + e.toString());
        }
    }

    public static void releaseCamera() {
        printDetailLog("nash_debug, release Camera.");
        Message.obtain(mSmartRotationHandler, 10006).sendToTarget();
    }

    /* access modifiers changed from: private */
    public static void releaseCameraInner() {
        try {
            int result = stop();
            printDetailLog("nash_debug, stop result is " + result);
        } catch (Exception e) {
            Log.e(SMART_ROTATION_TAG, "nash_debug, AON smart rotation releaseCameraInner error e = " + e.toString());
        }
    }

    public static boolean isErrorCodeNeedStopCamera(int errorCode) {
        if (!stopCameraErrorCodes.contains(Integer.valueOf(errorCode))) {
            return false;
        }
        Log.w(SMART_ROTATION_TAG, "nash_debug, AON return error, errorCode is " + errorCode);
        return true;
    }
}
