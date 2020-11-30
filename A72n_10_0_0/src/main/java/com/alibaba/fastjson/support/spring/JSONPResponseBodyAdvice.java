package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.support.spring.annotation.ResponseJSONP;
import com.alibaba.fastjson.util.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class JSONPResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    public final Log logger = LogFactory.getLog(getClass());

    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return FastJsonHttpMessageConverter.class.isAssignableFrom(converterType) && (returnType.getContainingClass().isAnnotationPresent(ResponseJSONP.class) || returnType.hasMethodAnnotation(ResponseJSONP.class));
    }

    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> cls, ServerHttpRequest request, ServerHttpResponse response) {
        ResponseJSONP responseJsonp = (ResponseJSONP) returnType.getMethodAnnotation(ResponseJSONP.class);
        if (responseJsonp == null) {
            responseJsonp = (ResponseJSONP) returnType.getContainingClass().getAnnotation(ResponseJSONP.class);
        }
        String callbackMethodName = ((ServletServerHttpRequest) request).getServletRequest().getParameter(responseJsonp.callback());
        if (!IOUtils.isValidJsonpQueryParam(callbackMethodName)) {
            if (this.logger.isDebugEnabled()) {
                Log log = this.logger;
                log.debug("Invalid jsonp parameter value:" + callbackMethodName);
            }
            callbackMethodName = null;
        }
        JSONPObject jsonpObject = new JSONPObject(callbackMethodName);
        jsonpObject.addParameter(body);
        beforeBodyWriteInternal(jsonpObject, selectedContentType, returnType, request, response);
        return jsonpObject;
    }

    public void beforeBodyWriteInternal(JSONPObject jsonpObject, MediaType contentType, MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
    }

    /* access modifiers changed from: protected */
    public MediaType getContentType(MediaType contentType, ServerHttpRequest request, ServerHttpResponse response) {
        return FastJsonHttpMessageConverter.APPLICATION_JAVASCRIPT;
    }
}
