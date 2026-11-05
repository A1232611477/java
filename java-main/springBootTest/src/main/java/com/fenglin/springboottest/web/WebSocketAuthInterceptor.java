package com.fenglin.springboottest.web;

import com.fenglin.springboottest.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WebSocket 握手鉴权：从 STOMP CONNECT 帧的 native headers 里取 {@code Authorization}
 * 校验 JWT，通过后把 userId / nickname 塞进 session attributes，供消息处理器读取。
 *
 * <p>注意：WebSocket 握手不走 Spring MVC 的 {@code JwtInterceptor}，所以这里单独校验一次。
 * 前端连接时在 {@code connectHeaders} 里带上 {@code Authorization: Bearer <token>} 即可。</p>
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = firstNativeHeader(accessor, "Authorization");
            if (auth == null || auth.isBlank()) {
                throw new IllegalArgumentException("未登录，无法联机");
            }
            String token = auth.trim();
            String prefix = "Bearer";
            if (token.regionMatches(true, 0, prefix, 0, prefix.length())) {
                token = token.substring(prefix.length()).trim();
            }
            Claims claims = jwtUtil.parse(token); // 校验失败会抛 BizException(401)
            Long userId = Long.valueOf(claims.getSubject());
            String nickname = claims.get("nickname", String.class);
            accessor.getSessionAttributes().put("userId", userId);
            accessor.getSessionAttributes().put("nickname", nickname);
            // 设置 Principal（name = userId），使 convertAndSendToUser 能按用户回发消息
            accessor.setUser(new java.security.Principal() {
                @Override
                public String getName() { return String.valueOf(userId); }
            });
        }
        return message;
    }

    private String firstNativeHeader(StompHeaderAccessor accessor, String name) {
        Map<String, Object> nativeHeaders = accessor.getMessageHeaders().get("nativeHeaders", Map.class);
        if (nativeHeaders == null) return null;
        Object v = nativeHeaders.get(name);
        if (v == null) return null;
        if (v instanceof java.util.List<?> list && !list.isEmpty()) return String.valueOf(list.get(0));
        return String.valueOf(v);
    }
}
