package com.android.server.policy;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
public class OppoScreenOffGestureUtil {
    private static int POINT_COUNT;
    private static String PROC_PATH_COORDINATE;
    private static String SPLIT_POINT;
    private static String SPLIT_X_Y;
    private static String TAG;
    private boolean DEBUG;
    int mFlagClockwise;
    int mGestureType;
    Point[] mPoints;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.OppoScreenOffGestureUtil.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.OppoScreenOffGestureUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.OppoScreenOffGestureUtil.<clinit>():void");
    }

    OppoScreenOffGestureUtil(Context context) {
        this.DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mPoints = new Point[POINT_COUNT];
        this.mFlagClockwise = 1;
        this.mGestureType = 0;
        for (int i = 0; i < this.mPoints.length; i++) {
            this.mPoints[i] = new Point();
        }
    }

    public void updateGestureInfo() {
        try {
            String[] strPoint = readFileFromProc(PROC_PATH_COORDINATE).split(SPLIT_POINT);
            this.mGestureType = Integer.parseInt(strPoint[0]);
            for (int i = 0; i < POINT_COUNT; i++) {
                String[] strXY = strPoint[i + 1].split(SPLIT_X_Y);
                this.mPoints[i].x = Integer.parseInt(strXY[0]);
                this.mPoints[i].y = Integer.parseInt(strXY[1]);
            }
            this.mFlagClockwise = Integer.parseInt(strPoint[strPoint.length - 1]);
            if (this.DEBUG) {
                for (int k = 0; k < this.mPoints.length; k++) {
                    Log.d(TAG, " ************* " + this.mPoints[k].x + "  " + this.mPoints[k].y + "  mFlagClockwise = " + this.mFlagClockwise + "  mGestureType = " + this.mGestureType);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "get gesture info error");
        }
    }

    public String readFileFromProc(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String strline = br.readLine();
            fr.close();
            br.close();
            return strline;
        } catch (IOException e) {
            return IElsaManager.EMPTY_PACKAGE;
        }
    }
}
