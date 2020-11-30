package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.lang.reflect.Type;

public class PropertyProcessableDeserializer implements ObjectDeserializer {
    public final Class<PropertyProcessable> type;

    public PropertyProcessableDeserializer(Class<PropertyProcessable> type2) {
        this.type = type2;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type2, Object fieldName) {
        try {
            return (T) parser.parse(this.type.newInstance(), fieldName);
        } catch (Exception e) {
            throw new JSONException("craete instance error");
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 12;
    }
}
