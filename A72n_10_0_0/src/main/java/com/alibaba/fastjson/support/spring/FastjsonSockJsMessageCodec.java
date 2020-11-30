package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.socket.sockjs.frame.AbstractSockJsMessageCodec;

public class FastjsonSockJsMessageCodec extends AbstractSockJsMessageCodec {
    public String[] decode(String content) throws IOException {
        return (String[]) JSON.parseObject(content, String[].class);
    }

    public String[] decodeInputStream(InputStream content) throws IOException {
        return (String[]) JSON.parseObject(content, String[].class, new Feature[0]);
    }

    /* access modifiers changed from: protected */
    public char[] applyJsonQuoting(String content) {
        SerializeWriter out = new SerializeWriter();
        try {
            new JSONSerializer(out).write(content);
            return out.toCharArrayForSpringWebSocket();
        } finally {
            out.close();
        }
    }
}
