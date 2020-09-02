package com.android.server.pm;

import android.content.pm.PackageParser;
import com.android.server.pm.Installer;

public class PackageManagerException extends Exception {
    public final int error;

    public PackageManagerException(String detailMessage) {
        super(detailMessage);
        this.error = -110;
    }

    public PackageManagerException(int error2, String detailMessage) {
        super(detailMessage);
        this.error = error2;
    }

    public PackageManagerException(int error2, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.error = error2;
    }

    public PackageManagerException(Throwable e) {
        super(e);
        this.error = -110;
    }

    public static PackageManagerException from(PackageParser.PackageParserException e) throws PackageManagerException {
        throw new PackageManagerException(e.error, e.getMessage(), e.getCause());
    }

    public static PackageManagerException from(Installer.InstallerException e) throws PackageManagerException {
        throw new PackageManagerException(-110, e.getMessage(), e.getCause());
    }
}
