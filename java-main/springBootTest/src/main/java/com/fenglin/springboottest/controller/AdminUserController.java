package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminUserVO;
import com.fenglin.springboottest.dto.PageData;
import com.fenglin.springboottest.dto.UserCreateForm;
import com.fenglin.springboottest.dto.UserUpdateForm;
import com.fenglin.springboottest.service.AdminUserService;
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 后台账号管理接口（需要管理员令牌，由 AdminInterceptor 校验角色）
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService service;

    /** 账号列表：分页 + 关键词 + 状态 + 角色过滤 */
    @GetMapping
    public Result<PageData<AdminUserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(service.list(keyword, status, role, page, size));
    }

    /** 账号概览统计 */
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        long[] s = service.stats();
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("total", s[0]);
        map.put("adminCount", s[1]);
        map.put("disabled", s[2]);
        return Result.success(map);
    }

    /** 新建账号 */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateForm form) {
        return Result.success(service.create(form));
    }

    /** 编辑账号（改昵称 / 角色 / 状态） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserUpdateForm form) {
        service.update(id, form);
        return Result.success();
    }

    /** 切换禁用 / 启用状态（status=1 禁用，0 启用） */
    @PatchMapping("/{id}/status")
    public Result<Void> setStatus(@PathVariable Long id, @RequestParam Integer status) {
        service.setStatus(id, status == null ? 0 : status);
        return Result.success();
    }

    /** 重置密码 */
    @PatchMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.resetPassword(id, body == null ? null : body.get("password"));
        return Result.success();
    }

    /** 删除账号 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.remove(id);
        return Result.success();
    }
}
