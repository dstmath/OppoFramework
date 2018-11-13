package com.android.server.am;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import com.android.server.display.OppoBrightUtils;
import java.util.Map;
import java.util.Map.Entry;

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
final class CoreSettingsObserver extends ContentObserver {
    private static final String LOG_TAG = null;
    private static final Map<String, Class<?>> sGlobalSettingToTypeMap = null;
    private static final Map<String, Class<?>> sSecureSettingToTypeMap = null;
    private static final Map<String, Class<?>> sSystemSettingToTypeMap = null;
    private final ActivityManagerService mActivityManagerService;
    private final Bundle mCoreSettings;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.CoreSettingsObserver.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.CoreSettingsObserver.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.CoreSettingsObserver.<clinit>():void");
    }

    public CoreSettingsObserver(ActivityManagerService activityManagerService) {
        super(activityManagerService.mHandler);
        this.mCoreSettings = new Bundle();
        this.mActivityManagerService = activityManagerService;
        beginObserveCoreSettings();
        sendCoreSettings();
    }

    public Bundle getCoreSettingsLocked() {
        return (Bundle) this.mCoreSettings.clone();
    }

    public void onChange(boolean selfChange) {
        synchronized (this.mActivityManagerService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                sendCoreSettings();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void sendCoreSettings() {
        populateSettings(this.mCoreSettings, sSecureSettingToTypeMap);
        populateSettings(this.mCoreSettings, sSystemSettingToTypeMap);
        populateSettings(this.mCoreSettings, sGlobalSettingToTypeMap);
        this.mActivityManagerService.onCoreSettingsChange(this.mCoreSettings);
    }

    private void beginObserveCoreSettings() {
        for (String setting : sSecureSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(setting), false, this);
        }
        for (String setting2 : sSystemSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(System.getUriFor(setting2), false, this);
        }
        for (String setting22 : sGlobalSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Global.getUriFor(setting22), false, this);
        }
    }

    private void populateSettings(Bundle snapshot, Map<String, Class<?>> map) {
        Context context = this.mActivityManagerService.mContext;
        for (Entry<String, Class<?>> entry : map.entrySet()) {
            String setting = (String) entry.getKey();
            Class<?> type = (Class) entry.getValue();
            if (type == String.class) {
                String value;
                if (map == sSecureSettingToTypeMap) {
                    value = Secure.getString(context.getContentResolver(), setting);
                } else if (map == sSystemSettingToTypeMap) {
                    value = System.getString(context.getContentResolver(), setting);
                } else {
                    value = Global.getString(context.getContentResolver(), setting);
                }
                snapshot.putString(setting, value);
            } else if (type == Integer.TYPE) {
                int value2;
                if (map == sSecureSettingToTypeMap) {
                    value2 = Secure.getInt(context.getContentResolver(), setting, 0);
                } else if (map == sSystemSettingToTypeMap) {
                    value2 = System.getInt(context.getContentResolver(), setting, 0);
                } else {
                    value2 = Global.getInt(context.getContentResolver(), setting, 0);
                }
                snapshot.putInt(setting, value2);
            } else if (type == Float.TYPE) {
                float value3;
                if (map == sSecureSettingToTypeMap) {
                    value3 = Secure.getFloat(context.getContentResolver(), setting, OppoBrightUtils.MIN_LUX_LIMITI);
                } else if (map == sSystemSettingToTypeMap) {
                    value3 = System.getFloat(context.getContentResolver(), setting, OppoBrightUtils.MIN_LUX_LIMITI);
                } else {
                    value3 = Global.getFloat(context.getContentResolver(), setting, OppoBrightUtils.MIN_LUX_LIMITI);
                }
                snapshot.putFloat(setting, value3);
            } else if (type == Long.TYPE) {
                long value4;
                if (map == sSecureSettingToTypeMap) {
                    value4 = Secure.getLong(context.getContentResolver(), setting, 0);
                } else if (map == sSystemSettingToTypeMap) {
                    value4 = System.getLong(context.getContentResolver(), setting, 0);
                } else {
                    value4 = Global.getLong(context.getContentResolver(), setting, 0);
                }
                snapshot.putLong(setting, value4);
            }
        }
    }
}
