package android.provider;

import android.content.Context;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.File;

public class OppoDisplayPerformanceHelper extends RomUpdateHelper {
    private static final int CODE_PARSE_DPS_CONFIG_FILE = 20002;
    private static final String DATA_FILE_DIR = "/data/oppo/multimedia/oppo_display_perf_list.xml";
    public static final String FILTER_NAME = "oppo_display_perf_list";
    private static final String SF_COMPOSER_TOKEN = "android.ui.ISurfaceComposer";
    private static final String SF_SERVICE_NAME = "SurfaceFlinger";
    private static final String SYS_FILE_DIR = "system/etc/oppo_display_perf_list.xml";
    private static final String TAG = "OppoDisplayPerformanceHelper";

    public class DisplayPerformanceParser extends UpdateInfo {
        public /* bridge */ /* synthetic */ void clear() {
            super.clear();
        }

        public /* bridge */ /* synthetic */ boolean clone(UpdateInfo updateInfo) {
            return super.clone(updateInfo);
        }

        public /* bridge */ /* synthetic */ void dump() {
            super.dump();
        }

        public /* bridge */ /* synthetic */ long getVersion() {
            return super.getVersion();
        }

        public /* bridge */ /* synthetic */ boolean insert(int i, String str) {
            return super.insert(i, str);
        }

        public /* bridge */ /* synthetic */ boolean updateToLowerVersion(String str) {
            return super.updateToLowerVersion(str);
        }

        public DisplayPerformanceParser() {
            super(OppoDisplayPerformanceHelper.this);
        }

        private void changeFilePermisson(String filename) {
            File file = new File(filename);
            if (file.exists()) {
                Slog.d(OppoDisplayPerformanceHelper.TAG, "setReadable result :" + file.setReadable(true, false));
                return;
            }
            Slog.d(OppoDisplayPerformanceHelper.TAG, "filename :" + filename + " is not exist");
        }

        private void parseSurfaceFlinger() {
            IBinder flinger = ServiceManager.getService(OppoDisplayPerformanceHelper.SF_SERVICE_NAME);
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(OppoDisplayPerformanceHelper.SF_COMPOSER_TOKEN);
                Slog.d(OppoDisplayPerformanceHelper.TAG, "parseSurfaceFlinger");
                try {
                    flinger.transact(OppoDisplayPerformanceHelper.CODE_PARSE_DPS_CONFIG_FILE, data, null, 0);
                } catch (RemoteException ex) {
                    Slog.e(OppoDisplayPerformanceHelper.TAG, "parseSurfaceFlinger failed", ex);
                } finally {
                    data.recycle();
                }
                return;
            }
            Slog.d(OppoDisplayPerformanceHelper.TAG, "get SurfaceFlinger Service failed");
        }

        public void parseContentFromXML(String content) {
            if (content == null) {
                Slog.d(OppoDisplayPerformanceHelper.TAG, "parseContentFromXML content is null");
                return;
            }
            changeFilePermisson(OppoDisplayPerformanceHelper.DATA_FILE_DIR);
            parseSurfaceFlinger();
            AudioManager mAudioManager = (AudioManager) OppoDisplayPerformanceHelper.this.mContext.getSystemService("audio");
            if (mAudioManager != null) {
                Slog.d(OppoDisplayPerformanceHelper.TAG, "set KTV_Loopback_UpdateList..");
                mAudioManager.setParameters("KTV_Loopback_UpdateList=true");
                Slog.d(OppoDisplayPerformanceHelper.TAG, "set KTV_Loopback_UpdateList done");
            } else {
                Slog.d(OppoDisplayPerformanceHelper.TAG, "get AudioManager fail, can not KTV_Loopback_UpdateList");
            }
        }
    }

    public OppoDisplayPerformanceHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new DisplayPerformanceParser(), new DisplayPerformanceParser());
        initUpdateBroadcastReceiver();
    }
}
