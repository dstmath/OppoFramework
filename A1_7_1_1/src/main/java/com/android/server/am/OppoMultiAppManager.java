package com.android.server.am;

import android.content.pm.UserInfo;
import android.os.IUserManager.Stub;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.pm.UserManagerService;
import java.util.Set;

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
public class OppoMultiAppManager {
    public static final int FLAG_FROM = 67108864;
    private static final String TAG = "OppoMultiAppManager";
    public static final int USER_ID = 999;
    private static final Object mAppLock = null;
    private static OppoMultiAppManager mInstance;
    private static final Object mLock = null;
    private ActivityManagerService mAms;
    private int mCurrentUserId;
    private UserManagerService mUserManager;
    private UserManagerInternal mUserManagerInternal;
    private UserState mUss;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoMultiAppManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoMultiAppManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoMultiAppManager.<clinit>():void");
    }

    public static OppoMultiAppManager getInstance() {
        OppoMultiAppManager oppoMultiAppManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new OppoMultiAppManager();
            }
            oppoMultiAppManager = mInstance;
        }
        return oppoMultiAppManager;
    }

    private OppoMultiAppManager() {
        this.mCurrentUserId = -1;
    }

    public void setAms(ActivityManagerService ams) {
        this.mAms = ams;
        this.mUserManager = getUserManagerLocked();
        if (this.mUserManager.exists(USER_ID)) {
            synchronized (this.mAms) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    Log.v(TAG, "multi app: setAms begin:");
                    this.mCurrentUserId = USER_ID;
                    setStartedUsersLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    UserManagerService getUserManagerLocked() {
        if (this.mUserManager == null) {
            this.mUserManager = (UserManagerService) Stub.asInterface(ServiceManager.getService("user"));
        }
        return this.mUserManager;
    }

    UserManagerInternal getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }
        return this.mUserManagerInternal;
    }

    public boolean isCurrentProfile(int userId) {
        if (userId == this.mCurrentUserId) {
            return true;
        }
        return false;
    }

    public void createUser(UserInfo info) {
        boolean isOppo = false;
        if (this.mAms != null && info != null) {
            synchronized (this.mAms) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if ((info.flags & 67108864) != 0) {
                        isOppo = true;
                    }
                    if (isOppo) {
                        Log.v(TAG, "multi app: createUser " + info);
                        this.mCurrentUserId = info.id;
                        setStartedUsersLocked();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    private void setStartedUsersLocked() {
        this.mUss = new UserState(new UserHandle(this.mCurrentUserId));
        this.mUss.state = 3;
        this.mAms.mUserController.getStartedUsers().put(this.mCurrentUserId, this.mUss);
        this.mAms.mUserController.updateStartedUserArrayLocked();
        getUserManagerInternal().setUserState(this.mCurrentUserId, this.mUss.state);
        this.mAms.mHandler.obtainMessage(61, USER_ID, 0, this.mUss).sendToTarget();
        this.mAms.startUserInBackground(USER_ID);
    }

    public void removeUser(UserInfo info) {
        boolean isOppo = false;
        if (this.mAms != null && info != null) {
            synchronized (this.mAms) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if ((info.flags & 67108864) != 0) {
                        isOppo = true;
                    }
                    if (isOppo) {
                        Log.v(TAG, "multi app: removeUser " + info);
                        this.mCurrentUserId = -1;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public void inRecentTask(Set<Integer> includedUsers) {
        if (this.mCurrentUserId > 0) {
            includedUsers.add(Integer.valueOf(this.mCurrentUserId));
        }
    }

    public boolean enforceCrossUserPermission(int callingUid, int userId) {
        if (userId == this.mCurrentUserId || this.mCurrentUserId == UserHandle.getUserId(callingUid)) {
            return true;
        }
        return false;
    }
}
