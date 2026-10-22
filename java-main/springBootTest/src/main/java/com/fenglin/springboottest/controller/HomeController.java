package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.HomeVO;
import com.fenglin.springboottest.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页展示接口（需要登录，由 JwtInterceptor 统一鉴权）
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /**
     * 获取首页数据：分级分类树 + 精选内容
     */
    @GetMapping
    public Result<HomeVO> home() {
        return Result.success(homeService.getHome());
    }
}
