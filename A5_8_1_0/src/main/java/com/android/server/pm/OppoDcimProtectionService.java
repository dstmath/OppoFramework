package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.util.Slog;
import java.util.HashSet;

public class OppoDcimProtectionService {
    private static final String ACTION_DCIM_PROTECTION = "com.oppo.intent.action.DCIM_PROTECTION";
    public static boolean DEBUG_DETAIL = SystemProperties.getBoolean(KEY_ASSERT_PANIC_PROPERTIES, false);
    private static final String KEY_ASSERT_PANIC_PROPERTIES = "persist.sys.assert.panic";
    private static final String TAG = "OppoDcimProtectionService";
    private static final String UEVENT_MSG = "DENIED_STAT=DENIED";
    private static OppoDcimProtectionService mInstall = null;
    private final Object lock = new Object();
    private Context mContext = null;
    private UEventObserver mDcimObserver = new UEventObserver() {
        /* JADX WARNING: Missing block: B:10:0x004d, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onUEvent(UEvent event) {
            if (OppoDcimProtectionService.DEBUG_DETAIL) {
                Slog.d(OppoDcimProtectionService.TAG, "receive event " + event);
            }
            synchronized (OppoDcimProtectionService.this.lock) {
                String uid = event.get("UID");
                String path = event.get("PATH");
                String scontext = event.get("SCONTEXT");
                String tcontext = event.get("TCONTEXT");
                String tclass = event.get("TCLASS");
                if (uid == null || path == null || scontext == null || tcontext == null || tclass == null) {
                } else {
                    if (OppoDcimProtectionService.DEBUG_DETAIL) {
                        Slog.d(OppoDcimProtectionService.TAG, "avc onUEvent:uid=" + uid + " path=" + path + " scontext=" + scontext + " tcontext=" + tcontext + " tclass=" + tclass);
                        Slog.d(OppoDcimProtectionService.TAG, uid + " unlink " + path + " denied");
                    }
                    Intent intent = new Intent(OppoDcimProtectionService.ACTION_DCIM_PROTECTION);
                    intent.putExtra("uid", uid);
                    intent.putExtra("path", path);
                    intent.putExtra("scontext", scontext);
                    intent.putExtra("tcontext", tcontext);
                    intent.putExtra("tclass", tclass);
                    OppoDcimProtectionService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }
            }
        }
    };
    private HashSet<String> mRecordUid = new HashSet();

    private OppoDcimProtectionService(Context context) {
        this.mContext = context;
        startObserving();
    }

    public static OppoDcimProtectionService getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoDcimProtectionService(context);
        }
        return mInstall;
    }

    private void startObserving() {
        this.mDcimObserver.startObserving(UEVENT_MSG);
    }
}
