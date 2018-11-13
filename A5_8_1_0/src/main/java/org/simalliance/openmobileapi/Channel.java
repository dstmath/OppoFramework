package org.simalliance.openmobileapi;

import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import org.simalliance.openmobileapi.service.ISmartcardServiceChannel;
import org.simalliance.openmobileapi.service.SmartcardError;

public class Channel {
    public static final String _TAG = "SmartcardService - Channel";
    private final ISmartcardServiceChannel mChannel;
    private final Object mLock = new Object();
    private final SEService mService;
    private Session mSession;

    Channel(SEService service, Session session, ISmartcardServiceChannel channel) {
        this.mService = service;
        this.mSession = session;
        this.mChannel = channel;
    }

    public void close() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            Log.e(_TAG, "close(): throw IllegalStateException");
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else if (!isClosed()) {
            synchronized (this.mLock) {
                try {
                    SmartcardError error = new SmartcardError();
                    this.mChannel.close(error);
                    if (error.isSet()) {
                        error.throwException();
                    }
                } catch (Exception e) {
                    Log.e(_TAG, "Error closing channel", e);
                }
            }
            return;
        } else {
            return;
        }
    }

    public boolean isClosed() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            try {
                return this.mChannel.isClosed();
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    public boolean isBasicChannel() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            try {
                return this.mChannel.isBasicChannel();
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    public byte[] transmit(byte[] command) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NullPointerException {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            byte[] response;
            synchronized (this.mLock) {
                try {
                    SmartcardError error = new SmartcardError();
                    response = this.mChannel.transmit(command, error);
                    if (error.isSet()) {
                        error.throwException();
                    }
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
            return response;
        }
    }

    public Session getSession() {
        return this.mSession;
    }

    public byte[] getSelectResponse() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            try {
                byte[] response = this.mChannel.getSelectResponse();
                if (response == null || response.length != 0) {
                    return response;
                }
                return null;
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    public boolean selectNext() throws IOException, IllegalStateException, UnsupportedOperationException {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            try {
                if (this.mChannel.isClosed()) {
                    throw new IllegalStateException("channel is closed");
                }
                boolean response;
                synchronized (this.mLock) {
                    try {
                        SmartcardError error = new SmartcardError();
                        response = this.mChannel.selectNext(error);
                        if (error.isSet()) {
                            error.throwException();
                        }
                    } catch (RemoteException e) {
                        throw new IllegalStateException(e.getMessage());
                    }
                }
                return response;
            } catch (RemoteException e2) {
                throw new IllegalStateException(e2.getMessage());
            }
        }
    }
}
