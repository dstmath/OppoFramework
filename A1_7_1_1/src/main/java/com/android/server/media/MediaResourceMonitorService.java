package com.android.server.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.media.IMediaResourceMonitor.Stub;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import com.android.server.SystemService;

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
public class MediaResourceMonitorService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String SERVICE_NAME = "media_resource_monitor";
    private static final String TAG = "MediaResourceMonitor";
    private final MediaResourceMonitorImpl mMediaResourceMonitorImpl;

    class MediaResourceMonitorImpl extends Stub {
        MediaResourceMonitorImpl() {
        }

        public void notifyResourceGranted(int pid, int type) throws RemoteException {
            if (MediaResourceMonitorService.DEBUG) {
                Slog.d(MediaResourceMonitorService.TAG, "notifyResourceGranted(pid=" + pid + ", type=" + type + ")");
            }
            long identity = Binder.clearCallingIdentity();
            try {
                String[] pkgNames = getPackageNamesFromPid(pid);
                if (pkgNames != null) {
                    int[] userIds = ((UserManager) MediaResourceMonitorService.this.getContext().getSystemService("user")).getEnabledProfileIds(ActivityManager.getCurrentUser());
                    if (userIds == null || userIds.length == 0) {
                        Binder.restoreCallingIdentity(identity);
                        return;
                    }
                    Intent intent = new Intent("android.intent.action.MEDIA_RESOURCE_GRANTED");
                    intent.putExtra("android.intent.extra.PACKAGES", pkgNames);
                    intent.putExtra("android.intent.extra.MEDIA_RESOURCE_TYPE", type);
                    for (int userId : userIds) {
                        MediaResourceMonitorService.this.getContext().sendBroadcastAsUser(intent, UserHandle.of(userId), "android.permission.RECEIVE_MEDIA_RESOURCE_USAGE");
                    }
                    Binder.restoreCallingIdentity(identity);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private String[] getPackageNamesFromPid(int pid) {
            try {
                for (RunningAppProcessInfo proc : ActivityManagerNative.getDefault().getRunningAppProcesses()) {
                    if (proc.pid == pid) {
                        return proc.pkgList;
                    }
                }
            } catch (RemoteException e) {
                Slog.w(MediaResourceMonitorService.TAG, "ActivityManager.getRunningAppProcesses() failed");
            }
            return null;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.media.MediaResourceMonitorService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.media.MediaResourceMonitorService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.media.MediaResourceMonitorService.<clinit>():void");
    }

    public MediaResourceMonitorService(Context context) {
        super(context);
        this.mMediaResourceMonitorImpl = new MediaResourceMonitorImpl();
    }

    public void onStart() {
        publishBinderService(SERVICE_NAME, this.mMediaResourceMonitorImpl);
    }
}
