package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.fingerprint.util.LogUtil;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class FingerprintsUserState {
    private static final String ATTR_DEVICE_ID = "deviceId";
    private static final String ATTR_FINGER_ID = "fingerId";
    private static final String ATTR_GROUP_ID = "groupId";
    private static final String ATTR_NAME = "name";
    private static final String FINGERPRINT_FILE = "settings_fingerprint.xml";
    private static final String TAG = "FingerprintService.UserState";
    private static final String TAG_FINGERPRINT = "fingerprint";
    private static final String TAG_FINGERPRINTS = "fingerprints";
    private final Context mCtx;
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    private final File mFile;
    @GuardedBy("this")
    private final ArrayList<Fingerprint> mFingerprints = new ArrayList();
    private final Runnable mWriteStateRunnable = new Runnable() {
        public void run() {
            FingerprintsUserState.this.doWriteState();
        }
    };

    public FingerprintsUserState(Context ctx, int userId) {
        this.mFile = getFileForUser(userId);
        this.mCtx = ctx;
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(this.mCtx);
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public void addFingerprint(int fingerId, int groupId) {
        synchronized (this) {
            this.mFingerprints.add(new Fingerprint(getUniqueName(), groupId, fingerId, 0));
            scheduleWriteStateLocked();
            LogUtil.d(TAG, "addFingerprint finished");
        }
    }

    public void removeFingerprint(int fingerId) {
        if (fingerId == 0) {
            synchronized (this) {
                this.mFingerprints.clear();
                scheduleWriteStateLocked();
                LogUtil.d(TAG, "remove all fingerprints finished");
            }
            return;
        }
        synchronized (this) {
            for (int i = 0; i < this.mFingerprints.size(); i++) {
                if (((Fingerprint) this.mFingerprints.get(i)).getFingerId() == fingerId) {
                    this.mFingerprints.remove(i);
                    scheduleWriteStateLocked();
                    break;
                }
            }
            LogUtil.d(TAG, "removeFingerprint finished");
        }
    }

    public void renameFingerprint(int fingerId, CharSequence name) {
        synchronized (this) {
            for (int i = 0; i < this.mFingerprints.size(); i++) {
                if (((Fingerprint) this.mFingerprints.get(i)).getFingerId() == fingerId) {
                    Fingerprint old = (Fingerprint) this.mFingerprints.get(i);
                    this.mFingerprints.set(i, new Fingerprint(name, old.getGroupId(), old.getFingerId(), old.getDeviceId()));
                    scheduleWriteStateLocked();
                    break;
                }
            }
        }
    }

    public void syncFingerprints(int[] fingerIds, int groupId) {
        synchronized (this) {
            ArrayList<Integer> fids = getArrayListCopy(fingerIds);
            ArrayList<Integer> removeFids = new ArrayList();
            if (fids == null) {
                LogUtil.e(TAG, "syncFingerprints skip, because fids is null ");
                return;
            }
            int i;
            int fingerId;
            boolean updated = false;
            int remainingTemplates = fids.size();
            int fingerprintNum = this.mFingerprints.size();
            for (i = 0; i < remainingTemplates; i++) {
                LogUtil.d(TAG, "fids[" + i + "] = " + fids.get(i));
            }
            LogUtil.d(TAG, "syncFingerprints, groupId = " + groupId + ", remainingTemplates = " + remainingTemplates + ", fingerprintNum = " + fingerprintNum);
            for (i = 0; i < fingerprintNum; i++) {
                fingerId = ((Fingerprint) this.mFingerprints.get(i)).getFingerId();
                LogUtil.d(TAG, "mFingerprints[" + i + "] = " + fingerId);
                if (fids.contains(Integer.valueOf(fingerId))) {
                    fids.remove(Integer.valueOf(fingerId));
                } else {
                    removeFids.add(Integer.valueOf(fingerId));
                    updated = true;
                }
            }
            int removeNum = removeFids.size();
            LogUtil.d(TAG, "removeNum = " + removeNum);
            for (i = 0; i < removeNum; i++) {
                fingerId = ((Integer) removeFids.get(i)).intValue();
                fingerprintNum = this.mFingerprints.size();
                for (int j = 0; j < fingerprintNum; j++) {
                    if (((Fingerprint) this.mFingerprints.get(j)).getFingerId() == fingerId) {
                        LogUtil.e(TAG, "remove not exist fingerId = " + fingerId);
                        this.mFingerprints.remove(j);
                        break;
                    }
                }
            }
            int addNum = fids.size();
            LogUtil.d(TAG, "addNum = " + addNum);
            for (int index = 0; index < addNum; index++) {
                fingerId = ((Integer) fids.get(index)).intValue();
                LogUtil.d(TAG, "append fingerId = " + fingerId + " -> mFingerprints");
                this.mFingerprints.add(new Fingerprint(getUniqueName(), groupId, fingerId, 0));
                updated = true;
            }
            if (updated) {
                if (this.mDcsStatisticsUtil != null) {
                    this.mDcsStatisticsUtil.sendSyncTemplateTimes();
                }
                scheduleWriteStateLocked();
            }
            LogUtil.d(TAG, "syncFingerprints finished");
        }
    }

    private ArrayList<Integer> getArrayListCopy(int[] ids) {
        if (ids == null) {
            return null;
        }
        ArrayList<Integer> res = new ArrayList();
        for (int valueOf : ids) {
            res.add(Integer.valueOf(valueOf));
        }
        return res;
    }

    public List<Fingerprint> getFingerprints() {
        List copy;
        synchronized (this) {
            copy = getCopy(this.mFingerprints);
        }
        return copy;
    }

    private String getUniqueName() {
        int guess = 1;
        while (true) {
            String name = this.mCtx.getString(17039919, new Object[]{Integer.valueOf(guess)});
            if (isUnique(name)) {
                return name;
            }
            guess++;
        }
    }

    private boolean isUnique(String name) {
        for (Fingerprint fp : this.mFingerprints) {
            if (fp.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private static File getFileForUser(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), FINGERPRINT_FILE);
    }

    private void scheduleWriteStateLocked() {
        AsyncTask.execute(this.mWriteStateRunnable);
    }

    private ArrayList<Fingerprint> getCopy(ArrayList<Fingerprint> array) {
        ArrayList<Fingerprint> result = new ArrayList(array.size());
        for (int i = 0; i < array.size(); i++) {
            Fingerprint fp = (Fingerprint) array.get(i);
            result.add(new Fingerprint(fp.getName(), fp.getGroupId(), fp.getFingerId(), fp.getDeviceId()));
        }
        return result;
    }

    private void doWriteState() {
        ArrayList<Fingerprint> fingerprints;
        AtomicFile destination = new AtomicFile(this.mFile);
        synchronized (this) {
            fingerprints = getCopy(this.mFingerprints);
        }
        AutoCloseable out = null;
        try {
            out = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_FINGERPRINTS);
            int count = fingerprints.size();
            for (int i = 0; i < count; i++) {
                Fingerprint fp = (Fingerprint) fingerprints.get(i);
                LogUtil.d(TAG, "doWriteState-> fingerprints[" + i + "] = [" + fp.getFingerId() + ", " + fp.getName().toString() + ", " + fp.getGroupId() + "]");
                serializer.startTag(null, TAG_FINGERPRINT);
                serializer.attribute(null, ATTR_FINGER_ID, Integer.toString(fp.getFingerId()));
                serializer.attribute(null, ATTR_NAME, fp.getName().toString());
                serializer.attribute(null, ATTR_GROUP_ID, Integer.toString(fp.getGroupId()));
                serializer.attribute(null, ATTR_DEVICE_ID, Long.toString(fp.getDeviceId()));
                serializer.endTag(null, TAG_FINGERPRINT);
            }
            serializer.endTag(null, TAG_FINGERPRINTS);
            serializer.endDocument();
            destination.finishWrite(out);
            IoUtils.closeQuietly(out);
        } catch (Throwable th) {
            IoUtils.closeQuietly(out);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002b A:{Splitter: B:5:0x0011, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:12:0x002b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0047, code:
            throw new java.lang.IllegalStateException("Failed parsing settings file: " + r7.mFile, r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readStateSyncLocked() {
        if (this.mFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(this.mFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    parseStateLocked(parser);
                    IoUtils.closeQuietly(in);
                } catch (Exception e) {
                } catch (Throwable th) {
                    IoUtils.closeQuietly(in);
                }
            } catch (FileNotFoundException e2) {
                LogUtil.i(TAG, "No fingerprint state");
            }
        }
    }

    private void parseStateLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_FINGERPRINTS))) {
                parseFingerprintsLocked(parser);
            }
        }
    }

    private void parseFingerprintsLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_FINGERPRINT))) {
                this.mFingerprints.add(new Fingerprint(parser.getAttributeValue(null, ATTR_NAME), Integer.parseInt(parser.getAttributeValue(null, ATTR_GROUP_ID)), Integer.parseInt(parser.getAttributeValue(null, ATTR_FINGER_ID)), (long) Integer.parseInt(parser.getAttributeValue(null, ATTR_DEVICE_ID))));
            }
        }
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args, String prefix) {
        String subPrefix = "  " + prefix;
        for (int i = 0; i < this.mFingerprints.size(); i++) {
            pw.print(subPrefix);
            pw.println("Fingerprint " + i + " {" + " fingerId = " + ((Fingerprint) this.mFingerprints.get(i)).getFingerId() + ", groupId = " + ((Fingerprint) this.mFingerprints.get(i)).getGroupId() + ", name = " + ((Fingerprint) this.mFingerprints.get(i)).getName());
        }
    }
}
