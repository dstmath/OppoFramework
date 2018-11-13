package com.android.server.coloros;

import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.LocationManagerService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

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
public class OppoTransactCodeGenerator {
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";
    static boolean DEBUG = false;
    private static final String OTA_FILE_PATH = "/cache/recovery/intent";
    private static final String TAG = "ActivityManager";
    private static final String TAG_INTERFACE_DESCRIPTOR = "interface_descriptor";
    private static final String TAG_OPPO_PERMISSION = "oppo_permission";
    private static final String TAG_TRANSACTION = "transaction";
    private static final String XML_FILE_PATH = "/data/system/transcodes.xml";
    private static OppoTransactCodeGenerator sInstance;
    private static final Object sInstanceSync = null;
    private ArrayList<CodeMap> mCodeMapArrayList;

    private class CodeMap {
        private String mClassName = null;
        private String mInterfaceDescriptor = null;
        private Map<String, Integer> mTransactMap = null;

        CodeMap(String className, String interfaceDescriptor) {
            this.mClassName = className;
            this.mInterfaceDescriptor = interfaceDescriptor;
            this.mTransactMap = new HashMap();
        }

        CodeMap addTransact(String transactName) {
            Slog.e(OppoTransactCodeGenerator.TAG, "OppoTransactCodeGenerator addTransact: " + transactName);
            this.mTransactMap.put(transactName, Integer.valueOf(ReflectionUtil.getStaticInt(this.mClassName, transactName, -1)));
            return this;
        }

        String getInterfaceDescriptor() {
            return this.mInterfaceDescriptor;
        }

        Map<String, Integer> getTransactMap() {
            return this.mTransactMap;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.coloros.OppoTransactCodeGenerator.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.coloros.OppoTransactCodeGenerator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.coloros.OppoTransactCodeGenerator.<clinit>():void");
    }

    public static OppoTransactCodeGenerator getInstance() {
        synchronized (sInstanceSync) {
            if (sInstance == null) {
                sInstance = new OppoTransactCodeGenerator();
            }
        }
        return sInstance;
    }

    private void addCodeMap(String className, String interfaceDescriptor, String[] transactionNameArray) {
        CodeMap codeMap = new CodeMap(className, interfaceDescriptor);
        for (String transactionName : transactionNameArray) {
            codeMap.addTransact(transactionName);
        }
        this.mCodeMapArrayList.add(codeMap);
    }

    private OppoTransactCodeGenerator() {
        this.mCodeMapArrayList = new ArrayList();
        String[] transactArrayIActivityManager = new String[1];
        transactArrayIActivityManager[0] = "START_ACTIVITY_TRANSACTION";
        addCodeMap("android.app.IActivityManager", "android.app.IActivityManager", transactArrayIActivityManager);
        String[] transactArrayIContentProvider = new String[4];
        transactArrayIContentProvider[0] = "QUERY_TRANSACTION";
        transactArrayIContentProvider[1] = "INSERT_TRANSACTION";
        transactArrayIContentProvider[2] = "DELETE_TRANSACTION";
        transactArrayIContentProvider[3] = "APPLY_BATCH_TRANSACTION";
        addCodeMap("android.content.IContentProvider", "android.content.IContentProvider", transactArrayIContentProvider);
        String[] transactArrayISms = new String[7];
        transactArrayISms[0] = "TRANSACTION_sendDataForSubscriber";
        transactArrayISms[1] = "TRANSACTION_sendDataForSubscriberWithSelfPermissions";
        transactArrayISms[2] = "TRANSACTION_sendTextForSubscriber";
        transactArrayISms[3] = "TRANSACTION_sendTextForSubscriberWithSelfPermissions";
        transactArrayISms[4] = "TRANSACTION_sendTextForSubscriberWithOptions";
        transactArrayISms[5] = "TRANSACTION_sendMultipartTextForSubscriber";
        transactArrayISms[6] = "TRANSACTION_sendMultipartTextForSubscriberWithOptions";
        addCodeMap("com.android.internal.telephony.ISms$Stub", "com.android.internal.telephony.ISms", transactArrayISms);
        String[] transactArrayIWifiManager = new String[2];
        transactArrayIWifiManager[0] = "TRANSACTION_setWifiEnabled";
        transactArrayIWifiManager[1] = "TRANSACTION_setWifiEnabled";
        addCodeMap("android.net.wifi.IWifiManager$Stub", "android.net.wifi.IWifiManager", transactArrayIWifiManager);
        String[] transactArrayIBluetoothManager = new String[2];
        transactArrayIBluetoothManager[0] = "TRANSACTION_enable";
        transactArrayIBluetoothManager[1] = "TRANSACTION_disable";
        addCodeMap("android.bluetooth.IBluetoothManager$Stub", "android.bluetooth.IBluetoothManager", transactArrayIBluetoothManager);
        String[] transactArrayILocationManager = new String[3];
        transactArrayILocationManager[0] = "TRANSACTION_requestLocationUpdates";
        transactArrayILocationManager[1] = "TRANSACTION_getLastLocation";
        transactArrayILocationManager[2] = "TRANSACTION_registerGnssStatusCallback";
        addCodeMap("android.location.ILocationManager$Stub", "android.location.ILocationManager", transactArrayILocationManager);
        String[] transactArrayITelephony = new String[7];
        transactArrayITelephony[0] = "TRANSACTION_call";
        transactArrayITelephony[1] = "TRANSACTION_endCall";
        transactArrayITelephony[2] = "TRANSACTION_setDataEnabled";
        transactArrayITelephony[3] = "TRANSACTION_getDeviceId";
        transactArrayITelephony[4] = "TRANSACTION_getCellLocation";
        transactArrayITelephony[5] = "TRANSACTION_getNeighboringCellInfo";
        transactArrayITelephony[6] = "TRANSACTION_getAllCellInfo";
        addCodeMap("com.android.internal.telephony.ITelephony$Stub", "com.android.internal.telephony.ITelephony", transactArrayITelephony);
        String[] transactArrayICameraService = new String[1];
        transactArrayICameraService[0] = "TRANSACTION_connect";
        addCodeMap("android.hardware.ICameraService$Stub", "android.hardware.ICameraService", transactArrayICameraService);
        String[] transactArrayINfcAdapter = new String[2];
        transactArrayINfcAdapter[0] = "TRANSACTION_disable";
        transactArrayINfcAdapter[1] = "TRANSACTION_enable";
        addCodeMap("android.nfc.INfcAdapter$Stub", "android.nfc.INfcAdapter", transactArrayINfcAdapter);
        String[] transactArrayIPhoneStateListener = new String[1];
        transactArrayIPhoneStateListener[0] = "TRANSACTION_onCellLocationChanged";
        addCodeMap("com.android.internal.telephony.IPhoneStateListener$Stub", "com.android.internal.telephony.IPhoneStateListener", transactArrayIPhoneStateListener);
        String[] transactArrayISipService = new String[2];
        transactArrayISipService[0] = "TRANSACTION_open";
        transactArrayISipService[1] = "TRANSACTION_open3";
        addCodeMap("android.net.sip.ISipService$Stub", "android.net.sip.ISipService", transactArrayISipService);
    }

    private boolean validate() {
        boolean valid = true;
        try {
            File xmlFile = new File(XML_FILE_PATH);
            if (xmlFile.exists()) {
                FileInputStream inputStream = new FileInputStream(xmlFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream, "utf-8");
                parser.nextTag();
                while (true) {
                    int eventType = parser.next();
                    if (2 == eventType && parser.getName().equalsIgnoreCase(TAG_TRANSACTION)) {
                        String value = parser.getAttributeValue(null, ATTRIBUTE_VALUE);
                        if (value != null) {
                            if (Integer.valueOf(value).intValue() == -1) {
                                valid = false;
                                break;
                            }
                        }
                        valid = false;
                        break;
                    }
                    if (eventType == 1) {
                        break;
                    }
                }
                inputStream.close();
            } else {
                valid = false;
            }
        } catch (Exception e) {
            valid = false;
            Slog.e(TAG, "OppoTransactCodeGenerator validate exception.");
            e.printStackTrace();
        }
        Slog.e(TAG, "OppoTransactCodeGenerator validate return: " + valid);
        return valid;
    }

    public void generate() {
        if (!hasGenerated()) {
            writeCodesToXml();
        } else if (isBootFromOTA()) {
            regenerate();
        }
    }

    public void writeCodesToXml() {
        try {
            File file = new File(XML_FILE_PATH);
            boolean result = file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(outputStream, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_OPPO_PERMISSION);
            for (CodeMap codeMap : this.mCodeMapArrayList) {
                String interfaceDescriptor = codeMap.getInterfaceDescriptor();
                serializer.startTag(null, TAG_INTERFACE_DESCRIPTOR);
                serializer.attribute(null, ATTRIBUTE_NAME, interfaceDescriptor);
                for (Entry<String, Integer> entry : codeMap.getTransactMap().entrySet()) {
                    serializer.startTag(null, TAG_TRANSACTION);
                    serializer.attribute(null, ATTRIBUTE_NAME, (String) entry.getKey());
                    serializer.attribute(null, ATTRIBUTE_VALUE, String.valueOf(entry.getValue()));
                    serializer.endTag(null, TAG_TRANSACTION);
                }
                serializer.endTag(null, TAG_INTERFACE_DESCRIPTOR);
            }
            serializer.endTag(null, TAG_OPPO_PERMISSION);
            serializer.endDocument();
            serializer.flush();
            outputStream.close();
            Runtime.getRuntime().exec("chmod 744 /data/system/transcodes.xml");
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, "OppoTransactCodeGenerator generate exception.");
        }
    }

    private boolean isBootFromOTA() {
        File file = new File(OTA_FILE_PATH);
        String OTA_UPDATE_OK = "0";
        String OTA_UPDATE_FAILED = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON;
        String RECOVER_UPDATE_OK = "2";
        String RECOVER_UPDATE_FAILED = "3";
        if (file.exists()) {
            Slog.i(TAG, "/cache/recovery/intent file is exist!!!");
            String otaResultStr = readOTAUpdateResult(OTA_FILE_PATH);
            if (OTA_UPDATE_OK.equals(otaResultStr)) {
                if (DEBUG) {
                    Slog.i(TAG, "is boot from OTA");
                }
                return true;
            } else if (OTA_UPDATE_FAILED.equals(otaResultStr)) {
                if (DEBUG) {
                    Slog.i(TAG, "not boot from OTA,normal boot");
                }
                return false;
            } else if (RECOVER_UPDATE_OK.equals(otaResultStr)) {
                if (DEBUG) {
                    Slog.i(TAG, "is boot from recover");
                }
                return true;
            } else if (RECOVER_UPDATE_FAILED.equals(otaResultStr)) {
                if (DEBUG) {
                    Slog.i(TAG, "not boot from recover,normal boot");
                }
                return false;
            } else {
                if (DEBUG) {
                    Slog.i(TAG, "OTA update file's date is invalid,normal boot");
                }
                return false;
            }
        }
        if (DEBUG) {
            Slog.i(TAG, "OTA file path is no exist,normal boot");
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0045 A:{SYNTHETIC, Splitter: B:22:0x0045} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readOTAUpdateResult(String fileName) {
        IOException e;
        Throwable th;
        String resultStr = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                resultStr = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e1);
                    }
                }
                reader = reader2;
            } catch (IOException e2) {
                e = e2;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            try {
                Slog.e(TAG, "readOTAUpdateResult failed!!!", e);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e12);
                    }
                }
                return resultStr;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e122);
                    }
                }
                throw th;
            }
        }
        return resultStr;
    }

    private boolean hasGenerated() {
        return new File(XML_FILE_PATH).exists();
    }

    private void regenerate() {
        new File(XML_FILE_PATH).delete();
        writeCodesToXml();
    }
}
