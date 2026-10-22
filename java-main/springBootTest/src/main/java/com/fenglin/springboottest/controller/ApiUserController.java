package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.UserVO;
import com.fenglin.springboottest.interceptor.JwtInterceptor;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心接口（需要登录，由 JwtInterceptor 统一鉴权）
 */
@RestController
@RequestMapping("/api/user")
public class ApiUserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户信息
     *
     * <p>userId 由拦截器解析令牌后写入 request attribute，这里直接取。</p>
     */
    @GetMapping("/me")
    public Result<UserVO> me(@RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        return Result.success(userService.getUserVO(userId));
    }
}
