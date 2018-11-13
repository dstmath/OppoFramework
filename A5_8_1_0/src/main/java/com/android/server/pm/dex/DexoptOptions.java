package com.android.server.pm.dex;

import com.android.server.pm.PackageManagerServiceCompilerMapping;

public final class DexoptOptions {
    public static final int DEXOPT_AS_SHARED_LIBRARY = 64;
    public static final int DEXOPT_BOOT_COMPLETE = 4;
    public static final int DEXOPT_CHECK_FOR_PROFILES_UPDATES = 1;
    public static final int DEXOPT_DOWNGRADE = 32;
    public static final int DEXOPT_FORCE = 2;
    public static final int DEXOPT_ONLY_SECONDARY_DEX = 8;
    public static final int DEXOPT_ONLY_SHARED_DEX = 16;
    private final String mCompilerFilter;
    private final int mFlags;
    private final String mPackageName;
    private final String mSplitName;

    public DexoptOptions(String packageName, String compilerFilter, int flags) {
        this(packageName, compilerFilter, null, flags);
    }

    public DexoptOptions(String packageName, int compilerReason, int flags) {
        this(packageName, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(compilerReason), flags);
    }

    public DexoptOptions(String packageName, String compilerFilter, String splitName, int flags) {
        if (((~127) & flags) != 0) {
            throw new IllegalArgumentException("Invalid flags : " + Integer.toHexString(flags));
        }
        this.mPackageName = packageName;
        this.mCompilerFilter = compilerFilter;
        this.mFlags = flags;
        this.mSplitName = splitName;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public boolean isCheckForProfileUpdates() {
        return (this.mFlags & 1) != 0;
    }

    public String getCompilerFilter() {
        return this.mCompilerFilter;
    }

    public boolean isForce() {
        return (this.mFlags & 2) != 0;
    }

    public boolean isBootComplete() {
        return (this.mFlags & 4) != 0;
    }

    public boolean isDexoptOnlySecondaryDex() {
        return (this.mFlags & 8) != 0;
    }

    public boolean isDexoptOnlySharedDex() {
        return (this.mFlags & 16) != 0;
    }

    public boolean isDowngrade() {
        return (this.mFlags & 32) != 0;
    }

    public boolean isDexoptAsSharedLibrary() {
        return (this.mFlags & 64) != 0;
    }

    public String getSplitName() {
        return this.mSplitName;
    }

    public int getFlags() {
        return this.mFlags;
    }
}
