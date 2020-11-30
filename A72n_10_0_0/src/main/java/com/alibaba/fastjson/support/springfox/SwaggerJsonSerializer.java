package com.alibaba.fastjson.support.springfox;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import java.io.IOException;
import java.lang.reflect.Type;
import springfox.documentation.spring.web.json.Json;

public class SwaggerJsonSerializer implements ObjectSerializer {
    public static final SwaggerJsonSerializer instance = new SwaggerJsonSerializer();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        serializer.getWriter().write(((Json) object).value());
    }
}
