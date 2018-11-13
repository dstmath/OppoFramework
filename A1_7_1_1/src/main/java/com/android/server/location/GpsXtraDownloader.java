package com.android.server.location;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.display.DisplayTransformManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class GpsXtraDownloader {
    private static final int CONNECTION_TIMEOUT_MS = 0;
    private static final boolean DEBUG = false;
    private static final String DEFAULT_USER_AGENT = "Android";
    private static final long MAXIMUM_CONTENT_LENGTH_BYTES = 1000000;
    private static final String TAG = "GpsXtraDownloader";
    private int mNextServerIndex;
    private final String mUserAgent;
    private final String[] mXtraServers;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.location.GpsXtraDownloader.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.location.GpsXtraDownloader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsXtraDownloader.<clinit>():void");
    }

    GpsXtraDownloader(Properties properties) {
        int count = 0;
        String server1 = properties.getProperty("XTRA_SERVER_1");
        String server2 = properties.getProperty("XTRA_SERVER_2");
        String server3 = properties.getProperty("XTRA_SERVER_3");
        if (server1 != null) {
            count = 1;
        }
        if (server2 != null) {
            count++;
        }
        if (server3 != null) {
            count++;
        }
        String agent = properties.getProperty("XTRA_USER_AGENT");
        if (TextUtils.isEmpty(agent)) {
            this.mUserAgent = DEFAULT_USER_AGENT;
        } else {
            this.mUserAgent = agent;
        }
        if (count == 0) {
            Log.e(TAG, "No XTRA servers were specified in the GPS configuration");
            this.mXtraServers = null;
            return;
        }
        int count2;
        this.mXtraServers = new String[count];
        if (server1 != null) {
            this.mXtraServers[0] = server1;
            count2 = 1;
        } else {
            count2 = 0;
        }
        if (server2 != null) {
            count = count2 + 1;
            this.mXtraServers[count2] = server2;
            count2 = count;
        }
        if (server3 != null) {
            count = count2 + 1;
            this.mXtraServers[count2] = server3;
        } else {
            count = count2;
        }
        this.mNextServerIndex = new Random().nextInt(count);
    }

    byte[] downloadXtraData() {
        byte[] result = null;
        int startIndex = this.mNextServerIndex;
        if (this.mXtraServers == null) {
            return null;
        }
        while (result == null) {
            result = doDownload(this.mXtraServers[this.mNextServerIndex]);
            this.mNextServerIndex++;
            if (this.mNextServerIndex == this.mXtraServers.length) {
                this.mNextServerIndex = 0;
            }
            if (this.mNextServerIndex == startIndex) {
                break;
            }
        }
        return result;
    }

    protected byte[] doDownload(String url) {
        Throwable th;
        if (DEBUG) {
            Log.d(TAG, "Downloading XTRA data from " + url);
        }
        HttpURLConnection connection = null;
        try {
            Throwable th2;
            InputStream inputStream;
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Accept", "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
            connection.setRequestProperty("x-wap-profile", "http://www.openmobilealliance.org/tech/profiles/UAPROF/ccppschema-20021212#");
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.connect();
            int statusCode = connection.getResponseCode();
            if (statusCode != DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE) {
                if (DEBUG) {
                    Log.d(TAG, "HTTP error downloading gps XTRA: " + statusCode);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                return null;
            }
            th2 = null;
            inputStream = null;
            try {
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                do {
                    int count = inputStream.read(buffer);
                    if (count != -1) {
                        bytes.write(buffer, 0, count);
                    } else {
                        byte[] toByteArray = bytes.toByteArray();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        if (th2 != null) {
                            throw th2;
                        } else {
                            if (connection != null) {
                                connection.disconnect();
                            }
                            return toByteArray;
                        }
                    }
                } while (((long) bytes.size()) <= MAXIMUM_CONTENT_LENGTH_BYTES);
                if (DEBUG) {
                    Log.d(TAG, "XTRA file too large");
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th4) {
                        th2 = th4;
                    }
                }
                if (th2 != null) {
                    throw th2;
                } else {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    return null;
                }
            } catch (Throwable th22) {
                Throwable th5 = th22;
                th22 = th;
                th = th5;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th6) {
                    if (th22 == null) {
                        th22 = th6;
                    } else if (th22 != th6) {
                        th22.addSuppressed(th6);
                    }
                }
            }
            if (th22 != null) {
                throw th22;
            }
            throw th;
        } catch (IOException ioe) {
            if (DEBUG) {
                Log.d(TAG, "Error downloading gps XTRA: ", ioe);
            }
            if (connection != null) {
                connection.disconnect();
            }
            return null;
        } catch (Throwable th7) {
            if (connection != null) {
                connection.disconnect();
            }
            throw th7;
        }
    }
}
