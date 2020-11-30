package com.alibaba.fastjson.serializer;

public class SerialContext {
    public final int features;
    public final Object fieldName;
    public final Object object;
    public final SerialContext parent;

    public SerialContext(SerialContext parent2, Object object2, Object fieldName2, int features2, int fieldFeatures) {
        this.parent = parent2;
        this.object = object2;
        this.fieldName = fieldName2;
        this.features = features2;
    }

    public String toString() {
        if (this.parent == null) {
            return "$";
        }
        StringBuilder buf = new StringBuilder();
        toString(buf);
        return buf.toString();
    }

    /* access modifiers changed from: protected */
    public void toString(StringBuilder buf) {
        if (this.parent == null) {
            buf.append('$');
            return;
        }
        this.parent.toString(buf);
        if (this.fieldName == null) {
            buf.append(".null");
        } else if (this.fieldName instanceof Integer) {
            buf.append('[');
            buf.append(((Integer) this.fieldName).intValue());
            buf.append(']');
        } else {
            buf.append('.');
            String fieldName2 = this.fieldName.toString();
            boolean special = false;
            int i = 0;
            while (true) {
                if (i >= fieldName2.length()) {
                    break;
                }
                char ch = fieldName2.charAt(i);
                if ((ch < '0' || ch > '9') && ((ch < 'A' || ch > 'Z') && ((ch < 'a' || ch > 'z') && ch <= 128))) {
                    special = true;
                    break;
                }
                i++;
            }
            if (special) {
                for (int i2 = 0; i2 < fieldName2.length(); i2++) {
                    char ch2 = fieldName2.charAt(i2);
                    if (ch2 == '\\') {
                        buf.append('\\');
                        buf.append('\\');
                        buf.append('\\');
                    } else if ((ch2 < '0' || ch2 > '9') && ((ch2 < 'A' || ch2 > 'Z') && ((ch2 < 'a' || ch2 > 'z') && ch2 <= 128))) {
                        buf.append('\\');
                        buf.append('\\');
                    } else {
                        buf.append(ch2);
                    }
                    buf.append(ch2);
                }
                return;
            }
            buf.append(fieldName2);
        }
    }

    public SerialContext getParent() {
        return this.parent;
    }

    public Object getObject() {
        return this.object;
    }

    public Object getFieldName() {
        return this.fieldName;
    }

    public String getPath() {
        return toString();
    }
}
