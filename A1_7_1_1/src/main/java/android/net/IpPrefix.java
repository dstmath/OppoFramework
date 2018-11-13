package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Pair;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public final class IpPrefix implements Parcelable {
    public static final Creator<IpPrefix> CREATOR = null;
    private final byte[] address;
    private final int prefixLength;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.IpPrefix.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.IpPrefix.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.IpPrefix.<clinit>():void");
    }

    private void checkAndMaskAddressAndPrefixLength() {
        if (this.address.length == 4 || this.address.length == 16) {
            NetworkUtils.maskRawAddress(this.address, this.prefixLength);
            return;
        }
        throw new IllegalArgumentException("IpPrefix has " + this.address.length + " bytes which is neither 4 nor 16");
    }

    public IpPrefix(byte[] address, int prefixLength) {
        this.address = (byte[]) address.clone();
        this.prefixLength = prefixLength;
        checkAndMaskAddressAndPrefixLength();
    }

    public IpPrefix(InetAddress address, int prefixLength) {
        this.address = address.getAddress();
        this.prefixLength = prefixLength;
        checkAndMaskAddressAndPrefixLength();
    }

    public IpPrefix(String prefix) {
        Pair<InetAddress, Integer> ipAndMask = NetworkUtils.parseIpAndMask(prefix);
        this.address = ((InetAddress) ipAndMask.first).getAddress();
        this.prefixLength = ((Integer) ipAndMask.second).intValue();
        checkAndMaskAddressAndPrefixLength();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof IpPrefix)) {
            return false;
        }
        IpPrefix that = (IpPrefix) obj;
        if (Arrays.equals(this.address, that.address) && this.prefixLength == that.prefixLength) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Arrays.hashCode(this.address) + (this.prefixLength * 11);
    }

    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(this.address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public byte[] getRawAddress() {
        return (byte[]) this.address.clone();
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public boolean contains(InetAddress address) {
        byte[] addrBytes = null;
        if (address != null) {
            addrBytes = address.getAddress();
        }
        if (addrBytes == null || addrBytes.length != this.address.length) {
            return false;
        }
        NetworkUtils.maskRawAddress(addrBytes, this.prefixLength);
        return Arrays.equals(this.address, addrBytes);
    }

    public String toString() {
        try {
            return InetAddress.getByAddress(this.address).getHostAddress() + "/" + this.prefixLength;
        } catch (UnknownHostException e) {
            throw new IllegalStateException("IpPrefix with invalid address! Shouldn't happen.", e);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.address);
        dest.writeInt(this.prefixLength);
    }
}
