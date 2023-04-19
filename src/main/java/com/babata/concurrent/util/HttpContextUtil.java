package com.babata.concurrent.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: zqj
 */
public class HttpContextUtil {

    public static HttpServletResponse getResponse(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

}
