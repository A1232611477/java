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
 * 后台管理接口鉴权拦截器（在 {@link JwtInterceptor} 校验令牌之后执行）
 *
 * <p>从已解析的 JWT 中读取 {@code role} 声明，仅允许 {@code ADMIN} 角色访问
 * {@code /api/admin/**}（登录接口 {@code /api/admin/login} 除外，由白名单放行）。</p>
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    /** 当前管理员 id 的 request attribute 名 */
    public static final String ATTR_ADMIN_ID = "adminUserId";

    /** 当前管理员昵称的 request attribute 名 */
    public static final String ATTR_ADMIN_NICKNAME = "adminUserNickname";

    private final JwtUtil jwtUtil;
    private final String header;
    private final String prefix;

    public AdminInterceptor(JwtUtil jwtUtil,
                           @Value("${jwt.header:Authorization}") String header,
                           @Value("${jwt.prefix:Bearer}") String prefix) {
        this.jwtUtil = jwtUtil;
        this.header = header;
        this.prefix = prefix.trim();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求不带令牌，直接放行
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

        // 解析令牌并校验管理员角色
        Claims claims = jwtUtil.parse(token);
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限：需要管理员身份");
        }

        request.setAttribute(ATTR_ADMIN_ID, Long.valueOf(claims.getSubject()));
        request.setAttribute(ATTR_ADMIN_NICKNAME, claims.get("nickname", String.class));
        return true;
    }
}
