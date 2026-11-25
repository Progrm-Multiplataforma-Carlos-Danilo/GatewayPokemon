package com.api.gateway.pokemon.config;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Set;

/**
 * Rotas usadas para entrar ou sair da conta. Nelas o cookie "access_token" não é usado
 * como credencial, porque o navegador pode ainda guardar um token vencido ou inválido.
 */
final class EntryPaths {

    private static final Set<String> PATHS = Set.of(
            "/login/v1/auth",
            "/login/v1/create",
            "/v1/auth",
            "/v1/create",
            "/v1/logout",
            "/usuario/cadastrar"
    );

    private EntryPaths() {
    }

    static boolean matches(ServerHttpRequest request) {
        return PATHS.contains(request.getPath().pathWithinApplication().value());
    }
}
