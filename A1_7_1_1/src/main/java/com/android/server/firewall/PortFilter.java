package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
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
class PortFilter implements Filter {
    private static final String ATTR_EQUALS = "equals";
    private static final String ATTR_MAX = "max";
    private static final String ATTR_MIN = "min";
    public static final FilterFactory FACTORY = null;
    private static final int NO_BOUND = -1;
    private final int mLowerBound;
    private final int mUpperBound;

    /* renamed from: com.android.server.firewall.PortFilter$1 */
    static class AnonymousClass1 extends FilterFactory {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            int lowerBound = -1;
            int upperBound = -1;
            String equalsValue = parser.getAttributeValue(null, PortFilter.ATTR_EQUALS);
            if (equalsValue != null) {
                try {
                    int value = Integer.parseInt(equalsValue);
                    lowerBound = value;
                    upperBound = value;
                } catch (NumberFormatException e) {
                    throw new XmlPullParserException("Invalid port value: " + equalsValue, parser, null);
                }
            }
            String lowerBoundString = parser.getAttributeValue(null, PortFilter.ATTR_MIN);
            String upperBoundString = parser.getAttributeValue(null, PortFilter.ATTR_MAX);
            if (!(lowerBoundString == null && upperBoundString == null)) {
                if (equalsValue != null) {
                    throw new XmlPullParserException("Port filter cannot use both equals and range filtering", parser, null);
                }
                if (lowerBoundString != null) {
                    try {
                        lowerBound = Integer.parseInt(lowerBoundString);
                    } catch (NumberFormatException e2) {
                        throw new XmlPullParserException("Invalid minimum port value: " + lowerBoundString, parser, null);
                    }
                }
                if (upperBoundString != null) {
                    try {
                        upperBound = Integer.parseInt(upperBoundString);
                    } catch (NumberFormatException e3) {
                        throw new XmlPullParserException("Invalid maximum port value: " + upperBoundString, parser, null);
                    }
                }
            }
            return new PortFilter(lowerBound, upperBound, null);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.firewall.PortFilter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.firewall.PortFilter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.firewall.PortFilter.<clinit>():void");
    }

    /* synthetic */ PortFilter(int lowerBound, int upperBound, PortFilter portFilter) {
        this(lowerBound, upperBound);
    }

    private PortFilter(int lowerBound, int upperBound) {
        this.mLowerBound = lowerBound;
        this.mUpperBound = upperBound;
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        int port = -1;
        Uri uri = intent.getData();
        if (uri != null) {
            port = uri.getPort();
        }
        if (port == -1 || (this.mLowerBound != -1 && this.mLowerBound > port)) {
            return false;
        }
        if (this.mUpperBound == -1 || this.mUpperBound >= port) {
            return true;
        }
        return false;
    }
}
