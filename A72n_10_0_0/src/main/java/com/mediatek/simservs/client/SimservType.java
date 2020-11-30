package com.mediatek.simservs.client;

import android.util.Log;
import com.mediatek.simservs.client.policy.RuleSet;
import com.mediatek.simservs.xcap.InquireType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.XcapDebugParam;
import com.mediatek.xcap.client.uri.XcapUri;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class SimservType extends InquireType {
    static final String ATT_ACTIVE = "active";
    public static final String TAG = "SimservType";
    public boolean mActived = false;
    protected boolean mHasXcapNS = false;
    protected String mPrefix = null;
    String mSsTc;

    public abstract void initServiceInstance(Document document);

    public SimservType(XcapUri xcapUri, String parentUri, String intendedId) throws XcapException, ParserConfigurationException {
        super(xcapUri, parentUri, intendedId);
        Log.d(TAG, "Xcap debug params: \n" + this.mDebugParams.toString());
    }

    public void refresh() throws Exception {
        loadConfiguration();
    }

    public boolean isSupportEtag() {
        return this.mIsSupportEtag;
    }

    /* access modifiers changed from: protected */
    public void loadConfiguration() throws XcapException, ParserConfigurationException {
        String xmlContent;
        Document doc;
        String nodeName = getNodeName();
        Log.d(TAG, "loadConfiguration():nodeName=" + nodeName);
        if (XcapDebugParam.getInstance().getEnablePredefinedSimservQueryResult()) {
            String xmlContent2 = readXmlFromFile("/data/ss.xml");
            if (!xmlContent2.contains(nodeName)) {
                Log.d(TAG, "loadConfiguration():fail to get tested xml for nodeName=" + nodeName);
                return;
            }
            Log.d(TAG, "loadConfiguration():get tested xml for nodeName=" + nodeName);
            xmlContent = xmlContent2;
        } else {
            xmlContent = getContent();
        }
        if (SimServs.sDebug) {
            Log.v(TAG, "xmlContent=" + xmlContent);
        }
        if (xmlContent != null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder db = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlContent));
            try {
                doc = db.parse(is);
            } catch (SAXException e) {
                factory.setNamespaceAware(false);
                DocumentBuilder db2 = factory.newDocumentBuilder();
                InputSource is2 = new InputSource();
                is2.setCharacterStream(new StringReader(xmlContent));
                try {
                    doc = db2.parse(is2);
                } catch (SAXException err) {
                    err.printStackTrace();
                    throw new XcapException(500);
                } catch (IOException err2) {
                    err2.printStackTrace();
                    throw new XcapException(500);
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                throw new XcapException(500);
            }
            NodeList currentNode = doc.getElementsByTagName(getNodeName());
            if (SimServs.sDebug) {
                Log.v(TAG, "getNodeName()=" + getNodeName());
            }
            if (currentNode.getLength() > 0) {
                NamedNodeMap map = ((Element) currentNode.item(0)).getAttributes();
                this.mPrefix = currentNode.item(0).getPrefix();
                this.mHasXcapNS = false;
                Log.d("loadConfiguration", "Got " + getNodeName());
                if (map.getLength() > 0) {
                    int i = 0;
                    while (true) {
                        if (i >= map.getLength()) {
                            break;
                        }
                        Node node = map.item(i);
                        if (node.getNodeName().equals(ATT_ACTIVE)) {
                            this.mActived = node.getNodeValue().endsWith(XcapElement.TRUE);
                            break;
                        }
                        i++;
                    }
                }
            } else {
                NodeList currentNode2 = doc.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", getNodeName());
                if (currentNode2.getLength() > 0) {
                    NamedNodeMap map2 = ((Element) currentNode2.item(0)).getAttributes();
                    this.mPrefix = currentNode2.item(0).getPrefix();
                    this.mHasXcapNS = true;
                    Log.i("loadConfiguration", "Got " + getNodeName() + ":http://uri.etsi.org/ngn/params/xml/simservs/xcap, prefix=" + this.mPrefix + ", NS=" + this.mHasXcapNS);
                    if (map2.getLength() > 0) {
                        int i2 = 0;
                        while (true) {
                            if (i2 >= map2.getLength()) {
                                break;
                            }
                            Node node2 = map2.item(i2);
                            if (node2.getNodeName().equals(ATT_ACTIVE)) {
                                this.mActived = node2.getNodeValue().endsWith(XcapElement.TRUE);
                                break;
                            }
                            i2++;
                        }
                    }
                } else {
                    NodeList currentNode3 = doc.getElementsByTagName("ss:" + getNodeName());
                    if (currentNode3.getLength() > 0) {
                        NamedNodeMap map3 = ((Element) currentNode3.item(0)).getAttributes();
                        this.mPrefix = currentNode3.item(0).getPrefix();
                        this.mHasXcapNS = false;
                        Log.i("loadConfiguration", "Got ss:" + getNodeName());
                        if (map3.getLength() > 0) {
                            int i3 = 0;
                            while (true) {
                                if (i3 >= map3.getLength()) {
                                    break;
                                }
                                Node node3 = map3.item(i3);
                                if (node3.getNodeName().equals(ATT_ACTIVE)) {
                                    this.mActived = node3.getNodeValue().endsWith(XcapElement.TRUE);
                                    break;
                                }
                                i3++;
                            }
                        }
                    }
                }
            }
            if (SimServs.sDebug) {
                Log.v(TAG, "xmldoc=" + doc.toString());
            }
            initServiceInstance(doc);
        }
    }

    public boolean isActive() {
        return this.mActived;
    }

    public void setActive(boolean active) throws XcapException {
        String xml;
        this.mActived = active;
        String str = XcapElement.FALSE;
        String useXcapNs = System.getProperty("xcap.ns.ss", str);
        String property = System.getProperty("xcap.attr.active.quote", str);
        String str2 = XcapElement.TRUE;
        boolean quotationMarkNeeded = str2.equals(property);
        if (str2.equals(useXcapNs)) {
            if (this.mActived) {
                xml = "<ss:" + getNodeName() + " active=\"true\"/>";
            } else {
                xml = "<ss:" + getNodeName() + " active=\"false\"/>";
            }
            setContent(xml);
        } else if (this.mActived) {
            if (quotationMarkNeeded) {
                str2 = XcapElement.TRUE_WITH_QUOTE;
            }
            setByAttrName(ATT_ACTIVE, str2);
        } else {
            if (quotationMarkNeeded) {
                str = XcapElement.FALSE_WITH_QUOTE;
            }
            setByAttrName(ATT_ACTIVE, str);
        }
    }

    public void saveNode(boolean active, RuleSet ruleSet) throws XcapException {
        String str = "";
        String useXcapNs = XcapElement.TRUE.equals(System.getProperty("xcap.ns.ss", XcapElement.FALSE)) ? "ss:" : str;
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(useXcapNs);
        sb.append(getNodeName());
        sb.append(" ");
        sb.append(useXcapNs.equals("ss:") ? "xmlns:ss=\"http://uri.etsi.org/ngn/params/xml/simservs/xcap\" " : str);
        sb.append(ATT_ACTIVE);
        sb.append("=\"");
        sb.append(active);
        sb.append("\">");
        if (!(ruleSet == null || ruleSet.getRules().size() == 0)) {
            str = ruleSet.toXmlString();
        }
        sb.append(str);
        sb.append("</");
        sb.append(useXcapNs);
        sb.append(getNodeName());
        sb.append(">");
        String thisXml = sb.toString();
        Log.d(TAG, "saveNode: thisXml=" + thisXml);
        setContent(thisXml);
    }

    @Override // com.mediatek.simservs.xcap.XcapElement
    public void setContent(String xml) throws XcapException {
        try {
            String str = "";
            String useXcapNs = XcapElement.TRUE.equals(System.getProperty("xcap.ns.ss", XcapElement.FALSE)) ? "ss:" : str;
            this.mNodeUri = getNodeUri().toString();
            String str2 = this.mNodeUri;
            String nodeName = getNodeName();
            this.mNodeUri = str2.replace(nodeName, useXcapNs + getNodeName());
            StringBuilder sb = new StringBuilder();
            sb.append(this.mNodeUri);
            if (useXcapNs.equals("ss:")) {
                str = "?xmlns(ss=http://uri.etsi.org/ngn/params/xml/simservs/xcap)";
            }
            sb.append(str);
            this.mNodeUri = sb.toString();
            Log.d(TAG, "setContent: mNodeUri=" + this.mNodeUri);
            saveContent(xml);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
