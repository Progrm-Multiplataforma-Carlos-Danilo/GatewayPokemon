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
 * Filtro global que converte o cookie HttpOnly "access_token" em um header
 * "Authorization: Bearer <token>" antes que o OAuth2 Resource Server valide
 * a requisição.
 *
 * Ordem de precedência:
 *   1. Se o header Authorization já estiver presente → não faz nada (Bearer
 *      enviado pelo frontend tem prioridade).
 *   2. Se não houver Authorization mas houver o cookie → injeta o header.
 *   3. Se nenhum dos dois → deixa passar (o Resource Server retornará 401).
 *
 * Isso permite autenticação híbrida: o frontend pode usar Bearer OU cookie,
 * e ambos funcionam no Gateway.
 */
@Component
public class CookieToAuthorizationFilter implements GlobalFilter, Ordered {

    private static final String COOKIE_NAME = "access_token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Se já possui Authorization header, segue sem alteração.
        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
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
     * Prioridade máxima: deve rodar ANTES do filtro de segurança OAuth2
     * (que tem ordem -1 no Spring Security reactive).
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
