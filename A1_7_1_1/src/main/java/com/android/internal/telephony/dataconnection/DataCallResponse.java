package com.android.internal.telephony.dataconnection;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DataCallResponse {
    private final boolean DBG = true;
    private final String LOG_TAG = "DataCallResponse";
    public int active = 0;
    public String[] addresses = new String[0];
    public int cid = 0;
    public String[] dnses = new String[0];
    public String[] gateways = new String[0];
    public String ifname = UsimPBMemInfo.STRING_NOT_SET;
    public int mtu = 0;
    public String[] pcscf = new String[0];
    public int rat = 1;
    public int status = 0;
    public int suggestedRetryTime = -1;
    public String type = UsimPBMemInfo.STRING_NOT_SET;
    public int version = 0;

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
    public enum SetupResult {
        ;
        
        public DcFailCause mFailCause;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult.<clinit>():void");
        }

        public String toString() {
            return name() + "  SetupResult.mFailCause=" + this.mFailCause;
        }
    }

    public String toString() {
        int i = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("DataCallResponse: {").append("version=").append(this.version).append(" status=").append(this.status).append(" retry=").append(this.suggestedRetryTime).append(" cid=").append(this.cid).append(" active=").append(this.active).append(" type=").append(this.type).append(" ifname=").append(this.ifname).append(" mtu=").append(this.mtu).append(" rat=").append(this.rat).append(" addresses=[");
        for (String addr : this.addresses) {
            sb.append(addr);
            sb.append(",");
        }
        if (this.addresses.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("] dnses=[");
        for (String addr2 : this.dnses) {
            sb.append(addr2);
            sb.append(",");
        }
        if (this.dnses.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("] gateways=[");
        for (String addr22 : this.gateways) {
            sb.append(addr22);
            sb.append(",");
        }
        if (this.gateways.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("] pcscf=[");
        String[] strArr = this.pcscf;
        int length = strArr.length;
        while (i < length) {
            sb.append(strArr[i]);
            sb.append(",");
            i++;
        }
        if (this.pcscf.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]}");
        return sb.toString();
    }

    public SetupResult setLinkProperties(LinkProperties linkProperties, boolean okToUseSystemPropertyDns) {
        SetupResult result;
        if (linkProperties == null) {
            linkProperties = new LinkProperties();
        } else {
            linkProperties.clear();
        }
        if (this.status == DcFailCause.NONE.getErrorCode()) {
            String propertyPrefix = "net." + this.ifname + ".";
            String addr;
            int addrPrefixLen;
            InetAddress ia;
            String dnsAddr;
            try {
                linkProperties.setInterfaceName(this.ifname);
                if (this.addresses == null || this.addresses.length <= 0) {
                    throw new UnknownHostException("no address for ifname=" + this.ifname);
                }
                for (String addr2 : this.addresses) {
                    addr2 = addr2.trim();
                    if (!addr2.isEmpty()) {
                        String[] ap = addr2.split("/");
                        if (ap.length == 2) {
                            addr2 = ap[0];
                            addrPrefixLen = Integer.parseInt(ap[1]);
                        } else {
                            addrPrefixLen = 0;
                        }
                        ia = NetworkUtils.numericToInetAddress(addr2);
                        if (ia.isAnyLocalAddress()) {
                            continue;
                        } else {
                            if (addrPrefixLen == 0) {
                                addrPrefixLen = ia instanceof Inet4Address ? 32 : 128;
                            }
                            Rlog.d("DataCallResponse", "addr/pl=" + addr2 + "/" + addrPrefixLen);
                            linkProperties.addLinkAddress(new LinkAddress(ia, addrPrefixLen));
                        }
                    }
                }
                if (this.dnses != null && this.dnses.length > 0) {
                    for (String addr22 : this.dnses) {
                        addr22 = addr22.trim();
                        if (!addr22.isEmpty()) {
                            ia = NetworkUtils.numericToInetAddress(addr22);
                            if (!ia.isAnyLocalAddress()) {
                                linkProperties.addDnsServer(ia);
                            }
                        }
                    }
                } else if (okToUseSystemPropertyDns) {
                    for (String dnsAddr2 : new String[]{SystemProperties.get(propertyPrefix + "dns1"), SystemProperties.get(propertyPrefix + "dns2")}) {
                        dnsAddr2 = dnsAddr2.trim();
                        if (!dnsAddr2.isEmpty()) {
                            ia = NetworkUtils.numericToInetAddress(dnsAddr2);
                            if (!ia.isAnyLocalAddress()) {
                                linkProperties.addDnsServer(ia);
                            }
                        }
                    }
                } else {
                    throw new UnknownHostException("Empty dns response and no system default dns");
                }
                if (this.gateways == null || this.gateways.length == 0) {
                    String sysGateways = SystemProperties.get(propertyPrefix + "gw");
                    if (sysGateways != null) {
                        this.gateways = sysGateways.split(" ");
                    } else {
                        this.gateways = new String[0];
                    }
                }
                for (String addr222 : this.gateways) {
                    addr222 = addr222.trim();
                    if (!addr222.isEmpty()) {
                        linkProperties.addRoute(new RouteInfo(NetworkUtils.numericToInetAddress(addr222)));
                    }
                }
                linkProperties.setMtu(this.mtu);
                result = SetupResult.SUCCESS;
            } catch (IllegalArgumentException e) {
                throw new UnknownHostException("Non-numeric gateway addr=" + addr222);
            } catch (IllegalArgumentException e2) {
                throw new UnknownHostException("Non-numeric dns addr=" + dnsAddr2);
            } catch (IllegalArgumentException e3) {
                throw new UnknownHostException("Non-numeric dns addr=" + addr222);
            } catch (IllegalArgumentException e4) {
                throw new UnknownHostException("Bad parameter for LinkAddress, ia=" + ia.getHostAddress() + "/" + addrPrefixLen);
            } catch (IllegalArgumentException e5) {
                throw new UnknownHostException("Non-numeric ip addr=" + addr222);
            } catch (UnknownHostException e6) {
                Rlog.d("DataCallResponse", "setLinkProperties: UnknownHostException " + e6);
                e6.printStackTrace();
                result = SetupResult.ERR_UnacceptableParameter;
            }
        } else if (this.version < 4) {
            result = SetupResult.ERR_GetLastErrorFromRil;
        } else {
            result = SetupResult.ERR_RilError;
        }
        if (result != SetupResult.SUCCESS) {
            Rlog.d("DataCallResponse", "setLinkProperties: error clearing LinkProperties status=" + this.status + " result=" + result);
            linkProperties.clear();
        }
        return result;
    }
}
