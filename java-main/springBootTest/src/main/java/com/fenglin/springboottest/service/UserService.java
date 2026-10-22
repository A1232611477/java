package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.dto.UserVO;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务：注册、登录、查询
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 注册新用户（REST 接口用，参数来自 {@link RegisterRequest}）
     */
    public void register(RegisterRequest req) {
        register(req.getNickname(), req.getPassword());
    }

    /**
     * 注册新用户（原始昵称 + 明文密码）
     *
     * <p>REST 接口和服务器端页面流程都走这里，保证两条链路的行为完全一致。
     * 昵称会先做 trim，避免「同名但带空格」的重复账号。</p>
     *
     * @return 落库后的用户实体（已带自增 id）
     */
    public User register(String nickname, String rawPassword) {
        String name = nickname == null ? "" : nickname.trim();
        // 1. 昵称是否已注册
        Long count = userMapper.selectCount(
                Wrappers.lambdaQuery(User.class).eq(User::getNickname, name));
        if (count != null && count > 0) {
            throw new BizException(409, "该昵称已被注册");
        }
        // 2. 构建用户并加密密码
        User user = new User();
        user.setNickname(name);
        user.setPassword(encoder.encode(rawPassword));
        user.setStatus(0);
        userMapper.insert(user);
        return user;
    }

    /**
     * 校验账号密码（不签发令牌）
     *
     * <p>服务器端页面流程用它做「登录校验 + 存 Session」。</p>
     *
     * @return 校验通过的完整用户实体
     * @throws BizException 401 账号或密码错误；403 账号被禁用
     */
    public User authenticate(String nickname, String rawPassword) {
        String name = nickname == null ? "" : nickname.trim();
        User user = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class).eq(User::getNickname, name));

        // 账号不存在与密码错误返回同一句提示，避免被用来枚举账号是否存在
        if (user == null || !encoder.matches(rawPassword, user.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BizException(403, "该账号已被禁用，请联系管理员");
        }
        return user;
    }

    /**
     * 登录：校验账号密码并签发 JWT
     *
     * <p>勾选「记住我」签发长效令牌，两种档位的有效期都在响应的 {@code expiresIn}
     * 里原样返回，前端据此展示。</p>
     */
    public LoginResponse login(LoginRequest req) {
        User user = authenticate(req.getNickname(), req.getPassword());
        boolean remember = req.isRememberMe();

        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generate(user.getId(), user.getNickname(), remember));
        resp.setTokenType("Bearer");
        resp.setExpiresIn(jwtUtil.getExpireSeconds(remember));
        resp.setUser(UserVO.from(user));
        return resp;
    }

    /**
     * 按 id 查用户信息（不含密码）
     */
    public UserVO getUserVO(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "用户不存在或已被删除，请重新登录");
        }
        return UserVO.from(user);
    }
}
