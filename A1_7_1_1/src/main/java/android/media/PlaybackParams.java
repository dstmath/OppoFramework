package android.media;

import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

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
public final class PlaybackParams implements Parcelable {
    public static final int AUDIO_FALLBACK_MODE_DEFAULT = 0;
    public static final int AUDIO_FALLBACK_MODE_FAIL = 2;
    public static final int AUDIO_FALLBACK_MODE_MUTE = 1;
    public static final int AUDIO_STRETCH_MODE_DEFAULT = 0;
    public static final int AUDIO_STRETCH_MODE_VOICE = 1;
    public static final Creator<PlaybackParams> CREATOR = null;
    private static final int SET_AUDIO_FALLBACK_MODE = 4;
    private static final int SET_AUDIO_STRETCH_MODE = 8;
    private static final int SET_PITCH = 2;
    private static final int SET_SPEED = 1;
    private int mAudioFallbackMode;
    private int mAudioStretchMode;
    private float mPitch;
    private int mSet;
    private float mSpeed;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.PlaybackParams.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.PlaybackParams.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.PlaybackParams.<clinit>():void");
    }

    /* synthetic */ PlaybackParams(Parcel in, PlaybackParams playbackParams) {
        this(in);
    }

    public PlaybackParams() {
        this.mSet = 0;
        this.mAudioFallbackMode = 0;
        this.mAudioStretchMode = 0;
        this.mPitch = 1.0f;
        this.mSpeed = 1.0f;
    }

    private PlaybackParams(Parcel in) {
        this.mSet = 0;
        this.mAudioFallbackMode = 0;
        this.mAudioStretchMode = 0;
        this.mPitch = 1.0f;
        this.mSpeed = 1.0f;
        this.mSet = in.readInt();
        this.mAudioFallbackMode = in.readInt();
        this.mAudioStretchMode = in.readInt();
        this.mPitch = in.readFloat();
        if (this.mPitch < TonemapCurve.LEVEL_BLACK) {
            this.mPitch = TonemapCurve.LEVEL_BLACK;
        }
        this.mSpeed = in.readFloat();
    }

    public PlaybackParams allowDefaults() {
        this.mSet |= 15;
        return this;
    }

    public PlaybackParams setAudioFallbackMode(int audioFallbackMode) {
        this.mAudioFallbackMode = audioFallbackMode;
        this.mSet |= 4;
        return this;
    }

    public int getAudioFallbackMode() {
        if ((this.mSet & 4) != 0) {
            return this.mAudioFallbackMode;
        }
        throw new IllegalStateException("audio fallback mode not set");
    }

    public PlaybackParams setAudioStretchMode(int audioStretchMode) {
        this.mAudioStretchMode = audioStretchMode;
        this.mSet |= 8;
        return this;
    }

    public int getAudioStretchMode() {
        if ((this.mSet & 8) != 0) {
            return this.mAudioStretchMode;
        }
        throw new IllegalStateException("audio stretch mode not set");
    }

    public PlaybackParams setPitch(float pitch) {
        if (pitch < TonemapCurve.LEVEL_BLACK) {
            throw new IllegalArgumentException("pitch must not be negative");
        }
        this.mPitch = pitch;
        this.mSet |= 2;
        return this;
    }

    public float getPitch() {
        if ((this.mSet & 2) != 0) {
            return this.mPitch;
        }
        throw new IllegalStateException("pitch not set");
    }

    public PlaybackParams setSpeed(float speed) {
        this.mSpeed = speed;
        this.mSet |= 1;
        return this;
    }

    public float getSpeed() {
        if ((this.mSet & 1) != 0) {
            return this.mSpeed;
        }
        throw new IllegalStateException("speed not set");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSet);
        dest.writeInt(this.mAudioFallbackMode);
        dest.writeInt(this.mAudioStretchMode);
        dest.writeFloat(this.mPitch);
        dest.writeFloat(this.mSpeed);
    }
}
