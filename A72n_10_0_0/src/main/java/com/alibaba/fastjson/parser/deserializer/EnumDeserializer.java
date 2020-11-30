package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EnumDeserializer implements ObjectDeserializer {
    protected final Class<?> enumClass;
    protected long[] enumNameHashCodes;
    protected final Enum[] enums;
    protected final Enum[] ordinalEnums;

    /* JADX INFO: Multiple debug info for r7v7 long: [D('alterNameHash' long), D('name' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v32 int: [D('ch' char), D('j' int)] */
    public EnumDeserializer(Class<?> enumClass2) {
        String jsonFieldName;
        Class<?> cls = enumClass2;
        this.enumClass = cls;
        this.ordinalEnums = (Enum[]) enumClass2.getEnumConstants();
        Map<Long, Enum> enumMap = new HashMap<>();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= this.ordinalEnums.length) {
                break;
            }
            Enum e = this.ordinalEnums[i2];
            String name = e.name();
            JSONField jsonField = null;
            try {
                jsonField = (JSONField) cls.getField(name).getAnnotation(JSONField.class);
                if (!(jsonField == null || (jsonFieldName = jsonField.name()) == null || jsonFieldName.length() <= 0)) {
                    name = jsonFieldName;
                }
            } catch (Exception e2) {
            }
            long hash = -3750763034362895579L;
            long hash_lower = -3750763034362895579L;
            int j = 0;
            while (j < name.length()) {
                char ch = name.charAt(j);
                long hash2 = ((long) ch) ^ hash;
                hash_lower = (((long) ((ch < 'A' || ch > 'Z') ? ch : ch + ' ')) ^ hash_lower) * 1099511628211L;
                j++;
                hash = hash2 * 1099511628211L;
                i2 = i2;
            }
            enumMap.put(Long.valueOf(hash), e);
            if (hash != hash_lower) {
                enumMap.put(Long.valueOf(hash_lower), e);
            }
            if (jsonField != null) {
                String[] alternateNames = jsonField.alternateNames();
                int length = alternateNames.length;
                int i3 = 0;
                while (i3 < length) {
                    String alterName = alternateNames[i3];
                    long alterNameHash = -3750763034362895579L;
                    int j2 = 0;
                    while (j2 < alterName.length()) {
                        alterNameHash = (((long) alterName.charAt(j2)) ^ alterNameHash) * 1099511628211L;
                        j2++;
                        alternateNames = alternateNames;
                        name = name;
                        jsonField = jsonField;
                    }
                    if (!(alterNameHash == hash || alterNameHash == hash_lower)) {
                        enumMap.put(Long.valueOf(alterNameHash), e);
                    }
                    i3++;
                    alternateNames = alternateNames;
                    name = name;
                    jsonField = jsonField;
                }
            }
            i = i2 + 1;
            cls = enumClass2;
        }
        this.enumNameHashCodes = new long[enumMap.size()];
        int i4 = 0;
        for (Long h : enumMap.keySet()) {
            this.enumNameHashCodes[i4] = h.longValue();
            i4++;
        }
        Arrays.sort(this.enumNameHashCodes);
        this.enums = new Enum[this.enumNameHashCodes.length];
        for (int i5 = 0; i5 < this.enumNameHashCodes.length; i5++) {
            this.enums[i5] = enumMap.get(Long.valueOf(this.enumNameHashCodes[i5]));
        }
    }

    public Enum getEnumByHashCode(long hashCode) {
        int enumIndex;
        if (this.enums != null && (enumIndex = Arrays.binarySearch(this.enumNameHashCodes, hashCode)) >= 0) {
            return this.enums[enumIndex];
        }
        return null;
    }

    public Enum<?> valueOf(int ordinal) {
        return this.ordinalEnums[ordinal];
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        try {
            JSONLexer lexer = parser.lexer;
            int token = lexer.token();
            if (token == 2) {
                int intValue = lexer.intValue();
                lexer.nextToken(16);
                if (intValue >= 0 && intValue <= this.ordinalEnums.length) {
                    return (T) this.ordinalEnums[intValue];
                }
                throw new JSONException("parse enum " + this.enumClass.getName() + " error, value : " + intValue);
            } else if (token == 4) {
                String name = lexer.stringVal();
                lexer.nextToken(16);
                if (name.length() == 0) {
                    return null;
                }
                long hash = -3750763034362895579L;
                long hash_lower = -3750763034362895579L;
                for (int j = 0; j < name.length(); j++) {
                    char ch = name.charAt(j);
                    hash = (hash ^ ((long) ch)) * 1099511628211L;
                    hash_lower = (hash_lower ^ ((long) ((ch < 'A' || ch > 'Z') ? ch : ch + ' '))) * 1099511628211L;
                }
                Enum e = (T) getEnumByHashCode(hash);
                if (e == null && hash_lower != hash) {
                    e = (T) getEnumByHashCode(hash_lower);
                }
                if (e == null) {
                    if (lexer.isEnabled(Feature.ErrorOnEnumNotMatch)) {
                        throw new JSONException("not match enum value, " + this.enumClass.getName() + " : " + name);
                    }
                }
                return (T) e;
            } else if (token == 8) {
                lexer.nextToken(16);
                return null;
            } else {
                Object value = parser.parse();
                throw new JSONException("parse enum " + this.enumClass.getName() + " error, value : " + value);
            }
        } catch (JSONException e2) {
            throw e2;
        } catch (Exception e3) {
            throw new JSONException(e3.getMessage(), e3);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
