package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
public class CivicLocationElement extends ANQPElement {
    public static final int ADDITIONAL_CODE = 32;
    public static final int ADDITIONAL_LOCATION = 22;
    public static final int BLOCK = 5;
    public static final int BRANCH_ROAD = 36;
    public static final int BUILDING = 25;
    public static final int CITY = 3;
    public static final int COUNTY_DISTRICT = 2;
    public static final int DIVISION_BOROUGH = 4;
    public static final int FLOOR = 27;
    private static final int GEOCONF_CIVIC4 = 99;
    public static final int HOUSE_NUMBER = 19;
    public static final int HOUSE_NUMBER_SUFFIX = 20;
    public static final int LANDMARK = 21;
    public static final int LANGUAGE = 0;
    public static final int LEADING_STREET_SUFFIX = 17;
    public static final int NAME = 23;
    public static final int POSTAL_COMMUNITY = 30;
    public static final int POSTAL_ZIP = 24;
    public static final int PO_BOX = 31;
    public static final int PRIMARY_ROAD = 34;
    public static final int RESERVED = 255;
    private static final int RFC4776 = 0;
    public static final int ROAD_SECTION = 35;
    public static final int ROOM = 28;
    public static final int SCRIPT = 128;
    public static final int SEAT_DESK = 33;
    public static final int STATE_PROVINCE = 1;
    public static final int STREET_DIRECTION = 16;
    public static final int STREET_GROUP = 6;
    public static final int STREET_NAME_POST_MOD = 39;
    public static final int STREET_NAME_PRE_MOD = 38;
    public static final int STREET_SUFFIX = 18;
    public static final int SUB_BRANCH_ROAD = 37;
    public static final int TYPE = 29;
    public static final int UNIT = 26;
    private static final Map<Integer, CAType> s_caTypes = null;
    private final Locale mLocale;
    private final LocationType mLocationType;
    private final Map<CAType, String> mValues;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum CAType {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.CivicLocationElement.CAType.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.CivicLocationElement.CAType.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.CivicLocationElement.CAType.<clinit>():void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum LocationType {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.CivicLocationElement.LocationType.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.CivicLocationElement.LocationType.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.CivicLocationElement.LocationType.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.CivicLocationElement.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.CivicLocationElement.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.CivicLocationElement.<clinit>():void");
    }

    public CivicLocationElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        LocationType locationType = null;
        super(infoID);
        if (payload.remaining() < 6) {
            throw new ProtocolException("Runt civic location:" + payload.remaining());
        }
        int locType = payload.get() & 255;
        if (locType != 0) {
            throw new ProtocolException("Bad Civic location type: " + locType);
        }
        int locSubType = payload.get() & 255;
        if (locSubType != GEOCONF_CIVIC4) {
            throw new ProtocolException("Unexpected Civic location sub-type: " + locSubType + " (cannot handle sub elements)");
        }
        int length = payload.get() & 255;
        if (length > payload.remaining()) {
            throw new ProtocolException("Invalid CA type length: " + length);
        }
        int what = payload.get() & 255;
        if (what < LocationType.values().length) {
            locationType = LocationType.values()[what];
        }
        this.mLocationType = locationType;
        this.mLocale = Locale.forLanguageTag(Constants.getString(payload, 2, StandardCharsets.US_ASCII));
        this.mValues = new HashMap();
        while (payload.hasRemaining()) {
            CAType caType = (CAType) s_caTypes.get(Integer.valueOf(payload.get() & 255));
            int caValLen = payload.get() & 255;
            if (caValLen > payload.remaining()) {
                throw new ProtocolException("Bad CA value length: " + caValLen);
            }
            byte[] caValOctets = new byte[caValLen];
            payload.get(caValOctets);
            if (caType != null) {
                this.mValues.put(caType, new String(caValOctets, StandardCharsets.UTF_8));
            }
        }
    }

    public LocationType getLocationType() {
        return this.mLocationType;
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public Map<CAType, String> getValues() {
        return Collections.unmodifiableMap(this.mValues);
    }

    public String toString() {
        return "CivicLocation{mLocationType=" + this.mLocationType + ", mLocale=" + this.mLocale + ", mValues=" + this.mValues + '}';
    }
}
