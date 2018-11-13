package android_maps_conflict_avoidance.com.google.googlenav;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufUtil;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import android_maps_conflict_avoidance.com.google.map.Geometry;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import java.util.Hashtable;

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
public class Placemark {
    private static final EnhancedDataSource[] EMPTY_ENHANCED_DATA_SOURCE = null;
    private static final ImageResource[] EMPTY_IMAGE_SOURCE = null;
    private static final SnippetSource[] EMPTY_SNIPPET_SOURCE = null;
    private Hashtable events;
    private Geometry geometry;
    private byte iconClass;
    private boolean isSelectable;
    private String name;
    private final ProtoBuf proto;
    private int resultType;

    public static class EnhancedDataSource {
    }

    public static class ImageResource {
    }

    public static class SnippetSource {
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.googlenav.Placemark.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.googlenav.Placemark.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.Placemark.<clinit>():void");
    }

    protected Placemark(Geometry geometry, String name) {
        this.name = "";
        this.events = null;
        this.isSelectable = true;
        this.geometry = geometry;
        this.name = name;
        this.proto = null;
        this.iconClass = (byte) 0;
        this.resultType = 7;
    }

    public Geometry getGeometry() {
        return this.geometry;
    }

    public MapPoint getLocation() {
        Geometry location = getGeometry();
        if (location == null) {
            return null;
        }
        return location.getDefiningPoint();
    }

    public String getAddressLine1() {
        return AddressUtil.getAddressLine(4, 0, this.proto);
    }

    public String getAddressLine2() {
        return AddressUtil.getAddressLine(4, 1, this.proto);
    }

    public boolean isKmlResult() {
        return this.resultType == 2;
    }

    public boolean isKmlPlacemark() {
        return this.resultType == 5;
    }

    public boolean isKml() {
        return isKmlPlacemark() || isKmlResult();
    }

    public String getTitle() {
        if (!TextUtil.isEmpty(this.name)) {
            return this.name;
        }
        if (isKml()) {
            return getKmlSupplementalDisplayLine();
        }
        String addressLine1 = getAddressLine1();
        String addressLine2 = getAddressLine2();
        if (!Config.isChinaVersion()) {
            if (addressLine1.equals("")) {
                addressLine1 = addressLine2;
            }
            return addressLine1;
        } else if (addressLine2.equals("")) {
            return addressLine1;
        } else {
            return addressLine2;
        }
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        Geometry geometry = getLocation();
        if (geometry != null) {
            str.append(geometry.toString());
        }
        str.append(":");
        str.append(getTitle());
        str.append(":");
        if (getAddressLine1() != null) {
            str.append(getAddressLine1());
        }
        str.append(":");
        if (getAddressLine1() != null) {
            str.append(getAddressLine2());
        }
        return str.toString();
    }

    public String getKmlSnippet() {
        return ProtoBufUtil.getSubProtoValueOrEmpty(this.proto, 90, 92);
    }

    public String getKmlSupplementalDisplayLine() {
        return !TextUtil.isEmpty(getAddressLine1()) ? getAddressLine1() : getKmlSnippet();
    }
}
