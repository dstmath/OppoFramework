package android.media.audiopolicy;

import android.media.AudioAttributes;
import android.os.Parcel;
import android.util.Log;
import java.util.ArrayList;
import java.util.Objects;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public class AudioMixingRule {
    public static final int RULE_EXCLUDE_ATTRIBUTE_CAPTURE_PRESET = 32770;
    public static final int RULE_EXCLUDE_ATTRIBUTE_USAGE = 32769;
    public static final int RULE_EXCLUDE_UID = 32772;
    private static final int RULE_EXCLUSION_MASK = 32768;
    public static final int RULE_MATCH_ATTRIBUTE_CAPTURE_PRESET = 2;
    public static final int RULE_MATCH_ATTRIBUTE_USAGE = 1;
    public static final int RULE_MATCH_UID = 4;
    private final ArrayList<AudioMixMatchCriterion> mCriteria;
    private final int mTargetMixType;

    static final class AudioMixMatchCriterion {
        final AudioAttributes mAttr;
        final int mIntProp;
        final int mRule;

        AudioMixMatchCriterion(AudioAttributes attributes, int rule) {
            this.mAttr = attributes;
            this.mIntProp = Integer.MIN_VALUE;
            this.mRule = rule;
        }

        AudioMixMatchCriterion(Integer intProp, int rule) {
            this.mAttr = null;
            this.mIntProp = intProp.intValue();
            this.mRule = rule;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mAttr, Integer.valueOf(this.mIntProp), Integer.valueOf(this.mRule)});
        }

        void writeToParcel(Parcel dest) {
            dest.writeInt(this.mRule);
            int match_rule = this.mRule & -32769;
            switch (match_rule) {
                case 1:
                    dest.writeInt(this.mAttr.getUsage());
                    return;
                case 2:
                    dest.writeInt(this.mAttr.getCapturePreset());
                    return;
                case 4:
                    dest.writeInt(this.mIntProp);
                    return;
                default:
                    Log.e("AudioMixMatchCriterion", "Unknown match rule" + match_rule + " when writing to Parcel");
                    dest.writeInt(-1);
                    return;
            }
        }
    }

    public static class Builder {
        private ArrayList<AudioMixMatchCriterion> mCriteria;
        private int mTargetMixType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.media.audiopolicy.AudioMixingRule.Builder.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public Builder() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.media.audiopolicy.AudioMixingRule.Builder.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.media.audiopolicy.AudioMixingRule.Builder.addRuleInternal(android.media.AudioAttributes, java.lang.Integer, int):android.media.audiopolicy.AudioMixingRule$Builder, dex:  in method: android.media.audiopolicy.AudioMixingRule.Builder.addRuleInternal(android.media.AudioAttributes, java.lang.Integer, int):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.media.audiopolicy.AudioMixingRule.Builder.addRuleInternal(android.media.AudioAttributes, java.lang.Integer, int):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        private android.media.audiopolicy.AudioMixingRule.Builder addRuleInternal(android.media.AudioAttributes r1, java.lang.Integer r2, int r3) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.media.audiopolicy.AudioMixingRule.Builder.addRuleInternal(android.media.AudioAttributes, java.lang.Integer, int):android.media.audiopolicy.AudioMixingRule$Builder, dex:  in method: android.media.audiopolicy.AudioMixingRule.Builder.addRuleInternal(android.media.AudioAttributes, java.lang.Integer, int):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.addRuleInternal(android.media.AudioAttributes, java.lang.Integer, int):android.media.audiopolicy.AudioMixingRule$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.checkAddRuleObjInternal(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private android.media.audiopolicy.AudioMixingRule.Builder checkAddRuleObjInternal(int r1, java.lang.Object r2) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.checkAddRuleObjInternal(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.checkAddRuleObjInternal(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.addMixRule(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.media.audiopolicy.AudioMixingRule.Builder addMixRule(int r1, java.lang.Object r2) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.addMixRule(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.addMixRule(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.addRule(android.media.AudioAttributes, int):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.media.audiopolicy.AudioMixingRule.Builder addRule(android.media.AudioAttributes r1, int r2) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.addRule(android.media.AudioAttributes, int):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.addRule(android.media.AudioAttributes, int):android.media.audiopolicy.AudioMixingRule$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.addRuleFromParcel(android.os.Parcel):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        android.media.audiopolicy.AudioMixingRule.Builder addRuleFromParcel(android.os.Parcel r1) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.addRuleFromParcel(android.os.Parcel):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.addRuleFromParcel(android.os.Parcel):android.media.audiopolicy.AudioMixingRule$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.media.audiopolicy.AudioMixingRule.Builder.build():android.media.audiopolicy.AudioMixingRule, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.media.audiopolicy.AudioMixingRule build() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.media.audiopolicy.AudioMixingRule.Builder.build():android.media.audiopolicy.AudioMixingRule, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.build():android.media.audiopolicy.AudioMixingRule");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.excludeMixRule(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.media.audiopolicy.AudioMixingRule.Builder excludeMixRule(int r1, java.lang.Object r2) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.excludeMixRule(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.excludeMixRule(int, java.lang.Object):android.media.audiopolicy.AudioMixingRule$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.excludeRule(android.media.AudioAttributes, int):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.media.audiopolicy.AudioMixingRule.Builder excludeRule(android.media.AudioAttributes r1, int r2) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMixingRule.Builder.excludeRule(android.media.AudioAttributes, int):android.media.audiopolicy.AudioMixingRule$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMixingRule.Builder.excludeRule(android.media.AudioAttributes, int):android.media.audiopolicy.AudioMixingRule$Builder");
        }
    }

    /* synthetic */ AudioMixingRule(int mixType, ArrayList criteria, AudioMixingRule audioMixingRule) {
        this(mixType, criteria);
    }

    private AudioMixingRule(int mixType, ArrayList<AudioMixMatchCriterion> criteria) {
        this.mCriteria = criteria;
        this.mTargetMixType = mixType;
    }

    int getTargetMixType() {
        return this.mTargetMixType;
    }

    ArrayList<AudioMixMatchCriterion> getCriteria() {
        return this.mCriteria;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mTargetMixType), this.mCriteria});
    }

    private static boolean isValidSystemApiRule(int rule) {
        switch (rule) {
            case 1:
            case 2:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean isValidAttributesSystemApiRule(int rule) {
        switch (rule) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    private static boolean isValidRule(int rule) {
        switch (rule & -32769) {
            case 1:
            case 2:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean isPlayerRule(int rule) {
        switch (rule & -32769) {
            case 1:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean isAudioAttributeRule(int match_rule) {
        switch (match_rule) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }
}
