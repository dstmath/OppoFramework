package android.security.net.config;

import android.content.Context;
import android.util.Pair;
import java.util.Set;

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
public class XmlConfigSource implements ConfigSource {
    private static final int CONFIG_BASE = 0;
    private static final int CONFIG_DEBUG = 2;
    private static final int CONFIG_DOMAIN = 1;
    private Context mContext;
    private final boolean mDebugBuild;
    private NetworkSecurityConfig mDefaultConfig;
    private Set<Pair<Domain, NetworkSecurityConfig>> mDomainMap;
    private boolean mInitialized;
    private final Object mLock;
    private final int mResourceId;
    private final int mTargetSdkVersion;

    public static class ParserException extends Exception {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.net.config.XmlConfigSource.ParserException.<init>(org.xmlpull.v1.XmlPullParser, java.lang.String):void, dex: 
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
        public ParserException(org.xmlpull.v1.XmlPullParser r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.net.config.XmlConfigSource.ParserException.<init>(org.xmlpull.v1.XmlPullParser, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.ParserException.<init>(org.xmlpull.v1.XmlPullParser, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.ParserException.<init>(org.xmlpull.v1.XmlPullParser, java.lang.String, java.lang.Throwable):void, dex: 
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
        public ParserException(org.xmlpull.v1.XmlPullParser r1, java.lang.String r2, java.lang.Throwable r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.ParserException.<init>(org.xmlpull.v1.XmlPullParser, java.lang.String, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.ParserException.<init>(org.xmlpull.v1.XmlPullParser, java.lang.String, java.lang.Throwable):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.net.config.XmlConfigSource.<init>(android.content.Context, int, boolean, int):void, dex: 
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
    public XmlConfigSource(android.content.Context r1, int r2, boolean r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.net.config.XmlConfigSource.<init>(android.content.Context, int, boolean, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.<init>(android.content.Context, int, boolean, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.net.config.XmlConfigSource.addDebugAnchorsIfNeeded(android.security.net.config.NetworkSecurityConfig$Builder, android.security.net.config.NetworkSecurityConfig$Builder):void, dex:  in method: android.security.net.config.XmlConfigSource.addDebugAnchorsIfNeeded(android.security.net.config.NetworkSecurityConfig$Builder, android.security.net.config.NetworkSecurityConfig$Builder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.net.config.XmlConfigSource.addDebugAnchorsIfNeeded(android.security.net.config.NetworkSecurityConfig$Builder, android.security.net.config.NetworkSecurityConfig$Builder):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void addDebugAnchorsIfNeeded(android.security.net.config.NetworkSecurityConfig.Builder r1, android.security.net.config.NetworkSecurityConfig.Builder r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.net.config.XmlConfigSource.addDebugAnchorsIfNeeded(android.security.net.config.NetworkSecurityConfig$Builder, android.security.net.config.NetworkSecurityConfig$Builder):void, dex:  in method: android.security.net.config.XmlConfigSource.addDebugAnchorsIfNeeded(android.security.net.config.NetworkSecurityConfig$Builder, android.security.net.config.NetworkSecurityConfig$Builder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.addDebugAnchorsIfNeeded(android.security.net.config.NetworkSecurityConfig$Builder, android.security.net.config.NetworkSecurityConfig$Builder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.ensureInitialized():void, dex: 
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
    private void ensureInitialized() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.ensureInitialized():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.ensureInitialized():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.getConfigString(int):java.lang.String, dex: 
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
    private static final java.lang.String getConfigString(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.getConfigString(int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.getConfigString(int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.parseCertificatesEntry(android.content.res.XmlResourceParser, boolean):android.security.net.config.CertificatesEntryRef, dex: 
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
    private android.security.net.config.CertificatesEntryRef parseCertificatesEntry(android.content.res.XmlResourceParser r1, boolean r2) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.parseCertificatesEntry(android.content.res.XmlResourceParser, boolean):android.security.net.config.CertificatesEntryRef, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parseCertificatesEntry(android.content.res.XmlResourceParser, boolean):android.security.net.config.CertificatesEntryRef");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parseConfigEntry(android.content.res.XmlResourceParser, java.util.Set, android.security.net.config.NetworkSecurityConfig$Builder, int):java.util.List<android.util.Pair<android.security.net.config.NetworkSecurityConfig$Builder, java.util.Set<android.security.net.config.Domain>>>, dex: 
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
    private java.util.List<android.util.Pair<android.security.net.config.NetworkSecurityConfig.Builder, java.util.Set<android.security.net.config.Domain>>> parseConfigEntry(android.content.res.XmlResourceParser r1, java.util.Set<java.lang.String> r2, android.security.net.config.NetworkSecurityConfig.Builder r3, int r4) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parseConfigEntry(android.content.res.XmlResourceParser, java.util.Set, android.security.net.config.NetworkSecurityConfig$Builder, int):java.util.List<android.util.Pair<android.security.net.config.NetworkSecurityConfig$Builder, java.util.Set<android.security.net.config.Domain>>>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parseConfigEntry(android.content.res.XmlResourceParser, java.util.Set, android.security.net.config.NetworkSecurityConfig$Builder, int):java.util.List<android.util.Pair<android.security.net.config.NetworkSecurityConfig$Builder, java.util.Set<android.security.net.config.Domain>>>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.parseDebugOverridesResource():android.security.net.config.NetworkSecurityConfig$Builder, dex: 
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
    private android.security.net.config.NetworkSecurityConfig.Builder parseDebugOverridesResource() throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.parseDebugOverridesResource():android.security.net.config.NetworkSecurityConfig$Builder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parseDebugOverridesResource():android.security.net.config.NetworkSecurityConfig$Builder");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parseDomain(android.content.res.XmlResourceParser, java.util.Set):android.security.net.config.Domain, dex: 
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
    private android.security.net.config.Domain parseDomain(android.content.res.XmlResourceParser r1, java.util.Set<java.lang.String> r2) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parseDomain(android.content.res.XmlResourceParser, java.util.Set):android.security.net.config.Domain, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parseDomain(android.content.res.XmlResourceParser, java.util.Set):android.security.net.config.Domain");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.security.net.config.XmlConfigSource.parseNetworkSecurityConfig(android.content.res.XmlResourceParser):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void parseNetworkSecurityConfig(android.content.res.XmlResourceParser r1) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.security.net.config.XmlConfigSource.parseNetworkSecurityConfig(android.content.res.XmlResourceParser):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parseNetworkSecurityConfig(android.content.res.XmlResourceParser):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parsePin(android.content.res.XmlResourceParser):android.security.net.config.Pin, dex: 
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
    private android.security.net.config.Pin parsePin(android.content.res.XmlResourceParser r1) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parsePin(android.content.res.XmlResourceParser):android.security.net.config.Pin, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parsePin(android.content.res.XmlResourceParser):android.security.net.config.Pin");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parsePinSet(android.content.res.XmlResourceParser):android.security.net.config.PinSet, dex: 
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
    private android.security.net.config.PinSet parsePinSet(android.content.res.XmlResourceParser r1) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parsePinSet(android.content.res.XmlResourceParser):android.security.net.config.PinSet, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parsePinSet(android.content.res.XmlResourceParser):android.security.net.config.PinSet");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parseTrustAnchors(android.content.res.XmlResourceParser, boolean):java.util.Collection<android.security.net.config.CertificatesEntryRef>, dex: 
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
    private java.util.Collection<android.security.net.config.CertificatesEntryRef> parseTrustAnchors(android.content.res.XmlResourceParser r1, boolean r2) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, android.security.net.config.XmlConfigSource.ParserException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.net.config.XmlConfigSource.parseTrustAnchors(android.content.res.XmlResourceParser, boolean):java.util.Collection<android.security.net.config.CertificatesEntryRef>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.parseTrustAnchors(android.content.res.XmlResourceParser, boolean):java.util.Collection<android.security.net.config.CertificatesEntryRef>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.getDefaultConfig():android.security.net.config.NetworkSecurityConfig, dex: 
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
    public android.security.net.config.NetworkSecurityConfig getDefaultConfig() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.getDefaultConfig():android.security.net.config.NetworkSecurityConfig, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.getDefaultConfig():android.security.net.config.NetworkSecurityConfig");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.getPerDomainConfigs():java.util.Set<android.util.Pair<android.security.net.config.Domain, android.security.net.config.NetworkSecurityConfig>>, dex: 
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
    public java.util.Set<android.util.Pair<android.security.net.config.Domain, android.security.net.config.NetworkSecurityConfig>> getPerDomainConfigs() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.XmlConfigSource.getPerDomainConfigs():java.util.Set<android.util.Pair<android.security.net.config.Domain, android.security.net.config.NetworkSecurityConfig>>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.XmlConfigSource.getPerDomainConfigs():java.util.Set<android.util.Pair<android.security.net.config.Domain, android.security.net.config.NetworkSecurityConfig>>");
    }

    public XmlConfigSource(Context context, int resourceId) {
        this(context, resourceId, false);
    }

    public XmlConfigSource(Context context, int resourceId, boolean debugBuild) {
        this(context, resourceId, debugBuild, 10000);
    }
}
