package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public final class ListSerializer implements ObjectSerializer {
    public static final ListSerializer instance = new ListSerializer();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public final void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerialContext context;
        Throwable th;
        Object item;
        Iterator<?> it;
        char c;
        SerializeWriter out;
        char c2;
        List<?> list;
        Object obj = object;
        boolean writeClassName = serializer.out.isEnabled(SerializerFeature.WriteClassName) || SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName);
        SerializeWriter out2 = serializer.out;
        Type elementType = null;
        if (writeClassName) {
            elementType = TypeUtils.getCollectionItemType(fieldType);
        }
        if (obj == null) {
            out2.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        List<?> list2 = (List) obj;
        if (list2.size() == 0) {
            out2.append((CharSequence) "[]");
            return;
        }
        SerialContext context2 = serializer.context;
        Object obj2 = fieldName;
        serializer.setContext(context2, obj, obj2, 0);
        ObjectSerializer itemSerializer = null;
        try {
            char c3 = ']';
            char c4 = ',';
            if (out2.isEnabled(SerializerFeature.PrettyFormat)) {
                out2.append('[');
                serializer.incrementIndent();
                int i = 0;
                Iterator<?> it2 = list2.iterator();
                while (it2.hasNext()) {
                    Object item2 = it2.next();
                    if (i != 0) {
                        try {
                            out2.append(c4);
                        } catch (Throwable th2) {
                            th = th2;
                            context = context2;
                        }
                    }
                    serializer.println();
                    if (item2 == null) {
                        c2 = c3;
                        list = list2;
                        it = it2;
                        c = c4;
                        context = context2;
                        out = out2;
                        serializer.out.writeNull();
                    } else if (serializer.containsReference(item2)) {
                        serializer.writeReference(item2);
                        c2 = c3;
                        list = list2;
                        it = it2;
                        c = c4;
                        context = context2;
                        out = out2;
                    } else {
                        itemSerializer = serializer.getObjectWriter(item2.getClass());
                        it = it2;
                        c = c4;
                        serializer.context = new SerialContext(context2, obj, obj2, 0, 0);
                        c2 = c3;
                        context = context2;
                        list = list2;
                        out = out2;
                        try {
                            itemSerializer.write(serializer, item2, Integer.valueOf(i), elementType, features);
                        } catch (Throwable th3) {
                            th = th3;
                            serializer.context = context;
                            throw th;
                        }
                    }
                    i++;
                    obj2 = fieldName;
                    list2 = list;
                    c3 = c2;
                    context2 = context;
                    out2 = out;
                    c4 = c;
                    it2 = it;
                    obj = object;
                }
                serializer.decrementIdent();
                serializer.println();
                out2.append(c3);
                serializer.context = context2;
                return;
            }
            context = context2;
            char c5 = ',';
            out2.append('[');
            int i2 = 0;
            int size = list2.size();
            while (i2 < size) {
                try {
                    Object item3 = list2.get(i2);
                    if (i2 != 0) {
                        out2.append(c5);
                    }
                    if (item3 == null) {
                        out2.append((CharSequence) "null");
                    } else {
                        Class<?> clazz = item3.getClass();
                        if (clazz == Integer.class) {
                            out2.writeInt(((Integer) item3).intValue());
                        } else if (clazz == Long.class) {
                            long val = ((Long) item3).longValue();
                            if (writeClassName) {
                                out2.writeLong(val);
                                out2.write(76);
                            } else {
                                out2.writeLong(val);
                            }
                        } else if ((SerializerFeature.DisableCircularReferenceDetect.mask & features) != 0) {
                            serializer.getObjectWriter(item3.getClass()).write(serializer, item3, Integer.valueOf(i2), elementType, features);
                        } else {
                            if (!out2.disableCircularReferenceDetect) {
                                item = item3;
                                serializer.context = new SerialContext(context, object, fieldName, 0, 0);
                            } else {
                                item = item3;
                            }
                            if (serializer.containsReference(item)) {
                                serializer.writeReference(item);
                            } else {
                                ObjectSerializer itemSerializer2 = serializer.getObjectWriter(item.getClass());
                                if ((SerializerFeature.WriteClassName.mask & features) == 0 || !(itemSerializer2 instanceof JavaBeanSerializer)) {
                                    itemSerializer2.write(serializer, item, Integer.valueOf(i2), elementType, features);
                                } else {
                                    ((JavaBeanSerializer) itemSerializer2).writeNoneASM(serializer, item, Integer.valueOf(i2), elementType, features);
                                }
                            }
                        }
                    }
                    i2++;
                    size = size;
                    c5 = ',';
                } catch (Throwable th4) {
                    th = th4;
                    serializer.context = context;
                    throw th;
                }
            }
            out2.append(']');
            serializer.context = context;
        } catch (Throwable th5) {
            th = th5;
            context = context2;
            serializer.context = context;
            throw th;
        }
    }
}
