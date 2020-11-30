package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateFormatSerializer implements ObjectSerializer {
    private final String pattern;

    public SimpleDateFormatSerializer(String pattern2) {
        this.pattern = pattern2;
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            serializer.out.writeNull();
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat(this.pattern, serializer.locale);
        format.setTimeZone(serializer.timeZone);
        serializer.write(format.format((Date) object));
    }
}
