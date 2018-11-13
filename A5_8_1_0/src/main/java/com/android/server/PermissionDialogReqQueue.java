package com.android.server;

import java.util.ArrayList;
import java.util.List;

public class PermissionDialogReqQueue {
    private PermissionDialog mDialog = null;
    private final List<PermissionDialogReq> mResultList = new ArrayList();

    public static final class PermissionDialogReq {
        boolean mHasResult = false;
        int mResult;

        public void set(int res) {
            synchronized (this) {
                this.mHasResult = true;
                this.mResult = res;
                notifyAll();
            }
        }

        public int get() {
            synchronized (this) {
                while (!this.mHasResult) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return this.mResult;
        }
    }

    public PermissionDialog getDialog() {
        return this.mDialog;
    }

    public void setDialog(PermissionDialog mDialog) {
        this.mDialog = mDialog;
    }

    public void register(PermissionDialogReq res) {
        synchronized (this) {
            this.mResultList.add(res);
        }
    }

    public void notifyAll(int mode) {
        synchronized (this) {
            while (this.mResultList.size() != 0) {
                ((PermissionDialogReq) this.mResultList.get(0)).set(mode);
                this.mResultList.remove(0);
            }
        }
    }
}
