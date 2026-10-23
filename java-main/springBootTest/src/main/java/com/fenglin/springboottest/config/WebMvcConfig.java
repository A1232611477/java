package com.fenglin.springboottest.config;

import com.fenglin.springboottest.interceptor.AdminInterceptor;
import com.fenglin.springboottest.interceptor.JwtInterceptor;
import com.fenglin.springboottest.web.PageAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册两套互不相干的鉴权拦截器
 *
 * <ol>
 *   <li>{@link JwtInterceptor}（REST 接口）—— 拦 {@code /api/**}，校验请求头里的 JWT；
 *       注册、登录属于白名单，不需要令牌。</li>
 *   <li>{@link PageAuthInterceptor}（服务器端页面）—— 拦 {@code /home}，校验 session；
 *       未登录会被 <b>重定向</b> 到 {@code /login?redirect=...}。</li>
 * </ol>
 *
 * <p>前端 Vue 那套走的是第 1 套（无状态 JWT）；后端自带的 Thymeleaf 页面走第 2 套（有状态 Session）。
 * 新增公开接口记得加进 excludePathPatterns，否则会被拦。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final PageAuthInterceptor pageAuthInterceptor;
    private final AdminInterceptor adminInterceptor;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, PageAuthInterceptor pageAuthInterceptor,
                        AdminInterceptor adminInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.pageAuthInterceptor = pageAuthInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/register",
                        "/api/auth/login",
                        // 后台管理员登录是白名单，不需要携带用户 JWT
                        "/api/admin/login"
                );

        // 后台管理接口：在 JWT 校验通过后，再校验角色必须为 ADMIN
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");

        // 服务器端页面：只有用户主页需要登录，未登录会被重定向到 /login?redirect=...
        registry.addInterceptor(pageAuthInterceptor)
                .addPathPatterns("/home");
    }
}
