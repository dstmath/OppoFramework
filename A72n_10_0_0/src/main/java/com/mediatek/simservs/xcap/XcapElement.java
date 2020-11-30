package com.mediatek.simservs.xcap;

import android.content.Context;
import android.net.Network;
import android.util.Log;
import com.android.okhttp.Headers;
import com.android.okhttp.Response;
import com.mediatek.simservs.client.SimServs;
import com.mediatek.xcap.client.XcapClient;
import com.mediatek.xcap.client.XcapConstants;
import com.mediatek.xcap.client.XcapDebugParam;
import com.mediatek.xcap.client.uri.XcapUri;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class XcapElement implements Attributable {
    protected static final String AUTH_XCAP_3GPP_INTENDED = "X-3GPP-Intended-Identity";
    protected static final String COMMON_POLICY_ALIAS = "cp";
    protected static final String COMMON_POLICY_NAMESPACE = "urn:ietf:params:xml:ns:common-policy";
    public static final String FALSE = "false";
    public static final String FALSE_WITH_QUOTE = "\"false\"";
    public static final String TAG = "XcapElement";
    public static final String TRUE = "true";
    public static final String TRUE_WITH_QUOTE = "\"true\"";
    protected static final String XCAP_ALIAS = "ss";
    protected static final String XCAP_NAMESPACE = "http://uri.etsi.org/ngn/params/xml/simservs/xcap";
    protected Context mContext;
    public XcapDebugParam mDebugParams = XcapDebugParam.getInstance();
    protected String mEtag = null;
    public String mIntendedId = null;
    protected boolean mIsSupportEtag = false;
    protected Network mNetwork;
    protected String mNodeUri = null;
    public String mParentUri = null;
    public XcapUri mXcapUri = null;

    /* access modifiers changed from: protected */
    public abstract String getNodeName();

    public XcapElement(XcapUri xcapUri, String parentUri, String intendedId) {
        this.mXcapUri = xcapUri;
        this.mParentUri = parentUri;
        this.mIntendedId = intendedId;
    }

    public void setNetwork(Network network) {
        if (network != null) {
            Log.i(TAG, "XCAP dedicated network netid:" + network);
            this.mNetwork = network;
        }
    }

    public void setEtag(String etag) {
        this.mEtag = etag;
    }

    public String getEtag() {
        return this.mEtag;
    }

    public URI getNodeUri() throws IllegalArgumentException, URISyntaxException {
        return this.mXcapUri.setNodeSelector(new XcapUri.XcapNodeSelector(XcapConstants.ROOT_SIMSERVS).queryByNodeName(this.mParentUri).queryByNodeName(getNodeName())).toURI();
    }

    private URI getAttributeUri(String attribute) throws IllegalArgumentException, URISyntaxException {
        return this.mXcapUri.setNodeSelector(new XcapUri.XcapNodeSelector(XcapConstants.ROOT_SIMSERVS).queryByNodeName(this.mParentUri).queryByNodeName(getNodeName(), attribute)).toURI();
    }

    @Override // com.mediatek.simservs.xcap.Attributable
    public String getByAttrName(String attribute) throws XcapException {
        XcapClient xcapClient;
        SimServs simSrv = SimServs.getInstance();
        if (this.mNetwork != null) {
            xcapClient = new XcapClient(simSrv.getContext(), this.mNetwork, simSrv.getPhoneId());
        } else {
            xcapClient = new XcapClient(simSrv.getContext(), simSrv.getPhoneId());
        }
        String ret = null;
        Headers.Builder headers = new Headers.Builder();
        try {
            if (this.mIntendedId != null) {
                headers.add(AUTH_XCAP_3GPP_INTENDED, "\"" + this.mIntendedId + "\"");
            }
            boolean disableETag = this.mDebugParams.getDisableETag() ? true : SimServs.sETagDisable;
            if (this.mEtag != null && !disableETag) {
                headers.add(XcapConstants.HDR_KEY_IF_NONE_MATCH, this.mEtag);
            }
            Response response = xcapClient.get(getAttributeUri(attribute), headers.build());
            if (response != null) {
                if (response.code() == 200) {
                    String etagValue = response.header(XcapConstants.HDR_KEY_ETAG);
                    if (etagValue != null) {
                        this.mEtag = etagValue;
                    }
                    ret = convertStreamToString(response.body().byteStream());
                } else {
                    throw new XcapException(response.code());
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
            throw new XcapException(e2);
        } catch (URISyntaxException e3) {
            e3.printStackTrace();
        } catch (Throwable th) {
            xcapClient.shutdown();
            throw th;
        }
        xcapClient.shutdown();
        return ret;
    }

    @Override // com.mediatek.simservs.xcap.Attributable
    public void setByAttrName(String attrName, String attrValue) throws XcapException {
        XcapClient xcapClient;
        SimServs simSrv = SimServs.getInstance();
        if (this.mNetwork != null) {
            xcapClient = new XcapClient(simSrv.getContext(), this.mNetwork, simSrv.getPhoneId());
        } else {
            xcapClient = new XcapClient(simSrv.getContext(), simSrv.getPhoneId());
        }
        Headers.Builder headers = new Headers.Builder();
        try {
            if (this.mIntendedId != null) {
                headers.add(AUTH_XCAP_3GPP_INTENDED, "\"" + this.mIntendedId + "\"");
            }
            int errorRetry = 1;
            boolean disableETag = this.mDebugParams.getDisableETag() ? true : SimServs.sETagDisable;
            if (this.mEtag != null && !disableETag) {
                headers.add(XcapConstants.HDR_KEY_IF_MATCH, this.mEtag);
            }
            do {
                errorRetry--;
                Response response = xcapClient.put(getAttributeUri(attrName), "application/xcap-att+xml", attrValue, headers.build());
                if (response != null) {
                    if (response.code() == 200 || response.code() == 201) {
                        String etagValue = response.header(XcapConstants.HDR_KEY_ETAG);
                        if (etagValue != null) {
                            this.mEtag = etagValue;
                        }
                        Log.d("info", "document created in xcap server... etagValue=" + etagValue);
                        continue;
                    } else if (response.code() == 412) {
                        Log.d("info", "412 fail, retry without etag");
                        headers.removeAll(XcapConstants.HDR_KEY_IF_MATCH);
                        errorRetry++;
                        continue;
                    } else {
                        throw new XcapException(response.code());
                    }
                }
            } while (errorRetry > 0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
            throw new XcapException(e2);
        } catch (URISyntaxException e3) {
            e3.printStackTrace();
        } catch (Throwable th) {
            xcapClient.shutdown();
            throw th;
        }
        xcapClient.shutdown();
    }

    @Override // com.mediatek.simservs.xcap.Attributable
    public void deleteByAttrName(String attribute) throws XcapException {
        XcapClient xcapClient;
        SimServs simSrv = SimServs.getInstance();
        if (this.mNetwork != null) {
            xcapClient = new XcapClient(simSrv.getContext(), this.mNetwork, simSrv.getPhoneId());
        } else {
            xcapClient = new XcapClient(simSrv.getContext(), simSrv.getPhoneId());
        }
        Headers.Builder headers = new Headers.Builder();
        try {
            if (this.mIntendedId != null) {
                headers.add(AUTH_XCAP_3GPP_INTENDED, "\"" + this.mIntendedId + "\"");
            }
            int errorRetry = 1;
            boolean disableETag = this.mDebugParams.getDisableETag() ? true : SimServs.sETagDisable;
            if (this.mEtag != null && !disableETag) {
                headers.add(XcapConstants.HDR_KEY_IF_MATCH, this.mEtag);
            }
            do {
                errorRetry--;
                Response response = xcapClient.delete(getAttributeUri(attribute), headers.build());
                if (response != null) {
                    if (response.code() == 200) {
                        String etagValue = response.header(XcapConstants.HDR_KEY_ETAG);
                        if (etagValue != null) {
                            this.mEtag = etagValue;
                        }
                        Log.d("info", "document deleted in xcap server...");
                        continue;
                    } else if (response.code() == 412) {
                        Log.d("info", "412 fail, retry without etag");
                        headers.removeAll(XcapConstants.HDR_KEY_IF_MATCH);
                        errorRetry++;
                        continue;
                    } else {
                        throw new XcapException(response.code());
                    }
                }
            } while (errorRetry > 0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
            throw new XcapException(e2);
        } catch (URISyntaxException e3) {
            e3.printStackTrace();
        } catch (Throwable th) {
            xcapClient.shutdown();
            throw th;
        }
        xcapClient.shutdown();
    }

    public void setContent(String xml) throws XcapException {
        try {
            this.mNodeUri = getNodeUri().toString();
            saveContent(xml);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0153, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0157, code lost:
        if (r1 != null) goto L_0x0159;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0153 A[ExcHandler: URISyntaxException (r0v2 'e' java.net.URISyntaxException A[CUSTOM_DECLARE]), PHI: r1 
      PHI: (r1v1 'xcapClient' com.mediatek.xcap.client.XcapClient) = (r1v0 'xcapClient' com.mediatek.xcap.client.XcapClient), (r1v4 'xcapClient' com.mediatek.xcap.client.XcapClient) binds: [B:1:0x0009, B:42:0x00ed] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0009] */
    public void saveContent(String xml) throws XcapException {
        String putElementMime;
        String xMl;
        XcapClient xcapClient = null;
        Headers.Builder headers = new Headers.Builder();
        try {
            URI uri = new URI(this.mNodeUri);
            SimServs simSrv = SimServs.getInstance();
            if (this.mNetwork != null) {
                xcapClient = new XcapClient(simSrv.getContext(), this.mNetwork, simSrv.getPhoneId());
            } else {
                xcapClient = new XcapClient(simSrv.getContext(), simSrv.getPhoneId());
            }
            if (this.mIntendedId != null) {
                headers.add(AUTH_XCAP_3GPP_INTENDED, "\"" + this.mIntendedId + "\"");
            }
            int errorRetry = 1;
            boolean disableETag = this.mDebugParams.getDisableETag() ? true : SimServs.sETagDisable;
            if (this.mEtag != null && !disableETag) {
                headers.add(XcapConstants.HDR_KEY_IF_MATCH, this.mEtag);
            }
            if (this.mDebugParams.getEnablePredefinedSimservSetting() && !getNodeName().equals("NoReplyTimer") && (xMl = readXmlFromFile("/data/simservs.xml")) != null) {
                xml = xMl;
            }
            if (this.mDebugParams.getXcapPutElementMime() == null || this.mDebugParams.getXcapPutElementMime().isEmpty()) {
                putElementMime = System.getProperty("xcap.putelcontenttype", "application/xcap-el+xml");
            } else {
                putElementMime = this.mDebugParams.getXcapPutElementMime();
            }
            do {
                errorRetry--;
                Response response = xcapClient.put(uri, putElementMime, xml, headers.build());
                if (response != null) {
                    if (response.code() == 200 || response.code() == 201) {
                        String etagValue = response.header(XcapConstants.HDR_KEY_ETAG);
                        if (etagValue != null) {
                            this.mEtag = etagValue;
                        }
                        Log.d("info", "document created in xcap server... etagValue=" + etagValue);
                        continue;
                    } else if (response.code() == 412) {
                        Log.d("info", "412 fail, retry without etag");
                        headers.removeAll(XcapConstants.HDR_KEY_IF_MATCH);
                        errorRetry++;
                        continue;
                    } else if (response.code() == 409) {
                        InputStream is = response.body().byteStream();
                        if (is == null || !TRUE.equals(System.getProperty("xcap.handl409"))) {
                            throw new XcapException(409);
                        }
                        throw new XcapException(409, parse409ErrorMessage("phrase", is));
                    } else {
                        throw new XcapException(response.code());
                    }
                }
            } while (errorRetry > 0);
        } catch (IOException e) {
            e.printStackTrace();
            throw new XcapException(e);
        } catch (URISyntaxException e2) {
        } catch (IOException e3) {
            e3.printStackTrace();
            throw new XcapException(409);
        } catch (Throwable th) {
            if (xcapClient != null) {
                xcapClient.shutdown();
            }
            throw th;
        }
        xcapClient.shutdown();
    }

    public String getContentType() {
        return null;
    }

    public String getUri() {
        StringBuilder pathUri = new StringBuilder();
        String str = this.mParentUri;
        if (str == null) {
            return getNodeName();
        }
        pathUri.append(str);
        pathUri.append("\\");
        pathUri.append(getNodeName());
        return pathUri.toString();
    }

    public XcapElement getParent() {
        return null;
    }

    public String getNodeSelector() {
        return null;
    }

    public String domToXmlText(Element element) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty("omit-xml-declaration", "yes");
        transformer.transform(new DOMSource(element), new StreamResult(buffer));
        return buffer.toString();
    }

    public String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        while (true) {
            String line = r.readLine();
            if (line == null) {
                return total.toString();
            }
            total.append(line);
        }
    }

    /* access modifiers changed from: protected */
    public String readXmlFromFile(String file) {
        String text = "";
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            while (true) {
                String buf = dis.readLine();
                if (buf == null) {
                    break;
                }
                Log.d(TAG, "Read:" + buf);
                text = text + buf;
            }
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    /* access modifiers changed from: protected */
    public String parse409ErrorMessage(String xmlErrorTag, InputStream content) throws XcapException {
        ParserConfigurationException e;
        IOException e2;
        SAXException e3;
        DocumentBuilderFactory factory;
        String[] xmlError = {"xe:constraint-failure", "xe:cannot-insert", "constraint-failure", "cannot-insert"};
        try {
            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            int i = 0;
            factory2.setNamespaceAware(false);
            DocumentBuilder db = factory2.newDocumentBuilder();
            InputSource is = new InputSource();
            try {
                is.setCharacterStream(new StringReader(convertStreamToString(content)));
                Document doc = db.parse(is);
                int length = xmlError.length;
                int i2 = 0;
                while (i2 < length) {
                    NodeList currentNode = doc.getElementsByTagName(xmlError[i2]);
                    if (currentNode.getLength() > 0) {
                        try {
                            String textContent = ((Element) currentNode.item(i)).getAttributeNode(xmlErrorTag).getValue();
                            StringBuilder sb = new StringBuilder();
                            factory = factory2;
                            sb.append("parse409ErrorMessage:[");
                            sb.append(textContent);
                            sb.append("]");
                            Log.d(TAG, sb.toString());
                            if (textContent != null) {
                                return textContent;
                            }
                        } catch (ParserConfigurationException e4) {
                            e = e4;
                            e.printStackTrace();
                            throw new XcapException(500);
                        } catch (IOException e5) {
                            e2 = e5;
                            e2.printStackTrace();
                            throw new XcapException(500);
                        } catch (SAXException e6) {
                            e3 = e6;
                            e3.printStackTrace();
                            throw new XcapException(500);
                        }
                    } else {
                        factory = factory2;
                    }
                    i2++;
                    factory2 = factory;
                    i = 0;
                }
                return null;
            } catch (ParserConfigurationException e7) {
                e = e7;
                e.printStackTrace();
                throw new XcapException(500);
            } catch (IOException e8) {
                e2 = e8;
                e2.printStackTrace();
                throw new XcapException(500);
            } catch (SAXException e9) {
                e3 = e9;
                e3.printStackTrace();
                throw new XcapException(500);
            }
        } catch (ParserConfigurationException e10) {
            e = e10;
            e.printStackTrace();
            throw new XcapException(500);
        } catch (IOException e11) {
            e2 = e11;
            e2.printStackTrace();
            throw new XcapException(500);
        } catch (SAXException e12) {
            e3 = e12;
            e3.printStackTrace();
            throw new XcapException(500);
        }
    }
}
