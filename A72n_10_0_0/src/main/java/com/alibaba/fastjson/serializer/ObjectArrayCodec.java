package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class ObjectArrayCodec implements ObjectDeserializer, ObjectSerializer {
    public static final ObjectArrayCodec instance = new ObjectArrayCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public final void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        char c;
        Class<?> preClazz;
        SerializeWriter out = serializer.out;
        Object[] array = (Object[]) object;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        int size = array.length;
        int end = size - 1;
        if (end == -1) {
            out.append((CharSequence) "[]");
            return;
        }
        SerialContext context = serializer.context;
        int i = 0;
        serializer.setContext(context, object, fieldName, 0);
        try {
            out.append('[');
            char c2 = ',';
            if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                serializer.incrementIndent();
                serializer.println();
                while (i < size) {
                    if (i != 0) {
                        out.write(44);
                        serializer.println();
                    }
                    serializer.write(array[i]);
                    i++;
                }
                serializer.decrementIdent();
                serializer.println();
                out.write(93);
                return;
            }
            Class<?> preClazz2 = null;
            ObjectSerializer preWriter = null;
            while (i < end) {
                Object item = array[i];
                if (item == null) {
                    out.append((CharSequence) "null,");
                    c = c2;
                } else {
                    if (serializer.containsReference(item)) {
                        serializer.writeReference(item);
                        preClazz = preClazz2;
                        c = c2;
                    } else {
                        Class<?> clazz = item.getClass();
                        if (clazz == preClazz2) {
                            preClazz = preClazz2;
                            c = c2;
                            preWriter.write(serializer, item, Integer.valueOf(i), null, 0);
                        } else {
                            c = c2;
                            preClazz = clazz;
                            ObjectSerializer preWriter2 = serializer.getObjectWriter(clazz);
                            preWriter2.write(serializer, item, Integer.valueOf(i), null, 0);
                            preWriter = preWriter2;
                        }
                    }
                    out.append(c);
                    preClazz2 = preClazz;
                }
                i++;
                c2 = c;
            }
            Object item2 = array[end];
            if (item2 == null) {
                out.append((CharSequence) "null]");
            } else {
                if (serializer.containsReference(item2)) {
                    serializer.writeReference(item2);
                } else {
                    serializer.writeWithFieldName(item2, Integer.valueOf(end));
                }
                out.append(']');
            }
            serializer.context = context;
        } finally {
            serializer.context = context;
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:43:0x0068 */
    /* JADX WARN: Type inference failed for: r6v2, types: [T, byte[]] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Type componentType;
        Class<?> cls;
        Class<?> cls2;
        Class<?> cls3;
        Class<?> cls4;
        JSONLexer lexer = parser.lexer;
        int token = lexer.token();
        if (token == 8) {
            lexer.nextToken(16);
            return null;
        } else if (token == 4 || token == 26) {
            ?? r6 = (T) lexer.bytesValue();
            lexer.nextToken(16);
            if (r6.length != 0 || type == byte[].class) {
                return r6;
            }
            return null;
        } else {
            if (type instanceof GenericArrayType) {
                componentType = ((GenericArrayType) type).getGenericComponentType();
                if (componentType instanceof TypeVariable) {
                    TypeVariable typeVar = (TypeVariable) componentType;
                    Type objType = parser.getContext().type;
                    if (objType instanceof ParameterizedType) {
                        ParameterizedType objParamType = (ParameterizedType) objType;
                        Type objRawType = objParamType.getRawType();
                        Type actualType = null;
                        if (objRawType instanceof Class) {
                            TypeVariable[] objTypeParams = ((Class) objRawType).getTypeParameters();
                            for (int i = 0; i < objTypeParams.length; i++) {
                                if (objTypeParams[i].getName().equals(typeVar.getName())) {
                                    actualType = objParamType.getActualTypeArguments()[i];
                                }
                            }
                        }
                        if (actualType instanceof Class) {
                            cls4 = (Class) actualType;
                        } else {
                            cls4 = Object.class;
                        }
                        cls3 = cls4;
                    } else {
                        cls3 = TypeUtils.getClass(typeVar.getBounds()[0]);
                    }
                    cls2 = cls3;
                } else {
                    cls2 = TypeUtils.getClass(componentType);
                }
                cls = cls2;
            } else {
                componentType = ((Class) type).getComponentType();
                cls = componentType;
            }
            JSONArray array = new JSONArray();
            parser.parseArray(componentType, array, fieldName);
            return (T) toObjectArray(parser, cls, array);
        }
    }

    private <T> T toObjectArray(DefaultJSONParser parser, Class<?> componentType, JSONArray array) {
        Object element;
        if (array == null) {
            return null;
        }
        int size = array.size();
        T t = (T) Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            Object value = array.get(i);
            if (value == array) {
                Array.set(t, i, t);
            } else if (componentType.isArray()) {
                if (componentType.isInstance(value)) {
                    element = value;
                } else {
                    element = toObjectArray(parser, componentType, (JSONArray) value);
                }
                Array.set(t, i, element);
            } else {
                Object element2 = null;
                if (value instanceof JSONArray) {
                    JSONArray valueArray = (JSONArray) value;
                    int valueArraySize = valueArray.size();
                    boolean contains = false;
                    for (int y = 0; y < valueArraySize; y++) {
                        if (valueArray.get(y) == array) {
                            valueArray.set(i, t);
                            contains = true;
                        }
                    }
                    if (contains) {
                        element2 = valueArray.toArray();
                    }
                }
                if (element2 == null) {
                    element2 = TypeUtils.cast(value, (Class<Object>) componentType, parser.getConfig());
                }
                Array.set(t, i, element2);
            }
        }
        array.setRelatedArray(t);
        array.setComponentType(componentType);
        return t;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 14;
    }
}
