package android.icu.impl.duration;

import android.icu.impl.duration.impl.PeriodFormatterDataService;
import java.util.TimeZone;

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
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class BasicPeriodBuilderFactory implements PeriodBuilderFactory {
    private static final short allBits = (short) 255;
    private PeriodFormatterDataService ds;
    private Settings settings;

    class Settings {
        boolean allowMillis;
        boolean allowZero;
        boolean inUse;
        int maxLimit;
        TimeUnit maxUnit;
        int minLimit;
        TimeUnit minUnit;
        final /* synthetic */ BasicPeriodBuilderFactory this$0;
        short uset;
        boolean weeksAloneOnly;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.<init>(android.icu.impl.duration.BasicPeriodBuilderFactory):void, dex: 
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
        Settings(android.icu.impl.duration.BasicPeriodBuilderFactory r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.<init>(android.icu.impl.duration.BasicPeriodBuilderFactory):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.<init>(android.icu.impl.duration.BasicPeriodBuilderFactory):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.copy():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
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
        public android.icu.impl.duration.BasicPeriodBuilderFactory.Settings copy() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.copy():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.copy():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.createLimited(long, boolean):android.icu.impl.duration.Period, dex: 
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
        android.icu.impl.duration.Period createLimited(long r1, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.createLimited(long, boolean):android.icu.impl.duration.Period, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.createLimited(long, boolean):android.icu.impl.duration.Period");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.effectiveMinUnit():android.icu.impl.duration.TimeUnit, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        android.icu.impl.duration.TimeUnit effectiveMinUnit() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.effectiveMinUnit():android.icu.impl.duration.TimeUnit, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.effectiveMinUnit():android.icu.impl.duration.TimeUnit");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.effectiveSet():short, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        short effectiveSet() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.effectiveSet():short, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.effectiveSet():short");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setAllowMilliseconds(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setAllowMilliseconds(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setAllowMilliseconds(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setAllowMilliseconds(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setAllowZero(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setAllowZero(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setAllowZero(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setAllowZero(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setInUse():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setInUse() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setInUse():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setInUse():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setLocale(java.lang.String):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
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
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setLocale(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setLocale(java.lang.String):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setLocale(java.lang.String):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMaxLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setMaxLimit(float r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMaxLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMaxLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMinLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex:  in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMinLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMinLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setMinLimit(float r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMinLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex:  in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMinLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setMinLimit(float):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00f2 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setUnits(int):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00f2
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setUnits(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00f2 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setUnits(int):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setUnits(int):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setWeeksAloneOnly(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        android.icu.impl.duration.BasicPeriodBuilderFactory.Settings setWeeksAloneOnly(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setWeeksAloneOnly(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.Settings.setWeeksAloneOnly(boolean):android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.-get0(android.icu.impl.duration.BasicPeriodBuilderFactory):android.icu.impl.duration.impl.PeriodFormatterDataService, dex: 
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
    /* renamed from: -get0 */
    static /* synthetic */ android.icu.impl.duration.impl.PeriodFormatterDataService m56-get0(android.icu.impl.duration.BasicPeriodBuilderFactory r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.-get0(android.icu.impl.duration.BasicPeriodBuilderFactory):android.icu.impl.duration.impl.PeriodFormatterDataService, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.-get0(android.icu.impl.duration.BasicPeriodBuilderFactory):android.icu.impl.duration.impl.PeriodFormatterDataService");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.<init>(android.icu.impl.duration.impl.PeriodFormatterDataService):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    BasicPeriodBuilderFactory(android.icu.impl.duration.impl.PeriodFormatterDataService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.<init>(android.icu.impl.duration.impl.PeriodFormatterDataService):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.<init>(android.icu.impl.duration.impl.PeriodFormatterDataService):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00f0 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.approximateDurationOf(android.icu.impl.duration.TimeUnit):long, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00f0
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static long approximateDurationOf(android.icu.impl.duration.TimeUnit r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00f0 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.approximateDurationOf(android.icu.impl.duration.TimeUnit):long, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.approximateDurationOf(android.icu.impl.duration.TimeUnit):long");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.getSettings():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
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
    private android.icu.impl.duration.BasicPeriodBuilderFactory.Settings getSettings() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.getSettings():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.getSettings():android.icu.impl.duration.BasicPeriodBuilderFactory$Settings");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setAllowMilliseconds(boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
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
    public android.icu.impl.duration.PeriodBuilderFactory setAllowMilliseconds(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setAllowMilliseconds(boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setAllowMilliseconds(boolean):android.icu.impl.duration.PeriodBuilderFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setAllowZero(boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
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
    public android.icu.impl.duration.PeriodBuilderFactory setAllowZero(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setAllowZero(boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setAllowZero(boolean):android.icu.impl.duration.PeriodBuilderFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00f0 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setAvailableUnitRange(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.TimeUnit):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00f0
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.icu.impl.duration.PeriodBuilderFactory setAvailableUnitRange(android.icu.impl.duration.TimeUnit r1, android.icu.impl.duration.TimeUnit r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00f0 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setAvailableUnitRange(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.TimeUnit):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setAvailableUnitRange(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.TimeUnit):android.icu.impl.duration.PeriodBuilderFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setLocale(java.lang.String):android.icu.impl.duration.PeriodBuilderFactory, dex: 
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
    public android.icu.impl.duration.PeriodBuilderFactory setLocale(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setLocale(java.lang.String):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setLocale(java.lang.String):android.icu.impl.duration.PeriodBuilderFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setMaxLimit(float):android.icu.impl.duration.PeriodBuilderFactory, dex: 
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
    public android.icu.impl.duration.PeriodBuilderFactory setMaxLimit(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setMaxLimit(float):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setMaxLimit(float):android.icu.impl.duration.PeriodBuilderFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setMinLimit(float):android.icu.impl.duration.PeriodBuilderFactory, dex: 
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
    public android.icu.impl.duration.PeriodBuilderFactory setMinLimit(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setMinLimit(float):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setMinLimit(float):android.icu.impl.duration.PeriodBuilderFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setUnitIsAvailable(android.icu.impl.duration.TimeUnit, boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
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
    public android.icu.impl.duration.PeriodBuilderFactory setUnitIsAvailable(android.icu.impl.duration.TimeUnit r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setUnitIsAvailable(android.icu.impl.duration.TimeUnit, boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setUnitIsAvailable(android.icu.impl.duration.TimeUnit, boolean):android.icu.impl.duration.PeriodBuilderFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setWeeksAloneOnly(boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
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
    public android.icu.impl.duration.PeriodBuilderFactory setWeeksAloneOnly(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.BasicPeriodBuilderFactory.setWeeksAloneOnly(boolean):android.icu.impl.duration.PeriodBuilderFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodBuilderFactory.setWeeksAloneOnly(boolean):android.icu.impl.duration.PeriodBuilderFactory");
    }

    public PeriodBuilderFactory setTimeZone(TimeZone timeZone) {
        return this;
    }

    public PeriodBuilder getFixedUnitBuilder(TimeUnit unit) {
        return FixedUnitBuilder.get(unit, getSettings());
    }

    public PeriodBuilder getSingleUnitBuilder() {
        return SingleUnitBuilder.get(getSettings());
    }

    public PeriodBuilder getOneOrTwoUnitBuilder() {
        return OneOrTwoUnitBuilder.get(getSettings());
    }

    public PeriodBuilder getMultiUnitBuilder(int periodCount) {
        return MultiUnitBuilder.get(periodCount, getSettings());
    }
}
