package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.WorkSource;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.LocationManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

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
public class OppoNlpProxy implements LocationProviderInterface {
    private static boolean D = false;
    private static final int FAIL_BIND_CN = 2;
    private static final int FAIL_BIND_GMS = 1;
    private static final int FAIL_UNSET = -1;
    private static final String TAG = "LocationProviderProxy";
    private final String ACTION_LIVE_IN_ABOARD;
    private final String ACTION_LIVE_IN_CHINA;
    private LocationProviderProxy mCnLPProxy;
    private final Context mContext;
    private LocationProviderProxy mCurLPProxy;
    private int mFailCode;
    private GeocoderProxy mGeocodeProvider;
    private LocationProviderProxy mGmsLPProxy;
    private OppoGnssWhiteListProxy mGnssWhiteListProxy;
    private final Handler mHandler;
    private boolean mIsInAboard;
    private PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mTestReceiver;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.location.OppoNlpProxy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.location.OppoNlpProxy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.OppoNlpProxy.<clinit>():void");
    }

    public static LocationProviderInterface createAndBind(Context context, String name, String action, boolean isExpRom, Handler handler) {
        OppoNlpProxy proxy = new OppoNlpProxy(context, name, action, isExpRom, handler);
        if (!proxy.bind()) {
            return null;
        }
        proxy.listenPhoneState();
        return proxy;
    }

    private OppoNlpProxy(Context context, String name, String action, boolean isExpRom, Handler handler) {
        this.mFailCode = -1;
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);
                if (serviceState.getOperatorNumeric() != null) {
                    if (OppoNlpProxy.D) {
                        Log.d(OppoNlpProxy.TAG, "Get the operator Numeric is " + serviceState.getOperatorNumeric());
                    }
                    if (!serviceState.getOperatorNumeric().startsWith("000")) {
                        OppoNlpProxy.this.mIsInAboard = !OppoNlpProxy.this.mGnssWhiteListProxy.inWithoutGmsContryList(serviceState.getOperatorNumeric());
                        OppoNlpProxy.this.reBindNLP();
                    }
                }
            }
        };
        this.ACTION_LIVE_IN_CHINA = "com.oppo.live.in.china";
        this.ACTION_LIVE_IN_ABOARD = "com.oppo.live.in.aboard";
        this.mTestReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OppoNlpProxy.D) {
                    Log.d(OppoNlpProxy.TAG, "Get the test action is : " + action);
                }
                if (action.equals("com.oppo.live.in.china")) {
                    OppoNlpProxy.this.mIsInAboard = false;
                } else if (action.equals("com.oppo.live.in.aboard")) {
                    OppoNlpProxy.this.mIsInAboard = true;
                }
                OppoNlpProxy.this.reBindNLP();
            }
        };
        this.mContext = context;
        this.mHandler = handler;
        this.mGnssWhiteListProxy = OppoGnssWhiteListProxy.getInstall(this.mContext);
        this.mCnLPProxy = LocationProviderProxy.create(context, name, action, 17956943, 17039427, 17236016, this.mHandler);
        this.mGmsLPProxy = LocationProviderProxy.create(this.mContext, name, action, 17956943, 17039424, 17236016, this.mHandler);
        this.mIsInAboard = isExpRom;
        this.mCurLPProxy = this.mIsInAboard ? this.mGmsLPProxy : this.mCnLPProxy;
        D = LocationManagerService.D;
    }

    private boolean bind() {
        return this.mCurLPProxy.bind();
    }

    public String getName() {
        return this.mCurLPProxy.getName();
    }

    public String getConnectedPackageName() {
        return this.mCurLPProxy.getConnectedPackageName();
    }

    public void enable() {
        this.mCurLPProxy.enable();
    }

    public void disable() {
        this.mCurLPProxy.disable();
    }

    public boolean isEnabled() {
        return this.mCurLPProxy.isEnabled();
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        this.mCurLPProxy.setRequest(request, source);
    }

    public ProviderProperties getProperties() {
        return this.mCurLPProxy.getProperties();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mCurLPProxy.dump(fd, pw, args);
    }

    public int getStatus(Bundle extras) {
        return this.mCurLPProxy.getStatus(extras);
    }

    public long getStatusUpdateTime() {
        return this.mCurLPProxy.getStatusUpdateTime();
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        return this.mCurLPProxy.sendExtraCommand(command, extras);
    }

    private void listenPhoneState() {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        if (phone != null) {
            phone.listen(this.mPhoneStateListener, 1);
        }
        if (D) {
            registTestReceiver();
        }
    }

    private void reBindNLP() {
        LocationProviderProxy des = this.mIsInAboard ? this.mGmsLPProxy : this.mCnLPProxy;
        if (des != this.mCurLPProxy) {
            if (this.mIsInAboard && this.mFailCode == 1) {
                if (D) {
                    Log.d(TAG, "not install GMS");
                }
            } else if (this.mIsInAboard || this.mFailCode != 2) {
                switchToNLP(des);
            } else {
                if (D) {
                    Log.d(TAG, "not install CN NLP");
                }
            }
        } else if (D) {
            Log.d(TAG, "no need to switch!");
        }
    }

    private void switchToNLP(LocationProviderProxy des) {
        int i = 1;
        this.mCurLPProxy.dumpRequestStateTo(des);
        if (des.bind()) {
            this.mFailCode = -1;
            LocationProviderProxy last = this.mCurLPProxy;
            this.mCurLPProxy = des;
            LocationProviderProxy.close(last);
            if (this.mGeocodeProvider != null) {
                boolean z;
                GeocoderProxy geocoderProxy = this.mGeocodeProvider;
                if (this.mCurLPProxy != this.mGmsLPProxy) {
                    z = false;
                }
                geocoderProxy.switchToNLP(z);
            }
            if (D) {
                Log.d(TAG, "switch sucess");
                return;
            }
            return;
        }
        if (des != this.mGmsLPProxy) {
            i = 2;
        }
        this.mFailCode = i;
        if (D) {
            Log.d(TAG, "fail:" + this.mFailCode);
        }
    }

    public void setGeocodeProvider(GeocoderProxy geocodeProvider) {
        if (geocodeProvider != null) {
            this.mGeocodeProvider = geocodeProvider;
            this.mGeocodeProvider.switchToNLP(this.mCurLPProxy == this.mGmsLPProxy);
        }
    }

    private void registTestReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.oppo.live.in.china");
        intentFilter.addAction("com.oppo.live.in.aboard");
        this.mContext.registerReceiver(this.mTestReceiver, intentFilter);
    }
}
