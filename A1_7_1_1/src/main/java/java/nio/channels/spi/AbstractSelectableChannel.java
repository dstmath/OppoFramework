package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

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
public abstract class AbstractSelectableChannel extends SelectableChannel {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f55-assertionsDisabled = false;
    boolean blocking;
    private int keyCount;
    private final Object keyLock;
    private SelectionKey[] keys;
    private final SelectorProvider provider;
    private final Object regLock;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.nio.channels.spi.AbstractSelectableChannel.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.nio.channels.spi.AbstractSelectableChannel.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.channels.spi.AbstractSelectableChannel.<clinit>():void");
    }

    protected abstract void implCloseSelectableChannel() throws IOException;

    protected abstract void implConfigureBlocking(boolean z) throws IOException;

    protected AbstractSelectableChannel(SelectorProvider provider) {
        this.keys = null;
        this.keyCount = 0;
        this.keyLock = new Object();
        this.regLock = new Object();
        this.blocking = true;
        this.provider = provider;
    }

    public final SelectorProvider provider() {
        return this.provider;
    }

    private void addKey(SelectionKey k) {
        if (f55-assertionsDisabled || Thread.holdsLock(this.keyLock)) {
            int i = 0;
            if (this.keys != null && this.keyCount < this.keys.length) {
                i = 0;
                while (i < this.keys.length && this.keys[i] != null) {
                    i++;
                }
            } else if (this.keys == null) {
                this.keys = new SelectionKey[3];
            } else {
                SelectionKey[] ks = new SelectionKey[(this.keys.length * 2)];
                for (i = 0; i < this.keys.length; i++) {
                    ks[i] = this.keys[i];
                }
                this.keys = ks;
                i = this.keyCount;
            }
            this.keys[i] = k;
            this.keyCount++;
            return;
        }
        throw new AssertionError();
    }

    private SelectionKey findKey(Selector sel) {
        synchronized (this.keyLock) {
            if (this.keys == null) {
                return null;
            }
            int i = 0;
            while (i < this.keys.length) {
                if (this.keys[i] == null || this.keys[i].selector() != sel) {
                    i++;
                } else {
                    SelectionKey selectionKey = this.keys[i];
                    return selectionKey;
                }
            }
            return null;
        }
    }

    void removeKey(SelectionKey k) {
        synchronized (this.keyLock) {
            for (int i = 0; i < this.keys.length; i++) {
                if (this.keys[i] == k) {
                    this.keys[i] = null;
                    this.keyCount--;
                }
            }
            ((AbstractSelectionKey) k).invalidate();
        }
    }

    private boolean haveValidKeys() {
        synchronized (this.keyLock) {
            if (this.keyCount == 0) {
                return false;
            }
            int i = 0;
            while (i < this.keys.length) {
                if (this.keys[i] == null || !this.keys[i].isValid()) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public final boolean isRegistered() {
        boolean z = false;
        synchronized (this.keyLock) {
            if (this.keyCount != 0) {
                z = true;
            }
        }
        return z;
    }

    public final SelectionKey keyFor(Selector sel) {
        return findKey(sel);
    }

    public final SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException {
        SelectionKey k;
        synchronized (this.regLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (((~validOps()) & ops) != 0) {
                throw new IllegalArgumentException();
            } else if (this.blocking) {
                throw new IllegalBlockingModeException();
            } else {
                k = findKey(sel);
                if (k != null) {
                    k.interestOps(ops);
                    k.attach(att);
                }
                if (k == null) {
                    synchronized (this.keyLock) {
                        if (isOpen()) {
                            k = ((AbstractSelector) sel).register(this, ops, att);
                            addKey(k);
                        } else {
                            throw new ClosedChannelException();
                        }
                    }
                }
            }
        }
        return k;
    }

    protected final void implCloseChannel() throws IOException {
        implCloseSelectableChannel();
        synchronized (this.keyLock) {
            int count = this.keys == null ? 0 : this.keys.length;
            for (int i = 0; i < count; i++) {
                SelectionKey k = this.keys[i];
                if (k != null) {
                    k.cancel();
                }
            }
        }
    }

    public final boolean isBlocking() {
        boolean z;
        synchronized (this.regLock) {
            z = this.blocking;
        }
        return z;
    }

    public final Object blockingLock() {
        return this.regLock;
    }

    public final SelectableChannel configureBlocking(boolean block) throws IOException {
        synchronized (this.regLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (this.blocking == block) {
                return this;
            } else {
                if (block) {
                    if (haveValidKeys()) {
                        throw new IllegalBlockingModeException();
                    }
                }
                implConfigureBlocking(block);
                this.blocking = block;
                return this;
            }
        }
    }
}
