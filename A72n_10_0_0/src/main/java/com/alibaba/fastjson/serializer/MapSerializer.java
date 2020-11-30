package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MapSerializer extends SerializeFilterable implements ObjectSerializer {
    private static final int NON_STRINGKEY_AS_STRING = SerializerFeature.of(new SerializerFeature[]{SerializerFeature.BrowserCompatible, SerializerFeature.WriteNonStringKeyAsString, SerializerFeature.BrowserSecure});
    public static MapSerializer instance = new MapSerializer();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, object, fieldName, fieldType, features, false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:110:0x019a, code lost:
        if ((r5 instanceof java.lang.Number) == false) goto L_0x01b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x01a4, code lost:
        if (apply(r32, r33, com.alibaba.fastjson.JSON.toJSONString(r5), r1) != false) goto L_0x01b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x01b1, code lost:
        if (apply(r32, r33, (java.lang.String) r5, r1) == false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00ec, code lost:
        if ((r5 instanceof java.lang.Number) == false) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f6, code lost:
        if (applyName(r32, r33, com.alibaba.fastjson.JSON.toJSONString(r5)) != false) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0102, code lost:
        if (applyName(r32, r33, (java.lang.String) r5) == false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0105, code lost:
        r12 = r6;
        r28 = r14;
        r14 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x012a, code lost:
        if ((r5 instanceof java.lang.Number) == false) goto L_0x0143;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0134, code lost:
        if (applyName(r32, r33, com.alibaba.fastjson.JSON.toJSONString(r5)) != false) goto L_0x0143;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0140, code lost:
        if (applyName(r32, r33, (java.lang.String) r5) == false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0162, code lost:
        if ((r5 instanceof java.lang.Number) == false) goto L_0x017b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x016c, code lost:
        if (apply(r32, r33, com.alibaba.fastjson.JSON.toJSONString(r5), r1) != false) goto L_0x017b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0178, code lost:
        if (apply(r32, r33, (java.lang.String) r5, r1) == false) goto L_0x0105;
     */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0224  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x0266 A[Catch:{ all -> 0x034f }] */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x0297 A[Catch:{ all -> 0x034f }] */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x02af A[Catch:{ all -> 0x034f }] */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x02d4 A[Catch:{ all -> 0x034f }] */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x02e2 A[Catch:{ all -> 0x034f }] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b  */
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features, boolean unwrapped) throws IOException {
        Map<?, ?> map;
        SerialContext parent;
        Throwable th;
        Class<?> preClazz;
        int mapSortFieldMask;
        Class<?> preClazz2;
        SerialContext parent2;
        Object entryKey;
        Object value;
        ObjectSerializer preWriter;
        ObjectSerializer preWriter2;
        Type valueType;
        Object obj;
        Object value2;
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        Map<?, ?> map2 = (Map) object;
        int mapSortFieldMask2 = SerializerFeature.MapSortField.mask;
        if (!((out.features & mapSortFieldMask2) == 0 && (features & mapSortFieldMask2) == 0)) {
            if (map2 instanceof JSONObject) {
                map2 = ((JSONObject) map2).getInnerMap();
            }
            if (!(map2 instanceof SortedMap) && !(map2 instanceof LinkedHashMap)) {
                try {
                    map2 = new TreeMap<>((Map<? extends Object, ? extends Object>) map2);
                } catch (Exception e) {
                }
            }
            map = map2;
            if (!serializer.containsReference(object)) {
                serializer.writeReference(object);
                return;
            }
            SerialContext parent3 = serializer.context;
            serializer.setContext(parent3, object, fieldName, 0);
            if (!unwrapped) {
                try {
                    out.write(123);
                } catch (Throwable th2) {
                    th = th2;
                    parent = parent3;
                }
            }
            try {
                serializer.incrementIndent();
                boolean first = true;
                if (out.isEnabled(SerializerFeature.WriteClassName)) {
                    String typeKey = serializer.config.typeKey;
                    Class<?> mapClass = map.getClass();
                    preClazz = null;
                    if (!((mapClass == JSONObject.class || mapClass == HashMap.class || mapClass == LinkedHashMap.class) && map.containsKey(typeKey))) {
                        out.writeFieldName(typeKey);
                        out.writeString(object.getClass().getName());
                        first = false;
                    }
                } else {
                    preClazz = null;
                }
                Iterator<Map.Entry<?, ?>> it = map.entrySet().iterator();
                ObjectSerializer preWriter3 = null;
                boolean first2 = first;
                Class<?> preClazz3 = preClazz;
                while (it.hasNext()) {
                    try {
                        Map.Entry entry = it.next();
                        Object value3 = entry.getValue();
                        Object entryKey2 = entry.getKey();
                        List<PropertyPreFilter> preFilters = serializer.propertyPreFilters;
                        if (preFilters != null && preFilters.size() > 0) {
                            if (entryKey2 != null) {
                                if (!(entryKey2 instanceof String)) {
                                    if (!entryKey2.getClass().isPrimitive()) {
                                    }
                                }
                            }
                        }
                        List<PropertyPreFilter> preFilters2 = this.propertyPreFilters;
                        if (preFilters2 != null && preFilters2.size() > 0) {
                            if (entryKey2 != null) {
                                if (!(entryKey2 instanceof String)) {
                                    if (!entryKey2.getClass().isPrimitive()) {
                                    }
                                }
                            }
                        }
                        List<PropertyFilter> propertyFilters = serializer.propertyFilters;
                        if (propertyFilters != null && propertyFilters.size() > 0) {
                            if (entryKey2 != null) {
                                if (!(entryKey2 instanceof String)) {
                                    if (!entryKey2.getClass().isPrimitive()) {
                                    }
                                }
                            }
                        }
                        List<PropertyFilter> propertyFilters2 = this.propertyFilters;
                        if (propertyFilters2 != null && propertyFilters2.size() > 0) {
                            if (entryKey2 != null) {
                                if (!(entryKey2 instanceof String)) {
                                    if (!entryKey2.getClass().isPrimitive()) {
                                    }
                                }
                            }
                        }
                        List<NameFilter> nameFilters = serializer.nameFilters;
                        if (nameFilters != null && nameFilters.size() > 0) {
                            if (entryKey2 != null) {
                                if (!(entryKey2 instanceof String)) {
                                    if (entryKey2.getClass().isPrimitive() || (entryKey2 instanceof Number)) {
                                        entryKey2 = processKey(serializer, object, JSON.toJSONString(entryKey2), value3);
                                    }
                                }
                            }
                            entryKey2 = processKey(serializer, object, (String) entryKey2, value3);
                        }
                        List<NameFilter> nameFilters2 = this.nameFilters;
                        if (nameFilters2 != null && nameFilters2.size() > 0) {
                            if (entryKey2 != null) {
                                if (!(entryKey2 instanceof String)) {
                                    if (entryKey2.getClass().isPrimitive() || (entryKey2 instanceof Number)) {
                                        entryKey = processKey(serializer, object, JSON.toJSONString(entryKey2), value3);
                                        if (entryKey == null) {
                                            value2 = value3;
                                            parent2 = parent3;
                                            mapSortFieldMask = mapSortFieldMask2;
                                            preClazz2 = preClazz3;
                                        } else if (entryKey instanceof String) {
                                            value2 = value3;
                                            parent2 = parent3;
                                            mapSortFieldMask = mapSortFieldMask2;
                                            preClazz2 = preClazz3;
                                        } else {
                                            if (!((entryKey instanceof Map) || (entryKey instanceof Collection))) {
                                                mapSortFieldMask = mapSortFieldMask2;
                                                preClazz2 = preClazz3;
                                                parent2 = parent3;
                                                try {
                                                    obj = processValue(serializer, null, object, JSON.toJSONString(entryKey), value3);
                                                    value = obj;
                                                    if (value != null || out.isEnabled(SerializerFeature.WriteMapNullValue)) {
                                                        if (entryKey instanceof String) {
                                                            String key = (String) entryKey;
                                                            if (!first2) {
                                                                out.write(44);
                                                            }
                                                            if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                                                                serializer.println();
                                                            }
                                                            out.writeFieldName(key, true);
                                                        } else {
                                                            if (!first2) {
                                                                out.write(44);
                                                            }
                                                            if (!out.isEnabled(NON_STRINGKEY_AS_STRING) || (entryKey instanceof Enum)) {
                                                                serializer.write(entryKey);
                                                            } else {
                                                                serializer.write(JSON.toJSONString(entryKey));
                                                            }
                                                            out.write(58);
                                                        }
                                                        first2 = false;
                                                        if (value == null) {
                                                            out.writeNull();
                                                            parent3 = parent2;
                                                            preClazz3 = preClazz2;
                                                        } else {
                                                            Class<?> clazz = value.getClass();
                                                            if (clazz != preClazz2) {
                                                                preClazz2 = clazz;
                                                                preWriter = serializer.getObjectWriter(clazz);
                                                            } else {
                                                                preWriter = preWriter3;
                                                            }
                                                            if (!SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName) || !(preWriter instanceof JavaBeanSerializer)) {
                                                                preWriter2 = preWriter;
                                                                preWriter2.write(serializer, value, entryKey, null, features);
                                                            } else {
                                                                if (fieldType instanceof ParameterizedType) {
                                                                    Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
                                                                    valueType = null;
                                                                    if (actualTypeArguments.length == 2) {
                                                                        valueType = actualTypeArguments[1];
                                                                    }
                                                                } else {
                                                                    valueType = null;
                                                                }
                                                                preWriter2 = preWriter;
                                                                ((JavaBeanSerializer) preWriter).writeNoneASM(serializer, value, entryKey, valueType, features);
                                                            }
                                                            parent3 = parent2;
                                                            preClazz3 = preClazz2;
                                                            preWriter3 = preWriter2;
                                                        }
                                                        it = it;
                                                        mapSortFieldMask2 = mapSortFieldMask;
                                                    }
                                                    parent3 = parent2;
                                                    preClazz3 = preClazz2;
                                                    it = it;
                                                    mapSortFieldMask2 = mapSortFieldMask;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    parent = parent2;
                                                    serializer.context = parent;
                                                    throw th;
                                                }
                                            } else {
                                                parent2 = parent3;
                                                mapSortFieldMask = mapSortFieldMask2;
                                                preClazz2 = preClazz3;
                                                value = value3;
                                                if (entryKey instanceof String) {
                                                }
                                                first2 = false;
                                                if (value == null) {
                                                }
                                                it = it;
                                                mapSortFieldMask2 = mapSortFieldMask;
                                            }
                                        }
                                        obj = processValue(serializer, null, object, (String) entryKey, value2);
                                        value = obj;
                                        if (entryKey instanceof String) {
                                        }
                                        first2 = false;
                                        if (value == null) {
                                        }
                                        it = it;
                                        mapSortFieldMask2 = mapSortFieldMask;
                                    }
                                }
                            }
                            entryKey = processKey(serializer, object, (String) entryKey2, value3);
                            if (entryKey == null) {
                            }
                            obj = processValue(serializer, null, object, (String) entryKey, value2);
                            value = obj;
                            if (entryKey instanceof String) {
                            }
                            first2 = false;
                            if (value == null) {
                            }
                            it = it;
                            mapSortFieldMask2 = mapSortFieldMask;
                        }
                        entryKey = entryKey2;
                        if (entryKey == null) {
                        }
                        obj = processValue(serializer, null, object, (String) entryKey, value2);
                        value = obj;
                        if (entryKey instanceof String) {
                        }
                        first2 = false;
                        if (value == null) {
                        }
                        it = it;
                        mapSortFieldMask2 = mapSortFieldMask;
                    } catch (Throwable th4) {
                        th = th4;
                        parent = parent3;
                        serializer.context = parent;
                        throw th;
                    }
                }
                serializer.context = parent3;
                serializer.decrementIdent();
                if (out.isEnabled(SerializerFeature.PrettyFormat) && map.size() > 0) {
                    serializer.println();
                }
                if (!unwrapped) {
                    out.write(125);
                    return;
                }
                return;
            } catch (Throwable th5) {
                th = th5;
                parent = parent3;
                serializer.context = parent;
                throw th;
            }
        }
        map = map2;
        if (!serializer.containsReference(object)) {
        }
    }
}
