package android.hardware.radio;

import android.content.Context;
import android.hardware.radio.RadioTuner.Callback;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public class RadioManager {
    public static final int BAND_AM = 0;
    public static final int BAND_AM_HD = 3;
    public static final int BAND_FM = 1;
    public static final int BAND_FM_HD = 2;
    public static final int CLASS_AM_FM = 0;
    public static final int CLASS_DT = 2;
    public static final int CLASS_SAT = 1;
    public static final int REGION_ITU_1 = 0;
    public static final int REGION_ITU_2 = 1;
    public static final int REGION_JAPAN = 3;
    public static final int REGION_KOREA = 4;
    public static final int REGION_OIRT = 2;
    public static final int STATUS_BAD_VALUE = -22;
    public static final int STATUS_DEAD_OBJECT = -32;
    public static final int STATUS_ERROR = Integer.MIN_VALUE;
    public static final int STATUS_INVALID_OPERATION = -38;
    public static final int STATUS_NO_INIT = -19;
    public static final int STATUS_OK = 0;
    public static final int STATUS_PERMISSION_DENIED = -1;
    public static final int STATUS_TIMED_OUT = -110;
    private final Context mContext;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class BandConfig implements Parcelable {
        public static final Creator<BandConfig> CREATOR = null;
        final BandDescriptor mDescriptor;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.BandConfig.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.BandConfig.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.BandConfig.<clinit>():void");
        }

        /* synthetic */ BandConfig(Parcel in, BandConfig bandConfig) {
            this(in);
        }

        BandConfig(BandDescriptor descriptor) {
            this.mDescriptor = descriptor;
        }

        BandConfig(int region, int type, int lowerLimit, int upperLimit, int spacing) {
            this.mDescriptor = new BandDescriptor(region, type, lowerLimit, upperLimit, spacing);
        }

        private BandConfig(Parcel in) {
            this.mDescriptor = new BandDescriptor(in, null);
        }

        BandDescriptor getDescriptor() {
            return this.mDescriptor;
        }

        public int getRegion() {
            return this.mDescriptor.getRegion();
        }

        public int getType() {
            return this.mDescriptor.getType();
        }

        public int getLowerLimit() {
            return this.mDescriptor.getLowerLimit();
        }

        public int getUpperLimit() {
            return this.mDescriptor.getUpperLimit();
        }

        public int getSpacing() {
            return this.mDescriptor.getSpacing();
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.mDescriptor.writeToParcel(dest, flags);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "BandConfig [ " + this.mDescriptor.toString() + "]";
        }

        public int hashCode() {
            return this.mDescriptor.hashCode() + 31;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BandConfig)) {
                return false;
            }
            return this.mDescriptor == ((BandConfig) obj).getDescriptor();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class AmBandConfig extends BandConfig {
        public static final Creator<AmBandConfig> CREATOR = null;
        private final boolean mStereo;

        public static class Builder {
            private final BandDescriptor mDescriptor;
            private boolean mStereo;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$AmBandConfig):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public Builder(android.hardware.radio.RadioManager.AmBandConfig r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$AmBandConfig):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.AmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$AmBandConfig):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$AmBandDescriptor):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public Builder(android.hardware.radio.RadioManager.AmBandDescriptor r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$AmBandDescriptor):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.AmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$AmBandDescriptor):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.build():android.hardware.radio.RadioManager$AmBandConfig, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.AmBandConfig build() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.build():android.hardware.radio.RadioManager$AmBandConfig, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.AmBandConfig.Builder.build():android.hardware.radio.RadioManager$AmBandConfig");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.setStereo(boolean):android.hardware.radio.RadioManager$AmBandConfig$Builder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.AmBandConfig.Builder setStereo(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.AmBandConfig.Builder.setStereo(boolean):android.hardware.radio.RadioManager$AmBandConfig$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.AmBandConfig.Builder.setStereo(boolean):android.hardware.radio.RadioManager$AmBandConfig$Builder");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.AmBandConfig.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.AmBandConfig.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.AmBandConfig.<clinit>():void");
        }

        /* synthetic */ AmBandConfig(Parcel in, AmBandConfig amBandConfig) {
            this(in);
        }

        AmBandConfig(AmBandDescriptor descriptor) {
            super((BandDescriptor) descriptor);
            this.mStereo = descriptor.isStereoSupported();
        }

        AmBandConfig(int region, int type, int lowerLimit, int upperLimit, int spacing, boolean stereo) {
            super(region, type, lowerLimit, upperLimit, spacing);
            this.mStereo = stereo;
        }

        public boolean getStereo() {
            return this.mStereo;
        }

        private AmBandConfig(Parcel in) {
            boolean z = true;
            super(in, null);
            if (in.readByte() != (byte) 1) {
                z = false;
            }
            this.mStereo = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (this.mStereo ? 1 : 0));
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "AmBandConfig [" + super.toString() + ", mStereo=" + this.mStereo + "]";
        }

        public int hashCode() {
            return (super.hashCode() * 31) + (this.mStereo ? 1 : 0);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || !(obj instanceof AmBandConfig)) {
                return false;
            }
            return this.mStereo == ((AmBandConfig) obj).getStereo();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class BandDescriptor implements Parcelable {
        public static final Creator<BandDescriptor> CREATOR = null;
        private final int mLowerLimit;
        private final int mRegion;
        private final int mSpacing;
        private final int mType;
        private final int mUpperLimit;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.BandDescriptor.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.BandDescriptor.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.BandDescriptor.<clinit>():void");
        }

        /* synthetic */ BandDescriptor(Parcel in, BandDescriptor bandDescriptor) {
            this(in);
        }

        BandDescriptor(int region, int type, int lowerLimit, int upperLimit, int spacing) {
            this.mRegion = region;
            this.mType = type;
            this.mLowerLimit = lowerLimit;
            this.mUpperLimit = upperLimit;
            this.mSpacing = spacing;
        }

        public int getRegion() {
            return this.mRegion;
        }

        public int getType() {
            return this.mType;
        }

        public int getLowerLimit() {
            return this.mLowerLimit;
        }

        public int getUpperLimit() {
            return this.mUpperLimit;
        }

        public int getSpacing() {
            return this.mSpacing;
        }

        private BandDescriptor(Parcel in) {
            this.mRegion = in.readInt();
            this.mType = in.readInt();
            this.mLowerLimit = in.readInt();
            this.mUpperLimit = in.readInt();
            this.mSpacing = in.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mRegion);
            dest.writeInt(this.mType);
            dest.writeInt(this.mLowerLimit);
            dest.writeInt(this.mUpperLimit);
            dest.writeInt(this.mSpacing);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "BandDescriptor [mRegion=" + this.mRegion + ", mType=" + this.mType + ", mLowerLimit=" + this.mLowerLimit + ", mUpperLimit=" + this.mUpperLimit + ", mSpacing=" + this.mSpacing + "]";
        }

        public int hashCode() {
            return ((((((((this.mRegion + 31) * 31) + this.mType) * 31) + this.mLowerLimit) * 31) + this.mUpperLimit) * 31) + this.mSpacing;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BandDescriptor)) {
                return false;
            }
            BandDescriptor other = (BandDescriptor) obj;
            return this.mRegion == other.getRegion() && this.mType == other.getType() && this.mLowerLimit == other.getLowerLimit() && this.mUpperLimit == other.getUpperLimit() && this.mSpacing == other.getSpacing();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class AmBandDescriptor extends BandDescriptor {
        public static final Creator<AmBandDescriptor> CREATOR = null;
        private final boolean mStereo;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.AmBandDescriptor.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.AmBandDescriptor.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.AmBandDescriptor.<clinit>():void");
        }

        /* synthetic */ AmBandDescriptor(Parcel in, AmBandDescriptor amBandDescriptor) {
            this(in);
        }

        AmBandDescriptor(int region, int type, int lowerLimit, int upperLimit, int spacing, boolean stereo) {
            super(region, type, lowerLimit, upperLimit, spacing);
            this.mStereo = stereo;
        }

        public boolean isStereoSupported() {
            return this.mStereo;
        }

        private AmBandDescriptor(Parcel in) {
            boolean z = true;
            super(in, null);
            if (in.readByte() != (byte) 1) {
                z = false;
            }
            this.mStereo = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (this.mStereo ? 1 : 0));
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "AmBandDescriptor [ " + super.toString() + " mStereo=" + this.mStereo + "]";
        }

        public int hashCode() {
            return (super.hashCode() * 31) + (this.mStereo ? 1 : 0);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || !(obj instanceof AmBandDescriptor)) {
                return false;
            }
            return this.mStereo == ((AmBandDescriptor) obj).isStereoSupported();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class FmBandConfig extends BandConfig {
        public static final Creator<FmBandConfig> CREATOR = null;
        private final boolean mAf;
        private final boolean mEa;
        private final boolean mRds;
        private final boolean mStereo;
        private final boolean mTa;

        public static class Builder {
            private boolean mAf;
            private final BandDescriptor mDescriptor;
            private boolean mEa;
            private boolean mRds;
            private boolean mStereo;
            private boolean mTa;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$FmBandConfig):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public Builder(android.hardware.radio.RadioManager.FmBandConfig r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$FmBandConfig):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$FmBandConfig):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$FmBandDescriptor):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public Builder(android.hardware.radio.RadioManager.FmBandDescriptor r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$FmBandDescriptor):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.<init>(android.hardware.radio.RadioManager$FmBandDescriptor):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.build():android.hardware.radio.RadioManager$FmBandConfig, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.FmBandConfig build() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.build():android.hardware.radio.RadioManager$FmBandConfig, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.build():android.hardware.radio.RadioManager$FmBandConfig");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setAf(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.FmBandConfig.Builder setAf(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setAf(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.setAf(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setEa(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.FmBandConfig.Builder setEa(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setEa(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.setEa(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setRds(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.FmBandConfig.Builder setRds(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setRds(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.setRds(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setStereo(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.FmBandConfig.Builder setStereo(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setStereo(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.setStereo(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setTa(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.hardware.radio.RadioManager.FmBandConfig.Builder setTa(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.hardware.radio.RadioManager.FmBandConfig.Builder.setTa(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.Builder.setTa(boolean):android.hardware.radio.RadioManager$FmBandConfig$Builder");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.FmBandConfig.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.FmBandConfig.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandConfig.<clinit>():void");
        }

        /* synthetic */ FmBandConfig(Parcel in, FmBandConfig fmBandConfig) {
            this(in);
        }

        FmBandConfig(FmBandDescriptor descriptor) {
            super((BandDescriptor) descriptor);
            this.mStereo = descriptor.isStereoSupported();
            this.mRds = descriptor.isRdsSupported();
            this.mTa = descriptor.isTaSupported();
            this.mAf = descriptor.isAfSupported();
            this.mEa = descriptor.isEaSupported();
        }

        FmBandConfig(int region, int type, int lowerLimit, int upperLimit, int spacing, boolean stereo, boolean rds, boolean ta, boolean af, boolean ea) {
            super(region, type, lowerLimit, upperLimit, spacing);
            this.mStereo = stereo;
            this.mRds = rds;
            this.mTa = ta;
            this.mAf = af;
            this.mEa = ea;
        }

        public boolean getStereo() {
            return this.mStereo;
        }

        public boolean getRds() {
            return this.mRds;
        }

        public boolean getTa() {
            return this.mTa;
        }

        public boolean getAf() {
            return this.mAf;
        }

        public boolean getEa() {
            return this.mEa;
        }

        private FmBandConfig(Parcel in) {
            boolean z;
            boolean z2 = true;
            super(in, null);
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mStereo = z;
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mRds = z;
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mTa = z;
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mAf = z;
            if (in.readByte() != (byte) 1) {
                z2 = false;
            }
            this.mEa = z2;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            super.writeToParcel(dest, flags);
            if (this.mStereo) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mRds) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mTa) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mAf) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (!this.mEa) {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "FmBandConfig [" + super.toString() + ", mStereo=" + this.mStereo + ", mRds=" + this.mRds + ", mTa=" + this.mTa + ", mAf=" + this.mAf + ", mEa =" + this.mEa + "]";
        }

        public int hashCode() {
            int i;
            int i2 = 1;
            int hashCode = ((super.hashCode() * 31) + (this.mStereo ? 1 : 0)) * 31;
            if (this.mRds) {
                i = 1;
            } else {
                i = 0;
            }
            hashCode = (hashCode + i) * 31;
            if (this.mTa) {
                i = 1;
            } else {
                i = 0;
            }
            hashCode = (hashCode + i) * 31;
            if (this.mAf) {
                i = 1;
            } else {
                i = 0;
            }
            i = (hashCode + i) * 31;
            if (!this.mEa) {
                i2 = 0;
            }
            return i + i2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || !(obj instanceof FmBandConfig)) {
                return false;
            }
            FmBandConfig other = (FmBandConfig) obj;
            return this.mStereo == other.mStereo && this.mRds == other.mRds && this.mTa == other.mTa && this.mAf == other.mAf && this.mEa == other.mEa;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class FmBandDescriptor extends BandDescriptor {
        public static final Creator<FmBandDescriptor> CREATOR = null;
        private final boolean mAf;
        private final boolean mEa;
        private final boolean mRds;
        private final boolean mStereo;
        private final boolean mTa;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.FmBandDescriptor.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.FmBandDescriptor.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.FmBandDescriptor.<clinit>():void");
        }

        /* synthetic */ FmBandDescriptor(Parcel in, FmBandDescriptor fmBandDescriptor) {
            this(in);
        }

        FmBandDescriptor(int region, int type, int lowerLimit, int upperLimit, int spacing, boolean stereo, boolean rds, boolean ta, boolean af, boolean ea) {
            super(region, type, lowerLimit, upperLimit, spacing);
            this.mStereo = stereo;
            this.mRds = rds;
            this.mTa = ta;
            this.mAf = af;
            this.mEa = ea;
        }

        public boolean isStereoSupported() {
            return this.mStereo;
        }

        public boolean isRdsSupported() {
            return this.mRds;
        }

        public boolean isTaSupported() {
            return this.mTa;
        }

        public boolean isAfSupported() {
            return this.mAf;
        }

        public boolean isEaSupported() {
            return this.mEa;
        }

        private FmBandDescriptor(Parcel in) {
            boolean z;
            boolean z2 = true;
            super(in, null);
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mStereo = z;
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mRds = z;
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mTa = z;
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mAf = z;
            if (in.readByte() != (byte) 1) {
                z2 = false;
            }
            this.mEa = z2;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            super.writeToParcel(dest, flags);
            if (this.mStereo) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mRds) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mTa) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mAf) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (!this.mEa) {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "FmBandDescriptor [ " + super.toString() + " mStereo=" + this.mStereo + ", mRds=" + this.mRds + ", mTa=" + this.mTa + ", mAf=" + this.mAf + ", mEa =" + this.mEa + "]";
        }

        public int hashCode() {
            int i;
            int i2 = 1;
            int hashCode = ((super.hashCode() * 31) + (this.mStereo ? 1 : 0)) * 31;
            if (this.mRds) {
                i = 1;
            } else {
                i = 0;
            }
            hashCode = (hashCode + i) * 31;
            if (this.mTa) {
                i = 1;
            } else {
                i = 0;
            }
            hashCode = (hashCode + i) * 31;
            if (this.mAf) {
                i = 1;
            } else {
                i = 0;
            }
            i = (hashCode + i) * 31;
            if (!this.mEa) {
                i2 = 0;
            }
            return i + i2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || !(obj instanceof FmBandDescriptor)) {
                return false;
            }
            FmBandDescriptor other = (FmBandDescriptor) obj;
            return this.mStereo == other.isStereoSupported() && this.mRds == other.isRdsSupported() && this.mTa == other.isTaSupported() && this.mAf == other.isAfSupported() && this.mEa == other.isEaSupported();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class ModuleProperties implements Parcelable {
        public static final Creator<ModuleProperties> CREATOR = null;
        private final BandDescriptor[] mBands;
        private final int mClassId;
        private final int mId;
        private final String mImplementor;
        private final boolean mIsCaptureSupported;
        private final int mNumAudioSources;
        private final int mNumTuners;
        private final String mProduct;
        private final String mSerial;
        private final String mVersion;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.ModuleProperties.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.ModuleProperties.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.ModuleProperties.<clinit>():void");
        }

        /* synthetic */ ModuleProperties(Parcel in, ModuleProperties moduleProperties) {
            this(in);
        }

        ModuleProperties(int id, int classId, String implementor, String product, String version, String serial, int numTuners, int numAudioSources, boolean isCaptureSupported, BandDescriptor[] bands) {
            this.mId = id;
            this.mClassId = classId;
            this.mImplementor = implementor;
            this.mProduct = product;
            this.mVersion = version;
            this.mSerial = serial;
            this.mNumTuners = numTuners;
            this.mNumAudioSources = numAudioSources;
            this.mIsCaptureSupported = isCaptureSupported;
            this.mBands = bands;
        }

        public int getId() {
            return this.mId;
        }

        public int getClassId() {
            return this.mClassId;
        }

        public String getImplementor() {
            return this.mImplementor;
        }

        public String getProduct() {
            return this.mProduct;
        }

        public String getVersion() {
            return this.mVersion;
        }

        public String getSerial() {
            return this.mSerial;
        }

        public int getNumTuners() {
            return this.mNumTuners;
        }

        public int getNumAudioSources() {
            return this.mNumAudioSources;
        }

        public boolean isCaptureSupported() {
            return this.mIsCaptureSupported;
        }

        public BandDescriptor[] getBands() {
            return this.mBands;
        }

        private ModuleProperties(Parcel in) {
            boolean z = true;
            this.mId = in.readInt();
            this.mClassId = in.readInt();
            this.mImplementor = in.readString();
            this.mProduct = in.readString();
            this.mVersion = in.readString();
            this.mSerial = in.readString();
            this.mNumTuners = in.readInt();
            this.mNumAudioSources = in.readInt();
            if (in.readInt() != 1) {
                z = false;
            }
            this.mIsCaptureSupported = z;
            Parcelable[] tmp = in.readParcelableArray(BandDescriptor.class.getClassLoader());
            this.mBands = new BandDescriptor[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                this.mBands[i] = (BandDescriptor) tmp[i];
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mId);
            dest.writeInt(this.mClassId);
            dest.writeString(this.mImplementor);
            dest.writeString(this.mProduct);
            dest.writeString(this.mVersion);
            dest.writeString(this.mSerial);
            dest.writeInt(this.mNumTuners);
            dest.writeInt(this.mNumAudioSources);
            dest.writeInt(this.mIsCaptureSupported ? 1 : 0);
            dest.writeParcelableArray(this.mBands, flags);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "ModuleProperties [mId=" + this.mId + ", mClassId=" + this.mClassId + ", mImplementor=" + this.mImplementor + ", mProduct=" + this.mProduct + ", mVersion=" + this.mVersion + ", mSerial=" + this.mSerial + ", mNumTuners=" + this.mNumTuners + ", mNumAudioSources=" + this.mNumAudioSources + ", mIsCaptureSupported=" + this.mIsCaptureSupported + ", mBands=" + Arrays.toString(this.mBands) + "]";
        }

        public int hashCode() {
            int i = 0;
            int hashCode = (((((((((((((((this.mId + 31) * 31) + this.mClassId) * 31) + (this.mImplementor == null ? 0 : this.mImplementor.hashCode())) * 31) + (this.mProduct == null ? 0 : this.mProduct.hashCode())) * 31) + (this.mVersion == null ? 0 : this.mVersion.hashCode())) * 31) + (this.mSerial == null ? 0 : this.mSerial.hashCode())) * 31) + this.mNumTuners) * 31) + this.mNumAudioSources) * 31;
            if (this.mIsCaptureSupported) {
                i = 1;
            }
            return ((hashCode + i) * 31) + Arrays.hashCode(this.mBands);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ModuleProperties)) {
                return false;
            }
            ModuleProperties other = (ModuleProperties) obj;
            if (this.mId != other.getId() || this.mClassId != other.getClassId()) {
                return false;
            }
            if (this.mImplementor == null) {
                if (other.getImplementor() != null) {
                    return false;
                }
            } else if (!this.mImplementor.equals(other.getImplementor())) {
                return false;
            }
            if (this.mProduct == null) {
                if (other.getProduct() != null) {
                    return false;
                }
            } else if (!this.mProduct.equals(other.getProduct())) {
                return false;
            }
            if (this.mVersion == null) {
                if (other.getVersion() != null) {
                    return false;
                }
            } else if (!this.mVersion.equals(other.getVersion())) {
                return false;
            }
            if (this.mSerial == null) {
                if (other.getSerial() != null) {
                    return false;
                }
            } else if (!this.mSerial.equals(other.getSerial())) {
                return false;
            }
            return this.mNumTuners == other.getNumTuners() && this.mNumAudioSources == other.getNumAudioSources() && this.mIsCaptureSupported == other.isCaptureSupported() && Arrays.equals(this.mBands, other.getBands());
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class ProgramInfo implements Parcelable {
        public static final Creator<ProgramInfo> CREATOR = null;
        private final int mChannel;
        private final boolean mDigital;
        private final RadioMetadata mMetadata;
        private final int mSignalStrength;
        private final boolean mStereo;
        private final int mSubChannel;
        private final boolean mTuned;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.ProgramInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.radio.RadioManager.ProgramInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioManager.ProgramInfo.<clinit>():void");
        }

        /* synthetic */ ProgramInfo(Parcel in, ProgramInfo programInfo) {
            this(in);
        }

        ProgramInfo(int channel, int subChannel, boolean tuned, boolean stereo, boolean digital, int signalStrength, RadioMetadata metadata) {
            this.mChannel = channel;
            this.mSubChannel = subChannel;
            this.mTuned = tuned;
            this.mStereo = stereo;
            this.mDigital = digital;
            this.mSignalStrength = signalStrength;
            this.mMetadata = metadata;
        }

        public int getChannel() {
            return this.mChannel;
        }

        public int getSubChannel() {
            return this.mSubChannel;
        }

        public boolean isTuned() {
            return this.mTuned;
        }

        public boolean isStereo() {
            return this.mStereo;
        }

        public boolean isDigital() {
            return this.mDigital;
        }

        public int getSignalStrength() {
            return this.mSignalStrength;
        }

        public RadioMetadata getMetadata() {
            return this.mMetadata;
        }

        private ProgramInfo(Parcel in) {
            boolean z;
            boolean z2 = false;
            this.mChannel = in.readInt();
            this.mSubChannel = in.readInt();
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mTuned = z;
            if (in.readByte() == (byte) 1) {
                z = true;
            } else {
                z = false;
            }
            this.mStereo = z;
            if (in.readByte() == (byte) 1) {
                z2 = true;
            }
            this.mDigital = z2;
            this.mSignalStrength = in.readInt();
            if (in.readByte() == (byte) 1) {
                this.mMetadata = (RadioMetadata) RadioMetadata.CREATOR.createFromParcel(in);
            } else {
                this.mMetadata = null;
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(this.mChannel);
            dest.writeInt(this.mSubChannel);
            if (this.mTuned) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mStereo) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.mDigital) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            dest.writeInt(this.mSignalStrength);
            if (this.mMetadata == null) {
                dest.writeByte((byte) 0);
                return;
            }
            dest.writeByte((byte) 1);
            this.mMetadata.writeToParcel(dest, flags);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "ProgramInfo [mChannel=" + this.mChannel + ", mSubChannel=" + this.mSubChannel + ", mTuned=" + this.mTuned + ", mStereo=" + this.mStereo + ", mDigital=" + this.mDigital + ", mSignalStrength=" + this.mSignalStrength + (this.mMetadata == null ? "" : ", mMetadata=" + this.mMetadata.toString()) + "]";
        }

        public int hashCode() {
            int i;
            int i2 = 1;
            int i3 = 0;
            int i4 = (((((this.mChannel + 31) * 31) + this.mSubChannel) * 31) + (this.mTuned ? 1 : 0)) * 31;
            if (this.mStereo) {
                i = 1;
            } else {
                i = 0;
            }
            i = (i4 + i) * 31;
            if (!this.mDigital) {
                i2 = 0;
            }
            i = (((i + i2) * 31) + this.mSignalStrength) * 31;
            if (this.mMetadata != null) {
                i3 = this.mMetadata.hashCode();
            }
            return i + i3;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ProgramInfo)) {
                return false;
            }
            ProgramInfo other = (ProgramInfo) obj;
            if (this.mChannel != other.getChannel() || this.mSubChannel != other.getSubChannel() || this.mTuned != other.isTuned() || this.mStereo != other.isStereo() || this.mDigital != other.isDigital() || this.mSignalStrength != other.getSignalStrength()) {
                return false;
            }
            if (this.mMetadata == null) {
                if (other.getMetadata() != null) {
                    return false;
                }
            } else if (!this.mMetadata.equals(other.getMetadata())) {
                return false;
            }
            return true;
        }
    }

    public native int listModules(List<ModuleProperties> list);

    public RadioTuner openTuner(int moduleId, BandConfig config, boolean withAudio, Callback callback, Handler handler) {
        if (callback == null) {
            return null;
        }
        RadioModule module = new RadioModule(moduleId, config, withAudio, callback, handler);
        if (!(module == null || module.initCheck())) {
            module = null;
        }
        return module;
    }

    public RadioManager(Context context) {
        this.mContext = context;
    }
}
