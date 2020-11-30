package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
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
        /* class com.android.server.pm.OppoDcimProtectionService.AnonymousClass1 */

        public void onUEvent(UEventObserver.UEvent event) {
            if (OppoDcimProtectionService.DEBUG_DETAIL) {
                Slog.d(OppoDcimProtectionService.TAG, "receive event " + event);
            }
            synchronized (OppoDcimProtectionService.this.lock) {
                String uid = event.get("UID");
                String path = event.get("PATH");
                String scontext = event.get("SCONTEXT");
                String tcontext = event.get("TCONTEXT");
                String tclass = event.get("TCLASS");
                if (!(uid == null || path == null || scontext == null || tcontext == null)) {
                    if (tclass != null) {
                        if (OppoDcimProtectionService.DEBUG_DETAIL) {
                            Slog.d(OppoDcimProtectionService.TAG, "avc onUEvent:uid=" + uid + " path=" + path + " scontext=" + scontext + " tcontext=" + tcontext + " tclass=" + tclass);
                            StringBuilder sb = new StringBuilder();
                            sb.append(uid);
                            sb.append(" unlink ");
                            sb.append(path);
                            sb.append(" denied");
                            Slog.d(OppoDcimProtectionService.TAG, sb.toString());
                        }
                        Intent intent = new Intent(OppoDcimProtectionService.ACTION_DCIM_PROTECTION);
                        intent.putExtra(WatchlistLoggingHandler.WatchlistEventKeys.UID, uid);
                        intent.putExtra("path", path);
                        intent.putExtra("scontext", scontext);
                        intent.putExtra("tcontext", tcontext);
                        intent.putExtra("tclass", tclass);
                        OppoDcimProtectionService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                }
            }
        }
    };
    private HashSet<String> mRecordUid = new HashSet<>();

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
