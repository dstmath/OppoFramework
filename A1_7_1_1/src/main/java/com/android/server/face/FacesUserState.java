package com.android.server.face;

import android.content.Context;
import android.hardware.face.FaceFeature;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.server.face.utils.LogUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class FacesUserState {
    private static final String ATTR_DEVICE_ID = "deviceId";
    private static final String ATTR_FACE_ID = "faceFeatureId";
    private static final String ATTR_GROUP_ID = "groupId";
    private static final String ATTR_NAME = "name";
    private static final String FACE_FILE = "settings_face.xml";
    private static final String TAG = "FaceService.FacesUserState";
    private static final String TAG_FACE = "face";
    private static final String TAG_FACES = "faces";
    private final Context mCtx;
    @GuardedBy("this")
    private final ArrayList<FaceFeature> mFaceFeatures = new ArrayList();
    private final File mFile;
    private final Runnable mWriteStateRunnable = new Runnable() {
        public void run() {
            FacesUserState.this.doWriteState();
        }
    };

    public FacesUserState(Context ctx, int userId) {
        this.mFile = getFileForUser(userId);
        this.mCtx = ctx;
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public void addFace(int faceFeatureId, int groupId) {
        synchronized (this) {
            this.mFaceFeatures.add(new FaceFeature(getUniqueName(), groupId, faceFeatureId, 0));
            scheduleWriteStateLocked();
        }
        LogUtil.d(TAG, "addFace finished");
    }

    public void removeFace(int faceFeatureId) {
        if (faceFeatureId == 0) {
            synchronized (this) {
                this.mFaceFeatures.clear();
                scheduleWriteStateLocked();
                LogUtil.d(TAG, "remove all mFaceFeatures finished");
            }
            return;
        }
        synchronized (this) {
            for (int i = 0; i < this.mFaceFeatures.size(); i++) {
                if (((FaceFeature) this.mFaceFeatures.get(i)).getFaceFeatureId() == faceFeatureId) {
                    this.mFaceFeatures.remove(i);
                    scheduleWriteStateLocked();
                    break;
                }
            }
        }
        LogUtil.d(TAG, "removeFace finished");
    }

    public void renameFace(int faceFeatureId, CharSequence name) {
        synchronized (this) {
            for (int i = 0; i < this.mFaceFeatures.size(); i++) {
                if (((FaceFeature) this.mFaceFeatures.get(i)).getFaceFeatureId() == faceFeatureId) {
                    FaceFeature old = (FaceFeature) this.mFaceFeatures.get(i);
                    this.mFaceFeatures.set(i, new FaceFeature(name, old.getGroupId(), old.getFaceFeatureId(), old.getDeviceId()));
                    scheduleWriteStateLocked();
                    break;
                }
            }
        }
    }

    public void syncFaces(int[] faceIds, int groupId) {
        synchronized (this) {
            ArrayList<Integer> faces = getArrayListCopy(faceIds);
            ArrayList<Integer> removeFaces = new ArrayList();
            if (faces == null) {
                LogUtil.e(TAG, "syncFaces skip, because faces is null ");
                return;
            }
            int i;
            int faceFeatureId;
            boolean updated = false;
            int remaining_templates = faces.size();
            int faceNum = this.mFaceFeatures.size();
            for (i = 0; i < remaining_templates; i++) {
                LogUtil.d(TAG, "faces[" + i + "] = " + faces.get(i));
            }
            LogUtil.d(TAG, "syncFaces, groupId = " + groupId + ", remaining_templates = " + remaining_templates + ", faceNum = " + faceNum);
            for (i = 0; i < faceNum; i++) {
                faceFeatureId = ((FaceFeature) this.mFaceFeatures.get(i)).getFaceFeatureId();
                LogUtil.d(TAG, "mFaceFeatures[" + i + "] = " + faceFeatureId);
                if (faces.contains(Integer.valueOf(faceFeatureId))) {
                    faces.remove(Integer.valueOf(faceFeatureId));
                } else {
                    removeFaces.add(Integer.valueOf(faceFeatureId));
                    updated = true;
                }
            }
            int removeNum = removeFaces.size();
            LogUtil.d(TAG, "removeNum = " + removeNum);
            for (i = 0; i < removeNum; i++) {
                faceFeatureId = ((Integer) removeFaces.get(i)).intValue();
                faceNum = this.mFaceFeatures.size();
                for (int j = 0; j < faceNum; j++) {
                    if (((FaceFeature) this.mFaceFeatures.get(j)).getFaceFeatureId() == faceFeatureId) {
                        LogUtil.e(TAG, "remove not exist faceFeatureId = " + faceFeatureId);
                        this.mFaceFeatures.remove(j);
                        break;
                    }
                }
            }
            int addNum = faces.size();
            LogUtil.d(TAG, "addNum = " + addNum);
            for (int index = 0; index < addNum; index++) {
                faceFeatureId = ((Integer) faces.get(index)).intValue();
                LogUtil.d(TAG, "append faceFeatureId = " + faceFeatureId + " -> mFaceFeatures");
                this.mFaceFeatures.add(new FaceFeature(getUniqueName(), groupId, faceFeatureId, 0));
                updated = true;
            }
            if (updated) {
                scheduleWriteStateLocked();
            }
            LogUtil.d(TAG, "syncFaces finished");
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

    public List<FaceFeature> getFaces() {
        List copy;
        synchronized (this) {
            copy = getCopy(this.mFaceFeatures);
        }
        return copy;
    }

    private String getUniqueName() {
        int guess = 1;
        while (true) {
            String name = this.mCtx.getString(17039861, new Object[]{Integer.valueOf(guess)});
            if (isUnique(name)) {
                return name;
            }
            guess++;
        }
    }

    private boolean isUnique(String name) {
        for (FaceFeature feature : this.mFaceFeatures) {
            if (feature.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private static File getFileForUser(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), FACE_FILE);
    }

    private void scheduleWriteStateLocked() {
        AsyncTask.execute(this.mWriteStateRunnable);
    }

    private ArrayList<FaceFeature> getCopy(ArrayList<FaceFeature> array) {
        ArrayList<FaceFeature> result = new ArrayList(array.size());
        for (int i = 0; i < array.size(); i++) {
            FaceFeature feature = (FaceFeature) array.get(i);
            result.add(new FaceFeature(feature.getName(), feature.getGroupId(), feature.getFaceFeatureId(), feature.getDeviceId()));
        }
        return result;
    }

    private void doWriteState() {
        ArrayList<FaceFeature> faceFeatures;
        AtomicFile destination = new AtomicFile(this.mFile);
        synchronized (this) {
            faceFeatures = getCopy(this.mFaceFeatures);
        }
        AutoCloseable out = null;
        try {
            out = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_FACES);
            int count = faceFeatures.size();
            for (int i = 0; i < count; i++) {
                FaceFeature feature = (FaceFeature) faceFeatures.get(i);
                serializer.startTag(null, TAG_FACE);
                serializer.attribute(null, ATTR_FACE_ID, Integer.toString(feature.getFaceFeatureId()));
                serializer.attribute(null, ATTR_NAME, feature.getName().toString());
                serializer.attribute(null, ATTR_GROUP_ID, Integer.toString(feature.getGroupId()));
                serializer.attribute(null, ATTR_DEVICE_ID, Long.toString(feature.getDeviceId()));
                serializer.endTag(null, TAG_FACE);
            }
            serializer.endTag(null, TAG_FACES);
            serializer.endDocument();
            destination.finishWrite(out);
            IoUtils.closeQuietly(out);
        } catch (Throwable th) {
            IoUtils.closeQuietly(out);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002a A:{Splitter: B:5:0x0010, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:12:0x002a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0046, code:
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
                LogUtil.i(TAG, "No face state");
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
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_FACES))) {
                parseFacesLocked(parser);
            }
        }
    }

    private void parseFacesLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
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
                this.mFaceFeatures.add(new FaceFeature(parser.getAttributeValue(null, ATTR_NAME), Integer.parseInt(parser.getAttributeValue(null, ATTR_GROUP_ID)), Integer.parseInt(parser.getAttributeValue(null, ATTR_FACE_ID)), (long) Integer.parseInt(parser.getAttributeValue(null, ATTR_DEVICE_ID))));
            }
        }
    }
}
