package android.net.netlink;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class StructInetDiagReqV2Oppo {
    public static final int INET_DIAG_REQ_V2_ALL_STATES = -1;
    public static final int STRUCT_SIZE = 56;
    private final StructInetDiagSockId mId;
    private final byte mIdiagExt;
    private final byte mPad;
    private final byte mSdiagFamily;
    private final byte mSdiagProtocol;
    private final int mState;

    public StructInetDiagReqV2Oppo(int protocol, InetSocketAddress local, InetSocketAddress remote, int family) {
        this(protocol, local, remote, family, 0, 0, -1);
    }

    public StructInetDiagReqV2Oppo(int protocol, InetSocketAddress local, InetSocketAddress remote, int family, int pad, int extension, int state) throws NullPointerException {
        this.mSdiagFamily = (byte) family;
        this.mSdiagProtocol = (byte) protocol;
        if ((local == null) == (remote != null ? false : true)) {
            this.mId = (local == null || remote == null) ? null : new StructInetDiagSockId(local, remote);
            this.mPad = (byte) pad;
            this.mIdiagExt = (byte) extension;
            this.mState = state;
            return;
        }
        throw new NullPointerException("Local and remote must be both null or both non-null");
    }

    public void pack(ByteBuffer byteBuffer) {
        byteBuffer.put(this.mSdiagFamily);
        byteBuffer.put(this.mSdiagProtocol);
        byteBuffer.put(this.mIdiagExt);
        byteBuffer.put(this.mPad);
        byteBuffer.putInt(this.mState);
        StructInetDiagSockId structInetDiagSockId = this.mId;
        if (structInetDiagSockId != null) {
            structInetDiagSockId.pack(byteBuffer);
        }
    }

    public String toString() {
        String familyStr = NetlinkConstants.stringForAddressFamily(this.mSdiagFamily);
        String protocolStr = NetlinkConstants.stringForAddressFamily(this.mSdiagProtocol);
        StringBuilder sb = new StringBuilder();
        sb.append("StructInetDiagReqV2Oppo{ sdiag_family{");
        sb.append(familyStr);
        sb.append("}, sdiag_protocol{");
        sb.append(protocolStr);
        sb.append("}, idiag_ext{");
        sb.append((int) this.mIdiagExt);
        sb.append(")}, pad{");
        sb.append((int) this.mPad);
        sb.append("}, idiag_states{");
        sb.append(Integer.toHexString(this.mState));
        sb.append("}, ");
        StructInetDiagSockId structInetDiagSockId = this.mId;
        sb.append(structInetDiagSockId != null ? structInetDiagSockId.toString() : "inet_diag_sockid=null");
        sb.append("}");
        return sb.toString();
    }
}
