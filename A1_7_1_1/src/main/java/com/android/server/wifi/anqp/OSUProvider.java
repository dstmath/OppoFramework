package com.android.server.wifi.anqp;

import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OSUProvider {
    private final int mHashCode;
    private final List<IconInfo> mIcons;
    private final List<I18Name> mNames;
    private final List<OSUMethod> mOSUMethods;
    private final String mOSUServer;
    private final String mOsuNai;
    private final List<I18Name> mServiceDescriptions;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
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
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
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
    public enum OSUMethod {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.OSUProvider.OSUMethod.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.anqp.OSUProvider.OSUMethod.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.OSUProvider.OSUMethod.<clinit>():void");
        }
    }

    public OSUProvider(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() < 11) {
            throw new ProtocolException("Truncated OSU provider: " + payload.remaining());
        }
        int length = payload.getShort() & Constants.SHORT_MASK;
        int namesLength = payload.getShort() & Constants.SHORT_MASK;
        ByteBuffer namesBuffer = payload.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        namesBuffer.limit(namesBuffer.position() + namesLength);
        payload.position(payload.position() + namesLength);
        this.mNames = new ArrayList();
        while (namesBuffer.hasRemaining()) {
            this.mNames.add(new I18Name(namesBuffer));
        }
        this.mOSUServer = Constants.getPrefixedString(payload, 1, StandardCharsets.UTF_8);
        int methodLength = payload.get() & 255;
        this.mOSUMethods = new ArrayList(methodLength);
        while (methodLength > 0) {
            Object obj;
            int methodID = payload.get() & 255;
            List list = this.mOSUMethods;
            if (methodID < OSUMethod.values().length) {
                obj = OSUMethod.values()[methodID];
            } else {
                obj = null;
            }
            list.add(obj);
            methodLength--;
        }
        int iconsLength = payload.getShort() & Constants.SHORT_MASK;
        ByteBuffer iconsBuffer = payload.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        iconsBuffer.limit(iconsBuffer.position() + iconsLength);
        payload.position(payload.position() + iconsLength);
        this.mIcons = new ArrayList();
        while (iconsBuffer.hasRemaining()) {
            this.mIcons.add(new IconInfo(iconsBuffer));
        }
        this.mOsuNai = Constants.getPrefixedString(payload, 1, StandardCharsets.UTF_8, true);
        int descriptionsLength = payload.getShort() & Constants.SHORT_MASK;
        ByteBuffer descriptionsBuffer = payload.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        descriptionsBuffer.limit(descriptionsBuffer.position() + descriptionsLength);
        payload.position(payload.position() + descriptionsLength);
        this.mServiceDescriptions = new ArrayList();
        while (descriptionsBuffer.hasRemaining()) {
            this.mServiceDescriptions.add(new I18Name(descriptionsBuffer));
        }
        this.mHashCode = (((((((((this.mNames.hashCode() * 31) + this.mOSUServer.hashCode()) * 31) + this.mOSUMethods.hashCode()) * 31) + this.mIcons.hashCode()) * 31) + (this.mOsuNai != null ? this.mOsuNai.hashCode() : 0)) * 31) + this.mServiceDescriptions.hashCode();
    }

    public List<I18Name> getNames() {
        return this.mNames;
    }

    public String getOSUServer() {
        return this.mOSUServer;
    }

    public List<OSUMethod> getOSUMethods() {
        return this.mOSUMethods;
    }

    public List<IconInfo> getIcons() {
        return this.mIcons;
    }

    public String getOsuNai() {
        return this.mOsuNai;
    }

    public List<I18Name> getServiceDescriptions() {
        return this.mServiceDescriptions;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OSUProvider that = (OSUProvider) o;
        if (this.mOSUServer.equals(that.mOSUServer) && this.mNames.equals(that.mNames) && this.mServiceDescriptions.equals(that.mServiceDescriptions) && this.mIcons.equals(that.mIcons) && this.mOSUMethods.equals(that.mOSUMethods)) {
            return this.mOsuNai == null ? that.mOsuNai == null : this.mOsuNai.equals(that.mOsuNai);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.mHashCode;
    }

    public String toString() {
        return "OSUProvider{names=" + this.mNames + ", OSUServer='" + this.mOSUServer + '\'' + ", OSUMethods=" + this.mOSUMethods + ", icons=" + this.mIcons + ", NAI='" + this.mOsuNai + '\'' + ", serviceDescriptions=" + this.mServiceDescriptions + '}';
    }
}
