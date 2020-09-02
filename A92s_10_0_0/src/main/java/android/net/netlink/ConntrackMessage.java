package android.net.netlink;

import android.system.OsConstants;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ConntrackMessage extends NetlinkMessage {
    public static final short CTA_IP_V4_DST = 2;
    public static final short CTA_IP_V4_SRC = 1;
    public static final short CTA_PROTO_DST_PORT = 3;
    public static final short CTA_PROTO_NUM = 1;
    public static final short CTA_PROTO_SRC_PORT = 2;
    public static final short CTA_TIMEOUT = 7;
    public static final short CTA_TUPLE_IP = 1;
    public static final short CTA_TUPLE_ORIG = 1;
    public static final short CTA_TUPLE_PROTO = 2;
    public static final short CTA_TUPLE_REPLY = 2;
    public static final short IPCTNL_MSG_CT_NEW = 0;
    public static final short NFNL_SUBSYS_CTNETLINK = 1;
    public static final int STRUCT_SIZE = 20;
    protected StructNfGenMsg mNfGenMsg = new StructNfGenMsg((byte) OsConstants.AF_INET);

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.net.netlink.StructNlAttr.<init>(short, android.net.netlink.StructNlAttr[]):void
     arg types: [int, android.net.netlink.StructNlAttr[]]
     candidates:
      android.net.netlink.StructNlAttr.<init>(short, byte):void
      android.net.netlink.StructNlAttr.<init>(short, int):void
      android.net.netlink.StructNlAttr.<init>(short, java.net.InetAddress):void
      android.net.netlink.StructNlAttr.<init>(short, short):void
      android.net.netlink.StructNlAttr.<init>(short, android.net.netlink.StructNlAttr[]):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.net.netlink.StructNlAttr.<init>(short, java.net.InetAddress):void
     arg types: [int, java.net.Inet4Address]
     candidates:
      android.net.netlink.StructNlAttr.<init>(short, byte):void
      android.net.netlink.StructNlAttr.<init>(short, int):void
      android.net.netlink.StructNlAttr.<init>(short, short):void
      android.net.netlink.StructNlAttr.<init>(short, android.net.netlink.StructNlAttr[]):void
      android.net.netlink.StructNlAttr.<init>(short, java.net.InetAddress):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.net.netlink.StructNlAttr.<init>(short, byte):void
     arg types: [int, byte]
     candidates:
      android.net.netlink.StructNlAttr.<init>(short, int):void
      android.net.netlink.StructNlAttr.<init>(short, java.net.InetAddress):void
      android.net.netlink.StructNlAttr.<init>(short, short):void
      android.net.netlink.StructNlAttr.<init>(short, android.net.netlink.StructNlAttr[]):void
      android.net.netlink.StructNlAttr.<init>(short, byte):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.net.netlink.StructNlAttr.<init>(short, short, java.nio.ByteOrder):void
     arg types: [int, short, java.nio.ByteOrder]
     candidates:
      android.net.netlink.StructNlAttr.<init>(short, int, java.nio.ByteOrder):void
      android.net.netlink.StructNlAttr.<init>(short, short, java.nio.ByteOrder):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.net.netlink.StructNlAttr.<init>(short, int, java.nio.ByteOrder):void
     arg types: [int, int, java.nio.ByteOrder]
     candidates:
      android.net.netlink.StructNlAttr.<init>(short, short, java.nio.ByteOrder):void
      android.net.netlink.StructNlAttr.<init>(short, int, java.nio.ByteOrder):void */
    public static byte[] newIPv4TimeoutUpdateRequest(int proto, Inet4Address src, int sport, Inet4Address dst, int dport, int timeoutSec) {
        StructNlAttr ctaTupleOrig = new StructNlAttr((short) 1, new StructNlAttr((short) 1, new StructNlAttr((short) 1, (InetAddress) src), new StructNlAttr((short) 2, (InetAddress) dst)), new StructNlAttr((short) 2, new StructNlAttr((short) 1, (byte) proto), new StructNlAttr((short) 2, (short) sport, ByteOrder.BIG_ENDIAN), new StructNlAttr((short) 3, (short) dport, ByteOrder.BIG_ENDIAN)));
        StructNlAttr ctaTimeout = new StructNlAttr((short) 7, timeoutSec, ByteOrder.BIG_ENDIAN);
        byte[] bytes = new byte[(ctaTupleOrig.getAlignedLength() + ctaTimeout.getAlignedLength() + 20)];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        ConntrackMessage ctmsg = new ConntrackMessage();
        ctmsg.mHeader.nlmsg_len = bytes.length;
        ctmsg.mHeader.nlmsg_type = 256;
        ctmsg.mHeader.nlmsg_flags = 261;
        ctmsg.mHeader.nlmsg_seq = 1;
        ctmsg.pack(byteBuffer);
        ctaTupleOrig.pack(byteBuffer);
        ctaTimeout.pack(byteBuffer);
        return bytes;
    }

    private ConntrackMessage() {
        super(new StructNlMsgHdr());
    }

    public void pack(ByteBuffer byteBuffer) {
        this.mHeader.pack(byteBuffer);
        this.mNfGenMsg.pack(byteBuffer);
    }
}
