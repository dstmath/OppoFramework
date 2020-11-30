package com.mediatek.simservs.capability;

import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ActionCapabilities extends ServiceCapabilities implements ConfigureType {
    public static final String NODE_NAME = "serv-cap-actions";
    static final String TAG_NOTIFY_CALLER = "serv-cap-notify-caller";
    static final String TAG_NOTIFY_SERVED_USER = "serv-cap-notify-served-user";
    static final String TAG_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL = "serv-cap-notify-served-user-on-outbound-call";
    static final String TAG_REVEAL_IDENTITY_TO_CALLER = "serv-cap-reveal-identity-to-caller";
    static final String TAG_REVEAL_IDENTITY_TO_TARGET = "serv-cap-reveal-identity-to-target";
    static final String TAG_REVEAL_SERVED_USER_IDENTITY_TO_CALLER = "serv-cap-reveal-served-user-identity-to-caller";
    static final String TAG_TARGET = "serv-cap-target";
    public boolean mNotifyCallerProvisioned = false;
    public boolean mNotifyServedUserOnOutboundCallProvisioned = false;
    public boolean mNotifyServedUserProvisioned = false;
    public boolean mRevealIdentityToCallerProvisioned = false;
    public boolean mRevealIdentityToTargetProvisioned = false;
    public boolean mRevealServedUserIdentityToCallerProvisioned = false;

    public ActionCapabilities(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    public ActionCapabilities(XcapUri xcapUri, String parentUri, String intendedId, Node nodes) {
        super(xcapUri, parentUri, intendedId);
        instantiateFromXmlNode(nodes);
    }

    @Override // com.mediatek.simservs.xcap.ConfigureType
    public void instantiateFromXmlNode(Node domNode) {
        Element domElement = (Element) domNode;
        NodeList conditionNode = domElement.getElementsByTagName(TAG_NOTIFY_CALLER);
        if (conditionNode.getLength() > 0) {
            this.mNotifyCallerProvisioned = ((Element) conditionNode.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode2 = domElement.getElementsByTagName(TAG_NOTIFY_SERVED_USER);
        if (conditionNode2.getLength() > 0) {
            this.mNotifyServedUserProvisioned = ((Element) conditionNode2.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode3 = domElement.getElementsByTagName(TAG_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL);
        if (conditionNode3.getLength() > 0) {
            this.mNotifyServedUserOnOutboundCallProvisioned = ((Element) conditionNode3.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode4 = domElement.getElementsByTagName(TAG_REVEAL_IDENTITY_TO_CALLER);
        if (conditionNode4.getLength() > 0) {
            this.mRevealIdentityToCallerProvisioned = ((Element) conditionNode4.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode5 = domElement.getElementsByTagName(TAG_REVEAL_SERVED_USER_IDENTITY_TO_CALLER);
        if (conditionNode5.getLength() > 0) {
            this.mRevealServedUserIdentityToCallerProvisioned = ((Element) conditionNode5.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode6 = domElement.getElementsByTagName(TAG_REVEAL_IDENTITY_TO_TARGET);
        if (conditionNode6.getLength() > 0) {
            this.mRevealIdentityToTargetProvisioned = ((Element) conditionNode6.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement, com.mediatek.simservs.capability.ServiceCapabilities
    public String getNodeName() {
        return NODE_NAME;
    }

    public boolean isNotifyCallerProvisioned() {
        return this.mNotifyCallerProvisioned;
    }

    public boolean isNotifyServedUserProvisioned() {
        return this.mNotifyServedUserProvisioned;
    }

    public boolean isNotifyServedUserOnOutboundCallProvisioned() {
        return this.mNotifyServedUserOnOutboundCallProvisioned;
    }

    public boolean isRevealIdentityToCallerProvisioned() {
        return this.mRevealIdentityToCallerProvisioned;
    }

    public boolean isRevealServedUserIdentityToCallerProvisioned() {
        return this.mRevealServedUserIdentityToCallerProvisioned;
    }

    public boolean isRevealIdentityToTargetProvisioned() {
        return this.mRevealIdentityToTargetProvisioned;
    }
}
