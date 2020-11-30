package com.mediatek.simservs.client.policy;

import android.os.SystemProperties;
import android.telephony.Rlog;
import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ForwardTo extends XcapElement implements ConfigureType {
    public static final String NODE_NAME = "forward-to";
    private static final boolean SDBG = (SystemProperties.get("ro.build.type").equals("user") ? SDBG : true);
    public static final String TAG = "ForwardTo";
    static final String TAG_NOTIFY_CALLER = "notify-caller";
    static final String TAG_NOTIFY_SERVED_USER = "notify-served-user";
    static final String TAG_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL = "notify-served-user-on-outbound-call";
    static final String TAG_REVEAL_IDENTITY_TO_CALLER = "reveal-identity-to-caller";
    static final String TAG_REVEAL_IDENTITY_TO_TARGET = "reveal-identity-to-target";
    static final String TAG_REVEAL_SERVED_USER_IDENTITY_TO_CALLER = "reveal-served-user-identity-to-caller";
    static final String TAG_TARGET = "target";
    public boolean mIsValidTargetNumber = true;
    public boolean mNotifyCaller = true;
    public boolean mNotifyServedUser = SDBG;
    public boolean mNotifyServedUserOnOutboundCall = SDBG;
    public boolean mRevealIdentityToCaller = true;
    public boolean mRevealIdentityToTarget = true;
    public boolean mRevealServedUserIdentityToCaller = true;
    public String mTarget;

    public ForwardTo(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    public ForwardTo(XcapUri xcapUri, String parentUri, String intendedId, Element domElement) {
        super(xcapUri, parentUri, intendedId);
        instantiateFromXmlNode(domElement);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    private boolean isValidTargetNumber(String uri) {
        if ((uri != null && uri.startsWith("sip:")) || uri.startsWith("sips:") || uri.startsWith("tel") || uri.startsWith("+") || uri.matches("^[0-9]+$")) {
            Rlog.d(TAG, "isValidTargetNumber = " + Rlog.pii(SDBG, uri) + ", result = true");
            return true;
        } else if (uri.equals("")) {
            Rlog.d(TAG, "Number is empty, we should put CF number to server");
            return SDBG;
        } else {
            Rlog.d(TAG, "isValidTargetNumber = " + Rlog.pii(SDBG, uri) + ", result = false");
            return SDBG;
        }
    }

    @Override // com.mediatek.simservs.xcap.ConfigureType
    public void instantiateFromXmlNode(Node domNode) {
        Element domElement = (Element) domNode;
        NodeList forwardToNode = domElement.getElementsByTagName(TAG_TARGET);
        if (forwardToNode.getLength() > 0) {
            this.mTarget = ((Element) forwardToNode.item(0)).getTextContent();
            this.mIsValidTargetNumber = isValidTargetNumber(this.mTarget);
        } else {
            NodeList forwardToNode2 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_TARGET);
            if (forwardToNode2.getLength() > 0) {
                this.mTarget = ((Element) forwardToNode2.item(0)).getTextContent();
                this.mIsValidTargetNumber = isValidTargetNumber(this.mTarget);
            } else {
                NodeList forwardToNode3 = domElement.getElementsByTagName("ss:target");
                if (forwardToNode3.getLength() > 0) {
                    this.mTarget = ((Element) forwardToNode3.item(0)).getTextContent();
                    this.mIsValidTargetNumber = isValidTargetNumber(this.mTarget);
                }
            }
        }
        NodeList forwardToNode4 = domElement.getElementsByTagName(TAG_NOTIFY_CALLER);
        if (forwardToNode4.getLength() > 0) {
            this.mNotifyCaller = ((Element) forwardToNode4.item(0)).getTextContent().equals(XcapElement.TRUE);
        } else {
            NodeList forwardToNode5 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_NOTIFY_CALLER);
            if (forwardToNode5.getLength() > 0) {
                this.mNotifyCaller = ((Element) forwardToNode5.item(0)).getTextContent().equals(XcapElement.TRUE);
            } else {
                NodeList forwardToNode6 = domElement.getElementsByTagName("ss:notify-caller");
                if (forwardToNode6.getLength() > 0) {
                    this.mNotifyCaller = ((Element) forwardToNode6.item(0)).getTextContent().equals(XcapElement.TRUE);
                }
            }
        }
        NodeList forwardToNode7 = domElement.getElementsByTagName(TAG_REVEAL_IDENTITY_TO_CALLER);
        if (forwardToNode7.getLength() > 0) {
            this.mRevealIdentityToCaller = ((Element) forwardToNode7.item(0)).getTextContent().equals(XcapElement.TRUE);
        } else {
            NodeList forwardToNode8 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_REVEAL_IDENTITY_TO_CALLER);
            if (forwardToNode8.getLength() > 0) {
                this.mRevealIdentityToCaller = ((Element) forwardToNode8.item(0)).getTextContent().equals(XcapElement.TRUE);
            } else {
                NodeList forwardToNode9 = domElement.getElementsByTagName("ss:reveal-identity-to-caller");
                if (forwardToNode9.getLength() > 0) {
                    this.mRevealIdentityToCaller = ((Element) forwardToNode9.item(0)).getTextContent().equals(XcapElement.TRUE);
                }
            }
        }
        NodeList forwardToNode10 = domElement.getElementsByTagName(TAG_REVEAL_IDENTITY_TO_TARGET);
        if (forwardToNode10.getLength() > 0) {
            this.mRevealIdentityToTarget = ((Element) forwardToNode10.item(0)).getTextContent().equals(XcapElement.TRUE);
        } else {
            NodeList forwardToNode11 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_REVEAL_IDENTITY_TO_TARGET);
            if (forwardToNode11.getLength() > 0) {
                this.mRevealIdentityToTarget = ((Element) forwardToNode11.item(0)).getTextContent().equals(XcapElement.TRUE);
            } else {
                NodeList forwardToNode12 = domElement.getElementsByTagName("ss:reveal-identity-to-target");
                if (forwardToNode12.getLength() > 0) {
                    this.mRevealIdentityToTarget = ((Element) forwardToNode12.item(0)).getTextContent().equals(XcapElement.TRUE);
                }
            }
        }
        NodeList forwardToNode13 = domElement.getElementsByTagName(TAG_REVEAL_SERVED_USER_IDENTITY_TO_CALLER);
        if (forwardToNode13.getLength() > 0) {
            this.mRevealServedUserIdentityToCaller = ((Element) forwardToNode13.item(0)).getTextContent().equals(XcapElement.TRUE);
        } else {
            NodeList forwardToNode14 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_REVEAL_SERVED_USER_IDENTITY_TO_CALLER);
            if (forwardToNode14.getLength() > 0) {
                this.mRevealServedUserIdentityToCaller = ((Element) forwardToNode14.item(0)).getTextContent().equals(XcapElement.TRUE);
            } else {
                NodeList forwardToNode15 = domElement.getElementsByTagName("ss:reveal-served-user-identity-to-caller");
                if (forwardToNode15.getLength() > 0) {
                    this.mRevealServedUserIdentityToCaller = ((Element) forwardToNode15.item(0)).getTextContent().equals(XcapElement.TRUE);
                }
            }
        }
        NodeList forwardToNode16 = domElement.getElementsByTagName(TAG_NOTIFY_SERVED_USER);
        if (forwardToNode16.getLength() > 0) {
            this.mNotifyServedUser = ((Element) forwardToNode16.item(0)).getTextContent().equals(XcapElement.TRUE);
        } else {
            NodeList forwardToNode17 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_NOTIFY_SERVED_USER);
            if (forwardToNode17.getLength() > 0) {
                this.mNotifyServedUser = ((Element) forwardToNode17.item(0)).getTextContent().equals(XcapElement.TRUE);
            } else {
                NodeList forwardToNode18 = domElement.getElementsByTagName("ss:notify-served-user");
                if (forwardToNode18.getLength() > 0) {
                    this.mNotifyServedUser = ((Element) forwardToNode18.item(0)).getTextContent().equals(XcapElement.TRUE);
                }
            }
        }
        NodeList forwardToNode19 = domElement.getElementsByTagName(TAG_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL);
        if (forwardToNode19.getLength() > 0) {
            this.mNotifyServedUserOnOutboundCall = ((Element) forwardToNode19.item(0)).getTextContent().equals(XcapElement.TRUE);
            return;
        }
        NodeList forwardToNode20 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL);
        if (forwardToNode20.getLength() > 0) {
            this.mNotifyServedUserOnOutboundCall = ((Element) forwardToNode20.item(0)).getTextContent().equals(XcapElement.TRUE);
            return;
        }
        NodeList forwardToNode21 = domElement.getElementsByTagName("ss:notify-served-user-on-outbound-call");
        if (forwardToNode21.getLength() > 0) {
            this.mNotifyServedUserOnOutboundCall = ((Element) forwardToNode21.item(0)).getTextContent().equals(XcapElement.TRUE);
        }
    }

    public Element toXmlElement(Document document) {
        String str = XcapElement.FALSE;
        if (XcapElement.TRUE.equals(System.getProperty("xcap.ns.ss", str))) {
            Element forwardElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:forward-to");
            Element allowElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:target");
            allowElement.setTextContent(this.mTarget);
            forwardElement.appendChild(allowElement);
            if (XcapElement.TRUE.equals(System.getProperty("xcap.completeforwardto", str))) {
                Element notifyCallerElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:notify-caller");
                notifyCallerElement.setTextContent(this.mNotifyCaller ? XcapElement.TRUE : str);
                forwardElement.appendChild(notifyCallerElement);
                Element revealIdentityToCallerElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:reveal-identity-to-caller");
                revealIdentityToCallerElement.setTextContent(this.mRevealIdentityToCaller ? XcapElement.TRUE : str);
                forwardElement.appendChild(revealIdentityToCallerElement);
                Element revealServedUserIdentityToCallerElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:reveal-served-user-identity-to-caller");
                revealServedUserIdentityToCallerElement.setTextContent(this.mRevealServedUserIdentityToCaller ? XcapElement.TRUE : str);
                forwardElement.appendChild(revealServedUserIdentityToCallerElement);
                Element notifyServedUserElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:notify-served-user");
                notifyServedUserElement.setTextContent(this.mNotifyServedUser ? XcapElement.TRUE : str);
                forwardElement.appendChild(notifyServedUserElement);
                Element notifyServedUserOnOutboundCallElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:notify-served-user-on-outbound-call");
                notifyServedUserOnOutboundCallElement.setTextContent(this.mNotifyServedUserOnOutboundCall ? XcapElement.TRUE : str);
                forwardElement.appendChild(notifyServedUserOnOutboundCallElement);
                Element revealIdentityToTargetElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:reveal-identity-to-target");
                if (this.mRevealIdentityToTarget) {
                    str = XcapElement.TRUE;
                }
                revealIdentityToTargetElement.setTextContent(str);
                forwardElement.appendChild(revealIdentityToTargetElement);
            }
            return forwardElement;
        }
        Element forwardElement2 = document.createElement(NODE_NAME);
        Element allowElement2 = document.createElement(TAG_TARGET);
        allowElement2.setTextContent(this.mTarget);
        forwardElement2.appendChild(allowElement2);
        if (XcapElement.TRUE.equals(System.getProperty("xcap.completeforwardto", str))) {
            Element notifyCallerElement2 = document.createElement(TAG_NOTIFY_CALLER);
            notifyCallerElement2.setTextContent(this.mNotifyCaller ? XcapElement.TRUE : str);
            forwardElement2.appendChild(notifyCallerElement2);
            Element revealIdentityToCallerElement2 = document.createElement(TAG_REVEAL_IDENTITY_TO_CALLER);
            revealIdentityToCallerElement2.setTextContent(this.mRevealIdentityToCaller ? XcapElement.TRUE : str);
            forwardElement2.appendChild(revealIdentityToCallerElement2);
            Element revealServedUserIdentityToCallerElement2 = document.createElement(TAG_REVEAL_SERVED_USER_IDENTITY_TO_CALLER);
            revealServedUserIdentityToCallerElement2.setTextContent(this.mRevealServedUserIdentityToCaller ? XcapElement.TRUE : str);
            forwardElement2.appendChild(revealServedUserIdentityToCallerElement2);
            Element notifyServedUserElement2 = document.createElement(TAG_NOTIFY_SERVED_USER);
            notifyServedUserElement2.setTextContent(this.mNotifyServedUser ? XcapElement.TRUE : str);
            forwardElement2.appendChild(notifyServedUserElement2);
            Element notifyServedUserOnOutboundCallElement2 = document.createElement(TAG_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL);
            notifyServedUserOnOutboundCallElement2.setTextContent(this.mNotifyServedUserOnOutboundCall ? XcapElement.TRUE : str);
            forwardElement2.appendChild(notifyServedUserOnOutboundCallElement2);
            Element revealIdentityToTargetElement2 = document.createElement(TAG_REVEAL_IDENTITY_TO_TARGET);
            if (this.mRevealIdentityToTarget) {
                str = XcapElement.TRUE;
            }
            revealIdentityToTargetElement2.setTextContent(str);
            forwardElement2.appendChild(revealIdentityToTargetElement2);
        }
        return forwardElement2;
    }

    public void setTarget(String target) {
        this.mTarget = target;
    }

    public void setNotifyCaller(boolean notifyCaller) {
        this.mNotifyCaller = notifyCaller;
    }

    public void setRevealIdentityToCaller(boolean revealIdToCaller) {
        this.mRevealIdentityToCaller = revealIdToCaller;
    }

    public void setRevealServedUserIdentityToCaller(boolean revealIdToCaller) {
        this.mRevealServedUserIdentityToCaller = revealIdToCaller;
    }

    public void setNotifyServedUser(boolean notifyToServedUser) {
        this.mNotifyServedUser = notifyToServedUser;
    }

    public void setNotifyServedUserOnOutboundCall(boolean notifyToServedUser) {
        this.mNotifyServedUserOnOutboundCall = notifyToServedUser;
    }

    public void setRevealIdentityToTarget(boolean revealIdToTarget) {
        this.mRevealIdentityToTarget = revealIdToTarget;
    }

    public String getTarget() {
        return this.mTarget;
    }

    public boolean isNotifyCaller() {
        return this.mNotifyCaller;
    }

    public boolean isRevealIdentityToCaller() {
        return this.mRevealIdentityToCaller;
    }

    public boolean isRevealServedUserIdentityToCaller() {
        return this.mRevealServedUserIdentityToCaller;
    }

    public boolean isNotifyServedUse() {
        return this.mNotifyServedUser;
    }

    public boolean isNotifyServedUserOnOutboundCall() {
        return this.mNotifyServedUserOnOutboundCall;
    }

    public boolean isRevealIdentityToTarget() {
        return this.mRevealIdentityToTarget;
    }
}
