package android.hardware.usb;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class UsbConstants {
    public static final int USB_CLASS_APP_SPEC = 254;
    public static final int USB_CLASS_AUDIO = 1;
    public static final int USB_CLASS_CDC_DATA = 10;
    public static final int USB_CLASS_COMM = 2;
    public static final int USB_CLASS_CONTENT_SEC = 13;
    public static final int USB_CLASS_CSCID = 11;
    public static final int USB_CLASS_HID = 3;
    public static final int USB_CLASS_HUB = 9;
    public static final int USB_CLASS_MASS_STORAGE = 8;
    public static final int USB_CLASS_MISC = 239;
    public static final int USB_CLASS_PER_INTERFACE = 0;
    public static final int USB_CLASS_PHYSICA = 5;
    public static final int USB_CLASS_PRINTER = 7;
    public static final int USB_CLASS_STILL_IMAGE = 6;
    public static final int USB_CLASS_VENDOR_SPEC = 255;
    public static final int USB_CLASS_VIDEO = 14;
    public static final int USB_CLASS_WIRELESS_CONTROLLER = 224;
    public static final int USB_DIR_IN = 128;
    public static final int USB_DIR_OUT = 0;
    public static final int USB_ENDPOINT_DIR_MASK = 128;
    public static final int USB_ENDPOINT_NUMBER_MASK = 15;
    public static final int USB_ENDPOINT_XFERTYPE_MASK = 3;
    public static final int USB_ENDPOINT_XFER_BULK = 2;
    public static final int USB_ENDPOINT_XFER_CONTROL = 0;
    public static final int USB_ENDPOINT_XFER_INT = 3;
    public static final int USB_ENDPOINT_XFER_ISOC = 1;
    public static final int USB_INTERFACE_SUBCLASS_BOOT = 1;
    public static final int USB_SUBCLASS_VENDOR_SPEC = 255;
    public static final int USB_TYPE_CLASS = 32;
    public static final int USB_TYPE_MASK = 96;
    public static final int USB_TYPE_RESERVED = 96;
    public static final int USB_TYPE_STANDARD = 0;
    public static final int USB_TYPE_VENDOR = 64;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.usb.UsbConstants.<init>():void, dex: 
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
    public UsbConstants() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.usb.UsbConstants.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.usb.UsbConstants.<init>():void");
    }
}
