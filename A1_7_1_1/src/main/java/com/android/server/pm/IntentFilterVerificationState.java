package com.android.server.pm;

import android.content.pm.PackageParser.ActivityIntentInfo;
import android.util.ArraySet;
import android.util.Slog;
import java.util.ArrayList;

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
public class IntentFilterVerificationState {
    public static final int STATE_UNDEFINED = 0;
    public static final int STATE_VERIFICATION_FAILURE = 3;
    public static final int STATE_VERIFICATION_PENDING = 1;
    public static final int STATE_VERIFICATION_SUCCESS = 2;
    static final String TAG = null;
    private ArrayList<ActivityIntentInfo> mFilters;
    private ArraySet<String> mHosts;
    private String mPackageName;
    private int mRequiredVerifierUid;
    private int mState;
    private int mUserId;
    private boolean mVerificationComplete;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.IntentFilterVerificationState.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.IntentFilterVerificationState.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.IntentFilterVerificationState.<clinit>():void");
    }

    public IntentFilterVerificationState(int verifierUid, int userId, String packageName) {
        this.mRequiredVerifierUid = 0;
        this.mFilters = new ArrayList();
        this.mHosts = new ArraySet();
        this.mRequiredVerifierUid = verifierUid;
        this.mUserId = userId;
        this.mPackageName = packageName;
        this.mState = 0;
        this.mVerificationComplete = false;
    }

    public void setState(int state) {
        if (state > 3 || state < 0) {
            this.mState = 0;
        } else {
            this.mState = state;
        }
    }

    public int getState() {
        return this.mState;
    }

    public void setPendingState() {
        setState(1);
    }

    public ArrayList<ActivityIntentInfo> getFilters() {
        return this.mFilters;
    }

    public boolean isVerificationComplete() {
        return this.mVerificationComplete;
    }

    public boolean isVerified() {
        boolean z = false;
        if (!this.mVerificationComplete) {
            return false;
        }
        if (this.mState == 2) {
            z = true;
        }
        return z;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getHostsString() {
        StringBuilder sb = new StringBuilder();
        int count = this.mHosts.size();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            String host = (String) this.mHosts.valueAt(i);
            if (host.startsWith("*.")) {
                host = host.substring(2);
            }
            sb.append(host);
        }
        return sb.toString();
    }

    public boolean setVerifierResponse(int callerUid, int code) {
        if (this.mRequiredVerifierUid == callerUid) {
            int state = 0;
            if (code == 1) {
                state = 2;
            } else if (code == -1) {
                state = 3;
            }
            this.mVerificationComplete = true;
            setState(state);
            return true;
        }
        Slog.d(TAG, "Cannot set verifier response with callerUid:" + callerUid + " and code:" + code + " as required verifierUid is:" + this.mRequiredVerifierUid);
        return false;
    }

    public void addFilter(ActivityIntentInfo filter) {
        this.mFilters.add(filter);
        this.mHosts.addAll(filter.getHostsList());
    }
}
