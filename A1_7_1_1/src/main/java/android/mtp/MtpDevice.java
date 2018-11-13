package android.mtp;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.os.UserManager;
import com.android.internal.util.Preconditions;
import java.io.IOException;

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
public final class MtpDevice {
    private static final String TAG = "MtpDevice";
    private final UsbDevice mDevice;
    private long mNativeContext;

    /* renamed from: android.mtp.MtpDevice$1 */
    class AnonymousClass1 implements OnCancelListener {
        final /* synthetic */ MtpDevice this$0;
        final /* synthetic */ int val$handle;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDevice.1.<init>(android.mtp.MtpDevice, int):void, dex: 
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
        AnonymousClass1(android.mtp.MtpDevice r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDevice.1.<init>(android.mtp.MtpDevice, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDevice.1.<init>(android.mtp.MtpDevice, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.mtp.MtpDevice.1.onCancel():void, dex: 
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
        public void onCancel() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.mtp.MtpDevice.1.onCancel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDevice.1.onCancel():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpDevice.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpDevice.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDevice.<clinit>():void");
    }

    private native void native_close();

    private native boolean native_delete_object(int i);

    private native void native_discard_event_request(int i);

    private native MtpDeviceInfo native_get_device_info();

    private native byte[] native_get_object(int i, long j);

    private native int[] native_get_object_handles(int i, int i2, int i3);

    private native MtpObjectInfo native_get_object_info(int i);

    private native long native_get_object_size_long(int i, int i2) throws IOException;

    private native int native_get_parent(int i);

    private native long native_get_partial_object(int i, long j, long j2, byte[] bArr) throws IOException;

    private native int native_get_partial_object_64(int i, long j, long j2, byte[] bArr) throws IOException;

    private native int native_get_storage_id(int i);

    private native int[] native_get_storage_ids();

    private native MtpStorageInfo native_get_storage_info(int i);

    private native byte[] native_get_thumbnail(int i);

    private native boolean native_import_file(int i, int i2);

    private native boolean native_import_file(int i, String str);

    private native boolean native_open(String str, int i);

    private native MtpEvent native_reap_event_request(int i) throws IOException;

    private native boolean native_send_object(int i, long j, int i2);

    private native MtpObjectInfo native_send_object_info(MtpObjectInfo mtpObjectInfo);

    private native int native_submit_event_request() throws IOException;

    public MtpDevice(UsbDevice device) {
        this.mDevice = device;
    }

    public boolean open(UsbDeviceConnection connection) {
        boolean result = false;
        Context context = connection.getContext();
        if (!(context == null || ((UserManager) context.getSystemService(Context.USER_SERVICE)).hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER))) {
            result = native_open(this.mDevice.getDeviceName(), connection.getFileDescriptor());
        }
        if (!result) {
            connection.close();
        }
        return result;
    }

    public void close() {
        native_close();
    }

    protected void finalize() throws Throwable {
        try {
            native_close();
        } finally {
            super.finalize();
        }
    }

    public String getDeviceName() {
        return this.mDevice.getDeviceName();
    }

    public int getDeviceId() {
        return this.mDevice.getDeviceId();
    }

    public String toString() {
        return this.mDevice.getDeviceName();
    }

    public MtpDeviceInfo getDeviceInfo() {
        return native_get_device_info();
    }

    public int[] getStorageIds() {
        return native_get_storage_ids();
    }

    public int[] getObjectHandles(int storageId, int format, int objectHandle) {
        return native_get_object_handles(storageId, format, objectHandle);
    }

    public byte[] getObject(int objectHandle, int objectSize) {
        Preconditions.checkArgumentNonnegative(objectSize, "objectSize should not be negative");
        return native_get_object(objectHandle, (long) objectSize);
    }

    public long getPartialObject(int objectHandle, long offset, long size, byte[] buffer) throws IOException {
        return native_get_partial_object(objectHandle, offset, size, buffer);
    }

    public long getPartialObject64(int objectHandle, long offset, long size, byte[] buffer) throws IOException {
        return (long) native_get_partial_object_64(objectHandle, offset, size, buffer);
    }

    public byte[] getThumbnail(int objectHandle) {
        return native_get_thumbnail(objectHandle);
    }

    public MtpStorageInfo getStorageInfo(int storageId) {
        return native_get_storage_info(storageId);
    }

    public MtpObjectInfo getObjectInfo(int objectHandle) {
        return native_get_object_info(objectHandle);
    }

    public boolean deleteObject(int objectHandle) {
        return native_delete_object(objectHandle);
    }

    public long getParent(int objectHandle) {
        return (long) native_get_parent(objectHandle);
    }

    public long getStorageId(int objectHandle) {
        return (long) native_get_storage_id(objectHandle);
    }

    public boolean importFile(int objectHandle, String destPath) {
        return native_import_file(objectHandle, destPath);
    }

    public boolean importFile(int objectHandle, ParcelFileDescriptor descriptor) {
        return native_import_file(objectHandle, descriptor.getFd());
    }

    public boolean sendObject(int objectHandle, long size, ParcelFileDescriptor descriptor) {
        return native_send_object(objectHandle, size, descriptor.getFd());
    }

    public MtpObjectInfo sendObjectInfo(MtpObjectInfo info) {
        return native_send_object_info(info);
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
    public android.mtp.MtpEvent readEvent(android.os.CancellationSignal r5) throws java.io.IOException {
        /*
        r4 = this;
        r1 = 0;
        r3 = 0;
        r0 = r4.native_submit_event_request();
        if (r0 < 0) goto L_0x0009;
    L_0x0008:
        r1 = 1;
    L_0x0009:
        r2 = "Other thread is reading an event.";
        com.android.internal.util.Preconditions.checkState(r1, r2);
        if (r5 == 0) goto L_0x0019;
    L_0x0011:
        r1 = new android.mtp.MtpDevice$1;
        r1.<init>(r4, r0);
        r5.setOnCancelListener(r1);
    L_0x0019:
        r1 = r4.native_reap_event_request(r0);	 Catch:{ all -> 0x0023 }
        if (r5 == 0) goto L_0x0022;
    L_0x001f:
        r5.setOnCancelListener(r3);
    L_0x0022:
        return r1;
    L_0x0023:
        r1 = move-exception;
        if (r5 == 0) goto L_0x0029;
    L_0x0026:
        r5.setOnCancelListener(r3);
    L_0x0029:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDevice.readEvent(android.os.CancellationSignal):android.mtp.MtpEvent");
    }

    public long getObjectSizeLong(int handle, int format) throws IOException {
        return native_get_object_size_long(handle, format);
    }
}
