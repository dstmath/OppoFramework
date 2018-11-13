package com.android.server.face.test.oppo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.face.ClientMode;
import com.android.server.face.FaceService;
import com.android.server.face.test.sensetime.util.FileUtil;
import com.android.server.face.utils.LogUtil;
import com.android.server.oppo.IElsaManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class ImageUtil {
    /* renamed from: -android-hardware-face-ClientModeSwitchesValues */
    private static final /* synthetic */ int[] f13-android-hardware-face-ClientModeSwitchesValues = null;
    public static final String DIR = null;
    public static final String TAG = "FaceService.ImageUtil";
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_PERF = 3;
    public static final int TYPE_SECRECY = 0;
    private static Object mMutex;
    private static ImageUtil mSingleInstance;
    private boolean mIsImageSaveEnable;

    /* renamed from: -getandroid-hardware-face-ClientModeSwitchesValues */
    private static /* synthetic */ int[] m26-getandroid-hardware-face-ClientModeSwitchesValues() {
        if (f13-android-hardware-face-ClientModeSwitchesValues != null) {
            return f13-android-hardware-face-ClientModeSwitchesValues;
        }
        int[] iArr = new int[ClientMode.values().length];
        try {
            iArr[ClientMode.AUTHEN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ClientMode.ENGINEERING_INFO.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ClientMode.ENROLL.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ClientMode.NONE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ClientMode.REMOVE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ClientMode.UPDATE_FEATURE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f13-android-hardware-face-ClientModeSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.face.test.oppo.util.ImageUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.face.test.oppo.util.ImageUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.test.oppo.util.ImageUtil.<clinit>():void");
    }

    public ImageUtil() {
        this.mIsImageSaveEnable = false;
    }

    public static ImageUtil getImageUtil() {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new ImageUtil();
            }
        }
        return mSingleInstance;
    }

    private String getFilterDirFromResult(ClientMode mode, int acquiredResult) {
        String rootDir = IElsaManager.EMPTY_PACKAGE;
        switch (m26-getandroid-hardware-face-ClientModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                rootDir = "auth/";
                break;
            case 2:
                rootDir = "enroll/";
                break;
        }
        String subDir = IElsaManager.EMPTY_PACKAGE;
        switch (acquiredResult) {
            case 1:
                subDir = "outOfFrame/";
                break;
            case 2:
                subDir = "outOfScreen/";
                break;
            case 3:
                subDir = "imageQuality/";
                break;
            case 6:
                subDir = "far/";
                break;
            case 7:
                subDir = "near/";
                break;
            case 101:
                subDir = "noFace/";
                break;
            case 102:
            case 107:
            case 108:
            case 109:
            case 110:
                subDir = "angle/";
                break;
            case 103:
                subDir = "dark/";
                break;
            case 104:
                subDir = "hackerness/";
                break;
            case 106:
                subDir = "bright/";
                break;
            default:
                subDir = "drop/";
                break;
        }
        return rootDir + subDir;
    }

    public void updateDebugSwitch(int type, boolean isOn) {
        LogUtil.e(TAG, "updateDebugSwitch, type = " + type + ", isOn = " + isOn);
        this.mIsImageSaveEnable = isOn;
    }

    public String saveBytes(byte[] nv21, ClientMode mode, int acquiredResult, String info) {
        boolean on;
        if (FaceService.IS_REALEASE_VERSION) {
            on = this.mIsImageSaveEnable;
        } else {
            on = FaceService.DEBUG;
        }
        if (!on) {
            return null;
        }
        String fullPath;
        String subdir = getFilterDirFromResult(mode, acquiredResult);
        String time = getFormatTime(System.currentTimeMillis());
        if (info != null) {
            fullPath = DIR + subdir + time + "_(" + info + ")" + ".yuv";
        } else {
            fullPath = DIR + subdir + time + ".yuv";
        }
        if (nv21 != null) {
            LogUtil.d(TAG, "saveBytes fullPath = " + fullPath);
            FileUtil.geFileUtil().saveBytes(nv21, fullPath);
            return fullPath;
        }
        LogUtil.d(TAG, "avatarBitmap == null");
        return null;
    }

    public String saveBitmap(byte[] nv21, int width, int height, String subdir, String name) {
        if (FaceService.IS_REALEASE_VERSION || !FaceService.DEBUG) {
            LogUtil.d(TAG, "DEBUG = " + FaceService.DEBUG);
            return null;
        }
        YuvImage image = new YuvImage(nv21, 17, width, height, null);
        ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, outputSteam);
        byte[] jpegData = outputSteam.toByteArray();
        Bitmap avatarBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        if (avatarBitmap != null) {
            String fullPath = DIR + subdir + "/" + name + ".jpg";
            LogUtil.d(TAG, "saveBitmap fullPath = " + fullPath);
            FileUtil.geFileUtil().saveBitmap(avatarBitmap, fullPath);
            try {
                outputSteam.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return fullPath;
        }
        LogUtil.d(TAG, "avatarBitmap == null");
        try {
            outputSteam.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return null;
    }

    public String getFormatTime(long milliseconds) {
        return new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss.SSSS").format(new Date(milliseconds));
    }
}
