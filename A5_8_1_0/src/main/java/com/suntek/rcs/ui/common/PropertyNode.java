package com.suntek.rcs.ui.common;

import android.content.ContentValues;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropertyNode implements Serializable {
    public ContentValues paramMap;
    public Set<String> paramMap_TYPE;
    public Set<String> propGroupSet;
    public String propName;
    public String propValue;
    public byte[] propValue_bytes;
    public List<String> propValue_vector;

    public PropertyNode() {
        this.propName = "";
        this.propValue = "";
        this.propValue_vector = new ArrayList();
        this.paramMap = new ContentValues();
        this.paramMap_TYPE = new HashSet();
        this.propGroupSet = new HashSet();
    }

    public PropertyNode(String propName, String propValue, List<String> propValue_vector, byte[] propValue_bytes, ContentValues paramMap, Set<String> paramMap_TYPE, Set<String> propGroupSet) {
        if (propName != null) {
            this.propName = propName;
        } else {
            this.propName = "";
        }
        if (propValue != null) {
            this.propValue = propValue;
        } else {
            this.propValue = "";
        }
        if (propValue_vector != null) {
            this.propValue_vector = propValue_vector;
        } else {
            this.propValue_vector = new ArrayList();
        }
        this.propValue_bytes = propValue_bytes;
        if (paramMap != null) {
            this.paramMap = paramMap;
        } else {
            this.paramMap = new ContentValues();
        }
        if (paramMap_TYPE != null) {
            this.paramMap_TYPE = paramMap_TYPE;
        } else {
            this.paramMap_TYPE = new HashSet();
        }
        if (propGroupSet != null) {
            this.propGroupSet = propGroupSet;
        } else {
            this.propGroupSet = new HashSet();
        }
    }

    public int hashCode() {
        throw new UnsupportedOperationException("PropertyNode does not provide hashCode() implementation intentionally.");
    }

    /* JADX WARNING: Missing block: B:7:0x001a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(Object obj) {
        boolean z = true;
        if (!(obj instanceof PropertyNode)) {
            return false;
        }
        PropertyNode node = (PropertyNode) obj;
        if (this.propName == null || (this.propName.equals(node.propName) ^ 1) != 0 || !this.paramMap_TYPE.equals(node.paramMap_TYPE) || !this.paramMap_TYPE.equals(node.paramMap_TYPE) || !this.propGroupSet.equals(node.propGroupSet)) {
            return false;
        }
        if (this.propValue_bytes != null && Arrays.equals(this.propValue_bytes, node.propValue_bytes)) {
            return true;
        }
        if (!this.propValue.equals(node.propValue)) {
            return false;
        }
        if (!(this.propValue_vector.equals(node.propValue_vector) || this.propValue_vector.size() == 1 || node.propValue_vector.size() == 1)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("propName: ");
        builder.append(this.propName);
        builder.append(", paramMap: ");
        builder.append(this.paramMap.toString());
        builder.append(", paramMap_TYPE: [");
        boolean first = true;
        for (String elem : this.paramMap_TYPE) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append('\"');
            builder.append(elem);
            builder.append('\"');
        }
        builder.append("]");
        if (!this.propGroupSet.isEmpty()) {
            builder.append(", propGroupSet: [");
            first = true;
            for (String elem2 : this.propGroupSet) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append('\"');
                builder.append(elem2);
                builder.append('\"');
            }
            builder.append("]");
        }
        if (this.propValue_vector != null && this.propValue_vector.size() > 1) {
            builder.append(", propValue_vector size: ");
            builder.append(this.propValue_vector.size());
        }
        if (this.propValue_bytes != null) {
            builder.append(", propValue_bytes size: ");
            builder.append(this.propValue_bytes.length);
        }
        builder.append(", propValue: \"");
        builder.append(this.propValue);
        builder.append("\"");
        return builder.toString();
    }
}
