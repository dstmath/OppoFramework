package com.alibaba.fastjson.support.spring;

import java.io.IOException;
import java.lang.reflect.Type;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

@Deprecated
public class FastJsonpHttpMessageConverter4 extends FastJsonHttpMessageConverter {
    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
    public boolean supports(Class<?> clazz) {
        return super.supports(clazz);
    }

    @Override // com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return super.canRead(type, contextClass, mediaType);
    }

    @Override // com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return super.canWrite(type, clazz, mediaType);
    }

    @Override // com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return super.read(type, contextClass, inputMessage);
    }

    @Override // com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
    public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        super.write(o, type, contentType, outputMessage);
    }

    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
    public Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return super.readInternal(clazz, inputMessage);
    }

    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
    public void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        super.writeInternal(object, outputMessage);
    }
}
