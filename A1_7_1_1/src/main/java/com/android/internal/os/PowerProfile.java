package com.android.internal.os;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

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
public class PowerProfile {
    private static final String ATTR_NAME = "name";
    public static final String BASIC_CURRENT = "basic.current";
    public static final double BASIC_CURRENT_DEFAULT = 3.7d;
    public static final String BT_DELTA = "bt.connect.delta";
    public static final double BT_DELTA_DEFAULT = 1.64d;
    public static final String MULTI_SIM_DELTA = "multi.sim";
    public static final double MULTI_SIM_DELTA_DEFAULT = 5.5d;
    public static final String NO_SIM_DELTA = "no.sim";
    public static final double NO_SIM_DELTA_DEFAULT = 2.5d;
    public static final String POWER_AUDIO = "dsp.audio";
    public static final String POWER_BATTERY_CAPACITY = "battery.capacity";
    @Deprecated
    public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";
    @Deprecated
    public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";
    public static final String POWER_BLUETOOTH_CONTROLLER_IDLE = "bluetooth.controller.idle";
    public static final String POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE = "bluetooth.controller.voltage";
    public static final String POWER_BLUETOOTH_CONTROLLER_RX = "bluetooth.controller.rx";
    public static final String POWER_BLUETOOTH_CONTROLLER_TX = "bluetooth.controller.tx";
    @Deprecated
    public static final String POWER_BLUETOOTH_ON = "bluetooth.on";
    public static final String POWER_CAMERA = "camera.avg";
    @Deprecated
    public static final String POWER_CPU_ACTIVE = "cpu.active";
    public static final String POWER_CPU_AWAKE = "cpu.awake";
    private static final String POWER_CPU_CLUSTER_ACTIVE_PREFIX = "cpu.active.cluster";
    private static final String POWER_CPU_CLUSTER_CORE_COUNT = "cpu.clusters.cores";
    private static final String POWER_CPU_CLUSTER_SPEED_PREFIX = "cpu.speeds.cluster";
    public static final String POWER_CPU_IDLE = "cpu.idle";
    @Deprecated
    public static final String POWER_CPU_SPEEDS = "cpu.speeds";
    public static final String POWER_FLASHLIGHT = "camera.flashlight";
    public static final String POWER_GPS_ON = "gps.on";
    public static final String POWER_MODEM_CONTROLLER_IDLE = "modem.controller.idle";
    public static final String POWER_MODEM_CONTROLLER_OPERATING_VOLTAGE = "modem.controller.voltage";
    public static final String POWER_MODEM_CONTROLLER_RX = "modem.controller.rx";
    public static final String POWER_MODEM_CONTROLLER_TX = "modem.controller.tx";
    public static final String POWER_NONE = "none";
    public static final String POWER_RADIO_ACTIVE = "radio.active";
    public static final String POWER_RADIO_ON = "radio.on";
    public static final String POWER_RADIO_SCANNING = "radio.scanning";
    public static final String POWER_SCREEN_FULL = "screen.full";
    public static final String POWER_SCREEN_ON = "screen.on";
    public static final String POWER_VIDEO = "dsp.video";
    public static final String POWER_WIFI_ACTIVE = "wifi.active";
    public static final String POWER_WIFI_BATCHED_SCAN = "wifi.batchedscan";
    public static final String POWER_WIFI_CONTROLLER_IDLE = "wifi.controller.idle";
    public static final String POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE = "wifi.controller.voltage";
    public static final String POWER_WIFI_CONTROLLER_RX = "wifi.controller.rx";
    public static final String POWER_WIFI_CONTROLLER_TX = "wifi.controller.tx";
    public static final String POWER_WIFI_CONTROLLER_TX_LEVELS = "wifi.controller.tx_levels";
    public static final String POWER_WIFI_ON = "wifi.on";
    public static final String POWER_WIFI_SCAN = "wifi.scan";
    private static final String PROJECT_VERSION_PATH = "/proc/oppoVersion/prjVersion";
    public static final String SINGLE_SIM_DELTA = "single.sim";
    public static final double SINGLE_SIM_DELTA_DEFAULT = 3.5d;
    private static final String TAG = "PowerProfile";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    public static final String WIFI_BT_DELTA = "wifi.bt.delta";
    public static final double WIFI_BT_DELTA_DEFAULT = 7.4d;
    public static final String WIFI_DELTA = "wifi.connect.delta";
    public static final double WIFI_DELTA_DEFAULT = 5.74d;
    static final HashMap<String, Object> sPowerMap = null;
    private CpuClusterKey[] mCpuClusters;

    public static class CpuClusterKey {
        private final int numCpus;
        private final String powerKey;
        private final String timeKey;

        /* synthetic */ CpuClusterKey(String timeKey, String powerKey, int numCpus, CpuClusterKey cpuClusterKey) {
            this(timeKey, powerKey, numCpus);
        }

        private CpuClusterKey(String timeKey, String powerKey, int numCpus) {
            this.timeKey = timeKey;
            this.powerKey = powerKey;
            this.numCpus = numCpus;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.PowerProfile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.PowerProfile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.PowerProfile.<clinit>():void");
    }

    public PowerProfile(Context context) {
        if (sPowerMap.size() == 0) {
            readPowerValuesFromXml(context);
        }
        initCpuClusters();
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00be A:{SYNTHETIC, Splitter: B:32:0x00be} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readStringFromFile(String path) {
        String tempString;
        IOException e;
        Throwable th;
        if (path == null || path.isEmpty()) {
            Log.i(TAG, "readStringFromFile invalid file path");
            return null;
        }
        File file = new File(path);
        BufferedReader reader = null;
        if (new File(path).exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                try {
                    tempString = reader2.readLine();
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e1) {
                            Log.e(TAG, "readStringFromFile io close exception :" + e1.getMessage());
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
                tempString = null;
                try {
                    Log.e(TAG, "readStringFromFile io exception:" + e.getMessage());
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e12) {
                            Log.e(TAG, "readStringFromFile io close exception :" + e12.getMessage());
                        }
                    }
                    return tempString;
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e122) {
                            Log.e(TAG, "readStringFromFile io close exception :" + e122.getMessage());
                        }
                    }
                    throw th;
                }
            }
            return tempString;
        }
        Log.i(TAG, "readStringFromFile file not exists : " + path);
        return null;
    }

    private Object getDeclaredField(Object target, String cls_name, String field_name) {
        Object result = null;
        try {
            Field field = Class.forName(cls_name).getDeclaredField(field_name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "ClassNotFoundException : " + e.getMessage());
            return result;
        } catch (NoSuchFieldException e2) {
            Log.i(TAG, "NoSuchFieldException : " + e2.getMessage());
            return result;
        } catch (IllegalAccessException e3) {
            Log.i(TAG, "IllegalAccessException : " + e3.getMessage());
            return result;
        } catch (SecurityException e4) {
            Log.i(TAG, "SecurityException : " + e4.getMessage());
            return result;
        }
    }

    private void readPowerValuesFromXml(Context context) {
        int id = R.xml.power_profile;
        String projectVersion = readStringFromFile(PROJECT_VERSION_PATH);
        if (!(projectVersion == null || projectVersion.isEmpty())) {
            Object[] objArr = new Object[1];
            objArr[0] = projectVersion;
            String projectPowerProfile = String.format("zzz_power_profile_%s", objArr);
            Log.i(TAG, "target project power profile : " + projectPowerProfile);
            if (!(projectPowerProfile == null || projectPowerProfile.isEmpty())) {
                Object oId = getDeclaredField(null, "com.android.internal.R$xml", projectPowerProfile);
                if (oId != null) {
                    id = ((Integer) oId).intValue();
                }
            }
        }
        Resources resources = context.getResources();
        XmlResourceParser parser = resources.getXml(id);
        boolean parsingArray = false;
        ArrayList<Double> array = new ArrayList();
        Object arrayName = null;
        try {
            XmlUtils.beginDocument(parser, TAG_DEVICE);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    if (parsingArray) {
                        sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
                    }
                    parser.close();
                    int i = 8;
                    int[] configResIds = new int[]{R.integer.config_bluetooth_idle_cur_ma, R.integer.config_bluetooth_rx_cur_ma, R.integer.config_bluetooth_tx_cur_ma, R.integer.config_bluetooth_operating_voltage_mv, R.integer.config_wifi_idle_receive_cur_ma, R.integer.config_wifi_active_rx_cur_ma, R.integer.config_wifi_tx_cur_ma, R.integer.config_wifi_operating_voltage_mv};
                    String[] configResIdKeys = new String[8];
                    configResIdKeys[0] = POWER_BLUETOOTH_CONTROLLER_IDLE;
                    configResIdKeys[1] = POWER_BLUETOOTH_CONTROLLER_RX;
                    configResIdKeys[2] = POWER_BLUETOOTH_CONTROLLER_TX;
                    configResIdKeys[3] = POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE;
                    configResIdKeys[4] = POWER_WIFI_CONTROLLER_IDLE;
                    configResIdKeys[5] = POWER_WIFI_CONTROLLER_RX;
                    configResIdKeys[6] = POWER_WIFI_CONTROLLER_TX;
                    configResIdKeys[7] = POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE;
                    for (int i2 = 0; i2 < configResIds.length; i2++) {
                        String key = configResIdKeys[i2];
                        if (!sPowerMap.containsKey(key) || ((Double) sPowerMap.get(key)).doubleValue() <= 0.0d) {
                            int value = resources.getInteger(configResIds[i2]);
                            if (value > 0) {
                                sPowerMap.put(key, Double.valueOf((double) value));
                            }
                        }
                    }
                    return;
                }
                if (parsingArray) {
                    if (!element.equals(TAG_ARRAYITEM)) {
                        sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
                        parsingArray = false;
                    }
                }
                if (element.equals(TAG_ARRAY)) {
                    parsingArray = true;
                    array.clear();
                    arrayName = parser.getAttributeValue(null, ATTR_NAME);
                } else if (element.equals(TAG_ITEM) || element.equals(TAG_ARRAYITEM)) {
                    Object name = null;
                    if (!parsingArray) {
                        name = parser.getAttributeValue(null, ATTR_NAME);
                    }
                    if (parser.next() == 4) {
                        double value2 = 0.0d;
                        try {
                            value2 = Double.valueOf(parser.getText()).doubleValue();
                        } catch (NumberFormatException e) {
                        }
                        if (element.equals(TAG_ITEM)) {
                            sPowerMap.put(name, Double.valueOf(value2));
                        } else if (parsingArray) {
                            array.add(Double.valueOf(value2));
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
        } catch (XmlPullParserException e2) {
            throw new RuntimeException(e2);
        } catch (IOException e3) {
            throw new RuntimeException(e3);
        } catch (Throwable th) {
            parser.close();
        }
    }

    private void initCpuClusters() {
        Object obj = sPowerMap.get(POWER_CPU_CLUSTER_CORE_COUNT);
        if (obj == null || !(obj instanceof Double[])) {
            this.mCpuClusters = new CpuClusterKey[1];
            this.mCpuClusters[0] = new CpuClusterKey(POWER_CPU_SPEEDS, POWER_CPU_ACTIVE, 1, null);
            return;
        }
        Double[] array = (Double[]) obj;
        this.mCpuClusters = new CpuClusterKey[array.length];
        for (int cluster = 0; cluster < array.length; cluster++) {
            this.mCpuClusters[cluster] = new CpuClusterKey(POWER_CPU_CLUSTER_SPEED_PREFIX + cluster, POWER_CPU_CLUSTER_ACTIVE_PREFIX + cluster, (int) Math.round(array[cluster].doubleValue()), null);
        }
    }

    public int getNumCpuClusters() {
        return this.mCpuClusters.length;
    }

    public int getNumCoresInCpuCluster(int index) {
        return this.mCpuClusters[index].numCpus;
    }

    public int getNumSpeedStepsInCpuCluster(int index) {
        Object value = sPowerMap.get(this.mCpuClusters[index].timeKey);
        if (value == null || !(value instanceof Double[])) {
            return 1;
        }
        return ((Double[]) value).length;
    }

    public int getNumSpeedSteps() {
        Object value = sPowerMap.get(POWER_CPU_SPEEDS);
        if (value == null || !(value instanceof Double[])) {
            return 1;
        }
        return ((Double[]) value).length;
    }

    public double getAveragePowerForCpu(int cluster, int step) {
        if (cluster < 0 || cluster >= this.mCpuClusters.length) {
            return 0.0d;
        }
        return getAveragePower(this.mCpuClusters[cluster].powerKey, step);
    }

    public double getAveragePowerOrDefault(String type, double defaultValue) {
        if (!sPowerMap.containsKey(type)) {
            return defaultValue;
        }
        Object data = sPowerMap.get(type);
        if (data instanceof Double[]) {
            return ((Double[]) data)[0].doubleValue();
        }
        return ((Double) sPowerMap.get(type)).doubleValue();
    }

    public double getAveragePower(String type) {
        return getAveragePowerOrDefault(type, 0.0d);
    }

    public double getAveragePower(String type, int level) {
        if (!sPowerMap.containsKey(type)) {
            return 0.0d;
        }
        Object data = sPowerMap.get(type);
        if (!(data instanceof Double[])) {
            return ((Double) data).doubleValue();
        }
        Double[] values = (Double[]) data;
        if (values.length > level && level >= 0) {
            return values[level].doubleValue();
        }
        if (level < 0 || values.length == 0) {
            return 0.0d;
        }
        return values[values.length - 1].doubleValue();
    }

    public double getBatteryCapacity() {
        return getAveragePower(POWER_BATTERY_CAPACITY);
    }

    public double getAveragePower(String type, double power_default) {
        if (!sPowerMap.containsKey(type)) {
            return power_default;
        }
        Object data = sPowerMap.get(type);
        if (data instanceof Double[]) {
            return ((Double[]) data)[0].doubleValue();
        }
        return ((Double) sPowerMap.get(type)).doubleValue();
    }

    public double getBasicCurrent() {
        return getAveragePower(BASIC_CURRENT, 3.7d);
    }

    public double getNoSimDelta() {
        return getAveragePower(NO_SIM_DELTA, 2.5d);
    }

    public double getSingleSimDelta() {
        return getAveragePower(SINGLE_SIM_DELTA, 3.5d);
    }

    public double getMultiSimDelta() {
        return getAveragePower(MULTI_SIM_DELTA, 5.5d);
    }

    public double getWifiDelta() {
        return getAveragePower(WIFI_DELTA, 5.74d);
    }

    public double getBtDelta() {
        return getAveragePower(BT_DELTA, 1.64d);
    }

    public double getWifiBtDelta() {
        return getAveragePower(WIFI_BT_DELTA, 7.4d);
    }
}
