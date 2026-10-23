package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminStatsVO;
import com.fenglin.springboottest.dto.CategoryOptionVO;
import com.fenglin.springboottest.dto.ContentForm;
import com.fenglin.springboottest.dto.PageData;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.service.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台内容管理接口（需要管理员令牌，由 AdminInterceptor 校验角色）
 */
@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

    @Autowired
    private AdminContentService service;

    /** 内容列表：分页 + 关键词 + 分类 + 状态过滤 */
    @GetMapping
    public Result<PageData<Content>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(service.list(keyword, categoryId, status, page, size));
    }

    /** 内容详情 */
    @GetMapping("/{id}")
    public Result<Content> get(@PathVariable Long id) {
        return Result.success(service.get(id));
    }

    /** 新增内容 */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ContentForm form) {
        return Result.success(service.create(form));
    }

    /** 编辑内容 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ContentForm form) {
        service.update(id, form);
        return Result.success();
    }

    /** 删除内容 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.remove(id);
        return Result.success();
    }

    /** 切换发布 / 下架状态（status=1 发布，0 下架） */
    @PatchMapping("/{id}/status")
    public Result<Void> setStatus(@PathVariable Long id, @RequestParam Integer status) {
        service.setStatus(id, status == null ? 0 : status);
        return Result.success();
    }

    /** 概览统计 */
    @GetMapping("/stats/overview")
    public Result<AdminStatsVO> stats() {
        return Result.success(service.stats());
    }

    /** 内容表单用分类选项 */
    @GetMapping("/categories/options")
    public Result<List<CategoryOptionVO>> categories() {
        return Result.success(service.categoryOptions());
    }
}
