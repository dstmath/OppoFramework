package com.android.server.wifi.hotspot2.omadm;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
public class OMAConstants {
    public static final String DevDetailURN = "urn:oma:mo:oma-dm-devdetail:1.0";
    public static final String DevDetailXURN = "urn:wfa:mo-ext:hotspot2dot0-devdetail-ext:1.0";
    public static final String DevInfoURN = "urn:oma:mo:oma-dm-devinfo:1.0";
    private static final byte[] INDENT = null;
    public static final String MOVersion = "1.0";
    public static final String OMAVersion = "1.2";
    public static final String PPS_URN = "urn:wfa:mo:hotspot2dot0-perprovidersubscription:1.0";
    public static final String SppMOAttribute = "spp:moURN";
    public static final String[] SupportedMO_URNs = null;
    public static final String SyncML = "syncml:dmddf1.2";
    public static final String SyncMLVersionTag = "VerDTD";
    public static final String TAG_Error = "spp:sppError";
    public static final String TAG_MOContainer = "spp:moContainer";
    public static final String TAG_PostDevData = "spp:sppPostDevData";
    public static final String TAG_SessionID = "spp:sessionID";
    public static final String TAG_Status = "spp:sppStatus";
    public static final String TAG_SupportedMOs = "spp:supportedMOList";
    public static final String TAG_SupportedVersions = "spp:supportedSPPVersions";
    public static final String TAG_UpdateResponse = "spp:sppUpdateResponse";
    public static final String TAG_Version = "spp:sppVersion";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.omadm.OMAConstants.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.omadm.OMAConstants.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.omadm.OMAConstants.<clinit>():void");
    }

    private OMAConstants() {
    }

    public static void serializeString(String s, OutputStream out) throws IOException {
        byte[] octets = s.getBytes(StandardCharsets.UTF_8);
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(octets.length);
        out.write(String.format("%x:", objArr).getBytes(StandardCharsets.UTF_8));
        out.write(octets);
    }

    public static void indent(int level, OutputStream out) throws IOException {
        out.write(INDENT, 0, level);
    }

    public static String deserializeString(InputStream in) throws IOException {
        StringBuilder prefix = new StringBuilder();
        while (true) {
            byte b = (byte) in.read();
            if (b == (byte) 46) {
                return null;
            }
            if (b == (byte) 58) {
                byte[] octets = new byte[Integer.parseInt(prefix.toString(), 16)];
                int offset = 0;
                while (offset < octets.length) {
                    int amount = in.read(octets, offset, octets.length - offset);
                    if (amount <= 0) {
                        throw new EOFException();
                    }
                    offset += amount;
                }
                return new String(octets, StandardCharsets.UTF_8);
            } else if (b > (byte) 32) {
                prefix.append((char) b);
            }
        }
    }

    public static String readURN(InputStream in) throws IOException {
        StringBuilder urn = new StringBuilder();
        while (true) {
            byte b = (byte) in.read();
            if (b == (byte) 41) {
                return urn.toString();
            }
            urn.append((char) b);
        }
    }
}
