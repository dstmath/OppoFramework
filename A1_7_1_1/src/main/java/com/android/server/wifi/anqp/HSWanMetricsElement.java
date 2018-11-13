package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class HSWanMetricsElement extends ANQPElement {
    private final boolean mCapped;
    private final int mDlLoad;
    private final long mDlSpeed;
    private final int mLMD;
    private final LinkStatus mStatus;
    private final boolean mSymmetric;
    private final int mUlLoad;
    private final long mUlSpeed;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum LinkStatus {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus.<clinit>():void");
        }
    }

    public HSWanMetricsElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        boolean z = true;
        super(infoID);
        if (payload.remaining() != 13) {
            throw new ProtocolException("Bad WAN metrics length: " + payload.remaining());
        }
        boolean z2;
        int status = payload.get() & 255;
        this.mStatus = LinkStatus.values()[status & 3];
        if ((status & 4) != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mSymmetric = z2;
        if ((status & 8) == 0) {
            z = false;
        }
        this.mCapped = z;
        this.mDlSpeed = ((long) payload.getInt()) & Constants.INT_MASK;
        this.mUlSpeed = ((long) payload.getInt()) & Constants.INT_MASK;
        this.mDlLoad = payload.get() & 255;
        this.mUlLoad = payload.get() & 255;
        this.mLMD = payload.getShort() & Constants.SHORT_MASK;
    }

    public LinkStatus getStatus() {
        return this.mStatus;
    }

    public boolean isSymmetric() {
        return this.mSymmetric;
    }

    public boolean isCapped() {
        return this.mCapped;
    }

    public long getDlSpeed() {
        return this.mDlSpeed;
    }

    public long getUlSpeed() {
        return this.mUlSpeed;
    }

    public int getDlLoad() {
        return this.mDlLoad;
    }

    public int getUlLoad() {
        return this.mUlLoad;
    }

    public int getLMD() {
        return this.mLMD;
    }

    public String toString() {
        return String.format("HSWanMetrics{mStatus=%s, mSymmetric=%s, mCapped=%s, mDlSpeed=%d, mUlSpeed=%d, mDlLoad=%f, mUlLoad=%f, mLMD=%d}", new Object[]{this.mStatus, Boolean.valueOf(this.mSymmetric), Boolean.valueOf(this.mCapped), Long.valueOf(this.mDlSpeed), Long.valueOf(this.mUlSpeed), Double.valueOf((((double) this.mDlLoad) * 100.0d) / 256.0d), Double.valueOf((((double) this.mUlLoad) * 100.0d) / 256.0d), Integer.valueOf(this.mLMD)});
    }
}
