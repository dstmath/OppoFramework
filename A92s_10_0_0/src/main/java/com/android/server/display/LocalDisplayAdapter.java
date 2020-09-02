package com.android.server.display;

import android.app.ActivityThread;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.sidekick.SidekickInternal;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayAddress;
import android.view.DisplayCutout;
import android.view.DisplayEventReceiver;
import android.view.SurfaceControl;
import com.android.server.LocalServices;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class LocalDisplayAdapter extends DisplayAdapter {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("dbg.dms.lda", false);
    /* access modifiers changed from: private */
    public static final boolean MTK_DEBUG = "eng".equals(Build.TYPE);
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    private static final String TAG = "LocalDisplayAdapter";
    private static final String UNIQUE_ID_PREFIX = "local:";
    /* access modifiers changed from: private */
    public final LongSparseArray<LocalDisplayDevice> mDevices = new LongSparseArray<>();
    private PhysicalDisplayEventReceiver mPhysicalDisplayEventReceiver;

    public LocalDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener) {
        super(syncRoot, context, handler, listener, TAG);
    }

    @Override // com.android.server.display.DisplayAdapter
    public void registerLocked() {
        super.registerLocked();
        this.mPhysicalDisplayEventReceiver = new PhysicalDisplayEventReceiver(getHandler().getLooper());
        for (long physicalDisplayId : SurfaceControl.getPhysicalDisplayIds()) {
            tryConnectDisplayLocked(physicalDisplayId);
        }
    }

    /* access modifiers changed from: private */
    public void tryConnectDisplayLocked(long physicalDisplayId) {
        int activeColorMode;
        IBinder displayToken = SurfaceControl.getPhysicalDisplayToken(physicalDisplayId);
        if (displayToken != null) {
            SurfaceControl.PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken);
            if (configs == null) {
                Slog.w(TAG, "No valid configs found for display device " + physicalDisplayId);
                return;
            }
            int activeConfig = SurfaceControl.getActiveConfig(displayToken);
            if (activeConfig < 0) {
                Slog.w(TAG, "No active config found for display device " + physicalDisplayId);
                return;
            }
            int activeColorMode2 = SurfaceControl.getActiveColorMode(displayToken);
            if (activeColorMode2 < 0) {
                Slog.w(TAG, "Unable to get active color mode for display device " + physicalDisplayId);
                activeColorMode = -1;
            } else {
                activeColorMode = activeColorMode2;
            }
            int[] colorModes = SurfaceControl.getDisplayColorModes(displayToken);
            int[] allowedConfigs = SurfaceControl.getAllowedDisplayConfigs(displayToken);
            LocalDisplayDevice device = this.mDevices.get(physicalDisplayId);
            if (device == null) {
                LocalDisplayDevice device2 = new LocalDisplayDevice(displayToken, physicalDisplayId, configs, activeConfig, allowedConfigs, colorModes, activeColorMode, this.mDevices.size() == 0);
                this.mDevices.put(physicalDisplayId, device2);
                sendDisplayDeviceEventLocked(device2, 1);
            } else if (device.updatePhysicalDisplayInfoLocked(configs, activeConfig, allowedConfigs, colorModes, activeColorMode)) {
                sendDisplayDeviceEventLocked(device, 2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void tryDisconnectDisplayLocked(long physicalDisplayId) {
        LocalDisplayDevice device = this.mDevices.get(physicalDisplayId);
        if (device != null) {
            this.mDevices.remove(physicalDisplayId);
            sendDisplayDeviceEventLocked(device, 3);
        }
    }

    static int getPowerModeForState(int state) {
        if (state == 1) {
            return 0;
        }
        if (state == 6) {
            return 4;
        }
        if (state == 3) {
            return 1;
        }
        if (state != 4) {
            return 2;
        }
        return 3;
    }

    /* access modifiers changed from: private */
    public final class LocalDisplayDevice extends DisplayDevice {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private int mActiveColorMode;
        private boolean mActiveColorModeInvalid;
        private int mActiveModeId;
        private boolean mActiveModeInvalid;
        private int mActivePhysIndex;
        private int[] mAllowedModeIds;
        private boolean mAllowedModeIdsInvalid;
        private int[] mAllowedPhysIndexes;
        /* access modifiers changed from: private */
        public final Light mBacklight;
        private int mBrightness = -1;
        private int mDefaultModeId;
        private SurfaceControl.PhysicalDisplayInfo[] mDisplayInfos;
        private boolean mHavePendingChanges;
        private Display.HdrCapabilities mHdrCapabilities;
        private DisplayDeviceInfo mInfo;
        private final boolean mIsInternal;
        private final long mPhysicalDisplayId;
        /* access modifiers changed from: private */
        public boolean mSidekickActive;
        /* access modifiers changed from: private */
        public SidekickInternal mSidekickInternal;
        private int mState = 0;
        private final ArrayList<Integer> mSupportedColorModes = new ArrayList<>();
        private final SparseArray<DisplayModeRecord> mSupportedModes = new SparseArray<>();

        LocalDisplayDevice(IBinder displayToken, long physicalDisplayId, SurfaceControl.PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo, int[] allowedDisplayInfos, int[] colorModes, int activeColorMode, boolean isInternal) {
            super(LocalDisplayAdapter.this, displayToken, LocalDisplayAdapter.UNIQUE_ID_PREFIX + physicalDisplayId);
            this.mPhysicalDisplayId = physicalDisplayId;
            this.mIsInternal = isInternal;
            updatePhysicalDisplayInfoLocked(physicalDisplayInfos, activeDisplayInfo, allowedDisplayInfos, colorModes, activeColorMode);
            updateColorModesLocked(colorModes, activeColorMode);
            this.mSidekickInternal = (SidekickInternal) LocalServices.getService(SidekickInternal.class);
            if (this.mIsInternal) {
                this.mBacklight = ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0);
            } else {
                this.mBacklight = null;
            }
            this.mHdrCapabilities = SurfaceControl.getHdrCapabilities(displayToken);
        }

        @Override // com.android.server.display.DisplayDevice
        public boolean hasStableUniqueId() {
            return true;
        }

        public boolean updatePhysicalDisplayInfoLocked(SurfaceControl.PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo, int[] allowedDisplayInfos, int[] colorModes, int activeColorMode) {
            this.mDisplayInfos = (SurfaceControl.PhysicalDisplayInfo[]) Arrays.copyOf(physicalDisplayInfos, physicalDisplayInfos.length);
            this.mActivePhysIndex = activeDisplayInfo;
            this.mAllowedPhysIndexes = Arrays.copyOf(allowedDisplayInfos, allowedDisplayInfos.length);
            ArrayList<DisplayModeRecord> records = new ArrayList<>();
            boolean modesAdded = false;
            for (SurfaceControl.PhysicalDisplayInfo info : physicalDisplayInfos) {
                boolean existingMode = false;
                int j = 0;
                while (true) {
                    if (j >= records.size()) {
                        break;
                    } else if (records.get(j).hasMatchingMode(info)) {
                        existingMode = true;
                        break;
                    } else {
                        j++;
                    }
                }
                if (!existingMode) {
                    DisplayModeRecord record = findDisplayModeRecord(info);
                    if (record == null) {
                        record = new DisplayModeRecord(info);
                        modesAdded = true;
                    }
                    records.add(record);
                }
            }
            DisplayModeRecord activeRecord = null;
            int i = 0;
            while (true) {
                if (i >= records.size()) {
                    break;
                }
                DisplayModeRecord record2 = records.get(i);
                if (record2.hasMatchingMode(physicalDisplayInfos[activeDisplayInfo])) {
                    activeRecord = record2;
                    break;
                }
                i++;
            }
            int i2 = this.mActiveModeId;
            if (!(i2 == 0 || i2 == activeRecord.mMode.getModeId())) {
                this.mActiveModeInvalid = true;
                LocalDisplayAdapter.this.sendTraversalRequestLocked();
            }
            if (!(records.size() != this.mSupportedModes.size() || modesAdded)) {
                return false;
            }
            this.mHavePendingChanges = true;
            this.mSupportedModes.clear();
            Iterator<DisplayModeRecord> it = records.iterator();
            while (it.hasNext()) {
                DisplayModeRecord record3 = it.next();
                this.mSupportedModes.put(record3.mMode.getModeId(), record3);
            }
            if (findDisplayInfoIndexLocked(this.mDefaultModeId) < 0) {
                if (this.mDefaultModeId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Default display mode no longer available, using currently active mode as default.");
                }
                this.mDefaultModeId = activeRecord.mMode.getModeId();
            }
            if (this.mSupportedModes.indexOfKey(this.mActiveModeId) < 0) {
                if (this.mActiveModeId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Active display mode no longer available, reverting to default mode.");
                }
                this.mActiveModeId = this.mDefaultModeId;
                this.mActiveModeInvalid = true;
            }
            this.mAllowedModeIds = new int[]{this.mActiveModeId};
            int[] iArr = this.mAllowedPhysIndexes;
            int[] allowedModeIds = new int[iArr.length];
            int size = 0;
            for (int physIndex : iArr) {
                int modeId = findMatchingModeIdLocked(physIndex);
                if (modeId > 0) {
                    allowedModeIds[size] = modeId;
                    size++;
                }
            }
            this.mAllowedModeIdsInvalid = !Arrays.equals(allowedModeIds, this.mAllowedModeIds);
            LocalDisplayAdapter.this.sendTraversalRequestLocked();
            return true;
        }

        private boolean updateColorModesLocked(int[] colorModes, int activeColorMode) {
            List<Integer> pendingColorModes = new ArrayList<>();
            if (colorModes == null) {
                return false;
            }
            boolean colorModesAdded = false;
            for (int colorMode : colorModes) {
                if (!this.mSupportedColorModes.contains(Integer.valueOf(colorMode))) {
                    colorModesAdded = true;
                }
                pendingColorModes.add(Integer.valueOf(colorMode));
            }
            if (!(pendingColorModes.size() != this.mSupportedColorModes.size() || colorModesAdded)) {
                return false;
            }
            this.mHavePendingChanges = true;
            this.mSupportedColorModes.clear();
            this.mSupportedColorModes.addAll(pendingColorModes);
            Collections.sort(this.mSupportedColorModes);
            if (!this.mSupportedColorModes.contains(Integer.valueOf(this.mActiveColorMode))) {
                if (this.mActiveColorMode != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Active color mode no longer available, reverting to default mode.");
                    this.mActiveColorMode = 0;
                    this.mActiveColorModeInvalid = true;
                } else if (!this.mSupportedColorModes.isEmpty()) {
                    Slog.e(LocalDisplayAdapter.TAG, "Default and active color mode is no longer available! Reverting to first available mode.");
                    this.mActiveColorMode = this.mSupportedColorModes.get(0).intValue();
                    this.mActiveColorModeInvalid = true;
                } else {
                    Slog.e(LocalDisplayAdapter.TAG, "No color modes available!");
                }
            }
            return true;
        }

        private DisplayModeRecord findDisplayModeRecord(SurfaceControl.PhysicalDisplayInfo info) {
            for (int i = 0; i < this.mSupportedModes.size(); i++) {
                DisplayModeRecord record = this.mSupportedModes.valueAt(i);
                if (record.hasMatchingMode(info)) {
                    return record;
                }
            }
            return null;
        }

        @Override // com.android.server.display.DisplayDevice
        public void applyPendingDisplayDeviceInfoChangesLocked() {
            if (this.mHavePendingChanges) {
                this.mInfo = null;
                this.mHavePendingChanges = false;
            }
        }

        @Override // com.android.server.display.DisplayDevice
        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                SurfaceControl.PhysicalDisplayInfo phys = this.mDisplayInfos[this.mActivePhysIndex];
                this.mInfo = new DisplayDeviceInfo();
                this.mInfo.width = phys.width;
                this.mInfo.height = phys.height;
                DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                displayDeviceInfo.modeId = this.mActiveModeId;
                displayDeviceInfo.defaultModeId = this.mDefaultModeId;
                displayDeviceInfo.supportedModes = getDisplayModes(this.mSupportedModes);
                DisplayDeviceInfo displayDeviceInfo2 = this.mInfo;
                displayDeviceInfo2.colorMode = this.mActiveColorMode;
                displayDeviceInfo2.supportedColorModes = new int[this.mSupportedColorModes.size()];
                for (int i = 0; i < this.mSupportedColorModes.size(); i++) {
                    this.mInfo.supportedColorModes[i] = this.mSupportedColorModes.get(i).intValue();
                }
                DisplayDeviceInfo displayDeviceInfo3 = this.mInfo;
                displayDeviceInfo3.hdrCapabilities = this.mHdrCapabilities;
                displayDeviceInfo3.appVsyncOffsetNanos = phys.appVsyncOffsetNanos;
                this.mInfo.presentationDeadlineNanos = phys.presentationDeadlineNanos;
                DisplayDeviceInfo displayDeviceInfo4 = this.mInfo;
                displayDeviceInfo4.state = this.mState;
                displayDeviceInfo4.uniqueId = getUniqueId();
                DisplayAddress.Physical physicalAddress = DisplayAddress.fromPhysicalDisplayId(this.mPhysicalDisplayId);
                this.mInfo.address = physicalAddress;
                if (phys.secure) {
                    this.mInfo.flags = 12;
                }
                Resources res = LocalDisplayAdapter.this.getOverlayContext().getResources();
                if (this.mIsInternal) {
                    this.mInfo.name = res.getString(17039867);
                    DisplayDeviceInfo displayDeviceInfo5 = this.mInfo;
                    displayDeviceInfo5.flags = 3 | displayDeviceInfo5.flags;
                    if (res.getBoolean(17891478) || (Build.IS_EMULATOR && SystemProperties.getBoolean(LocalDisplayAdapter.PROPERTY_EMULATOR_CIRCULAR, false))) {
                        this.mInfo.flags |= 256;
                    }
                    if (res.getBoolean(17891479)) {
                        this.mInfo.flags |= 2048;
                    }
                    DisplayDeviceInfo displayDeviceInfo6 = this.mInfo;
                    displayDeviceInfo6.displayCutout = DisplayCutout.fromResourcesRectApproximation(res, displayDeviceInfo6.width, this.mInfo.height);
                    DisplayDeviceInfo displayDeviceInfo7 = this.mInfo;
                    displayDeviceInfo7.type = 1;
                    displayDeviceInfo7.densityDpi = (int) ((phys.density * 160.0f) + 0.5f);
                    this.mInfo.xDpi = phys.xDpi;
                    this.mInfo.yDpi = phys.yDpi;
                    this.mInfo.touch = 1;
                } else {
                    DisplayDeviceInfo displayDeviceInfo8 = this.mInfo;
                    displayDeviceInfo8.displayCutout = null;
                    displayDeviceInfo8.type = 2;
                    displayDeviceInfo8.flags |= 64;
                    this.mInfo.name = LocalDisplayAdapter.this.getContext().getResources().getString(17039868);
                    DisplayDeviceInfo displayDeviceInfo9 = this.mInfo;
                    displayDeviceInfo9.touch = 2;
                    displayDeviceInfo9.setAssumedDensityForExternalDisplay(phys.width, phys.height);
                    if ("portrait".equals(SystemProperties.get("persist.demo.hdmirotation"))) {
                        this.mInfo.rotation = 3;
                    }
                    if (SystemProperties.getBoolean("persist.demo.hdmirotates", false)) {
                        this.mInfo.flags |= 2;
                    }
                    if (!res.getBoolean(17891474)) {
                        this.mInfo.flags |= 128;
                    }
                    if (isDisplayPrivate(physicalAddress)) {
                        this.mInfo.flags |= 16;
                    }
                }
            }
            return this.mInfo;
        }

        @Override // com.android.server.display.DisplayDevice
        public Runnable requestDisplayStateLocked(final int state, final int brightness) {
            final boolean brightnessChanged = true;
            boolean stateChanged = this.mState != state;
            if (this.mBrightness == brightness || this.mBacklight == null) {
                brightnessChanged = false;
            }
            if (!stateChanged && !brightnessChanged) {
                return null;
            }
            final long physicalDisplayId = this.mPhysicalDisplayId;
            final IBinder token = getDisplayTokenLocked();
            final int oldState = this.mState;
            if (stateChanged) {
                this.mState = state;
                updateDeviceInfoLocked();
            }
            if (brightnessChanged) {
                this.mBrightness = brightness;
            }
            return new Runnable() {
                /* class com.android.server.display.LocalDisplayAdapter.LocalDisplayDevice.AnonymousClass1 */

                public void run() {
                    int i;
                    int i2;
                    int currentState = oldState;
                    if (Display.isSuspendedState(oldState) || oldState == 0) {
                        if (!Display.isSuspendedState(state)) {
                            setDisplayState(state);
                            currentState = state;
                        } else {
                            int i3 = state;
                            if (i3 == 4 || (i2 = oldState) == 4) {
                                setDisplayState(3);
                                currentState = 3;
                            } else if (i3 == 6 || i2 == 6) {
                                setDisplayState(2);
                                currentState = 2;
                            } else {
                                return;
                            }
                        }
                    }
                    boolean vrModeChange = false;
                    if ((state == 5 || currentState == 5) && currentState != (i = state)) {
                        setVrMode(i == 5);
                        vrModeChange = true;
                    }
                    if (brightnessChanged || vrModeChange) {
                        setDisplayBrightness(brightness);
                    }
                    int i4 = state;
                    if (i4 != currentState) {
                        setDisplayState(i4);
                    }
                    if (brightnessChanged || vrModeChange) {
                        OppoBrightUtils.mLastBrightness = brightness;
                    }
                }

                private void setVrMode(boolean isVrEnabled) {
                    if (LocalDisplayAdapter.DEBUG) {
                        Slog.d(LocalDisplayAdapter.TAG, "setVrMode(id=" + physicalDisplayId + ", state=" + Display.stateToString(state) + ")");
                    }
                    LocalDisplayDevice.this.mBacklight.setVrMode(isVrEnabled);
                }

                /* JADX INFO: finally extract failed */
                private void setDisplayState(int state) {
                    if (LocalDisplayAdapter.DEBUG || LocalDisplayAdapter.MTK_DEBUG) {
                        Slog.d(LocalDisplayAdapter.TAG, "setDisplayState(id=" + physicalDisplayId + ", state=" + Display.stateToString(state) + ")");
                    }
                    if (LocalDisplayDevice.this.mSidekickActive) {
                        Trace.traceBegin(131072, "SidekickInternal#endDisplayControl");
                        try {
                            LocalDisplayDevice.this.mSidekickInternal.endDisplayControl();
                            Trace.traceEnd(131072);
                            boolean unused = LocalDisplayDevice.this.mSidekickActive = false;
                        } catch (Throwable th) {
                            Trace.traceEnd(131072);
                            throw th;
                        }
                    }
                    int mode = LocalDisplayAdapter.getPowerModeForState(state);
                    Trace.traceBegin(131072, "setDisplayState(id=" + physicalDisplayId + ", state=" + Display.stateToString(state) + ")");
                    try {
                        OppoBrightUtils.getInstance();
                        int lastBrightness = OppoBrightUtils.mLastBrightness;
                        if ((mode == 2 || mode == 3) && (lastBrightness == 0 || lastBrightness == 3)) {
                            OppoBrightUtils.mBrightnessNoAnimation = true;
                        }
                        if (OppoBrightUtils.DEBUG) {
                            Slog.e(LocalDisplayAdapter.TAG, "setDisplayState  state =" + state + " color = " + brightness + " last = " + lastBrightness);
                        }
                        SurfaceControl.setDisplayPowerMode(token, mode);
                        Trace.traceCounter(131072, "DisplayPowerMode", mode);
                        Trace.traceEnd(131072);
                        if (Display.isSuspendedState(state) && state != 1 && LocalDisplayDevice.this.mSidekickInternal != null && !LocalDisplayDevice.this.mSidekickActive) {
                            Trace.traceBegin(131072, "SidekickInternal#startDisplayControl");
                            try {
                                boolean unused2 = LocalDisplayDevice.this.mSidekickActive = LocalDisplayDevice.this.mSidekickInternal.startDisplayControl(state);
                            } finally {
                                Trace.traceEnd(131072);
                            }
                        }
                    } catch (Throwable th2) {
                        Trace.traceEnd(131072);
                        throw th2;
                    }
                }

                private void setDisplayBrightness(int brightness) {
                    if (LocalDisplayAdapter.DEBUG || LocalDisplayAdapter.MTK_DEBUG) {
                        Slog.d(LocalDisplayAdapter.TAG, "setDisplayBrightness(id=" + physicalDisplayId + ", brightness=" + brightness + ")");
                    }
                    Trace.traceBegin(131072, "setDisplayBrightness(id=" + physicalDisplayId + ", brightness=" + brightness + ")");
                    try {
                        LocalDisplayDevice.this.mBacklight.setBrightness(brightness);
                        Trace.traceCounter(131072, "ScreenBrightness", brightness);
                    } finally {
                        Trace.traceEnd(131072);
                    }
                }
            };
        }

        @Override // com.android.server.display.DisplayDevice
        public void setRequestedColorModeLocked(int colorMode) {
            if (requestColorModeLocked(colorMode)) {
                updateDeviceInfoLocked();
            }
        }

        @Override // com.android.server.display.DisplayDevice
        public void setAllowedDisplayModesLocked(int[] modes) {
            updateAllowedModesLocked(modes);
        }

        @Override // com.android.server.display.DisplayDevice
        public void onOverlayChangedLocked() {
            updateDeviceInfoLocked();
        }

        public void onActivePhysicalDisplayModeChangedLocked(int physIndex) {
            if (updateActiveModeLocked(physIndex)) {
                updateDeviceInfoLocked();
            }
        }

        public boolean updateActiveModeLocked(int activePhysIndex) {
            boolean z = false;
            if (this.mActivePhysIndex == activePhysIndex) {
                return false;
            }
            this.mActivePhysIndex = activePhysIndex;
            this.mActiveModeId = findMatchingModeIdLocked(activePhysIndex);
            if (this.mActiveModeId == 0) {
                z = true;
            }
            this.mActiveModeInvalid = z;
            if (this.mActiveModeInvalid) {
                Slog.w(LocalDisplayAdapter.TAG, "In unknown mode after setting allowed configs: allowedPhysIndexes=" + this.mAllowedPhysIndexes + ", activePhysIndex=" + this.mActivePhysIndex);
            }
            return true;
        }

        public void updateAllowedModesLocked(int[] allowedModes) {
            if ((!Arrays.equals(allowedModes, this.mAllowedModeIds) || this.mAllowedModeIdsInvalid) && updateAllowedModesInternalLocked(allowedModes)) {
                updateDeviceInfoLocked();
            }
        }

        public boolean updateAllowedModesInternalLocked(int[] allowedModes) {
            if (LocalDisplayAdapter.DEBUG) {
                Slog.w(LocalDisplayAdapter.TAG, "updateAllowedModesInternalLocked(allowedModes=" + Arrays.toString(allowedModes) + ")");
            }
            int[] allowedPhysIndexes = new int[allowedModes.length];
            int size = 0;
            for (int modeId : allowedModes) {
                int physIndex = findDisplayInfoIndexLocked(modeId);
                if (physIndex < 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Requested mode ID " + modeId + " not available, dropping from allowed set.");
                } else {
                    allowedPhysIndexes[size] = physIndex;
                    size++;
                }
            }
            if (size != allowedModes.length) {
                allowedPhysIndexes = Arrays.copyOf(allowedPhysIndexes, size);
            }
            if (size == 0) {
                if (LocalDisplayAdapter.DEBUG) {
                    Slog.w(LocalDisplayAdapter.TAG, "No valid modes allowed, falling back to default mode (id=" + this.mDefaultModeId + ")");
                }
                int i = this.mDefaultModeId;
                allowedModes = new int[]{i};
                allowedPhysIndexes = new int[]{findDisplayInfoIndexLocked(i)};
            }
            this.mAllowedModeIds = allowedModes;
            this.mAllowedModeIdsInvalid = false;
            if (Arrays.equals(this.mAllowedPhysIndexes, allowedPhysIndexes)) {
                return false;
            }
            this.mAllowedPhysIndexes = allowedPhysIndexes;
            if (LocalDisplayAdapter.DEBUG) {
                Slog.w(LocalDisplayAdapter.TAG, "Setting allowed physical configs: allowedPhysIndexes=" + Arrays.toString(allowedPhysIndexes));
            }
            SurfaceControl.setAllowedDisplayConfigs(getDisplayTokenLocked(), allowedPhysIndexes);
            return updateActiveModeLocked(SurfaceControl.getActiveConfig(getDisplayTokenLocked()));
        }

        public boolean requestColorModeLocked(int colorMode) {
            if (this.mActiveColorMode == colorMode) {
                return false;
            }
            if (!this.mSupportedColorModes.contains(Integer.valueOf(colorMode))) {
                Slog.w(LocalDisplayAdapter.TAG, "Unable to find color mode " + colorMode + ", ignoring request.");
                return false;
            }
            SurfaceControl.setActiveColorMode(getDisplayTokenLocked(), colorMode);
            this.mActiveColorMode = colorMode;
            this.mActiveColorModeInvalid = false;
            return true;
        }

        @Override // com.android.server.display.DisplayDevice
        public void dumpLocked(PrintWriter pw) {
            super.dumpLocked(pw);
            pw.println("mPhysicalDisplayId=" + this.mPhysicalDisplayId);
            pw.println("mAllowedPhysIndexes=" + Arrays.toString(this.mAllowedPhysIndexes));
            pw.println("mAllowedModeIds=" + Arrays.toString(this.mAllowedModeIds));
            pw.println("mAllowedModeIdsInvalid=" + this.mAllowedModeIdsInvalid);
            pw.println("mActivePhysIndex=" + this.mActivePhysIndex);
            pw.println("mActiveModeId=" + this.mActiveModeId);
            pw.println("mActiveColorMode=" + this.mActiveColorMode);
            pw.println("mDefaultModeId=" + this.mDefaultModeId);
            pw.println("mState=" + Display.stateToString(this.mState));
            pw.println("mBrightness=" + this.mBrightness);
            pw.println("mBacklight=" + this.mBacklight);
            pw.println("mDisplayInfos=");
            for (int i = 0; i < this.mDisplayInfos.length; i++) {
                pw.println("  " + this.mDisplayInfos[i]);
            }
            pw.println("mSupportedModes=");
            for (int i2 = 0; i2 < this.mSupportedModes.size(); i2++) {
                pw.println("  " + this.mSupportedModes.valueAt(i2));
            }
            pw.print("mSupportedColorModes=[");
            for (int i3 = 0; i3 < this.mSupportedColorModes.size(); i3++) {
                if (i3 != 0) {
                    pw.print(", ");
                }
                pw.print(this.mSupportedColorModes.get(i3));
            }
            pw.println("]");
        }

        private int findDisplayInfoIndexLocked(int modeId) {
            DisplayModeRecord record = this.mSupportedModes.get(modeId);
            if (record == null) {
                return -1;
            }
            int i = 0;
            while (true) {
                SurfaceControl.PhysicalDisplayInfo[] physicalDisplayInfoArr = this.mDisplayInfos;
                if (i >= physicalDisplayInfoArr.length) {
                    return -1;
                }
                if (record.hasMatchingMode(physicalDisplayInfoArr[i])) {
                    return i;
                }
                i++;
            }
        }

        private int findMatchingModeIdLocked(int physIndex) {
            SurfaceControl.PhysicalDisplayInfo info = this.mDisplayInfos[physIndex];
            for (int i = 0; i < this.mSupportedModes.size(); i++) {
                DisplayModeRecord record = this.mSupportedModes.valueAt(i);
                if (record.hasMatchingMode(info)) {
                    return record.mMode.getModeId();
                }
            }
            return 0;
        }

        private void updateDeviceInfoLocked() {
            this.mInfo = null;
            LocalDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
        }

        private Display.Mode[] getDisplayModes(SparseArray<DisplayModeRecord> records) {
            int size = records.size();
            Display.Mode[] modes = new Display.Mode[size];
            for (int i = 0; i < size; i++) {
                modes[i] = records.valueAt(i).mMode;
            }
            return modes;
        }

        private boolean isDisplayPrivate(DisplayAddress.Physical physicalAddress) {
            int[] ports;
            if (!(physicalAddress == null || (ports = LocalDisplayAdapter.this.getOverlayContext().getResources().getIntArray(17236039)) == null)) {
                int port = physicalAddress.getPort();
                for (int p : ports) {
                    if (p == port) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public Context getOverlayContext() {
        return ActivityThread.currentActivityThread().getSystemUiContext();
    }

    private static final class DisplayModeRecord {
        public final Display.Mode mMode;

        public DisplayModeRecord(SurfaceControl.PhysicalDisplayInfo phys) {
            this.mMode = DisplayAdapter.createMode(phys.width, phys.height, phys.refreshRate);
        }

        public boolean hasMatchingMode(SurfaceControl.PhysicalDisplayInfo info) {
            return this.mMode.getPhysicalWidth() == info.width && this.mMode.getPhysicalHeight() == info.height && Float.floatToIntBits(this.mMode.getRefreshRate()) == Float.floatToIntBits(info.refreshRate);
        }

        public String toString() {
            return "DisplayModeRecord{mMode=" + this.mMode + "}";
        }
    }

    private final class PhysicalDisplayEventReceiver extends DisplayEventReceiver {
        PhysicalDisplayEventReceiver(Looper looper) {
            super(looper, 0);
        }

        public void onHotplug(long timestampNanos, long physicalDisplayId, boolean connected) {
            synchronized (LocalDisplayAdapter.this.getSyncRoot()) {
                if (connected) {
                    LocalDisplayAdapter.this.tryConnectDisplayLocked(physicalDisplayId);
                } else {
                    LocalDisplayAdapter.this.tryDisconnectDisplayLocked(physicalDisplayId);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0064, code lost:
            return;
         */
        public void onConfigChanged(long timestampNanos, long physicalDisplayId, int physIndex) {
            if (LocalDisplayAdapter.DEBUG) {
                Slog.d(LocalDisplayAdapter.TAG, "onConfigChanged(timestampNanos=" + timestampNanos + ", physicalDisplayId=" + physicalDisplayId + ", physIndex=" + physIndex + ")");
            }
            synchronized (LocalDisplayAdapter.this.getSyncRoot()) {
                LocalDisplayDevice device = (LocalDisplayDevice) LocalDisplayAdapter.this.mDevices.get(physicalDisplayId);
                if (device != null) {
                    device.onActivePhysicalDisplayModeChangedLocked(physIndex);
                } else if (LocalDisplayAdapter.DEBUG) {
                    Slog.d(LocalDisplayAdapter.TAG, "Received config change for unhandled physical display: physicalDisplayId=" + physicalDisplayId);
                }
            }
        }
    }
}
