package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class OptionalCodec implements ObjectDeserializer, ObjectSerializer {
    public static OptionalCodec instance = new OptionalCodec();

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        if (type == OptionalInt.class) {
            Integer value = TypeUtils.castToInt(parser.parseObject((Class<Object>) Integer.class));
            return value == null ? (T) OptionalInt.empty() : (T) OptionalInt.of(value.intValue());
        } else if (type == OptionalLong.class) {
            Long value2 = TypeUtils.castToLong(parser.parseObject((Class<Object>) Long.class));
            return value2 == null ? (T) OptionalLong.empty() : (T) OptionalLong.of(value2.longValue());
        } else if (type == OptionalDouble.class) {
            Double value3 = TypeUtils.castToDouble(parser.parseObject((Class<Object>) Double.class));
            return value3 == null ? (T) OptionalDouble.empty() : (T) OptionalDouble.of(value3.doubleValue());
        } else {
            Object value4 = parser.parseObject(TypeUtils.unwrapOptional(type));
            return value4 == null ? (T) Optional.empty() : (T) Optional.of(value4);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 12;
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            serializer.writeNull();
        } else if (object instanceof Optional) {
            Optional<?> optional = (Optional) object;
            serializer.write(optional.isPresent() ? optional.get() : null);
        } else if (object instanceof OptionalDouble) {
            OptionalDouble optional2 = (OptionalDouble) object;
            if (optional2.isPresent()) {
                serializer.write(Double.valueOf(optional2.getAsDouble()));
            } else {
                serializer.writeNull();
            }
        } else if (object instanceof OptionalInt) {
            OptionalInt optional3 = (OptionalInt) object;
            if (optional3.isPresent()) {
                serializer.out.writeInt(optional3.getAsInt());
            } else {
                serializer.writeNull();
            }
        } else if (object instanceof OptionalLong) {
            OptionalLong optional4 = (OptionalLong) object;
            if (optional4.isPresent()) {
                serializer.out.writeLong(optional4.getAsLong());
            } else {
                serializer.writeNull();
            }
        } else {
            throw new JSONException("not support optional : " + object.getClass());
        }
    }
}
