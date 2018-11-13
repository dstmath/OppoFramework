package com.android.server.firewall;

import android.app.AppGlobals;
import android.os.Process;
import android.os.RemoteException;
import android.util.Slog;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class SenderFilter {
    private static final String ATTR_TYPE = "type";
    public static final FilterFactory FACTORY = null;
    private static final Filter SIGNATURE = null;
    private static final Filter SYSTEM = null;
    private static final Filter SYSTEM_OR_SIGNATURE = null;
    private static final Filter USER_ID = null;
    private static final String VAL_SIGNATURE = "signature";
    private static final String VAL_SYSTEM = "system";
    private static final String VAL_SYSTEM_OR_SIGNATURE = "system|signature";
    private static final String VAL_USER_ID = "userId";

    /* renamed from: com.android.server.firewall.SenderFilter$1 */
    static class AnonymousClass1 extends FilterFactory {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            String typeString = parser.getAttributeValue(null, "type");
            if (typeString == null) {
                throw new XmlPullParserException("type attribute must be specified for <sender>", parser, null);
            } else if (typeString.equals(SenderFilter.VAL_SYSTEM)) {
                return SenderFilter.SYSTEM;
            } else {
                if (typeString.equals(SenderFilter.VAL_SIGNATURE)) {
                    return SenderFilter.SIGNATURE;
                }
                if (typeString.equals(SenderFilter.VAL_SYSTEM_OR_SIGNATURE)) {
                    return SenderFilter.SYSTEM_OR_SIGNATURE;
                }
                if (typeString.equals(SenderFilter.VAL_USER_ID)) {
                    return SenderFilter.USER_ID;
                }
                throw new XmlPullParserException("Invalid type attribute for <sender>: " + typeString, parser, null);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.firewall.SenderFilter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.firewall.SenderFilter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.firewall.SenderFilter.<clinit>():void");
    }

    SenderFilter() {
    }

    static boolean isPrivilegedApp(int callerUid, int callerPid) {
        boolean z = true;
        if (callerUid == 1000 || callerUid == 0 || callerPid == Process.myPid() || callerPid == 0) {
            return true;
        }
        try {
            if ((AppGlobals.getPackageManager().getPrivateFlagsForUid(callerUid) & 8) == 0) {
                z = false;
            }
            return z;
        } catch (RemoteException ex) {
            Slog.e("IntentFirewall", "Remote exception while retrieving uid flags", ex);
            return false;
        }
    }
}
