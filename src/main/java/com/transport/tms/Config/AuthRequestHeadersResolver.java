package com.transport.tms.Config;

import com.transport.tms.GlobalException.UNAuthorizedException;
import com.transport.tms.UserManagement.Dto.AccessTokenVO;
import com.transport.tms.UserManagement.Dto.UserVO;
import com.transport.tms.UserManagement.Service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.WebUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

//@Slf4j
//@Component
//public class AuthRequestHeadersResolver implements HandlerMethodArgumentResolver {
//
//    @Autowired
//    private TokenService tokenService;
//
//    @Override
//    public boolean supportsParameter(MethodParameter parameter) {
//        Method method = parameter.getMethod();
//        if (method != null && method.isAnnotationPresent(Anonymous.class)) {
//            return false;
//        }
//        return parameter.getParameterType().equals(AccessTokenVO.class);
//    }
//
//    @Override
//    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//        if(parameter.getParameterType().equals(AccessTokenVO.class)) {
//            HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
//            final Cookie cookie = WebUtils.getCookie(servletRequest, "token");
//            if(Objects.nonNull(cookie)) {
//                return this.getAccessTokenVO(cookie);
//            } else {
//                throw new UNAuthorizedException(HttpStatus.UNAUTHORIZED.value(), "Token missing");
//            }
//        }
//        return new AccessTokenVO();
//    }
//
//    private AccessTokenVO getAccessTokenVO(Cookie cookie) {
//        try {
//            Claims claims = tokenService.decodeAccessToken(cookie.getValue());
//            AccessTokenVO accessTokenVO = new AccessTokenVO();
//            accessTokenVO.setAccessToken(cookie.getValue());
//            UserVO userVO = new UserVO();
//            if(Objects.nonNull(claims.get("username"))) {
//                userVO.setXusrname(claims.get("username").toString());
//            }
//            if(Objects.nonNull(claims.get("authorities"))) {
//                List<String> permissions = (List) claims.get("authorities");
//                accessTokenVO.setPermissions(permissions);
//            }
//            return accessTokenVO;
//        }catch (Exception e) {
//            log.error("User Unauthorized", e.getMessage());
//        }
//        throw new UNAuthorizedException(HttpStatus.UNAUTHORIZED.value(), "User is UNAuthorized");
//    }
//}

@Component
public class AuthRequestHeadersResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // ✅ Only apply resolver if AccessTokenVO is used
        return parameter.getParameterType().equals(AccessTokenVO.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Method method = parameter.getMethod();

        // ✅ VERY IMPORTANT: Skip login API
        if (method != null && method.isAnnotationPresent(Anonymous.class)) {
            return null;
        }

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Cookie cookie = WebUtils.getCookie(request, "token");

        if (cookie != null) {
            return getAccessTokenVO(cookie);
        }

        // ❌ Only throw for secured APIs
        throw new UNAuthorizedException(HttpStatus.UNAUTHORIZED.value(), "Token missing");
    }

    private AccessTokenVO getAccessTokenVO(Cookie cookie) {
        try {
            Claims claims = tokenService.decodeAccessToken(cookie.getValue());

            AccessTokenVO accessTokenVO = new AccessTokenVO();
            accessTokenVO.setAccessToken(cookie.getValue());

            if (claims.get("authorities") != null) {
                accessTokenVO.setPermissions((List<String>) claims.get("authorities"));
            }

            return accessTokenVO;

        } catch (Exception e) {
            throw new UNAuthorizedException(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
        }
    }
}
