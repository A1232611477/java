package com.fenglin.springboottest.config;

import com.fenglin.springboottest.web.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置：小游戏「好友联机」的实时通信通道。
 *
 * <p>使用 STOMP over WebSocket，消息代理走「简单内存代理」（单机即可，无需外部 MQ）。
 * 前端通过 SockJS + STOMP 客户端连到 {@code /ws} 端点；房间广播统一发到 {@code /topic/room/{房间号}}。</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅的前缀：房间内广播（/topic/room/123456）+ 点对点回发（/user/queue/...）
        registry.enableSimpleBroker("/topic", "/queue");
        // convertAndSendToUser 时，客户端订阅地址为 /user/queue/...
        registry.setUserDestinationPrefix("/user");
        // 客户端向服务端发消息时的目标前缀（/app/game/...）
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS 兜底：浏览器不支持原生 WebSocket 时自动降级
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // 原生 WebSocket 端点：给原生客户端 / 现代浏览器直接用
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // CONNECT 帧鉴权：校验 JWT，失败则拒绝连接
        registration.interceptors(authInterceptor);
    }
}
