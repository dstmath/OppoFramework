package com.mediatek.common.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OperatorCustomizationFactoryLoader {
    private static final String CUSTOM_APK_PATH = "/custom/app/";
    private static final String CUSTOM_JAR_PATH = "/custom/operator/libs/";
    private static final boolean LOG_ENABLE = (("eng".equals(getSysProperty("ro.build.type", "eng")) || Log.isLoggable(TAG, 3)) ? true : LOG_ENABLE);
    private static final String PRODUCT_APK_PATH = "/product/app/";
    private static final String PROPERTY_OPERATOR_OPTR = "persist.vendor.operator.optr";
    private static final String PROPERTY_OPERATOR_SEG = "persist.vendor.operator.seg";
    private static final String PROPERTY_OPERATOR_SPEC = "persist.vendor.operator.spec";
    private static final String RSC_PRODUCT_APK_PATH = getSysProperty("ro.product.current_rsc_path", "");
    private static final String RSC_SYSTEM_APK_PATH = getSysProperty("ro.sys.current_rsc_path", "");
    private static final String SYSTEM_APK_PATH = "/system/app/";
    private static final String SYSTEM_JAR_PATH = "/system/operator/libs/";
    private static final String TAG = "OperatorCustomizationFactoryLoader";
    private static final String USP_PACKAGE = getSysProperty("ro.vendor.mtk_carrierexpress_pack", "no");
    private static final Map<OperatorFactoryInfo, Object> sFactoryMap = new HashMap();

    /* access modifiers changed from: private */
    public static class OperatorInfo {
        private String mOperator;
        private String mSegment;
        private String mSpecification;

        public OperatorInfo(String optr, String spec, String seg) {
            this.mOperator = optr;
            this.mSpecification = spec;
            this.mSegment = seg;
        }

        public String toString() {
            return this.mOperator + "_" + this.mSpecification + "_" + this.mSegment;
        }
    }

    public static class OperatorFactoryInfo {
        String mFactoryName;
        String mLibName;
        String mOperator;
        String mPackageName;
        String mSegment;
        String mSpecification;

        public OperatorFactoryInfo(String libName, String factoryName, String packageName, String operator) {
            this(libName, factoryName, packageName, operator, null, null);
        }

        public OperatorFactoryInfo(String libName, String factoryName, String packageName, String operator, String segment) {
            this(libName, factoryName, packageName, operator, segment, null);
        }

        public OperatorFactoryInfo(String libName, String factoryName, String packageName, String operator, String segment, String specification) {
            this.mLibName = libName;
            this.mFactoryName = factoryName;
            this.mPackageName = packageName;
            this.mOperator = operator;
            this.mSegment = segment;
            this.mSpecification = specification;
        }

        public String toString() {
            return "OperatorFactoryInfo(" + this.mOperator + "_" + this.mSpecification + "_" + this.mSegment + ":" + this.mLibName + ":" + this.mFactoryName + ":" + this.mPackageName + ")";
        }
    }

    private static OperatorInfo getActiveOperatorInfo() {
        return new OperatorInfo(getSysProperty(PROPERTY_OPERATOR_OPTR, ""), getSysProperty(PROPERTY_OPERATOR_SPEC, ""), getSysProperty(PROPERTY_OPERATOR_SEG, ""));
    }

    private static OperatorInfo getActiveOperatorInfo(int slot) {
        String[] items;
        OperatorInfo info = null;
        if (slot == -1 || "no".equals(USP_PACKAGE)) {
            info = getActiveOperatorInfo();
        } else {
            String optrProperty = getSysProperty("persist.vendor.mtk_usp_optr_slot_" + slot, "");
            logD("usp optr property is " + optrProperty);
            if (!TextUtils.isEmpty(optrProperty) && (items = optrProperty.split("_")) != null) {
                if (items.length == 1) {
                    info = new OperatorInfo(items[0], "", "");
                } else if (items.length == 3) {
                    info = new OperatorInfo(items[0], items[1], items[2]);
                } else {
                    logE("usp optr property no content or wrong");
                }
            }
        }
        logD("Slot " + slot + "'s OperatorInfo is" + info);
        return info;
    }

    public static Object loadFactory(ClassLoader clazzLoader, List<OperatorFactoryInfo> list) {
        return loadFactory(clazzLoader, list, -1);
    }

    public static Object loadFactory(ClassLoader clazzLoader, List<OperatorFactoryInfo> list, int slot) {
        return loadFactory(clazzLoader, null, list, slot);
    }

    public static Object loadFactory(Context context, List<OperatorFactoryInfo> list) {
        return loadFactory(context, list, -1);
    }

    public static synchronized Object loadFactory(Context context, List<OperatorFactoryInfo> list, int slot) {
        Object loadFactory;
        synchronized (OperatorCustomizationFactoryLoader.class) {
            loadFactory = loadFactory(null, context, list, slot);
        }
        return loadFactory;
    }

    private static synchronized Object loadFactory(ClassLoader clazzLoader, Context context, List<OperatorFactoryInfo> list, int slot) {
        synchronized (OperatorCustomizationFactoryLoader.class) {
            if (list == null) {
                logE("loadFactory failed, because param list is null");
                return null;
            }
            OperatorFactoryInfo factoryInfo = findOpertorFactoryInfo(list, slot);
            if (factoryInfo == null) {
                StringBuilder sb = new StringBuilder();
                for (int index = 0; index < list.size(); index++) {
                    sb.append(index + ": ");
                    sb.append(list.get(index));
                    sb.append("\n");
                }
                logD("can not find operatorFactoryInfo by slot id " + slot + " from \n" + sb.toString());
                return null;
            }
            Object factory = sFactoryMap.get(factoryInfo);
            if (factory != null) {
                logD("return " + factory + " from cache by " + factoryInfo);
                return factory;
            }
            String path = searchTargetPath(factoryInfo.mLibName);
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            Object factory2 = loadFactoryInternal(clazzLoader, context, path, factoryInfo.mFactoryName, factoryInfo.mPackageName);
            if (factory2 != null) {
                sFactoryMap.put(factoryInfo, factory2);
            }
            return factory2;
        }
    }

    private static Object loadFactoryInternal(ClassLoader clazzLoader, Context context, String target, String factoryClassName, String packageName) {
        ClassLoader classLoader;
        logD("load factory " + factoryClassName + " from " + target + " whose packageName is " + packageName + ", context is " + context);
        if (clazzLoader != null) {
            try {
                classLoader = new PathClassLoader(target, clazzLoader);
            } catch (Exception ex) {
                Log.e(TAG, "Exception when initial instance", ex);
                return null;
            }
        } else if (context != null) {
            classLoader = new PathClassLoader(target, context.getClassLoader());
        } else {
            classLoader = new PathClassLoader(target, ClassLoader.getSystemClassLoader().getParent());
        }
        Class<?> clazz = classLoader.loadClass(factoryClassName);
        logD("Load class : " + factoryClassName + " successfully with classLoader:" + classLoader);
        if (!TextUtils.isEmpty(packageName) && context != null) {
            try {
                return clazz.getConstructor(Context.class).newInstance(context.createPackageContext(packageName, 3));
            } catch (NoSuchMethodException e) {
                logD("Exception occurs when using constructor with Context");
            } catch (InvocationTargetException e2) {
                Log.e(TAG, "Exception occurs when execute constructor with Context", e2);
            }
        }
        return clazz.newInstance();
    }

    private static String getSysProperty(String prop, String def) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class, String.class).invoke(null, prop, def);
        } catch (ClassNotFoundException e) {
            logE("Get system properties failed! " + e);
            return "";
        } catch (NoSuchMethodException e2) {
            logE("Get system properties failed! " + e2);
            return "";
        } catch (IllegalAccessException e3) {
            logE("Get system properties failed! " + e3);
            return "";
        } catch (InvocationTargetException e4) {
            logE("Get system properties failed! " + e4);
            return "";
        }
    }

    private static String searchTargetPath(String target) {
        String[] searchFolders;
        if (TextUtils.isEmpty(target)) {
            logE("target is null");
            return null;
        }
        String search = target;
        if (target.endsWith(".apk")) {
            search = target.substring(0, target.length() - 4) + '/' + target;
            searchFolders = !TextUtils.isEmpty(RSC_SYSTEM_APK_PATH) ? new String[]{RSC_SYSTEM_APK_PATH + "/app/", RSC_PRODUCT_APK_PATH + "/app/", SYSTEM_APK_PATH, PRODUCT_APK_PATH, CUSTOM_APK_PATH} : new String[]{SYSTEM_APK_PATH, PRODUCT_APK_PATH, CUSTOM_APK_PATH};
        } else {
            searchFolders = new String[]{SYSTEM_JAR_PATH, CUSTOM_JAR_PATH};
        }
        for (String folder : searchFolders) {
            if (new File(folder + search).exists()) {
                return folder + search;
            }
        }
        logD("can not find target " + target + " in " + Arrays.toString(searchFolders));
        return null;
    }

    private static OperatorFactoryInfo findOpertorFactoryInfo(List<OperatorFactoryInfo> list, int slot) {
        OperatorFactoryInfo factoryInfo;
        OperatorFactoryInfo ret = null;
        OperatorInfo optrInfo = getActiveOperatorInfo(slot);
        if (optrInfo == null || TextUtils.isEmpty(optrInfo.mOperator)) {
            logD("It's OM load or parse failed, because operator is null");
            return null;
        }
        List<OperatorFactoryInfo> unSignedOperatorIdFactoryInfos = new ArrayList<>();
        Iterator<OperatorFactoryInfo> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            factoryInfo = it.next();
            if (optrInfo.mOperator.equals(factoryInfo.mOperator)) {
                if (factoryInfo.mSegment != null) {
                    if (factoryInfo.mSegment.equals(optrInfo.mSegment) && (factoryInfo.mSpecification == null || factoryInfo.mSpecification.equals(optrInfo.mSpecification))) {
                        ret = factoryInfo;
                    }
                } else if (factoryInfo.mSpecification == null || factoryInfo.mSpecification.equals(optrInfo.mSpecification)) {
                    ret = factoryInfo;
                }
            } else if (TextUtils.isEmpty(factoryInfo.mOperator)) {
                unSignedOperatorIdFactoryInfos.add(factoryInfo);
            }
        }
        ret = factoryInfo;
        if (ret != null) {
            return ret;
        }
        for (OperatorFactoryInfo factoryInfo2 : unSignedOperatorIdFactoryInfos) {
            if (!TextUtils.isEmpty(searchTargetPath(factoryInfo2.mLibName))) {
                return factoryInfo2;
            }
        }
        return ret;
    }

    private static void logD(String log) {
        if (LOG_ENABLE) {
            Log.d(TAG, log);
        }
    }

    private static void logE(String log) {
        if (LOG_ENABLE) {
            Log.e(TAG, log);
        }
    }
}
