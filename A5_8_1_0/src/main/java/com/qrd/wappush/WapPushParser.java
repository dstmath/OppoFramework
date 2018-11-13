package com.qrd.wappush;

import com.android.internal.util.HexDump;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.SAXException;

public class WapPushParser {
    private static final int ATTR_START_TAG_BASE = 5;
    private static final String[] ATTR_VALUE_TOKEN;
    private static final int ATTR_VALUE_TOKEN_BASE = 85;
    private static final int OPERATION_PARSE_ATTR = 3;
    private static final int OPERATION_PARSE_CONTENT = 4;
    private static final int OPERATION_PARSE_END = 2;
    private static final int OPERATION_PARSE_HEAD = 1;
    private static final String[] SI_ATTR_START_MAPPING = new String[]{"action=signal-none", "action=signal-low", "action=signal-medium", "action=signal-high", "action=delete", "created=", "href=", "href=http://", "href=http://www.", "href=https://", "href=https://www.", "si_expires=", "si_id=", "class="};
    public static final int SI_TYPE = 1;
    private static final String[] SL_ATTR_START_MAPPING = new String[]{"action=execute-low", "action=execute-high", "action=cache", "href=", "href=http://", "href=http://www.", "href=https://", "href=https://www."};
    public static final int SL_TYPE = 2;
    private static String TAG = "WAP_PUSH_Parser";
    private static HashMap<Integer, String> charsetMapping = new HashMap();
    private boolean bHaveAttr = false;
    private boolean bHaveContent = false;
    private String mAction = null;
    private int mCharSet;
    private String mContent = null;
    private int mCurrentAttrToken;
    private byte[] mEmbeddedStrTbl;
    private String mHyperLink = null;
    private InputStream mInputStream;
    private int mPushType;
    private int mVersion;

    public class CONST_VALUE_TOKEN {
        public static final int END = 1;
        public static final int ENTITY = 2;
        public static final int INDICATION_TAG = 6;
        public static final int OPAQUE = 195;
        public static final int SI_PUB_ID = 5;
        public static final int SI_TAG_CONTENT_ONLY = 69;
        public static final int SL_PUB_ID = 6;
        public static final int SL_TAG = 5;
        public static final int STRING_INLINE = 3;
        public static final int STR_TBL_REF = 131;
        public static final int SWITCH_PAGE = 0;
    }

    static {
        String[] strArr = new String[OPERATION_PARSE_CONTENT];
        strArr[0] = ".com/";
        strArr[1] = ".edu/";
        strArr[2] = ".net/";
        strArr[3] = ".org/";
        ATTR_VALUE_TOKEN = strArr;
        charsetMapping.put(Integer.valueOf(0), "*");
        charsetMapping.put(Integer.valueOf(3), "us-ascii");
        charsetMapping.put(Integer.valueOf(OPERATION_PARSE_CONTENT), "iso-8859-1");
        charsetMapping.put(Integer.valueOf(5), "iso-8859-2");
        charsetMapping.put(Integer.valueOf(6), "iso-8859-3");
        charsetMapping.put(Integer.valueOf(7), "iso-8859-4");
        charsetMapping.put(Integer.valueOf(8), "iso-8859-5");
        charsetMapping.put(Integer.valueOf(9), "iso-8859-6");
        charsetMapping.put(Integer.valueOf(10), "iso-8859-7");
        charsetMapping.put(Integer.valueOf(11), "iso-8859-8");
        charsetMapping.put(Integer.valueOf(12), "iso-8859-9");
        charsetMapping.put(Integer.valueOf(17), "shift_JIS");
        charsetMapping.put(Integer.valueOf(106), "utf-8");
        charsetMapping.put(Integer.valueOf(2026), "big5");
        charsetMapping.put(Integer.valueOf(1000), "iso-10646-ucs-2");
        charsetMapping.put(Integer.valueOf(1015), "utf-16");
    }

    private int Helper_readByte() throws IOException, SAXException {
        int nRet = this.mInputStream.read();
        if (-1 != nRet) {
            return nRet;
        }
        throw new SAXException("Error: Unexpected EOF");
    }

    private int Helper_readInt32() throws SAXException, IOException {
        int nRet = 0;
        int nTemp;
        do {
            nTemp = Helper_readByte();
            nRet = (nTemp & 127) | (nRet << 7);
        } while ((nTemp & 128) != 0);
        return nRet;
    }

    private String Helper_readInlineStr() throws IOException, SAXException {
        int length = 0;
        int nRet = this.mInputStream.read();
        ArrayList<Byte> inlineArraylist = new ArrayList();
        while (nRet != 0) {
            if (nRet == -1) {
                throw new SAXException("Error: Unexpected EOF");
            }
            inlineArraylist.add(Byte.valueOf((byte) (nRet & 255)));
            length++;
            nRet = this.mInputStream.read();
        }
        byte[] destData = new byte[length];
        for (int j = 0; j < length; j++) {
            destData[j] = ((Byte) inlineArraylist.get(j)).byteValue();
        }
        return new String(destData, (String) charsetMapping.get(Integer.valueOf(this.mCharSet)));
    }

    private String Helper_readStringTbl() throws IOException, SAXException {
        int offset = Helper_readInt32();
        int end = offset;
        while (this.mEmbeddedStrTbl[end] != (byte) 0) {
            end++;
        }
        return new String(this.mEmbeddedStrTbl, offset, end - offset, (String) charsetMapping.get(Integer.valueOf(this.mCharSet)));
    }

    private String Helper_ReadOpaque() throws SAXException, IOException {
        int length = Helper_readInt32();
        byte[] opaqueData = new byte[length];
        for (int j = 0; j < length; j++) {
            opaqueData[j] = (byte) Helper_readByte();
        }
        return HexDump.toHexString(opaqueData);
    }

    private void Helper_IgnoreEntity() throws SAXException, IOException {
        int ignore = Helper_readInt32();
    }

    private void Helper_IgnoreSwitchPage() throws SAXException, IOException {
        int ignore = Helper_readByte();
    }

    private int parseHead() throws SAXException, IOException {
        boolean z = true;
        this.mVersion = Helper_readByte();
        int nCheckPID = this.mPushType == 1 ? 5 : 6;
        int nCheckToken = this.mPushType == 1 ? 6 : 5;
        if (Helper_readInt32() != nCheckPID) {
            throw new SAXException("Error: incorrect Public ID in SI Head");
        }
        this.mCharSet = Helper_readInt32();
        int stringTblLen = Helper_readInt32();
        this.mEmbeddedStrTbl = new byte[stringTblLen];
        for (int j = 0; j < stringTblLen; j++) {
            this.mEmbeddedStrTbl[j] = (byte) Helper_readByte();
        }
        if (this.mPushType != 1 || Helper_readByte() == 69) {
            int indication_Tag = Helper_readByte();
            if ((((byte) indication_Tag) & 63) != nCheckToken) {
                throw new SAXException("Error: incorrect TAG in Head");
            }
            boolean z2;
            if ((indication_Tag & 128) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.bHaveAttr = z2;
            if ((indication_Tag & 64) == 0) {
                z = false;
            }
            this.bHaveContent = z;
            if (this.bHaveAttr) {
                return 3;
            }
            if (this.bHaveContent) {
                return OPERATION_PARSE_CONTENT;
            }
            throw new SAXException("Error: TAG have none attr and content");
        }
        throw new SAXException("Error: incorrect SI TAG in SI Head");
    }

    private void storeAttr(int attr, String value) throws SAXException, IOException {
        int length;
        if (this.mPushType == 1) {
            length = SI_ATTR_START_MAPPING.length;
        } else {
            length = SL_ATTR_START_MAPPING.length;
        }
        if (attr > (length - 1) + 5) {
            throw new SAXException("Error: unSupported TAG, discard");
        }
        String[] attrStartTemp;
        if (this.mPushType == 1) {
            attrStartTemp = SI_ATTR_START_MAPPING;
        } else {
            attrStartTemp = SL_ATTR_START_MAPPING;
        }
        String attrFullVal = attrStartTemp[attr - 5] + value;
        if (attrFullVal.startsWith("action=")) {
            this.mAction = attrFullVal.substring(new String("action=").length());
        } else if (attrFullVal.startsWith("href=")) {
            this.mHyperLink = attrFullVal.substring(new String("href=").length());
        }
    }

    private void storeContent(String value) throws SAXException, IOException {
        this.mContent = value;
    }

    private int parseAttr() throws SAXException, IOException {
        StringBuffer attrVal = new StringBuffer();
        if (this.mCurrentAttrToken == 0) {
            this.mCurrentAttrToken = Helper_readByte();
        }
        int token = Helper_readByte();
        while (true) {
            if (token > 128 || token == 2 || token == 1 || token == 3 || token == CONST_VALUE_TOKEN.STR_TBL_REF || token == CONST_VALUE_TOKEN.OPAQUE) {
                switch (token) {
                    case 1:
                        storeAttr(this.mCurrentAttrToken, attrVal.toString());
                        return OPERATION_PARSE_CONTENT;
                    case 2:
                        attrVal.append((char) Helper_readInt32());
                        break;
                    case 3:
                        attrVal.append(Helper_readInlineStr());
                        break;
                    case CONST_VALUE_TOKEN.STR_TBL_REF /*131*/:
                        attrVal.append(Helper_readStringTbl());
                        break;
                    case CONST_VALUE_TOKEN.OPAQUE /*195*/:
                        attrVal.append(Helper_ReadOpaque());
                        break;
                    default:
                        attrVal.append(ATTR_VALUE_TOKEN[token - 85]);
                        break;
                }
                token = Helper_readByte();
            } else {
                storeAttr(this.mCurrentAttrToken, attrVal.toString());
                this.mCurrentAttrToken = token;
                return 3;
            }
        }
    }

    private int parseContent() throws SAXException, IOException {
        if (!this.bHaveContent) {
            return 2;
        }
        StringBuffer attrVal = new StringBuffer();
        int token = Helper_readByte();
        while (true) {
            if (token > 128 || token == 2 || token == 0 || token == 1 || token == 3 || token == CONST_VALUE_TOKEN.STR_TBL_REF || token == CONST_VALUE_TOKEN.OPAQUE) {
                switch (token) {
                    case 0:
                        Helper_IgnoreSwitchPage();
                        break;
                    case 1:
                        storeContent(attrVal.toString());
                        return 2;
                    case 2:
                        Helper_IgnoreEntity();
                        break;
                    case 3:
                        attrVal.append(Helper_readInlineStr());
                        break;
                    case CONST_VALUE_TOKEN.STR_TBL_REF /*131*/:
                        attrVal.append(Helper_readStringTbl());
                        break;
                    case CONST_VALUE_TOKEN.OPAQUE /*195*/:
                        attrVal.append(Helper_ReadOpaque());
                        break;
                    default:
                        attrVal.append(ATTR_VALUE_TOKEN[token - 133]);
                        break;
                }
                token = Helper_readByte();
            } else {
                throw new SAXException("Error: Exception when Parse Content");
            }
        }
    }

    public void parse(InputStream inputstream, int pushType) throws SAXException, IOException {
        this.mInputStream = inputstream;
        this.mPushType = pushType;
        int nextOperation = 1;
        this.mCurrentAttrToken = 0;
        if (this.mPushType == 1 || this.mPushType == 2) {
            while (nextOperation != 2) {
                switch (nextOperation) {
                    case 1:
                        nextOperation = parseHead();
                        break;
                    case 3:
                        nextOperation = parseAttr();
                        break;
                    case OPERATION_PARSE_CONTENT /*4*/:
                        nextOperation = parseContent();
                        break;
                    default:
                        break;
                }
            }
            return;
        }
        throw new SAXException("Error: unsupport Push Type");
    }

    public String getAction() {
        if (this.mAction == null || (this.mAction.isEmpty() ^ 1) == 0) {
            return null;
        }
        return this.mAction;
    }

    public String getContent() {
        if (this.mContent == null || (this.mContent.isEmpty() ^ 1) == 0) {
            return null;
        }
        return this.mContent;
    }

    public String getHyperLink() {
        if (this.mHyperLink == null || (this.mHyperLink.isEmpty() ^ 1) == 0) {
            return null;
        }
        return this.mHyperLink;
    }
}
