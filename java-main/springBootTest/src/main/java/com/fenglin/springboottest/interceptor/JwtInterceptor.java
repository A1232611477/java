package com.fenglin.springboottest.interceptor;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器：校验 Authorization 请求头中的 JWT
 *
 * <p>校验通过后把用户 id / 昵称写入 request attribute，Controller 用
 * {@code @RequestAttribute} 直接取，不用重复解析令牌。</p>
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /** 当前登录用户 id 的 request attribute 名 */
    public static final String ATTR_USER_ID = "loginUserId";

    /** 当前登录用户昵称的 request attribute 名 */
    public static final String ATTR_USER_NICKNAME = "loginUserNickname";

    private final JwtUtil jwtUtil;
    private final String header;
    private final String prefix;

    public JwtInterceptor(JwtUtil jwtUtil,
                          @Value("${jwt.header:Authorization}") String header,
                          @Value("${jwt.prefix:Bearer}") String prefix) {
        this.jwtUtil = jwtUtil;
        this.header = header;
        this.prefix = prefix.trim();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求不带令牌，直接放行（虽然 Spring 的预检处理通常不会走到拦截器）
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String raw = request.getHeader(header);
        if (raw == null || raw.isBlank()) {
            throw new BizException(401, "未登录，请先登录");
        }

        String token = raw.trim();
        if (!prefix.isEmpty() && token.regionMatches(true, 0, prefix, 0, prefix.length())) {
            token = token.substring(prefix.length()).trim();
        }
        if (token.isEmpty()) {
            throw new BizException(401, "未登录，请先登录");
        }

        Claims claims = jwtUtil.parse(token);
        request.setAttribute(ATTR_USER_ID, Long.valueOf(claims.getSubject()));
        request.setAttribute(ATTR_USER_NICKNAME, claims.get("nickname", String.class));
        return true;
    }
}
