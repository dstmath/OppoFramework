package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkAuthenticationTypeElement extends ANQPElement {
    private final List<NetworkAuthentication> m_authenticationTypes = new ArrayList();

    public static class NetworkAuthentication {
        private final NwkAuthTypeEnum m_type;
        private final String m_url;

        /* synthetic */ NetworkAuthentication(NwkAuthTypeEnum type, String url, NetworkAuthentication networkAuthentication) {
            this(type, url);
        }

        private NetworkAuthentication(NwkAuthTypeEnum type, String url) {
            this.m_type = type;
            this.m_url = url;
        }

        public NwkAuthTypeEnum getType() {
            return this.m_type;
        }

        public String getURL() {
            return this.m_url;
        }

        public String toString() {
            return "NetworkAuthentication{m_type=" + this.m_type + ", m_url='" + this.m_url + '\'' + '}';
        }
    }

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
    public enum NwkAuthTypeEnum {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.NetworkAuthenticationTypeElement.NwkAuthTypeEnum.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.NetworkAuthenticationTypeElement.NwkAuthTypeEnum.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.NetworkAuthenticationTypeElement.NwkAuthTypeEnum.<clinit>():void");
        }
    }

    public NetworkAuthenticationTypeElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        while (payload.hasRemaining()) {
            NwkAuthTypeEnum type;
            int typeNumber = payload.get() & 255;
            if (typeNumber >= NwkAuthTypeEnum.values().length) {
                type = NwkAuthTypeEnum.Reserved;
            } else {
                type = NwkAuthTypeEnum.values()[typeNumber];
            }
            this.m_authenticationTypes.add(new NetworkAuthentication(type, Constants.getPrefixedString(payload, 2, StandardCharsets.UTF_8), null));
        }
    }

    public List<NetworkAuthentication> getAuthenticationTypes() {
        return Collections.unmodifiableList(this.m_authenticationTypes);
    }

    public String toString() {
        return "NetworkAuthenticationType{m_authenticationTypes=" + this.m_authenticationTypes + '}';
    }
}
