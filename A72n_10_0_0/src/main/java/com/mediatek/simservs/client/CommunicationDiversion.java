package com.mediatek.simservs.client;

import android.net.Network;
import android.util.Log;
import com.mediatek.simservs.client.policy.Rule;
import com.mediatek.simservs.client.policy.RuleSet;
import com.mediatek.simservs.xcap.RuleType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.uri.XcapUri;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CommunicationDiversion extends SimservType implements RuleType {
    public static final String NODE_NAME = "communication-diversion";
    private static final int NO_REPLY_TIMER_DEFAULT_VAULE = -2;
    NoReplyTimer mNoReplyTimer;
    RuleSet mRuleSet;

    public CommunicationDiversion(XcapUri documentUri, String parentUri, String intendedId) throws XcapException, ParserConfigurationException {
        super(documentUri, parentUri, intendedId);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override // com.mediatek.simservs.client.SimservType
    public void initServiceInstance(Document domDoc) {
        int noReplyTimer;
        int noReplyTimer2;
        int noReplyTimer3;
        NodeList noReplyTimerNode = domDoc.getElementsByTagName("NoReplyTimer");
        if (noReplyTimerNode.getLength() > 0) {
            Log.d("CommunicationDiversion", "Got NoReplyTimer");
            String prefix = noReplyTimerNode.item(0).getPrefix();
            String namespaceUri = noReplyTimerNode.item(0).getNamespaceURI();
            try {
                noReplyTimer3 = Integer.parseInt(((Element) noReplyTimerNode.item(0)).getFirstChild().getTextContent().trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                noReplyTimer3 = NO_REPLY_TIMER_DEFAULT_VAULE;
            }
            this.mNoReplyTimer = new NoReplyTimer(this, this.mXcapUri, NODE_NAME, this.mIntendedId, noReplyTimer3, prefix, namespaceUri);
            if (this.mNetwork != null) {
                this.mNoReplyTimer.setNetwork(this.mNetwork);
            }
            if (this.mEtag != null) {
                this.mNoReplyTimer.setEtag(this.mEtag);
            }
        } else {
            NodeList noReplyTimerNode2 = domDoc.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "NoReplyTimer");
            if (noReplyTimerNode2.getLength() > 0) {
                Log.d("CommunicationDiversion", "Got NoReplyTimer with xcap namespace");
                String prefix2 = noReplyTimerNode2.item(0).getPrefix();
                String namespaceUri2 = noReplyTimerNode2.item(0).getNamespaceURI();
                try {
                    noReplyTimer2 = Integer.parseInt(((Element) noReplyTimerNode2.item(0)).getFirstChild().getTextContent().trim());
                } catch (NumberFormatException e2) {
                    e2.printStackTrace();
                    noReplyTimer2 = NO_REPLY_TIMER_DEFAULT_VAULE;
                }
                this.mNoReplyTimer = new NoReplyTimer(this, this.mXcapUri, NODE_NAME, this.mIntendedId, noReplyTimer2, prefix2, namespaceUri2);
                if (this.mNetwork != null) {
                    this.mNoReplyTimer.setNetwork(this.mNetwork);
                }
                if (this.mEtag != null) {
                    this.mNoReplyTimer.setEtag(this.mEtag);
                }
            } else {
                NodeList noReplyTimerNode3 = domDoc.getElementsByTagName("ss:NoReplyTimer");
                if (noReplyTimerNode3.getLength() > 0) {
                    Log.d("CommunicationDiversion", "Got ss:NoReplyTimer");
                    String prefix3 = noReplyTimerNode3.item(0).getPrefix();
                    String namespaceUri3 = noReplyTimerNode3.item(0).getNamespaceURI();
                    try {
                        noReplyTimer = Integer.parseInt(((Element) noReplyTimerNode3.item(0)).getFirstChild().getTextContent().trim());
                    } catch (NumberFormatException e3) {
                        e3.printStackTrace();
                        noReplyTimer = NO_REPLY_TIMER_DEFAULT_VAULE;
                    }
                    this.mNoReplyTimer = new NoReplyTimer(this, this.mXcapUri, NODE_NAME, this.mIntendedId, noReplyTimer, prefix3, namespaceUri3);
                    if (this.mNetwork != null) {
                        this.mNoReplyTimer.setNetwork(this.mNetwork);
                    }
                    if (this.mEtag != null) {
                        this.mNoReplyTimer.setEtag(this.mEtag);
                    }
                } else {
                    this.mNoReplyTimer = new NoReplyTimer(this.mXcapUri, NODE_NAME, this.mIntendedId, -1);
                    if (this.mNetwork != null) {
                        this.mNoReplyTimer.setNetwork(this.mNetwork);
                    }
                    if (this.mEtag != null) {
                        this.mNoReplyTimer.setEtag(this.mEtag);
                    }
                }
            }
        }
        NodeList ruleSetNode = domDoc.getElementsByTagName("ruleset");
        String str = this.mPrefix;
        String tmpNodeName = NODE_NAME;
        if (str != null) {
            tmpNodeName = this.mPrefix + ":" + tmpNodeName;
        }
        if (ruleSetNode.getLength() > 0) {
            Log.d("CommunicationDiversion", "Got ruleset");
            this.mRuleSet = new RuleSet(this.mXcapUri, tmpNodeName, this.mIntendedId, (Element) ruleSetNode.item(0));
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
            Log.d("CommunicationDiversion", "Got ruleset with commmon policy namespace");
            this.mRuleSet = new RuleSet(this.mXcapUri, tmpNodeName, this.mIntendedId, (Element) ruleSetNode2.item(0));
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
            Log.d("CommunicationDiversion", "Got cp:ruleset");
            this.mRuleSet = new RuleSet(this.mXcapUri, tmpNodeName, this.mIntendedId, (Element) ruleSetNode3.item(0));
            if (this.mNetwork != null) {
                this.mRuleSet.setNetwork(this.mNetwork);
            }
            if (this.mEtag != null) {
                this.mRuleSet.setEtag(this.mEtag);
                return;
            }
            return;
        }
        this.mRuleSet = new RuleSet(this.mXcapUri, tmpNodeName, this.mIntendedId);
        if (this.mNetwork != null) {
            this.mRuleSet.setNetwork(this.mNetwork);
        }
        if (this.mEtag != null) {
            this.mRuleSet.setEtag(this.mEtag);
        }
    }

    @Override // com.mediatek.simservs.xcap.XcapElement
    public void setNetwork(Network network) {
        super.setNetwork(network);
        if (network != null) {
            if (this.mNoReplyTimer != null) {
                Log.d(SimservType.TAG, "XCAP dedicated network netid to mNoReplyTimer:" + network);
                this.mNoReplyTimer.setNetwork(this.mNetwork);
            }
            if (this.mRuleSet != null) {
                Log.d(SimservType.TAG, "XCAP dedicated network netid to mRuleSet:" + network);
                this.mRuleSet.setNetwork(this.mNetwork);
            }
        }
    }

    public int getNoReplyTimer() {
        return this.mNoReplyTimer.getValue();
    }

    public void setNoReplyTimer(int timerValue, boolean isSentToNW) throws XcapException {
        this.mNoReplyTimer.setValue(timerValue);
        if (isSentToNW) {
            this.mNoReplyTimer.setContent(this.mNoReplyTimer.toXmlString());
            if (this.mNoReplyTimer.getEtag() != null) {
                this.mEtag = this.mNoReplyTimer.getEtag();
                Log.d("CommunicationDiversion", "setNoReplyTimer: mEtag=" + this.mEtag);
                this.mRuleSet.setEtag(this.mEtag);
            }
        }
    }

    @Override // com.mediatek.simservs.xcap.RuleType
    public RuleSet getRuleSet() {
        return this.mRuleSet;
    }

    @Override // com.mediatek.simservs.xcap.RuleType
    public void saveRuleSet() throws XcapException {
        String ruleXml = this.mRuleSet.toXmlString();
        if (ruleXml == null) {
            Log.e("CommunicationDiversion", "saveRuleSet: null xml string");
            return;
        }
        this.mRuleSet.setContent(ruleXml);
        if (this.mRuleSet.getEtag() != null) {
            this.mEtag = this.mRuleSet.getEtag();
            Log.d("CommunicationDiversion", "saveRuleSet: mEtag=" + this.mEtag);
            this.mRuleSet.setEtag(this.mEtag);
            this.mNoReplyTimer.setEtag(this.mEtag);
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
                Log.e("CommunicationDiversion", "saveRule: null xml string: " + rule.mId);
                return;
            }
            rule.setContent(ruleXml, this.mHasXcapNS);
            if (rule.getEtag() != null) {
                this.mEtag = rule.getEtag();
                Log.d("CommunicationDiversion", "saveRule: mEtag=" + this.mEtag);
                this.mRuleSet.setEtag(this.mEtag);
                this.mNoReplyTimer.setEtag(this.mEtag);
                return;
            }
            return;
        }
        Log.d("saveRule", "rule is null");
    }

    public void save(boolean active) throws XcapException {
        saveNode(active, this.mRuleSet);
        if (this.mEtag != null) {
            Log.d("CommunicationDiversion", "save: mEtag=" + this.mEtag);
            this.mRuleSet.setEtag(this.mEtag);
            this.mNoReplyTimer.setEtag(this.mEtag);
        }
    }

    public class NoReplyTimer extends XcapElement {
        public static final String NODE_NAME = "NoReplyTimer";
        private static final String NODE_XML_NAMESPACE_FORMAT = "?xmlns(%s=%s)";
        private String mNameSpace;
        private String mNodeName;
        private String mPrefix;
        public int mValue;

        public NoReplyTimer(XcapUri cdUri, String parentUri, String intendedId) {
            super(cdUri, parentUri, intendedId);
        }

        public NoReplyTimer(XcapUri cdUri, String parentUri, String intendedId, int initValue) {
            super(cdUri, parentUri, intendedId);
            this.mValue = initValue;
        }

        public NoReplyTimer(CommunicationDiversion this$02, XcapUri cdUri, String parentUri, String intendedId, int initValue, String prefix, String namespaceUri) {
            this(cdUri, parentUri, intendedId, initValue);
            this.mPrefix = prefix;
            if (!(prefix == null || namespaceUri == null)) {
                this.mNameSpace = String.format(NODE_XML_NAMESPACE_FORMAT, prefix, namespaceUri);
                this.mNodeName = String.format("%s:NoReplyTimer", prefix);
            }
            Log.d("[NoReplyTimer]", "prefix =" + this.mPrefix + ", uri =" + this.mNameSpace + "\n[NoReplyTimer]node =" + this.mNodeName);
        }

        /* access modifiers changed from: protected */
        @Override // com.mediatek.simservs.xcap.XcapElement
        public String getNodeName() {
            return this.mPrefix != null ? this.mNodeName : "NoReplyTimer";
        }

        public int getValue() {
            return this.mValue;
        }

        public void setValue(int value) {
            this.mValue = value;
        }

        @Override // com.mediatek.simservs.xcap.XcapElement
        public void setContent(String xml) throws XcapException {
            try {
                this.mNodeUri = getNodeUri().toString();
                if (this.mPrefix != null) {
                    this.mNodeUri += this.mNameSpace;
                }
                Log.d("NoReplyTimer", "setContent etag=" + this.mEtag);
                saveContent(xml);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        public String toXmlString() {
            String nodeName = this.mPrefix != null ? this.mNodeName : "NoReplyTimer";
            return String.format("<%s>" + String.valueOf(this.mValue) + "</%s>", nodeName, nodeName);
        }
    }
}
