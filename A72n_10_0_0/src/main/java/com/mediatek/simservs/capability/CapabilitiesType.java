package com.mediatek.simservs.capability;

import com.mediatek.simservs.xcap.InquireType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.uri.XcapUri;
import java.io.IOException;
import java.io.StringReader;
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

public abstract class CapabilitiesType extends InquireType {
    static final String ATT_ACTIVE = "active";
    public boolean mActived = false;

    public abstract void initServiceInstance(Document document);

    public CapabilitiesType(XcapUri xcapUri, String parentUri, String intendedId) throws XcapException, ParserConfigurationException {
        super(xcapUri, parentUri, intendedId);
        loadConfiguration();
    }

    private void loadConfiguration() throws XcapException, ParserConfigurationException {
        String xmlContent = getContent();
        if (xmlContent != null) {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlContent));
            try {
                Document doc = db.parse(is);
                NodeList currentNode = doc.getElementsByTagName(getNodeName());
                if (currentNode.getLength() > 0) {
                    NamedNodeMap map = ((Element) currentNode.item(0)).getAttributes();
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
                }
                initServiceInstance(doc);
            } catch (SAXException e) {
                e.printStackTrace();
                throw new XcapException(500);
            } catch (IOException e2) {
                e2.printStackTrace();
                throw new XcapException(500);
            }
        }
    }

    public boolean isActive() throws XcapException {
        if (getByAttrName(ATT_ACTIVE) == null) {
            return true;
        }
        return getByAttrName(ATT_ACTIVE).equals(XcapElement.TRUE);
    }

    public void setActive(boolean active) throws XcapException {
        if (active) {
            setByAttrName(ATT_ACTIVE, XcapElement.TRUE);
        } else {
            setByAttrName(ATT_ACTIVE, XcapElement.FALSE);
        }
    }
}
