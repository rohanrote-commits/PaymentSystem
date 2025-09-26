package com.payment.interceptor;

import com.payment.helper.RequestUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@Scope(
        value = WebApplicationContext.SCOPE_REQUEST,
        proxyMode = ScopedProxyMode.TARGET_CLASS
)
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    RequestUserContext requestUserContext;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String username = request.getHeader("User-Name"); // header name
        if (username != null && !username.isEmpty()) {
            requestUserContext.setUsername(username);
            log.info("Username is set to " + username);
        }else{
            log.info("Username is empty");
        }
        return true;
    }
}
