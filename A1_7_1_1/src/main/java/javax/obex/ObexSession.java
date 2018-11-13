package javax.obex;

import android.util.Log;
import java.io.IOException;

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
public class ObexSession {
    private static final String TAG = "ObexSession";
    private static final boolean V = false;
    protected Authenticator mAuthenticator;
    protected byte[] mChallengeDigest;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: javax.obex.ObexSession.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: javax.obex.ObexSession.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.obex.ObexSession.<clinit>():void");
    }

    public boolean handleAuthChall(HeaderSet header) throws IOException {
        if (this.mAuthenticator == null) {
            return false;
        }
        byte[] challenge = ObexHelper.getTagValue((byte) 0, header.mAuthChall);
        byte[] option = ObexHelper.getTagValue((byte) 1, header.mAuthChall);
        byte[] description = ObexHelper.getTagValue((byte) 2, header.mAuthChall);
        String realm = null;
        if (description != null) {
            byte[] realmString = new byte[(description.length - 1)];
            System.arraycopy(description, 1, realmString, 0, realmString.length);
            switch (description[0] & 255) {
                case ObexHelper.OBEX_AUTH_REALM_CHARSET_ASCII /*0*/:
                case 1:
                    try {
                        realm = new String(realmString, "ISO8859_1");
                        break;
                    } catch (Exception e) {
                        throw new IOException("Unsupported Encoding Scheme");
                    }
                case 255:
                    realm = ObexHelper.convertToUnicode(realmString, false);
                    break;
                default:
                    throw new IOException("Unsupported Encoding Scheme");
            }
        }
        boolean isUserIDRequired = false;
        boolean isFullAccess = true;
        if (option != null) {
            if ((option[0] & 1) != 0) {
                isUserIDRequired = true;
            }
            if ((option[0] & 2) != 0) {
                isFullAccess = false;
            }
        }
        header.mAuthChall = null;
        try {
            PasswordAuthentication result = this.mAuthenticator.onAuthenticationChallenge(realm, isUserIDRequired, isFullAccess);
            if (result == null) {
                return false;
            }
            byte[] password = result.getPassword();
            if (password == null) {
                return false;
            }
            byte[] userName = result.getUserName();
            if (userName != null) {
                header.mAuthResp = new byte[(userName.length + 38)];
                header.mAuthResp[36] = (byte) 1;
                header.mAuthResp[37] = (byte) userName.length;
                System.arraycopy(userName, 0, header.mAuthResp, 38, userName.length);
            } else {
                header.mAuthResp = new byte[36];
            }
            byte[] digest = new byte[((challenge.length + password.length) + 1)];
            System.arraycopy(challenge, 0, digest, 0, challenge.length);
            digest[challenge.length] = (byte) 58;
            System.arraycopy(password, 0, digest, challenge.length + 1, password.length);
            header.mAuthResp[0] = (byte) 0;
            header.mAuthResp[1] = (byte) 16;
            System.arraycopy(ObexHelper.computeMd5Hash(digest), 0, header.mAuthResp, 2, 16);
            header.mAuthResp[18] = (byte) 2;
            header.mAuthResp[19] = (byte) 16;
            System.arraycopy(challenge, 0, header.mAuthResp, 20, 16);
            return true;
        } catch (Exception e2) {
            if (V) {
                Log.d(TAG, "Exception occured - returning false", e2);
            }
            return false;
        }
    }

    public boolean handleAuthResp(byte[] authResp) {
        if (this.mAuthenticator == null) {
            return false;
        }
        byte[] correctPassword = this.mAuthenticator.onAuthenticationResponse(ObexHelper.getTagValue((byte) 1, authResp));
        if (correctPassword == null) {
            return false;
        }
        byte[] temp = new byte[(correctPassword.length + 16)];
        System.arraycopy(this.mChallengeDigest, 0, temp, 0, 16);
        System.arraycopy(correctPassword, 0, temp, 16, correctPassword.length);
        byte[] correctResponse = ObexHelper.computeMd5Hash(temp);
        byte[] actualResponse = ObexHelper.getTagValue((byte) 0, authResp);
        for (int i = 0; i < 16; i++) {
            if (correctResponse[i] != actualResponse[i]) {
                return false;
            }
        }
        return true;
    }
}
