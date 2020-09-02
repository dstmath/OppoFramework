package com.mediatek.simservs.capability;

import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConditionCapabilities extends ServiceCapabilities implements ConfigureType {
    public static final String NODE_NAME = "serv-cap-conditions";
    static final String TAG_ANONYMOUS = "serv-cap-anonymous";
    static final String TAG_BUSY = "serv-cap-busy";
    static final String TAG_COMMUNICATION_DIVERTED = "serv-cap-communication-diverted";
    static final String TAG_EXTERNAL_LIST = "serv-cap-external-list";
    static final String TAG_IDENTITY = "serv-cap-identity";
    static final String TAG_INTERNATIONAL = "serv-cap-international";
    static final String TAG_INTERNATIONAL_EXHC = "serv-cap-international-exHC";
    static final String TAG_MEDIA = "serv-cap-media";
    static final String TAG_NOT_REACHABLE = "serv-cap-not-reachable";
    static final String TAG_NOT_REGISTERED = "serv-cap-not-registered";
    static final String TAG_NO_ANSWER = "serv-cap-no-answer";
    static final String TAG_OTHER_IDENTITY = "serv-cap-other-identity";
    static final String TAG_PRESENCE_STATUS = "serv-cap-presence-status";
    static final String TAG_REQUEST_NAME = "serv-cap-request-name";
    static final String TAG_ROAMING = "serv-cap-roaming";
    static final String TAG_RULE_DEACTIVATED = "serv-cap-rule-deactivated";
    static final String TAG_VALIDITY = "serv-cap-validity";
    public boolean mAnonymousProvisioned = false;
    public boolean mCommunicationDivertedProvisioned = false;
    public boolean mExternalListProvisioned = false;
    public boolean mIdentityProvisioned = false;
    public boolean mInternationalProvisioned = false;
    public boolean mInternationalexHCProvisioned = false;
    MediaConditions mMediaConditions;
    public boolean mOtherIdentityProvisioned = false;
    public boolean mPresenceStatusProvisioned = false;
    public boolean mRequestNameProvisioned = false;
    public boolean mRoamingProvisioned = false;
    public boolean mRuleDeactivatedProvisioned = false;
    public boolean mValidityProvisioned = false;

    public ConditionCapabilities(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    public ConditionCapabilities(XcapUri xcapUri, String parentUri, String intendedId, Node nodes) {
        super(xcapUri, parentUri, intendedId);
        instantiateFromXmlNode(nodes);
    }

    @Override // com.mediatek.simservs.xcap.ConfigureType
    public void instantiateFromXmlNode(Node domNode) {
        Element domElement = (Element) domNode;
        NodeList conditionNode = domElement.getElementsByTagName(TAG_ANONYMOUS);
        if (conditionNode.getLength() > 0) {
            this.mAnonymousProvisioned = ((Element) conditionNode.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode2 = domElement.getElementsByTagName(TAG_REQUEST_NAME);
        if (conditionNode2.getLength() > 0) {
            this.mRequestNameProvisioned = ((Element) conditionNode2.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode3 = domElement.getElementsByTagName(TAG_COMMUNICATION_DIVERTED);
        if (conditionNode3.getLength() > 0) {
            this.mCommunicationDivertedProvisioned = ((Element) conditionNode3.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode4 = domElement.getElementsByTagName(TAG_EXTERNAL_LIST);
        if (conditionNode4.getLength() > 0) {
            this.mExternalListProvisioned = ((Element) conditionNode4.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode5 = domElement.getElementsByTagName(TAG_IDENTITY);
        if (conditionNode5.getLength() > 0) {
            this.mIdentityProvisioned = ((Element) conditionNode5.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode6 = domElement.getElementsByTagName(TAG_INTERNATIONAL);
        if (conditionNode6.getLength() > 0) {
            this.mInternationalProvisioned = ((Element) conditionNode6.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode7 = domElement.getElementsByTagName(TAG_INTERNATIONAL_EXHC);
        if (conditionNode7.getLength() > 0) {
            this.mInternationalexHCProvisioned = ((Element) conditionNode7.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode8 = domElement.getElementsByTagName(TAG_OTHER_IDENTITY);
        if (conditionNode8.getLength() > 0) {
            this.mOtherIdentityProvisioned = ((Element) conditionNode8.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode9 = domElement.getElementsByTagName(TAG_PRESENCE_STATUS);
        if (conditionNode9.getLength() > 0) {
            this.mPresenceStatusProvisioned = ((Element) conditionNode9.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode10 = domElement.getElementsByTagName(TAG_ROAMING);
        if (conditionNode10.getLength() > 0) {
            this.mRoamingProvisioned = ((Element) conditionNode10.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode11 = domElement.getElementsByTagName(TAG_RULE_DEACTIVATED);
        if (conditionNode11.getLength() > 0) {
            this.mRuleDeactivatedProvisioned = ((Element) conditionNode11.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList conditionNode12 = domElement.getElementsByTagName(TAG_VALIDITY);
        if (conditionNode12.getLength() > 0) {
            this.mValidityProvisioned = ((Element) conditionNode12.item(0)).getAttribute(ServiceCapabilities.ATT_PROVISIONED).equals(XcapElement.TRUE);
        }
        NodeList mediassNode = domElement.getElementsByTagName("serv-cap-media");
        if (mediassNode.getLength() > 0) {
            this.mMediaConditions = new MediaConditions(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) mediassNode.item(0));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement, com.mediatek.simservs.capability.ServiceCapabilities
    public String getNodeName() {
        return NODE_NAME;
    }

    public boolean isAnonymousProvisioned() {
        return this.mAnonymousProvisioned;
    }

    public boolean isRequestNameProvisioned() {
        return this.mRequestNameProvisioned;
    }

    public boolean isCommunicationDivertedProvisioned() {
        return this.mCommunicationDivertedProvisioned;
    }

    public boolean isExternalListProvisioned() {
        return this.mExternalListProvisioned;
    }

    public boolean isIdentityProvisioned() {
        return this.mIdentityProvisioned;
    }

    public boolean isInternationalProvisioned() {
        return this.mInternationalProvisioned;
    }

    public boolean isInternationalexHCProvisioned() {
        return this.mInternationalexHCProvisioned;
    }

    public boolean isOtherIdentityProvisioned() {
        return this.mOtherIdentityProvisioned;
    }

    public boolean isPresenceStatusProvisioned() {
        return this.mPresenceStatusProvisioned;
    }

    public boolean isRoamingProvisioned() {
        return this.mRoamingProvisioned;
    }

    public boolean isRuleDeactivatedProvisioned() {
        return this.mRuleDeactivatedProvisioned;
    }

    public boolean isValidityProvisioned() {
        return this.mValidityProvisioned;
    }

    public MediaConditions getMediaConditions() {
        return this.mMediaConditions;
    }
}
