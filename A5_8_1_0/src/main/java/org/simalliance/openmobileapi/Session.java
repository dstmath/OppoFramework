package org.simalliance.openmobileapi;

import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.simalliance.openmobileapi.service.ISmartcardServiceChannel;
import org.simalliance.openmobileapi.service.ISmartcardServiceSession;
import org.simalliance.openmobileapi.service.SmartcardError;

public class Session {
    public static final String _TAG = "SmartcardService - Session";
    public int mHandle;
    private final Object mLock = new Object();
    private final Reader mReader;
    private final SEService mService;
    private final ISmartcardServiceSession mSession;

    Session(SEService service, ISmartcardServiceSession session, Reader reader) {
        this.mService = service;
        this.mReader = reader;
        this.mSession = session;
    }

    Session(SEService service, ISmartcardServiceSession session, Reader reader, int sessionHandle) {
        this.mService = service;
        this.mReader = reader;
        this.mSession = session;
        this.mHandle = sessionHandle;
    }

    public Reader getReader() {
        return this.mReader;
    }

    public byte[] getATR() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else {
            try {
                return this.mSession.getAtr();
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    public void close() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession != null) {
            synchronized (this.mLock) {
                try {
                    SmartcardError error = new SmartcardError();
                    this.mSession.close(error);
                    if (error.isSet()) {
                        error.throwException();
                    }
                } catch (Exception e) {
                    Log.e(_TAG, "Error closing session", e);
                }
            }
            return;
        } else {
            return;
        }
    }

    public boolean isClosed() {
        try {
            return this.mSession != null ? this.mSession.isClosed() : true;
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void closeChannels() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession != null) {
            synchronized (this.mLock) {
                try {
                    SmartcardError error = new SmartcardError();
                    this.mSession.closeChannels(error);
                    if (error.isSet()) {
                        error.throwException();
                    }
                } catch (Exception e) {
                    Log.e(_TAG, "Error closing channels", e);
                }
            }
            return;
        } else {
            return;
        }
    }

    public int getHandle() {
        int handle = 0;
        try {
            return this.mSession.getHandle();
        } catch (Exception e) {
            Log.e(_TAG, "unable to get the session handle :" + e.getMessage());
            return handle;
        }
    }

    public void setHandle(int handle) {
        this.mHandle = handle;
    }

    public Channel openBasicChannel(byte[] aid, byte p2) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (getReader() == null) {
            throw new IllegalStateException("reader must not be null");
        } else {
            synchronized (this.mLock) {
                try {
                    SmartcardError error = new SmartcardError();
                    ISmartcardServiceChannel channel = this.mSession.openBasicChannel(aid, p2, this.mReader.getSEService().getCallback(), error);
                    if (error.isSet()) {
                        error.throwException();
                    }
                    if (channel == null) {
                        return null;
                    }
                    Channel channel2 = new Channel(this.mService, this, channel);
                    return channel2;
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }

    public Channel openBasicChannel(byte[] aid) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        return openBasicChannel(aid, (byte) 0);
    }

    public Channel openLogicalChannel(byte[] aid, byte p2) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        if (this.mReader.getName().startsWith("SIM") && aid == null) {
            Log.e(_TAG, "NULL AID not supported on " + this.mReader.getName());
            return null;
        } else if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (getReader() == null) {
            throw new IllegalStateException("reader must not be null");
        } else {
            synchronized (this.mLock) {
                try {
                    SmartcardError error = new SmartcardError();
                    ISmartcardServiceChannel channel = this.mSession.openLogicalChannel(aid, p2, this.mReader.getSEService().getCallback(), error);
                    if (error.isSet()) {
                        error.throwException();
                    }
                    if (channel == null) {
                        return null;
                    }
                    Channel channel2 = new Channel(this.mService, this, channel);
                    return channel2;
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }

    public Channel openLogicalChannel(byte[] aid) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        return openLogicalChannel(aid, (byte) 0);
    }
}
