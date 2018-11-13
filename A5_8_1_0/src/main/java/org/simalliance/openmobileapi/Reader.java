package org.simalliance.openmobileapi;

import android.os.RemoteException;
import java.io.IOException;
import org.simalliance.openmobileapi.service.ISmartcardServiceReader;
import org.simalliance.openmobileapi.service.ISmartcardServiceSession;
import org.simalliance.openmobileapi.service.SmartcardError;

public class Reader {
    public static final String TAG = "SmartcardService - Reader";
    private final Object mLock = new Object();
    private final String mName;
    private ISmartcardServiceReader mReader;
    private final SEService mService;

    Reader(SEService service, String name) {
        this.mName = name;
        this.mService = service;
        this.mReader = null;
    }

    public String getName() {
        return this.mName;
    }

    public Session openSession() throws IOException {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service is not connected");
        }
        Session session;
        if (this.mReader == null) {
            try {
                this.mReader = this.mService.getReader(this.mName);
            } catch (Exception e) {
                throw new IOException("service reader cannot be accessed.");
            }
        }
        synchronized (this.mLock) {
            SmartcardError error = new SmartcardError();
            try {
                ISmartcardServiceSession session2 = this.mReader.openSession(error);
                if (error.isSet()) {
                    error.throwException();
                }
                if (session2 == null) {
                    throw new IOException("service session is null.");
                }
                session = new Session(this.mService, session2, this, session2.getHandle());
            } catch (Exception e2) {
                throw new IOException("Unable to get the Session handle");
            } catch (RemoteException e3) {
                throw new IOException(e3.getMessage());
            }
        }
        return session;
    }

    public boolean isSecureElementPresent() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service is not connected");
        }
        if (this.mReader == null) {
            try {
                this.mReader = this.mService.getReader(this.mName);
            } catch (Exception e) {
                throw new IllegalStateException("service reader cannot be accessed. " + e.getLocalizedMessage());
            }
        }
        try {
            return this.mReader.isSecureElementPresent();
        } catch (RemoteException e2) {
            throw new IllegalStateException(e2.getMessage());
        }
    }

    public SEService getSEService() {
        return this.mService;
    }

    public void closeSessions() {
        if (this.mService == null || (this.mService.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("service is not connected");
        } else if (this.mReader != null) {
            synchronized (this.mLock) {
                try {
                    this.mReader.closeSessions(new SmartcardError());
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }
}
