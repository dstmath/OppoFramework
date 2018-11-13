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

public class DisplayPerformanceHelper extends RomUpdateHelper {
    private static final int CODE_PARSE_DPS_CONFIG_FILE = 20000;
    private static final String DATA_FILE_DIR = "data/system/oppo_display_perf_list.xml";
    public static final String FILTER_NAME = "oppo_display_perf_list";
    private static final String SF_COMPOSER_TOKEN = "android.ui.ISurfaceComposer";
    private static final String SF_SERVICE_NAME = "SurfaceFlinger";
    private static final String SYS_FILE_DIR = "system/etc/oppo_display_perf_list.xml";
    private static final String TAG = "DisplayPerformanceHelper";

    public class DisplayPerformanceParser extends UpdateInfo {
        public /* bridge */ /* synthetic */ void clear() {
            super.clear();
        }

        public /* bridge */ /* synthetic */ boolean clone(UpdateInfo other) {
            return super.clone(other);
        }

        public /* bridge */ /* synthetic */ void dump() {
            super.dump();
        }

        public /* bridge */ /* synthetic */ long getVersion() {
            return super.getVersion();
        }

        public /* bridge */ /* synthetic */ boolean insert(int type, String verifyStr) {
            return super.insert(type, verifyStr);
        }

        public /* bridge */ /* synthetic */ boolean updateToLowerVersion(String newContent) {
            return super.updateToLowerVersion(newContent);
        }

        public DisplayPerformanceParser() {
            super(DisplayPerformanceHelper.this);
        }

        private void changeFilePermisson(String filename) {
            File file = new File(filename);
            if (file.exists()) {
                Slog.d(DisplayPerformanceHelper.TAG, "setReadable result :" + file.setReadable(true, false));
                return;
            }
            Slog.d(DisplayPerformanceHelper.TAG, "filename :" + filename + " is not exist");
        }

        private void parseSurfaceFlinger() {
            IBinder flinger = ServiceManager.getService(DisplayPerformanceHelper.SF_SERVICE_NAME);
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(DisplayPerformanceHelper.SF_COMPOSER_TOKEN);
                Slog.d(DisplayPerformanceHelper.TAG, "parseSurfaceFlinger");
                try {
                    flinger.transact(DisplayPerformanceHelper.CODE_PARSE_DPS_CONFIG_FILE, data, null, 0);
                } catch (RemoteException ex) {
                    Slog.e(DisplayPerformanceHelper.TAG, "parseSurfaceFlinger failed", ex);
                } finally {
                    data.recycle();
                }
                return;
            }
            Slog.d(DisplayPerformanceHelper.TAG, "get SurfaceFlinger Service failed");
        }

        public void parseContentFromXML(String content) {
            if (content == null) {
                Slog.d(DisplayPerformanceHelper.TAG, "parseContentFromXML content is null");
                return;
            }
            changeFilePermisson(DisplayPerformanceHelper.DATA_FILE_DIR);
            parseSurfaceFlinger();
            ((AudioManager) DisplayPerformanceHelper.this.mContext.getSystemService("audio")).setParameters("KTV_Loopback_UpdateList=true");
        }
    }

    public DisplayPerformanceHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new DisplayPerformanceParser(), new DisplayPerformanceParser());
        initUpdateBroadcastReceiver();
    }
}
