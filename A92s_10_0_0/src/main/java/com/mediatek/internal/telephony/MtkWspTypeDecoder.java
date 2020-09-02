package com.mediatek.internal.telephony;

import com.android.internal.telephony.WspTypeDecoder;
import com.mediatek.android.mms.pdu.MtkPduPart;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;
import java.util.HashMap;

public class MtkWspTypeDecoder extends WspTypeDecoder {
    public static final String CONTENT_MIME_TYPE_B_CONNECTIVITY = "application/vnd.wap.connectivity-wbxml";
    public static final String CONTENT_MIME_TYPE_B_VND_SULP_INIT = "application/vnd.omaloc-supl-init";
    public static final int CONTENT_TYPE_B_CONNECTIVITY = 53;
    private static final HashMap<Integer, String> WELL_KNOWN_HEADERS = new HashMap<>();
    private static final HashMap<Integer, String> WELL_KNOWN_X_WAP_APPLICATION_ID = new HashMap<>();
    HashMap<String, String> mHeaders;

    static {
        WELL_KNOWN_HEADERS.put(0, "Accept");
        WELL_KNOWN_HEADERS.put(1, "Accept-Charset");
        WELL_KNOWN_HEADERS.put(2, "Accept-Encoding");
        WELL_KNOWN_HEADERS.put(3, "Accept-Language");
        WELL_KNOWN_HEADERS.put(4, "Accept-Ranges");
        WELL_KNOWN_HEADERS.put(5, "Age");
        WELL_KNOWN_HEADERS.put(6, "Allow");
        WELL_KNOWN_HEADERS.put(7, "Authorization");
        WELL_KNOWN_HEADERS.put(8, "Cache-Control");
        WELL_KNOWN_HEADERS.put(9, "Connection");
        WELL_KNOWN_HEADERS.put(10, "Content-Base");
        WELL_KNOWN_HEADERS.put(11, "Content-Encoding");
        WELL_KNOWN_HEADERS.put(12, "Content-Language");
        WELL_KNOWN_HEADERS.put(13, "Content-Length");
        WELL_KNOWN_HEADERS.put(14, MtkPduPart.CONTENT_LOCATION);
        WELL_KNOWN_HEADERS.put(15, "Content-MD5");
        WELL_KNOWN_HEADERS.put(16, "Content-Range");
        WELL_KNOWN_HEADERS.put(17, MtkPduPart.CONTENT_TYPE);
        WELL_KNOWN_HEADERS.put(18, "Date");
        WELL_KNOWN_HEADERS.put(19, "Etag");
        WELL_KNOWN_HEADERS.put(20, "Expires");
        WELL_KNOWN_HEADERS.put(21, PplSmsFilterExtension.INSTRUCTION_KEY_FROM);
        WELL_KNOWN_HEADERS.put(22, "Host");
        WELL_KNOWN_HEADERS.put(23, "If-Modified-Since");
        WELL_KNOWN_HEADERS.put(24, "If-Match");
        WELL_KNOWN_HEADERS.put(25, "If-None-Match");
        WELL_KNOWN_HEADERS.put(26, "If-Range");
        WELL_KNOWN_HEADERS.put(27, "If-Unmodified-Since");
        WELL_KNOWN_HEADERS.put(28, "Location");
        WELL_KNOWN_HEADERS.put(29, "Last-Modified");
        WELL_KNOWN_HEADERS.put(30, "Max-Forwards");
        WELL_KNOWN_HEADERS.put(31, "Pragma");
        WELL_KNOWN_HEADERS.put(32, "Proxy-Authenticate");
        WELL_KNOWN_HEADERS.put(33, "Proxy-Authorization");
        WELL_KNOWN_HEADERS.put(34, "Public");
        WELL_KNOWN_HEADERS.put(35, "Range");
        WELL_KNOWN_HEADERS.put(36, "Referer");
        WELL_KNOWN_HEADERS.put(37, "Retry-After");
        WELL_KNOWN_HEADERS.put(38, "Server");
        WELL_KNOWN_HEADERS.put(39, "Transfer-Encoding");
        WELL_KNOWN_HEADERS.put(40, "Upgrade");
        WELL_KNOWN_HEADERS.put(41, "User-Agent");
        WELL_KNOWN_HEADERS.put(42, "Vary");
        WELL_KNOWN_HEADERS.put(43, "Via");
        WELL_KNOWN_HEADERS.put(44, "Warning");
        WELL_KNOWN_HEADERS.put(45, "WWW-Authenticate");
        WELL_KNOWN_HEADERS.put(46, MtkPduPart.CONTENT_DISPOSITION);
        WELL_KNOWN_HEADERS.put(47, "X-Wap-Application-Id");
        WELL_KNOWN_HEADERS.put(48, "X-Wap-Content-URI");
        WELL_KNOWN_HEADERS.put(49, "X-Wap-Initiator-URI");
        WELL_KNOWN_HEADERS.put(50, "Accept-Application");
        WELL_KNOWN_HEADERS.put(51, "Bearer-Indication");
        WELL_KNOWN_HEADERS.put(52, "Push-Flag");
        WELL_KNOWN_HEADERS.put(53, "Profile");
        WELL_KNOWN_HEADERS.put(54, "Profile-Diff");
        WELL_KNOWN_HEADERS.put(55, "Profile-Warning");
        WELL_KNOWN_HEADERS.put(56, "Expect");
        WELL_KNOWN_HEADERS.put(57, "TE");
        WELL_KNOWN_HEADERS.put(58, "Trailer");
        WELL_KNOWN_HEADERS.put(59, "Accept-Charset");
        WELL_KNOWN_HEADERS.put(60, "Accept-Encoding");
        WELL_KNOWN_HEADERS.put(61, "Cache-Control");
        WELL_KNOWN_HEADERS.put(62, "Content-Range");
        WELL_KNOWN_HEADERS.put(63, "X-Wap-Tod");
        WELL_KNOWN_HEADERS.put(64, MtkPduPart.CONTENT_ID);
        WELL_KNOWN_HEADERS.put(65, "Set-Cookie");
        WELL_KNOWN_HEADERS.put(66, "Cookie");
        WELL_KNOWN_HEADERS.put(67, "Encoding-Version");
        WELL_KNOWN_HEADERS.put(68, "Profile-Warning");
        WELL_KNOWN_HEADERS.put(69, MtkPduPart.CONTENT_DISPOSITION);
        WELL_KNOWN_HEADERS.put(70, "X-WAP-Security");
        WELL_KNOWN_HEADERS.put(71, "Cache-Control");
        WELL_KNOWN_HEADERS.put(72, "Expect");
        WELL_KNOWN_HEADERS.put(73, "X-Wap-Loc-Invocation");
        WELL_KNOWN_HEADERS.put(74, "X-Wap-Loc-Delivery");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(0, "x-wap-application:*");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(1, "x-wap-application:push.sia");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(2, "x-wap-application:wml.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(3, "x-wap-application:wta.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(4, "x-wap-application:mms.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(5, "x-wap-application:push.syncml");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(6, "x-wap-application:loc.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(7, "x-wap-application:syncml.dm");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(8, "x-wap-application:drm.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(9, "x-wap-application:emn.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(10, "x-wap-application:wv.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(16, "x-oma-application:ulp.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(17, "x-oma-application:dlota.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(18, "x-oma-application:java-ams");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(19, "x-oma-application:bcast.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(20, "x-oma-application:dpe.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(21, "x-oma-application:cpm:ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32768, "x-wap-microsoft:localcontent.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32769, "x-wap-microsoft:IMclient.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32770, "x-wap-docomo:imode.mail.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32771, "x-wap-docomo:imode.mr.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32772, "x-wap-docomo:imode.mf.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32773, "x-motorola:location.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32774, "x-motorola:now.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32775, "x-motorola:otaprov.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32776, "x-motorola:browser.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32777, "x-motorola:splash.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32779, "x-wap-nai:mvsw.command");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(32784, "x-wap-openwave:iota.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36864, "x-wap-docomo:imode.mail2.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36865, "x-oma-nec:otaprov.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36866, "x-oma-nokia:call.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36867, "x-oma-coremobility:sqa.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36868, "x-oma-docomo:doja.jam.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36880, "x-oma-nokia:sip.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36881, "x-oma-vodafone:otaprov.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36882, "x-hutchison:ad.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36883, "x-oma-nokia:voip.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36884, "x-oma-docomo:voice.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36885, "x-oma-docomo:browser.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36886, "x-oma-docomo:dan.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36887, "x-oma-nokia:vs.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36888, "x-oma-nokia:voip.ext1.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36889, "x-wap-vodafone:casting.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36890, "x-oma-docomo:imode.data.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36891, "x-oma-snapin:otaprov.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36892, "x-oma-nokia:vrs.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36893, "x-oma-nokia:vrpg.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36894, "x-oma-motorola:screen3.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36895, "x-oma-docomo:device.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36896, "x-oma-nokia:msc.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36897, "x-3gpp2:lcs.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36898, "x-wap-vodafone:dcd.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36899, "x-3gpp:mbms.service.announcement.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36900, "x-oma-vodafone:dltmtbl.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36901, "x-oma-vodafone:dvcctl.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36902, "x-oma-cmcc:mail.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36903, "x-oma-nokia:vmb.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36904, "x-oma-nokia:ldapss.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36905, "x-hutchison:al.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36906, "x-oma-nokia:uma.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36907, "x-oma-nokia:news.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36908, "x-oma-docomo:pf");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36909, "x-oma-docomo:ub>");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36910, "x-oma-nokia:nat.traversal.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36911, "x-oma-intromobile:intropad.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36912, "x-oma-docomo:uin.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36913, "x-oma-nokia:iptv.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36914, "x-hutchison:il.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36915, "x-oma-nokia:voip.general.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36916, "x-microsoft:drm.meter");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36917, "x-microsoft:drm.license");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36918, "x-oma-docomo:ic.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36919, "x-oma-slingmedia:SPM.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36920, "x-cibenix:odp.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36921, "x-oma-motorola:voip.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36922, "x-oma-motorola:ims");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36923, "x-oma-docomo:imode.remote.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36924, "x-oma-docomo:device.ctl.um");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36925, "x-microsoft:playready.drm.initiator");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36926, "x-microsoft:playready.drm");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36927, "x-oma-sbm:ms.mexa.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36928, "urn:oma:drms:org-LGE:L650V");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36929, "x-oma-docomo:um");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36930, "x-oma-docomo:uin.um");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36931, "urn:oma:drms:org-LGE:KU450");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36932, "x-wap-microsoft:cfgmgr.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36933, "x-3gpp:mbms.download.delivery.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36934, "x-oma-docomo:star.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36935, "urn:oma:drms:org-LGE:KU380");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36936, "x-oma-docomo:pf2");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36937, "x-oma-motorola:blogcentral.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36938, "x-oma-docomo:imode.agent.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36939, "x-wap-application:push.sia");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36940, "x-oma-nokia:destination.network.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36941, "x-oma-sbm:mid2.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36942, "x-carrieriq:avm.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36943, "x-oma-sbm:ms.xml.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36944, "urn:dvb:ipdc:notification:2008");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36945, "x-oma-docomo:imode.mvch.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36946, "x-oma-motorola:webui.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36947, "x-oma-sbm:cid.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36948, "x-oma-nokia:vcc.v1.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36949, "x-oma-docomo:open.ctl");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36950, "x-oma-docomo:sp.mail.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36951, "x-essoy-application:push.erace");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36952, "x-oma-docomo:open.fu");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36953, "x-samsung:osp.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36954, "x-oma-docomo:imode.mchara.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36955, "X-Wap-Application-Id:x-oma-application: scidm.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36956, "x-oma-docomo:xmd.mail.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36957, "x-oma-application:pal.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36958, "x-oma-docomo:imode.relation.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36959, "x-oma-docomo:xmd.storage.ua");
        WELL_KNOWN_X_WAP_APPLICATION_ID.put(36960, "x-oma-docomo:xmd.lcsapp.ua");
    }

    public MtkWspTypeDecoder(byte[] pdu) {
        super(pdu);
    }

    public void decodeHeaders(int startIndex, int headerLength) {
        String headerName;
        String headerValue;
        this.mHeaders = new HashMap<>();
        int index = startIndex;
        while (index < startIndex + headerLength) {
            decodeHeaderFieldName(index);
            index += getDecodedDataLength();
            expandWellKnownHeadersName();
            int intValues = (int) this.mUnsigned32bit;
            if (this.mStringValue != null) {
                headerName = this.mStringValue;
            } else if (intValues >= 0) {
                headerName = String.valueOf(intValues);
            }
            decodeHeaderFieldValues(index);
            index += getDecodedDataLength();
            int intValues2 = (int) this.mUnsigned32bit;
            if (this.mStringValue != null) {
                headerValue = this.mStringValue;
            } else if (intValues2 >= 0) {
                headerValue = String.valueOf(intValues2);
            }
            this.mHeaders.put(headerName, headerValue);
        }
    }

    public boolean decodeHeaderFieldName(int startIndex) {
        if (!decodeShortInteger(startIndex)) {
            return decodeTextString(startIndex);
        }
        this.mStringValue = null;
        return true;
    }

    public boolean decodeHeaderFieldValues(int startIndex) {
        if (this.mWspData[startIndex] == 31 && decodeUintvarInteger(startIndex + 1)) {
            this.mStringValue = null;
            this.mDataLength++;
            return true;
        } else if (!decodeIntegerValue(startIndex)) {
            return decodeTextString(startIndex);
        } else {
            this.mStringValue = null;
            return true;
        }
    }

    public void expandWellKnownHeadersName() {
        if (this.mStringValue == null) {
            this.mStringValue = WELL_KNOWN_HEADERS.get(Integer.valueOf((int) this.mUnsigned32bit));
        } else {
            this.mUnsigned32bit = -1;
        }
    }

    public HashMap<String, String> getHeaders() {
        expandWellKnownXWapApplicationIdName();
        return this.mHeaders;
    }

    public void expandWellKnownXWapApplicationIdName() {
        String value;
        try {
            int binaryCode = Integer.valueOf(this.mHeaders.get("X-Wap-Application-Id")).intValue();
            if (binaryCode != -1 && (value = WELL_KNOWN_X_WAP_APPLICATION_ID.get(Integer.valueOf(binaryCode))) != null) {
                this.mHeaders.put("X-Wap-Application-Id", value);
            }
        } catch (Exception e) {
        }
    }
}
