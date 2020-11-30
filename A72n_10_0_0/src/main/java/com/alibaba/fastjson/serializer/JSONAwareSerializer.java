package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONAware;
import java.io.IOException;
import java.lang.reflect.Type;

public class JSONAwareSerializer implements ObjectSerializer {
    public static JSONAwareSerializer instance = new JSONAwareSerializer();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
        } else {
            out.write(((JSONAware) object).toJSONString());
        }
    }
}
