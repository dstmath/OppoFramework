package org.apache.harmony.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import libcore.io.IoUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    */
class ExpatParser {
    private static final int BUFFER_SIZE = 8096;
    static final String CHARACTER_ENCODING = "UTF-16";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String OUTSIDE_START_ELEMENT = "Attributes can only be used within the scope of startElement().";
    private static final int TIMEOUT = 20000;
    private int attributeCount;
    private long attributePointer;
    private final ExpatAttributes attributes;
    private final String encoding;
    private boolean inStartElement;
    private final Locator locator;
    private long pointer;
    private final String publicId;
    private final String systemId;
    private final ExpatReader xmlReader;

    private static class ClonedAttributes extends ExpatAttributes {
        private static final Attributes EMPTY = null;
        private final int length;
        private final long parserPointer;
        private long pointer;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<init>(long, long, int):void, dex:  in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<init>(long, long, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<init>(long, long, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private ClonedAttributes(long r1, long r3, int r5) {
            /*
            // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<init>(long, long, int):void, dex:  in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<init>(long, long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<init>(long, long, int):void");
        }

        /* synthetic */ ClonedAttributes(long parserPointer, long pointer, int length, ClonedAttributes clonedAttributes) {
            this(parserPointer, pointer, length);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.finalize():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected synchronized void finalize() throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.finalize():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ClonedAttributes.finalize():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getLength():int, dex:  in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getLength():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getLength():int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public int getLength() {
            /*
            // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getLength():int, dex:  in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getLength():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getLength():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getParserPointer():long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public long getParserPointer() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getParserPointer():long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getParserPointer():long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getPointer():long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public long getPointer() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getPointer():long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ClonedAttributes.getPointer():long");
        }
    }

    private class CurrentAttributes extends ExpatAttributes {
        /* synthetic */ CurrentAttributes(ExpatParser this$0, CurrentAttributes currentAttributes) {
            this();
        }

        private CurrentAttributes() {
        }

        public long getParserPointer() {
            return ExpatParser.this.pointer;
        }

        public long getPointer() {
            if (ExpatParser.this.inStartElement) {
                return ExpatParser.this.attributePointer;
            }
            throw new IllegalStateException(ExpatParser.OUTSIDE_START_ELEMENT);
        }

        public int getLength() {
            if (ExpatParser.this.inStartElement) {
                return ExpatParser.this.attributeCount;
            }
            throw new IllegalStateException(ExpatParser.OUTSIDE_START_ELEMENT);
        }
    }

    private static class EntityParser extends ExpatParser {
        private int depth;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: org.apache.harmony.xml.ExpatParser.EntityParser.<init>(java.lang.String, org.apache.harmony.xml.ExpatReader, long, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private EntityParser(java.lang.String r1, org.apache.harmony.xml.ExpatReader r2, long r3, java.lang.String r5, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: org.apache.harmony.xml.ExpatParser.EntityParser.<init>(java.lang.String, org.apache.harmony.xml.ExpatReader, long, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.EntityParser.<init>(java.lang.String, org.apache.harmony.xml.ExpatReader, long, java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.EntityParser.<init>(java.lang.String, org.apache.harmony.xml.ExpatReader, long, java.lang.String, java.lang.String, org.apache.harmony.xml.ExpatParser$EntityParser):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        /* synthetic */ EntityParser(java.lang.String r1, org.apache.harmony.xml.ExpatReader r2, long r3, java.lang.String r5, java.lang.String r6, org.apache.harmony.xml.ExpatParser.EntityParser r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.EntityParser.<init>(java.lang.String, org.apache.harmony.xml.ExpatReader, long, java.lang.String, java.lang.String, org.apache.harmony.xml.ExpatParser$EntityParser):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.EntityParser.<init>(java.lang.String, org.apache.harmony.xml.ExpatReader, long, java.lang.String, java.lang.String, org.apache.harmony.xml.ExpatParser$EntityParser):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: org.apache.harmony.xml.ExpatParser.EntityParser.endElement(java.lang.String, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void endElement(java.lang.String r1, java.lang.String r2, java.lang.String r3) throws org.xml.sax.SAXException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: org.apache.harmony.xml.ExpatParser.EntityParser.endElement(java.lang.String, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.EntityParser.endElement(java.lang.String, java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.EntityParser.finalize():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected synchronized void finalize() throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.EntityParser.finalize():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.EntityParser.finalize():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.harmony.xml.ExpatParser.EntityParser.startElement(java.lang.String, java.lang.String, java.lang.String, long, int):void, dex:  in method: org.apache.harmony.xml.ExpatParser.EntityParser.startElement(java.lang.String, java.lang.String, java.lang.String, long, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.harmony.xml.ExpatParser.EntityParser.startElement(java.lang.String, java.lang.String, java.lang.String, long, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        void startElement(java.lang.String r1, java.lang.String r2, java.lang.String r3, long r4, int r6) throws org.xml.sax.SAXException {
            /*
            // Can't load method instructions: Load method exception: null in method: org.apache.harmony.xml.ExpatParser.EntityParser.startElement(java.lang.String, java.lang.String, java.lang.String, long, int):void, dex:  in method: org.apache.harmony.xml.ExpatParser.EntityParser.startElement(java.lang.String, java.lang.String, java.lang.String, long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.EntityParser.startElement(java.lang.String, java.lang.String, java.lang.String, long, int):void");
        }
    }

    private class ExpatLocator implements Locator {
        final /* synthetic */ ExpatParser this$0;

        /* synthetic */ ExpatLocator(ExpatParser this$0, ExpatLocator expatLocator) {
            this(this$0);
        }

        private ExpatLocator(ExpatParser this$0) {
            this.this$0 = this$0;
        }

        public String getPublicId() {
            return this.this$0.publicId;
        }

        public String getSystemId() {
            return this.this$0.systemId;
        }

        public int getLineNumber() {
            return this.this$0.line();
        }

        public int getColumnNumber() {
            return this.this$0.column();
        }

        public String toString() {
            return "Locator[publicId: " + this.this$0.publicId + ", systemId: " + this.this$0.systemId + ", line: " + getLineNumber() + ", column: " + getColumnNumber() + "]";
        }
    }

    private static class ParseException extends SAXParseException {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.ParseException.<init>(java.lang.String, org.xml.sax.Locator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private ParseException(java.lang.String r1, org.xml.sax.Locator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.ParseException.<init>(java.lang.String, org.xml.sax.Locator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ParseException.<init>(java.lang.String, org.xml.sax.Locator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.ParseException.<init>(java.lang.String, org.xml.sax.Locator, org.apache.harmony.xml.ExpatParser$ParseException):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        /* synthetic */ ParseException(java.lang.String r1, org.xml.sax.Locator r2, org.apache.harmony.xml.ExpatParser.ParseException r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.ParseException.<init>(java.lang.String, org.xml.sax.Locator, org.apache.harmony.xml.ExpatParser$ParseException):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ParseException.<init>(java.lang.String, org.xml.sax.Locator, org.apache.harmony.xml.ExpatParser$ParseException):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.ExpatParser.ParseException.makeMessage(java.lang.String, int, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private static java.lang.String makeMessage(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.harmony.xml.ExpatParser.ParseException.makeMessage(java.lang.String, int, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ParseException.makeMessage(java.lang.String, int, int):java.lang.String");
        }

        private static String makeMessage(String message, Locator locator) {
            return makeMessage(message, locator.getLineNumber(), locator.getColumnNumber());
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.xml.ExpatParser.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.<clinit>():void");
    }

    /* synthetic */ ExpatParser(String encoding, ExpatReader xmlReader, long pointer, String publicId, String systemId, ExpatParser expatParser) {
        this(encoding, xmlReader, pointer, publicId, systemId);
    }

    private native void appendBytes(long j, byte[] bArr, int i, int i2) throws SAXException, ExpatException;

    private native void appendChars(long j, char[] cArr, int i, int i2) throws SAXException, ExpatException;

    private native void appendString(long j, String str, boolean z) throws SAXException, ExpatException;

    private static native long cloneAttributes(long j, int i);

    private static native int column(long j);

    private static native long createEntityParser(long j, String str);

    private native long initialize(String str, boolean z);

    private static native int line(long j);

    private native void release(long j);

    private static native void releaseParser(long j);

    private static native void staticInitialize(String str);

    ExpatParser(String encoding, ExpatReader xmlReader, boolean processNamespaces, String publicId, String systemId) {
        this.inStartElement = false;
        this.attributeCount = -1;
        this.attributePointer = 0;
        this.locator = new ExpatLocator(this, null);
        this.attributes = new CurrentAttributes(this, null);
        this.publicId = publicId;
        this.systemId = systemId;
        this.xmlReader = xmlReader;
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        this.encoding = encoding;
        this.pointer = initialize(this.encoding, processNamespaces);
    }

    private ExpatParser(String encoding, ExpatReader xmlReader, long pointer, String publicId, String systemId) {
        this.inStartElement = false;
        this.attributeCount = -1;
        this.attributePointer = 0;
        this.locator = new ExpatLocator(this, null);
        this.attributes = new CurrentAttributes(this, null);
        this.encoding = encoding;
        this.xmlReader = xmlReader;
        this.pointer = pointer;
        this.systemId = systemId;
        this.publicId = publicId;
    }

    void startElement(String uri, String localName, String qName, long attributePointer, int attributeCount) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            try {
                this.inStartElement = true;
                this.attributePointer = attributePointer;
                this.attributeCount = attributeCount;
                contentHandler.startElement(uri, localName, qName, this.attributes);
            } finally {
                this.inStartElement = false;
                this.attributeCount = -1;
                this.attributePointer = 0;
            }
        }
    }

    void endElement(String uri, String localName, String qName) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endElement(uri, localName, qName);
        }
    }

    void text(char[] text, int length) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.characters(text, 0, length);
        }
    }

    void comment(char[] text, int length) throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.comment(text, 0, length);
        }
    }

    void startCdata() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    void endCdata() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    void startNamespace(String prefix, String uri) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    void endNamespace(String prefix) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    void startDtd(String name, String publicId, String systemId) throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(name, publicId, systemId);
        }
    }

    void endDtd() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }

    void processingInstruction(String target, String data) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.processingInstruction(target, data);
        }
    }

    void notationDecl(String name, String publicId, String systemId) throws SAXException {
        DTDHandler dtdHandler = this.xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.notationDecl(name, publicId, systemId);
        }
    }

    void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        DTDHandler dtdHandler = this.xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }

    void handleExternalEntity(String context, String publicId, String systemId) throws SAXException, IOException {
        EntityResolver entityResolver = this.xmlReader.entityResolver;
        if (entityResolver != null) {
            if (this.systemId != null) {
                try {
                    URI systemUri = new URI(systemId);
                    if (!(systemUri.isAbsolute() || systemUri.isOpaque())) {
                        systemId = new URI(this.systemId).resolve(systemUri).toString();
                    }
                } catch (Exception e) {
                    System.logI("Could not resolve '" + systemId + "' relative to" + " '" + this.systemId + "' at " + this.locator, e);
                }
            }
            InputSource inputSource = entityResolver.resolveEntity(publicId, systemId);
            if (inputSource != null) {
                String encoding = pickEncoding(inputSource);
                long pointer = createEntityParser(this.pointer, context);
                try {
                    parseExternalEntity(new EntityParser(encoding, this.xmlReader, pointer, inputSource.getPublicId(), inputSource.getSystemId(), null), inputSource);
                } finally {
                    releaseParser(pointer);
                }
            }
        }
    }

    private String pickEncoding(InputSource inputSource) {
        if (inputSource.getCharacterStream() != null) {
            return CHARACTER_ENCODING;
        }
        String encoding = inputSource.getEncoding();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
    }

    private void parseExternalEntity(ExpatParser entityParser, InputSource inputSource) throws IOException, SAXException {
        AutoCloseable reader = inputSource.getCharacterStream();
        if (reader != null) {
            try {
                entityParser.append("<externalEntity>");
                entityParser.parseFragment((Reader) reader);
                entityParser.append("</externalEntity>");
            } finally {
                IoUtils.closeQuietly(reader);
            }
        } else {
            AutoCloseable in = inputSource.getByteStream();
            if (in != null) {
                try {
                    entityParser.append("<externalEntity>".getBytes(entityParser.encoding));
                    entityParser.parseFragment((InputStream) in);
                    entityParser.append("</externalEntity>".getBytes(entityParser.encoding));
                } finally {
                    IoUtils.closeQuietly(in);
                }
            } else {
                String systemId = inputSource.getSystemId();
                if (systemId == null) {
                    throw new ParseException("No input specified.", this.locator, null);
                }
                in = openUrl(systemId);
                try {
                    entityParser.append("<externalEntity>".getBytes(entityParser.encoding));
                    entityParser.parseFragment((InputStream) in);
                    entityParser.append("</externalEntity>".getBytes(entityParser.encoding));
                } finally {
                    IoUtils.closeQuietly(in);
                }
            }
        }
    }

    void append(String xml) throws SAXException {
        try {
            appendString(this.pointer, xml, false);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    void append(char[] xml, int offset, int length) throws SAXException {
        try {
            appendChars(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    void append(byte[] xml) throws SAXException {
        append(xml, 0, xml.length);
    }

    void append(byte[] xml, int offset, int length) throws SAXException {
        try {
            appendBytes(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    void parseDocument(InputStream in) throws IOException, SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }

    void parseDocument(Reader in) throws IOException, SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }

    private void parseFragment(Reader in) throws IOException, SAXException {
        char[] buffer = new char[4048];
        while (true) {
            int length = in.read(buffer);
            if (length != -1) {
                try {
                    appendChars(this.pointer, buffer, 0, length);
                } catch (ExpatException e) {
                    throw new ParseException(e.getMessage(), this.locator, null);
                }
            }
            return;
        }
    }

    private void parseFragment(InputStream in) throws IOException, SAXException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int length = in.read(buffer);
            if (length != -1) {
                try {
                    appendBytes(this.pointer, buffer, 0, length);
                } catch (ExpatException e) {
                    throw new ParseException(e.getMessage(), this.locator, null);
                }
            }
            return;
        }
    }

    private void startDocument() throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.setDocumentLocator(this.locator);
            contentHandler.startDocument();
        }
    }

    private void endDocument() throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endDocument();
        }
    }

    void finish() throws SAXException {
        try {
            appendString(this.pointer, "", true);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    protected synchronized void finalize() throws Throwable {
        try {
            if (this.pointer != 0) {
                release(this.pointer);
                this.pointer = 0;
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private int line() {
        return line(this.pointer);
    }

    private int column() {
        return column(this.pointer);
    }

    Attributes cloneAttributes() {
        if (!this.inStartElement) {
            throw new IllegalStateException(OUTSIDE_START_ELEMENT);
        } else if (this.attributeCount == 0) {
            return ClonedAttributes.EMPTY;
        } else {
            return new ClonedAttributes(this.pointer, cloneAttributes(this.attributePointer, this.attributeCount), this.attributeCount, null);
        }
    }

    static InputStream openUrl(String url) throws IOException {
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
            return urlConnection.getInputStream();
        } catch (Exception e) {
            IOException ioe = new IOException("Couldn't open " + url);
            ioe.initCause(e);
            throw ioe;
        }
    }
}
