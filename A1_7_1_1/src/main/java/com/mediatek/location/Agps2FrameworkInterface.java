package com.mediatek.location;

import com.mediatek.socket.base.SocketUtils;
import com.mediatek.socket.base.SocketUtils.BaseBuffer;
import com.mediatek.socket.base.SocketUtils.ProtocolHandler;
import com.mediatek.socket.base.SocketUtils.UdpServerInterface;
import com.mediatek.socket.base.UdpClient;

public class Agps2FrameworkInterface {
    public static final int ACQUIRE_WAKE_LOCK = 1;
    public static final int IS_EXIST = 0;
    public static final int MAX_BUFF_SIZE = 271;
    public static final int PROTOCOL_TYPE = 301;
    public static final int RELEASE_DEDICATED_APN = 4;
    public static final int RELEASE_WAKE_LOCK = 2;
    public static final int REMOVE_GPS_ICON = 6;
    public static final int REQUEST_DEDICATED_APN_AND_DNS_QUERY = 3;
    public static final int REQUEST_GPS_ICON = 5;

    public static abstract class Agps2FrameworkInterfaceReceiver implements ProtocolHandler {
        public abstract void acquireWakeLock();

        public abstract void isExist();

        public abstract void releaseDedicatedApn();

        public abstract void releaseWakeLock();

        public abstract void removeGpsIcon();

        public abstract void requestDedicatedApnAndDnsQuery(String str, boolean z, boolean z2);

        public abstract void requestGpsIcon();

        public boolean readAndDecode(UdpServerInterface server) {
            if (server.read()) {
                return decode(server);
            }
            return false;
        }

        public int getProtocolType() {
            return Agps2FrameworkInterface.PROTOCOL_TYPE;
        }

        public boolean decode(UdpServerInterface server) {
            BaseBuffer buff = server.getBuff();
            buff.setOffset(4);
            switch (buff.getInt()) {
                case 0:
                    isExist();
                    return true;
                case 1:
                    acquireWakeLock();
                    return true;
                case 2:
                    releaseWakeLock();
                    return true;
                case 3:
                    requestDedicatedApnAndDnsQuery(buff.getString(), buff.getBool(), buff.getBool());
                    return true;
                case 4:
                    releaseDedicatedApn();
                    return true;
                case 5:
                    requestGpsIcon();
                    return true;
                case 6:
                    removeGpsIcon();
                    return true;
                default:
                    return false;
            }
        }
    }

    public static class Agps2FrameworkInterfaceSender {
        public boolean isExist(UdpClient client) {
            if (!client.connect()) {
                return false;
            }
            BaseBuffer buff = client.getBuff();
            buff.putInt(Agps2FrameworkInterface.PROTOCOL_TYPE);
            buff.putInt(0);
            boolean _ret = client.write();
            client.close();
            return _ret;
        }

        public boolean acquireWakeLock(UdpClient client) {
            if (!client.connect()) {
                return false;
            }
            BaseBuffer buff = client.getBuff();
            buff.putInt(Agps2FrameworkInterface.PROTOCOL_TYPE);
            buff.putInt(1);
            boolean _ret = client.write();
            client.close();
            return _ret;
        }

        public boolean releaseWakeLock(UdpClient client) {
            if (!client.connect()) {
                return false;
            }
            BaseBuffer buff = client.getBuff();
            buff.putInt(Agps2FrameworkInterface.PROTOCOL_TYPE);
            buff.putInt(2);
            boolean _ret = client.write();
            client.close();
            return _ret;
        }

        public boolean requestDedicatedApnAndDnsQuery(UdpClient client, String fqdn, boolean isEmergencySupl, boolean isApnEnabled) {
            if (!client.connect()) {
                return false;
            }
            BaseBuffer buff = client.getBuff();
            buff.putInt(Agps2FrameworkInterface.PROTOCOL_TYPE);
            buff.putInt(3);
            SocketUtils.assertSize(fqdn, 256, 0);
            buff.putString(fqdn);
            buff.putBool(isEmergencySupl);
            buff.putBool(isApnEnabled);
            boolean _ret = client.write();
            client.close();
            return _ret;
        }

        public boolean releaseDedicatedApn(UdpClient client) {
            if (!client.connect()) {
                return false;
            }
            BaseBuffer buff = client.getBuff();
            buff.putInt(Agps2FrameworkInterface.PROTOCOL_TYPE);
            buff.putInt(4);
            boolean _ret = client.write();
            client.close();
            return _ret;
        }

        public boolean requestGpsIcon(UdpClient client) {
            if (!client.connect()) {
                return false;
            }
            BaseBuffer buff = client.getBuff();
            buff.putInt(Agps2FrameworkInterface.PROTOCOL_TYPE);
            buff.putInt(5);
            boolean _ret = client.write();
            client.close();
            return _ret;
        }

        public boolean removeGpsIcon(UdpClient client) {
            if (!client.connect()) {
                return false;
            }
            BaseBuffer buff = client.getBuff();
            buff.putInt(Agps2FrameworkInterface.PROTOCOL_TYPE);
            buff.putInt(6);
            boolean _ret = client.write();
            client.close();
            return _ret;
        }
    }
}
