package com.android.internal.telephony;

import android.util.Log;

public class OppoNvFactory {
    private static final String TAG = "NvFactory";
    public CommandsInterface cm;
    public int cmd;
    private int mCancelCracked = 21;
    private int mDynamicNvAutoCheck = 2;
    private int mDynamicNvBackup = 4;
    private int mDynamicNvCheck = 1;
    private int mDynamicNvRestore = 3;
    private int mGoCracked = 21;
    private int mHasCracked = 20;
    private int mLteNvChange = 9;
    private int mStaticNvAutoCheck = 6;
    private int mStaticNvBackup = 8;
    private int mStaticNvCheck = 5;
    private int mStaticNvRestore = 7;

    public OppoNvFactory(CommandsInterface ci) {
        this.cm = ci;
    }

    public void onDynamicNvCheck() {
        Log.d(TAG, "onDynamicNvCheck");
        this.cmd = this.mDynamicNvCheck;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onDynamicNvAutoCheck() {
        Log.d(TAG, "onDynamicNvAutoCheck");
        this.cmd = this.mDynamicNvAutoCheck;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onDynamicNvRestore() {
        Log.d(TAG, "onDynamicNvRestore");
        this.cmd = this.mDynamicNvRestore;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onDynamicNvBackup() {
        Log.d(TAG, "onDynamicNvBackup");
        this.cmd = this.mDynamicNvBackup;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onStaticNvCheck() {
        Log.d(TAG, "onStaticNvCheck");
        this.cmd = this.mStaticNvCheck;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onStaticNvAutoCheck() {
        Log.d(TAG, "onStaticNvAutoCheck");
        this.cmd = this.mStaticNvAutoCheck;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onStaticNvRestore() {
        Log.d(TAG, "onStaticNvRestore");
        this.cmd = this.mStaticNvRestore;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onStaticNvBackup() {
        Log.d(TAG, "onStaticNvBackup");
        this.cmd = this.mStaticNvBackup;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void onLteNvChange() {
        Log.d(TAG, "onLteNvChange");
        this.cmd = this.mLteNvChange;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void hasCracked() {
        Log.d(TAG, "hasCracked");
        this.cmd = this.mHasCracked;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void cancelCracked() {
        Log.d(TAG, "cancelCracked");
        this.cmd = this.mCancelCracked;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }

    public void goCracked() {
        Log.d(TAG, "goCracked");
        this.cmd = this.mGoCracked;
        this.cm.setFactoryModeNvProcess(this.cmd, null);
    }
}
