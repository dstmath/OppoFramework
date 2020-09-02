package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class TaskInfo {
    private static final String TAG = "TaskInfo";
    public ComponentName baseActivity;
    public Intent baseIntent;
    @UnsupportedAppUsage
    public final Configuration configuration = new Configuration();
    public int displayId;
    public boolean isRunning;
    @UnsupportedAppUsage
    public long lastActiveTime;
    public int numActivities;
    public ComponentName origActivity;
    public ComponentName realActivity;
    @UnsupportedAppUsage
    public int resizeMode;
    @UnsupportedAppUsage
    public int stackId;
    @UnsupportedAppUsage
    public boolean supportsSplitScreenMultiWindow;
    public ActivityManager.TaskDescription taskDescription;
    public int taskId;
    public ComponentName topActivity;
    @UnsupportedAppUsage
    public int userId;

    TaskInfo() {
    }

    private TaskInfo(Parcel source) {
        readFromParcel(source);
    }

    public ActivityManager.TaskSnapshot getTaskSnapshot(boolean reducedResolution) {
        try {
            return ActivityManager.getService().getTaskSnapshot(this.taskId, reducedResolution);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get task snapshot, taskId=" + this.taskId, e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void readFromParcel(Parcel source) {
        Intent intent;
        this.userId = source.readInt();
        this.stackId = source.readInt();
        this.taskId = source.readInt();
        this.displayId = source.readInt();
        this.isRunning = source.readBoolean();
        ActivityManager.TaskDescription taskDescription2 = null;
        if (source.readInt() != 0) {
            intent = Intent.CREATOR.createFromParcel(source);
        } else {
            intent = null;
        }
        this.baseIntent = intent;
        this.baseActivity = ComponentName.readFromParcel(source);
        this.topActivity = ComponentName.readFromParcel(source);
        this.origActivity = ComponentName.readFromParcel(source);
        this.realActivity = ComponentName.readFromParcel(source);
        this.numActivities = source.readInt();
        this.lastActiveTime = source.readLong();
        if (source.readInt() != 0) {
            taskDescription2 = ActivityManager.TaskDescription.CREATOR.createFromParcel(source);
        }
        this.taskDescription = taskDescription2;
        this.supportsSplitScreenMultiWindow = source.readBoolean();
        this.resizeMode = source.readInt();
        this.configuration.readFromParcel(source);
    }

    /* access modifiers changed from: package-private */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeInt(this.stackId);
        dest.writeInt(this.taskId);
        dest.writeInt(this.displayId);
        dest.writeBoolean(this.isRunning);
        if (this.baseIntent != null) {
            dest.writeInt(1);
            this.baseIntent.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        ComponentName.writeToParcel(this.baseActivity, dest);
        ComponentName.writeToParcel(this.topActivity, dest);
        ComponentName.writeToParcel(this.origActivity, dest);
        ComponentName.writeToParcel(this.realActivity, dest);
        dest.writeInt(this.numActivities);
        dest.writeLong(this.lastActiveTime);
        if (this.taskDescription != null) {
            dest.writeInt(1);
            this.taskDescription.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeBoolean(this.supportsSplitScreenMultiWindow);
        dest.writeInt(this.resizeMode);
        this.configuration.writeToParcel(dest, flags);
    }

    public String toString() {
        return "TaskInfo{userId=" + this.userId + " stackId=" + this.stackId + " taskId=" + this.taskId + " displayId=" + this.displayId + " isRunning=" + this.isRunning + " baseIntent=" + this.baseIntent + " baseActivity=" + this.baseActivity + " topActivity=" + this.topActivity + " origActivity=" + this.origActivity + " realActivity=" + this.realActivity + " numActivities=" + this.numActivities + " lastActiveTime=" + this.lastActiveTime + " supportsSplitScreenMultiWindow=" + this.supportsSplitScreenMultiWindow + " resizeMode=" + this.resizeMode;
    }
}
