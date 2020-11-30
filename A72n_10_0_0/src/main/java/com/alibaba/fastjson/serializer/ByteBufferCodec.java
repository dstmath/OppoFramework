package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public class ByteBufferCodec implements ObjectDeserializer, ObjectSerializer {
    public static final ByteBufferCodec instance = new ByteBufferCodec();

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) ((ByteBufferBean) parser.parseObject((Class<Object>) ByteBufferBean.class)).byteBuffer();
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 14;
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        ByteBuffer byteBuf = (ByteBuffer) object;
        byte[] array = byteBuf.array();
        SerializeWriter out = serializer.out;
        out.write(123);
        out.writeFieldName("array");
        out.writeByteArray(array);
        out.writeFieldValue(',', "limit", byteBuf.limit());
        out.writeFieldValue(',', "position", byteBuf.position());
        out.write(125);
    }

    public static class ByteBufferBean {
        public byte[] array;
        public int limit;
        public int position;

        public ByteBuffer byteBuffer() {
            ByteBuffer buf = ByteBuffer.wrap(this.array);
            buf.limit(this.limit);
            buf.position(this.position);
            return buf;
        }
    }
}
