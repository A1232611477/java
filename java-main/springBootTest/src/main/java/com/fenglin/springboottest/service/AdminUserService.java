package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.AdminUserVO;
import com.fenglin.springboottest.dto.PageData;
import com.fenglin.springboottest.dto.UserCreateForm;
import com.fenglin.springboottest.dto.UserUpdateForm;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台账号管理业务：账号列表、禁用/启用、改角色、重置密码、删除、新建
 */
@Service
public class AdminUserService {

    /** 内置超级管理员昵称，禁止被禁用/删除/降级 */
    private static final String ROOT_ADMIN = "admin";

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 账号列表（分页 + 关键词 + 状态 + 角色过滤） */
    public PageData<AdminUserVO> list(String keyword, Integer status, String role, int page, int size) {
        int pg = Math.max(page, 1);
        int sz = Math.max(size, 1);

        LambdaQueryWrapper<User> w = Wrappers.lambdaQuery(User.class);
        if (keyword != null && !keyword.isBlank()) {
            w.like(User::getNickname, keyword.trim());
        }
        if (status != null) {
            w.eq(User::getStatus, status);
        }
        if (role != null && !role.isBlank()) {
            w.eq(User::getRole, role.trim().toUpperCase());
        }
        w.orderByAsc(User::getId);

        long total = userMapper.selectCount(w);
        int from = (pg - 1) * sz;
        w.last("LIMIT " + from + ", " + sz);
        List<User> records = userMapper.selectList(w);

        List<AdminUserVO> list = records.stream().map(AdminUserVO::from).collect(Collectors.toList());
        return new PageData<>(list, total, pg, sz);
    }

    /** 新建账号 */
    public Long create(UserCreateForm form) {
        String name = form.getNickname() == null ? "" : form.getNickname().trim();
        if (name.isEmpty()) {
            throw new BizException(400, "昵称不能为空");
        }
        Long count = userMapper.selectCount(
                Wrappers.lambdaQuery(User.class).eq(User::getNickname, name));
        if (count != null && count > 0) {
            throw new BizException(409, "该昵称已被注册");
        }

        User user = new User();
        user.setNickname(name);
        user.setPassword(encoder.encode(form.getPassword()));
        user.setRole(normalizeRole(form.getRole()));
        user.setStatus(form.getStatus() != null && form.getStatus() == 1 ? 1 : 0);
        userMapper.insert(user);
        return user.getId();
    }

    /** 编辑账号（仅覆盖传入的非空字段） */
    public void update(Long id, UserUpdateForm form) {
        User user = requireUser(id);
        assertNotRoot(user, "内置管理员账号不允许修改角色/状态/昵称");

        if (form.getNickname() != null && !form.getNickname().isBlank()) {
            String name = form.getNickname().trim();
            Long count = userMapper.selectCount(
                    Wrappers.lambdaQuery(User.class)
                            .eq(User::getNickname, name)
                            .ne(User::getId, id));
            if (count != null && count > 0) {
                throw new BizException(409, "该昵称已被注册");
            }
            user.setNickname(name);
        }
        if (form.getRole() != null && !form.getRole().isBlank()) {
            user.setRole(normalizeRole(form.getRole()));
        }
        if (form.getStatus() != null) {
            user.setStatus(form.getStatus() == 1 ? 1 : 0);
        }
        userMapper.updateById(user);
    }

    /** 切换禁用 / 启用状态 */
    public void setStatus(Long id, int status) {
        User user = requireUser(id);
        assertNotRoot(user, "内置管理员账号不允许禁用");
        user.setStatus(status == 1 ? 1 : 0);
        userMapper.updateById(user);
    }

    /** 重置密码 */
    public void resetPassword(Long id, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BizException(400, "新密码不能为空");
        }
        if (rawPassword.length() < 8 || rawPassword.length() > 20) {
            throw new BizException(400, "密码需 8-20 位");
        }
        User user = requireUser(id);
        user.setPassword(encoder.encode(rawPassword));
        userMapper.updateById(user);
    }

    /** 删除账号 */
    public void remove(Long id) {
        User user = requireUser(id);
        assertNotRoot(user, "内置管理员账号不允许删除");
        userMapper.deleteById(id);
    }

    /** 统计各类账号数量，供概览展示 */
    public long[] stats() {
        long total = userMapper.selectCount(null);
        long adminCount = userMapper.selectCount(
                Wrappers.lambdaQuery(User.class).eq(User::getRole, "ADMIN"));
        long disabled = userMapper.selectCount(
                Wrappers.lambdaQuery(User.class).eq(User::getStatus, 1));
        return new long[]{total, adminCount, disabled};
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "账号不存在");
        }
        return user;
    }

    private void assertNotRoot(User user, String message) {
        if (ROOT_ADMIN.equals(user.getNickname())) {
            throw new BizException(403, message);
        }
    }

    /** 角色归一化：仅接受 USER / ADMIN，其它一律按 USER 处理 */
    private String normalizeRole(String role) {
        if (role != null && "ADMIN".equals(role.trim().toUpperCase())) {
            return "ADMIN";
        }
        return "USER";
    }
}
