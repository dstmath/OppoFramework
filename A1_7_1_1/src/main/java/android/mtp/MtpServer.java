package android.mtp;

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
public class MtpServer implements Runnable {
    private final MtpDatabase mDatabase;
    private long mNativeContext;
    private boolean mServerEndup;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpServer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpServer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpServer.<clinit>():void");
    }

    private final native void native_add_storage(MtpStorage mtpStorage);

    private final native void native_cleanup();

    private final native void native_remove_storage(int i);

    private final native void native_run();

    private final native void native_send_device_property_changed(int i);

    private final native void native_send_object_added(int i);

    private final native void native_send_object_infoChanged(int i);

    private final native void native_send_object_removed(int i);

    private final native void native_send_storage_infoChanged(int i);

    private final native void native_setup(MtpDatabase mtpDatabase, boolean z);

    private final native void native_update_storage(MtpStorage mtpStorage);

    public MtpServer(MtpDatabase database, boolean usePtp) {
        this.mServerEndup = false;
        this.mServerEndup = false;
        this.mDatabase = database;
        native_setup(database, usePtp);
        database.setServer(this);
    }

    public void start() {
        this.mServerEndup = false;
        new Thread(this, "MtpServer").start();
    }

    public void run() {
        native_run();
        native_cleanup();
        this.mServerEndup = true;
        this.mDatabase.close();
    }

    public void sendObjectAdded(int handle) {
        native_send_object_added(handle);
    }

    public void sendObjectRemoved(int handle) {
        native_send_object_removed(handle);
    }

    public void sendDevicePropertyChanged(int property) {
        native_send_device_property_changed(property);
    }

    public void addStorage(MtpStorage storage) {
        native_add_storage(storage);
    }

    public void removeStorage(MtpStorage storage) {
        native_remove_storage(storage.getStorageId());
    }

    public void updateStorage(MtpStorage storage) {
        native_update_storage(storage);
    }

    public void sendStorageInfoChanged(MtpStorage storage) {
        native_send_storage_infoChanged(storage.getStorageId());
    }

    public void endSession() {
    }

    public boolean getStatus() {
        return this.mServerEndup;
    }

    public void sendObjectInfoChanged(int handle) {
        native_send_object_infoChanged(handle);
    }
}
