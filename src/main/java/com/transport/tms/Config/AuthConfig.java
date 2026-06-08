package com.transport.tms.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration (proxyBeanMethods = false)
public class AuthConfig implements WebMvcConfigurer {

    @Autowired
    AuthRequestHeadersResolver authRequestHeadersResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authRequestHeadersResolver);
    }
}
