package com.yupaits.docs.shiro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupaits.docs.config.JwtHelper;
import com.yupaits.docs.response.Result;
import com.yupaits.docs.response.ResultCode;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Jwt验证过滤器
 * Created by ts495 on 2017/9/9.
 */
public class StatelessAuthcFilter extends AccessControlFilter {

    private final ObjectMapper objectMapper;

    private final JwtHelper jwtHelper;

    public StatelessAuthcFilter(ObjectMapper objectMapper, JwtHelper jwtHelper) {
        this.objectMapper = objectMapper;
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = jwtHelper.getToken(request);
        String username = jwtHelper.getUsernameFromToken(token);
        StatelessToken accessToken = new StatelessToken(username, token);
        try {
            getSubject(servletRequest, servletResponse).login(accessToken);
        } catch (AuthenticationException e) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            objectMapper.writeValue(response.getWriter(), Result.fail(ResultCode.UNAUTHORIZED));
            return false;
        }
        getSubject(servletRequest, servletResponse).isPermitted(request.getRequestURI());
        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.fail(ResultCode.FORBIDDEN));
        return false;
    }

    @Override
    protected boolean preHandle(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 允许跨域访问
        if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        return super.preHandle(request, servletResponse);
    }
}
