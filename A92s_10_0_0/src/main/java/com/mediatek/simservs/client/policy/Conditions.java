package com.mediatek.simservs.client.policy;

import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Conditions extends XcapElement implements ConfigureType {
    public static final String NODE_NAME = "cp:conditions";
    static final String TAG_ANONYMOUS = "anonymous";
    static final String TAG_BUSY = "busy";
    static final String TAG_COMMUNICATION_DIVERTED = "communication-diverted";
    static final String TAG_INTERNATIONAL = "international";
    static final String TAG_INTERNATIONAL_EXHC = "international-exHC";
    static final String TAG_MEDIA = "media";
    static final String TAG_NOT_REACHABLE = "not-reachable";
    static final String TAG_NOT_REGISTERED = "not-registered";
    static final String TAG_NO_ANSWER = "no-answer";
    static final String TAG_PRESENCE_STATUS = "presence-status";
    static final String TAG_ROAMING = "roaming";
    static final String TAG_RULE_DEACTIVATED = "rule-deactivated";
    static final String TAG_TIME = "time";
    public boolean mComprehendAnonymous;
    public boolean mComprehendBusy;
    public boolean mComprehendCommunicationDiverted;
    public boolean mComprehendInternational;
    public boolean mComprehendInternationalexHc;
    public boolean mComprehendNoAnswer;
    public boolean mComprehendNotReachable;
    public boolean mComprehendNotRegistered;
    public boolean mComprehendPresenceStatus;
    public boolean mComprehendRoaming;
    public boolean mComprehendRuleDeactivated;
    public String mComprehendTime;
    public List<String> mMedias;

    public Conditions(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
        this.mComprehendBusy = false;
        this.mComprehendNoAnswer = false;
        this.mComprehendNotReachable = false;
        this.mComprehendNotRegistered = false;
        this.mComprehendRoaming = false;
        this.mComprehendRuleDeactivated = false;
        this.mComprehendInternational = false;
        this.mComprehendInternationalexHc = false;
        this.mComprehendCommunicationDiverted = false;
        this.mComprehendPresenceStatus = false;
        this.mComprehendAnonymous = false;
        this.mMedias = new LinkedList();
    }

    public Conditions(XcapUri xcapUri, String parentUri, String intendedId, Element domElement) {
        super(xcapUri, parentUri, intendedId);
        this.mComprehendBusy = false;
        this.mComprehendNoAnswer = false;
        this.mComprehendNotReachable = false;
        this.mComprehendNotRegistered = false;
        this.mComprehendRoaming = false;
        this.mComprehendRuleDeactivated = false;
        this.mComprehendInternational = false;
        this.mComprehendInternationalexHc = false;
        this.mComprehendCommunicationDiverted = false;
        this.mComprehendPresenceStatus = false;
        this.mComprehendAnonymous = false;
        instantiateFromXmlNode(domElement);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override // com.mediatek.simservs.xcap.ConfigureType
    public void instantiateFromXmlNode(Node domNode) {
        Element domElement = (Element) domNode;
        if (domElement.getElementsByTagName(TAG_BUSY).getLength() > 0) {
            this.mComprehendBusy = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_BUSY).getLength() > 0) {
            this.mComprehendBusy = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_BUSY).getLength() > 0) {
                this.mComprehendBusy = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_NO_ANSWER).getLength() > 0) {
            this.mComprehendNoAnswer = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_NO_ANSWER).getLength() > 0) {
            this.mComprehendNoAnswer = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_NO_ANSWER).getLength() > 0) {
                this.mComprehendNoAnswer = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_NOT_REACHABLE).getLength() > 0) {
            this.mComprehendNotReachable = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_NOT_REACHABLE).getLength() > 0) {
            this.mComprehendNotReachable = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_NOT_REACHABLE).getLength() > 0) {
                this.mComprehendNotReachable = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_NOT_REGISTERED).getLength() > 0) {
            this.mComprehendNotRegistered = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_NOT_REGISTERED).getLength() > 0) {
            this.mComprehendNotRegistered = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_NOT_REGISTERED).getLength() > 0) {
                this.mComprehendNotRegistered = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_ROAMING).getLength() > 0) {
            this.mComprehendRoaming = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_ROAMING).getLength() > 0) {
            this.mComprehendRoaming = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_ROAMING).getLength() > 0) {
                this.mComprehendRoaming = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_RULE_DEACTIVATED).getLength() > 0) {
            this.mComprehendRuleDeactivated = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_RULE_DEACTIVATED).getLength() > 0) {
            this.mComprehendRuleDeactivated = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_RULE_DEACTIVATED).getLength() > 0) {
                this.mComprehendRuleDeactivated = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_INTERNATIONAL).getLength() > 0) {
            this.mComprehendInternational = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_INTERNATIONAL).getLength() > 0) {
            this.mComprehendInternational = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_INTERNATIONAL).getLength() > 0) {
                this.mComprehendInternational = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_INTERNATIONAL_EXHC).getLength() > 0) {
            this.mComprehendInternationalexHc = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_INTERNATIONAL_EXHC).getLength() > 0) {
            this.mComprehendInternationalexHc = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_INTERNATIONAL_EXHC).getLength() > 0) {
                this.mComprehendInternationalexHc = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_COMMUNICATION_DIVERTED).getLength() > 0) {
            this.mComprehendCommunicationDiverted = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_COMMUNICATION_DIVERTED).getLength() > 0) {
            this.mComprehendCommunicationDiverted = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_COMMUNICATION_DIVERTED).getLength() > 0) {
                this.mComprehendCommunicationDiverted = true;
            }
        }
        if (domElement.getElementsByTagName(TAG_PRESENCE_STATUS).getLength() > 0) {
            this.mComprehendPresenceStatus = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_PRESENCE_STATUS).getLength() > 0) {
            this.mComprehendPresenceStatus = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_PRESENCE_STATUS).getLength() > 0) {
                this.mComprehendPresenceStatus = true;
            }
        }
        NodeList conditionsNode = domElement.getElementsByTagName(TAG_MEDIA);
        this.mMedias = new LinkedList();
        if (conditionsNode.getLength() > 0) {
            for (int i = 0; i < conditionsNode.getLength(); i++) {
                this.mMedias.add(((Element) conditionsNode.item(i)).getTextContent());
            }
        } else {
            NodeList conditionsNode2 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_MEDIA);
            if (conditionsNode2.getLength() > 0) {
                for (int i2 = 0; i2 < conditionsNode2.getLength(); i2++) {
                    this.mMedias.add(((Element) conditionsNode2.item(i2)).getTextContent());
                }
            } else {
                NodeList conditionsNode3 = domElement.getElementsByTagName("ss:" + TAG_MEDIA);
                if (conditionsNode3.getLength() > 0) {
                    for (int i3 = 0; i3 < conditionsNode3.getLength(); i3++) {
                        this.mMedias.add(((Element) conditionsNode3.item(i3)).getTextContent());
                    }
                }
            }
        }
        if (domElement.getElementsByTagName(TAG_ANONYMOUS).getLength() > 0) {
            this.mComprehendAnonymous = true;
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_ANONYMOUS).getLength() > 0) {
            this.mComprehendAnonymous = true;
        } else {
            if (domElement.getElementsByTagName("ss:" + TAG_ANONYMOUS).getLength() > 0) {
                this.mComprehendAnonymous = true;
            }
        }
        NodeList conditionsNode4 = domElement.getElementsByTagName(TAG_TIME);
        if (conditionsNode4.getLength() > 0) {
            this.mComprehendTime = ((Element) conditionsNode4.item(0)).getTextContent();
            return;
        }
        NodeList conditionsNode5 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_TIME);
        if (conditionsNode5.getLength() > 0) {
            this.mComprehendTime = ((Element) conditionsNode5.item(0)).getTextContent();
            return;
        }
        NodeList conditionsNode6 = domElement.getElementsByTagName("ss:" + TAG_TIME);
        if (conditionsNode6.getLength() > 0) {
            this.mComprehendTime = ((Element) conditionsNode6.item(0)).getTextContent();
        }
    }

    public Element toXmlElement(Document document) {
        if (XcapElement.TRUE.equals(System.getProperty("xcap.ns.ss", XcapElement.FALSE))) {
            Element conditionsElement = document.createElement(NODE_NAME);
            if (comprehendBusy()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:busy"));
            }
            if (comprehendNoAnswer()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:no-answer"));
            }
            if (comprehendNotReachable()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:not-reachable"));
            }
            if (comprehendNotRegistered()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:not-registered"));
            }
            if (comprehendRoaming()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:roaming"));
            }
            if (comprehendRuleDeactivated()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:rule-deactivated"));
            }
            if (comprehendInternational()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:international"));
            }
            if (comprehendInternationalExHc()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:international-exHC"));
            }
            if (comprehendCommunicationDiverted()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:communication-diverted"));
            }
            if (comprehendPresenceStatus()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:presence-status"));
            }
            List<String> list = this.mMedias;
            if (list != null && list.size() > 0) {
                for (String str : this.mMedias) {
                    Element ruleElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:media");
                    ruleElement.setTextContent(str);
                    conditionsElement.appendChild(ruleElement);
                }
            }
            if (comprehendAnonymous()) {
                conditionsElement.appendChild(document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:anonymous"));
            }
            if (comprehendTime() != null && !comprehendTime().isEmpty()) {
                Element conditionElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:time");
                conditionElement.setTextContent(this.mComprehendTime);
                conditionsElement.appendChild(conditionElement);
            }
            return conditionsElement;
        }
        Element conditionsElement2 = document.createElement(NODE_NAME);
        if (comprehendBusy()) {
            conditionsElement2.appendChild(document.createElement(TAG_BUSY));
        }
        if (comprehendNoAnswer()) {
            conditionsElement2.appendChild(document.createElement(TAG_NO_ANSWER));
        }
        if (comprehendNotReachable()) {
            conditionsElement2.appendChild(document.createElement(TAG_NOT_REACHABLE));
        }
        if (comprehendNotRegistered()) {
            conditionsElement2.appendChild(document.createElement(TAG_NOT_REGISTERED));
        }
        if (comprehendRoaming()) {
            conditionsElement2.appendChild(document.createElement(TAG_ROAMING));
        }
        if (comprehendRuleDeactivated()) {
            conditionsElement2.appendChild(document.createElement(TAG_RULE_DEACTIVATED));
        }
        if (comprehendInternational()) {
            conditionsElement2.appendChild(document.createElement(TAG_INTERNATIONAL));
        }
        if (comprehendInternationalExHc()) {
            conditionsElement2.appendChild(document.createElement(TAG_INTERNATIONAL_EXHC));
        }
        if (comprehendCommunicationDiverted()) {
            conditionsElement2.appendChild(document.createElement(TAG_COMMUNICATION_DIVERTED));
        }
        if (comprehendPresenceStatus()) {
            conditionsElement2.appendChild(document.createElement(TAG_PRESENCE_STATUS));
        }
        List<String> list2 = this.mMedias;
        if (list2 != null && list2.size() > 0) {
            for (String str2 : this.mMedias) {
                Element ruleElement2 = document.createElement(TAG_MEDIA);
                ruleElement2.setTextContent(str2);
                conditionsElement2.appendChild(ruleElement2);
            }
        }
        if (comprehendAnonymous()) {
            conditionsElement2.appendChild(document.createElement(TAG_ANONYMOUS));
        }
        if (comprehendTime() != null && !comprehendTime().isEmpty()) {
            Element conditionElement2 = document.createElement(TAG_TIME);
            conditionElement2.setTextContent(this.mComprehendTime);
            conditionsElement2.appendChild(conditionElement2);
        }
        return conditionsElement2;
    }

    public void addBusy() {
        this.mComprehendBusy = true;
    }

    public void addNoAnswer() {
        this.mComprehendNoAnswer = true;
    }

    public void addNotReachable() {
        this.mComprehendNotReachable = true;
    }

    public void addNotRegistered() {
        this.mComprehendNotRegistered = true;
    }

    public void addRoaming() {
        this.mComprehendRoaming = true;
    }

    public void addRuleDeactivated() {
        this.mComprehendRuleDeactivated = true;
    }

    public void addInternational() {
        this.mComprehendInternational = true;
    }

    public void addInternationalExHc() {
        this.mComprehendInternationalexHc = true;
    }

    public void addCommunicationDiverted() {
        this.mComprehendCommunicationDiverted = true;
    }

    public void addPresenceStatus() {
        this.mComprehendPresenceStatus = true;
    }

    public void addAnonymous() {
        this.mComprehendAnonymous = true;
    }

    public void removeRuleDeactivated() {
        this.mComprehendRuleDeactivated = false;
    }

    public boolean comprehendBusy() {
        return this.mComprehendBusy;
    }

    public boolean comprehendNoAnswer() {
        return this.mComprehendNoAnswer;
    }

    public boolean comprehendNotReachable() {
        return this.mComprehendNotReachable;
    }

    public boolean comprehendNotRegistered() {
        return this.mComprehendNotRegistered;
    }

    public boolean comprehendRoaming() {
        return this.mComprehendRoaming;
    }

    public boolean comprehendRuleDeactivated() {
        return this.mComprehendRuleDeactivated;
    }

    public boolean comprehendInternational() {
        return this.mComprehendInternational;
    }

    public boolean comprehendInternationalExHc() {
        return this.mComprehendInternationalexHc;
    }

    public boolean comprehendCommunicationDiverted() {
        return this.mComprehendCommunicationDiverted;
    }

    public boolean comprehendPresenceStatus() {
        return this.mComprehendPresenceStatus;
    }

    public boolean comprehendAnonymous() {
        return this.mComprehendAnonymous;
    }

    public void addTime(String time) {
        this.mComprehendTime = time;
    }

    public String comprehendTime() {
        return this.mComprehendTime;
    }

    public void addMedia(String media) {
        if (this.mMedias == null) {
            this.mMedias = new LinkedList();
        }
        this.mMedias.add(media);
    }

    public List<String> getMedias() {
        return this.mMedias;
    }

    public void clearConditions() {
        this.mComprehendBusy = false;
        this.mComprehendNoAnswer = false;
        this.mComprehendNotReachable = false;
        this.mComprehendNotRegistered = false;
        this.mComprehendRoaming = false;
        this.mComprehendRuleDeactivated = false;
        this.mComprehendInternational = false;
        this.mComprehendCommunicationDiverted = false;
        this.mComprehendPresenceStatus = false;
        if (this.mMedias == null) {
            this.mMedias = new LinkedList();
        }
        this.mMedias.clear();
        this.mComprehendAnonymous = false;
        this.mComprehendTime = null;
    }
}
