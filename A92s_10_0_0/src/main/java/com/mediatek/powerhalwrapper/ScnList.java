package com.mediatek.powerhalwrapper;

public class ScnList {
    public int handle;
    public int pid;
    public int uid;

    ScnList(int handle2, int pid2, int uid2) {
        this.handle = handle2;
        this.pid = pid2;
        this.uid = uid2;
    }

    public int getpid() {
        return this.pid;
    }

    public void setpid(int pid2) {
        this.pid = pid2;
    }

    public int getuid() {
        return this.uid;
    }

    public void setPack_Name(int uid2) {
        this.uid = uid2;
    }

    public int gethandle() {
        return this.handle;
    }

    public void sethandle(int handle2) {
        this.handle = handle2;
    }
}
