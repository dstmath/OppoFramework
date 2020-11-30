package com.alibaba.fastjson.support.spring.messaging;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import java.nio.charset.Charset;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

public class MappingFastJsonMessageConverter extends AbstractMessageConverter {
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }

    public void setFastJsonConfig(FastJsonConfig fastJsonConfig2) {
        this.fastJsonConfig = fastJsonConfig2;
    }

    public MappingFastJsonMessageConverter() {
        super(new MimeType("application", "json", Charset.forName("UTF-8")));
    }

    /* access modifiers changed from: protected */
    public boolean supports(Class<?> cls) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean canConvertFrom(Message<?> message, Class<?> targetClass) {
        return supports(targetClass);
    }

    /* access modifiers changed from: protected */
    public boolean canConvertTo(Object payload, MessageHeaders headers) {
        return supports(payload.getClass());
    }

    /* access modifiers changed from: protected */
    public Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        Object payload = message.getPayload();
        if (payload instanceof byte[]) {
            return JSON.parseObject((byte[]) payload, this.fastJsonConfig.getCharset(), targetClass, this.fastJsonConfig.getParserConfig(), this.fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, this.fastJsonConfig.getFeatures());
        }
        if (payload instanceof String) {
            return JSON.parseObject((String) payload, targetClass, this.fastJsonConfig.getParserConfig(), this.fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, this.fastJsonConfig.getFeatures());
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        if (byte[].class == getSerializedPayloadClass()) {
            if (!(payload instanceof String) || !JSON.isValid((String) payload)) {
                return JSON.toJSONBytes(this.fastJsonConfig.getCharset(), payload, this.fastJsonConfig.getSerializeConfig(), this.fastJsonConfig.getSerializeFilters(), this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, this.fastJsonConfig.getSerializerFeatures());
            }
            return ((String) payload).getBytes(this.fastJsonConfig.getCharset());
        } else if (!(payload instanceof String) || !JSON.isValid((String) payload)) {
            return JSON.toJSONString(payload, this.fastJsonConfig.getSerializeConfig(), this.fastJsonConfig.getSerializeFilters(), this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, this.fastJsonConfig.getSerializerFeatures());
        } else {
            return payload;
        }
    }
}
