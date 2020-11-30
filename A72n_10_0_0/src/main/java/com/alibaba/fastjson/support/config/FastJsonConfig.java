package com.alibaba.fastjson.support.config;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;
import java.nio.charset.Charset;
import java.util.Map;

public class FastJsonConfig {
    private Charset charset = IOUtils.UTF8;
    private Map<Class<?>, SerializeFilter> classSerializeFilters;
    private String dateFormat;
    private Feature[] features = new Feature[0];
    private ParseProcess parseProcess;
    private ParserConfig parserConfig = ParserConfig.getGlobalInstance();
    private SerializeConfig serializeConfig = SerializeConfig.getGlobalInstance();
    private SerializeFilter[] serializeFilters = new SerializeFilter[0];
    private SerializerFeature[] serializerFeatures = {SerializerFeature.BrowserSecure};
    private boolean writeContentLength = true;

    public SerializeConfig getSerializeConfig() {
        return this.serializeConfig;
    }

    public void setSerializeConfig(SerializeConfig serializeConfig2) {
        this.serializeConfig = serializeConfig2;
    }

    public ParserConfig getParserConfig() {
        return this.parserConfig;
    }

    public void setParserConfig(ParserConfig parserConfig2) {
        this.parserConfig = parserConfig2;
    }

    public SerializerFeature[] getSerializerFeatures() {
        return this.serializerFeatures;
    }

    public void setSerializerFeatures(SerializerFeature... serializerFeatures2) {
        this.serializerFeatures = serializerFeatures2;
    }

    public SerializeFilter[] getSerializeFilters() {
        return this.serializeFilters;
    }

    public void setSerializeFilters(SerializeFilter... serializeFilters2) {
        this.serializeFilters = serializeFilters2;
    }

    public Feature[] getFeatures() {
        return this.features;
    }

    public void setFeatures(Feature... features2) {
        this.features = features2;
    }

    public Map<Class<?>, SerializeFilter> getClassSerializeFilters() {
        return this.classSerializeFilters;
    }

    public void setClassSerializeFilters(Map<Class<?>, SerializeFilter> classSerializeFilters2) {
        if (classSerializeFilters2 != null) {
            for (Map.Entry<Class<?>, SerializeFilter> entry : classSerializeFilters2.entrySet()) {
                this.serializeConfig.addFilter(entry.getKey(), entry.getValue());
            }
            this.classSerializeFilters = classSerializeFilters2;
        }
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public void setDateFormat(String dateFormat2) {
        this.dateFormat = dateFormat2;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public void setCharset(Charset charset2) {
        this.charset = charset2;
    }

    public boolean isWriteContentLength() {
        return this.writeContentLength;
    }

    public void setWriteContentLength(boolean writeContentLength2) {
        this.writeContentLength = writeContentLength2;
    }

    public ParseProcess getParseProcess() {
        return this.parseProcess;
    }

    public void setParseProcess(ParseProcess parseProcess2) {
        this.parseProcess = parseProcess2;
    }
}
