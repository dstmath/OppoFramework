package android.view.textservice;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.textservice.ISpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager;
import com.android.internal.textservice.ITextServicesSessionListener;
import com.android.internal.textservice.ITextServicesSessionListener.Stub;
import java.util.LinkedList;
import java.util.Queue;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SpellCheckerSession {
    private static final boolean DBG = false;
    private static final int MSG_ON_GET_SUGGESTION_MULTIPLE = 1;
    private static final int MSG_ON_GET_SUGGESTION_MULTIPLE_FOR_SENTENCE = 2;
    public static final String SERVICE_META_DATA = "android.view.textservice.scs";
    private static final String TAG = null;
    private final Handler mHandler;
    private final InternalListener mInternalListener;
    private boolean mIsUsed;
    private final SpellCheckerInfo mSpellCheckerInfo;
    private final SpellCheckerSessionListener mSpellCheckerSessionListener;
    private final SpellCheckerSessionListenerImpl mSpellCheckerSessionListenerImpl;
    private final SpellCheckerSubtype mSubtype;
    private final ITextServicesManager mTextServicesManager;

    private static class InternalListener extends Stub {
        private final SpellCheckerSessionListenerImpl mParentSpellCheckerSessionListenerImpl;

        public InternalListener(SpellCheckerSessionListenerImpl spellCheckerSessionListenerImpl) {
            this.mParentSpellCheckerSessionListenerImpl = spellCheckerSessionListenerImpl;
        }

        public void onServiceConnected(ISpellCheckerSession session) {
            this.mParentSpellCheckerSessionListenerImpl.onServiceConnected(session);
        }
    }

    public interface SpellCheckerSessionListener {
        void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfoArr);

        void onGetSuggestions(SuggestionsInfo[] suggestionsInfoArr);
    }

    private static class SpellCheckerSessionListenerImpl extends ISpellCheckerSessionListener.Stub {
        private static final int STATE_CLOSED_AFTER_CONNECTION = 2;
        private static final int STATE_CLOSED_BEFORE_CONNECTION = 3;
        private static final int STATE_CONNECTED = 1;
        private static final int STATE_WAIT_CONNECTION = 0;
        private static final int TASK_CANCEL = 1;
        private static final int TASK_CLOSE = 3;
        private static final int TASK_GET_SUGGESTIONS_MULTIPLE = 2;
        private static final int TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE = 4;
        private Handler mAsyncHandler;
        private Handler mHandler;
        private ISpellCheckerSession mISpellCheckerSession;
        private final Queue<SpellCheckerParams> mPendingTasks;
        private int mState;
        private HandlerThread mThread;

        /* renamed from: android.view.textservice.SpellCheckerSession$SpellCheckerSessionListenerImpl$1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ SpellCheckerSessionListenerImpl this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.1.<init>(android.view.textservice.SpellCheckerSession$SpellCheckerSessionListenerImpl, android.os.Looper):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl r1, android.os.Looper r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.1.<init>(android.view.textservice.SpellCheckerSession$SpellCheckerSessionListenerImpl, android.os.Looper):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.1.<init>(android.view.textservice.SpellCheckerSession$SpellCheckerSessionListenerImpl, android.os.Looper):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.1.handleMessage(android.os.Message):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void handleMessage(android.os.Message r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.1.handleMessage(android.os.Message):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.1.handleMessage(android.os.Message):void");
            }
        }

        private static class SpellCheckerParams {
            public final boolean mSequentialWords;
            public ISpellCheckerSession mSession;
            public final int mSuggestionsLimit;
            public final TextInfo[] mTextInfos;
            public final int mWhat;

            public SpellCheckerParams(int what, TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
                this.mWhat = what;
                this.mTextInfos = textInfos;
                this.mSuggestionsLimit = suggestionsLimit;
                this.mSequentialWords = sequentialWords;
            }
        }

        private static String taskToString(int task) {
            switch (task) {
                case 1:
                    return "TASK_CANCEL";
                case 2:
                    return "TASK_GET_SUGGESTIONS_MULTIPLE";
                case 3:
                    return "TASK_CLOSE";
                case 4:
                    return "TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE";
                default:
                    return "Unexpected task=" + task;
            }
        }

        private static String stateToString(int state) {
            switch (state) {
                case 0:
                    return "STATE_WAIT_CONNECTION";
                case 1:
                    return "STATE_CONNECTED";
                case 2:
                    return "STATE_CLOSED_AFTER_CONNECTION";
                case 3:
                    return "STATE_CLOSED_BEFORE_CONNECTION";
                default:
                    return "Unexpected state=" + state;
            }
        }

        public SpellCheckerSessionListenerImpl(Handler handler) {
            this.mPendingTasks = new LinkedList();
            this.mState = 0;
            this.mHandler = handler;
        }

        private void processTask(ISpellCheckerSession session, SpellCheckerParams scp, boolean async) {
            if (async || this.mAsyncHandler == null) {
                switch (scp.mWhat) {
                    case 1:
                        try {
                            session.onCancel();
                            break;
                        } catch (RemoteException e) {
                            Log.e(SpellCheckerSession.TAG, "Failed to cancel " + e);
                            break;
                        }
                    case 2:
                        try {
                            session.onGetSuggestionsMultiple(scp.mTextInfos, scp.mSuggestionsLimit, scp.mSequentialWords);
                            break;
                        } catch (RemoteException e2) {
                            Log.e(SpellCheckerSession.TAG, "Failed to get suggestions " + e2);
                            break;
                        }
                    case 3:
                        try {
                            session.onClose();
                            break;
                        } catch (RemoteException e22) {
                            Log.e(SpellCheckerSession.TAG, "Failed to close " + e22);
                            break;
                        }
                    case 4:
                        try {
                            session.onGetSentenceSuggestionsMultiple(scp.mTextInfos, scp.mSuggestionsLimit);
                            break;
                        } catch (RemoteException e222) {
                            Log.e(SpellCheckerSession.TAG, "Failed to get suggestions " + e222);
                            break;
                        }
                }
            }
            scp.mSession = session;
            this.mAsyncHandler.sendMessage(Message.obtain(this.mAsyncHandler, 1, scp));
            if (scp.mWhat == 3) {
                synchronized (this) {
                    processCloseLocked();
                }
            }
        }

        private void processCloseLocked() {
            this.mISpellCheckerSession = null;
            if (this.mThread != null) {
                this.mThread.quit();
            }
            this.mHandler = null;
            this.mPendingTasks.clear();
            this.mThread = null;
            this.mAsyncHandler = null;
            switch (this.mState) {
                case 0:
                    this.mState = 3;
                    return;
                case 1:
                    this.mState = 2;
                    return;
                default:
                    Log.e(SpellCheckerSession.TAG, "processCloseLocked is called unexpectedly. mState=" + stateToString(this.mState));
                    return;
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public synchronized void onServiceConnected(com.android.internal.textservice.ISpellCheckerSession r4) {
            /*
            r3 = this;
            monitor-enter(r3);
            monitor-enter(r3);	 Catch:{ all -> 0x0082 }
            r0 = r3.mState;	 Catch:{ all -> 0x0085 }
            switch(r0) {
                case 0: goto L_0x002e;
                case 1: goto L_0x0007;
                case 2: goto L_0x0007;
                case 3: goto L_0x002b;
                default: goto L_0x0007;
            };	 Catch:{ all -> 0x0085 }
        L_0x0007:
            r0 = android.view.textservice.SpellCheckerSession.TAG;	 Catch:{ all -> 0x0085 }
            r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0085 }
            r1.<init>();	 Catch:{ all -> 0x0085 }
            r2 = "ignoring onServiceConnected due to unexpected mState=";	 Catch:{ all -> 0x0085 }
            r1 = r1.append(r2);	 Catch:{ all -> 0x0085 }
            r2 = r3.mState;	 Catch:{ all -> 0x0085 }
            r2 = stateToString(r2);	 Catch:{ all -> 0x0085 }
            r1 = r1.append(r2);	 Catch:{ all -> 0x0085 }
            r1 = r1.toString();	 Catch:{ all -> 0x0085 }
            android.util.Log.e(r0, r1);	 Catch:{ all -> 0x0085 }
            monitor-exit(r3);	 Catch:{ all -> 0x0082 }
            monitor-exit(r3);
            return;
        L_0x002b:
            monitor-exit(r3);	 Catch:{ all -> 0x0082 }
            monitor-exit(r3);
            return;
        L_0x002e:
            if (r4 != 0) goto L_0x003d;
        L_0x0030:
            r0 = android.view.textservice.SpellCheckerSession.TAG;	 Catch:{ all -> 0x0085 }
            r1 = "ignoring onServiceConnected due to session=null";	 Catch:{ all -> 0x0085 }
            android.util.Log.e(r0, r1);	 Catch:{ all -> 0x0085 }
            monitor-exit(r3);	 Catch:{ all -> 0x0082 }
            monitor-exit(r3);
            return;
        L_0x003d:
            r3.mISpellCheckerSession = r4;	 Catch:{ all -> 0x0085 }
            r0 = r4.asBinder();	 Catch:{ all -> 0x0085 }
            r0 = r0 instanceof android.os.Binder;	 Catch:{ all -> 0x0085 }
            if (r0 == 0) goto L_0x0069;	 Catch:{ all -> 0x0085 }
        L_0x0047:
            r0 = r3.mThread;	 Catch:{ all -> 0x0085 }
            if (r0 != 0) goto L_0x0069;	 Catch:{ all -> 0x0085 }
        L_0x004b:
            r0 = new android.os.HandlerThread;	 Catch:{ all -> 0x0085 }
            r1 = "SpellCheckerSession";	 Catch:{ all -> 0x0085 }
            r2 = 10;	 Catch:{ all -> 0x0085 }
            r0.<init>(r1, r2);	 Catch:{ all -> 0x0085 }
            r3.mThread = r0;	 Catch:{ all -> 0x0085 }
            r0 = r3.mThread;	 Catch:{ all -> 0x0085 }
            r0.start();	 Catch:{ all -> 0x0085 }
            r0 = new android.view.textservice.SpellCheckerSession$SpellCheckerSessionListenerImpl$1;	 Catch:{ all -> 0x0085 }
            r1 = r3.mThread;	 Catch:{ all -> 0x0085 }
            r1 = r1.getLooper();	 Catch:{ all -> 0x0085 }
            r0.<init>(r3, r1);	 Catch:{ all -> 0x0085 }
            r3.mAsyncHandler = r0;	 Catch:{ all -> 0x0085 }
        L_0x0069:
            r0 = 1;	 Catch:{ all -> 0x0085 }
            r3.mState = r0;	 Catch:{ all -> 0x0085 }
            monitor-exit(r3);	 Catch:{ all -> 0x0082 }
        L_0x006d:
            r0 = r3.mPendingTasks;	 Catch:{ all -> 0x0082 }
            r0 = r0.isEmpty();	 Catch:{ all -> 0x0082 }
            if (r0 != 0) goto L_0x0088;	 Catch:{ all -> 0x0082 }
        L_0x0075:
            r0 = r3.mPendingTasks;	 Catch:{ all -> 0x0082 }
            r0 = r0.poll();	 Catch:{ all -> 0x0082 }
            r0 = (android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.SpellCheckerParams) r0;	 Catch:{ all -> 0x0082 }
            r1 = 0;	 Catch:{ all -> 0x0082 }
            r3.processTask(r4, r0, r1);	 Catch:{ all -> 0x0082 }
            goto L_0x006d;
        L_0x0082:
            r0 = move-exception;
            monitor-exit(r3);
            throw r0;
        L_0x0085:
            r0 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x0082 }
            throw r0;	 Catch:{ all -> 0x0082 }
        L_0x0088:
            monitor-exit(r3);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.onServiceConnected(com.android.internal.textservice.ISpellCheckerSession):void");
        }

        public void cancel() {
            processOrEnqueueTask(new SpellCheckerParams(1, null, 0, false));
        }

        public void getSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
            processOrEnqueueTask(new SpellCheckerParams(2, textInfos, suggestionsLimit, sequentialWords));
        }

        public void getSentenceSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit) {
            processOrEnqueueTask(new SpellCheckerParams(4, textInfos, suggestionsLimit, false));
        }

        public void close() {
            processOrEnqueueTask(new SpellCheckerParams(3, null, 0, false));
        }

        public boolean isDisconnected() {
            boolean z = true;
            synchronized (this) {
                if (this.mState == 1) {
                    z = false;
                }
            }
            return z;
        }

        /* JADX WARNING: Missing block: B:31:0x0075, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void processOrEnqueueTask(SpellCheckerParams scp) {
            synchronized (this) {
                if (this.mState != 0 && this.mState != 1) {
                    Log.e(SpellCheckerSession.TAG, "ignoring processOrEnqueueTask due to unexpected mState=" + taskToString(scp.mWhat) + " scp.mWhat=" + taskToString(scp.mWhat));
                } else if (this.mState != 0) {
                    ISpellCheckerSession session = this.mISpellCheckerSession;
                    processTask(session, scp, false);
                } else if (scp.mWhat == 3) {
                    processCloseLocked();
                } else {
                    Object closeTask = null;
                    if (scp.mWhat == 1) {
                        while (!this.mPendingTasks.isEmpty()) {
                            SpellCheckerParams tmp = (SpellCheckerParams) this.mPendingTasks.poll();
                            if (tmp.mWhat == 3) {
                                SpellCheckerParams closeTask2 = tmp;
                            }
                        }
                    }
                    this.mPendingTasks.offer(scp);
                    if (closeTask != null) {
                        this.mPendingTasks.offer(closeTask);
                    }
                }
            }
        }

        public void onGetSuggestions(SuggestionsInfo[] results) {
            synchronized (this) {
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(Message.obtain(this.mHandler, 1, results));
                }
            }
        }

        public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
            synchronized (this) {
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(Message.obtain(this.mHandler, 2, results));
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.textservice.SpellCheckerSession.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.textservice.SpellCheckerSession.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SpellCheckerSession.<clinit>():void");
    }

    public SpellCheckerSession(SpellCheckerInfo info, ITextServicesManager tsm, SpellCheckerSessionListener listener, SpellCheckerSubtype subtype) {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        SpellCheckerSession.this.handleOnGetSuggestionsMultiple((SuggestionsInfo[]) msg.obj);
                        return;
                    case 2:
                        SpellCheckerSession.this.handleOnGetSentenceSuggestionsMultiple((SentenceSuggestionsInfo[]) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        if (info == null || listener == null || tsm == null) {
            throw new NullPointerException();
        }
        this.mSpellCheckerInfo = info;
        this.mSpellCheckerSessionListenerImpl = new SpellCheckerSessionListenerImpl(this.mHandler);
        this.mInternalListener = new InternalListener(this.mSpellCheckerSessionListenerImpl);
        this.mTextServicesManager = tsm;
        this.mIsUsed = true;
        this.mSpellCheckerSessionListener = listener;
        this.mSubtype = subtype;
    }

    public boolean isSessionDisconnected() {
        return this.mSpellCheckerSessionListenerImpl.isDisconnected();
    }

    public SpellCheckerInfo getSpellChecker() {
        return this.mSpellCheckerInfo;
    }

    public void cancel() {
        this.mSpellCheckerSessionListenerImpl.cancel();
    }

    public void close() {
        this.mIsUsed = false;
        try {
            this.mSpellCheckerSessionListenerImpl.close();
            this.mTextServicesManager.finishSpellCheckerService(this.mSpellCheckerSessionListenerImpl);
        } catch (RemoteException e) {
        }
    }

    public void getSentenceSuggestions(TextInfo[] textInfos, int suggestionsLimit) {
        this.mSpellCheckerSessionListenerImpl.getSentenceSuggestionsMultiple(textInfos, suggestionsLimit);
    }

    @Deprecated
    public void getSuggestions(TextInfo textInfo, int suggestionsLimit) {
        TextInfo[] textInfoArr = new TextInfo[1];
        textInfoArr[0] = textInfo;
        getSuggestions(textInfoArr, suggestionsLimit, false);
    }

    @Deprecated
    public void getSuggestions(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
        this.mSpellCheckerSessionListenerImpl.getSuggestionsMultiple(textInfos, suggestionsLimit, sequentialWords);
    }

    private void handleOnGetSuggestionsMultiple(SuggestionsInfo[] suggestionInfos) {
        this.mSpellCheckerSessionListener.onGetSuggestions(suggestionInfos);
    }

    private void handleOnGetSentenceSuggestionsMultiple(SentenceSuggestionsInfo[] suggestionInfos) {
        this.mSpellCheckerSessionListener.onGetSentenceSuggestions(suggestionInfos);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (this.mIsUsed) {
            Log.e(TAG, "SpellCheckerSession was not finished properly.You should call finishShession() when you finished to use a spell checker.");
            close();
        }
    }

    public ITextServicesSessionListener getTextServicesSessionListener() {
        return this.mInternalListener;
    }

    public ISpellCheckerSessionListener getSpellCheckerSessionListener() {
        return this.mSpellCheckerSessionListenerImpl;
    }
}
