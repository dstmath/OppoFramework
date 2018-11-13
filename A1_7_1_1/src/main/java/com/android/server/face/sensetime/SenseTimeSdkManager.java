package com.android.server.face.sensetime;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.face.ClientMode;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.android.server.ServiceThread;
import com.android.server.display.OppoBrightUtils;
import com.android.server.face.FaceFilter;
import com.android.server.face.FaceHypnus;
import com.android.server.face.sensetime.faceapi.FaceDetect;
import com.android.server.face.sensetime.faceapi.FaceHacker;
import com.android.server.face.sensetime.faceapi.FaceOcular;
import com.android.server.face.sensetime.faceapi.FaceSelect;
import com.android.server.face.sensetime.faceapi.FaceTrack;
import com.android.server.face.sensetime.faceapi.FaceVerify;
import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.sensetime.faceapi.model.FaceConfig.FaceImageResize;
import com.android.server.face.sensetime.faceapi.model.FaceOrientation;
import com.android.server.face.test.oppo.util.ImageUtil;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import com.sensetime.faceapi.FaceLibrary;
import com.sensetime.faceapi.model.FaceInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SenseTimeSdkManager {
    public static boolean DEBUG = false;
    private static final int MAX_NO_FACE_NUMBER = 3;
    private static final int MSG_FACE_ENROLL = 3;
    private static final int MSG_FACE_VERIFY = 4;
    private static final int MSG_INIT_LICENSE = 1;
    private static final int MSG_LOAD_ALGORITHM_MODEL = 2;
    private static final int MSG_UNLOCK_NEXT_REGOGNITION = 5;
    private static final String TAG = "FaceService.SenseTimeSdkManager";
    private static final long TIMEOUT_FOR_NEXT_ALL = 0;
    private static final long TIMEOUT_FOR_NEXT_AUTHEN = 0;
    private static long TIMEOUT_FOR_NEXT_ENROLL = 0;
    private static final long TIMEOUT_GET_FITER_SUCCESS_FACE = 3000;
    public static final int TYPE_ALL = 3;
    public static final int TYPE_AUTHEN = 2;
    public static final int TYPE_ENROLL = 1;
    private static Object mMutex;
    private static SenseTimeSdkManager mSingleInstance;
    private final String LICENSE_PATH;
    private int mAcquiredResult;
    private final Context mContext;
    private int mDegrees;
    private FaceDetect mFaceDetect320;
    private FaceDetect mFaceDetect640;
    private FaceHacker mFaceHacker;
    private FaceOcular mFaceOcular;
    private FaceSelect mFaceSelect;
    private FaceTrack mFaceTrack;
    private FaceVerify mFaceVerify;
    private ExHandler mHandler;
    private IFaceFilterListener mIFaceFilterListener;
    private volatile boolean mIsHacker;
    private final Looper mLooper;
    private long mNV21ImageBufferSeq;
    private byte[] mNV21ImageData;
    private int mNV21ImageDataHeight;
    private int mNV21ImageDataWidth;
    private long mNoFaceNumber;
    private boolean mRecognitionLocked;
    private Rect mRegistPreviewRect;
    private final ServiceThread mServiceThread;
    private long mStartGetNewFilterSuccessTime;
    private final int mVervifyVersion;

    public interface IFaceFilterListener {
        void onFaceFilterFailed(long j, int i);

        void onFaceFilterSucceeded(long j, int i, byte[] bArr, int i2, int i3, FaceInfo faceInfo);

        void onFailReasonAcquired(long j, int i);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.face.sensetime.SenseTimeSdkManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.face.sensetime.SenseTimeSdkManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.sensetime.SenseTimeSdkManager.<clinit>():void");
    }

    public static SenseTimeSdkManager initSenseTimeSdkManager(Context c, IFaceFilterListener faceFilterListener) {
        return getSenseTimeSdkManager(c, faceFilterListener);
    }

    public static SenseTimeSdkManager getSenseTimeSdkManager(Context c, IFaceFilterListener faceFilterListener) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new SenseTimeSdkManager(c, faceFilterListener);
            }
            mSingleInstance.updateCallback(faceFilterListener);
        }
        return mSingleInstance;
    }

    private void updateCallback(IFaceFilterListener faceFilterListener) {
        this.mIFaceFilterListener = faceFilterListener;
    }

    public static SenseTimeSdkManager getSenseTimeSdkManager() {
        return mSingleInstance;
    }

    public void setPreviewCoordinate(Rect rect) {
        if (rect != null) {
            LogUtil.d(TAG, "setPreviewCoordinate, rect = " + rect.toString());
            this.mRegistPreviewRect = rect;
        }
    }

    public SenseTimeSdkManager(Context context, IFaceFilterListener faceFilterListener) {
        this.mRegistPreviewRect = null;
        this.mVervifyVersion = -1;
        this.mAcquiredResult = 0;
        this.LICENSE_PATH = "/system/bpm/license.lic";
        this.mNV21ImageBufferSeq = 0;
        this.mNoFaceNumber = 0;
        this.mRecognitionLocked = false;
        this.mContext = context;
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        this.mLooper = this.mServiceThread.getLooper();
        initHandler();
        updateCallback(faceFilterListener);
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            public void handleMessage(Message msg) {
                LogUtil.d(SenseTimeSdkManager.TAG, "handleMessage, begin msg.what = " + SenseTimeSdkManager.this.msgToString(msg.what) + ", msg.arg2 = " + msg.arg2);
                switch (msg.what) {
                    case 1:
                        SenseTimeSdkManager.this.handleInitLicense();
                        break;
                    case 2:
                        SenseTimeSdkManager.this.handleLoadAlgorithmModel();
                        break;
                    case 3:
                        SenseTimeSdkManager.this.handleFaceEnroll();
                        break;
                    case 4:
                        SenseTimeSdkManager.this.handleFaceVerification();
                        break;
                    case 5:
                        SenseTimeSdkManager.this.handleUnlockNextRecognition();
                        break;
                }
                LogUtil.d(SenseTimeSdkManager.TAG, "handleMessage, end msg.what = " + SenseTimeSdkManager.this.msgToString(msg.what) + ", msg.arg2 = " + msg.arg2);
                super.handleMessage(msg);
            }
        };
    }

    public void initLicense() {
        sendMessageWithArgs(1, 0);
    }

    public void loadAlgorithmModel() {
        sendMessageWithArgs(2, 0);
    }

    public void faceEnroll(long nv21ImageBufferSeq, byte[] nv21PreviewData, int nv21PpreviewWidth, int nv21PreviewHeight, int degrees) {
        updateImageData(nv21ImageBufferSeq, nv21PreviewData, nv21PpreviewWidth, nv21PreviewHeight, degrees);
        sendMessageWithArgs(3, 0);
    }

    public void faceVerification(long nv21ImageBufferSeq, byte[] nv21PreviewData, int nv21PpreviewWidth, int nv21PreviewHeight, int degrees) {
        updateImageData(nv21ImageBufferSeq, nv21PreviewData, nv21PpreviewWidth, nv21PreviewHeight, degrees);
        sendMessageWithArgs(4, 0);
    }

    public void resetTrack() {
        if (this.mFaceTrack != null) {
            LogUtil.d(TAG, "mFaceTrack reset");
            long startTime = SystemClock.uptimeMillis();
            this.mFaceTrack.reset();
            TimeUtils.calculateTime(TAG, "resetTrack", SystemClock.uptimeMillis() - startTime);
            return;
        }
        LogUtil.e(TAG, "mFaceTrack is null !");
    }

    public void resetSelect() {
        if (this.mFaceSelect != null) {
            LogUtil.d(TAG, "mFaceSelect reset");
            long startTime = SystemClock.uptimeMillis();
            this.mFaceSelect.reset();
            TimeUtils.calculateTime(TAG, "resetSelect", SystemClock.uptimeMillis() - startTime);
            return;
        }
        LogUtil.e(TAG, "mFaceSelect is null !");
    }

    public void unLockRecognitionForNext(int type) {
        sendMessageWithArgsDelayed(5, 0, getTimeoutForNextRecognition(type));
    }

    private void handleInitLicense() {
        String license = getAssertData(this.mContext);
        if (license == null) {
            LogUtil.e(TAG, "no license file");
            return;
        }
        long startTime = SystemClock.uptimeMillis();
        int result = FaceLibrary.initLiscenceStr(license);
        LogUtil.d(TAG, "init license result = " + result + ", " + (result == 0 ? "sucess" : "fail"));
        TimeUtils.calculateTime(TAG, "initLiscenceStr", SystemClock.uptimeMillis() - startTime);
    }

    private void handleLoadAlgorithmModel() {
        LogUtil.d(TAG, "loadAlgoritmModel");
        long startTime = SystemClock.uptimeMillis();
        initDetect();
        initTrack();
        initSelect();
        initOcular();
        TimeUtils.calculateTime(TAG, "loadAlgoritmModel", SystemClock.uptimeMillis() - startTime);
    }

    private void handleFaceEnroll() {
        FaceInfo[] faces = track(this.mNV21ImageData, this.mNV21ImageDataWidth, this.mNV21ImageDataHeight);
        boolean filterSuccess = false;
        if (faces == null || faces.length <= 0) {
            resetTrack();
            resetSelect();
            this.mIFaceFilterListener.onFaceFilterFailed(this.mNV21ImageBufferSeq, 101);
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, 101, null);
        } else {
            FaceHypnus.getInstance().hypnusSpeedUp(300);
            FaceInfo face = filterEnrollFaces(this.mNV21ImageData, faces, this.mNV21ImageDataWidth, this.mNV21ImageDataHeight, this.mDegrees);
            if (face != null) {
                filterSuccess = true;
                this.mIFaceFilterListener.onFaceFilterSucceeded(this.mNV21ImageBufferSeq, 0, this.mNV21ImageData, this.mNV21ImageDataWidth, this.mNV21ImageDataHeight, face);
            } else {
                this.mIFaceFilterListener.onFaceFilterFailed(this.mNV21ImageBufferSeq, this.mAcquiredResult);
            }
        }
        if (!filterSuccess) {
            unLockRecognitionForNext(1);
        }
    }

    private void handleFaceVerification() {
        if (this.mNV21ImageBufferSeq == 1) {
            this.mNoFaceNumber = 0;
        }
        LogUtil.d(TAG, "handleFaceVerification mNV21ImageBufferSeq = " + this.mNV21ImageBufferSeq + ", mNoFaceNumber = " + this.mNoFaceNumber);
        FaceInfo[] faces = detect(this.mNoFaceNumber, this.mNV21ImageData, this.mNV21ImageDataWidth, this.mNV21ImageDataHeight);
        boolean filterSuccess = false;
        if (faces == null || faces.length <= 0) {
            this.mNoFaceNumber++;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.AUTHEN, 101, null);
            this.mIFaceFilterListener.onFaceFilterFailed(this.mNV21ImageBufferSeq, 101);
        } else {
            FaceHypnus.getInstance().hypnusSpeedUp(300);
            FaceInfo faceInfo = FaceFilter.getMaxFace(faces);
            FaceInfo face = filterVerificationFaces(faceInfo, this.mNV21ImageDataWidth, this.mNV21ImageDataHeight);
            if (face != null) {
                filterSuccess = true;
                if (hasPossibleReasonOfFail(faceInfo, this.mNV21ImageData, this.mNV21ImageDataWidth, this.mNV21ImageDataHeight)) {
                    this.mIFaceFilterListener.onFailReasonAcquired(this.mNV21ImageBufferSeq, this.mAcquiredResult);
                }
                this.mIFaceFilterListener.onFaceFilterSucceeded(this.mNV21ImageBufferSeq, 0, this.mNV21ImageData, this.mNV21ImageDataWidth, this.mNV21ImageDataHeight, face);
            } else {
                this.mIFaceFilterListener.onFaceFilterFailed(this.mNV21ImageBufferSeq, this.mAcquiredResult);
                ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.AUTHEN, this.mAcquiredResult, null);
            }
        }
        if (!filterSuccess) {
            unLockRecognitionForNext(2);
        }
    }

    protected void handleUnlockNextRecognition() {
        LogUtil.d(TAG, "handleUnlockNextRecognition");
        unlockRecognition();
    }

    private boolean hasPossibleReasonOfFail(FaceInfo faceInfo, byte[] nv21, int width, int height) {
        long startTime = SystemClock.uptimeMillis();
        LogUtil.d(TAG, "hasPossibleReasonOfFail");
        this.mAcquiredResult = 0;
        if (FaceFilter.isFitAngle(faceInfo)) {
            LogUtil.w(TAG, "pitch or yaw is not illegal, faceInfo = " + faceInfo.toString());
            this.mAcquiredResult = 102;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.AUTHEN, this.mAcquiredResult, "yaw=" + faceInfo.yaw + "_pitch=" + faceInfo.pitch);
            return true;
        }
        float brightness = FaceFilter.getBrightness(nv21, width, height, faceInfo.faceRect);
        TimeUtils.calculateTime(TAG, "getBrightness", SystemClock.uptimeMillis() - startTime);
        if (brightness >= 0.17f) {
            return false;
        }
        LogUtil.w(TAG, "too dark, brightness = " + brightness);
        this.mAcquiredResult = 103;
        ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.AUTHEN, this.mAcquiredResult, "brightness=" + brightness);
        return true;
    }

    private FaceInfo filterVerificationFaces(FaceInfo faceInfo, int width, int height) {
        LogUtil.d(TAG, "filterVerificationFaces ");
        long startTime = SystemClock.uptimeMillis();
        this.mAcquiredResult = 0;
        Rect faceRect = null;
        if (faceInfo != null) {
            faceRect = faceInfo.faceRect;
        }
        if (faceRect == null || faceInfo == null) {
            LogUtil.e(TAG, "max face is null");
            this.mNoFaceNumber++;
            this.mAcquiredResult = 101;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.AUTHEN, this.mAcquiredResult, null);
            return null;
        } else if (!FaceFilter.isFitRollAngle(faceInfo, -45.0f, 45.0f)) {
            LogUtil.w(TAG, "verify face roll is too large or too small, maybe is landscape");
            this.mAcquiredResult = 101;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.AUTHEN, this.mAcquiredResult, "roll=" + faceInfo.roll);
            return null;
        } else if (FaceFilter.containFace(new Rect(0, 0, width, height), faceRect)) {
            TimeUtils.calculateTime(TAG, "filterVerificationFaces", SystemClock.uptimeMillis() - startTime);
            return faceInfo;
        } else {
            LogUtil.w(TAG, "face out of frame, faceInfo = " + faceInfo.toString());
            this.mAcquiredResult = 2;
            return null;
        }
    }

    private FaceInfo filterEnrollFaces(byte[] nv21, FaceInfo[] faces, int width, int height, int degrees) {
        LogUtil.d(TAG, "filterEnrollFaces");
        long startTime = SystemClock.uptimeMillis();
        this.mAcquiredResult = 0;
        Rect rect = null;
        FaceInfo faceInfo = FaceFilter.getMaxFace(faces);
        if (faceInfo != null) {
            rect = new Rect(faceInfo.faceRect);
        }
        if (faceInfo == null || rect == null || this.mRegistPreviewRect == null) {
            LogUtil.w(TAG, "rect is null");
            this.mAcquiredResult = 101;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, null);
            return null;
        }
        FaceFilter.rotateFaceRect(rect, width, height, true, degrees);
        if (FaceFilter.hasNoneFaceInWidgetRect(rect, this.mRegistPreviewRect)) {
            LogUtil.w(TAG, "find face but it is out of preview frame");
            this.mAcquiredResult = 101;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, rect.toString());
            return null;
        }
        int faceScale = FaceFilter.faceScaleFit(rect, this.mRegistPreviewRect, faceInfo.yaw);
        if (faceScale == -1) {
            LogUtil.w(TAG, "face is too close to screen");
            this.mAcquiredResult = 6;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, null);
            return null;
        } else if (FaceFilter.isFaceBeyondWidgetRectBorder(rect, this.mRegistPreviewRect)) {
            LogUtil.w(TAG, "face is not in whole of preview frame");
            this.mAcquiredResult = 1;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, rect.toString());
            return null;
        } else if (!FaceFilter.isFitRollAngle(faceInfo, -135.0f, -45.0f)) {
            LogUtil.w(TAG, "face roll is too large or too small, maybe is landscape");
            this.mAcquiredResult = 101;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, "roll=" + faceInfo.roll);
            return null;
        } else if (faceInfo.pitch < -8.0f || faceInfo.pitch > 13.0f || faceInfo.yaw < -8.0f || faceInfo.yaw > 8.0f) {
            LogUtil.w(TAG, "not front face, pitch = " + faceInfo.pitch + ", yaw = " + faceInfo.yaw);
            if (faceInfo.pitch < -8.0f) {
                this.mAcquiredResult = 110;
            } else if (faceInfo.pitch > 13.0f) {
                this.mAcquiredResult = 109;
            } else if (faceInfo.yaw < -8.0f) {
                this.mAcquiredResult = 108;
            } else if (faceInfo.yaw > 8.0f) {
                this.mAcquiredResult = 107;
            }
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, "yaw=" + faceInfo.yaw + "_pitch=" + faceInfo.pitch);
            return null;
        } else if (faceScale == 1) {
            LogUtil.w(TAG, "face is too far from screen");
            this.mAcquiredResult = 7;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, null);
            return null;
        } else {
            float brightness = FaceFilter.getBrightness(nv21, width, height, rect);
            if (brightness < 0.17f) {
                LogUtil.w(TAG, "too dark, brightness = " + brightness);
                this.mAcquiredResult = 103;
                ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, "brightness=" + brightness);
                return null;
            }
            float score = getQuality(nv21, width, height, faceInfo);
            if (FaceFilter.isHighQuality(score)) {
                TimeUtils.calculateTime(TAG, "filterEnrollFaces", SystemClock.uptimeMillis() - startTime);
                return faceInfo;
            }
            LogUtil.w(TAG, "filterFaces: this face quality is illegal !");
            this.mAcquiredResult = 3;
            ImageUtil.getImageUtil().saveBytes(this.mNV21ImageData, ClientMode.ENROLL, this.mAcquiredResult, "score=" + score);
            return null;
        }
    }

    private void sendMessageWithArgs(int what, int arg1) {
        if (!this.mHandler.hasMessages(what)) {
            int triggerTime = (int) SystemClock.uptimeMillis();
            this.mHandler.obtainMessage(what, arg1, triggerTime, null).sendToTarget();
            LogUtil.d(TAG, "sendMessageWithArgs, what = " + msgToString(what) + ", arg2 = " + triggerTime);
        }
    }

    private void sendMessageWithArgsDelayed(int what, int arg1, long delayMillis) {
        if (!this.mHandler.hasMessages(what)) {
            int triggerTime = (int) SystemClock.uptimeMillis();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg1, triggerTime, null), delayMillis);
            LogUtil.d(TAG, "sendMessageWithArgs, what = " + msgToString(what) + ", arg2 = " + triggerTime);
        }
    }

    private void updateImageData(long nv21ImageBufferSeq, byte[] nv21ImageData, int nv21PreviewWidth, int nv21PpreviewHeight, int degrees) {
        LogUtil.d(TAG, "updateImageData");
        synchronized (mMutex) {
            if (this.mNV21ImageData == null || nv21ImageData.length != this.mNV21ImageData.length) {
                this.mNV21ImageData = new byte[nv21ImageData.length];
            }
            System.arraycopy(nv21ImageData, 0, this.mNV21ImageData, 0, nv21ImageData.length);
            this.mNV21ImageDataWidth = nv21PreviewWidth;
            this.mNV21ImageDataHeight = nv21PpreviewHeight;
            this.mNV21ImageBufferSeq = nv21ImageBufferSeq;
            this.mDegrees = degrees;
            lockRecognition();
        }
    }

    private void lockRecognition() {
        synchronized (mMutex) {
            this.mRecognitionLocked = true;
        }
    }

    private void unlockRecognition() {
        synchronized (mMutex) {
            this.mRecognitionLocked = false;
        }
    }

    public boolean isRecognitionLocked() {
        boolean locked;
        synchronized (mMutex) {
            locked = this.mRecognitionLocked;
        }
        return locked;
    }

    private long getTimeoutForNextRecognition(int type) {
        switch (type) {
            case 1:
                return TIMEOUT_FOR_NEXT_ENROLL;
            case 2:
                return 0;
            case 3:
                return 0;
            default:
                return -1;
        }
    }

    private void initDetect() {
        long startTime;
        if (this.mFaceDetect320 == null) {
            startTime = SystemClock.uptimeMillis();
            this.mFaceDetect320 = new FaceDetect(FaceImageResize.RESIZE_320W);
            TimeUtils.calculateTime(TAG, "initDetect320", SystemClock.uptimeMillis() - startTime);
        }
        if (this.mFaceDetect640 == null) {
            startTime = SystemClock.uptimeMillis();
            this.mFaceDetect640 = new FaceDetect(FaceImageResize.RESIZE_640W);
            TimeUtils.calculateTime(TAG, "initDetect640", SystemClock.uptimeMillis() - startTime);
        }
    }

    private void initOcular() {
        if (this.mFaceOcular != null) {
            LogUtil.d(TAG, "initOcular mFaceOcular != null");
            return;
        }
        LogUtil.d(TAG, "initOcular");
        long startTime = SystemClock.uptimeMillis();
        this.mFaceOcular = new FaceOcular();
        TimeUtils.calculateTime(TAG, "initOcular", SystemClock.uptimeMillis() - startTime);
    }

    private boolean checkOcular(byte[] nv21, int width, int height, Rect rect) {
        if (this.mFaceOcular == null) {
            LogUtil.w(TAG, "mFaceOcular = null");
            return false;
        }
        long startTime = SystemClock.uptimeMillis();
        boolean eyeOpen = this.mFaceOcular.checkOcular(nv21, CvPixelFormat.NV21, width, height, width, rect, FaceOrientation.RIGHT);
        TimeUtils.calculateTime(TAG, "checkOcular", SystemClock.uptimeMillis() - startTime);
        return eyeOpen;
    }

    private void releaseOcular() {
        if (this.mFaceOcular != null) {
            LogUtil.d(TAG, "releaseOcular");
            long startTime = SystemClock.uptimeMillis();
            this.mFaceOcular.release();
            TimeUtils.calculateTime(TAG, "releaseOcular", SystemClock.uptimeMillis() - startTime);
            this.mFaceOcular = null;
        }
    }

    private FaceInfo[] detect(long noFaceNumber, byte[] nv21, int width, int height) {
        long startTime;
        FaceInfo[] faces;
        if (this.mFaceDetect320 != null && noFaceNumber >= 0 && noFaceNumber < 3) {
            startTime = SystemClock.uptimeMillis();
            faces = this.mFaceDetect320.detect(nv21, CvPixelFormat.NV21, width, height, width, FaceOrientation.RIGHT);
            TimeUtils.calculateTime(TAG, "detect320", SystemClock.uptimeMillis() - startTime);
            return faces;
        } else if (this.mFaceDetect640 == null || noFaceNumber < 3) {
            return null;
        } else {
            startTime = SystemClock.uptimeMillis();
            faces = this.mFaceDetect640.detect(nv21, CvPixelFormat.NV21, width, height, width, FaceOrientation.RIGHT);
            TimeUtils.calculateTime(TAG, "detect640", SystemClock.uptimeMillis() - startTime);
            return faces;
        }
    }

    private void releaseDetect() {
        long startTime;
        if (this.mFaceDetect320 != null) {
            startTime = SystemClock.uptimeMillis();
            this.mFaceDetect320.release();
            TimeUtils.calculateTime(TAG, "releaseDetect320", SystemClock.uptimeMillis() - startTime);
            this.mFaceDetect320 = null;
        }
        if (this.mFaceDetect640 != null) {
            startTime = SystemClock.uptimeMillis();
            this.mFaceDetect640.release();
            TimeUtils.calculateTime(TAG, "releaseDetect640", SystemClock.uptimeMillis() - startTime);
            this.mFaceDetect640 = null;
        }
    }

    public void initTrack() {
        if (this.mFaceTrack != null) {
            LogUtil.d(TAG, "initTrack mFaceTrack != null");
            return;
        }
        LogUtil.d(TAG, "initTrack");
        long startTime = SystemClock.uptimeMillis();
        this.mFaceTrack = new FaceTrack();
        this.mFaceTrack.setFaceTrackInterval(1);
        TimeUtils.calculateTime(TAG, "initTrack", SystemClock.uptimeMillis() - startTime);
    }

    private FaceInfo[] track(byte[] nv21, int width, int height) {
        if (this.mFaceTrack == null) {
            LogUtil.w(TAG, "mFaceTrack is null !");
            return null;
        }
        long startTime = SystemClock.uptimeMillis();
        FaceInfo[] faces = this.mFaceTrack.track(nv21, CvPixelFormat.NV21, width, height, width, FaceOrientation.RIGHT);
        TimeUtils.calculateTime(TAG, "track", SystemClock.uptimeMillis() - startTime);
        return faces;
    }

    private void releaseTrack() {
        if (this.mFaceTrack != null) {
            LogUtil.d(TAG, "releaseTrack");
            long startTime = SystemClock.uptimeMillis();
            this.mFaceTrack.release();
            TimeUtils.calculateTime(TAG, "releaseTrack", SystemClock.uptimeMillis() - startTime);
            this.mFaceTrack = null;
        }
    }

    private void initSelect() {
        if (this.mFaceSelect != null) {
            LogUtil.d(TAG, "initSelect mFaceSelect != null");
            return;
        }
        LogUtil.d(TAG, "initSelect time");
        long startTime = SystemClock.uptimeMillis();
        this.mFaceSelect = new FaceSelect(FaceImageResize.RESIZE_640W);
        TimeUtils.calculateTime(TAG, "initSelect", SystemClock.uptimeMillis() - startTime);
    }

    private float getQuality(byte[] nv21, int width, int height, FaceInfo faceInfo) {
        if (this.mFaceSelect == null) {
            LogUtil.w(TAG, "mFaceSelect is null !");
            return OppoBrightUtils.MIN_LUX_LIMITI;
        }
        long startTime = SystemClock.uptimeMillis();
        float score = this.mFaceSelect.selectFrame(nv21, CvPixelFormat.NV21, width, height, width, faceInfo);
        LogUtil.d(TAG, "getQuality score = " + score);
        TimeUtils.calculateTime(TAG, "getQuality", SystemClock.uptimeMillis() - startTime);
        return score;
    }

    private void releaseSelect() {
        if (this.mFaceSelect != null) {
            long startTime = SystemClock.uptimeMillis();
            LogUtil.v(TAG, "releaseSelect");
            this.mFaceSelect.release();
            TimeUtils.calculateTime(TAG, "releaseSelect", SystemClock.uptimeMillis() - startTime);
            this.mFaceSelect = null;
        }
    }

    private String getAssertData(Context context) {
        try {
            InputStream stream = new FileInputStream(new File("/system/bpm/license.lic"));
            byte[] data = new byte[stream.available()];
            stream.read(data);
            stream.close();
            return new String(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String msgToString(int msgCode) {
        switch (msgCode) {
            case 1:
                return "MSG_INIT_LICENSE";
            case 2:
                return "MSG_LOAD_ALGORITHM_MODEL";
            case 3:
                return "MSG_FACE_ENROLL";
            case 4:
                return "MSG_FACE_VERIFY";
            case 5:
                return "MSG_UNLOCK_NEXT_REGOGNITION";
            default:
                return "unknown-code";
        }
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        String subPrefix = "  " + prefix;
        pw.print(prefix);
        pw.println("SenseTimeSdkManager dump");
        pw.print(subPrefix);
        pw.println("mVervifyVersion = -1");
        pw.print(subPrefix);
        pw.println("mRegistPreviewRect = " + this.mRegistPreviewRect);
        pw.print(subPrefix);
        pw.println("mAcquiredResult = " + this.mAcquiredResult);
        pw.print(subPrefix);
        pw.println("mRecognitionLocked = " + this.mRecognitionLocked);
        pw.print(subPrefix);
        pw.println("TIMEOUT_FOR_NEXT_ENROLL = " + TIMEOUT_FOR_NEXT_ENROLL);
    }

    public void updateEnrollTimeout(String enrollTimeout) {
        try {
            TIMEOUT_FOR_NEXT_ENROLL = (long) Integer.parseInt(enrollTimeout);
        } catch (NumberFormatException e) {
            LogUtil.d(TAG, "updateEnrollTimeout error happend " + e);
        }
        LogUtil.d(TAG, "updateEnrollTimeout , TIMEOUT_FOR_NEXT_ENROLL = " + TIMEOUT_FOR_NEXT_ENROLL);
    }
}
