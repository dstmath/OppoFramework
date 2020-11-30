package com.android.server.usb.descriptors;

import android.hardware.usb.UsbDeviceConnection;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.usb.descriptors.report.UsbStrings;
import vendor.oppo.hardware.nfc.V1_0.OppoNfcChipVersion;

public final class UsbBinaryParser {
    private static final boolean LOGGING = false;
    private static final String TAG = "UsbBinaryParser";

    private void dumpDescriptor(ByteStream stream, int length, byte type, StringBuilder builder) {
        builder.append("<p>");
        builder.append("<b> l: " + length + " t:0x" + Integer.toHexString(type) + StringUtils.SPACE + UsbStrings.getDescriptorName(type) + "</b><br>");
        for (int index = 2; index < length; index++) {
            builder.append("0x" + Integer.toHexString(stream.getByte() & OppoNfcChipVersion.NONE) + StringUtils.SPACE);
        }
        builder.append("</p>");
    }

    public void parseDescriptors(UsbDeviceConnection connection, byte[] descriptors, StringBuilder builder) {
        builder.append("<tt>");
        ByteStream stream = new ByteStream(descriptors);
        while (stream.available() > 0) {
            dumpDescriptor(stream, stream.getByte() & OppoNfcChipVersion.NONE, stream.getByte(), builder);
        }
        builder.append("</tt>");
    }
}
