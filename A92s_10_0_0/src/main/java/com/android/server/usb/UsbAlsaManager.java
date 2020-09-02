package com.android.server.usb;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.media.IAudioService;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.alsa.AlsaCardsParser;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.usb.descriptors.UsbDescriptorParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import libcore.io.IoUtils;

public final class UsbAlsaManager {
    private static final String ALSA_DIRECTORY = "/dev/snd/";
    private static final boolean DEBUG = false;
    private static final String TAG = UsbAlsaManager.class.getSimpleName();
    private static final int USB_BLACKLIST_INPUT = 2;
    private static final int USB_BLACKLIST_OUTPUT = 1;
    private static final int USB_PRODUCTID_PS4CONTROLLER_ZCT1 = 1476;
    private static final int USB_PRODUCTID_PS4CONTROLLER_ZCT2 = 2508;
    private static final int USB_VENDORID_SONY = 1356;
    private static final boolean mIsSingleMode = true;
    static final List<BlackListEntry> sDeviceBlacklist = Arrays.asList(new BlackListEntry(USB_VENDORID_SONY, USB_PRODUCTID_PS4CONTROLLER_ZCT1, 1), new BlackListEntry(USB_VENDORID_SONY, USB_PRODUCTID_PS4CONTROLLER_ZCT2, 1));
    private final ArrayList<UsbAlsaDevice> mAlsaDevices = new ArrayList<>();
    private IAudioService mAudioService;
    private final AlsaCardsParser mCardsParser = new AlsaCardsParser();
    private final Context mContext;
    private final boolean mHasMidiFeature;
    private final HashMap<String, UsbMidiDevice> mMidiDevices = new HashMap<>();
    private UsbMidiDevice mPeripheralMidiDevice = null;
    private UsbAlsaDevice mSelectedDevice;

    private static class BlackListEntry {
        final int mFlags;
        final int mProductId;
        final int mVendorId;

        BlackListEntry(int vendorId, int productId, int flags) {
            this.mVendorId = vendorId;
            this.mProductId = productId;
            this.mFlags = flags;
        }
    }

    private static boolean isDeviceBlacklisted(int vendorId, int productId, int flags) {
        for (BlackListEntry entry : sDeviceBlacklist) {
            if (entry.mVendorId == vendorId && entry.mProductId == productId) {
                if ((entry.mFlags & flags) != 0) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    UsbAlsaManager(Context context) {
        this.mContext = context;
        this.mHasMidiFeature = context.getPackageManager().hasSystemFeature("android.software.midi");
    }

    public void systemReady() {
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
    }

    private synchronized void selectAlsaDevice(UsbAlsaDevice alsaDevice) {
        if (this.mSelectedDevice != null) {
            deselectAlsaDevice();
        }
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "usb_audio_automatic_routing_disabled", 0) == 0) {
            this.mSelectedDevice = alsaDevice;
            alsaDevice.start();
        }
    }

    private synchronized void deselectAlsaDevice() {
        if (this.mSelectedDevice != null) {
            this.mSelectedDevice.stop();
            this.mSelectedDevice = null;
        }
    }

    private int getAlsaDeviceListIndexFor(String deviceAddress) {
        for (int index = 0; index < this.mAlsaDevices.size(); index++) {
            if (this.mAlsaDevices.get(index).getDeviceAddress().equals(deviceAddress)) {
                return index;
            }
        }
        return -1;
    }

    private UsbAlsaDevice removeAlsaDeviceFromList(String deviceAddress) {
        int index = getAlsaDeviceListIndexFor(deviceAddress);
        if (index > -1) {
            return this.mAlsaDevices.remove(index);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public UsbAlsaDevice selectDefaultDevice() {
        if (this.mAlsaDevices.size() <= 0) {
            return null;
        }
        UsbAlsaDevice alsaDevice = this.mAlsaDevices.get(0);
        if (alsaDevice != null) {
            selectAlsaDevice(alsaDevice);
        }
        return alsaDevice;
    }

    /* access modifiers changed from: package-private */
    public void usbDeviceAdded(String deviceAddress, UsbDevice usbDevice, UsbDescriptorParser parser) {
        String name;
        this.mCardsParser.scan();
        AlsaCardsParser.AlsaCardRecord cardRec = this.mCardsParser.findCardNumFor(deviceAddress);
        if (cardRec != null) {
            boolean hasOutput = true;
            boolean hasInput = parser.hasInput() && !isDeviceBlacklisted(usbDevice.getVendorId(), usbDevice.getProductId(), 2);
            if (!parser.hasOutput() || isDeviceBlacklisted(usbDevice.getVendorId(), usbDevice.getProductId(), 1)) {
                hasOutput = false;
            }
            if (hasInput || hasOutput) {
                boolean isInputHeadset = parser.isInputHeadset();
                boolean isOutputHeadset = parser.isOutputHeadset();
                IAudioService iAudioService = this.mAudioService;
                if (iAudioService == null) {
                    Slog.e(TAG, "no AudioService");
                    return;
                }
                UsbAlsaDevice alsaDevice = new UsbAlsaDevice(iAudioService, cardRec.getCardNum(), 0, deviceAddress, hasOutput, hasInput, isInputHeadset, isOutputHeadset);
                alsaDevice.setDeviceNameAndDescription(cardRec.getCardName(), cardRec.getCardDescription());
                this.mAlsaDevices.add(0, alsaDevice);
                selectAlsaDevice(alsaDevice);
            }
            if (parser.hasMIDIInterface() && this.mHasMidiFeature) {
                Bundle properties = new Bundle();
                String manufacturer = usbDevice.getManufacturerName();
                String product = usbDevice.getProductName();
                String version = usbDevice.getVersion();
                if (manufacturer == null || manufacturer.isEmpty()) {
                    name = product;
                } else if (product == null || product.isEmpty()) {
                    name = manufacturer;
                } else {
                    name = manufacturer + StringUtils.SPACE + product;
                }
                properties.putString(com.android.server.pm.Settings.ATTR_NAME, name);
                properties.putString("manufacturer", manufacturer);
                properties.putString("product", product);
                properties.putString("version", version);
                properties.putString("serial_number", usbDevice.getSerialNumber());
                properties.putInt("alsa_card", cardRec.getCardNum());
                properties.putInt("alsa_device", 0);
                properties.putParcelable("usb_device", usbDevice);
                UsbMidiDevice usbMidiDevice = UsbMidiDevice.create(this.mContext, properties, cardRec.getCardNum(), 0);
                if (usbMidiDevice != null) {
                    this.mMidiDevices.put(deviceAddress, usbMidiDevice);
                }
            }
            logDevices("deviceAdded()");
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void usbDeviceRemoved(String deviceAddress) {
        UsbAlsaDevice alsaDevice = removeAlsaDeviceFromList(deviceAddress);
        String str = TAG;
        Slog.i(str, "USB Audio Device Removed: " + alsaDevice);
        if (alsaDevice != null && alsaDevice == this.mSelectedDevice) {
            deselectAlsaDevice();
            selectDefaultDevice();
        }
        UsbMidiDevice usbMidiDevice = this.mMidiDevices.remove(deviceAddress);
        if (usbMidiDevice != null) {
            String str2 = TAG;
            Slog.i(str2, "USB MIDI Device Removed: " + usbMidiDevice);
            IoUtils.closeQuietly(usbMidiDevice);
        }
        logDevices("usbDeviceRemoved()");
    }

    /* access modifiers changed from: package-private */
    public void setPeripheralMidiState(boolean enabled, int card, int device) {
        UsbMidiDevice usbMidiDevice;
        if (this.mHasMidiFeature) {
            if (enabled && this.mPeripheralMidiDevice == null) {
                Bundle properties = new Bundle();
                Resources r = this.mContext.getResources();
                properties.putString(com.android.server.pm.Settings.ATTR_NAME, r.getString(17041168));
                properties.putString("manufacturer", r.getString(17041167));
                properties.putString("product", r.getString(17041169));
                properties.putInt("alsa_card", card);
                properties.putInt("alsa_device", device);
                this.mPeripheralMidiDevice = UsbMidiDevice.create(this.mContext, properties, card, device);
            } else if (!enabled && (usbMidiDevice = this.mPeripheralMidiDevice) != null) {
                IoUtils.closeQuietly(usbMidiDevice);
                this.mPeripheralMidiDevice = null;
            }
        }
    }

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        dump.write("cards_parser", 1120986464257L, this.mCardsParser.getScanStatus());
        Iterator<UsbAlsaDevice> it = this.mAlsaDevices.iterator();
        while (it.hasNext()) {
            it.next().dump(dump, "alsa_devices", 2246267895810L);
        }
        for (String deviceAddr : this.mMidiDevices.keySet()) {
            this.mMidiDevices.get(deviceAddr).dump(deviceAddr, dump, "midi_devices", 2246267895811L);
        }
        dump.end(token);
    }

    public void logDevicesList(String title) {
    }

    public void logDevices(String title) {
    }
}
