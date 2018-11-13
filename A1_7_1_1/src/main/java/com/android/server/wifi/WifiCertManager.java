package com.android.server.wifi;

import android.app.admin.IDevicePolicyManager.Stub;
import android.content.Context;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.DelayedDiskWrite.Writer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
public class WifiCertManager {
    private static final String CONFIG_FILE = null;
    private static final String SEP = "\n";
    private static final String TAG = "WifiCertManager";
    private final Set<String> mAffiliatedUserOnlyCerts;
    private final String mConfigFile;
    private final Context mContext;
    private final DelayedDiskWrite mWriter;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiCertManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiCertManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiCertManager.<clinit>():void");
    }

    WifiCertManager(Context context) {
        this(context, CONFIG_FILE);
    }

    WifiCertManager(Context context, String configFile) {
        this.mAffiliatedUserOnlyCerts = new HashSet();
        this.mWriter = new DelayedDiskWrite();
        this.mContext = context;
        this.mConfigFile = configFile;
        byte[] bytes = readConfigFile();
        if (bytes != null) {
            String[] keys = new String(bytes, StandardCharsets.UTF_8).split(SEP);
            for (String key : keys) {
                this.mAffiliatedUserOnlyCerts.add(key);
            }
            if (this.mAffiliatedUserOnlyCerts.retainAll(Arrays.asList(listClientCertsForAllUsers()))) {
                writeConfig();
            }
        }
    }

    public void hideCertFromUnaffiliatedUsers(String key) {
        if (this.mAffiliatedUserOnlyCerts.add("USRPKEY_" + key)) {
            writeConfig();
        }
    }

    public String[] listClientCertsForCurrentUser() {
        HashSet<String> results = new HashSet();
        String[] keys = listClientCertsForAllUsers();
        if (isAffiliatedUser()) {
            return keys;
        }
        for (String key : keys) {
            if (!this.mAffiliatedUserOnlyCerts.contains(key)) {
                results.add(key);
            }
        }
        return (String[]) results.toArray(new String[results.size()]);
    }

    private void writeConfig() {
        writeConfigFile(TextUtils.join(SEP, (String[]) this.mAffiliatedUserOnlyCerts.toArray(new String[this.mAffiliatedUserOnlyCerts.size()])).getBytes(StandardCharsets.UTF_8));
    }

    protected byte[] readConfigFile() {
        byte[] bytes = null;
        try {
            long fileSize;
            File file = new File(this.mConfigFile);
            if (file.exists()) {
                fileSize = file.length();
            } else {
                fileSize = 0;
            }
            if (fileSize == 0 || fileSize >= 2147483647L) {
                return null;
            }
            bytes = new byte[((int) file.length())];
            new DataInputStream(new FileInputStream(file)).readFully(bytes);
            return bytes;
        } catch (IOException e) {
            Log.e(TAG, "readConfigFile: failed to read " + e, e);
        }
    }

    protected void writeConfigFile(final byte[] payload) {
        byte[] data = payload;
        this.mWriter.write(this.mConfigFile, new Writer() {
            public void onWriteCalled(DataOutputStream out) throws IOException {
                out.write(payload, 0, payload.length);
            }
        });
    }

    protected String[] listClientCertsForAllUsers() {
        return KeyStore.getInstance().list("USRPKEY_", UserHandle.myUserId());
    }

    protected boolean isAffiliatedUser() {
        boolean result = false;
        try {
            return Stub.asInterface(ServiceManager.getService("device_policy")).isAffiliatedUser();
        } catch (Exception e) {
            Log.e(TAG, "failed to check user affiliation", e);
            return result;
        }
    }
}
