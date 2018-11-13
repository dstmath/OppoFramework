package android.mtp;

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
public class MtpEvent {
    public static final int EVENT_CANCEL_TRANSACTION = 16385;
    public static final int EVENT_CAPTURE_COMPLETE = 16397;
    public static final int EVENT_DEVICE_INFO_CHANGED = 16392;
    public static final int EVENT_DEVICE_PROP_CHANGED = 16390;
    public static final int EVENT_DEVICE_RESET = 16395;
    public static final int EVENT_OBJECT_ADDED = 16386;
    public static final int EVENT_OBJECT_INFO_CHANGED = 16391;
    public static final int EVENT_OBJECT_PROP_CHANGED = 51201;
    public static final int EVENT_OBJECT_PROP_DESC_CHANGED = 51202;
    public static final int EVENT_OBJECT_REFERENCES_CHANGED = 51203;
    public static final int EVENT_OBJECT_REMOVED = 16387;
    public static final int EVENT_REQUEST_OBJECT_TRANSFER = 16393;
    public static final int EVENT_STORAGE_INFO_CHANGED = 16396;
    public static final int EVENT_STORE_ADDED = 16388;
    public static final int EVENT_STORE_FULL = 16394;
    public static final int EVENT_STORE_REMOVED = 16389;
    public static final int EVENT_UNDEFINED = 16384;
    public static final int EVENT_UNREPORTED_STATUS = 16398;
    private int mEventCode;
    private int mParameter1;
    private int mParameter2;
    private int mParameter3;

    private static class IllegalParameterAccess extends UnsupportedOperationException {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.mtp.MtpEvent.IllegalParameterAccess.<init>(java.lang.String, int):void, dex: 
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
        public IllegalParameterAccess(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.mtp.MtpEvent.IllegalParameterAccess.<init>(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpEvent.IllegalParameterAccess.<init>(java.lang.String, int):void");
        }
    }

    private MtpEvent() {
        this.mEventCode = 16384;
    }

    public int getEventCode() {
        return this.mEventCode;
    }

    public int getParameter1() {
        return this.mParameter1;
    }

    public int getParameter2() {
        return this.mParameter2;
    }

    public int getParameter3() {
        return this.mParameter3;
    }

    public int getObjectHandle() {
        switch (this.mEventCode) {
            case 16386:
                return this.mParameter1;
            case 16387:
                return this.mParameter1;
            case 16391:
                return this.mParameter1;
            case 16393:
                return this.mParameter1;
            case 51201:
                return this.mParameter1;
            case 51203:
                return this.mParameter1;
            default:
                throw new IllegalParameterAccess("objectHandle", this.mEventCode);
        }
    }

    public int getStorageId() {
        switch (this.mEventCode) {
            case 16388:
                return this.mParameter1;
            case 16389:
                return this.mParameter1;
            case 16394:
                return this.mParameter1;
            case 16396:
                return this.mParameter1;
            default:
                throw new IllegalParameterAccess("storageID", this.mEventCode);
        }
    }

    public int getDevicePropCode() {
        switch (this.mEventCode) {
            case 16390:
                return this.mParameter1;
            default:
                throw new IllegalParameterAccess("devicePropCode", this.mEventCode);
        }
    }

    public int getTransactionId() {
        switch (this.mEventCode) {
            case 16397:
                return this.mParameter1;
            default:
                throw new IllegalParameterAccess("transactionID", this.mEventCode);
        }
    }

    public int getObjectPropCode() {
        switch (this.mEventCode) {
            case 51201:
                return this.mParameter2;
            case 51202:
                return this.mParameter1;
            default:
                throw new IllegalParameterAccess("objectPropCode", this.mEventCode);
        }
    }

    public int getObjectFormatCode() {
        switch (this.mEventCode) {
            case 51202:
                return this.mParameter2;
            default:
                throw new IllegalParameterAccess("objectFormatCode", this.mEventCode);
        }
    }
}
