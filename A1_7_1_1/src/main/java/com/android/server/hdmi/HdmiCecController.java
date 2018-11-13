package com.android.server.hdmi;

import android.hardware.hdmi.HdmiPortInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Predicate;
import com.android.server.hdmi.HdmiAnnotations.IoThreadOnly;
import com.android.server.hdmi.HdmiAnnotations.ServiceThreadOnly;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
final class HdmiCecController {
    private static final byte[] EMPTY_BODY = null;
    private static final int NUM_LOGICAL_ADDRESS = 16;
    private static final String TAG = "HdmiCecController";
    private Handler mControlHandler;
    private Handler mIoHandler;
    private final SparseArray<HdmiCecLocalDevice> mLocalDevices;
    private volatile long mNativePtr;
    private final Predicate<Integer> mRemoteDeviceAddressPredicate;
    private final HdmiControlService mService;
    private final Predicate<Integer> mSystemAudioAddressPredicate;

    interface AllocateAddressCallback {
        void onAllocated(int i, int i2);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.hdmi.HdmiCecController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.hdmi.HdmiCecController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiCecController.<clinit>():void");
    }

    private static native int nativeAddLogicalAddress(long j, int i);

    private static native void nativeClearLogicalAddress(long j);

    private static native int nativeGetPhysicalAddress(long j);

    private static native HdmiPortInfo[] nativeGetPortInfos(long j);

    private static native int nativeGetVendorId(long j);

    private static native int nativeGetVersion(long j);

    private static native long nativeInit(HdmiCecController hdmiCecController, MessageQueue messageQueue);

    private static native boolean nativeIsConnected(long j, int i);

    private static native int nativeSendCecCommand(long j, int i, int i2, byte[] bArr);

    private static native void nativeSetAudioReturnChannel(long j, int i, boolean z);

    private static native void nativeSetOption(long j, int i, int i2);

    private HdmiCecController(HdmiControlService service) {
        this.mRemoteDeviceAddressPredicate = new Predicate<Integer>() {
            public boolean apply(Integer address) {
                return !HdmiCecController.this.isAllocatedLocalDeviceAddress(address.intValue());
            }
        };
        this.mSystemAudioAddressPredicate = new Predicate<Integer>() {
            public boolean apply(Integer address) {
                return HdmiUtils.getTypeFromAddress(address.intValue()) == 5;
            }
        };
        this.mLocalDevices = new SparseArray();
        this.mService = service;
    }

    static HdmiCecController create(HdmiControlService service) {
        HdmiCecController controller = new HdmiCecController(service);
        long nativePtr = nativeInit(controller, service.getServiceLooper().getQueue());
        if (nativePtr == 0) {
            return null;
        }
        controller.init(nativePtr);
        return controller;
    }

    private void init(long nativePtr) {
        this.mIoHandler = new Handler(this.mService.getIoLooper());
        this.mControlHandler = new Handler(this.mService.getServiceLooper());
        this.mNativePtr = nativePtr;
    }

    @ServiceThreadOnly
    void addLocalDevice(int deviceType, HdmiCecLocalDevice device) {
        assertRunOnServiceThread();
        this.mLocalDevices.put(deviceType, device);
    }

    @ServiceThreadOnly
    void allocateLogicalAddress(final int deviceType, final int preferredAddress, final AllocateAddressCallback callback) {
        assertRunOnServiceThread();
        runOnIoThread(new Runnable() {
            public void run() {
                HdmiCecController.this.handleAllocateLogicalAddress(deviceType, preferredAddress, callback);
            }
        });
    }

    @IoThreadOnly
    private void handleAllocateLogicalAddress(final int deviceType, int preferredAddress, final AllocateAddressCallback callback) {
        int i;
        assertRunOnIoThread();
        int startAddress = preferredAddress;
        if (preferredAddress == 15) {
            for (i = 0; i < 16; i++) {
                if (deviceType == HdmiUtils.getTypeFromAddress(i)) {
                    startAddress = i;
                    break;
                }
            }
        }
        int logicalAddress = 15;
        for (i = 0; i < 16; i++) {
            int curAddress = (startAddress + i) % 16;
            if (curAddress != 15 && deviceType == HdmiUtils.getTypeFromAddress(curAddress)) {
                int failedPollingCount = 0;
                for (int j = 0; j < 3; j++) {
                    if (!sendPollMessage(curAddress, curAddress, 1)) {
                        failedPollingCount++;
                    }
                }
                if (failedPollingCount * 2 > 3) {
                    logicalAddress = curAddress;
                    break;
                }
            }
        }
        final int assignedAddress = logicalAddress;
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(deviceType);
        objArr[1] = Integer.valueOf(preferredAddress);
        objArr[2] = Integer.valueOf(assignedAddress);
        HdmiLogger.debug("New logical address for device [%d]: [preferred:%d, assigned:%d]", objArr);
        if (callback != null) {
            runOnServiceThread(new Runnable() {
                public void run() {
                    callback.onAllocated(deviceType, assignedAddress);
                }
            });
        }
    }

    private static byte[] buildBody(int opcode, byte[] params) {
        byte[] body = new byte[(params.length + 1)];
        body[0] = (byte) opcode;
        System.arraycopy(params, 0, body, 1, params.length);
        return body;
    }

    HdmiPortInfo[] getPortInfos() {
        return nativeGetPortInfos(this.mNativePtr);
    }

    HdmiCecLocalDevice getLocalDevice(int deviceType) {
        return (HdmiCecLocalDevice) this.mLocalDevices.get(deviceType);
    }

    @ServiceThreadOnly
    int addLogicalAddress(int newLogicalAddress) {
        assertRunOnServiceThread();
        if (HdmiUtils.isValidAddress(newLogicalAddress)) {
            return nativeAddLogicalAddress(this.mNativePtr, newLogicalAddress);
        }
        return -1;
    }

    @ServiceThreadOnly
    void clearLogicalAddress() {
        assertRunOnServiceThread();
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            ((HdmiCecLocalDevice) this.mLocalDevices.valueAt(i)).clearAddress();
        }
        nativeClearLogicalAddress(this.mNativePtr);
    }

    @ServiceThreadOnly
    void clearLocalDevices() {
        assertRunOnServiceThread();
        this.mLocalDevices.clear();
    }

    @ServiceThreadOnly
    int getPhysicalAddress() {
        assertRunOnServiceThread();
        return nativeGetPhysicalAddress(this.mNativePtr);
    }

    @ServiceThreadOnly
    int getVersion() {
        assertRunOnServiceThread();
        return nativeGetVersion(this.mNativePtr);
    }

    @ServiceThreadOnly
    int getVendorId() {
        assertRunOnServiceThread();
        return nativeGetVendorId(this.mNativePtr);
    }

    @ServiceThreadOnly
    void setOption(int flag, int value) {
        assertRunOnServiceThread();
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(flag);
        objArr[1] = Integer.valueOf(value);
        HdmiLogger.debug("setOption: [flag:%d, value:%d]", objArr);
        nativeSetOption(this.mNativePtr, flag, value);
    }

    @ServiceThreadOnly
    void setAudioReturnChannel(int port, boolean enabled) {
        assertRunOnServiceThread();
        nativeSetAudioReturnChannel(this.mNativePtr, port, enabled);
    }

    @ServiceThreadOnly
    boolean isConnected(int port) {
        assertRunOnServiceThread();
        return nativeIsConnected(this.mNativePtr, port);
    }

    @ServiceThreadOnly
    void pollDevices(DevicePollingCallback callback, int sourceAddress, int pickStrategy, int retryCount) {
        assertRunOnServiceThread();
        runDevicePolling(sourceAddress, pickPollCandidates(pickStrategy), retryCount, callback, new ArrayList());
    }

    @ServiceThreadOnly
    List<HdmiCecLocalDevice> getLocalDeviceList() {
        assertRunOnServiceThread();
        return HdmiUtils.sparseArrayToList(this.mLocalDevices);
    }

    private List<Integer> pickPollCandidates(int pickStrategy) {
        Predicate<Integer> pickPredicate;
        switch (pickStrategy & 3) {
            case 2:
                pickPredicate = this.mSystemAudioAddressPredicate;
                break;
            default:
                pickPredicate = this.mRemoteDeviceAddressPredicate;
                break;
        }
        int iterationStrategy = pickStrategy & 196608;
        LinkedList<Integer> pollingCandidates = new LinkedList();
        int i;
        switch (iterationStrategy) {
            case DumpState.DUMP_INSTALLS /*65536*/:
                for (i = 0; i <= 14; i++) {
                    if (pickPredicate.apply(Integer.valueOf(i))) {
                        pollingCandidates.add(Integer.valueOf(i));
                    }
                }
                break;
            default:
                for (i = 14; i >= 0; i--) {
                    if (pickPredicate.apply(Integer.valueOf(i))) {
                        pollingCandidates.add(Integer.valueOf(i));
                    }
                }
                break;
        }
        return pollingCandidates;
    }

    @ServiceThreadOnly
    private boolean isAllocatedLocalDeviceAddress(int address) {
        assertRunOnServiceThread();
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            if (((HdmiCecLocalDevice) this.mLocalDevices.valueAt(i)).isAddressOf(address)) {
                return true;
            }
        }
        return false;
    }

    @ServiceThreadOnly
    private void runDevicePolling(int sourceAddress, List<Integer> candidates, int retryCount, DevicePollingCallback callback, List<Integer> allocated) {
        assertRunOnServiceThread();
        if (candidates.isEmpty()) {
            if (callback != null) {
                Object[] objArr = new Object[1];
                objArr[0] = allocated.toString();
                HdmiLogger.debug("[P]:AllocatedAddress=%s", objArr);
                callback.onPollingFinished(allocated);
            }
            return;
        }
        final Integer candidate = (Integer) candidates.remove(0);
        final int i = sourceAddress;
        final int i2 = retryCount;
        final List<Integer> list = allocated;
        final List<Integer> list2 = candidates;
        final DevicePollingCallback devicePollingCallback = callback;
        runOnIoThread(new Runnable() {
            public void run() {
                if (HdmiCecController.this.sendPollMessage(i, candidate.intValue(), i2)) {
                    list.add(candidate);
                }
                HdmiCecController hdmiCecController = HdmiCecController.this;
                final int i = i;
                final List list = list2;
                final int i2 = i2;
                final DevicePollingCallback devicePollingCallback = devicePollingCallback;
                final List list2 = list;
                hdmiCecController.runOnServiceThread(new Runnable() {
                    public void run() {
                        HdmiCecController.this.runDevicePolling(i, list, i2, devicePollingCallback, list2);
                    }
                });
            }
        });
    }

    @IoThreadOnly
    private boolean sendPollMessage(int sourceAddress, int destinationAddress, int retryCount) {
        assertRunOnIoThread();
        for (int i = 0; i < retryCount; i++) {
            if (nativeSendCecCommand(this.mNativePtr, sourceAddress, destinationAddress, EMPTY_BODY) == 0) {
                return true;
            }
        }
        return false;
    }

    private void assertRunOnIoThread() {
        if (Looper.myLooper() != this.mIoHandler.getLooper()) {
            throw new IllegalStateException("Should run on io thread.");
        }
    }

    private void assertRunOnServiceThread() {
        if (Looper.myLooper() != this.mControlHandler.getLooper()) {
            throw new IllegalStateException("Should run on service thread.");
        }
    }

    private void runOnIoThread(Runnable runnable) {
        this.mIoHandler.post(runnable);
    }

    private void runOnServiceThread(Runnable runnable) {
        this.mControlHandler.post(runnable);
    }

    @ServiceThreadOnly
    void flush(final Runnable runnable) {
        assertRunOnServiceThread();
        runOnIoThread(new Runnable() {
            public void run() {
                HdmiCecController.this.runOnServiceThread(runnable);
            }
        });
    }

    private boolean isAcceptableAddress(int address) {
        if (address == 15) {
            return true;
        }
        return isAllocatedLocalDeviceAddress(address);
    }

    @ServiceThreadOnly
    private void onReceiveCommand(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!isAcceptableAddress(message.getDestination()) || !this.mService.handleCecCommand(message)) {
            maySendFeatureAbortCommand(message, 0);
        }
    }

    @ServiceThreadOnly
    void maySendFeatureAbortCommand(HdmiCecMessage message, int reason) {
        assertRunOnServiceThread();
        int src = message.getDestination();
        int dest = message.getSource();
        if (src != 15 && dest != 15) {
            int originalOpcode = message.getOpcode();
            if (originalOpcode != 0) {
                sendCommand(HdmiCecMessageBuilder.buildFeatureAbortCommand(src, dest, originalOpcode, reason));
            }
        }
    }

    @ServiceThreadOnly
    void sendCommand(HdmiCecMessage cecMessage) {
        assertRunOnServiceThread();
        sendCommand(cecMessage, null);
    }

    @ServiceThreadOnly
    void sendCommand(final HdmiCecMessage cecMessage, final SendMessageCallback callback) {
        assertRunOnServiceThread();
        runOnIoThread(new Runnable() {
            public void run() {
                final int errorCode;
                HdmiLogger.debug("[S]:" + cecMessage, new Object[0]);
                byte[] body = HdmiCecController.buildBody(cecMessage.getOpcode(), cecMessage.getParams());
                int i = 0;
                while (true) {
                    errorCode = HdmiCecController.nativeSendCecCommand(HdmiCecController.this.mNativePtr, cecMessage.getSource(), cecMessage.getDestination(), body);
                    if (errorCode != 0) {
                        int i2 = i + 1;
                        if (i >= 1) {
                            break;
                        }
                        i = i2;
                    } else {
                        break;
                    }
                }
                int finalError = errorCode;
                if (errorCode != 0) {
                    Slog.w(HdmiCecController.TAG, "Failed to send " + cecMessage);
                }
                if (callback != null) {
                    HdmiCecController hdmiCecController = HdmiCecController.this;
                    final SendMessageCallback sendMessageCallback = callback;
                    hdmiCecController.runOnServiceThread(new Runnable() {
                        public void run() {
                            sendMessageCallback.onSendCompleted(errorCode);
                        }
                    });
                }
            }
        });
    }

    @ServiceThreadOnly
    private void handleIncomingCecCommand(int srcAddress, int dstAddress, byte[] body) {
        assertRunOnServiceThread();
        HdmiCecMessage command = HdmiCecMessageBuilder.of(srcAddress, dstAddress, body);
        HdmiLogger.debug("[R]:" + command, new Object[0]);
        onReceiveCommand(command);
    }

    @ServiceThreadOnly
    private void handleHotplug(int port, boolean connected) {
        assertRunOnServiceThread();
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(port);
        objArr[1] = Boolean.valueOf(connected);
        HdmiLogger.debug("Hotplug event:[port:%d, connected:%b]", objArr);
        this.mService.onHotplug(port, connected);
    }

    void dump(IndentingPrintWriter pw) {
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            pw.println("HdmiCecLocalDevice #" + i + ":");
            pw.increaseIndent();
            ((HdmiCecLocalDevice) this.mLocalDevices.valueAt(i)).dump(pw);
            pw.decreaseIndent();
        }
    }
}
