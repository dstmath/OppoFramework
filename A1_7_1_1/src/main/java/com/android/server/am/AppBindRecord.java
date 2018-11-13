package com.android.server.am;

import android.util.ArraySet;
import android.util.Slog;
import java.io.PrintWriter;

final class AppBindRecord {
    final ProcessRecord client;
    final ArraySet<ConnectionRecord> connections = new ArraySet();
    final IntentBindRecord intent;
    final ServiceRecord service;

    void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "service=" + this.service);
        pw.println(prefix + "client=" + this.client);
        dumpInIntentBind(pw, prefix);
    }

    void dumpInIntentBind(PrintWriter pw, String prefix) {
        int N = this.connections.size();
        if (N > 0) {
            pw.println(prefix + "Per-process Connections:");
            for (int i = 0; i < N; i++) {
                pw.println(prefix + "  " + ((ConnectionRecord) this.connections.valueAt(i)));
            }
        }
    }

    AppBindRecord(ServiceRecord _service, IntentBindRecord _intent, ProcessRecord _client) {
        this.service = _service;
        this.intent = _intent;
        this.client = _client;
    }

    public String toString() {
        return "AppBindRecord{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.service.shortName + ":" + this.client.processName + "}";
    }

    void logOutIntentBindWithTypeInfo() {
        int N = this.connections.size();
        Slog.d("AppBindRecord", "size:" + N);
        if (N > 0) {
            Slog.d("AppBindRecord", "Per-process Connections:");
            for (int i = 0; i < N; i++) {
                Object obj = this.connections.valueAt(i);
                if (obj == null) {
                    Slog.d("AppBindRecord", "Connections null at: " + i);
                } else if (obj instanceof ConnectionRecord) {
                    Slog.d("AppBindRecord", "Connections at: " + i + " = " + ((ConnectionRecord) obj));
                } else {
                    Slog.d("AppBindRecord", "Connections at: " + i + " is not ConnectionRecord. " + obj);
                }
            }
        }
    }
}
