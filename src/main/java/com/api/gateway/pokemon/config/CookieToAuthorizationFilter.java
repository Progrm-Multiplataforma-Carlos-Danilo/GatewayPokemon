package com.api.gateway.pokemon.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro global que repassa o cookie HttpOnly "access_token" aos serviços como
 * header "Authorization: Bearer <token>".
 *
 * Como todo GlobalFilter, roda depois do Spring Security: quem autentica o cookie
 * no próprio Gateway é o bearerTokenConverter do {@link RouteConfig}.
 *
 * Não altera a requisição nas rotas de {@link EntryPaths}, quando o header
 * Authorization já existe ou quando não há cookie.
 */
@Component
public class CookieToAuthorizationFilter implements GlobalFilter, Ordered {

    private static final String COOKIE_NAME = "access_token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Nas rotas de entrada, ou se já houver Authorization header, segue sem alteração.
        if (EntryPaths.matches(request) || request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return chain.filter(exchange);
        }

        // Tenta ler o token do cookie.
        HttpCookie cookie = request.getCookies().getFirst(COOKIE_NAME);
        if (cookie == null || cookie.getValue().isBlank()) {
            return chain.filter(exchange);
        }

        // Injeta o header Authorization com o valor do cookie.
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cookie.getValue())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * Prioridade máxima entre os filtros do Gateway: o header é definido antes do roteamento.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
