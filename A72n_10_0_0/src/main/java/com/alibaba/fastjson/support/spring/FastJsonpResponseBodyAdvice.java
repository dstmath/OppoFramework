package com.alibaba.fastjson.support.spring;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Deprecated
@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class FastJsonpResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private static final Pattern CALLBACK_PARAM_PATTERN = Pattern.compile("[0-9A-Za-z_\\.]*");
    public static final String[] DEFAULT_JSONP_QUERY_PARAM_NAMES = {"callback", "jsonp"};
    private final String[] jsonpQueryParamNames;

    public FastJsonpResponseBodyAdvice() {
        this.jsonpQueryParamNames = DEFAULT_JSONP_QUERY_PARAM_NAMES;
    }

    public FastJsonpResponseBodyAdvice(String... queryParamNames) {
        Assert.isTrue(!ObjectUtils.isEmpty(queryParamNames), "At least one query param name is required");
        this.jsonpQueryParamNames = queryParamNames;
    }

    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return FastJsonHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> cls, ServerHttpRequest request, ServerHttpResponse response) {
        MappingFastJsonValue container = getOrCreateContainer(body);
        beforeBodyWriteInternal(container, selectedContentType, returnType, request, response);
        return container;
    }

    /* access modifiers changed from: protected */
    public MappingFastJsonValue getOrCreateContainer(Object body) {
        return body instanceof MappingFastJsonValue ? (MappingFastJsonValue) body : new MappingFastJsonValue(body);
    }

    public void beforeBodyWriteInternal(MappingFastJsonValue bodyContainer, MediaType contentType, MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        for (String name : this.jsonpQueryParamNames) {
            String value = servletRequest.getParameter(name);
            if (value != null && isValidJsonpQueryParam(value)) {
                bodyContainer.setJsonpFunction(value);
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isValidJsonpQueryParam(String value) {
        return CALLBACK_PARAM_PATTERN.matcher(value).matches();
    }

    /* access modifiers changed from: protected */
    public MediaType getContentType(MediaType contentType, ServerHttpRequest request, ServerHttpResponse response) {
        return new MediaType("application", "javascript");
    }
}
