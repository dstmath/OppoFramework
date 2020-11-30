package com.mediatek.simservs.client.policy;

import android.net.Network;
import android.util.Log;
import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RuleSet extends XcapElement implements ConfigureType {
    public static final String NODE_NAME = "cp:ruleset";
    public static final String NODE_NAME_WITH_NAMESPACE = "cp:ruleset?xmlns(cp=urn:ietf:params:xml:ns:common-policy)";
    public List<Rule> mRules;

    public RuleSet(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
        this.mRules = new LinkedList();
    }

    public RuleSet(XcapUri xcapUri, String parentUri, String intendedId, Element domElement) {
        super(xcapUri, parentUri, intendedId);
        instantiateFromXmlNode(domElement);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME_WITH_NAMESPACE;
    }

    @Override // com.mediatek.simservs.xcap.ConfigureType
    public void instantiateFromXmlNode(Node domNode) {
        Element domElement = (Element) domNode;
        NodeList domNodes = domElement.getElementsByTagName("rule");
        this.mRules = new LinkedList();
        if (domNodes.getLength() > 0) {
            Log.d("RuleSet", "Got rule");
            for (int i = 0; i < domNodes.getLength(); i++) {
                XcapUri xcapUri = this.mXcapUri;
                Rule aRule = new Rule(xcapUri, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, (Element) domNodes.item(i));
                if (this.mNetwork != null) {
                    aRule.setNetwork(this.mNetwork);
                }
                if (this.mEtag != null) {
                    aRule.setEtag(this.mEtag);
                }
                this.mRules.add(aRule);
            }
        }
        NodeList domNodes2 = domElement.getElementsByTagNameNS("urn:ietf:params:xml:ns:common-policy", "rule");
        if (domNodes2.getLength() > 0) {
            Log.d("RuleSet", "Got rule with common policy namespace");
            for (int i2 = 0; i2 < domNodes2.getLength(); i2++) {
                XcapUri xcapUri2 = this.mXcapUri;
                Rule aRule2 = new Rule(xcapUri2, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, (Element) domNodes2.item(i2));
                if (this.mNetwork != null) {
                    aRule2.setNetwork(this.mNetwork);
                }
                if (this.mEtag != null) {
                    aRule2.setEtag(this.mEtag);
                }
                this.mRules.add(aRule2);
            }
        } else {
            NodeList domNodes3 = domElement.getElementsByTagName(Rule.NODE_NAME);
            if (domNodes3.getLength() > 0) {
                Log.d("RuleSet", "Got cp:rule");
                for (int i3 = 0; i3 < domNodes3.getLength(); i3++) {
                    XcapUri xcapUri3 = this.mXcapUri;
                    Rule aRule3 = new Rule(xcapUri3, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, (Element) domNodes3.item(i3));
                    if (this.mNetwork != null) {
                        aRule3.setNetwork(this.mNetwork);
                    }
                    if (this.mEtag != null) {
                        aRule3.setEtag(this.mEtag);
                    }
                    this.mRules.add(aRule3);
                }
            }
        }
        Log.d("RuleSet", "rules size:" + this.mRules.size());
    }

    private void unfoldRules(Rule aRule, Element element, String media) {
        if (aRule.getConditions().comprehendBusy()) {
            XcapUri xcapUri = this.mXcapUri;
            Rule ruleBusy = new Rule(xcapUri, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, element);
            if (this.mNetwork != null) {
                ruleBusy.setNetwork(this.mNetwork);
            }
            ruleBusy.getConditions().clearConditions();
            ruleBusy.getConditions().addBusy();
            if (media != null) {
                ruleBusy.getConditions().addMedia(media);
            }
            this.mRules.add(ruleBusy);
        }
        if (aRule.getConditions().comprehendNotReachable()) {
            XcapUri xcapUri2 = this.mXcapUri;
            Rule ruleNotReachable = new Rule(xcapUri2, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, element);
            if (this.mNetwork != null) {
                ruleNotReachable.setNetwork(this.mNetwork);
            }
            ruleNotReachable.getConditions().clearConditions();
            ruleNotReachable.getConditions().addNotReachable();
            if (media != null) {
                ruleNotReachable.getConditions().addMedia(media);
            }
            this.mRules.add(ruleNotReachable);
        }
        if (aRule.getConditions().comprehendInternational()) {
            XcapUri xcapUri3 = this.mXcapUri;
            Rule ruleInternational = new Rule(xcapUri3, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, element);
            if (this.mNetwork != null) {
                ruleInternational.setNetwork(this.mNetwork);
            }
            ruleInternational.getConditions().clearConditions();
            ruleInternational.getConditions().addInternational();
            if (media != null) {
                ruleInternational.getConditions().addMedia(media);
            }
            this.mRules.add(ruleInternational);
        }
        if (aRule.getConditions().comprehendInternationalExHc()) {
            XcapUri xcapUri4 = this.mXcapUri;
            Rule ruleInternationalExHc = new Rule(xcapUri4, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, element);
            if (this.mNetwork != null) {
                ruleInternationalExHc.setNetwork(this.mNetwork);
            }
            ruleInternationalExHc.getConditions().clearConditions();
            ruleInternationalExHc.getConditions().addInternational();
            if (media != null) {
                ruleInternationalExHc.getConditions().addMedia(media);
            }
            this.mRules.add(ruleInternationalExHc);
        }
        if (aRule.getConditions().comprehendNoAnswer()) {
            XcapUri xcapUri5 = this.mXcapUri;
            Rule ruleNoAnswer = new Rule(xcapUri5, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, element);
            if (this.mNetwork != null) {
                ruleNoAnswer.setNetwork(this.mNetwork);
            }
            ruleNoAnswer.getConditions().clearConditions();
            ruleNoAnswer.getConditions().addNoAnswer();
            if (media != null) {
                ruleNoAnswer.getConditions().addMedia(media);
            }
            this.mRules.add(ruleNoAnswer);
        }
        if (aRule.getConditions().comprehendRoaming()) {
            XcapUri xcapUri6 = this.mXcapUri;
            Rule ruleRoaming = new Rule(xcapUri6, this.mParentUri + "/" + NODE_NAME, this.mIntendedId, element);
            if (this.mNetwork != null) {
                ruleRoaming.setNetwork(this.mNetwork);
            }
            ruleRoaming.getConditions().clearConditions();
            ruleRoaming.getConditions().addRoaming();
            if (media != null) {
                ruleRoaming.getConditions().addMedia(media);
            }
            this.mRules.add(ruleRoaming);
        }
    }

    public List<Rule> getRules() {
        return this.mRules;
    }

    public Rule createNewRule(String id) {
        if (this.mRules == null) {
            this.mRules = new LinkedList();
        }
        XcapUri xcapUri = this.mXcapUri;
        Rule aRule = new Rule(xcapUri, this.mParentUri + "/" + NODE_NAME, this.mIntendedId);
        if (this.mNetwork != null) {
            aRule.setNetwork(this.mNetwork);
        }
        if (this.mEtag != null) {
            aRule.setEtag(this.mEtag);
        }
        aRule.setId(id);
        this.mRules.add(aRule);
        return aRule;
    }

    public void clearRules() {
        if (this.mRules == null) {
            this.mRules = new LinkedList();
        }
        this.mRules.clear();
    }

    public String toXmlString() {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement(NODE_NAME);
            document.appendChild(root);
            if (this.mRules != null) {
                for (Rule rule : this.mRules) {
                    root.appendChild(rule.toXmlElement(document));
                }
            }
            return domToXmlText(root);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            return null;
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return null;
        } catch (TransformerException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    @Override // com.mediatek.simservs.xcap.XcapElement
    public void setEtag(String etag) {
        this.mEtag = etag;
        for (Rule rule : this.mRules) {
            Log.d("RuleSet", "rule:" + rule.mId + ", set etag:" + etag);
            rule.setEtag(etag);
        }
    }

    @Override // com.mediatek.simservs.xcap.XcapElement
    public void setNetwork(Network network) {
        super.setNetwork(network);
        if (network != null) {
            for (Rule rule : this.mRules) {
                Log.d("RuleSet", "rule:" + rule.mId + ", netid:" + network);
                rule.setNetwork(network);
            }
        }
    }
}
