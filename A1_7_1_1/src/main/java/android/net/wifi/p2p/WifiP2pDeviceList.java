package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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
public class WifiP2pDeviceList implements Parcelable {
    public static final Creator<WifiP2pDeviceList> CREATOR = null;
    private final HashMap<String, WifiP2pDevice> mDevices;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pDeviceList.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pDeviceList.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pDeviceList.<clinit>():void");
    }

    public WifiP2pDeviceList() {
        this.mDevices = new HashMap();
    }

    public WifiP2pDeviceList(WifiP2pDeviceList source) {
        this.mDevices = new HashMap();
        if (source != null) {
            for (WifiP2pDevice d : source.getDeviceList()) {
                this.mDevices.put(d.deviceAddress, new WifiP2pDevice(d));
            }
        }
    }

    public WifiP2pDeviceList(ArrayList<WifiP2pDevice> devices) {
        this.mDevices = new HashMap();
        for (WifiP2pDevice device : devices) {
            if (device.deviceAddress != null) {
                this.mDevices.put(device.deviceAddress, new WifiP2pDevice(device));
            }
        }
    }

    private void validateDevice(WifiP2pDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Null device");
        } else if (TextUtils.isEmpty(device.deviceAddress)) {
            throw new IllegalArgumentException("Empty deviceAddress");
        }
    }

    private void validateDeviceAddress(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) {
            throw new IllegalArgumentException("Empty deviceAddress");
        }
    }

    public boolean clear() {
        if (this.mDevices.isEmpty()) {
            return false;
        }
        this.mDevices.clear();
        return true;
    }

    public void update(WifiP2pDevice device) {
        updateSupplicantDetails(device);
        ((WifiP2pDevice) this.mDevices.get(device.deviceAddress)).status = device.status;
    }

    public void updateSupplicantDetails(WifiP2pDevice device) {
        validateDevice(device);
        WifiP2pDevice d = (WifiP2pDevice) this.mDevices.get(device.deviceAddress);
        if (d != null) {
            d.interfaceAddress = device.interfaceAddress;
            d.deviceName = device.deviceName;
            d.primaryDeviceType = device.primaryDeviceType;
            d.secondaryDeviceType = device.secondaryDeviceType;
            d.wpsConfigMethodsSupported = device.wpsConfigMethodsSupported;
            d.deviceCapability = device.deviceCapability;
            d.groupCapability = device.groupCapability;
            d.wfdInfo = device.wfdInfo;
            return;
        }
        this.mDevices.put(device.deviceAddress, device);
    }

    public void updateGroupCapability(String deviceAddress, int groupCapab) {
        validateDeviceAddress(deviceAddress);
        WifiP2pDevice d = (WifiP2pDevice) this.mDevices.get(deviceAddress);
        if (d != null) {
            d.groupCapability = groupCapab;
        }
    }

    public void updateStatus(String deviceAddress, int status) {
        validateDeviceAddress(deviceAddress);
        WifiP2pDevice d = (WifiP2pDevice) this.mDevices.get(deviceAddress);
        if (d != null) {
            d.status = status;
        }
    }

    public WifiP2pDevice get(String deviceAddress) {
        validateDeviceAddress(deviceAddress);
        return (WifiP2pDevice) this.mDevices.get(deviceAddress);
    }

    public boolean remove(WifiP2pDevice device) {
        boolean z = false;
        try {
            validateDevice(device);
            if (this.mDevices.remove(device.deviceAddress) != null) {
                z = true;
            }
            return z;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    public WifiP2pDevice remove(String deviceAddress) {
        try {
            validateDeviceAddress(deviceAddress);
            return (WifiP2pDevice) this.mDevices.remove(deviceAddress);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean remove(WifiP2pDeviceList list) {
        boolean ret = false;
        for (WifiP2pDevice d : list.mDevices.values()) {
            if (remove(d)) {
                ret = true;
            }
        }
        return ret;
    }

    public Collection<WifiP2pDevice> getDeviceList() {
        return Collections.unmodifiableCollection(this.mDevices.values());
    }

    public boolean containsPeer(String address) {
        return this.mDevices.containsKey(address);
    }

    public boolean isGroupOwner(String deviceAddress) {
        validateDeviceAddress(deviceAddress);
        WifiP2pDevice device = (WifiP2pDevice) this.mDevices.get(deviceAddress);
        if (device != null) {
            return device.isGroupOwner();
        }
        throw new IllegalArgumentException("Device not found " + deviceAddress);
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        for (WifiP2pDevice device : this.mDevices.values()) {
            sbuf.append("\n").append(device);
        }
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDevices.size());
        for (WifiP2pDevice device : this.mDevices.values()) {
            dest.writeParcelable(device, flags);
        }
    }
}
