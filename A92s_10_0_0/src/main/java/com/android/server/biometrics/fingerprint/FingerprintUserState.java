package com.android.server.biometrics.fingerprint;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.fingerprint.Fingerprint;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.server.biometrics.BiometricUserState;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class FingerprintUserState extends BiometricUserState {
    private static final String ATTR_DEVICE_ID = "deviceId";
    private static final String ATTR_FINGER_ID = "fingerId";
    private static final String ATTR_GROUP_ID = "groupId";
    private static final String ATTR_NAME = "name";
    private static final String FINGERPRINT_FILE = "settings_fingerprint.xml";
    private static final String TAG = "FingerprintState";
    private static final String TAG_FINGERPRINT = "fingerprint";
    private static final String TAG_FINGERPRINTS = "fingerprints";
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;

    public FingerprintUserState(Context context, int userId) {
        super(context, userId);
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public String getBiometricsTag() {
        return TAG_FINGERPRINTS;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public String getBiometricFile() {
        return FINGERPRINT_FILE;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public int getNameTemplateResource() {
        return 17040025;
    }

    @Override // com.android.server.biometrics.BiometricUserState
    public void addBiometric(BiometricAuthenticator.Identifier identifier) {
        if (identifier instanceof Fingerprint) {
            super.addBiometric(identifier);
        } else {
            Slog.w(TAG, "Attempted to add non-fingerprint identifier");
        }
        LogUtil.d(TAG, "addBiometric finished");
    }

    @Override // com.android.server.biometrics.BiometricUserState
    public void removeBiometric(int biometricId) {
        if (biometricId == 0) {
            synchronized (this) {
                this.mBiometrics.clear();
                scheduleWriteStateLocked();
                LogUtil.d(TAG, "remove all fingerprints finished");
            }
            return;
        }
        super.removeBiometric(biometricId);
        LogUtil.d(TAG, "removeBiometric finished");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public ArrayList getCopy(ArrayList array) {
        ArrayList<Fingerprint> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            Fingerprint fp = (Fingerprint) array.get(i);
            result.add(new Fingerprint(fp.getName(), fp.getGroupId(), fp.getBiometricId(), fp.getDeviceId()));
        }
        return result;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public void doWriteState() {
        ArrayList<Fingerprint> fingerprints;
        AtomicFile destination = new AtomicFile(this.mFile);
        synchronized (this) {
            fingerprints = getCopy(this.mBiometrics);
        }
        try {
            FileOutputStream out = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_FINGERPRINTS);
            int count = fingerprints.size();
            for (int i = 0; i < count; i++) {
                Fingerprint fp = fingerprints.get(i);
                LogUtil.d(TAG, "doWriteState-> fingerprints[" + i + "] = [" + fp.getBiometricId() + ", " + fp.getName().toString() + ", " + fp.getGroupId() + "]");
                serializer.startTag(null, TAG_FINGERPRINT);
                serializer.attribute(null, ATTR_FINGER_ID, Integer.toString(fp.getBiometricId()));
                serializer.attribute(null, "name", fp.getName().toString());
                serializer.attribute(null, ATTR_GROUP_ID, Integer.toString(fp.getGroupId()));
                serializer.attribute(null, ATTR_DEVICE_ID, Long.toString(fp.getDeviceId()));
                serializer.endTag(null, TAG_FINGERPRINT);
            }
            serializer.endTag(null, TAG_FINGERPRINTS);
            serializer.endDocument();
            destination.finishWrite(out);
            IoUtils.closeQuietly(out);
        } catch (Throwable t) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw t;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    @GuardedBy({"this"})
    public void parseBiometricsLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        XmlPullParser xmlPullParser = parser;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (type == 3) {
                xmlPullParser = parser;
            } else if (type != 4) {
                if (parser.getName().equals(TAG_FINGERPRINT)) {
                    this.mBiometrics.add(new Fingerprint(xmlPullParser.getAttributeValue(null, "name"), Integer.parseInt(xmlPullParser.getAttributeValue(null, ATTR_GROUP_ID)), Integer.parseInt(xmlPullParser.getAttributeValue(null, ATTR_FINGER_ID)), Long.parseLong(xmlPullParser.getAttributeValue(null, ATTR_DEVICE_ID))));
                }
                xmlPullParser = parser;
            }
        }
    }

    private ArrayList<Integer> getArrayListCopy(int[] ids) {
        if (ids == null) {
            return null;
        }
        ArrayList<Integer> res = new ArrayList<>();
        for (int i : ids) {
            res.add(Integer.valueOf(i));
        }
        return res;
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args, String prefix) {
        ArrayList<Fingerprint> mFingerprints;
        String subPrefix = "  " + prefix;
        synchronized (this) {
            mFingerprints = getCopy(this.mBiometrics);
        }
        for (int i = 0; i < mFingerprints.size(); i++) {
            pw.print(subPrefix);
            pw.println("Fingerprint " + i + " { fingerId = " + mFingerprints.get(i).getBiometricId() + ", groupId = " + mFingerprints.get(i).getGroupId() + ", name = " + ((Object) mFingerprints.get(i).getName()));
        }
    }

    public void syncFingerprints(int[] BiometricId, int groupId) {
        synchronized (this) {
            try {
                ArrayList<Integer> fids = getArrayListCopy(BiometricId);
                ArrayList<Integer> removeFids = new ArrayList<>();
                if (fids == null) {
                    LogUtil.e(TAG, "syncFingerprints skip, because fids is null ");
                    return;
                }
                int fingerId = 0;
                boolean updated = false;
                int remainingTemplates = fids.size();
                int fingerprintNum = this.mBiometrics.size();
                for (int i = 0; i < remainingTemplates; i++) {
                    LogUtil.d(TAG, "fids[" + i + "] = " + fids.get(i));
                }
                StringBuilder sb = new StringBuilder();
                sb.append("syncFingerprints, groupId = ");
                sb.append(groupId);
                sb.append(", remainingTemplates = ");
                sb.append(remainingTemplates);
                sb.append(", fingerprintNum = ");
                sb.append(fingerprintNum);
                LogUtil.d(TAG, sb.toString());
                for (int i2 = 0; i2 < fingerprintNum; i2++) {
                    fingerId = ((Fingerprint) this.mBiometrics.get(i2)).getBiometricId();
                    LogUtil.d(TAG, "mBiometrics[" + i2 + "] = " + fingerId);
                    if (fids.contains(Integer.valueOf(fingerId))) {
                        fids.remove(Integer.valueOf(fingerId));
                    } else {
                        removeFids.add(Integer.valueOf(fingerId));
                        updated = true;
                    }
                }
                int removeNum = removeFids.size();
                LogUtil.d(TAG, "removeNum = " + removeNum);
                for (int i3 = 0; i3 < removeNum; i3++) {
                    fingerId = removeFids.get(i3).intValue();
                    int fingerprintNum2 = this.mBiometrics.size();
                    int j = 0;
                    while (true) {
                        if (j >= fingerprintNum2) {
                            break;
                        } else if (((Fingerprint) this.mBiometrics.get(j)).getBiometricId() == fingerId) {
                            LogUtil.e(TAG, "remove not exist fingerId = " + fingerId);
                            this.mBiometrics.remove(j);
                            break;
                        } else {
                            j++;
                        }
                    }
                }
                int addNum = fids.size();
                LogUtil.d(TAG, "addNum = " + addNum);
                int index = 0;
                while (index < addNum) {
                    int fingerId2 = fids.get(index).intValue();
                    LogUtil.d(TAG, "append fingerId = " + fingerId2 + " -> mBiometrics");
                    this.mBiometrics.add(new Fingerprint(getUniqueName(), groupId, fingerId2, 0));
                    updated = true;
                    index++;
                    fids = fids;
                    removeFids = removeFids;
                }
                if (updated) {
                    if (this.mDcsStatisticsUtil != null) {
                        this.mDcsStatisticsUtil.sendSyncTemplateTimes();
                    }
                    scheduleWriteStateLocked();
                }
                LogUtil.d(TAG, "syncFingerprints finished");
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }
}
