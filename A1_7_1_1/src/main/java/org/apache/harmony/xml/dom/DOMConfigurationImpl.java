package org.apache.harmony.xml.dom;

import java.util.Map;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMStringList;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
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
public final class DOMConfigurationImpl implements DOMConfiguration {
    private static final Map<String, Parameter> PARAMETERS = null;
    private boolean cdataSections;
    private boolean comments;
    private boolean datatypeNormalization;
    private boolean entities;
    private DOMErrorHandler errorHandler;
    private boolean namespaces;
    private String schemaLocation;
    private String schemaType;
    private boolean splitCdataSections;
    private boolean validate;
    private boolean wellFormed;

    interface Parameter {
        boolean canSet(DOMConfigurationImpl dOMConfigurationImpl, Object obj);

        Object get(DOMConfigurationImpl dOMConfigurationImpl);

        void set(DOMConfigurationImpl dOMConfigurationImpl, Object obj);
    }

    static abstract class BooleanParameter implements Parameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.BooleanParameter.<init>():void, dex: 
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
        BooleanParameter() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.BooleanParameter.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.BooleanParameter.<init>():void");
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value instanceof Boolean;
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$13 */
    class AnonymousClass13 implements DOMStringList {
        final /* synthetic */ DOMConfigurationImpl this$0;
        final /* synthetic */ String[] val$result;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.<init>(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String[]):void, dex: 
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
        AnonymousClass13(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.String[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.<init>(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.<init>(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.getLength():int, dex: 
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
        public int getLength() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.getLength():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.getLength():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.item(int):java.lang.String, dex: 
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
        public java.lang.String item(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.item(int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.13.item(int):java.lang.String");
        }

        public boolean contains(String str) {
            return DOMConfigurationImpl.PARAMETERS.containsKey(str);
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$1 */
    static class AnonymousClass1 extends BooleanParameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.1.<init>():void, dex: 
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
        AnonymousClass1() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.1.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.1.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.1.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.1.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.1.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return Boolean.valueOf(DOMConfigurationImpl.m135-get1(config));
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$2 */
    static class AnonymousClass2 extends BooleanParameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.2.<init>():void, dex: 
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
        AnonymousClass2() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.2.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.2.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.2.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.2.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.2.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return Boolean.valueOf(DOMConfigurationImpl.m138-get2(config));
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$3 */
    static class AnonymousClass3 extends BooleanParameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.3.<init>():void, dex: 
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
        AnonymousClass3() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.3.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.3.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.3.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.3.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.3.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return Boolean.valueOf(DOMConfigurationImpl.m139-get3(config));
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$4 */
    static class AnonymousClass4 extends BooleanParameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.4.<init>():void, dex: 
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
        AnonymousClass4() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.4.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.4.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.4.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.4.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.4.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return Boolean.valueOf(DOMConfigurationImpl.m140-get4(config));
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$5 */
    static class AnonymousClass5 implements Parameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.5.<init>():void, dex: 
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
        AnonymousClass5() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.5.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.5.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.5.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
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
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.5.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.5.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return DOMConfigurationImpl.m141-get5(config);
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value != null ? value instanceof DOMErrorHandler : true;
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$6 */
    static class AnonymousClass6 extends BooleanParameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.6.<init>():void, dex: 
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
        AnonymousClass6() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.6.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.6.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.6.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.6.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.6.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            boolean z;
            if (DOMConfigurationImpl.m140-get4(config) || DOMConfigurationImpl.m139-get3(config) || DOMConfigurationImpl.m135-get1(config) || !DOMConfigurationImpl.m137-get11(config) || !DOMConfigurationImpl.m138-get2(config)) {
                z = false;
            } else {
                z = DOMConfigurationImpl.m142-get6(config);
            }
            return Boolean.valueOf(z);
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$7 */
    static class AnonymousClass7 extends BooleanParameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.7.<init>():void, dex: 
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
        AnonymousClass7() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.7.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.7.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.7.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.7.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.7.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return Boolean.valueOf(DOMConfigurationImpl.m142-get6(config));
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$8 */
    static class AnonymousClass8 implements Parameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.8.<init>():void, dex: 
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
        AnonymousClass8() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.8.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.8.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.8.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
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
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.8.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.8.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return DOMConfigurationImpl.m143-get7(config);
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value != null ? value instanceof String : true;
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl$9 */
    static class AnonymousClass9 implements Parameter {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.9.<init>():void, dex: 
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
        AnonymousClass9() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.9.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.9.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.9.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
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
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.9.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.9.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }

        public Object get(DOMConfigurationImpl config) {
            return DOMConfigurationImpl.m144-get8(config);
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value != null ? value instanceof String : true;
        }
    }

    static class FixedParameter implements Parameter {
        final Object onlyValue;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.<init>(java.lang.Object):void, dex: 
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
        FixedParameter(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.<init>(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.<init>(java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.canSet(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):boolean, dex: 
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
        public boolean canSet(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.canSet(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.canSet(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.get(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.Object, dex: 
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
        public java.lang.Object get(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.get(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.get(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
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
        public void set(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.FixedParameter.set(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.Object):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get1(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get1(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get1(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get1 */
    static /* synthetic */ boolean m135-get1(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get1(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get1(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get1(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get10(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
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
    /* renamed from: -get10 */
    static /* synthetic */ boolean m136-get10(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get10(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get10(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get11(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get11(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get11(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$24.decode(InstructionCodec.java:550)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get11 */
    static /* synthetic */ boolean m137-get11(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get11(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get11(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get11(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get2(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
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
    /* renamed from: -get2 */
    static /* synthetic */ boolean m138-get2(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get2(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get2(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get3(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
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
    /* renamed from: -get3 */
    static /* synthetic */ boolean m139-get3(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get3(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get3(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get4(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get4(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get4(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get4 */
    static /* synthetic */ boolean m140-get4(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get4(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get4(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get4(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get5(org.apache.harmony.xml.dom.DOMConfigurationImpl):org.w3c.dom.DOMErrorHandler, dex: 
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
    /* renamed from: -get5 */
    static /* synthetic */ org.w3c.dom.DOMErrorHandler m141-get5(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get5(org.apache.harmony.xml.dom.DOMConfigurationImpl):org.w3c.dom.DOMErrorHandler, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get5(org.apache.harmony.xml.dom.DOMConfigurationImpl):org.w3c.dom.DOMErrorHandler");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get6(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get6(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get6(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get6 */
    static /* synthetic */ boolean m142-get6(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get6(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get6(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get6(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get7(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.String, dex: 
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
    /* renamed from: -get7 */
    static /* synthetic */ java.lang.String m143-get7(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get7(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get7(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get8(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.String, dex: 
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
    /* renamed from: -get8 */
    static /* synthetic */ java.lang.String m144-get8(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get8(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get8(org.apache.harmony.xml.dom.DOMConfigurationImpl):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get9(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
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
    /* renamed from: -get9 */
    static /* synthetic */ boolean m145-get9(org.apache.harmony.xml.dom.DOMConfigurationImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get9(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-get9(org.apache.harmony.xml.dom.DOMConfigurationImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set0(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set0(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set0(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set0 */
    static /* synthetic */ boolean m146-set0(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set0(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set0(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set0(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set1(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
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
    /* renamed from: -set1 */
    static /* synthetic */ boolean m147-set1(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set1(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set1(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set10(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set10(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set10(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$24.decode(InstructionCodec.java:550)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set10 */
    static /* synthetic */ boolean m148-set10(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set10(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set10(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set10(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set2(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
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
    /* renamed from: -set2 */
    static /* synthetic */ boolean m149-set2(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set2(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set2(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set3(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set3(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set3(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set3 */
    static /* synthetic */ boolean m150-set3(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set3(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set3(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set3(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set4(org.apache.harmony.xml.dom.DOMConfigurationImpl, org.w3c.dom.DOMErrorHandler):org.w3c.dom.DOMErrorHandler, dex: 
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
    /* renamed from: -set4 */
    static /* synthetic */ org.w3c.dom.DOMErrorHandler m151-set4(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, org.w3c.dom.DOMErrorHandler r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set4(org.apache.harmony.xml.dom.DOMConfigurationImpl, org.w3c.dom.DOMErrorHandler):org.w3c.dom.DOMErrorHandler, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set4(org.apache.harmony.xml.dom.DOMConfigurationImpl, org.w3c.dom.DOMErrorHandler):org.w3c.dom.DOMErrorHandler");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set5(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set5(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set5(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set5 */
    static /* synthetic */ boolean m152-set5(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set5(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set5(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set5(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set6(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String):java.lang.String, dex: 
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
    /* renamed from: -set6 */
    static /* synthetic */ java.lang.String m153-set6(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set6(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set6(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set7(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String):java.lang.String, dex: 
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
    /* renamed from: -set7 */
    static /* synthetic */ java.lang.String m154-set7(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set7(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set7(org.apache.harmony.xml.dom.DOMConfigurationImpl, java.lang.String):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set8(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
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
    /* renamed from: -set8 */
    static /* synthetic */ boolean m155-set8(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set8(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set8(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set9(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
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
    /* renamed from: -set9 */
    static /* synthetic */ boolean m156-set9(org.apache.harmony.xml.dom.DOMConfigurationImpl r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set9(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.-set9(org.apache.harmony.xml.dom.DOMConfigurationImpl, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<init>():void, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<init>():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$24.decode(InstructionCodec.java:550)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public DOMConfigurationImpl() {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<init>():void, dex:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.checkTextValidity(java.lang.CharSequence):void, dex: 
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
    private void checkTextValidity(java.lang.CharSequence r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.checkTextValidity(java.lang.CharSequence):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.checkTextValidity(java.lang.CharSequence):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.report(short, java.lang.String):void, dex: 
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
    private void report(short r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.report(short, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.report(short, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.getParameter(java.lang.String):java.lang.Object, dex: 
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
    public java.lang.Object getParameter(java.lang.String r1) throws org.w3c.dom.DOMException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.getParameter(java.lang.String):java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.getParameter(java.lang.String):java.lang.Object");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.normalize(org.w3c.dom.Node):void, dex: 
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
    public void normalize(org.w3c.dom.Node r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.normalize(org.w3c.dom.Node):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.normalize(org.w3c.dom.Node):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.setParameter(java.lang.String, java.lang.Object):void, dex: 
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
    public void setParameter(java.lang.String r1, java.lang.Object r2) throws org.w3c.dom.DOMException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.setParameter(java.lang.String, java.lang.Object):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.setParameter(java.lang.String, java.lang.Object):void");
    }

    public boolean canSetParameter(String name, Object value) {
        Parameter parameter = (Parameter) PARAMETERS.get(name);
        return parameter != null ? parameter.canSet(this, value) : false;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public org.w3c.dom.DOMStringList getParameterNames() {
        /*
        r3 = this;
        r1 = PARAMETERS;
        r1 = r1.keySet();
        r2 = PARAMETERS;
        r2 = r2.size();
        r2 = new java.lang.String[r2];
        r0 = r1.toArray(r2);
        r0 = (java.lang.String[]) r0;
        r1 = new org.apache.harmony.xml.dom.DOMConfigurationImpl$13;
        r1.<init>(r3, r0);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.getParameterNames():org.w3c.dom.DOMStringList");
    }

    private boolean isValid(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean valid = (c == 9 || c == 10 || c == 13 || (c >= ' ' && c <= 55295)) ? true : c >= 57344 && c <= 65533;
            if (!valid) {
                return false;
            }
        }
        return true;
    }
}
