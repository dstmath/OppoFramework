package android.util.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import libcore.io.Streams;

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
public class StrictJarManifest implements Cloneable {
    static final int LINE_LENGTH_LIMIT = 72;
    private static final byte[] LINE_SEPARATOR = null;
    private static final byte[] VALUE_SEPARATOR = null;
    private HashMap<String, Chunk> chunks;
    private final HashMap<String, Attributes> entries;
    private final Attributes mainAttributes;
    private int mainEnd;

    static final class Chunk {
        final int end;
        final int start;

        Chunk(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.util.jar.StrictJarManifest.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.util.jar.StrictJarManifest.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.jar.StrictJarManifest.<clinit>():void");
    }

    public StrictJarManifest() {
        this.entries = new HashMap();
        this.mainAttributes = new Attributes();
    }

    public StrictJarManifest(InputStream is) throws IOException {
        this();
        read(Streams.readFully(is));
    }

    public StrictJarManifest(StrictJarManifest man) {
        this.mainAttributes = (Attributes) man.mainAttributes.clone();
        this.entries = (HashMap) ((HashMap) man.getEntries()).clone();
    }

    StrictJarManifest(byte[] manifestBytes, boolean readChunks) throws IOException {
        this();
        if (readChunks) {
            this.chunks = new HashMap();
        }
        read(manifestBytes);
    }

    public void clear() {
        this.entries.clear();
        this.mainAttributes.clear();
    }

    public Attributes getAttributes(String name) {
        return (Attributes) getEntries().get(name);
    }

    public Map<String, Attributes> getEntries() {
        return this.entries;
    }

    public Attributes getMainAttributes() {
        return this.mainAttributes;
    }

    public Object clone() {
        return new StrictJarManifest(this);
    }

    public void write(OutputStream os) throws IOException {
        write(this, os);
    }

    public void read(InputStream is) throws IOException {
        read(Streams.readFullyNoClose(is));
    }

    private void read(byte[] buf) throws IOException {
        if (buf.length != 0) {
            StrictJarManifestReader im = new StrictJarManifestReader(buf, this.mainAttributes);
            this.mainEnd = im.getEndOfMainSection();
            im.readEntries(this.entries, this.chunks);
        }
    }

    public int hashCode() {
        return this.mainAttributes.hashCode() ^ getEntries().hashCode();
    }

    public boolean equals(Object o) {
        if (o != null && o.getClass() == getClass() && this.mainAttributes.equals(((StrictJarManifest) o).mainAttributes)) {
            return getEntries().equals(((StrictJarManifest) o).getEntries());
        }
        return false;
    }

    Chunk getChunk(String name) {
        return (Chunk) this.chunks.get(name);
    }

    void removeChunks() {
        this.chunks = null;
    }

    int getMainAttributesEnd() {
        return this.mainEnd;
    }

    static void write(StrictJarManifest manifest, OutputStream out) throws IOException {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        ByteBuffer buffer = ByteBuffer.allocate(72);
        Name versionName = Name.MANIFEST_VERSION;
        String version = manifest.mainAttributes.getValue(versionName);
        if (version == null) {
            versionName = Name.SIGNATURE_VERSION;
            version = manifest.mainAttributes.getValue(versionName);
        }
        if (version != null) {
            writeEntry(out, versionName, version, encoder, buffer);
            for (Name name : manifest.mainAttributes.keySet()) {
                if (!name.equals(versionName)) {
                    writeEntry(out, name, manifest.mainAttributes.getValue(name), encoder, buffer);
                }
            }
        }
        out.write(LINE_SEPARATOR);
        for (String key : manifest.getEntries().keySet()) {
            writeEntry(out, Name.NAME, key, encoder, buffer);
            Attributes attributes = (Attributes) manifest.entries.get(key);
            for (Name name2 : attributes.keySet()) {
                writeEntry(out, name2, attributes.getValue(name2), encoder, buffer);
            }
            out.write(LINE_SEPARATOR);
        }
    }

    private static void writeEntry(OutputStream os, Name name, String value, CharsetEncoder encoder, ByteBuffer bBuf) throws IOException {
        String nameString = name.toString();
        os.write(nameString.getBytes(StandardCharsets.US_ASCII));
        os.write(VALUE_SEPARATOR);
        encoder.reset();
        bBuf.clear().limit((72 - nameString.length()) - 2);
        CharBuffer cBuf = CharBuffer.wrap(value);
        while (true) {
            CoderResult r = encoder.encode(cBuf, bBuf, true);
            if (CoderResult.UNDERFLOW == r) {
                r = encoder.flush(bBuf);
            }
            os.write(bBuf.array(), bBuf.arrayOffset(), bBuf.position());
            os.write(LINE_SEPARATOR);
            if (CoderResult.UNDERFLOW != r) {
                os.write(32);
                bBuf.clear().limit(71);
            } else {
                return;
            }
        }
    }
}
