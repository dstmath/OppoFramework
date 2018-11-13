package com.android.server.face;

import android.content.Context;
import android.hardware.face.FaceFeature;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.util.List;

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
public class FaceUtils {
    private static final long[] FACE_ERROR_VIBRATE_PATTERN = null;
    private static final long[] FACE_SUCCESS_VIBRATE_PATTERN = null;
    private static FaceUtils sInstance;
    private static final Object sInstanceLock = null;
    @GuardedBy("this")
    private final SparseArray<FacesUserState> mUsers;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.FaceUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.FaceUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.FaceUtils.<clinit>():void");
    }

    public static FaceUtils getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FaceUtils();
            }
        }
        return sInstance;
    }

    private FaceUtils() {
        this.mUsers = new SparseArray();
    }

    public List<FaceFeature> getFacesForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getFaces();
    }

    public void addFaceForUser(Context ctx, int faceId, int userId) {
        getStateForUser(ctx, userId).addFace(faceId, userId);
    }

    public void removeFaceIdForUser(Context ctx, int faceId, int userId) {
        getStateForUser(ctx, userId).removeFace(faceId);
    }

    public void syncFaceIdForUser(Context ctx, int[] faceIds, int userId) {
        getStateForUser(ctx, userId).syncFaces(faceIds, userId);
    }

    public void renameFaceForUser(Context ctx, int faceId, int userId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            getStateForUser(ctx, userId).renameFace(faceId, name);
        }
    }

    public static void vibrateFaceError(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FACE_ERROR_VIBRATE_PATTERN, -1);
        }
    }

    public static void vibrateFaceSuccess(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FACE_SUCCESS_VIBRATE_PATTERN, -1);
        }
    }

    private FacesUserState getStateForUser(Context ctx, int userId) {
        FacesUserState state;
        synchronized (this) {
            state = (FacesUserState) this.mUsers.get(userId);
            if (state == null) {
                state = new FacesUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }
}
