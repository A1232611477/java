package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
import com.fenglin.springboottest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理员认证：登录（白名单，无需令牌）
 *
 * <p>仅角色为 {@code ADMIN} 的账号可登录后台；普通用户登录会被拒绝（403）。
 * 登录成功返回携带角色声明的 JWT，后续访问 {@code /api/admin/**} 由
 * {@code AdminInterceptor} 校验角色。</p>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    /**
     * 管理员登录：POST /api/admin/login { nickname, password, rememberMe }
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(userService.adminLogin(req));
    }
}
