package com.fenglin.springboottest.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 服务器端页面的登录态拦截器（和 REST 接口的 JWT 那套互不影响）
 *
 * <p>页面流程用的是传统 Session：登录成功把用户 id 写进 session，
 * 这里发现没登录就把请求 **重定向** 到登录页，并把原本要访问的地址
 * 通过 {@code ?redirect=} 带上，登录成功后原路跳回。</p>
 */
@Component
public class PageAuthInterceptor implements HandlerInterceptor {

    /** session 中保存「当前登录用户 id」的键 */
    public static final String SESSION_USER_ID = "pageLoginUserId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SESSION_USER_ID);
        if (userId != null) {
            return true;
        }

        // 拼接原本想访问的地址（含查询串），登录后跳回来
        StringBuilder target = new StringBuilder(request.getRequestURI());
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            target.append('?').append(query);
        }

        String encoded = URLEncoder.encode(target.toString(), StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + encoded);
        return false;
    }
}
