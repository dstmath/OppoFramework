package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

public enum Error {
    Timeout,
    SocketError,
    HttpError,
    ServerError,
    NotFound,
    NotAvaliable,
    NotPermite,
    NotSupportted,
    notConfiged,
    NotLogin,
    FsNotSynced,
    FsNotFound,
    FsChanged,
    FsTooBig,
    FsFileExists,
    LocalFileExist,
    LocalFileNotFound,
    TaskExist,
    TaskNotExist,
    IllegalInputParam,
    IllegalOutputParam,
    MaxiumLimitted,
    UnKonw,
    McsError,
    xmlParseError,
    stateError,
    resumeTaskIdNotExsit,
    newTaskExist,
    SyncTokenNotChanged,
    sdkInnerError;

    public static Error valueOf(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        throw new IndexOutOfBoundsException("Invalid ordinal");
    }
}
