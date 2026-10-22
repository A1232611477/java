package com.fenglin.springboottest.util;

import com.fenglin.springboottest.common.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发 / 解析令牌（HS256 对称签名）
 *
 * <p>载荷约定：sub = 用户 id，nickname = 昵称。令牌无状态，服务端不保存会话，
 * 因此「退出登录」由前端清除本地令牌即可。</p>
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;
    private final long rememberExpireMillis;

    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expire-minutes:120}") long expireMinutes,
                   @Value("${jwt.remember-expire-minutes:10080}") long rememberExpireMinutes) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            // HS256 要求密钥不少于 256 位（32 字节），配置不合格直接启动失败，避免线上用弱密钥
            throw new IllegalStateException("jwt.secret 未配置或长度不足 32 字节，请检查 application.properties");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMinutes * 60_000L;
        // 勾选「记住我」时的长效令牌有效期，默认 10080 分钟 = 7 天
        this.rememberExpireMillis = rememberExpireMinutes * 60_000L;
    }

    /** 签发令牌（普通有效期） */
    public String generate(Long userId, String nickname) {
        return generate(userId, nickname, false);
    }

    /**
     * 签发令牌
     *
     * @param rememberMe 前端勾选「记住我」时传 true，签发长效令牌
     */
    public String generate(Long userId, String nickname, boolean rememberMe) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("nickname", nickname)
                .claim("rm", rememberMe)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMillis(rememberMe)))
                // 显式指定 HS256：不写的话 jjwt 会按密钥长度自动选更强的算法，行为不好预测
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** 按是否勾选「记住我」取对应的有效期毫秒数 */
    public long ttlMillis(boolean rememberMe) {
        return rememberMe ? rememberExpireMillis : expireMillis;
    }

    /** 令牌有效期（秒），返回给前端便于展示 / 提前续期 */
    public long getExpireSeconds() {
        return expireMillis / 1000;
    }

    /** 按是否勾选「记住我」取有效期（秒），登录接口直接塞进响应 */
    public long getExpireSeconds(boolean rememberMe) {
        return ttlMillis(rememberMe) / 1000;
    }

    /**
     * 解析并校验令牌；过期、签名不对、格式错误统一抛 401
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(401, "登录状态已失效，请重新登录");
        }
    }

    /** 从令牌中取用户 id */
    public Long getUserId(String token) {
        try {
            return Long.valueOf(parse(token).getSubject());
        } catch (NumberFormatException e) {
            throw new BizException(401, "登录状态已失效，请重新登录");
        }
    }
}
