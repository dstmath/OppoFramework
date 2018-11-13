package com.android.server.secrecy.policy;

import com.android.server.secrecy.policy.util.LogUtil;
import com.android.server.secrecy.policy.util.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

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
public class DownloadInfo {
    private static String TAG;
    private String mCurrentDownloadDate;
    private boolean mCurrentDownloadInternal;
    private String mCurrentDownloadStatus;
    private String mCurrentDownloadTime;
    private long mCurrentDownloadTimeInMillis;
    private long mLastDownloadTimeInMillis;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.DownloadInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.DownloadInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.secrecy.policy.DownloadInfo.<clinit>():void");
    }

    public void readDownloadInfo() {
        this.mCurrentDownloadStatus = Utils.getDownloadStatusString();
        this.mCurrentDownloadInternal = Utils.isFlashedInternal(this.mCurrentDownloadStatus);
        this.mCurrentDownloadDate = Utils.getDownloadDate(this.mCurrentDownloadStatus);
        this.mCurrentDownloadTime = Utils.getDownloadTime(this.mCurrentDownloadStatus);
        this.mCurrentDownloadTimeInMillis = Utils.getFlashIimeInMillis(this.mCurrentDownloadDate, this.mCurrentDownloadTime);
    }

    public boolean isCurrentDownloadInternal() {
        return this.mCurrentDownloadInternal;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("mCurrentDownloadInternal   = " + this.mCurrentDownloadInternal);
        pw.print(prefix);
        pw.println("mCurrentDownloadStatus = " + this.mCurrentDownloadStatus);
        pw.print(prefix);
        pw.println("mCurrentDownloadTimeInMillis = " + this.mCurrentDownloadTimeInMillis + ", mLastDownloadTimeInMillis = " + this.mLastDownloadTimeInMillis);
        pw.print(prefix);
    }

    public long getCurrentDownloadTimeInMills() {
        return this.mCurrentDownloadTimeInMillis;
    }

    public long getLastDownloadTimeInMills() {
        return this.mLastDownloadTimeInMillis;
    }

    public void setLastDownloadTimeInMills(String lastDownloadTimeInMillis) {
        if (lastDownloadTimeInMillis != null) {
            this.mLastDownloadTimeInMillis = Long.parseLong(lastDownloadTimeInMillis);
        }
        LogUtil.d(TAG, "setLastDownloadTimeInMills, mLastDownloadTimeInMillis = " + this.mLastDownloadTimeInMillis + ", lastDownloadTimeInMillis = " + lastDownloadTimeInMillis);
    }
}
