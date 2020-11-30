package com.mediatek.simservs.client;

import android.net.Network;
import android.util.Log;
import com.mediatek.simservs.client.policy.Rule;
import com.mediatek.simservs.client.policy.RuleSet;
import com.mediatek.simservs.xcap.RuleType;
import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.uri.XcapUri;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OutgoingCommunicationBarring extends SimservType implements RuleType {
    public static final String NODE_NAME = "outgoing-communication-barring";
    RuleSet mRuleSet;

    public OutgoingCommunicationBarring(XcapUri documentUri, String parentUri, String intendedId) throws XcapException, ParserConfigurationException {
        super(documentUri, parentUri, intendedId);
    }

    @Override // com.mediatek.simservs.client.SimservType
    public void initServiceInstance(Document domDoc) {
        NodeList ruleSetNode = domDoc.getElementsByTagName("ruleset");
        if (ruleSetNode.getLength() > 0) {
            Log.d("OutgoingCommunicationBarring", "Got ruleset");
            this.mRuleSet = new RuleSet(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) ruleSetNode.item(0));
            if (this.mNetwork != null) {
                this.mRuleSet.setNetwork(this.mNetwork);
            }
            if (this.mEtag != null) {
                this.mRuleSet.setEtag(this.mEtag);
                return;
            }
            return;
        }
        NodeList ruleSetNode2 = domDoc.getElementsByTagNameNS("urn:ietf:params:xml:ns:common-policy", "ruleset");
        if (ruleSetNode2.getLength() > 0) {
            Log.d("OutgoingCommunicationBarring", "Got ruleset");
            this.mRuleSet = new RuleSet(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) ruleSetNode2.item(0));
            if (this.mNetwork != null) {
                this.mRuleSet.setNetwork(this.mNetwork);
            }
            if (this.mEtag != null) {
                this.mRuleSet.setEtag(this.mEtag);
                return;
            }
            return;
        }
        NodeList ruleSetNode3 = domDoc.getElementsByTagName(RuleSet.NODE_NAME);
        if (ruleSetNode3.getLength() > 0) {
            Log.d("OutgoingCommunicationBarring", "Got cp:ruleset");
            this.mRuleSet = new RuleSet(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) ruleSetNode3.item(0));
            if (this.mNetwork != null) {
                this.mRuleSet.setNetwork(this.mNetwork);
            }
            if (this.mEtag != null) {
                this.mRuleSet.setEtag(this.mEtag);
                return;
            }
            return;
        }
        this.mRuleSet = new RuleSet(this.mXcapUri, NODE_NAME, this.mIntendedId);
        if (this.mNetwork != null) {
            this.mRuleSet.setNetwork(this.mNetwork);
        }
        if (this.mEtag != null) {
            this.mRuleSet.setEtag(this.mEtag);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override // com.mediatek.simservs.xcap.RuleType
    public RuleSet getRuleSet() {
        return this.mRuleSet;
    }

    @Override // com.mediatek.simservs.xcap.RuleType
    public void saveRuleSet() throws XcapException {
        String ruleXml = this.mRuleSet.toXmlString();
        if (ruleXml == null) {
            Log.e("OutgoingCommunicationBarring", "saveRuleSet: null xml");
            return;
        }
        this.mRuleSet.setContent(ruleXml);
        if (this.mRuleSet.getEtag() != null) {
            this.mEtag = this.mRuleSet.getEtag();
            Log.d("OutgoingCommunicationBarring", "saveRuleSet: mEtag=" + this.mEtag);
            this.mRuleSet.setEtag(this.mEtag);
        }
    }

    @Override // com.mediatek.simservs.xcap.RuleType
    public RuleSet createNewRuleSet() {
        this.mRuleSet = new RuleSet(this.mXcapUri, NODE_NAME, this.mIntendedId);
        if (this.mNetwork != null) {
            this.mRuleSet.setNetwork(this.mNetwork);
        }
        if (this.mEtag != null) {
            this.mRuleSet.setEtag(this.mEtag);
        }
        return this.mRuleSet;
    }

    @Override // com.mediatek.simservs.xcap.RuleType
    public void saveRule(String ruleId) throws XcapException {
        if (ruleId == null || ruleId.isEmpty()) {
            Log.d("saveRule", "ruleId is null");
            return;
        }
        Iterator<Rule> it = ((LinkedList) this.mRuleSet.getRules()).iterator();
        while (it.hasNext()) {
            Rule rule = it.next();
            if (ruleId.equals(rule.mId)) {
                saveRule(rule);
                return;
            }
        }
    }

    @Override // com.mediatek.simservs.xcap.RuleType
    public void saveRule(Rule rule) throws XcapException {
        if (rule != null) {
            String ruleXml = rule.toXmlString();
            if (ruleXml == null) {
                Log.e("OutgoingCommunicationBarring", "saveRule: null xml: " + rule.mId);
                return;
            }
            rule.setContent(ruleXml);
            if (rule.getEtag() != null) {
                this.mEtag = rule.getEtag();
                Log.d("OutgoingCommunicationBarring", "saveRule: mEtag=" + this.mEtag);
                this.mRuleSet.setEtag(this.mEtag);
                return;
            }
            return;
        }
        Log.d("saveRule", "rule is null");
    }

    public void save(boolean active) throws XcapException {
        saveNode(active, this.mRuleSet);
        if (this.mEtag != null) {
            Log.d("OutgoingCommunicationBarring", "save: mEtag=" + this.mEtag);
            this.mRuleSet.setEtag(this.mEtag);
        }
    }

    @Override // com.mediatek.simservs.xcap.XcapElement
    public void setNetwork(Network network) {
        super.setNetwork(network);
        if (network != null && this.mRuleSet != null) {
            Log.d(SimservType.TAG, "XCAP dedicated network netid to mRuleSet:" + network);
            this.mRuleSet.setNetwork(network);
        }
    }
}
