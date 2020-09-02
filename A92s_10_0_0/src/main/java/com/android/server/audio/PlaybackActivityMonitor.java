package com.android.server.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioSystem;
import android.media.IPlaybackConfigDispatcher;
import android.media.PlayerBase;
import android.media.VolumeShaper;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.server.audio.AudioEventLogger;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.slice.SliceClientPermissions;
import com.oppo.atlas.OppoAtlasManager;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class PlaybackActivityMonitor implements AudioPlaybackConfiguration.PlayerDeathMonitor, PlayerFocusEnforcer {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = ("eng".equals(Build.TYPE) || "userdebug".equals(Build.TYPE));
    /* access modifiers changed from: private */
    public static final VolumeShaper.Configuration DUCK_ID = new VolumeShaper.Configuration(1);
    /* access modifiers changed from: private */
    public static final VolumeShaper.Configuration DUCK_VSHAPE = new VolumeShaper.Configuration.Builder().setId(1).setCurve(new float[]{OppoBrightUtils.MIN_LUX_LIMITI, 1.0f}, new float[]{1.0f, 0.2f}).setOptionFlags(2).setDuration((long) MediaFocusControl.getFocusRampTimeMs(3, new AudioAttributes.Builder().setUsage(5).build())).build();
    private static final int FLAGS_FOR_SILENCE_OVERRIDE = 192;
    /* access modifiers changed from: private */
    public static final VolumeShaper.Operation PLAY_CREATE_IF_NEEDED = new VolumeShaper.Operation.Builder(VolumeShaper.Operation.PLAY).createIfNeeded().build();
    /* access modifiers changed from: private */
    public static final VolumeShaper.Operation PLAY_SKIP_RAMP = new VolumeShaper.Operation.Builder(PLAY_CREATE_IF_NEEDED).setXOffset(1.0f).build();
    public static final String TAG = "AudioService.PlaybackActivityMonitor";
    private static final int[] UNDUCKABLE_PLAYER_TYPES = {13, 3};
    private static final int VOLUME_SHAPER_SYSTEM_DUCK_ID = 1;
    /* access modifiers changed from: private */
    public static final AudioEventLogger sEventLogger = new AudioEventLogger(100, "playback activity as reported through PlayerBase");
    private final ArrayList<Integer> mBannedUids = new ArrayList<>();
    private final ArrayList<PlayMonitorClient> mClients = new ArrayList<>();
    private final Context mContext;
    private final DuckingManager mDuckingManager = new DuckingManager();
    private boolean mHasPublicClients = false;
    private final int mMaxAlarmVolume;
    private final ArrayList<Integer> mMutedPlayers = new ArrayList<>();
    private final Object mPlayerLock = new Object();
    private final HashMap<Integer, AudioPlaybackConfiguration> mPlayers = new HashMap<>();
    private int mPrivilegedAlarmActiveCount = 0;
    private int mSavedAlarmVolume = -1;

    PlaybackActivityMonitor(Context context, int maxAlarmVolume) {
        this.mContext = context;
        this.mMaxAlarmVolume = maxAlarmVolume;
        PlayMonitorClient.sListenerDeathMonitor = this;
        AudioPlaybackConfiguration.sPlayerDeathMonitor = this;
    }

    public void disableAudioForUid(boolean disable, int uid) {
        synchronized (this.mPlayerLock) {
            int index = this.mBannedUids.indexOf(new Integer(uid));
            if (index >= 0) {
                if (!disable) {
                    if (DEBUG) {
                        AudioEventLogger audioEventLogger = sEventLogger;
                        audioEventLogger.log(new AudioEventLogger.StringEvent("unbanning uid:" + uid));
                    }
                    this.mBannedUids.remove(index);
                }
            } else if (disable) {
                for (AudioPlaybackConfiguration apc : this.mPlayers.values()) {
                    checkBanPlayer(apc, uid);
                }
                if (DEBUG) {
                    AudioEventLogger audioEventLogger2 = sEventLogger;
                    audioEventLogger2.log(new AudioEventLogger.StringEvent("banning uid:" + uid));
                }
                this.mBannedUids.add(new Integer(uid));
            }
        }
    }

    private boolean checkBanPlayer(AudioPlaybackConfiguration apc, int uid) {
        boolean toBan = apc.getClientUid() == uid;
        if (toBan) {
            int piid = apc.getPlayerInterfaceId();
            try {
                Log.v(TAG, "banning player " + piid + " uid:" + uid);
                apc.getPlayerProxy().pause();
            } catch (Exception e) {
                Log.e(TAG, "error banning player " + piid + " uid:" + uid, e);
            }
        }
        return toBan;
    }

    public int trackPlayer(PlayerBase.PlayerIdCard pic) {
        int newPiid = AudioSystem.newAudioPlayerId();
        if (DEBUG) {
            Log.v(TAG, "trackPlayer() new piid=" + newPiid);
        }
        AudioPlaybackConfiguration apc = new AudioPlaybackConfiguration(pic, newPiid, Binder.getCallingUid(), Binder.getCallingPid());
        apc.init();
        sEventLogger.log(new NewPlayerEvent(apc));
        if (pic.mPlayerType == 12) {
            String value = OppoAtlasManager.getInstance((Context) null).getParameters("check_listinfo_bypid=not-addplayerbase=" + Binder.getCallingPid());
            if (value != null && value.equals(TemperatureProvider.SWITCH_ON)) {
                Log.d(TAG, "do not add piid to mplayer piid = " + newPiid);
                return newPiid;
            }
        }
        synchronized (this.mPlayerLock) {
            this.mPlayers.put(Integer.valueOf(newPiid), apc);
        }
        return newPiid;
    }

    public void playerAttributes(int piid, AudioAttributes attr, int binderUid) {
        boolean change;
        synchronized (this.mPlayerLock) {
            AudioPlaybackConfiguration apc = this.mPlayers.get(new Integer(piid));
            if (checkConfigurationCaller(piid, apc, binderUid)) {
                sEventLogger.log(new AudioAttrEvent(piid, attr));
                change = apc.handleAudioAttributesEvent(attr);
            } else {
                Log.e(TAG, "Error updating audio attributes");
                change = false;
            }
        }
        if (change) {
            dispatchPlaybackChange(false);
        }
    }

    private void checkVolumeForPrivilegedAlarm(AudioPlaybackConfiguration apc, int event) {
        if ((event != 2 && apc.getPlayerState() != 2) || (apc.getAudioAttributes().getAllFlags() & FLAGS_FOR_SILENCE_OVERRIDE) != FLAGS_FOR_SILENCE_OVERRIDE || apc.getAudioAttributes().getUsage() != 4 || this.mContext.checkPermission("android.permission.MODIFY_PHONE_STATE", apc.getClientPid(), apc.getClientUid()) != 0) {
            return;
        }
        if (event == 2 && apc.getPlayerState() != 2) {
            int i = this.mPrivilegedAlarmActiveCount;
            this.mPrivilegedAlarmActiveCount = i + 1;
            if (i == 0) {
                this.mSavedAlarmVolume = AudioSystem.getStreamVolumeIndex(4, 2);
                AudioSystem.setStreamVolumeIndexAS(4, this.mMaxAlarmVolume, 2);
            }
        } else if (event != 2 && apc.getPlayerState() == 2) {
            int i2 = this.mPrivilegedAlarmActiveCount - 1;
            this.mPrivilegedAlarmActiveCount = i2;
            if (i2 == 0 && AudioSystem.getStreamVolumeIndex(4, 2) == this.mMaxAlarmVolume) {
                AudioSystem.setStreamVolumeIndexAS(4, this.mSavedAlarmVolume, 2);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x009f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d9, code lost:
        if (r5 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00db, code lost:
        if (r10 != 0) goto L_0x00de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00de, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00df, code lost:
        dispatchPlaybackChange(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        return;
     */
    public void playerEvent(int piid, int event, int binderUid) {
        boolean change;
        boolean z = true;
        if (DEBUG) {
            Log.v(TAG, String.format("playerEvent(piid=%d, event=%d)", Integer.valueOf(piid), Integer.valueOf(event)));
        }
        synchronized (this.mPlayerLock) {
            AudioPlaybackConfiguration apc = this.mPlayers.get(new Integer(piid));
            if (apc != null) {
                sEventLogger.log(new PlayerEvent(piid, event));
                if (event == 2) {
                    Iterator<Integer> it = this.mBannedUids.iterator();
                    while (it.hasNext()) {
                        if (checkBanPlayer(apc, it.next().intValue())) {
                            sEventLogger.log(new AudioEventLogger.StringEvent("not starting piid:" + piid + " ,is banned"));
                            if (DEBUG) {
                                Log.w(TAG, "not starting pid:" + piid + " ,is banned");
                            }
                        }
                    }
                }
                if (apc.getPlayerType() != 3) {
                    if (checkConfigurationCaller(piid, apc, binderUid)) {
                        checkVolumeForPrivilegedAlarm(apc, event);
                        change = apc.handleStateEvent(event);
                    } else {
                        Log.e(TAG, "Error handling event " + event);
                        change = false;
                    }
                    if (change && event == 2) {
                        this.mDuckingManager.checkDuck(apc);
                    }
                }
            }
        }
    }

    public void playerHasOpPlayAudio(int piid, boolean hasOpPlayAudio, int binderUid) {
        sEventLogger.log(new PlayerOpPlayAudioEvent(piid, hasOpPlayAudio, binderUid));
    }

    public void releasePlayer(int piid, int binderUid) {
        if (DEBUG) {
            Log.v(TAG, "releasePlayer() for piid=" + piid);
        }
        boolean change = false;
        synchronized (this.mPlayerLock) {
            AudioPlaybackConfiguration apc = this.mPlayers.get(new Integer(piid));
            if (checkConfigurationCaller(piid, apc, binderUid)) {
                AudioEventLogger audioEventLogger = sEventLogger;
                audioEventLogger.log(new AudioEventLogger.StringEvent("releasing player piid:" + piid));
                this.mPlayers.remove(new Integer(piid));
                this.mDuckingManager.removeReleased(apc);
                checkVolumeForPrivilegedAlarm(apc, 0);
                change = apc.handleStateEvent(0);
            }
        }
        if (change) {
            dispatchPlaybackChange(true);
        }
    }

    public void playerDeath(int piid) {
        releasePlayer(piid, 0);
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("\nPlaybackActivityMonitor dump time: " + DateFormat.getTimeInstance().format(new Date()));
        synchronized (this.mPlayerLock) {
            pw.println("\n  playback listeners:");
            synchronized (this.mClients) {
                Iterator<PlayMonitorClient> it = this.mClients.iterator();
                while (it.hasNext()) {
                    PlayMonitorClient pmc = it.next();
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringUtils.SPACE);
                    sb.append(pmc.mIsPrivileged ? "(S)" : "(P)");
                    sb.append(pmc.toString());
                    pw.print(sb.toString());
                }
            }
            pw.println(StringUtils.LF);
            pw.println("\n  players:");
            List<Integer> piidIntList = new ArrayList<>(this.mPlayers.keySet());
            Collections.sort(piidIntList);
            for (Integer piidInt : piidIntList) {
                AudioPlaybackConfiguration apc = this.mPlayers.get(piidInt);
                if (apc != null) {
                    apc.dump(pw);
                }
            }
            pw.println("\n  ducked players piids:");
            this.mDuckingManager.dump(pw);
            pw.print("\n  muted player piids:");
            Iterator<Integer> it2 = this.mMutedPlayers.iterator();
            while (it2.hasNext()) {
                int piid = it2.next().intValue();
                pw.print(StringUtils.SPACE + piid);
            }
            pw.println();
            pw.print("\n  banned uids:");
            Iterator<Integer> it3 = this.mBannedUids.iterator();
            while (it3.hasNext()) {
                int uid = it3.next().intValue();
                pw.print(StringUtils.SPACE + uid);
            }
            pw.println(StringUtils.LF);
            sEventLogger.dump(pw);
        }
    }

    private static boolean checkConfigurationCaller(int piid, AudioPlaybackConfiguration apc, int binderUid) {
        if (apc == null) {
            return false;
        }
        if (binderUid == 0 || apc.getClientUid() == binderUid) {
            return true;
        }
        Log.e(TAG, "Forbidden operation from uid " + binderUid + " for player " + piid);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        android.util.Log.v(com.android.server.audio.PlaybackActivityMonitor.TAG, "dispatchPlaybackChange to " + r9.mClients.size() + " clients");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0033, code lost:
        r1 = r9.mPlayerLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003c, code lost:
        if (r9.mPlayers.isEmpty() == false) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003e, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0040, code lost:
        r0 = new java.util.ArrayList<>(r9.mPlayers.values());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004b, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004c, code lost:
        r2 = r9.mClients;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        if (r9.mClients.isEmpty() == false) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0058, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005b, code lost:
        if (r9.mHasPublicClients == false) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005d, code lost:
        r1 = anonymizeForPublicConsumption(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0062, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0063, code lost:
        r3 = r9.mClients.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006d, code lost:
        if (r3.hasNext() == false) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006f, code lost:
        r4 = r3.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0078, code lost:
        if (r4.mErrorCount >= 5) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x007c, code lost:
        if (r4.mIsPrivileged == false) goto L_0x0084;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007e, code lost:
        r4.mDispatcherCb.dispatchPlaybackConfigChange(r0, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0084, code lost:
        r4.mDispatcherCb.dispatchPlaybackConfigChange(r1, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x008c, code lost:
        r4.mErrorCount++;
        android.util.Log.e(com.android.server.audio.PlaybackActivityMonitor.TAG, "Error (" + r4.mErrorCount + ") trying to dispatch playback config change to " + r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b3, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b4, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        if (com.android.server.audio.PlaybackActivityMonitor.DEBUG == false) goto L_0x0033;
     */
    private void dispatchPlaybackChange(boolean iplayerReleased) {
        synchronized (this.mClients) {
            if (this.mClients.isEmpty()) {
            }
        }
    }

    private ArrayList<AudioPlaybackConfiguration> anonymizeForPublicConsumption(List<AudioPlaybackConfiguration> sysConfigs) {
        ArrayList<AudioPlaybackConfiguration> publicConfigs = new ArrayList<>();
        for (AudioPlaybackConfiguration config : sysConfigs) {
            if (config.isActive()) {
                publicConfigs.add(AudioPlaybackConfiguration.anonymizedCopy(config));
            }
        }
        return publicConfigs;
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public boolean duckPlayers(FocusRequester winner, FocusRequester loser, boolean forceDuck) {
        if (DEBUG) {
            Log.v(TAG, String.format("duckPlayers: uids winner=%d loser=%d", Integer.valueOf(winner.getClientUid()), Integer.valueOf(loser.getClientUid())));
        }
        synchronized (this.mPlayerLock) {
            if (this.mPlayers.isEmpty()) {
                return true;
            }
            ArrayList<AudioPlaybackConfiguration> apcsToDuck = new ArrayList<>();
            for (AudioPlaybackConfiguration apc : this.mPlayers.values()) {
                if (!winner.hasSameUid(apc.getClientUid()) && loser.hasSameUid(apc.getClientUid()) && apc.getPlayerState() == 2) {
                    if (!forceDuck && apc.getAudioAttributes().getContentType() == 1) {
                        Log.v(TAG, "not ducking player " + apc.getPlayerInterfaceId() + " uid:" + apc.getClientUid() + " pid:" + apc.getClientPid() + " - SPEECH");
                        return false;
                    } else if (ArrayUtils.contains(UNDUCKABLE_PLAYER_TYPES, apc.getPlayerType())) {
                        Log.v(TAG, "not ducking player " + apc.getPlayerInterfaceId() + " uid:" + apc.getClientUid() + " pid:" + apc.getClientPid() + " due to type:" + AudioPlaybackConfiguration.toLogFriendlyPlayerType(apc.getPlayerType()));
                        return false;
                    } else {
                        apcsToDuck.add(apc);
                    }
                }
            }
            this.mDuckingManager.duckUid(loser.getClientUid(), apcsToDuck);
            return true;
        }
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public void unduckPlayers(FocusRequester winner) {
        if (DEBUG) {
            Log.v(TAG, "unduckPlayers: uids winner=" + winner.getClientUid());
        }
        synchronized (this.mPlayerLock) {
            this.mDuckingManager.unduckUid(winner.getClientUid(), this.mPlayers);
        }
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public void mutePlayersForCall(int[] usagesToMute) {
        if (DEBUG) {
            String log = new String("mutePlayersForCall: usages=");
            for (int usage : usagesToMute) {
                log = log + StringUtils.SPACE + usage;
            }
            Log.v(TAG, log);
        }
        synchronized (this.mPlayerLock) {
            for (Integer piid : this.mPlayers.keySet()) {
                AudioPlaybackConfiguration apc = this.mPlayers.get(piid);
                if (apc != null) {
                    int playerUsage = apc.getAudioAttributes().getUsage();
                    boolean mute = false;
                    int length = usagesToMute.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        } else if (playerUsage == usagesToMute[i]) {
                            mute = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (mute) {
                        try {
                            sEventLogger.log(new AudioEventLogger.StringEvent("call: muting piid:" + piid + " uid:" + apc.getClientUid()).printLog(TAG));
                            apc.getPlayerProxy().setVolume((float) OppoBrightUtils.MIN_LUX_LIMITI);
                            this.mMutedPlayers.add(new Integer(piid.intValue()));
                        } catch (Exception e) {
                            Log.e(TAG, "call: error muting player " + piid, e);
                        }
                    }
                }
            }
        }
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public void unmutePlayersForCall() {
        if (DEBUG) {
            Log.v(TAG, "unmutePlayersForCall()");
        }
        synchronized (this.mPlayerLock) {
            if (!this.mMutedPlayers.isEmpty()) {
                Iterator<Integer> it = this.mMutedPlayers.iterator();
                while (it.hasNext()) {
                    int piid = it.next().intValue();
                    AudioPlaybackConfiguration apc = this.mPlayers.get(Integer.valueOf(piid));
                    if (apc != null) {
                        try {
                            AudioEventLogger audioEventLogger = sEventLogger;
                            audioEventLogger.log(new AudioEventLogger.StringEvent("call: unmuting piid:" + piid).printLog(TAG));
                            apc.getPlayerProxy().setVolume(1.0f);
                        } catch (Exception e) {
                            Log.e(TAG, "call: error unmuting player " + piid + " uid:" + apc.getClientUid(), e);
                        }
                    }
                }
                this.mMutedPlayers.clear();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerPlaybackCallback(IPlaybackConfigDispatcher pcdb, boolean isPrivileged) {
        if (pcdb != null) {
            synchronized (this.mClients) {
                PlayMonitorClient pmc = new PlayMonitorClient(pcdb, isPrivileged);
                if (pmc.init()) {
                    if (!isPrivileged) {
                        this.mHasPublicClients = true;
                    }
                    this.mClients.add(pmc);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterPlaybackCallback(IPlaybackConfigDispatcher pcdb) {
        if (pcdb != null) {
            synchronized (this.mClients) {
                Iterator<PlayMonitorClient> clientIterator = this.mClients.iterator();
                boolean hasPublicClients = false;
                while (clientIterator.hasNext()) {
                    PlayMonitorClient pmc = clientIterator.next();
                    if (pcdb.equals(pmc.mDispatcherCb)) {
                        pmc.release();
                        clientIterator.remove();
                    } else if (!pmc.mIsPrivileged) {
                        hasPublicClients = true;
                    }
                }
                this.mHasPublicClients = hasPublicClients;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<AudioPlaybackConfiguration> getActivePlaybackConfigurations(boolean isPrivileged) {
        List<AudioPlaybackConfiguration> configsPublic;
        synchronized (this.mPlayers) {
            if (isPrivileged) {
                ArrayList arrayList = new ArrayList(this.mPlayers.values());
                return arrayList;
            }
            synchronized (this.mPlayerLock) {
                configsPublic = anonymizeForPublicConsumption(new ArrayList(this.mPlayers.values()));
            }
            return configsPublic;
        }
    }

    private static final class PlayMonitorClient implements IBinder.DeathRecipient {
        static final int MAX_ERRORS = 5;
        static PlaybackActivityMonitor sListenerDeathMonitor;
        final IPlaybackConfigDispatcher mDispatcherCb;
        int mErrorCount = 0;
        final boolean mIsPrivileged;

        PlayMonitorClient(IPlaybackConfigDispatcher pcdb, boolean isPrivileged) {
            this.mDispatcherCb = pcdb;
            this.mIsPrivileged = isPrivileged;
        }

        public void binderDied() {
            Log.w(PlaybackActivityMonitor.TAG, "client died");
            sListenerDeathMonitor.unregisterPlaybackCallback(this.mDispatcherCb);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(PlaybackActivityMonitor.TAG, "Could not link to client death", e);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    private static final class DuckingManager {
        private final HashMap<Integer, DuckedApp> mDuckers;

        private DuckingManager() {
            this.mDuckers = new HashMap<>();
        }

        /* access modifiers changed from: package-private */
        public synchronized void duckUid(int uid, ArrayList<AudioPlaybackConfiguration> apcsToDuck) {
            if (PlaybackActivityMonitor.DEBUG) {
                Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: duckUid() uid:" + uid);
            }
            if (!this.mDuckers.containsKey(Integer.valueOf(uid))) {
                this.mDuckers.put(Integer.valueOf(uid), new DuckedApp(uid));
            }
            DuckedApp da = this.mDuckers.get(Integer.valueOf(uid));
            Iterator<AudioPlaybackConfiguration> it = apcsToDuck.iterator();
            while (it.hasNext()) {
                da.addDuck(it.next(), false);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void unduckUid(int uid, HashMap<Integer, AudioPlaybackConfiguration> players) {
            if (PlaybackActivityMonitor.DEBUG) {
                Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: unduckUid() uid:" + uid);
            }
            DuckedApp da = this.mDuckers.remove(Integer.valueOf(uid));
            if (da != null) {
                da.removeUnduckAll(players);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void checkDuck(AudioPlaybackConfiguration apc) {
            if (PlaybackActivityMonitor.DEBUG) {
                Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: checkDuck() player piid:" + apc.getPlayerInterfaceId() + " uid:" + apc.getClientUid());
            }
            DuckedApp da = this.mDuckers.get(Integer.valueOf(apc.getClientUid()));
            if (da != null) {
                da.addDuck(apc, true);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void dump(PrintWriter pw) {
            for (DuckedApp da : this.mDuckers.values()) {
                da.dump(pw);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void removeReleased(AudioPlaybackConfiguration apc) {
            int uid = apc.getClientUid();
            if (PlaybackActivityMonitor.DEBUG) {
                Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: removedReleased() player piid: " + apc.getPlayerInterfaceId() + " uid:" + uid);
            }
            DuckedApp da = this.mDuckers.get(Integer.valueOf(uid));
            if (da != null) {
                da.removeReleased(apc);
            }
        }

        private static final class DuckedApp {
            private final ArrayList<Integer> mDuckedPlayers = new ArrayList<>();
            private final int mUid;

            DuckedApp(int uid) {
                this.mUid = uid;
            }

            /* access modifiers changed from: package-private */
            public void dump(PrintWriter pw) {
                pw.print("\t uid:" + this.mUid + " piids:");
                Iterator<Integer> it = this.mDuckedPlayers.iterator();
                while (it.hasNext()) {
                    int piid = it.next().intValue();
                    pw.print(StringUtils.SPACE + piid);
                }
                pw.println("");
            }

            /* access modifiers changed from: package-private */
            public void addDuck(AudioPlaybackConfiguration apc, boolean skipRamp) {
                int piid = new Integer(apc.getPlayerInterfaceId()).intValue();
                if (!this.mDuckedPlayers.contains(Integer.valueOf(piid))) {
                    try {
                        PlaybackActivityMonitor.sEventLogger.log(new DuckEvent(apc, skipRamp).printLog(PlaybackActivityMonitor.TAG));
                        apc.getPlayerProxy().applyVolumeShaper(PlaybackActivityMonitor.DUCK_VSHAPE, skipRamp ? PlaybackActivityMonitor.PLAY_SKIP_RAMP : PlaybackActivityMonitor.PLAY_CREATE_IF_NEEDED);
                        this.mDuckedPlayers.add(Integer.valueOf(piid));
                    } catch (Exception e) {
                        Log.e(PlaybackActivityMonitor.TAG, "Error ducking player piid:" + piid + " uid:" + this.mUid, e);
                    }
                } else if (PlaybackActivityMonitor.DEBUG) {
                    Log.v(PlaybackActivityMonitor.TAG, "player piid:" + piid + " already ducked");
                }
            }

            /* access modifiers changed from: package-private */
            public void removeUnduckAll(HashMap<Integer, AudioPlaybackConfiguration> players) {
                Iterator<Integer> it = this.mDuckedPlayers.iterator();
                while (it.hasNext()) {
                    int piid = it.next().intValue();
                    AudioPlaybackConfiguration apc = players.get(Integer.valueOf(piid));
                    if (apc != null) {
                        try {
                            AudioEventLogger access$200 = PlaybackActivityMonitor.sEventLogger;
                            access$200.log(new AudioEventLogger.StringEvent("unducking piid:" + piid).printLog(PlaybackActivityMonitor.TAG));
                            apc.getPlayerProxy().applyVolumeShaper(PlaybackActivityMonitor.DUCK_ID, VolumeShaper.Operation.REVERSE);
                        } catch (Exception e) {
                            Log.e(PlaybackActivityMonitor.TAG, "Error unducking player piid:" + piid + " uid:" + this.mUid, e);
                        }
                    } else if (PlaybackActivityMonitor.DEBUG) {
                        Log.v(PlaybackActivityMonitor.TAG, "Error unducking player piid:" + piid + ", player not found for uid " + this.mUid);
                    }
                }
                this.mDuckedPlayers.clear();
            }

            /* access modifiers changed from: package-private */
            public void removeReleased(AudioPlaybackConfiguration apc) {
                this.mDuckedPlayers.remove(new Integer(apc.getPlayerInterfaceId()));
            }
        }
    }

    private static final class PlayerEvent extends AudioEventLogger.Event {
        final int mPlayerIId;
        final int mState;

        PlayerEvent(int piid, int state) {
            this.mPlayerIId = piid;
            this.mState = state;
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return "player piid:" + this.mPlayerIId + " state:" + AudioPlaybackConfiguration.toLogFriendlyPlayerState(this.mState);
        }
    }

    private static final class PlayerOpPlayAudioEvent extends AudioEventLogger.Event {
        final boolean mHasOp;
        final int mPlayerIId;
        final int mUid;

        PlayerOpPlayAudioEvent(int piid, boolean hasOp, int uid) {
            this.mPlayerIId = piid;
            this.mHasOp = hasOp;
            this.mUid = uid;
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return "player piid:" + this.mPlayerIId + " has OP_PLAY_AUDIO:" + this.mHasOp + " in uid:" + this.mUid;
        }
    }

    private static final class NewPlayerEvent extends AudioEventLogger.Event {
        private final int mClientPid;
        private final int mClientUid;
        private final AudioAttributes mPlayerAttr;
        private final int mPlayerIId;
        private final int mPlayerType;

        NewPlayerEvent(AudioPlaybackConfiguration apc) {
            this.mPlayerIId = apc.getPlayerInterfaceId();
            this.mPlayerType = apc.getPlayerType();
            this.mClientUid = apc.getClientUid();
            this.mClientPid = apc.getClientPid();
            this.mPlayerAttr = apc.getAudioAttributes();
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return new String("new player piid:" + this.mPlayerIId + " uid/pid:" + this.mClientUid + SliceClientPermissions.SliceAuthority.DELIMITER + this.mClientPid + " type:" + AudioPlaybackConfiguration.toLogFriendlyPlayerType(this.mPlayerType) + " attr:" + this.mPlayerAttr);
        }
    }

    private static final class DuckEvent extends AudioEventLogger.Event {
        private final int mClientPid;
        private final int mClientUid;
        private final int mPlayerIId;
        private final boolean mSkipRamp;

        DuckEvent(AudioPlaybackConfiguration apc, boolean skipRamp) {
            this.mPlayerIId = apc.getPlayerInterfaceId();
            this.mSkipRamp = skipRamp;
            this.mClientUid = apc.getClientUid();
            this.mClientPid = apc.getClientPid();
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return "ducking player piid:" + this.mPlayerIId + " uid/pid:" + this.mClientUid + SliceClientPermissions.SliceAuthority.DELIMITER + this.mClientPid + " skip ramp:" + this.mSkipRamp;
        }
    }

    private static final class AudioAttrEvent extends AudioEventLogger.Event {
        private final AudioAttributes mPlayerAttr;
        private final int mPlayerIId;

        AudioAttrEvent(int piid, AudioAttributes attr) {
            this.mPlayerIId = piid;
            this.mPlayerAttr = attr;
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return new String("player piid:" + this.mPlayerIId + " new AudioAttributes:" + this.mPlayerAttr);
        }
    }
}
