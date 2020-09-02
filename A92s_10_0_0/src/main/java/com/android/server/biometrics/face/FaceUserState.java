package com.android.server.biometrics.face;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.face.Face;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.server.biometrics.BiometricUserState;
import com.android.server.biometrics.face.utils.LogUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class FaceUserState extends BiometricUserState {
    private static final String ATTR_DEVICE_ID = "deviceId";
    private static final String ATTR_FACEFEATURE_ID = "faceFeatureId";
    private static final String ATTR_FACE_ID = "faceId";
    private static final String ATTR_NAME = "name";
    private static final String FACE_FILE = "settings_face.xml";
    private static final String TAG = "FaceService.FaceState";
    private static final String TAG_FACE = "face";
    private static final String TAG_FACES = "faces";

    public FaceUserState(Context ctx, int userId) {
        super(ctx, userId);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public String getBiometricsTag() {
        return TAG_FACES;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public String getBiometricFile() {
        return FACE_FILE;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public int getNameTemplateResource() {
        return 17039988;
    }

    @Override // com.android.server.biometrics.BiometricUserState
    public void addBiometric(BiometricAuthenticator.Identifier identifier) {
        if (identifier instanceof Face) {
            super.addBiometric(identifier);
        } else {
            Slog.w(TAG, "Attempted to add non-face identifier");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public ArrayList getCopy(ArrayList array) {
        ArrayList<Face> result = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            Face f = (Face) array.get(i);
            result.add(new Face(f.getName(), f.getBiometricId(), f.getDeviceId()));
        }
        return result;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricUserState
    public void doWriteState() {
        ArrayList<Face> faces;
        AtomicFile destination = new AtomicFile(this.mFile);
        synchronized (this) {
            faces = getCopy(this.mBiometrics);
        }
        try {
            FileOutputStream out = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_FACES);
            int count = faces.size();
            for (int i = 0; i < count; i++) {
                Face f = faces.get(i);
                serializer.startTag(null, TAG_FACE);
                serializer.attribute(null, ATTR_FACE_ID, Integer.toString(f.getBiometricId()));
                serializer.attribute(null, "name", f.getName().toString());
                serializer.attribute(null, ATTR_DEVICE_ID, Long.toString(f.getDeviceId()));
                serializer.endTag(null, TAG_FACE);
            }
            serializer.endTag(null, TAG_FACES);
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
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_FACE))) {
                String name = parser.getAttributeValue(null, "name");
                String faceId = parser.getAttributeValue(null, ATTR_FACE_ID);
                if (faceId == null) {
                    faceId = parser.getAttributeValue(null, ATTR_FACEFEATURE_ID);
                    Slog.w(TAG, "parseBiometricsLocked2 faceId:" + faceId);
                }
                this.mBiometrics.add(new Face(name, Integer.parseInt(faceId), Long.parseLong(parser.getAttributeValue(null, ATTR_DEVICE_ID))));
            }
        }
    }

    public void syncFaceIds(int[] BiometricId, int userId, long deviceId) {
        synchronized (this) {
            try {
                ArrayList<Integer> faceds = getArrayListCopy(BiometricId);
                ArrayList<Integer> removefaceds = new ArrayList<>();
                if (faceds == null) {
                    LogUtil.e(TAG, "syncfaceids skip, because faceds is null ");
                    return;
                }
                boolean updated = false;
                int remainingTemplates = faceds.size();
                int faceNum = this.mBiometrics.size();
                for (int i = 0; i < remainingTemplates; i++) {
                    LogUtil.d(TAG, "faceds[" + i + "] = " + faceds.get(i));
                }
                StringBuilder sb = new StringBuilder();
                sb.append("syncFaceIds, userId = ");
                try {
                    sb.append(userId);
                    sb.append(", remainingTemplates = ");
                    sb.append(remainingTemplates);
                    sb.append(", faceNum = ");
                    sb.append(faceNum);
                    LogUtil.d(TAG, sb.toString());
                    for (int i2 = 0; i2 < faceNum; i2++) {
                        int faceId = ((Face) this.mBiometrics.get(i2)).getBiometricId();
                        LogUtil.d(TAG, "mBiometrics[" + i2 + "] = " + faceId);
                        if (faceds.contains(Integer.valueOf(faceId))) {
                            faceds.remove(Integer.valueOf(faceId));
                        } else {
                            removefaceds.add(Integer.valueOf(faceId));
                            updated = true;
                        }
                    }
                    int removeNum = removefaceds.size();
                    LogUtil.d(TAG, "removeNum = " + removeNum);
                    for (int i3 = 0; i3 < removeNum; i3++) {
                        int faceId2 = removefaceds.get(i3).intValue();
                        int faceNum2 = this.mBiometrics.size();
                        int j = 0;
                        while (true) {
                            if (j >= faceNum2) {
                                break;
                            } else if (((Face) this.mBiometrics.get(j)).getBiometricId() == faceId2) {
                                LogUtil.e(TAG, "remove not exist faceId = " + faceId2);
                                this.mBiometrics.remove(j);
                                break;
                            } else {
                                j++;
                            }
                        }
                    }
                    int addNum = faceds.size();
                    LogUtil.d(TAG, "addNum = " + addNum);
                    for (int index = 0; index < addNum; index++) {
                        int faceId3 = faceds.get(index).intValue();
                        LogUtil.d(TAG, "append faceId = " + faceId3 + " -> mBiometrics");
                        this.mBiometrics.add(new Face(getUniqueName(), faceId3, deviceId));
                        updated = true;
                    }
                    if (updated) {
                        scheduleWriteStateLocked();
                    }
                    LogUtil.d(TAG, "syncFaceids finished");
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
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
}
