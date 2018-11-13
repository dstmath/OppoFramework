package com.android.server.wm;

import android.graphics.Rect;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

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
public class DisplaySettings {
    private static final String TAG = null;
    private final HashMap<String, Entry> mEntries;
    private final AtomicFile mFile;

    public static class Entry {
        public final String name;
        public int overscanBottom;
        public int overscanLeft;
        public int overscanRight;
        public int overscanTop;

        public Entry(String _name) {
            this.name = _name;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.DisplaySettings.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.DisplaySettings.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DisplaySettings.<clinit>():void");
    }

    public DisplaySettings() {
        this.mEntries = new HashMap();
        this.mFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "display_settings.xml"));
    }

    /* JADX WARNING: Missing block: B:3:0x000b, code:
            if (r0 == null) goto L_0x000d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getOverscanLocked(String name, String uniqueId, Rect outRect) {
        Entry entry;
        if (uniqueId != null) {
            entry = (Entry) this.mEntries.get(uniqueId);
        }
        entry = (Entry) this.mEntries.get(name);
        if (entry != null) {
            outRect.left = entry.overscanLeft;
            outRect.top = entry.overscanTop;
            outRect.right = entry.overscanRight;
            outRect.bottom = entry.overscanBottom;
            return;
        }
        outRect.set(0, 0, 0, 0);
    }

    public void setOverscanLocked(String uniqueId, String name, int left, int top, int right, int bottom) {
        if (left == 0 && top == 0 && right == 0 && bottom == 0) {
            this.mEntries.remove(uniqueId);
            this.mEntries.remove(name);
            return;
        }
        Entry entry = (Entry) this.mEntries.get(uniqueId);
        if (entry == null) {
            entry = new Entry(uniqueId);
            this.mEntries.put(uniqueId, entry);
        }
        entry.overscanLeft = left;
        entry.overscanTop = top;
        entry.overscanRight = right;
        entry.overscanBottom = bottom;
    }

    public void readSettingsLocked() {
        try {
            FileInputStream stream = this.mFile.openRead();
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new IllegalStateException("no start tag found");
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                        if (!(type == 3 || type == 4)) {
                            if (parser.getName().equals("display")) {
                                readDisplay(parser);
                            } else {
                                Slog.w(TAG, "Unknown element under <display-settings>: " + parser.getName());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                }
                if (!true) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e) {
                }
            } catch (IllegalStateException e2) {
                Slog.w(TAG, "Failed parsing " + e2);
                if (null == null) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e3) {
                }
            } catch (NullPointerException e4) {
                Slog.w(TAG, "Failed parsing " + e4);
                if (null == null) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e5) {
                }
            } catch (NumberFormatException e6) {
                Slog.w(TAG, "Failed parsing " + e6);
                if (null == null) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e7) {
                }
            } catch (XmlPullParserException e8) {
                Slog.w(TAG, "Failed parsing " + e8);
                if (null == null) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e9) {
                }
            } catch (IOException e10) {
                Slog.w(TAG, "Failed parsing " + e10);
                if (null == null) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e11) {
                }
            } catch (IndexOutOfBoundsException e12) {
                Slog.w(TAG, "Failed parsing " + e12);
                if (null == null) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e13) {
                }
            } catch (Throwable th) {
                if (null == null) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e14) {
                }
                throw th;
            }
        } catch (FileNotFoundException e15) {
            Slog.i(TAG, "No existing display settings " + this.mFile.getBaseFile() + "; starting empty");
        }
    }

    private int getIntAttribute(XmlPullParser parser, String name) {
        int i = 0;
        try {
            String str = parser.getAttributeValue(null, name);
            if (str != null) {
                i = Integer.parseInt(str);
            }
            return i;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void readDisplay(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, "name");
        if (name != null) {
            Entry entry = new Entry(name);
            entry.overscanLeft = getIntAttribute(parser, "overscanLeft");
            entry.overscanTop = getIntAttribute(parser, "overscanTop");
            entry.overscanRight = getIntAttribute(parser, "overscanRight");
            entry.overscanBottom = getIntAttribute(parser, "overscanBottom");
            this.mEntries.put(name, entry);
        }
        XmlUtils.skipCurrentTag(parser);
    }

    public void writeSettingsLocked() {
        try {
            FileOutputStream stream = this.mFile.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "display-settings");
                for (Entry entry : this.mEntries.values()) {
                    out.startTag(null, "display");
                    out.attribute(null, "name", entry.name);
                    if (entry.overscanLeft != 0) {
                        out.attribute(null, "overscanLeft", Integer.toString(entry.overscanLeft));
                    }
                    if (entry.overscanTop != 0) {
                        out.attribute(null, "overscanTop", Integer.toString(entry.overscanTop));
                    }
                    if (entry.overscanRight != 0) {
                        out.attribute(null, "overscanRight", Integer.toString(entry.overscanRight));
                    }
                    if (entry.overscanBottom != 0) {
                        out.attribute(null, "overscanBottom", Integer.toString(entry.overscanBottom));
                    }
                    out.endTag(null, "display");
                }
                out.endTag(null, "display-settings");
                out.endDocument();
                this.mFile.finishWrite(stream);
            } catch (IOException e) {
                Slog.w(TAG, "Failed to write display settings, restoring backup.", e);
                this.mFile.failWrite(stream);
            }
        } catch (IOException e2) {
            Slog.w(TAG, "Failed to write display settings: " + e2);
        }
    }
}
