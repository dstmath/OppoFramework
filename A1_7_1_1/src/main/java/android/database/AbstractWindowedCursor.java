package android.database;

import android.util.Log;

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
public abstract class AbstractWindowedCursor extends AbstractCursor {
    private static final boolean DEBUG_CLOSE_TRACE = false;
    private static final boolean DEBUG_IS_ENG_BUILD = false;
    protected CursorWindow mWindow;
    private Throwable mWindowCloseTrace;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.database.AbstractWindowedCursor.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.database.AbstractWindowedCursor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.AbstractWindowedCursor.<clinit>():void");
    }

    public byte[] getBlob(int columnIndex) {
        checkPosition();
        return this.mWindow.getBlob(this.mPos, columnIndex);
    }

    public String getString(int columnIndex) {
        checkPosition();
        return this.mWindow.getString(this.mPos, columnIndex);
    }

    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        checkPosition();
        this.mWindow.copyStringToBuffer(this.mPos, columnIndex, buffer);
    }

    public short getShort(int columnIndex) {
        checkPosition();
        return this.mWindow.getShort(this.mPos, columnIndex);
    }

    public int getInt(int columnIndex) {
        checkPosition();
        return this.mWindow.getInt(this.mPos, columnIndex);
    }

    public long getLong(int columnIndex) {
        checkPosition();
        return this.mWindow.getLong(this.mPos, columnIndex);
    }

    public float getFloat(int columnIndex) {
        checkPosition();
        return this.mWindow.getFloat(this.mPos, columnIndex);
    }

    public double getDouble(int columnIndex) {
        checkPosition();
        return this.mWindow.getDouble(this.mPos, columnIndex);
    }

    public boolean isNull(int columnIndex) {
        checkPosition();
        if (this.mWindow.getType(this.mPos, columnIndex) == 0) {
            return true;
        }
        return false;
    }

    @Deprecated
    public boolean isBlob(int columnIndex) {
        return getType(columnIndex) == 4;
    }

    @Deprecated
    public boolean isString(int columnIndex) {
        return getType(columnIndex) == 3;
    }

    @Deprecated
    public boolean isLong(int columnIndex) {
        return getType(columnIndex) == 1;
    }

    @Deprecated
    public boolean isFloat(int columnIndex) {
        return getType(columnIndex) == 2;
    }

    public int getType(int columnIndex) {
        checkPosition();
        return this.mWindow.getType(this.mPos, columnIndex);
    }

    protected void checkPosition() {
        super.checkPosition();
        if (this.mWindow == null) {
            if (DEBUG_IS_ENG_BUILD || DEBUG_CLOSE_TRACE) {
                Log.v("CursorOrCursorWindowClosed", "CursorWindow close stack trace", this.mWindowCloseTrace);
            }
            throw new StaleDataException("Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.");
        }
    }

    public CursorWindow getWindow() {
        return this.mWindow;
    }

    public void setWindow(CursorWindow window) {
        if (window != this.mWindow) {
            closeWindow();
            this.mWindow = window;
        }
    }

    public boolean hasWindow() {
        return this.mWindow != null;
    }

    protected void closeWindow() {
        if (this.mWindow != null) {
            this.mWindow.close();
            this.mWindow = null;
            if (DEBUG_IS_ENG_BUILD || DEBUG_CLOSE_TRACE) {
                this.mWindowCloseTrace = new Throwable("stacktrace");
            }
        }
    }

    protected void clearOrCreateWindow(String name) {
        if (this.mWindow == null) {
            this.mWindow = new CursorWindow(name);
        } else {
            this.mWindow.clear();
        }
    }

    protected void onDeactivateOrClose() {
        super.onDeactivateOrClose();
        closeWindow();
    }
}
