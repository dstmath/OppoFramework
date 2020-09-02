package com.android.internal.os;

import android.telephony.SmsManager;
import java.util.ArrayList;
import java.util.Arrays;

class ZygoteArguments {
    boolean mAbiListQuery;
    String[] mApiBlacklistExemptions;
    String mAppDataDir;
    boolean mCapabilitiesSpecified;
    long mEffectiveCapabilities;
    int mGid = 0;
    boolean mGidSpecified;
    int[] mGids;
    int mHiddenApiAccessLogSampleRate = -1;
    int mHiddenApiAccessStatslogSampleRate = -1;
    String mInstructionSet;
    String mInvokeWith;
    int mMountExternal = 0;
    String mNiceName;
    String mPackageName;
    long mPermittedCapabilities;
    boolean mPidQuery;
    String mPreloadApp;
    boolean mPreloadDefault;
    String mPreloadPackage;
    String mPreloadPackageCacheKey;
    String mPreloadPackageLibFileName;
    String mPreloadPackageLibs;
    ArrayList<int[]> mRLimits;
    String[] mRemainingArgs;
    int mRuntimeFlags;
    String mSeInfo;
    boolean mSeInfoSpecified;
    boolean mStartChildZygote;
    int mTargetSdkVersion;
    boolean mTargetSdkVersionSpecified;
    int mUid = 0;
    boolean mUidSpecified;
    boolean mUsapPoolEnabled;
    boolean mUsapPoolStatusSpecified = false;

    ZygoteArguments(String[] args) throws IllegalArgumentException {
        parseArgs(args);
    }

    private void parseArgs(String[] args) throws IllegalArgumentException {
        int i;
        int curArg = 0;
        boolean seenRuntimeArgs = false;
        boolean expectRuntimeArgs = true;
        while (true) {
            i = 0;
            if (curArg >= args.length) {
                break;
            }
            String arg = args[curArg];
            if (arg.equals("--")) {
                curArg++;
                break;
            }
            if (!arg.startsWith("--setuid=")) {
                if (!arg.startsWith("--setgid=")) {
                    if (!arg.startsWith("--target-sdk-version=")) {
                        if (!arg.equals("--runtime-args")) {
                            if (!arg.startsWith("--runtime-flags=")) {
                                if (!arg.startsWith("--seinfo=")) {
                                    if (!arg.startsWith("--capabilities=")) {
                                        if (!arg.startsWith("--rlimit=")) {
                                            if (!arg.startsWith("--setgroups=")) {
                                                if (!arg.equals("--invoke-with")) {
                                                    if (!arg.startsWith("--nice-name=")) {
                                                        if (!arg.equals("--mount-external-default")) {
                                                            if (!arg.equals("--mount-external-read")) {
                                                                if (!arg.equals("--mount-external-write")) {
                                                                    if (!arg.equals("--mount-external-full")) {
                                                                        if (!arg.equals("--mount-external-installer")) {
                                                                            if (!arg.equals("--mount-external-legacy")) {
                                                                                if (!arg.equals("--query-abi-list")) {
                                                                                    if (!arg.equals("--get-pid")) {
                                                                                        if (!arg.startsWith("--instruction-set=")) {
                                                                                            if (!arg.startsWith("--app-data-dir=")) {
                                                                                                if (!arg.equals("--preload-app")) {
                                                                                                    if (!arg.equals("--preload-package")) {
                                                                                                        if (!arg.equals("--preload-default")) {
                                                                                                            if (!arg.equals("--start-child-zygote")) {
                                                                                                                if (!arg.equals("--set-api-blacklist-exemptions")) {
                                                                                                                    if (!arg.startsWith("--hidden-api-log-sampling-rate=")) {
                                                                                                                        if (!arg.startsWith("--hidden-api-statslog-sampling-rate=")) {
                                                                                                                            if (!arg.startsWith("--package-name=")) {
                                                                                                                                if (!arg.startsWith("--usap-pool-enabled=")) {
                                                                                                                                    break;
                                                                                                                                }
                                                                                                                                this.mUsapPoolStatusSpecified = true;
                                                                                                                                this.mUsapPoolEnabled = Boolean.parseBoolean(arg.substring(arg.indexOf(61) + 1));
                                                                                                                                expectRuntimeArgs = false;
                                                                                                                            } else if (this.mPackageName == null) {
                                                                                                                                this.mPackageName = arg.substring(arg.indexOf(61) + 1);
                                                                                                                            } else {
                                                                                                                                throw new IllegalArgumentException("Duplicate arg specified");
                                                                                                                            }
                                                                                                                        } else {
                                                                                                                            String rateStr = arg.substring(arg.indexOf(61) + 1);
                                                                                                                            try {
                                                                                                                                this.mHiddenApiAccessStatslogSampleRate = Integer.parseInt(rateStr);
                                                                                                                                expectRuntimeArgs = false;
                                                                                                                            } catch (NumberFormatException nfe) {
                                                                                                                                throw new IllegalArgumentException("Invalid statslog sampling rate: " + rateStr, nfe);
                                                                                                                            }
                                                                                                                        }
                                                                                                                    } else {
                                                                                                                        String rateStr2 = arg.substring(arg.indexOf(61) + 1);
                                                                                                                        try {
                                                                                                                            this.mHiddenApiAccessLogSampleRate = Integer.parseInt(rateStr2);
                                                                                                                            expectRuntimeArgs = false;
                                                                                                                        } catch (NumberFormatException nfe2) {
                                                                                                                            throw new IllegalArgumentException("Invalid log sampling rate: " + rateStr2, nfe2);
                                                                                                                        }
                                                                                                                    }
                                                                                                                } else {
                                                                                                                    this.mApiBlacklistExemptions = (String[]) Arrays.copyOfRange(args, curArg + 1, args.length);
                                                                                                                    curArg = args.length;
                                                                                                                    expectRuntimeArgs = false;
                                                                                                                }
                                                                                                            } else {
                                                                                                                this.mStartChildZygote = true;
                                                                                                            }
                                                                                                        } else {
                                                                                                            this.mPreloadDefault = true;
                                                                                                            expectRuntimeArgs = false;
                                                                                                        }
                                                                                                    } else {
                                                                                                        int curArg2 = curArg + 1;
                                                                                                        this.mPreloadPackage = args[curArg2];
                                                                                                        int curArg3 = curArg2 + 1;
                                                                                                        this.mPreloadPackageLibs = args[curArg3];
                                                                                                        int curArg4 = curArg3 + 1;
                                                                                                        this.mPreloadPackageLibFileName = args[curArg4];
                                                                                                        curArg = curArg4 + 1;
                                                                                                        this.mPreloadPackageCacheKey = args[curArg];
                                                                                                    }
                                                                                                } else {
                                                                                                    curArg++;
                                                                                                    this.mPreloadApp = args[curArg];
                                                                                                }
                                                                                            } else {
                                                                                                this.mAppDataDir = arg.substring(arg.indexOf(61) + 1);
                                                                                            }
                                                                                        } else {
                                                                                            this.mInstructionSet = arg.substring(arg.indexOf(61) + 1);
                                                                                        }
                                                                                    } else {
                                                                                        this.mPidQuery = true;
                                                                                    }
                                                                                } else {
                                                                                    this.mAbiListQuery = true;
                                                                                }
                                                                            } else {
                                                                                this.mMountExternal = 4;
                                                                            }
                                                                        } else {
                                                                            this.mMountExternal = 5;
                                                                        }
                                                                    } else {
                                                                        this.mMountExternal = 6;
                                                                    }
                                                                } else {
                                                                    this.mMountExternal = 3;
                                                                }
                                                            } else {
                                                                this.mMountExternal = 2;
                                                            }
                                                        } else {
                                                            this.mMountExternal = 1;
                                                        }
                                                    } else if (this.mNiceName == null) {
                                                        this.mNiceName = arg.substring(arg.indexOf(61) + 1);
                                                    } else {
                                                        throw new IllegalArgumentException("Duplicate arg specified");
                                                    }
                                                } else if (this.mInvokeWith == null) {
                                                    curArg++;
                                                    try {
                                                        this.mInvokeWith = args[curArg];
                                                    } catch (IndexOutOfBoundsException e) {
                                                        throw new IllegalArgumentException("--invoke-with requires argument");
                                                    }
                                                } else {
                                                    throw new IllegalArgumentException("Duplicate arg specified");
                                                }
                                            } else if (this.mGids == null) {
                                                String[] params = arg.substring(arg.indexOf(61) + 1).split(SmsManager.REGEX_PREFIX_DELIMITER);
                                                this.mGids = new int[params.length];
                                                for (int i2 = params.length - 1; i2 >= 0; i2--) {
                                                    this.mGids[i2] = Integer.parseInt(params[i2]);
                                                }
                                            } else {
                                                throw new IllegalArgumentException("Duplicate arg specified");
                                            }
                                        } else {
                                            String[] limitStrings = arg.substring(arg.indexOf(61) + 1).split(SmsManager.REGEX_PREFIX_DELIMITER);
                                            if (limitStrings.length == 3) {
                                                int[] rlimitTuple = new int[limitStrings.length];
                                                for (int i3 = 0; i3 < limitStrings.length; i3++) {
                                                    rlimitTuple[i3] = Integer.parseInt(limitStrings[i3]);
                                                }
                                                if (this.mRLimits == null) {
                                                    this.mRLimits = new ArrayList<>();
                                                }
                                                this.mRLimits.add(rlimitTuple);
                                            } else {
                                                throw new IllegalArgumentException("--rlimit= should have 3 comma-delimited ints");
                                            }
                                        }
                                    } else if (!this.mCapabilitiesSpecified) {
                                        this.mCapabilitiesSpecified = true;
                                        String[] capStrings = arg.substring(arg.indexOf(61) + 1).split(SmsManager.REGEX_PREFIX_DELIMITER, 2);
                                        if (capStrings.length == 1) {
                                            this.mEffectiveCapabilities = Long.decode(capStrings[0]).longValue();
                                            this.mPermittedCapabilities = this.mEffectiveCapabilities;
                                        } else {
                                            this.mPermittedCapabilities = Long.decode(capStrings[0]).longValue();
                                            this.mEffectiveCapabilities = Long.decode(capStrings[1]).longValue();
                                        }
                                    } else {
                                        throw new IllegalArgumentException("Duplicate arg specified");
                                    }
                                } else if (!this.mSeInfoSpecified) {
                                    this.mSeInfoSpecified = true;
                                    this.mSeInfo = arg.substring(arg.indexOf(61) + 1);
                                } else {
                                    throw new IllegalArgumentException("Duplicate arg specified");
                                }
                            } else {
                                this.mRuntimeFlags = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                            }
                        } else {
                            seenRuntimeArgs = true;
                        }
                    } else if (!this.mTargetSdkVersionSpecified) {
                        this.mTargetSdkVersionSpecified = true;
                        this.mTargetSdkVersion = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                    } else {
                        throw new IllegalArgumentException("Duplicate target-sdk-version specified");
                    }
                } else if (!this.mGidSpecified) {
                    this.mGidSpecified = true;
                    this.mGid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                } else {
                    throw new IllegalArgumentException("Duplicate arg specified");
                }
            } else if (!this.mUidSpecified) {
                this.mUidSpecified = true;
                this.mUid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
            } else {
                throw new IllegalArgumentException("Duplicate arg specified");
            }
            curArg++;
        }
        if (this.mAbiListQuery || this.mPidQuery) {
            if (args.length - curArg > 0) {
                throw new IllegalArgumentException("Unexpected arguments after --query-abi-list.");
            }
        } else if (this.mPreloadPackage != null) {
            if (args.length - curArg > 0) {
                throw new IllegalArgumentException("Unexpected arguments after --preload-package.");
            }
        } else if (this.mPreloadApp != null) {
            if (args.length - curArg > 0) {
                throw new IllegalArgumentException("Unexpected arguments after --preload-app.");
            }
        } else if (expectRuntimeArgs) {
            if (seenRuntimeArgs) {
                this.mRemainingArgs = new String[(args.length - curArg)];
                String[] strArr = this.mRemainingArgs;
                System.arraycopy(args, curArg, strArr, 0, strArr.length);
            } else {
                throw new IllegalArgumentException("Unexpected argument : " + args[curArg]);
            }
        }
        if (this.mStartChildZygote) {
            boolean seenChildSocketArg = false;
            String[] strArr2 = this.mRemainingArgs;
            int length = strArr2.length;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr2[i].startsWith(Zygote.CHILD_ZYGOTE_SOCKET_NAME_ARG)) {
                    seenChildSocketArg = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!seenChildSocketArg) {
                throw new IllegalArgumentException("--start-child-zygote specified without --zygote-socket=");
            }
        }
    }
}
