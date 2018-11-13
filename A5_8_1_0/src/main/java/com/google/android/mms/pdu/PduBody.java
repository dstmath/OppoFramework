package com.google.android.mms.pdu;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class PduBody {
    private Map<String, PduPart> mPartMapByContentId;
    private Map<String, PduPart> mPartMapByContentLocation;
    private Map<String, PduPart> mPartMapByFileName;
    private Map<String, PduPart> mPartMapByName;
    private Vector<PduPart> mParts;

    public PduBody() {
        this.mParts = null;
        this.mPartMapByContentId = null;
        this.mPartMapByContentLocation = null;
        this.mPartMapByName = null;
        this.mPartMapByFileName = null;
        this.mParts = new Vector();
        this.mPartMapByContentId = new HashMap();
        this.mPartMapByContentLocation = new HashMap();
        this.mPartMapByName = new HashMap();
        this.mPartMapByFileName = new HashMap();
    }

    private void putPartToMaps(PduPart part) {
        byte[] contentId = part.getContentId();
        if (contentId != null) {
            this.mPartMapByContentId.put(new String(contentId), part);
        }
        byte[] contentLocation = part.getContentLocation();
        if (contentLocation != null) {
            this.mPartMapByContentLocation.put(new String(contentLocation), part);
        }
        byte[] name = part.getName();
        if (name != null) {
            this.mPartMapByName.put(new String(name), part);
        }
        byte[] fileName = part.getFilename();
        if (fileName != null) {
            this.mPartMapByFileName.put(new String(fileName), part);
        }
    }

    public boolean addPart(PduPart part) {
        if (part == null) {
            throw new NullPointerException();
        }
        putPartToMaps(part);
        return this.mParts.add(part);
    }

    public void addPart(int index, PduPart part) {
        if (part == null) {
            throw new NullPointerException();
        }
        putPartToMaps(part);
        this.mParts.add(index, part);
    }

    public PduPart removePart(int index) {
        return (PduPart) this.mParts.remove(index);
    }

    public void removeAll() {
        this.mParts.clear();
    }

    public PduPart getPart(int index) {
        return (PduPart) this.mParts.get(index);
    }

    public int getPartIndex(PduPart part) {
        return this.mParts.indexOf(part);
    }

    public int getPartsNum() {
        return this.mParts.size();
    }

    public PduPart getPartByContentId(String cid) {
        return (PduPart) this.mPartMapByContentId.get(cid);
    }

    public PduPart getPartByContentLocation(String contentLocation) {
        return (PduPart) this.mPartMapByContentLocation.get(contentLocation);
    }

    public PduPart getPartByName(String name) {
        return (PduPart) this.mPartMapByName.get(name);
    }

    public PduPart getPartByFileName(String filename) {
        return (PduPart) this.mPartMapByFileName.get(filename);
    }
}
