package com.hacker.mercury.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.hacker.mercury.gateway.utils.WebFluxUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNullApi;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 自定义限流异常处理
 *
 * @author ruoyi
 */
@Slf4j
public class SentinelFallbackHandler implements WebExceptionHandler {

    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange) {
        return WebFluxUtils.webFluxResponseWriter(exchange.getResponse(), "请求超过最大数，请稍候再试");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        log.error("Sentinel异常:", throwable);
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(throwable);
        }
        if (!BlockException.isBlockException(throwable)) {
            return Mono.error(throwable);
        }
        return handleBlockedRequest(exchange, throwable).flatMap(response -> writeResponse(response, exchange));
    }

    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }
}
