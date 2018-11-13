package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.BidiFormatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import com.android.server.input.InputManagerService;

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
public final class AppErrorDialog extends BaseErrorDialog implements OnClickListener {
    static int ALREADY_SHOWING = 0;
    static int BACKGROUND_USER = 0;
    static final int CANCEL = 7;
    static int CANT_SHOW = 0;
    static final long DISMISS_TIMEOUT = 300000;
    static final int FORCE_QUIT = 1;
    static final int FORCE_QUIT_AND_REPORT = 2;
    static final int MUTE = 5;
    static final int RESTART = 3;
    static final int TIMEOUT = 6;
    private final Handler mHandler;
    private final boolean mIsRestartable;
    private CharSequence mName;
    private final ProcessRecord mProc;
    private final BroadcastReceiver mReceiver;
    private final boolean mRepeating;
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    public static class Data {
        public String exceptionMsg;
        boolean isRestartableForService;
        ProcessRecord proc;
        boolean repeating;
        AppErrorResult result;
        TaskRecord task;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.AppErrorDialog.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.AppErrorDialog.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.AppErrorDialog.<clinit>():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x00e1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AppErrorDialog(Context context, ActivityManagerService service, Data data) {
        boolean z;
        int i;
        Object[] objArr;
        LayoutParams attrs;
        super(context);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                int result = msg.what;
                synchronized (AppErrorDialog.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (AppErrorDialog.this.mProc != null && AppErrorDialog.this.mProc.crashDialog == AppErrorDialog.this) {
                            AppErrorDialog.this.mProc.crashDialog = null;
                        }
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
                AppErrorDialog.this.mResult.set(result);
                removeMessages(6);
                AppErrorDialog.this.dismiss();
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                    AppErrorDialog.this.cancel();
                }
            }
        };
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = data.proc;
        this.mResult = data.result;
        this.mRepeating = data.repeating;
        if (data.task == null) {
            z = data.isRestartableForService;
        } else {
            z = true;
        }
        this.mIsRestartable = z;
        BidiFormatter bidi = BidiFormatter.getInstance();
        if (this.mProc.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            this.mName = applicationLabel;
            if (applicationLabel != null) {
                if (this.mRepeating) {
                    i = 17040283;
                } else {
                    i = 17040281;
                }
                objArr = new Object[2];
                objArr[0] = bidi.unicodeWrap(this.mName.toString());
                objArr[1] = bidi.unicodeWrap(this.mProc.info.processName);
                setTitle(res.getString(i, objArr));
                setCancelable(true);
                setCancelMessage(this.mHandler.obtainMessage(7));
                setButton(-1, res.getText(17039370), this.mHandler.obtainMessage(1));
                if (this.mProc.errorReportReceiver != null) {
                    setButton(-2, res.getText(17040297), this.mHandler.obtainMessage(2));
                }
                attrs = getWindow().getAttributes();
                attrs.setTitle("Application Error: " + this.mProc.info.processName);
                attrs.privateFlags |= InputManagerService.BTN_MOUSE;
                getWindow().setAttributes(attrs);
                if (this.mProc.persistent) {
                    getWindow().setType(2010);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 300000);
            }
        }
        this.mName = this.mProc.processName;
        if (this.mRepeating) {
            i = 17040284;
        } else {
            i = 17040282;
        }
        objArr = new Object[1];
        objArr[0] = bidi.unicodeWrap(this.mName.toString());
        setTitle(res.getString(i, objArr));
        setCancelable(true);
        setCancelMessage(this.mHandler.obtainMessage(7));
        setButton(-1, res.getText(17039370), this.mHandler.obtainMessage(1));
        if (this.mProc.errorReportReceiver != null) {
        }
        attrs = getWindow().getAttributes();
        attrs.setTitle("Application Error: " + this.mProc.info.processName);
        attrs.privateFlags |= InputManagerService.BTN_MOUSE;
        getWindow().setAttributes(attrs);
        if (this.mProc.persistent) {
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 300000);
    }

    public void onStart() {
        super.onStart();
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    protected void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.mReceiver);
    }

    public void dismiss() {
        if (!this.mResult.mHasResult) {
            this.mResult.set(1);
        }
        super.dismiss();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16909114:
                this.mHandler.obtainMessage(1).sendToTarget();
                return;
            case 16909116:
                this.mHandler.obtainMessage(2).sendToTarget();
                return;
            case 16909117:
                this.mHandler.obtainMessage(3).sendToTarget();
                return;
            case 16909118:
                this.mHandler.obtainMessage(5).sendToTarget();
                return;
            default:
                return;
        }
    }
}
