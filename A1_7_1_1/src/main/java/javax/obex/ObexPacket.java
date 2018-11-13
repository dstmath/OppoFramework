package javax.obex;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

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
public class ObexPacket {
    private static final String TAG = "ObexPacket";
    private static final boolean V = false;
    public int mHeaderId;
    public int mLength;
    public byte[] mPayload;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: javax.obex.ObexPacket.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: javax.obex.ObexPacket.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.obex.ObexPacket.<clinit>():void");
    }

    private ObexPacket(int headerId, int length) {
        this.mPayload = null;
        this.mHeaderId = headerId;
        this.mLength = length;
    }

    public static ObexPacket read(InputStream is) throws IOException {
        return read(is.read(), is);
    }

    public static ObexPacket read(int headerId, InputStream is) throws IOException {
        int length = (is.read() << 8) + is.read();
        if (V) {
            Log.d(TAG, "read packet length = " + length);
        }
        ObexPacket newPacket = new ObexPacket(headerId, length);
        byte[] temp = null;
        if (length > 3) {
            temp = new byte[(length - 3)];
            int bytesReceived = is.read(temp);
            while (bytesReceived != temp.length) {
                bytesReceived += is.read(temp, bytesReceived, temp.length - bytesReceived);
            }
        }
        newPacket.mPayload = temp;
        return newPacket;
    }
}
