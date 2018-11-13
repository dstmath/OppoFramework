package android.net.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Slog;
import com.android.server.LocationManagerService;

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
public class AvoidBadWifiTracker {
    private static String TAG;
    private volatile boolean mAvoidBadWifi;
    BroadcastReceiver mConfigUpdateReceiver;
    private final Context mContext;
    private final Handler mHandler;
    private final Runnable mReevaluateRunnable;
    private final SettingObserver mSettingObserver;

    final /* synthetic */ class -void__init__android_content_Context_ctx_android_os_Handler_handler_java_lang_Runnable_cb_LambdaImpl0 implements Runnable {
        private /* synthetic */ Runnable val$cb;

        public /* synthetic */ -void__init__android_content_Context_ctx_android_os_Handler_handler_java_lang_Runnable_cb_LambdaImpl0(Runnable runnable) {
            this.val$cb = runnable;
        }

        public void run() {
            AvoidBadWifiTracker.this.m1-android_net_util_AvoidBadWifiTracker_lambda$1(this.val$cb);
        }
    }

    private class SettingObserver extends ContentObserver {
        private final Uri mUri = Global.getUriFor("network_avoid_bad_wifi");

        public SettingObserver() {
            super(null);
            AvoidBadWifiTracker.this.mContext.getContentResolver().registerContentObserver(this.mUri, false, this);
        }

        public void onChange(boolean selfChange) {
            Slog.wtf(AvoidBadWifiTracker.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.mUri.equals(uri)) {
                AvoidBadWifiTracker.this.reevaluate();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.util.AvoidBadWifiTracker.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.util.AvoidBadWifiTracker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.util.AvoidBadWifiTracker.<clinit>():void");
    }

    public AvoidBadWifiTracker(Context ctx, Handler handler) {
        this(ctx, handler, null);
    }

    public AvoidBadWifiTracker(Context ctx, Handler handler, Runnable cb) {
        this.mAvoidBadWifi = true;
        this.mConfigUpdateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                AvoidBadWifiTracker.this.reevaluate();
            }
        };
        this.mContext = ctx;
        this.mHandler = handler;
        this.mReevaluateRunnable = new -void__init__android_content_Context_ctx_android_os_Handler_handler_java_lang_Runnable_cb_LambdaImpl0(cb);
        this.mSettingObserver = new SettingObserver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiverAsUser(this.mConfigUpdateReceiver, UserHandle.ALL, intentFilter, null, null);
        update();
    }

    /* renamed from: -android_net_util_AvoidBadWifiTracker_lambda$1 */
    /* synthetic */ void m1-android_net_util_AvoidBadWifiTracker_lambda$1(Runnable cb) {
        if (update() && cb != null) {
            cb.run();
        }
    }

    public void unregisterIntentReceiver() {
        this.mContext.unregisterReceiver(this.mConfigUpdateReceiver);
    }

    public boolean currentValue() {
        return this.mAvoidBadWifi;
    }

    public boolean configRestrictsAvoidBadWifi() {
        return this.mContext.getResources().getInteger(17694737) == 0;
    }

    public boolean shouldNotifyWifiUnvalidated() {
        return configRestrictsAvoidBadWifi() && getSettingsValue() == null;
    }

    public String getSettingsValue() {
        return Global.getString(this.mContext.getContentResolver(), "network_avoid_bad_wifi");
    }

    public void reevaluate() {
        this.mHandler.post(this.mReevaluateRunnable);
    }

    public boolean update() {
        boolean z;
        boolean settingAvoidBadWifi = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(getSettingsValue());
        boolean prev = this.mAvoidBadWifi;
        if (settingAvoidBadWifi || !configRestrictsAvoidBadWifi()) {
            z = true;
        } else {
            z = false;
        }
        this.mAvoidBadWifi = z;
        if (this.mAvoidBadWifi != prev) {
            return true;
        }
        return false;
    }
}
