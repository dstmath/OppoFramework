package android.location;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.HashMap;
import java.util.Map.Entry;

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
abstract class LocalListenerHelper<TListener> {
    private final Context mContext;
    private final HashMap<TListener, Handler> mListeners;
    private final String mTag;

    protected interface ListenerOperation<TListener> {
        void execute(TListener tListener) throws RemoteException;
    }

    /* renamed from: android.location.LocalListenerHelper$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ LocalListenerHelper this$0;
        final /* synthetic */ Entry val$listener;
        final /* synthetic */ ListenerOperation val$operation;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.location.LocalListenerHelper.1.<init>(android.location.LocalListenerHelper, android.location.LocalListenerHelper$ListenerOperation, java.util.Map$Entry):void, dex: 
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
        AnonymousClass1(android.location.LocalListenerHelper r1, android.location.LocalListenerHelper.ListenerOperation r2, java.util.Map.Entry r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.location.LocalListenerHelper.1.<init>(android.location.LocalListenerHelper, android.location.LocalListenerHelper$ListenerOperation, java.util.Map$Entry):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.location.LocalListenerHelper.1.<init>(android.location.LocalListenerHelper, android.location.LocalListenerHelper$ListenerOperation, java.util.Map$Entry):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.location.LocalListenerHelper.1.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.location.LocalListenerHelper.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.location.LocalListenerHelper.1.run():void");
        }
    }

    protected abstract boolean registerWithServer() throws RemoteException;

    protected abstract void unregisterFromServer() throws RemoteException;

    protected LocalListenerHelper(Context context, String name) {
        this.mListeners = new HashMap();
        Preconditions.checkNotNull(name);
        this.mContext = context;
        this.mTag = name;
    }

    public boolean add(TListener listener, Handler handler) {
        Preconditions.checkNotNull(listener);
        synchronized (this.mListeners) {
            if (this.mListeners.isEmpty()) {
                try {
                    if (!registerWithServer()) {
                        Log.e(this.mTag, "Unable to register listener transport.");
                        return false;
                    }
                } catch (RemoteException e) {
                    Log.e(this.mTag, "Error handling first listener.", e);
                    return false;
                }
            }
            if (this.mListeners.containsKey(listener)) {
                return true;
            }
            this.mListeners.put(listener, handler);
            return true;
        }
    }

    public void remove(TListener listener) {
        Preconditions.checkNotNull(listener);
        synchronized (this.mListeners) {
            boolean removed = this.mListeners.containsKey(listener);
            this.mListeners.remove(listener);
            if (removed ? this.mListeners.isEmpty() : false) {
                try {
                    unregisterFromServer();
                } catch (RemoteException e) {
                    Log.v(this.mTag, "Error handling last listener removal", e);
                }
            }
        }
        return;
    }

    protected Context getContext() {
        return this.mContext;
    }

    private void executeOperation(ListenerOperation<TListener> operation, TListener listener) {
        try {
            operation.execute(listener);
        } catch (RemoteException e) {
            Log.e(this.mTag, "Error in monitored listener.", e);
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
    protected void foreach(android.location.LocalListenerHelper.ListenerOperation<TListener> r6) {
        /*
        r5 = this;
        r4 = r5.mListeners;
        monitor-enter(r4);
        r2 = new java.util.ArrayList;	 Catch:{ all -> 0x002d }
        r3 = r5.mListeners;	 Catch:{ all -> 0x002d }
        r3 = r3.entrySet();	 Catch:{ all -> 0x002d }
        r2.<init>(r3);	 Catch:{ all -> 0x002d }
        monitor-exit(r4);
        r1 = r2.iterator();
    L_0x0013:
        r3 = r1.hasNext();
        if (r3 == 0) goto L_0x003f;
    L_0x0019:
        r0 = r1.next();
        r0 = (java.util.Map.Entry) r0;
        r3 = r0.getValue();
        if (r3 != 0) goto L_0x0030;
    L_0x0025:
        r3 = r0.getKey();
        r5.executeOperation(r6, r3);
        goto L_0x0013;
    L_0x002d:
        r3 = move-exception;
        monitor-exit(r4);
        throw r3;
    L_0x0030:
        r3 = r0.getValue();
        r3 = (android.os.Handler) r3;
        r4 = new android.location.LocalListenerHelper$1;
        r4.<init>(r5, r6, r0);
        r3.post(r4);
        goto L_0x0013;
    L_0x003f:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.LocalListenerHelper.foreach(android.location.LocalListenerHelper$ListenerOperation):void");
    }
}
