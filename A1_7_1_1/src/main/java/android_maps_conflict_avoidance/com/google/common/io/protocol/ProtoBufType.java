package android_maps_conflict_avoidance.com.google.common.io.protocol;

import android_maps_conflict_avoidance.com.google.common.util.IntMap;

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
public class ProtoBufType {
    private static final TypeInfo[] NULL_DATA_TYPEINFOS = null;
    private final String typeName;
    private final IntMap types;

    static class TypeInfo {
        private Object data;
        private int type;

        TypeInfo(int t, Object d) {
            this.type = t;
            this.data = d;
        }

        public int hashCode() {
            return this.type;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof TypeInfo)) {
                return false;
            }
            TypeInfo peerTypeInfo = (TypeInfo) obj;
            if (this.type == peerTypeInfo.type) {
                if (this.data == peerTypeInfo.data) {
                    return true;
                }
                if (this.data != null && this.data.equals(peerTypeInfo.data)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "TypeInfo{type=" + this.type + ", data=" + this.data + "}";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufType.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufType.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufType.<clinit>():void");
    }

    public ProtoBufType() {
        this.types = new IntMap();
        this.typeName = null;
    }

    public ProtoBufType(String typeName) {
        this.types = new IntMap();
        this.typeName = typeName;
    }

    private static TypeInfo getCacheTypeInfoForNullData(int optionsAndType) {
        return NULL_DATA_TYPEINFOS[(((65280 & optionsAndType) >> 8) * 21) + ((optionsAndType & 255) - 16)];
    }

    public ProtoBufType addElement(int optionsAndType, int tag, Object data) {
        this.types.put(tag, data == null ? getCacheTypeInfoForNullData(optionsAndType) : new TypeInfo(optionsAndType, data));
        return this;
    }

    public int getType(int tag) {
        TypeInfo typeInfo = (TypeInfo) this.types.get(tag);
        return typeInfo == null ? 16 : typeInfo.type & 255;
    }

    public Object getData(int tag) {
        TypeInfo typeInfo = (TypeInfo) this.types.get(tag);
        return typeInfo == null ? typeInfo : typeInfo.data;
    }

    public String toString() {
        return "ProtoBufType Name: " + this.typeName;
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        return this.types.equals(((ProtoBufType) object).types);
    }

    public int hashCode() {
        if (this.types != null) {
            return this.types.hashCode();
        }
        return super.hashCode();
    }
}
