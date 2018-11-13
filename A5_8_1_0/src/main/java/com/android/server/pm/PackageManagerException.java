package com.android.server.pm;

import android.content.pm.PackageParser.PackageParserException;
import com.android.server.pm.Installer.InstallerException;

public class PackageManagerException extends Exception {
    public final int error;

    public PackageManagerException(String detailMessage) {
        super(detailMessage);
        this.error = -110;
    }

    public PackageManagerException(int error, String detailMessage) {
        super(detailMessage);
        this.error = error;
    }

    public PackageManagerException(int error, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.error = error;
    }

    public PackageManagerException(Throwable e) {
        super(e);
        this.error = -110;
    }

    public static PackageManagerException from(PackageParserException e) throws PackageManagerException {
        throw new PackageManagerException(e.error, e.getMessage(), e.getCause());
    }

    public static PackageManagerException from(InstallerException e) throws PackageManagerException {
        throw new PackageManagerException(-110, e.getMessage(), e.getCause());
    }
}
