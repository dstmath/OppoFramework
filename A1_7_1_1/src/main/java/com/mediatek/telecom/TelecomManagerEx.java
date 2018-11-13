package com.mediatek.telecom;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public class TelecomManagerEx {
    public static final String ACTION_DEFAULT_ACCOUNT_CHANGED = "android.telecom.action.DEFAULT_ACCOUNT_CHANGED";
    public static final String ACTION_INCALL_SCREEN_STATE_CHANGED = "com.mediatek.telecom.action.INCALL_SCREEN_STATE_CHANGED";
    public static final String ACTION_PHONE_ACCOUNT_CHANGED = "android.telecom.action.PHONE_ACCOUNT_CHANGED";
    public static final int ASSURED_ECT = 2;
    public static final int BLIND_ECT = 1;
    public static final int BLIND_OR_ASSURED_ECT_FROM_NVRAM = 0;
    public static final String DISCONNECT_REASON_VOLTE_SS_DATA_OFF = "disconnect.reason.volte.ss.data.off";
    public static final String EVENT_CDMA_CALL_ACCEPTED = "com.mediatek.telecom.event.CDMA_CALL_ACCEPTED";
    public static final String EVENT_CONNECTION_LOST = "com.mediatek.telecom.event.CONNECTION_LOST";
    public static final String EVENT_INCOMING_INFO_UPDATED = "com.mediatek.telecom.event.INCOMING_INFO_UPDATED";
    public static final String EVENT_NUMBER_UPDATED = "com.mediatek.telecom.event.NUMBER_UPDATED";
    public static final String EVENT_ON_REMOTE_HOLD = "com.mediatek.telecom.event.ON_REMOTE_HOLD";
    public static final String EVENT_ON_REMOTE_RESUME = "com.mediatek.telecom.event.ON_REMOTE_RESUME";
    public static final String EVENT_OPERATION_FAIL = "com.mediatek.telecom.event.OPERATION_FAIL";
    public static final String EVENT_PHONE_ACCOUNT_CHANGED = "com.mediatek.telecom.event.PHONE_ACCOUNT_CHANGED";
    public static final String EVENT_SS_NOTIFICATION = "com.mediatek.telecom.event.SS_NOTIFICATION";
    public static final String EVENT_UPDATE_VOLTE_EXTRA = "com.mediatek.telecom.event.UPDATE_VOLTE_EXTRA";
    public static final String EVENT_VT_STATUS_UPDATED = "com.mediatek.telecom.event.VT_STATUS_UPDATED";
    public static final String EXTRA_INCALL_SCREEN_SHOW = "com.mediatek.telecom.extra.INCALL_SCREEN_SHOW";
    public static final String EXTRA_SUGGESTED_PHONE_ACCOUNT_HANDLE = "android.telecom.extra.SUGGESTED_PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_VOLTE_CONF_CALL_DIAL = "com.mediatek.volte.ConfCallDial";
    public static final String EXTRA_VOLTE_CONF_CALL_INCOMING = "com.mediatek.volte.conference.invite";
    public static final String EXTRA_VOLTE_CONF_CALL_NUMBERS = "com.mediatek.volte.ConfCallNumbers";
    public static final String EXTRA_VOLTE_MARKED_AS_EMERGENCY = "com.mediatek.volte.isMergency";
    public static final String EXTRA_VOLTE_PAU_FIELD = "com.mediatek.volte.pau";
    public static final String KEY_OF_CHANGED_PHONE_ACCOUNT = "CHANGED_PHONE_ACCOUNT";
    public static final String KEY_OF_FAILED_OPERATION = "FAILED_OPERATION";
    public static final String KEY_OF_SS_NOTIFICATION_CODE = "SS_NOTIFICATION_CODE";
    public static final String KEY_OF_SS_NOTIFICATION_INDEX = "SS_NOTIFICATION_INDEX";
    public static final String KEY_OF_SS_NOTIFICATION_NOTITYPE = "SS_NOTIFICATION_NOTITYPE";
    public static final String KEY_OF_SS_NOTIFICATION_NUMBER = "SS_NOTIFICATION_NUMBER";
    public static final String KEY_OF_SS_NOTIFICATION_TYPE = "SS_NOTIFICATION_TYPE";
    public static final String KEY_OF_UPDATED_INCOMING_INFO_ALPHAID = "UPDATED_INCOMING_INFO_ALPHAID";
    public static final String KEY_OF_UPDATED_INCOMING_INFO_CLI_VALIDITY = "INCOMING_INFO_CLI_VALIDITY";
    public static final String KEY_OF_UPDATED_INCOMING_INFO_TYPE = "UPDATED_INCOMING_INFO_TYPE";
    public static final String KEY_OF_UPDATED_NUMBER = "UPDATED_NUMBER";
    public static final String KEY_OF_UPDATED_VT_STATUS = "UPDATED_VT_STATUS";
    public static final String OPERATION_ANSWER_CALL = "answer";
    public static final String OPERATION_DISCONNECT_CALL = "disconnect";
    public static final String OPERATION_HOLD_CALL = "hold";
    public static final String OPERATION_OUTGOING = "outgoing";
    public static final String OPERATION_REJECT_CALL = "reject";
    public static final String OPERATION_UNHOLD_CALL = "unhold";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.telecom.TelecomManagerEx.<init>():void, dex: 
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
    public TelecomManagerEx() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.telecom.TelecomManagerEx.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telecom.TelecomManagerEx.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createIncomingInfoUpdatedBundle(int, java.lang.String, int):android.os.Bundle, dex: 
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
    public static android.os.Bundle createIncomingInfoUpdatedBundle(int r1, java.lang.String r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createIncomingInfoUpdatedBundle(int, java.lang.String, int):android.os.Bundle, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telecom.TelecomManagerEx.createIncomingInfoUpdatedBundle(int, java.lang.String, int):android.os.Bundle");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createNumberUpdatedBundle(java.lang.String):android.os.Bundle, dex: 
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
    public static android.os.Bundle createNumberUpdatedBundle(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createNumberUpdatedBundle(java.lang.String):android.os.Bundle, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telecom.TelecomManagerEx.createNumberUpdatedBundle(java.lang.String):android.os.Bundle");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createOperationFailBundle(int):android.os.Bundle, dex: 
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
    public static android.os.Bundle createOperationFailBundle(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createOperationFailBundle(int):android.os.Bundle, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telecom.TelecomManagerEx.createOperationFailBundle(int):android.os.Bundle");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createPhoneAccountChangedBundle(android.telecom.PhoneAccountHandle):android.os.Bundle, dex: 
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
    public static android.os.Bundle createPhoneAccountChangedBundle(android.telecom.PhoneAccountHandle r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createPhoneAccountChangedBundle(android.telecom.PhoneAccountHandle):android.os.Bundle, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telecom.TelecomManagerEx.createPhoneAccountChangedBundle(android.telecom.PhoneAccountHandle):android.os.Bundle");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createSsNotificationBundle(int, int, int, java.lang.String, int):android.os.Bundle, dex: 
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
    public static android.os.Bundle createSsNotificationBundle(int r1, int r2, int r3, java.lang.String r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createSsNotificationBundle(int, int, int, java.lang.String, int):android.os.Bundle, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telecom.TelecomManagerEx.createSsNotificationBundle(int, int, int, java.lang.String, int):android.os.Bundle");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createVtStatudUpdatedBundle(int):android.os.Bundle, dex: 
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
    public static android.os.Bundle createVtStatudUpdatedBundle(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telecom.TelecomManagerEx.createVtStatudUpdatedBundle(int):android.os.Bundle, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telecom.TelecomManagerEx.createVtStatudUpdatedBundle(int):android.os.Bundle");
    }
}
