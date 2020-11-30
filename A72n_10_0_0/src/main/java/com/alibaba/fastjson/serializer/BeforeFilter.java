package com.alibaba.fastjson.serializer;

public abstract class BeforeFilter implements SerializeFilter {
    private static final Character COMMA = ',';
    private static final ThreadLocal<Character> seperatorLocal = new ThreadLocal<>();
    private static final ThreadLocal<JSONSerializer> serializerLocal = new ThreadLocal<>();

    public abstract void writeBefore(Object obj);

    /* access modifiers changed from: package-private */
    public final char writeBefore(JSONSerializer serializer, Object object, char seperator) {
        serializerLocal.set(serializer);
        seperatorLocal.set(Character.valueOf(seperator));
        writeBefore(object);
        serializerLocal.set(null);
        return seperatorLocal.get().charValue();
    }

    /* access modifiers changed from: protected */
    public final void writeKeyValue(String key, Object value) {
        char seperator = seperatorLocal.get().charValue();
        serializerLocal.get().writeKeyValue(seperator, key, value);
        if (seperator != ',') {
            seperatorLocal.set(COMMA);
        }
    }
}
