package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.SearchResultVO;
import com.fenglin.springboottest.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搜索接口（需要登录，由 JwtInterceptor 统一鉴权）
 *
 * <p>支持分级分类搜索：categoryId 按一级分类过滤，subCategoryId 按二级分类过滤，
 * 二者可单独使用，也可组合；q 为关键词（标题/摘要/标签）。</p>
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private HomeService homeService;

    @GetMapping
    public Result<SearchResultVO> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(homeService.search(q, categoryId, subCategoryId, page, size));
    }
}
