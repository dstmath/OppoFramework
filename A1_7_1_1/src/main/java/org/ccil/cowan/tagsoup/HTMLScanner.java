package org.ccil.cowan.tagsoup;

import gov.nist.core.Separators;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;
import javax.sip.header.WarningHeader;
import javax.sip.message.Response;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class HTMLScanner implements Scanner, Locator {
    private static final int A_ADUP = 1;
    private static final int A_ADUP_SAVE = 2;
    private static final int A_ADUP_STAGC = 3;
    private static final int A_ANAME = 4;
    private static final int A_ANAME_ADUP = 5;
    private static final int A_ANAME_ADUP_STAGC = 6;
    private static final int A_AVAL = 7;
    private static final int A_AVAL_STAGC = 8;
    private static final int A_CDATA = 9;
    private static final int A_CMNT = 10;
    private static final int A_DECL = 11;
    private static final int A_EMPTYTAG = 12;
    private static final int A_ENTITY = 13;
    private static final int A_ENTITY_START = 14;
    private static final int A_ETAG = 15;
    private static final int A_GI = 16;
    private static final int A_GI_STAGC = 17;
    private static final int A_LT = 18;
    private static final int A_LT_PCDATA = 19;
    private static final int A_MINUS = 20;
    private static final int A_MINUS2 = 21;
    private static final int A_MINUS3 = 22;
    private static final int A_PCDATA = 23;
    private static final int A_PI = 24;
    private static final int A_PITARGET = 25;
    private static final int A_PITARGET_PI = 26;
    private static final int A_SAVE = 27;
    private static final int A_SKIP = 28;
    private static final int A_SP = 29;
    private static final int A_STAGC = 30;
    private static final int A_UNGET = 31;
    private static final int A_UNSAVE_PCDATA = 32;
    private static final int S_ANAME = 1;
    private static final int S_APOS = 2;
    private static final int S_AVAL = 3;
    private static final int S_BB = 4;
    private static final int S_BBC = 5;
    private static final int S_BBCD = 6;
    private static final int S_BBCDA = 7;
    private static final int S_BBCDAT = 8;
    private static final int S_BBCDATA = 9;
    private static final int S_CDATA = 10;
    private static final int S_CDATA2 = 11;
    private static final int S_CDSECT = 12;
    private static final int S_CDSECT1 = 13;
    private static final int S_CDSECT2 = 14;
    private static final int S_COM = 15;
    private static final int S_COM2 = 16;
    private static final int S_COM3 = 17;
    private static final int S_COM4 = 18;
    private static final int S_DECL = 19;
    private static final int S_DECL2 = 20;
    private static final int S_DONE = 21;
    private static final int S_EMPTYTAG = 22;
    private static final int S_ENT = 23;
    private static final int S_EQ = 24;
    private static final int S_ETAG = 25;
    private static final int S_GI = 26;
    private static final int S_NCR = 27;
    private static final int S_PCDATA = 28;
    private static final int S_PI = 29;
    private static final int S_PITARGET = 30;
    private static final int S_QUOT = 31;
    private static final int S_STAGC = 32;
    private static final int S_TAG = 33;
    private static final int S_TAGWS = 34;
    private static final int S_XNCR = 35;
    private static final String[] debug_actionnames = null;
    private static final String[] debug_statenames = null;
    private static int[] statetable;
    static short[][] statetableIndex;
    static int statetableIndexMaxChar;
    private int theCurrentColumn;
    private int theCurrentLine;
    private int theLastColumn;
    private int theLastLine;
    int theNextState;
    char[] theOutputBuffer;
    private String thePublicid;
    int theSize;
    int theState;
    private String theSystemid;
    int[] theWinMap;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.ccil.cowan.tagsoup.HTMLScanner.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.ccil.cowan.tagsoup.HTMLScanner.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.ccil.cowan.tagsoup.HTMLScanner.<clinit>():void");
    }

    public HTMLScanner() {
        this.theOutputBuffer = new char[Response.OK];
        this.theWinMap = new int[]{8364, 65533, 8218, Response.PAYMENT_REQUIRED, 8222, 8230, 8224, 8225, 710, 8240, 352, 8249, 338, 65533, 381, 65533, 65533, 8216, 8217, 8220, 8221, 8226, 8211, 8212, 732, 8482, 353, 8250, 339, 65533, 382, 376};
    }

    private void unread(PushbackReader r, int c) throws IOException {
        if (c != -1) {
            r.unread(c);
        }
    }

    public int getLineNumber() {
        return this.theLastLine;
    }

    public int getColumnNumber() {
        return this.theLastColumn;
    }

    public String getPublicId() {
        return this.thePublicid;
    }

    public String getSystemId() {
        return this.theSystemid;
    }

    public void resetDocumentLocator(String publicid, String systemid) {
        this.thePublicid = publicid;
        this.theSystemid = systemid;
        this.theCurrentColumn = 0;
        this.theCurrentLine = 0;
        this.theLastColumn = 0;
        this.theLastLine = 0;
    }

    public void scan(Reader r0, ScanHandler h) throws IOException, SAXException {
        PushbackReader r;
        this.theState = 28;
        if (r0 instanceof BufferedReader) {
            r = new PushbackReader(r0, 5);
        } else {
            r = new PushbackReader(new BufferedReader(r0), 5);
        }
        int firstChar = r.read();
        if (firstChar != 65279) {
            unread(r, firstChar);
        }
        while (this.theState != 21) {
            int ch = r.read();
            if (ch >= HTMLModels.M_DEF && ch <= 159) {
                ch = this.theWinMap[ch - 128];
            }
            if (ch == 13) {
                ch = r.read();
                if (ch != 10) {
                    unread(r, ch);
                    ch = 10;
                }
            }
            if (ch == 10) {
                this.theCurrentLine++;
                this.theCurrentColumn = 0;
            } else {
                this.theCurrentColumn++;
            }
            if (ch >= 32 || ch == 10 || ch == 9 || ch == -1) {
                int adjCh = (ch < -1 || ch >= statetableIndexMaxChar) ? -2 : ch;
                int statetableRow = statetableIndex[this.theState][adjCh + 2];
                int action = 0;
                if (statetableRow != -1) {
                    action = statetable[statetableRow + 2];
                    this.theNextState = statetable[statetableRow + 3];
                }
                switch (action) {
                    case 0:
                        throw new Error("HTMLScanner can't cope with " + Integer.toString(ch) + " in state " + Integer.toString(this.theState));
                    case 1:
                        h.adup(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 2:
                        h.adup(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        save(ch, h);
                        break;
                    case 3:
                        h.adup(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        h.stagc(this.theOutputBuffer, 0, this.theSize);
                        break;
                    case 4:
                        h.aname(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 5:
                        h.aname(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        h.adup(this.theOutputBuffer, 0, this.theSize);
                        break;
                    case 6:
                        h.aname(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        h.adup(this.theOutputBuffer, 0, this.theSize);
                        h.stagc(this.theOutputBuffer, 0, this.theSize);
                        break;
                    case 7:
                        h.aval(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 8:
                        h.aval(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        h.stagc(this.theOutputBuffer, 0, this.theSize);
                        break;
                    case 9:
                        mark();
                        if (this.theSize > 1) {
                            this.theSize -= 2;
                        }
                        h.pcdata(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case WarningHeader.ATTRIBUTE_NOT_UNDERSTOOD /*10*/:
                        mark();
                        h.cmnt(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 11:
                        h.decl(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 12:
                        mark();
                        if (this.theSize > 0) {
                            h.gi(this.theOutputBuffer, 0, this.theSize);
                        }
                        this.theSize = 0;
                        h.stage(this.theOutputBuffer, 0, this.theSize);
                        break;
                    case 13:
                        mark();
                        char ch1 = (char) ch;
                        if (this.theState != 23 || ch1 != '#') {
                            if (this.theState != 27 || (ch1 != 'x' && ch1 != 'X')) {
                                if (this.theState != 23 || !Character.isLetterOrDigit(ch1)) {
                                    if (this.theState != 27 || !Character.isDigit(ch1)) {
                                        if (this.theState == 35 && (Character.isDigit(ch1) || "abcdefABCDEF".indexOf(ch1) != -1)) {
                                            save(ch, h);
                                            break;
                                        }
                                        h.entity(this.theOutputBuffer, 1, this.theSize - 1);
                                        int ent = h.getEntity();
                                        if (ent != 0) {
                                            this.theSize = 0;
                                            if (ent >= HTMLModels.M_DEF && ent <= 159) {
                                                ent = this.theWinMap[ent - 128];
                                            }
                                            if (ent >= 32) {
                                                if (ent < 55296 || ent > 57343) {
                                                    if (ent <= 65535) {
                                                        save(ent, h);
                                                    } else {
                                                        ent -= HTMLModels.M_OPTION;
                                                        save((ent >> 10) + 55296, h);
                                                        save((ent & 1023) + 56320, h);
                                                    }
                                                }
                                            }
                                            if (ch != 59) {
                                                unread(r, ch);
                                                this.theCurrentColumn--;
                                            }
                                        } else {
                                            unread(r, ch);
                                            this.theCurrentColumn--;
                                        }
                                        this.theNextState = 28;
                                        break;
                                    }
                                    save(ch, h);
                                    break;
                                }
                                save(ch, h);
                                break;
                            }
                            this.theNextState = 35;
                            save(ch, h);
                            break;
                        }
                        this.theNextState = 27;
                        save(ch, h);
                        break;
                        break;
                    case 14:
                        h.pcdata(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        save(ch, h);
                        break;
                    case 15:
                        h.etag(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 16:
                        h.gi(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 17:
                        h.gi(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        h.stagc(this.theOutputBuffer, 0, this.theSize);
                        break;
                    case 18:
                        mark();
                        save(60, h);
                        save(ch, h);
                        break;
                    case 19:
                        mark();
                        save(60, h);
                        h.pcdata(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case WarningHeader.INCOMPATIBLE_BANDWIDTH_UNITS /*20*/:
                        break;
                    case WarningHeader.INCOMPATIBLE_MEDIA_FORMAT /*21*/:
                        save(45, h);
                        save(32, h);
                        break;
                    case WarningHeader.INCOMPATIBLE_NETWORK_ADDRESS_FORMATS /*22*/:
                        save(45, h);
                        save(32, h);
                        break;
                    case WarningHeader.INCOMPATIBLE_NETWORK_PROTOCOL /*23*/:
                        mark();
                        h.pcdata(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case WarningHeader.INCOMPATIBLE_TRANSPORT_PROTOCOL /*24*/:
                        mark();
                        h.pi(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 25:
                        h.pitarget(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 26:
                        h.pitarget(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        h.pi(this.theOutputBuffer, 0, this.theSize);
                        break;
                    case 27:
                        save(ch, h);
                        break;
                    case 28:
                        break;
                    case 29:
                        save(32, h);
                        break;
                    case WarningHeader.INSUFFICIENT_BANDWIDTH /*30*/:
                        h.stagc(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    case 31:
                        unread(r, ch);
                        this.theCurrentColumn--;
                        break;
                    case 32:
                        if (this.theSize > 0) {
                            this.theSize--;
                        }
                        h.pcdata(this.theOutputBuffer, 0, this.theSize);
                        this.theSize = 0;
                        break;
                    default:
                        throw new Error("Can't process state " + action);
                }
                save(45, h);
                save(ch, h);
                this.theState = this.theNextState;
                continue;
            }
        }
        h.eof(this.theOutputBuffer, 0, 0);
    }

    private void mark() {
        this.theLastColumn = this.theCurrentColumn;
        this.theLastLine = this.theCurrentLine;
    }

    public void startCDATA() {
        this.theNextState = 10;
    }

    private void save(int ch, ScanHandler h) throws IOException, SAXException {
        if (this.theSize >= this.theOutputBuffer.length - 20) {
            if (this.theState == 28 || this.theState == 10) {
                h.pcdata(this.theOutputBuffer, 0, this.theSize);
                this.theSize = 0;
            } else {
                char[] newOutputBuffer = new char[(this.theOutputBuffer.length * 2)];
                System.arraycopy(this.theOutputBuffer, 0, newOutputBuffer, 0, this.theSize + 1);
                this.theOutputBuffer = newOutputBuffer;
            }
        }
        char[] cArr = this.theOutputBuffer;
        int i = this.theSize;
        this.theSize = i + 1;
        cArr[i] = (char) ch;
    }

    public static void main(String[] argv) throws IOException, SAXException {
        Scanner s = new HTMLScanner();
        Reader r = new InputStreamReader(System.in, "UTF-8");
        Writer w = new OutputStreamWriter(System.out, "UTF-8");
        s.scan(r, new PYXWriter(w));
        w.close();
    }

    private static String nicechar(int in) {
        if (in == 10) {
            return "\\n";
        }
        if (in < 32) {
            return "0x" + Integer.toHexString(in);
        }
        return Separators.QUOTE + ((char) in) + Separators.QUOTE;
    }
}
