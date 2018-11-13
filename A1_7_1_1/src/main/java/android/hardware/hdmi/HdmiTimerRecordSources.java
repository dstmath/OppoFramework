package android.hardware.hdmi;

import android.hardware.hdmi.HdmiRecordSources.AnalogueServiceSource;
import android.hardware.hdmi.HdmiRecordSources.DigitalServiceSource;
import android.hardware.hdmi.HdmiRecordSources.ExternalPhysicalAddress;
import android.hardware.hdmi.HdmiRecordSources.ExternalPlugData;
import android.hardware.hdmi.HdmiRecordSources.RecordSource;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    */
public class HdmiTimerRecordSources {
    private static final int EXTERNAL_SOURCE_SPECIFIER_EXTERNAL_PHYSICAL_ADDRESS = 5;
    private static final int EXTERNAL_SOURCE_SPECIFIER_EXTERNAL_PLUG = 4;
    public static final int RECORDING_SEQUENCE_REPEAT_FRIDAY = 32;
    private static final int RECORDING_SEQUENCE_REPEAT_MASK = 127;
    public static final int RECORDING_SEQUENCE_REPEAT_MONDAY = 2;
    public static final int RECORDING_SEQUENCE_REPEAT_ONCE_ONLY = 0;
    public static final int RECORDING_SEQUENCE_REPEAT_SATUREDAY = 64;
    public static final int RECORDING_SEQUENCE_REPEAT_SUNDAY = 1;
    public static final int RECORDING_SEQUENCE_REPEAT_THURSDAY = 16;
    public static final int RECORDING_SEQUENCE_REPEAT_TUESDAY = 4;
    public static final int RECORDING_SEQUENCE_REPEAT_WEDNESDAY = 8;
    private static final String TAG = "HdmiTimerRecordingSources";

    static class TimeUnit {
        final int mHour;
        final int mMinute;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimeUnit.<init>(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        TimeUnit(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimeUnit.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.TimeUnit.<init>(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimeUnit.toByteArray(byte[], int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        int toByteArray(byte[] r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimeUnit.toByteArray(byte[], int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.TimeUnit.toByteArray(byte[], int):int");
        }

        static byte toBcdByte(int value) {
            return (byte) ((((value / 10) % 10) << 4) | (value % 10));
        }
    }

    public static final class Duration extends TimeUnit {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Duration.<init>(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private Duration(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Duration.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.Duration.<init>(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Duration.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Duration):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* synthetic */ Duration(int r1, int r2, android.hardware.hdmi.HdmiTimerRecordSources.Duration r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Duration.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Duration):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.Duration.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Duration):void");
        }
    }

    private static class ExternalSourceDecorator extends RecordSource {
        private final int mExternalSourceSpecifier;
        private final RecordSource mRecordSource;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.<init>(android.hardware.hdmi.HdmiRecordSources$RecordSource, int):void, dex:  in method: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.<init>(android.hardware.hdmi.HdmiRecordSources$RecordSource, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.<init>(android.hardware.hdmi.HdmiRecordSources$RecordSource, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private ExternalSourceDecorator(android.hardware.hdmi.HdmiRecordSources.RecordSource r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.<init>(android.hardware.hdmi.HdmiRecordSources$RecordSource, int):void, dex:  in method: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.<init>(android.hardware.hdmi.HdmiRecordSources$RecordSource, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.<init>(android.hardware.hdmi.HdmiRecordSources$RecordSource, int):void");
        }

        /* synthetic */ ExternalSourceDecorator(RecordSource recordSource, int externalSourceSpecifier, ExternalSourceDecorator externalSourceDecorator) {
            this(recordSource, externalSourceSpecifier);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.extraParamToByteArray(byte[], int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        int extraParamToByteArray(byte[] r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.extraParamToByteArray(byte[], int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.ExternalSourceDecorator.extraParamToByteArray(byte[], int):int");
        }
    }

    public static final class Time extends TimeUnit {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Time.<init>(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private Time(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Time.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.Time.<init>(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Time.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* synthetic */ Time(int r1, int r2, android.hardware.hdmi.HdmiTimerRecordSources.Time r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.Time.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.Time.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time):void");
        }
    }

    public static final class TimerInfo {
        private static final int BASIC_INFO_SIZE = 7;
        private static final int DAY_OF_MONTH_SIZE = 1;
        private static final int DURATION_SIZE = 2;
        private static final int MONTH_OF_YEAR_SIZE = 1;
        private static final int RECORDING_SEQUENCE_SIZE = 1;
        private static final int START_TIME_SIZE = 2;
        private final int mDayOfMonth;
        private final Duration mDuration;
        private final int mMonthOfYear;
        private final int mRecordingSequence;
        private final Time mStartTime;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):void, dex:  in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private TimerInfo(int r1, int r2, android.hardware.hdmi.HdmiTimerRecordSources.Time r3, android.hardware.hdmi.HdmiTimerRecordSources.Duration r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):void, dex:  in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.<init>(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):void");
        }

        /* synthetic */ TimerInfo(int dayOfMonth, int monthOfYear, Time startTime, Duration duration, int recordingSequence, TimerInfo timerInfo) {
            this(dayOfMonth, monthOfYear, startTime, duration, recordingSequence);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.toByteArray(byte[], int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        int toByteArray(byte[] r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.toByteArray(byte[], int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo.toByteArray(byte[], int):int");
        }

        int getDataSize() {
            return 7;
        }
    }

    public static final class TimerRecordSource {
        private final RecordSource mRecordSource;
        private final TimerInfo mTimerInfo;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.<init>(android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, android.hardware.hdmi.HdmiRecordSources$RecordSource):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private TimerRecordSource(android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo r1, android.hardware.hdmi.HdmiRecordSources.RecordSource r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.<init>(android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, android.hardware.hdmi.HdmiRecordSources$RecordSource):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.<init>(android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, android.hardware.hdmi.HdmiRecordSources$RecordSource):void");
        }

        /* synthetic */ TimerRecordSource(TimerInfo timerInfo, RecordSource recordSource, TimerRecordSource timerRecordSource) {
            this(timerInfo, recordSource);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.getDataSize():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        int getDataSize() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.getDataSize():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.getDataSize():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.toByteArray(byte[], int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        int toByteArray(byte[] r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.toByteArray(byte[], int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource.toByteArray(byte[], int):int");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private HdmiTimerRecordSources() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.hdmi.HdmiTimerRecordSources.checkDurationValue(int, int):void, dex: 
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
    private static void checkDurationValue(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.hdmi.HdmiTimerRecordSources.checkDurationValue(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.checkDurationValue(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.hdmi.HdmiTimerRecordSources.checkTimeValue(int, int):void, dex: 
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
    private static void checkTimeValue(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.hdmi.HdmiTimerRecordSources.checkTimeValue(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.checkTimeValue(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.checkTimerRecordSourceInputs(android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, android.hardware.hdmi.HdmiRecordSources$RecordSource):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static void checkTimerRecordSourceInputs(android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo r1, android.hardware.hdmi.HdmiRecordSources.RecordSource r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.hdmi.HdmiTimerRecordSources.checkTimerRecordSourceInputs(android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, android.hardware.hdmi.HdmiRecordSources$RecordSource):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.checkTimerRecordSourceInputs(android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, android.hardware.hdmi.HdmiRecordSources$RecordSource):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.hdmi.HdmiTimerRecordSources.timerInfoOf(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, dex: 
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
    public static android.hardware.hdmi.HdmiTimerRecordSources.TimerInfo timerInfoOf(int r1, int r2, android.hardware.hdmi.HdmiTimerRecordSources.Time r3, android.hardware.hdmi.HdmiTimerRecordSources.Duration r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.hdmi.HdmiTimerRecordSources.timerInfoOf(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiTimerRecordSources.timerInfoOf(int, int, android.hardware.hdmi.HdmiTimerRecordSources$Time, android.hardware.hdmi.HdmiTimerRecordSources$Duration, int):android.hardware.hdmi.HdmiTimerRecordSources$TimerInfo");
    }

    public static TimerRecordSource ofDigitalSource(TimerInfo timerInfo, DigitalServiceSource source) {
        checkTimerRecordSourceInputs(timerInfo, source);
        return new TimerRecordSource(timerInfo, source, null);
    }

    public static TimerRecordSource ofAnalogueSource(TimerInfo timerInfo, AnalogueServiceSource source) {
        checkTimerRecordSourceInputs(timerInfo, source);
        return new TimerRecordSource(timerInfo, source, null);
    }

    public static TimerRecordSource ofExternalPlug(TimerInfo timerInfo, ExternalPlugData source) {
        checkTimerRecordSourceInputs(timerInfo, source);
        return new TimerRecordSource(timerInfo, new ExternalSourceDecorator(source, 4, null), null);
    }

    public static TimerRecordSource ofExternalPhysicalAddress(TimerInfo timerInfo, ExternalPhysicalAddress source) {
        checkTimerRecordSourceInputs(timerInfo, source);
        return new TimerRecordSource(timerInfo, new ExternalSourceDecorator(source, 5, null), null);
    }

    public static Time timeOf(int hour, int minute) {
        checkTimeValue(hour, minute);
        return new Time(hour, minute, null);
    }

    public static Duration durationOf(int hour, int minute) {
        checkDurationValue(hour, minute);
        return new Duration(hour, minute, null);
    }

    public static boolean checkTimerRecordSource(int sourcetype, byte[] recordSource) {
        boolean z = true;
        int recordSourceSize = recordSource.length - 7;
        switch (sourcetype) {
            case 1:
                if (7 != recordSourceSize) {
                    z = false;
                }
                return z;
            case 2:
                if (4 != recordSourceSize) {
                    z = false;
                }
                return z;
            case 3:
                int specifier = recordSource[7];
                if (specifier == 4) {
                    if (2 != recordSourceSize) {
                        z = false;
                    }
                    return z;
                } else if (specifier != 5) {
                    return false;
                } else {
                    if (3 != recordSourceSize) {
                        z = false;
                    }
                    return z;
                }
            default:
                return false;
        }
    }
}
