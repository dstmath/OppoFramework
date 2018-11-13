package cm.android.mdm.manager;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;
import cm.android.mdm.interfaces.IRecordManager;
import cm.android.mdm.util.MethodSignature;
import java.io.File;
import java.util.List;

public class RecordManager implements IRecordManager {
    private static final String ACTION_RECORD_POLICY = "oppo.action.third.record.policy";
    private static final String ACTION_START_RECORD = "oppo.action.third.record.start";
    private static final String ACTION_STOP_RECORD = "oppo.action.third.record.stop";
    private static final int DEFAULT_AUDIO_SOURCE = 4;
    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String POLICY_STATUS = "policy_status";
    private static final String RECORD_DIR_PATH = "record_dir_path";
    private static final String RECORD_PATH = "record_path";
    private static final String SAVE_INTNET = "android.intent.action.MEDIA_SCANNER_SCAN_FILE";
    private static final String SERVICE_PACKAGER = "com.android.incallui";
    private static final String TAG = "RecordManager";
    private Context mContext;
    private String mFilePath = null;
    private MediaRecorder mRecorder;

    public RecordManager(Context context) {
        this.mContext = context;
    }

    public void startRecordPolicy(String dirPath) {
        Log.d(TAG, "startRecordPolicy dirPath = " + dirPath);
        Intent intent = new Intent(ACTION_RECORD_POLICY);
        intent.putExtra(POLICY_STATUS, ON);
        intent.putExtra(RECORD_DIR_PATH, dirPath);
        this.mContext.sendBroadcast(intent);
    }

    public void stopRecordPolicy() {
        Log.d(TAG, "stopRecordPolicy");
        Intent intent = new Intent(ACTION_RECORD_POLICY);
        intent.putExtra(POLICY_STATUS, OFF);
        this.mContext.sendBroadcast(intent);
    }

    public void startRecord(String filePath) {
        Log.d(TAG, "startRecord filePath = " + filePath);
        if (!TextUtils.isEmpty(filePath)) {
            int lastSeparator = filePath.lastIndexOf(File.separator);
            if (lastSeparator != -1) {
                String fileDirPath = filePath.substring(OFF, lastSeparator);
                Log.d(TAG, "startRecord fileDirPath = " + fileDirPath);
                File filedDir = new File(fileDirPath);
                if (filedDir == null) {
                    Log.d(TAG, "filedDir is null!");
                    return;
                }
                if (!filedDir.exists()) {
                    try {
                        filedDir.mkdir();
                    } catch (Exception e) {
                        Log.e(TAG, "startRecord", e);
                    }
                    if (!filedDir.exists()) {
                        Log.d(TAG, "filedDir is not exist!");
                        return;
                    }
                }
                try {
                    Intent intent = new Intent(ACTION_START_RECORD);
                    intent.setPackage(SERVICE_PACKAGER);
                    intent.putExtra(RECORD_PATH, filePath);
                    this.mContext.startService(intent);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public void stopRecord() {
        try {
            Intent intent = new Intent(ACTION_STOP_RECORD);
            intent.setPackage(SERVICE_PACKAGER);
            this.mContext.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(RecordManager.class);
    }

    private boolean doRecord(String storeName) throws Exception {
        reset();
        Log.d(TAG, "doRecord storeName = " + storeName);
        if (this.mRecorder == null) {
            this.mRecorder = new MediaRecorder();
            this.mRecorder.setAudioSource(DEFAULT_AUDIO_SOURCE);
            this.mRecorder.setOutputFormat(3);
            this.mRecorder.setAudioEncoder(ON);
            this.mRecorder.setOutputFile(storeName);
            this.mRecorder.prepare();
            this.mRecorder.start();
        }
        return true;
    }

    public void reset() {
        Log.d(TAG, "reset");
        if (this.mRecorder != null) {
            this.mRecorder.reset();
            this.mRecorder.release();
            this.mRecorder = null;
        }
    }

    public void deleteExceptionFile() {
        if (TextUtils.isEmpty(this.mFilePath)) {
            Log.w(TAG, "deleteExceptionFile mFilePath is null!");
            return;
        }
        File file = new File(this.mFilePath);
        if (file == null) {
            Log.w(TAG, "deleteExceptionFile no exsit file");
            return;
        }
        if (file.exists()) {
            file.delete();
            Log.d(TAG, "delete  " + this.mFilePath);
        }
    }
}
