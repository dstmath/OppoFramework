package android.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

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
public class ContentQueryMap extends Observable {
    private String[] mColumnNames;
    private ContentObserver mContentObserver;
    private volatile Cursor mCursor;
    private boolean mDirty;
    private Handler mHandlerForUpdateNotifications;
    private boolean mKeepUpdated;
    private int mKeyColumn;
    private Map<String, ContentValues> mValues;

    /* renamed from: android.content.ContentQueryMap$1 */
    class AnonymousClass1 extends ContentObserver {
        final /* synthetic */ ContentQueryMap this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.content.ContentQueryMap.1.<init>(android.content.ContentQueryMap, android.os.Handler):void, dex: 
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
        AnonymousClass1(android.content.ContentQueryMap r1, android.os.Handler r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.content.ContentQueryMap.1.<init>(android.content.ContentQueryMap, android.os.Handler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.ContentQueryMap.1.<init>(android.content.ContentQueryMap, android.os.Handler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.content.ContentQueryMap.1.onChange(boolean):void, dex: 
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
        public void onChange(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.content.ContentQueryMap.1.onChange(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.ContentQueryMap.1.onChange(boolean):void");
        }
    }

    public ContentQueryMap(Cursor cursor, String columnNameOfKey, boolean keepUpdated, Handler handlerForUpdateNotifications) {
        this.mHandlerForUpdateNotifications = null;
        this.mKeepUpdated = false;
        this.mValues = null;
        this.mDirty = false;
        this.mCursor = cursor;
        this.mColumnNames = this.mCursor.getColumnNames();
        this.mKeyColumn = this.mCursor.getColumnIndexOrThrow(columnNameOfKey);
        this.mHandlerForUpdateNotifications = handlerForUpdateNotifications;
        setKeepUpdated(keepUpdated);
        if (!keepUpdated) {
            readCursorIntoCache(cursor);
        }
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
    public void setKeepUpdated(boolean r4) {
        /*
        r3 = this;
        r2 = 0;
        r0 = r3.mKeepUpdated;
        if (r4 != r0) goto L_0x0006;
    L_0x0005:
        return;
    L_0x0006:
        r3.mKeepUpdated = r4;
        r0 = r3.mKeepUpdated;
        if (r0 != 0) goto L_0x0016;
    L_0x000c:
        r0 = r3.mCursor;
        r1 = r3.mContentObserver;
        r0.unregisterContentObserver(r1);
        r3.mContentObserver = r2;
    L_0x0015:
        return;
    L_0x0016:
        r0 = r3.mHandlerForUpdateNotifications;
        if (r0 != 0) goto L_0x0021;
    L_0x001a:
        r0 = new android.os.Handler;
        r0.<init>();
        r3.mHandlerForUpdateNotifications = r0;
    L_0x0021:
        r0 = r3.mContentObserver;
        if (r0 != 0) goto L_0x002e;
    L_0x0025:
        r0 = new android.content.ContentQueryMap$1;
        r1 = r3.mHandlerForUpdateNotifications;
        r0.<init>(r3, r1);
        r3.mContentObserver = r0;
    L_0x002e:
        r0 = r3.mCursor;
        r1 = r3.mContentObserver;
        r0.registerContentObserver(r1);
        r0 = 1;
        r3.mDirty = r0;
        goto L_0x0015;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ContentQueryMap.setKeepUpdated(boolean):void");
    }

    public synchronized ContentValues getValues(String rowName) {
        if (this.mDirty) {
            requery();
        }
        return (ContentValues) this.mValues.get(rowName);
    }

    public void requery() {
        Cursor cursor = this.mCursor;
        if (cursor != null) {
            this.mDirty = false;
            if (cursor.requery()) {
                readCursorIntoCache(cursor);
                setChanged();
                notifyObservers();
            }
        }
    }

    private synchronized void readCursorIntoCache(Cursor cursor) {
        this.mValues = new HashMap(this.mValues != null ? this.mValues.size() : 0);
        while (cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            for (int i = 0; i < this.mColumnNames.length; i++) {
                if (i != this.mKeyColumn) {
                    values.put(this.mColumnNames[i], cursor.getString(i));
                }
            }
            this.mValues.put(cursor.getString(this.mKeyColumn), values);
        }
    }

    public synchronized Map<String, ContentValues> getRows() {
        if (this.mDirty) {
            requery();
        }
        return this.mValues;
    }

    public synchronized void close() {
        if (this.mContentObserver != null) {
            this.mCursor.unregisterContentObserver(this.mContentObserver);
            this.mContentObserver = null;
        }
        this.mCursor.close();
        this.mCursor = null;
    }

    protected void finalize() throws Throwable {
        if (this.mCursor != null) {
            close();
        }
        super.finalize();
    }
}
