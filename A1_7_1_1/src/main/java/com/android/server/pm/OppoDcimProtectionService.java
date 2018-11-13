package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import com.mediatek.appworkingset.AWSDBHelper.PackageProcessList;
import java.util.HashSet;

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
public class OppoDcimProtectionService {
    private static final String ACTION_DCIM_PROTECTION = "com.oppo.intent.action.DCIM_PROTECTION";
    private static final String TAG = "OppoDcimProtectionService";
    private static final String UEVENT_MSG = "DENIED_STAT=DENIED";
    private static OppoDcimProtectionService mInstall;
    final Object lock;
    private Context mContext;
    private UEventObserver mDcimObserver;
    private HashSet<String> mRecordUid;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.OppoDcimProtectionService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.OppoDcimProtectionService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.OppoDcimProtectionService.<clinit>():void");
    }

    private OppoDcimProtectionService(Context context) {
        this.mContext = null;
        this.mRecordUid = new HashSet();
        this.lock = new Object();
        this.mDcimObserver = new UEventObserver() {
            /* JADX WARNING: Missing block: B:7:0x002d, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onUEvent(UEvent event) {
                synchronized (OppoDcimProtectionService.this.lock) {
                    String uid = event.get("UID");
                    String path = event.get("PATH");
                    String scontext = event.get("SCONTEXT");
                    String tcontext = event.get("TCONTEXT");
                    String tclass = event.get("TCLASS");
                    if (uid == null || path == null || scontext == null || tcontext == null || tclass == null) {
                    } else {
                        Intent intent = new Intent(OppoDcimProtectionService.ACTION_DCIM_PROTECTION);
                        intent.putExtra(PackageProcessList.KEY_UID, uid);
                        intent.putExtra("path", path);
                        intent.putExtra("scontext", scontext);
                        intent.putExtra("tcontext", tcontext);
                        intent.putExtra("tclass", tclass);
                        OppoDcimProtectionService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                }
            }
        };
        this.mContext = context;
        startObserving();
    }

    public static OppoDcimProtectionService getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoDcimProtectionService(context);
        }
        return mInstall;
    }

    private void startObserving() {
        this.mDcimObserver.startObserving(UEVENT_MSG);
    }
}
