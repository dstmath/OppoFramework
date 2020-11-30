package com.mediatek.simservs.client;

import android.content.Context;
import android.net.Network;
import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.simservs.capability.BarringServiceCapability;
import com.mediatek.simservs.capability.DiversionServiceCapability;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.XcapDebugParam;
import com.mediatek.xcap.client.uri.XcapUri;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

public class SimServs {
    public static String AUID_SIMSERVS = "simservs.ngn.etsi.org";
    public static final int CARDTYPE_ISIM = 2;
    public static final int CARDTYPE_UNSPECIFIED = 0;
    public static final int CARDTYPE_USIM = 1;
    public static final boolean LIB_CONFIG_MULTIPLE_RULE_CONDITIONS = true;
    public static final String SIMSERVS_FILENAME = "simservs.xml";
    public static final String TAG = "SimServs";
    public static boolean sDebug = false;
    public static boolean sETagDisable = false;
    public static SimServs sInstance = null;
    public static boolean sSimservQueryWhole = false;
    private static XcapDebugParam sXcapDebugParam;
    private static String sXcapRoot = null;
    private static String sXui = null;
    private int mCardType = 0;
    private Context mContext;
    public XcapUri.XcapDocumentSelector mDocumentSelector;
    public URI mDocumentUri;
    private String mImpi = null;
    private String mImpu = null;
    private String mImsi = null;
    private String mIntendedId = null;
    private String mMcc = null;
    private String mMnc = null;
    private String mPassword = null;
    private int mPhoneId;
    private String mUsername = null;
    public XcapUri mXcapUri;

    public SimServs() {
        if (SystemProperties.get("vendor.mediatek.simserv.debug", "0").equals("1")) {
            sDebug = true;
            Log.d(TAG, "sDebug enabled.");
        }
    }

    public static SimServs getInstance() {
        if (sInstance == null) {
            sInstance = new SimServs();
        }
        initializeDebugParam();
        return sInstance;
    }

    public XcapDebugParam getXcapDebugParam() {
        return sXcapDebugParam;
    }

    public void setXcapRoot(String xcapRoot) {
        sXcapRoot = xcapRoot;
        try {
            buildDocumentUri();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            sXcapRoot = null;
        }
    }

    public void setXcapRootByImpi(String impi) {
        this.mCardType = 2;
        this.mImpi = impi;
        try {
            buildRootUri();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setXcapRootByMccMnc(String mcc, String mnc) {
        this.mCardType = 1;
        this.mMcc = mcc;
        this.mMnc = mnc;
        try {
            buildRootUri();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setXui(String xui) {
        sXui = xui;
    }

    public void setXuiByImpu(String impu) {
        this.mCardType = 2;
        this.mImpu = impu;
        sXui = impu;
    }

    public void setXuiByImsiMccMnc(String imsi, String mcc, String mnc) {
        this.mCardType = 1;
        this.mImsi = imsi;
        this.mMcc = mcc;
        this.mMnc = mnc;
        sXui = String.format("sip:%s@ims.mnc%03d.mcc%03d.3gppnetwork.org", imsi, Integer.valueOf(Integer.parseInt(mnc)), Integer.valueOf(Integer.parseInt(mcc)));
    }

    public void setIntendedId(String intendedId) {
        this.mIntendedId = intendedId;
    }

    public void setHttpCredential(String username, String password) {
    }

    public void setUseHttpProtocolScheme(boolean value) {
        if (value) {
            System.setProperty("xcap.protocol", "http");
        } else {
            System.setProperty("xcap.protocol", "https");
        }
    }

    public void setElementUpdateContentType(boolean specdefined, String type) {
        if (specdefined) {
            System.setProperty("xcap.putelcontenttype", "application/xcap-el+xml");
        } else {
            System.setProperty("xcap.putelcontenttype", type);
        }
    }

    public void setHandleError409(boolean value) {
        if (value) {
            System.setProperty("xcap.handl409", XcapElement.TRUE);
        } else {
            System.setProperty("xcap.handl409", XcapElement.FALSE);
        }
    }

    public void setFillCompleteForwardTo(boolean value) {
        if (value) {
            System.setProperty("xcap.completeforwardto", XcapElement.TRUE);
        } else {
            System.setProperty("xcap.completeforwardto", XcapElement.FALSE);
        }
    }

    public void setXcapNSPrefixSS(boolean value) {
        if (value) {
            System.setProperty("xcap.ns.ss", XcapElement.TRUE);
        } else {
            System.setProperty("xcap.ns.ss", XcapElement.FALSE);
        }
    }

    public void setAttrNeedQuotationMark(boolean value) {
        if (value) {
            System.setProperty("xcap.attr.active.quote", XcapElement.TRUE);
        } else {
            System.setProperty("xcap.attr.active.quote", XcapElement.FALSE);
        }
    }

    public void setAUID(String auid) {
        AUID_SIMSERVS = auid;
        Log.d(TAG, "setAUID: " + auid);
    }

    public void setETagDisable(boolean disable) {
        Log.d(TAG, "setETagDisable: " + disable);
        sETagDisable = disable;
    }

    public void setSimservQueryWhole(boolean enable) {
        Log.d(TAG, "setSimservQueryWhole: " + enable);
        sSimservQueryWhole = enable;
    }

    public String getXcapRoot() {
        return sXcapRoot;
    }

    public String getXui() {
        return sXui;
    }

    public String getIntendedId() {
        return this.mIntendedId;
    }

    public void setContext(Context ctxt) {
        this.mContext = ctxt;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setPhoneId(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    private static void initializeDebugParam() {
        sXcapDebugParam = XcapDebugParam.getInstance();
        sXcapDebugParam.load();
        String xcapRoot = sXcapDebugParam.getXcapRoot();
        if (xcapRoot != null && !xcapRoot.isEmpty()) {
            sXcapRoot = xcapRoot;
        }
        String xui = sXcapDebugParam.getXcapXui();
        if (xui != null && !xui.isEmpty()) {
            sXui = xui;
        }
    }

    private String getImpiDomain() {
        String str = this.mImpi;
        if (str == null || str.isEmpty() || !this.mImpi.contains("@")) {
            return null;
        }
        return this.mImpi.split("@")[1];
    }

    private void buildRootUri() throws URISyntaxException {
        StringBuilder xcapRoot = new StringBuilder();
        Log.d("Simservs", "xcap.protocol=" + System.getProperty("xcap.protocol"));
        String protocol = System.getProperty("xcap.protocol", "https");
        int i = this.mCardType;
        if (i == 1) {
            xcapRoot.append(protocol + "://xcap.ims.mnc");
            xcapRoot.append(this.mMnc);
            xcapRoot.append(".mcc");
            xcapRoot.append(this.mMcc);
            xcapRoot.append(".pub.3gppnetwork.org");
        } else if (i == 2) {
            String str = this.mImpi;
            if (str == null || !str.endsWith("3gppnetwork.org")) {
                String domain = getImpiDomain();
                if (domain != null) {
                    xcapRoot.append(protocol + "://xcap.");
                    xcapRoot.append(domain);
                } else {
                    return;
                }
            } else {
                String domain2 = getImpiDomain();
                if (domain2 != null) {
                    xcapRoot.append(protocol + "://xcap.");
                    xcapRoot.append(domain2.substring(0, domain2.indexOf(".3gppnetwork.org")));
                    if (this.mImpi.contains(".pub")) {
                        xcapRoot.append(".3gppnetwork.org");
                    } else {
                        xcapRoot.append(".pub.3gppnetwork.org");
                    }
                } else {
                    return;
                }
            }
        } else {
            return;
        }
        xcapRoot.append("/");
        sXcapRoot = xcapRoot.toString();
        buildDocumentUri();
    }

    public void buildDocumentUri() throws URISyntaxException {
        String xcapDocumentName = sXcapDebugParam.getXcapDocumentName();
        String xcapAUID = sXcapDebugParam.getXcapAUID();
        this.mDocumentSelector = new XcapUri.XcapDocumentSelector((xcapAUID == null || xcapAUID.isEmpty()) ? AUID_SIMSERVS : xcapAUID, sXui, (xcapDocumentName == null || xcapDocumentName.isEmpty()) ? SIMSERVS_FILENAME : xcapDocumentName);
        Log.d(TAG, "document selector is " + this.mDocumentSelector.toString());
        this.mXcapUri = new XcapUri();
        Log.d(TAG, "buildDocumentUri():Create instance for mXcapUri");
        this.mXcapUri.setXcapRoot(sXcapRoot).setDocumentSelector(this.mDocumentSelector);
        this.mDocumentUri = this.mXcapUri.toURI();
    }

    public String getDocumentUri() {
        return this.mDocumentUri.toString();
    }

    public CommunicationWaiting getCommunicationWaiting(boolean syncInstance) throws Exception {
        CommunicationWaiting cw = new CommunicationWaiting(this.mXcapUri, null, this.mIntendedId);
        cw.loadConfiguration();
        return cw;
    }

    public CommunicationWaiting getCommunicationWaiting(boolean syncInstance, Network network) throws Exception {
        CommunicationWaiting cw = new CommunicationWaiting(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            cw.setNetwork(network);
        }
        cw.loadConfiguration();
        return cw;
    }

    public CommunicationWaiting getCommunicationWaiting(XcapUri documentUri) throws Exception {
        return new CommunicationWaiting(documentUri, null, this.mIntendedId);
    }

    public OriginatingIdentityPresentation getOriginatingIdentityPresentation(boolean syncInstance) throws Exception {
        OriginatingIdentityPresentation oip = new OriginatingIdentityPresentation(this.mXcapUri, null, this.mIntendedId);
        oip.loadConfiguration();
        return oip;
    }

    public OriginatingIdentityPresentation getOriginatingIdentityPresentation(boolean syncInstance, Network network) throws Exception {
        OriginatingIdentityPresentation oip = new OriginatingIdentityPresentation(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            oip.setNetwork(network);
        }
        oip.loadConfiguration();
        return oip;
    }

    public TerminatingIdentityPresentation getTerminatingIdentityPresentation(boolean syncInstance) throws Exception {
        TerminatingIdentityPresentation tip = new TerminatingIdentityPresentation(this.mXcapUri, null, this.mIntendedId);
        tip.loadConfiguration();
        return tip;
    }

    public TerminatingIdentityPresentation getTerminatingIdentityPresentation(boolean syncInstance, Network network) throws Exception {
        TerminatingIdentityPresentation tip = new TerminatingIdentityPresentation(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            tip.setNetwork(network);
        }
        tip.loadConfiguration();
        return tip;
    }

    public OriginatingIdentityPresentation getOriginatingIdentityPresentation(XcapUri documentUri) throws Exception {
        return new OriginatingIdentityPresentation(documentUri, null, this.mIntendedId);
    }

    public TerminatingIdentityPresentation getTerminatingIdentityPresentation(XcapUri documentUri) throws Exception {
        return new TerminatingIdentityPresentation(documentUri, null, this.mIntendedId);
    }

    public OriginatingIdentityPresentationRestriction getOriginatingIdentityPresentationRestriction(boolean syncInstance) throws Exception {
        OriginatingIdentityPresentationRestriction oip = new OriginatingIdentityPresentationRestriction(this.mXcapUri, null, this.mIntendedId);
        oip.loadConfiguration();
        return oip;
    }

    public OriginatingIdentityPresentationRestriction getOriginatingIdentityPresentationRestriction(boolean syncInstance, Network network) throws Exception {
        OriginatingIdentityPresentationRestriction oipr = new OriginatingIdentityPresentationRestriction(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            oipr.setNetwork(network);
        }
        oipr.loadConfiguration();
        return oipr;
    }

    public TerminatingIdentityPresentationRestriction getTerminatingIdentityPresentationRestriction(boolean syncInstance) throws Exception {
        TerminatingIdentityPresentationRestriction tipr = new TerminatingIdentityPresentationRestriction(this.mXcapUri, null, this.mIntendedId);
        tipr.loadConfiguration();
        return tipr;
    }

    public TerminatingIdentityPresentationRestriction getTerminatingIdentityPresentationRestriction(boolean syncInstance, Network network) throws Exception {
        TerminatingIdentityPresentationRestriction tipr = new TerminatingIdentityPresentationRestriction(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            tipr.setNetwork(network);
        }
        tipr.loadConfiguration();
        return tipr;
    }

    public OriginatingIdentityPresentationRestriction getOriginatingIdentityPresentationRestriction(XcapUri documentUri) throws Exception {
        return new OriginatingIdentityPresentationRestriction(documentUri, null, this.mIntendedId);
    }

    public TerminatingIdentityPresentationRestriction getTerminatingIdentityPresentationRestriction(XcapUri documentUri) throws Exception {
        return new TerminatingIdentityPresentationRestriction(documentUri, null, this.mIntendedId);
    }

    public CommunicationDiversion getCommunicationDiversion(boolean syncInstance) throws Exception {
        CommunicationDiversion cd = new CommunicationDiversion(this.mXcapUri, null, this.mIntendedId);
        cd.loadConfiguration();
        return cd;
    }

    public CommunicationDiversion getCommunicationDiversion(boolean syncInstance, Network network) throws Exception {
        CommunicationDiversion cd = new CommunicationDiversion(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            cd.setNetwork(network);
        }
        cd.loadConfiguration();
        return cd;
    }

    public CommunicationDiversion getCommunicationDiversion(XcapUri documentUri) throws Exception {
        return new CommunicationDiversion(documentUri, null, this.mIntendedId);
    }

    public IncomingCommunicationBarring getIncomingCommunicationBarring(XcapUri documentUri) throws Exception {
        return new IncomingCommunicationBarring(documentUri, null, this.mIntendedId);
    }

    public IncomingCommunicationBarring getIncomingCommunicationBarring(boolean syncInstance) throws Exception {
        IncomingCommunicationBarring icb = new IncomingCommunicationBarring(this.mXcapUri, null, this.mIntendedId);
        icb.loadConfiguration();
        return icb;
    }

    public IncomingCommunicationBarring getIncomingCommunicationBarring(boolean syncInstance, Network network) throws Exception {
        IncomingCommunicationBarring icb = new IncomingCommunicationBarring(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            icb.setNetwork(network);
        }
        icb.loadConfiguration();
        return icb;
    }

    public OutgoingCommunicationBarring getOutgoingCommunicationBarring(XcapUri documentUri) throws Exception {
        return new OutgoingCommunicationBarring(documentUri, null, this.mIntendedId);
    }

    public OutgoingCommunicationBarring getOutgoingCommunicationBarring(boolean syncInstance) throws Exception {
        OutgoingCommunicationBarring ocb = new OutgoingCommunicationBarring(this.mXcapUri, null, this.mIntendedId);
        ocb.loadConfiguration();
        return ocb;
    }

    public OutgoingCommunicationBarring getOutgoingCommunicationBarring(boolean syncInstance, Network network) throws Exception {
        OutgoingCommunicationBarring ocb = new OutgoingCommunicationBarring(this.mXcapUri, null, this.mIntendedId);
        if (network != null) {
            ocb.setNetwork(network);
        }
        ocb.loadConfiguration();
        return ocb;
    }

    public DiversionServiceCapability getDiversionServiceCapability(boolean syncInstance) throws Exception {
        return new DiversionServiceCapability(this.mXcapUri, null, this.mIntendedId);
    }

    public DiversionServiceCapability getDiversionServiceCapability(XcapUri documentUri) throws Exception {
        return new DiversionServiceCapability(documentUri, null, this.mIntendedId);
    }

    public BarringServiceCapability getBarringServiceCapability(boolean syncInstance) throws Exception {
        return new BarringServiceCapability(this.mXcapUri, null, this.mIntendedId);
    }

    public BarringServiceCapability getBarringServiceCapability(XcapUri documentUri) throws Exception {
        return new BarringServiceCapability(documentUri, null, this.mIntendedId);
    }

    public void resetParameters() {
        sXcapRoot = null;
        sXui = null;
        this.mIntendedId = null;
    }

    public static String encryptString(String message) {
        byte[] textByte;
        Base64.Encoder encoder = Base64.getEncoder();
        try {
            textByte = message.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            textByte = null;
        }
        if (textByte == null) {
            return "";
        }
        return encoder.encodeToString(textByte);
    }
}
