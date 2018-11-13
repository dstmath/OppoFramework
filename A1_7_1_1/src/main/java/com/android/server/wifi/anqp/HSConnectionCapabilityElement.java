package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSConnectionCapabilityElement extends ANQPElement {
    private final List<ProtocolTuple> mStatusList = new ArrayList();

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
    public enum ProtoStatus {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus.<clinit>():void");
        }
    }

    public static class ProtocolTuple {
        private final int mPort;
        private final int mProtocol;
        private final ProtoStatus mStatus;

        /* synthetic */ ProtocolTuple(ByteBuffer payload, ProtocolTuple protocolTuple) {
            this(payload);
        }

        private ProtocolTuple(ByteBuffer payload) throws ProtocolException {
            if (payload.remaining() < 4) {
                throw new ProtocolException("Runt protocol tuple: " + payload.remaining());
            }
            ProtoStatus protoStatus;
            this.mProtocol = payload.get() & 255;
            this.mPort = payload.getShort() & Constants.SHORT_MASK;
            int statusNumber = payload.get() & 255;
            if (statusNumber < ProtoStatus.values().length) {
                protoStatus = ProtoStatus.values()[statusNumber];
            } else {
                protoStatus = null;
            }
            this.mStatus = protoStatus;
        }

        public int getProtocol() {
            return this.mProtocol;
        }

        public int getPort() {
            return this.mPort;
        }

        public ProtoStatus getStatus() {
            return this.mStatus;
        }

        public String toString() {
            return "ProtocolTuple{mProtocol=" + this.mProtocol + ", mPort=" + this.mPort + ", mStatus=" + this.mStatus + '}';
        }
    }

    public HSConnectionCapabilityElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        while (payload.hasRemaining()) {
            this.mStatusList.add(new ProtocolTuple(payload, null));
        }
    }

    public List<ProtocolTuple> getStatusList() {
        return Collections.unmodifiableList(this.mStatusList);
    }

    public String toString() {
        return "HSConnectionCapability{mStatusList=" + this.mStatusList + '}';
    }
}
