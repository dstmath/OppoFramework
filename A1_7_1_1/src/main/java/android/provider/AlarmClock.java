package android.provider;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class AlarmClock {
    public static final String ACTION_DISMISS_ALARM = "android.intent.action.DISMISS_ALARM";
    public static final String ACTION_SET_ALARM = "android.intent.action.SET_ALARM";
    public static final String ACTION_SET_TIMER = "android.intent.action.SET_TIMER";
    public static final String ACTION_SHOW_ALARMS = "android.intent.action.SHOW_ALARMS";
    public static final String ACTION_SNOOZE_ALARM = "android.intent.action.SNOOZE_ALARM";
    public static final String ALARM_SEARCH_MODE_ALL = "android.all";
    public static final String ALARM_SEARCH_MODE_LABEL = "android.label";
    public static final String ALARM_SEARCH_MODE_NEXT = "android.next";
    public static final String ALARM_SEARCH_MODE_TIME = "android.time";
    public static final String EXTRA_ALARM_SEARCH_MODE = "android.intent.extra.alarm.SEARCH_MODE";
    public static final String EXTRA_ALARM_SNOOZE_DURATION = "android.intent.extra.alarm.SNOOZE_DURATION";
    public static final String EXTRA_DAYS = "android.intent.extra.alarm.DAYS";
    public static final String EXTRA_HOUR = "android.intent.extra.alarm.HOUR";
    public static final String EXTRA_IS_PM = "android.intent.extra.alarm.IS_PM";
    public static final String EXTRA_LENGTH = "android.intent.extra.alarm.LENGTH";
    public static final String EXTRA_MESSAGE = "android.intent.extra.alarm.MESSAGE";
    public static final String EXTRA_MINUTES = "android.intent.extra.alarm.MINUTES";
    public static final String EXTRA_RINGTONE = "android.intent.extra.alarm.RINGTONE";
    public static final String EXTRA_SKIP_UI = "android.intent.extra.alarm.SKIP_UI";
    public static final String EXTRA_VIBRATE = "android.intent.extra.alarm.VIBRATE";
    public static final String VALUE_RINGTONE_SILENT = "silent";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.AlarmClock.<init>():void, dex: 
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
    public AlarmClock() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.AlarmClock.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.AlarmClock.<init>():void");
    }
}
