package com.oppo.autotest.olt.testlib.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ConfigUtils {
    private static HashMap<String, String> sHashMap;

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0071, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0075, code lost:
        if (r1 != null) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x007b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007c, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0080, code lost:
        r0.close();
     */
    public static void parser(String configFilePath) {
        LogUtils.logInfo("parser configFilePath=" + configFilePath);
        sHashMap = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath), "UTF-8"));
            while (true) {
                String line = in.readLine();
                if (line != null) {
                    String[] keyValue = line.split("=");
                    if (keyValue.length == 2) {
                        LogUtils.logInfo("key=" + keyValue[0] + ",val=" + keyValue[1]);
                        sHashMap.put(keyValue[0], keyValue[1]);
                    }
                } else {
                    in.close();
                    in.close();
                    return;
                }
            }
            throw th;
        } catch (Exception e) {
            LogUtils.logError("parser error " + e.getLocalizedMessage());
        }
    }
}
