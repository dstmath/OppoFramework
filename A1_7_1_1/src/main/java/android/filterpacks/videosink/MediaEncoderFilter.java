package android.filterpacks.videosink;

import android.filterfw.core.Filter;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.ShaderProgram;
import android.filterfw.geometry.Quad;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import java.io.FileDescriptor;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class MediaEncoderFilter extends Filter {
    private static final int NO_AUDIO_SOURCE = -1;
    private static final String TAG = "MediaEncoderFilter";
    @GenerateFieldPort(hasDefault = true, name = "audioSource")
    private int mAudioSource;
    @GenerateFieldPort(hasDefault = true, name = "releasedListener")
    private OnInfoListener mCameraReleasedListener;
    private boolean mCaptureTimeLapse;
    @GenerateFieldPort(hasDefault = true, name = "errorListener")
    private OnErrorListener mErrorListener;
    @GenerateFieldPort(hasDefault = true, name = "outputFileDescriptor")
    private FileDescriptor mFd;
    @GenerateFieldPort(hasDefault = true, name = "framerate")
    private int mFps;
    @GenerateFieldPort(hasDefault = true, name = "height")
    private int mHeight;
    @GenerateFieldPort(hasDefault = true, name = "infoListener")
    private OnInfoListener mInfoListener;
    private long mLastTimeLapseFrameRealTimestampNs;
    private boolean mLogVerbose;
    @GenerateFieldPort(hasDefault = true, name = "maxDurationMs")
    private int mMaxDurationMs;
    @GenerateFieldPort(hasDefault = true, name = "maxFileSize")
    private long mMaxFileSize;
    private MediaRecorder mMediaRecorder;
    @GenerateFieldPort(hasDefault = true, name = "muteAudio")
    private boolean mMuteAudio;
    private int mNumFramesEncoded;
    @GenerateFieldPort(hasDefault = true, name = "orientationHint")
    private int mOrientationHint;
    @GenerateFieldPort(hasDefault = true, name = "outputFile")
    private String mOutputFile;
    @GenerateFieldPort(hasDefault = true, name = "outputFormat")
    private int mOutputFormat;
    @GenerateFieldPort(hasDefault = true, name = "recordingProfile")
    private CamcorderProfile mProfile;
    private ShaderProgram mProgram;
    @GenerateFieldPort(hasDefault = true, name = "recording")
    private boolean mRecording;
    private boolean mRecordingActive;
    @GenerateFieldPort(hasDefault = true, name = "recordingDoneListener")
    private OnRecordingDoneListener mRecordingDoneListener;
    private GLFrame mScreen;
    @GenerateFieldPort(hasDefault = true, name = "inputRegion")
    private Quad mSourceRegion;
    private int mSurfaceId;
    @GenerateFieldPort(hasDefault = true, name = "timelapseRecordingIntervalUs")
    private long mTimeBetweenTimeLapseFrameCaptureUs;
    private long mTimestampNs;
    @GenerateFieldPort(hasDefault = true, name = "videoEncoder")
    private int mVideoEncoder;
    @GenerateFieldPort(hasDefault = true, name = "width")
    private int mWidth;
    private long recorderStartTimestampNs;

    public interface OnRecordingDoneListener {
        void onRecordingDone();
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.filterpacks.videosink.MediaEncoderFilter.<init>(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public MediaEncoderFilter(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.filterpacks.videosink.MediaEncoderFilter.<init>(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.<init>(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.startRecording(android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void startRecording(android.filterfw.core.FilterContext r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.startRecording(android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.startRecording(android.filterfw.core.FilterContext):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.stopRecording(android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void stopRecording(android.filterfw.core.FilterContext r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.stopRecording(android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.stopRecording(android.filterfw.core.FilterContext):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.filterpacks.videosink.MediaEncoderFilter.updateMediaRecorderParams():void, dex:  in method: android.filterpacks.videosink.MediaEncoderFilter.updateMediaRecorderParams():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.filterpacks.videosink.MediaEncoderFilter.updateMediaRecorderParams():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: f
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void updateMediaRecorderParams() {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.filterpacks.videosink.MediaEncoderFilter.updateMediaRecorderParams():void, dex:  in method: android.filterpacks.videosink.MediaEncoderFilter.updateMediaRecorderParams():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.updateMediaRecorderParams():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.filterpacks.videosink.MediaEncoderFilter.updateSourceRegion():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void updateSourceRegion() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.filterpacks.videosink.MediaEncoderFilter.updateSourceRegion():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.updateSourceRegion():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.close(android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void close(android.filterfw.core.FilterContext r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.close(android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.close(android.filterfw.core.FilterContext):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.fieldPortValueUpdated(java.lang.String, android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void fieldPortValueUpdated(java.lang.String r1, android.filterfw.core.FilterContext r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.fieldPortValueUpdated(java.lang.String, android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.fieldPortValueUpdated(java.lang.String, android.filterfw.core.FilterContext):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.open(android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void open(android.filterfw.core.FilterContext r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.open(android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.open(android.filterfw.core.FilterContext):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.prepare(android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void prepare(android.filterfw.core.FilterContext r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.prepare(android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.prepare(android.filterfw.core.FilterContext):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.filterpacks.videosink.MediaEncoderFilter.process(android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void process(android.filterfw.core.FilterContext r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.filterpacks.videosink.MediaEncoderFilter.process(android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.process(android.filterfw.core.FilterContext):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.filterpacks.videosink.MediaEncoderFilter.setupPorts():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setupPorts() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.filterpacks.videosink.MediaEncoderFilter.setupPorts():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.setupPorts():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.skipFrameAndModifyTimestamp(long):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean skipFrameAndModifyTimestamp(long r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.filterpacks.videosink.MediaEncoderFilter.skipFrameAndModifyTimestamp(long):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.skipFrameAndModifyTimestamp(long):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.filterpacks.videosink.MediaEncoderFilter.tearDown(android.filterfw.core.FilterContext):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void tearDown(android.filterfw.core.FilterContext r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.filterpacks.videosink.MediaEncoderFilter.tearDown(android.filterfw.core.FilterContext):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosink.MediaEncoderFilter.tearDown(android.filterfw.core.FilterContext):void");
    }
}
