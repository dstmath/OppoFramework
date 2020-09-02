package com.color.inner.app;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.List;

public class NotificationManagerWrapper {
    private static final String TAG = "NotificationManagerWrapper";

    public static boolean areNotificationsEnabledForPackage(String pkg, int uid) {
        try {
            return NotificationManager.getService().areNotificationsEnabledForPackage(pkg, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "areNotificationsEnabledForPackage failed.");
            return false;
        }
    }

    public static NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) {
        try {
            return NotificationManager.getService().getNotificationChannelForPackage(pkg, uid, channelId, includeDeleted);
        } catch (RemoteException e) {
            Log.w(TAG, "getNotificationChannelForPackage failed.");
            return null;
        }
    }

    public static StatusBarNotification[] getActiveNotifications(String callingPkg) {
        try {
            return NotificationManager.getService().getActiveNotifications(callingPkg);
        } catch (RemoteException e) {
            Log.w(TAG, "getActiveNotifications failed.");
            return null;
        }
    }

    public static List<NotificationChannelGroup> getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) {
        try {
            ParceledListSlice<NotificationChannelGroup> mGroup = NotificationManager.getService().getNotificationChannelGroupsForPackage(pkg, uid, includeDeleted);
            if (mGroup != null) {
                return mGroup.getList();
            }
            return null;
        } catch (RemoteException e) {
            Log.w(TAG, "getNotificationChannelGroupsForPackage failed.");
            return null;
        }
    }

    public static List<NotificationChannel> getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) {
        try {
            ParceledListSlice<NotificationChannel> mGroup = NotificationManager.getService().getNotificationChannelsForPackage(pkg, uid, includeDeleted);
            if (mGroup != null) {
                return mGroup.getList();
            }
            return null;
        } catch (RemoteException e) {
            Log.w(TAG, "getNotificationChannelsForPackage failed.");
            return null;
        }
    }

    public static void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
        try {
            NotificationManager.getService().setNotificationsEnabledForPackage(pkg, uid, enabled);
        } catch (RemoteException e) {
            Log.w(TAG, "setNotificationsEnabledForPackage failed.");
        }
    }

    public static void setNotificationListenerAccessGranted(ComponentName listener, boolean granted) {
        try {
            NotificationManager.getService().setNotificationListenerAccessGranted(listener, granted);
        } catch (RemoteException e) {
            Log.e(TAG, "setNotificationListenerAccessGranted failed.");
        }
    }

    public static void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) {
        try {
            NotificationManager.getService().updateNotificationChannelForPackage(pkg, uid, channel);
        } catch (RemoteException e) {
            Log.w(TAG, "updateNotificationChannelForPackage failed.");
        }
    }

    public static void updateNotificationChannelGroupForPackage(String pkg, int uid, NotificationChannelGroup group) {
        try {
            NotificationManager.getService().updateNotificationChannelGroupForPackage(pkg, uid, group);
        } catch (RemoteException e) {
            Log.w(TAG, "updateNotificationChannelGroupForPackage failed.");
        }
    }

    public static boolean onlyHasDefaultChannel(String pkg, int uid) {
        try {
            return NotificationManager.getService().onlyHasDefaultChannel(pkg, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "onlyHasDefaultChannel failed.");
            return false;
        }
    }

    public static int getDeletedChannelCount(String pkg, int uid) {
        try {
            return NotificationManager.getService().getDeletedChannelCount(pkg, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "getDeletedChannelCount failed.");
            return -1;
        }
    }

    public static List<NotificationChannelGroup> getNotificationChannelGroups(String pkg) {
        try {
            ParceledListSlice<NotificationChannelGroup> mGroup = NotificationManager.getService().getNotificationChannelGroups(pkg);
            if (mGroup != null) {
                return mGroup.getList();
            }
            return null;
        } catch (RemoteException e) {
            Log.w(TAG, "getNotificationChannelGroups failed.");
            return null;
        }
    }

    public static NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) {
        try {
            return NotificationManager.getService().getNotificationChannelGroupForPackage(groupId, pkg, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "getNotificationChannelGroupForPackage failed.");
            return null;
        }
    }

    public static void createNotificationChannelsForPackage(String pkg, int uid, List channelsList) {
        try {
            NotificationManager.getService().createNotificationChannelsForPackage(pkg, uid, new ParceledListSlice(channelsList));
        } catch (RemoteException e) {
            Log.w(TAG, "createNotificationChannelsForPackage failed.");
        }
    }

    public static void createNotificationChannelGroups(String pkg, List channelGroupList) {
        try {
            NotificationManager.getService().createNotificationChannelGroups(pkg, new ParceledListSlice(channelGroupList));
        } catch (RemoteException e) {
            Log.w(TAG, "createNotificationChannelGroups failed.");
        }
    }

    public static void cancelAllNotifications(String pkg, int userId) {
        try {
            NotificationManager.getService().cancelAllNotifications(pkg, userId);
        } catch (RemoteException e) {
            Log.w(TAG, "cancelAllNotifications failed.");
        }
    }
}
