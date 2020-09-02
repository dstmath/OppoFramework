package com.android.server.audio;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecordingConfiguration;
import android.media.AudioSystem;
import android.media.IRecordingConfigDispatcher;
import android.media.MediaRecorder;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.audio.AudioEventLogger;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public final class RecordingActivityMonitor implements AudioSystem.AudioRecordingCallback {
    private static final boolean DEBUG = ("eng".equals(Build.TYPE) || "userdebug".equals(Build.TYPE));
    public static final String TAG = "AudioService.RecordingActivityMonitor";
    private static final AudioEventLogger sEventLogger = new AudioEventLogger(50, "recording activity received by AudioService");
    private ArrayList<RecMonitorClient> mClients = new ArrayList<>();
    private boolean mHasPublicClients = false;
    private final PackageManager mPackMan;
    private List<RecordingState> mRecordStates = new ArrayList();

    static final class RecordingState {
        private AudioRecordingConfiguration mConfig;
        private final RecorderDeathHandler mDeathHandler;
        private boolean mIsActive;
        private final int mRiid;

        RecordingState(int riid, RecorderDeathHandler handler) {
            this.mRiid = riid;
            this.mDeathHandler = handler;
        }

        RecordingState(AudioRecordingConfiguration config) {
            this.mRiid = -1;
            this.mDeathHandler = null;
            this.mConfig = config;
        }

        /* access modifiers changed from: package-private */
        public int getRiid() {
            return this.mRiid;
        }

        /* access modifiers changed from: package-private */
        public int getPortId() {
            AudioRecordingConfiguration audioRecordingConfiguration = this.mConfig;
            if (audioRecordingConfiguration != null) {
                return audioRecordingConfiguration.getClientPortId();
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public AudioRecordingConfiguration getConfig() {
            return this.mConfig;
        }

        /* access modifiers changed from: package-private */
        public boolean hasDeathHandler() {
            return this.mDeathHandler != null;
        }

        /* access modifiers changed from: package-private */
        public boolean isActiveConfiguration() {
            return this.mIsActive && this.mConfig != null;
        }

        /* access modifiers changed from: package-private */
        public boolean setActive(boolean active) {
            if (this.mIsActive == active) {
                return false;
            }
            this.mIsActive = active;
            if (this.mConfig != null) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean setConfig(AudioRecordingConfiguration config) {
            if (config.equals(this.mConfig)) {
                return false;
            }
            this.mConfig = config;
            return this.mIsActive;
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            pw.println("riid " + this.mRiid + "; active? " + this.mIsActive);
            AudioRecordingConfiguration audioRecordingConfiguration = this.mConfig;
            if (audioRecordingConfiguration != null) {
                audioRecordingConfiguration.dump(pw);
            } else {
                pw.println("  no config");
            }
        }
    }

    RecordingActivityMonitor(Context ctxt) {
        RecMonitorClient.sMonitor = this;
        RecorderDeathHandler.sMonitor = this;
        this.mPackMan = ctxt.getPackageManager();
    }

    public void onRecordingConfigurationChanged(int event, int riid, int uid, int session, int source, int portId, boolean silenced, int[] recordingInfo, AudioEffect.Descriptor[] clientEffects, AudioEffect.Descriptor[] effects, int activeSource, String packName) {
        AudioRecordingConfiguration config = createRecordingConfiguration(uid, session, source, recordingInfo, portId, silenced, activeSource, clientEffects, effects);
        if (MediaRecorder.isSystemOnlyAudioSource(source)) {
            sEventLogger.log(new RecordingEvent(event, riid, config).printLog(TAG));
            if (DEBUG) {
                Log.i(TAG, "onRecordingConfigurationChanged(" + "uid=" + uid + "riid=" + riid + "event=" + event + "session=" + session + "source=" + source + "config=" + config + "packName=" + packName);
                return;
            }
            return;
        }
        dispatchCallbacks(updateSnapshot(event, riid, config));
    }

    public int trackRecorder(IBinder recorder) {
        if (recorder == null) {
            Log.e(TAG, "trackRecorder called with null token");
            return -1;
        }
        int newRiid = AudioSystem.newAudioRecorderId();
        RecorderDeathHandler handler = new RecorderDeathHandler(newRiid, recorder);
        if (!handler.init()) {
            return -1;
        }
        synchronized (this.mRecordStates) {
            this.mRecordStates.add(new RecordingState(newRiid, handler));
        }
        return newRiid;
    }

    public void recorderEvent(int riid, int event) {
        int configEvent = 1;
        if (event == 0) {
            configEvent = 0;
        } else if (event != 1) {
            configEvent = -1;
        }
        if (riid == -1 || configEvent == -1) {
            sEventLogger.log(new RecordingEvent(event, riid, null).printLog(TAG));
        } else {
            dispatchCallbacks(updateSnapshot(configEvent, riid, null));
        }
    }

    public void releaseRecorder(int riid) {
        dispatchCallbacks(updateSnapshot(3, riid, null));
    }

    private void dispatchCallbacks(List<AudioRecordingConfiguration> configs) {
        List<AudioRecordingConfiguration> configsPublic;
        if (configs != null) {
            synchronized (this.mClients) {
                if (this.mHasPublicClients) {
                    configsPublic = anonymizeForPublicConsumption(configs);
                } else {
                    configsPublic = new ArrayList<>();
                }
                Iterator<RecMonitorClient> it = this.mClients.iterator();
                while (it.hasNext()) {
                    RecMonitorClient rmc = it.next();
                    try {
                        if (rmc.mIsPrivileged) {
                            rmc.mDispatcherCb.dispatchRecordingConfigChange(configs);
                        } else {
                            rmc.mDispatcherCb.dispatchRecordingConfigChange(configsPublic);
                        }
                    } catch (RemoteException e) {
                        Log.w(TAG, "Could not call dispatchRecordingConfigChange() on client", e);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("\nRecordActivityMonitor dump time: " + DateFormat.getTimeInstance().format(new Date()));
        synchronized (this.mRecordStates) {
            for (RecordingState state : this.mRecordStates) {
                state.dump(pw);
            }
        }
        pw.println(StringUtils.LF);
        sEventLogger.dump(pw);
    }

    private static ArrayList<AudioRecordingConfiguration> anonymizeForPublicConsumption(List<AudioRecordingConfiguration> sysConfigs) {
        ArrayList<AudioRecordingConfiguration> publicConfigs = new ArrayList<>();
        for (AudioRecordingConfiguration config : sysConfigs) {
            publicConfigs.add(AudioRecordingConfiguration.anonymizedCopy(config));
        }
        return publicConfigs;
    }

    /* access modifiers changed from: package-private */
    public void initMonitor() {
        AudioSystem.setRecordingCallback(this);
    }

    /* access modifiers changed from: package-private */
    public void onAudioServerDied() {
        List<AudioRecordingConfiguration> configs = null;
        synchronized (this.mRecordStates) {
            boolean configChanged = false;
            Iterator<RecordingState> it = this.mRecordStates.iterator();
            while (it.hasNext()) {
                RecordingState state = it.next();
                if (!state.hasDeathHandler()) {
                    if (state.isActiveConfiguration()) {
                        configChanged = true;
                        sEventLogger.log(new RecordingEvent(3, state.getRiid(), state.getConfig()));
                    }
                    it.remove();
                }
            }
            if (configChanged) {
                configs = getActiveRecordingConfigurations(true);
            }
        }
        dispatchCallbacks(configs);
    }

    /* access modifiers changed from: package-private */
    public void registerRecordingCallback(IRecordingConfigDispatcher rcdb, boolean isPrivileged) {
        if (rcdb != null) {
            synchronized (this.mClients) {
                RecMonitorClient rmc = new RecMonitorClient(rcdb, isPrivileged);
                if (rmc.init()) {
                    if (!isPrivileged) {
                        this.mHasPublicClients = true;
                    }
                    this.mClients.add(rmc);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        if (rcdb != null) {
            synchronized (this.mClients) {
                Iterator<RecMonitorClient> clientIterator = this.mClients.iterator();
                boolean hasPublicClients = false;
                while (clientIterator.hasNext()) {
                    RecMonitorClient rmc = clientIterator.next();
                    if (rcdb.equals(rmc.mDispatcherCb)) {
                        rmc.release();
                        clientIterator.remove();
                    } else if (!rmc.mIsPrivileged) {
                        hasPublicClients = true;
                    }
                }
                this.mHasPublicClients = hasPublicClients;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations(boolean isPrivileged) {
        List<AudioRecordingConfiguration> configs = new ArrayList<>();
        synchronized (this.mRecordStates) {
            for (RecordingState state : this.mRecordStates) {
                if (state.isActiveConfiguration()) {
                    configs.add(state.getConfig());
                }
            }
        }
        if (!isPrivileged) {
            return anonymizeForPublicConsumption(configs);
        }
        return configs;
    }

    private AudioRecordingConfiguration createRecordingConfiguration(int uid, int session, int source, int[] recordingInfo, int portId, boolean silenced, int activeSource, AudioEffect.Descriptor[] clientEffects, AudioEffect.Descriptor[] effects) {
        String packageName;
        AudioFormat clientFormat = new AudioFormat.Builder().setEncoding(recordingInfo[0]).setChannelMask(recordingInfo[1]).setSampleRate(recordingInfo[2]).build();
        AudioFormat deviceFormat = new AudioFormat.Builder().setEncoding(recordingInfo[3]).setChannelMask(recordingInfo[4]).setSampleRate(recordingInfo[5]).build();
        int patchHandle = recordingInfo[6];
        String[] packages = this.mPackMan.getPackagesForUid(uid);
        if (packages == null || packages.length <= 0) {
            packageName = "";
        } else {
            packageName = packages[0];
        }
        return new AudioRecordingConfiguration(uid, session, source, clientFormat, deviceFormat, patchHandle, packageName, portId, silenced, activeSource, clientEffects, effects);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0054, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d0, code lost:
        return r0;
     */
    private List<AudioRecordingConfiguration> updateSnapshot(int event, int riid, AudioRecordingConfiguration config) {
        boolean configChanged;
        List<AudioRecordingConfiguration> configs = null;
        synchronized (this.mRecordStates) {
            int stateIndex = -1;
            if (riid != -1) {
                try {
                    stateIndex = findStateByRiid(riid);
                } catch (Throwable th) {
                    throw th;
                }
            } else if (config != null) {
                stateIndex = findStateByPortId(config.getClientPortId());
            }
            boolean z = false;
            if (stateIndex == -1) {
                if (event == 0 && config != null) {
                    this.mRecordStates.add(new RecordingState(config));
                    stateIndex = this.mRecordStates.size() - 1;
                } else if (config == null) {
                    Log.e(TAG, String.format("Unexpected event %d for riid %d", Integer.valueOf(event), Integer.valueOf(riid)));
                }
            }
            RecordingState state = this.mRecordStates.get(stateIndex);
            if (event == 0) {
                configChanged = state.setActive(true);
                if (config != null) {
                    if (state.setConfig(config) || configChanged) {
                        z = true;
                    }
                    configChanged = z;
                }
            } else if (event == 1) {
                configChanged = state.setActive(false);
                if (!state.hasDeathHandler()) {
                    this.mRecordStates.remove(stateIndex);
                }
            } else if (event == 2) {
                configChanged = state.setConfig(config);
            } else if (event != 3) {
                Log.e(TAG, String.format("Unknown event %d for riid %d / portid %d", Integer.valueOf(event), Integer.valueOf(riid), Integer.valueOf(state.getPortId())));
                configChanged = false;
            } else {
                configChanged = state.isActiveConfiguration();
                this.mRecordStates.remove(stateIndex);
            }
            if (configChanged) {
                sEventLogger.log(new RecordingEvent(event, riid, state.getConfig()));
                configs = getActiveRecordingConfigurations(true);
            }
        }
    }

    private int findStateByRiid(int riid) {
        synchronized (this.mRecordStates) {
            for (int i = 0; i < this.mRecordStates.size(); i++) {
                if (this.mRecordStates.get(i).getRiid() == riid) {
                    return i;
                }
            }
            return -1;
        }
    }

    private int findStateByPortId(int portId) {
        synchronized (this.mRecordStates) {
            for (int i = 0; i < this.mRecordStates.size(); i++) {
                if (!this.mRecordStates.get(i).hasDeathHandler() && this.mRecordStates.get(i).getPortId() == portId) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class RecMonitorClient implements IBinder.DeathRecipient {
        static RecordingActivityMonitor sMonitor;
        final IRecordingConfigDispatcher mDispatcherCb;
        final boolean mIsPrivileged;

        RecMonitorClient(IRecordingConfigDispatcher rcdb, boolean isPrivileged) {
            this.mDispatcherCb = rcdb;
            this.mIsPrivileged = isPrivileged;
        }

        public void binderDied() {
            Log.w(RecordingActivityMonitor.TAG, "client died");
            sMonitor.unregisterRecordingCallback(this.mDispatcherCb);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(RecordingActivityMonitor.TAG, "Could not link to client death", e);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    private static final class RecorderDeathHandler implements IBinder.DeathRecipient {
        static RecordingActivityMonitor sMonitor;
        private final IBinder mRecorderToken;
        final int mRiid;

        RecorderDeathHandler(int riid, IBinder recorderToken) {
            this.mRiid = riid;
            this.mRecorderToken = recorderToken;
        }

        public void binderDied() {
            sMonitor.releaseRecorder(this.mRiid);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            try {
                this.mRecorderToken.linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(RecordingActivityMonitor.TAG, "Could not link to recorder death", e);
                return false;
            }
        }
    }

    private static final class RecordingEvent extends AudioEventLogger.Event {
        private final int mClientUid;
        private final String mPackName;
        private final int mRIId;
        private final int mRecEvent;
        private final int mSession;
        private final int mSource;

        RecordingEvent(int event, int riid, AudioRecordingConfiguration config) {
            this.mRecEvent = event;
            this.mRIId = riid;
            if (config != null) {
                this.mClientUid = config.getClientUid();
                this.mSession = config.getClientAudioSessionId();
                this.mSource = config.getClientAudioSource();
                this.mPackName = config.getClientPackageName();
                return;
            }
            this.mClientUid = -1;
            this.mSession = -1;
            this.mSource = -1;
            this.mPackName = null;
        }

        private static String recordEventToString(int recEvent) {
            if (recEvent == 0) {
                return "start";
            }
            if (recEvent == 1) {
                return "stop";
            }
            if (recEvent == 2) {
                return "update";
            }
            if (recEvent == 3) {
                return "release";
            }
            return "unknown (" + recEvent + ")";
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            String str;
            StringBuilder sb = new StringBuilder("rec ");
            sb.append(recordEventToString(this.mRecEvent));
            sb.append(" riid:");
            sb.append(this.mRIId);
            sb.append(" uid:");
            sb.append(this.mClientUid);
            sb.append(" session:");
            sb.append(this.mSession);
            sb.append(" src:");
            sb.append(MediaRecorder.toLogFriendlyAudioSource(this.mSource));
            if (this.mPackName == null) {
                str = "";
            } else {
                str = " pack:" + this.mPackName;
            }
            sb.append(str);
            return sb.toString();
        }
    }
}
