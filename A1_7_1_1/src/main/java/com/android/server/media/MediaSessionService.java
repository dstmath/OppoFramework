package com.android.server.media;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IRemoteVolumeController;
import android.media.session.IActiveSessionsListener;
import android.media.session.ISession;
import android.media.session.ISessionCallback;
import android.media.session.ISessionController;
import android.media.session.ISessionManager.Stub;
import android.media.session.MediaSession.Token;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.am.OppoMultiAppManagerUtil;
import com.android.server.oppo.IElsaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
public class MediaSessionService extends SystemService implements Monitor {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_MEDIA_KEY_EVENT = false;
    private static final String TAG = "MediaSessionService";
    private static final int WAKELOCK_TIMEOUT = 5000;
    private String EXP_VERSION_NAME;
    private final ArrayList<MediaSessionRecord> mAllSessions;
    private AudioManagerInternal mAudioManagerInternal;
    private IAudioService mAudioService;
    private ContentResolver mContentResolver;
    private final List<Integer> mCurrentUserIdList;
    private final MessageHandler mHandler;
    final IBinder mICallback;
    private boolean mIsExpVersion;
    private KeyguardManager mKeyguardManager;
    private final Object mLock;
    private final WakeLock mMediaEventWakeLock;
    private final MediaSessionStack mPriorityStack;
    private IRemoteVolumeController mRvc;
    private final SessionManagerImpl mSessionManagerImpl;
    private final ArrayList<SessionsListenerRecord> mSessionsListeners;
    private SettingsObserver mSettingsObserver;
    private final SparseArray<UserRecord> mUserRecords;

    final class MessageHandler extends Handler {
        private static final int MSG_SESSIONS_CHANGED = 1;

        MessageHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MediaSessionService.this.pushSessionsChanged(msg.arg1);
                    return;
                default:
                    return;
            }
        }

        public void post(int what, int arg1, int arg2) {
            obtainMessage(what, arg1, arg2).sendToTarget();
        }
    }

    class SessionManagerImpl extends Stub {
        private static final String EXTRA_WAKELOCK_ACQUIRED = "android.media.AudioService.WAKELOCK_ACQUIRED";
        private static final int WAKELOCK_RELEASE_ON_FINISHED = 1980;
        BroadcastReceiver mKeyEventDone = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        synchronized (MediaSessionService.this.mLock) {
                            if (extras.containsKey(SessionManagerImpl.EXTRA_WAKELOCK_ACQUIRED) && MediaSessionService.this.mMediaEventWakeLock.isHeld()) {
                                MediaSessionService.this.mMediaEventWakeLock.release();
                            }
                        }
                    }
                }
            }
        };
        private KeyEventWakeLockReceiver mKeyEventReceiver = new KeyEventWakeLockReceiver(MediaSessionService.this.mHandler);
        private boolean mVoiceButtonDown = false;
        private boolean mVoiceButtonHandled = false;

        class KeyEventWakeLockReceiver extends ResultReceiver implements Runnable, OnFinished {
            private final Handler mHandler;
            private int mLastTimeoutId = 0;
            private int mRefCount = 0;

            public KeyEventWakeLockReceiver(Handler handler) {
                super(handler);
                this.mHandler = handler;
            }

            public void onTimeout() {
                synchronized (MediaSessionService.this.mLock) {
                    if (this.mRefCount == 0) {
                        return;
                    }
                    this.mLastTimeoutId++;
                    this.mRefCount = 0;
                    releaseWakeLockLocked();
                }
            }

            public void aquireWakeLockLocked() {
                if (this.mRefCount == 0) {
                    MediaSessionService.this.mMediaEventWakeLock.acquire();
                }
                this.mRefCount++;
                this.mHandler.removeCallbacks(this);
                this.mHandler.postDelayed(this, 5000);
            }

            public void run() {
                onTimeout();
            }

            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode >= this.mLastTimeoutId) {
                    synchronized (MediaSessionService.this.mLock) {
                        if (this.mRefCount > 0) {
                            this.mRefCount--;
                            if (this.mRefCount == 0) {
                                releaseWakeLockLocked();
                            }
                        }
                    }
                }
            }

            private void releaseWakeLockLocked() {
                MediaSessionService.this.mMediaEventWakeLock.release();
                this.mHandler.removeCallbacks(this);
            }

            public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
                send(resultCode, null);
            }
        }

        SessionManagerImpl() {
        }

        public ISession createSession(String packageName, ISessionCallback cb, String tag, int userId) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionService.this.enforcePackageName(packageName, uid);
                int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, false, true, "createSession", packageName);
                if (cb == null) {
                    throw new IllegalArgumentException("Controller callback cannot be null");
                }
                ISession sessionBinder = MediaSessionService.this.createSessionInternal(pid, uid, resolvedUserId, packageName, cb, tag).getSessionBinder();
                return sessionBinder;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public List<IBinder> getSessions(ComponentName componentName, int userId) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = verifySessionsRequest(componentName, userId, pid, uid);
                ArrayList<IBinder> binders = new ArrayList();
                synchronized (MediaSessionService.this.mLock) {
                    ArrayList<MediaSessionRecord> records = MediaSessionService.this.mPriorityStack.getActiveSessions(resolvedUserId);
                    int size = records.size();
                    for (int i = 0; i < size; i++) {
                        binders.add(((MediaSessionRecord) records.get(i)).getControllerBinder().asBinder());
                    }
                }
                return binders;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void addSessionsListener(IActiveSessionsListener listener, ComponentName componentName, int userId) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = verifySessionsRequest(componentName, userId, pid, uid);
                synchronized (MediaSessionService.this.mLock) {
                    if (MediaSessionService.this.findIndexOfSessionsListenerLocked(listener) != -1) {
                        Log.w(MediaSessionService.TAG, "ActiveSessionsListener is already added, ignoring");
                    } else {
                        SessionsListenerRecord record = new SessionsListenerRecord(listener, componentName, resolvedUserId, pid, uid);
                        try {
                            listener.asBinder().linkToDeath(record, 0);
                            MediaSessionService.this.mSessionsListeners.add(record);
                            Binder.restoreCallingIdentity(token);
                        } catch (RemoteException e) {
                            Log.e(MediaSessionService.TAG, "ActiveSessionsListener is dead, ignoring it", e);
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void removeSessionsListener(IActiveSessionsListener listener) throws RemoteException {
            synchronized (MediaSessionService.this.mLock) {
                int index = MediaSessionService.this.findIndexOfSessionsListenerLocked(listener);
                if (index != -1) {
                    SessionsListenerRecord record = (SessionsListenerRecord) MediaSessionService.this.mSessionsListeners.remove(index);
                    try {
                        record.mListener.asBinder().unlinkToDeath(record, 0);
                    } catch (Exception e) {
                    }
                }
            }
        }

        public void dispatchMediaKeyEvent(KeyEvent keyEvent, boolean needWakeLock) {
            if (keyEvent == null || !KeyEvent.isMediaKey(keyEvent.getKeyCode())) {
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-media key event.");
                return;
            }
            int flags = keyEvent.getFlags();
            if (keyEvent.getKeyCode() != 79 || (flags & 32) == 0) {
                int pid = Binder.getCallingPid();
                int uid = Binder.getCallingUid();
                long token = Binder.clearCallingIdentity();
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "dispatchMediaKeyEvent, pid=" + pid + ", uid=" + uid + ", event=" + keyEvent);
                }
                if (!isUserSetupComplete()) {
                    Slog.i(MediaSessionService.TAG, "Not dispatching media key event because user setup is in progress.");
                    return;
                } else if (!isGlobalPriorityActive() || uid == 1000) {
                    try {
                        if (!isUserSetupComplete()) {
                            Slog.i(MediaSessionService.TAG, "Not dispatching media key event because user setup is in progress.");
                            Binder.restoreCallingIdentity(token);
                            return;
                        } else if (!isGlobalPriorityActive() || uid == 1000) {
                            synchronized (MediaSessionService.this.mLock) {
                                boolean useNotPlayingSessions = true;
                                for (Integer intValue : MediaSessionService.this.mCurrentUserIdList) {
                                    UserRecord ur = (UserRecord) MediaSessionService.this.mUserRecords.get(intValue.intValue());
                                    if (ur.mLastMediaButtonReceiver == null) {
                                        if (ur.mRestoredMediaButtonReceiver != null) {
                                        }
                                    }
                                    useNotPlayingSessions = false;
                                }
                                if (MediaSessionService.DEBUG) {
                                    Log.d(MediaSessionService.TAG, "dispatchMediaKeyEvent, useNotPlayingSessions=" + useNotPlayingSessions);
                                }
                                MediaSessionRecord session = MediaSessionService.this.mPriorityStack.getDefaultMediaButtonSession(MediaSessionService.this.mCurrentUserIdList, useNotPlayingSessions);
                                if (isVoiceKey(keyEvent.getKeyCode())) {
                                    handleVoiceKeyEventLocked(keyEvent, needWakeLock, session);
                                } else {
                                    dispatchMediaKeyEventLocked(keyEvent, needWakeLock, session);
                                }
                            }
                            Binder.restoreCallingIdentity(token);
                            return;
                        } else {
                            Slog.i(MediaSessionService.TAG, "Only the system can dispatch media key event to the global priority session.");
                            Binder.restoreCallingIdentity(token);
                            return;
                        }
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                } else {
                    Slog.i(MediaSessionService.TAG, "Only the system can dispatch media key event to the global priority session.");
                    Binder.restoreCallingIdentity(token);
                    return;
                }
            }
            Log.d(MediaSessionService.TAG, "Ignore keyEvent=" + keyEvent + "Flags =" + flags);
        }

        public void dispatchAdjustVolume(int suggestedStream, int delta, int flags) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (MediaSessionService.this.mLock) {
                    dispatchAdjustVolumeLocked(suggestedStream, delta, flags, MediaSessionService.this.mPriorityStack.getDefaultVolumeSession(MediaSessionService.this.mCurrentUserIdList));
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setRemoteVolumeController(IRemoteVolumeController rvc) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionService.this.enforceSystemUiPermission("listen for volume changes", pid, uid);
                MediaSessionService.this.mRvc = rvc;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isGlobalPriorityActive() {
            return MediaSessionService.this.mPriorityStack.isGlobalPriorityActive();
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (MediaSessionService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump MediaSessionService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            pw.println("MEDIA SESSION SERVICE (dumpsys media_session)");
            pw.println();
            synchronized (MediaSessionService.this.mLock) {
                int i;
                pw.println(MediaSessionService.this.mSessionsListeners.size() + " sessions listeners.");
                int count = MediaSessionService.this.mAllSessions.size();
                pw.println(count + " Sessions:");
                for (i = 0; i < count; i++) {
                    ((MediaSessionRecord) MediaSessionService.this.mAllSessions.get(i)).dump(pw, IElsaManager.EMPTY_PACKAGE);
                    pw.println();
                }
                MediaSessionService.this.mPriorityStack.dump(pw, IElsaManager.EMPTY_PACKAGE);
                pw.println("User Records:");
                count = MediaSessionService.this.mUserRecords.size();
                for (i = 0; i < count; i++) {
                    ((UserRecord) MediaSessionService.this.mUserRecords.get(MediaSessionService.this.mUserRecords.keyAt(i))).dumpLocked(pw, IElsaManager.EMPTY_PACKAGE);
                }
            }
        }

        private int verifySessionsRequest(ComponentName componentName, int userId, int pid, int uid) {
            String packageName = null;
            if (componentName != null) {
                packageName = componentName.getPackageName();
                MediaSessionService.this.enforcePackageName(packageName, uid);
            }
            int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, true, true, "getSessions", packageName);
            MediaSessionService.this.enforceMediaPermissions(componentName, pid, uid, resolvedUserId);
            return resolvedUserId;
        }

        private void dispatchAdjustVolumeLocked(int suggestedStream, int direction, int flags, MediaSessionRecord session) {
            boolean preferSuggestedStream = false;
            if (isValidLocalStreamType(suggestedStream) && AudioSystem.isStreamActive(suggestedStream, 0)) {
                preferSuggestedStream = true;
            }
            if (MediaSessionService.DEBUG) {
                Log.d(MediaSessionService.TAG, "Adjusting " + session + " by " + direction + ". flags=" + flags + ", suggestedStream=" + suggestedStream + ", preferSuggestedStream=" + preferSuggestedStream);
            }
            if (session != null && !preferSuggestedStream) {
                session.adjustVolume(direction, flags, MediaSessionService.this.getContext().getPackageName(), 1000, true);
            } else if ((flags & 512) == 0 || AudioSystem.isStreamActive(3, 0)) {
                try {
                    int i = direction;
                    int i2 = suggestedStream;
                    int i3 = flags;
                    MediaSessionService.this.mAudioService.adjustSuggestedStreamVolume(i, i2, i3, MediaSessionService.this.getContext().getOpPackageName(), MediaSessionService.TAG);
                } catch (RemoteException e) {
                    Log.e(MediaSessionService.TAG, "Error adjusting default volume.", e);
                }
            } else {
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "No active session to adjust, skipping media only volume event");
                }
            }
        }

        private void handleVoiceKeyEventLocked(KeyEvent keyEvent, boolean needWakeLock, MediaSessionRecord session) {
            if (session == null || !session.hasFlag(DumpState.DUMP_INSTALLS)) {
                int action = keyEvent.getAction();
                boolean isLongPress = (keyEvent.getFlags() & 128) != 0;
                if (action == 0) {
                    if (keyEvent.getRepeatCount() == 0) {
                        this.mVoiceButtonDown = true;
                        this.mVoiceButtonHandled = false;
                    } else if (this.mVoiceButtonDown && !this.mVoiceButtonHandled && isLongPress) {
                        this.mVoiceButtonHandled = true;
                        startVoiceInput(needWakeLock);
                    }
                } else if (action == 1 && this.mVoiceButtonDown) {
                    this.mVoiceButtonDown = false;
                    if (!(this.mVoiceButtonHandled || keyEvent.isCanceled())) {
                        dispatchMediaKeyEventLocked(KeyEvent.changeAction(keyEvent, 0), needWakeLock, session);
                        dispatchMediaKeyEventLocked(keyEvent, needWakeLock, session);
                    }
                }
                return;
            }
            dispatchMediaKeyEventLocked(keyEvent, needWakeLock, session);
        }

        private void dispatchMediaKeyEventLocked(KeyEvent keyEvent, boolean needWakeLock, MediaSessionRecord session) {
            if (session != null) {
                if (MediaSessionService.DEBUG_MEDIA_KEY_EVENT) {
                    Log.d(MediaSessionService.TAG, "Sending " + keyEvent + " to " + session);
                }
                if (needWakeLock) {
                    this.mKeyEventReceiver.aquireWakeLockLocked();
                }
                session.sendMediaButton(keyEvent, needWakeLock ? this.mKeyEventReceiver.mLastTimeoutId : -1, this.mKeyEventReceiver, 1000, MediaSessionService.this.getContext().getPackageName());
            } else {
                for (Integer intValue : MediaSessionService.this.mCurrentUserIdList) {
                    int userId = intValue.intValue();
                    UserRecord user = (UserRecord) MediaSessionService.this.mUserRecords.get(userId);
                    if (user.mLastMediaButtonReceiver == null) {
                        if (user.mRestoredMediaButtonReceiver != null) {
                        }
                    }
                    if (needWakeLock) {
                        this.mKeyEventReceiver.aquireWakeLockLocked();
                    }
                    Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
                    mediaButtonIntent.addFlags(268435456);
                    mediaButtonIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                    try {
                        if (user.mLastMediaButtonReceiver != null) {
                            if (MediaSessionService.DEBUG_MEDIA_KEY_EVENT) {
                                Log.d(MediaSessionService.TAG, "Sending " + keyEvent + " to the last known pendingIntent " + user.mLastMediaButtonReceiver);
                            }
                            user.mLastMediaButtonReceiver.send(MediaSessionService.this.getContext(), needWakeLock ? this.mKeyEventReceiver.mLastTimeoutId : -1, mediaButtonIntent, this.mKeyEventReceiver, MediaSessionService.this.mHandler);
                        } else {
                            if (MediaSessionService.DEBUG_MEDIA_KEY_EVENT) {
                                Log.d(MediaSessionService.TAG, "Sending " + keyEvent + " to the restored intent " + user.mRestoredMediaButtonReceiver);
                            }
                            mediaButtonIntent.setComponent(user.mRestoredMediaButtonReceiver);
                            MediaSessionService.this.getContext().sendBroadcastAsUser(mediaButtonIntent, UserHandle.of(userId));
                        }
                    } catch (CanceledException e) {
                        Log.i(MediaSessionService.TAG, "Error sending key event to media button receiver " + user.mLastMediaButtonReceiver, e);
                    }
                    return;
                }
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "Sending media key ordered broadcast");
                }
                if (needWakeLock) {
                    MediaSessionService.this.mMediaEventWakeLock.acquire();
                }
                Intent keyIntent = new Intent("android.intent.action.MEDIA_BUTTON", null);
                keyIntent.addFlags(268435456);
                keyIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                if (needWakeLock) {
                    keyIntent.putExtra(EXTRA_WAKELOCK_ACQUIRED, WAKELOCK_RELEASE_ON_FINISHED);
                }
                MediaSessionService.this.getContext().sendOrderedBroadcastAsUser(keyIntent, UserHandle.CURRENT, null, this.mKeyEventDone, MediaSessionService.this.mHandler, -1, null, null);
            }
        }

        /* JADX WARNING: Failed to extract finally block: empty outs */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void startVoiceInput(boolean needWakeLock) {
            boolean isLocked;
            Intent voiceIntent;
            boolean z = false;
            PowerManager pm = (PowerManager) MediaSessionService.this.getContext().getSystemService("power");
            if (MediaSessionService.this.mKeyguardManager != null) {
                isLocked = MediaSessionService.this.mKeyguardManager.isKeyguardLocked();
            } else {
                isLocked = false;
            }
            if (isLocked || !pm.isScreenOn()) {
                voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
                String str = "android.speech.extras.EXTRA_SECURE";
                if (isLocked) {
                    z = MediaSessionService.this.mKeyguardManager.isKeyguardSecure();
                }
                voiceIntent.putExtra(str, z);
                Log.i(MediaSessionService.TAG, "voice-based interactions: about to use ACTION_VOICE_SEARCH_HANDS_FREE");
            } else {
                voiceIntent = new Intent("android.speech.action.WEB_SEARCH");
                Log.i(MediaSessionService.TAG, "voice-based interactions: about to use ACTION_WEB_SEARCH");
            }
            if (needWakeLock) {
                MediaSessionService.this.mMediaEventWakeLock.acquire();
            }
            if (voiceIntent != null) {
                try {
                    voiceIntent.setFlags(276824064);
                    if (MediaSessionService.DEBUG) {
                        Log.d(MediaSessionService.TAG, "voiceIntent: " + voiceIntent);
                    }
                    MediaSessionService.this.getContext().startActivityAsUser(voiceIntent, UserHandle.CURRENT);
                } catch (ActivityNotFoundException e) {
                    Log.w(MediaSessionService.TAG, "No activity for search: " + e);
                    if (needWakeLock) {
                        MediaSessionService.this.mMediaEventWakeLock.release();
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    if (needWakeLock) {
                        MediaSessionService.this.mMediaEventWakeLock.release();
                    }
                    throw th;
                }
            }
            if (needWakeLock) {
                MediaSessionService.this.mMediaEventWakeLock.release();
            }
        }

        private boolean isVoiceKey(int keyCode) {
            return keyCode == 79;
        }

        private boolean isUserSetupComplete() {
            return Secure.getIntForUser(MediaSessionService.this.getContext().getContentResolver(), "user_setup_complete", 0, -2) != 0;
        }

        private boolean isValidLocalStreamType(int streamType) {
            if (streamType < 0 || streamType > 5) {
                return false;
            }
            return true;
        }
    }

    final class SessionsListenerRecord implements DeathRecipient {
        private final ComponentName mComponentName;
        private final IActiveSessionsListener mListener;
        private final int mPid;
        private final int mUid;
        private final int mUserId;

        public SessionsListenerRecord(IActiveSessionsListener listener, ComponentName componentName, int userId, int pid, int uid) {
            this.mListener = listener;
            this.mComponentName = componentName;
            this.mUserId = userId;
            this.mPid = pid;
            this.mUid = uid;
        }

        public void binderDied() {
            synchronized (MediaSessionService.this.mLock) {
                MediaSessionService.this.mSessionsListeners.remove(this);
            }
        }
    }

    final class SettingsObserver extends ContentObserver {
        private final Uri mSecureSettingsUri;

        /* synthetic */ SettingsObserver(MediaSessionService this$0, SettingsObserver settingsObserver) {
            this();
        }

        private SettingsObserver() {
            super(null);
            this.mSecureSettingsUri = Secure.getUriFor("enabled_notification_listeners");
        }

        private void observe() {
            MediaSessionService.this.mContentResolver.registerContentObserver(this.mSecureSettingsUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            MediaSessionService.this.updateActiveSessionListeners();
        }
    }

    final class UserRecord {
        private final Context mContext;
        private PendingIntent mLastMediaButtonReceiver;
        private ComponentName mRestoredMediaButtonReceiver;
        private final ArrayList<MediaSessionRecord> mSessions = new ArrayList();
        private final int mUserId;

        public UserRecord(Context context, int userId) {
            this.mContext = context;
            this.mUserId = userId;
            restoreMediaButtonReceiver();
        }

        public void destroyLocked() {
            for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                MediaSessionService.this.destroySessionLocked((MediaSessionRecord) this.mSessions.get(i));
            }
        }

        public ArrayList<MediaSessionRecord> getSessionsLocked() {
            return this.mSessions;
        }

        public void addSessionLocked(MediaSessionRecord session) {
            this.mSessions.add(session);
        }

        public void removeSessionLocked(MediaSessionRecord session) {
            this.mSessions.remove(session);
        }

        public void dumpLocked(PrintWriter pw, String prefix) {
            pw.println(prefix + "Record for user " + this.mUserId);
            String indent = prefix + "  ";
            pw.println(indent + "MediaButtonReceiver:" + this.mLastMediaButtonReceiver);
            pw.println(indent + "Restored ButtonReceiver:" + this.mRestoredMediaButtonReceiver);
            int size = this.mSessions.size();
            pw.println(indent + size + " Sessions:");
            for (int i = 0; i < size; i++) {
                pw.println(indent + ((MediaSessionRecord) this.mSessions.get(i)).toString());
            }
        }

        private void restoreMediaButtonReceiver() {
            String receiverName = Secure.getStringForUser(MediaSessionService.this.mContentResolver, "media_button_receiver", this.mUserId);
            if (!TextUtils.isEmpty(receiverName)) {
                ComponentName eventReceiver = ComponentName.unflattenFromString(receiverName);
                if (eventReceiver != null) {
                    this.mRestoredMediaButtonReceiver = eventReceiver;
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.media.MediaSessionService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.media.MediaSessionService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.media.MediaSessionService.<clinit>():void");
    }

    public MediaSessionService(Context context) {
        super(context);
        this.mICallback = new Binder();
        this.mAllSessions = new ArrayList();
        this.mUserRecords = new SparseArray();
        this.mSessionsListeners = new ArrayList();
        this.mLock = new Object();
        this.mHandler = new MessageHandler();
        this.mCurrentUserIdList = new ArrayList();
        this.EXP_VERSION_NAME = "oppo.version.exp";
        this.mIsExpVersion = false;
        this.mSessionManagerImpl = new SessionManagerImpl();
        this.mPriorityStack = new MediaSessionStack();
        this.mMediaEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "handleMediaEvent");
        this.mIsExpVersion = context.getPackageManager().hasSystemFeature(this.EXP_VERSION_NAME);
    }

    public void onStart() {
        publishBinderService("media_session", this.mSessionManagerImpl);
        Watchdog.getInstance().addMonitor(this);
        this.mKeyguardManager = (KeyguardManager) getContext().getSystemService("keyguard");
        this.mAudioService = getAudioService();
        this.mAudioManagerInternal = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
        this.mContentResolver = getContext().getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this, null);
        this.mSettingsObserver.observe();
        updateUser();
    }

    private IAudioService getAudioService() {
        return IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
    }

    public void updateSession(MediaSessionRecord record) {
        synchronized (this.mLock) {
            if (this.mAllSessions.contains(record)) {
                this.mPriorityStack.onSessionStateChange(record);
                this.mHandler.post(1, record.getUserId(), 0);
                return;
            }
            Log.d(TAG, "Unknown session updated. Ignoring.");
        }
    }

    public void notifyRemoteVolumeChanged(int flags, MediaSessionRecord session) {
        if (this.mRvc != null) {
            try {
                this.mRvc.remoteVolumeChanged(session.getControllerBinder(), flags);
            } catch (Exception e) {
                Log.wtf(TAG, "Error sending volume change to system UI.", e);
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001e, code:
            if (r0 == false) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:12:0x0020, code:
            r5.mHandler.post(1, r6.getUserId(), 0);
     */
    /* JADX WARNING: Missing block: B:13:0x002b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSessionPlaystateChange(MediaSessionRecord record, int oldState, int newState) {
        synchronized (this.mLock) {
            if (this.mAllSessions.contains(record)) {
                boolean updateSessions = this.mPriorityStack.onPlaystateChange(record, oldState, newState);
            } else {
                Log.d(TAG, "Unknown session changed playback state. Ignoring.");
            }
        }
    }

    public void onSessionPlaybackTypeChanged(MediaSessionRecord record) {
        synchronized (this.mLock) {
            if (this.mAllSessions.contains(record)) {
                pushRemoteVolumeUpdateLocked(record.getUserId());
                return;
            }
            Log.d(TAG, "Unknown session changed playback type. Ignoring.");
        }
    }

    public void onStartUser(int userId) {
        if (DEBUG) {
            Log.d(TAG, "onStartUser: " + userId);
        }
        updateUser();
    }

    public void onSwitchUser(int userId) {
        if (DEBUG) {
            Log.d(TAG, "onSwitchUser: " + userId);
        }
        updateUser();
    }

    public void onStopUser(int userId) {
        if (DEBUG) {
            Log.d(TAG, "onStopUser: " + userId);
        }
        synchronized (this.mLock) {
            UserRecord user = (UserRecord) this.mUserRecords.get(userId);
            if (user != null) {
                destroyUserLocked(user);
            }
            updateUser();
        }
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    protected void enforcePhoneStatePermission(int pid, int uid) {
        if (getContext().checkPermission("android.permission.MODIFY_PHONE_STATE", pid, uid) != 0) {
            throw new SecurityException("Must hold the MODIFY_PHONE_STATE permission.");
        }
    }

    void sessionDied(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    void destroySession(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    private void updateUser() {
        synchronized (this.mLock) {
            int userId;
            UserManager manager = (UserManager) getContext().getSystemService("user");
            int currentUser = ActivityManager.getCurrentUser();
            int[] userIds = manager.getProfileIdsWithDisabled(currentUser);
            this.mCurrentUserIdList.clear();
            if (userIds == null || userIds.length <= 0) {
                Log.w(TAG, "Failed to get enabled profiles.");
                this.mCurrentUserIdList.add(Integer.valueOf(currentUser));
            } else {
                for (int userId2 : userIds) {
                    this.mCurrentUserIdList.add(Integer.valueOf(userId2));
                }
            }
            for (Integer intValue : this.mCurrentUserIdList) {
                userId2 = intValue.intValue();
                if (this.mUserRecords.get(userId2) == null) {
                    this.mUserRecords.put(userId2, new UserRecord(getContext(), userId2));
                }
            }
        }
    }

    private void updateActiveSessionListeners() {
        synchronized (this.mLock) {
            for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                SessionsListenerRecord listener = (SessionsListenerRecord) this.mSessionsListeners.get(i);
                try {
                    enforceMediaPermissions(listener.mComponentName, listener.mPid, listener.mUid, listener.mUserId);
                } catch (SecurityException e) {
                    Log.i(TAG, "ActiveSessionsListener " + listener.mComponentName + " is no longer authorized. Disconnecting.");
                    this.mSessionsListeners.remove(i);
                    try {
                        listener.mListener.onActiveSessionsChanged(new ArrayList());
                    } catch (Exception e2) {
                    }
                }
            }
        }
    }

    private void destroyUserLocked(UserRecord user) {
        user.destroyLocked();
        this.mUserRecords.remove(user.mUserId);
    }

    private void destroySessionLocked(MediaSessionRecord session) {
        if (DEBUG) {
            Log.d(TAG, "Destroying " + session);
        }
        UserRecord user = (UserRecord) this.mUserRecords.get(session.getUserId());
        if (user != null) {
            if (session.getMediaButtonReceiver() == user.mLastMediaButtonReceiver) {
                user.mLastMediaButtonReceiver = null;
            }
            ComponentName lRestoredMediaButtonReceiver = user.mRestoredMediaButtonReceiver;
            if (lRestoredMediaButtonReceiver != null && session.getPackageName().equals(lRestoredMediaButtonReceiver.getPackageName())) {
                user.mRestoredMediaButtonReceiver = null;
            }
            user.removeSessionLocked(session);
        }
        this.mPriorityStack.removeSession(session);
        this.mAllSessions.remove(session);
        try {
            session.getCallback().asBinder().unlinkToDeath(session, 0);
        } catch (Exception e) {
        }
        session.onDestroy();
        this.mHandler.post(1, session.getUserId(), 0);
    }

    private void enforcePackageName(String packageName, int uid) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName may not be empty");
        }
        String[] packages = getContext().getPackageManager().getPackagesForUid(uid);
        int packageCount = packages.length;
        int i = 0;
        while (i < packageCount) {
            if (!packageName.equals(packages[i])) {
                i++;
            } else {
                return;
            }
        }
        throw new IllegalArgumentException("packageName is not owned by the calling process");
    }

    private void enforceMediaPermissions(ComponentName compName, int pid, int uid, int resolvedUserId) {
        if (!isCurrentVolumeController(uid) && getContext().checkPermission("android.permission.MEDIA_CONTENT_CONTROL", pid, uid) != 0 && !isEnabledNotificationListener(compName, UserHandle.getUserId(uid), resolvedUserId)) {
            throw new SecurityException("Missing permission to control media.");
        }
    }

    private boolean isCurrentVolumeController(int uid) {
        if (this.mAudioManagerInternal != null) {
            int vcuid = this.mAudioManagerInternal.getVolumeControllerUid();
            if (vcuid > 0 && uid == vcuid) {
                return true;
            }
        }
        return false;
    }

    private void enforceSystemUiPermission(String action, int pid, int uid) {
        if (!isCurrentVolumeController(uid) && getContext().checkPermission("android.permission.STATUS_BAR_SERVICE", pid, uid) != 0) {
            throw new SecurityException("Only system ui may " + action);
        }
    }

    private boolean isEnabledNotificationListener(ComponentName compName, int userId, int forUserId) {
        if (userId != forUserId) {
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "Checking if enabled notification listener " + compName);
        }
        if (compName != null) {
            String enabledNotifListeners = Secure.getStringForUser(this.mContentResolver, "enabled_notification_listeners", userId);
            if (enabledNotifListeners != null) {
                String[] components = enabledNotifListeners.split(":");
                int i = 0;
                while (i < components.length) {
                    ComponentName component = ComponentName.unflattenFromString(components[i]);
                    if (component == null || !compName.equals(component)) {
                        i++;
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "ok to get sessions. " + component + " is authorized notification listener");
                        }
                        return true;
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "not ok to get sessions. " + compName + " is not in list of ENABLED_NOTIFICATION_LISTENERS for user " + userId);
            }
        }
        return false;
    }

    private MediaSessionRecord createSessionInternal(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) throws RemoteException {
        MediaSessionRecord createSessionLocked;
        synchronized (this.mLock) {
            createSessionLocked = createSessionLocked(callerPid, callerUid, userId, callerPackageName, cb, tag);
        }
        return createSessionLocked;
    }

    private MediaSessionRecord createSessionLocked(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) {
        if (this.mIsExpVersion && userId == OppoMultiAppManager.USER_ID && OppoMultiAppManagerUtil.getInstance().isMultiAllowedApp(callerPackageName)) {
            userId = 0;
        }
        UserRecord user = (UserRecord) this.mUserRecords.get(userId);
        if (user == null) {
            Log.wtf(TAG, "Request from invalid user: " + userId);
            throw new RuntimeException("Session request from invalid user.");
        }
        MediaSessionRecord session = new MediaSessionRecord(callerPid, callerUid, userId, callerPackageName, cb, tag, this, this.mHandler);
        try {
            cb.asBinder().linkToDeath(session, 0);
            this.mAllSessions.add(session);
            this.mPriorityStack.addSession(session, this.mCurrentUserIdList.contains(Integer.valueOf(userId)));
            user.addSessionLocked(session);
            this.mHandler.post(1, userId, 0);
            if (DEBUG) {
                Log.d(TAG, "Created session for " + callerPackageName + " with tag " + tag);
            }
            return session;
        } catch (RemoteException e) {
            throw new RuntimeException("Media Session owner died prematurely.", e);
        }
    }

    private int findIndexOfSessionsListenerLocked(IActiveSessionsListener listener) {
        for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
            if (((SessionsListenerRecord) this.mSessionsListeners.get(i)).mListener.asBinder() == listener.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    private void pushSessionsChanged(int userId) {
        synchronized (this.mLock) {
            int i;
            List<MediaSessionRecord> records = this.mPriorityStack.getActiveSessions(userId);
            int size = records.size();
            if (size > 0 && ((MediaSessionRecord) records.get(0)).isPlaybackActive(false)) {
                rememberMediaButtonReceiverLocked((MediaSessionRecord) records.get(0));
            }
            ArrayList<Token> tokens = new ArrayList();
            for (i = 0; i < size; i++) {
                tokens.add(new Token(((MediaSessionRecord) records.get(i)).getControllerBinder()));
            }
            pushRemoteVolumeUpdateLocked(userId);
            for (i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                SessionsListenerRecord record = (SessionsListenerRecord) this.mSessionsListeners.get(i);
                if (record.mUserId == -1 || record.mUserId == userId) {
                    try {
                        record.mListener.onActiveSessionsChanged(tokens);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Dead ActiveSessionsListener in pushSessionsChanged, removing", e);
                        this.mSessionsListeners.remove(i);
                    }
                }
            }
        }
    }

    private void pushRemoteVolumeUpdateLocked(int userId) {
        ISessionController iSessionController = null;
        if (this.mRvc != null) {
            try {
                MediaSessionRecord record = this.mPriorityStack.getDefaultRemoteSession(userId);
                IRemoteVolumeController iRemoteVolumeController = this.mRvc;
                if (record != null) {
                    iSessionController = record.getControllerBinder();
                }
                iRemoteVolumeController.updateRemoteController(iSessionController);
            } catch (RemoteException e) {
                Log.e(TAG, "Error sending default remote volume to sys ui.", e);
            }
        }
    }

    private void rememberMediaButtonReceiverLocked(MediaSessionRecord record) {
        PendingIntent receiver = record.getMediaButtonReceiver();
        UserRecord user = (UserRecord) this.mUserRecords.get(record.getUserId());
        if (receiver != null && user != null) {
            user.mLastMediaButtonReceiver = receiver;
            ComponentName component = receiver.getIntent().getComponent();
            if (component != null && record.getPackageName().equals(component.getPackageName())) {
                Secure.putStringForUser(this.mContentResolver, "media_button_receiver", component.flattenToString(), record.getUserId());
            }
        }
    }
}
