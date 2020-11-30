package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;

public class ClobSeriliazer implements ObjectSerializer {
    public static final ClobSeriliazer instance = new ClobSeriliazer();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            try {
                serializer.writeNull();
            } catch (SQLException e) {
                throw new IOException("write clob error", e);
            }
        } else {
            Reader reader = ((Clob) object).getCharacterStream();
            StringBuilder buf = new StringBuilder();
            try {
                char[] chars = new char[2048];
                while (true) {
                    int len = reader.read(chars, 0, chars.length);
                    if (len < 0) {
                        String text = buf.toString();
                        reader.close();
                        serializer.write(text);
                        return;
                    }
                    buf.append(chars, 0, len);
                }
            } catch (Exception ex) {
                throw new JSONException("read string from reader error", ex);
            }
        }
    }
}
