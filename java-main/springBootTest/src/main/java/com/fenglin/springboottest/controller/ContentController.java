package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.ContentDetailVO;
import com.fenglin.springboottest.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容详情接口（需要登录，由 JwtInterceptor 统一鉴权）
 */
@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private HomeService homeService;

    /**
     * 内容详情（含正文 content 与视频地址），供详情弹窗展示
     */
    @GetMapping("/{id}")
    public Result<ContentDetailVO> detail(@PathVariable Long id) {
        ContentDetailVO vo = homeService.getDetail(id);
        if (vo == null) {
            return Result.error(404, "内容不存在或未发布");
        }
        return Result.success(vo);
    }
}
