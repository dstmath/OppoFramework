package java.util;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import sun.util.locale.LanguageTag;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class UUID implements Serializable, Comparable<UUID> {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f79-assertionsDisabled = false;
    private static final long serialVersionUID = -4856846361193249489L;
    private final long leastSigBits;
    private final long mostSigBits;

    private static class Holder {
        static final SecureRandom numberGenerator = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.UUID.Holder.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.UUID.Holder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.UUID.Holder.<clinit>():void");
        }

        private Holder() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.UUID.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.UUID.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.UUID.<clinit>():void");
    }

    private UUID(byte[] data) {
        int i;
        long msb = 0;
        long lsb = 0;
        if (!f79-assertionsDisabled) {
            if ((data.length == 16 ? 1 : null) == null) {
                throw new AssertionError((Object) "data must be 16 bytes in length");
            }
        }
        for (i = 0; i < 8; i++) {
            msb = (msb << 8) | ((long) (data[i] & 255));
        }
        for (i = 8; i < 16; i++) {
            lsb = (lsb << 8) | ((long) (data[i] & 255));
        }
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    public static UUID randomUUID() {
        byte[] randomBytes = new byte[16];
        Holder.numberGenerator.nextBytes(randomBytes);
        randomBytes[6] = (byte) (randomBytes[6] & 15);
        randomBytes[6] = (byte) (randomBytes[6] | 64);
        randomBytes[8] = (byte) (randomBytes[8] & 63);
        randomBytes[8] = (byte) (randomBytes[8] | 128);
        return new UUID(randomBytes);
    }

    public static UUID nameUUIDFromBytes(byte[] name) {
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(name);
            md5Bytes[6] = (byte) (md5Bytes[6] & 15);
            md5Bytes[6] = (byte) (md5Bytes[6] | 48);
            md5Bytes[8] = (byte) (md5Bytes[8] & 63);
            md5Bytes[8] = (byte) (md5Bytes[8] | 128);
            return new UUID(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not supported");
        }
    }

    public static UUID fromString(String name) {
        String[] components = name.split(LanguageTag.SEP);
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }
        return new UUID((((Long.decode(components[0]).longValue() << 16) | Long.decode(components[1]).longValue()) << 16) | Long.decode(components[2]).longValue(), (Long.decode(components[3]).longValue() << 48) | Long.decode(components[4]).longValue());
    }

    public long getLeastSignificantBits() {
        return this.leastSigBits;
    }

    public long getMostSignificantBits() {
        return this.mostSigBits;
    }

    public int version() {
        return (int) ((this.mostSigBits >> 12) & 15);
    }

    public int variant() {
        return (int) ((this.leastSigBits >>> ((int) (64 - (this.leastSigBits >>> 62)))) & (this.leastSigBits >> 63));
    }

    public long timestamp() {
        if (version() == 1) {
            return (((this.mostSigBits & 4095) << 48) | (((this.mostSigBits >> 16) & 65535) << 32)) | (this.mostSigBits >>> 32);
        }
        throw new UnsupportedOperationException("Not a time-based UUID");
    }

    public int clockSequence() {
        if (version() == 1) {
            return (int) ((this.leastSigBits & 4611404543450677248L) >>> 48);
        }
        throw new UnsupportedOperationException("Not a time-based UUID");
    }

    public long node() {
        if (version() == 1) {
            return this.leastSigBits & 281474976710655L;
        }
        throw new UnsupportedOperationException("Not a time-based UUID");
    }

    public String toString() {
        return digits(this.mostSigBits >> 32, 8) + LanguageTag.SEP + digits(this.mostSigBits >> 16, 4) + LanguageTag.SEP + digits(this.mostSigBits, 4) + LanguageTag.SEP + digits(this.leastSigBits >> 48, 4) + LanguageTag.SEP + digits(this.leastSigBits, 12);
    }

    private static String digits(long val, int digits) {
        long hi = 1 << (digits * 4);
        return Long.toHexString(((hi - 1) & val) | hi).substring(1);
    }

    public int hashCode() {
        long hilo = this.mostSigBits ^ this.leastSigBits;
        return ((int) (hilo >> 32)) ^ ((int) hilo);
    }

    public boolean equals(Object obj) {
        boolean z = f79-assertionsDisabled;
        if (obj == null || obj.getClass() != UUID.class) {
            return f79-assertionsDisabled;
        }
        UUID id = (UUID) obj;
        if (this.mostSigBits == id.mostSigBits && this.leastSigBits == id.leastSigBits) {
            z = true;
        }
        return z;
    }

    public int compareTo(UUID val) {
        if (this.mostSigBits < val.mostSigBits) {
            return -1;
        }
        if (this.mostSigBits > val.mostSigBits) {
            return 1;
        }
        if (this.leastSigBits < val.leastSigBits) {
            return -1;
        }
        if (this.leastSigBits > val.leastSigBits) {
            return 1;
        }
        return 0;
    }
}
