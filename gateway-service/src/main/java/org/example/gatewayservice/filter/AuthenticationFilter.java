package org.example.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.example.gatewayservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redisTemplate;

    public AuthenticationFilter(JwtUtil jwtUtil, ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Authorization header is missing or invalid format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            Claims claims = jwtUtil.getClaims(token);
            String email = claims.getSubject();
            String redisKey = "active_token:" + email;

            return redisTemplate.opsForValue().get(redisKey)
                    .defaultIfEmpty("")
                    .flatMap(storedToken -> {
                        if (storedToken.isEmpty()) {
                            log.warn("No active token found in Redis for user: {}", email);
                            return onError(exchange, "Token not found. Please login again.", HttpStatus.UNAUTHORIZED);
                        }
                        if (!storedToken.equals(token)) {
                            log.warn("Token mismatch for user: {}. A newer token exists.", email);
                            return onError(exchange, "Token has been invalidated. Please login again.", HttpStatus.UNAUTHORIZED);
                        }
                        String role = claims.get("role", String.class);
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-User-Email", email)
                                .header("X-User-Role", role != null ? role : "")
                                .build();
                        log.info("Gateway authenticated request for user: {}", email);
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Gateway Auth Error: {}", err);
        return response.setComplete();
    }
}
