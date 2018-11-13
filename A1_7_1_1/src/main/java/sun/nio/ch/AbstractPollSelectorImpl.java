package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

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
abstract class AbstractPollSelectorImpl extends SelectorImpl {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f133-assertionsDisabled = false;
    protected final int INIT_CAP;
    protected SelectionKeyImpl[] channelArray;
    protected int channelOffset;
    private Object closeLock;
    private boolean closed;
    PollArrayWrapper pollWrapper;
    protected int totalChannels;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.AbstractPollSelectorImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.AbstractPollSelectorImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.AbstractPollSelectorImpl.<clinit>():void");
    }

    protected abstract int doSelect(long j) throws IOException;

    protected abstract void implCloseInterrupt() throws IOException;

    AbstractPollSelectorImpl(SelectorProvider sp, int channels, int offset) {
        super(sp);
        this.INIT_CAP = 10;
        this.channelOffset = 0;
        this.closed = false;
        this.closeLock = new Object();
        this.totalChannels = channels;
        this.channelOffset = offset;
    }

    void putEventOps(SelectionKeyImpl sk, int ops) {
        synchronized (this.closeLock) {
            if (this.closed) {
                throw new ClosedSelectorException();
            }
            this.pollWrapper.putEventOps(sk.getIndex(), ops);
        }
    }

    public Selector wakeup() {
        this.pollWrapper.interrupt();
        return this;
    }

    protected void implClose() throws IOException {
        synchronized (this.closeLock) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            for (int i = this.channelOffset; i < this.totalChannels; i++) {
                SelectionKeyImpl ski = this.channelArray[i];
                if (!f133-assertionsDisabled) {
                    if ((ski.getIndex() != -1 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                ski.setIndex(-1);
                deregister(ski);
                SelectableChannel selch = this.channelArray[i].channel();
                if (!(selch.isOpen() || selch.isRegistered())) {
                    ((SelChImpl) selch).kill();
                }
            }
            implCloseInterrupt();
            this.pollWrapper.free();
            this.pollWrapper = null;
            this.selectedKeys = null;
            this.channelArray = null;
            this.totalChannels = 0;
        }
    }

    protected int updateSelectedKeys() {
        int numKeysUpdated = 0;
        for (int i = this.channelOffset; i < this.totalChannels; i++) {
            int rOps = this.pollWrapper.getReventOps(i);
            if (rOps != 0) {
                SelectionKeyImpl sk = this.channelArray[i];
                this.pollWrapper.putReventOps(i, 0);
                if (!this.selectedKeys.contains(sk)) {
                    sk.channel.translateAndSetReadyOps(rOps, sk);
                    if ((sk.nioReadyOps() & sk.nioInterestOps()) != 0) {
                        this.selectedKeys.add(sk);
                        numKeysUpdated++;
                    }
                } else if (sk.channel.translateAndSetReadyOps(rOps, sk)) {
                    numKeysUpdated++;
                }
            }
        }
        return numKeysUpdated;
    }

    protected void implRegister(SelectionKeyImpl ski) {
        synchronized (this.closeLock) {
            if (this.closed) {
                throw new ClosedSelectorException();
            }
            if (this.channelArray.length == this.totalChannels) {
                int newSize = this.pollWrapper.totalChannels * 2;
                SelectionKeyImpl[] temp = new SelectionKeyImpl[newSize];
                for (int i = this.channelOffset; i < this.totalChannels; i++) {
                    temp[i] = this.channelArray[i];
                }
                this.channelArray = temp;
                this.pollWrapper.grow(newSize);
            }
            this.channelArray[this.totalChannels] = ski;
            ski.setIndex(this.totalChannels);
            this.pollWrapper.addEntry(ski.channel);
            this.totalChannels++;
            this.keys.add(ski);
        }
    }

    protected void implDereg(SelectionKeyImpl ski) throws IOException {
        Object obj = null;
        int i = ski.getIndex();
        if (!f133-assertionsDisabled) {
            if (i >= 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (i != this.totalChannels - 1) {
            SelectionKeyImpl endChannel = this.channelArray[this.totalChannels - 1];
            this.channelArray[i] = endChannel;
            endChannel.setIndex(i);
            this.pollWrapper.release(i);
            PollArrayWrapper.replaceEntry(this.pollWrapper, this.totalChannels - 1, this.pollWrapper, i);
        } else {
            this.pollWrapper.release(i);
        }
        this.channelArray[this.totalChannels - 1] = null;
        this.totalChannels--;
        PollArrayWrapper pollArrayWrapper = this.pollWrapper;
        pollArrayWrapper.totalChannels--;
        ski.setIndex(-1);
        this.keys.remove(ski);
        this.selectedKeys.remove(ski);
        deregister(ski);
        SelectableChannel selch = ski.channel();
        if (!selch.isOpen() && !selch.isRegistered()) {
            ((SelChImpl) selch).kill();
        }
    }
}
