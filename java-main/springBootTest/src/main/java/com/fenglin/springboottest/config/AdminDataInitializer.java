package com.fenglin.springboottest.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时为库里植入一个内置管理员账号（admin / admin123）
 *
 * <p>仅当昵称 {@code admin} 不存在时才创建，因此脚本可重复启动、不会重复插入。
 * 角色为 {@code ADMIN}，可登录后台管理系统。普通注册用户角色为 USER。</p>
 */
@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public AdminDataInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        try {
            Long count = userMapper.selectCount(
                    Wrappers.lambdaQuery(User.class).eq(User::getNickname, "admin"));
            if (count != null && count > 0) {
                return;
            }
            User admin = new User();
            admin.setNickname("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setStatus(0);
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            log.info("已创建内置管理员账号：admin / admin123（角色 ADMIN）");
        } catch (Exception e) {
            // 容忍：若 t_user 尚无 role 列（未执行最新 init.sql），仅记录告警，不影响应用启动
            log.warn("内置管理员账号初始化失败（可先执行最新 sql/init.sql）：{}", e.getMessage());
        }
    }
}
