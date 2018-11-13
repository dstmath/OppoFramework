package android.app;

import android.Manifest.permission;
import android.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.params.TonemapCurve;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Size;
import android.util.Slog;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.FastPrintWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlSerializer;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public class ActivityManager {
    public static final String ACTION_REPORT_HEAP_LIMIT = "android.app.action.REPORT_HEAP_LIMIT";
    public static final int APP_START_MODE_DELAYED = 1;
    public static final int APP_START_MODE_DISABLED = 2;
    public static final int APP_START_MODE_NORMAL = 0;
    public static final int ASSIST_CONTEXT_BASIC = 0;
    public static final int ASSIST_CONTEXT_FULL = 1;
    public static final int BROADCAST_FAILED_USER_STOPPED = -2;
    public static final int BROADCAST_STICKY_CANT_HAVE_PERMISSION = -1;
    public static final int BROADCAST_SUCCESS = 0;
    public static final int BUGREPORT_OPTION_FULL = 0;
    public static final int BUGREPORT_OPTION_INTERACTIVE = 1;
    public static final int BUGREPORT_OPTION_REMOTE = 2;
    public static final int BUGREPORT_OPTION_WEAR = 3;
    public static final int COMPAT_MODE_ALWAYS = -1;
    public static final int COMPAT_MODE_DISABLED = 0;
    public static final int COMPAT_MODE_ENABLED = 1;
    public static final int COMPAT_MODE_NEVER = -2;
    public static final int COMPAT_MODE_TOGGLE = 2;
    public static final int COMPAT_MODE_UNKNOWN = -3;
    public static final int DOCKED_STACK_CREATE_MODE_BOTTOM_OR_RIGHT = 1;
    public static final int DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT = 0;
    public static final int FLAG_AND_LOCKED = 2;
    public static final int FLAG_AND_UNLOCKED = 4;
    public static final int FLAG_AND_UNLOCKING_OR_UNLOCKED = 8;
    public static final int FLAG_OR_STOPPED = 1;
    public static final int INTENT_SENDER_ACTIVITY = 2;
    public static final int INTENT_SENDER_ACTIVITY_RESULT = 3;
    public static final int INTENT_SENDER_BROADCAST = 1;
    public static final int INTENT_SENDER_SERVICE = 4;
    public static final int LOCK_TASK_MODE_LOCKED = 1;
    public static final int LOCK_TASK_MODE_NONE = 0;
    public static final int LOCK_TASK_MODE_PINNED = 2;
    public static final int MAX_PROCESS_STATE = 16;
    public static final String META_HOME_ALTERNATE = "android.app.home.alternate";
    public static final int MIN_PROCESS_STATE = -1;
    public static final int MOVE_TASK_NO_USER_ACTION = 2;
    public static final int MOVE_TASK_WITH_HOME = 1;
    public static final int PROCESS_STATE_BACKUP = 8;
    public static final int PROCESS_STATE_BOUND_FOREGROUND_SERVICE = 3;
    public static final int PROCESS_STATE_CACHED_ACTIVITY = 14;
    public static final int PROCESS_STATE_CACHED_ACTIVITY_CLIENT = 15;
    public static final int PROCESS_STATE_CACHED_EMPTY = 16;
    public static final int PROCESS_STATE_FOREGROUND_SERVICE = 4;
    public static final int PROCESS_STATE_HEAVY_WEIGHT = 9;
    public static final int PROCESS_STATE_HOME = 12;
    public static final int PROCESS_STATE_IMPORTANT_BACKGROUND = 7;
    public static final int PROCESS_STATE_IMPORTANT_FOREGROUND = 6;
    public static final int PROCESS_STATE_LAST_ACTIVITY = 13;
    public static final int PROCESS_STATE_NONEXISTENT = -1;
    public static final int PROCESS_STATE_PERSISTENT = 0;
    public static final int PROCESS_STATE_PERSISTENT_UI = 1;
    public static final int PROCESS_STATE_RECEIVER = 11;
    public static final int PROCESS_STATE_SERVICE = 10;
    public static final int PROCESS_STATE_TOP = 2;
    public static final int PROCESS_STATE_TOP_SLEEPING = 5;
    public static final int RECENT_IGNORE_HOME_STACK_TASKS = 8;
    public static final int RECENT_IGNORE_UNAVAILABLE = 2;
    public static final int RECENT_INCLUDE_PROFILES = 4;
    public static final int RECENT_INGORE_DOCKED_STACK_TOP_TASK = 16;
    public static final int RECENT_INGORE_PINNED_STACK_TASKS = 32;
    public static final int RECENT_WITH_EXCLUDED = 1;
    public static final int RESIZE_MODE_FORCED = 2;
    public static final int RESIZE_MODE_PRESERVE_WINDOW = 1;
    public static final int RESIZE_MODE_SYSTEM = 0;
    public static final int RESIZE_MODE_SYSTEM_SCREEN_ROTATION = 1;
    public static final int RESIZE_MODE_USER = 1;
    public static final int RESIZE_MODE_USER_FORCED = 3;
    public static final int START_CANCELED = -6;
    public static final int START_CLASS_NOT_FOUND = -2;
    public static final int START_DELIVERED_TO_TOP = 3;
    public static final int START_FLAG_DEBUG = 2;
    public static final int START_FLAG_NATIVE_DEBUGGING = 8;
    public static final int START_FLAG_ONLY_IF_NEEDED = 1;
    public static final int START_FLAG_TRACK_ALLOCATION = 4;
    public static final int START_FORBIDDEN_FOR_FREQUENCY_CONTROL = -15;
    public static final int START_FORWARD_AND_REQUEST_CONFLICT = -3;
    public static final int START_INTENT_NOT_RESOLVED = -1;
    public static final int START_NOT_ACTIVITY = -5;
    public static final int START_NOT_CURRENT_USER_ACTIVITY = -8;
    public static final int START_NOT_VOICE_COMPATIBLE = -7;
    public static final int START_PERMISSION_DENIED = -4;
    public static final int START_RETURN_INTENT_TO_CALLER = 1;
    public static final int START_RETURN_LOCK_TASK_MODE_VIOLATION = 5;
    public static final int START_SUCCESS = 0;
    public static final int START_SWITCHES_CANCELED = 4;
    public static final int START_TASK_TO_FRONT = 2;
    public static final int START_VOICE_HIDDEN_SESSION = -10;
    public static final int START_VOICE_NOT_ACTIVE_SESSION = -9;
    private static String TAG = null;
    public static final int UID_OBSERVER_ACTIVE = 8;
    public static final int UID_OBSERVER_GONE = 2;
    public static final int UID_OBSERVER_IDLE = 4;
    public static final int UID_OBSERVER_PROCSTATE = 1;
    public static final int USER_OP_ERROR_IS_SYSTEM = -3;
    public static final int USER_OP_ERROR_RELATED_USERS_CANNOT_STOP = -4;
    public static final int USER_OP_IS_CURRENT = -2;
    public static final int USER_OP_SUCCESS = 0;
    public static final int USER_OP_UNKNOWN_USER = -1;
    private static int gMaxRecentTasks;
    Point mAppTaskThumbnailSize;
    private final Context mContext;
    private final Handler mHandler;

    public static class AppTask {
        private IAppTask mAppTaskImpl;

        public AppTask(IAppTask task) {
            this.mAppTaskImpl = task;
        }

        public void finishAndRemoveTask() {
            try {
                this.mAppTaskImpl.finishAndRemoveTask();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public RecentTaskInfo getTaskInfo() {
            try {
                return this.mAppTaskImpl.getTaskInfo();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void moveToFront() {
            try {
                this.mAppTaskImpl.moveToFront();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void startActivity(Context context, Intent intent, Bundle options) {
            ActivityThread thread = ActivityThread.currentActivityThread();
            thread.getInstrumentation().execStartActivityFromAppTask(context, thread.getApplicationThread(), this.mAppTaskImpl, intent, options);
        }

        public void setExcludeFromRecents(boolean exclude) {
            try {
                this.mAppTaskImpl.setExcludeFromRecents(exclude);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static class MemoryInfo implements Parcelable {
        public static final Creator<MemoryInfo> CREATOR = null;
        public long availMem;
        public long foregroundAppThreshold;
        public long hiddenAppThreshold;
        public boolean lowMemory;
        public long secondaryServerThreshold;
        public long threshold;
        public long totalMem;
        public long visibleAppThreshold;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.MemoryInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.MemoryInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.MemoryInfo.<clinit>():void");
        }

        /* synthetic */ MemoryInfo(Parcel source, MemoryInfo memoryInfo) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.availMem);
            dest.writeLong(this.totalMem);
            dest.writeLong(this.threshold);
            dest.writeInt(this.lowMemory ? 1 : 0);
            dest.writeLong(this.hiddenAppThreshold);
            dest.writeLong(this.secondaryServerThreshold);
            dest.writeLong(this.visibleAppThreshold);
            dest.writeLong(this.foregroundAppThreshold);
        }

        public void readFromParcel(Parcel source) {
            boolean z = false;
            this.availMem = source.readLong();
            this.totalMem = source.readLong();
            this.threshold = source.readLong();
            if (source.readInt() != 0) {
                z = true;
            }
            this.lowMemory = z;
            this.hiddenAppThreshold = source.readLong();
            this.secondaryServerThreshold = source.readLong();
            this.visibleAppThreshold = source.readLong();
            this.foregroundAppThreshold = source.readLong();
        }

        private MemoryInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class ProcessErrorStateInfo implements Parcelable {
        public static final int CRASHED = 1;
        public static final Creator<ProcessErrorStateInfo> CREATOR = null;
        public static final int NOT_RESPONDING = 2;
        public static final int NO_ERROR = 0;
        public int condition;
        public byte[] crashData;
        public String longMsg;
        public int pid;
        public String processName;
        public String shortMsg;
        public String stackTrace;
        public String tag;
        public int uid;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.ProcessErrorStateInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.ProcessErrorStateInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.ProcessErrorStateInfo.<clinit>():void");
        }

        /* synthetic */ ProcessErrorStateInfo(Parcel source, ProcessErrorStateInfo processErrorStateInfo) {
            this(source);
        }

        public ProcessErrorStateInfo() {
            this.crashData = null;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.condition);
            dest.writeString(this.processName);
            dest.writeInt(this.pid);
            dest.writeInt(this.uid);
            dest.writeString(this.tag);
            dest.writeString(this.shortMsg);
            dest.writeString(this.longMsg);
            dest.writeString(this.stackTrace);
        }

        public void readFromParcel(Parcel source) {
            this.condition = source.readInt();
            this.processName = source.readString();
            this.pid = source.readInt();
            this.uid = source.readInt();
            this.tag = source.readString();
            this.shortMsg = source.readString();
            this.longMsg = source.readString();
            this.stackTrace = source.readString();
        }

        private ProcessErrorStateInfo(Parcel source) {
            this.crashData = null;
            readFromParcel(source);
        }
    }

    public static class RecentTaskInfo implements Parcelable {
        public static final Creator<RecentTaskInfo> CREATOR = null;
        public int affiliatedTaskColor;
        public int affiliatedTaskId;
        public ComponentName baseActivity;
        public Intent baseIntent;
        public Rect bounds;
        public CharSequence description;
        public long firstActiveTime;
        public int id;
        public boolean isDockable;
        public long lastActiveTime;
        public int numActivities;
        public ComponentName origActivity;
        public int persistentId;
        public ComponentName realActivity;
        public int resizeMode;
        public int stackId;
        public TaskDescription taskDescription;
        public ComponentName topActivity;
        public int userId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RecentTaskInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RecentTaskInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.RecentTaskInfo.<clinit>():void");
        }

        /* synthetic */ RecentTaskInfo(Parcel source, RecentTaskInfo recentTaskInfo) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = 1;
            dest.writeInt(this.id);
            dest.writeInt(this.persistentId);
            if (this.baseIntent != null) {
                dest.writeInt(1);
                this.baseIntent.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            ComponentName.writeToParcel(this.origActivity, dest);
            ComponentName.writeToParcel(this.realActivity, dest);
            TextUtils.writeToParcel(this.description, dest, 1);
            if (this.taskDescription != null) {
                dest.writeInt(1);
                this.taskDescription.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(this.stackId);
            dest.writeInt(this.userId);
            dest.writeLong(this.firstActiveTime);
            dest.writeLong(this.lastActiveTime);
            dest.writeInt(this.affiliatedTaskId);
            dest.writeInt(this.affiliatedTaskColor);
            ComponentName.writeToParcel(this.baseActivity, dest);
            ComponentName.writeToParcel(this.topActivity, dest);
            dest.writeInt(this.numActivities);
            if (this.bounds != null) {
                dest.writeInt(1);
                this.bounds.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            if (!this.isDockable) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.resizeMode);
        }

        public void readFromParcel(Parcel source) {
            Intent intent;
            TaskDescription taskDescription;
            boolean z;
            this.id = source.readInt();
            this.persistentId = source.readInt();
            if (source.readInt() > 0) {
                intent = (Intent) Intent.CREATOR.createFromParcel(source);
            } else {
                intent = null;
            }
            this.baseIntent = intent;
            this.origActivity = ComponentName.readFromParcel(source);
            this.realActivity = ComponentName.readFromParcel(source);
            this.description = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            if (source.readInt() > 0) {
                taskDescription = (TaskDescription) TaskDescription.CREATOR.createFromParcel(source);
            } else {
                taskDescription = null;
            }
            this.taskDescription = taskDescription;
            this.stackId = source.readInt();
            this.userId = source.readInt();
            this.firstActiveTime = source.readLong();
            this.lastActiveTime = source.readLong();
            this.affiliatedTaskId = source.readInt();
            this.affiliatedTaskColor = source.readInt();
            this.baseActivity = ComponentName.readFromParcel(source);
            this.topActivity = ComponentName.readFromParcel(source);
            this.numActivities = source.readInt();
            this.bounds = source.readInt() > 0 ? (Rect) Rect.CREATOR.createFromParcel(source) : null;
            if (source.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.isDockable = z;
            this.resizeMode = source.readInt();
        }

        private RecentTaskInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningAppProcessInfo implements Parcelable {
        public static final Creator<RunningAppProcessInfo> CREATOR = null;
        public static final int FLAG_CANT_SAVE_STATE = 1;
        public static final int FLAG_HAS_ACTIVITIES = 4;
        public static final int FLAG_PERSISTENT = 2;
        public static final int IMPORTANCE_BACKGROUND = 400;
        public static final int IMPORTANCE_CANT_SAVE_STATE = 170;
        public static final int IMPORTANCE_EMPTY = 500;
        public static final int IMPORTANCE_FOREGROUND = 100;
        public static final int IMPORTANCE_FOREGROUND_SERVICE = 125;
        public static final int IMPORTANCE_GONE = 1000;
        public static final int IMPORTANCE_PERCEPTIBLE = 130;
        public static final int IMPORTANCE_SERVICE = 300;
        public static final int IMPORTANCE_TOP_SLEEPING = 150;
        public static final int IMPORTANCE_VISIBLE = 200;
        public static final int REASON_PROVIDER_IN_USE = 1;
        public static final int REASON_SERVICE_IN_USE = 2;
        public static final int REASON_UNKNOWN = 0;
        public int flags;
        public int importance;
        public int importanceReasonCode;
        public ComponentName importanceReasonComponent;
        public int importanceReasonImportance;
        public int importanceReasonPid;
        public int lastTrimLevel;
        public int lru;
        public int pid;
        public String[] pkgList;
        public String processName;
        public int processState;
        public int uid;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RunningAppProcessInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RunningAppProcessInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.RunningAppProcessInfo.<clinit>():void");
        }

        /* synthetic */ RunningAppProcessInfo(Parcel source, RunningAppProcessInfo runningAppProcessInfo) {
            this(source);
        }

        public static int procStateToImportance(int procState) {
            if (procState == -1) {
                return 1000;
            }
            if (procState >= 12) {
                return 400;
            }
            if (procState >= 10) {
                return 300;
            }
            if (procState > 9) {
                return 170;
            }
            if (procState >= 7) {
                return 130;
            }
            if (procState >= 6) {
                return 200;
            }
            if (procState >= 5) {
                return 150;
            }
            if (procState >= 4) {
                return 125;
            }
            return 100;
        }

        public RunningAppProcessInfo() {
            this.importance = 100;
            this.importanceReasonCode = 0;
            this.processState = 6;
        }

        public RunningAppProcessInfo(String pProcessName, int pPid, String[] pArr) {
            this.processName = pProcessName;
            this.pid = pPid;
            this.pkgList = pArr;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.processName);
            dest.writeInt(this.pid);
            dest.writeInt(this.uid);
            dest.writeStringArray(this.pkgList);
            dest.writeInt(this.flags);
            dest.writeInt(this.lastTrimLevel);
            dest.writeInt(this.importance);
            dest.writeInt(this.lru);
            dest.writeInt(this.importanceReasonCode);
            dest.writeInt(this.importanceReasonPid);
            ComponentName.writeToParcel(this.importanceReasonComponent, dest);
            dest.writeInt(this.importanceReasonImportance);
            dest.writeInt(this.processState);
        }

        public void readFromParcel(Parcel source) {
            this.processName = source.readString();
            this.pid = source.readInt();
            this.uid = source.readInt();
            this.pkgList = source.readStringArray();
            this.flags = source.readInt();
            this.lastTrimLevel = source.readInt();
            this.importance = source.readInt();
            this.lru = source.readInt();
            this.importanceReasonCode = source.readInt();
            this.importanceReasonPid = source.readInt();
            this.importanceReasonComponent = ComponentName.readFromParcel(source);
            this.importanceReasonImportance = source.readInt();
            this.processState = source.readInt();
        }

        private RunningAppProcessInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningServiceInfo implements Parcelable {
        public static final Creator<RunningServiceInfo> CREATOR = null;
        public static final int FLAG_FOREGROUND = 2;
        public static final int FLAG_PERSISTENT_PROCESS = 8;
        public static final int FLAG_STARTED = 1;
        public static final int FLAG_SYSTEM_PROCESS = 4;
        public long activeSince;
        public int clientCount;
        public int clientLabel;
        public String clientPackage;
        public int crashCount;
        public int flags;
        public boolean foreground;
        public long lastActivityTime;
        public int pid;
        public String process;
        public long restarting;
        public ComponentName service;
        public boolean started;
        public int uid;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RunningServiceInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RunningServiceInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.RunningServiceInfo.<clinit>():void");
        }

        /* synthetic */ RunningServiceInfo(Parcel source, RunningServiceInfo runningServiceInfo) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            ComponentName.writeToParcel(this.service, dest);
            dest.writeInt(this.pid);
            dest.writeInt(this.uid);
            dest.writeString(this.process);
            if (this.foreground) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeLong(this.activeSince);
            if (!this.started) {
                i2 = 0;
            }
            dest.writeInt(i2);
            dest.writeInt(this.clientCount);
            dest.writeInt(this.crashCount);
            dest.writeLong(this.lastActivityTime);
            dest.writeLong(this.restarting);
            dest.writeInt(this.flags);
            dest.writeString(this.clientPackage);
            dest.writeInt(this.clientLabel);
        }

        public void readFromParcel(Parcel source) {
            boolean z;
            boolean z2 = true;
            this.service = ComponentName.readFromParcel(source);
            this.pid = source.readInt();
            this.uid = source.readInt();
            this.process = source.readString();
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.foreground = z;
            this.activeSince = source.readLong();
            if (source.readInt() == 0) {
                z2 = false;
            }
            this.started = z2;
            this.clientCount = source.readInt();
            this.crashCount = source.readInt();
            this.lastActivityTime = source.readLong();
            this.restarting = source.readLong();
            this.flags = source.readInt();
            this.clientPackage = source.readString();
            this.clientLabel = source.readInt();
        }

        private RunningServiceInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningTaskInfo implements Parcelable {
        public static final Creator<RunningTaskInfo> CREATOR = null;
        public ComponentName baseActivity;
        public CharSequence description;
        public int id;
        public boolean isDockable;
        public boolean isTopDockable;
        public long lastActiveTime;
        public int numActivities;
        public int numRunning;
        public int resizeMode;
        public int stackId;
        public Bitmap thumbnail;
        public ComponentName topActivity;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RunningTaskInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.RunningTaskInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.RunningTaskInfo.<clinit>():void");
        }

        /* synthetic */ RunningTaskInfo(Parcel source, RunningTaskInfo runningTaskInfo) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            dest.writeInt(this.id);
            dest.writeInt(this.stackId);
            ComponentName.writeToParcel(this.baseActivity, dest);
            ComponentName.writeToParcel(this.topActivity, dest);
            if (this.thumbnail != null) {
                dest.writeInt(1);
                this.thumbnail.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            TextUtils.writeToParcel(this.description, dest, 1);
            dest.writeInt(this.numActivities);
            dest.writeInt(this.numRunning);
            if (this.isDockable) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.resizeMode);
            if (!this.isTopDockable) {
                i2 = 0;
            }
            dest.writeInt(i2);
        }

        public void readFromParcel(Parcel source) {
            boolean z;
            boolean z2 = true;
            this.id = source.readInt();
            this.stackId = source.readInt();
            this.baseActivity = ComponentName.readFromParcel(source);
            this.topActivity = ComponentName.readFromParcel(source);
            if (source.readInt() != 0) {
                this.thumbnail = (Bitmap) Bitmap.CREATOR.createFromParcel(source);
            } else {
                this.thumbnail = null;
            }
            this.description = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            this.numActivities = source.readInt();
            this.numRunning = source.readInt();
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.isDockable = z;
            this.resizeMode = source.readInt();
            if (source.readInt() == 0) {
                z2 = false;
            }
            this.isTopDockable = z2;
        }

        private RunningTaskInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class StackId {
        public static final int DOCKED_STACK_ID = 3;
        public static final int FIRST_DYNAMIC_STACK_ID = 5;
        public static final int FIRST_STATIC_STACK_ID = 0;
        public static final int FREEFORM_WORKSPACE_STACK_ID = 2;
        public static final int FULLSCREEN_WORKSPACE_STACK_ID = 1;
        public static final int HOME_STACK_ID = 0;
        public static final int INVALID_STACK_ID = -1;
        public static final int LAST_STATIC_STACK_ID = 4;
        public static final int PINNED_STACK_ID = 4;

        public static boolean isStaticStack(int stackId) {
            return stackId >= 0 && stackId <= 4;
        }

        public static boolean hasWindowShadow(int stackId) {
            return stackId == 2 || stackId == 4;
        }

        public static boolean hasWindowDecor(int stackId) {
            return stackId == 2;
        }

        public static boolean isTaskResizeAllowed(int stackId) {
            return stackId == 2;
        }

        public static boolean persistTaskBounds(int stackId) {
            return stackId == 2;
        }

        public static boolean isDynamicStacksVisibleBehindAllowed(int stackId) {
            return stackId == 4;
        }

        public static boolean keepFocusInStackIfPossible(int stackId) {
            if (stackId == 2 || stackId == 3 || stackId == 4) {
                return true;
            }
            return false;
        }

        public static boolean isResizeableByDockedStack(int stackId) {
            if (!isStaticStack(stackId) || stackId == 3 || stackId == 4) {
                return false;
            }
            return true;
        }

        public static boolean isTaskResizeableByDockedStack(int stackId) {
            if (!isStaticStack(stackId) || stackId == 2 || stackId == 3 || stackId == 4) {
                return false;
            }
            return true;
        }

        public static boolean replaceWindowsOnTaskMove(int sourceStackId, int targetStackId) {
            if (sourceStackId == 2 || targetStackId == 2) {
                return true;
            }
            return false;
        }

        public static boolean tasksAreFloating(int stackId) {
            if (stackId == 2 || stackId == 4) {
                return true;
            }
            return false;
        }

        public static boolean useAnimationSpecForAppTransition(int stackId) {
            if (stackId == 2 || stackId == 1 || stackId == 3 || stackId == -1) {
                return true;
            }
            return false;
        }

        public static boolean canReceiveKeys(int stackId) {
            return stackId != 4;
        }

        public static boolean isAllowedOverLockscreen(int stackId) {
            return stackId == 0 || stackId == 1;
        }

        public static boolean isAlwaysOnTop(int stackId) {
            return stackId == 4;
        }

        public static boolean allowTopTaskToReturnHome(int stackId) {
            return stackId != 4;
        }

        public static boolean resizeStackWithLaunchBounds(int stackId) {
            return stackId == 4;
        }

        public static boolean keepVisibleDeadAppWindowOnScreen(int stackId) {
            return stackId != 4;
        }

        public static boolean useWindowFrameForBackdrop(int stackId) {
            return stackId == 2 || stackId == 4;
        }

        public static boolean normallyFullscreenWindows(int stackId) {
            if (stackId == 4 || stackId == 2 || stackId == 3) {
                return false;
            }
            return true;
        }

        public static boolean isMultiWindowStack(int stackId) {
            if (isStaticStack(stackId) || stackId == 4 || stackId == 2 || stackId == 3) {
                return true;
            }
            return false;
        }

        public static boolean activitiesCanRequestVisibleBehind(int stackId) {
            return stackId == 1;
        }

        public static boolean windowsAreScaleable(int stackId) {
            return stackId == 4;
        }

        public static boolean hasMovementAnimations(int stackId) {
            return stackId != 4;
        }
    }

    public static class StackInfo implements Parcelable {
        public static final Creator<StackInfo> CREATOR = null;
        public Rect bounds;
        public int displayId;
        public int position;
        public int stackId;
        public Rect[] taskBounds;
        public int[] taskIds;
        public String[] taskNames;
        public int[] taskUserIds;
        public ComponentName topActivity;
        public int userId;
        public boolean visible;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.StackInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.StackInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.StackInfo.<clinit>():void");
        }

        /* synthetic */ StackInfo(Parcel source, StackInfo stackInfo) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.stackId);
            dest.writeInt(this.bounds.left);
            dest.writeInt(this.bounds.top);
            dest.writeInt(this.bounds.right);
            dest.writeInt(this.bounds.bottom);
            dest.writeIntArray(this.taskIds);
            dest.writeStringArray(this.taskNames);
            int boundsCount = this.taskBounds == null ? 0 : this.taskBounds.length;
            dest.writeInt(boundsCount);
            for (int i = 0; i < boundsCount; i++) {
                dest.writeInt(this.taskBounds[i].left);
                dest.writeInt(this.taskBounds[i].top);
                dest.writeInt(this.taskBounds[i].right);
                dest.writeInt(this.taskBounds[i].bottom);
            }
            dest.writeIntArray(this.taskUserIds);
            dest.writeInt(this.displayId);
            dest.writeInt(this.userId);
            dest.writeInt(this.visible ? 1 : 0);
            dest.writeInt(this.position);
            if (this.topActivity != null) {
                dest.writeInt(1);
                this.topActivity.writeToParcel(dest, 0);
                return;
            }
            dest.writeInt(0);
        }

        public void readFromParcel(Parcel source) {
            boolean z = false;
            this.stackId = source.readInt();
            this.bounds = new Rect(source.readInt(), source.readInt(), source.readInt(), source.readInt());
            this.taskIds = source.createIntArray();
            this.taskNames = source.createStringArray();
            int boundsCount = source.readInt();
            if (boundsCount > 0) {
                this.taskBounds = new Rect[boundsCount];
                for (int i = 0; i < boundsCount; i++) {
                    this.taskBounds[i] = new Rect();
                    this.taskBounds[i].set(source.readInt(), source.readInt(), source.readInt(), source.readInt());
                }
            } else {
                this.taskBounds = null;
            }
            this.taskUserIds = source.createIntArray();
            this.displayId = source.readInt();
            this.userId = source.readInt();
            if (source.readInt() > 0) {
                z = true;
            }
            this.visible = z;
            this.position = source.readInt();
            if (source.readInt() > 0) {
                this.topActivity = ComponentName.readFromParcel(source);
            }
        }

        public StackInfo() {
            this.bounds = new Rect();
        }

        private StackInfo(Parcel source) {
            this.bounds = new Rect();
            readFromParcel(source);
        }

        public String toString(String prefix) {
            StringBuilder sb = new StringBuilder(256);
            sb.append(prefix);
            sb.append("Stack id=");
            sb.append(this.stackId);
            sb.append(" bounds=");
            sb.append(this.bounds.toShortString());
            sb.append(" displayId=");
            sb.append(this.displayId);
            sb.append(" userId=");
            sb.append(this.userId);
            sb.append("\n");
            prefix = prefix + "  ";
            for (int i = 0; i < this.taskIds.length; i++) {
                sb.append(prefix);
                sb.append("taskId=");
                sb.append(this.taskIds[i]);
                sb.append(": ");
                sb.append(this.taskNames[i]);
                if (this.taskBounds != null) {
                    sb.append(" bounds=");
                    sb.append(this.taskBounds[i].toShortString());
                }
                sb.append(" userId=").append(this.taskUserIds[i]);
                sb.append(" visible=").append(this.visible);
                if (this.topActivity != null) {
                    sb.append(" topActivity=").append(this.topActivity);
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        public String toString() {
            return toString("");
        }
    }

    public static class TaskDescription implements Parcelable {
        private static final String ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND = "task_description_colorBackground";
        private static final String ATTR_TASKDESCRIPTIONCOLOR_PRIMARY = "task_description_color";
        private static final String ATTR_TASKDESCRIPTIONICONFILENAME = "task_description_icon_filename";
        private static final String ATTR_TASKDESCRIPTIONLABEL = "task_description_label";
        public static final String ATTR_TASKDESCRIPTION_PREFIX = "task_description_";
        public static final Creator<TaskDescription> CREATOR = null;
        private int mColorBackground;
        private int mColorPrimary;
        private Bitmap mIcon;
        private String mIconFilename;
        private String mLabel;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.TaskDescription.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.TaskDescription.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.TaskDescription.<clinit>():void");
        }

        public TaskDescription(String label, Bitmap icon, int colorPrimary) {
            this(label, icon, null, colorPrimary, 0);
            if (colorPrimary != 0 && Color.alpha(colorPrimary) != 255) {
                throw new RuntimeException("A TaskDescription's primary color should be opaque");
            }
        }

        public TaskDescription(String label, Bitmap icon) {
            this(label, icon, null, 0, 0);
        }

        public TaskDescription(String label) {
            this(label, null, null, 0, 0);
        }

        public TaskDescription() {
            this(null, null, null, 0, 0);
        }

        public TaskDescription(String label, Bitmap icon, String iconFilename, int colorPrimary, int colorBackground) {
            this.mLabel = label;
            this.mIcon = icon;
            this.mIconFilename = iconFilename;
            this.mColorPrimary = colorPrimary;
            this.mColorBackground = colorBackground;
        }

        public TaskDescription(TaskDescription td) {
            copyFrom(td);
        }

        public void copyFrom(TaskDescription other) {
            this.mLabel = other.mLabel;
            this.mIcon = other.mIcon;
            this.mIconFilename = other.mIconFilename;
            this.mColorPrimary = other.mColorPrimary;
            this.mColorBackground = other.mColorBackground;
        }

        private TaskDescription(Parcel source) {
            readFromParcel(source);
        }

        public void setLabel(String label) {
            this.mLabel = label;
        }

        public void setPrimaryColor(int primaryColor) {
            if (primaryColor == 0 || Color.alpha(primaryColor) == 255) {
                this.mColorPrimary = primaryColor;
                return;
            }
            throw new RuntimeException("A TaskDescription's primary color should be opaque");
        }

        public void setBackgroundColor(int backgroundColor) {
            if (backgroundColor == 0 || Color.alpha(backgroundColor) == 255) {
                this.mColorBackground = backgroundColor;
                return;
            }
            throw new RuntimeException("A TaskDescription's background color should be opaque");
        }

        public void setIcon(Bitmap icon) {
            this.mIcon = icon;
        }

        public void setIconFilename(String iconFilename) {
            this.mIconFilename = iconFilename;
            this.mIcon = null;
        }

        public String getLabel() {
            return this.mLabel;
        }

        public Bitmap getIcon() {
            if (this.mIcon != null) {
                return this.mIcon;
            }
            return loadTaskDescriptionIcon(this.mIconFilename, UserHandle.myUserId());
        }

        public String getIconFilename() {
            return this.mIconFilename;
        }

        public Bitmap getInMemoryIcon() {
            return this.mIcon;
        }

        public static Bitmap loadTaskDescriptionIcon(String iconFilename, int userId) {
            if (iconFilename == null) {
                return null;
            }
            try {
                return ActivityManagerNative.getDefault().getTaskDescriptionIcon(iconFilename, userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public int getPrimaryColor() {
            return this.mColorPrimary;
        }

        public int getBackgroundColor() {
            return this.mColorBackground;
        }

        public void saveToXml(XmlSerializer out) throws IOException {
            if (this.mLabel != null) {
                out.attribute(null, ATTR_TASKDESCRIPTIONLABEL, this.mLabel);
            }
            if (this.mColorPrimary != 0) {
                out.attribute(null, ATTR_TASKDESCRIPTIONCOLOR_PRIMARY, Integer.toHexString(this.mColorPrimary));
            }
            if (this.mColorBackground != 0) {
                out.attribute(null, ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND, Integer.toHexString(this.mColorBackground));
            }
            if (this.mIconFilename != null) {
                out.attribute(null, ATTR_TASKDESCRIPTIONICONFILENAME, this.mIconFilename);
            }
        }

        public void restoreFromXml(String attrName, String attrValue) {
            if (ATTR_TASKDESCRIPTIONLABEL.equals(attrName)) {
                setLabel(attrValue);
            } else if (ATTR_TASKDESCRIPTIONCOLOR_PRIMARY.equals(attrName)) {
                setPrimaryColor((int) Long.parseLong(attrValue, 16));
            } else if (ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND.equals(attrName)) {
                setBackgroundColor((int) Long.parseLong(attrValue, 16));
            } else if (ATTR_TASKDESCRIPTIONICONFILENAME.equals(attrName)) {
                setIconFilename(attrValue);
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mLabel == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                dest.writeString(this.mLabel);
            }
            if (this.mIcon == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                this.mIcon.writeToParcel(dest, 0);
            }
            dest.writeInt(this.mColorPrimary);
            dest.writeInt(this.mColorBackground);
            if (this.mIconFilename == null) {
                dest.writeInt(0);
                return;
            }
            dest.writeInt(1);
            dest.writeString(this.mIconFilename);
        }

        public void readFromParcel(Parcel source) {
            String readString;
            Bitmap bitmap;
            String str = null;
            if (source.readInt() > 0) {
                readString = source.readString();
            } else {
                readString = null;
            }
            this.mLabel = readString;
            if (source.readInt() > 0) {
                bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(source);
            } else {
                bitmap = null;
            }
            this.mIcon = bitmap;
            this.mColorPrimary = source.readInt();
            this.mColorBackground = source.readInt();
            if (source.readInt() > 0) {
                str = source.readString();
            }
            this.mIconFilename = str;
        }

        public String toString() {
            return "TaskDescription Label: " + this.mLabel + " Icon: " + this.mIcon + " IconFilename: " + this.mIconFilename + " colorPrimary: " + this.mColorPrimary + " colorBackground: " + this.mColorBackground;
        }
    }

    public static class TaskThumbnail implements Parcelable {
        public static final Creator<TaskThumbnail> CREATOR = null;
        public Bitmap mainThumbnail;
        public ParcelFileDescriptor thumbnailFileDescriptor;
        public TaskThumbnailInfo thumbnailInfo;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.TaskThumbnail.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.TaskThumbnail.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.TaskThumbnail.<clinit>():void");
        }

        /* synthetic */ TaskThumbnail(Parcel source, TaskThumbnail taskThumbnail) {
            this(source);
        }

        private TaskThumbnail(Parcel source) {
            readFromParcel(source);
        }

        public int describeContents() {
            if (this.thumbnailFileDescriptor != null) {
                return this.thumbnailFileDescriptor.describeContents();
            }
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mainThumbnail != null) {
                dest.writeInt(1);
                this.mainThumbnail.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            if (this.thumbnailFileDescriptor != null) {
                dest.writeInt(1);
                this.thumbnailFileDescriptor.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            if (this.thumbnailInfo != null) {
                dest.writeInt(1);
                this.thumbnailInfo.writeToParcel(dest, flags);
                return;
            }
            dest.writeInt(0);
        }

        public void readFromParcel(Parcel source) {
            if (source.readInt() != 0) {
                this.mainThumbnail = (Bitmap) Bitmap.CREATOR.createFromParcel(source);
            } else {
                this.mainThumbnail = null;
            }
            if (source.readInt() != 0) {
                this.thumbnailFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(source);
            } else {
                this.thumbnailFileDescriptor = null;
            }
            if (source.readInt() != 0) {
                this.thumbnailInfo = (TaskThumbnailInfo) TaskThumbnailInfo.CREATOR.createFromParcel(source);
            } else {
                this.thumbnailInfo = null;
            }
        }
    }

    public static class TaskThumbnailInfo implements Parcelable {
        private static final String ATTR_SCREEN_ORIENTATION = "task_thumbnailinfo_screen_orientation";
        private static final String ATTR_TASK_HEIGHT = "task_thumbnailinfo_task_height";
        public static final String ATTR_TASK_THUMBNAILINFO_PREFIX = "task_thumbnailinfo_";
        private static final String ATTR_TASK_WIDTH = "task_thumbnailinfo_task_width";
        public static final Creator<TaskThumbnailInfo> CREATOR = null;
        public int screenOrientation;
        public int taskHeight;
        public int taskWidth;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.TaskThumbnailInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.TaskThumbnailInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.TaskThumbnailInfo.<clinit>():void");
        }

        /* synthetic */ TaskThumbnailInfo(Parcel source, TaskThumbnailInfo taskThumbnailInfo) {
            this(source);
        }

        public TaskThumbnailInfo() {
            this.screenOrientation = 0;
        }

        private TaskThumbnailInfo(Parcel source) {
            this.screenOrientation = 0;
            readFromParcel(source);
        }

        public void reset() {
            this.taskWidth = 0;
            this.taskHeight = 0;
            this.screenOrientation = 0;
        }

        public void copyFrom(TaskThumbnailInfo o) {
            this.taskWidth = o.taskWidth;
            this.taskHeight = o.taskHeight;
            this.screenOrientation = o.screenOrientation;
        }

        public void saveToXml(XmlSerializer out) throws IOException {
            out.attribute(null, ATTR_TASK_WIDTH, Integer.toString(this.taskWidth));
            out.attribute(null, ATTR_TASK_HEIGHT, Integer.toString(this.taskHeight));
            out.attribute(null, ATTR_SCREEN_ORIENTATION, Integer.toString(this.screenOrientation));
        }

        public void restoreFromXml(String attrName, String attrValue) {
            if (ATTR_TASK_WIDTH.equals(attrName)) {
                this.taskWidth = Integer.parseInt(attrValue);
            } else if (ATTR_TASK_HEIGHT.equals(attrName)) {
                this.taskHeight = Integer.parseInt(attrValue);
            } else if (ATTR_SCREEN_ORIENTATION.equals(attrName)) {
                this.screenOrientation = Integer.parseInt(attrValue);
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.taskWidth);
            dest.writeInt(this.taskHeight);
            dest.writeInt(this.screenOrientation);
        }

        public void readFromParcel(Parcel source) {
            this.taskWidth = source.readInt();
            this.taskHeight = source.readInt();
            this.screenOrientation = source.readInt();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManager.<clinit>():void");
    }

    public static final boolean isProcStateBackground(int procState) {
        return procState >= 8;
    }

    ActivityManager(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public int getFrontActivityScreenCompatMode() {
        try {
            return ActivityManagerNative.getDefault().getFrontActivityScreenCompatMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setFrontActivityScreenCompatMode(int mode) {
        try {
            ActivityManagerNative.getDefault().setFrontActivityScreenCompatMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPackageScreenCompatMode(String packageName) {
        try {
            return ActivityManagerNative.getDefault().getPackageScreenCompatMode(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPackageScreenCompatMode(String packageName, int mode) {
        try {
            ActivityManagerNative.getDefault().setPackageScreenCompatMode(packageName, mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getPackageAskScreenCompat(String packageName) {
        try {
            return ActivityManagerNative.getDefault().getPackageAskScreenCompat(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPackageAskScreenCompat(String packageName, boolean ask) {
        try {
            ActivityManagerNative.getDefault().setPackageAskScreenCompat(packageName, ask);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMemoryClass() {
        return staticGetMemoryClass();
    }

    public static int staticGetMemoryClass() {
        String vmHeapSize = SystemProperties.get("dalvik.vm.heapgrowthlimit", "");
        if (vmHeapSize == null || "".equals(vmHeapSize)) {
            return staticGetLargeMemoryClass();
        }
        return Integer.parseInt(vmHeapSize.substring(0, vmHeapSize.length() - 1));
    }

    public int getLargeMemoryClass() {
        return staticGetLargeMemoryClass();
    }

    public static int staticGetLargeMemoryClass() {
        String vmHeapSize = SystemProperties.get("dalvik.vm.heapsize", "16m");
        return Integer.parseInt(vmHeapSize.substring(0, vmHeapSize.length() - 1));
    }

    public boolean isLowRamDevice() {
        return isLowRamDeviceStatic();
    }

    public static boolean isLowRamDeviceStatic() {
        return "true".equals(SystemProperties.get("ro.config.low_ram", "false"));
    }

    public static boolean isOppoLowRamDeviceStatic() {
        return "true".equals(SystemProperties.get("ro.config.oppo.low_ram", "false"));
    }

    public static boolean isHighEndGfx() {
        if (isLowRamDeviceStatic() || Resources.getSystem().getBoolean(17956882)) {
            return false;
        }
        return true;
    }

    public static int getMaxRecentTasksStatic() {
        if (gMaxRecentTasks >= 0) {
            return gMaxRecentTasks;
        }
        if (SystemProperties.get("ro.mtk_gmo_ram_optimize").equals(WifiEnterpriseConfig.ENGINE_ENABLE) || isLowRamDeviceStatic()) {
            gMaxRecentTasks = 36;
            return 36;
        }
        gMaxRecentTasks = 48;
        return 48;
    }

    public static int getDefaultAppRecentsLimitStatic() {
        return getMaxRecentTasksStatic() / 6;
    }

    public static int getMaxAppRecentsLimitStatic() {
        return getMaxRecentTasksStatic() / 2;
    }

    public static boolean supportsMultiWindow() {
        if (isLowRamDeviceStatic()) {
            return false;
        }
        return Resources.getSystem().getBoolean(17957039);
    }

    @Deprecated
    public List<RecentTaskInfo> getRecentTasks(int maxNum, int flags) throws SecurityException {
        try {
            return ActivityManagerNative.getDefault().getRecentTasks(maxNum, flags, UserHandle.myUserId()).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<RecentTaskInfo> getRecentTasksForUser(int maxNum, int flags, int userId) throws SecurityException {
        try {
            return ActivityManagerNative.getDefault().getRecentTasks(maxNum, flags, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<AppTask> getAppTasks() {
        ArrayList<AppTask> tasks = new ArrayList();
        try {
            List<IAppTask> appTasks = ActivityManagerNative.getDefault().getAppTasks(this.mContext.getPackageName());
            int numAppTasks = appTasks.size();
            for (int i = 0; i < numAppTasks; i++) {
                tasks.add(new AppTask((IAppTask) appTasks.get(i)));
            }
            return tasks;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Size getAppTaskThumbnailSize() {
        Size size;
        synchronized (this) {
            ensureAppTaskThumbnailSizeLocked();
            size = new Size(this.mAppTaskThumbnailSize.x, this.mAppTaskThumbnailSize.y);
        }
        return size;
    }

    private void ensureAppTaskThumbnailSizeLocked() {
        if (this.mAppTaskThumbnailSize == null) {
            try {
                this.mAppTaskThumbnailSize = ActivityManagerNative.getDefault().getAppTaskThumbnailSize();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int addAppTask(Activity activity, Intent intent, TaskDescription description, Bitmap thumbnail) {
        Point size;
        synchronized (this) {
            ensureAppTaskThumbnailSizeLocked();
            size = this.mAppTaskThumbnailSize;
        }
        int tw = thumbnail.getWidth();
        int th = thumbnail.getHeight();
        if (!(tw == size.x && th == size.y)) {
            float scale;
            Bitmap bm = Bitmap.createBitmap(size.x, size.y, thumbnail.getConfig());
            float dx = TonemapCurve.LEVEL_BLACK;
            if (size.x * tw > size.y * th) {
                scale = ((float) size.x) / ((float) th);
                dx = (((float) size.y) - (((float) tw) * scale)) * 0.5f;
            } else {
                scale = ((float) size.y) / ((float) tw);
                float dy = (((float) size.x) - (((float) th) * scale)) * 0.5f;
            }
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate((float) ((int) (0.5f + dx)), TonemapCurve.LEVEL_BLACK);
            Canvas canvas = new Canvas(bm);
            canvas.drawBitmap(thumbnail, matrix, null);
            canvas.setBitmap(null);
            thumbnail = bm;
        }
        if (description == null) {
            description = new TaskDescription();
        }
        try {
            return ActivityManagerNative.getDefault().addAppTask(activity.getActivityToken(), intent, description, thumbnail);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public List<RunningTaskInfo> getRunningTasks(int maxNum) throws SecurityException {
        try {
            return ActivityManagerNative.getDefault().getTasks(maxNum, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeTask(int taskId) throws SecurityException {
        try {
            return ActivityManagerNative.getDefault().removeTask(taskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public TaskThumbnail getTaskThumbnail(int id) throws SecurityException {
        try {
            return ActivityManagerNative.getDefault().getTaskThumbnail(id);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInHomeStack(int taskId) {
        try {
            return ActivityManagerNative.getDefault().isInHomeStack(taskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void moveTaskToFront(int taskId, int flags) {
        moveTaskToFront(taskId, flags, null);
    }

    public void moveTaskToFront(int taskId, int flags, Bundle options) {
        try {
            ActivityManagerNative.getDefault().moveTaskToFront(taskId, flags, options);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<RunningServiceInfo> getRunningServices(int maxNum) throws SecurityException {
        try {
            return ActivityManagerNative.getDefault().getServices(maxNum, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PendingIntent getRunningServiceControlPanel(ComponentName service) throws SecurityException {
        try {
            return ActivityManagerNative.getDefault().getRunningServiceControlPanel(service);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void getMemoryInfo(MemoryInfo outInfo) {
        try {
            ActivityManagerNative.getDefault().getMemoryInfo(outInfo);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        try {
            return ActivityManagerNative.getDefault().clearApplicationUserData(packageName, observer, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearApplicationUserData() {
        return clearApplicationUserData(this.mContext.getPackageName(), null);
    }

    public ParceledListSlice<UriPermission> getGrantedUriPermissions(String packageName) {
        try {
            return ActivityManagerNative.getDefault().getGrantedUriPermissions(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearGrantedUriPermissions(String packageName) {
        try {
            ActivityManagerNative.getDefault().clearGrantedUriPermissions(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ProcessErrorStateInfo> getProcessesInErrorState() {
        try {
            return ActivityManagerNative.getDefault().getProcessesInErrorState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ApplicationInfo> getRunningExternalApplications() {
        try {
            return ActivityManagerNative.getDefault().getRunningExternalApplications();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setProcessMemoryTrimLevel(String process, int userId, int level) {
        try {
            return ActivityManagerNative.getDefault().setProcessMemoryTrimLevel(process, userId, level);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<RunningAppProcessInfo> getRunningAppProcesses() {
        try {
            return ActivityManagerNative.getDefault().getRunningAppProcesses();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPackageImportance(String packageName) {
        try {
            return RunningAppProcessInfo.procStateToImportance(ActivityManagerNative.getDefault().getPackageProcessState(packageName, this.mContext.getOpPackageName()));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void getMyMemoryState(RunningAppProcessInfo outState) {
        try {
            ActivityManagerNative.getDefault().getMyMemoryState(outState);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public android.os.Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) {
        try {
            return ActivityManagerNative.getDefault().getProcessMemoryInfo(pids);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void restartPackage(String packageName) {
        killBackgroundProcesses(packageName);
    }

    public void killBackgroundProcesses(String packageName) {
        try {
            ActivityManagerNative.getDefault().killBackgroundProcesses(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void killUid(int uid, String reason) {
        try {
            ActivityManagerNative.getDefault().killUid(UserHandle.getAppId(uid), UserHandle.getUserId(uid), reason);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forceStopPackageAsUser(String packageName, int userId) {
        try {
            ActivityManagerNative.getDefault().forceStopPackage(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forceStopPackage(String packageName) {
        forceStopPackageAsUser(packageName, UserHandle.myUserId());
    }

    public ConfigurationInfo getDeviceConfigurationInfo() {
        try {
            return ActivityManagerNative.getDefault().getDeviceConfigurationInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getLauncherLargeIconDensity() {
        Resources res = this.mContext.getResources();
        int density = res.getDisplayMetrics().densityDpi;
        if (res.getConfiguration().smallestScreenWidthDp < CalendarColumns.CAL_ACCESS_EDITOR) {
            return density;
        }
        switch (density) {
            case 120:
                return 160;
            case 160:
                return 240;
            case 213:
                return 320;
            case 240:
                return 320;
            case 320:
                return 480;
            case 480:
                return 640;
            default:
                return (int) ((((float) density) * 1.5f) + 0.5f);
        }
    }

    public int getLauncherLargeIconSize() {
        return getLauncherLargeIconSizeInner(this.mContext);
    }

    static int getLauncherLargeIconSizeInner(Context context) {
        Resources res = context.getResources();
        int size = res.getDimensionPixelSize(R.dimen.app_icon_size);
        if (res.getConfiguration().smallestScreenWidthDp < CalendarColumns.CAL_ACCESS_EDITOR) {
            return size;
        }
        switch (res.getDisplayMetrics().densityDpi) {
            case 120:
                return (size * 160) / 120;
            case 160:
                return (size * 240) / 160;
            case 213:
                return (size * 320) / 240;
            case 240:
                return (size * 320) / 240;
            case 320:
                return (size * 480) / 320;
            case 480:
                return ((size * 320) * 2) / 480;
            default:
                return (int) ((((float) size) * 1.5f) + 0.5f);
        }
    }

    public static boolean isUserAMonkey() {
        try {
            return ActivityManagerNative.getDefault().isUserAMonkey();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isRunningInTestHarness() {
        return SystemProperties.getBoolean("ro.test_harness", false);
    }

    public static int checkComponentPermission(String permission, int uid, int owningUid, boolean exported) {
        int appId = UserHandle.getAppId(uid);
        if (appId == 0 || appId == 1000) {
            return 0;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        if (owningUid >= 0 && UserHandle.isSameApp(uid, owningUid)) {
            return 0;
        }
        if (!exported) {
            return -1;
        }
        if (permission == null) {
            return 0;
        }
        if (permission.equals(permission.GRANT_RUNTIME_PERMISSIONS)) {
            int hasOppoComponentPerm = -1;
            try {
                hasOppoComponentPerm = AppGlobals.getPackageManager().checkUidPermission("oppo.permission.OPPO_COMPONENT_SAFE", uid);
            } catch (RemoteException e) {
                Slog.e(TAG, "PackageManager is dead?!?", e);
            }
            if (hasOppoComponentPerm == 0) {
                return 0;
            }
        }
        try {
            return AppGlobals.getPackageManager().checkUidPermission(permission, uid);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public static int checkUidPermission(String permission, int uid) {
        try {
            return AppGlobals.getPackageManager().checkUidPermission(permission, uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) {
        if (UserHandle.getUserId(callingUid) == userId) {
            return userId;
        }
        try {
            return ActivityManagerNative.getDefault().handleIncomingUser(callingPid, callingUid, userId, allowAll, requireFull, name, callerPackage);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getCurrentUser() {
        try {
            UserInfo ui = ActivityManagerNative.getDefault().getCurrentUser();
            return ui != null ? ui.id : 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean switchUser(int userid) {
        try {
            return ActivityManagerNative.getDefault().switchUser(userid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void logoutCurrentUser() {
        int currentUser = getCurrentUser();
        if (currentUser != 0) {
            try {
                ActivityManagerNative.getDefault().switchUser(0);
                ActivityManagerNative.getDefault().stopUser(currentUser, false, null);
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isUserRunning(int userId) {
        try {
            return ActivityManagerNative.getDefault().isUserRunning(userId, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isVrModePackageEnabled(ComponentName component) {
        try {
            return ActivityManagerNative.getDefault().isVrModePackageEnabled(component);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void dumpPackageState(FileDescriptor fd, String packageName) {
        dumpPackageStateStatic(fd, packageName);
    }

    public static void dumpPackageStateStatic(FileDescriptor fd, String packageName) {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        String[] strArr = new String[1];
        strArr[0] = packageName;
        dumpService(pw, fd, "package", strArr);
        pw.println();
        String str = Context.ACTIVITY_SERVICE;
        strArr = new String[3];
        strArr[0] = "-a";
        strArr[1] = "package";
        strArr[2] = packageName;
        dumpService(pw, fd, str, strArr);
        pw.println();
        strArr = new String[3];
        strArr[0] = "--local";
        strArr[1] = "--package";
        strArr[2] = packageName;
        dumpService(pw, fd, "meminfo", strArr);
        pw.println();
        strArr = new String[1];
        strArr[0] = packageName;
        dumpService(pw, fd, "procstats", strArr);
        pw.println();
        str = Context.USAGE_STATS_SERVICE;
        strArr = new String[2];
        strArr[0] = "--packages";
        strArr[1] = packageName;
        dumpService(pw, fd, str, strArr);
        pw.println();
        str = BatteryStats.SERVICE_NAME;
        strArr = new String[1];
        strArr[0] = packageName;
        dumpService(pw, fd, str, strArr);
        pw.flush();
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0040  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void dumpService(PrintWriter pw, FileDescriptor fd, String name, String[] args) {
        Throwable e;
        pw.print("DUMP OF SERVICE ");
        pw.print(name);
        pw.println(":");
        IBinder service = ServiceManager.checkService(name);
        if (service == null) {
            pw.println("  (Service not found)");
            return;
        }
        TransferPipe tp = null;
        try {
            pw.flush();
            TransferPipe tp2 = new TransferPipe();
            try {
                tp2.setBufferPrefix("  ");
                service.dumpAsync(tp2.getWriteFd().getFileDescriptor(), args);
                tp2.go(fd, 10000);
                tp = tp2;
            } catch (Throwable th) {
                e = th;
                tp = tp2;
                if (tp != null) {
                }
                pw.println("Failure dumping service:");
                e.printStackTrace(pw);
            }
        } catch (Throwable th2) {
            e = th2;
            if (tp != null) {
                tp.kill();
            }
            pw.println("Failure dumping service:");
            e.printStackTrace(pw);
        }
    }

    public void setWatchHeapLimit(long pssSize) {
        try {
            ActivityManagerNative.getDefault().setDumpHeapDebugLimit(null, 0, pssSize, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearWatchHeapLimit() {
        try {
            ActivityManagerNative.getDefault().setDumpHeapDebugLimit(null, 0, 0, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startLockTaskMode(int taskId) {
        try {
            ActivityManagerNative.getDefault().startLockTaskMode(taskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void stopLockTaskMode() {
        try {
            ActivityManagerNative.getDefault().stopLockTaskMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInLockTaskMode() {
        return getLockTaskModeState() != 0;
    }

    public int getLockTaskModeState() {
        try {
            return ActivityManagerNative.getDefault().getLockTaskModeState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void setVrThread(int tid) {
        try {
            ActivityManagerNative.getDefault().setVrThread(tid);
        } catch (RemoteException e) {
        }
    }

    public String[] getPackageListFromPid(int pid) {
        try {
            return ActivityManagerNative.getDefault().getPackageListFromPid(pid);
        } catch (RemoteException e) {
            return null;
        }
    }

    public ArrayMap<Integer, ArrayList<Integer>> getProcessesWithAdj() {
        try {
            return ActivityManagerNative.getDefault().getProcessesWithAdj();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setAalMode(int mode) {
        try {
            ActivityManagerNative.getDefault().setAalMode(mode);
        } catch (RemoteException e) {
        }
    }

    public void setAalEnabled(boolean enabled) {
        try {
            ActivityManagerNative.getDefault().setAalEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public ComponentName getTopAppName() {
        ComponentName cn = null;
        try {
            return ActivityManagerNative.getDefault().getTopAppName();
        } catch (RemoteException e) {
            return cn;
        }
    }

    public boolean addDisallowedRunningApp(List<String> appPkgNamesList) {
        if (appPkgNamesList == null || appPkgNamesList.size() <= 0) {
            Slog.w(TAG, "addDisallowedRunningApp:appPkgNamesList is empty!");
            return false;
        }
        try {
            return ActivityManagerNative.getDefault().addDisallowedRunningApp(appPkgNamesList);
        } catch (RemoteException e) {
            Slog.w(TAG, "addDisallowedRunningApp failed.");
            return false;
        }
    }

    public boolean removeDisallowedRunningApp(List<String> appPkgNamesList) {
        if (appPkgNamesList == null || appPkgNamesList.size() <= 0) {
            Slog.w(TAG, "removeDisallowedRunningApp:appPkgNamesList is empty!");
            return false;
        }
        try {
            return ActivityManagerNative.getDefault().removeDisallowedRunningApp(appPkgNamesList);
        } catch (RemoteException e) {
            Slog.w(TAG, "removeDisallowedRunningApp failed.");
            return false;
        }
    }

    public List<String> getDisallowedRunningApp() {
        try {
            return ActivityManagerNative.getDefault().getDisallowedRunningApp();
        } catch (RemoteException e) {
            Slog.w(TAG, "getDisallowedRunningApp failed.");
            return null;
        }
    }

    public void enableSdcard() {
        try {
            ActivityManagerNative.getDefault().allowToUseSdcard(true);
        } catch (RemoteException e) {
            Slog.w(TAG, "enableSdcard failed.");
        }
    }

    public void disableSdcard() {
        try {
            ActivityManagerNative.getDefault().allowToUseSdcard(false);
        } catch (RemoteException e) {
            Slog.w(TAG, "disableSdcard failed.");
        }
    }

    public boolean adjustSplitScreen(int action, int from) {
        boolean result = false;
        try {
            return ActivityManagerNative.getDefault().adjustSplitScreen(action, from);
        } catch (RemoteException e) {
            return result;
        }
    }

    public ComponentName getDockTopAppName() {
        ComponentName cn = null;
        try {
            return ActivityManagerNative.getDefault().getDockTopAppName();
        } catch (RemoteException e) {
            return cn;
        }
    }
}
